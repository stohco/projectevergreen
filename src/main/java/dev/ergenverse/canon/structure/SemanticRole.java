package dev.ergenverse.canon.structure;

/**
 * SemanticRole — the <b>navigational / functional role</b> of a location in the
 * world, independent of any block or coordinate.
 *
 * <p><b>CRON-127 — WORLD ASSEMBLY COMPILER (user architectural directive)</b>
 *
 * <p>The user's "anchors" concept:
 * <blockquote>
 *   I'd introduce {@code Anchor { String id; SemanticRole role; }}
 *   Examples: bed, meditation, bookshelf, window, entrance, well, courtyard,
 *   chimney. The compiler assigns world coordinates. AI queries anchors.
 *   […] AI never searches blocks. Find Wang Lin → Find House → Find Bedroom →
 *   Find Bed → Compiler Anchor → Navigation Target.
 * </blockquote>
 *
 * <p>A {@code SemanticRole} says <i>what kind of place</i> this is for an actor:
 * a place to sleep, to cultivate, to study, to enter. The
 * {@link dev.ergenverse.assembly.WorldAssembler} resolves each anchor to a
 * concrete world coordinate during compilation; AI and simulation then ask
 * "where is Wang Lin's bed?" by role, never by scanning blocks.
 *
 * <p>If Wang Lin's house is redesigned and the bed moves from x=4 to x=11,
 * nothing breaks — the anchor still answers "bed" with the new coordinate.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17. No Minecraft import.</p>
 */
public enum SemanticRole {
    /** A sleeping place (bed, sleeping mat). */
    BED,
    /** A meditation / cultivation spot. */
    MEDITATION,
    /** A bookshelf or study point. */
    BOOKSHELF,
    /** A window opening. */
    WINDOW,
    /** A door / entrance to a building or room. */
    ENTRANCE,
    /** The village / courtyard well. */
    WELL,
    /** An open courtyard area. */
    COURTYARD,
    /** A hearth / chimney / cooking fire. */
    CHIMNEY,
    /** A storage container (chest, hidden storage). */
    STORAGE,
    /** An alchemy furnace work point. */
    ALCHEMY,
    /** A work table / desk. */
    WORK,
    /** A farm plot cell. */
    FARM,
    /** A lectern / reading stand. */
    LECTERN,
    /** A formation core anchor. */
    FORMATION
}
