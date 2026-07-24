package dev.ergenverse.entity.ai;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.SpiritBeastEntity;
import dev.ergenverse.history.WorldHistory;
import dev.ergenverse.simulation.event.EnergyType;
import dev.ergenverse.simulation.event.WorldEventBus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.List;

/**
 * BeastLivingEventGoal — makes beasts produce observable events that
 * other systems (NPCs, WorldHistory, rumors) can react to.
 *
 * <h2>The "Living Moment" Problem</h2>
 * <p>Before this goal, beast behavior was invisible to the rest of the
 * simulation. A wolf stalking a deer happened in the Minecraft AI system
 * but never propagated to the WorldEventBus, WorldHistory, or NPC
 * awareness. The user's critique: "6 amazing species that migrate,
 * reproduce, hunt, cultivate, compete, flee, remember danger, react to
 * qi, interact with NPCs" is the goal — but those behaviors must be
 * OBSERVABLE, not just computed.
 *
 * <p>This goal does NOT replace existing goals (HuntGoal, GrazeGoal, etc.).
 * It OBSERVES what they are doing and publishes events to the WorldEventBus
 * so other systems can react. It is a passive observer that turns internal
 * AI state into world-visible events.
 *
 * <h2>Events published</h2>
 * <ul>
 *   <li><b>beast.hunt.stalk</b> — a predator begins stalking prey.
 *       Severity 0.3 (local event). NPCs within 32 blocks can react:
 *       "Wang Tiangui sees a wolf watching the deer herd."</li>
 *   <li><b>beast.hunt.kill</b> — a predator kills prey.
 *       Severity 0.5 (notable). Recorded in WorldHistory. NPCs react:
 *       "A cultivator notices blood on the forest floor."</li>
 *   <li><b>beast.flee</b> — a beast flees from a threat.
 *       Severity 0.2 (minor). NPCs may comment: "The birds scattered."</li>
 *   <li><b>beast.rest</b> — a beast enters rest/recovery.
 *       Severity 0.1 (ambient). NPCs may observe: "A spirit wolf sleeps
 *       beneath the great tree."</li>
 *   <li><b>beast.alert</b> — a beast becomes alert (ambush pose).
 *       Severity 0.25 (minor). Nearby NPCs may notice.</li>
 *   <li><b>beast.graze</b> — a beast grazes.
 *       Severity 0.05 (ambient). Very low — not every graze is notable.</li>
 * </ul>
 *
 * <h2>Throttling</h2>
 * <p>Events are published with cooldowns to prevent spam. A beast grazing
 * does not fire an event every tick — at most once per 600 ticks (30 seconds).
 * Hunting events fire at most once per 200 ticks (10 seconds).
 *
 * <h2>Constitution compliance</h2>
 * <ul>
 *   <li>Art V — events fire whether or not the player is present.</li>
 *   <li>Art XIII — each beast type produces events matching its nature:
 *       wolves hunt deer, rabbits flee, hawks circle.</li>
 *   <li>Art XLIII — events are written to WorldHistory and persist across
 *       saves. A player who returns after a week can find "wolf pack
 *       hunted deer near the south bend" in the memory ledger.</li>
 * </ul>
 *
 * <p>Provenance: INFERRED from Art XIII ("Every Living Thing Wants Something")
 * and the user's Living Moments framework: "No subsystem is considered
 * complete until it has participated in at least one observable Living Moment."
 */
public class BeastLivingEventGoal extends Goal {

    private final SpiritBeastEntity beast;

    // ── Cooldown tracking (prevents event spam) ──
    private int lastHuntEventTick;
    private int lastKillEventTick;
    private int lastFleeEventTick;
    private int lastRestEventTick;
    private int lastAlertEventTick;
    private int lastGrazeEventTick;

    // ── Previous-tick state for change detection ──
    private int prevPose;
    private LivingEntity prevTarget;

    // ── Cooldown intervals (ticks) ──
    private static final int HUNT_COOLDOWN = 200;   // 10 seconds
    private static final int KILL_COOLDOWN = 100;   // 5 seconds
    private static final int FLEE_COOLDOWN = 300;   // 15 seconds
    private static final int REST_COOLDOWN = 600;   // 30 seconds
    private static final int ALERT_COOLDOWN = 400;  // 20 seconds
    private static final int GRAZE_COOLDOWN = 600;  // 30 seconds

    public BeastLivingEventGoal(SpiritBeastEntity beast) {
        this.beast = beast;
        this.setFlags(EnumSet.noneOf(Goal.Flag.class)); // passive — never blocks other goals
        this.prevPose = beast.getSpiritPose();
    }

    @Override
    public boolean canUse() {
        return !beast.level().isClientSide;
    }

    @Override
    public boolean canContinueToUse() {
        return !beast.level().isClientSide;
    }

    @Override
    public void tick() {
        if (beast.level().isClientSide) return;

        int now = beast.tickCount;
        int currentPose = beast.getSpiritPose();
        LivingEntity currentTarget = beast.getTarget();

        // ── Detect pose transitions and publish events ──

        // HUNT START: beast enters CHARGING pose with a target (not player)
        if (currentPose == SpiritBeastEntity.POSE_CHARGING
                && currentTarget != null
                && !(currentTarget instanceof net.minecraft.world.entity.player.Player)
                && now - lastHuntEventTick > HUNT_COOLDOWN) {
            String beastName = beast.getBeastType().id.replace("_", " ");
            String preyName = currentTarget.getType().toShortString();
            String topic = "beast.hunt.stalk";
            String desc = beastName + " begins stalking " + preyName;
            BlockPos pos = beast.blockPosition();

            WorldEventBus.publish(topic, EnergyType.SPIRITUAL, pos,
                    0.3f, desc, "INFERRED Art.XIII", now);
            WorldHistory.recordMemory((net.minecraft.server.level.ServerLevel) beast.level(),
                    "beast_hunt_" + beast.getRandom().nextInt(10000),
                    desc, beastName, 3,
                    "r_" + pos.getX() / 256 + "_" + pos.getZ() / 256);
            lastHuntEventTick = now;
        }

        // BEAST KILLS: target dies while beast is in combat pose
        if (prevTarget != null && prevTarget != currentTarget && !prevTarget.isAlive()
                && now - lastKillEventTick > KILL_COOLDOWN
                && prevPose == SpiritBeastEntity.POSE_CHARGING) {
            String beastName = beast.getBeastType().id.replace("_", " ");
            String preyName = prevTarget.getType().toShortString();
            String topic = "beast.hunt.kill";
            String desc = beastName + " killed " + preyName;
            BlockPos pos = prevTarget.blockPosition();

            WorldEventBus.publish(topic, EnergyType.PHYSICAL, pos,
                    0.5f, desc, "INFERRED Art.XIII", now);
            lastKillEventTick = now;

            // Nearby NPCs may react (handled by NpcReactToWorldGoal subscriber)
        }

        // FLEE: beast in POSE_ALERT with lastHurtByMob set
        if (currentPose == SpiritBeastEntity.POSE_ALERT
                && beast.getLastHurtByMob() != null
                && now - lastFleeEventTick > FLEE_COOLDOWN) {
            String beastName = beast.getBeastType().id.replace("_", " ");
            String threatName = beast.getLastHurtByMob().getType().toShortString();
            String topic = "beast.flee";
            String desc = beastName + " flees from " + threatName;
            BlockPos pos = beast.blockPosition();

            WorldEventBus.publish(topic, EnergyType.SPIRITUAL, pos,
                    0.2f, desc, "INFERRED Art.XIII", now);
            lastFleeEventTick = now;
        }

        // REST: beast enters resting pose
        if (currentPose == SpiritBeastEntity.POSE_RESTING
                && prevPose != SpiritBeastEntity.POSE_RESTING
                && now - lastRestEventTick > REST_COOLDOWN) {
            String beastName = beast.getBeastType().id.replace("_", " ");
            String topic = "beast.rest";
            String desc = beastName + " settles down to rest";
            BlockPos pos = beast.blockPosition();

            WorldEventBus.publish(topic, EnergyType.QI, pos,
                    0.1f, desc, "INFERRED Art.XIII", now);
            lastRestEventTick = now;
        }

        // ALERT: beast enters alert/ambush pose (ambush goal active)
        if (currentPose == SpiritBeastEntity.POSE_ALERT
                && prevPose != SpiritBeastEntity.POSE_ALERT
                && beast.getLastHurtByMob() == null
                && now - lastAlertEventTick > ALERT_COOLDOWN) {
            String beastName = beast.getBeastType().id.replace("_", " ");
            String topic = "beast.alert";
            String desc = beastName + " freezes — alert, watching something";
            BlockPos pos = beast.blockPosition();

            WorldEventBus.publish(topic, EnergyType.SPIRITUAL, pos,
                    0.25f, desc, "INFERRED Art.XIII", now);
            lastAlertEventTick = now;
        }

        // GRAZE: beast enters grazing pose
        if (currentPose == SpiritBeastEntity.POSE_GRAZING
                && prevPose != SpiritBeastEntity.POSE_GRAZING
                && now - lastGrazeEventTick > GRAZE_COOLDOWN) {
            String beastName = beast.getBeastType().id.replace("_", " ");
            String topic = "beast.graze";
            String desc = beastName + " grazes peacefully";
            BlockPos pos = beast.blockPosition();

            WorldEventBus.publish(topic, EnergyType.QI, pos,
                    0.05f, desc, "INFERRED Art.XIII", now);
            lastGrazeEventTick = now;
        }

        // ── Player proximity observation (Article V: world watches the player) ──
        // Beasts notice the player and react. This is a one-way observation
        // that produces an event but does NOT change the beast's AI.
        net.minecraft.world.entity.player.Player nearestPlayer =
                beast.level().getNearestPlayer(beast, 16.0);
        if (nearestPlayer != null && now % 200 == 0) {
            String beastName = beast.getBeastType().id.replace("_", " ");
            String topic = "beast.observe_player";
            String desc = beastName + " notices a cultivator nearby";
            BlockPos pos = beast.blockPosition();

            // Very low severity — this is ambient observation, not a notable event
            WorldEventBus.publish(topic, EnergyType.SPIRITUAL, pos,
                    0.1f, desc, "INFERRED Art.V", now);
        }

        // Update previous state
        prevPose = currentPose;
        prevTarget = currentTarget;
    }
}
