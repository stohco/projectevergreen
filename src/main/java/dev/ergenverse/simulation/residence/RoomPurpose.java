package dev.ergenverse.simulation.residence;

/**
 * RoomPurpose — the semantic function of a room in a residence.
 *
 * <p>A room exists because a {@link NeedCategory} demands it. The
 * {@link ResidenceManifestBuilder} maps needs to rooms; this enum names the
 * room types that can result. The actual block placement (a later layer) reads
 * the RoomPurpose to decide what blocks to place — but the manifest itself
 * never touches blocks. It is pure semantics.
 *
 * <p>USER-DIRECTED. The 2026-07-26 review: "A residence is not a schematic.
 * It is a simulation object." Rooms are the semantic subdivisions of that
 * simulation object.
 */
public enum RoomPurpose {
    /** The entry — door, threshold, possibly a small foyer. */
    ENTRY,
    /** Kitchen — hearth, water, food prep surface, storage for grain. */
    KITCHEN,
    /** Bedroom — bed, possibly a chest for personal items. */
    BEDROOM,
    /** General storage — shelves, chests, racks. */
    STORAGE,
    /** Courtyard — open air, the center of a residence. Often where animals/gardens are. */
    COURTYARD,
    /** Cultivation chamber — quiet, warded, a meditation mat or cushion. */
    CULTIVATION_CHAMBER,
    /** Alchemy room — pill furnace, herb shelves, ventilation, workbench. */
    ALCHEMY_ROOM,
    /** Herb garden — planting beds, possibly a spirit-water source. */
    HERB_GARDEN,
    /** Training yard — open ground, weapon rack, striking post. */
    TRAINING_YARD,
    /** Library — shelves, reading desk, possibly locked cases for rare texts. */
    LIBRARY,
    /** Observation post — rooftop platform, high window, or a perch with sightlines. */
    OBSERVATION_POST,
    /** Hidden stash — concealed compartment, false floor, warded cache. */
    HIDDEN_STASH,
    /** Animal pen — dog shelter, chicken coop, beast enclosure. */
    ANIMAL_PEN,
    /** Memorial nook — shrine, portrait, incense holder, a place for grief. */
    MEMORIAL_NOOK,
    /** Tea room / reception — table, seating, a tea set. For receiving visitors. */
    RECEPTION,
    /** Workshop — workbench, tools, raw material storage. Occupation-specific. */
    WORKSHOP,
    /** Meditation room — sparse, minimal, a single cushion. The Situ Nan aesthetic. */
    MEDITATION_ROOM
}
