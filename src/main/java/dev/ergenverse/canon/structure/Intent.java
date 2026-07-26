package dev.ergenverse.canon.structure;

/**
 * Intent — the <b>semantic purpose</b> of a canon object, decoupled from its
 * physical representation.
 *
 * <p><b>CRON-127 — WORLD ASSEMBLY COMPILER (user architectural directive)</b>
 *
 * <p>The user's "Intent" layer:
 * <blockquote>
 *   One more layer: Canonical Object → Intent → Template → Assembly → Voxel
 *   Instructions → Minecraft. For example: Meditation Mat → Cultivation Focus
 *   → Poor Meditation Template → Voxel Layout → Minecraft. Later… A Core
 *   Formation Elder uses: Meditation Mat → High Realm Template. Same semantic
 *   object. Different representation.
 * </blockquote>
 *
 * <p>An {@code Intent} says <i>what the object is for</i> in the world's
 * simulation — sleep, cultivate, study, cook, etc. The same semantic furniture
 * kind (e.g. a meditation mat) can carry the intent {@link #CULTIVATE}, but the
 * <em>template</em> chosen by the assembler differs by the owner's realm: a
 * poor mortal gets a coarse gray-carpet mat; a Core Formation elder gets a
 * spirit-vein-stone dais. The canon object does not change; only the
 * materialization does.
 *
 * <p>This is the seam that lets the world grow visually richer as Wang Lin's
 * status rises, without rewriting any canon data.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17. No Minecraft import.</p>
 */
public enum Intent {
    /** A place to sleep / rest. */
    SLEEP,
    /** A place to meditate and gather qi. */
    CULTIVATE,
    /** A place to read or study texts. */
    STUDY,
    /** A place to prepare food. */
    COOK,
    /** A place to refine pills / medicines. */
    ALCHEMY,
    /** A place to store belongings. */
    STORE,
    /** A place to farm spirit herbs. */
    FARM,
    /** A place to do manual work (crafting, repairs, meals). */
    WORK,
    /** A place to guard or watch. */
    GUARD,
    /** A place to gather socially. */
    SOCIALIZE,
    /** A place to set up or tend a formation. */
    FORMATION_WORK,
    /** A light source / landmark. */
    ILLUMINATE,
    /** A boundary marker (fence, wall). */
    BOUNDARY,
    /** A pathway / road. */
    PATHWAY
}
