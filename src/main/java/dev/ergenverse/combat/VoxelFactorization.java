package dev.ergenverse.combat;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.cultivation.RealmId;

/**
 * The Voxel Factorization Engine — the smart terraforming/destruction system.
 *
 * <p>This is the system that determines what happens when a cultivator's
 * technique hits a block. It answers: "can this technique vaporize/terraform/
 * freeze this block in this world?"
 *
 * <h2>The Equations</h2>
 *
 * <pre>
 *   F_destruct = (B_base × P_player × C_tech × Q_artifact) / (S_eff + 1)²
 *   R_voxel    = μ_mat × (1 + (σ_world × L_world) / 10)
 *
 *   If F_destruct >= R_voxel       → vaporize (block destroyed)
 *   If F_destruct >= R_voxel × 2   → terraform (block moved/reshaped, not destroyed)
 * </pre>
 *
 * <h2>What each variable means</h2>
 *
 * <ul>
 *   <li><b>B_base</b> — technique grade base. mortal=5, magical=15, spirit=50,
 *       immortal=200, dao=1000. A dao-grade technique is 200× more destructive
 *       than a mortal-grade one, before any other factors.</li>
 *   <li><b>P_player</b> — absolute tier of the player's realm + 1. A Nascent Soul
 *       cultivator (tier 4) has P_player=5; a Transcendent (tier 17) has
 *       P_player=18.</li>
 *   <li><b>C_tech</b> — technique comprehension factor.
 *       (1 - comprehensionDifficulty) × 1.5 + 0.5. A fully-comprehended
 *       technique (difficulty 0) has C_tech=2.0; an uncomprehended one
 *       (difficulty 1) has C_tech=0.5.</li>
 *   <li><b>Q_artifact</b> — artifact quality multiplier (1-3). Casting through
 *      a sentient sword triples destructive force.</li>
 *   <li><b>S_eff</b> — effective suppression = max(0, L_world - P_player) -
 *       suppressBypass. When the world's law tier exceeds the player's tier,
 *       techniques are suppressed. A suppressBypass (from dao-suppression
 *       techniques) can reduce this.</li>
 *   <li><b>μ_mat</b> — material hardness. air=0, dirt=2, wood=3, stone=8,
 *       iron_ore=14, obsidian=25, bedrock=100, spirit_stone_block=18, jade=30.</li>
 *   <li><b>σ_world</b> — world law strength. fragile=0.3, low=0.6, medium=1.0,
 *       high=1.6, absolute=2.5. A high-law world makes blocks harder to affect.</li>
 *   <li><b>L_world</b> — world law tier (0-12).</li>
 * </ul>
 *
 * <h2>The Block Operators</h2>
 *
 * <p>When a technique hits a block, the operator determines what happens:
 *
 * <ul>
 *   <li><b>vaporize</b> — the block is destroyed (dropped as item or gone)</li>
 *   <li><b>terraform</b> — the block is MOVED/RESHAPED, not destroyed. This is
 *       the operator for divine sense manipulation techniques (Vein Extraction,
 *       Mountain Moving, Object Lifting).</li>
 *   <li><b>conceal</b> — the block is hidden (concealment techniques)</li>
 *   <li><b>freeze</b> — the block is frozen (ice/control techniques)</li>
 *   <li><b>seal</b> — the block is sealed (formation techniques)</li>
 *   <li><b>shatter</b> — the block is shattered into fragments (execution techniques)</li>
 *   <li><b>ignite</b> — the block is ignited (fire techniques)</li>
 *   <li><b>purify</b> — the block is purified (healing/protection techniques)</li>
 * </ul>
 *
 * <h2>Why this matters for divine sense terraforming</h2>
 *
 * <p>Divine sense manipulation techniques (Vein Extraction, Mountain Moving,
 * Object Lifting) use the {@code terraform} operator. The difference between
 * {@code vaporize} and {@code terraform}:
 *
 * <ul>
 *   <li>{@code vaporize}: F_destruct >= R_voxel → block disappears</li>
 *   <li>{@code terraform}: F_destruct >= R_voxel × 2 (HIGHER threshold) →
 *       block is moved to a target location, not destroyed</li>
 * </ul>
 *
 * <p>Terraforming is harder than destruction. Moving a mountain requires more
 * force than vaporizing one — because you have to move the mass, not just
 * unmake it. This is why Mountain Moving is a Soul Formation+ technique.
 */
public final class VoxelFactorization {

    private VoxelFactorization() {}

    // ─── B_base: technique grade → base destructive force ────────────
    public static final java.util.Map<String, Integer> GRADE_BASE = java.util.Map.of(
        "mortal", 5,
        "magical", 15,
        "spirit", 50,
        "immortal", 200,
        "dao", 1000
    );

    // ─── μ_mat: material hardness ────────────────────────────────────
    public static final java.util.Map<String, Integer> MATERIAL_HARDNESS = java.util.Map.ofEntries(
        java.util.Map.entry("air", 0),
        java.util.Map.entry("dirt", 2),
        java.util.Map.entry("wood", 3),
        java.util.Map.entry("stone", 8),
        java.util.Map.entry("iron_ore", 14),
        java.util.Map.entry("obsidian", 25),
        java.util.Map.entry("bedrock", 100),
        java.util.Map.entry("spirit_stone_block", 18),
        java.util.Map.entry("jade", 30)
    );

    // ─── σ_world: world law strength ─────────────────────────────────
    public static final java.util.Map<String, Double> WORLD_SIGMA = java.util.Map.of(
        "fragile", 0.3,
        "low", 0.6,
        "medium", 1.0,
        "high", 1.6,
        "absolute", 2.5
    );

    // ─── Block operators ─────────────────────────────────────────────
    public enum BlockOperator {
        VAPORIZE,   // destruction — block destroyed
        TERRAFORM,  // manipulation — block moved/reshaped
        CONCEAL,    // concealment — block hidden
        FREEZE,     // control — block frozen
        SEAL,       // formation — block sealed
        SHATTER,    // execution — block shattered
        IGNITE,     // fire — block ignited
        PURIFY;     // healing/protection — block purified

        /** Does this operator destroy the block, or move/reshape it? */
        public boolean isDestructive() {
            return this == VAPORIZE || this == SHATTER || this == IGNITE;
        }

        /** Is this the terraform operator (move/reshape, not destroy)? */
        public boolean isTerraform() {
            return this == TERRAFORM;
        }
    }

    // ─── Voxel geometry (the shape of the technique's footprint) ─────
    public enum VoxelGeometry {
        NARROW_SLICE,          // sword techniques — a thin cutting plane
        DESCENDING_CYLINDER,   // earth/body techniques — a vertical column
        EXPANDING_DOME,        // divine sense/concealment — a growing sphere
        RADIAL_BURST,          // fire/slaughter — omnidirectional explosion
        LOCKING_CHUNK,         // space/seal — a frozen cube of space
        RISING_PILLAR,         // wood/life — a vertical growing pillar
        TIDAL_WAVE,            // water — a horizontal sweeping wave
        STARFALL_CONE,         // light — a cone from above
        FROZEN_FIELD;          // ice — a flat frozen area
    }

    // ─── Inputs ──────────────────────────────────────────────────────
    public static final class Inputs {
        public final String techniqueGrade;       // mortal/magical/spirit/immortal/dao
        public final RealmId playerRealm;
        public final double comprehensionDifficulty;  // 0.0-1.0
        public final double artifactQuality;      // Q_artifact, 1.0-3.0 (1.0 = no artifact)
        public final int worldLawTier;            // L_world, 0-12
        public final String worldLawStrength;     // fragile/low/medium/high/absolute
        public final double suppressBypass;       // 0.0-1.0 (dao-suppression effect)
        public final String blockMaterial;        // μ_mat key
        public final BlockOperator operator;      // what the technique does to blocks
        public final VoxelGeometry geometry;      // the shape of the footprint
        public final int scale;                   // technique scale (chunk radius / height)

        public Inputs(String techniqueGrade, RealmId playerRealm, double comprehensionDifficulty,
                      double artifactQuality, int worldLawTier, String worldLawStrength,
                      double suppressBypass, String blockMaterial, BlockOperator operator,
                      VoxelGeometry geometry, int scale) {
            this.techniqueGrade = techniqueGrade;
            this.playerRealm = playerRealm;
            this.comprehensionDifficulty = comprehensionDifficulty;
            this.artifactQuality = artifactQuality;
            this.worldLawTier = worldLawTier;
            this.worldLawStrength = worldLawStrength;
            this.suppressBypass = suppressBypass;
            this.blockMaterial = blockMaterial;
            this.operator = operator;
            this.geometry = geometry;
            this.scale = scale;
        }
    }

    // ─── Result ──────────────────────────────────────────────────────
    public static final class Result {
        public final long fDestruct;          // F_destruct — the technique's force
        public final long rVoxel;             // R_voxel — the block's resistance
        public final boolean vaporized;       // F_destruct >= R_voxel (destroyed)
        public final boolean terraformed;     // F_destruct >= R_voxel × 2 (moved/reshaped)
        public final int blocksAffected;      // scale × geometry footprint
        public final BlockOperator operator;  // what happened
        public final VoxelGeometry geometry;  // the shape

        public Result(long fDestruct, long rVoxel, boolean vaporized, boolean terraformed,
                      int blocksAffected, BlockOperator operator, VoxelGeometry geometry) {
            this.fDestruct = fDestruct;
            this.rVoxel = rVoxel;
            this.vaporized = vaporized;
            this.terraformed = terraformed;
            this.blocksAffected = blocksAffected;
            this.operator = operator;
            this.geometry = geometry;
        }

        /** Human-readable summary for logging / HUD. */
        public String summary() {
            if (terraformed) {
                return "F_destruct " + fDestruct + " >= R_voxel×2 " + (rVoxel * 2) + " — " + blocksAffected + " blocks terraformed (" + operator + ", " + geometry + ")";
            }
            if (vaporized) {
                return "F_destruct " + fDestruct + " >= R_voxel " + rVoxel + " — " + blocksAffected + " blocks vaporized (" + operator + ", " + geometry + ")";
            }
            return "F_destruct " + fDestruct + " < R_voxel " + rVoxel + " — the " + geometry + " fizzles; ~" + Math.max(1, blocksAffected / 16) + " blocks scuffed";
        }
    }

    /**
     * Run the voxel factorization.
     *
     * <p>Computes F_destruct and R_voxel, then determines whether the block
     * is vaporized, terraformed, or unaffected.
     */
    public static Result factorize(Inputs inp) {
        // ── F_destruct = (B_base × P_player × C_tech × Q_artifact) / (S_eff + 1)² ──
        int bBase = GRADE_BASE.getOrDefault(inp.techniqueGrade, 10);
        int pPlayer = inp.playerRealm.order + 1;
        double cTech = (1 - inp.comprehensionDifficulty) * 1.5 + 0.5;
        double qArtifact = inp.artifactQuality;

        int absoluteTier = inp.playerRealm.order;
        int sEffRaw = Math.max(0, inp.worldLawTier - absoluteTier) - (int) inp.suppressBypass;
        int sEff = Math.max(0, sEffRaw);
        long fDestruct = Math.round((bBase * pPlayer * cTech * qArtifact) / Math.pow(sEff + 1, 2));

        // ── R_voxel = μ_mat × (1 + (σ_world × L_world) / 10) ──
        int muMat = MATERIAL_HARDNESS.getOrDefault(inp.blockMaterial, 8);
        double sigmaWorld = WORLD_SIGMA.getOrDefault(inp.worldLawStrength, 0.6);
        long rVoxel = Math.round(muMat * (1 + (sigmaWorld * inp.worldLawTier) / 10.0));

        // ── Determine outcome ──
        boolean vaporized = fDestruct >= rVoxel;
        boolean terraformed = inp.operator == BlockOperator.TERRAFORM && fDestruct >= rVoxel * 2;
        int blocksAffected = inp.scale * (vaporized || terraformed ? 64 : 4);

        Ergenverse.LOGGER.debug("[Ergenverse] Voxel factorization: {}", new Result(fDestruct, rVoxel, vaporized, terraformed, blocksAffected, inp.operator, inp.geometry).summary());

        return new Result(fDestruct, rVoxel, vaporized, terraformed, blocksAffected, inp.operator, inp.geometry);
    }

    /**
     * Compute the TOTAL R_voxel for a volume of blocks (for Mountain Moving).
     *
     * <p>Mountain Moving targets a volume, not a single block. The total
     * resistance is the sum of all blocks' R_voxel values. The technique's
     * F_destruct must exceed total R_voxel × 2 (terraform threshold).
     *
     * @param blockMaterials the materials in the volume (e.g., 1000 stone, 50 dirt, 10 obsidian)
     * @param worldLawStrength the world's law strength
     * @param worldLawTier the world's law tier
     * @return the total R_voxel for the volume
     */
    public static long totalResistanceForVolume(java.util.Map<String, Integer> blockMaterials,
                                                 String worldLawStrength, int worldLawTier) {
        double sigmaWorld = WORLD_SIGMA.getOrDefault(worldLawStrength, 0.6);
        long total = 0;
        for (java.util.Map.Entry<String, Integer> e : blockMaterials.entrySet()) {
            int muMat = MATERIAL_HARDNESS.getOrDefault(e.getKey(), 8);
            long rVoxel = Math.round(muMat * (1 + (sigmaWorld * worldLawTier) / 10.0));
            total += rVoxel * e.getValue();
        }
        return total;
    }
}
