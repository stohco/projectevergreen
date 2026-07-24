package dev.ergenverse.simulation.residence;

import java.util.Collections;
import java.util.List;

/**
 * RoomSpec — a room in a residence, derived from a resident's needs.
 *
 * <p>A RoomSpec is the manifest's unit. It names the room's purpose, explains
 * <b>why</b> it exists (which need it satisfies), lists the objects it contains,
 * and carries any memories attached to locations within it. The block-placement
 * layer (a future cycle) reads RoomSpecs to generate blocks — but the RoomSpec
 * itself is pure semantics. It never touches a BlockPos or a BlockState.
 *
 * <p>This is the user's 2026-07-26 authorship directive: "A residence is not a
 * schematic. It is a simulation object. […] Rooms [are] semantic subdivisions."
 * Each room's {@code reason} field is the audit trail: it says which need
 * caused this room to exist. If you remove the need, the room disappears.
 *
 * @param purpose        the semantic function (never null)
 * @param name           human-readable label (e.g. "Old Chen's Kitchen")
 * @param reason         WHY this room exists — the need it satisfies
 * @param objects        the objects this room contains (unmodifiable, never null)
 * @param memories       memories attached to locations in this room (unmodifiable)
 * @param evidenceFrom   provenance: "canon", "inferred:personality=cautious", etc.
 */
public record RoomSpec(
        RoomPurpose purpose,
        String name,
        String reason,
        List<ObjectSpec> objects,
        List<ResidenceMemory> memories,
        String evidenceFrom
) {
    public RoomSpec {
        if (purpose == null) throw new IllegalArgumentException("purpose");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name");
        if (reason == null || reason.isBlank()) throw new IllegalArgumentException("reason");
        objects = objects == null ? List.of() : Collections.unmodifiableList(List.copyOf(objects));
        memories = memories == null ? List.of() : Collections.unmodifiableList(List.copyOf(memories));
        if (evidenceFrom == null) evidenceFrom = "inferred";
    }
}
