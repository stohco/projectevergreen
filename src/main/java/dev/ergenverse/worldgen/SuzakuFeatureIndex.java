package dev.ergenverse.worldgen;

import java.util.List;
import java.util.Map;

/**
 * Suzaku Feature Index — a static lookup of which canon spirit-herb and spirit-vein
 * placed features appear in which Planet Suzaku biome.
 *
 * <p><b>What this class is:</b> A pure data index. It maps biome id (e.g.
 * {@code ergenverse:zhao_plains}) to the list of placed feature ids that should
 * appear in that biome. It is consumed by the (future) perception system:
 * a cultivator at a higher tier perceives more herb types in the same biome.
 * A mortal sees only the vanilla features; a Core Formation cultivator perceives
 * the spirit herbs; a Soul Formation cultivator perceives the spirit vein ore
 * directly (the "vein" itself, not just the visible herb above it).</p>
 *
 * <p><b>What this class is NOT:</b> It does NOT register features. All feature
 * registration is data-driven via JSON files in
 * {@code data/ergenverse/worldgen/configured_feature/} and
 * {@code data/ergenverse/worldgen/placed_feature/}. The biome JSONs reference
 * these placed features directly in their {@code features} array (proper 1.20.1
 * list-of-lists format, one inner list per generation step).</p>
 *
 * <p><b>Prime Directive compliance:</b> The features exist objectively. A mortal
 * walking through Fire Burn Country sees the orange flowers (vanilla
 * {@code orange_tulip} stand-in) — they simply don't recognize them as
 * "Fire-Bloom Lotus" or understand their fire-aspect Qi. Cultivation changes
 * perception, not existence.</p>
 *
 * <h2>Canon Source</h2>
 * <ul>
 *   <li>{@code CANON_RI_ECOLOGY.md} 1.3 — Zhao Country flora (Type C, conf 3):
 *       Qi-Gathering Grass, Foundation-Root Vine, Sword-Edge Moss.</li>
 *   <li>{@code CANON_RI_ECOLOGY.md} 2.3 — Planet Suzaku flora (Type C, conf 3):
 *       Fire-Bloom Lotus (Fire Burn), Snow-Heart Herb (Snow Domain),
 *       Demon-Corpse Mushroom (Sky/Fire Demon), Vermilion-Blood Ginseng
 *       (Vermilion Bird Country).</li>
 *   <li>{@code CANON_RI_ECOLOGY.md} 2.2 — Spirit Vein Status: ~200-400 minor
 *       spirit veins across Planet Suzaku. The Cultivation Planet Crystal
 *       (supreme) is sealed inside Suzaku Tomb — not a surface feature.</li>
 * </ul>
 *
 * <h2>Perception Tier Model (future system)</h2>
 * <ul>
 *   <li><b>Mortal (no cultivation):</b> sees vanilla features only. Spirit herbs
 *       appear as their vanilla stand-in (sea_pickle, lily_of_the_valley, etc.)
 *       — the player doesn't know they're special.</li>
 *   <li><b>Qi Condensation:</b> recognizes Qi-Gathering Grass, Snow-Heart Herb,
 *       Fire-Bloom Lotus — the basic herbs. Tooltip shows canon name.</li>
 *   <li><b>Foundation Establishment:</b> recognizes Foundation-Root Vine,
 *       Sword-Edge Moss, Demon-Corpse Mushroom.</li>
 *   <li><b>Core Formation:</b> recognizes Vermilion-Blood Ginseng.</li>
 *   <li><b>Nascent Soul+:</b> perceives spirit vein ore directly (sees the
 *       quartz_ore stand-in as a "minor spirit vein" — the Qi pathway
 *       underground, not just the ore block).</li>
 * </ul>
 *
 * @see dev.ergenverse.world.WorldLaws — Dao affinities per country determine
 *      which herbs grow where. Fire Burn has fire 1.5x so fire herbs grow there;
 *      Snow Domain has fire 0.4x so fire herbs fail.
 */
public final class SuzakuFeatureIndex {

    private SuzakuFeatureIndex() {}

    /**
     * Map of biome id -> list of placed feature ids (including vanilla + ergenverse:).
     * Populated at class init from the canon mapping. Used by the perception system.
     *
     * <p>Note: this is the FULL list of ergenverse features per biome, not the
     * vanilla ones (those are referenced directly in the biome JSON). The
     * perception system uses this to determine which ergenverse features a
     * cultivator of tier T can perceive.</p>
     */
    private static final Map<String, List<String>> BIOME_FEATURES = Map.ofEntries(
            Map.entry("zhao_plains", List.of(
                    "ergenverse:qi_gathering_grass_placed",
                    "ergenverse:spirit_vein_ore_placed"
            )),
            Map.entry("zhao_mountains", List.of(
                    "ergenverse:foundation_root_vine_placed",
                    "ergenverse:sword_edge_moss_placed",
                    "ergenverse:spirit_vein_ore_placed"
            )),
            Map.entry("chu_country", List.of(
                    "ergenverse:foundation_root_vine_placed",
                    "ergenverse:qi_gathering_grass_placed",
                    "ergenverse:spirit_vein_ore_placed"
            )),
            Map.entry("fire_burn_country", List.of(
                    "ergenverse:fire_bloom_lotus_placed",
                    "ergenverse:spirit_vein_ore_placed"
            )),
            Map.entry("fire_demon_country", List.of(
                    "ergenverse:demon_corpse_mushroom_placed",
                    "ergenverse:spirit_vein_ore_placed"
            )),
            Map.entry("sky_demon_country", List.of(
                    "ergenverse:demon_corpse_mushroom_placed",
                    "ergenverse:spirit_vein_ore_placed"
            )),
            Map.entry("pilu_kingdom", List.of(
                    "ergenverse:demon_corpse_mushroom_placed",
                    "ergenverse:spirit_vein_ore_placed"
            )),
            Map.entry("snow_domain_country", List.of(
                    "ergenverse:snow_heart_herb_placed",
                    "ergenverse:spirit_vein_ore_placed"
            )),
            Map.entry("xuan_wu_country", List.of(
                    "ergenverse:foundation_root_vine_placed",
                    "ergenverse:spirit_vein_ore_placed"
            )),
            Map.entry("vermilion_bird_country", List.of(
                    "ergenverse:vermilion_blood_ginseng_placed",
                    "ergenverse:foundation_root_vine_placed",
                    "ergenverse:spirit_vein_ore_placed"
            )),
            Map.entry("qing_shui_ruin", List.of(
                    "ergenverse:demon_corpse_mushroom_placed",
                    "ergenverse:spirit_vein_ore_placed"
            )),
            Map.entry("jue_ming_valley", List.of(
                    "ergenverse:foundation_root_vine_placed",
                    "ergenverse:demon_corpse_mushroom_placed",
                    "ergenverse:spirit_vein_ore_placed"
            )),
            Map.entry("sea_of_devils", List.of(
                    "ergenverse:demon_corpse_mushroom_placed",
                    "ergenverse:spirit_vein_ore_placed"
            )),
            Map.entry("forest_of_distorted_sense", List.of(
                    "ergenverse:demon_corpse_mushroom_placed",
                    "ergenverse:spirit_vein_ore_placed"
            ))
    );

    /**
     * Get the placed features registered for a given biome.
     * @param biomeId the biome's ResourceLocation string (e.g. "ergenverse:zhao_plains")
     * @return immutable list of placed feature ids (never null; empty if unknown biome)
     */
    public static List<String> featuresForBiome(String biomeId) {
        return BIOME_FEATURES.getOrDefault(biomeId, List.of());
    }

    /**
     * Get the spirit herb features (excluding spirit vein ore) for a biome.
     * Used by the perception system: a cultivator of tier T perceives herbs up
     * to tier T.
     * @param biomeId the biome's ResourceLocation string
     * @return immutable list of herb placed feature ids
     */
    public static List<String> herbsForBiome(String biomeId) {
        return BIOME_FEATURES.getOrDefault(biomeId, List.of()).stream()
                .filter(id -> !id.contains("spirit_vein_ore"))
                .toList();
    }

    /**
     * Whether a biome contains spirit vein ore (canon: all Planet Suzaku biomes do).
     * @param biomeId the biome's ResourceLocation string
     * @return true if spirit vein ore generates in this biome
     */
    public static boolean hasSpiritVein(String biomeId) {
        return BIOME_FEATURES.getOrDefault(biomeId, List.of())
                .stream().anyMatch(id -> id.contains("spirit_vein_ore"));
    }
}
