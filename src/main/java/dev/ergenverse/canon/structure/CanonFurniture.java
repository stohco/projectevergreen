package dev.ergenverse.canon.structure;

import java.util.List;

/**
 * CanonFurniture — the <b>semantic</b> leaves of the structure composition tree.
 *
 * <p><b>CRON-127 — WORLD ASSEMBLY COMPILER (user architectural directive)</b>
 *
 * <p>Per the user's directive:
 * <blockquote>
 *   Layer 1 should literally know nothing about Minecraft — not even strings.
 *   {@code enum FurnitureType { BED, MEDITATION_MAT, BOOKSHELF,
 *   HIDDEN_STORAGE, ALCHEMY_TABLE }} — that's it.
 * </blockquote>
 *
 * <p>Each constant declares only <b>semantic</b> data:
 * <ul>
 *   <li>{@link #canonEvidence canonEvidence} — honest provenance.</li>
 *   <li>{@link #intent intent} — what the object is <em>for</em>
 *       ({@link Intent#SLEEP}, {@link Intent#CULTIVATE}, …).</li>
 *   <li>{@link #relativeBounds relativeBounds} — pure-{@code int} volume.</li>
 *   <li>{@link #anchors anchors} — named attachment points for AI navigation.</li>
 *   <li>Bilingual display names ({@code displayName} / {@code nameCn}).</li>
 * </ul>
 *
 * <p><b>No {@code BlockState}, no {@code Blocks}, no {@code BlockPos}.</b> The
 * geometry — which voxel materials make up a sleeping mat — lives in the
 * {@link dev.ergenverse.assembly.FurnitureLibrary} (assembly layer). This means
 * a "Meditation Mat" can be rendered as a coarse gray carpet for a poor mortal
 * or as a spirit-vein-stone dais for a Core Formation elder, without changing
 * any canon data. That is the user's "same semantic object, different
 * representation" seam.
 *
 * <h2>Canon fidelity</h2>
 *
 * <p>Each constant declares its canon evidence:
 * <ul>
 *   <li><b>canon</b> — directly attested (e.g. Wang Lin's father kept an
 *       alchemy furnace; Wang Lin meditated on a mat).</li>
 *   <li><b>mod-original</b> — invented for the mod (e.g. the specific geometry;
 *       the hidden-storage concept).</li>
 *   <li><b>inferred</b> — derived from canon (e.g. a poor village family would
 *       have a sleeping mat, not a bed).</li>
 * </ul>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17. No Minecraft import.</p>
 */
public enum CanonFurniture implements CanonObject {

    SLEEPING_MAT(
            "sleeping_mat", "Sleeping Mat", "草席",
            "inferred — poor village family; canon attests poverty",
            Intent.SLEEP,
            new RelativeBounds(0, 0, 0, 0, 0, 0),
            List.of(Anchor.at("sleeping_mat", SemanticRole.BED, 0, 0))
    ),

    HIDDEN_STORAGE(
            "hidden_storage", "Hidden Storage", "暗格",
            "mod-original — inferred from Wang Lin's secretive personality",
            Intent.STORE,
            new RelativeBounds(0, 0, 0, 0, 1, 0),
            List.of(Anchor.at("hidden_storage", SemanticRole.STORAGE, 0, 0))
    ),

    MEDITATION_MAT(
            "meditation_mat", "Meditation Mat", "蒲团",
            "inferred — Wang Lin meditates daily; the mat itself is inferred",
            Intent.CULTIVATE,
            new RelativeBounds(0, 0, 0, 0, 0, 0),
            List.of(Anchor.at("meditation_mat", SemanticRole.MEDITATION, 0, 0))
    ),

    BOOKSHELF(
            "bookshelf", "Bookshelf", "书架",
            "inferred — Wang Lin's father kept alchemy notes; a bookshelf is implied",
            Intent.STUDY,
            new RelativeBounds(0, 0, 0, 0, 0, 0),
            List.of(Anchor.at("bookshelf", SemanticRole.BOOKSHELF, 0, 0))
    ),

    LECTERN(
            "lectern", "Lectern", "书台",
            "mod-original — chosen because it holds a written book for Wang Lin's notes",
            Intent.STUDY,
            new RelativeBounds(0, 0, 0, 0, 0, 0),
            List.of(Anchor.at("lectern", SemanticRole.LECTERN, 0, 0))
    ),

    ALCHEMY_FURNACE(
            "alchemy_furnace", "Alchemy Furnace", "丹炉",
            "canon — Wang Lin's father kept an alchemy furnace (it grew cold)",
            Intent.ALCHEMY,
            new RelativeBounds(0, 0, 0, 0, 0, 0),
            List.of(Anchor.at("alchemy_furnace", SemanticRole.ALCHEMY, 0, 0))
    ),

    LANTERN(
            "lantern", "Lantern", "灯笼",
            "inferred — mortal villages have light sources at night",
            Intent.ILLUMINATE,
            new RelativeBounds(0, 0, 0, 0, 1, 0),
            List.of()
    ),

    WORK_TABLE(
            "work_table", "Work Table", "工作台",
            "inferred — a poor family has a table for meals and repairs",
            Intent.WORK,
            new RelativeBounds(0, 0, 0, 0, 1, 0),
            List.of(Anchor.at("work_table", SemanticRole.WORK, 0, 0))
    ),

    STORAGE_CHEST(
            "storage_chest", "Storage Chest", "储物箱",
            "inferred — a family has a chest for valuables",
            Intent.STORE,
            new RelativeBounds(0, 0, 0, 0, 0, 0),
            List.of(Anchor.at("storage_chest", SemanticRole.STORAGE, 0, 0))
    ),

    SPIRIT_WELL(
            "spirit_well", "Spirit Well", "灵井",
            "canon — Wang Lin discovers a spirit vein beneath the village well",
            Intent.CULTIVATE,
            new RelativeBounds(0, 0, 0, 0, 2, 0),
            List.of(Anchor.at("spirit_well", SemanticRole.WELL, 0, 0))
    ),

    FORMATION_CORE(
            "formation_core", "Formation Core", "阵眼",
            "mod-original — the elder's status symbol; inferred from canon's elder role",
            Intent.FORMATION_WORK,
            new RelativeBounds(0, 0, 0, 0, 0, 0),
            List.of(Anchor.at("formation_core", SemanticRole.FORMATION, 0, 0))
    ),

    FARM_PLOT_CELL(
            "farm_plot_cell", "Farm Plot Cell", "灵田",
            "canon — the village grows spirit herbs unknowingly",
            Intent.FARM,
            new RelativeBounds(0, 0, 0, 0, 1, 0),
            List.of(Anchor.at("farm_plot_cell", SemanticRole.FARM, 0, 0))
    );

    private final String id;
    private final String displayName;
    private final String nameCn;
    private final String evidence;
    private final Intent intent;
    private final RelativeBounds bounds;
    private final List<Anchor> anchorList;

    CanonFurniture(String id, String displayName, String nameCn, String evidence,
                   Intent intent, RelativeBounds bounds, List<Anchor> anchorList) {
        this.id = id;
        this.displayName = displayName;
        this.nameCn = nameCn;
        this.evidence = evidence;
        this.intent = intent;
        this.bounds = bounds;
        this.anchorList = List.copyOf(anchorList);
    }

    @Override
    public String canonId() {
        return id;
    }

    @Override
    public String canonEvidence() {
        return evidence;
    }

    @Override
    public RelativeBounds relativeBounds() {
        return bounds;
    }

    @Override
    public List<Anchor> anchors() {
        return anchorList;
    }

    /** What this furniture is semantically <em>for</em>. */
    public Intent intent() {
        return intent;
    }

    /** English display name. */
    public String displayName() {
        return displayName;
    }

    /** Chinese display name. */
    public String nameCn() {
        return nameCn;
    }
}
