package dev.ergenverse.cultivation;

/**
 * AptitudeData — defines the five aptitude grades (资质) used in the
 * Renegade Immortal world.
 *
 * <p><b>Lore correction:</b> Er Gen's RI uses <b>Aptitude/Talent (资质 —
 * zīzhì)</b>, NOT Spiritual Roots (灵根 — that's from <i>A Record of a
 * Mortal's Journey to Immortality</i>). Wang Lin's defining trait was
 * <b>waste-grade aptitude</b> (下下等资质). The Heaven-Defying Bead's true
 * power: it can absorb others' aptitude fragments to improve your own.
 *
 * <p>All players ALWAYS qualify for cultivation (minimum waste-grade). No
 * game-over failure path. Higher aptitude = faster cultivation velocity
 * (qi regen multiplier, breakthrough progress gain multiplier).
 *
 * <h2>Grade table</h2>
 * <table>
 *   <tr><th>Grade</th><th>CN</th><th>Qi Multiplier</th><th>Progress Multiplier</th><th>Rarity</th></tr>
 *   <tr><td>WASTE</td><td>下下等</td><td>0.4x</td><td>0.5x</td><td>40%</td></tr>
 *   <tr><td>POOR</td><td>下等</td><td>0.7x</td><td>0.7x</td><td>30%</td></tr>
 *   <tr><td>AVERAGE</td><td>中等</td><td>1.0x</td><td>1.0x</td><td>20%</td></tr>
 *   <tr><td>GOOD</td><td>上等</td><td>1.5x</td><td>1.5x</td><td>8%</td></tr>
 *   <tr><td>HEAVENLY</td><td>上上等</td><td>2.5x</td><td>2.5x</td><td>2%</td></tr>
 * </table>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
 */
public final class AptitudeData {

    private AptitudeData() {}

    /** Aptitude grades from lowest to highest. */
    public enum Grade {
        WASTE(1, "Waste-grade", "\u4E0B\u4E0B\u7B49", 0.4, 0.5, 40),
        POOR(2, "Poor-grade", "\u4E0B\u7B49", 0.7, 0.7, 30),
        AVERAGE(3, "Average", "\u4E2D\u7B49", 1.0, 1.0, 20),
        GOOD(4, "Good-grade", "\u4E0A\u7B49", 1.5, 1.5, 8),
        HEAVENLY(5, "Heavenly-grade", "\u4E0A\u4E0A\u7B49", 2.5, 2.5, 2);

        /** Ordinal for NBT serialization. */
        public final int order;
        /** English display name. */
        public final String nameEn;
        /** Chinese display name. */
        public final String nameCn;
        /** Multiplier for Qi regeneration speed. */
        public final double qiMultiplier;
        /** Multiplier for breakthrough progress gain. */
        public final double progressMultiplier;
        /** Weight for random rolling (out of 100). */
        public final int weight;

        Grade(int order, String nameEn, String nameCn,
               double qiMultiplier, double progressMultiplier, int weight) {
            this.order = order;
            this.nameEn = nameEn;
            this.nameCn = nameCn;
            this.qiMultiplier = qiMultiplier;
            this.progressMultiplier = progressMultiplier;
            this.weight = weight;
        }

        /** Total weight for random rolling. */
        public static final int TOTAL_WEIGHT = 100;

        /**
         * Look up a Grade by its ordinal (1-5). Returns AVERAGE for invalid values.
         */
        public static Grade byOrder(int order) {
            for (Grade g : values()) {
                if (g.order == order) return g;
            }
            return AVERAGE;
        }
    }

    /**
     * Roll a random aptitude grade using the canonical weight distribution.
     * Most players get waste or poor (mortal world = mostly untalented).
     *
     * @param rng the random source
     * @return the rolled grade
     */
    public static Grade rollAptitude(net.minecraft.util.RandomSource rng) {
        int roll = rng.nextIntBetweenInclusive(1, Grade.TOTAL_WEIGHT);
        int cumulative = 0;
        for (Grade g : Grade.values()) {
            cumulative += g.weight;
            if (roll <= cumulative) return g;
        }
        return Grade.WASTE; // fallback
    }

    /**
     * Get the display text for an aptitude grade.
     * Format: "Grade (CN)" e.g. "Waste-grade (下下等)"
     */
    public static String getDisplayText(Grade grade) {
        if (grade == null) grade = Grade.AVERAGE;
        return grade.nameEn + " \u00A77(" + grade.nameCn + ")";
    }
}