package dev.ergenverse.simulation;

import dev.ergenverse.core.Ergenverse;

/**
 * LocationLayerSeeder — seeds the canonical RI locations with their
 * 15-layer components at world bootstrap.
 *
 * <p>For each canon location (Mosquito Valley, Heng Yue, Sea of Devils,
 * Ancient God Battlefield, Suzaku Tomb, Zhao Country, plus the second-tier
 * canon locations), this seeder writes a {@link LocationLayers} instance
 * into the {@link LocationLayerRegistry}.
 *
 * <p>Layer values are short semantic strings (the registry stores strings;
 * richer types can be added per-layer later). For example, the FLORA layer
 * of Mosquito Valley = "high:spirit_grass,moderate:blood_forgetting_grass".
 */
public final class LocationLayerSeeder {

    private LocationLayerSeeder() {}

    public static void seed() {
        seedMosquitoValley();
        seedHengYueMountain();
        seedSeaOfDevils();
        seedAncientGodBattlefield();
        seedSuzakuTomb();
        seedZhaoCountry();
        seedCloudSea();      // Meng Hao crossover reference
        seedReincarnationRealm();  // Wang Lin's reincarnation-stage territory

        Ergenverse.LOGGER.info("[Ergenverse] LocationLayerSeeder seeded canon locations.");
    }

    private static void seedMosquitoValley() {
        LocationLayers l = new LocationLayers();
        l.put(LocationLayers.LayerId.PHYSICAL_TERRAIN,   "swamp:low_elevation,dense_undergrowth");
        l.put(LocationLayers.LayerId.SPIRITUAL_QI,       "qi_density=0.45,vein=sv_mosquito");
        l.put(LocationLayers.LayerId.FLORA,              "high:spirit_grass,moderate:blood_forgetting_grass");
        l.put(LocationLayers.LayerId.FAUNA,              "high:herbivore,moderate:predator,swarm:mosquito");
        l.put(LocationLayers.LayerId.WEATHER,            "humid,frequent_fog");
        l.put(LocationLayers.LayerId.FORMATIONS,         "none");
        l.put(LocationLayers.LayerId.RESTRICTIONS,       "weak:blood_binding");
        l.put(LocationLayers.LayerId.OWNERSHIP,          "unclaimed");
        l.put(LocationLayers.LayerId.HISTORY,            "Wang Lin obtained mosquito swarm");
        l.put(LocationLayers.LayerId.SOCIAL,             "none");
        l.put(LocationLayers.LayerId.ECONOMIC,           "none");
        l.put(LocationLayers.LayerId.KARMIC,             "moderate:beast_slaughter");
        l.put(LocationLayers.LayerId.DAO_RESIDUE,        "faint:slaughter_dao");
        l.put(LocationLayers.LayerId.DIVINE_SENSE_ECHOES,"none");
        l.put(LocationLayers.LayerId.STORY,              "canon:wang_lin_origin");
        LocationLayerRegistry.register("mosquito_valley", l);
    }

    private static void seedHengYueMountain() {
        LocationLayers l = new LocationLayers();
        l.put(LocationLayers.LayerId.PHYSICAL_TERRAIN,   "mountain:peaks,terraces");
        l.put(LocationLayers.LayerId.SPIRITUAL_QI,       "qi_density=0.55,vein=sv_heng_yue");
        l.put(LocationLayers.LayerId.FLORA,              "moderate:qi_gathering_grass,moderate:sword_edge_moss");
        l.put(LocationLayers.LayerId.FAUNA,              "moderate:herbivore,low:predator");
        l.put(LocationLayers.LayerId.WEATHER,            "clear,seasonal_rain");
        l.put(LocationLayers.LayerId.FORMATIONS,         "active:sect_protection_array");
        l.put(LocationLayers.LayerId.RESTRICTIONS,       "moderate:sect_seal");
        l.put(LocationLayers.LayerId.OWNERSHIP,          "owned:heng_yue_faction");
        l.put(LocationLayers.LayerId.HISTORY,            "Heng Yue Faction founded");
        l.put(LocationLayers.LayerId.SOCIAL,             "active:outer_disciples,inner_disciples,elders");
        l.put(LocationLayers.LayerId.ECONOMIC,           "active:spirit_stone_trade,herb_market");
        l.put(LocationLayers.LayerId.KARMIC,             "low");
        l.put(LocationLayers.LayerId.DAO_RESIDUE,        "moderate:sect_leader_dao");
        l.put(LocationLayers.LayerId.DIVINE_SENSE_ECHOES,"active:patrol_sweep");
        l.put(LocationLayers.LayerId.STORY,              "canon:wang_lin_first_sect");
        LocationLayerRegistry.register("heng_yue_mountain", l);
    }

    private static void seedSeaOfDevils() {
        LocationLayers l = new LocationLayers();
        l.put(LocationLayers.LayerId.PHYSICAL_TERRAIN,   "swamp:poisoned_water,dark_mist");
        l.put(LocationLayers.LayerId.SPIRITUAL_QI,       "qi_density=0.65,vein=sv_devils,corrupt");
        l.put(LocationLayers.LayerId.FLORA,              "low:poison_grass");
        l.put(LocationLayers.LayerId.FAUNA,              "low:herbivore,high:predator,high:apex_devil");
        l.put(LocationLayers.LayerId.WEATHER,            "miasma,acid_rain");
        l.put(LocationLayers.LayerId.FORMATIONS,         "active:devil_seal");
        l.put(LocationLayers.LayerId.RESTRICTIONS,       "strong:devil_pact");
        l.put(LocationLayers.LayerId.OWNERSHIP,          "contested:devil_cultivators");
        l.put(LocationLayers.LayerId.HISTORY,            "Devil cultivators entrenched");
        l.put(LocationLayers.LayerId.SOCIAL,             "active:devil_sects");
        l.put(LocationLayers.LayerId.ECONOMIC,           "active:poison_pill_trade");
        l.put(LocationLayers.LayerId.KARMIC,             "high:demonic_karma");
        l.put(LocationLayers.LayerId.DAO_RESIDUE,        "strong:devil_dao");
        l.put(LocationLayers.LayerId.DIVINE_SENSE_ECHOES,"active:devil_scouts");
        l.put(LocationLayers.LayerId.STORY,              "canon:wang_lin_devil_arc");
        LocationLayerRegistry.register("sea_of_devils", l);
    }

    private static void seedAncientGodBattlefield() {
        LocationLayers l = new LocationLayers();
        l.put(LocationLayers.LayerId.PHYSICAL_TERRAIN,   "wasteland:cracked_reality");
        l.put(LocationLayers.LayerId.SPIRITUAL_QI,       "qi_density=0.75,vein=sv_ancient_god");
        l.put(LocationLayers.LayerId.FLORA,              "none");
        l.put(LocationLayers.LayerId.FAUNA,              "low:herbivore,moderate:ancient_predator,high:apex");
        l.put(LocationLayers.LayerId.WEATHER,            "reality_storms,spatial_tears");
        l.put(LocationLayers.LayerId.FORMATIONS,         "broken:ancient_god_formation");
        l.put(LocationLayers.LayerId.RESTRICTIONS,       "ancient:reality_seal_weakening");
        l.put(LocationLayers.LayerId.OWNERSHIP,          "unclaimed");
        l.put(LocationLayers.LayerId.HISTORY,            "Ancient God fell here");
        l.put(LocationLayers.LayerId.SOCIAL,             "none");
        l.put(LocationLayers.LayerId.ECONOMIC,           "none");
        l.put(LocationLayers.LayerId.KARMIC,             "extreme:ancient_karma");
        l.put(LocationLayers.LayerId.DAO_RESIDUE,        "extreme:ancient_god_dao");
        l.put(LocationLayers.LayerId.DIVINE_SENSE_ECHOES,"intermittent:ancient_resonance");
        l.put(LocationLayers.LayerId.STORY,              "canon:wang_lin_ancient_god_arc");
        LocationLayerRegistry.register("ancient_god_battlefield", l);
    }

    private static void seedSuzakuTomb() {
        LocationLayers l = new LocationLayers();
        l.put(LocationLayers.LayerId.PHYSICAL_TERRAIN,   "tomb:sealed_interior");
        l.put(LocationLayers.LayerId.SPIRITUAL_QI,       "qi_density=0.85,vein=sv_suzaku");
        l.put(LocationLayers.LayerId.FLORA,              "none");
        l.put(LocationLayers.LayerId.FAUNA,              "none,sealed_apex:suzaku_fragment");
        l.put(LocationLayers.LayerId.WEATHER,            "still");
        l.put(LocationLayers.LayerId.FORMATIONS,         "active:suzaku_seal");
        l.put(LocationLayers.LayerId.RESTRICTIONS,       "extreme:suzaku_restriction");
        l.put(LocationLayers.LayerId.OWNERSHIP,          "sealed");
        l.put(LocationLayers.LayerId.HISTORY,            "Suzaku's inheritance sealed here");
        l.put(LocationLayers.LayerId.SOCIAL,             "none");
        l.put(LocationLayers.LayerId.ECONOMIC,           "none");
        l.put(LocationLayers.LayerId.KARMIC,             "extreme:suzaku_karma");
        l.put(LocationLayers.LayerId.DAO_RESIDUE,        "extreme:suzaku_fire_dao");
        l.put(LocationLayers.LayerId.DIVINE_SENSE_ECHOES,"none:seal_blocks");
        l.put(LocationLayers.LayerId.STORY,              "canon:wang_lin_suzaku_inheritance");
        LocationLayerRegistry.register("suzaku_tomb", l);
    }

    private static void seedZhaoCountry() {
        LocationLayers l = new LocationLayers();
        l.put(LocationLayers.LayerId.PHYSICAL_TERRAIN,   "plains:rolling_hills,rivers");
        l.put(LocationLayers.LayerId.SPIRITUAL_QI,       "qi_density=0.15,vein=sv_zhao");
        l.put(LocationLayers.LayerId.FLORA,              "moderate:mortal_crops,low:spirit_herb");
        l.put(LocationLayers.LayerId.FAUNA,              "high:mortal_animals,low:spirit_beast");
        l.put(LocationLayers.LayerId.WEATHER,            "seasonal_temperate");
        l.put(LocationLayers.LayerId.FORMATIONS,         "weak:village_wards");
        l.put(LocationLayers.LayerId.RESTRICTIONS,       "none");
        l.put(LocationLayers.LayerId.OWNERSHIP,          "owned:zhao_dynasty");
        l.put(LocationLayers.LayerId.HISTORY,            "Wang Lin's mortal homeland");
        l.put(LocationLayers.LayerId.SOCIAL,             "active:mortal_villages,nobility");
        l.put(LocationLayers.LayerId.ECONOMIC,           "active:joss_flame_economy,grain_trade");
        l.put(LocationLayers.LayerId.KARMIC,             "low");
        l.put(LocationLayers.LayerId.DAO_RESIDUE,        "none");
        l.put(LocationLayers.LayerId.DIVINE_SENSE_ECHOES,"none");
        l.put(LocationLayers.LayerId.STORY,              "canon:wang_lin_origin_country");
        LocationLayerRegistry.register("zhao_country", l);
    }

    private static void seedCloudSea() {
        LocationLayers l = new LocationLayers();
        l.put(LocationLayers.LayerId.PHYSICAL_TERRAIN,   "sky:cloud_sea,floating_peaks");
        l.put(LocationLayers.LayerId.SPIRITUAL_QI,       "qi_density=0.70,vein=sv_cloud_sea");
        l.put(LocationLayers.LayerId.FLORA,              "moderate:cloud_lotus");
        l.put(LocationLayers.LayerId.FAUNA,              "moderate:sky_beasts");
        l.put(LocationLayers.LayerId.WEATHER,            "perpetual_clouds");
        l.put(LocationLayers.LayerId.FORMATIONS,         "active:cloud_seal");
        l.put(LocationLayers.LayerId.RESTRICTIONS,       "moderate");
        l.put(LocationLayers.LayerId.OWNERSHIP,          "owned:cloud_sea_immortals");
        l.put(LocationLayers.LayerId.HISTORY,            "Crossover reference with Meng Hao arc");
        l.put(LocationLayers.LayerId.SOCIAL,             "active:immortal_sects");
        l.put(LocationLayers.LayerId.ECONOMIC,           "active:immortal_pill_trade");
        l.put(LocationLayers.LayerId.KARMIC,             "moderate");
        l.put(LocationLayers.LayerId.DAO_RESIDUE,        "strong:wind_dao");
        l.put(LocationLayers.LayerId.DIVINE_SENSE_ECHOES,"active");
        l.put(LocationLayers.LayerId.STORY,              "canon:crossover_arc");
        LocationLayerRegistry.register("cloud_sea", l);
    }

    private static void seedReincarnationRealm() {
        LocationLayers l = new LocationLayers();
        l.put(LocationLayers.LayerId.PHYSICAL_TERRAIN,   "void:reincarnation_bridge");
        l.put(LocationLayers.LayerId.SPIRITUAL_QI,       "qi_density=0.95,vein=sv_reincarnation");
        l.put(LocationLayers.LayerId.FLORA,              "none");
        l.put(LocationLayers.LayerId.FAUNA,              "none");
        l.put(LocationLayers.LayerId.WEATHER,            "void");
        l.put(LocationLayers.LayerId.FORMATIONS,         "active:reincarnation_formation");
        l.put(LocationLayers.LayerId.RESTRICTIONS,       "extreme:reincarnation_law");
        l.put(LocationLayers.LayerId.OWNERSHIP,          "owned:world_will");
        l.put(LocationLayers.LayerId.HISTORY,            "Wang Lin's reincarnation-stage territory");
        l.put(LocationLayers.LayerId.SOCIAL,             "none");
        l.put(LocationLayers.LayerId.ECONOMIC,           "none");
        l.put(LocationLayers.LayerId.KARMIC,             "extreme");
        l.put(LocationLayers.LayerId.DAO_RESIDUE,        "extreme:reincarnation_dao");
        l.put(LocationLayers.LayerId.DIVINE_SENSE_ECHOES,"world_will");
        l.put(LocationLayers.LayerId.STORY,              "canon:wang_lin_late_arc");
        LocationLayerRegistry.register("reincarnation_realm", l);
    }
}
