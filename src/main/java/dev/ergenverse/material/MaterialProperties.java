package dev.ergenverse.material;

import java.util.*;

/**
 * Universal Material Properties — the backbone of every crafting system.
 *
 * <p>Per the user's design directive: "Everything should derive from a common
 * material ontology." Every item, block, herb, beast part, ore, and artifact
 * in the game has MaterialProperties. ALL crafting stations read this same data.
 *
 * <h2>Four Dimensions</h2>
 * <ul>
 *   <li><b>Physical</b> — Density, Hardness, Brittleness, Elasticity</li>
 *   <li><b>Spiritual</b> — Qi Conductivity, Soul Conductivity, Divine Sense Conductivity,
 *       Restriction Affinity, Blood Affinity, Dao Resonance</li>
 *   <li><b>Elemental</b> — Fire, Water, Earth, Metal, Wood, Lightning, Wind, Yin, Yang,
 *       Death, Life, Karma, Time, Space, Dream (each 0.0-1.0)</li>
 *   <li><b>Historical</b> — Origin, Age, Previous Owners, Refinements, Tribulations</li>
 * </ul>
 *
 * <p>This means:
 * <ul>
 *   <li>Artifact Forge reads material properties to determine artifact type</li>
 *   <li>Alchemy Furnace reads herb properties to determine pill result</li>
 *   <li>Restriction Desk reads conductivity and affinity</li>
 *   <li>Formation Platform reads Qi conductivity</li>
 *   <li>Beast Taming reads bloodline compatibility</li>
 * </ul>
 *
 * <p>All crafting stations use the SAME material data — no separate systems.
 */
public final class MaterialProperties {

    // ═══ Physical Properties ═════════════════════════════════════════
    private double density;        // kg/m³ equivalent (0.1-100.0)
    private double hardness;       // Mohs scale equivalent (0.1-10.0)
    private double brittleness;    // 0.0 (flexible) to 1.0 (shatters easily)
    private double elasticity;     // 0.0 (rigid) to 1.0 (very elastic)

    // ═══ Spiritual Properties ════════════════════════════════════════
    private double qiConductivity;        // 0.0-1.0 (how well Qi flows through it)
    private double soulConductivity;      // 0.0-1.0 (how well soul force flows)
    private double divineSenseConductivity; // 0.0-1.0 (divine sense transparency)
    private double restrictionAffinity;   // 0.0-1.0 (how well it holds restrictions)
    private double bloodAffinity;         // 0.0-1.0 (how well it bonds with blood)
    private double daoResonance;          // 0.0-1.0 (how it resonates with Dao)

    // ═══ Elemental Affinities (each 0.0-1.0) ════════════════════════
    private final Map<Element, Double> elementalAffinities = new EnumMap<>(Element.class);

    // ═══ Historical Properties ═══════════════════════════════════════
    private String origin;           // "natural", "ancient_god", "star_fall", "sect_refined"
    private long ageYears;           // how old the material is
    private final List<String> previousOwners = new ArrayList<>();
    private int refinementCount;     // how many times it's been refined
    private int tribulationCount;    // how many tribulations it's survived

    public enum Element {
        FIRE, WATER, EARTH, METAL, WOOD,
        LIGHTNING, WIND, YIN, YANG,
        DEATH, LIFE, KARMA, TIME, SPACE, DREAM
    }

    public MaterialProperties() {
        // Default: mundane material with no spiritual properties
        this.density = 2.5;
        this.hardness = 3.0;
        this.brittleness = 0.3;
        this.elasticity = 0.1;
        this.qiConductivity = 0.0;
        this.soulConductivity = 0.0;
        this.divineSenseConductivity = 0.0;
        this.restrictionAffinity = 0.0;
        this.bloodAffinity = 0.0;
        this.daoResonance = 0.0;
        this.origin = "natural";
        this.ageYears = 0;
        this.refinementCount = 0;
        this.tribulationCount = 0;
        // Initialize all elements to 0
        for (Element e : Element.values()) {
            elementalAffinities.put(e, 0.0);
        }
    }

    // ═══ Builder Pattern ═════════════════════════════════════════════

    public MaterialProperties density(double v) { this.density = v; return this; }
    public MaterialProperties hardness(double v) { this.hardness = v; return this; }
    public MaterialProperties brittleness(double v) { this.brittleness = v; return this; }
    public MaterialProperties elasticity(double v) { this.elasticity = v; return this; }
    public MaterialProperties qiConductivity(double v) { this.qiConductivity = v; return this; }
    public MaterialProperties soulConductivity(double v) { this.soulConductivity = v; return this; }
    public MaterialProperties divineSenseConductivity(double v) { this.divineSenseConductivity = v; return this; }
    public MaterialProperties restrictionAffinity(double v) { this.restrictionAffinity = v; return this; }
    public MaterialProperties bloodAffinity(double v) { this.bloodAffinity = v; return this; }
    public MaterialProperties daoResonance(double v) { this.daoResonance = v; return this; }
    public MaterialProperties origin(String o) { this.origin = o; return this; }
    public MaterialProperties ageYears(long a) { this.ageYears = a; return this; }
    public MaterialProperties refinementCount(int c) { this.refinementCount = c; return this; }
    public MaterialProperties tribulationCount(int c) { this.tribulationCount = c; return this; }

    public MaterialProperties element(Element e, double value) {
        elementalAffinities.put(e, Math.max(0.0, Math.min(1.0, value)));
        return this;
    }

    public MaterialProperties addOwner(String owner) {
        previousOwners.add(owner);
        return this;
    }

    // ═══ Getters ═════════════════════════════════════════════════════

    public double density() { return density; }
    public double hardness() { return hardness; }
    public double brittleness() { return brittleness; }
    public double elasticity() { return elasticity; }
    public double qiConductivity() { return qiConductivity; }
    public double soulConductivity() { return soulConductivity; }
    public double divineSenseConductivity() { return divineSenseConductivity; }
    public double restrictionAffinity() { return restrictionAffinity; }
    public double bloodAffinity() { return bloodAffinity; }
    public double daoResonance() { return daoResonance; }
    public String origin() { return origin; }
    public long ageYears() { return ageYears; }
    public List<String> previousOwners() { return Collections.unmodifiableList(previousOwners); }
    public int refinementCount() { return refinementCount; }
    public int tribulationCount() { return tribulationCount; }

    public double elementAffinity(Element e) {
        return elementalAffinities.getOrDefault(e, 0.0);
    }

    public Map<Element, Double> elementalAffinities() {
        return Collections.unmodifiableMap(elementalAffinities);
    }

    /**
     * Get the dominant element (highest affinity).
     */
    public Element dominantElement() {
        Element dominant = Element.METAL; // default
        double max = 0.0;
        for (Map.Entry<Element, Double> e : elementalAffinities.entrySet()) {
            if (e.getValue() > max) {
                max = e.getValue();
                dominant = e.getKey();
            }
        }
        return dominant;
    }

    /**
     * Check if this material is compatible with another (for crafting).
     * Compatibility = similar elemental profiles + compatible spiritual properties.
     */
    public double compatibilityWith(MaterialProperties other) {
        // Elemental compatibility
        double elemScore = 0.0;
        for (Element e : Element.values()) {
            double diff = Math.abs(this.elementAffinity(e) - other.elementAffinity(e));
            elemScore += (1.0 - diff);
        }
        elemScore /= Element.values().length;

        // Spiritual compatibility
        double spiritScore = 1.0 - Math.abs(this.qiConductivity - other.qiConductivity);
        spiritScore += 1.0 - Math.abs(this.soulConductivity - other.soulConductivity);
        spiritScore /= 2.0;

        return (elemScore * 0.6) + (spiritScore * 0.4);
    }

    /**
     * Get a summary string for tooltips.
     */
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Physical: H").append(String.format("%.1f", hardness))
          .append(" D").append(String.format("%.1f", density))
          .append(" B").append(String.format("%.1f", brittleness))
          .append(" E").append(String.format("%.1f", elasticity));
        sb.append("\nSpiritual: Qi").append(String.format("%.1f", qiConductivity))
          .append(" Soul").append(String.format("%.1f", soulConductivity))
          .append(" DS").append(String.format("%.1f", divineSenseConductivity));
        if (restrictionAffinity > 0) sb.append(" Restr").append(String.format("%.1f", restrictionAffinity));
        if (bloodAffinity > 0) sb.append(" Blood").append(String.format("%.1f", bloodAffinity));
        if (daoResonance > 0) sb.append(" Dao").append(String.format("%.1f", daoResonance));
        Element dom = dominantElement();
        if (elementAffinity(dom) > 0) {
            sb.append("\nElement: ").append(dom);
        }
        if (ageYears > 100) sb.append("\nAge: ").append(ageYears).append(" years");
        if (refinementCount > 0) sb.append(" | Refined: ").append(refinementCount).append("x");
        if (tribulationCount > 0) sb.append(" | Tribulations: ").append(tribulationCount);
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Material{H=" + String.format("%.1f", hardness) +
                ", Qi=" + String.format("%.1f", qiConductivity) +
                ", " + dominantElement() + "}";
    }
}
