package dev.ergenverse.simulation.action;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.entity.SpiritBeastEntity;
import dev.ergenverse.simulation.event.EnergyType;
import dev.ergenverse.simulation.event.SemanticEventTopics;
import dev.ergenverse.simulation.event.SemanticTag;
import dev.ergenverse.simulation.event.WorldEvent;
import dev.ergenverse.simulation.event.WorldEventBus;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;

/**
 * PlayerCombatBridge — makes player combat a <b>first-class event</b> on the
 * WorldEventBus.
 *
 * <p>Per the user's directive (2026-07-23): "all player actions flow through
 * SimulationActions &rarr; WorldEventBus." Before this bridge, combat was
 * invisible to the simulation. When the player attacked or killed an NPC:
 * <ul>
 *   <li>Wang Lin never noticed — his opinion stayed unchanged regardless of
 *       whether the player was a pacifist or a murderer.</li>
 *   <li>NPC relationships never updated — killing Old Chen's neighbor had
 *       zero effect on Old Chen's trust/fear/grievance axes.</li>
 *   <li>History never recorded combat — WorldChronicle contains interactions
 *       and gifts but no battles.</li>
 *   <li>Rumors never spread — no NPC could gossip about the player's violence.</li>
 * </ul>
 *
 * <p>Now, combat flows through the same bus as interactions, gifts, spells,
 * and breakthroughs. Every subscriber reacts:
 * <ul>
 *   <li>{@code HistorySubscriber} records the combat to WorldHistory.</li>
 *   <li>{@code RelationshipEngine} infers relationship deltas.</li>
 *   <li>{@code NpcSemanticRelationshipSubscriber} updates witness NPCs' multi-axis
 *       relationships (trust, respect, fear, familiarity, grievance).</li>
 *   <li>{@code WangLinSemanticSubscriber} adjusts Wang Lin's opinion if he
 *       witnesses the combat.</li>
 *   <li>{@code ChronicleSubscriber} compiles the event into the WorldChronicle.</li>
 *   <li>{@code BeliefFormationSubscriber} forms beliefs about the player's nature.</li>
 *   <li>{@code ReputationObserver} spreads localized reputation from combat.</li>
 *   <li>{@code MemoryEventSubscriber} stores combat memories for nearby NPCs.</li>
 * </ul>
 *
 * <h2>Events published</h2>
 * <p>Two Forge events are bridged:
 *
 * <h3>1. LivingDamageEvent — combat initiated</h3>
 * <p>When a player damages a living entity:
 * <ul>
 *   <li>If the target is an {@code EntityCultivator} (mod NPC):
 *       publish {@code player.combat.engaged} with the NPC's canon ID.
 *       Severity 0.35 (notable but below ledger threshold 0.45 — a single
 *       hit doesn't warrant historical recording).</li>
 *   <li>If the target is a {@code SpiritBeastEntity} (mod beast):
 *       publish {@code player.combat.engaged} with the beast's type name.
 *       Severity 0.2 (routine — beasts are food/threats).</li>
 *   <li>If the target is a vanilla monster: publish nothing (vanilla mobs are
 *       not part of the simulation).</li>
 * </ul>
 *
 * <h3>2. LivingDeathEvent — combat concluded (kill)</h3>
 * <p>When a player kills a living entity:
 * <ul>
 *   <li>If the target is an {@code EntityCultivator}:
 *       publish {@code player.combat.engaged} (outcome=VICTORY, severity 0.8).
 *       Also publish {@code semantic.act_of_cruelty} if the NPC had low health
 *       (killing a fleeing/defeated opponent is cruel in cultivation world).
 *       The semantic companion event is what drives relationship changes —
 *       the action event is what drives history recording.</li>
 *   <li>If the target is a {@code SpiritBeastEntity}:
 *       publish {@code player.combat.engaged} (outcome=VICTORY, severity 0.5).
 *       No semantic cruelty event — killing beasts is normal survival.</li>
 *   <li>If the target is a vanilla monster: publish nothing.</li>
 * </ul>
 *
 * <h2>Throttle</h2>
 * <p>Damaging the same NPC multiple times per second floods the bus. This bridge
 * throttles damage events: at most one combat-engaged event per target per
 * 40 ticks (2 seconds). Kill events are never throttled — a kill is always
 * notable.
 *
 * <h2>Design principles</h2>
 * <ul>
 *   <li><b>Article V — Everything Exists Without The Player:</b> Player combat
 *       is now indistinguishable from NPC combat on the bus.</li>
 *   <li><b>Not a new Engine (Art XXVI):</b> Pure Forge event handler that calls
 *       existing factory methods. No new bus, no new subscriber, no new store.</li>
 *   <li><b>Additive, not disruptive:</b> Registered at HIGHEST priority. Does NOT
 *       cancel the event — vanilla damage computation continues normally.</li>
 *   <li><b>Two-layer events:</b> Each kill publishes TWO events: one action-level
 *       (COMBAT_ENGAGED) for history, and one semantic-level (ACT_OF_CRUELTY or
 *       none) for relationship/belief updates. This separation is per the user's
 *       2026-07-23 directive: "actions describe what happened; semantics describe
 *       what it meant."</li>
 * </ul>
 *
 * <p>Must be registered on the Forge event bus:
 * {@code MinecraftForge.EVENT_BUS.register(PlayerCombatBridge.class)}.
 *
 * @see SimulationActions#combatEvent
 * @see WorldEventBus#dispatch
 * @see PlayerActionBridge
 */
public final class PlayerCombatBridge {

    private PlayerCombatBridge() {}

    /**
     * Throttle map: target entity UUID → last tick a combat-engaged event was
     * published for that target. Prevents bus flooding from rapid hits.
     * Key: target entity's UUID string.
     * Value: the tick when the last event was published.
     */
    private static final java.util.concurrent.ConcurrentHashMap<String, Long>
            lastDamageEventTick = new java.util.concurrent.ConcurrentHashMap<>();

    /** Minimum ticks between damage events for the same target. */
    private static final long DAMAGE_THROTTLE_TICKS = 40; // 2 seconds

    // ═══════════════════════════════════════════════════════════════
    //  Damage → combat.engaged
    // ═══════════════════════════════════════════════════════════════

    /**
     * Bridge player damage to the WorldEventBus.
     *
     * <p>When a player damages an EntityCultivator or SpiritBeastEntity,
     * publish a {@code player.combat.engaged} event. Throttled to prevent
     * rapid-hit flooding.
     *
     * @param event the Forge LivingDamageEvent
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        // Only server-side.
        if (event.getEntity() == null) return;
        if (event.getEntity().level().isClientSide()) return;

        // Only player-sourced damage.
        if (!(event.getSource().getEntity() instanceof ServerPlayer serverPlayer)) return;

        LivingEntity target = event.getEntity();
        String targetCanonId = resolveTargetId(target);
        if (targetCanonId == null) return; // vanilla mob or untracked entity

        long tick = event.getEntity().level().getGameTime();

        // Throttle check.
        String targetKey = target.getStringUUID();
        Long lastTick = lastDamageEventTick.get(targetKey);
        if (lastTick != null && tick - lastTick < DAMAGE_THROTTLE_TICKS) return;
        lastDamageEventTick.put(targetKey, tick);

        // Build and dispatch the combat-engaged event.
        WorldEvent combatEvent = SimulationActions.combatEvent(
                serverPlayer, targetCanonId,
                target.getDisplayName().getString(), false, tick);
        WorldEventBus.dispatch(combatEvent);

        Ergenverse.LOGGER.debug("[PlayerCombatBridge] {} damaged {} → dispatched player.combat.engaged",
                serverPlayer.getName().getString(), targetCanonId);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Death → combat.engaged (VICTORY) + semantic event
    // ═══════════════════════════════════════════════════════════════

    /**
     * Bridge player kills to the WorldEventBus.
     *
     * <p>When a player kills an EntityCultivator or SpiritBeastEntity,
     * publish a {@code player.combat.engaged} event with outcome=VICTORY.
     * For NPC kills, also publish a semantic event:
     * <ul>
     *   <li>{@code semantic.act_of_cruelty} if the NPC was significantly
     *       weaker (health percentage below 15% before death — the NPC was
     *       essentially defeated and the player finished them off).</li>
     *   <li>No semantic event if the NPC was still fighting (fair combat
     *       in the cultivation world — strength determines right).</li>
     * </ul>
     *
     * <p>In the Er Gen novels, killing is context-dependent:
     * <ul>
     *   <li>Killing an equal-strength opponent in fair combat: normal.
     *       No cruelty tag — strength is the law.</li>
     *   <li>Killing a fleeing, surrendered, or vastly weaker opponent:
     *       cruel. Generates fear and grievance in observers.</li>
     *   <li>Killing beasts for cultivation resources: normal survival.</li>
     * </ul>
     *
     * @param event the Forge LivingDeathEvent
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() == null) return;
        if (event.getEntity().level().isClientSide()) return;

        // Only player-sourced kills.
        LivingEntity target = event.getEntity();
        if (!(event.getSource().getEntity() instanceof ServerPlayer serverPlayer)) return;

        String targetCanonId = resolveTargetId(target);
        if (targetCanonId == null) return; // vanilla mob

        long tick = target.level().getGameTime();

        // (1) Publish the combat-engaged event with outcome=VICTORY.
        WorldEvent combatEvent = SimulationActions.combatEvent(
                serverPlayer, targetCanonId,
                target.getDisplayName().getString(), true, tick);
        WorldEventBus.dispatch(combatEvent);

        // (2) For NPC kills, potentially publish a semantic cruelty event.
        if (target instanceof EntityCultivator) {
            // Check if the kill was cruel: the NPC had very low health relative
            // to their max when the killing blow landed. If below 15%, the NPC
            // was essentially defeated and the player executed them.
            float healthPct = target.getHealth() / Math.max(1.0f, target.getMaxHealth());
            boolean cruelKill = healthPct < 0.15f;

            if (cruelKill) {
                publishCrueltyEvent(serverPlayer, targetCanonId, target, tick);
            }

            Ergenverse.LOGGER.info("[PlayerCombatBridge] {} killed {} (HP={}/{}, cruel={})",
                    serverPlayer.getName().getString(), targetCanonId,
                    String.format("%.0f", target.getHealth()),
                    String.format("%.0f", target.getMaxHealth()),
                    cruelKill);
        } else {
            Ergenverse.LOGGER.debug("[PlayerCombatBridge] {} killed beast {}",
                    serverPlayer.getName().getString(), targetCanonId);
        }

        // Clear throttle entry so a future re-encounter doesn't get throttled.
        lastDamageEventTick.remove(target.getStringUUID());
    }

    // ═══════════════════════════════════════════════════════════════
    //  Helpers
    // ═══════════════════════════════════════════════════════════════

    /**
     * Resolve the target entity to a canon ID for event routing.
     *
     * <p>Rules:
     * <ul>
     *   <li>{@code EntityCultivator} → {@code getCharacterId()} (e.g. "wang_lin", "old_chen")</li>
     *   <li>{@code SpiritBeastEntity} → beast type name (e.g. "spirit_wolf", "qilin")</li>
     *   <li>Vanilla mobs → null (not part of the simulation)</li>
     * </ul>
     */
    private static String resolveTargetId(LivingEntity target) {
        if (target instanceof EntityCultivator cultivator) {
            String id = cultivator.getCharacterId();
            return (id != null && !id.isEmpty()) ? id : "unknown_npc";
        }
        if (target instanceof SpiritBeastEntity beast) {
            return beast.getBeastType().name().toLowerCase();
        }
        // Vanilla Player, Monster, Animal, etc. — not part of the simulation.
        return null;
    }

    /**
     * Publish a semantic act_of_cruelty event when the player executes a
     * significantly weakened NPC.
     *
     * <p>This companion semantic event is what drives relationship and
     * belief updates. The action event (combat.engaged) handles history.
     * The semantic event handles meaning.
     *
     * <p>Per the user's directive: "actions describe what happened; semantics
     * describe what it meant." Killing a 5% HP NPC means cruelty. The
     * NpcSemanticRelationshipSubscriber will make nearby NPCs update their
     * grievance and fear axes. Wang Lin's SemanticSubscriber will adjust
     * his opinion of the player (lowering SAFETY if the player is cruel).
     */
    private static void publishCrueltyEvent(ServerPlayer player, String npcCanonId,
                                              LivingEntity target, long tick) {
        String desc = player.getName().getString() + " killed " + npcCanonId
                + " (" + target.getDisplayName().getString() + ") while severely wounded. "
                + "Observers may interpret this as cruelty.";
        WorldEvent semanticEvent = WorldEvent.of(
                SemanticEventTopics.SEMANTIC_ACT_OF_CRUELTY, EnergyType.PHYSICAL,
                target.blockPosition(), 0.7f, 0.75f,
                desc, "PLAYER_ACTION", tick,
                player.getStringUUID(), npcCanonId,
                SemanticTag.ACT_OF_CRUELTY.name(),
                Map.of(
                        "npc_id", npcCanonId,
                        "npc_name", target.getDisplayName().getString(),
                        "context", "executed_weakened_target",
                        "player_uuid", player.getStringUUID()
                )
        );
        WorldEventBus.dispatch(semanticEvent);

        Ergenverse.LOGGER.debug("[PlayerCombatBridge] Published semantic.act_of_cruelty for kill of {}",
                npcCanonId);
    }
}
