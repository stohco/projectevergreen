package dev.ergenverse.canon.structure;

import dev.ergenverse.block.ErgenverseBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * CanonFurniture — the leaves of the structure composition tree.
 *
 * <p>Each enum constant is a semantic furniture type that knows how to emit
 * its block geometry into a {@link VolumePlacer}.
 *
 * <h2>Canon fidelity</h2>
 *
 * <p>Each furniture constant declares its canon evidence:
 * <ul>
 *   <li><b>canon</b> — directly attested (e.g. Wang Lin's father kept an
 *       alchemy furnace; Wang Lin meditated on a mat).</li>
 *   <li><b>mod-original</b> — invented for the mod (e.g. the specific block
 *       palette; the exact geometry).</li>
 *   <li><b>inferred</b> — derived from canon (e.g. a poor village family
 *       would have a sleeping mat, not a bed).</li>
 * </ul>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public enum CanonFurniture implements CanonObject {

    SLEEPING_MAT("sleeping_mat",
            "inferred — poor village family; canon attests poverty") {
        @Override
        public void materializeInto(VolumePlacer placer, int dx, int dy, int dz) {
            placer.placeBlock(new BlockPos(dx, dy, dz),
                    Blocks.WHITE_CARPET.defaultBlockState());
        }
        @Override
        public RelativeBounds relativeBounds() {
            return new RelativeBounds(0, 0, 0, 0, 0, 0);
        }
    },

    HIDDEN_STORAGE("hidden_storage",
            "mod-original — inferred from Wang Lin's secretive personality") {
        @Override
        public void materializeInto(VolumePlacer placer, int dx, int dy, int dz) {
            placer.placeBlock(new BlockPos(dx, dy, dz),
                    Blocks.TRAPPED_CHEST.defaultBlockState());
            placer.placeBlock(new BlockPos(dx, dy + 1, dz),
                    Blocks.WHITE_CARPET.defaultBlockState());
        }
        @Override
        public RelativeBounds relativeBounds() {
            return new RelativeBounds(0, 0, 0, 0, 1, 0);
        }
    },

    MEDITATION_MAT("meditation_mat",
            "inferred — Wang Lin meditates daily; the mat itself is inferred") {
        @Override
        public void materializeInto(VolumePlacer placer, int dx, int dy, int dz) {
            placer.placeBlock(new BlockPos(dx, dy, dz),
                    Blocks.GRAY_CARPET.defaultBlockState());
        }
        @Override
        public RelativeBounds relativeBounds() {
            return new RelativeBounds(0, 0, 0, 0, 0, 0);
        }
    },

    BOOKSHELF("bookshelf",
            "inferred — Wang Lin's father kept alchemy notes; a bookshelf is implied") {
        @Override
        public void materializeInto(VolumePlacer placer, int dx, int dy, int dz) {
            placer.placeBlock(new BlockPos(dx, dy, dz),
                    Blocks.BOOKSHELF.defaultBlockState());
        }
        @Override
        public RelativeBounds relativeBounds() {
            return new RelativeBounds(0, 0, 0, 0, 0, 0);
        }
    },

    LECTERN("lectern",
            "mod-original — chosen because it holds a written book for Wang Lin's notes") {
        @Override
        public void materializeInto(VolumePlacer placer, int dx, int dy, int dz) {
            placer.placeBlock(new BlockPos(dx, dy, dz),
                    Blocks.LECTERN.defaultBlockState());
        }
        @Override
        public RelativeBounds relativeBounds() {
            return new RelativeBounds(0, 0, 0, 0, 0, 0);
        }
    },

    ALCHEMY_FURNACE("alchemy_furnace",
            "canon — Wang Lin's father kept an alchemy furnace (it grew cold)") {
        @Override
        public void materializeInto(VolumePlacer placer, int dx, int dy, int dz) {
            placer.placeBlock(new BlockPos(dx, dy, dz),
                    ErgenverseBlocks.ALCHEMY_FURNACE.get().defaultBlockState());
        }
        @Override
        public RelativeBounds relativeBounds() {
            return new RelativeBounds(0, 0, 0, 0, 0, 0);
        }
    },

    LANTERN("lantern",
            "inferred — mortal villages have light sources at night") {
        @Override
        public void materializeInto(VolumePlacer placer, int dx, int dy, int dz) {
            placer.placeBlock(new BlockPos(dx, dy + 1, dz),
                    Blocks.OAK_FENCE.defaultBlockState());
            placer.placeBlock(new BlockPos(dx, dy, dz),
                    Blocks.LANTERN.defaultBlockState());
        }
        @Override
        public RelativeBounds relativeBounds() {
            return new RelativeBounds(0, 0, 0, 0, 1, 0);
        }
    },

    WORK_TABLE("work_table",
            "inferred — a poor family has a table for meals and repairs") {
        @Override
        public void materializeInto(VolumePlacer placer, int dx, int dy, int dz) {
            placer.placeBlock(new BlockPos(dx, dy, dz),
                    Blocks.OAK_FENCE.defaultBlockState());
            placer.placeBlock(new BlockPos(dx, dy + 1, dz),
                    Blocks.OAK_PLANKS.defaultBlockState());
        }
        @Override
        public RelativeBounds relativeBounds() {
            return new RelativeBounds(0, 0, 0, 0, 1, 0);
        }
    },

    STORAGE_CHEST("storage_chest",
            "inferred — a family has a chest for valuables") {
        @Override
        public void materializeInto(VolumePlacer placer, int dx, int dy, int dz) {
            placer.placeBlock(new BlockPos(dx, dy, dz),
                    Blocks.CHEST.defaultBlockState());
        }
        @Override
        public RelativeBounds relativeBounds() {
            return new RelativeBounds(0, 0, 0, 0, 0, 0);
        }
    },

    SPIRIT_WELL("spirit_well",
            "canon — Wang Lin discovers a spirit vein beneath the village well") {
        @Override
        public void materializeInto(VolumePlacer placer, int dx, int dy, int dz) {
            BlockState spiritVeinStone = ErgenverseBlocks.SPIRIT_VEIN_STONE.get().defaultBlockState();
            for (int i = 0; i < 3; i++) {
                placer.placeBlock(new BlockPos(dx, dy + i, dz), spiritVeinStone);
            }
        }
        @Override
        public RelativeBounds relativeBounds() {
            return new RelativeBounds(0, 0, 0, 0, 2, 0);
        }
    },

    FORMATION_CORE("formation_core",
            "mod-original — the elder's status symbol; inferred from canon's elder role") {
        @Override
        public void materializeInto(VolumePlacer placer, int dx, int dy, int dz) {
            placer.placeBlock(new BlockPos(dx, dy, dz),
                    ErgenverseBlocks.FORMATION_CORE_STONE.get().defaultBlockState());
        }
        @Override
        public RelativeBounds relativeBounds() {
            return new RelativeBounds(0, 0, 0, 0, 0, 0);
        }
    },

    FARM_PLOT_CELL("farm_plot_cell",
            "canon — the village grows spirit herbs unknowingly") {
        @Override
        public void materializeInto(VolumePlacer placer, int dx, int dy, int dz) {
            placer.placeBlock(new BlockPos(dx, dy, dz),
                    Blocks.FARMLAND.defaultBlockState());
            placer.placeBlock(new BlockPos(dx, dy + 1, dz),
                    ErgenverseBlocks.QI_GATHERING_GRASS.get().defaultBlockState());
        }
        @Override
        public RelativeBounds relativeBounds() {
            return new RelativeBounds(0, 0, 0, 0, 1, 0);
        }
    };

    private final String id;
    private final String evidence;

    CanonFurniture(String id, String evidence) {
        this.id = id;
        this.evidence = evidence;
    }

    @Override
    public String canonId() {
        return id;
    }

    @Override
    public String canonEvidence() {
        return evidence;
    }
}
