package dev.ergenverse.simulation.residence;

import java.util.Collections;
import java.util.List;

/**
 * ResidenceManifest — the semantic description of a residence, derived from a
 * {@link ResidentProfile}.
 *
 * <p>This is the user's 2026-07-26 authorship directive, realized:
 *
 * <blockquote>
 * A residence is not a schematic. It is a simulation object. […] The house is
 * literally generated from the resident. Not the other way around.
 * </blockquote>
 *
 * <p>A ResidenceManifest is the intermediate layer between a person's life
 * (the {@link ResidentProfile}) and the blocks that render their house. It
 * lists the rooms, the objects in each room, the memories attached to locations,
 * and — critically — <b>why each room exists</b> (which need it satisfies).
 *
 * <p>The manifest is <b>canon</b>. The block placement is <b>derived</b>. This
 * separation means:
 * <ul>
 *   <li>Changing the resident's profile changes the manifest, which changes the house.</li>
 *   <li>Changing the block-placement rules changes all houses consistently.</li>
 *   <li>The manifest can be inspected, debugged, and audited without entering Minecraft.</li>
 *   <li>The same manifest can render to different block palettes (thatch vs tile, poor vs wealthy) without changing the authorship.</li>
 * </ul>
 *
 * <h2>The authorship chain</h2>
 *
 * <pre>
 *   ResidentProfile (who the person IS)
 *     → ResidenceManifestBuilder.build()
 *     → ResidenceManifest (what the house CONTAINS and WHY)
 *     → [future: BlockPlacementEngine]
 *     → Minecraft blocks
 * </pre>
 *
 * <p>This class is the second link. It never touches blocks. It never touches
 * the resident's cognition. It is pure domestic semantics — the translation of
 * a life into a home.
 *
 * @param residentId         the resident this manifest belongs to
 * @param settlementId       the settlement this residence is in
 * @param residenceLabel     human-readable label (e.g. "Wang Lin's Home")
 * @param rooms              the rooms, each with purpose, reason, objects, memories
 * @param locationMemories   memories attached to the residence as a whole
 *                           (not tied to a specific room — e.g. "the house
 *                           survived the spring flood")
 * @param manifestReasoning  a human-readable summary of WHY these rooms exist —
 *                           the audit trail from profile to manifest
 */
public record ResidenceManifest(
        String residentId,
        String settlementId,
        String residenceLabel,
        List<RoomSpec> rooms,
        List<ResidenceMemory> locationMemories,
        String manifestReasoning
) {
    public ResidenceManifest {
        if (residentId == null || residentId.isBlank()) throw new IllegalArgumentException("residentId");
        if (settlementId == null || settlementId.isBlank()) throw new IllegalArgumentException("settlementId");
        if (residenceLabel == null || residenceLabel.isBlank()) throw new IllegalArgumentException("residenceLabel");
        rooms = rooms == null ? List.of() : Collections.unmodifiableList(List.copyOf(rooms));
        locationMemories = locationMemories == null ? List.of() : Collections.unmodifiableList(List.copyOf(locationMemories));
        if (manifestReasoning == null) manifestReasoning = "";
    }

    /** Count of rooms in this manifest. */
    public int roomCount() { return rooms.size(); }

    /** Count of total objects across all rooms. */
    public int objectCount() {
        return rooms.stream().mapToInt(r -> r.objects().size()).sum();
    }

    /** Find the first room with a given purpose, or null. */
    public RoomSpec roomOf(RoomPurpose purpose) {
        return rooms.stream().filter(r -> r.purpose() == purpose).findFirst().orElse(null);
    }
}
