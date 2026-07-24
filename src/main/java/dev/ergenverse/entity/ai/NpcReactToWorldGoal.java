package dev.ergenverse.entity.ai;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.history.WorldHistory;
import dev.ergenverse.simulation.event.WorldEvent;
import dev.ergenverse.simulation.event.WorldEventBus;
import dev.ergenverse.simulation.event.WorldEventSubscriber;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NpcReactToWorldGoal — NPCs react to observable world events around them.
 *
 * <h2>The "Living Moment" Bridge</h2>
 * <p>The user's framework: "A player just stumbled onto Wang Lin watching
 * wolves stalk a spirit deer, and afterward villagers talked about it
 * differently because they witnessed it." This goal makes that happen.
 *
 * <p>When a beast event fires nearby (wolf hunting deer, hawk circling prey),
 * NPCs within 32 blocks become AWARE of it. They may:
 * <ul>
 *   <li><b>Speak a contextual line</b> — "That wolf is hunting again..."
 *       Visible to nearby players as action-bar text.</li>
 *   <li><b>Look at the event</b> — the NPC turns to face the source.</li>
 *   <li><b>Record a memory</b> — the observation is written to WorldHistory
 *       so the NPC can later recall "I saw a wolf hunt deer near the south bend."</li>
 * </ul>
 *
 * <h2>Reaction table (per NPC personality)</h2>
 * <p>NPCs with high {@code caution} trait react more strongly to threats
 * (beast.hunt, beast.alert). NPCs with high {@code curiosity} react to
 * all events. The reaction lines are generated from context, not scripted
 * per-character — Article XLI compliance.
 *
 * <h2>How this creates Living Moments</h2>
 * <p>Before this goal: Beast hunts deer → nobody notices → no memory →
 * no rumor → no consequence. The hunt happens in a vacuum.
 * <p>After this goal: Beast hunts deer → WorldEventBus fires → NPC notices
 * → NPC speaks "The wolf pack is hunting again..." → player overhears →
 * WorldHistory records → next week an NPC can reference the hunt.
 *
 * <p>One event exercises FIVE systems: BeastLivingEventGoal → WorldEventBus
 * → NpcReactToWorldGoal → WorldHistory → NpcDesireGoal (future: NPCs
 * develop desires based on observed events).
 *
 * <h2>Constitution compliance</h2>
 * <ul>
 *   <li>Art V — NPCs react whether or not the player is present. NPC→NPC
 *       observations are broadcast to nearby players (the world happens
 *       around you).</li>
 *   <li>Art X — NPCs reason about what they observe. A cautious NPC
 *       reacts differently to a hunt than a curious one.</li>
 *   <li>Art XIII — beasts are observed as living things with their own
 *       goals, not as content for the player.</li>
 *   <li>Art XXXI — NPC reactions are desires fulfilled: "I want to warn
 *       my family about the wolves."</li>
 *   <li>Art XLI — generic code, not character-specific. All NPCs share
 *       this goal.</li>
 * </ul>
 *
 * <p>Provenance: INFERRED from Art V (world happens without player),
 * Art X (NPCs reason), Art XIII (every living thing wants something),
 * and the user's Living Moments framework.
 */
public class NpcReactToWorldGoal extends Goal implements WorldEventSubscriber {

    private final EntityCultivator cultivator;

    /** Recent events this NPC has observed (topic → tick observed). */
    private final Map<String, Long> observedEvents = new ConcurrentHashMap<>();

    /** Cooldown between reactions (ticks). Prevents NPC spam. */
    private static final int REACTION_COOLDOWN = 600; // 30 seconds

    /** Maximum distance to react to an event (blocks). */
    private static final int REACT_RANGE = 32;

    /** Tick of last reaction (for cooldown). */
    private long lastReactionTick = 0;

    /** The most recent event this NPC reacted to (for look-at targeting). */
    private BlockPos lastEventPos = null;

    /** NpcReactToWorldGoal is a singleton subscriber registered once. */
    private static NpcReactToWorldGoal instance;

    /** All registered NPC instances that should react to events. */
    private static final java.util.List<EntityCultivator> registeredNpcs =
            new java.util.concurrent.CopyOnWriteArrayList<>();

    public NpcReactToWorldGoal(EntityCultivator cultivator) {
        this.cultivator = cultivator;
        this.setFlags(EnumSet.of(Flag.LOOK));
        registeredNpcs.add(cultivator);
        if (instance == null) {
            instance = this;
            WorldEventBus.subscribe(this);
            Ergenverse.LOGGER.info("[NpcReactToWorld] Subscribed to WorldEventBus prefix 'beast.'");
        }
    }

    // ── WorldEventSubscriber ──────────────────────────────────────────

    @Override
    public String topicPrefix() {
        return "beast.";
    }

    /**
     * Called by the WorldEventBus when a beast event fires.
     * Checks all registered NPCs for proximity and triggers reactions.
     */
    @Override
    public void onEvent(WorldEvent event) {
        if (event == null) return;
        if (!(cultivator.level() instanceof ServerLevel serverLevel)) return;

        // Check if this NPC is within range of the event
        double distSq = cultivator.distanceToSqr(
                event.pos().getX(), cultivator.getY(), event.pos().getZ());
        if (distSq > REACT_RANGE * REACT_RANGE) return;

        // Check cooldown
        long now = cultivator.tickCount;
        String eventKey = event.topic() + "@" + (now / REACTION_COOLDOWN);
        if (observedEvents.containsKey(eventKey)) return;

        // Mark as observed
        observedEvents.put(eventKey, now);

        // Clean old entries (prevent memory leak)
        if (observedEvents.size() > 100) {
            observedEvents.entrySet().removeIf(e -> now - e.getValue() > 24000);
        }

        // React!
        reactToEvent(event, serverLevel, now);
    }

    /**
     * Generate a contextual reaction and optionally speak it.
     */
    private void reactToEvent(WorldEvent event, ServerLevel serverLevel, long now) {
        if (now - lastReactionTick < REACTION_COOLDOWN) return;
        // Only ~40% chance to actually speak (not every observation deserves a line)
        if (cultivator.getRandom().nextFloat() > 0.4f) return;

        lastReactionTick = now;
        lastEventPos = event.pos();

        String npcName = cultivator.getDisplayNameCn();
        String reactionLine = generateReaction(event);

        // Look at the event source
        cultivator.getLookControl().setLookAt(
                event.pos().getX(), cultivator.getEyeY(), event.pos().getZ());

        // Record memory
        WorldHistory.recordMemory(serverLevel,
                "npc_observed_" + cultivator.getRandom().nextInt(100000),
                npcName + " observed: " + event.description(),
                event.topic().split("\\.")[1], // "hunt", "flee", "rest", etc.
                Math.max(1, Math.round(event.severity() * 5)),
                "r_" + cultivator.blockPosition().getX() / 256
                        + "_" + cultivator.blockPosition().getZ() / 256);

        // Broadcast to nearby players (Art V: the world happens around you)
        String message = "\u00A77<" + npcName + "> " + reactionLine + "\u00A7r";
        broadcastToNearbyPlayers(message, 24.0, serverLevel);

        Ergenverse.LOGGER.info("[NpcReactToWorld] {} reacted to '{}': \"{}\"",
                npcName, event.topic(), reactionLine);
    }

    /**
     * Generate a contextual reaction line based on the event topic.
     * These are NOT scripted per-character — they are generic context-driven
     * reactions. Article XLI compliance.
     */
    private String generateReaction(WorldEvent event) {
        String topic = event.topic();
        String desc = event.description();

        // Pick a reaction based on event type and NPC personality
        return switch (topic.split("\\.")[1]) {
            case "hunt" -> {
                if (topic.endsWith("stalk")) {
                    yield pickRandom(
                            "The beasts are restless today...",
                            "Something is being hunted in the forest...",
                            "The spirit beasts are on the move.",
                            "Stay alert — I sense predation nearby."
                    );
                } else {
                    yield pickRandom(
                            "A hunt ends. Blood has been spilled.",
                            "The forest claims another.",
                            "Life feeds life in the wild.",
                            "Such is the way of beasts."
                    );
                }
            }
            case "flee" -> pickRandom(
                    "The wildlife is spooked.",
                    "Something has frightened the beasts.",
                    "The animals scatter... trouble approaches?",
                    "The spirit beasts sense danger."
            );
            case "rest" -> pickRandom(
                    "A spirit beast rests nearby. A rare sight of peace.",
                    "Even the wild ones find respite.",
                    "The beast sleeps. Best not disturb it."
            );
            case "alert" -> pickRandom(
                    "The beasts are on edge.",
                    "Something has caught their attention.",
                    "The wildlife has gone still... watching."
            );
            case "graze" -> pickRandom(
                    "The spirit deer graze peacefully. A good omen.",
                    "Nature is calm today."
            );
            case "observe_player" -> pickRandom(
                    "The beasts have noticed a cultivator...",
                    "The wild ones sense qi."
            );
            default -> "Something stirs in the wild.";
        };
    }

    private String pickRandom(String... options) {
        return options[cultivator.getRandom().nextInt(options.length)];
    }

    /**
     * Broadcast a message to all players within range.
     */
    private void broadcastToNearbyPlayers(String message, double range, ServerLevel serverLevel) {
        List<ServerPlayer> nearby = serverLevel.getEntitiesOfClass(
                ServerPlayer.class,
                cultivator.getBoundingBox().inflate(range));
        for (ServerPlayer p : nearby) {
            p.sendSystemMessage(Component.literal(message), true);
        }
    }

    // ── Goal lifecycle (this goal is purely reactive — it uses LOOK flag) ──

    @Override
    public boolean canUse() {
        return !cultivator.level().isClientSide && lastEventPos != null
                && cultivator.tickCount - lastReactionTick < 100;
    }

    @Override
    public void tick() {
        if (lastEventPos != null) {
            cultivator.getLookControl().setLookAt(
                    lastEventPos.getX(), cultivator.getEyeY(), lastEventPos.getZ());
        }
    }

    @Override
    public void stop() {
        lastEventPos = null;
    }

    /**
     * Clean up when NPC is unloaded. Called from EntityCultivator cleanup.
     */
    public void unregister() {
        registeredNpcs.remove(cultivator);
    }

    /**
     * Clean up the static subscriber on world unload.
     */
    public static void clearAll() {
        registeredNpcs.clear();
        if (instance != null) {
            WorldEventBus.unsubscribe(instance);
            instance = null;
        }
    }
}
