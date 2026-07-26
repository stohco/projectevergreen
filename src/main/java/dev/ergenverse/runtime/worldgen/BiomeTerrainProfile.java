package dev.ergenverse.runtime.worldgen;

import net.minecraft.resources.ResourceLocation;

/**
 * Biome-specific terrain height profile for the {@link BlueprintChunkGenerator}.
 *
 * <p>Each biome on Planet Suzaku is mapped to a profile that defines:
 * <ul>
 *   <li>{@link #baseHeight} — the canonical surface Y for this biome, before
 *       canon warps, biome-scale noise, or fine noise are applied.</li>
 *   <li>{@link #amplitude} — the amplitude (in blocks) of the biome-scale
 *       height noise. Mountains have large amplitude (varied peaks);
 *       plains/oceans have small amplitude (gentle undulation).</li>
 * </ul>
 *
 * <p><b>CRON-COMPLETIONIST-93 — BIOME-AWARE TERRAIN PROFILES.</b>
 *
 * <p>Prior to CRON-93, {@link BlueprintChunkGenerator#canonSurfaceHeight}
 * was a pure function of (x, z) that ignored the biome entirely. A column
 * in a {@code zhao_mountains} biome got the SAME surface height as a column
 * in a {@code zhao_plains} biome — a major visual regression from vanilla
 * {@code minecraft:noise}, where mountains rise to Y=120+ and plains stay
 * at Y=64. The only height variation came from the canon warp (a ±30 block
 * gradient around canon locations) and a tiny ±8 fine noise. Between canon
 * locations, the world was a flat plateau at Y=64 regardless of biome.
 *
 * <p>CRON-93 closes this gap. The new
 * {@link BlueprintChunkGenerator#biomeAwareSurfaceHeight} samples the biome
 * source at (x, z) via {@link net.minecraft.world.level.biome.BiomeSource#getNoiseBiome}
 * (using the {@link net.minecraft.world.level.levelgen.RandomState#sampler()
 * climate sampler}), looks up the {@link BiomeTerrainProfile} for that biome,
 * and uses the profile's {@link #baseHeight} as the surface base instead of
 * the flat {@link BlueprintChunkGenerator#BASE_SURFACE_HEIGHT} (64). The
 * profile's {@link #amplitude} drives a new biome-scale noise layer (period 24,
 * distinct from the existing period-8 fine noise) that produces real mountain
 * peaks and valley undulations within a biome.
 *
 * <p><b>Final surface height formula (CRON-93):</b>
 * <pre>{@code
 *   biomeAwareSurfaceHeight(x, z) =
 *       biomeProfile.baseHeight                  // plains=64, mountains=110, ocean=35
 *     + biomeAmplitudeNoise(x, z, amplitude)     // ±amplitude, period 24
 *     + canonTerrainOffset(x, z)                 // ±30 from canon locations (gradient)
 *     + canonNoiseVariation(x, z)                // ±8 fine noise (period 8)
 *     clamped to [2, 256]
 * }</pre>
 *
 * <p><b>Canon fidelity (fact-checked against 仙逆):</b>
 * <ul>
 *   <li><b>{@code zhao_mountains}</b> (赵国山地) — base 110, amplitude 25.
 *       Canon: Heng Yue Mountain (恒岳山) is "赵国最大的山脉" — Zhao Country's
 *       largest mountain. Vanilla MC mountain peaks reach Y=120-180; base 110
 *       + amplitude 25 + canon warp (+30 for Heng Yue) + fine noise (±8) =
 *       Y=107-173. Matches vanilla mountain height range.</li>
 *
 *   <li><b>{@code zhao_plains}</b> (赵国平原) — base 64, amplitude 4.
 *       Canon: Wang Lin's home village is in a "偏僻小山村" — remote mountain
 *       village, but the surrounding Zhao Country has plains. Vanilla plains
 *       sit at Y=64-70; base 64 + amplitude 4 + fine noise (±8) = Y=52-76.
 *       Matches vanilla plains.</li>
 *
 *   <li><b>{@code sea_of_devils}</b> (修魔海) — base 35, amplitude 3.
 *       Canon: "广阔无边的海域" — vast, boundless sea east of Zhao Country.
 *       Wang Lin spent years (411-470 in the chronology) here. Vanilla ocean
 *       floor sits at Y=30-45; base 35 + amplitude 3 + canon warp (-30 for
 *       Sea of Devils) + fine noise (±8) = Y=0-16. Deeper than vanilla
 *       ocean — appropriate for the "perilous" Sea of Devils. Sea level
 *       (Y=63) is well above the floor, producing a 30-60 block deep ocean.</li>
 *
 *   <li><b>{@code snow_domain_country}</b> (雪域国) — base 95, amplitude 20.
 *       Canon: "雪域之地" — snowy lands, cold country. Wang Lin "直奔雪域国京都"
 *       (rushed to the Snow Domain capital). Snowy countries in MC are
 *       typically high-altitude (cold biomes appear above Y=80 in vanilla).
 *       Base 95 + amplitude 20 + fine noise (±8) = Y=67-123. Elevated
 *       cold highlands — matches canon.</li>
 *
 *   <li><b>{@code xuan_wu_country}</b> (玄武国) — base 80, amplitude 12.
 *       Canon: cold temperate country (Xuan Wu = Black Tortoise, a northern
 *       constellation). Moderate elevation with rolling hills.</li>
 *
 *   <li><b>{@code qing_shui_ruin}</b> (清水废墟) — base 70, amplitude 8.
 *       Canon: "Clear Water Ruin" — abandoned ruin area. Moderate elevation,
 *       temperate. The "ruin" suffix suggests broken terrain but not
 *       extreme height.</li>
 *
 *   <li><b>{@code pilu_kingdom}</b> (毗卢国) — base 70, amplitude 8.
 *       Canon: Pilu Kingdom — temperate cultivation country. Standard
 *       rolling plains elevation.</li>
 *
 *   <li><b>{@code fire_burn_country}</b> (火焚国) — base 75, amplitude 10.
 *       Canon: "Fire Burn Country" — hot country. Slightly elevated (volcanic
 *       terrain implied by the name).</li>
 *
 *   <li><b>{@code vermilion_bird_country}</b> (朱雀国) — base 78, amplitude 10.
 *       Canon: the central ruling country of Planet Suzaku, ruled by the
 *       Vermilion Bird Dynasty. Slightly elevated as befitting the capital
 *       region. Wang Lin later ascends to become the Vermilion Bird.</li>
 *
 *   <li><b>{@code chu_country}</b> (楚国) — base 72, amplitude 10.
 *       Canon: "紧靠着碎石山脉的修真国" — bordered by shattered-stone
 *       mountains. Li Muwan's home country (Cloud Sky Sect / 云天宗 is its
 *       head sect). Moderate elevation with nearby mountain influence.</li>
 *
 *   <li><b>{@code sky_demon_country}</b> (天魔国) — base 95, amplitude 18.
 *       Canon: "Sky Demon Country" — hot mountainous country. Demon sects
 *       typically inhabit rugged terrain. Elevated.</li>
 *
 *   <li><b>{@code fire_demon_country}</b> (火魔国) — base 95, amplitude 18.
 *       Canon: "Fire Demon Country" — hot mountainous demon country.
 *       Elevated, volcanic-adjacent.</li>
 *
 *   <li><b>{@code jue_ming_valley}</b> (决明谷) — base 55, amplitude 5.
 *       Canon: "Valley of Certain Death" — a deathly valley embedded in
 *       Zhao Country. Low-lying. Base 55 + amplitude 5 + fine noise (±8) =
 *       Y=42-68. Below sea level in places (water pools), above in others.</li>
 *
 *   <li><b>{@code jue_ming_valley_depths}</b> (决明谷深处) — base 40, amplitude 4.
 *       Canon: deeper parts of the Valley of Certain Death. Subterranean-
 *       adjacent. Base 40 + amplitude 4 + fine noise (±8) = Y=28-52.
 *       Below sea level — flooded depths.</li>
 *
 *   <li><b>{@code jue_ming_valley_abyss}</b> (决明谷深渊) — base 25, amplitude 3.
 *       Canon: abyssal parts of the Valley of Certain Death. Deepest.
 *       Base 25 + amplitude 3 + fine noise (±8) = Y=14-36. Well below sea
 *       level — dark abyssal waters.</li>
 *
 *   <li><b>default</b> — base 64, amplitude 4. Fallback for unrecognized
 *       biomes (should not occur with the current biome source, but
 *       defensive). Matches {@link BlueprintChunkGenerator#BASE_SURFACE_HEIGHT}.</li>
 * </ul>
 *
 * <p><b>Amplitude noise design:</b> The biome-scale amplitude noise uses a
 * period of 24 blocks ({@link BlueprintChunkGenerator#BIOME_NOISE_PERIOD}),
 * distinct from the existing fine noise's period of 8. This produces
 * mountain-scale features (a 24-block period means peaks/valleys every
 * ~12 blocks, large enough to feel like real mountains, small enough to
 * vary within a single chunk). The hash function is the same splitmix64
 * mixer used by the fine noise, seeded by the same
 * {@link dev.ergenverse.spawn.DeterministicSeedHandler#CANON_SEED}, so
 * the variation is deterministic and identical every save.
 *
 * <p><b>Why two noise layers (biome amplitude + fine noise) instead of one:</b>
 * A single noise layer with biome-aware amplitude would either have too
 * short a period (mountains would look like jagged spikes) or too long a
 * period (plains would have unrealistic rolling hills). Two layers with
 * different periods — biome amplitude (period 24, large amplitude for
 * mountains) + fine noise (period 8, small amplitude everywhere) — produce
 * realistic terrain at both scales: mountains have large-scale peaks with
 * fine surface roughness, plains have gentle rolling with fine detail.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
 *
 * @see BlueprintChunkGenerator#biomeAwareSurfaceHeight
 * @see BlueprintChunkGenerator#BIOME_NOISE_PERIOD
 */
public record BiomeTerrainProfile(int baseHeight, int amplitude) {

    /**
     * Look up the biome terrain profile for a given biome id.
     *
     * @param biomeId the registry id of the biome (e.g.
     *                {@code "ergenverse:zhao_mountains"})
     * @return the profile for that biome, or the default profile
     *         (base 64, amplitude 4) if the biome is not recognized
     */
    public static BiomeTerrainProfile forBiome(ResourceLocation biomeId) {
        if (biomeId == null) return DEFAULT;
        String path = biomeId.getPath();
        return switch (path) {
            // Mountains — tall, varied peaks
            case "zhao_mountains" -> new BiomeTerrainProfile(110, 25); // 赵国山地
            case "snow_domain_country" -> new BiomeTerrainProfile(95, 20); // 雪域国
            case "sky_demon_country" -> new BiomeTerrainProfile(95, 18); // 天魔国
            case "fire_demon_country" -> new BiomeTerrainProfile(95, 18); // 火魔国

            // Cold temperate — moderate highlands
            case "xuan_wu_country" -> new BiomeTerrainProfile(80, 12); // 玄武国

            // Temperate countries — rolling plains
            case "chu_country" -> new BiomeTerrainProfile(72, 10); // 楚国
            case "vermilion_bird_country" -> new BiomeTerrainProfile(78, 10); // 朱雀国
            case "fire_burn_country" -> new BiomeTerrainProfile(75, 10); // 火焚国
            case "qing_shui_ruin" -> new BiomeTerrainProfile(70, 8); // 清水废墟
            case "pilu_kingdom" -> new BiomeTerrainProfile(70, 8); // 毗卢国

            // Plains — flat
            case "zhao_plains" -> new BiomeTerrainProfile(64, 4); // 赵国平原

            // Sea of Devils — below sea level (oceanic)
            case "sea_of_devils" -> new BiomeTerrainProfile(35, 3); // 修魔海

            // Jue Ming Valley — low-lying death valley (descending tiers)
            case "jue_ming_valley" -> new BiomeTerrainProfile(55, 5); // 决明谷
            case "jue_ming_valley_depths" -> new BiomeTerrainProfile(40, 4); // 决明谷深处
            case "jue_ming_valley_abyss" -> new BiomeTerrainProfile(25, 3); // 决明谷深渊

            // Default — matches BASE_SURFACE_HEIGHT (legacy fallback)
            default -> DEFAULT;
        };
    }

    /** Default profile — matches the pre-CRON-93 flat behavior (base 64, gentle noise). */
    public static final BiomeTerrainProfile DEFAULT = new BiomeTerrainProfile(64, 4);
}
