package dev.ergenverse.canon.structure;

import dev.ergenverse.canon.Provenance;
import java.util.List;

/**
 * CanonFurnitureKind — the semantic kind of furniture. Each kind's block
 * emissions are in {@link CanonFurniture#materializeKind}.
 *
 * <p>CRON-125: per the user's vision, "Furniture knows how to materialize
 * itself. Meditation Mat → oak slabs, carpet, cushion. Bookshelf → bookshelf
 * blocks, lantern, chest."
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17. No Minecraft import.</p>
 */
public enum CanonFurnitureKind {
    BED("Bed", "床"),
    SLEEPING_MAT("Sleeping Mat", "草席"),
    MEDITATION_MAT("Meditation Mat", "蒲团"),
    BOOKSHELF("Bookshelf", "书架"),
    HIDDEN_STORAGE("Hidden Storage", "暗格"),
    LECTERN("Lectern", "书台"),
    DESK("Desk", "书桌"),
    LAMP("Lamp", "油灯"),
    ALCHEMY_FURNACE("Alchemy Furnace", "丹炉"),
    SPIRIT_WELL("Spirit Well", "灵井"),
    HERB_POT("Herb Pot", "药草盆"),
    FORMATION_CORE("Formation Core", "阵眼"),
    SPIRIT_VEIN_STONE("Spirit Vein Stone", "灵脉石"),
    WORK_TABLE("Work Table", "工作台"),
    STORAGE_CHEST("Storage Chest", "储物箱"),
    FARM_PLOT_CELL("Farm Plot Cell", "灵田"),
    LANTERN("Lantern", "灯笼"),
    FENCE_POST("Fence Post", "栅栏柱");

    private final String displayName, nameCn;
    CanonFurnitureKind(String d, String c) { this.displayName = d; this.nameCn = c; }
    public String displayName() { return displayName; }
    public String nameCn() { return nameCn; }
}
