package dev.ergenverse.entity.ai;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.history.WorldHistory;
import dev.ergenverse.simulation.actor.Actor;
import dev.ergenverse.simulation.actor.ActorRegistry;
import dev.ergenverse.simulation.cognition.DesireState;
import dev.ergenverse.simulation.intent.ActorEntityLink;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

/**
 * NpcDesireGoal — Article XXXI: The World Must Desire the Player.
 *
 * <h2>What this goal does</h2>
 * <p>Makes NPCs ACT on their desires. Not respond, not wait — ACT.
 * An NPC with an active desire that has a spoken line will either:
 * <ul>
 *   <li><b>mode="line"</b> — speak when the target is nearby (no movement)</li>
 *   <li><b>mode="approach"</b> — physically walk to the target, then speak</li>
 * </ul>
 *
 * <h2>Named Living Moment</h2>
 * <p>"Wang Tianshui walks up to a family member and warns about the Teng
 * family's grain purchases." This is directly from RI Ch.1-3. The elder
 * of the family is worried, and he acts on that worry. The player does
 * not ask. The NPC approaches unbidden. No marker. No quest log.
 * Everything simply happens.</p>
 *
 * <h2>Target resolution</h2>
 * <ul>
 *   <li>"player" — nearest ServerPlayer</li>
 *   <li>"any_family_member" — nearest EntityCultivator first, then player
 *       (Art V: the family exists without the player)</li>
 *   <li>"nearby_cultivator" — nearest EntityCultivator</li>
 *   <li>"npc_X" — specific NPC by character ID (no fallback)</li>
 *   <li>Other/unrecognized — null (desire does not fire)</li>
 * </ul>
 *
 * <h2>Article V compliance</h2>
 * <p>NPC→NPC desires fire without any player present. The Recruiter
 * asks the elder about candidates. Wang Zhou asks his father about
 * the test. Wang Yiyi helps her mother. These happen because the
 * NPCs want them to, not because a player triggered them.</p>
 *
 * <h2>Article XXVI compliance</h2>
 * <p>NO new Engine/Subscriber/Bus. This is a single Minecraft Goal class
 * that reads existing DesireState data from the Actor system and uses
 * the existing sendSystemMessage infrastructure. It bridges existing
 * systems (DesireState → Goal) without creating new ones.</p>
 *
 * <h2>Article XLI compliance</h2>
 * <p>This goal is not special-cased to any character. Every EntityCultivator
 * whose Actor has active desires with lines will use this goal. Wang
 * Tianshui, Wang Qingyue, Wang Zhou, Wang Wei, Wang Yiyi, Wang Ping —
 * all treated identically by the same code.</p>
 *
 * <p><b>Provenance:</b> INFERRED from Article XXXI: "Every NPC every cycle
 * asks: 'Do I want something from someone else right now? Should I ask /
 * offer / teach / warn / recruit / betray / gift / request / reveal?'"
 * No specific canon line describes this exact mechanism, but it is
 * the system-level implementation of the Article.</p>
 */
public class NpcDesireGoal extends Goal {

    // ═══════════════════════════════════════════════════════════════════
    //  Tunables
    // ═══════════════════════════════════════════════════════════════

    /** Detection range for "line" mode (speak without moving). */
    private static final double LINE_RANGE = 10.0;

    /** Detection range for "approach" mode (spot the target, then walk). */
    private static final double APPROACH_DETECT_RANGE = 16.0;

    /** How close the NPC must get before speaking (approach mode). */
    private static final double SPEAK_RANGE = 3.5;

    /** Approach speed (blocks/tick). Slower than gift offers — casual. */
    private static final double APPROACH_SPEED = 0.7D;

    /** How long the NPC observes the target before committing to approach. */
    private static final long SETTLE_TICKS = 200L; // 10 seconds — everyday interactions

    /** Re-path interval (ticks). */
    private static final int REPATH_INTERVAL = 40;

    /** Minimum urgency for a desire to be considered (dormant threshold). */
    private static final double MIN_URGENCY = 0.3;

    /** How long the NPC looks at the target after delivering a line (ticks). */
    private static final long POST_DELIVER_LOOK_TICKS = 60L; // 3 seconds

    // ════════════════════════════════════════════════════════════════
    //  State
    // ═════════════════════════════════════════════════════════════════════════

    /** The desire currently being acted on. */
    private DesireState activeDesire = null;

    /** The target entity (player or NPC) the NPC is approaching or speaking to. */
    private LivingEntity target = null;

    /** Tick when the target was first observed (for settle check). */
    private long targetFirstSeenTick = 0L;

    /** Tick when the line was delivered. */
    private long deliveredTick = 0L;

    /** Tick counter for re-pathing. */
    private int pathTimer = 0;

    /** Per-desire cooldown tracking: desire ID → last fulfilled tick. */
    private final java.util.Map<String, Long> desireCooldowns = new java.util.HashMap<>();

    /** Targeting conditions for finding NPC targets. */
    private static final TargetingConditions NPC_TARGETING = TargetingConditions.forNonCombat()
            .range(APPROACH_DETECT_RANGE);

    /** The NPC entity this goal controls. */
    private final EntityCultivator cultivator;

    public NpcDesireGoal(EntityCultivator cultivator) {
        this.cultivator = cultivator;
        // MOVE + LOOK: approach mode controls both movement and look direction
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    // ══════════════════════════════════════════════════════════════════════════════
    //  Goal lifecycle
    // ═══════════════════════════════════════════════════════════════════════════════════

    @Override
    public boolean canUse() {
        if (cultivator.level().isClientSide) return false;

        long now = cultivator.level().getGameTime();
        Actor actor = getLinkedActor();
        if (actor == null) return false;

        // Find the highest-urgency active desire that has a line
        DesireState best = null;
        for (DesireState d : actor.cognition.desires) {
            if (!d.hasLine()) continue;
            if (!d.isActive(now)) continue;
            if (d.urgency() < MIN_URGENCY) continue;
            // Check per-desire cooldown
            Long lastDone = desireCooldowns.get(d.id());
            if (lastDone != null && (now - lastDone) < d.cooldownTicks()) continue;
            if (best == null || d.urgency() > best.urgency()) {
                best = d;
            }
        }
        if (best == null) return false;

        // Resolve target
        LivingEntity resolved = resolveTarget(best.target(), now);
        if (resolved == null) return false;

        // For "line" mode: target must already be in range
        if (!best.requiresApproach()) {
            if (resolved.distanceTo(cultivator) > LINE_RANGE) return false;
            // No settle needed for line mode — speak immediately
            activeDesire = best;
            target = resolved;
            return true;
        }

        // For "approach" mode: target must be in detection range
        if (resolved.distanceTo(cultivator) > APPROACH_DETECT_RANGE) return false;

        // Settle check: the NPC observes before committing
        if (target != null && target.getUUID().equals(resolved.getUUID())) {
            // Same target as before — check if settle time elapsed
            if (now - targetFirstSeenTick >= SETTLE_TICKS) {
                activeDesire = best;
                return true;
            }
            // Still observing
            return false;
        }

        // New target — start observing
        target = resolved;
        targetFirstSeenTick = now;
        return false;
    }

    @Override
    public void start() {
        if (activeDesire == null || target == null) return;

        if (activeDesire.requiresApproach()) {
            // Begin pathing toward the target
            startApproach();
        } else {
            // Line mode: speak immediately
            deliverLine();
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (activeDesire == null || target == null) return false;
        if (!target.isAlive()) return false;
        if (deliveredTick > 0) {
            // Post-delivery: keep looking at target for a moment, then stop
            return cultivator.level().getGameTime() - deliveredTick < POST_DELIVER_LOOK_TICKS;
        }

        // Still approaching — continue until in speak range
        if (activeDesire.requiresApproach()) {
            return target.distanceTo(cultivator) > SPEAK_RANGE;
        }

        return false;
    }

    @Override
    public void tick() {
        if (activeDesire == null || target == null) return;

        // Always look at the target
        cultivator.getLookControl().setLookAt(target);

        if (deliveredTick > 0) {
            // Post-delivery: just look. Navigation already stopped.
            return;
        }

        if (activeDesire.requiresApproach()) {
            pathTimer++;
            if (pathTimer >= REPATH_INTERVAL) {
                pathTimer = 0;
                startApproach();
            }

            // Check if we've arrived
            if (target.distanceTo(cultivator) <= SPEAK_RANGE) {
                cultivator.getNavigation().stop();
                deliverLine();
            }
        }
    }

    @Override
    public void stop() {
        cultivator.getNavigation().stop();
        long now = cultivator.level().getGameTime();

        // Record the memory in WorldHistory when the desire fires (Art XXXI.5)
        String memoryTag = activeDesire.id();
        ServerLevel serverLevel = (ServerLevel) cultivator.level();
        try {
            dev.ergenverse.history.WorldHistory.recordMemory(
                    serverLevel, memoryTag,
                    cultivator.getCharacterId() + " expressed: " + activeDesire.line(),
                    activeDesire.socialEngine(), 7,
                    "wang_family_village");
        } catch (Exception ex) {
            // Memory recording is best-effort — never blocks desire delivery
            Ergenverse.LOGGER.debug("[NpcDesire] Memory recording failed for {}: {}",
                    memoryTag, ex.getMessage());
        }

        // Record cooldown if the line was delivered
        if (deliveredTick > 0 && activeDesire != null) {
            desireCooldowns.put(activeDesire.id(), now);
            // Also mark the desire as fulfilled in the Actor's state
            Actor actor = getLinkedActor();
            if (actor != null) {
                for (int i = 0; i < actor.cognition.desires.size(); i++) {
                    DesireState d = actor.cognition.desires.get(i);
                    if (d.id().equals(activeDesire.id())) {
                        actor.cognition.desires.set(i, d.fulfilled(now));
                        break;
                    }
                }
            }
        }

        // Reset state
        activeDesire = null;
        target = null;
        targetFirstSeenTick = 0L;
        deliveredTick = 0;
        pathTimer = 0;
    }

    // ══════════════════════════════════════════════════════════════════════════════════
    //  Internal: approach & delivery
    // ══════════════════════════════════════════════════════════════════════

    /** Start or re-start navigation toward the target. */
    private void startApproach() {
        if (target == null) return;
        cultivator.getNavigation().moveTo(
                target.getX(), target.getY(), target.getZ(), APPROACH_SPEED);
    }

    /**
     * Deliver the desire's spoken line to the target.
     * Player targets receive it directly. NPC targets broadcast to
     * nearby players (the world happens around you — Art XXXI).
     */
    private void deliverLine() {
        if (activeDesire == null || target == null) return;

        String line = activeDesire.line();
        String displayName = cultivator.getDisplayNameCn();
        String prefix = "\u00A77<" + displayName + "> "; // gray NPC name
        String fullMessage = prefix + line + "\u00A7r";

        if (target instanceof ServerPlayer player) {
            // Player target: send as action bar message
            player.sendSystemMessage(Component.literal(fullMessage), true);
        } else {
            // NPC→NPC desire: broadcast to nearby players so they can
            // OBSERVE the world happening (Art XXXI, Art XL §3).
            // The player sees: "<Wang Zhou> Father, will the sect elder really come?"
            // No quest marker. Just the world being alive.
            broadcastToNearbyPlayers(fullMessage, OBSERVE_RANGE);
        }

        deliveredTick = cultivator.level().getGameTime();

        String targetName = target instanceof ServerPlayer p
                ? p.getName().getString()
                : (target instanceof EntityCultivator ec ? ec.getCharacterId() : target.getUUID().toString());
        Ergenverse.LOGGER.info("[NpcDesire] {} -> {} [{}]: \"{}\"",
                cultivator.getCharacterId(), targetName,
                activeDesire.socialEngine(), line);

        // Stop navigation after delivery — the NPC stays and looks
        cultivator.getNavigation().stop();
    }

    /** Range at which players can observe NPC→NPC desire dialogue. */
    private static final double OBSERVE_RANGE = 16.0;

    /**
     * Broadcast a message to all players within OBSERVE_RANGE of this NPC.
     * This lets players OBSERVE NPC→NPC desires without being the target.
     * Art XXXI: the world happens around you. Art XL §3: observable experience.
     */
    private void broadcastToNearbyPlayers(String message, double range) {
        if (!(cultivator.level() instanceof ServerLevel serverLevel)) return;
        List<ServerPlayer> nearby = serverLevel.getEntitiesOfClass(
                ServerPlayer.class,
                cultivator.getBoundingBox().inflate(range));
        for (ServerPlayer p : nearby) {
            p.sendSystemMessage(Component.literal(message), true);
        }
    }

    // ════════════════════════════════════════════════════════════════════════════════
    //  Internal: target resolution
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Resolve a desire's target string to a LivingEntity.
     * Per Article V: the world works without the player.
     * Per Article XLI: generic, not character-specific.
     *
     * <p>Resolution order for each target type:</p>
     * <ul>
     *   <li>"player" — nearest player within DETECT range</li>
     *   <li>"any_family_member" — nearest EntityCultivator, then player
     *       (family exists without the player)</li>
     *   <li>"nearby_cultivator" — nearest EntityCultivator</li>
     *   <li>"npc_X" — specific NPC by character ID (null if absent)</li>
     * </ul>
     */
    private LivingEntity resolveTarget(String targetStr, long now) {
        if (targetStr == null || targetStr.isEmpty()) {
            return findNearestEntity();
        }

        return switch (targetStr) {
            case "player" -> findNearestPlayer();
            case "any_family_member" -> findNearestEntity();
            case "nearby_cultivator" -> findNearestCultivator();
            default -> {
                // "npc_X" pattern: find specific NPC — NO fallback to player.
                // If the specific target isn't nearby, the desire waits.
                if (targetStr.startsWith("npc_")) {
                    yield findSpecificNpc(targetStr);
                }
                // Unrecognized target: desire does not fire.
                yield null;
            }
        };
    }

    /** Find the nearest player within detection range. */
    private ServerPlayer findNearestPlayer() {
        List<ServerPlayer> players = cultivator.level().getEntitiesOfClass(
                ServerPlayer.class,
                cultivator.getBoundingBox().inflate(APPROACH_DETECT_RANGE));
        if (players.isEmpty()) return null;
        // Return the closest player
        ServerPlayer nearest = null;
        double nearestDist = Double.MAX_VALUE;
        for (ServerPlayer p : players) {
            double d = cultivator.distanceToSqr(p);
            if (d < nearestDist) {
                nearestDist = d;
                nearest = p;
            }
        }
        return nearest;
    }

    /**
     * Find the nearest living entity — NPC first, then player.
     * Art V: the family exists without the player. An NPC's desire to
     * talk to "any family member" should find another NPC, not require
     * a player to be present.
     */
    private LivingEntity findNearestEntity() {
        // Try NPC first (Art V: world works without player)
        EntityCultivator npc = findNearestCultivator();
        if (npc != null) return npc;
        // Fall back to player
        return findNearestPlayer();
    }

    /** Find the nearest EntityCultivator (for NPC→NPC desires). */
    private EntityCultivator findNearestCultivator() {
        List<EntityCultivator> npcs = cultivator.level().getEntitiesOfClass(
                EntityCultivator.class,
                cultivator.getBoundingBox().inflate(APPROACH_DETECT_RANGE),
                e -> !e.getUUID().equals(cultivator.getUUID()));
        if (npcs.isEmpty()) return null;
        EntityCultivator nearest = null;
        double nearestDist = Double.MAX_VALUE;
        for (EntityCultivator npc : npcs) {
            double d = cultivator.distanceToSqr(npc);
            if (d < nearestDist) {
                nearestDist = d;
                nearest = npc;
            }
        }
        return nearest;
    }

    /** Find a specific NPC by character ID. */
    private EntityCultivator findSpecificNpc(String characterId) {
        List<EntityCultivator> npcs = cultivator.level().getEntitiesOfClass(
                EntityCultivator.class,
                cultivator.getBoundingBox().inflate(APPROACH_DETECT_RANGE),
                e -> characterId.equals(e.getCharacterId()));
        return npcs.isEmpty() ? null : npcs.get(0);
    }

    /** Get the Actor linked to this entity, if any. */
    private Actor getLinkedActor() {
        String actorId = ActorEntityLink.getActorId(cultivator.getUUID());
        if (actorId == null) return null;
        return ActorRegistry.get(actorId);
    }
}