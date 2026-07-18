package dev.ergenverse.perception.observation;

import dev.ergenverse.cultivation.CultivationCapability;
import dev.ergenverse.cultivation.CultivationState;
import dev.ergenverse.cultivation.RealmId;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.perception.PerceptionTier;
// Kind is a nested enum of ObservationPhenomenon (same package) — use ObservationPhenomenon.Kind fully qualified
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ObservationEngine — the emergent discovery system.
 *
 * <p>This is the architectural shift from "trigger-reward" to "world
 * simulation." The engine does NOT grant rewards for actions. It scans the
 * world for objective {@link ObservationPhenomenon}s, checks whether each
 * player can perceive them (based on {@link PerceptionTier}), and records
 * what each player has noticed. When noticed phenomena align into an
 * {@link ObservationChain}, emergent understanding arises — granted as a
 * {@link KnowledgeTag} and a subtle whisper (action bar, not chat).
 *
 * <h2>The Prime Example</h2>
 * <pre>
 *   Player notices birds flying away from a valley.        (BIRD_MIGRATION)
 *   Player enters the valley. It is unusually silent.       (UNUSUAL_SILENCE)
 *   Player feels a heavy pressure in the air.               (SPIRIT_PRESSURE)
 *   ↓
 *   Understanding emerges: "Something powerful slumbers beneath."
 *   Knowledge granted: RECOGNIZES_QI_DENSITY
 * </pre>
 *
 * <p>No quest. No marker. No reward. The world was being itself; the player
 * paid attention; understanding emerged. That is the Er Gen feel.
 *
 * <h2>Phenomena generation</h2>
 * <p>Phenomena are generated FROM WORLD STATE, not scripted:
 * <ul>
 *   <li>{@link Kind#BIRD_MIGRATION} — parrots flying in a consistent direction</li>
 *   <li>{@link Kind#UNUSUAL_SILENCE} — 0 hostile mobs where some are expected</li>
 *   <li>{@link Kind#SPIRIT_FLUCTUATION} — high elevation with sky visibility</li>
 *   <li>{@link Kind#SPIRIT_PRESSURE} — near high-realm cultivator NPCs</li>
 *   <li>{@link Kind#ANCIENT_INSCRIPTION} — near chiseled stone bricks</li>
 *   <li>{@link Kind#BEAST_TERRITORY} — 2+ same-type hostile mobs clustered</li>
 *   <li>{@link Kind#HERB_CLUSTER} — 3+ flower/herb blocks within 8 blocks</li>
 *   <li>{@link Kind#FACTION_SCOUT} — near an EntityCultivator NPC</li>
 * </ul>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
 */
public final class ObservationEngine {

    private ObservationEngine() {}

    /** Per-player observation states (loaded on login, saved on logout). */
    private static final ConcurrentHashMap<UUID, PlayerObservationState> STATES = new ConcurrentHashMap<>();

    // ─── State management ──────────────────────────────────────────────

    public static PlayerObservationState getState(ServerPlayer player) {
        return STATES.computeIfAbsent(player.getUUID(), uuid -> PlayerObservationState.loadFromPlayer(player));
    }

    public static void saveState(ServerPlayer player) {
        PlayerObservationState state = STATES.get(player.getUUID());
        if (state != null) state.saveToPlayer(player);
    }

    public static void clearState(UUID playerId) {
        STATES.remove(playerId);
    }

    // ─── Perception ────────────────────────────────────────────────────

    /**
     * Attempt to perceive a phenomenon. Returns true if the player newly
     * notices it (perception tier sufficient, not previously noticed).
     */
    public static boolean perceive(ServerPlayer player, ObservationPhenomenon phenomenon) {
        PerceptionTier tier = getPerceptionTier(player);
        if (!tier.canPerceive(phenomenon.perceptionTierRequired)) return false;

        PlayerObservationState state = getState(player);
        long currentTick = player.serverLevel().getGameTime();
        boolean isNew = state.notice(phenomenon.id, phenomenon.kind, currentTick);

        if (isNew) {
            // Subtle perception message — only for genuinely new notices, and only
            // if the phenomenon is significant (intensity > 0.3). We do NOT announce
            // every noticed phenomenon — that would defeat the emergent design.
            if (phenomenon.intensity > 0.4f) {
                sendSubtlePerception(player, phenomenon);
            }
        }
        return isNew;
    }

    /**
     * Evaluate all chains for a player. For each newly-completed chain:
     * grant the knowledge tag, send the emergent understanding whisper.
     */
    public static void evaluateChains(ServerPlayer player) {
        PlayerObservationState state = getState(player);
        List<ObservationChain> newlyCompleted = state.evaluateNewlyCompletedChains();
        for (ObservationChain chain : newlyCompleted) {
            if (chain.grantsKnowledgeTag != null) {
                state.grantKnowledge(chain.grantsKnowledgeTag);
            }
            sendEmergentUnderstanding(player, chain);
        }
        if (!newlyCompleted.isEmpty()) {
            saveState(player);
        }
    }

    // ─── Phenomena generation (from world state) ───────────────────────

    /**
     * Scan the world around a position for observable phenomena. Returns 0-4
     * phenomena. Each scan is cheap (entity + block sampling, capped radius).
     */
    public static List<ObservationPhenomenon> scanForPhenomena(ServerLevel level, BlockPos center, int radius) {
        List<ObservationPhenomenon> phenomena = new ArrayList<>();
        long tick = level.getGameTime();
        ResourceKey<Level> dim = level.dimension();

        // 1. Bird migration — parrots moving in a direction
        AABB birdBox = new AABB(center).inflate(radius);
        List<Parrot> parrots = level.getEntitiesOfClass(Parrot.class, birdBox);
        if (parrots.size() >= 2) {
            // Check if they're moving (not perched)
            int moving = 0;
            BlockPos centroid = BlockPos.ZERO;
            for (Parrot p : parrots) {
                if (p.getDeltaMovement().horizontalDistance() > 0.05) {
                    moving++;
                    centroid = centroid.offset(p.blockPosition());
                }
            }
            if (moving >= 2) {
                BlockPos avg = new BlockPos(
                        centroid.getX() / Math.max(1, moving),
                        centroid.getY() / Math.max(1, moving),
                        centroid.getZ() / Math.max(1, moving));
                phenomena.add(new ObservationPhenomenon(
                        "bird_" + avg.getX() + "_" + avg.getZ() + "_" + (tick / 12000),
                        ObservationPhenomenon.Kind.BIRD_MIGRATION, avg, dim, ObservationPhenomenon.Kind.BIRD_MIGRATION.defaultTier,
                        0.6f, "A flock of birds circles here, agitated.",
                        tick, ObservationPhenomenon.Kind.BIRD_MIGRATION.defaultDuration));
            }
        }

        // 2. Unusual silence — no hostile mobs where some are expected
        AABB silenceBox = new AABB(center).inflate(radius * 2);
        List<Mob> mobs = level.getEntitiesOfClass(Mob.class, silenceBox);
        long hostileCount = mobs.stream().filter(m -> m.getType().getCategory() == MobCategory.MONSTER).count();
        // If we're in a non-peaceful area (check biome / sky) and there are 0 hostiles
        if (hostileCount == 0 && level.getMaxLocalRawBrightness(center) < 15 && level.isNight()) {
            phenomena.add(new ObservationPhenomenon(
                    "silence_" + center.getX() + "_" + center.getZ() + "_" + (tick / 6000),
                    ObservationPhenomenon.Kind.UNUSUAL_SILENCE, center, dim, ObservationPhenomenon.Kind.UNUSUAL_SILENCE.defaultTier,
                    0.7f, "An unnatural silence hangs over this place.",
                    tick, ObservationPhenomenon.Kind.UNUSUAL_SILENCE.defaultDuration));
        }

        // 3. Spirit fluctuation — high elevation with sky visibility
        if (center.getY() >= 90 && level.canSeeSky(center)) {
            phenomena.add(new ObservationPhenomenon(
                    "fluctuation_" + center.getX() + "_" + center.getZ() + "_" + (tick / 12000),
                    ObservationPhenomenon.Kind.SPIRIT_FLUCTUATION, center, dim, ObservationPhenomenon.Kind.SPIRIT_FLUCTUATION.defaultTier,
                    0.5f, "The qi here is thin but restless — the sky is close.",
                    tick, ObservationPhenomenon.Kind.SPIRIT_FLUCTUATION.defaultDuration));
        }

        // 4. Beast territory — 2+ same-type hostile mobs clustered
        java.util.Map<String, Integer> mobTypeCounts = new java.util.HashMap<>();
        java.util.Map<String, BlockPos> mobTypeCentroid = new java.util.HashMap<>();
        for (Mob m : mobs) {
            if (m.getType().getCategory() != MobCategory.MONSTER) continue;
            String typeName = m.getType().toShortString();
            mobTypeCounts.merge(typeName, 1, Integer::sum);
            mobTypeCentroid.putIfAbsent(typeName, m.blockPosition());
        }
        for (java.util.Map.Entry<String, Integer> entry : mobTypeCounts.entrySet()) {
            if (entry.getValue() >= 2) {
                BlockPos mobPos = mobTypeCentroid.get(entry.getKey());
                phenomena.add(new ObservationPhenomenon(
                        "beasts_" + entry.getKey() + "_" + mobPos.getX() + "_" + mobPos.getZ() + "_" + (tick / 12000),
                        ObservationPhenomenon.Kind.BEAST_TERRITORY, mobPos, dim, ObservationPhenomenon.Kind.BEAST_TERRITORY.defaultTier,
                        0.6f, "A pack of " + entry.getKey() + " patrols this area.",
                        tick, ObservationPhenomenon.Kind.BEAST_TERRITORY.defaultDuration));
                break; // one per scan
            }
        }

        // 5. Ancient inscription — chiseled stone bricks nearby
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx += 4) {
            for (int dy = -3; dy <= 3; dy += 2) {
                for (int dz = -radius; dz <= radius; dz += 4) {
                    cursor.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    BlockState bs = level.getBlockState(cursor);
                    if (bs.is(Blocks.CHISELED_STONE_BRICKS) || bs.is(Blocks.DEEPSLATE_BRICKS)) {
                        phenomena.add(new ObservationPhenomenon(
                                "inscription_" + cursor.getX() + "_" + cursor.getY() + "_" + cursor.getZ(),
                                ObservationPhenomenon.Kind.ANCIENT_INSCRIPTION, cursor.immutable(), dim,
                                ObservationPhenomenon.Kind.ANCIENT_INSCRIPTION.defaultTier, 0.8f,
                                "Ancient marks are carved into this stone.",
                                tick, ObservationPhenomenon.Kind.ANCIENT_INSCRIPTION.defaultDuration));
                        break;
                    }
                }
            }
        }

        // 6. Herb cluster — 3+ flower/herb blocks within 8 blocks
        int herbCount = 0;
        BlockPos herbCentroid = BlockPos.ZERO;
        for (int dx = -8; dx <= 8; dx += 2) {
            for (int dz = -8; dz <= 8; dz += 2) {
                cursor.set(center.getX() + dx, center.getY(), center.getZ() + dz);
                BlockState bs = level.getBlockState(cursor);
                Block b = bs.getBlock();
                if (b instanceof net.minecraft.world.level.block.FlowerBlock
                        || b instanceof net.minecraft.world.level.block.BushBlock
                        || bs.is(net.minecraft.tags.BlockTags.FLOWERS)) {
                    herbCount++;
                    herbCentroid = herbCentroid.offset(cursor);
                }
            }
        }
        if (herbCount >= 3) {
            BlockPos avg = new BlockPos(
                    herbCentroid.getX() / herbCount, center.getY(), herbCentroid.getZ() / herbCount);
            phenomena.add(new ObservationPhenomenon(
                    "herbs_" + avg.getX() + "_" + avg.getZ() + "_" + (tick / 24000),
                    ObservationPhenomenon.Kind.HERB_CLUSTER, avg, dim, ObservationPhenomenon.Kind.HERB_CLUSTER.defaultTier,
                    0.6f, "A cluster of plants grows thickly here.",
                    tick, ObservationPhenomenon.Kind.HERB_CLUSTER.defaultDuration));
        }

        // 7. Faction scout — near an EntityCultivator NPC
        AABB scanBox = new AABB(center).inflate(radius);
        List<EntityCultivator> cultivators = level.getEntitiesOfClass(EntityCultivator.class, scanBox);
        if (!cultivators.isEmpty()) {
            EntityCultivator npc = cultivators.get(0);
            phenomena.add(new ObservationPhenomenon(
                    "scout_" + npc.getUUID() + "_" + (tick / 18000),
                    ObservationPhenomenon.Kind.FACTION_SCOUT, npc.blockPosition(), dim, ObservationPhenomenon.Kind.FACTION_SCOUT.defaultTier,
                    0.5f, "A cultivator is observing this area.",
                    tick, ObservationPhenomenon.Kind.FACTION_SCOUT.defaultDuration));
        }

        // 8. Spirit pressure — near Wang Lin (high-realm cultivator)
        for (EntityCultivator npc : cultivators) {
            String name = npc.getDisplayName().getString();
            if (name.contains("Wang Lin") || name.contains("王林")) {
                phenomena.add(new ObservationPhenomenon(
                        "pressure_" + npc.getUUID() + "_" + (tick / 6000),
                        ObservationPhenomenon.Kind.SPIRIT_PRESSURE, npc.blockPosition(), dim, ObservationPhenomenon.Kind.SPIRIT_PRESSURE.defaultTier,
                        0.9f, "An immense spiritual pressure radiates from this being.",
                        tick, ObservationPhenomenon.Kind.SPIRIT_PRESSURE.defaultDuration));
                break;
            }
        }

        return phenomena;
    }

    // ─── Player perception tier ────────────────────────────────────────

    public static PerceptionTier getPerceptionTier(ServerPlayer player) {
        var opt = player.getCapability(CultivationCapability.CULTIVATION_STATE).resolve();
        if (opt.isEmpty()) return PerceptionTier.MORTAL;
        CultivationState cs = opt.get();
        RealmId realm = cs.getCurrentRealm();
        return PerceptionTier.fromRealm(realm);
    }

    // ─── Messages (subtle — never banners) ─────────────────────────────

    /** A subtle perception nudge — shown briefly on the action bar. */
    private static void sendSubtlePerception(ServerPlayer player, ObservationPhenomenon p) {
        Component msg = Component.literal(p.description).withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY);
        player.displayClientMessage(msg, true);
    }

    /**
     * The emergent understanding whisper — shown when a chain completes.
     * Italic, dark purple, action bar. Fades. Never "Quest Complete."
     */
    private static void sendEmergentUnderstanding(ServerPlayer player, ObservationChain chain) {
        Component understanding = Component.literal(chain.emergentUnderstanding)
                .withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_PURPLE);
        player.displayClientMessage(understanding, true);

        // If a knowledge tag was granted, whisper it separately after a moment
        if (chain.grantsKnowledgeTag != null) {
            Component tagMsg = Component.literal("§7§o" + chain.grantsKnowledgeTag.label + " — " + chain.grantsKnowledgeTag.description)
                    .withStyle(ChatFormatting.DARK_AQUA);
            // Send as system message (not action bar) so it persists in chat log
            player.sendSystemMessage(tagMsg);
        }
    }

    // ─── Public query API (for other systems) ──────────────────────────

    public static boolean hasKnowledge(ServerPlayer player, KnowledgeTag tag) {
        return getState(player).hasKnowledge(tag);
    }

    public static java.util.Set<KnowledgeTag> getAcquiredKnowledge(ServerPlayer player) {
        return getState(player).getAcquiredTags();
    }

    public static java.util.Set<ObservationPhenomenon.Kind> getNoticedKinds(ServerPlayer player) {
        return getState(player).getNoticedKinds();
    }

    public static java.util.List<ObservationChain> getActiveChains(ServerPlayer player) {
        PlayerObservationState state = getState(player);
        return ObservationChain.activeChainsFor(state.getNoticedKinds(), state.getCompletedChainIds());
    }
}
