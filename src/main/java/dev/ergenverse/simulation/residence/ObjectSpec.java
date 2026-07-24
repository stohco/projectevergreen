package dev.ergenverse.simulation.residence;

/**
 * ObjectSpec — a semantic object in a room, authored from a resident's life.
 *
 * <p>An object exists because the resident needs it for a purpose. The
 * {@link ResidenceManifestBuilder} derives objects from the resident's
 * occupation, habits, and history. The actual block placement (a later layer)
 * reads the ObjectSpec to decide what block/entity to place — but the manifest
 * itself never touches blocks.
 *
 * <p>This is the user's 2026-07-26 directive: "If every object has purpose —
 * Storage Jar → contains grain → Kitchen → family eats → harvest → field →
 * farmer — the interior practically generates itself."
 *
 * @param name          human-readable label (e.g. "Mortar and Pestle")
 * @param purpose       why this object exists (e.g. "grinding herbs for meals")
 * @param canonEvidence "canon" if the novels explicitly mention this object;
 *                      "inferred" if derived from personality/occupation;
 *                      "simulation" if generated for a non-canon NPC
 * @param memoryNote    an optional memory attached to this object (e.g. "a
 *                      gift from Wang Lin's mother") — null if none
 */
public record ObjectSpec(
        String name,
        String purpose,
        String canonEvidence,
        String memoryNote
) {
    public ObjectSpec {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name");
        if (purpose == null || purpose.isBlank()) throw new IllegalArgumentException("purpose");
        if (canonEvidence == null) canonEvidence = "inferred";
        if (memoryNote == null) memoryNote = "";
    }
}
