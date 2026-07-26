package dev.ergenverse.assembly;

import java.util.List;

/**
 * AssemblyResult — the output of the {@link WorldAssembler}: a flat list of
 * {@link VoxelInstruction}s plus a populated {@link AnchorRegistry}.
 *
 * <p><b>CRON-127 — WORLD ASSEMBLY COMPILER</b>
 *
 * <p>This is the "intermediate representation" the user asked for. It is a
 * pure, backend-agnostic artifact: a list of voxel tuples and a set of named
 * world coordinates. Neither references any Minecraft type. The
 * {@link dev.ergenverse.materialization.VoxelMaterializer} consumes the voxel
 * list; the AI / simulation layer consumes the anchor registry.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17. No Minecraft import.</p>
 */
public record AssemblyResult(List<VoxelInstruction> instructions, AnchorRegistry anchors) {
    public AssemblyResult {
        java.util.Objects.requireNonNull(instructions, "instructions");
        java.util.Objects.requireNonNull(anchors, "anchors");
        instructions = List.copyOf(instructions);
    }

    /** Number of voxel instructions in this assembly. */
    public int voxelCount() {
        return instructions.size();
    }
}
