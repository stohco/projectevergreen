package dev.ergenverse.simulation.cognition.perception;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.SpiritBeastEntity;
import dev.ergenverse.simulation.actor.Actor;
import dev.ergenverse.simulation.cognition.CultivationState;
import dev.ergenverse.simulation.cognition.Ontology;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * PerceptionSensor — builds a {@link PerceptionSnapshot} for one actor.
 *
 * <p>This is the FIRST layer of the Article XXXV character pipeline.
 * Without it, NPCs decide what to do based only on internal drives (hunger,
 * qi, fatigue) and never react to what is actually around them. A wolf
 * could kill a meditating cultivator and the cultivator's next decision
 * would still be "keep meditating" — because they never perceived the wolf.
 *
 * <h2>What it senses</h2>
 * <ol>
 *   <li><b>Living entities</b> in the perception radius (via
 *       {@link ServerLevel#getEntitiesOfClass}). Each is classified:
 *       hostile (Monster / angry SpiritBeast), prey (weak Animal),
 *       witness (Player / EntityCultivator), ally, neutral.</li>
 *   <li><b>Recent world events</b> — pulled from {@link WorldHistory} via
 *       {@code findNearby}. A beast panic, a qi disturbance, a rumor —
 *       anything the actor would have "heard" or "felt".</li>
 *   <li><b>Environment</b> — time of day (night = higher threat baseline),
 *       underground (no flight, no sky beasts), biome tag for location.</li>
 * </ol>
 *
 * <h2>Perception radius scales with cultivation (Art XIV LOD)</h2>
 * <ul>
 *   <li>Mortal / Qi Condensation: 24 blocks</li>
 *   <li>Foundation Establishment: 48 blocks</li>
 *   <li>Core Formation: 80 blocks</li>
 *   <li>Nascent Soul: 128 blocks</li>
 *   <li>Soul Formation+: 192 blocks (divine-sense tier)</li>
 * </ul>
 *
 * <p>This is NOT line-of-sight raytracing — that would be too expensive
 * per tick. It is "sphere of awareness." A real cultivator's divine sense
 * penetrates walls; this models that. Mortals get a smaller sphere.
 *
 * <h2>Performance</h2>
 * <p>The sensor only runs for actors at ACTIVE_ACTOR+ sim level, and only
 * on the seasonal tick OR when the actor is dirty / linked-and-active.
 * For a typical world with 5-20 active canon NPCs, this is 5-20 entity
 * scans per cognition pass — cheap.
 */
public final class PerceptionSensor {

    /** Hard cap on perceived entities per snapshot (performance). */
    private static final int MAX_ENTITIES = 24;
    /** Hard cap on perceived events per snapshot. */
    private static final int MAX_EVENTS = 8;

    private PerceptionSensor() {}

    /**
     * Build a perception snapshot for an actor.
     *
     * @param actor  the actor (must have blockX/blockY/blockZ set)
     * @param level  the server level (null = headless / no world, returns minimal snapshot)
     * @param tick   current server tick
     * @return the snapshot (never null; may be empty)
     */
    public static PerceptionSnapshot sense(Actor actor, ServerLevel level, long tick) {
        long start = System.nanoTime();

        int radius = perceptionRadiusFor(actor);
        String actorId = actor.id;
        int ax = actor.blockX, ay = actor.blockY, az = actor.blockZ;

        List<PerceptionSnapshot.PerceivedEntity> entities = new ArrayList<>();
        List<PerceptionSnapshot.PerceivedEvent> events = new ArrayList<>();

        boolean hasThreat = false;
        boolean hasOpportunity = false;
        boolean isObserved = false;
        boolean isAlone = true;

        String biomeOrLocation = "unknown";
        boolean isNight = false;
        boolean isUnderground = false;

        if (level != null) {
            // ── Entity scan ──
            net.minecraft.world.phys.AABB box = new net.minecraft.world.phys.AABB(
                    ax - radius, ay - radius, az - radius,
                    ax + radius, ay + radius, az + radius);

            double perceiverPower = perceiverPowerLevel(actor);

            List<LivingEntity> living = level.getEntitiesOfClass(LivingEntity.class, box);
            for (LivingEntity le : living) {
                if (entities.size() >= MAX_ENTITIES) break;
                if (le.isRemoved() || !le.isAlive()) continue;

                // Don't perceive yourself.
                String leId = le.getStringUUID();
                // Skip self by position match (actors don't always hold their entity UUID)
                double dx = le.getX() - (ax + 0.5);
                double dy = le.getY() - (ay + 0.5);
                double dz = le.getZ() - (az + 0.5);
                double distSq = dx * dx + dy * dy + dz * dz;
                if (distSq < 1.0) continue;  // self
                double dist = Math.sqrt(distSq);
                if (dist > radius) continue;

                String classification = classify(le, actor);
                String etype = entityTypeName(le);
                String dname = le.getName().getString();
                double relPower = relativePower(le, perceiverPower);

                entities.add(new PerceptionSnapshot.PerceivedEntity(
                        leId, classification, etype, dist, relPower, dname));

                if ("hostile".equals(classification)) hasThreat = true;
                if ("prey".equals(classification) || "opportunity".equals(classification)) hasOpportunity = true;
                if ("witness".equals(classification) || "neutral".equals(classification)) isObserved = true;
                isAlone = false;
            }

            // ── Event scan via WorldHistory ──
            try {
                dev.ergenverse.history.WorldHistory history = dev.ergenverse.history.WorldHistory.get(level);
                if (history != null) {
                    List<dev.ergenverse.history.WorldHistory.WorldEvent> nearby =
                            history.findNearby(ax, az, radius, MAX_EVENTS);
                    long now = tick;
                    for (dev.ergenverse.history.WorldHistory.WorldEvent we : nearby) {
                        if (events.size() >= MAX_EVENTS) break;
                        long age = now - we.timestamp();
                        if (age < 0) age = 0;
                        // Only perceive events from the last ~10 minutes (12000 ticks).
                        if (age > 12000L) continue;
                        double edx = we.posX() - ax;
                        double edz = we.posZ() - az;
                        double edist = Math.sqrt(edx * edx + edz * edz);
                        events.add(new PerceptionSnapshot.PerceivedEvent(
                                we.topic() == null ? "" : we.topic(),
                                we.description() == null ? "" : we.description(),
                                edist,
                                0.5f,  // history events don't carry intensity; default
                                age));
                        if (we.topic() != null && we.topic().contains("panic")) hasThreat = true;
                        if (we.topic() != null && (we.topic().contains("opportunity")
                                || we.topic().contains("ripe") || we.topic().contains("discovered"))) {
                            hasOpportunity = true;
                        }
                    }
                }
            } catch (Exception e) {
                Ergenverse.LOGGER.debug("[PerceptionSensor] WorldHistory scan failed for {}: {}", actorId, e.toString());
            }

            // ── Environment ──
            long dayTime = level.getDayTime() % 24000L;
            isNight = dayTime > 13000L && dayTime < 23000L;
            isUnderground = ay < level.getMinBuildHeight() + 32
                    || !level.canSeeSky(new BlockPos(ax, ay + 2, az));
            try {
                var holder = level.getBiome(new BlockPos(ax, ay, az));
                var rl = level.registryAccess().registryOrThrow(
                        net.minecraft.core.registries.Registries.BIOME).getKey(holder.value());
                biomeOrLocation = rl == null ? "unknown" : rl.getPath();
            } catch (Exception e) {
                biomeOrLocation = "unknown";
            }
        }

        long elapsed = System.nanoTime() - start;
        return new PerceptionSnapshot(
                actorId, tick, entities, events, radius,
                isNight, isUnderground, biomeOrLocation,
                hasThreat, hasOpportunity, isObserved, isAlone,
                elapsed);
    }

    // ── Classification ──

    private static String classify(LivingEntity le, Actor perceiver) {
        // Hostile: vanilla monsters, or spirit beasts that are currently targeting.
        if (le instanceof Monster) return "hostile";
        if (le instanceof SpiritBeastEntity sb) {
            if (sb.getTarget() != null) return "hostile";
            // Beast tier: if the perceiver is also a beast, classify by power.
            return "neutral";
        }
        // Player or cultivator = witness (they can observe and remember).
        if (le instanceof Player) return "witness";
        if (le.getType().getDescriptionId().contains("cultivator")) return "witness";
        // Weak animals = prey (food opportunity for beasts / hungry cultivators).
        if (le instanceof Animal) {
            if (perceiver.type == dev.ergenverse.simulation.actor.ActorType.BEAST) return "prey";
            return "neutral";
        }
        // Other mobs = neutral by default.
        if (le instanceof Mob) return "neutral";
        return "unknown";
    }

    private static String entityTypeName(LivingEntity le) {
        if (le instanceof Player) return "player";
        String descId = le.getType().getDescriptionId();  // "entity.ergenverse.spirit_wolf"
        int lastDot = descId.lastIndexOf('.');
        return lastDot >= 0 ? descId.substring(lastDot + 1) : descId;
    }

    // ── Power estimation ──

    /**
     * Rough 0..10 power scale for the perceiver, based on cultivation realm.
     * Mortal=0.5, QiCond=1, Foundation=3, Core=5, NascentSoul=7, SoulFormation=9, Ascendant=10.
     */
    private static double perceiverPowerLevel(Actor actor) {
        Ontology cog = actor.cognition;
        if (cog == null) return 0.5;
        CultivationState cs = cog.cultivation;
        if (cs == null) return 0.5;
        int order = cs.realmOrder();
        return switch (order) {
            case 0 -> 0.5;   // mortal
            case 1 -> 1.0;   // qi condensation
            case 2 -> 3.0;   // foundation
            case 3 -> 5.0;   // core formation
            case 4 -> 7.0;   // nascent soul
            case 5 -> 9.0;   // soul formation
            default -> 10.0; // ascendant+
        };
    }

    /**
     * Relative power of a perceived entity vs the perceiver.
     * Returns -1..+1 where negative means weaker, positive means stronger.
     */
    private static double relativePower(LivingEntity le, double perceiverPower) {
        double ePower;
        if (le instanceof Player p) {
            // Players are unknown; assume comparable unless we have data.
            ePower = 2.0;
        } else if (le instanceof Monster m) {
            ePower = 2.0;  // vanilla monsters ~ foundation tier threat
        } else if (le instanceof SpiritBeastEntity sb) {
            // Spirit beasts scale with their tier.
            ePower = 3.0;  // default; could read beastTier
        } else if (le instanceof Animal) {
            ePower = 0.5;
        } else {
            ePower = 1.0;
        }
        double diff = ePower - perceiverPower;
        // Normalize: a 5-point gap is "much stronger/weaker".
        return Math.max(-1.0, Math.min(1.0, diff / 5.0));
    }

    // ── Radius ──

    public static int perceptionRadiusFor(Actor actor) {
        Ontology cog = actor.cognition;
        if (cog == null) return 24;
        CultivationState cs = cog.cultivation;
        if (cs == null) return 24;
        int order = cs.realmOrder();
        if (order <= 0) return 24;
        if (order == 1) return 32;
        if (order == 2) return 48;
        if (order == 3) return 80;
        if (order == 4) return 128;
        if (order == 5) return 192;
        return 256;
    }
}
