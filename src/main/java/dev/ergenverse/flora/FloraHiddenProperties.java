package dev.ergenverse.flora;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;

import javax.annotation.Nullable;

/**
 * FloraHiddenProperties — the 11 hidden properties that make each plant
 * unique, even when two plants are the same species and stage.
 *
 * <p>This is the heart of the "two identical herbs, one worth 10x" system.
 * A mortal sees two weeds. A Foundation cultivator sees two Frost Herbs.
 * A Nascent Soul cultivator sees one Frost Herb (3 years old, pure, sun-exposed,
 * no disease) worth a single spirit stone, and one Frost Herb (300 years old,
 * moon-bathed, slightly diseased, mutated toward everbloom) worth ten stones.
 *
 * <p>All fields are hidden by default — they are surfaced through the
 * PerceptionEngine, not displayed directly. Cultivation is the lens; the
 * properties are objective truth.
 *
 * <p><b>Immutable value object.</b> Use {@code withX()} methods for updates.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
 */
public final class FloraHiddenProperties {

    // ── 11 hidden properties ─────────────────────────────────────────

    /** Tick count since the plant was placed. Drives age-based quality. */
    public final long age;

    /** Ambient qi absorbed, 0-1. Higher = more potent medicinal essence. */
    public final float qiSaturation;

    /** Intrinsic purity, 0-1. Higher = cleaner qi, fewer impurities. */
    public final float purity;

    /** Element affinity: FIRE/WATER/EARTH/WOOD/METAL/WIND/THUNDER/VOID/LIFE/DEATH/KARMA. */
    public final String element;

    /** Active mutation, or null. Mutations multiply harvest value. */
    @Nullable
    public final String mutation;

    /** Pharmacological potency, 0-1. The primary medicinal-quality driver. */
    public final float medicinalPotency;

    /** Accumulated karmic residue, 0-1. Lowers quality; high karma plants are dangerous. */
    public final float karmicResidue;

    /** UUID of the cultivator who tended this plant (string form), or null for wild. */
    @Nullable
    public final String ownerMarks;

    /** Active disease, or null. Diseased plants have degraded quality. */
    @Nullable
    public final String disease;

    /** Cumulative moon exposure, 0-1. Gains 0.001/night-tick under open sky. */
    public final float moonExposure;

    /** Tribulation exposure, 0-1. Plants near tribulations absorb trace lightning essence. */
    public final float tribulationExposure;

    // ── Constructor ──────────────────────────────────────────────────

    public FloraHiddenProperties(long age, float qiSaturation, float purity, String element,
                                 @Nullable String mutation, float medicinalPotency,
                                 float karmicResidue, @Nullable String ownerMarks,
                                 @Nullable String disease, float moonExposure,
                                 float tribulationExposure) {
        this.age = age;
        this.qiSaturation = clamp01(qiSaturation);
        this.purity = clamp01(purity);
        this.element = element;
        this.mutation = mutation;
        this.medicinalPotency = clamp01(medicinalPotency);
        this.karmicResidue = clamp01(karmicResidue);
        this.ownerMarks = ownerMarks;
        this.disease = disease;
        this.moonExposure = clamp01(moonExposure);
        this.tribulationExposure = clamp01(tribulationExposure);
    }

    // ── Getters (full set) ───────────────────────────────────────────

    public long getAge() { return age; }
    public float getQiSaturation() { return qiSaturation; }
    public float getPurity() { return purity; }
    public String getElement() { return element; }
    @Nullable public String getMutation() { return mutation; }
    public float getMedicinalPotency() { return medicinalPotency; }
    public float getKarmicResidue() { return karmicResidue; }
    @Nullable public String getOwnerMarks() { return ownerMarks; }
    @Nullable public String getDisease() { return disease; }
    public float getMoonExposure() { return moonExposure; }
    public float getTribulationExposure() { return tribulationExposure; }

    // ── Immutable updates (at least 6) ───────────────────────────────

    public FloraHiddenProperties withAge(long age) {
        return new FloraHiddenProperties(age, qiSaturation, purity, element, mutation,
                medicinalPotency, karmicResidue, ownerMarks, disease, moonExposure, tribulationExposure);
    }

    public FloraHiddenProperties withQiSaturation(float v) {
        return new FloraHiddenProperties(age, v, purity, element, mutation,
                medicinalPotency, karmicResidue, ownerMarks, disease, moonExposure, tribulationExposure);
    }

    public FloraHiddenProperties withPurity(float v) {
        return new FloraHiddenProperties(age, qiSaturation, v, element, mutation,
                medicinalPotency, karmicResidue, ownerMarks, disease, moonExposure, tribulationExposure);
    }

    public FloraHiddenProperties withMedicinalPotency(float v) {
        return new FloraHiddenProperties(age, qiSaturation, purity, element, mutation,
                v, karmicResidue, ownerMarks, disease, moonExposure, tribulationExposure);
    }

    public FloraHiddenProperties withMutation(@Nullable String m) {
        return new FloraHiddenProperties(age, qiSaturation, purity, element, m,
                medicinalPotency, karmicResidue, ownerMarks, disease, moonExposure, tribulationExposure);
    }

    public FloraHiddenProperties withMoonExposure(float v) {
        return new FloraHiddenProperties(age, qiSaturation, purity, element, mutation,
                medicinalPotency, karmicResidue, ownerMarks, disease, v, tribulationExposure);
    }

    public FloraHiddenProperties withDisease(@Nullable String d) {
        return new FloraHiddenProperties(age, qiSaturation, purity, element, mutation,
                medicinalPotency, karmicResidue, ownerMarks, d, moonExposure, tribulationExposure);
    }

    public FloraHiddenProperties withKarmicResidue(float v) {
        return new FloraHiddenProperties(age, qiSaturation, purity, element, mutation,
                medicinalPotency, v, ownerMarks, disease, moonExposure, tribulationExposure);
    }

    public FloraHiddenProperties withOwnerMarks(@Nullable String o) {
        return new FloraHiddenProperties(age, qiSaturation, purity, element, mutation,
                medicinalPotency, karmicResidue, o, disease, moonExposure, tribulationExposure);
    }

    // ── Quality score ────────────────────────────────────────────────

    /**
     * Composite quality score, 0-1. Weighted blend of:
     * <ul>
     *   <li>0.25 purity — clean qi matters most</li>
     *   <li>0.25 medicinalPotency — the active ingredient strength</li>
     *   <li>0.15 qiSaturation — absorbed ambient qi</li>
     *   <li>0.10 health (1 if no disease, 0 if diseased)</li>
     *   <li>0.10 moonExposure — long-term environmental conditioning</li>
     *   <li>0.10 (1 - karmicResidue) — karmic taint degrades value</li>
     *   <li>0.05 mutation bonus — mutations are valuable even if not always beneficial</li>
     * </ul>
     */
    public float qualityScore() {
        float health = disease == null ? 1f : 0f;
        float mutationBonus = mutation != null ? 1f : 0f;
        return clamp01(
                0.25f * purity
              + 0.25f * medicinalPotency
              + 0.15f * qiSaturation
              + 0.10f * health
              + 0.10f * moonExposure
              + 0.10f * (1f - karmicResidue)
              + 0.05f * mutationBonus
        );
    }

    // ── NBT persistence ──────────────────────────────────────────────

    public CompoundTag toNBT(CompoundTag tag) {
        tag.putLong("Age", age);
        tag.putFloat("QiSaturation", qiSaturation);
        tag.putFloat("Purity", purity);
        tag.putString("Element", element != null ? element : "WOOD");
        if (mutation != null) tag.putString("Mutation", mutation);
        else tag.remove("Mutation");
        tag.putFloat("MedicinalPotency", medicinalPotency);
        tag.putFloat("KarmicResidue", karmicResidue);
        if (ownerMarks != null) tag.putString("OwnerMarks", ownerMarks);
        else tag.remove("OwnerMarks");
        if (disease != null) tag.putString("Disease", disease);
        else tag.remove("Disease");
        tag.putFloat("MoonExposure", moonExposure);
        tag.putFloat("TribulationExposure", tribulationExposure);
        return tag;
    }

    public static FloraHiddenProperties fromNBT(CompoundTag tag) {
        return new FloraHiddenProperties(
                tag.getLong("Age"),
                tag.getFloat("QiSaturation"),
                tag.getFloat("Purity"),
                tag.contains("Element") ? tag.getString("Element") : "WOOD",
                tag.contains("Mutation") ? tag.getString("Mutation") : null,
                tag.getFloat("MedicinalPotency"),
                tag.getFloat("KarmicResidue"),
                tag.contains("OwnerMarks") ? tag.getString("OwnerMarks") : null,
                tag.contains("Disease") ? tag.getString("Disease") : null,
                tag.getFloat("MoonExposure"),
                tag.getFloat("TribulationExposure")
        );
    }

    // ── Random factory ───────────────────────────────────────────────

    /**
     * Generate random properties for a freshly-placed plant of the given
     * species. Age starts at 0. Other values are weighted by the species
     * defaults (baseMedicinalPotency, baseQiSaturation, defaultElement)
     * with ±20% random variation. No mutation, no disease, no owner.
     */
    public static FloraHiddenProperties generateRandom(FloraSpecies species, RandomSource rng) {
        float basePotency = species != null ? species.baseMedicinalPotency : 0.5f;
        float baseQi = species != null ? species.baseQiSaturation : 0.5f;
        String element = species != null ? species.defaultElement : "WOOD";

        float potency = clamp01(basePotency + (rng.nextFloat() - 0.5f) * 0.20f);
        float qi = clamp01(baseQi + (rng.nextFloat() - 0.5f) * 0.20f);
        float purity = clamp01(0.6f + rng.nextFloat() * 0.4f); // 0.6-1.0 baseline
        float karmic = clamp01(rng.nextFloat() * 0.05f); // traces of ambient karma
        float moon = 0f; // starts at 0, accumulates over time
        float tribulation = 0f;

        return new FloraHiddenProperties(
                0L, qi, purity, element, null, potency, karmic, null, null, moon, tribulation
        );
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private static float clamp01(float v) {
        if (v < 0f) return 0f;
        if (v > 1f) return 1f;
        return v;
    }
}
