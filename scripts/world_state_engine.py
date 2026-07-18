#!/usr/bin/env python3
"""
World State Engine - Layer 3 Canon-to-Simulation Generator
============================================================
Implements the 10 architectural gaps identified in the Er Gen simulation review:

  Pass 11: Comprehensive Species Database (every named beast across 6 Er Gen novels)
  Pass 12: Ecosystem Food Webs with Seasonality
  Pass 13: Migration Routes (cloud whales, spirit cranes, and follower chains)
  Pass 14: Macro-Scale Terrain (Ancient God as actual terrain)
  Pass 15: Expanded Provenance Schema (full lifecycle tracking)
  Pass 16: Civilization Simulation Definitions (sects as living organisms)
  Pass 17: Opportunity Generation Tables (continuous opportunity inference)
  Pass 18: Time-System Event Definitions (the world is not static)
  Pass 19: Item Physical Properties (lore-accurate size/weight/realm)
  Pass 20: World State Engine Master Schema (the unifying document)

Prime Directive: Reality is objective; cultivation changes understanding, not existence.
No-Locked-Upgrades Directive: every canonical state must be obtainable.

This script is IDEMPOTENT and re-runnable. It reads ri_canon_database.json and
emits JSON data files under src/main/resources/data/ergenverse/.
"""

import json
import os
import hashlib
from pathlib import Path

# ------------------------------------------------------------------
# Paths
# ------------------------------------------------------------------
ROOT = Path(__file__).resolve().parent.parent
DATA = ROOT / "src" / "main" / "resources" / "data" / "ergenverse"
CANON_DB = ROOT / "ri_canon_database.json"

def out(*parts):
    p = DATA.joinpath(*parts)
    p.mkdir(parents=True, exist_ok=True)
    return p

def load_canon():
    with open(CANON_DB, encoding="utf-8") as f:
        return json.load(f)

def slug(s):
    return "".join(c.lower() if c.isalnum() else "_" for c in s).strip("_")

def salt_for(name):
    h = hashlib.md5(name.encode("utf-8")).hexdigest()
    base = int(h[:8], 16)
    # positive 31-bit salt, deterministic per name
    return (base % 2000000000) + 1

def write_json(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(obj, f, ensure_ascii=False, indent=2)
        f.write("\n")

CANON_PRIME = ("Reality is objective; cultivation changes understanding, not existence. "
               "Every state described here exists independently of the player's perception.")

# ==================================================================
# PASS 11 - COMPREHENSIVE SPECIES DATABASE
# ==================================================================
# Every named beast across the six Er Gen novels. Each carries 13 canonical
# fields. After the canonical list is complete, life-state variants are
# generated (juvenile/adolescent/adult/ancient/mutated/injured/starving/
# tribulation_scarred/corrupted/enlightened) -- 10 variants per species.

SPECIES_CANON = [
    # ------------------ Renegade Immortal (仙逆) ------------------
    {"id":"mosquito_beast","name":"Mosquito Beast","nameCn":"蚊兽","novel":"Renegade Immortal",
     "appearance":"Insectoid, four translucent wings, needle-like proboscis the length of a forearm, carapace dark crimson after first blood-feeding; wild type is translucent white",
     "size":"wingspan 0.3 m (juvenile) to 3 m (adult ancient); body mass 4-400 jin",
     "behavior":"Swarm ambush predators; follow blood scent for li; queens cultivate in stillness for centuries; mutate toward gold carapace at Ancient realm",
     "habitat":"Mosquito Valley (Country of Zhao), blood-pool wetlands, corpse-laden battlefields, fog marshes",
     "cultivation":"Foundation Establishment (worker) -> Core Formation (soldier) -> Nascent Soul (elite) -> Spirit Severing (queen) -> Ascendant (ancient queen)",
     "intelligence":4,"diet":"blood and spirit-marrow; queens consume entire spirit veins",
     "relationships":"Symbiosis with blood-pool flora; preyed upon by thunder beasts and nether beasts; mutated gold variants feared by Core Formation cultivators",
     "techniques":["blood_sense","swarm_blood_drain","needle_sting","gold_carapace_defense"],
     "treasures":["mosquito_blood_essence","queen_carapace_fragment","gold_needle_core"],
     "bloodline":"Ancient Blood-Mosquito lineage; rare gold mutation traces to Blood Ancestor",
     "mutations":["gold_carapace","twin_queen","blood_spirit_body","ancient_bloodline_awakening"],
     "canon_confidence":5},

    {"id":"thunder_toad","name":"Thunder Toad","nameCn":"雷蟾","novel":"Renegade Immortal",
     "appearance":"Bloated toad 1-2 m long, skin crackling with blue-white thunder patterns, throat sac glows during storms",
     "size":"1.5 m adult, 80 jin; ancient specimens reach 5 m, 2000 jin",
     "behavior":"Ambush predator; buries in mud during dry season, emerges during thunderstorms to absorb lightning; territorial, croaks carry for li",
     "habitat":"Thunder-attribute wetlands, Ancient Thunder Realm, storm-lashed coasts of Fire Burn Country",
     "cultivation":"Core Formation -> Nascent Soul -> Spirit Severing (thunder attribute)",
     "intelligence":5,"diet":"lightning-attribute herbs, thunder-essence fish, juvenile thunder beasts",
     "relationships":"Rival of Lei Ji thunder beasts; preyed upon by Nascent Soul cultivators for thunder-essence core",
     "techniques":["thunder_croak","static_discharge","lightning_tongue","storm_call"],
     "treasures":["thunder_toad_core","storm_sac","thunder_pattern_skin"],
     "bloodline":"Ancient Thunder-Toad lineage; thunder-attribute bloodline",
     "mutations":["tribulation_marked","nine_sac","ancient_thunder_body"],
     "canon_confidence":5},

    {"id":"lei_ji_thunder_beast","name":"Lei Ji Thunder Beast","nameCn":"雷吉","novel":"Renegade Immortal",
     "appearance":"Quadrupedal beast resembling a wolf-lion hybrid, fur perpetually standing on end with static, eyes glow electric blue",
     "size":"2 m shoulder height adult, 600 jin; ancient 4 m, 8000 jin",
     "behavior":"Pack hunter during storms; solitary otherwise; cultivates by standing on high peaks during tribulations; imprints on cultivators who save them",
     "habitat":"Ancient Thunder Realm, thunder peaks of Suzaku mountains, high-altitude storm zones",
     "cultivation":"Nascent Soul -> Spirit Severing -> Ascendant (Wang Lin's Lei Ji reached Ascendant)",
     "intelligence":7,"diet":"thunder-essence herbs, thunder beast cores, lightning-attributed minerals",
     "relationships":"Bonded companion to Wang Lin; rival of thunder toads; preyed upon by thunder celestial beasts",
     "techniques":["thunder_step","lightning_claw","storm_howl","tribulation_absorb"],
     "treasures":["lei_ji_core","thunder_fur","lightning_eye"],
     "bloodline":"Ancient Thunder-Beast lineage; can absorb heavenly tribulation lightning",
     "mutations":["nine_tail","tribulation_survivor","ancient_thunder_emperor"],
     "canon_confidence":5},

    {"id":"nether_beast","name":"Nether Beast","nameCn":"冥兽","novel":"Renegade Immortal",
     "appearance":"Shadow-wreathed quadruped, body perpetually trailing black mist, eyes are pinpoints of cold green fire",
     "size":"3 m long adult, 1200 jin; ancient forms dissolve into pure shadow",
     "behavior":"Hunts from the nether plane, materializing only to strike; feeds on soul fragments; cannot tolerate sunlight",
     "habitat":"Sea of Devils, Soul Refining territory, nether-aspected caves, Yellow Spring Secret Realm",
     "cultivation":"Nascent Soul -> Spirit Severing -> Ascendant (nether attribute)",
     "intelligence":8,"diet":"soul fragments, nether-attribute herbs, corpse marrow",
     "relationships":"Servant-beasts of Soul Refining Sect; enemies of all light-attribute creatures",
     "techniques":["nether_step","soul_drain","shadow_rend","nether_breath"],
     "treasures":["nether_core","soul_fragment_cluster","shadow_pelt"],
     "bloodline":"Netherworld lineage; born from accumulated death qi",
     "mutations":["soul_body","twelve_eyes","pure_nether"],
     "canon_confidence":4},

    {"id":"flame_dragon","name":"Flame Dragon","nameCn":"火龙","novel":"Renegade Immortal",
     "appearance":"Serpentine dragon 30 m long (adult), scales the color of cooling magma, breath ignites the air",
     "size":"30 m adult, 50000 jin; ancient 200 m, millions of jin",
     "behavior":"Solitary apex predator; sleeps in magma for centuries; hoards fire-attribute treasures; attacks intruders with sun-temperature breath",
     "habitat":"Fire Burn Country volcanic regions, Pill Sea volcanic vents, fire-aspected spirit veins",
     "cultivation":"Spirit Severing -> Ascendant -> Immortal (fire attribute)",
     "intelligence":9,"diet":"fire-attribute herbs, magma minerals, fire-attribute spirit stones",
     "relationships":"Tribute-bond to Fire Burn Country imperial family; rival of ice phoenix",
     "techniques":["dragon_breath","magma_sleep","fire_scale_defense","dragon_coil"],
     "treasures":["flame_dragon_core","dragon_scale_armor","dragon_blood_essence"],
     "bloodline":"Ancient Dragon lineage; fire-attribute true dragon bloodline (diluted)",
     "mutations":["nine_claw","golden_scale","true_dragon_awakening"],
     "canon_confidence":4},

    {"id":"cloud_whale","name":"Cloud Whale","nameCn":"云鲸","novel":"Renegade Immortal",
     "appearance":"Leviathan whale 100 m long, body semi-translucent like condensed cloud, floats through sky by manipulating air-spirit density",
     "size":"100 m adult, 200000 jin; ancient 500 m, tens of millions of jin",
     "behavior":"Migrates in pods of 3-7 across sky realms; cultivates by absorbing cloud-essence; entire sky-civilizations form around their migration routes",
     "habitat":"Sky Realm, upper atmosphere of Planet Suzaku, cloud seas between continents",
     "cultivation":"Spirit Severing -> Ascendant -> Immortal (cloud/wind attribute)",
     "intelligence":10,"diet":"cloud-essence, sky-spirit herbs, aerial plankton of qi",
     "relationships":"Sky-civilization hosts follow migrations; predators: none save ancient thunder celestial beasts",
     "techniques":["cloud_breath","sky_dive","mist_shroud","whale_song"],
     "treasures":["cloud_whale_core","sky_pearl","mist_essence"],
     "bloodline":"Ancient Sky-Whale lineage; cloud-attribute true leviathan bloodline",
     "mutations":["nine_fin","ancient_sky_emperor","void_whale"],
     "canon_confidence":4},

    {"id":"thunder_celestial_beast","name":"Thunder Celestial Beast","nameCn":"雷仙兽","novel":"Renegade Immortal",
     "appearance":"Formless mass of living lightning vaguely shaped like a wolf, body crackles with nine colors of tribulation lightning",
     "size":"Variable; adult 5 m manifestation, true form is the storm itself",
     "behavior":"Born inside heavenly tribulations; hunts by calling down tribulation on prey; cannot exist long outside storms",
     "habitat":"Thunder Celestial Realm, inside active tribulations, Ancient Thunder Realm peaks",
     "cultivation":"Ascendant -> Immortal (tribulation attribute)",
     "intelligence":9,"diet":"tribulation lightning, thunder-attribute cores, lightning-struck herbs",
     "relationships":"Apex predator of thunder attribute; hunts cloud whales and lei ji",
     "techniques":["tribulation_call","lightning_rend","storm_body","nine_color_tribulation"],
     "treasures":["thunder_celestial_core","tribulation_fragment","nine_color_lightning_essence"],
     "bloodline":"Born of heavenly tribulation; closest bloodline to Heavenly Dao lightning",
     "mutations":["nine_color_body","true_tribulation_embodiment"],
     "canon_confidence":4},

    {"id":"giant_savage_ape","name":"Giant Savage Ape","nameCn":"巨猿","novel":"Renegade Immortal",
     "appearance":"Bipedal ape 10 m tall, fur iron-grey, fists the size of millstones, four arms on ancient specimens",
     "size":"10 m adult, 200000 jin; ancient 30 m, millions of jin",
     "behavior":"Territorial mountain predator; uses fists to shatter formations; hoards spirit-fruit in caves",
     "habitat":"Suzaku mountain range, Great Saint Continent peaks, Wild Continent forests",
     "cultivation":"Core Formation -> Nascent Soul -> Spirit Severing (body-tempering)",
     "intelligence":6,"diet":"spirit fruit, mountain herbs, weaker beasts, lost cultivators",
     "relationships":"Rival of mountain lions; tamed by body-cultivation sects",
     "techniques":["iron_fist","mountain_roar","four_arm_sweep","boulder_throw"],
     "treasures":["ape_blood_essence","iron_fur_pelt","four_arm_bone"],
     "bloodline":"Ancient Savage-Ape lineage; body-tempering bloodline",
     "mutations":["four_arm","iron_bone","ancient_savage_emperor"],
     "canon_confidence":4},

    {"id":"six_desire_devil","name":"Six-Desire Devil","nameCn":"六欲魔","novel":"Renegade Immortal",
     "appearance":"Humanoid devil 3 m tall, six arms, body wreathed in pink desire-mist, face shifts to mirror the desires of viewers",
     "size":"3 m adult, 400 jin (mostly spirit, not flesh)",
     "behavior":"Lures cultivators by manifesting their deepest desires; drains desire-essence; cultivates by consuming the fallen",
     "habitat":"Sea of Devils, desire-aspected caves, corrupted spirit veins",
     "cultivation":"Nascent Soul -> Spirit Severing -> Ascendant (desire attribute)",
     "intelligence":9,"diet":"desire-essence, fallen cultivator soul fragments, desire-aspected herbs",
     "relationships":"Servant-devils of devil-sects; enemies of all orthodox cultivators",
     "techniques":["desire_mirror","six_arm_grasp","pink_mist_drain","face_shift"],
     "treasures":["six_desire_core","desire_mirror_fragment","pink_mist_essence"],
     "bloodline":"Desire-Devil lineage; born of accumulated mortal desire",
     "mutations":["twelve_arm","true_desire_embodiment","desire_immortal_body"],
     "canon_confidence":4},

    {"id":"blood_ancestor_beast","name":"Blood Ancestor Beast","nameCn":"血祖兽","novel":"Renegade Immortal",
     "appearance":"Formless mass of congealed blood 50 m across, can shape into any beast form, perpetually dripping",
     "size":"50 m mass, 100000 jin; ancient forms span kilometers",
     "behavior":"Seeks bloodlines to consume; cultivates by absorbing entire bloodlines; killed by Wang Lin after long conflict",
     "habitat":"Blood-aspected forbidden zones, ancient battlefields, Blood Sea",
     "cultivation":"Ascendant -> Immortal (blood attribute)",
     "intelligence":8,"diet":"bloodlines, blood-aspected herbs, entire beast species",
     "relationships":"Enemy of Wang Lin; ancestor of all blood-mutation beasts",
     "techniques":["blood_assimilate","form_shift","blood_sea_flood","bloodline_devour"],
     "treasures":["blood_ancestor_core","congealed_blood_essence","bloodline_fragment"],
     "bloodline":"Originator of all blood-mutation bloodlines",
     "mutations":["formless","all_bloodline_absorption"],
     "canon_confidence":4},

    {"id":"soul_lasher_beast","name":"Soul Lasher Beast","nameCn":"魂鞭兽","novel":"Renegade Immortal",
     "appearance":"Serpentine body 8 m long covered in soul-tendrils, no eyes, senses by soul-fluctuation",
     "size":"8 m adult, 800 jin; ancient 30 m, 10000 jin",
     "behavior":"Lashes prey with soul-tendrils that drain cultivation base; herds prey into dead-end caves",
     "habitat":"Soul-aspected caves, Soul Refining territory, ghost valleys",
     "cultivation":"Nascent Soul -> Spirit Severing (soul attribute)",
     "intelligence":7,"diet":"soul fragments, cultivation base, soul-aspected herbs",
     "relationships":"Bred by Soul Refining Sect as guardians",
     "techniques":["soul_lash","soul_herd","cultivation_drain","soul_tendril_burst"],
     "treasures":["soul_lasher_core","soul_tendril","soul_fragment_cluster"],
     "bloodline":"Soul-Devil lineage; born of accumulated soul fragments",
     "mutations":["thousand_tendril","soul_immortal_body"],
     "canon_confidence":3},

    {"id":"restriction_beast","name":"Restriction Beast","nameCn":"禁制兽","novel":"Renegade Immortal",
     "appearance":"Formless beast made of living restriction runes, body shifts between inscription patterns",
     "size":"Variable; manifests as 2-20 m rune-cluster",
     "behavior":"Inhabits ancient restriction arrays; attacks those who break restrictions; cultivates by absorbing broken restrictions",
     "habitat":"Restriction Mountain, ancient formation sites, sealed caves",
     "cultivation":"Spirit Severing -> Ascendant (restriction attribute)",
     "intelligence":10,"diet":"broken restrictions, formation fragments, restriction-aspected herbs",
     "relationships":"Guardian of Restriction Mountain; neutral to all cultivators who do not break restrictions",
     "techniques":["restriction_rend","rune_burst","formation_absorb","seal_impose"],
     "treasures":["restriction_core","rune_fragment","formation_essence"],
     "bloodline":"Restriction-Dao lineage; born of accumulated broken restrictions",
     "mutations":["formation_body","true_restriction_embodiment"],
     "canon_confidence":3},

    {"id":"giant_python","name":"Giant Python","nameCn":"巨蟒","novel":"Renegade Immortal",
     "appearance":"Serpentine body 20 m long, scales earth-brown, jaw can dislocate to swallow cultivators whole",
     "size":"20 m adult, 8000 jin; ancient 100 m, 100000 jin",
     "behavior":"Ambush predator; coils in spirit-herb groves; swallows prey to cultivate in stillness",
     "habitat":"Spirit herb hills, dense forests, river basins of Zhao/Chu",
     "cultivation":"Core Formation -> Nascent Soul (earth attribute)",
     "intelligence":4,"diet":"spirit herbs, weaker beasts, lost cultivators",
     "relationships":"Common prey of thunder beasts; rival of giant centipedes",
     "techniques":["constrict","swallow_whole","python_coil","earth_burrow"],
     "treasures":["python_core","python_gall","scale_pelt"],
     "bloodline":"Earth-Python lineage; dilute dragon bloodline",
     "mutations":["horned_python","half_dragon","ancient_python_emperor"],
     "canon_confidence":4},

    {"id":"sand_scorpion","name":"Sand Scorpion","nameCn":"沙蝎","novel":"Renegade Immortal",
     "appearance":"Armored scorpion 2 m long, pincers can sever iron, stinger drips paralytic venom",
     "size":"2 m adult, 200 jin; ancient 8 m, 5000 jin",
     "behavior":"Burrows in sand; hunts in packs at dusk; paralyzes prey then drags to nest",
     "habitat":"Fire Burn Country deserts, karma wastes, arid plains",
     "cultivation":"Foundation Establishment -> Core Formation (venom attribute)",
     "intelligence":3,"diet":"desert beasts, lost travelers, sand-attribute herbs",
     "relationships":"Pack hunters; preyed upon by giant pythons",
     "techniques":["burrow","venom_sting","pincer_sever","sand_swim"],
     "treasures":["scorpion_core","venom_gland","scorpion_carapace"],
     "bloodline":"Desert-Scorpion lineage; venom-attribute bloodline",
     "mutations":["nine_stinger","golden_carapace","ancient_scorpion_emperor"],
     "canon_confidence":4},

    {"id":"ice_phoenix","name":"Ice Phoenix","nameCn":"冰凰","novel":"Renegade Immortal",
     "appearance":"Avian 5 m wingspan, feathers crystalline ice-blue, tears freeze the air",
     "size":"5 m wingspan adult, 600 jin; ancient 20 m, 8000 jin",
     "behavior":"Solitary; nests on glacial peaks; freezes prey then shatters; hoards ice-attribute treasures",
     "habitat":"Snow Domain Country peaks, ice-aspected spirit veins, glacial caves",
     "cultivation":"Spirit Severing -> Ascendant (ice attribute)",
     "intelligence":9,"diet":"ice-attribute herbs, frozen prey, cold-spring fish",
     "relationships":"Rival of flame dragon; tributed by Snow Domain royalty",
     "techniques":["ice_tears","glacial_roost","freeze_breath","phoenix_rebirth"],
     "treasures":["ice_phoenix_core","phoenix_feather","frost_essence"],
     "bloodline":"Ancient Phoenix lineage; ice-attribute true phoenix bloodline (diluted)",
     "mutations":["nine_tail_feather","true_phoenix_awakening","ice_immortal_body"],
     "canon_confidence":4},

    {"id":"vermilion_bird","name":"Vermilion Bird","nameCn":"朱雀","novel":"Renegade Immortal",
     "appearance":"Divine bird 30 m wingspan, feathers the color of molten fire, three tail feathers trail nine-color flame",
     "size":"30 m wingspan, 100000 jin; true form fills the sky",
     "behavior":"Divine spirit beast; sleeps for epochs; awakens to choose Divine Emperor; rivals Azure Dragon",
     "habitat":"Vermilion Bird Divine Sect, Vermilion Bird Starfield core, fire-aspected apex spirit veins",
     "cultivation":"Immortal (fire attribute, true divine beast)",
     "intelligence":12,"diet":"fire-attribute heavenly treasures, tribulation flames, sun-essence",
     "relationships":"Bonded to Vermilion Bird Divine Emperor (Wang Lin was 6th generation); rival of Azure Dragon",
     "techniques":["vermilion_breath","divine_rebirth","nine_color_flame","star_burning"],
     "treasures":["vermilion_bird_core","divine_feather","nine_color_flame_essence"],
     "bloodline":"True Divine Beast lineage; one of Four Symbols",
     "mutations":["nine_color_body","true_divine_awakening"],
     "canon_confidence":5},

    {"id":"azure_dragon","name":"Azure Dragon","nameCn":"青龙","novel":"Renegade Immortal",
     "appearance":"Serpentine dragon 100 m long, scales azure jade, antlers of crystal, breath summons storms",
     "size":"100 m adult, millions of jin; true form spans continents",
     "behavior":"Divine spirit beast; sleeps in deep seas; awakens for era-defining events",
     "habitat":"Eastern seas, dragon palaces, water-aspected apex spirit veins",
     "cultivation":"Immortal (water/wind attribute, true divine beast)",
     "intelligence":12,"diet":"water-attribute heavenly treasures, sea-spirit beasts, storm-essence",
     "relationships":"Bonded to Azure Dragon Divine lineage; rival of Vermilion Bird",
     "techniques":["storm_breath","dragon_coil","rain_summon","azure_scales"],
     "treasures":["azure_dragon_core","dragon_pearl","azure_scale"],
     "bloodline":"True Divine Beast lineage; one of Four Symbols",
     "mutations":["five_claw","true_divine_awakening"],
     "canon_confidence":4},

    {"id":"white_tiger","name":"White Tiger","nameCn":"白虎","novel":"Renegade Immortal",
     "appearance":"Tiger 8 m long, fur white-gold, stripes glow with killing intent, fangs drip metallic qi",
     "size":"8 m adult, 20000 jin; true form fills battlefields",
     "behavior":"Divine spirit beast of slaughter; appears at world wars; hunts the wicked",
     "habitat":"Foreign Battleground, slaughter-aspected apex spirit veins",
     "cultivation":"Immortal (metal/slaughter attribute, true divine beast)",
     "intelligence":12,"diet":"slaughter-qi, wicked cultivator souls, metal-attribute treasures",
     "relationships":"Bonded to White Tiger Divine lineage; rival of Black Tortoise",
     "techniques":["killing_pounce","metal_fang","slaughter_roar","white_gold_scales"],
     "treasures":["white_tiger_core","tiger_fang","white_gold_pelt"],
     "bloodline":"True Divine Beast lineage; one of Four Symbols",
     "mutations":["six_leg","true_divine_awakening"],
     "canon_confidence":4},

    {"id":"black_tortoise","name":"Black Tortoise","nameCn":"玄武","novel":"Renegade Immortal",
     "appearance":"Tortoise 20 m shell, shell black-jade with coiled snake-tail and snake-head neck",
     "size":"20 m shell adult, 500000 jin; true form supports continents",
     "behavior":"Divine spirit beast of defense; supports continents in slumber; awakens for world-founding events",
     "habitat":"Xuan Wu country, deep ocean trenches, earth-aspected apex spirit veins",
     "cultivation":"Immortal (earth/water attribute, true divine beast)",
     "intelligence":12,"diet":"earth-attribute heavenly treasures, deep-sea beasts, continent-essence",
     "relationships":"Bonded to Xuan Wu imperial lineage; rival of White Tiger",
     "techniques":["shell_defense","snake_bite","continent_support","black_jade_shell"],
     "treasures":["black_tortoise_core","shell_fragment","snake_pearl"],
     "bloodline":"True Divine Beast lineage; one of Four Symbols",
     "mutations":["six_leg","true_divine_awakening"],
     "canon_confidence":5},

    {"id":"qilin","name":"Qilin","nameCn":"麒麟","novel":"Renegade Immortal",
     "appearance":"Chimeric beast 5 m long, body of deer scaled in jade, tail of ox, single horn of crystal, hooves of fire",
     "size":"5 m adult, 8000 jin; ancient 15 m, 100000 jin",
     "behavior":"Auspicious beast; appears at the birth of great cultivators; avoids the wicked; cultivates by walking auspicious paths",
     "habitat":"Auspicious spirit veins, Qilin City region, continent hearts",
     "cultivation":"Ascendant -> Immortal (auspicious attribute)",
     "intelligence":11,"diet":"auspicious herbs, spirit-rice, dawn-dew",
     "relationships":"Bonded to Qilin City imperial lineage; auspicious to all cultivators",
     "techniques":["auspicious_step","horn_pierce","jade_scale_defense","fire_hoof"],
     "treasures":["qilin_core","horn_fragment","jade_scale"],
     "bloodline":"True Auspicious lineage; one of the four auspicious beasts",
     "mutations":["nine_horn","true_auspicious_awakening"],
     "canon_confidence":4},

    {"id":"spirit_crane","name":"Spirit Crane","nameCn":"仙鹤","novel":"Renegade Immortal",
     "appearance":"Elegant crane 2 m tall, feathers white with red crown, wings trail spirit-light",
     "size":"2 m tall, 2 m wingspan, 30 jin; ancient 5 m, 500 jin",
     "behavior":"Migratory; flies in formations sword-cultivators follow; nests on sword peaks; cultivates by dancing at dawn",
     "habitat":"Sword sect peaks, sky migration routes, dawn-aspected spirit veins",
     "cultivation":"Core Formation -> Nascent Soul (sword/wind attribute)",
     "intelligence":8,"diet":"dawn-dew, spirit-rice, sword-attribute herbs",
     "relationships":"Followed by sword cultivators; preyed upon by thunder birds",
     "techniques":["crane_dance","sword_wing","dawn_call","formation_flight"],
     "treasures":["crane_feather","spirit_crown","dawn_essence"],
     "bloodline":"Spirit-Crane lineage; sword-attribute bloodline",
     "mutations":["nine_feather","sword_body","ancient_crane_emperor"],
     "canon_confidence":4},

    {"id":"ant_beast","name":"Ant Beast","nameCn":"蚁兽","novel":"Renegade Immortal",
     "appearance":"Giant ant 1 m long (worker) to 3 m (soldier), mandibles can sever spirit-steel, queens 8 m",
     "size":"1-8 m, 50-5000 jin by caste",
     "behavior":"Eusocial; builds underground kingdoms millions strong; cultivates communally",
     "habitat":"Underground spirit mines, ant kingdom biomes, earth-aspected deep caves",
     "cultivation":"Foundation Establishment (worker) -> Core Formation (soldier) -> Nascent Soul (queen)",
     "intelligence":5,"diet":"spirit-ore, underground herbs, lost miners",
     "relationships":"Enemy of underground civilizations; rival of giant centipedes",
     "techniques":["mandible_sever","acid_spit","swarm_overwhelm","tunnel_burst"],
     "treasures":["ant_core","mandible_fragment","queen_pearl"],
     "bloodline":"Earth-Ant lineage; collective cultivation bloodline",
     "mutations":["winged_soldier","golden_mandible","ancient_ant_emperor"],
     "canon_confidence":4},

    {"id":"web_weaver_spider","name":"Web Weaver Spider","nameCn":"织网蛛","novel":"Renegade Immortal",
     "appearance":"Spider 3 m legspan, web silk is spirit-steel strong, eight eyes glow in patterns",
     "size":"3 m adult, 400 jin; ancient 10 m, 8000 jin",
     "behavior":"Spins webs across valleys; webs trap cultivators by cultivation base; cultivates by counting trapped souls",
     "habitat":"Forest valleys, fog marshes, web-canopied spirit-herb groves",
     "cultivation":"Core Formation -> Nascent Soul (binding attribute)",
     "intelligence":7,"diet":"trapped cultivators, weaker beasts, web-caught birds",
     "relationships":"Rival of giant centipedes; tamed by binding-sects",
     "techniques":["web_weave","silk_bind","venom_bite","eye_hypnosis"],
     "treasures":["spider_core","web_silk","venom_gland"],
     "bloodline":"Spirit-Spider lineage; binding-attribute bloodline",
     "mutations":["hundred_eye","golden_silk","ancient_spider_emperor"],
     "canon_confidence":3},

    {"id":"giant_centipede","name":"Giant Centipede","nameCn":"巨蜈蚣","novel":"Renegade Immortal",
     "appearance":"Centipede 8 m long, hundred legs of spirit-iron, mandibles drip corrosive venom",
     "size":"8 m adult, 2000 jin; ancient 30 m, 50000 jin",
     "behavior":"Subterranean predator; tunnels through stone; venom dissolves spirit-armor",
     "habitat":"Underground caverns, earth-aspected deep caves, beneath ancient battlefields",
     "cultivation":"Core Formation -> Nascent Soul (venom attribute)",
     "intelligence":4,"diet":"underground beasts, lost miners, corpse marrow",
     "relationships":"Enemy of ant beasts; rival of web weavers",
     "techniques":["tunnel","venom_bite","hundred_leg_sweep","corrode_armor"],
     "treasures":["centipede_core","venom_gland","spirit_iron_leg"],
     "bloodline":"Earth-Centipede lineage; venom-attribute bloodline",
     "mutations":["thousand_leg","golden_carapace","ancient_centipede_emperor"],
     "canon_confidence":3},

    {"id":"nine_headed_snake","name":"Nine-Headed Snake","nameCn":"九头蛇","novel":"Renegade Immortal",
     "appearance":"Serpentine body 20 m long, nine snake-heads on branching necks, each head breathes a different element",
     "size":"20 m adult, 15000 jin; ancient 100 m, 200000 jin",
     "behavior":"Apex predator; each head cultivates independently; regenerates severed heads",
     "habitat":"Forbidden valleys, deep swamps, chaos-aspected spirit veins",
     "cultivation":"Nascent Soul -> Spirit Severing (chaos attribute)",
     "intelligence":8,"diet":"nine-element herbs, weaker beasts, lost cultivators",
     "relationships":"Rival of giant pythons; preyed upon only by Spirit Severing cultivators",
     "techniques":["nine_breath","head_regen","coil_constrict","element_swap"],
     "treasures":["nine_head_core","element_essence","snake_pearl"],
     "bloodline":"Chaos-Snake lineage; nine-element bloodline",
     "mutations":["hundred_head","chaos_body","ancient_snake_emperor"],
     "canon_confidence":3},

    {"id":"wind_wolf","name":"Wind Wolf","nameCn":"风狼","novel":"Renegade Immortal",
     "appearance":"Wolf 2 m tall, fur silver-grey, moves faster than eye can track, breath carries cutting wind",
     "size":"2 m adult, 150 jin; ancient 4 m, 2000 jin",
     "behavior":"Pack hunter; surrounds prey then strikes as one; cultivates by running through storms",
     "habitat":"Plains, wind-aspected spirit veins, mountain foothills",
     "cultivation":"Foundation Establishment -> Core Formation (wind attribute)",
     "intelligence":6,"diet":"plains beasts, spirit-herb grazers, lost travelers",
     "relationships":"Pack hunters; preyed upon by thunder beasts",
     "techniques":["wind_step","pack_surround","cutting_breath","storm_run"],
     "treasures":["wolf_core","wind_fur","fang"],
     "bloodline":"Wind-Wolf lineage; wind-attribute bloodline",
     "mutations":["silver_fur","wind_body","ancient_wolf_emperor"],
     "canon_confidence":3},

    {"id":"mountain_lion","name":"Mountain Lion","nameCn":"山狮","novel":"Renegade Immortal",
     "appearance":"Lion 3 m long, fur earth-gold, mane of stone-spike fur, roar causes rockslides",
     "size":"3 m adult, 600 jin; ancient 8 m, 10000 jin",
     "behavior":"Solitary apex predator of mountains; territory spans peaks; hunts by ambush from above",
     "habitat":"Mountain ranges, earth-aspected peaks, cliff caves",
     "cultivation":"Core Formation -> Nascent Soul (earth attribute)",
     "intelligence":7,"diet":"mountain beasts, spirit-herb grazers, lost cultivators",
     "relationships":"Rival of giant savage apes; tributed by mountain-sects",
     "techniques":["rockslide_roar","ambush_pounce","stone_mane_defense","earth_step"],
     "treasures":["lion_core","mane_pelt","fang"],
     "bloodline":"Earth-Lion lineage; earth-attribute bloodline",
     "mutations":["stone_body","golden_mane","ancient_lion_emperor"],
     "canon_confidence":3},

    # ------------------ Pursuit of the Truth (求魔) ------------------
    {"id":"berserk_beast","name":"Berserk Beast","nameCn":"狂兽","novel":"Pursuit of the Truth",
     "appearance":"Shifting bestial form 4 m tall, body perpetually wreathed in savage qi, eyes crimson",
     "size":"4 m adult, 3000 jin; ancient 15 m, 100000 jin",
     "behavior":"Born of accumulated savage qi; hunts by berserk charge; cultivates by killing",
     "habitat":"Southern Region savage lands, berserk-aspected forbidden zones",
     "cultivation":"Nascent Soul -> Spirit Severing (berserk attribute)",
     "intelligence":5,"diet":"savage qi, weaker beasts, lost cultivators",
     "relationships":"Enemy of Su Ming's lineage; born of the same savage qi",
     "techniques":["berserk_charge","savage_roar","blood_frenzy","form_shift"],
     "treasures":["berserk_core","savage_qi_essence","crimson_fang"],
     "bloodline":"Savage-Beast lineage; berserk-attribute bloodline",
     "mutations":["true_berserk_body","savage_emperor"],
     "canon_confidence":3},

    {"id":"blood_beast","name":"Blood Beast","nameCn":"血兽","novel":"Pursuit of the Truth",
     "appearance":"Formless blood-mass 5 m, can shape into any beast, perpetually dripping",
     "size":"5 m mass, 8000 jin; ancient spans battlefields",
     "behavior":"Born of battlefield blood; cultivates by consuming bloodlines; mutates prey",
     "habitat":"Ancient battlefields, blood-aspected forbidden zones",
     "cultivation":"Spirit Severing (blood attribute)",
     "intelligence":6,"diet":"bloodlines, blood-aspected herbs",
     "relationships":"Lesser kin of Blood Ancestor Beast",
     "techniques":["blood_assimilate","form_shift","blood_drain"],
     "treasures":["blood_core","blood_essence"],
     "bloodline":"Blood-Beast lineage; dilute Blood Ancestor bloodline",
     "mutations":["formless","all_bloodline_absorption"],
     "canon_confidence":3},

    {"id":"soul_beast","name":"Soul Beast","nameCn":"魂兽","novel":"Pursuit of the Truth",
     "appearance":"Ghostly beast 3 m, body semi-translucent, eyes cold green fire",
     "size":"3 m adult, 100 jin (mostly spirit)",
     "behavior":"Hunts soul fragments; cultivates by consuming souls",
     "habitat":"Soul-aspected forbidden zones, ghost valleys",
     "cultivation":"Nascent Soul (soul attribute)",
     "intelligence":7,"diet":"soul fragments, soul-aspected herbs",
     "relationships":"Lesser kin of Nether Beast",
     "techniques":["soul_drain","ghost_step","soul_rend"],
     "treasures":["soul_core","soul_fragment"],
     "bloodline":"Soul-Beast lineage; soul-attribute bloodline",
     "mutations":["soul_immortal_body"],
     "canon_confidence":3},

    {"id":"southern_winged_snake","name":"Southern Winged Snake","nameCn":"南翼蛇","novel":"Pursuit of the Truth",
     "appearance":"Serpentine body 6 m, two feathered wings, venom-dripping fangs",
     "size":"6 m adult, 1200 jin; ancient 20 m, 20000 jin",
     "behavior":"Aerial ambush predator; nests on southern cliffs; hunts at dusk",
     "habitat":"Southern Region cliffs, sky-aspected spirit veins",
     "cultivation":"Core Formation -> Nascent Soul (wind/poison attribute)",
     "intelligence":5,"diet":"birds, spirit-herb grazers, lost cultivators",
     "relationships":"Rival of spirit cranes; preyed upon by thunder birds",
     "techniques":["aerial_dive","venom_bite","wing_buffet"],
     "treasures":["snake_core","wing_feather","venom_gland"],
     "bloodline":"Southern-Winged-Snake lineage; wind/poison bloodline",
     "mutations":["nine_wing","ancient_serpent_emperor"],
     "canon_confidence":3},

    {"id":"thunder_ape","name":"Thunder Ape","nameCn":"雷猿","novel":"Pursuit of the Truth",
     "appearance":"Bipedal ape 4 m tall, fur crackling with thunder, fists strike with lightning",
     "size":"4 m adult, 4000 jin; ancient 12 m, 80000 jin",
     "behavior":"Pack hunter during storms; cultivates by absorbing lightning on peaks",
     "habitat":"Thunder peaks, storm-aspected mountains",
     "cultivation":"Core Formation -> Nascent Soul (thunder attribute)",
     "intelligence":6,"diet":"thunder-herbs, weaker beasts, lost cultivators",
     "relationships":"Rival of Lei Ji; preyed upon by thunder celestial beasts",
     "techniques":["thunder_fist","storm_howl","lightning_pounce"],
     "treasures":["thunder_ape_core","thunder_fur"],
     "bloodline":"Thunder-Ape lineage; thunder-attribute bloodline",
     "mutations":["nine_arm","ancient_thunder_emperor"],
     "canon_confidence":3},

    {"id":"ink_bat","name":"Ink Bat","nameCn":"墨蝠","novel":"Pursuit of the Truth",
     "appearance":"Bat 2 m wingspan, fur ink-black, sonar drains cultivation base",
     "size":"2 m wingspan, 50 jin; ancient 8 m, 1000 jin",
     "behavior":"Cave swarm; hunts by sonar; cultivates in darkness",
     "habitat":"Ink caves, darkness-aspected caverns",
     "cultivation":"Foundation Establishment -> Core Formation (darkness attribute)",
     "intelligence":4,"diet":"cave insects, lost miners, weaker beasts",
     "relationships":"Swarm hunters; preyed upon by web weavers",
     "techniques":["sonar_drain","swarm_overwhelm","darkness_step"],
     "treasures":["bat_core","ink_fur"],
     "bloodline":"Ink-Bat lineage; darkness-attribute bloodline",
     "mutations":["thousand_wing","ancient_bat_emperor"],
     "canon_confidence":3},

    {"id":"ink_dragon","name":"Ink Dragon","nameCn":"墨龙","novel":"Pursuit of the Truth",
     "appearance":"Serpentine dragon 50 m, scales ink-black, breath extinguishes light",
     "size":"50 m adult, 100000 jin; ancient 300 m, millions of jin",
     "behavior":"Solitary apex predator of ink caves; sleeps for centuries; hoards darkness treasures",
     "habitat":"Ink dragon caves, darkness-aspected apex spirit veins",
     "cultivation":"Spirit Severing -> Ascendant (darkness attribute)",
     "intelligence":9,"diet":"darkness herbs, ink bats, lost cultivators",
     "relationships":"Apex of ink caves; rival of flame dragon",
     "techniques":["darkness_breath","light_extinguish","ink_coil"],
     "treasures":["ink_dragon_core","dragon_scale","darkness_essence"],
     "bloodline":"Ink-Dragon lineage; darkness-attribute true dragon bloodline (diluted)",
     "mutations":["true_dragon_awakening","darkness_immortal_body"],
     "canon_confidence":3},

    # ------------------ I Shall Seal the Heavens (我欲封天) ------------------
    {"id":"heaven_defying_beast","name":"Heaven-Defying Beast","nameCn":"逆天兽","novel":"I Shall Seal the Heavens",
     "appearance":"Formless beast of defied-heaven law, manifests as a chimeric horror 10 m tall",
     "size":"10 m adult, 20000 jin; true form spans skies",
     "behavior":"Born of accumulated defied-heaven qi; hunts those who defy heaven; cultivates by absorbing heavenly backlash",
     "habitat":"Heaven-defying forbidden zones, tribulation aftermath sites",
     "cultivation":"Ascendant -> Immortal (defy attribute)",
     "intelligence":10,"diet":"heavenly backlash, defied-heaven qi, tribulation remnants",
     "relationships":"Enemy of all cultivators who defy heaven",
     "techniques":["heaven_defy","backlash_absorb","defy_breath"],
     "treasures":["defy_core","backlash_essence"],
     "bloodline":"Heaven-Defying lineage; defy-attribute bloodline",
     "mutations":["true_defy_body","defy_immortal_emperor"],
     "canon_confidence":3},

    {"id":"meat_jelly","name":"Meat Jelly","nameCn":"肉冻","novel":"I Shall Seal the Heavens",
     "appearance":"Quivering mass of sentient meat 1 m, can shape into any form, perpetually shimmering",
     "size":"1 m mass, 200 jin; ancient 5 m, 5000 jin",
     "behavior":"Companion beast; cultivates by absorbing meat-essence; speaks in squeaks",
     "habitat":"Anywhere Meng Hao goes; meat-aspected spirit veins",
     "cultivation":"Nascent Soul -> Spirit Severing (meat attribute)",
     "intelligence":8,"diet":"meat, meat-essence, spirit-rice",
     "relationships":"Bonded companion to Meng Hao",
     "techniques":["meat_shape","meat_absorb","squeak_call"],
     "treasures":["meat_jelly_core","meat_essence"],
     "bloodline":"Meat-Jelly lineage; unique bloodline",
     "mutations":["true_meat_body","meat_immortal_emperor"],
     "canon_confidence":3},

    {"id":"ghost_spirit_beast","name":"Ghost Spirit Beast","nameCn":"鬼灵兽","novel":"I Shall Seal the Heavens",
     "appearance":"Ghostly beast 4 m, body shifts between physical and ghost-form",
     "size":"4 m adult, 500 jin (material) / 0 jin (ghost)",
     "behavior":"Hunts in ghost-form, strikes in physical-form; cultivates by consuming ghosts",
     "habitat":"Ghost-aspected forbidden zones, nether-aspected caves",
     "cultivation":"Nascent Soul -> Spirit Severing (ghost attribute)",
     "intelligence":7,"diet":"ghosts, ghost-aspected herbs",
     "relationships":"Lesser kin of Nether Beast",
     "techniques":["ghost_shift","physical_strike","ghost_drain"],
     "treasures":["ghost_core","ghost_essence"],
     "bloodline":"Ghost-Spirit lineage; ghost-attribute bloodline",
     "mutations":["true_ghost_body"],
     "canon_confidence":3},

    {"id":"nine_colored_peacock","name":"Nine-Colored Peacock","nameCn":"九色孔雀","novel":"I Shall Seal the Heavens",
     "appearance":"Peacock 4 m tall, tail of nine-color feathers 12 m long, each feather holds a different law",
     "size":"4 m adult, 1000 jin; ancient 12 m, 20000 jin",
     "behavior":"Divine beast; displays tail to project nine laws; cultivates by absorbing law-fragments",
     "habitat":"Law-aspected apex spirit veins, nine-color forbidden zones",
     "cultivation":"Ascendant -> Immortal (law attribute)",
     "intelligence":11,"diet":"law-fragments, nine-element herbs, spirit-rice",
     "relationships":"Auspicious to cultivators who comprehend law",
     "techniques":["nine_law_display","tail_strike","law_projection"],
     "treasures":["peacock_core","nine_color_feather","law_fragment"],
     "bloodline":"Nine-Color-Peacock lineage; law-attribute bloodline",
     "mutations":["true_law_body","law_immortal_emperor"],
     "canon_confidence":3},

    {"id":"dragon_ape","name":"Dragon Ape","nameCn":"龙猿","novel":"I Shall Seal the Heavens",
     "appearance":"Bipedal ape 6 m tall, scales of dragon, fists of dragon-strength, horns on head",
     "size":"6 m adult, 15000 jin; ancient 20 m, 200000 jin",
     "behavior":"Solitary apex predator; combines ape strength with dragon durability",
     "habitat":"Dragon-aspected mountains, dragon-ape peaks",
     "cultivation":"Nascent Soul -> Spirit Severing (dragon/body attribute)",
     "intelligence":7,"diet":"dragon-herbs, weaker beasts, lost cultivators",
     "relationships":"Hybrid bloodline; rival of giant savage ape",
     "techniques":["dragon_fist","scale_defense","dragon_roar"],
     "treasures":["dragon_ape_core","dragon_scale","horn"],
     "bloodline":"Dragon-Ape hybrid lineage; dragon + ape bloodline",
     "mutations":["true_dragon_awakening","ancient_dragon_ape_emperor"],
     "canon_confidence":3},

    {"id":"sun_moon_spider","name":"Sun-Moon Spider","nameCn":"日月蛛","novel":"I Shall Seal the Heavens",
     "appearance":"Spider 4 m, body half sun-gold half moon-silver, web is sun-moon law",
     "size":"4 m adult, 600 jin; ancient 12 m, 12000 jin",
     "behavior":"Spins sun-moon webs that trap by law; cultivates by absorbing sun-moon essence",
     "habitat":"Sun-moon peaks, law-aspected spirit veins",
     "cultivation":"Spirit Severing (sun/moon attribute)",
     "intelligence":9,"diet":"sun-moon essence, sun-moon herbs, trapped cultivators",
     "relationships":"Auspicious to cultivators who comprehend sun-moon",
     "techniques":["sun_moon_web","law_bind","sun_moon_bite"],
     "treasures":["sun_moon_core","sun_moon_silk","law_fragment"],
     "bloodline":"Sun-Moon-Spider lineage; sun/moon bloodline",
     "mutations":["true_sun_moon_body"],
     "canon_confidence":3},

    # ------------------ A Will Eternal (一念永恒) ------------------
    {"id":"heaven_devouring_beast","name":"Heaven-Devouring Beast","nameCn":"吞天兽","novel":"A Will Eternal",
     "appearance":"Formless maw 20 m, can swallow mountains, body is hunger incarnate",
     "size":"20 m adult, 100000 jin; true form swallows continents",
     "behavior":"Cultivates by devouring everything; hunger drives all behavior",
     "habitat":"Devour-aspected forbidden zones, void edges",
     "cultivation":"Ascendant -> Immortal (devour attribute)",
     "intelligence":6,"diet":"everything",
     "relationships":"Enemy of all existence",
     "techniques":["devour_swallow","void_maw","hunger_frenzy"],
     "treasures":["devour_core","void_essence"],
     "bloodline":"Devour lineage; devour-attribute bloodline",
     "mutations":["true_devour_body","devour_immortal_emperor"],
     "canon_confidence":3},

    {"id":"ghost_face_beast","name":"Ghost Face Beast","nameCn":"鬼面兽","novel":"A Will Eternal",
     "appearance":"Beast 5 m, face is a screaming ghost, body shadow-wreathed",
     "size":"5 m adult, 2000 jin; ancient 15 m, 40000 jin",
     "behavior":"Hunts by fear; cultivates by consuming terror",
     "habitat":"Fear-aspected forbidden zones, ghost valleys",
     "cultivation":"Nascent Soul -> Spirit Severing (fear attribute)",
     "intelligence":7,"diet":"fear, weaker beasts, lost cultivators",
     "relationships":"Lesser kin of Six-Desire Devil",
     "techniques":["fear_roar","ghost_face_display","shadow_drain"],
     "treasures":["ghost_face_core","fear_essence"],
     "bloodline":"Ghost-Face lineage; fear-attribute bloodline",
     "mutations":["true_fear_body"],
     "canon_confidence":3},

    {"id":"iron_bear","name":"Iron Bear","nameCn":"铁熊","novel":"A Will Eternal",
     "appearance":"Bear 4 m tall, fur iron-grey, hide repels spirit-steel, claws cut armor",
     "size":"4 m adult, 6000 jin; ancient 10 m, 80000 jin",
     "behavior":"Territorial body-tempering beast; cultivates by absorbing metal-essence",
     "habitat":"Metal-aspected mountains, iron-ore regions",
     "cultivation":"Core Formation -> Nascent Soul (metal attribute)",
     "intelligence":5,"diet":"metal-herbs, weaker beasts, lost miners",
     "relationships":"Tributed by body-cultivation sects",
     "techniques":["iron_hide","metal_claw","body_slam"],
     "treasures":["iron_bear_core","iron_hide","metal_claw"],
     "bloodline":"Iron-Bear lineage; metal-attribute bloodline",
     "mutations":["iron_body","ancient_iron_emperor"],
     "canon_confidence":3},

    {"id":"spirit_rat","name":"Spirit Rat","nameCn":"灵鼠","novel":"A Will Eternal",
     "appearance":"Rat 0.5 m, fur spirit-gold, can sense spirit-treasures for li",
     "size":"0.5 m adult, 5 jin; ancient 2 m, 200 jin",
     "behavior":"Treasure-seeking companion beast; cultivates by hoarding spirit-treasures",
     "habitat":"Spirit-treasure-rich regions, ancient ruins",
     "cultivation":"Foundation Establishment -> Core Formation (treasure attribute)",
     "intelligence":8,"diet":"spirit-treasure fragments, spirit-rice",
     "relationships":"Bonded companion to treasure-seekers",
     "techniques":["treasure_sense","burrow","squeak_alarm"],
     "treasures":["rat_core","treasure_fragment"],
     "bloodline":"Spirit-Rat lineage; treasure-attribute bloodline",
     "mutations":["golden_fur","treasure_immortal_body"],
     "canon_confidence":3},

    {"id":"cold_dragon","name":"Cold Dragon","nameCn":"寒龙","novel":"A Will Eternal",
     "appearance":"Serpentine dragon 40 m, scales ice-blue, breath freezes spirit",
     "size":"40 m adult, 80000 jin; ancient 200 m, millions of jin",
     "behavior":"Solitary apex of cold regions; sleeps in glacial depths",
     "habitat":"Glacial seas, ice-aspected apex spirit veins",
     "cultivation":"Spirit Severing -> Ascendant (ice attribute)",
     "intelligence":9,"diet":"ice-herbs, cold-spring fish, frozen prey",
     "relationships":"Rival of ice phoenix; kin of flame dragon (opposite attribute)",
     "techniques":["cold_breath","ice_coil","freeze_spirit"],
     "treasures":["cold_dragon_core","ice_scale","cold_essence"],
     "bloodline":"Cold-Dragon lineage; ice-attribute true dragon bloodline (diluted)",
     "mutations":["true_dragon_awakening","ice_immortal_body"],
     "canon_confidence":3},

    {"id":"seven_colored_deer","name":"Seven-Colored Deer","nameCn":"七色鹿","novel":"A Will Eternal",
     "appearance":"Deer 2 m tall, coat shifts through seven colors, antlers of crystal",
     "size":"2 m adult, 300 jin; ancient 5 m, 5000 jin",
     "behavior":"Auspicious beast; appears to cultivators at breakthroughs; cultivates by walking auspicious paths",
     "habitat":"Auspicious spirit veins, dawn-aspected groves",
     "cultivation":"Nascent Soul -> Spirit Severing (auspicious attribute)",
     "intelligence":10,"diet":"dawn-dew, auspicious herbs, spirit-rice",
     "relationships":"Auspicious to all cultivators",
     "techniques":["auspicious_step","seven_color_display","crystal_antler"],
     "treasures":["deer_core","seven_color_fur","crystal_antler"],
     "bloodline":"Seven-Color-Deer lineage; auspicious bloodline",
     "mutations":["true_auspicious_body"],
     "canon_confidence":3},

    # ------------------ A World Worth Protecting (三寸人间) ------------------
    {"id":"void_beast","name":"Void Beast","nameCn":"虚空兽","novel":"A World Worth Protecting",
     "appearance":"Formless beast of void 8 m, body is a tear in reality",
     "size":"8 m adult, 0 jin (void); true form spans void",
     "behavior":"Hunts by stepping through void; cultivates by absorbing void-essence",
     "habitat":"Void edges, void-aspected forbidden zones",
     "cultivation":"Ascendant -> Immortal (void attribute)",
     "intelligence":9,"diet":"void-essence, void-aspected herbs, lost travelers",
     "relationships":"Enemy of all who enter the void",
     "techniques":["void_step","void_maw","reality_tear"],
     "treasures":["void_core","void_essence"],
     "bloodline":"Void lineage; void-attribute bloodline",
     "mutations":["true_void_body"],
     "canon_confidence":3},

    {"id":"star_eating_beast","name":"Star-Eating Beast","nameCn":"噬星兽","novel":"A World Worth Protecting",
     "appearance":"Leviathan maw 1000 m, can swallow stars, body is cosmic hunger",
     "size":"1000 m adult, planetary mass; true form swallows star systems",
     "behavior":"Cultivates by devouring stars; sleeps between meals for millennia",
     "habitat":"Star-system voids, star-aspected forbidden zones",
     "cultivation":"Immortal (star/devour attribute)",
     "intelligence":8,"diet":"stars, star-essence, cosmic qi",
     "relationships":"Threat to all star-systems",
     "techniques":["star_swallow","cosmic_maw","stellar_devour"],
     "treasures":["star_eating_core","stellar_essence"],
     "bloodline":"Star-Eating lineage; cosmic bloodline",
     "mutations":["true_cosmic_body"],
     "canon_confidence":3},

    {"id":"cosmic_whale","name":"Cosmic Whale","nameCn":"宇宙鲸","novel":"A World Worth Protecting",
     "appearance":"Leviathan whale 10 km, body swims through cosmic void, skin is starlight",
     "size":"10 km adult, planetary mass; ancient spans star systems",
     "behavior":"Migrates through cosmic void; cultivates by absorbing cosmic qi; entire civilizations form on its back",
     "habitat":"Cosmic void, between star systems",
     "cultivation":"Immortal (cosmic attribute)",
     "intelligence":11,"diet":"cosmic qi, star-essence, cosmic plankton",
     "relationships":"Host to cosmic civilizations; kin of cloud whale (greater form)",
     "techniques":["cosmic_swim","starlight_breath","void_dive"],
     "treasures":["cosmic_whale_core","starlight_essence"],
     "bloodline":"Cosmic-Whale lineage; cosmic bloodline (greater cloud whale)",
     "mutations":["true_cosmic_body","cosmic_immortal_emperor"],
     "canon_confidence":3},

    {"id":"light_beast","name":"Light Beast","nameCn":"光兽","novel":"A World Worth Protecting",
     "appearance":"Formless beast of pure light 3 m, body is radiance incarnate",
     "size":"3 m adult, 0 jin (light); true form is a sun",
     "behavior":"Cultivates by absorbing light; cannot exist in darkness",
     "habitat":"Sun-aspected forbidden zones, light-aspected peaks",
     "cultivation":"Spirit Severing -> Ascendant (light attribute)",
     "intelligence":8,"diet":"light, light-aspected herbs",
     "relationships":"Enemy of all darkness-attribute beasts",
     "techniques":["light_step","radiance_burst","light_burn"],
     "treasures":["light_core","radiance_essence"],
     "bloodline":"Light lineage; light-attribute bloodline",
     "mutations":["true_light_body"],
     "canon_confidence":3},

    # ------------------ Beyond the Timescape (光阴之外) ------------------
    {"id":"timescape_beast","name":"Timescape Beast","nameCn":"光阴兽","novel":"Beyond the Timescape",
     "appearance":"Beast of living time 5 m, body shifts through moments, eyes are clocks",
     "size":"5 m adult, 0 jin (time); true form spans eras",
     "behavior":"Cultivates by absorbing time; hunts by aging prey in moments",
     "habitat":"Time-aspected forbidden zones, timescape edges",
     "cultivation":"Immortal (time attribute)",
     "intelligence":12,"diet":"time-essence, time-aspected herbs",
     "relationships":"Neutral; rarely interacts with cultivators",
     "techniques":["time_step","age_touch","moment_rewind"],
     "treasures":["timescape_core","time_essence"],
     "bloodline":"Timescape lineage; time-attribute bloodline",
     "mutations":["true_time_body"],
     "canon_confidence":3},

    {"id":"eternal_silence_beast","name":"Eternal Silence Beast","nameCn":"永寂兽","novel":"Beyond the Timescape",
     "appearance":"Beast of perfect silence 6 m, body absorbs all sound, presence is deafening quiet",
     "size":"6 m adult, 3000 jin; true form silences regions",
     "behavior":"Cultivates by absorbing sound; hunts by silencing prey",
     "habitat":"Silence-aspected forbidden zones, void edges",
     "cultivation":"Ascendant -> Immortal (silence attribute)",
     "intelligence":9,"diet":"sound, silence-aspected herbs",
     "relationships":"Enemy of all sound-attribute beasts",
     "techniques":["silence_step","sound_absorb","deafening_touch"],
     "treasures":["silence_core","silence_essence"],
     "bloodline":"Eternal-Silence lineage; silence-attribute bloodline",
     "mutations":["true_silence_body"],
     "canon_confidence":3},

    {"id":"void_fissure_beast","name":"Void Fissure Beast","nameCn":"虚裂兽","novel":"Beyond the Timescape",
     "appearance":"Beast that IS a void fissure, body is a moving tear in reality 10 m",
     "size":"10 m fissure, 0 jin (void)",
     "behavior":"Cultivates by widening reality tears; hunts by pulling prey into void",
     "habitat":"Void fissure zones, reality-edge regions",
     "cultivation":"Ascendant -> Immortal (void/fissure attribute)",
     "intelligence":8,"diet":"reality fragments, void-essence",
     "relationships":"Enemy of all reality-dwelling beasts",
     "techniques":["fissure_step","void_pull","reality_tear"],
     "treasures":["fissure_core","void_essence"],
     "bloodline":"Void-Fissure lineage; void-attribute bloodline",
     "mutations":["true_fissure_body"],
     "canon_confidence":3},

    {"id":"ancient_dream_beast","name":"Ancient Dream Beast","nameCn":"古梦兽","novel":"Beyond the Timescape",
     "appearance":"Beast of living dream 4 m, body shifts between dream and reality",
     "size":"4 m adult, 0 jin (dream); true form spans dreams",
     "behavior":"Cultivates by consuming dreams; hunts by pulling prey into dream",
     "habitat":"Dream-aspected forbidden zones, dream-realm edges",
     "cultivation":"Spirit Severing -> Ascendant (dream attribute)",
     "intelligence":10,"diet":"dreams, dream-aspected herbs",
     "relationships":"Neutral; rarely interacts with cultivators",
     "techniques":["dream_step","dream_pull","reality_shift"],
     "treasures":["dream_core","dream_essence"],
     "bloodline":"Ancient-Dream lineage; dream-attribute bloodline",
     "mutations":["true_dream_body"],
     "canon_confidence":3},

    {"id":"destiny_beast","name":"Destiny Beast","nameCn":"命兽","novel":"Beyond the Timescape",
     "appearance":"Beast of living destiny 5 m, body is woven fate-threads, eyes see all possible futures",
     "size":"5 m adult, 0 jin (fate); true form spans destinies",
     "behavior":"Cultivates by absorbing destiny; appears at fate-defining moments",
     "habitat":"Destiny-aspected forbidden zones, fate-nexus points",
     "cultivation":"Immortal (destiny attribute)",
     "intelligence":13,"diet":"destiny-essence, fate-threads",
     "relationships":"Neutral; observed by all cultivators at breakthroughs",
     "techniques":["destiny_step","fate_sever","future_see"],
     "treasures":["destiny_core","fate_thread"],
     "bloodline":"Destiny lineage; destiny-attribute bloodline (one of the rarest)",
     "mutations":["true_destiny_body"],
     "canon_confidence":3},
]

# Life-state variants (per user spec: build generators, not assets)
LIFE_STATES = [
    {"id":"juvenile","name":"Juvenile","desc":"Newly born, small, weak, learning; cultivates by observing",
     "size_mult":0.15,"power_mult":0.1,"intelligence_mult":0.5,"temperament":"curious","rarity":"common"},
    {"id":"adolescent","name":"Adolescent","desc":"Growing, half-size, testing strength; cultivates by play-fighting",
     "size_mult":0.45,"power_mult":0.35,"intelligence_mult":0.75,"temperament":"reckless","rarity":"uncommon"},
    {"id":"adult","name":"Adult","desc":"Full size, full power, territory-holder; cultivates by hunting",
     "size_mult":1.0,"power_mult":1.0,"intelligence_mult":1.0,"temperament":"territorial","rarity":"uncommon"},
    {"id":"ancient","name":"Ancient","desc":"Centuries old, massive, sage-like; cultivates by meditation",
     "size_mult":2.5,"power_mult":4.0,"intelligence_mult":1.3,"temperament":"contemplative","rarity":"rare"},
    {"id":"mutated","name":"Mutated","desc":"Bloodline-awakened, altered form; cultivates by absorbing the mutation source",
     "size_mult":1.5,"power_mult":3.0,"intelligence_mult":1.2,"temperament":"aggressive","rarity":"rare"},
    {"id":"injured","name":"Injured","desc":"Wounded, weakened, desperate; cultivates by seeking healing herbs",
     "size_mult":0.9,"power_mult":0.5,"intelligence_mult":1.1,"temperament":"desperate","rarity":"uncommon"},
    {"id":"starving","name":"Starving","desc":"Depleted, frenzied, will attack anything; cultivates by any feeding",
     "size_mult":0.85,"power_mult":0.7,"intelligence_mult":0.6,"temperament":"frenzied","rarity":"uncommon"},
    {"id":"tribulation_scarred","name":"Tribulation-Scarred","desc":"Survived heavenly tribulation, body marked by lightning; cultivates by absorbing residual tribulation",
     "size_mult":1.2,"power_mult":2.5,"intelligence_mult":1.4,"temperament":"sober","rarity":"rare"},
    {"id":"corrupted","name":"Corrupted","desc":"Corrupted by devil qi or nether, form twisted; cultivates by consuming corruption",
     "size_mult":1.3,"power_mult":2.0,"intelligence_mult":0.9,"temperament":"malevolent","rarity":"rare"},
    {"id":"enlightened","name":"Enlightened","desc":"Achieved Dao comprehension, body radiates law; cultivates by contemplation",
     "size_mult":1.1,"power_mult":5.0,"intelligence_mult":1.6,"temperament":"serene","rarity":"very_rare"},
]

CULTIVATION_TIERS = [
    "Qi Condensation","Foundation Establishment","Core Formation","Nascent Soul",
    "Spirit Severing","Ascendant","Immortal","Ancient Immortal","Heaven Trampling"
]

def pass_11_species_database(canon):
    """Pass 11: Comprehensive Species Database + life-state variants."""
    species_dir = out("species")
    variants_dir = out("species_variants")
    count_species = 0
    count_variants = 0

    for sp in SPECIES_CANON:
        # Canonical species definition (full 13 fields + provenance)
        sp_file = species_dir / f"{sp['id']}.json"
        # Only write if not already present (preserve existing canon-rich definitions)
        if not sp_file.exists():
            doc = {
                "_comment": f"Species: {sp['name']} ({sp['nameCn']}) from {sp['novel']}. "
                             f"Canonical definition with all 13 fields per the Er Gen simulation spec. "
                             f"Prime Directive: {CANON_PRIME}",
                "species_id": sp["id"],
                "name": sp["name"],
                "nameCn": sp["nameCn"],
                "novel": sp["novel"],
                "derivation_type": "A" if sp["canon_confidence"] >= 4 else "B",
                "canon_confidence": sp["canon_confidence"],
                "canonical": {
                    "appearance": sp["appearance"],
                    "size": sp["size"],
                    "behavior": sp["behavior"],
                    "habitat": sp["habitat"],
                    "cultivation": sp["cultivation"],
                    "intelligence": sp["intelligence"],
                    "diet": sp["diet"],
                    "relationships": sp["relationships"],
                    "techniques": sp["techniques"],
                    "treasures": sp["treasures"],
                    "bloodline": sp["bloodline"],
                    "mutations": sp["mutations"],
                    "ecology": "Inferred AFTER canonical fields; see ecosystem definitions."
                },
                "art_direction": {
                    "generator": "single canonical definition drives 10 life-state variants",
                    "variants": [v["id"] for v in LIFE_STATES],
                    "note": "Per art-direction directive: build generators, not assets."
                },
                "salt": salt_for(sp["id"]),
            }
            write_json(sp_file, doc)
            count_species += 1

        # Generate 10 life-state variants per species
        for v in LIFE_STATES:
            vid = f"{sp['id']}__{v['id']}"
            vfile = variants_dir / f"{vid}.json"
            if vfile.exists():
                continue
            vdoc = {
                "_comment": f"Life-state variant: {sp['name']} ({v['name']}). "
                             f"Generated from canonical species definition. "
                             f"Prime Directive: this state exists objectively; "
                             f"perception tier determines whether player recognizes it.",
                "variant_id": vid,
                "species_id": sp["id"],
                "species_name": sp["name"],
                "species_nameCn": sp["nameCn"],
                "novel": sp["novel"],
                "life_state": v["id"],
                "life_state_name": v["name"],
                "description": v["desc"],
                "modifiers": {
                    "size_multiplier": v["size_mult"],
                    "power_multiplier": v["power_mult"],
                    "intelligence_multiplier": v["intelligence_mult"],
                    "temperament": v["temperament"],
                    "rarity": v["rarity"],
                },
                "perception_tiers": {
                    "mortal": f"Sees a {v['temperament']} beast.",
                    "qi_condensation": f"Sees a {v['temperament']} beast of {sp['id'].replace('_',' ')} kind.",
                    "foundation_establishment": f"Recognizes life-state: {v['name']}.",
                    "core_formation": f"Recognizes species, life-state, and approximate cultivation.",
                    "nascent_soul": f"Sees bloodline, mutation potential, and treasure affinity.",
                    "spirit_severing": f"Sees complete ecology and Dao-comprehension state.",
                },
                "derivation_type": "B",
                "canon_confidence": sp["canon_confidence"],
                "salt": salt_for(vid),
            }
            write_json(vfile, vdoc)
            count_variants += 1

    return count_species, count_variants

# ==================================================================
# PASS 12 - ECOSYSTEM FOOD WEBS WITH SEASONALITY
# ==================================================================
# Each ecosystem has trophic layers, seasonal states, and environmental
# triggers. The seasonal states change population multipliers and behavior
# (winter larvae disappear, blood moon doubles population, nearby corpse
# battlefield explodes population, etc.).

ECOSYSTEMS = [
    {"id":"mosquito_valley","name":"Mosquito Valley","location":"Country of Zhao",
     "description":"The canonical Mosquito Valley where Wang Lin obtained his mosquito beast swarm. Millions of larvae, adult swarms, queens, egg chambers, blood pools.",
     "trophic_layers":[
        {"layer":"producer","members":["blood_pool_algae","spirit_tree_roots","corpse_marrow_flora"],"population":"environmental"},
        {"layer":"primary","members":["mosquito_beast#larva"],"population":10000000,"biomass_jin":2000000},
        {"layer":"secondary","members":["mosquito_beast#worker"],"population":1000000,"biomass_jin":4000000},
        {"layer":"tertiary","members":["mosquito_beast#soldier"],"population":100000,"biomass_jin":500000},
        {"layer":"quaternary","members":["mosquito_beast#elite"],"population":1000,"biomass_jin":200000},
        {"layer":"apex","members":["mosquito_beast#queen","mosquito_beast#ancient_queen"],"population":11,"biomass_jin":500000}
     ],
     "features":["blood_pools","egg_chambers","nest_tunnels","spirit_trees","corpse_middens","queen_chamber"],
     "seasonal_states":{
        "spring":{"larva_mult":1.2,"worker_mult":1.0,"soldier_mult":0.9,"queen_mult":1.0,"behavior":"breeding","note":"Egg chambers swell; queens lay millions."},
        "summer":{"larva_mult":1.5,"worker_mult":1.3,"soldier_mult":1.1,"queen_mult":1.0,"behavior":"peak_activity","note":"Swarms darken the sky; cultivation peaks."},
        "autumn":{"larva_mult":0.8,"worker_mult":1.0,"soldier_mult":1.2,"queen_mult":1.1,"behavior":"hoarding","note":"Workers hoard blood-essence; queens cultivate."},
        "winter":{"larva_mult":0.1,"worker_mult":0.3,"soldier_mult":0.7,"queen_mult":1.2,"behavior":"hibernation","note":"Larvae almost disappear; workers hibernate; queens cultivate in stillness."},
        "tribulation_storm":{"larva_mult":0.5,"worker_mult":0.8,"soldier_mult":1.0,"queen_mult":1.0,"behavior":"awakening","note":"Ancient mosquitoes awaken; mutations possible from lightning."},
        "blood_moon":{"larva_mult":2.0,"worker_mult":2.0,"soldier_mult":2.0,"queen_mult":1.5,"behavior":"frenzy","note":"Population doubles; all castes frenzied; mutations likely."},
        "nearby_corpse_battlefield":{"larva_mult":3.0,"worker_mult":3.0,"soldier_mult":3.0,"queen_mult":2.0,"behavior":"explosion","note":"Population explodes; corpse-marrow fuels rapid breeding."},
        "nearby_spirit_spring":{"larva_mult":1.0,"worker_mult":1.0,"soldier_mult":1.0,"queen_mult":1.0,"behavior":"mutation","note":"Population stable but mutations occur; gold-carapace chance +500%."},
        "nearby_dragon_corpse":{"larva_mult":1.5,"worker_mult":1.5,"soldier_mult":2.0,"queen_mult":2.0,"behavior":"bloodline_evolution","note":"Bloodline evolution chance; ancient-queen-tier mutation possible."},
        "nearby_ancient_god_remains":{"larva_mult":1.0,"worker_mult":1.0,"soldier_mult":1.0,"queen_mult":5.0,"behavior":"valley_transformation","note":"Entire valley changes; queens ascend to ancient-queen tier; ecosystem restructures."}
     },
     "canon_confidence":5},

    {"id":"thunder_peak_ecology","name":"Thunder Peak Ecology","location":"Ancient Thunder Realm",
     "description":"The thunder-attribute ecology of the Ancient Thunder Realm peaks where Wang Lin cultivated the Ji Realm.",
     "trophic_layers":[
        {"layer":"producer","members":["thunder_essence_herb","lightning_struck_tree","storm_crystal_flora"],"population":"environmental"},
        {"layer":"primary","members":["thunder_toad#juvenile","lei_ji#juvenile"],"population":50000,"biomass_jin":100000},
        {"layer":"secondary","members":["thunder_toad#adult","lei_ji#adolescent"],"population":5000,"biomass_jin":200000},
        {"layer":"tertiary","members":["lei_ji#adult","thunder_ape#adult"],"population":500,"biomass_jin":300000},
        {"layer":"quaternary","members":["lei_ji#ancient","thunder_ape#ancient"],"population":20,"biomass_jin":200000},
        {"layer":"apex","members":["thunder_celestial_beast#adult","thunder_celestial_beast#ancient"],"population":3,"biomass_jin":500000}
     ],
     "features":["thunder_essence_springs","lightning_struck_trees","storm_crystal_caves","tribulation_scarred_peaks","beast_nests"],
     "seasonal_states":{
        "calm":{"toad_mult":1.0,"lei_ji_mult":1.0,"celestial_mult":0.5,"behavior":"normal","note":"Standard ecology."},
        "storm":{"toad_mult":1.5,"lei_ji_mult":2.0,"celestial_mult":1.5,"behavior":"active","note":"All castes active; cultivation accelerates."},
        "tribulation":{"toad_mult":2.0,"lei_ji_mult":3.0,"celestial_mult":3.0,"behavior":"frenzy","note":"Tribulation calls celestial beasts; mutations peak."},
        "dry_season":{"toad_mult":0.3,"lei_ji_mult":0.7,"celestial_mult":0.2,"behavior":"buried","note":"Toads bury in mud; lei_ji go solitary; celestials retreat to storms."},
        "blood_moon":{"toad_mult":1.5,"lei_ji_mult":1.5,"celestial_mult":2.0,"behavior":"aggressive","note":"All castes aggressive; thunder-aspected mutations likely."}
     },
     "canon_confidence":5},

    {"id":"sea_of_devils_ecology","name":"Sea of Devils Ecology","location":"Sea of Devils",
     "description":"The devil-aspected ecology of the Sea of Devils, home to nether beasts, soul beasts, and six-desire devils.",
     "trophic_layers":[
        {"layer":"producer","members":["devil_qi_flora","soul_fragment_moss","nether_lotus"],"population":"environmental"},
        {"layer":"primary","members":["soul_beast#juvenile","ghost_spirit_beast#juvenile"],"population":100000,"biomass_jin":50000},
        {"layer":"secondary","members":["soul_beast#adult","nether_beast#juvenile"],"population":10000,"biomass_jin":100000},
        {"layer":"tertiary","members":["nether_beast#adult","ghost_face_beast#adult"],"population":1000,"biomass_jin":300000},
        {"layer":"quaternary","members":["six_desire_devil#adult","nether_beast#ancient"],"population":50,"biomass_jin":200000},
        {"layer":"apex","members":["six_desire_devil#ancient","six_desire_devil#enlightened"],"population":3,"biomass_jin":500000}
     ],
     "features":["devil_qi_pools","soul_fragment_mists","nether_caves","desire_mirages","corpse_battlefields"],
     "seasonal_states":{
        "normal":{"soul_mult":1.0,"nether_mult":1.0,"desire_mult":1.0,"behavior":"hunting","note":"Standard devil ecology."},
        "blood_moon":{"soul_mult":2.0,"nether_mult":2.0,"desire_mult":3.0,"behavior":"frenzy","note":"All devils frenzied; desire-mirages intensify."},
        "nearby_corpse_battlefield":{"soul_mult":3.0,"nether_mult":2.5,"desire_mult":1.5,"behavior":"explosion","note":"Soul fragments fuel population explosion."},
        "nearby_spirit_vein_collapse":{"soul_mult":1.5,"nether_mult":1.5,"desire_mult":1.0,"behavior":"migration","note":"Devils migrate to the collapse; vein-essence feeds them."},
        "nearby_sealed_demon":{"soul_mult":1.0,"nether_mult":2.0,"desire_mult":1.0,"behavior":"guardian","note":"Nether beasts gather as guardians of the seal."}
     },
     "canon_confidence":4},

    {"id":"spirit_vein_ecology","name":"Spirit Vein Ecology","location":"universal",
     "description":"The ecology that forms around spirit veins — herb-grazers, vein-guardian beasts, and cultivator competition.",
     "trophic_layers":[
        {"layer":"producer","members":["spirit_vein_herbs","vein_crystal_flora","qi_gathering_grass"],"population":"environmental"},
        {"layer":"primary","members":["spirit_rat#adult","wind_wolf#juvenile"],"population":20000,"biomass_jin":40000},
        {"layer":"secondary","members":["wind_wolf#adult","mountain_lion#juvenile"],"population":2000,"biomass_jin":100000},
        {"layer":"tertiary","members":["mountain_lion#adult","giant_python#adult"],"population":200,"biomass_jin":200000},
        {"layer":"quaternary","members":["giant_savage_ape#adult","giant_python#ancient"],"population":10,"biomass_jin":200000},
        {"layer":"apex","members":["vein_guardian_beast","giant_savage_ape#ancient"],"population":1,"biomass_jin":500000}
     ],
     "features":["spirit_vein_crystals","herb_groves","vein_springs","guardian_caves","cultivator_camps"],
     "seasonal_states":{
        "vein_active":{"herb_mult":1.5,"grazer_mult":1.3,"guardian_mult":1.2,"behavior":"abundance","note":"Vein active; herbs bloom; grazers multiply."},
        "vein_dormant":{"herb_mult":0.5,"grazer_mult":0.7,"guardian_mult":1.0,"behavior":"scarcity","note":"Vein dormant; herbs wither; grazers scatter."},
        "vein_collapsing":{"herb_mult":2.0,"grazer_mult":0.3,"guardian_mult":3.0,"behavior":"crisis","note":"Vein collapsing; herbs bloom one last time; guardian frenzied."},
        "cultivator_harvest":{"herb_mult":0.3,"grazer_mult":0.8,"guardian_mult":1.5,"behavior":"disturbed","note":"Cultivators harvesting; ecology disturbed."},
        "tribulation_storm":{"herb_mult":1.2,"grazer_mult":0.5,"guardian_mult":1.0,"behavior":"sheltering","note":"Beasts shelter from tribulation; herbs absorb lightning."}
     },
     "canon_confidence":4},

    {"id":"sky_realm_ecology","name":"Sky Realm Ecology","location":"Sky Realm",
     "description":"The sky-attribute ecology of the Sky Realm — cloud whales, spirit cranes, and their sky-civilization followers.",
     "trophic_layers":[
        {"layer":"producer","members":["cloud_essence_plankton","sky_spirit_herb","aerial_qi_crystals"],"population":"environmental"},
        {"layer":"primary","members":["spirit_crane#juvenile","wind_wolf#juvenile"],"population":50000,"biomass_jin":100000},
        {"layer":"secondary","members":["spirit_crane#adult","southern_winged_snake#adult"],"population":5000,"biomass_jin":200000},
        {"layer":"tertiary","members":["southern_winged_snake#ancient","thunder_bird#adult"],"population":500,"biomass_jin":300000},
        {"layer":"quaternary","members":["cloud_whale#juvenile","thunder_bird#ancient"],"population":20,"biomass_jin":2000000},
        {"layer":"apex","members":["cloud_whale#adult","cloud_whale#ancient"],"population":7,"biomass_jin":14000000}
     ],
     "features":["cloud_seas","floating_islands","sky_civilizations","migration_routes","thunder_nests"],
     "seasonal_states":{
        "calm_skies":{"crane_mult":1.0,"whale_mult":1.0,"civilization_mult":1.0,"behavior":"normal","note":"Standard sky ecology."},
        "migration_season":{"crane_mult":2.0,"whale_mult":3.0,"civilization_mult":2.0,"behavior":"migration","note":"Cloud whales migrate; sky-civilizations follow; cranes form up."},
        "storm_season":{"crane_mult":0.5,"whale_mult":0.7,"civilization_mult":0.5,"behavior":"sheltering","note":"Beasts shelter; civilizations dock at floating islands."},
        "thunder_tribulation":{"crane_mult":0.3,"whale_mult":0.5,"thunder_bird_mult":3.0,"behavior":"frenzy","note":"Thunder birds called by tribulation; whales dive deep."},
        "void_fissure":{"crane_mult":0.1,"whale_mult":0.3,"void_beast_mult":5.0,"behavior":"crisis","note":"Void fissure opens; void beasts hunt; sky ecology collapses locally."}
     },
     "canon_confidence":4},

    {"id":"foreign_battleground_ecology","name":"Foreign Battleground Ecology","location":"Foreign Battleground",
     "description":"The war-aspected ecology of the Foreign Battleground — slaughter beasts, blood beasts, and war-scarred survivors.",
     "trophic_layers":[
        {"layer":"producer","members":["slaughter_qi_flora","blood_lotus","war_iron_grass"],"population":"environmental"},
        {"layer":"primary","members":["blood_beast#juvenile","wind_wolf#starving"],"population":100000,"biomass_jin":200000},
        {"layer":"secondary","members":["blood_beast#adult","white_tiger#juvenile"],"population":10000,"biomass_jin":500000},
        {"layer":"tertiary","members":["white_tiger#adult","berserk_beast#adult"],"population":100,"biomass_jin":500000},
        {"layer":"quaternary","members":["berserk_beast#ancient","white_tiger#ancient"],"population":5,"biomass_jin":1000000},
        {"layer":"apex","members":["white_tiger#enlightened","blood_ancestor_beast#juvenile"],"population":1,"biomass_jin":5000000}
     ],
     "features":["battlefield_corpses","blood_seas","slaughter_qi_pools","war_memorials","shattered_formations"],
     "seasonal_states":{
        "calm":{"blood_mult":1.0,"slaughter_mult":1.0,"behavior":"hunting","note":"Standard battleground ecology."},
        "war":{"blood_mult":3.0,"slaughter_mult":2.0,"behavior":"frenzy","note":"Active war; corpses fuel population explosion."},
        "blood_moon":{"blood_mult":5.0,"slaughter_mult":3.0,"behavior":"apex","note":"Blood moon peaks all populations; blood ancestor manifestations possible."},
        "truce":{"blood_mult":0.5,"slaughter_mult":0.7,"behavior":"scavenging","note":"Truce; scavengers clean battlefields."},
        "ancient_battlefield_awakening":{"blood_mult":2.0,"slaughter_mult":2.0,"white_tiger_mult":3.0,"behavior":"awakening","note":"Ancient battleground awakens; white tiger manifestations; slaughter qi surges."}
     },
     "canon_confidence":4},

    {"id":"underground_crystal_ecology","name":"Underground Crystal Ecology","location":"underground",
     "description":"The crystal-aspected ecology of underground crystal forests.",
     "trophic_layers":[
        {"layer":"producer","members":["crystal_flora","amethyst_moss","deep_mineral_herbs"],"population":"environmental"},
        {"layer":"primary","members":["ant_beast#worker","ink_bat#juvenile"],"population":1000000,"biomass_jin":2000000},
        {"layer":"secondary","members":["ant_beast#soldier","giant_centipede#juvenile"],"population":100000,"biomass_jin":1000000},
        {"layer":"tertiary","members":["giant_centipede#adult","web_weaver_spider#adult"],"population":10000,"biomass_jin":500000},
        {"layer":"quaternary","members":["ant_beast#queen","giant_centipede#ancient"],"population":100,"biomass_jin":1000000},
        {"layer":"apex","members":["ink_dragon#juvenile","ink_dragon#adult"],"population":3,"biomass_jin":300000}
     ],
     "features":["crystal_forests","amethyst_geodes","ant_kingdoms","centipede_tunnels","deep_springs"],
     "seasonal_states":{
        "stable":{"ant_mult":1.0,"centipede_mult":1.0,"ink_dragon_mult":1.0,"behavior":"normal","note":"Standard underground ecology."},
        "vein_shift":{"ant_mult":1.5,"centipede_mult":1.5,"ink_dragon_mult":0.5,"behavior":"migration","note":"Vein shifts; beasts migrate; dragons retreat."},
        "cave_collapse":{"ant_mult":0.3,"centipede_mult":2.0,"ink_dragon_mult":0.2,"behavior":"frenzy","note":"Collapse; centipedes tunnel frenzied; others crushed."},
        "miner_invasion":{"ant_mult":1.2,"centipede_mult":1.0,"ink_dragon_mult":1.0,"behavior":"defensive","note":"Miners invade; ant kingdoms defend."}
     },
     "canon_confidence":3},

    {"id":"immortal_graveyard_ecology","name":"Immortal Graveyard Ecology","location":"Immortal Graveyard",
     "description":"The death-aspected ecology of the Immortal Graveyard where fallen immortals lie.",
     "trophic_layers":[
        {"layer":"producer","members":["death_qi_flora","soul_lotus","corpse_marsh_herbs"],"population":"environmental"},
        {"layer":"primary","members":["soul_beast#juvenile","ghost_spirit_beast#juvenile"],"population":50000,"biomass_jin":50000},
        {"layer":"secondary","members":["soul_beast#adult","nether_beast#juvenile"],"population":5000,"biomass_jin":200000},
        {"layer":"tertiary","members":["nether_beast#adult","soul_lasher_beast#adult"],"population":500,"biomass_jin":400000},
        {"layer":"quaternary","members":["nether_beast#ancient","soul_lasher_beast#ancient"],"population":20,"biomass_jin":500000},
        {"layer":"apex","members":["immortal_ghost","ancient_nether_emperor"],"population":2,"biomass_jin":2000000}
     ],
     "features":["immortal_corpses","soul_mists","death_qi_pools","ancient_tombs","shattered_immortal_treasures"],
     "seasonal_states":{
        "still":{"soul_mult":1.0,"nether_mult":1.0,"behavior":"stillness","note":"Deathly stillness; beasts dormant."},
        "blood_moon":{"soul_mult":3.0,"nether_mult":3.0,"behavior":"frenzy","note":"Blood moon awakens all; immortal ghosts manifest."},
        "graveyard_quake":{"soul_mult":2.0,"nether_mult":1.5,"immortal_ghost_mult":5.0,"behavior":"awakening","note":"Quake disturbs corpses; immortal ghosts awaken."},
        "cultivator_desecration":{"soul_mult":1.5,"nether_mult":3.0,"behavior":"defensive","note":"Cultivators desecrate tombs; nether beasts swarm."}
     },
     "canon_confidence":4},

    {"id":"pill_sea_ecology","name":"Pill Sea Ecology","location":"Pill Sea",
     "description":"The pill-aspected ecology of the Pill Sea where alchemical residue fuels unique life.",
     "trophic_layers":[
        {"layer":"producer","members":["pill_residue_flora","alchemy_herbs","spirit_lotus"],"population":"environmental"},
        {"layer":"primary","members":["spirit_rat#juvenile","spirit_toad#juvenile"],"population":30000,"biomass_jin":60000},
        {"layer":"secondary","members":["spirit_toad#adult","flame_dragon#juvenile"],"population":3000,"biomass_jin":300000},
        {"layer":"tertiary","members":["flame_dragon#adult","spirit_toad#ancient"],"population":30,"biomass_jin":1500000},
        {"layer":"quaternary","members":["flame_dragon#ancient"],"population":3,"biomass_jin":150000},
        {"layer":"apex","members":["flame_dragon#enlightened","pill_demon"],"population":1,"biomass_jin":1000000}
     ],
     "features":["pill_residue_pools","alchemy_vents","spirit_lotus_fields","flame_dragon_nests","pill_demon_caves"],
     "seasonal_states":{
        "calm":{"toad_mult":1.0,"dragon_mult":1.0,"behavior":"normal","note":"Standard pill-sea ecology."},
        "alchemical_storm":{"toad_mult":2.0,"dragon_mult":2.0,"pill_demon_mult":3.0,"behavior":"mutation","note":"Alchemical storm; mutations peak; pill demons manifest."},
        "furnace_eruption":{"toad_mult":3.0,"dragon_mult":3.0,"behavior":"frenzy","note":"Furnace eruption; alchemical qi surges; all beasts frenzy."}
     },
     "canon_confidence":3},

    {"id":"vermilion_bird_realm_ecology","name":"Vermilion Bird Realm Ecology","location":"Vermilion Bird Starfield",
     "description":"The fire-divine ecology of the Vermilion Bird Realm.",
     "trophic_layers":[
        {"layer":"producer","members":["divine_fire_herb","phoenix_tree","sun_essence_flora"],"population":"environmental"},
        {"layer":"primary","members":["fire_phoenix#juvenile","flame_dragon#juvenile"],"population":1000,"biomass_jin":500000},
        {"layer":"secondary","members":["fire_phoenix#adult","flame_dragon#adult"],"population":100,"biomass_jin":5000000},
        {"layer":"tertiary","members":["fire_phoenix#ancient","flame_dragon#ancient"],"population":10,"biomass_jin":50000000},
        {"layer":"quaternary","members":["flame_dragon#enlightened","fire_phoenix#enlightened"],"population":2,"biomass_jin":100000000},
        {"layer":"apex","members":["vermilion_bird"],"population":1,"biomass_jin":1000000000}
     ],
     "features":["divine_fire_pools","phoenix_trees","sun_essence_springs","vermilion_nest","starfire_rain"],
     "seasonal_states":{
        "divine_slumber":{"phoenix_mult":0.5,"dragon_mult":0.5,"vermilion_mult":0.0,"behavior":"slumber","note":"Vermilion bird slumbers; ecology subdued."},
        "divine_awakening":{"phoenix_mult":2.0,"dragon_mult":2.0,"vermilion_mult":1.0,"behavior":"awakening","note":"Vermilion bird awakens; all fire-beasts active."},
        "imperial_selection":{"phoenix_mult":3.0,"dragon_mult":3.0,"vermilion_mult":1.0,"behavior":"convergence","note":"Vermilion bird selects new Divine Emperor; all fire-beasts converge."},
        "tribulation":{"phoenix_mult":1.5,"dragon_mult":1.5,"vermilion_mult":1.0,"behavior":"ascension","note":"Vermilion bird faces tribulation; ecology supports."}
     },
     "canon_confidence":4},

    {"id":"ancient_god_remains_ecology","name":"Ancient God Remains Ecology","location":"Land of the Ancient God",
     "description":"The unique ecology that forms around Ancient God remains — everything is altered by god-essence.",
     "trophic_layers":[
        {"layer":"producer","members":["god_bone_flora","god_blood_lotus","ancient_spirit_herbs"],"population":"environmental"},
        {"layer":"primary","members":["spirit_rat#mutated","mosquito_beast#mutated"],"population":10000,"biomass_jin":500000},
        {"layer":"secondary","members":["giant_savage_ape#mutated","giant_python#mutated"],"population":1000,"biomass_jin":5000000},
        {"layer":"tertiary","members":["giant_savage_ape#ancient","giant_python#ancient"],"population":50,"biomass_jin":50000000},
        {"layer":"quaternary","members":["blood_ancestor_beast#juvenile","nether_beast#enlightened"],"population":5,"biomass_jin":50000000},
        {"layer":"apex","members":["ancient_god_guardian_beast"],"population":1,"biomass_jin":1000000000}
     ],
     "features":["god_bone_mountains","god_blood_lakes","god_eye_pools","ancient_spirit_veins","god_guardian_chambers"],
     "seasonal_states":{
        "slumber":{"all_mult":0.5,"behavior":"slumber","note":"Ancient god slumbers; ecology subdued but still mutated."},
        "stirring":{"all_mult":1.5,"behavior":"stirring","note":"Ancient god stirs; all beasts grow restless."},
        "awakening":{"all_mult":3.0,"behavior":"frenzy","note":"Ancient god awakens; entire ecology restructures; guardian beasts manifest."},
        "god_tribulation":{"all_mult":5.0,"behavior":"apex","note":"God tribulation; apex beasts manifest; reality warps."}
     },
     "canon_confidence":4},

    {"id":"snow_domain_ecology","name":"Snow Domain Ecology","location":"Snow Domain Country",
     "description":"The ice-aspected ecology of the Snow Domain Country.",
     "trophic_layers":[
        {"layer":"producer","members":["ice_lotus","frost_herb","glacial_moss"],"population":"environmental"},
        {"layer":"primary","members":["spirit_rat#corrupted","wind_wolf#starving"],"population":8000,"biomass_jin":160000},
        {"layer":"secondary","members":["wind_wolf#adult","mountain_lion#starving"],"population":800,"biomass_jin":400000},
        {"layer":"tertiary","members":["mountain_lion#adult","ice_phoenix#juvenile"],"population":50,"biomass_jin":300000},
        {"layer":"quaternary","members":["ice_phoenix#adult","cold_dragon#juvenile"],"population":5,"biomass_jin":5000000},
        {"layer":"apex","members":["ice_phoenix#ancient","cold_dragon#adult"],"population":2,"biomass_jin":50000000}
     ],
     "features":["glacial_peaks","ice_caves","frost_springs","phoenix_roosts","dragon_lairs"],
     "seasonal_states":{
        "winter":{"wolf_mult":1.5,"phoenix_mult":1.2,"dragon_mult":1.5,"behavior":"peak","note":"Peak ice ecology."},
        "spring":{"wolf_mult":0.7,"phoenix_mult":0.8,"dragon_mult":0.5,"behavior":"retreat","note":"Beasts retreat to glaciers."},
        "blizzard":{"wolf_mult":2.0,"phoenix_mult":1.5,"dragon_mult":2.0,"behavior":"frenzy","note":"Blizzard; ice beasts empowered."},
        "ice_tribulation":{"phoenix_mult":3.0,"dragon_mult":3.0,"behavior":"ascension","note":"Ice tribulation; phoenix/dragon ascend."}
     },
     "canon_confidence":4},

    {"id":"karma_wastes_ecology","name":"Karma Wastes Ecology","location":"Immortal Astral Continent",
     "description":"The karma-aspected ecology of the Karma Wastes where crystallized karma alters all life.",
     "trophic_layers":[
        {"layer":"producer","members":["karma_crystal_flora","karmic_lotus","sin_herb"],"population":"environmental"},
        {"layer":"primary","members":["spirit_rat#corrupted","sand_scorpion#corrupted"],"population":5000,"biomass_jin":100000},
        {"layer":"secondary","members":["sand_scorpion#mutated","ghost_face_beast#corrupted"],"population":500,"biomass_jin":250000},
        {"layer":"tertiary","members":["ghost_face_beast#adult","nether_beast#corrupted"],"population":50,"biomass_jin":300000},
        {"layer":"quaternary","members":["nether_beast#ancient","destiny_beast#juvenile"],"population":3,"biomass_jin":1000000},
        {"layer":"apex","members":["destiny_beast#adult","karma_demon"],"population":1,"biomass_jin":5000000}
     ],
     "features":["karma_crystals","sin_pools","karmic_mirages","destiny_nexus","karma_demon_lairs"],
     "seasonal_states":{
        "still":{"all_mult":0.8,"behavior":"stillness","note":"Karmic stillness; beasts subdued."},
        "karmic_storm":{"all_mult":2.0,"destiny_mult":3.0,"behavior":"awakening","note":"Karmic storm; destiny beasts manifest."},
        "sin_purge":{"all_mult":0.5,"karma_demon_mult":5.0,"behavior":"apex","note":"Sin purge; karma demons manifest."}
     },
     "canon_confidence":3},

    {"id":"reincarnation_realm_ecology","name":"Reincarnation Realm Ecology","location":"Reincarnation Secret Realm",
     "description":"The reincarnation-aspected ecology where beings cycle through lives.",
     "trophic_layers":[
        {"layer":"producer","members":["reincarnation_lotus","past_life_herb","cycle_flora"],"population":"environmental"},
        {"layer":"primary","members":["spirit_rat#enlightened","seven_colored_deer#juvenile"],"population":500,"biomass_jin":100000},
        {"layer":"secondary","members":["seven_colored_deer#adult","ancient_dream_beast#juvenile"],"population":50,"biomass_jin":500000},
        {"layer":"tertiary","members":["ancient_dream_beast#adult","destiny_beast#juvenile"],"population":5,"biomass_jin":2000000},
        {"layer":"quaternary","members":["destiny_beast#adult","timescape_beast#juvenile"],"population":2,"biomass_jin":10000000},
        {"layer":"apex","members":["timescape_beast#adult","reincarnation_spirit"],"population":1,"biomass_jin":100000000}
     ],
     "features":["reincarnation_pools","past_life_mirrors","cycle_altars","destiny_threads","time_crystals"],
     "seasonal_states":{
        "cycle_active":{"all_mult":1.5,"behavior":"cycling","note":"Reincarnation cycle active; beasts cycle through lives."},
        "cycle_pause":{"all_mult":0.3,"behavior":"pause","note":"Cycle paused; beasts dormant."},
        "destiny_convergence":{"destiny_mult":5.0,"timescape_mult":3.0,"behavior":"convergence","note":"Destiny convergence; time/destiny beasts manifest."}
     },
     "canon_confidence":3},

    {"id":"sword_intent_realm_ecology","name":"Sword Intent Realm Ecology","location":"Da Lou Sword Sect Region",
     "description":"The sword-aspected ecology where residual sword intent alters all life.",
     "trophic_layers":[
        {"layer":"producer","members":["sword_grass","intent_herb","blade_moss"],"population":"environmental"},
        {"layer":"primary","members":["spirit_crane#juvenile","wind_wolf#juvenile"],"population":20000,"biomass_jin":60000},
        {"layer":"secondary","members":["spirit_crane#adult","wind_wolf#adult"],"population":2000,"biomass_jin":400000},
        {"layer":"tertiary","members":["spirit_crane#ancient","dragon_ape#juvenile"],"population":100,"biomass_jin":1000000},
        {"layer":"quaternary","members":["dragon_ape#adult","spirit_crane#enlightened"],"population":10,"biomass_jin":5000000},
        {"layer":"apex","members":["sword_spirit_beast","dragon_ape#enlightened"],"population":1,"biomass_jin":50000000}
     ],
     "features":["sword_peaks","intent_lakes","blade_forests","crane_roosts","sword_spring"],
     "seasonal_states":{
        "calm":{"crane_mult":1.0,"dragon_ape_mult":1.0,"behavior":"cultivation","note":"Standard sword ecology."},
        "sword_rain":{"crane_mult":2.0,"dragon_ape_mult":1.5,"sword_spirit_mult":3.0,"behavior":"manifestation","note":"Sword rain; sword spirits manifest."},
        "intent_storm":{"crane_mult":3.0,"dragon_ape_mult":2.0,"behavior":"breakthrough","note":"Intent storm; beasts break through."}
     },
     "canon_confidence":3},
]

def pass_12_ecosystems(canon):
    """Pass 12: Ecosystem Food Webs with Seasonality."""
    eco_dir = out("ecosystems")
    count = 0
    for eco in ECOSYSTEMS:
        f = eco_dir / f"{eco['id']}.json"
        if f.exists():
            continue
        doc = {
            "_comment": f"Ecosystem: {eco['name']} ({eco['location']}). "
                         f"Full food web with {len(eco['trophic_layers'])} trophic layers and "
                         f"{len(eco['seasonal_states'])} seasonal/environmental states. "
                         f"Prime Directive: ecology exists objectively; "
                         f"player perception determines what they observe. "
                         f"No-Locked-Upgrades: all seasonal states are reachable by time/weather/event.",
            "ecosystem_id": eco["id"],
            "name": eco["name"],
            "location": eco["location"],
            "description": eco["description"],
            "derivation_type": "A" if eco["canon_confidence"] >= 4 else "B",
            "canon_confidence": eco["canon_confidence"],
            "trophic_layers": eco["trophic_layers"],
            "features": eco["features"],
            "seasonal_states": eco["seasonal_states"],
            "state_transitions": {
                "spring->summer": "temperature_rise",
                "summer->autumn": "temperature_fall",
                "autumn->winter": "temperature_fall",
                "winter->spring": "temperature_rise",
                "any->tribulation_storm": "tribulation_event",
                "any->blood_moon": "blood_moon_event",
                "any->nearby_corpse_battlefield": "battlefield_proximity",
                "any->nearby_spirit_spring": "spring_proximity",
                "any->nearby_dragon_corpse": "dragon_corpse_proximity",
                "any->nearby_ancient_god_remains": "ancient_god_proximity",
            },
            "perception_tiers": {
                "mortal": "Sees beasts and terrain; does not perceive trophic structure.",
                "qi_condensation": "Senses qi-flows of the ecosystem.",
                "foundation_establishment": "Perceives trophic layers and seasonal shifts.",
                "core_formation": "Perceives population dynamics and triggers.",
                "nascent_soul": "Perceives mutation potential and bloodline flows.",
                "spirit_severing": "Perceives complete ecology as a living system.",
            },
            "salt": salt_for(eco["id"]),
        }
        write_json(f, doc)
        count += 1
    return count

# ==================================================================
# PASS 13 - MIGRATION ROUTES
# ==================================================================
# Migration as a major simulation system. Each route has waypoints,
# seasonal timing, and a follower chain (cultivators follow cranes
# follow predators follow scavengers follow bandits -> markets appear).

MIGRATION_ROUTES = [
    {"id":"cloud_whale_great_circuit","name":"Cloud Whale Great Circuit","species":"cloud_whale",
     "description":"The annual migration of cloud whale pods across the Sky Realm. Entire sky-civilizations adapt around this route.",
     "waypoints":[
        {"point":"eastern_cloud_sea","season":"spring","duration_days":45},
        {"point":"floating_island_archipelago","season":"late_spring","duration_days":30},
        {"point":"celestial_observation_peak","season":"summer","duration_days":60},
        {"point":"western_sky_realm","season":"late_summer","duration_days":45},
        {"point":"southern_storm_zone","season":"autumn","duration_days":40},
        {"point":"northern_sky_realm","season":"winter","duration_days":75}
     ],
     "follower_chain":[
        {"follower":"spirit_crane","relationship":"symbiotic","distance_li":10,"note":"Cranes follow whales for cloud-essence scraps."},
        {"follower":"sword_cultivators","relationship":"symbiotic","distance_li":20,"note":"Sword cultivators follow cranes for sword-intent cultivation."},
        {"follower":"thunder_birds","relationship":"predator","distance_li":50,"note":"Thunder birds prey on straggler cranes."},
        {"follower":"wind_wolves","relationship":"scavenger","distance_li":100,"note":"Wind wolves scavenge crane falls."},
        {"follower":"bandit_cults","relationship":"parasite","distance_li":200,"note":"Bandit cults rob cultivators."},
        {"follower":"sky_merchants","relationship":"opportunist","distance_li":150,"note":"Markets appear seasonally along the route."}
     ],
     "emergent_markets":[
        {"market":"eastern_cloud_market","season":"spring","goods":["cloud_essence","whale_pearl","crane_feather"]},
        {"market":"celestial_peak_market","season":"summer","goods":["sun_essence","sword_intent_herb","whale_core"]},
        {"market":"northern_sky_market","season":"winter","goods":["ice_lotus","storm_crystal","whale_bone"]}
     ],
     "canon_confidence":4},

    {"id":"spirit_crane_sword_flight","name":"Spirit Crane Sword Flight","species":"spirit_crane",
     "description":"The seasonal migration of spirit cranes between sword peaks. Sword cultivators follow this route for centuries.",
     "waypoints":[
        {"point":"da_lou_sword_peak","season":"spring","duration_days":20},
        {"point":"heng_yue_sword_peak","season":"late_spring","duration_days":25},
        {"point":"xuan_dao_sword_peak","season":"summer","duration_days":30},
        {"point":"vermilion_sword_peak","season":"autumn","duration_days":25},
        {"point":"snow_domain_sword_peak","season":"winter","duration_days":40}
     ],
     "follower_chain":[
        {"follower":"sword_cultivators","relationship":"symbiotic","distance_li":5,"note":"Sword cultivators follow cranes to cultivate sword-intent."},
        {"follower":"thunder_birds","relationship":"predator","distance_li":20,"note":"Thunder birds prey on cranes."},
        {"follower":"wind_wolves","relationship":"scavenger","distance_li":50,"note":"Wolves scavenge crane falls."},
        {"follower":"treasure_seeking_cults","relationship":"opportunist","distance_li":80,"note":"Treasure seekers follow for sword-intent herbs."},
        {"follower":"bandit_cults","relationship":"parasite","distance_li":120,"note":"Bandits rob cultivators."}
     ],
     "emergent_markets":[
        {"market":"sword_intent_spring_market","season":"spring","goods":["sword_grass","crane_feather","intent_herb"]},
        {"market":"snow_sword_market","season":"winter","goods":["ice_sword_crystal","crane_crown","winter_intent"]}
     ],
     "canon_confidence":4},

    {"id":"lei_ji_thunder_migration","name":"Lei Ji Thunder Migration","species":"lei_ji_thunder_beast",
     "description":"The storm-season migration of Lei Ji packs to thunder peaks for tribulation cultivation.",
     "waypoints":[
        {"point":"thunder_valley","season":"spring","duration_days":30},
        {"point":"ancient_thunder_realm","season":"summer","duration_days":90},
        {"point":"thunder_celestial_realm","season":"autumn","duration_days":45},
        {"point":"thunder_peak_sanctuary","season":"winter","duration_days":60}
     ],
     "follower_chain":[
        {"follower":"thunder_toads","relationship":"rival","distance_li":15,"note":"Toads follow storms too."},
        {"follower":"thunder_cultivators","relationship":"symbiotic","distance_li":30,"note":"Thunder cultivators follow for tribulation-essence."},
        {"follower":"thunder_celestial_beasts","relationship":"predator","distance_li":100,"note":"Celestials hunt during tribulations."},
        {"follower":"thunder_herb_collectors","relationship":"opportunist","distance_li":50,"note":"Collectors follow for thunder-herbs."},
        {"follower":"sky_merchants","relationship":"opportunist","distance_li":150,"note":"Markets appear at peaks."}
     ],
     "emergent_markets":[
        {"market":"thunder_essence_market","season":"summer","goods":["thunder_essence_herb","lei_ji_fur","thunder_crystal"]},
        {"market":"tribulation_market","season":"autumn","goods":["tribulation_fragment","thunder_toad_core","storm_sac"]}
     ],
     "canon_confidence":4},

    {"id":"flame_dragon_volcano_circuit","name":"Flame Dragon Volcano Circuit","species":"flame_dragon",
     "description":"The centennial migration of flame dragons between volcanic regions.",
     "waypoints":[
        {"point":"fire_burn_volcano","season":"year_1","duration_days":365},
        {"point":"pill_sea_vents","season":"year_2","duration_days":365},
        {"point":"vermilion_realm_volcano","season":"year_3","duration_days":365},
        {"point":"foreign_battleground_volcano","season":"year_4","duration_days":365}
     ],
     "follower_chain":[
        {"follower":"fire_phoenix","relationship":"rival","distance_li":200,"note":"Phoenixes follow for fire-essence."},
        {"follower":"fire_cultivators","relationship":"symbiotic","distance_li":500,"note":"Fire cultivators follow for dragon-essence."},
        {"follower":"dragon_hunters","relationship":"predator","distance_li":1000,"note":"Spirit Severing hunters track dragons."},
        {"follower":"treasure_seeking_cults","relationship":"opportunist","distance_li":800,"note":"Treasure seekers follow for fire-herbs."},
        {"follower":"sky_merchants","relationship":"opportunist","distance_li":1500,"note":"Markets appear at volcano stops."}
     ],
     "emergent_markets":[
        {"market":"fire_burn_dragon_market","season":"year_1","goods":["dragon_scale","fire_herb","dragon_blood"]},
        {"market":"pill_sea_dragon_market","season":"year_2","goods":["dragon_core","pill_essence","volcano_crystal"]}
     ],
     "canon_confidence":3},

    {"id":"ice_phoenix_glacial_circuit","name":"Ice Phoenix Glacial Circuit","species":"ice_phoenix",
     "description":"The centennial migration of ice phoenixes between glacial peaks.",
     "waypoints":[
        {"point":"snow_domain_peak","season":"winter","duration_days":90},
        {"point":"ice_realm_glacier","season":"spring","duration_days":60},
        {"point":"northern_ocean_ice","season":"summer","duration_days":90},
        {"point":"glacial_sanctuary","season":"autumn","duration_days":60}
     ],
     "follower_chain":[
        {"follower":"cold_dragon","relationship":"rival","distance_li":300,"note":"Cold dragons follow for ice-essence."},
        {"follower":"ice_cultivators","relationship":"symbiotic","distance_li":500,"note":"Ice cultivators follow for phoenix-essence."},
        {"follower":"phoenix_hunters","relationship":"predator","distance_li":1000,"note":"Spirit Severing hunters track phoenixes."},
        {"follower":"treasure_seeking_cults","relationship":"opportunist","distance_li":800,"note":"Treasure seekers follow for ice-herbs."}
     ],
     "emergent_markets":[
        {"market":"snow_domain_phoenix_market","season":"winter","goods":["phoenix_feather","ice_lotus","frost_essence"]}
     ],
     "canon_confidence":3},

    {"id":"spirit_beast_tidal_migration","name":"Spirit Beast Tidal Migration","species":"multiple",
     "description":"The annual tidal migration of spirit beasts across continents, triggered by spirit-vein shifts.",
     "waypoints":[
        {"point":"zhao_spirit_vein","season":"spring","duration_days":30},
        {"point":"chu_spirit_vein","season":"late_spring","duration_days":30},
        {"point":"fire_burn_spirit_vein","season":"summer","duration_days":45},
        {"point":"sky_demon_spirit_vein","season":"autumn","duration_days":40},
        {"point":"snow_domain_spirit_vein","season":"winter","duration_days":60}
     ],
     "follower_chain":[
        {"follower":"herbivore_beasts","relationship":"prey","distance_li":5,"note":"Herbivores migrate with vegetation."},
        {"follower":"predator_beasts","relationship":"predator","distance_li":20,"note":"Predators follow herbivores."},
        {"follower":"scavenger_beasts","relationship":"scavenger","distance_li":50,"note":"Scavengers follow predators."},
        {"follower":"cultivator_hunters","relationship":"predator","distance_li":100,"note":"Hunters follow beasts."},
        {"follower":"merchant_caravans","relationship":"opportunist","distance_li":150,"note":"Caravans follow hunters."},
        {"follower":"bandit_cults","relationship":"parasite","distance_li":200,"note":"Bandits rob caravans."},
        {"follower":"sect_elders","relationship":"overseer","distance_li":500,"note":"Sect elders monitor migration for threats."}
     ],
     "emergent_markets":[
        {"market":"zhao_migration_market","season":"spring","goods":["beast_core","herb","fur"]},
        {"market":"chu_migration_market","season":"late_spring","goods":["beast_core","herb","caravan_goods"]},
        {"market":"fire_burn_migration_market","season":"summer","goods":["beast_core","fire_herb","volcano_crystal"]}
     ],
     "canon_confidence":3},

    {"id":"nether_beast_soul_migration","name":"Nether Beast Soul Migration","species":"nether_beast",
     "description":"The centennial migration of nether beasts to soul-rich battlefields, triggered by mass death events.",
     "waypoints":[
        {"point":"ancient_battlefield","season":"post_war","duration_days":180},
        {"point":"sea_of_devils","season":"post_war","duration_days":365},
        {"point":"immortal_graveyard","season":"post_war","duration_days":365},
        {"point":"soul_refining_territory","season":"return","duration_days":180}
     ],
     "follower_chain":[
        {"follower":"soul_beasts","relationship":"symbiotic","distance_li":50,"note":"Soul beasts follow nether beasts."},
        {"follower":"soul_refining_cultivators","relationship":"symbiotic","distance_li":100,"note":"Soul cultivators harvest soul fragments."},
        {"follower":"six_desire_devils","relationship":"opportunist","distance_li":200,"note":"Devils harvest fallen cultivators."},
        {"follower":"orthodox_hunters","relationship":"predator","distance_li":300,"note":"Orthodox hunters cull devils."}
     ],
     "emergent_markets":[
        {"market":"soul_fragment_market","season":"post_war","goods":["soul_fragment","nether_core","devil_core"]}
     ],
     "canon_confidence":3},

    {"id":"mosquito_swarm_blood_migration","name":"Mosquito Swarm Blood Migration","species":"mosquito_beast",
     "description":"The mass migration of mosquito swarms toward blood-rich events, triggered by wars or tribulations.",
     "waypoints":[
        {"point":"mosquito_valley","season":"pre_war","duration_days":30},
        {"point":"approaching_battlefield","season":"war","duration_days":60},
        {"point":"battlefield_aftermath","season":"post_war","duration_days":180},
        {"point":"mosquito_valley","season":"return","duration_days":30}
     ],
     "follower_chain":[
        {"follower":"blood_beasts","relationship":"symbiotic","distance_li":30,"note":"Blood beasts follow swarms."},
        {"follower":"blood_cultivators","relationship":"symbiotic","distance_li":80,"note":"Blood cultivators harvest mosquito cores."},
        {"follower":"scavenger_beasts","relationship":"scavenger","distance_li":100,"note":"Scavengers clean what swarms leave."},
        {"follower":"orthodox_purifiers","relationship":"predator","distance_li":200,"note":"Purifiers cull swarms."}
     ],
     "emergent_markets":[
        {"market":"blood_essence_market","season":"post_war","goods":["mosquito_core","blood_essence","carapace"]}
     ],
     "canon_confidence":4},

    {"id":"qilin_auspicious_walk","name":"Qilin Auspicious Walk","species":"qilin",
     "description":"The era-defining walk of a qilin across auspicious paths, appearing at the birth of great cultivators.",
     "waypoints":[
        {"point":"qilin_city","season":"era_event","duration_days":1},
        {"point":"auspicious_path_1","season":"era_event","duration_days":7},
        {"point":"auspicious_path_2","season":"era_event","duration_days":7},
        {"point":"auspicious_path_3","season":"era_event","duration_days":7}
     ],
     "follower_chain":[
        {"follower":"auspicious_cultivators","relationship":"symbiotic","distance_li":10,"note":"Auspicious cultivators follow for blessings."},
        {"follower":"treasure_seeking_cults","relationship":"opportunist","distance_li":50,"note":"Treasure seekers follow for auspicious herbs."},
        {"follower":"orthodox_observers","relationship":"observer","distance_li":100,"note":"Orthodox observers record the walk."}
     ],
     "emergent_markets":[
        {"market":"qilin_blessing_market","season":"era_event","goods":["auspicious_herb","qilin_fur","blessing_charm"]}
     ],
     "canon_confidence":4},

    {"id":"void_beast_reality_migration","name":"Void Beast Reality Migration","species":"void_beast",
     "description":"The rare migration of void beasts along reality-edge faults, triggered by void-fissure events.",
     "waypoints":[
        {"point":"void_edge_1","season":"fissure_event","duration_days":90},
        {"point":"reality_fault_2","season":"fissure_event","duration_days":90},
        {"point":"void_edge_3","season":"fissure_event","duration_days":90}
     ],
     "follower_chain":[
        {"follower":"void_cultivators","relationship":"symbiotic","distance_li":500,"note":"Void cultivators follow for void-essence."},
        {"follower":"void_fissure_beasts","relationship":"symbiotic","distance_li":200,"note":"Fissure beasts follow void beasts."},
        {"follower":"void_hunters","relationship":"predator","distance_li":1000,"note":"Spirit Severing void-hunters track them."}
     ],
     "emergent_markets":[
        {"market":"void_essence_market","season":"fissure_event","goods":["void_core","void_essence","reality_fragment"]}
     ],
     "canon_confidence":3},

    {"id":"destiny_beast_nexus_walk","name":"Destiny Beast Nexus Walk","species":"destiny_beast",
     "description":"The era-defining walk of a destiny beast between fate-nexus points.",
     "waypoints":[
        {"point":"fate_nexus_1","season":"era_event","duration_days":1},
        {"point":"fate_nexus_2","season":"era_event","duration_days":1},
        {"point":"fate_nexus_3","season":"era_event","duration_days":1}
     ],
     "follower_chain":[
        {"follower":"destiny_cultivators","relationship":"symbiotic","distance_li":50,"note":"Destiny cultivators follow for fate-threads."},
        {"follower":"orthodox_observers","relationship":"observer","distance_li":200,"note":"Orthodox observers record the walk."}
     ],
     "emergent_markets":[
        {"market":"destiny_thread_market","season":"era_event","goods":["fate_thread","destiny_core","destiny_essence"]}
     ],
     "canon_confidence":3},

    {"id":"ant_kingdom_expansion","name":"Ant Kingdom Expansion","species":"ant_beast",
     "description":"The decades-long expansion of ant kingdoms through underground spirit-vein networks.",
     "waypoints":[
        {"point":"ant_kingdom_core","season":"continuous","duration_days":3650},
        {"point":"vein_tunnel_1","season":"continuous","duration_days":3650},
        {"point":"vein_tunnel_2","season":"continuous","duration_days":3650},
        {"point":"vein_tunnel_3","season":"continuous","duration_days":3650}
     ],
     "follower_chain":[
        {"follower":"giant_centipedes","relationship":"predator","distance_li":1,"note":"Centipedes prey on ant workers."},
        {"follower":"underground_civilizations","relationship":"rival","distance_li":10,"note":"Civilizations fight ant expansion."},
        {"follower":"miner_cults","relationship":"prey","distance_li":5,"note":"Miners are prey for ants."}
     ],
     "emergent_markets":[
        {"market":"ant_pearl_market","season":"continuous","goods":["ant_pearl","mandible","spirit_ore"]}
     ],
     "canon_confidence":3},

    {"id":"thunder_celestial_tribulation_hunt","name":"Thunder Celestial Tribulation Hunt","species":"thunder_celestial_beast",
     "description":"The hunt of thunder celestial beasts appearing during heavenly tribulations.",
     "waypoints":[
        {"point":"tribulation_site_1","season":"tribulation","duration_days":1},
        {"point":"tribulation_site_2","season":"tribulation","duration_days":1},
        {"point":"tribulation_site_3","season":"tribulation","duration_days":1}
     ],
     "follower_chain":[
        {"follower":"thunder_cultivators","relationship":"symbiotic","distance_li":50,"note":"Thunder cultivators harvest tribulation fragments."},
        {"follower":"lei_ji_packs","relationship":"prey","distance_li":100,"note":"Lei Ji are prey for celestials."},
        {"follower":"thunder_hunters","relationship":"predator","distance_li":200,"note":"Spirit Severing hunters track celestials."}
     ],
     "emergent_markets":[
        {"market":"tribulation_fragment_market","season":"tribulation","goods":["celestial_core","tribulation_fragment","nine_color_lightning"]}
     ],
     "canon_confidence":3},

    {"id":"dragon_corpse_decay_circuit","name":"Dragon Corpse Decay Circuit","species":"multiple",
     "description":"The migration of scavenger beasts toward dragon corpses, creating temporary ecology booms.",
     "waypoints":[
        {"point":"dragon_corpse_site","season":"corpse_event","duration_days":365},
        {"point":"scavenger_dispersion","season":"post_decay","duration_days":180}
     ],
     "follower_chain":[
        {"follower":"mosquito_beasts","relationship":"scavenger","distance_li":50,"note":"Mosquitoes feed on dragon blood."},
        {"follower":"blood_beasts","relationship":"scavenger","distance_li":80,"note":"Blood beasts feed on dragon marrow."},
        {"follower":"nether_beasts","relationship":"scavenger","distance_li":100,"note":"Nether beasts feed on dragon soul."},
        {"follower":"treasure_seeking_cults","relationship":"opportunist","distance_li":150,"note":"Treasure seekers harvest dragon parts."}
     ],
     "emergent_markets":[
        {"market":"dragon_part_market","season":"corpse_event","goods":["dragon_scale","dragon_blood","dragon_bone","dragon_core"]}
     ],
     "canon_confidence":3},

    {"id":"soul_refining_pilgrimage","name":"Soul Refining Pilgrimage","species":"cultivator_migration",
     "description":"The pilgrimage of soul-refining cultivators to soul-rich sites for cultivation.",
     "waypoints":[
        {"point":"soul_refining_ancestral","season":"decennial","duration_days":90},
        {"point":"immortal_graveyard","season":"decennial","duration_days":90},
        {"point":"sea_of_devils","season":"decennial","duration_days":90}
     ],
     "follower_chain":[
        {"follower":"merchant_caravans","relationship":"opportunist","distance_li":50,"note":"Caravans serve pilgrims."},
        {"follower":"orthodox_purifiers","relationship":"predator","distance_li":200,"note":"Orthodox cull soul cultivators."},
        {"follower":"bandit_cults","relationship":"parasite","distance_li":100,"note":"Bandits rob pilgrims."}
     ],
     "emergent_markets":[
        {"market":"soul_pilgrimage_market","season":"decennial","goods":["soul_fragment","nether_core","soul_herb"]}
     ],
     "canon_confidence":3},

    {"id":"vermilion_bird_imperial_circuit","name":"Vermilion Bird Imperial Circuit","species":"vermilion_bird",
     "description":"The era-defining circuit of the Vermilion Bird between Divine Sect sites.",
     "waypoints":[
        {"point":"vermilion_divine_sect","season":"imperial_event","duration_days":7},
        {"point":"vermilion_starfield_core","season":"imperial_event","duration_days":30},
        {"point":"vermilion_nest","season":"imperial_event","duration_days":90}
     ],
     "follower_chain":[
        {"follower":"vermilion_cultivators","relationship":"symbiotic","distance_li":1000,"note":"Vermilion cultivators follow for blessing."},
        {"follower":"fire_phoenix","relationship":"rival","distance_li":500,"note":"Phoenixes follow for fire-essence."},
        {"follower":"imperial_candidates","relationship":"competitor","distance_li":100,"note":"Candidates seek Imperial selection."}
     ],
     "emergent_markets":[
        {"market":"vermilion_imperial_market","season":"imperial_event","goods":["vermilion_feather","divine_fire_essence","imperial_charm"]}
     ],
     "canon_confidence":4},

    {"id":"sword_intent_storm_circuit","name":"Sword Intent Storm Circuit","species":"sword_intent",
     "description":"The seasonal circuit of sword-intent storms between sword peaks, drawing sword cultivators.",
     "waypoints":[
        {"point":"da_lou_sword_peak","season":"spring","duration_days":30},
        {"point":"xuan_dao_sword_peak","season":"summer","duration_days":30},
        {"point":"snow_domain_sword_peak","season":"winter","duration_days":30}
     ],
     "follower_chain":[
        {"follower":"sword_cultivators","relationship":"symbiotic","distance_li":10,"note":"Sword cultivators follow for intent cultivation."},
        {"follower":"spirit_crane","relationship":"symbiotic","distance_li":20,"note":"Cranes follow for sword-essence."},
        {"follower":"treasure_seeking_cults","relationship":"opportunist","distance_li":50,"note":"Treasure seekers follow for intent-herbs."}
     ],
     "emergent_markets":[
        {"market":"sword_intent_market","season":"spring","goods":["sword_grass","intent_herb","crane_feather"]}
     ],
     "canon_confidence":3},

    {"id":"ancient_god_remains_pilgrimage","name":"Ancient God Remains Pilgrimage","species":"cultivator_migration",
     "description":"The pilgrimage of body-cultivators and seekers to Ancient God remains for inheritance opportunities.",
     "waypoints":[
        {"point":"ancient_god_cave","season":"continuous","duration_days":365},
        {"point":"ancient_god_finger_mountain","season":"continuous","duration_days":365},
        {"point":"ancient_god_eye_lake","season":"continuous","duration_days":365}
     ],
     "follower_chain":[
        {"follower":"body_cultivators","relationship":"symbiotic","distance_li":50,"note":"Body cultivators seek god-body inheritance."},
        {"follower":"treasure_seeking_cults","relationship":"opportunist","distance_li":100,"note":"Treasure seekers follow for god-treasures."},
        {"follower":"ancient_god_guardian_beasts","relationship":"predator","distance_li":200,"note":"Guardian beasts cull seekers."}
     ],
     "emergent_markets":[
        {"market":"god_remains_market","season":"continuous","goods":["god_bone","god_blood_essence","ancient_spirit_herb"]}
     ],
     "canon_confidence":4},

    {"id":"pill_sea_alchemy_gathering","name":"Pill Sea Alchemy Gathering","species":"cultivator_migration",
     "description":"The decennial gathering of alchemists at the Pill Sea for alchemical cultivation.",
     "waypoints":[
        {"point":"pill_sea_center","season":"decennial","duration_days":180},
        {"point":"pill_sea_vents","season":"decennial","duration_days":90},
        {"point":"pill_demon_lair","season":"decennial","duration_days":30}
     ],
     "follower_chain":[
        {"follower":"alchemist_cults","relationship":"symbiotic","distance_li":50,"note":"Alchemists gather for pill-essence."},
        {"follower":"flame_dragon","relationship":"symbiotic","distance_li":200,"note":"Dragons gather for fire-essence."},
        {"follower":"pill_demon_hunters","relationship":"predator","distance_li":100,"note":"Hunters track pill demons."},
        {"follower":"merchant_caravans","relationship":"opportunist","distance_li":80,"note":"Caravans serve alchemists."}
     ],
     "emergent_markets":[
        {"market":"pill_gathering_market","season":"decennial","goods":["pill_essence","alchemy_herb","dragon_blood"]}
     ],
     "canon_confidence":3},
]

def pass_13_migration_routes(canon):
    """Pass 13: Migration Routes with follower chains."""
    mig_dir = out("migrations")
    count = 0
    for m in MIGRATION_ROUTES:
        f = mig_dir / f"{m['id']}.json"
        if f.exists():
            continue
        doc = {
            "_comment": f"Migration: {m['name']}. "
                         f"{len(m['waypoints'])} waypoints, {len(m['follower_chain'])} follower types, "
                         f"{len(m['emergent_markets'])} emergent markets. "
                         f"Per migration directive: migration is a major simulation system. "
                         f"The world breathes because markets appear seasonally along routes. "
                         f"Prime Directive: route exists objectively; player enters a world already moving.",
            "migration_id": m["id"],
            "name": m["name"],
            "species": m["species"],
            "description": m["description"],
            "derivation_type": "A" if m["canon_confidence"] >= 4 else "B",
            "canon_confidence": m["canon_confidence"],
            "waypoints": m["waypoints"],
            "follower_chain": m["follower_chain"],
            "emergent_markets": m["emergent_markets"],
            "simulation_notes": {
                "world_breathes": "Markets appear and disappear seasonally; "
                                  "the world changes whether or not the player acts.",
                "follower_dynamics": "Each follower type arrives with a delay proportional to distance_li; "
                                     "predators arrive after prey, scavengers after predators, "
                                     "bandits after cultivators, merchants after stability.",
                "perception_tiers": {
                    "mortal": "Sees beasts moving.",
                    "qi_condensation": "Senses migration direction.",
                    "foundation_establishment": "Perceives follower chain.",
                    "core_formation": "Predicts market appearances.",
                    "nascent_soul": "Perceives complete migration system.",
                }
            },
            "salt": salt_for(m["id"]),
        }
        write_json(f, doc)
        count += 1
    return count

# ==================================================================
# PASS 14 - MACRO-SCALE TERRAIN (ANCIENT GOD AS TERRAIN)
# ==================================================================
# Terrain that IS a being. Player shouldn't initially realize they're
# standing on bone. Later: Divine Sense -> Perception changes ->
# "You realize this mountain range... is one finger."

MACRO_TERRAIN = [
    {"id":"ancient_god_finger_mountain","name":"Ancient God Finger Mountain","being":"Ancient God Tu Si",
     "appearance_mortal":"A mountain range of unusual grey stone, 200 li long, peaks oddly knuckle-shaped.",
     "appearance_qi_condensation":"The stone has a faint bone-like grain; cold to the touch regardless of season.",
     "appearance_foundation_establishment":"The 'stone' is clearly petrified bone; marrow-channels visible in cross-sections.",
     "appearance_core_formation":"This is an Ancient God's finger. The peaks are knuckle-joints; the valleys are tendon-grooves.",
     "appearance_nascent_soul":"The finger still holds residual god-essence; marrow-channels pulse faintly with ancient qi.",
     "appearance_spirit_severing":"The entire finger is a relic of Tu Si's Ancient God body; god-essence permeates every grain.",
     "scale":"200 li long, 30 li wide, 5 li tall — one finger of a being whose body spanned a continent.",
     "canonical_event":"Tu Si's Ancient God body fell here; Wang Lin inherited Tu Si's god-knowledge here.",
     "derivation_type":"A","canon_confidence":5},

    {"id":"ancient_god_eye_lake","name":"Ancient God Eye Lake","being":"Ancient God Tu Si",
     "appearance_mortal":"A perfectly circular lake 10 li across, water unnaturally clear, depths immeasurable.",
     "appearance_qi_condensation":"The water reflects light oddly; seems to track the viewer.",
     "appearance_foundation_establishment":"The lake is an eye socket; the 'water' is petrified vitreous humor.",
     "appearance_core_formation":"This is an Ancient God's eye; the socket still holds residual god-sight.",
     "appearance_nascent_soul":"The eye holds Tu Si's last sight; those who meet its gaze see what he saw.",
     "appearance_spirit_severing":"The eye is a window to Tu Si's god-memory; cultivating here grants god-knowledge.",
     "scale":"10 li diameter, depth immeasurable — one eye of a continent-spanning being.",
     "canonical_event":"Tu Si's eye held his last sight before his god-body fell.",
     "derivation_type":"A","canon_confidence":4},

    {"id":"ancient_god_rib_canyon","name":"Ancient God Rib Canyon","being":"Ancient God Tu Si",
     "appearance_mortal":"A canyon 500 li long, walls of curved grey stone arching overhead like ribs.",
     "appearance_qi_condensation":"The walls hum in storms; the canyon amplifies sound strangely.",
     "appearance_foundation_establishment":"The walls are petrified ribs; the canyon is the space between them.",
     "appearance_core_formation":"This is an Ancient God's ribcage; the canyon runs between two ribs.",
     "appearance_nascent_soul":"The ribs still hold residual god-essence; marrow-channels line the walls.",
     "appearance_spirit_severing":"The ribcage protected Tu Si's heart; cultivating here grants body-tempering essence.",
     "scale":"500 li long, 50 li wide, 10 li tall — the ribcage of a continent-spanning being.",
     "canonical_event":"Tu Si's ribcage formed this canyon when his god-body fell.",
     "derivation_type":"B","canon_confidence":3},

    {"id":"dragon_corpse_canyon","name":"Dragon Corpse Canyon","being":"Ancient Flame Dragon",
     "appearance_mortal":"A winding canyon 100 li long, walls of red stone, scales visible in places.",
     "appearance_qi_condensation":"The canyon radiates heat; the walls are warm to the touch.",
     "appearance_foundation_establishment":"The walls are petrified dragon scales; the canyon is the dragon's body.",
     "appearance_core_formation":"This is an ancient flame dragon's corpse; the canyon runs along its spine.",
     "appearance_nascent_soul":"The dragon's marrow still holds fire-essence; the corpse cultivates passively.",
     "appearance_spirit_severing":"The dragon's soul still lingers; cultivating here grants dragon-essence.",
     "scale":"100 li long, 10 li wide, 2 li tall — the corpse of an ancient dragon.",
     "canonical_event":"An ancient flame dragon fell here; its corpse formed the canyon.",
     "derivation_type":"B","canon_confidence":3},

    {"id":"black_tortoise_shell_continent","name":"Black Tortoise Shell Continent","being":"Black Tortoise",
     "appearance_mortal":"A continent of black-jade stone, oddly domed, 1000 li across.",
     "appearance_qi_condensation":"The continent feels stable beyond reason; earthquakes never occur here.",
     "appearance_foundation_establishment":"The continent is a single piece of shell; the dome is its curve.",
     "appearance_core_formation":"This is the Black Tortoise's shell; the continent rests on the sleeping divine beast.",
     "appearance_nascent_soul":"The Tortoise slumbers beneath; its breath is the continent's climate.",
     "appearance_spirit_severing":"The Tortoise supports the continent in its slumber; waking it would sink the continent.",
     "scale":"1000 li across — the shell of a divine beast that supports a continent.",
     "canonical_event":"The Black Tortoise sleeps beneath Xuan Wu; its shell IS the continent.",
     "derivation_type":"A","canon_confidence":4},

    {"id":"vermilion_bird_ember_mountains","name":"Vermilion Bird Ember Mountains","being":"Vermilion Bird",
     "appearance_mortal":"A mountain range of red stone, perpetually warm, peaks glow at dusk.",
     "appearance_qi_condensation":"The mountains radiate fire-essence; the stone is warm year-round.",
     "appearance_foundation_establishment":"The mountains are petrified feathers; the glow is residual divine fire.",
     "appearance_core_formation":"This is where the Vermilion Bird once nested; the mountains are its shed feathers.",
     "appearance_nascent_soul":"The divine fire still smolders; cultivating here grants fire-essence.",
     "appearance_spirit_severing":"The Vermilion Bird's nest-essence permeates the range; the divine bird returns here.",
     "scale":"300 li long — the shed feathers of a divine bird.",
     "canonical_event":"The Vermilion Bird nested here before ascending to the Divine Sect.",
     "derivation_type":"A","canon_confidence":4},

    {"id":"azure_dragon_spine_sea","name":"Azure Dragon Spine Sea","being":"Azure Dragon",
     "appearance_mortal":"A sea ridge 1000 li long, peaks break the surface as islands, stone is azure jade.",
     "appearance_qi_condensation":"The sea currents swirl around the ridge in dragon-like patterns.",
     "appearance_foundation_establishment":"The ridge is a petrified dragon spine; the islands are vertebrae.",
     "appearance_core_formation":"This is the Azure Dragon's spine; the sea is its grave.",
     "appearance_nascent_soul":"The dragon's water-essence still permeates the sea; cultivating here grants dragon-essence.",
     "appearance_spirit_severing":"The Azure Dragon's soul sleeps in the deep; waking it would reshape the sea.",
     "scale":"1000 li long — the spine of a divine dragon.",
     "canonical_event":"The Azure Dragon's corpse formed this sea-ridge in the ancient era.",
     "derivation_type":"B","canon_confidence":3},

    {"id":"white_tiger_fang_peaks","name":"White Tiger Fang Peaks","being":"White Tiger",
     "appearance_mortal":"A cluster of sharp white peaks, 50 li across, stone white-gold, air carries killing intent.",
     "appearance_qi_condensation":"The peaks radiate slaughter-qi; cultivators feel uneasy here.",
     "appearance_foundation_establishment":"The peaks are petrified fangs; the slaughter-qi is residual divine intent.",
     "appearance_core_formation":"This is the White Tiger's shed fang cluster; the peaks formed from its teeth.",
     "appearance_nascent_soul":"The slaughter-essence still saturates the peaks; cultivating here grants slaughter-essence.",
     "appearance_spirit_severing":"The White Tiger's killing intent lingers; the divine beast returns at world-wars.",
     "scale":"50 li across — the shed fangs of a divine beast.",
     "canonical_event":"The White Tiger shed its fangs here before ascending to the Foreign Battleground.",
     "derivation_type":"B","canon_confidence":3},

    {"id":"blood_ancestor_congealed_sea","name":"Blood Ancestor Congealed Sea","being":"Blood Ancestor Beast",
     "appearance_mortal":"A sea of congealed blood 200 li across, perpetually liquid, no waves.",
     "appearance_qi_condensation":"The sea radiates blood-essence; the air is thick with blood-qi.",
     "appearance_foundation_establishment":"The sea is congealed blood; the blood is alive, slowly regenerating.",
     "appearance_core_formation":"This is the Blood Ancestor's corpse-sea; the blood is its congealed body.",
     "appearance_nascent_soul":"The Blood Ancestor's will still lingers in the blood; cultivating here is dangerous.",
     "appearance_spirit_severing":"The Blood Ancestor can reform from this sea; the sea is its anchor.",
     "scale":"200 li across — the congealed body of an ancient beast.",
     "canonical_event":"Wang Lin killed the Blood Ancestor here; its corpse-sea remains.",
     "derivation_type":"A","canon_confidence":4},

    {"id":"six_desire_devil_mirror_lake","name":"Six-Desire Devil Mirror Lake","being":"Six-Desire Devil",
     "appearance_mortal":"A lake 5 li across, surface perfectly mirror-still, reflects impossible scenes.",
     "appearance_qi_condensation":"The lake radiates desire-essence; viewers see their desires reflected.",
     "appearance_foundation_establishment":"The lake is a congealed desire-mass; the reflections are the devil's last dreams.",
     "appearance_core_formation":"This is the Six-Desire Devil's corpse-lake; the desire-mass is its congealed body.",
     "appearance_nascent_soul":"The devil's desire-will still lingers; cultivating here is corrupting.",
     "appearance_spirit_severing":"The devil can reform from this lake; the lake is its anchor.",
     "scale":"5 li across — the congealed body of an ancient devil.",
     "canonical_event":"An ancient Six-Desire Devil fell here; its desire-mass formed the lake.",
     "derivation_type":"B","canon_confidence":3},

    {"id":"cosmic_whale_back_civilization","name":"Cosmic Whale-Back Civilization","being":"Cosmic Whale",
     "appearance_mortal":"A floating continent 100 li across, hovers in the void, surface is starlight.",
     "appearance_qi_condensation":"The continent radiates cosmic-essence; the void around it is calm.",
     "appearance_foundation_establishment":"The continent is the back of a living cosmic whale; the whale swims through void.",
     "appearance_core_formation":"This is a cosmic whale's back; an entire civilization lives on it.",
     "appearance_nascent_soul":"The whale's cosmic-essence sustains the civilization; the whale cultivates by swimming.",
     "appearance_spirit_severing":"The whale and civilization are symbiotic; the civilization feeds the whale cosmic-qi.",
     "scale":"100 li across — the back of a cosmic leviathan that swims through the void.",
     "canonical_event":"Cosmic whales carry entire civilizations through the void.",
     "derivation_type":"B","canon_confidence":3},

    {"id":"ancient_battlefield_bone_plain","name":"Ancient Battlefield Bone Plain","being":"millions of fallen",
     "appearance_mortal":"A plain 500 li across, soil is grey-white, oddly fertile.",
     "appearance_qi_condensation":"The plain radiates death-qi; the soil is clearly not normal earth.",
     "appearance_foundation_establishment":"The soil is crushed bone; the plain is a mass grave.",
     "appearance_core_formation":"This is an ancient battlefield; millions of cultivators fell here; the soil is their bone.",
     "appearance_nascent_soul":"The death-essence still saturates the plain; nether beasts cultivate here.",
     "appearance_spirit_severing":"The battlefield's karmic weight warps reality; cultivating here is dangerous.",
     "scale":"500 li across — the bone-crushed remains of millions of fallen cultivators.",
     "canonical_event":"An ancient war killed millions here; the battlefield remains saturated with death.",
     "derivation_type":"A","canon_confidence":4},
]

def pass_14_macro_terrain(canon):
    """Pass 14: Macro-Scale Terrain where terrain IS a being."""
    terr_dir = out("macro_terrain")
    count = 0
    for t in MACRO_TERRAIN:
        f = terr_dir / f"{t['id']}.json"
        if f.exists():
            continue
        doc = {
            "_comment": f"Macro-Terrain: {t['name']}. "
                         f"Per scale directive: this terrain IS a being. "
                         f"Player shouldn't initially realize they're standing on bone. "
                         f"Divine Sense -> Perception changes -> 'You realize this mountain range... is one finger.' "
                         f"Prime Directive: the being's corpse is objectively terrain; "
                         f"perception tier determines what the player sees.",
            "terrain_id": t["id"],
            "name": t["name"],
            "being": t["being"],
            "derivation_type": t["derivation_type"],
            "canon_confidence": t["canon_confidence"],
            "scale": t["scale"],
            "canonical_event": t["canonical_event"],
            "perception_tiered_appearance": {
                "mortal": t["appearance_mortal"],
                "qi_condensation": t["appearance_qi_condensation"],
                "foundation_establishment": t["appearance_foundation_establishment"],
                "core_formation": t["appearance_core_formation"],
                "nascent_soul": t["appearance_nascent_soul"],
                "spirit_severing": t["appearance_spirit_severing"],
            },
            "perception_mechanic": {
                "trigger": "When player's perception tier rises, the appearance re-rolls automatically.",
                "no_locked_upgrades": "A mortal who later reaches Core Formation will see the truth; "
                                      "the terrain does not change, only understanding does.",
                "canon_fidelity": "This is exactly how Wang Lin perceived Ancient God Tu Si's remains — "
                                  "first as terrain, then as god-bone, then as god-memory.",
            },
            "salt": salt_for(t["id"]),
        }
        write_json(f, doc)
        count += 1
    return count

# ==================================================================
# PASS 15 - EXPANDED PROVENANCE SCHEMA
# ==================================================================
# Upgrade the 178 provenance files with full lifecycle tracking:
# creator, every_known_owner, documented_battles, repairs, refinements,
# blood_bonds, spirit_state, seal_history, destruction_history,
# reconstruction_history, corruption_history, heavenly_tribulations_survived.

def pass_15_provenance_expansion(canon):
    """Pass 15: Expand provenance files with full lifecycle schema."""
    prov_dir = DATA / "provenance"
    if not prov_dir.exists():
        return 0
    count = 0
    for art in canon.get("artifacts", []):
        aid = art.get("id","")
        name = art.get("name","")
        # match existing file by slug
        slug_name = slug(name)
        candidates = list(prov_dir.glob(f"{slug_name}*.json"))
        if not candidates:
            # try by id
            candidates = list(prov_dir.glob(f"*{aid}*.json"))
        if not candidates:
            continue
        f = candidates[0]
        try:
            with open(f, encoding="utf-8") as fh:
                doc = json.load(fh)
        except Exception:
            continue
        # Add expanded lifecycle fields if not present
        if "lifecycle" in doc:
            continue  # already upgraded
        doc["lifecycle"] = {
            "creator": art.get("origin","unknown") if "found" not in str(art.get("origin","")).lower() else "unknown (pre-canon)",
            "creation_circumstances": art.get("origin","unknown"),
            "every_known_owner": _infer_owners(art),
            "documented_battles": [],
            "repairs": [],
            "refinements": [],
            "blood_bonds": [],
            "spirit_state": "dormant",
            "seal_history": [],
            "destruction_history": [],
            "reconstruction_history": [],
            "corruption_history": [],
            "heavenly_tribulations_survived": [],
        }
        doc["expanded_schema"] = {
            "note": "Per provenance directive: schema supports emergent history even if many fields are empty initially.",
            "fields": ["creator","creation_circumstances","every_known_owner","documented_battles",
                       "repairs","refinements","blood_bonds","spirit_state","seal_history",
                       "destruction_history","reconstruction_history","corruption_history",
                       "heavenly_tribulations_survived"],
            "emergent_history": "These fields will be populated by the World State Engine as history unfolds.",
        }
        write_json(f, doc)
        count += 1
    return count

def _infer_owners(art):
    """Infer owners from canon facts."""
    owners = []
    facts = art.get("knownFacts", [])
    for fact in facts:
        fact_str = str(fact).lower()
        for name in ["wang lin","situ nan","xuan luo","tu si","li muwan","wang zhuo","seven-colored daoist"]:
            if name in fact_str and name not in owners:
                owners.append(name)
    return owners if owners else ["unknown"]

# ==================================================================
# PASS 16 - CIVILIZATION SIMULATION DEFINITIONS
# ==================================================================
# Each sect/clan/city simulates: population structure, economy, politics,
# lifecycle (recruitment, expansion, decline, schisms, wars, recovery).

CIVILIZATIONS = [
    {"id":"heng_yue_sect","name":"Heng Yue Sect","type":"sect","location":"Zhao Country",
     "peak_realm":"Nascent Soul (Sect Master)","canon_status":"declined_after_wang_lin",
     "population_structure":{
        "outer_disciples":2000,"inner_disciples":300,"core_disciples":30,
        "elders":12,"peak_lords":7,"ancestor":1,"spirit_beasts":50},
     "economy":{
        "spirit_stones":"moderate","food":"self-sufficient","water":"self-sufficient",
        "alchemy":"basic","forging":"basic","formation":"basic",
        "trade":"Zhao Country markets","recruitment":"Zhao mortal families"},
     "politics":{
        "allies":["Zhao royal family (nominal)"],"enemies":["rival Zhao sects"],
        "vassals":[],"rivals":["Cloud Sky Sect (regional)"],
        "internal_factions":["orthodox elders","reformist disciples"]},
     "lifecycle":{
        "recruitment":"annual examination of Zhao mortal youth",
        "expansion":"slow; constrained by Zhao Country size",
        "decline":"accelerated after Wang Lin's departure and elder deaths",
        "schisms":"none canonical",
        "wars":["border skirmishes with rival sects"],
        "recovery":"stagnant; lacks Nascent Soul+ leadership"},
     "libraries":["basic techniques","Heng Yue formation manuals","spirit herb catalogues"],
     "spirit_beasts":["mounts for inner disciples","guardian beasts for treasury"],
     "canon_confidence":5},

    {"id":"cloud_sky_sect","name":"Cloud Sky Sect","type":"sect","location":"Chu Country",
     "peak_realm":"Spirit Severing (Sect Master)","canon_status":"major_power",
     "population_structure":{
        "outer_disciples":5000,"inner_disciples":800,"core_disciples":80,
        "elders":25,"peak_lords":12,"ancestor":2,"spirit_beasts":200},
     "economy":{
        "spirit_stones":"abundant","food":"self-sufficient","water":"self-sufficient",
        "alchemy":"moderate","forging":"moderate","formation":"moderate",
        "trade":"Chu Country + regional","recruitment":"Chu + neighboring countries"},
     "politics":{
        "allies":["Chu royal family"],"enemies":["Sky Demon Country sects"],
        "vassals":["minor Chu sects"],"rivals":["Heavenly Fate Sect"],
        "internal_factions":["main lineage","branch lineages"]},
     "lifecycle":{
        "recruitment":"decennial examination",
        "expansion":"steady; regional influence growing",
        "decline":"none current",
        "schisms":"branch lineage tension",
        "wars":["border conflicts with Sky Demon Country"],
        "recovery":"strong; multiple Spirit Severing elders"},
     "libraries":["intermediate techniques","Cloud Sky formation manuals","spirit beast catalogues","alchemy basics"],
     "spirit_beasts":["mounts for core disciples","guardian beasts for peaks","war beasts for conflicts"],
     "canon_confidence":5},

    {"id":"soul_refining_sect","name":"Soul Refining Sect","type":"sect","location":"Sea of Devils",
     "peak_realm":"Ascendant (Ancestor)","canon_status":"major_devil_sect",
     "population_structure":{
        "outer_disciples":3000,"inner_disciples":500,"core_disciples":50,
        "elders":15,"peak_lords":9,"ancestor":1,"spirit_beasts":1000},
     "economy":{
        "spirit_stones":"abundant (devil-aspected)","food":"corpse-aspected","water":"nether-aspected",
        "alchemy":"soul-pill focus","forging":"soul-weapon focus","formation":"soul-formation focus",
        "trade":"black markets","recruitment":"fallen cultivators, devil-aspected youth"},
     "politics":{
        "allies":["devil-sects of Sea of Devils"],"enemies":["all orthodox sects"],
        "vassals":["minor devil-sects"],"rivals":["Heavenly Fate Sect (orthodox rival)"],
        "internal_factions":["ancestor loyalists","reformist devil-cultivators"]},
     "lifecycle":{
        "recruitment":"continuous; takes in fallen cultivators",
        "expansion":"aggressive in Sea of Devils",
        "decline":"none current; ancestor present",
        "schisms":"orthodox-reformist tension",
        "wars":["perennial with orthodox sects"],
        "recovery":"strong; ancestor ensures continuity"},
     "libraries":["soul-refining techniques","devil-formation manuals","soul-beast catalogues","soul-pill recipes"],
     "spirit_beasts":["nether beasts","soul lashers","ghost-spirit beasts","devil mounts"],
     "canon_confidence":5},

    {"id":"heavenly_fate_sect","name":"Heavenly Fate Sect","type":"sect","location":"Heavenly Fate Country",
     "peak_realm":"Ascendant (Sect Master)","canon_status":"major_orthodox_power",
     "population_structure":{
        "outer_disciples":8000,"inner_disciples":1500,"core_disciples":150,
        "elders":30,"peak_lords":18,"ancestor":3,"spirit_beasts":500},
     "economy":{
        "spirit_stones":"abundant","food":"self-sufficient","water":"self-sufficient",
        "alchemy":"advanced","forging":"advanced","formation":"advanced",
        "trade":"regional + star-system","recruitment":"multiple countries"},
     "politics":{
        "allies":["multiple orthodox sects"],"enemies":["devil-sects","Sky Demon Country"],
        "vassals":["minor orthodox sects"],"rivals":["Soul Refining Sect"],
        "internal_factions":["main lineage","elder council","peak lord council"]},
     "lifecycle":{
        "recruitment":"annual examination across multiple countries",
        "expansion":"steady; star-system influence",
        "decline":"none current",
        "schisms":"elder council vs peak lord tension",
        "wars":["perennial with devil-sects"],
        "recovery":"very strong; multiple Ascendant ancestors"},
     "libraries":["advanced techniques","Heavenly Fate formation manuals","spirit beast catalogues","advanced alchemy","star-system lore"],
     "spirit_beasts":["mounts for inner disciples","guardian beasts","war beasts","star-system beasts"],
     "canon_confidence":5},

    {"id":"da_lou_sword_sect","name":"Da Lou Sword Sect","type":"sect","location":"Suzaku sword region",
     "peak_realm":"Ascendant (Sword Master)","canon_status":"major_sword_sect",
     "population_structure":{
        "outer_disciples":2000,"inner_disciples":400,"core_disciples":40,
        "elders":10,"peak_lords":9,"ancestor":1,"spirit_beasts":100},
     "economy":{
        "spirit_stones":"moderate","food":"self-sufficient","water":"self-sufficient",
        "alchemy":"basic","forging":"sword-focus","formation":"sword-formation focus",
        "trade":"sword markets","recruitment":"sword-aspected youth"},
     "politics":{
        "allies":["other sword sects"],"enemies":["non-sword rival sects"],
        "vassals":[],"rivals":["Heavenly Fate Sect (general rivalry)"],
        "internal_factions":["main sword lineage","branch sword lineages"]},
     "lifecycle":{
        "recruitment":"decennial sword-aspected examination",
        "expansion":"steady; sword-aspected regions",
        "decline":"none current",
        "schisms":"sword lineage disputes",
        "wars":["sword-duel conflicts"],
        "recovery":"strong; sword-master present"},
     "libraries":["sword techniques","sword-formation manuals","sword-intent catalogues","sword-herb lore"],
     "spirit_beasts":["spirit cranes","sword-aspected beasts"],
     "canon_confidence":4},

    {"id":"vermilion_bird_divine_sect","name":"Vermilion Bird Divine Sect","type":"divine_sect","location":"Vermilion Bird Starfield",
     "peak_realm":"Immortal (Divine Emperor)","canon_status":"apex_power",
     "population_structure":{
        "outer_disciples":50000,"inner_disciples":10000,"core_disciples":1000,
        "elders":100,"peak_lords":36,"divine_emperor":1,"spirit_beasts":5000},
     "economy":{
        "spirit_stones":"vast","food":"self-sufficient","water":"self-sufficient",
        "alchemy":"immortal-grade","forging":"immortal-grade","formation":"immortal-grade",
        "trade":"star-system","recruitment":"star-system-wide"},
     "politics":{
        "allies":["vassal star-sects"],"enemies":["rival divine sects"],
        "vassals":["entire star-sects"],"rivals":["Azure Dragon Divine Sect"],
        "internal_factions":["divine emperor loyalists","elder council","star-system lords"]},
     "lifecycle":{
        "recruitment":"star-system-wide divine examination",
        "expansion":"star-system-scale",
        "decline":"none; divine emperor ensures continuity",
        "schisms":"star-system lord tension",
        "wars":["divine-sect wars"],
        "recovery":"divine; divine emperor can intervene personally"},
     "libraries":["immortal techniques","divine formation manuals","divine beast catalogues","immortal alchemy","star-system lore","divine inheritance"],
     "spirit_beasts":["vermilion bird (divine)","fire phoenixes","flame dragons","star-system beasts"],
     "canon_confidence":5},

    {"id":"wang_family_village","name":"Wang Family Village","type":"mortal_clan","location":"Zhao Country",
     "peak_realm":"Qi Condensation (village head)","canon_status":"wang_lin_origin",
     "population_structure":{
        "mortals":500,"qi_condensation":5,"foundation_establishment":1,
        "elders":3,"village_head":1,"spirit_beasts":10},
     "economy":{
        "spirit_stones":"minimal","food":"self-sufficient (farming)","water":"well",
        "alchemy":"none","forging":"none","formation":"none",
        "trade":"local market","recruitment":"birth"},
     "politics":{
        "allies":["neighboring villages"],"enemies":["bandits"],
        "vassals":[],"rivals":["neighboring clans"],
        "internal_factions":["main family","branch families"]},
     "lifecycle":{
        "recruitment":"birth",
        "expansion":"none",
        "decline":"after Wang Lin's departure; bandit raids",
        "schisms":"none",
        "wars":["bandit raids"],
        "recovery":"slow; Wang Lin's occasional protection"},
     "libraries":["mortal records","family genealogy"],
     "spirit_beasts":["farm animals","watch dogs"],
     "canon_confidence":5},

    {"id":"tian_shui_city","name":"Tian Shui City","type":"mortal_city","location":"Zhao Country",
     "peak_realm":"Core Formation (city lord)","canon_status":"major_zhao_city",
     "population_structure":{
        "mortals":100000,"qi_condensation":500,"foundation_establishment":50,
        "core_formation":5,"city_lord":1,"spirit_beasts":200},
     "economy":{
        "spirit_stones":"moderate","food":"trade hub","water":"river",
        "alchemy":"basic","forging":"basic","formation":"basic",
        "trade":"Zhao Country hub","recruitment":"migration"},
     "politics":{
        "allies":["Zhao royal family"],"enemies":["bandit clans"],
        "vassals":["surrounding villages"],"rivals":["other Zhao cities"],
        "internal_factions":["merchant guild","cultivator guild","city guard"]},
     "lifecycle":{
        "recruitment":"migration",
        "expansion":"steady",
        "decline":"none current",
        "schisms":"merchant vs cultivator tension",
        "wars":["bandit suppression"],
        "recovery":"strong; trade hub"},
     "libraries":["city records","merchant ledgers","basic technique manuals"],
     "spirit_beasts":["caravan beasts","guard beasts"],
     "canon_confidence":4},

    {"id":"qing_lin_ancestral","name":"Qing Lin Ancestral Temple","type":"ancient_clan","location":"Ancient Clan territory",
     "peak_realm":"Ascendant (clan ancestor)","canon_status":"ancient_clan",
     "population_structure":{
        "outer_members":1000,"inner_members":200,"core_members":20,
        "elders":8,"clan_ancestor":1,"spirit_beasts":50},
     "economy":{
        "spirit_stones":"ancient reserves","food":"self-sufficient","water":"self-sufficient",
        "alchemy":"ancient","forging":"ancient","formation":"ancient",
        "trade":"ancient-clan network","recruitment":"bloodline only"},
     "politics":{
        "allies":["other ancient clans"],"enemies":["newer powers"],
        "vassals":[],"rivals":["other ancient clans"],
        "internal_factions":["main bloodline","branch bloodlines"]},
     "lifecycle":{
        "recruitment":"bloodline only",
        "expansion":"slow; bloodline-constrained",
        "decline":"slow; ancient power waning",
        "schisms":"bloodline disputes",
        "wars":["ancient-clan conflicts"],
        "recovery":"slow; depends on bloodline awakening"},
     "libraries":["ancient techniques","clan formation manuals","bloodline catalogues","ancient alchemy"],
     "spirit_beasts":["ancient-clan guardian beasts"],
     "canon_confidence":4},

    {"id":"snow_domain_royal_court","name":"Snow Domain Royal Court","type":"royal_court","location":"Snow Domain Country",
     "peak_realm":"Spirit Severing (Snow Emperor)","canon_status":"regional_power",
     "population_structure":{
        "mortals":500000,"qi_condensation":5000,"foundation_establishment":500,
        "core_formation":50,"elders":20,"snow_emperor":1,"spirit_beasts":1000},
     "economy":{
        "spirit_stones":"moderate","food":"ice-aspected","water":"ice-melt",
        "alchemy":"ice-focus","forging":"ice-focus","formation":"ice-focus",
        "trade":"ice-attribute goods","recruitment":"Snow Domain"},
     "politics":{
        "allies":["ice-aspected sects"],"enemies":["fire-aspected powers"],
        "vassals":["Snow Domain sects"],"rivals":["Fire Burn Country"],
        "internal_factions":["royal family","minister council"]},
     "lifecycle":{
        "recruitment":"Snow Domain nobility",
        "expansion":"steady",
        "decline":"none current",
        "schisms":"royal vs minister tension",
        "wars":["border conflicts with Fire Burn"],
        "recovery":"strong; Snow Emperor present"},
     "libraries":["ice techniques","ice formation manuals","ice beast catalogues","royal records"],
     "spirit_beasts":["ice phoenixes (tributed)","cold dragons","ice wolves"],
     "canon_confidence":4},

    {"id":"fire_burn_royal_court","name":"Fire Burn Royal Court","type":"royal_court","location":"Fire Burn Country",
     "peak_realm":"Spirit Severing (Fire Emperor)","canon_status":"regional_power",
     "population_structure":{
        "mortals":600000,"qi_condensation":6000,"foundation_establishment":600,
        "core_formation":60,"elders":25,"fire_emperor":1,"spirit_beasts":1200},
     "economy":{
        "spirit_stones":"moderate","food":"volcano-aspected","water":"volcano-spring",
        "alchemy":"fire-focus","forging":"fire-focus","formation":"fire-focus",
        "trade":"fire-attribute goods","recruitment":"Fire Burn Country"},
     "politics":{
        "allies":["fire-aspected sects","flame dragon (tributed)"],"enemies":["ice-aspected powers"],
        "vassals":["Fire Burn sects"],"rivals":["Snow Domain Country"],
        "internal_factions":["royal family","war council"]},
     "lifecycle":{
        "recruitment":"Fire Burn nobility",
        "expansion":"steady",
        "decline":"none current",
        "schisms":"royal vs war council tension",
        "wars":["border conflicts with Snow Domain"],
        "recovery":"strong; Fire Emperor + flame dragon tribute"},
     "libraries":["fire techniques","fire formation manuals","fire beast catalogues","royal records"],
     "spirit_beasts":["flame dragons (tributed)","fire phoenixes","fire wolves"],
     "canon_confidence":4},

    {"id":"sky_demon_court","name":"Sky Demon Court","type":"royal_court","location":"Sky Demon Country",
     "peak_realm":"Spirit Severing (Sky Demon Emperor)","canon_status":"regional_power",
     "population_structure":{
        "mortals":400000,"qi_condensation":4000,"foundation_establishment":400,
        "core_formation":40,"elders":15,"sky_demon_emperor":1,"spirit_beasts":800},
     "economy":{
        "spirit_stones":"moderate","food":"self-sufficient","water":"self-sufficient",
        "alchemy":"demon-focus","forging":"demon-focus","formation":"demon-focus",
        "trade":"demon-attribute goods","recruitment":"Sky Demon Country"},
     "politics":{
        "allies":["demon-aspected sects"],"enemies":["orthodox powers"],
        "vassals":["Sky Demon sects"],"rivals":["Heavenly Fate Sect"],
        "internal_factions":["royal family","demon council"]},
     "lifecycle":{
        "recruitment":"Sky Demon nobility",
        "expansion":"steady",
        "decline":"none current",
        "schisms":"royal vs demon council tension",
        "wars":["perennial with orthodox powers"],
        "recovery":"strong; Sky Demon Emperor present"},
     "libraries":["demon techniques","demon formation manuals","demon beast catalogues","royal records"],
     "spirit_beasts":["demon beasts","sky demon mounts"],
     "canon_confidence":4},

    {"id":"allheaven_star_system","name":"Allheaven Star System","type":"star_system_civilization","location":"Allheaven Star System",
     "peak_realm":"Immortal (star lord)","canon_status":"star_system_power",
     "population_structure":{
        "mortals":"billions","cultivators":"millions","elders":"thousands","star_lord":1,"spirit_beasts":"vast"},
     "economy":{
        "spirit_stones":"vast","food":"star-system","water":"star-system",
        "alchemy":"immortal-grade","forging":"immortal-grade","formation":"immortal-grade",
        "trade":"inter-star-system","recruitment":"star-system-wide"},
     "politics":{
        "allies":["vassal star-sects"],"enemies":["rival star systems"],
        "vassals":["entire star-sects"],"rivals":["Cloud Sea Star System"],
        "internal_factions":["star lord loyalists","star-system lords"]},
     "lifecycle":{
        "recruitment":"star-system-wide",
        "expansion":"star-system-scale",
        "decline":"none current",
        "schisms":"star-system lord tension",
        "wars":["star-system wars"],
        "recovery":"strong; star lord present"},
     "libraries":["immortal techniques","star-system lore","vast collections"],
     "spirit_beasts":["star-system beasts","cosmic beasts"],
     "canon_confidence":3},

    {"id":"cloud_sea_star_system","name":"Cloud Sea Star System","type":"star_system_civilization","location":"Cloud Sea Star System",
     "peak_realm":"Immortal (star lord)","canon_status":"star_system_power",
     "population_structure":{
        "mortals":"billions","cultivators":"millions","elders":"thousands","star_lord":1,"spirit_beasts":"vast"},
     "economy":{
        "spirit_stones":"vast","food":"star-system","water":"star-system",
        "alchemy":"immortal-grade","forging":"immortal-grade","formation":"immortal-grade",
        "trade":"inter-star-system","recruitment":"star-system-wide"},
     "politics":{
        "allies":["vassal star-sects"],"enemies":["rival star systems"],
        "vassals":["entire star-sects"],"rivals":["Allheaven Star System"],
        "internal_factions":["star lord loyalists","star-system lords"]},
     "lifecycle":{
        "recruitment":"star-system-wide",
        "expansion":"star-system-scale",
        "decline":"none current",
        "schisms":"star-system lord tension",
        "wars":["star-system wars"],
        "recovery":"strong; star lord present"},
     "libraries":["immortal techniques","star-system lore","vast collections"],
     "spirit_beasts":["star-system beasts","cloud whales"],
     "canon_confidence":3},

    {"id":"pill_sect","name":"Pill Sect","type":"sect","location":"Pill Sea",
     "peak_realm":"Ascendant (pill ancestor)","canon_status":"major_alchemy_sect",
     "population_structure":{
        "outer_disciples":3000,"inner_disciples":600,"core_disciples":60,
        "elders":15,"peak_lords":9,"pill_ancestor":1,"spirit_beasts":300},
     "economy":{
        "spirit_stones":"abundant","food":"self-sufficient","water":"self-sufficient",
        "alchemy":"apex","forging":"basic","formation":"basic",
        "trade":"pill trade (regional)","recruitment":"alchemy-aspected youth"},
     "politics":{
        "allies":["alchemy-aspected sects"],"enemies":["rival alchemy sects"],
        "vassals":[],"rivals":["other alchemy powers"],
        "internal_factions":["pill lineage","furnace lineage"]},
     "lifecycle":{
        "recruitment":"alchemy-aspected examination",
        "expansion":"steady",
        "decline":"none current",
        "schisms":"pill vs furnace lineage tension",
        "wars":["pill-trade conflicts"],
        "recovery":"strong; pill ancestor present"},
     "libraries":["apex alchemy","pill recipes","alchemy formation manuals","herb catalogues"],
     "spirit_beasts":["pill-aspected beasts","flame dragons (alchemy furnaces)"],
     "canon_confidence":3},

    {"id":"formation_sect","name":"Formation Sect","type":"sect","location":"Restriction Mountain region",
     "peak_realm":"Ascendant (formation ancestor)","canon_status":"major_formation_sect",
     "population_structure":{
        "outer_disciples":2000,"inner_disciples":400,"core_disciples":40,
        "elders":12,"peak_lords":7,"formation_ancestor":1,"spirit_beasts":100},
     "economy":{
        "spirit_stones":"moderate","food":"self-sufficient","water":"self-sufficient",
        "alchemy":"basic","forging":"basic","formation":"apex",
        "trade":"formation trade","recruitment":"formation-aspected youth"},
     "politics":{
        "allies":["formation-aspected sects"],"enemies":["rival formation sects"],
        "vassals":[],"rivals":["other formation powers"],
        "internal_factions":["formation lineage","restriction lineage"]},
     "lifecycle":{
        "recruitment":"formation-aspected examination",
        "expansion":"steady",
        "decline":"none current",
        "schisms":"formation vs restriction tension",
        "wars":["formation conflicts"],
        "recovery":"strong; formation ancestor present"},
     "libraries":["apex formation","formation manuals","restriction catalogues","formation beast lore"],
     "spirit_beasts":["restriction beasts (tamed)","formation guardians"],
     "canon_confidence":3},

    {"id":"trading_planet_guild","name":"Trading Planet Guild","type":"merchant_guild","location":"Trading Planet",
     "peak_realm":"Spirit Severing (guild master)","canon_status":"major_merchant_power",
     "population_structure":{
        "mortals":"millions","cultivators":"thousands","elders":50,"guild_master":1,"spirit_beasts":500},
     "economy":{
        "spirit_stones":"vast","food":"trade","water":"trade",
        "alchemy":"trade","forging":"trade","formation":"trade",
        "trade":"inter-star-system apex","recruitment":"merchant families"},
     "politics":{
        "allies":["all trade partners"],"enemies":["bandit clans"],
        "vassals":["caravan fleets"],"rivals":["other merchant guilds"],
        "internal_factions":["merchant families","caravan fleets"]},
     "lifecycle":{
        "recruitment":"merchant family birth",
        "expansion":"trade-route expansion",
        "decline":"none current",
        "schisms":"merchant family disputes",
        "wars":["trade-route conflicts"],
        "recovery":"strong; vast reserves"},
     "libraries":["trade records","star-system maps","goods catalogues","basic technique manuals"],
     "spirit_beasts":["caravan beasts","guard beasts"],
     "canon_confidence":3},

    {"id":"great_soul_sect","name":"Great Soul Sect","type":"sect","location":"Immortal Astral Continent",
     "peak_realm":"Immortal (sect master)","canon_status":"immortal_sect",
     "population_structure":{
        "outer_disciples":100000,"inner_disciples":20000,"core_disciples":2000,
        "elders":200,"peak_lords":100,"sect_master":1,"spirit_beasts":10000},
     "economy":{
        "spirit_stones":"vast","food":"self-sufficient","water":"self-sufficient",
        "alchemy":"immortal-grade","forging":"immortal-grade","formation":"immortal-grade",
        "trade":"continent-scale","recruitment":"continent-wide"},
     "politics":{
        "allies":["vassal immortal sects"],"enemies":["rival immortal sects"],
        "vassals":["entire mortal sects"],"rivals":["Dark Scorpion Clan"],
        "internal_factions":["sect master loyalists","elder council"]},
     "lifecycle":{
        "recruitment":"continent-wide examination",
        "expansion":"continent-scale",
        "decline":"none current",
        "schisms":"elder council tension",
        "wars":["immortal-sect wars"],
        "recovery":"strong; immortal sect master"},
     "libraries":["immortal techniques","soul cultivation","vast collections","continent lore"],
     "spirit_beasts":["immortal-grade beasts","soul beasts"],
     "canon_confidence":4},

    {"id":"dark_scorpion_clan","name":"Dark Scorpion Clan","type":"ancient_clan","location":"Immortal Astral Continent",
     "peak_realm":"Immortal (clan ancestor)","canon_status":"immortal_ancient_clan",
     "population_structure":{
        "outer_members":5000,"inner_members":1000,"core_members":100,
        "elders":30,"clan_ancestor":1,"spirit_beasts":500},
     "economy":{
        "spirit_stones":"vast","food":"self-sufficient","water":"self-sufficient",
        "alchemy":"ancient","forging":"ancient","formation":"ancient",
        "trade":"ancient-clan network","recruitment":"bloodline only"},
     "politics":{
        "allies":["other ancient clans"],"enemies":["Great Soul Sect (rival)"],
        "vassals":[],"rivals":["Great Soul Sect"],
        "internal_factions":["main bloodline","branch bloodlines"]},
     "lifecycle":{
        "recruitment":"bloodline only",
        "expansion":"slow; bloodline-constrained",
        "decline":"none current",
        "schisms":"bloodline disputes",
        "wars":["ancient-clan conflicts"],
        "recovery":"strong; immortal ancestor"},
     "libraries":["ancient scorpion techniques","bloodline catalogues","ancient lore"],
     "spirit_beasts":["dark scorpions (clan totem)","ancient-clan guardians"],
     "canon_confidence":4},

    {"id":"origin_sect","name":"Origin Sect","type":"sect","location":"Immortal Astral Continent",
     "peak_realm":"Immortal (sect master)","canon_status":"immortal_sect",
     "population_structure":{
        "outer_disciples":50000,"inner_disciples":10000,"core_disciples":1000,
        "elders":100,"peak_lords":50,"sect_master":1,"spirit_beasts":5000},
     "economy":{
        "spirit_stones":"vast","food":"self-sufficient","water":"self-sufficient",
        "alchemy":"immortal-grade","forging":"immortal-grade","formation":"immortal-grade",
        "trade":"continent-scale","recruitment":"continent-wide"},
     "politics":{
        "allies":["vassal sects"],"enemies":["rival immortal sects"],
        "vassals":["entire mortal sects"],"rivals":["other immortal sects"],
        "internal_factions":["sect master loyalists","elder council"]},
     "lifecycle":{
        "recruitment":"continent-wide examination",
        "expansion":"continent-scale",
        "decline":"none current",
        "schisms":"elder council tension",
        "wars":["immortal-sect wars"],
        "recovery":"strong; immortal sect master"},
     "libraries":["origin techniques","vast collections","continent lore"],
     "spirit_beasts":["immortal-grade beasts"],
     "canon_confidence":3},

    {"id":"xuan_wu_royal_court","name":"Xuan Wu Royal Court","type":"royal_court","location":"Xuan Wu",
     "peak_realm":"Ascendant (Xuan Wu Emperor)","canon_status":"continental_power",
     "population_structure":{
        "mortals":"millions","qi_condensation":"tens of thousands","foundation_establishment":"thousands",
        "core_formation":"hundreds","elders":50,"xuan_wu_emperor":1,"spirit_beasts":2000},
     "economy":{
        "spirit_stones":"abundant","food":"self-sufficient","water":"self-sufficient",
        "alchemy":"advanced","forging":"advanced","formation":"advanced",
        "trade":"continental","recruitment":"Xuan Wu continent"},
     "politics":{
        "allies":["earth-aspected sects"],"enemies":["foreign invaders"],
        "vassals":["Xuan Wu sects"],"rivals":["other continental powers"],
        "internal_factions":["royal family","minister council"]},
     "lifecycle":{
        "recruitment":"Xuan Wu nobility",
        "expansion":"continental",
        "decline":"none current",
        "schisms":"royal vs minister tension",
        "wars":["continental wars"],
        "recovery":"strong; Black Tortoise bloodline"},
     "libraries":["earth techniques","Xuan Wu formation manuals","earth beast catalogues","royal records"],
     "spirit_beasts":["black tortoise (divine, slumbering)","earth-aspected beasts"],
     "canon_confidence":4},

    {"id":"qilin_city_court","name":"Qilin City Court","type":"royal_court","location":"Qilin City",
     "peak_realm":"Ascendant (Qilin Emperor)","canon_status":"regional_power",
     "population_structure":{
        "mortals":"hundreds of thousands","qi_condensation":"thousands","foundation_establishment":"hundreds",
        "core_formation":"dozens","elders":20,"qilin_emperor":1,"spirit_beasts":500},
     "economy":{
        "spirit_stones":"abundant","food":"self-sufficient","water":"self-sufficient",
        "alchemy":"advanced","forging":"advanced","formation":"advanced",
        "trade":"regional","recruitment":"Qilin City region"},
     "politics":{
        "allies":["auspicious sects"],"enemies":["corrupt powers"],
        "vassals":["Qilin City sects"],"rivals":["other regional powers"],
        "internal_factions":["royal family","auspicious council"]},
     "lifecycle":{
        "recruitment":"Qilin City nobility",
        "expansion":"steady",
        "decline":"none current",
        "schisms":"royal vs council tension",
        "wars":["regional conflicts"],
        "recovery":"strong; Qilin bloodline"},
     "libraries":["auspicious techniques","Qilin formation manuals","auspicious beast catalogues","royal records"],
     "spirit_beasts":["qilins (tributed)","auspicious beasts"],
     "canon_confidence":4},

    {"id":"ancient_demon_city_court","name":"Ancient Demon City Court","type":"royal_court","location":"Ancient Demon City",
     "peak_realm":"Ascendant (Demon Emperor)","canon_status":"regional_demon_power",
     "population_structure":{
        "mortals":"hundreds of thousands","qi_condensation":"thousands","foundation_establishment":"hundreds",
        "core_formation":"dozens","elders":20,"demon_emperor":1,"spirit_beasts":800},
     "economy":{
        "spirit_stones":"abundant","food":"self-sufficient","water":"self-sufficient",
        "alchemy":"demon-focus","forging":"demon-focus","formation":"demon-focus",
        "trade":"demon goods","recruitment":"demon-aspected"},
     "politics":{
        "allies":["demon sects"],"enemies":["orthodox powers"],
        "vassals":["demon sects"],"rivals":["orthodox regional powers"],
        "internal_factions":["royal family","demon council"]},
     "lifecycle":{
        "recruitment":"demon-aspected nobility",
        "expansion":"steady",
        "decline":"none current",
        "schisms":"royal vs council tension",
        "wars":["perennial with orthodox"],
        "recovery":"strong; Demon Emperor"},
     "libraries":["demon techniques","demon formation manuals","demon beast catalogues","royal records"],
     "spirit_beasts":["demon beasts","demon mounts"],
     "canon_confidence":3},

    {"id":"soul_refining_tribe","name":"Soul Refining Tribe","type":"tribe","location":"Sea of Devils frontier",
     "peak_realm":"Nascent Soul (tribe chief)","canon_status":"frontier_devil_tribe",
     "population_structure":{
        "mortals":5000,"qi_condensation":500,"foundation_establishment":50,
        "core_formation":5,"elders":8,"tribe_chief":1,"spirit_beasts":200},
     "economy":{
        "spirit_stones":"minimal","food":"hunt","water":"well",
        "alchemy":"soul-focus","forging":"soul-focus","formation":"soul-focus",
        "trade":"black market","recruitment":"birth + fallen cultivators"},
     "politics":{
        "allies":["Soul Refining Sect"],"enemies":["orthodox frontier sects"],
        "vassals":[],"rivals":["other frontier tribes"],
        "internal_factions":["chief loyalists","elder council"]},
     "lifecycle":{
        "recruitment":"birth + fallen cultivators",
        "expansion":"slow",
        "decline":"slow; frontier pressure",
        "schisms":"chief vs elder tension",
        "wars":["frontier conflicts"],
        "recovery":"moderate; Soul Refining Sect backing"},
     "libraries":["soul techniques","tribe records","soul beast catalogues"],
     "spirit_beasts":["nether beasts","soul beasts"],
     "canon_confidence":3},

    {"id":"foreign_battleground_war_camps","name":"Foreign Battleground War Camps","type":"war_camps","location":"Foreign Battleground",
     "peak_realm":"Spirit Severing (war camp commander)","canon_status":"active_war_zone",
     "population_structure":{
        "soldiers":"tens of thousands","officers":"hundreds","commanders":12,"war_camp_commander":1,"spirit_beasts":1000},
     "economy":{
        "spirit_stones":"war-spoils","food":"supply lines","water":"supply lines",
        "alchemy":"war-focus","forging":"war-focus","formation":"war-focus",
        "trade":"war-spoils","recruitment":"conscription + volunteers"},
     "politics":{
        "allies":["home factions"],"enemies":["opposing war camps"],
        "vassals":[],"rivals":["rival war camps (same side)"],
        "internal_factions":["commander loyalists","officer council"]},
     "lifecycle":{
        "recruitment":"conscription + volunteers",
        "expansion":"war-driven",
        "decline":"war-driven",
        "schisms":"rival war camp tension",
        "wars":["active"],
        "recovery":"war-driven"},
     "libraries":["war techniques","battle records","war beast catalogues"],
     "spirit_beasts":["war beasts","white tiger (divine, appears at wars)"],
     "canon_confidence":4},

    {"id":"kunlun_immortal_palace","name":"Kunlun Immortal Palace","type":"immortal_palace","location":"Immortal Astral Continent core",
     "peak_realm":"Ancient Immortal (palace lord)","canon_status":"apex_immortal_power",
     "population_structure":{
        "immortals":"thousands","ancient_immortals":"dozens","palace_lord":1,"spirit_beasts":"vast"},
     "economy":{
        "spirit_stones":"infinite","food":"immortal","water":"immortal",
        "alchemy":"ancient-immortal-grade","forging":"ancient-immortal-grade","formation":"ancient-immortal-grade",
        "trade":"immortal","recruitment":"immortal examination"},
     "politics":{
        "allies":["all immortal sects (nominal)"],"enemies":["foreign immortal powers"],
        "vassals":["all immortal sects"],"rivals":["none on continent"],
        "internal_factions":["palace lord loyalists","ancient immortal council"]},
     "lifecycle":{
        "recruitment":"immortal examination",
        "expansion":"static (apex)",
        "decline":"none",
        "schisms":"none",
        "wars":["immortal wars (rare)"],
        "recovery":"immortal"},
     "libraries":["ancient-immortal techniques","all lore","infinite collections"],
     "spirit_beasts":["ancient-immortal beasts","divine beasts"],
     "canon_confidence":3},

    {"id":"xuan_dao_sect","name":"Xuan Dao Sect","type":"sect","location":"Chu Country",
     "peak_realm":"Spirit Severing (sect master)","canon_status":"major_chu_sect",
     "population_structure":{
        "outer_disciples":4000,"inner_disciples":700,"core_disciples":70,
        "elders":18,"peak_lords":9,"sect_master":1,"spirit_beasts":300},
     "economy":{
        "spirit_stones":"abundant","food":"self-sufficient","water":"self-sufficient",
        "alchemy":"moderate","forging":"moderate","formation":"moderate",
        "trade":"Chu Country","recruitment":"Chu Country"},
     "politics":{
        "allies":["Chu royal family"],"enemies":["rival Chu sects"],
        "vassals":[],"rivals":["Cloud Sky Sect"],
        "internal_factions":["main lineage","branch lineages"]},
     "lifecycle":{
        "recruitment":"annual examination",
        "expansion":"steady",
        "decline":"none current",
        "schisms":"branch lineage tension",
        "wars":["border conflicts"],
        "recovery":"strong"},
     "libraries":["intermediate techniques","Xuan Dao formation manuals","spirit beast catalogues"],
     "spirit_beasts":["mounts","guardian beasts"],
     "canon_confidence":4},

    {"id":"wang_family_sect","name":"Wang Family Sect","type":"family_sect","location":"Immortal Astral Continent",
     "peak_realm":"Immortal (family ancestor - Wang Lin)","canon_status":"wang_lin_lineage",
     "population_structure":{
        "outer_disciples":10000,"inner_disciples":2000,"core_disciples":200,
        "elders":50,"family_ancestor":1,"spirit_beasts":2000},
     "economy":{
        "spirit_stones":"vast","food":"self-sufficient","water":"self-sufficient",
        "alchemy":"immortal-grade","forging":"immortal-grade","formation":"immortal-grade",
        "trade":"continent-scale","recruitment":"Wang bloodline + examination"},
     "politics":{
        "allies":["vassal sects"],"enemies":["Wang Lin's enemies"],
        "vassals":["mortal sects"],"rivals":["other immortal families"],
        "internal_factions":["main bloodline","branch bloodlines"]},
     "lifecycle":{
        "recruitment":"bloodline + examination",
        "expansion":"steady",
        "decline":"none (Wang Lin immortal)",
        "schisms":"bloodline disputes",
        "wars":["Wang Lin's enemies"],
        "recovery":"divine; Wang Lin can intervene"},
     "libraries":["Wang Lin's techniques","inheritance","vast collections"],
     "spirit_beasts":["Wang Lin's legacy beasts","mosquito swarm (legacy)"],
     "canon_confidence":4},

    {"id":"seven_colored_realm_court","name":"Seven-Colored Realm Court","type":"secret_realm_court","location":"Seven-Colored Realm",
     "peak_realm":"Immortal (realm lord)","canon_status":"secret_realm_power",
     "population_structure":{
        "cultivators":"thousands","elders":20,"realm_lord":1,"spirit_beasts":500},
     "economy":{
        "spirit_stones":"abundant","food":"self-sufficient","water":"self-sufficient",
        "alchemy":"advanced","forging":"advanced","formation":"advanced",
        "trade":"secret-realm goods","recruitment":"examination"},
     "politics":{
        "allies":[],"enemies":["invaders"],
        "vassals":[],"rivals":[],
        "internal_factions":["realm lord loyalists"]},
     "lifecycle":{
        "recruitment":"examination",
        "expansion":"static (secret realm)",
        "decline":"none current",
        "schisms":"none",
        "wars":["defense against invaders"],
        "recovery":"strong; realm lord"},
     "libraries":["seven-colored techniques","realm lore","secret-realm collections"],
     "spirit_beasts":["seven-colored beasts"],
     "canon_confidence":3},

    {"id":"kunxu_realm_court","name":"Kunxu Realm Court","type":"secret_realm_court","location":"Kunxu Realm",
     "peak_realm":"Ascendant (realm lord)","canon_status":"secret_realm_power",
     "population_structure":{
        "cultivators":"thousands","elders":15,"realm_lord":1,"spirit_beasts":300},
     "economy":{
        "spirit_stones":"abundant","food":"self-sufficient","water":"self-sufficient",
        "alchemy":"advanced","forging":"advanced","formation":"advanced",
        "trade":"secret-realm goods","recruitment":"examination"},
     "politics":{
        "allies":[],"enemies":["invaders"],
        "vassals":[],"rivals":[],
        "internal_factions":["realm lord loyalists"]},
     "lifecycle":{
        "recruitment":"examination",
        "expansion":"static (secret realm)",
        "decline":"none current",
        "schisms":"none",
        "wars":["defense against invaders"],
        "recovery":"strong; realm lord"},
     "libraries":["Kunxu techniques","realm lore","secret-realm collections"],
     "spirit_beasts":["Kunxu beasts"],
     "canon_confidence":3},
]

def pass_16_civilizations(canon):
    """Pass 16: Civilization Simulation Definitions."""
    civ_dir = out("civilizations")
    count = 0
    for c in CIVILIZATIONS:
        f = civ_dir / f"{c['id']}.json"
        if f.exists():
            continue
        doc = {
            "_comment": f"Civilization: {c['name']} ({c['type']}) at {c['location']}. "
                         f"Per civilization directive: sects are living organisms. "
                         f"Simulates population, economy, politics, lifecycle. "
                         f"Prime Directive: civilization exists objectively; "
                         f"player enters a world already in motion.",
            "civilization_id": c["id"],
            "name": c["name"],
            "type": c["type"],
            "location": c["location"],
            "peak_realm": c["peak_realm"],
            "canon_status": c["canon_status"],
            "derivation_type": "A" if c["canon_confidence"] >= 4 else "B",
            "canon_confidence": c["canon_confidence"],
            "population_structure": c["population_structure"],
            "economy": c["economy"],
            "politics": c["politics"],
            "lifecycle": c["lifecycle"],
            "libraries": c["libraries"],
            "spirit_beasts": c["spirit_beasts"],
            "simulation_notes": {
                "living_organism": "Population, economy, and politics shift over time; "
                                   "the civilization changes whether or not the player acts.",
                "lifecycle_phases": "recruitment -> expansion -> (decline OR stability) -> "
                                    "(schisms OR unity) -> (wars OR peace) -> recovery",
                "perception_tiers": {
                    "mortal": "Sees the sect's outer face.",
                    "qi_condensation": "Sees population structure.",
                    "foundation_establishment": "Perceives economy and politics.",
                    "core_formation": "Perceives lifecycle phase and internal factions.",
                    "nascent_soul": "Perceives complete civilization as a living system.",
                }
            },
            "salt": salt_for(c["id"]),
        }
        write_json(f, doc)
        count += 1
    return count

# ==================================================================
# PASS 17 - OPPORTUNITY GENERATION TABLES
# ==================================================================
# Engine continuously asks: "If this location has existed for 80,000 years,
# what opportunities naturally exist?" Then opportunities become discoverable
# by cultivation, perception, and luck.

OPPORTUNITY_ARCHETYPES = [
    {"id":"forgotten_inheritance","name":"Forgotten Inheritance",
     "description":"A cultivator's inheritance left for a successor, sealed in a cave or formation. "
                    "Exists because: a cultivator reached end-of-life and chose to leave their legacy.",
     "age_requirement_years":1000,
     "location_types":["ancient_cave","sealed_formation","abandoned_peak","underground_chamber"],
     "cultivation_required":"Foundation Establishment",
     "perception_required":"Core Formation (to find the seal)",
     "luck_modifier":1.0,
     "discovery_methods":["divine_sense_sweep","formation_breaking","accidental_stumble","rumor_following"],
     "reward_tables":["technique_scroll","treasure_inheritance","cultivation_base_transfer","spirit_vein_ownership"],
     "why_untaken":"Sealed by the cultivator's formation; requires specific cultivation/perception to find.",
     "natural_next_event":"If unclaimed for 10000+ years, the seal weakens and beasts claim it.",
     "canon_confidence":5},

    {"id":"broken_formation","name":"Broken Formation",
     "description":"An ancient formation that has partially collapsed, leaving gaps. "
                    "Exists because: formations degrade over millennia; parts still hold power.",
     "age_requirement_years":5000,
     "location_types":["ancient_battlefield","ruined_sect","ancient_altar","collapsed_realm"],
     "cultivation_required":"Core Formation",
     "perception_required":"Nascent Soul (to perceive the formation's logic)",
     "luck_modifier":1.2,
     "discovery_methods":["formation_study","divine_sense","rumor_following"],
     "reward_tables":["formation_fragment","formation_knowledge","trapped_treasure","formation_core"],
     "why_untaken":"Requires formation expertise to safely navigate; most cultivators avoid broken formations.",
     "natural_next_event":"Formation continues to degrade; eventually collapses entirely.",
     "canon_confidence":4},

    {"id":"sealed_cave","name":"Sealed Cave",
     "description":"A cave sealed by an ancient cultivator, containing treasures or a sleeping beast. "
                    "Exists because: a cultivator sealed something away for later retrieval or to protect it.",
     "age_requirement_years":3000,
     "location_types":["mountain_cave","underground_cave","cliff_cave","ancient_mine"],
     "cultivation_required":"Core Formation",
     "perception_required":"Nascent Soul (to perceive the seal)",
     "luck_modifier":1.1,
     "discovery_methods":["divine_sense","seal_breaking","rumor_following"],
     "reward_tables":["sealed_treasure","sleeping_beast_core","ancient_herb","cultivation_manual"],
     "why_untaken":"Sealed; requires specific cultivation or seal-breaking technique.",
     "natural_next_event":"Seal weakens over time; eventually breaks naturally.",
     "canon_confidence":5},

    {"id":"sleeping_beast","name":"Sleeping Beast",
     "description":"An ancient beast in deep cultivation slumber. "
                    "Exists because: beasts of sufficient age enter slumber to cultivate.",
     "age_requirement_years":10000,
     "location_types":["ancient_cave","spirit_vein_core","deep_ocean","high_peak"],
     "cultivation_required":"Nascent Soul (to survive encounter)",
     "perception_required":"Nascent Soul (to perceive the beast before waking it)",
     "luck_modifier":0.8,
     "discovery_methods":["divine_sense","accidental_stumble","rumor_following"],
     "reward_tables":["beast_core","beast_blood_essence","beast_pelt","beast_treasure"],
     "why_untaken":"Too dangerous; most cultivators flee sleeping beasts.",
     "natural_next_event":"Beast wakes naturally when cultivation completes or disturbance occurs.",
     "canon_confidence":5},

    {"id":"abandoned_library","name":"Abandoned Library",
     "description":"A library of an extinct sect, containing rare techniques and lore. "
                    "Exists because: a sect fell and its library was left behind.",
     "age_requirement_years":2000,
     "location_types":["ruined_sect","ancient_city","underground_vault","sealed_chamber"],
     "cultivation_required":"Foundation Establishment",
     "perception_required":"Core Formation (to find hidden sections)",
     "luck_modifier":1.3,
     "discovery_methods":["exploration","rumor_following","formation_breaking"],
     "reward_tables":["technique_scroll","formation_manual","alchemy_recipe","spirit_beast_catalogue","historical_record"],
     "why_untaken":"Location lost; requires exploration or rumor to find.",
     "natural_next_event":"Library degrades; scrolls rot; knowledge lost.",
     "canon_confidence":4},

    {"id":"dead_cultivator","name":"Dead Cultivator",
     "description":"The corpse of a cultivator who fell in the wilderness, still holding treasures. "
                    "Exists because: cultivators die in the wild and are not always found.",
     "age_requirement_years":100,
     "location_types":["wilderness","ancient_battlefield","cave","ruin"],
     "cultivation_required":"Qi Condensation",
     "perception_required":"Foundation Establishment (to find the corpse)",
     "luck_modifier":1.5,
     "discovery_methods":["divine_sense","accidental_stumble","rumor_following"],
     "reward_tables":["storage_pouch","cultivation_manual","treasure","spirit_stones"],
     "why_untaken":"Corpse hidden by terrain; requires perception to find.",
     "natural_next_event":"Corpse rots; treasures scatter; beasts claim the remains.",
     "canon_confidence":4},

    {"id":"spirit_vein_collapse","name":"Spirit Vein Collapse",
     "description":"A spirit vein in the process of collapsing, releasing its essence. "
                    "Exists because: spirit veins have lifespans; collapse releases vast essence.",
     "age_requirement_years":50000,
     "location_types":["spirit_vein_core","ancient_mine","underground"],
     "cultivation_required":"Core Formation",
     "perception_required":"Nascent Soul (to perceive the collapse)",
     "luck_modifier":0.7,
     "discovery_methods":["divine_sense","earth_tremor","rumor_following"],
     "reward_tables":["spirit_stone_burst","vein_essence","rare_ore","ancient_herb_bloom"],
     "why_untaken":"Collapse is rare and brief; requires timing.",
     "natural_next_event":"Collapse completes; essence disperses; beasts converge.",
     "canon_confidence":3},

    {"id":"hidden_medicinal_valley","name":"Hidden Medicinal Valley",
     "description":"A valley hidden by formation or terrain, rich in spirit herbs. "
                    "Exists because: a cultivator or nature created a protected herb garden.",
     "age_requirement_years":1000,
     "location_types":["hidden_valley","formation_sealed_valley","mountain_valley"],
     "cultivation_required":"Foundation Establishment",
     "perception_required":"Core Formation (to perceive the formation)",
     "luck_modifier":1.4,
     "discovery_methods":["divine_sense","formation_breaking","rumor_following"],
     "reward_tables":["rare_herb","ancient_herb","spirit_seed","herb_garden_ownership"],
     "why_untaken":"Hidden by formation; requires perception to find.",
     "natural_next_event":"Herbs bloom seasonally; valley remains hidden.",
     "canon_confidence":4},

    {"id":"collapsed_secret_realm","name":"Collapsed Secret Realm",
     "description":"A secret realm that has partially collapsed, exposing its contents. "
                    "Exists because: secret realms are unstable and can collapse.",
     "age_requirement_years":10000,
     "location_types":["secret_realm_edge","reality_fault","void_rift"],
     "cultivation_required":"Nascent Soul",
     "perception_required":"Spirit Severing (to navigate the collapse)",
     "luck_modifier":0.6,
     "discovery_methods":["divine_sense","reality_fault_navigation","rumor_following"],
     "reward_tables":["realm_treasure","realm_essence","ancient_inheritance","realm_beast_core"],
     "why_untaken":"Extremely dangerous; collapse can kill Spirit Severing cultivators.",
     "natural_next_event":"Collapse completes; realm lost.",
     "canon_confidence":3},

    {"id":"ancient_battlefield_opportunity","name":"Ancient Battlefield Opportunity",
     "description":"An ancient battlefield saturated with death-qi and fallen cultivator treasures. "
                    "Exists because: ancient wars killed millions; their treasures remain.",
     "age_requirement_years":10000,
     "location_types":["ancient_battlefield","mass_grave","war_ruin"],
     "cultivation_required":"Core Formation",
     "perception_required":"Nascent Soul (to perceive treasures among death-qi)",
     "luck_modifier":1.0,
     "discovery_methods":["divine_sense","rumor_following","scavenging"],
     "reward_tables":["fallen_treasure","war_technique","death_essence","ancient_weapon"],
     "why_untaken":"Death-qi corrupts; requires cultivation to survive.",
     "natural_next_event":"Death-qi disperses over millennia; beasts claim the battlefield.",
     "canon_confidence":5},

    {"id":"lost_sword_intent","name":"Lost Sword Intent",
     "description":"Residual sword intent left by a fallen sword cultivator. "
                    "Exists because: sword intent can persist after the cultivator's death.",
     "age_requirement_years":1000,
     "location_types":["sword_peak","ancient_battlefield","sword_cave","sword_lake"],
     "cultivation_required":"Core Formation (sword focus)",
     "perception_required":"Nascent Soul (sword focus, to perceive the intent)",
     "luck_modifier":1.1,
     "discovery_methods":["sword_cultivation","divine_sense","rumor_following"],
     "reward_tables":["sword_intent_fragment","sword_technique","sword_treasure","sword_comprehension"],
     "why_untaken":"Requires sword focus to perceive; most cultivators cannot.",
     "natural_next_event":"Intent disperses over millennia; eventually fades.",
     "canon_confidence":4},

    {"id":"residual_divine_sense","name":"Residual Divine Sense",
     "description":"A fragment of a fallen cultivator's divine sense, holding memories or knowledge. "
                    "Exists because: powerful divine sense can persist after death.",
     "age_requirement_years":500,
     "location_types":["ancient_cave","fallen_cultivator_site","ancient_altar"],
     "cultivation_required":"Nascent Soul",
     "perception_required":"Spirit Severing (to perceive the fragment)",
     "luck_modifier":0.9,
     "discovery_methods":["divine_sense_sweep","rumor_following"],
     "reward_tables":["memory_fragment","technique_knowledge","cultivation_tip","historical_record"],
     "why_untaken":"Requires Spirit Severing perception; rare.",
     "natural_next_event":"Fragment disperses; eventually fades.",
     "canon_confidence":3},

    {"id":"dao_fragment","name":"Dao Fragment",
     "description":"A fragment of natural law, condensed by ancient events. "
                    "Exists because: powerful events can condense law fragments.",
     "age_requirement_years":100000,
     "location_types":["tribulation_site","ancient_battlefield","reality_fault","secret_realm_core"],
     "cultivation_required":"Spirit Severing",
     "perception_required":"Ascendant (to perceive the fragment)",
     "luck_modifier":0.5,
     "discovery_methods":["dao_comprehension","divine_sense","rumor_following"],
     "reward_tables":["dao_fragment","law_comprehension","essence_fragment","breakthrough_opportunity"],
     "why_untaken":"Requires Ascendant perception; extremely rare.",
     "natural_next_event":"Fragment disperses; eventually reabsorbed by heaven.",
     "canon_confidence":3},

    {"id":"tribulation_aftermath","name":"Tribulation Aftermath",
     "description":"The site of a recent heavenly tribulation, saturated with residual lightning. "
                    "Exists because: tribulations leave residual energy.",
     "age_requirement_years":0,
     "location_types":["tribulation_site","burned_peak","glassed_ground"],
     "cultivation_required":"Core Formation",
     "perception_required":"Nascent Soul (to perceive the residual energy)",
     "luck_modifier":1.2,
     "discovery_methods":["divine_sense","rumor_following","lightning_tracking"],
     "reward_tables":["tribulation_fragment","lightning_essence","thunder_herb","tribulation_scarred_beast"],
     "why_untaken":"Site is dangerous; residual lightning can kill.",
     "natural_next_event":"Residual energy disperses; site returns to normal.",
     "canon_confidence":4},

    {"id":"ancient_tomb_undisturbed","name":"Ancient Tomb (Undisturbed)",
     "description":"An ancient cultivator's tomb, undisturbed since burial. "
                    "Exists because: powerful cultivators are buried with their treasures.",
     "age_requirement_years":5000,
     "location_types":["ancient_tomb","sealed_burial","mountain_tomb"],
     "cultivation_required":"Nascent Soul",
     "perception_required":"Spirit Severing (to perceive the tomb's traps)",
     "luck_modifier":0.8,
     "discovery_methods":["divine_sense","rumor_following","tomb_robbing"],
     "reward_tables":["burial_treasure","ancient_technique","preserved_corpse","tomb_guardian_core"],
     "why_untaken":"Tomb traps kill; requires Spirit Severing to survive.",
     "natural_next_event":"Tomb degrades; eventually collapses.",
     "canon_confidence":5},

    {"id":"vein_essence_pool","name":"Vein Essence Pool",
     "description":"A pool of condensed spirit-vein essence, naturally forming at vein cores. "
                    "Exists because: spirit veins condense essence over millennia.",
     "age_requirement_years":10000,
     "location_types":["spirit_vein_core","underground_spring","vein_core_cave"],
     "cultivation_required":"Core Formation",
     "perception_required":"Nascent Soul (to perceive the pool)",
     "luck_modifier":1.0,
     "discovery_methods":["divine_sense","vein_tracking","rumor_following"],
     "reward_tables":["vein_essence","spirit_stone_burst","rare_ore","vein_ownership"],
     "why_untaken":"Vein cores are guarded by vein-guardian beasts.",
     "natural_next_event":"Pool overflows; essence disperses; beasts converge.",
     "canon_confidence":3},

    {"id":"fallen_beast_territory","name":"Fallen Beast Territory",
     "description":"A territory left vacant by a fallen apex beast, rich in treasures. "
                    "Exists because: apex beasts hoard treasures; their fall leaves the hoard.",
     "age_requirement_years":100,
     "location_types":["beast_lair","ancient_peak","deep_cave"],
     "cultivation_required":"Core Formation",
     "perception_required":"Nascent Soul (to perceive the hoard)",
     "luck_modifier":1.1,
     "discovery_methods":["divine_sense","beast_tracking","rumor_following"],
     "reward_tables":["beast_hoard","beast_core","beast_treasure","territory_ownership"],
     "why_untaken":"Other beasts claim the territory quickly.",
     "natural_next_event":"Other beasts claim the territory; hoard scattered.",
     "canon_confidence":4},

    {"id":"ancient_formation_node","name":"Ancient Formation Node",
     "description":"A node of an ancient formation network, holding condensed formation power. "
                    "Exists because: ancient formation networks span continents; nodes persist.",
     "age_requirement_years":20000,
     "location_types":["formation_node","ancient_altar","mountain_peak","continent_core"],
     "cultivation_required":"Nascent Soul",
     "perception_required":"Spirit Severing (to perceive the node's logic)",
     "luck_modifier":0.9,
     "discovery_methods":["formation_study","divine_sense","rumor_following"],
     "reward_tables":["formation_node_essence","formation_knowledge","formation_fragment","network_access"],
     "why_untaken":"Requires formation expertise; nodes are hidden.",
     "natural_next_event":"Node degrades; eventually fails.",
     "canon_confidence":3},

    {"id":"blood_lotus_bloom","name":"Blood Lotus Bloom",
     "description":"A blood lotus blooming at a site of mass death, condensing death-essence. "
                    "Exists because: blood lotuses grow from accumulated death-qi.",
     "age_requirement_years":500,
     "location_types":["mass_grave","ancient_battlefield","execution_ground"],
     "cultivation_required":"Core Formation",
     "perception_required":"Nascent Soul (to perceive the lotus)",
     "luck_modifier":1.0,
     "discovery_methods":["divine_sense","rumor_following","death_qi_tracking"],
     "reward_tables":["blood_lotus","death_essence","blood_herb","blood_attribute_treasure"],
     "why_untaken":"Site is death-saturated; requires cultivation to survive.",
     "natural_next_event":"Lotus blooms once per century; brief window.",
     "canon_confidence":3},

    {"id":"void_fissure_treasure","name":"Void Fissure Treasure",
     "description":"A treasure drawn into a void fissure, occasionally spat back out. "
                    "Exists because: void fissures absorb and release objects unpredictably.",
     "age_requirement_years":0,
     "location_types":["void_fissure","reality_fault","void_edge"],
     "cultivation_required":"Spirit Severing",
     "perception_required":"Ascendant (to perceive the fissure's cycles)",
     "luck_modifier":0.4,
     "discovery_methods":["void_cultivation","divine_sense","fissure_monitoring"],
     "reward_tables":["void_treasure","void_essence","reality_fragment","lost_inheritance"],
     "why_untaken":"Extremely dangerous; fissures can kill Ascendant cultivators.",
     "natural_next_event":"Fissure closes; treasure lost forever.",
     "canon_confidence":2},
]

def pass_17_opportunities(canon):
    """Pass 17: Opportunity Generation Tables."""
    opp_dir = out("opportunities")
    count = 0
    for o in OPPORTUNITY_ARCHETYPES:
        f = opp_dir / f"{o['id']}.json"
        if f.exists():
            continue
        doc = {
            "_comment": f"Opportunity: {o['name']}. "
                         f"Per opportunity directive: the engine continuously asks "
                         f"'what opportunities naturally exist at a location that has existed for {o['age_requirement_years']} years?'. "
                         f"Discoverable by cultivation ({o['cultivation_required']}), "
                         f"perception ({o['perception_required']}), and luck (modifier {o['luck_modifier']}). "
                         f"Prime Directive: opportunity exists objectively; "
                         f"player perception determines whether they find it.",
            "opportunity_id": o["id"],
            "name": o["name"],
            "description": o["description"],
            "derivation_type": "A" if o["canon_confidence"] >= 4 else "B",
            "canon_confidence": o["canon_confidence"],
            "age_requirement_years": o["age_requirement_years"],
            "location_types": o["location_types"],
            "cultivation_required": o["cultivation_required"],
            "perception_required": o["perception_required"],
            "luck_modifier": o["luck_modifier"],
            "discovery_methods": o["discovery_methods"],
            "reward_tables": o["reward_tables"],
            "why_untaken": o["why_untaken"],
            "natural_next_event": o["natural_next_event"],
            "simulation_notes": {
                "continuous_inference": "The World State Engine continuously infers opportunities at every location "
                                        "based on the location's age, type, and history.",
                "no_locked_upgrades": "A cultivator who later reaches the required perception will find opportunities "
                                      "they previously missed; the opportunities do not disappear.",
                "canon_fidelity": "This is how Wang Lin found inheritances throughout his journey — "
                                  "the opportunities existed before he arrived; his perception determined what he found.",
            },
            "salt": salt_for(o["id"]),
        }
        write_json(f, doc)
        count += 1
    return count

# ==================================================================
# PASS 18 - TIME-SYSTEM EVENT DEFINITIONS
# ==================================================================
# The world is not static. Spirit herbs grow, beasts migrate, veins shift,
# forests burn, wars happen, sect leaders die, markets move, etc.
# Player returns 200 years later and barely recognizes it.

TIME_EVENTS = [
    {"id":"spirit_herb_growth","name":"Spirit Herb Growth","trigger":"seasonal",
     "scope":"local","duration_days":90,"effects":["herb_tier_rises","herb_population_increases"],
     "canon_confidence":4,"note":"Herbs grow seasonally; ancient herbs take centuries."},
    {"id":"spirit_herb_bloom","name":"Spirit Herb Bloom","trigger":"centennial",
     "scope":"local","duration_days":7,"effects":["rare_herb_appears","beasts_converge"],
     "canon_confidence":4,"note":"Rare herbs bloom once per century; brief window."},
    {"id":"beast_migration","name":"Beast Migration","trigger":"seasonal",
     "scope":"regional","duration_days":60,"effects":["beast_population_shifts","markets_appear","caravans_move"],
     "canon_confidence":4,"note":"Beasts migrate seasonally; see migration routes."},
    {"id":"spirit_vein_shift","name":"Spirit Vein Shift","trigger":"decennial",
     "scope":"regional","duration_days":365,"effects":["vein_moves","ecology_shifts","territory_changes"],
     "canon_confidence":3,"note":"Spirit veins shift slowly over decades."},
    {"id":"forest_fire","name":"Forest Fire","trigger":"random",
     "scope":"local","duration_days":7,"effects":["forest_destroyed","herbs_lost","beasts_flee","ash_fertilizes"],
     "canon_confidence":3,"note":"Random forest fires; can be caused by tribulation or cultivators."},
    {"id":"sect_war","name":"Sect War","trigger":"political",
     "scope":"regional","duration_days":365,"effects":["casualties","territory_changes","treasures_redistribute","economy_disrupts"],
     "canon_confidence":5,"note":"Wars between sects; reshape regional politics."},
    {"id":"sect_leader_death","name":"Sect Leader Death","trigger":"age_or_war",
     "scope":"regional","duration_days":90,"effects":["succession_crisis","internal_faction_struggle","power_vacuum"],
     "canon_confidence":5,"note":"Sect leaders die; succession crises follow."},
    {"id":"market_movement","name":"Market Movement","trigger":"seasonal",
     "scope":"regional","duration_days":30,"effects":["markets_relocate","trade_routes_shift"],
     "canon_confidence":3,"note":"Markets move seasonally along migration routes."},
    {"id":"caravan_disappearance","name":"Caravan Disappearance","trigger":"random",
     "scope":"regional","duration_days":1,"effects":["trade_disrupts","bandits_blamed","investigations"],
     "canon_confidence":3,"note":"Caravans disappear; causes vary (bandits, beasts, void fissures)."},
    {"id":"river_course_change","name":"River Course Change","trigger":"centennial",
     "scope":"regional","duration_days":30,"effects":["terrain_changes","cities_relocate","ecology_shifts"],
     "canon_confidence":3,"note":"Rivers change course over centuries."},
    {"id":"spirit_vein_exhaustion","name":"Spirit Vein Exhaustion","trigger":"millennial",
     "scope":"regional","duration_days":3650,"effects":["vein_dies","ecology_collapses","territory_abandoned"],
     "canon_confidence":4,"note":"Spirit veins exhaust over millennia."},
    {"id":"cave_collapse","name":"Cave Collapse","trigger":"random",
     "scope":"local","duration_days":1,"effects":["cave_lost","treasures_buried","beasts_killed"],
     "canon_confidence":3,"note":"Caves collapse; can bury treasures or beasts."},
    {"id":"ancient_ruin_opening","name":"Ancient Ruin Opening","trigger":"decennial",
     "scope":"regional","duration_days":30,"effects":["ruin_accessible","cultivators_converge","treasures_found"],
     "canon_confidence":5,"note":"Ancient ruins open periodically; cultivators rush to claim treasures."},
    {"id":"tribulation_scarring","name":"Tribulation Scarring","trigger":"tribulation",
     "scope":"local","duration_days":1,"effects":["terrain_scarred","residual_lightning","beasts_mutate"],
     "canon_confidence":4,"note":"Tribulations scar the landscape; residual lightning lingers."},
    {"id":"blood_moon","name":"Blood Moon","trigger":"centennial",
     "scope":"global","duration_days":1,"effects":["beast_frenzy","devil_empowerment","mutations_likely"],
     "canon_confidence":4,"note":"Blood moon once per century; all blood-attribute beings empowered."},
    {"id":"era_defining_event","name":"Era-Defining Event","trigger":"millennial",
     "scope":"global","duration_days":365,"effects":["divine_beast_appearance","great_cultivator_breakthrough","world_shifts"],
     "canon_confidence":5,"note":"Era-defining events: divine beast appearance, great cultivator breakthrough, etc."},
    {"id":"void_fissure_event","name":"Void Fissure Event","trigger":"random",
     "scope":"regional","duration_days":90,"effects":["reality_tears","void_beasts_hunt","treasures_redistribute"],
     "canon_confidence":3,"note":"Void fissures open randomly; void beasts hunt."},
    {"id":"secret_realm_collapse","name":"Secret Realm Collapse","trigger":"millennial",
     "scope":"regional","duration_days":365,"effects":["realm_lost","treasures_scatter","reality_warps"],
     "canon_confidence":3,"note":"Secret realms collapse over millennia."},
    {"id":"ancient_god_stirring","name":"Ancient God Stirring","trigger":"era",
     "scope":"global","duration_days":3650,"effects":["god_essence_rises","ecology_restructures","guardian_beasts_manifest"],
     "canon_confidence":4,"note":"Ancient gods stir in their slumber; reality warps."},
    {"id":"inheritance_appearance","name":"Inheritance Appearance","trigger":"random",
     "scope":"regional","duration_days":30,"effects":["inheritance_accessible","cultivators_converge","wars_likely"],
     "canon_confidence":5,"note":"Inheritances appear when seals weaken; cultivators converge."},
    {"id":"spirit_spring_birth","name":"Spirit Spring Birth","trigger":"decennial",
     "scope":"local","duration_days":90,"effects":["spring_appears","herbs_bloom","beasts_converge"],
     "canon_confidence":3,"note":"Spirit springs birth naturally; create new ecologies."},
    {"id":"spirit_spring_drying","name":"Spirit Spring Drying","trigger":"centennial",
     "scope":"local","duration_days":365,"effects":["spring_dries","ecology_collapses","territory_abandoned"],
     "canon_confidence":3,"note":"Spirit springs dry over centuries."},
    {"id":"bandit_uprising","name":"Bandit Uprising","trigger":"political",
     "scope":"regional","duration_days":180,"effects":["trade_disrupts","caravans_attacked","sect_response"],
     "canon_confidence":3,"note":"Bandit uprisings; disrupt trade."},
    {"id":"demon_invasion","name":"Demon Invasion","trigger":"era",
     "scope":"regional","duration_days":365,"effects":["devils_invade","sect_response","casualties","territory_changes"],
     "canon_confidence":4,"note":"Demon invasions; major conflicts."},
    {"id":"cultivator_breakthrough","name":"Cultivator Breakthrough","trigger":"random",
     "scope":"regional","duration_days":1,"effects":["tribulation","power_shift","faction_strengthens"],
     "canon_confidence":5,"note":"Cultivator breakthroughs; reshape faction power."},
    {"id":"formation_decay","name":"Formation Decay","trigger":"millennial",
     "scope":"regional","duration_days":3650,"effects":["formations_weaken","sealed_things_escape","treasures_exposed"],
     "canon_confidence":3,"note":"Formations decay over millennia."},
    {"id":"new_vein_birth","name":"New Vein Birth","trigger":"decennial",
     "scope":"regional","duration_days":365,"effects":["vein_appears","ecology_forms","territory_contested"],
     "canon_confidence":3,"note":"New spirit veins birth naturally; create new contested territories."},
    {"id":"beast_tide","name":"Beast Tide","trigger":"random",
     "scope":"regional","duration_days":30,"effects":["beasts_swarm","cities_threatened","sect_response"],
     "canon_confidence":4,"note":"Beast tides; mass beast migrations that threaten cities."},
    {"id":"immortal_descent","name":"Immortal Descent","trigger":"era",
     "scope":"global","duration_days":90,"effects":["immortal_visits","power_shifts","era_defines"],
     "canon_confidence":4,"note":"Immortals descend rarely; reshape eras."},
    {"id":"karmic_settlement","name":"Karmic Settlement","trigger":"era",
     "scope":"global","duration_days":1,"effects":["karma_resolves","beings_fall_or_rise","world_shifts"],
     "canon_confidence":3,"note":"Karmic settlements; rare era-defining events."},
]

def pass_18_time_events(canon):
    """Pass 18: Time-System Event Definitions."""
    ev_dir = out("time_events")
    count = 0
    for e in TIME_EVENTS:
        f = ev_dir / f"{e['id']}.json"
        if f.exists():
            continue
        doc = {
            "_comment": f"Time-Event: {e['name']}. "
                         f"Per time directive: the world is not static. "
                         f"Trigger: {e['trigger']}, scope: {e['scope']}, duration: {e['duration_days']} days. "
                         f"Prime Directive: event occurs objectively; "
                         f"player perception determines whether they witness it.",
            "event_id": e["id"],
            "name": e["name"],
            "trigger": e["trigger"],
            "scope": e["scope"],
            "duration_days": e["duration_days"],
            "effects": e["effects"],
            "derivation_type": "A" if e["canon_confidence"] >= 4 else "B",
            "canon_confidence": e["canon_confidence"],
            "note": e["note"],
            "simulation_notes": {
                "world_breathes": "The world changes whether or not the player acts; "
                                  "a player who returns 200 years later will barely recognize it.",
                "trigger_types": {
                    "seasonal": "occurs every season",
                    "decennial": "occurs every decade",
                    "centennial": "occurs every century",
                    "millennial": "occurs every millennium",
                    "era": "occurs every era (10000+ years)",
                    "random": "occurs randomly",
                    "political": "triggered by political events",
                    "tribulation": "triggered by tribulations",
                },
                "perception_tiers": {
                    "mortal": "Witnesses only local effects.",
                    "qi_condensation": "Senses the event occurring.",
                    "foundation_establishment": "Perceives the event's scope and effects.",
                    "core_formation": "Predicts the event's consequences.",
                    "nascent_soul": "Perceives the event's place in the larger pattern.",
                }
            },
            "salt": salt_for(e["id"]),
        }
        write_json(f, doc)
        count += 1
    return count

# ==================================================================
# PASS 19 - ITEM PHYSICAL PROPERTIES (LORE-ACCURATE SIZE/WEIGHT)
# ==================================================================
# Every object has physical size (not block size, actual lore size).
# Length, weight, material density, suitable realm, body strength required,
# effect when undersized. Example: 4.2m sword, 18400 jin, Spirit Steel,
# Soul Formation suitable, Foundation Establishment body -> barely lift,
# functions as heavy physical weapon, can't activate spiritual properties.

ITEM_PHYSICAL_PROPERTIES = [
    {"id":"heaven_defying_bead","name":"Heaven-Defying Bead / Pearl","type":"bead",
     "length_m":0.03,"weight_jin":0.5,"material_density":"condensed heaven-defying essence",
     "suitable_realm":"Heaven Trampling","body_strength_required":"Ascendant",
     "effect_when_undersized":"Cannot activate; bead remains inert.",
     "canon_note":"Wang Lin's defining treasure; a bead that defies heaven itself."},
    {"id":"god_slaying_sword","name":"God-Slaying Sword","type":"sword",
     "length_m":4.2,"weight_jin":18400,"material_density":"Spirit Steel",
     "suitable_realm":"Soul Formation (Spirit Severing)","body_strength_required":"Foundation Establishment",
     "effect_when_undersized":"Can barely lift with both hands; functions as extremely heavy physical weapon; cannot activate spiritual properties.",
     "canon_note":"A sword that can slay gods; massive and heavy beyond mortal comprehension."},
    {"id":"karma_whip","name":"Karma Whip","type":"whip",
     "length_m":3.5,"weight_jin":320,"material_density":"karma-thread steel",
     "suitable_realm":"Ascendant","body_strength_required":"Core Formation",
     "effect_when_undersized":"Can wield physically but cannot activate karmic properties; whip is heavy and unwieldy.",
     "canon_note":"A whip that strikes at karma itself."},
    {"id":"soul_lasher","name":"Soul Lasher","type":"whip",
     "length_m":2.8,"weight_jin":180,"material_density":"soul-steel",
     "suitable_realm":"Nascent Soul","body_strength_required":"Foundation Establishment",
     "effect_when_undersized":"Can wield physically but cannot activate soul-drain properties.",
     "canon_note":"A whip that drains soul fragments."},
    {"id":"eighteen_hell_stamp","name":"18-Hell Stamp","type":"stamp",
     "length_m":0.15,"weight_jin":5000,"material_density":"hell-iron",
     "suitable_realm":"Spirit Severing","body_strength_required":"Core Formation",
     "effect_when_undersized":"Cannot lift; stamp is impossibly dense for its size.",
     "canon_note":"A stamp that commands the 18 hells."},
    {"id":"restriction_flag","name":"Restriction Flag","type":"flag",
     "length_m":2.0,"weight_jin":80,"material_density":"restriction-silk",
     "suitable_realm":"Nascent Soul","body_strength_required":"Foundation Establishment",
     "effect_when_undersized":"Can wield but cannot activate restriction properties.",
     "canon_note":"A flag that imposes restrictions."},
    {"id":"heaven_fan","name":"Heaven Fan","type":"fan",
     "length_m":1.2,"weight_jin":450,"material_density":"heaven-silk",
     "suitable_realm":"Ascendant","body_strength_required":"Core Formation",
     "effect_when_undersized":"Cannot open fully; fan is too heavy.",
     "canon_note":"A fan that summons heaven-winds."},
    {"id":"star_sealing_flag","name":"Star-Sealing Flag","type":"flag",
     "length_m":3.0,"weight_jin":8000,"material_density":"star-iron",
     "suitable_realm":"Immortal","body_strength_required":"Spirit Severing",
     "effect_when_undersized":"Cannot lift; flag is impossibly heavy.",
     "canon_note":"A flag that seals stars."},
    {"id":"soul_refining_flag","name":"Soul Refining Flag","type":"flag",
     "length_m":2.5,"weight_jin":600,"material_density":"soul-silk",
     "suitable_realm":"Nascent Soul","body_strength_required":"Core Formation",
     "effect_when_undersized":"Can wield but cannot activate soul-refining properties.",
     "canon_note":"A flag that refines souls."},
    {"id":"vermilion_bird_feather","name":"Vermilion Bird Feather","type":"feather",
     "length_m":1.5,"weight_jin":120,"material_density":"divine fire-feather",
     "suitable_realm":"Immortal","body_strength_required":"Spirit Severing",
     "effect_when_undersized":"Holds but cannot activate; feather is warm but inert.",
     "canon_note":"A feather of the Vermilion Bird divine beast."},
    {"id":"ancient_god_bone","name":"Ancient God Bone","type":"bone",
     "length_m":5.0,"weight_jin":50000,"material_density":"god-bone",
     "suitable_realm":"Heaven Trampling","body_strength_required":"Immortal",
     "effect_when_undersized":"Cannot lift; bone is impossibly dense.",
     "canon_note":"A bone of an Ancient God."},
    {"id":"dragon_scale","name":"Dragon Scale","type":"scale",
     "length_m":0.8,"weight_jin":300,"material_density":"dragon-scale",
     "suitable_realm":"Ascendant","body_strength_required":"Nascent Soul",
     "effect_when_undersized":"Can hold but cannot activate; scale is heavy.",
     "canon_note":"A scale of an ancient dragon."},
    {"id":"spirit_vein_essence","name":"Spirit Vein Essence","type":"essence",
     "length_m":0.1,"weight_jin":2,"material_density":"condensed spirit essence",
     "suitable_realm":"Nascent Soul","body_strength_required":"Foundation Establishment",
     "effect_when_undersized":"Can hold; essence is light but requires cultivation to absorb.",
     "canon_note":"Condensed essence of a spirit vein."},
    {"id":"tribulation_fragment","name":"Tribulation Fragment","type":"fragment",
     "length_m":0.05,"weight_jin":0.1,"material_density":"condensed lightning",
     "suitable_realm":"Spirit Severing","body_strength_required":"Core Formation",
     "effect_when_undersized":"Cannot absorb; fragment shocks the holder.",
     "canon_note":"A fragment of heavenly tribulation."},
    {"id":"nine_color_flame","name":"Nine-Color Flame","type":"flame",
     "length_m":0.3,"weight_jin":0,"material_density":"pure law-flame",
     "suitable_realm":"Immortal","body_strength_required":"Spirit Severing",
     "effect_when_undersized":"Cannot contain; flame burns the holder.",
     "canon_note":"The nine-color flame of the Vermilion Bird."},
    {"id":"void_essence","name":"Void Essence","type":"essence",
     "length_m":0.2,"weight_jin":0,"material_density":"pure void",
     "suitable_realm":"Ascendant","body_strength_required":"Spirit Severing",
     "effect_when_undersized":"Cannot contain; essence dissolves the holder.",
     "canon_note":"Condensed essence of the void."},
    {"id":"soul_fragment","name":"Soul Fragment","type":"fragment",
     "length_m":0.05,"weight_jin":0,"material_density":"pure soul",
     "suitable_realm":"Nascent Soul","body_strength_required":"Core Formation",
     "effect_when_undersized":"Cannot absorb; fragment dissipates.",
     "canon_note":"A fragment of a fallen cultivator's soul."},
    {"id":"dao_fragment","name":"Dao Fragment","type":"fragment",
     "length_m":0.1,"weight_jin":0,"material_density":"pure law",
     "suitable_realm":"Ascendant","body_strength_required":"Spirit Severing",
     "effect_when_undersized":"Cannot comprehend; fragment is invisible to undersized.",
     "canon_note":"A fragment of natural law."},
    {"id":"blood_essence","name":"Blood Essence","type":"essence",
     "length_m":0.05,"weight_jin":0.5,"material_density":"condensed blood",
     "suitable_realm":"Core Formation","body_strength_required":"Foundation Establishment",
     "effect_when_undersized":"Can hold but cannot absorb; essence is heavy for its size.",
     "canon_note":"Condensed essence of a powerful being's blood."},
    {"id":"mosquito_core","name":"Mosquito Core","type":"core",
     "length_m":0.08,"weight_jin":1,"material_density":"condensed blood-essence",
     "suitable_realm":"Foundation Establishment","body_strength_required":"Qi Condensation",
     "effect_when_undersized":"Can hold; core is light but requires cultivation to absorb.",
     "canon_note":"A mosquito beast's core."},
    {"id":"thunder_toad_core","name":"Thunder Toad Core","type":"core",
     "length_m":0.1,"weight_jin":3,"material_density":"condensed thunder-essence",
     "suitable_realm":"Core Formation","body_strength_required":"Foundation Establishment",
     "effect_when_undersized":"Can hold but cannot absorb; core shocks the holder.",
     "canon_note":"A thunder toad's core."},
    {"id":"lei_ji_core","name":"Lei Ji Core","type":"core",
     "length_m":0.15,"weight_jin":8,"material_density":"condensed thunder-essence",
     "suitable_realm":"Nascent Soul","body_strength_required":"Core Formation",
     "effect_when_undersized":"Can hold but cannot absorb; core shocks the holder.",
     "canon_note":"A Lei Ji thunder beast's core."},
    {"id":"nether_core","name":"Nether Core","type":"core",
     "length_m":0.12,"weight_jin":4,"material_density":"condensed nether-essence",
     "suitable_realm":"Nascent Soul","body_strength_required":"Core Formation",
     "effect_when_undersized":"Can hold but cannot absorb; core chills the holder.",
     "canon_note":"A nether beast's core."},
    {"id":"flame_dragon_core","name":"Flame Dragon Core","type":"core",
     "length_m":0.3,"weight_jin":50,"material_density":"condensed fire-essence",
     "suitable_realm":"Spirit Severing","body_strength_required":"Nascent Soul",
     "effect_when_undersized":"Can hold but cannot absorb; core burns the holder.",
     "canon_note":"A flame dragon's core."},
    {"id":"cloud_whale_core","name":"Cloud Whale Core","type":"core",
     "length_m":0.5,"weight_jin":200,"material_density":"condensed cloud-essence",
     "suitable_realm":"Spirit Severing","body_strength_required":"Nascent Soul",
     "effect_when_undersized":"Can hold but cannot absorb; core is heavy.",
     "canon_note":"A cloud whale's core."},
    {"id":"vermilion_bird_core","name":"Vermilion Bird Core","type":"core",
     "length_m":1.0,"weight_jin":5000,"material_density":"condensed divine fire-essence",
     "suitable_realm":"Immortal","body_strength_required":"Ascendant",
     "effect_when_undersized":"Cannot lift; core is impossibly dense.",
     "canon_note":"The Vermilion Bird's core."},
    {"id":"azure_dragon_core","name":"Azure Dragon Core","type":"core",
     "length_m":1.2,"weight_jin":6000,"material_density":"condensed divine water-essence",
     "suitable_realm":"Immortal","body_strength_required":"Ascendant",
     "effect_when_undersized":"Cannot lift; core is impossibly dense.",
     "canon_note":"The Azure Dragon's core."},
    {"id":"white_tiger_core","name":"White Tiger Core","type":"core",
     "length_m":0.8,"weight_jin":4000,"material_density":"condensed divine slaughter-essence",
     "suitable_realm":"Immortal","body_strength_required":"Ascendant",
     "effect_when_undersized":"Cannot lift; core is impossibly dense.",
     "canon_note":"The White Tiger's core."},
    {"id":"black_tortoise_core","name":"Black Tortoise Core","type":"core",
     "length_m":1.5,"weight_jin":8000,"material_density":"condensed divine earth-essence",
     "suitable_realm":"Immortal","body_strength_required":"Ascendant",
     "effect_when_undersized":"Cannot lift; core is impossibly dense.",
     "canon_note":"The Black Tortoise's core."},
    {"id":"qilin_core","name":"Qilin Core","type":"core",
     "length_m":0.6,"weight_jin":3000,"material_density":"condensed divine auspicious-essence",
     "suitable_realm":"Immortal","body_strength_required":"Ascendant",
     "effect_when_undersized":"Cannot lift; core is impossibly dense.",
     "canon_note":"A qilin's core."},
    {"id":"ancient_god_core","name":"Ancient God Core","type":"core",
     "length_m":2.0,"weight_jin":50000,"material_density":"condensed god-essence",
     "suitable_realm":"Heaven Trampling","body_strength_required":"Immortal",
     "effect_when_undersized":"Cannot lift; core is impossibly dense.",
     "canon_note":"An Ancient God's core."},
    {"id":"spirit_stone_low","name":"Low-Grade Spirit Stone","type":"stone",
     "length_m":0.05,"weight_jin":0.2,"material_density":"spirit-stone",
     "suitable_realm":"Qi Condensation","body_strength_required":"Mortal",
     "effect_when_undersized":"Can hold and absorb.",
     "canon_note":"A low-grade spirit stone."},
    {"id":"spirit_stone_mid","name":"Mid-Grade Spirit Stone","type":"stone",
     "length_m":0.08,"weight_jin":1,"material_density":"spirit-stone",
     "suitable_realm":"Foundation Establishment","body_strength_required":"Qi Condensation",
     "effect_when_undersized":"Can hold; absorption is slow.",
     "canon_note":"A mid-grade spirit stone."},
    {"id":"spirit_stone_high","name":"High-Grade Spirit Stone","type":"stone",
     "length_m":0.12,"weight_jin":5,"material_density":"spirit-stone",
     "suitable_realm":"Core Formation","body_strength_required":"Foundation Establishment",
     "effect_when_undersized":"Can hold but absorption shocks the holder.",
     "canon_note":"A high-grade spirit stone."},
    {"id":"immortal_stone","name":"Immortal Stone","type":"stone",
     "length_m":0.15,"weight_jin":20,"material_density":"immortal-stone",
     "suitable_realm":"Immortal","body_strength_required":"Spirit Severing",
     "effect_when_undersized":"Cannot absorb; stone is too dense.",
     "canon_note":"An immortal-grade spirit stone."},
    {"id":"nine_leaf_clover","name":"Nine-Leaf Clover","type":"herb",
     "length_m":0.3,"weight_jin":0.1,"material_density":"spirit-herb",
     "suitable_realm":"Foundation Establishment","body_strength_required":"Mortal",
     "effect_when_undersized":"Can hold; herb is light.",
     "canon_note":"A nine-leaf spirit clover."},
    {"id":"soul_nourishing_lotus","name":"Soul-Nourishing Lotus","type":"herb",
     "length_m":0.4,"weight_jin":0.3,"material_density":"soul-herb",
     "suitable_realm":"Nascent Soul","body_strength_required":"Foundation Establishment",
     "effect_when_undersized":"Can hold but cannot absorb; lotus is delicate.",
     "canon_note":"A lotus that nourishes the soul."},
    {"id":"five_color_ginseng","name":"Five-Color Ginseng","type":"herb",
     "length_m":0.5,"weight_jin":1,"material_density":"spirit-herb",
     "suitable_realm":"Core Formation","body_strength_required":"Foundation Establishment",
     "effect_when_undersized":"Can hold; ginseng is heavy for its size.",
     "canon_note":"A five-color spirit ginseng."},
    {"id":"blood_forgetting_grass","name":"Blood-Forgetting Grass","type":"herb",
     "length_m":0.3,"weight_jin":0.1,"material_density":"blood-herb",
     "suitable_realm":"Nascent Soul","body_strength_required":"Foundation Establishment",
     "effect_when_undersized":"Can hold; grass is light but unsettling.",
     "canon_note":"A grass that makes one forget blood ties."},
    {"id":"dao_trace_vine","name":"Dao Trace Vine","type":"herb",
     "length_m":2.0,"weight_jin":3,"material_density":"law-herb",
     "suitable_realm":"Spirit Severing","body_strength_required":"Core Formation",
     "effect_when_undersized":"Can hold but cannot perceive; vine is heavy.",
     "canon_note":"A vine that traces the Dao."},
    {"id":"heart_devil_flower","name":"Heart-Devil Flower","type":"herb",
     "length_m":0.2,"weight_jin":0.05,"material_density":"devil-herb",
     "suitable_realm":"Nascent Soul","body_strength_required":"Foundation Establishment",
     "effect_when_undersized":"Can hold; flower is light but disturbing.",
     "canon_note":"A flower that manifests heart-devils."},
    {"id":"reincarnation_lily","name":"Reincarnation Lily","type":"herb",
     "length_m":0.3,"weight_jin":0.2,"material_density":"law-herb",
     "suitable_realm":"Ascendant","body_strength_required":"Nascent Soul",
     "effect_when_undersized":"Can hold but cannot perceive; lily is delicate.",
     "canon_note":"A lily that touches reincarnation."},
    {"id":"void_nether_grass","name":"Void-Nether Grass","type":"herb",
     "length_m":0.4,"weight_jin":0.3,"material_density":"void-herb",
     "suitable_realm":"Spirit Severing","body_strength_required":"Nascent Soul",
     "effect_when_undersized":"Can hold; grass is light but unsettling.",
     "canon_note":"A grass of the void-nether."},
    {"id":"ji_realm","name":"Ji Realm","type":"realm_seed",
     "length_m":0,"weight_jin":0,"material_density":"pure law",
     "suitable_realm":"Ascendant","body_strength_required":"Nascent Soul",
     "effect_when_undersized":"Cannot perceive; realm is invisible to undersized.",
     "canon_note":"Wang Lin's Ji Realm — a condensed realm of pure law."},
    {"id":"storage_pouch","name":"Storage Pouch","type":"container",
     "length_m":0.2,"weight_jin":0.5,"material_density":"spirit-silk",
     "suitable_realm":"Foundation Establishment","body_strength_required":"Mortal",
     "effect_when_undersized":"Can hold; pouch is light.",
     "canon_note":"A pouch that stores items in a pocket dimension."},
    {"id":"flying_sword","name":"Flying Sword","type":"sword",
     "length_m":1.2,"weight_jin":15,"material_density":"spirit-steel",
     "suitable_realm":"Core Formation","body_strength_required":"Foundation Establishment",
     "effect_when_undersized":"Can wield but cannot activate flight.",
     "canon_note":"A standard flying sword."},
    {"id":"spirit_armor","name":"Spirit Armor","type":"armor",
     "length_m":1.8,"weight_jin":80,"material_density":"spirit-steel",
     "suitable_realm":"Foundation Establishment","body_strength_required":"Qi Condensation",
     "effect_when_undersized":"Can wear but is encumbered.",
     "canon_note":"Standard spirit armor."},
    {"id":"soul_formation_array","name":"Soul Formation Array","type":"array",
     "length_m":10,"weight_jin":5000,"material_density":"formation-essence",
     "suitable_realm":"Nascent Soul","body_strength_required":"Core Formation",
     "effect_when_undersized":"Cannot deploy; array is too heavy.",
     "canon_note":"A soul formation array."},
    {"id":"heaven_sealing_array","name":"Heaven-Sealing Array","type":"array",
     "length_m":100,"weight_jin":500000,"material_density":"law-essence",
     "suitable_realm":"Immortal","body_strength_required":"Ascendant",
     "effect_when_undersized":"Cannot deploy; array is impossibly heavy.",
     "canon_note":"An array that seals heaven."},
    {"id":"ancient_god_crown","name":"Ancient God Crown","type":"crown",
     "length_m":0.4,"weight_jin":2000,"material_density":"god-gold",
     "suitable_realm":"Heaven Trampling","body_strength_required":"Immortal",
     "effect_when_undersized":"Cannot wear; crown is impossibly heavy.",
     "canon_note":"The crown of an Ancient God."},
    {"id":"vermilion_emperor_seal","name":"Vermilion Bird Divine Emperor Seal","type":"seal",
     "length_m":0.2,"weight_jin":3000,"material_density":"divine fire-gold",
     "suitable_realm":"Immortal","body_strength_required":"Ascendant",
     "effect_when_undersized":"Cannot lift; seal is impossibly dense.",
     "canon_note":"The seal of the Vermilion Bird Divine Emperor."},
    {"id":"cave_world_key","name":"Cave World Key","type":"key",
     "length_m":0.1,"weight_jin":50,"material_density":"cave-world essence",
     "suitable_realm":"Immortal","body_strength_required":"Spirit Severing",
     "effect_when_undersized":"Can hold but cannot activate; key is heavy.",
     "canon_note":"The key to the Cave World."},
]

def pass_19_item_properties(canon):
    """Pass 19: Item Physical Properties."""
    prop_dir = out("item_properties")
    count = 0
    for p in ITEM_PHYSICAL_PROPERTIES:
        f = prop_dir / f"{p['id']}.json"
        if f.exists():
            continue
        doc = {
            "_comment": f"Item Properties: {p['name']}. "
                         f"Per physical-size directive: every object has lore-accurate physical size, "
                         f"not block size. "
                         f"Length: {p['length_m']} m, Weight: {p['weight_jin']} jin, "
                         f"Material: {p['material_density']}, "
                         f"Suitable realm: {p['suitable_realm']}, "
                         f"Body strength required: {p['body_strength_required']}. "
                         f"Prime Directive: physical size exists objectively; "
                         f"cultivation determines what the player can lift/activate.",
            "item_id": p["id"],
            "name": p["name"],
            "type": p["type"],
            "physical_properties": {
                "length_m": p["length_m"],
                "weight_jin": p["weight_jin"],
                "material_density": p["material_density"],
            },
            "usage_requirements": {
                "suitable_realm": p["suitable_realm"],
                "body_strength_required": p["body_strength_required"],
                "effect_when_undersized": p["effect_when_undersized"],
            },
            "canon_note": p["canon_note"],
            "derivation_type": "A",
            "canon_confidence": 4,
            "simulation_notes": {
                "no_locked_upgrades": "A cultivator who later reaches the required body strength will be able to lift/activate the item; "
                                      "the item does not change, only the cultivator does.",
                "canon_fidelity": "This reflects the novels: Wang Lin could not lift the God-Slaying Sword until his body cultivation matched; "
                                  "he could barely use it as a heavy physical weapon before then.",
            },
            "salt": salt_for(p["id"]),
        }
        write_json(f, doc)
        count += 1
    return count

# ==================================================================
# PASS 20 - WORLD STATE ENGINE MASTER SCHEMA
# ==================================================================
# The unifying document. Everything derives from it. Continuously answers:
# What exists? Who owns it? Who wants it? Who knows about it?
# Why hasn't it been taken already? What would naturally happen next
# if the player did nothing?

def pass_20_world_state_engine(canon):
    """Pass 20: World State Engine Master Schema."""
    ws_dir = out("world_state")
    count = 0

    # Master schema
    master = {
        "_comment": "WORLD STATE ENGINE - MASTER SCHEMA. "
                    "The unifying simulation system. Everything derives from this. "
                    "Per the user's directive: 'If I could only ask the agent to build one more foundational system, "
                    "it would be this: The World State Engine.' "
                    "The engine continuously answers: What exists? Who owns it? Who wants it? "
                    "Who knows about it? Why hasn't it been taken already? "
                    "What would naturally happen next if the player did nothing? "
                    "Prime Directive: the player is not spawning content — they are entering a world that is already moving.",
        "engine_id": "world_state_engine",
        "version": "1.0",
        "derivation_type": "A",
        "canon_confidence": 5,
        "core_questions": [
            "What exists?",
            "Who owns it?",
            "Who wants it?",
            "Who knows about it?",
            "Why hasn't it been taken already?",
            "What would naturally happen next if the player did nothing?"
        ],
        "subsystem_registry": {
            "species": "data/ergenverse/species/ (canonical beast definitions)",
            "species_variants": "data/ergenverse/species_variants/ (10 life-state variants per species)",
            "ecosystems": "data/ergenverse/ecosystems/ (food webs with seasonality)",
            "migrations": "data/ergenverse/migrations/ (migration routes with follower chains)",
            "macro_terrain": "data/ergenverse/macro_terrain/ (terrain that IS a being)",
            "provenance": "data/ergenverse/provenance/ (artifact lifecycle tracking)",
            "civilizations": "data/ergenverse/civilizations/ (sect/clan/city simulation)",
            "opportunities": "data/ergenverse/opportunities/ (opportunity generation tables)",
            "time_events": "data/ergenverse/time_events/ (time-system event definitions)",
            "item_properties": "data/ergenverse/item_properties/ (lore-accurate physical properties)",
            "worldgen": "data/ergenverse/worldgen/ (biomes, structures, features, dimensions)",
        },
        "simulation_loop": {
            "tick_rate": "1 tick = 1 in-game day",
            "per_tick": [
                "advance time_events (check triggers, apply effects)",
                "advance migrations (move waypoints, spawn markets)",
                "advance ecosystems (apply seasonal state, adjust populations)",
                "advance civilizations (recruitment, economy, politics, lifecycle)",
                "advance opportunities (infer new opportunities at aged locations, expire old ones)",
                "advance provenance (record any state changes to artifacts)",
                "advance macro_terrain (perception re-roll for cultivators whose perception changed)",
                "answer player queries (what does the player perceive at their location?)",
            ],
            "player_arrival": "When the player arrives at a location, the engine does NOT spawn content; "
                              "it reveals what already exists according to the player's perception tier.",
        },
        "perception_engine": {
            "description": "Determines what the player perceives based on cultivation tier.",
            "tiers": ["mortal","qi_condensation","foundation_establishment","core_formation",
                      "nascent_soul","spirit_severing","ascendant","immortal","heaven_trampling"],
            "rule": "Perception NEVER changes what exists; it only changes what the player observes. "
                    "A mortal standing on Ancient God bone sees a mountain; "
                    "a Core Formation cultivator sees bone; "
                    "a Spirit Severing cultivator sees god-memory.",
            "no_locked_upgrades": "A mortal who later reaches Core Formation will see the truth; "
                                  "the terrain does not change, only understanding does.",
        },
        "objective_nature": {
            "description": "The ObjectiveNature layer records the TRUE state of everything, independent of perception.",
            "rule": "Reality is objective; cultivation changes understanding, not existence.",
            "fields": ["true_owner","true_age","true_history","true_contents","true_danger"],
            "perception_overlay": "PerceptionEngine overlays a filtered view on top of ObjectiveNature.",
        },
        "world_breathes": {
            "description": "The world changes whether or not the player acts.",
            "examples": [
                "Spirit herbs grow seasonally.",
                "Beasts migrate along routes.",
                "Spirit veins shift over decades.",
                "Forests burn randomly.",
                "Sect wars occur politically.",
                "Sect leaders die of age or war.",
                "Markets move seasonally.",
                "Caravans disappear randomly.",
                "Rivers change course over centuries.",
                "Spirit veins exhaust over millennia.",
                "Caves collapse randomly.",
                "Ancient ruins open periodically.",
                "Tribulations scar the landscape.",
                "A player who returns 200 years later will barely recognize the world.",
            ],
        },
        "salt": salt_for("world_state_engine"),
    }
    write_json(ws_dir / "engine.json", master)
    count += 1

    # Per-question query schemas
    questions = [
        {"question":"What exists?","schema":"object_registry",
         "fields":["object_id","object_type","location","age","true_state"],
         "data_sources":["worldgen/biome","worldgen/structure","species","macro_terrain","provenance","civilizations"],
         "note":"The engine maintains a complete registry of everything that exists in the world."},
        {"question":"Who owns it?","schema":"ownership_registry",
         "fields":["object_id","true_owner","owner_type","owner_strength","claim_strength"],
         "data_sources":["provenance","civilizations"],
         "note":"Ownership is objective; it may be a sect, a beast, a cultivator, or unclaimed."},
        {"question":"Who wants it?","schema":"desire_registry",
         "fields":["object_id","desirer","desire_type","desire_strength","desire_reason"],
         "data_sources":["civilizations","species","provenance"],
         "note":"Desire is inferred from faction needs, beast ecology, and cultivator inheritance."},
        {"question":"Who knows about it?","schema":"knowledge_registry",
         "fields":["object_id","knower","knowledge_tier","knowledge_accuracy"],
         "data_sources":["civilizations","provenance"],
         "note":"Knowledge is tiered; a sect may know a rumor, an individual may know the truth."},
        {"question":"Why hasn't it been taken already?","schema":"untaken_reason_registry",
         "fields":["object_id","reason","reason_type","reason_strength"],
         "reason_types":["sealed","hidden","guarded","too_dangerous","unknown","taboo","contested","requires_specific_cultivation","requires_specific_perception","requires_specific_luck"],
         "note":"This is the most important question; it explains why opportunities persist for the player to find."},
        {"question":"What would naturally happen next if the player did nothing?","schema":"natural_next_event_registry",
         "fields":["object_id","next_event","event_timeline","event_probability"],
         "data_sources":["time_events","ecosystems","migrations","civilizations"],
         "note":"The world does not wait for the player; events unfold on their own schedule."},
    ]
    for q in questions:
        ds = q.get("data_sources", [])
        doc = {
            "_comment": f"World State Engine query: {q['question']} "
                         f"Schema: {q['schema']}. "
                         f"Data sources: {ds}.",
            "question": q["question"],
            "schema": q["schema"],
            "fields": q["fields"],
            "data_sources": q.get("data_sources",[]),
            "reason_types": q.get("reason_types",[]),
            "note": q["note"],
            "derivation_type": "A",
            "canon_confidence": 4,
            "salt": salt_for(q["schema"]),
        }
        write_json(ws_dir / f"{q['schema']}.json", doc)
        count += 1

    return count

# ==================================================================
# LANG FILE UPDATE
# ==================================================================
def update_lang_file(species_count, variant_count, eco_count, mig_count,
                     terr_count, prov_count, civ_count, opp_count, ev_count, prop_count):
    lang_path = ROOT / "src" / "main" / "resources" / "assets" / "ergenverse" / "lang" / "en_us.json"
    try:
        with open(lang_path, encoding="utf-8") as f:
            lang = json.load(f)
    except Exception:
        lang = {}

    added = 0

    def add(key, val):
        nonlocal added
        if key not in lang:
            lang[key] = val
            added += 1

    # Species
    for sp in SPECIES_CANON:
        add(f"species.ergenverse.{sp['id']}", sp["name"])
    for v in LIFE_STATES:
        add(f"life_state.ergenverse.{v['id']}", v["name"])

    # Ecosystems
    for eco in ECOSYSTEMS:
        add(f"ecosystem.ergenverse.{eco['id']}", eco["name"])

    # Migrations
    for m in MIGRATION_ROUTES:
        add(f"migration.ergenverse.{m['id']}", m["name"])

    # Macro terrain
    for t in MACRO_TERRAIN:
        add(f"macro_terrain.ergenverse.{t['id']}", t["name"])

    # Civilizations
    for c in CIVILIZATIONS:
        add(f"civilization.ergenverse.{c['id']}", c["name"])

    # Opportunities
    for o in OPPORTUNITY_ARCHETYPES:
        add(f"opportunity.ergenverse.{o['id']}", o["name"])

    # Time events
    for e in TIME_EVENTS:
        add(f"time_event.ergenverse.{e['id']}", e["name"])

    # Item properties
    for p in ITEM_PHYSICAL_PROPERTIES:
        add(f"item_property.ergenverse.{p['id']}", p["name"])

    # Sort and write
    lang_sorted = dict(sorted(lang.items()))
    with open(lang_path, "w", encoding="utf-8") as f:
        json.dump(lang_sorted, f, ensure_ascii=False, indent=2)
        f.write("\n")
    return added

# ==================================================================
# MAIN
# ==================================================================
def main():
    print("=" * 70)
    print("WORLD STATE ENGINE - Layer 3 Canon-to-Simulation Generator")
    print("Implements the 10 architectural gaps + unifying engine.")
    print("=" * 70)
    canon = load_canon()
    print(f"Loaded canon: {len(canon.get('characters',[]))} characters, "
          f"{len(canon.get('locations',[]))} locations, "
          f"{len(canon.get('artifacts',[]))} artifacts, "
          f"{len(canon.get('techniques',[]))} techniques")

    results = {}

    print("\n[Pass 11] Comprehensive Species Database + life-state variants...")
    s, v = pass_11_species_database(canon)
    results['species_new'] = s
    results['species_variants_new'] = v
    print(f"  +{s} new species definitions, +{v} new life-state variants")

    print("\n[Pass 12] Ecosystem Food Webs with Seasonality...")
    e = pass_12_ecosystems(canon)
    results['ecosystems_new'] = e
    print(f"  +{e} new ecosystem definitions")

    print("\n[Pass 13] Migration Routes with follower chains...")
    m = pass_13_migration_routes(canon)
    results['migrations_new'] = m
    print(f"  +{m} new migration routes")

    print("\n[Pass 14] Macro-Scale Terrain (Ancient God as terrain)...")
    t = pass_14_macro_terrain(canon)
    results['macro_terrain_new'] = t
    print(f"  +{t} new macro-terrain features")

    print("\n[Pass 15] Expanded Provenance Schema...")
    p = pass_15_provenance_expansion(canon)
    results['provenance_upgraded'] = p
    print(f"  +{p} provenance files upgraded with lifecycle schema")

    print("\n[Pass 16] Civilization Simulation Definitions...")
    c = pass_16_civilizations(canon)
    results['civilizations_new'] = c
    print(f"  +{c} new civilization definitions")

    print("\n[Pass 17] Opportunity Generation Tables...")
    o = pass_17_opportunities(canon)
    results['opportunities_new'] = o
    print(f"  +{o} new opportunity archetypes")

    print("\n[Pass 18] Time-System Event Definitions...")
    te = pass_18_time_events(canon)
    results['time_events_new'] = te
    print(f"  +{te} new time-event definitions")

    print("\n[Pass 19] Item Physical Properties...")
    ip = pass_19_item_properties(canon)
    results['item_properties_new'] = ip
    print(f"  +{ip} new item property definitions")

    print("\n[Pass 20] World State Engine Master Schema...")
    ws = pass_20_world_state_engine(canon)
    results['world_state_schemas_new'] = ws
    print(f"  +{ws} new world-state schemas")

    print("\n[Lang] Updating lang file...")
    la = update_lang_file(s, v, e, m, t, p, c, o, te, ip)
    results['lang_added'] = la
    print(f"  +{la} new lang entries")

    total_new = sum(results.values())
    print("\n" + "=" * 70)
    print(f"DONE. Total new assets: {total_new}")
    for k, v in results.items():
        print(f"  {k}: {v}")
    print("=" * 70)

if __name__ == "__main__":
    main()
