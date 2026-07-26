package dev.ergenverse.assembly;

/**
 * MaterialID — a <b>backend-agnostic</b> voxel material identifier.
 *
 * <p><b>CRON-127 — WORLD ASSEMBLY COMPILER (user architectural directive)</b>
 *
 * <p>This is the core of the user's "compilation" mental model:
 * <blockquote>
 *   Layer 1 should literally know nothing about Minecraft — not even strings
 *   like {@code "minecraft:oak_planks"}, not BlockState, not BlockPos, nothing.
 *   […] The compiler should emit an intermediate representation.
 *   {@code VoxelInstruction { int x, y, z; MaterialID material; Rotation rotation; Layer layer; }}
 *   Notice: No BlockState. Then VoxelInstruction → MinecraftBackend → BlockState.
 * </blockquote>
 *
 * <p>{@code MaterialID} is a semantic material name. It is <b>not</b> a
 * Minecraft block id — it carries no namespace, no block-state properties, no
 * reference to {@code net.minecraft}. The {@link dev.ergenverse.materialization.MaterialResolver}
 * is the <b>only</b> place that translates a {@code MaterialID} into a concrete
 * {@code net.minecraft.world.level.block.state.BlockState}. This means a future
 * backend (Godot, a custom renderer) could resolve the same {@code MaterialID}
 * into a mesh without touching any canon or assembly code.
 *
 * <h2>Why this matters</h2>
 *
 * <p>Per the user's constitutional rule: <i>"Minecraft classes (BlockState,
 * BlockPos, Blocks, ServerLevel, Entity, etc.) may not appear in the Canon or
 * Semantic World layers. They are backend implementation details and belong
 * exclusively to the world assembly/materialization backend."</i>
 *
 * <p>{@code MaterialID} lives in the assembly layer — above Minecraft, below
 * canon. Canon objects never reference it; they reference semantic kinds
 * ({@link dev.ergenverse.canon.structure.CanonFurniture},
 * {@link dev.ergenverse.canon.structure.BuildingTheme}). The four libraries
 * ({@link FurnitureLibrary}, {@link BuildingLibrary}, etc.) translate those
 * semantic kinds into {@code MaterialID}s.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17. No Minecraft import.</p>
 */
public enum MaterialID {
    // ── Structural ──
    AIR,
    OAK_PLANKS,
    SPRUCE_PLANKS,
    OAK_LOG,
    COBBLESTONE,
    POLISHED_ANDESITE,
    FARMLAND,
    OAK_FENCE,
    OAK_DOOR_LOWER,
    OAK_DOOR_UPPER,

    // ── Furnishings ──
    WHITE_CARPET,
    GRAY_CARPET,
    BOOKSHELF,
    LECTERN,
    CHEST,
    TRAPPED_CHEST,
    LANTERN,
    WORK_TABLE_TOP,

    // ── Mod blocks (ergenverse) ──
    ALCHEMY_FURNACE,
    SPIRIT_VEIN_STONE,
    FORMATION_CORE_STONE,
    QI_GATHERING_GRASS,
    SPIRIT_SAND,
    SPIRIT_WOOD_LEAVES
}
