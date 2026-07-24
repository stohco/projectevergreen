package dev.ergenverse.simulation.residence;

/**
 * NeedCategory — what a resident's life requires of their home.
 *
 * <p>This is the user's 2026-07-26 authorship directive, made concrete:
 * "You don't author buildings. You author people. […] Everything exists
 * because someone needed it."
 *
 * <p>A NeedCategory is a dimension of a resident's life that demands space or
 * objects in their residence. The {@link ResidenceManifestBuilder} reads a
 * resident's needs and derives the rooms + objects that satisfy them. The
 * residence emerges from the life, not the other way around.
 *
 * <h2>Canon asymmetry</h2>
 *
 * <p>These categories are not generic "RPG house slots." They are Er Gen
 * categories. CONCEALMENT and OBSERVATION exist because cultivators hide their
 * strength and watch for threats — Wang Lin's entire early arc. MOURNING exists
 * because loss is a central Er Gen theme. ANIMAL_HOUSING exists because Old
 * Chen has a dog and farmers have livestock. ALCHEMY and HERB_GARDEN exist
 * because pill refinement is a major cultivation subsystem.
 *
 * <p>USER-DIRECTED. The 2026-07-26 review explicitly named this inversion:
 * residences are authored from lives, not placed as schematics.
 */
public enum NeedCategory {
    /** Basic shelter — a roof, a bed, a door. Everyone needs this. */
    BASIC_SHELTER,
    /** Storage for tools, grain, valuables, supplies. Scale depends on occupation. */
    STORAGE,
    /** A space to practice cultivation (meditation, qi circulation). Cultivators only. */
    CULTIVATION_SPACE,
    /** An alchemy room with a pill furnace, herb shelves, ventilation. Pill refiners only. */
    ALCHEMY,
    /** A herb garden — living space for spirit plants and medicinal herbs. */
    HERB_GARDEN,
    /** A training yard for martial practice, weapon drills, body cultivation. */
    COMBAT_TRAINING,
    /** A library or study — shelves for jade slips, manuals, collected knowledge. */
    LIBRARY,
    /** Defensive features — reinforced door, escape route, alarm array. For those with enemies. */
    DEFENSE,
    /** An observation point — a rooftop, a window with sightlines, a perch. For the cautious. */
    OBSERVATION,
    /** Concealed storage — a hidden compartment, a false floor, a warded stash. For the concealment-first. */
    CONCEALMENT,
    /** Housing for animals — a dog shelter, a chicken pen, a beast pen. */
    ANIMAL_HOUSING,
    /** A space to receive visitors — a tea table, seating. For those with social ties. */
    SOCIAL_SPACE,
    /** A memorial nook — a shrine, a portrait, a place to remember the dead. */
    MOURNING,
    /** A kitchen — hearth, water source, food preparation. Everyone who cooks. */
    KITCHEN,
    /** A workspace for a trade — carpentry, smithing, tailoring. Occupation-specific. */
    WORKSHOP
}
