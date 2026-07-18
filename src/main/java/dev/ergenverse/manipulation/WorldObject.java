package dev.ergenverse.manipulation;

import dev.ergenverse.core.WorldPhilosophy;

/**
 * A universal world object — from a pebble to a continent.
 *
 * <p>Per the {@link WorldPhilosophy} and the Heaven and Earth Manipulation
 * design: every object in the world has the same underlying properties.
 * Cultivation, Divine Sense, techniques, treasures, and Dao comprehension
 * determine how the player can interact with those properties.
 *
 * <p>This is the foundation of the manipulation system. The
 * {@link HeavenAndEarthManipulation} engine compares a player's
 * {@link ManipulationCapability} against the object's resistance
 * (derived from these properties) to determine whether the manipulation
 * succeeds, partially succeeds, or fails with backlash.
 *
 * <h2>The properties</h2>
 *
 * <ul>
 *   <li><b>physicalMass</b> — weight/volume. A pebble is 0.1; a mountain is 10^9.</li>
 *   <li><b>spiritualMass</b> — spiritual weight. A dirt block is 0; a spirit vein is 500.</li>
 *   <li><b>daoAnchoring</b> — how deeply tied to a Dao (0-1). An ordinary rock is 0; Bai Fan's Mountain Crumble imprint is 0.9.</li>
 *   <li><b>daoAffinity</b> — which Dao (earth, water, fire, seal, karma...). Determines which player Dao helps.</li>
 *   <li><b>historicalDaoImprint</b> — has a powerful cultivator imprinted this? (0-1). Bai Fan's imprint is high.</li>
 *   <li><b>worldLawResistance</b> — how hard the world's laws resist change (0-1). Higher in high-law worlds.</li>
 *   <li><b>heavenlyResistance</b> — does the world's heaven actively resist? (0-1). Sealed worlds resist.</li>
 *   <li><b>ownerId</b> — null = unowned; otherwise the sect/NPC/protagonist who owns it.</li>
 *   <li><b>formationAnchoring</b> — anchored by a formation? (0-1). A sect's mountain has 0.8.</li>
 *   <li><b>karmicSignificance</b> — karmically significant? (0-1). Wang Lin's birthplace is 1.0.</li>
 * </ul>
 *
 * <p>These properties are <b>objective</b> — they exist regardless of who
 * is looking. A mortal and a Transcendent both see the same mountain. The
 * difference is what they can DO with it.
 */
public final class WorldObject {

    // ─── Physical properties ───
    public double physicalMass;          // weight/volume in scaled units
    public String material;              // dirt, stone, obsidian, jade, spirit_stone, bedrock...

    // ─── Spiritual properties ───
    public double spiritualMass;         // spiritual weight
    public boolean hasSoul;              // does this object have a soul?
    public String soulNature;            // "mountain soul", "river spirit", "ancient tree will"

    // ─── Dao properties ───
    public double daoAnchoring;          // 0-1: how deeply tied to a Dao
    public String daoAffinity;           // which Dao: earth, water, fire, seal, karma...
    public double historicalDaoImprint;  // 0-1: has a powerful cultivator imprinted this?

    // ─── World properties ───
    public double worldLawResistance;    // 0-1: how hard the world's laws resist change
    public double heavenlyResistance;    // 0-1: does the world's heaven actively resist?

    // ─── Ownership properties ───
    public String ownerId;               // null = unowned; otherwise sect/NPC/protagonist id
    public double formationAnchoring;    // 0-1: anchored by a formation?

    // ─── Karmic properties ───
    public double karmicSignificance;    // 0-1: how karmically significant
    public String karmicNote;            // why is this significant?

    public WorldObject(double physicalMass, String material) {
        this.physicalMass = physicalMass;
        this.material = material;
        this.spiritualMass = 0;
        this.hasSoul = false;
        this.soulNature = "";
        this.daoAnchoring = 0;
        this.daoAffinity = "";
        this.historicalDaoImprint = 0;
        this.worldLawResistance = 0.1;
        this.heavenlyResistance = 0;
        this.ownerId = null;
        this.formationAnchoring = 0;
        this.karmicSignificance = 0;
        this.karmicNote = "";
    }

    // ─── Builder for fluent construction ─────────────────────────────
    public WorldObject spiritual(double mass, String soulNature) {
        this.spiritualMass = mass;
        this.hasSoul = soulNature != null && !soulNature.isEmpty();
        this.soulNature = soulNature != null ? soulNature : "";
        return this;
    }

    public WorldObject dao(double anchoring, String affinity) {
        this.daoAnchoring = anchoring;
        this.daoAffinity = affinity != null ? affinity : "";
        return this;
    }

    public WorldObject imprint(double imprint) {
        this.historicalDaoImprint = imprint;
        return this;
    }

    public WorldObject worldResistance(double law, double heavenly) {
        this.worldLawResistance = law;
        this.heavenlyResistance = heavenly;
        return this;
    }

    public WorldObject owner(String ownerId, double formationAnchoring) {
        this.ownerId = ownerId;
        this.formationAnchoring = formationAnchoring;
        return this;
    }

    public WorldObject karmic(double significance, String note) {
        this.karmicSignificance = significance;
        this.karmicNote = note != null ? note : "";
        return this;
    }

    // ─── Common object factories ─────────────────────────────────────

    /** A single dirt block. */
    public static WorldObject dirtBlock() {
        return new WorldObject(1.0, "dirt").worldResistance(0.1, 0);
    }

    /** A single stone block. */
    public static WorldObject stoneBlock() {
        return new WorldObject(8.0, "stone").worldResistance(0.2, 0);
    }

    /** A spirit vein (in the Spiritual Layer). */
    public static WorldObject spiritVein(double rank) {
        return new WorldObject(0, "spiritual")
            .spiritual(500 * rank, "spirit vein")
            .dao(0.7, "earth")
            .worldResistance(0.5, 0)
            .karmic(0.2, "spirit veins anchor the local ecosystem");
    }

    /** A mountain (physical). */
    public static WorldObject mountain(double scale) {
        // scale: 1 = small hill, 10 = modest mountain, 100 = major peak, 1000 = mountain range
        return new WorldObject(1_000_000_000.0 * scale, "stone")
            .spiritual(100 * Math.sqrt(scale), scale > 10 ? "mountain soul" : "")
            .dao(0.4, "earth")
            .worldResistance(0.6, 0)
            .karmic(0.1, "");
    }

    /** A sect's mountain (anchored by a formation). */
    public static WorldObject sectMountain(String sectId, double formationStrength) {
        return new WorldObject(1_000_000_000.0, "stone")
            .spiritual(100, "mountain soul")
            .dao(0.4, "earth")
            .worldResistance(0.6, 0)
            .owner(sectId, formationStrength)
            .karmic(0.3, "sect territory");
    }

    /** Wang Lin's birthplace (maximally karmically significant). */
    public static WorldObject wangLinBirthplace() {
        return new WorldObject(1_000_000.0, "mixed")
            .spiritual(50, "ancestral echo")
            .dao(0.2, "karma")
            .worldResistance(0.4, 0)
            .karmic(1.0, "Wang Lin's birthplace — moving this would draw cosmic attention");
    }

    /** A pebble (the simplest object). */
    public static WorldObject pebble() {
        return new WorldObject(0.1, "stone").worldResistance(0.05, 0);
    }

    /** An object with Bai Fan's Mountain Crumble imprint. */
    public static WorldObject baiFanImprintMountain() {
        return new WorldObject(1_000_000_000.0, "stone")
            .spiritual(500, "mountain soul — Bai Fan's imprint")
            .dao(0.9, "earth")
            .imprint(0.9)
            .worldResistance(0.8, 0)
            .karmic(0.7, "Bai Fan's Mountain Crumble — the imprint's creator may notice interference");
    }
}
