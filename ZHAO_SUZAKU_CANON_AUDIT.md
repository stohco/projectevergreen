# Zhao → Planet Suzaku Canon Completeness Audit

**Task ID:** A
**Agent:** canon-audit
**Scope:** Renegade Immortal ONLY — Zhao Country (Wang Lin as mortal) → Heng Yue Sect → Sea of Devils → Demon Spirit Land → Planet Suzaku → leaving Suzaku (ch. ~901 karmic completion).
**Audit date:** 2026-07-12
**Canon sources audited:** 7 docs (`CANON_RI_COMPLETE_WORLD.md`, `CANON_RI_TIMELINE.md`, `CANON_RI_COMPLETE_ITEMS.md`, `CANON_RI_COMPLETE_TECHNIQUES.md`, `CANON_RI_ECOLOGY.md`, `CANON_RI_CIVILIZATION.md`, `CANON_RI_EDGE_OF_CANON.md`)
**Implementation sources audited:** `ri_canon_database.json` (632 entries), `npcs/` (152 files), `species/` (116 + 6 BTT), `worldgen/biome/` (330), `worldgen/structure/` (257), `species_variants/` (921), `provenance/` (179), `WangLinItems.java` (303 bridge mappings), `CanonicalInventory.java` + 17 sibling registries (555 total canonical entries), `wanglin_arsenal_manifest.json` (309 entries).

---

## Executive Summary

The Zhao → Suzaku arc is **~93% canon-complete**. The mod has implemented the vast majority of named characters, locations, techniques, and artifacts from the canon docs. Notable gaps:

- **5 named NPCs missing** (Li Qiqing, Dao Master Blue Dream, 2nd-Gen Vermilion Bird, Nian Tian, Ling'er)
- **3 critical Zhao→Suzaku arc locations missing biome/structure implementations** (Demon Spirit Land, Immortal Emperor's Cave Mansion, Chaotic Broken Stars as a distinct biome)
- **2 endgame Suzaku-arc realms missing biome/structure implementations** (Thunder Celestial Realm, Thunder Celestial Temple)
- **16 deceased antagonists have `location: "unknown"` instead of `deceased`** (accuracy issue vs the Edge-of-Canon framing)
- **24 of 116 species lack the standard 10 life-state variants** (regional/unique creatures only)
- **6 duplicate entries in the arsenal manifest** (extreme_*_dao ×4, heavenly_fate_finger, heavenly_bull_bead)

The Wang Lin Manifestation companion is correctly implemented as the player's gateway to Wang Lin's lineage (per IR-14 of the Edge-of-Canon doc). All 178 DB artifacts have provenance files; 303/303 unique arsenal manifest IDs are bridged to canonical registries; all 309 arsenal items have model+texture assets.

---

## Section 1 — Fully Implemented

### 1A. Characters (152 of 161 DB entries have NPC JSONs — 94.4%)

All major Zhao→Suzaku arc characters are implemented as `npc_*.json` files with the standard schema (`canon_id`, `name`, `nameCn`, `type`, `faction`, `location`, `cultivation`, `personality`, `speech`, `relationship_to_wanglin`, `dialogue_available`, `quest_available`, `trade_available`, `teaching_available`, `perception_tiers`, `canon_confidence`, `derivation_type`, `salt`, `dao_heart`, `soul_state`, `tribulation_debt`).

**Protagonist & family (verified):**
- N01 Wang Lin → `npc_wang_lin_manifestation.json` (intentional — Wang Lin is Transcendent; his manifestation is the player's companion per IR-14)
- N02 Wang Tianshui (father) ✓
- N03 Zhou Tingsu (mother) ✓
- N04 Wang Tianshan (Fourth Uncle) ✓
- N05 Wang Zhuo (cousin) ✓
- N06 Wang Hao (cousin) ✓
- N07 Wang Ping (son) ✓
- N08 Wang Yiyi (daughter) ✓
- N09 Wang Jiduo (adopted son) ✓
- N10 Zhou Ru (adopted daughter) ✓
- N11 Qing Yi (daughter-in-law) ✓
- N12 Song Yu (daughter-in-law) ✓
- N15 Teng Xiuxiu ✓

**Spouses:**
- N17 Li Muwan ✓
- N18 Li Qianmei ✓
- N19 Mu Bingmei / Liu Mei ✓

**Mentors:**
- N20 Situ Nan ✓
- N21 All-Seer ✓
- N22 Tu Si ✓
- N23 Du Tian ✓
- N24 Bai Fan ✓
- N25 Lu Yun ✓
- N26 Qing Lin ✓
- N27 Su Dao (mentor) ✓
- N28 Xuan Luo ✓

**Allies (Zhao→Suzaku arc, verified):**
- N30 Qing Shui, N31 Zhou Yi, N32 Qing Shuang, N33 Chi Hu, N34 Qiu Siping, N35 Mo Ling, N36 Mo Lihai, N37 Sun Tai, N38 Li Yuan, N39 Ling Tianhou, N40 Bei Lou, N41 Wang Wei, N42 Hu Juan, N44 Ta Shan, N45 Big Head Cultivator, N46 Lei Ji, N47 Liu Jinbiao, N48 Ling Dong, N49 Zhou Jin, N50 Zhong Dahong, N51 Daoist Scattered Spirit, N52 (MISSING — see §3), N53 Master Hong Shan, N54 Master South Cloud, N55 Twin Great Heavenly Venerables, N56 Tan Lang, N57 Hong Die, N58 Zhou Wutai, N59 Yun Quezi, N60 Mo Zhi, N61 Lian Daofei, N62 Xu Liguo, N63 Tuo Sen, N64 Xu Yunshan, N65 Ouyang Hua, N66 Li Dannan, N67 Bai Wei, N68 Master Flamespark, N69 Russell, N70 Zhao Yu, N71 Lu Yanfei, N72 Xu Yun, N73 Lu Yuncong, N74 Song Wude, N75 Rudy, N76 Ji Si, N77 Jiu Di, N78 Sea Child Celestial Venerable, N79 Ye Wuyou, N80 Qian Pinghai, N81 3rd-Gen Vermilion Bird, N82 14th-Gen Vermilion Bird ✓

**Antagonists (Zhao→Suzaku arc, verified):**
- N83 Teng Huayuan, N84 Teng Li, N85 Sun Dazhu, N86 Old Man Ji Mo, N87 Gao Qiming, N88 Punnan Zi / Lou Hou, N89 Lin Yi, N90 Duanmu Ji, N91 Hunchback Meng, N92 Xu Liqing, N93 Gun Lan, N94 Wang Qingyue, N95 Yun Fei, N96 Qian Kun, N97 Master Void, N98 Blood Ancestor (Yao Xinghai), N99 Yao Xixue, N100 Wind Demon, N101 Yao Family (faction, not individual — see §4), N102 Daoist Water, N103 Wu Qing, N104 Master Ashen Pine, N105 Master Cloud Soul, N106 Noble Money, N107 Cang Songzi, N108 Lu Fuzi, N109 Ye Dao, N110 Lian Daozhen, N111 Yan Leizi, N112 Su Dao (antagonist — see §5 — alias confusion with N27), N113 Tianyunzi, N114 Seven-Colored Daoist, N115 Old Man Miesheng, N116 Taga, N117 Gu Dao, N118 Song Tian Great Celestial Venerable, N119 Dao Ancient Great Celestial Venerable, N120 Dao Yi Great Celestial Venerable, N121 Imperial Preceptor, N122 Dao Devil Sect Master, N123 Du Qing, N124 Kang Ren, N125 Purple Dawn Immortal Emperor, N126 White Tiger General, N127 Yun Yifeng, N128 Lord of the Sealed Realm / Palm Venerable, N129 Zhan Laogui, N130 Yun Kong, N131 Zhan Xingye, N132 Green Devil ✓

**Disciples:**
- N133 Thirteen, N134 Huo Pao, N135 Xie Qing, N136 Xi Zifeng ✓

**Other / sect-rivals / pets:**
- N137 Adai, N138 Wu Yu, N139 Ye Zi, N140 Zhao Xingsha, N141 Sima Rufeng, N142 Zhao Xinming, N143 Chen Tao, N144 Wang Zhou, N145 Wang Jie, N146 Sun Zhenwei, N147 Chen Bailiang, N148 Zhang Hu, N149 Hui Bing, N150 Zhou Rui, N151 Leng Sheng, N152 Da Niu, N153 Zhou Lin ✓
- N154 Xiao Bai, N155 Mosquito Beast, N156 Thunder Toad, N157 Thunder Celestial Beast, N158 Nether Beast, N159 Brilliant Void, N160 Golden Exalt Sea Dragon ✓

### 1B. Locations — Countries (11/11 verified)

All 11 Planet Suzaku countries have full biome sets (~22 sub-biomes per country = ~242 biomes):

- L23 Country of Zhao ✓ (`zhao_*` 32 biomes including `zhao_mountains`, `zhao_plains`)
- L24 Chu Country ✓ (`chu_*` 22 biomes)
- L25 Fire Burn Country ✓ (`fire_burn_*` 22 biomes)
- L26 Sky Demon Country ✓ (`sky_demon_*` 22 biomes)
- L27 Pilu Kingdom ✓ (`pilu_*` 22 biomes)
- L28 Snow Domain Country ✓ (`snow_domain_*` 22 biomes)
- L29 Xuan Wu Country ✓ (`xuan_wu_*` 22 biomes)
- L30 Fire Demon Country ✓ (`fire_demon_*` 22 biomes)
- L31 Vermilion Bird Country ✓ (`vermilion_bird_*` 22 biomes)
- L32 Great Wang Dynasty ✓ (`great_wang_*` 22 biomes)
- L33 Qing Shui Kingdom ✓ (`qing_shui_*` 22 biomes)

### 1C. Locations — Cities/Villages/Regions (8 of 11 verified with full structure sets)

Each verified city has 12 district sub-structures (`_city_gate`, `_cultivator_quarter`, `_governor_mansion`, `_market_district`, `_mortal_quarter`, `_port_docks`, `_residential_district`, `_smuggler_tunnels`, `_tavern_district`, `_temple_district`, `_warehouse_district`) + a parent structure file:

- L34 Wang Family Village ✓ (`wang_family_village*.json` × 12)
- L35 Tian Shui City ✓ (`tian_shui_city*.json` × 12)
- L36 Teng Family City ✓ (`teng_family_city*.json` × 12)
- L37 Nan Dou City ✓ (`nan_dou_city*.json` × 12)
- L38 Qilin City ✓ (`qilin_city*.json` × 12)
- L39 Ancient Demon City ✓ (`ancient_demon_city*.json` × 12)
- L43 Soul Refining Tribe ✓ (`soul_refining_sect*.json` × 22 sub-structures)
- L44 Mountain Valley Tribe ✓ (covered by `soul_refining_ancestral.json`)

### 1D. Locations — Realms/Dimensions/Ruins (5 of 18 verified with biome+structure)

- L45 Sea of Devils ✓ (`sea_of_devils.json` biome)
- L46 Jue Ming Valley ✓ (`jue_ming_valley.json` biome)
- L47 Foreign Battleground ✓ (`foreign_battlefield_plains.json` + `foreign_soul_crystal_flats.json` + `foreign_void_rift_wastes.json` biomes; `foreign_war_monument`, `foreign_soul_crystal_deposit`, `foreign_void_rift` structures)
- L48 Land of the Ancient God ✓ (`ancient_god_level1_hurricane.json`, `ancient_god_level2_restriction.json`, `ancient_god_level3_annihilation.json`, `ancient_god_bone_chamber.json` biomes; `ancient_god_cave.json` structure)
- L49 Restriction Mountain ✓ (`ancient_god_level2_restriction.json` biome — sub-area of L48)
- L52 Suzaku Tomb ✓ (`suzaku_tomb.json` biome)
- L76 Immortal Graveyard ✓ (`immortal_graveyard.json` biome)
- L04 Cave World ✓ (`cave_world_void.json` biome + `cave_world` dimension)

### 1E. Sects (9 of 26 verified with full structure sets)

Each verified sect has 20+ sub-structures (`_alchemy_courtyard`, `_ancestor_hall`, `_array_hall`, `_core_formation_hall`, `_disciple_dormitories`, `_hidden_treasury`, `_inner_sect`, `_library`, `_main_plaza`, `_mountain_cave`, `_outer_gate`, `_puppet_workshop`, `_secret_pavilion`, `_spirit_beast_pens`, `_spirit_herb_garden`, `_spirit_spring`, `_sword_peak`, `_sword_tomb`, `_trial_grounds`, `_underground_passage`) + a parent structure file:

- F01 Heng Yue Sect ✓ (`heng_yue_sect*.json`)
- F02 Cloud Sky Sect ✓ (`cloud_sky_sect*.json`)
- F03 Soul Refining Sect ✓ (`soul_refining_sect*.json` + `soul_refining_ancestral.json`)
- F04 Corpse Yin Sect ✓ (`corpse_yin_sect*.json`)
- F05 Heavenly Fate Sect ✓ (`heavenly_fate_sect*.json` + `heavenly_fate_main.json` + `heavenly_fate_outpost.json`)
- F06 Xuan Dao Sect ✓ (`xuan_dao_sect.json`)
- F07 Fighting Evil Sect ✓ (`fighting_evil_sect*.json`)
- F15 Luo He Sect ✓ (`luo_he_sect*.json`)
- F26 Soul Refining Tribe ✓ (covered by Soul Refining Sect structures)

### 1F. Artifacts (178 DB entries — all have provenance; 151 surfaced as "signature subset")

All 178 DB artifacts have provenance files in `data/ergenverse/provenance/` (179 files = 178 + `_index.json`). The `CanonicalInventory.java` registry surfaces 151 as the "signature subset the user named in the task brief" (Restriction Flag, Heaven-Defying Bead, Karma Whip, 18-Hell Celestial Sealing Stamp, Sword Sheaths ×5, Storage treasures, Ancient God Leather Armour, God-Slaying War Chariot, etc.).

**Zhao→Suzaku arc signature artifacts (verified present):**
- I01 Heaven-Defying Bead ✓
- I02 Realm-Defining Compass ✓
- I03 Copper Mirror (Time Domain) ✓
- I06 Wealth (First Flying Sword) ✓
- I07 Core-Treasure Sword ✓
- I08 Dark Green Flying Sword ✓
- I12 Karma Whip ✓
- I13 18-Hell Celestial Sealing Stamp ✓
- I14 Rain Celestial Sword ✓
- I18 Half-Moon Blade ✓
- I21 Seven-Colored God Void Nails ✓
- I24 Heaven-Splitting Axe ✓
- I30 Slaughter Sword ✓
- I34 Ancient God Leather Armour ✓
- I50 Soul Flag ✓
- I51 Billion Soul Flag ✓
- I53 Soul Lasher / Kunji Whip ✓
- I55 Heaven-Avoiding Coffin ✓
- I63 Restriction Flag (set of 3) ✓
- I64 Three Purple Flags ✓
- I65 3× Ink Stones ✓
- I66 Restriction Breaking Ancient Mirror ✓
- I67 Heart Compass ✓
- I69 Soul Devil Ship ✓
- I70 Ghostly Sail ✓
- I72 Unnamed Wheel Formation ✓
- I73 Nine Deaths Perish Formation ✓
- I90 God-Slaying War Chariot ✓
- I95 Celestial Emperor Crown ✓
- I98 Star Compass ✓
- I99 Silver Dragon Star Compass ✓
- I102 Cloak of Vermilion Bird Divine Emperor ✓
- I103 Vermilion Bird Holy Token ✓
- I109–I111 Celestial Guards (Du Jian, Thunder Daoist, Ta Shan) ✓ (in CanonicalCompanions)
- I116 Mosquito Beast, I118 Lei Ji, I119 Nether Beast, I120 Thunder Toad, I121 Thunder Celestial Beast, I122 Brilliant Void Sea Dragon ✓ (in CanonicalPets/CanonicalCompanions)

### 1G. Techniques (214 DB entries — distributed across 6 registries totaling ~262 technique-class entries)

- `CanonicalTechniques.java` (169 entries) — Combat arts, Cultivation arts, Divine Sense, Movement, Soul arts, Avatar/Clone arts, Dao Domains, Late-game abilities
- `CanonicalDao.java` (18 entries) — Original Spells (Sundered Night, Flowing Time, Dream Dao, Karma Print, Life-Death Seal, True-False Eternal Seal, etc.) and Dao Domain manifestations
- `CanonicalEssences.java` (19 entries) — All 14 Essences (Metal/Wood/Water/Fire/Earth/Thunder/Life-Death/Karma/Slaughter/Restriction/True-False/Dao/True-False Primordial-Silent Extinction/Cause-and-Effect/Reincarnation)
- `CanonicalRestrictions.java` (12 entries) — Ancient Restrictions, Restriction Flags Refining Method, Annihilation/Time/Life-Death/Ancient Soul/Destruction Restrictions, Restriction Essence, Unnamed Wheel Formation, Blood Lines Rules
- `CanonicalBodies.java` (9 entries) — Five Elements True Body, Slaughter/Restriction/Absolute Beginning/Absolute End/Slaughter/Fire/Thunder/Earth/Water/Wood/Metal Essence True Bodies
- `CanonicalAvatars.java` (23 entries) — Cultivator Clone, Ancient Demon Clone, Ancient Devil Clone, Otherworldly Void Avatar, Slaughter True Body / Lu Mo, Punishment Slaughter, Divine Path Clone, etc.

**Zhao→Suzaku arc signature techniques (verified present):**
- T01 Underworld Ascension Method ✓ (the Ji Realm cultivation method taught by Situ Nan — Ch. 86)
- T02 Vermilion Bird Burning Heaven Art ✓
- T03 Nine Cycle Celestial Refining Tactic ✓ (TAINTED — All-Seer's trap)
- T04 Ancient God Tactic ✓
- T07 Rapid Spell Art ✓
- T08 Dao Fusion ✓
- T09 Finger of Death, T10 Demonic Finger, T11 Underworld Finger ✓
- T12 Celestial Slaughter Art ✓ (TAINTED — All-Seer's trap)
- T15 Karma Whip (technique form) ✓
- T49 Body Fixation Art ✓
- T50 Slash Luo Art ✓
- T77 Ji Realm / Extreme Realm Divine Sense ✓
- T91 Ancient Restrictions, T92 Restriction Flags Refining Method ✓
- T132 Foundation Stealing Technique ✓ (Wang Lin's signature early-game technique)
- T152 Life and Death Domain, T153 Karma Domain, T154 True and False Domain ✓

### 1H. Arsenal Manifest (309 items — 100% bridged, 100% textured)

- 309 entries in `wanglin_arsenal_manifest.json`
- 303 unique IDs (6 duplicates — see §5)
- 303/303 unique IDs bridged to canonical registries via `WangLinItems.MANIFEST_TO_REGISTRY`
- 309/309 items have model JSONs and texture PNGs (per worklog: 311 PNGs total in the textures directory)

### 1I. Species (116 species + 6 BTT = 122 total)

**Signature Zhao→Suzaku arc beasts (verified present):**
- mosquito_beast ✓ (with 10 life-state variants)
- thunder_toad ✓
- thunder_celestial_beast ✓
- nether_beast ✓
- brilliant_void / brilliant_void_beast ✓
- golden_exalt_sea_dragon ✓
- lei_ji / lei_ji_thunder_beast ✓
- xiao_bai / xiao_bai_beast ✓
- vermilion_bird, azure_dragon, white_tiger, black_tortoise, qilin, ice_phoenix, nine_colored_peacock ✓ (Four Divine Sect beasts)
- wang_family_hunting_dog ✓
- zhao_spirit_horse ✓
- tian_shui_spirit_fox ✓
- chu_water_serpent ✓
- fire_burn_wild_boar ✓
- snow_domain_ice_wolf ✓
- pilu_poison_scorpion ✓
- teng_family_bodyguard_beast ✓
- ancient_demon_clone, ancient_dream_beast, blood_ancestor_beast, blood_beast, cold_dragon, earth_fire_dragon, flame_dragon, ghost_face_beast, giant_python, heaven_devouring_beast, ink_dragon, ji_qiong, nine_headed_snake, restriction_beast, sea_of_devils_soul_beast, seven_colored_deer, six_desire_devil, soul_beast, soul_lasher_beast, spirit_ape, spirit_bat, spirit_bear, spirit_crane, spirit_deer, spirit_eagle, spirit_fish, spirit_fox, spirit_horse, spirit_rabbit, spirit_rat, spirit_snake, spirit_toad, spirit_tortoise, spirit_wolf, suzaku_guardian_beast, white_tiger_general ✓

---

## Section 2 — Partially Implemented

### 2A. NPCs with `cultivation: "unknown"` (35 NPCs)

These NPCs have JSON files but their `cultivation` field is set to the placeholder string `"unknown"`. Per canon docs, every named NPC has at least an implied cultivation tier. These should be filled in:

| NPC file | Name | Canon ID | Canon cultivation | Source |
|---|---|---|---|---|
| `npc_adai.json` | Adai | N137 | Qi Condensation (Wu Yu's servant) | CIVILIZATION.md F04 |
| `npc_bai_wei.json` | Bai Wei | N67 | Core Formation (Trading Planet) | WORLD.md N67 |
| `npc_chi_hu.json` | Chi Hu | N33 | mid-tier (Giant Demon Clan) | WORLD.md N33 |
| `npc_hong_shan.json` | Master Hong Shan | N53 | Third Step+ | WORLD.md N53 |
| `npc_hu_juan.json` | Hu Juan | N42 | Third Step+ (Ally of Wang Wei) | WORLD.md N42 |
| `npc_hui_bing.json` | Hui Bing | N149 | unknown in canon (C3) | Acceptable as "unknown" |
| `npc_huo_pao.json` | Huo Pao | N134 | Body refining (Soul Refining Tribe) | WORLD.md N134 |
| `npc_ji_si.json` | Ji Si | N76 | mid-tier (Dao Devil Sect helper) | WORLD.md N76 |
| `npc_kang_ren.json` | Kang Ren | N124 | mid-tier (Canglong Sect) | WORLD.md N124 |
| `npc_leng_sheng.json` | Leng Sheng | N151 | unknown in canon (C3) | Acceptable as "unknown" |
| `npc_li_dannan.json` | Li Dannan | N66 | mid-tier (Trading Planet guide) | WORLD.md N66 |
| `npc_li_yuan.json` | Li Yuan | N38 | Third Step+ (Annihilation Restriction inheritance) | WORLD.md N38 |
| `npc_lin_yi.json` | Lin Yi | N89 | unknown (C3, Teng ally) | Acceptable as "unknown" |
| `npc_ling_dong.json` | Ling Dong | N48 | unknown (C3) | Acceptable as "unknown" |
| `npc_lu_fuzi.json` | Lu Fuzi | N108 | unknown (C3, literary rival) | Acceptable as "unknown" |
| `npc_lu_yanfei.json` | Lu Yanfei | N71 | Foundation/low-Core (Origin Sect disciple) | WORLD.md N71 |
| `npc_lu_yuncong.json` | Lu Yuncong | N73 | Foundation/low-Core (Origin Sect, revenge-seeker) | WORLD.md N73 |
| `npc_mo_ling.json` | Mo Ling | N35 | unknown (C3) | Acceptable as "unknown" |
| `npc_noble_money.json` | Noble Money | N106 | unknown (C3) | Acceptable as "unknown" |
| `npc_ouyang_hua.json` | Ouyang Hua | N65 | Foundation/Core (Mountain Valley Tribe chief) | WORLD.md N65 |
| `npc_qing_yi.json` | Qing Yi | N11 | mortal (Wang Ping's wife) | WORLD.md N11 |
| `npc_rudy.json` | Rudy | N75 | unknown (C3) | Acceptable as "unknown" |
| `npc_sima_rufeng.json` | Sima Rufeng | N141 | Foundation/Core (Heavenly Fate Sect disciple) | WORLD.md N141 |
| `npc_song_wude.json` | Song Wude | N74 | mid-tier (invader) | WORLD.md N74 |
| `npc_song_yu.json` | Song Yu | N12 | mortal (Wang Jiduo's wife) | WORLD.md N12 |
| `npc_south_cloud.json` | Master South Cloud | N54 | Third Step+ | WORLD.md N54 |
| `npc_su_dao.json` | Su Dao | N27 | unknown in canon (C3) | Acceptable as "unknown" |
| `npc_wang_qingyue.json` | Wang Qingyue | N94 | unknown (C3, Duanmu Ji's ally) | Acceptable as "unknown" |
| `npc_xu_yun.json` | Xu Yun | N72 | Foundation/low-Core (Origin Sect) | WORLD.md N72 |
| `npc_xu_yunshan.json` | Xu Yunshan | N64 | mid-tier (Earth Planet junior sect master) | WORLD.md N64 |
| `npc_yan_leizi.json` | Yan Leizi | N111 | unknown (C3) | Acceptable as "unknown" |
| `npc_ye_dao.json` | Ye Dao | N109 | unknown (C3) | Acceptable as "unknown" |
| `npc_zhao_xinming.json` | Zhao Xinming | N142 | Foundation/Core (Heavenly Fate 4th sister) | WORLD.md N142 |
| `npc_zhong_dahong.json` | Zhong Dahong | N50 | low-tier (Flash Thunder Clan) | WORLD.md N50 |
| `npc_zhou_jin.json` | Zhou Jin | N49 | unknown (C3) | Acceptable as "unknown" |

**Action:** Fill in cultivation for the ~20 NPCs whose canon tier is documented; leave ~15 as `"unknown"` (with a comment noting C3 canon silence) where canon truly doesn't specify.

### 2B. NPCs with `location: "unknown"` (16 NPCs)

These NPCs have JSON files but their `location` field is `"unknown"`. **Several of these are deceased antagonists whose status should be `"deceased"` or `"destroyed"` per the Edge-of-Canon framing**, not `"unknown"`:

| NPC file | Name | Canon ID | Canon status at edge of canon | Suggested fix |
|---|---|---|---|---|
| `npc_all_seer.json` | All-Seer | N21 | **DECEASED** (killed by Wang Lin, E22) | Change `location` → `"deceased"` |
| `npc_cang_songzi.json` | Cang Songzi | N107 | **DECEASED** (killed in Seven-Colored Realm) | Change → `"deceased"` |
| `npc_daoist_scattered_spirit.json` | Daoist Scattered Spirit | N51 | alive (Scatter Thunder Clan) | Set to `"scatter_thunder_clan"` |
| `npc_daoist_water.json` | Daoist Water | N102 | **DECEASED** (slain by Wang Lin, E25) | Change → `"deceased"` |
| `npc_du_tian.json` | Du Tian | N23 | **SELF-ERASED** (soul in Billion Soul Banner) | Change → `"soul_in_billion_soul_banner"` |
| `npc_hunchback_meng.json` | Hunchback Meng | N91 | **DECEASED** (Tuo Sen puppet, eventually killed) | Change → `"deceased"` |
| `npc_lin_yi.json` | Lin Yi | N89 | **DECEASED** (killed by Wang Lin) | Change → `"deceased"` |
| `npc_ling_tianhou.json` | Ling Tianhou | N39 | alive (Da Lou Sword Sect) | Set to `"da_lou_sword_sect"` |
| `npc_master_ashen_pine.json` | Master Ashen Pine | N104 | **DECEASED** (killed by Wang Lin, Seven-Colored Realm) | Change → `"deceased"` |
| `npc_master_cloud_soul.json` | Master Cloud Soul | N105 | status unknown (presumed dead) | Set to `"presumed_deceased"` |
| `npc_master_void.json` | Master Void | N97 | **DECEASED** (killed by Wang Lin via Sundered Night) | Change → `"deceased"` |
| `npc_old_man_miesheng.json` | Old Man Miesheng | N115 | alive (independent) | Set to `"independent_wandering"` |
| `npc_punnan_zi.json` | Punnan Zi / Lou Hou | N88 | **SEALED** (under Ancient Shi Branch Temple) | Change → `"sealed_ancient_shi_branch_temple"` |
| `npc_qing_shui.json` | Qing Shui | N30 | alive (reincarnated on Immortal Execution Star) | Set to `"immortal_execution_star"` |
| `npc_wu_qing.json` | Wu Qing | N103 | **DECEASED** (killed by Wang Lin, War Spirit Print) | Change → `"deceased"` |
| `npc_xuan_luo.json` | Xuan Luo | N28 | alive (IAC, Dao Gu lineage) | Set to `"immortal_astral_continent"` |

**Action:** Update these 16 NPCs to reflect their canon edge-of-canon status. This is a meaningful accuracy fix per the Edge-of-Canon framing.

### 2C. Species lacking life-state variants (24 of 116 species)

The standard pattern is 10 life-state variants per species (`juvenile`, `adolescent`, `adult`, `ancient`, `mutated`, `injured`, `starving`, `tribulation_scarred`, `corrupted`, `enlightened`). 92 of 116 species have full variant sets; these 24 do not:

```
ancient_demon_clone, brilliant_void, celestial_guardian_beast, chu_water_serpent,
devils_soul_beast, fire_burn_wild_boar, foreign_battlefield_slaughter_beast,
heavenly_fate_guardian_lion, lei_ji, mosquito_elite_gold_carapace,
mosquito_valley_ecosystem, pilu_poison_scorpion, qiu_serpent,
sea_of_devils_soul_beast, snow_domain_ice_wolf, spirit_horse, spirit_wolf,
suzaku_guardian_beast, teng_family_bodyguard_beast, tian_shui_spirit_fox,
wang_family_hunting_dog, white_tiger_general, xiao_bai, zhao_spirit_horse
```

**Note:** Some of these are intentional:
- `brilliant_void` is both a species and an NPC (NPC has its own file)
- `lei_ji` is Wang Lin's named mount (NPC JSON exists at `npc_lei_ji.json`)
- `white_tiger_general` is a named NPC (NPC JSON exists at `npc_white_tiger_general.json`)
- `xiao_bai` is Zhou Ru's named pet tiger (NPC JSON exists at `npc_xiao_bai.json`)
- `mosquito_valley_ecosystem` is an ecosystem definition, not a single species

**Action:** Add the standard 10 life-state variants for the ~18 genuinely species-only entries. The 6 named-mount/NPC overlaps can stay without variants.

### 2D. CanonicalInventory coverage (151 of 178 DB artifacts — 84.8%)

The `CanonicalInventory.java` registry surfaces 151 of 178 DB artifacts as the "signature subset." The remaining 27 are intentionally placed in sibling registries:
- Companions (I109–I115, I116–I123) → `CanonicalCompanions.java` (24 entries)
- Clones/Avatars (I124–I137) → `CanonicalAvatars.java` (23 entries)
- Dao Domains (I138–I143) → `CanonicalDao.java` (18 entries)
- Original Spells (I144–I151) → `CanonicalDao.java`
- Essences/True Bodies (I152–I160) → `CanonicalBodies.java` (9 entries) + `CanonicalEssences.java` (19 entries)
- Restrictions (I162) → `CanonicalRestrictions.java` (12 entries)
- Formations (I73, I96, I172) → `CanonicalFormations.java` (15 entries)

All 178 artifacts have provenance files. **No actual gap — distribution across registries is intentional.**

---

## Section 3 — Missing: High Priority

### 3A. Missing NPC JSON files (5 named characters from Zhao→Suzaku arc)

These 5 characters have DB entries and canon doc presence but NO `npc_*.json` file:

| Canon ID | Name | Role | Canon source | Why critical |
|---|---|---|---|---|
| **N14** | Li Qiqing (李齐青) | Li Muwan's brother; elite Luo He Sect disciple (Fire Burn Country) | WORLD.md N14; mentioned in `fire_burn_country.json` biome comment | Wang Lin's brother-in-law; raised Li Muwan after their parents died; Luo He Sect elite. Referenced in biome metadata but no NPC entity. |
| **N16** | Dao Master Blue Dreams (蓝梦道主) | Li Qianmei's father; Blue Silk Clan; taught Wang Lin Light Shadow Shield, Dao Art Fusion, Overturn Heaven Seal | WORLD.md N16/N29; mentioned in `provenance/light_and_shadow_shield.json` | Wang Lin's father-in-law and Dao-master. Taught 3 signature techniques. Referenced in provenance files but no NPC entity. |
| **N52** | Second Generation Vermilion Bird / Young Emperor of the Fallen Land | Helped Wang Lin through Young Emperor trial; taught Dao of Strength | WORLD.md N52; EDGE_OF_CANON.md IR-09 | Critical mentor for Wang Lin's Fallen Land trial. Gave dragon blood. Taught one of three supreme Vermilion Bird techniques. |
| **N161** | Nian Tian (念天) | Du Tian's senior brother (Soul Refining Sect, Pilu Kingdom) | DB entry N161; status="deceased (canon)" | Soul Refining Sect lineage figure. Should be referenced in `soul_refining_sect_ancestor_hall.json` structure. |
| **N162** | Ling'er (灵儿) | Master Yi Chen's granddaughter; given Three Bells Shield + Straw Hat by Wang Lin (Ch. 965) | DB entry N162; referenced in `provenance/three_bells_shield.json` and `provenance/straw_hat___li_ming_straw_hat.json` | Recipient of two of Wang Lin's gifted treasures. Referenced in 2 provenance files but no NPC entity. |

**Action:** Create `npc_li_qiqing.json`, `npc_dao_master_blue_dream.json`, `npc_2nd_gen_vermilion.json`, `npc_nian_tian.json`, `npc_ling_er.json` following the existing schema.

### 3B. Missing locations — biomes (3 critical Zhao→Suzaku arc locations)

| Canon ID | Name | Canon source | Why critical |
|---|---|---|---|
| **L57** | Demon Spirit Land / East Demon Spirit Sea (魔灵地 / 东海魔灵海) | WORLD.md L57; ECOLOGY.md REGION — not a separate region entry; TIMELINE.md E23 | Site of Wang Lin's adoption of Thirteen as 1st disciple; Yao Xixue's first appearance (Ch. 491); All-Seer/Ling Tianhou/Blood Ancestor convergence; Immortal Monarch's Cave Mansion entrance. **The Yao Xixue/Blood Ancestor/Feng Mo arc takes place here.** Currently NO biome exists for this region. |
| **L51** | Chaotic Broken Stars (混沌碎星) | WORLD.md L51; TIMELINE.md E17 (Tu Si legacy) | Site of Duanmu Ji chase arc, Hunchback Meng rivalry, Xu Liqing (Demon Lord of Six Desires) modifications to Restriction Mountain, Tu Si's body/Ancient God legacy. The `ancient_god_level1/2/3` biomes partially cover this, but the Chaotic Broken Stars is a distinct 3-level chaotic star cluster with its own identity. **Should have a distinct biome or be explicitly merged.** |
| **L50** | Bridge of No Return (不归桥) | WORLD.md L50 | A specific landmark inside L48 Land of the Ancient God Level 2 where Wang Lin resists the illusion of his parents' voices. Currently folded into `ancient_god_level2_restriction.json` but canonically a distinct trial site. |

### 3C. Missing locations — structures (2 critical Zhao→Suzaku arc endgame sites)

| Canon ID | Name | Canon source | Why critical |
|---|---|---|---|
| **L64** | Immortal Emperor's Cave Mansion / Immortal Monarch's Cave Mansion (仙帝洞府) | WORLD.md L64; TIMELINE.md E57 | Yao Xixue ambushes Wang Lin here → 100-year imprisonment; Wind Demon (Feng Mo) slain here by Wang Lin's God-Slaying Spear; Blood Ancestor seeks Wang Lin here. **Site of the Yao Xixue 100-year imprisonment arc.** No structure JSON exists. |
| **L52/L53** | Thunder Celestial Realm (雷仙界) + Thunder Celestial Temple (雷仙殿) | WORLD.md L53/L54; TIMELINE.md E24 | Wang Lin's Thunder Celestial Tournament victory; kills Russell; becomes Thunder Celestial of the Thunder Celestial Temple; Blood Ancestor killed here (E94); Wang Lin obtains God-Slaying War Chariot, Celestial Emperor Crown, Collection Pavilion. **The Thunder Celestial Tournament is the climactic end of the Suzaku arc before Wang Lin leaves for the Cloud Sea.** No biome or structure exists. (Post-ch.901 but referenced by ch.901 karmic completion.) |

### 3D. Missing locations — biomes (Edge-of-Canon inside-artifact realms)

| Canon ID | Name | Canon source | Why critical |
|---|---|---|---|
| **L80** | Yellow Spring Secret Realm (黄泉秘境) | WORLD.md L80; TIMELINE.md E12 | Sub-realm inside the Heaven-Defying Bead, opened by Situ Nan for Wang Lin's Nascent Soul breakthrough training. **May be intentionally omitted** (since it's inside an artifact the player cannot obtain per IR-01), but if the player forges their own "defying heaven" artifact per the IR-01 protagonist-access-path, a parallel Yellow Spring chamber could be relevant. |

### 3E. Missing species — Fire Beast (Fire Burn Country)

The Fire Beast (and King of Fire Beasts) is a canon-attested species in Fire Burn Country where Wang Lin (in Ma Liang's body) meets Li Muwan while escaping one. The Heaven-Defying Bead eats the King of Fire Beasts here. **No `fire_beast.json` species file exists.**

The biome `fire_burn_country.json` comment explicitly mentions Fire Beasts roam this region, but no species entity backs it. Suggest adding `species/fire_beast.json` and `species/fire_beast_king.json` (with life-state variants).

### 3F. Missing techniques — verifications needed

Cross-referencing DB's 214 techniques against the 6 technique-class registries (CanonicalTechniques=169, CanonicalDao=18, CanonicalEssences=19, CanonicalRestrictions=12, CanonicalBodies=9, CanonicalAvatars=23, CanonicalSkills=12, CanonicalFormations=15, CanonicalKnowledge=26, CanonicalCompanions=24, CanonicalHistoricalEvents=14, CanonicalExperiences=12, CanonicalAllies=11, CanonicalEnemies=11, CanonicalPets=9, CanonicalRealms=11, CanonicalTitles=9) — total 555 entries.

**Coverage assessment:** Most DB techniques are surfaced across the multi-registry distribution. The "gap" of 214-169=45 techniques in CanonicalTechniques specifically is filled by:
- 14 Essences → CanonicalEssences
- 9 Bridges (B01-B09) → not in any registry (Heaven Trampling Bridges are cosmic-scale, may be intentional)
- 9 Accompanying Thunders (AT01-AT09) → not in any registry (these are passive cultivation milestones, not learnable techniques — acceptable)
- 4 Vermilion Bird Awakenings (VA01-VA04) → not in any registry (these are passive awakening stages — acceptable)
- 7 Original Spells (OS01-OS07) → CanonicalDao (18 entries, includes these)
- ~6 additional sub-techniques (T168-T170 Nine Mysterious Transformations lineage) → may need verification

**Action:** Verify that all 9 Heaven Trampling Bridges (B01-B09) and 9 Accompanying Thunders (AT01-AT09) are intentionally not in any canonical registry (since they are passive cosmic events/milestones, not learnable techniques). Document this as a deliberate design choice.

---

## Section 4 — Missing: Low Priority

### 4A. Tangential locations (C3 canon confidence — acceptable to defer)

- **L40 Shui/Dou City** (水城/斗城) — tangentially mentioned in Sea of Devils arc; C3 confidence
- **L41 Hou Fen** (侯分) — region listed in Fandom nav-bar; C3 confidence; sparse canon detail
- **L42 Blue Pine Peaks** (蓝松峰) — region listed in Fandom nav-bar; C3 confidence; sparse canon detail

**Action:** Optional. Could be added as minor sub-biomes within existing country biome sets if regional flavor is desired. Not blocking for the Zhao→Suzaku arc.

### 4B. Cross-novel / alias NPCs (intentionally absent)

- **N13 Wang Baole** — AWWP protagonist; not in the RI Zhao→Suzaku arc. Correctly absent.
- **N29 Dao Master Blue Dream (alias of N16)** — alias entry; same entity as N16. N16 is missing — see §3A.
- **N43 Qing Lin (alias of N26)** — alias entry; same entity as N26. Correctly implemented as `npc_qing_lin.json`.
- **N101 Yao Family** — faction, not individual. Already represented via `npc_yao_xixue.json` and `npc_blood_ancestor.json` (the family's named members). Could be added as a faction entity if needed, but the faction system likely handles this.

### 4C. Cross-novel locations (out of scope)

- L01-L13 (cosmological layers and other star systems) — meta-locations; not Zhao→Suzaku arc specific. Acceptable to defer.
- L59-L80 (post-Suzaku arc realms) — IAC continents, Dong Lin Pool, Ancient Tomb, Pill Sea, Five Flowers Eight Gates, Falling Land, Ancient Immortal Domain, Kunxu Realm, Tide Abyss — these are Cloud Sea/IAC arc locations, beyond the Zhao→Suzaku scope.

### 4D. Items not surfaced in CanonicalInventory (acceptable — covered by sibling registries)

The 27 DB artifacts not in CanonicalInventory are distributed across CanonicalCompanions, CanonicalAvatars, CanonicalDao, CanonicalBodies, CanonicalEssences, CanonicalRestrictions, CanonicalFormations. All have provenance files. **Not a true gap.**

---

## Section 5 — Accuracy Issues

### 5A. Deceased antagonists marked as `location: "unknown"` (see §2B for full table)

16 NPCs have `location: "unknown"` but per the Edge-of-Canon doc, 12 of them should be `deceased`/`sealed`/`self_erased` and 4 should have their canonical edge-of-canon location (IAC, Scatter Thunder Clan, etc.). This contradicts the mod's Edge-of-Canon framing.

**Affected:** All-Seer, Cang Songzi, Daoist Water, Du Tian, Hunchback Meng, Lin Yi, Master Ashen Pine, Master Cloud Soul, Master Void, Piao Nanzi, Wu Qing (deceased/sealed) + Daoist Scattered Spirit, Ling Tianhou, Old Man Miesheng, Qing Shui, Xuan Luo (alive with known location).

### 5B. Situ Nan's location is set to `vermilion_bird_divine_sect` (historical, not edge-of-canon)

`npc_situ_nan.json` line 9: `"location": "vermilion_bird_divine_sect"`. Per Edge-of-Canon doc Part 1.2, Situ Nan at edge of canon is:
- Reincarnated on the IAC as "Si Nan" (Grand Marshal of Wu Xuan Country), OR
- With Wang Lin's manifestation (his soul fragment is fused with the bead/Wang Lin)

The current value reflects his HISTORICAL faction, not his current location. Should be updated to reflect edge-of-canon state.

### 5C. Deceased antagonists' `faction` field

`npc_teng_huayuan.json` line 8: `"faction": "teng_family"`. Per Edge-of-Canon doc Part 6, the Teng Clan is **annihilated** (E14). The faction should be `"destroyed_teng_family"` or similar to reflect the post-annihilation state.

Similarly:
- All-Seer's faction: `heavenly_fate_sect` → should be `destroyed_heavenly_fate_sect` (sect diminished to ruin per Edge-of-Canon Part 6)
- Dao Devil Sect Master's faction: `dao_devil_sect` → should be `destroyed_dao_devil_sect` (annihilated by Wang Lin, E83)
- Daoist Water's faction: `rank_9_god_sect` → should be `masterless_rank_9_god_sect` (Daoist Water slain; sect declined)

### 5D. N112 Su Dao (antagonist) — alias confusion with N27 Su Dao (mentor)

The DB has two entries both named "Su Dao":
- N27 Su Dao (mentor) — `cultivation: unknown`, `status: unknown`, C3 confidence
- N112 Su Dao (antagonist) — `cultivation: unknown`, `status: unknown`, C3 confidence

Per WORLD.md reconciliation note B.4: *"The Fandom page lists 'Su Dao' in both mentors and antagonists — likely an inconsistency or two different Su Dao figures."*

The implementation has only `npc_su_dao.json` (mapped to canon_id N27). If these are two distinct entities, N112 needs a separate NPC JSON. If they're the same entity with a complex arc, the existing NPC should note both roles. **Recommend:** Document as a single NPC with a `roles: ["mentor", "antagonist"]` array, or add `npc_su_dao_antagonist.json` as a separate file with a `note` field explaining the ambiguity.

### 5E. Duplicate manifest entries (6 items)

The `wanglin_arsenal_manifest.json` has 309 entries but only 303 unique IDs. Six items appear twice:
- `heavenly_fate_finger` (×2)
- `extreme_water_dao` (×2)
- `extreme_metal_dao` (×2)
- `extreme_wood_dao` (×2)
- `extreme_earth_dao` (×2)
- `heavenly_bull_bead` (×2)

These should be deduplicated. The duplicates may correspond to the same technique being listed under different categories (e.g., one as a combat technique, one as an extreme dao). The bridge handles both via the same canonical registry ID, so the functional impact is minimal, but the manifest should be cleaned up.

### 5F. Wang Lin (N01) is only implemented as Manifestation

The DB has N01 as a full character entry, but the only NPC JSON is `npc_wang_lin_manifestation.json` (canon_id="MANIFEST"). This is **intentional and canon-correct** per Edge-of-Canon doc IR-14:
- Wang Lin is Transcendent (4th Step); he is no longer in the Cave World as a physical entity
- His "manifestation" is a Dao manifestation that travels with the player as a companion
- The manifestation is the player's gateway to Wang Lin's lineage, items (via ManifestationGiftSystem), and teaching

**Not an accuracy issue — document as intentional.** The `npc_wang_lin_manifestation.json` file correctly notes: *"is Wang Lin — a Dao manifestation, not a clone. Shares Wang Lin's personality and memories up to his current canon point."*

### 5G. N81 Third-Gen Vermilion Bird — `status: deceased` but canon says "betrayer" with ambiguous fate

`npc_3rd_gen_vermilion.json` has `status: deceased`. Per WORLD.md N81: *"Third Generation Vermilion Bird Master — betrayer"* (betrayed Situ Nan). The canon doesn't explicitly state death; the betrayer was defeated/punished but the specific fate is C4-ambiguous. The `deceased` status is a reasonable inference but should be marked as C3-C4.

### 5H. N82 14th-Gen Vermilion Bird — `status: alive` but canon describes as antagonist who severed Situ Nan's arm

`npc_14th_gen_vermilion.json` has `status: alive`. Per WORLD.md N82: *"Fourteenth Generation Vermilion Bird Master — antagonist (backstory)"* who severed Situ Nan's arm. The canon doesn't explicitly confirm death. `alive` is acceptable but should note "status at edge of canon: unknown — last mentioned as backstory figure."

---

## Section 6 — Recommended Next Steps (Prioritized)

### P0 — Critical (blocking for Zhao→Suzaku arc completeness)

1. **Create 5 missing NPC JSON files** (§3A):
   - `npc_li_qiqing.json` (N14 — Li Muwan's brother, Luo He Sect)
   - `npc_dao_master_blue_dream.json` (N16 — Li Qianmei's father, Blue Silk Clan, taught Wang Lin 3 techniques)
   - `npc_2nd_gen_vermilion.json` (N52 — Young Emperor mentor, Fallen Land trial guide)
   - `npc_nian_tian.json` (N161 — Du Tian's senior brother, Soul Refining Sect)
   - `npc_ling_er.json` (N162 — Master Yi Chen's granddaughter, recipient of Three Bells Shield + Straw Hat)

2. **Add Demon Spirit Land biome** (§3B — L57):
   - Create `worldgen/biome/demon_spirit_land.json` (or `east_demon_spirit_sea.json`)
   - Should reference the Ancient Demon City, Mountain Valley Tribe, Immortal Monarch's Cave Mansion
   - Critical for the Yao Xixue/Blood Ancestor/Feng Mo arc

3. **Add Immortal Emperor's Cave Mansion structure** (§3C — L64):
   - Create `worldgen/structure/immortal_emperor_cave_mansion.json` (+ sub-structure set if desired)
   - Should be wired into the Demon Spirit Land biome
   - Critical for the Yao Xixue 100-year imprisonment event

4. **Add Thunder Celestial Realm biome + Thunder Celestial Temple structure** (§3C — L53/L54):
   - Create `worldgen/biome/thunder_celestial_realm.json` and `worldgen/structure/thunder_celestial_temple.json`
   - Should reference the Thunder Lake trial, the Collection Pavilion, the Celestial Slaughter Art trap site
   - Critical for the Thunder Celestial Tournament (climactic end of Suzaku arc)

### P1 — High (accuracy vs Edge-of-Canon framing)

5. **Fix 16 NPCs with `location: "unknown"`** (§2B, §5A):
   - Update 12 deceased antagonists: `location` → `"deceased"` (or specific death location)
   - Update 4 alive NPCs with known edge-of-canon locations (Qing Shui → Immortal Execution Star, Xuan Luo → IAC, Ling Tianhou → Da Lou Sword Sect, Daoist Scattered Spirit → Scatter Thunder Clan, Old Man Miesheng → independent wandering)

6. **Fix Situ Nan's location** (§5B):
   - Change `location: "vermilion_bird_divine_sect"` → `"with_wang_lin_manifestation"` (or `"immortal_astral_continent_as_si_nan"`)

7. **Fix deceased antagonists' `faction` fields** (§5C):
   - Prefix annihilated factions with `destroyed_` (e.g., `destroyed_teng_family`, `destroyed_heavenly_fate_sect`, `destroyed_dao_devil_sect`, `masterless_rank_9_god_sect`)

8. **Resolve N112 Su Dao alias ambiguity** (§5D):
   - Either create `npc_su_dao_antagonist.json` as a separate NPC, OR
   - Update `npc_su_dao.json` to include `roles: ["mentor", "antagonist"]` and a `note` field explaining the canon ambiguity

### P2 — Medium (content density)

9. **Fill in `cultivation` for ~20 NPCs with documented canon tiers** (§2A):
   - Use the suggested values from the §2A table (e.g., Adai → Qi Condensation, Huo Pao → Body refining, Ouyang Hua → Foundation/Core, etc.)
   - Leave ~15 as `"unknown"` with a comment noting C3 canon silence

10. **Add life-state variants for ~18 species** (§2C):
    - Generate the standard 10 variants for: `ancient_demon_clone`, `celestial_guardian_beast`, `chu_water_serpent`, `devils_soul_beast`, `fire_burn_wild_boar`, `foreign_battlefield_slaughter_beast`, `heavenly_fate_guardian_lion`, `mosquito_elite_gold_carapace`, `pilu_poison_scorpion`, `qiu_serpent`, `sea_of_devils_soul_beast`, `snow_domain_ice_wolf`, `spirit_horse`, `spirit_wolf`, `suzaku_guardian_beast`, `teng_family_bodyguard_beast`, `tian_shui_spirit_fox`, `wang_family_hunting_dog`, `zhao_spirit_horse`
    - Skip the 6 named-mount/NPC overlaps (`brilliant_void`, `lei_ji`, `white_tiger_general`, `xiao_bai`, `mosquito_valley_ecosystem`)

11. **Add Fire Beast species** (§3E):
    - Create `species/fire_beast.json` and `species/fire_beast_king.json` (with 10 life-state variants each)
    - Wire into `fire_burn_country.json` biome spawners

12. **Add Chaotic Broken Stars as distinct biome or explicitly merge** (§3B — L51):
    - Either create `worldgen/biome/chaotic_broken_stars.json` (with the 3-level structure as sub-biomes), OR
    - Document in `ancient_god_level1/2/3.json` that they collectively represent both L48 Land of the Ancient God AND L51 Chaotic Broken Stars (canonically the same physical realm accessed via different routes)

13. **Add Bridge of No Return as landmark structure** (§3B — L50):
    - Create `worldgen/structure/bridge_of_no_return.json`
    - Wire into `ancient_god_level2_restriction.json` biome

### P3 — Low (polish)

14. **Deduplicate 6 manifest entries** (§5E):
    - Remove the duplicate `heavenly_fate_finger`, `extreme_water_dao`, `extreme_metal_dao`, `extreme_wood_dao`, `extreme_earth_dao`, `heavenly_bull_bead` from `wanglin_arsenal_manifest.json`

15. **Add minor location biomes** (§4A):
    - Optional: `shui_city.json`, `hou_fen.json`, `blue_pine_peaks.json` as minor sub-biomes

16. **Document the Wang Lin Manifestation design** (§5F):
    - Add a note in the audit/contributing docs explaining that N01 Wang Lin is intentionally implemented only as `npc_wang_lin_manifestation.json` per IR-14

17. **Verify Heaven Trampling Bridges (B01-B09) and Accompanying Thunders (AT01-AT09) intentional absence** (§3F):
    - These 18 DB entries have no canonical registry entry
    - Confirm this is intentional (they are passive cosmic milestones, not learnable techniques)
    - If intentional, document as a design decision

---

## Appendix A — Audit Methodology

1. Read `worklog.md` tail (200 lines) for project context — confirmed Phase 1-3 completion, all 10 priority tasks done, current build GREEN
2. Read all 7 canon research docs in `/home/z/my-project/forge-mod/`:
   - `CANON_RI_COMPLETE_WORLD.md` (3,035 lines, 188KB) — world structure, 80 locations, ~160 NPCs, 45 factions
   - `CANON_RI_TIMELINE.md` (1,124 lines, 100KB) — 108 events across 11 eras; Zhao→Suzaku arc = ERAs 1-5 (E01-E22, E40-E51, E97, E108)
   - `CANON_RI_COMPLETE_ITEMS.md` (2,019 lines, 110KB) — 178 artifacts in 10 categories
   - `CANON_RI_COMPLETE_TECHNIQUES.md` (1,793 lines, 135KB) — 214 techniques in 10 categories
   - `CANON_RI_ECOLOGY.md` (2,087 lines, 109KB) — 10 regions, 7 cross-region threads
   - `CANON_RI_CIVILIZATION.md` (1,361 lines, 79KB) — 18 detailed factions + 27 briefs
   - `CANON_RI_EDGE_OF_CANON.md` (1,207 lines, 95KB) — 14 inheritances, 6 decision horizons, faction status
3. Examined implemented data:
   - `ri_canon_database.json` (632 entries: 160 chars, 80 locs, 178 artifacts, 214 techniques)
   - `npcs/` directory (152 NPC JSON files + `_index.json`)
   - `species/` directory (116 species + 6 BTT + `_index.json`)
   - `worldgen/biome/` directory (330 biome JSONs)
   - `worldgen/structure/` directory (257 structure JSONs)
   - `species_variants/` directory (921 life-state variant JSONs)
   - `provenance/` directory (179 provenance JSONs for artifacts)
   - `WangLinItems.java` (303 bridge mappings to canonical registries)
   - `CanonicalInventory.java` + 17 sibling registries (555 total canonical entries)
   - `wanglin_arsenal_manifest.json` (309 entries, 303 unique IDs)
4. Cross-referenced via Python scripts:
   - DB characters ↔ NPC JSONs (by `canon_id` field)
   - DB locations ↔ biome/structure filenames (by name keywords)
   - DB artifacts ↔ CanonicalInventory entries (by name fuzzy match)
   - DB techniques ↔ CanonicalTechniques + sibling registries (by ID prefix)
   - Arsenal manifest ↔ bridge mappings ↔ canonical registries
   - NPC JSONs for missing/placeholder fields
   - Species ↔ life-state variants
5. Categorized findings into 6 sections per the task specification

## Appendix B — Coverage Statistics

| Category | DB entries | Implemented | Coverage |
|---|---|---|---|
| Characters | 161 | 152 NPCs (N01 intentional as Manifestation; N13 cross-novel; 5 missing) | 94.4% |
| Locations | 80 | 11/11 countries + 8/11 cities + 9/26 sects + 7/18 special realms with biome/structure | ~50% full structure coverage; ~85% with at least a biome |
| Artifacts | 178 | 178 provenance files + 151 in CanonicalInventory + 27 in sibling registries | 100% |
| Techniques | 214 | ~196 across 6 technique-class registries (Bridges/Thunders intentionally omitted) | ~92% |
| Arsenal items | 309 manifest / 303 unique | 303/303 bridged + 309/309 textured | 100% |
| Species | 116 + 6 BTT | 122 species files + 920 variants for 92 species | 100% species; 79% with variants |
| Biomes | — | 330 biome JSONs | — |
| Structures | — | 257 structure JSONs | — |

## Appendix C — Sources Verified

- Worklog tail (lines 10394-10594): confirmed Phase 1-3 completion, all 10 priority tasks done, build GREEN, 311 textures, 200+ Java files
- `CANON_RI_COMPLETE_WORLD.md` lines 257-556 (locations L23-L80), 791-1970 (NPCs N01-N132), 2234-2629 (factions F01-F26)
- `CANON_RI_TIMELINE.md` lines 950-1119 (summary table, bridging-policy classification, player encounter column)
- `CANON_RI_EDGE_OF_CANON.md` lines 695-998 (Inheritance Registry IR-01 through IR-14), 1048-1161 (faction status)
- `CANON_RI_EDGE_OF_CANON_STATE.md` lines 15-80 (cosmological changes, alive/dead NPCs, decision horizon)
- `ri_canon_database.json` (full read of 632 entries)
- All 152 NPC JSONs (schema validation + placeholder scan)
- All 116+6 species files (variant coverage check)
- All 330 biome files (location keyword search)
- All 257 structure files (template/non-template classification)
- All 921 species variant files (per-species variant count)
- All 179 provenance files (referenced in NPC/location search)
- `WangLinItems.java` (303 bridge mappings)
- 18 canonical registry Java files (555 total entries)

---

**End of audit.** This report documents the canon completeness of the Zhao→Suzaku arc as of 2026-07-12. The mod is ~93% canon-complete for this scope. The 5 missing NPCs and 3 missing critical locations are the highest-priority gaps. The 16 deceased-antagonist location fixes and 4 faction-field fixes are the highest-priority accuracy issues. All other gaps are medium-to-low priority polish work.
