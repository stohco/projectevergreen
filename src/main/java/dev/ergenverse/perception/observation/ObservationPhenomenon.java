package dev.ergenverse.perception.observation;

import dev.ergenverse.perception.PerceptionTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Objects;

/**
 * ObservationPhenomenon — an objective, observable event/state in the world.
 *
 * <p>Per the Prime Directive ({@link dev.ergenverse.core.WorldPhilosophy}), the
 * world exists independently of the player. Phenomena happen whether or not
 * anyone notices. A player's {@link PerceptionTier} determines whether they
 * <i>perceive</i> the phenomenon — not whether it exists.
 *
 * <p>This is the fundamental unit of the ObservationEngine. A phenomenon is
 * generated from world state (birds migrating, unusual silence, spirit
 * pressure, ancient inscriptions, herb clusters). When a player with
 * sufficient perception enters range, they "notice" it. Noticing phenomena
 * builds toward {@link ObservationChain}s, which yield emergent understanding
 * and {@link KnowledgeTag}s.
 *
 * <h2>Design rule</h2>
 * <blockquote>
 *   Phenomena are NEVER "quest triggers." They are the world being itself.
 *   A bird migration exists because birds migrate. If the player follows it,
 *   they discover what the birds know. No interaction required. No reward
 *   granted. The world responded to the player's curiosity, not to a trigger.
 * </blockquote>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
 */
public final class ObservationPhenomenon {

    /** The kind of phenomenon. Determines how it is generated and perceived. */
    public enum Kind {
        /** Birds/parrots flying in a consistent direction — may lead to herb groves, water, or danger. */
        BIRD_MIGRATION("bird_migration", PerceptionTier.MORTAL, 24000L),
        /** Spirit qi fluctuation — felt at high elevation, near veins, or during celestial events. */
        SPIRIT_FLUCTUATION("spirit_fluctuation", PerceptionTier.QI_CONDENSATION, 12000L),
        /** Unusual silence — no beasts/insects in an area that should have them. Something scared them off. */
        UNUSUAL_SILENCE("unusual_silence", PerceptionTier.MORTAL, 6000L),
        /** Spirit pressure — a powerful entity slumbers nearby. Beasts flee; air feels heavy. */
        SPIRIT_PRESSURE("spirit_pressure", PerceptionTier.FOUNDATION, 6000L),
        /** Ancient inscription — carved marks on stone. Mortals see scratches; cultivators see restrictions. */
        ANCIENT_INSCRIPTION("ancient_inscription", PerceptionTier.MORTAL, -1L),
        /** Beast territory — pack of hostile mobs patrolling a boundary. Something within is worth guarding. */
        BEAST_TERRITORY("beast_territory", PerceptionTier.MORTAL, 12000L),
        /** Karmic trace — residual karma from a past event (battle, betrayal, enlightenment). */
        KARMIC_TRACE("karmic_trace", PerceptionTier.NASCENT_SOUL, 60000L),
        /** Qi density anomaly — unusually rich (or poor) qi in an area. */
        QI_DENSITY_ANOMALY("qi_density_anomaly", PerceptionTier.FOUNDATION, 12000L),
        /** Concealment trace — a formation is hiding something here. The "edge" of the concealment is visible. */
        CONCEALMENT_TRACE("concealment_trace", PerceptionTier.FOUNDATION, 24000L),
        /** Tribulation aftermath — scorch marks, law distortion, residual heavenly punishment. */
        TRIBULATION_AFTERMATH("tribulation_aftermath", PerceptionTier.QI_CONDENSATION, 48000L),
        /** Faction scout — a cultivator NPC from a sect is observing this area. */
        FACTION_SCOUT("faction_scout", PerceptionTier.MORTAL, 18000L),
        /** Herb cluster — 3+ spirit herbs growing together. Indicates a spirit vein or blessed soil. */
        HERB_CLUSTER("herb_cluster", PerceptionTier.QI_CONDENSATION, 24000L);

        public final String id;
        public final PerceptionTier defaultTier;
        public final long defaultDuration;

        Kind(String id, PerceptionTier tier, long duration) {
            this.id = id;
            this.defaultTier = tier;
            this.defaultDuration = duration;
        }

        public static Kind fromId(String id) {
            for (Kind k : values()) if (k.id.equals(id)) return k;
            return null;
        }
    }

    public final String id;
    public final Kind kind;
    public final BlockPos position;
    public final ResourceKey<Level> dimension;
    public final PerceptionTier perceptionTierRequired;
    public final float intensity;          // 0-1, affects description richness
    public final String description;       // what the phenomenon looks like (observer-neutral)
    public final long createdTick;
    public final long durationTicks;       // -1 = permanent (until noticed + expired)

    public ObservationPhenomenon(String id, Kind kind, BlockPos position,
                                  ResourceKey<Level> dimension,
                                  PerceptionTier tierRequired, float intensity,
                                  String description, long createdTick, long durationTicks) {
        this.id = id;
        this.kind = kind;
        this.position = position;
        this.dimension = dimension;
        this.perceptionTierRequired = tierRequired;
        this.intensity = Math.max(0f, Math.min(1f, intensity));
        this.description = description;
        this.createdTick = createdTick;
        this.durationTicks = durationTicks;
    }

    /** Has this phenomenon expired by the given tick? */
    public boolean isExpired(long currentTick) {
        if (durationTicks < 0) return false;
        return (currentTick - createdTick) > durationTicks;
    }

    /** Is this phenomenon at the given position (within tolerance)? */
    public boolean isNear(BlockPos other, int radius) {
        return position.closerThan(other, radius);
    }

    // ─── NBT ───────────────────────────────────────────────────────────

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", id);
        tag.putString("kind", kind.id);
        tag.putLong("x", position.getX());
        tag.putLong("y", position.getY());
        tag.putLong("z", position.getZ());
        tag.putString("dim", dimension.location().toString());
        tag.putString("tier", perceptionTierRequired.name());
        tag.putFloat("intensity", intensity);
        tag.putString("desc", description);
        tag.putLong("created", createdTick);
        tag.putLong("duration", durationTicks);
        return tag;
    }

    public static ObservationPhenomenon fromNBT(CompoundTag tag) {
        try {
            Kind kind = Kind.fromId(tag.getString("kind"));
            if (kind == null) return null;
            BlockPos pos = new BlockPos(
                    tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
            ResourceKey<Level> dim = Level.OVERWORLD;
            String dimStr = tag.getString("dim");
            if (!dimStr.isEmpty()) {
                ResourceLocation rl = new ResourceLocation(dimStr);
                dim = ResourceKey.create(Registries.DIMENSION, rl);
            }
            PerceptionTier tier = PerceptionTier.MORTAL;
            try { tier = PerceptionTier.valueOf(tag.getString("tier")); } catch (Exception ignored) {}
            return new ObservationPhenomenon(
                    tag.getString("id"), kind, pos, dim, tier,
                    tag.getFloat("intensity"), tag.getString("desc"),
                    tag.getLong("created"), tag.getLong("duration"));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ObservationPhenomenon that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
