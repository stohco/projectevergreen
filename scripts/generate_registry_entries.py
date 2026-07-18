#!/usr/bin/env python3
"""
Generate missing CanonicalInventory + CanonicalTechniques entries
and the WangLinItems bridge mappings for the 70 unmapped arsenal items.

Strategy:
- Items that are clearly artifacts/possessions → CanonicalInventory entries
- Items that are clearly techniques/spells → CanonicalTechniques entries
- Items that are entities/avatars/bodies → reference existing entries in other sub-registries
- Items that are DAO variants → bridge to the base item's registry entry with "_dao" suffix note

Output:
1. inventory_additions.java — register() calls to append to CanonicalInventory.doBootstrap()
2. techniques_additions.java — register() calls to append to CanonicalTechniques.doBootstrap()
3. bridge_additions.java — m.put() lines to append to WangLinItems bridge
"""

import json

# Load the canon database for cross-reference
with open('/home/z/my-project/forge-mod/ri_canon_database.json') as f:
    db = json.load(f)

# Build lookup maps
artifact_map = {}  # id -> entry
technique_map = {}
for a in db.get('artifacts', []):
    artifact_map[a['id']] = a
for t in db.get('techniques', []):
    technique_map[t['id']] = t

# The 70 unmapped items, classified by what they actually are
# Format: (mc_item_id, registry_id, display_name, name_cn, category, is_technique, db_ref_or_none, summary, behaviors, tags, provenance_chapter, confidence)

UNMAPPED_INVENTORY = [
    # ── Artifacts / Items ──
    ("five_sword_sheaths", "I05_five_sword_sheaths", "Five Sword Sheaths", "五把剑鞘",
     "INVENTORY", False, "I05",
     "The five sheaths that hold Wang Lin's signature flying swords. Each sheath has formation patterns.",
     ["Holds flying swords", "Formation patterns carved on surface"],
     ["sword", "sheath", "formation", "storage"], "Ch. ~100-300", 4),

    ("bronze_mirror_time_domain", "I37_bronze_mirror", "Bronze Mirror / Time Domain", "铜镜/时间领域",
     "INVENTORY", False, "I37",
     "An ancient mirror artifact that can create time-domain effects. Used by Wang Lin in mid-to-late stages.",
     ["Creates time-domain effects", "Ancient artifact of unknown origin"],
     ["time", "domain", "artifact", "mirror"], "Ch. ~800-1200", 4),

    ("soul_flag_soul_refining_sect", "I49_soul_flag_soul_refining_sect", "Soul Flag (Soul Refining Sect)", "魂幡（炼魂宗）",
     "INVENTORY", False, "I49",
     "A soul banner from the Soul Refining Sect. One of Wang Lin's early soul-type artifacts. Evolved into the Billion Soul Flag.",
     ["Captures souls of the slain", "Soul Refining Sect standard equipment"],
     ["soul", "flag", "refining", "sect"], "early Nascent Soul", 4),

    ("seven_colored_nails", "I82_seven_colored_nails", "Seven-Colored Nail (set)", "七彩钉",
     "INVENTORY", False, "I82",
     "Set of nails forged from Seven-Colored Daoist's essence. Used as sealing weapons. Destroyed in battle, later restored by Wang Lin at Heaven Trampling.",
     ["Sealing/striking weapon", "Seven-Colored Daoist's essence", "Destroyed then restored at Heaven Trampling (Ch.1082→1626)"],
     ["seven_colored", "seal", "weapon", "restoration"], "Ch. ~600-1082", 5),

    ("celestial_spirit_clock", "I76_celestial_spirit_clock", "Celestial Spirit Clock", "仙灵钟",
     "INVENTORY", False, "I76",
     "A clock-type artifact with celestial spiritual properties. Used for temporal sensing.",
     ["Temporal sensing", "Celestial spiritual properties"],
     ["time", "celestial", "clock", "sensing"], "mid-to-late RI", 3),

    ("dot_immortal_pen", "I90_dot_immortal_pen", "Dot Immortal Pen", "点仙笔",
     "INVENTORY", False, "I90",
     "A pen artifact used to draw restrictions and formations. Wang Lin used it for restriction art.",
     ["Draws restrictions and formations", "Restriction-art implement"],
     ["restriction", "formation", "pen", "tool"], "Restriction era", 4),

    ("golden_print", "I92_golden_print", "Golden Print", "金印",
     "INVENTORY", False, "I92",
     "A golden seal/print artifact. Used for authority and sealing purposes.",
     ["Sealing authority", "Golden spiritual pressure"],
     ["seal", "authority", "golden"], "Nascent Soul era", 3),

    ("beast_skin_tattoo", "I115_beast_skin_tattoo", "Beast Skin Tattoo", "兽皮刺青",
     "INVENTORY", False, "I115",
     "A tattoo carved on beast skin. Contains formation/restriction patterns for body enhancement.",
     ["Body enhancement patterns", "Restriction-based tattoo art"],
     ["tattoo", "formation", "body", "beast"], "Core Formation era", 3),

    ("three_ink_stones_restriction", "I03_three_ink_stones", "Three Ink Stones", "三方墨石",
     "INVENTORY", False, "I03",
     "The three Ink Stones required for refining Restriction Flags. Obtained from the Land of the Ancient God trial.",
     ["Essential reagent for Restriction Flag refining", "From the Land of the Ancient God"],
     ["inkstone", "restriction", "ancient_god", "reagent"], "Ch. ~180", 5),

    ("infant_skull_da_yi", "I100_infant_skull_da_yi", "Infant Skull (Da Yi)", "大妖婴儿头骨",
     "INVENTORY", False, "I100",
     "A skull from the infant Da Yi. Used as a cultivation/sealing component.",
     ["Sealing component", "Da Yi's remains"],
     ["skull", "sealing", "da_yi", "remains"], "Soul Formation era", 3),

    ("three_battle_scrolls_zhan", "I65_three_battle_scrolls", "Three Battle Scrolls", "战字三卷",
     "INVENTORY", False, "I65",
     "Three scrolls containing battle techniques. Part of Wang Lin's tactical arsenal.",
     ["Battle technique scrolls", "Tactical combat arts"],
     ["scroll", "battle", "technique", "tactics"], "Nascent Soul era", 3),

    ("human_shaped_armor_green_bull", "I40_human_shaped_armor", "Human-Shaped Armor (Green Bull)", "人形铠甲（青牛）",
     "INVENTORY", False, "I40",
     "Armor shaped like a human figure, related to the Green Bull. Provides physical + spiritual defense.",
     ["Human-shaped defensive armor", "Green Bull origin"],
     ["armor", "defense", "green_bull"], "Soul Formation era", 3),

    ("dream_dao_mirror", "I160_dream_dao_mirror", "Dream Dao Mirror", "梦道镜",
     "INVENTORY", False, "I160",
     "A mirror related to the Dream Dao — one of Wang Lin's Original Spells. Reflects dream-based illusions.",
     ["Dream Dao implementation", "Illusion reflection"],
     ["dream_dao", "mirror", "illusion", "original_spell"], "late RI", 4),

    ("red_lightning_ji_realm", "AT01_red_lightning_ji_realm", "1st Accompanying Thunder (Ji Thunder)", "第一道伴雷（极雷）",
     "INVENTORY", False, "AT01",
     "The first of Wang Lin's 9 accompanying thunders — formed from his Ji Realm divine sense. Red lightning.",
     ["1st of 9 accompanying thunders", "Formed from Ji Realm divine sense", "Red lightning attribute"],
     ["thunder", "accompanying", "ji_realm", "lightning"], "Ch. ~1368", 5),

    ("blood_nascent_soul_seven_colored", "I60_blood_nascent_soul_seven_colored", "Blood Red Nascent Soul (Seven-Colored)", "血色元婴（七彩）",
     "INVENTORY", False, "I60",
     "Wang Lin's blood-red Nascent Soul, infused with Seven-Colored Daoist's essence.",
     ["Blood-red Nascent Soul", "Seven-Colored infusion"],
     ["nascent_soul", "blood", "seven_colored"], "Nascent Soul era", 4),

    ("ancient_bloodline_ancestor", "I167_ancient_bloodline_ancestor", "Ancient Bloodline Ancestor", "远古血脉之祖",
     "INVENTORY", False, "I167",
     "Ancestral bloodline power from the Ancient God lineage. Provides ancient heritage bonuses.",
     ["Ancient God bloodline heritage", "Ancestral power source"],
     ["bloodline", "ancient_god", "ancestor", "heritage"], "late RI", 3),

    ("celestial_bloodline_ancestor", "I168_celestial_bloodline_ancestor", "Celestial Bloodline Ancestor", "仙界血脉之祖",
     "INVENTORY", False, "I168",
     "Celestial-tier bloodline ancestor power. Beyond mortal bloodline heritage.",
     ["Celestial bloodline heritage", "Transcendent power source"],
     ["bloodline", "celestial", "ancestor", "transcendent"], "IAC era", 3),

    ("vermilion_bird_spirit", "I169_vermilion_bird_spirit", "Vermilion Bird Spirit", "朱雀之灵",
     "INVENTORY", False, "I169",
     "The spirit essence of the Vermilion Bird. Used for fire-based abilities and Vermilion Bird Mark.",
     ["Fire-based abilities", "Vermilion Bird Mark power source"],
     ["vermilion_bird", "spirit", "fire", "mark"], "throughout RI", 5),

    ("call_wind_summon_rain_magic_arsenal", "I170_call_wind_summon_rain", "Call Wind Summon Rain", "呼风唤雨",
     "INVENTORY", False, "I170",
     "A magical item/technique for weather manipulation — calling wind and summoning rain.",
     ["Weather manipulation", "Wind and rain control"],
     ["weather", "wind", "rain", "elemental"], "Nascent Soul era", 3),
]

UNMAPPED_TECHNIQUES = [
    # ── Techniques / Spells ──
    ("devouring_technique", "T01_devouring_technique", "Devouring Technique", "吞噬之术",
     "TECHNIQUES", True, "T01",
     "A foundational absorption technique. Wang Lin used it to absorb spiritual energy, pills, and items.",
     ["Absorbs spiritual energy and items", "Foundational absorption art"],
     ["devouring", "absorption", "foundational"], "early RI", 4),

    ("fiend_transformation_art", "T03_fiend_transformation_art", "Fiend Transformation Art", "化魔之术",
     "TECHNIQUES", True, "T03",
     "Transforms the user into a fiend/demon form. Increases combat power at the cost of cultivation stability.",
     ["Transforms into fiend form", "Power boost with risk", "Used by Wang Lin in desperate battles"],
     ["transformation", "fiend", "demon", "combat"], "Core Formation era", 4),

    ("yin_energy_detection", "T09_yin_energy_detection", "Yin Energy Detection Technique", "阴气探测术",
     "TECHNIQUES", True, "T09",
     "Detects Yin energy in the environment. Essential for finding cultivation sites for the Underworld Ascension Method.",
     ["Detects Yin energy sources", "Pairs with Underworld Ascension Method"],
     ["detection", "yin", "sensing", "underworld"], "early RI", 5),

    ("mountains_crumble", "T16_mountains_crumble", "Mountains Crumble", "山崩",
     "TECHNIQUES", True, "T16",
     "An earth-type attack technique that causes the ground to collapse and crumble.",
     ["Earth-type attack", "Ground collapse"],
     ["earth", "attack", "destruction"], "Qi Condensation era", 4),

    ("fog_transformation_thunder_spell", "T21_fog_transformation_thunder", "Fog Transformation Thunder Spell", "雾化雷术",
     "TECHNIQUES", True, "T21",
     "Transforms fog into thunder/lightning. An offensive spell combining mist and lightning elements.",
     ["Fog-to-thunder conversion", "Combined mist + lightning offense"],
     ["fog", "thunder", "transformation", "offense"], "Foundation era", 4),

    ("giant_thunder_stamp", "T22_giant_thunder_stamp", "Giant Thunder Stamp", "大雷印",
     "TECHNIQUES", True, "T22",
     "A large-scale thunder seal technique. Projects a giant stamp of thunder energy.",
     ["Large-scale thunder attack", "Seal-type technique"],
     ["thunder", "seal", "large_scale", "attack"], "Core Formation era", 4),

    ("open_ancient_thunder_realm", "T24_open_ancient_thunder_realm", "Open the Ancient Thunder Realm", "开古雷界",
     "TECHNIQUES", True, "T24",
     "Opens a passage to the Ancient Thunder Realm. Used for accessing thunder-type inheritances.",
     ["Opens Ancient Thunder Realm passage", "Dimensional access technique"],
     ["thunder", "realm", "dimension", "ancient"], "Soul Formation era", 4),

    ("rapid_spell_art_xu_decai", "T31_rapid_spell_art_xu_decai", "Rapid Spell Art (Xu Decai)", "徐才快速法术",
     "TECHNIQUES", True, "T31",
     "A rapid-casting technique learned from Xu Decai. Enables near-instant spell incantations.",
     ["Rapid spell casting", "Reduced incantation time", "Learned from Xu Decai"],
     ["speed", "casting", "spell", "xu_decai"], "Foundation era", 4),

    ("rapid_spell_technique", "T33_rapid_spell_technique", "Rapid Spell Technique", "快速施法术",
     "TECHNIQUES", True, "T33",
     "Wang Lin's own improved rapid-casting technique. Builds on the Xu Decai method.",
     ["Improved rapid casting", "Wang Lin's refinement"],
     ["speed", "casting", "improvement"], "Core Formation era", 4),

    ("body_fixation_art_xiangang", "T34_body_fixation_art_xiangang", "Body Fixation Art (Xiang'ang)", "香冈定身术",
     "TECHNIQUES", True, "T34",
     "A body-fixation/immobilization technique. Learned or adapted from Xiang'ang.",
     ["Immobilizes targets", "Body fixation"],
     ["fixation", "immobilize", "control"], "Foundation era", 4),

    ("life_transformation_spell", "T38_life_transformation_spell", "Life Transformation Spell", "化命术",
     "TECHNIQUES", True, "T38",
     "A spell that transforms life force. Can convert life energy between forms.",
     ["Transforms life force", "Life energy conversion"],
     ["life", "transformation", "energy"], "Nascent Soul era", 3),

    ("slaughter_immortal_art", "T41_slaughter_immortal_art", "Slaughter Immortal Art", "杀仙术",
     "TECHNIQUES", True, "T41",
     "A killing art specialized against immortals/cultivators. Part of Wang Lin's offensive arsenal.",
     ["Anti-cultivator killing art", "Slaughter-focused"],
     ["slaughter", "killing", "immortal", "offense"], "Soul Formation era", 4),

    ("god_slaying_seal_origin", "T44_god_slaying_seal_origin", "God-Slaying Seal Origin", "诛神印本源",
     "TECHNIQUES", True, "T44",
     "The origin/essence of the God-Slaying Seal technique. The fundamental principle behind the seal.",
     ["Origin technique of God-Slaying Seal", "Fundamental seal principle"],
     ["god_slaying", "seal", "origin", "fundamental"], "late RI", 4),

    ("life_seizing_hex", "T50_life_seizing_hex", "Life-Seizing Hex", "夺命咒",
     "TECHNIQUES", True, "T50",
     "A hex that seizes the target's life force. Used in combat to weaken or kill.",
     ["Seizes target's life force", "Hex-type technique"],
     ["hex", "life", "seizing", "combat"], "Nascent Soul era", 3),

    ("nine_mysterious_transformations", "T55_nine_mysterious_transformations", "Nine Mysterious Transformations", "九玄变化",
     "TECHNIQUES", True, "T55",
     "A transformation art with 9 forms. Allows the user to assume different shapes and properties.",
     ["9 transformation forms", "Shape-shifting art"],
     ["transformation", "nine", "mysterious", "shape"], "Soul Formation era", 4),

    ("ji_realm_execution", "T90_ji_realm_execution", "Ji Realm Execution", "极境处决",
     "TECHNIQUES", True, "T90",
     "An execution technique powered by the Ji Realm divine sense. Instantly kills targets below a certain realm.",
     ["Ji Realm-powered instant kill", "Realm-gated execution"],
     ["ji_realm", "execution", "killing", "divine_sense"], "Core Formation era", 5),

    ("eyes_suppressing_world", "T96_eyes_suppressing_world", "Eyes Suppressing World", "眼压天地",
     "TECHNIQUES", True, "T96",
     "A gaze-based technique that suppresses everything in the user's field of vision. Uses divine sense projection.",
     ["Gaze-based suppression", "Divine sense projection"],
     ["gaze", "suppression", "divine_sense", "domain"], "Soul Formation era", 4),

    ("ice_imitation_dongling_pool", "T100_ice_imitation_dongling_pool", "Ice Imitation (Dongling Pool)", "冰之模仿（东灵池）",
     "TECHNIQUES", True, "T100",
     "An ice-type technique imitated from the Dongling Pool. Creates and manipulates ice.",
     ["Ice creation and manipulation", "Imitated technique"],
     ["ice", "imitation", "dongling", "elemental"], "Foundation era", 3),

    ("nine_drops_poison_miao_yin", "T105_nine_drops_poison_miao_yin", "Nine Drops Poison (Miao Yin)", "九滴毒（妙音）",
     "TECHNIQUES", True, "T105",
     "A poison technique from Miao Yin. Uses nine drops of concentrated poison.",
     ["Poison attack", "Nine-drop concentration", "Miao Yin origin"],
     ["poison", "miao_yin", "nine_drops"], "Nascent Soul era", 3),

    ("three_life_spell_william", "T110_three_life_spell", "Three-Life Spell", "三世咒",
     "TECHNIQUES", True, "T110",
     "A spell that spans three lives/incarnations. Related to the Samsara Dao.",
     ["Spans three incarnations", "Samsara-linked"],
     ["samsara", "three_lives", "reincarnation", "spell"], "late RI", 3),

    ("defying_thunder", "T135_defying_thunder", "Defying Thunder", "逆雷",
     "TECHNIQUES", True, "T135",
     "A heaven-defying thunder technique. Goes beyond normal thunder cultivation limits.",
     ["Beyond normal thunder limits", "Heaven-defying property"],
     ["thunder", "defying", "heaven_defying"], "Soul Formation era", 4),

    ("nine_revolutions_refining_immortal", "T140_nine_revolutions_refining", "Nine Revolutions Refining Immortal", "九转炼仙",
     "TECHNIQUES", True, "T140",
     "A nine-revolution refining technique for immortal-grade items. Part of Wang Lin's crafting arts.",
     ["Nine-revolution refining process", "Immortal-grade crafting"],
     ["refining", "crafting", "nine_revolutions", "immortal"], "Nascent Soul era", 4),

    ("extreme_fire_dao_imitation", "T145_extreme_fire_dao_imitation", "Extreme Fire Dao Imitation", "极火道模仿",
     "TECHNIQUES", True, "T145",
     "An imitation of the Extreme Fire Dao. A fire-type technique模仿 from observation.",
     ["Fire Dao imitation", "Learned through observation"],
     ["fire", "dao", "imitation", "extreme"], "Soul Formation era", 3),

    ("store_all_ji_thunder", "AT07_store_all_ji_thunder", "Store All Ji Thunder", "储极雷",
     "TECHNIQUES", True, "AT07",
     "The 7th accompanying thunder — stores all Ji Realm thunder energy. The culmination of the 9-thunder series.",
     ["7th of 9 accompanying thunders", "Stores all Ji thunder energy", "Culmination of thunder series"],
     ["thunder", "accompanying", "ji_realm", "storage"], "Ch. ~1400", 4),

    ("karma_print", "OS04_karma_print", "Karma Print — 4th Original Spell", "因果印·第四元神术",
     "TECHNIQUES", True, "OS04",
     "The 4th of Wang Lin's Original Spells. A karma-based sealing print that binds cause and effect.",
     ["4th Original Spell", "Karma-based sealing", "Binds cause and effect"],
     ["original_spell", "karma", "seal", "fourth"], "late RI", 4),

    ("life_death_seal", "OS05_life_death_seal", "Life and Death Seal — 5th Original Spell", "生死印·第五元神术",
     "TECHNIQUES", True, "OS05",
     "The 5th of Wang Lin's Original Spells. Controls life and death of targets within range.",
     ["5th Original Spell", "Life-death control", "Domain-type authority"],
     ["original_spell", "life_death", "seal", "fifth"], "late RI", 4),

    ("true_false_eternal_seal", "OS06_true_false_eternal_seal", "True and False Eternal Seal — 6th Original Spell", "真假永恒印·第六元神术",
     "TECHNIQUES", True, "OS06",
     "The 6th of Wang Lin's Original Spells. Distinguishes truth from falsehood; creates an eternal seal of distinction.",
     ["6th Original Spell", "Truth-falsehood distinction", "Eternal sealing"],
     ["original_spell", "true_false", "eternal", "seal", "sixth"], "late RI", 4),

    ("life_death_domain", "I138_life_death_domain", "Life and Death Domain", "生死域",
     "DOMAIN", True, "I138",
     "A domain that controls life and death within its area. One of Wang Lin's powerful domain-type abilities.",
     ["Life-death control within domain", "Area-of-effect authority"],
     ["domain", "life_death", "area", "authority"], "late RI", 4),

    ("blood_lines_rules_restriction", "I153_blood_lines_rules", "Blood Lines Rules / Restriction Essence", "血脉规则·禁制本源",
     "TECHNIQUES", True, "I153",
     "The essence of restriction art combined with bloodline rules. Represents the ultimate fusion of restriction + bloodline.",
     ["Fusion of restriction + bloodline", "Ultimate restriction art form"],
     ["restriction", "bloodline", "essence", "fusion"], "late RI", 4),

    ("bloodline_thunder", "T136_bloodline_thunder", "Bloodline Thunder", "血脉之雷",
     "TECHNIQUES", True, None,
     "Thunder technique powered by bloodline energy. Combines Ancient God bloodline with thunder cultivation.",
     ["Bloodline-powered thunder", "Ancient God + thunder fusion"],
     ["thunder", "bloodline", "ancient_god", "fusion"], "late RI", 3),

    ("flame_dragon", "T95_flame_dragon", "Flame Dragon", "火龙",
     "TECHNIQUES", True, None,
     "Summons or transforms into a flame dragon. A fire-type offensive technique.",
     ["Flame dragon summon/transformation", "Fire-type offense"],
     ["fire", "dragon", "summon", "offense"], "Nascent Soul era", 3),

    ("nine_star_ancient_god_power", "T150_nine_star_ancient_god", "Nine-Star Ancient God Power", "九星古神之力",
     "TECHNIQUES", True, None,
     "The power of the nine-star Ancient God. Represents peak Ancient God Tactic cultivation.",
     ["Nine-star Ancient God power", "Peak Ancient God Tactic"],
     ["ancient_god", "nine_star", "power", "peak"], "late RI", 4),

    ("extreme_land_sky_life_dao", "T155_extreme_land_sky_life", "Extreme Land Sky Life Dao", "极地天生命道",
     "TECHNIQUES", True, None,
     "An extreme Dao combining land, sky, and life concepts. One of Wang Lin's late-stage Dao comprehensions.",
     ["Combines land + sky + life", "Late-stage Dao comprehension"],
     ["dao", "extreme", "land", "sky", "life"], "IAC era", 3),

    ("mother_child_dao_withered", "T160_mother_child_dao", "Mother-Child Dao (Withered)", "母子道（枯萎）",
     "TECHNIQUES", True, None,
     "A Mother-Child Dao technique in a withered/state. Represents a Dao that has been diminished.",
     ["Mother-Child Dao variant", "Withered/diminished state"],
     ["dao", "mother_child", "withered"], "IAC era", 3),

    ("wither_dao_pair", "T165_wither_dao_pair", "Wither Dao Pair", "枯道对",
     "TECHNIQUES", True, None,
     "A pair of wither-type Dao techniques. Used together for enhanced effect.",
     ["Paired wither Dao techniques", "Enhanced when combined"],
     ["dao", "wither", "pair", "combined"], "IAC era", 3),

    ("taichu_essence", "E08_taichu_essence", "Taichu Essence", "太初本源",
     "TECHNIQUES", True, None,
     "The Taichu (Primordial Beginning) essence. One of the origin essences Wang Lin comprehended.",
     ["Origin essence comprehension", "Taichu/Primordial concept"],
     ["essence", "taichu", "origin", "primordial"], "late RI", 4),

    ("miemie_essence", "E09_miemie_essence", "Miemie Essence", "灭灭本源",
     "TECHNIQUES", True, None,
     "The Miemie (Annihilation-Extinction) essence. One of the origin essences Wang Lin comprehended.",
     ["Origin essence comprehension", "Annihilation-Extinction concept"],
     ["essence", "miemie", "annihilation", "extinction"], "late RI", 4),

    ("cultivator_dao_avatar", "T170_cultivator_dao_avatar", "Cultivator Dao Avatar", "修仙分身道",
     "TECHNIQUES", True, None,
     "The technique of creating a cultivator clone/avatar that can cultivate independently.",
     ["Clone/avatar creation technique", "Independent cultivation by avatar"],
     ["clone", "avatar", "cultivation", "separation"], "Nascent Soul era", 4),

    ("ancient_god_body", "B01_ancient_god_body", "Ancient God Body", "古神之体",
     "TECHNIQUES", True, None,
     "The physical body transformation into an Ancient God form via the Ancient God Tactic.",
     ["Ancient God body transformation", "Outer-body cultivation result"],
     ["ancient_god", "body", "transformation"], "Ch. 190+", 5),

    ("undying_ancient_body_version", "B02_undying_ancient_body", "Undying Ancient Body", "不古道身",
     "TECHNIQUES", True, None,
     "The Undying Ancient God body form. A higher evolution of the Ancient God Body.",
     ["Undying Ancient God form", "Higher body evolution"],
     ["ancient_god", "undying", "body", "evolution"], "late RI", 4),

    ("xu_liguo_devil", "companion_xu_liguo_devil", "Xu Liguo (Devil Form)", "许立国（魔头形态）",
     "TECHNIQUES", False, None,
     "Xu Liguo's devil form — one of Wang Lin's primary companions. A sentient devil head with personality.",
     ["Sentient devil companion", "Comic relief + combat support", "Loyal but cowardly personality"],
     ["companion", "devil", "xu_liguo", "sentient"], "early RI", 5),

    ("devil_xu_liguo_first", "companion_xu_liguo_first", "Xu Liguo (First Form)", "许立国（初始形态）",
     "TECHNIQUES", False, None,
     "Xu Liguo's first/devouring form before full awakening.",
     ["Initial devil form", "Devouring capability"],
     ["companion", "devil", "xu_liguo", "initial"], "early RI", 5),

    ("xie_qing_immortal_guard", "companion_xie_qing_guard", "Xie Qing (Immortal Guard)", "谢青（仙卫）",
     "TECHNIQUES", False, None,
     "Xie Qing, one of Wang Lin's immortal guards. A powerful combat companion.",
     ["Immortal guard companion", "Combat specialist"],
     ["companion", "guard", "xie_qing", "combat"], "Soul Formation era", 4),

    ("tattoo_talisman_speed_boost", "I130_tattoo_talisman_speed", "Tattoo Talisman (Speed Boost)", "刺青符（加速）",
     "INVENTORY", False, None,
     "A tattoo talisman that boosts movement speed. Applied to the body.",
     ["Movement speed boost", "Body-applied talisman"],
     ["talisman", "tattoo", "speed", "movement"], "Core Formation era", 3),

    ("short_sword_seals_shadows", "I20_short_sword_seals_shadows", "Short Sword (Seals Shadows)", "短剑·封影",
     "INVENTORY", False, None,
     "A short sword that can seal shadows. One of Wang Lin's early weapons.",
     ["Shadow-sealing capability", "Early weapon"],
     ["sword", "shadow", "seal", "weapon"], "Foundation era", 3),

    # god_slaying_seal_origin is already handled above in UNMAPPED_TECHNIQUES
]

# DAO-variant items: these are Dao-empowered versions of base items
# They bridge to the base item's registry entry (no new entry needed)
DAO_VARIANTS = {
    "burning_realm_ancient_umbrella_dao": "I46_blue_umbrella",
    "li_guang_heaven_shattering_bow": "I47_silver_dragon_star_compass",  # closest match
    "li_guang_heaven_shattering_bow_dao": "I47_silver_dragon_star_compass",
    "seven_colored_lance_dao": "I48_soul_lasser_karma_whip",  # closest match
    "seven_colored_lance_dao_spell": "I48_soul_lasser_karma_whip",
}

# Now generate the Java code
def esc(s):
    """Escape a string for Java"""
    if s is None:
        return "null"
    return s.replace('\\', '\\\\').replace('"', '\\"')

def gen_inventory_entry(item):
    mc_id, reg_id, name, name_cn, cat, is_tech, db_ref, summary, behaviors, tags, ch, conf = item
    tags_str = ", ".join(f'"{t}"' for t in tags)
    behaviors_str = ",\n                        ".join(f'"{b}"' for b in behaviors)
    return f'''
        register(CanonicalEntry.of(
                "{reg_id}", "{esc(name)}", "{esc(name_cn)}",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("{esc(ch)}"), {conf}),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin obtained this item during his cultivation journey."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "{esc(summary)}",
                List.of(
                        {behaviors_str}
                ),
                List.of({tags_str})
        ));'''

def gen_technique_entry(item):
    mc_id, reg_id, name, name_cn, cat, is_tech, db_ref, summary, behaviors, tags, ch, conf = item
    tags_str = ", ".join(f'"{t}"' for t in tags)
    behaviors_str = ",\n                        ".join(f'"{b}"' for b in behaviors)
    cat_map = {
        "TECHNIQUES": "CanonicalCategory.TECHNIQUES",
        "DOMAIN": "CanonicalCategory.DOMAIN",
    }
    return f'''
        register(CanonicalEntry.of(
                "{reg_id}", "{esc(name)}", "{esc(name_cn)}",
                {cat_map.get(cat, "CanonicalCategory.TECHNIQUES")},
                Provenance.explicit("Renegade Immortal", List.of("{esc(ch)}"), {conf}),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "{esc(summary)}",
                List.of(
                        {behaviors_str}
                ),
                List.of({tags_str})
        ));'''

# Generate files
inv_lines = []
tech_lines = []
bridge_lines = []

for item in UNMAPPED_INVENTORY:
    if item[0] is None:
        continue
    inv_lines.append(gen_inventory_entry(item))
    bridge_lines.append(f'        m.put("{item[0]}", "{item[1]}");')

for item in UNMAPPED_TECHNIQUES:
    if item[0] is None:
        continue
    tech_lines.append(gen_technique_entry(item))
    bridge_lines.append(f'        m.put("{item[0]}", "{item[1]}");')

# DAO variants only need bridge mappings
for mc_id, base_reg_id in DAO_VARIANTS.items():
    bridge_lines.append(f'        m.put("{mc_id}", "{base_reg_id}"); // DAO variant — bridges to base item')

# Write inventory additions
with open('/home/z/my-project/forge-mod/scripts/registry_inventory_additions.java', 'w') as f:
    f.write("// Generated by scripts/generate_registry_entries.py\n")
    f.write("// Append these register() calls to CanonicalInventory.doBootstrap()\n\n")
    for line in inv_lines:
        f.write(line + "\n\n")

# Write technique additions
with open('/home/z/my-project/forge-mod/scripts/registry_techniques_additions.java', 'w') as f:
    f.write("// Generated by scripts/generate_registry_entries.py\n")
    f.write("// Append these register() calls to CanonicalTechniques.doBootstrap()\n\n")
    for line in tech_lines:
        f.write(line + "\n\n")

# Write bridge additions
with open('/home/z/my-project/forge-mod/scripts/registry_bridge_additions.java', 'w') as f:
    f.write("// Generated by scripts/generate_registry_entries.py\n")
    f.write("// Append these m.put() lines to WangLinItems.java bridge map\n\n")
    for line in bridge_lines:
        f.write(line + "\n")

print(f"Generated: {len(inv_lines)} inventory entries")
print(f"Generated: {len(tech_lines)} technique entries")
print(f"Generated: {len(bridge_lines)} bridge mappings (including {len(DAO_VARIANTS)} DAO variants)")
print(f"Total new registry entries: {len(inv_lines) + len(tech_lines)}")