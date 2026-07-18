package dev.ergenverse.simulation.actor;

/**
 * Territory — a territory owned/claimed by an actor.
 *
 * <p>Per Canon Ecological Principles, a territory has 19 fields that
 * completely describe its ecological, mystical, social, and political
 * state. The territory is the smallest unit at which the
 * {@link dev.ergenverse.ecology.CausalEcology} engine simulates.
 *
 * <p>The 19 fields (canon ecological principles):
 * <ol>
 *   <li>{@code name} — territory name</li>
 *   <li>{@code regionId} — parent region</li>
 *   <li>{@code ownerId} — actor that controls the territory</li>
 *   <li>{@code claimedById} — actor that has claimed (but not pacified) it</li>
 *   <li>{@code centerBlockX/Y/Z} — territory center, world coordinates</li>
 *   <li>{@code radiusBlocks} — territory radius</li>
 *   <li>{@code spiritVeinId} — spirit vein that powers the territory</li>
 *   <li>{@code qiDensity} — ambient Qi density (0..1)</li>
 *   <li>{@code biomeType} — biome type tag</li>
 *   <li>{@code floraPopulation} — spirit flora population</li>
 *   <li>{@code herbivorePopulation} — spirit herbivore population</li>
 *   <li>{@code predatorPopulation} — spirit predator population</li>
 *   <li>{@code apexPopulation} — ancient apex population</li>
 *   <li>{@code humanPopulation} — mortal + cultivator population</li>
 *   <li>{@code dangerLevel} — 0..1 danger rating</li>
 *   <li>{@code resourceValue} — 0..1 resource richness</li>
 *   <li>{@code politicalStability} — 0..1</li>
 *   <li>{@code canonicalEventHistory} — list of canonical events</li>
 *   <li>{@code restrictions} — active restrictions / formations</li>
 * </ol>
 */
public final class Territory {

    public final String name;
    public final String regionId;
    public String ownerId;
    public String claimedById;
    public final int centerBlockX;
    public final int centerBlockY;
    public final int centerBlockZ;
    public final int radiusBlocks;
    public String spiritVeinId;
    public double qiDensity;
    public String biomeType;
    public double floraPopulation;
    public double herbivorePopulation;
    public double predatorPopulation;
    public double apexPopulation;
    public double humanPopulation;
    public double dangerLevel;
    public double resourceValue;
    public double politicalStability;
    public final java.util.List<String> canonicalEventHistory = new java.util.ArrayList<>();
    public final java.util.List<String> restrictions = new java.util.ArrayList<>();

    public Territory(String name, String regionId, int cx, int cy, int cz, int radius) {
        this.name = name;
        this.regionId = regionId;
        this.ownerId = null;
        this.claimedById = null;
        this.centerBlockX = cx;
        this.centerBlockY = cy;
        this.centerBlockZ = cz;
        this.radiusBlocks = radius;
        this.spiritVeinId = null;
        this.qiDensity = 0.3;
        this.biomeType = "plains";
        this.floraPopulation = 50;
        this.herbivorePopulation = 20;
        this.predatorPopulation = 5;
        this.apexPopulation = 0;
        this.humanPopulation = 0;
        this.dangerLevel = 0.2;
        this.resourceValue = 0.3;
        this.politicalStability = 0.5;
    }

    /** Total canonical field count — canon audit requires 19. */
    public static final int CANON_FIELD_COUNT = 19;

    public boolean isOwned()       { return ownerId != null; }
    public boolean isClaimed()     { return claimedById != null; }
    public boolean isContested()   { return claimedById != null
            && ownerId != null
            && !claimedById.equals(ownerId); }
}
