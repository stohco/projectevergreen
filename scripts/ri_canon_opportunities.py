#!/usr/bin/env python3
"""
RI Canon Event Opportunities — every canon event becomes a discoverable opportunity.
Per user directive: "please make sure to fully have the renegade immortal content in the mod"

This generates opportunity definitions for every major canon event from RI, so the
player can discover/relive Wang Lin's journey.
"""

import json
import hashlib
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DATA = ROOT / "src" / "main" / "resources" / "data" / "ergenverse"

def slug(s):
    return "".join(c.lower() if c.isalnum() else "_" for c in s).strip("_")

def salt_for(name):
    h = hashlib.md5(name.encode("utf-8")).hexdigest()
    return (int(h[:8], 16) % 2000000000) + 1

def write_json(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(obj, f, ensure_ascii=False, indent=2)
        f.write("\n")

# Canon event opportunities — every major event from Wang Lin's journey
RI_CANON_OPPORTUNITIES = [
    {"id":"ri_heaven_defying_bead_discovery","name":"Heaven-Defying Bead Discovery",
     "canon_event":"Wang Lin finds the Heaven-Defying Bead in a dead bird under a cliff as a youth",
     "location":"heng_yue_sect_outer_peaks","age_requirement_years":0,
     "cultivation_required":"Qi Condensation","perception_required":"Foundation Establishment",
     "luck_modifier":0.1,"discovery_methods":["exploration","divine_sense"],
     "reward_tables":["heaven_defying_bead","dream_dao_access"],
     "why_untaken":"The bead is hidden in a dead bird under a cliff; requires specific luck/perception to find.",
     "natural_next_event":"Wang Lin (or player) discovers the bead and begins their journey.",
     "canon_confidence":5},

    {"id":"ri_mosquito_valley_taming","name":"Mosquito Valley Taming",
     "canon_event":"Wang Lin tames the Mosquito Beast swarm via Dream Dao in Mosquito Valley",
     "location":"mosquito_valley","age_requirement_years":10000,
     "cultivation_required":"Core Formation","perception_required":"Nascent Soul",
     "luck_modifier":0.5,"discovery_methods":["exploration","divine_sense","rumor"],
     "reward_tables":["mosquito_beast_swarm","mosquito_valley_ecosystem_access"],
     "why_untaken":"Mosquito Valley is extremely dangerous; requires Core Formation+ to survive.",
     "natural_next_event":"The ancient queen continues cultivating; swarm grows.",
     "canon_confidence":5},

    {"id":"ri_ancient_god_inheritance","name":"Ancient God Tu Si Inheritance",
     "canon_event":"Wang Lin inherits Tu Si's Ancient God knowledge in the Land of the Ancient God",
     "location":"ancient_god_land","age_requirement_years":100000,
     "cultivation_required":"Nascent Soul","perception_required":"Spirit Severing",
     "luck_modifier":0.3,"discovery_methods":["divine_sense","rumor","formation_breaking"],
     "reward_tables":["ancient_god_knowledge","god_body_cultivation","tu_si_remnants"],
     "why_untaken":"Sealed by Tu Si's formation; requires specific cultivation/perception; contested by Tuo Sen.",
     "natural_next_event":"Tuo Sen contests the inheritance; the seal weakens over time.",
     "canon_confidence":5},

    {"id":"ri_suzaku_inheritance","name":"Suzaku Inheritance",
     "canon_event":"Wang Lin receives the Suzaku inheritance in the Suzaku Tomb",
     "location":"suzaku_tomb","age_requirement_years":50000,
     "cultivation_required":"Core Formation","perception_required":"Nascent Soul",
     "luck_modifier":0.4,"discovery_methods":["divine_sense","rumor","seal_breaking"],
     "reward_tables":["suzaku_inheritance","ji_realm","suzaku_treasures"],
     "why_untaken":"Sealed in the underground Cultivation Planet Crystal; requires specific cultivation.",
     "natural_next_event":"The seal weakens over millennia; eventually breaks.",
     "canon_confidence":5},

    {"id":"ri_vermilion_bird_selection","name":"Vermilion Bird Divine Emperor Selection",
     "canon_event":"Wang Lin is selected as the 6th-Generation Vermilion Bird Divine Emperor",
     "location":"vermilion_bird_divine_sect","age_requirement_years":0,
     "cultivation_required":"Spirit Severing","perception_required":"Ascendant",
     "luck_modifier":0.2,"discovery_methods":["divine_sense","imperial_summons"],
     "reward_tables":["vermilion_bird_inheritance","divine_emperor_seal","vermilion_bird_feather"],
     "why_untaken":"Requires Spirit Severing+ cultivation; the Vermilion Bird must choose you.",
     "natural_next_event":"The Vermilion Bird awakens and selects the next Divine Emperor.",
     "canon_confidence":5},

    {"id":"ri_cave_world_revelation","name":"Cave World Revelation",
     "canon_event":"Wang Lin discovers the Cave World is an artificial Joss Flame farm",
     "location":"cave_world","age_requirement_years":0,
     "cultivation_required":"Ascendant","perception_required":"Immortal",
     "luck_modifier":0.1,"discovery_methods":["dao_comprehension","divine_sense"],
     "reward_tables":["cave_world_truth","joss_flame_understanding","seven_colored_daoist_revelation"],
     "why_untaken":"Requires Immortal-tier perception to comprehend the truth.",
     "natural_next_event":"The Seven-Colored Daoist continues harvesting Joss Flame.",
     "canon_confidence":5},

    {"id":"ri_seven_colored_daoist_confrontation","name":"Seven-Colored Daoist Confrontation",
     "canon_event":"Wang Lin kills the Seven-Colored Daoist and becomes the owner of the Cave World",
     "location":"cave_world_core","age_requirement_years":0,
     "cultivation_required":"Heaven Trampling","perception_required":"Heaven Trampling",
     "luck_modifier":0.05,"discovery_methods":["heaven_trampling_realm"],
     "reward_tables":["cave_world_ownership","seven_colored_daoist_remnants"],
     "why_untaken":"Requires Heaven Trampling cultivation; the Seven-Colored Daoist is the strongest being in the Cave World.",
     "natural_next_event":"The confrontation occurs when Wang Lin reaches sufficient cultivation.",
     "canon_confidence":5},

    {"id":"ri_heaven_trampling_transcendence","name":"Heaven Trampling Transcendence",
     "canon_event":"Wang Lin transcends by crossing the 9 Heaven Trampling Bridges with Li Muwan",
     "location":"heaven_trampling_bridges","age_requirement_years":0,
     "cultivation_required":"Heaven Trampling","perception_required":"Heaven Trampling",
     "luck_modifier":0.01,"discovery_methods":["heaven_trampling_realm"],
     "reward_tables":["transcendence","li_muwan_resurrection"],
     "why_untaken":"Requires completing all 9 Heaven Trampling Bridges; the hardest challenge in existence.",
     "natural_next_event":"Wang Lin transcends with Li Muwan.",
     "canon_confidence":5},

    {"id":"ri_heng_yue_sect_destruction","name":"Heng Yue Sect Destruction",
     "canon_event":"Teng Huayuan destroys Heng Yue Sect; Wang Lin swears revenge",
     "location":"heng_yue_sect","age_requirement_years":0,
     "cultivation_required":"Qi Condensation","perception_required":"Foundation Establishment",
     "luck_modifier":1.0,"discovery_methods":["witness","rumor"],
     "reward_tables":["heng_yue_sect_ruins","scattered_treasures","revenge_quest"],
     "why_untaken":"The destruction is a canon event; the ruins remain for the player to explore.",
     "natural_next_event":"The sect falls into ruin; treasures scatter; survivors flee.",
     "canon_confidence":5},

    {"id":"ri_blood_ancestor_battle","name":"Blood Ancestor Battle",
     "canon_event":"Wang Lin kills the Blood Ancestor (Yao Xinghai)",
     "location":"blood_sea","age_requirement_years":50000,
     "cultivation_required":"Spirit Severing","perception_required":"Ascendant",
     "luck_modifier":0.3,"discovery_methods":["divine_sense","rumor"],
     "reward_tables":["blood_ancestor_remnants","blood_sea_essence","blood_lineage_fragment"],
     "why_untaken":"Requires Spirit Severing+ to survive the blood sea; the Blood Ancestor can reform.",
     "natural_next_event":"The Blood Ancestor's corpse-sea remains; blood beasts converge.",
     "canon_confidence":5},

    {"id":"ri_soul_refining_sect_inheritance","name":"Soul Refining Sect Inheritance",
     "canon_event":"Wang Lin receives the Soul Refining Sect heritage transferred from Pilu Kingdom",
     "location":"soul_refining_sect","age_requirement_years":3000,
     "cultivation_required":"Core Formation","perception_required":"Nascent Soul",
     "luck_modifier":0.4,"discovery_methods":["divine_sense","rumor","sect_connection"],
     "reward_tables":["soul_refining_techniques","soul_refining_treasures","nether_beast_bond"],
     "why_untaken":"Requires sect connection; the heritage was transferred from Pilu Kingdom ruins.",
     "natural_next_event":"The heritage awaits a suitable successor.",
     "canon_confidence":4},

    {"id":"ri_immortal_graveyard_exploration","name":"Immortal Graveyard Exploration",
     "canon_event":"Wang Lin explores the Immortal Graveyard (17 Layers)",
     "location":"immortal_graveyard","age_requirement_years":100000,
     "cultivation_required":"Nascent Soul","perception_required":"Spirit Severing",
     "luck_modifier":0.2,"discovery_methods":["divine_sense","rumor"],
     "reward_tables":["immortal_corpses","soul_fragments","ancient_treasures"],
     "why_untaken":"Extremely dangerous; death-aspected; requires high cultivation to survive.",
     "natural_next_event":"The graveyard's death-qi disperses over millennia.",
     "canon_confidence":4},

    {"id":"ri_foreign_battleground_participation","name":"Foreign Battleground Participation",
     "canon_event":"Wang Lin participates in the Foreign Battleground war",
     "location":"foreign_battleground","age_requirement_years":10000,
     "cultivation_required":"Core Formation","perception_required":"Nascent Soul",
     "luck_modifier":0.5,"discovery_methods":["war_summons","rumor"],
     "reward_tables":["war_spoils","white_tiger_manifestation","slaughter_essence"],
     "why_untaken":"Requires war participation; the battleground is active war zone.",
     "natural_next_event":"Wars continue; the White Tiger manifests at major battles.",
     "canon_confidence":4},

    {"id":"ri_pill_sea_alchemy","name":"Pill Sea Alchemy",
     "canon_event":"Wang Lin cultivates alchemy at the Pill Sea",
     "location":"pill_sea","age_requirement_years":10000,
     "cultivation_required":"Core Formation","perception_required":"Nascent Soul",
     "luck_modifier":0.6,"discovery_methods":["divine_sense","rumor"],
     "reward_tables":["pill_essence","alchemy_recipes","flame_dragon_encounter"],
     "why_untaken":"Requires alchemy talent; the Pill Sea is remote.",
     "natural_next_event":"Alchemical storms occur periodically; flame dragons gather.",
     "canon_confidence":3},

    {"id":"ri_realm_sealing_grand_array","name":"Realm-Sealing Grand Array",
     "canon_event":"Wang Lin encounters the Realm-Sealing Grand Array that seals the Cave World",
     "location":"sealed_realm_boundary","age_requirement_years":100000,
     "cultivation_required":"Ascendant","perception_required":"Immortal",
     "luck_modifier":0.2,"discovery_methods":["divine_sense","formation_study"],
     "reward_tables":["realm_sealing_knowledge","array_fragment","sealed_realm_access"],
     "why_untaken":"Requires Ascendant+ cultivation to even perceive the array.",
     "natural_next_event":"The array was placed by the Seven-Colored Daoist; it persists.",
     "canon_confidence":5},

    {"id":"ri_joss_flame_economy_revelation","name":"Joss Flame Economy Revelation",
     "canon_event":"Wang Lin discovers the Joss Flame economy — mortals are unknowing livestock",
     "location":"cave_world","age_requirement_years":0,
     "cultivation_required":"Spirit Severing","perception_required":"Ascendant",
     "luck_modifier":0.2,"discovery_methods":["dao_comprehension","divine_sense"],
     "reward_tables":["joss_flame_understanding","mortal_truth","cave_world_mechanism"],
     "why_untaken":"Requires Spirit Severing+ to comprehend the truth; mortals cannot perceive it.",
     "natural_next_event":"The Joss Flame continues flowing to the Seven-Colored Daoist (until Wang Lin takes over).",
     "canon_confidence":5},

    {"id":"ri_samsara_dao_comprehension","name":"Samsara Dao Comprehension",
     "canon_event":"Wang Lin comprehends the Samsara Dao",
     "location":"reincarnation_realm","age_requirement_years":0,
     "cultivation_required":"Ascendant","perception_required":"Immortal",
     "luck_modifier":0.1,"discovery_methods":["dao_comprehension","divine_sense"],
     "reward_tables":["samsara_dao","reincarnation_essence","past_life_memories"],
     "why_untaken":"Requires Immortal-tier perception to comprehend the Samsara Dao.",
     "natural_next_event":"The Samsara Dao awaits a cultivator who can comprehend it.",
     "canon_confidence":4},

    {"id":"ri_situ_nan_friendship","name":"Situ Nan Friendship",
     "canon_event":"Wang Lin forms a sworn brotherhood with Situ Nan",
     "location":"vermilion_bird_divine_sect","age_requirement_years":0,
     "cultivation_required":"Core Formation","perception_required":"Nascent Soul",
     "luck_modifier":0.7,"discovery_methods":["divine_sense","social_connection"],
     "reward_tables":["situ_nan_alliance","divine_emperor_connection"],
     "why_untaken":"Requires meeting Situ Nan; he is a Divine Emperor.",
     "natural_next_event":"Situ Nan remains Wang Lin's loyal friend.",
     "canon_confidence":5},

    {"id":"ri_qing_shui_mentorship","name":"Qing Shui Mentorship",
     "canon_event":"Qing Shui gives Wang Lin the Slaughter Sword and becomes his mentor",
     "location":"variable","age_requirement_years":0,
     "cultivation_required":"Nascent Soul","perception_required":"Spirit Severing",
     "luck_modifier":0.5,"discovery_methods":["divine_sense","social_connection"],
     "reward_tables":["slaughter_sword","qing_shui_mentorship","water_dao_insight"],
     "why_untaken":"Requires meeting Qing Shui; he is a Third Step cultivator.",
     "natural_next_event":"Qing Shui mentors Wang Lin.",
     "canon_confidence":5},

    {"id":"ri_immortal_astral_continent_arrival","name":"Immortal Astral Continent Arrival",
     "canon_event":"Wang Lin arrives at the Immortal Astral Continent (the true reality outside the Cave World)",
     "location":"immortal_astral_continent","age_requirement_years":0,
     "cultivation_required":"Ascendant","perception_required":"Immortal",
     "luck_modifier":0.3,"discovery_methods":["cave_world_seal_breaking"],
     "reward_tables":["iac_access","ancient_tomb_access","great_soul_sect_elder_position"],
     "why_untaken":"Requires breaking the Cave World seal; endgame content.",
     "natural_next_event":"The IAC exists objectively; the player can arrive when they break the seal.",
     "canon_confidence":5},
]

def main():
    print("=" * 60)
    print("RI Canon Event Opportunities")
    print("=" * 60)

    opp_dir = DATA / "opportunities"
    opp_dir.mkdir(parents=True, exist_ok=True)

    count = 0
    for opp in RI_CANON_OPPORTUNITIES:
        f = opp_dir / f"{opp['id']}.json"
        if f.exists():
            continue
        doc = {
            "_comment": f"RI Canon Opportunity: {opp['name']}. "
                         f"Canon event: {opp['canon_event']}. "
                         f"Per user directive: every canon event must be accessible in-game.",
            "opportunity_id": opp["id"],
            "name": opp["name"],
            "canon_event": opp["canon_event"],
            "location": opp["location"],
            "age_requirement_years": opp["age_requirement_years"],
            "cultivation_required": opp["cultivation_required"],
            "perception_required": opp["perception_required"],
            "luck_modifier": opp["luck_modifier"],
            "discovery_methods": opp["discovery_methods"],
            "reward_tables": opp["reward_tables"],
            "why_untaken": opp["why_untaken"],
            "natural_next_event": opp["natural_next_event"],
            "canon_confidence": opp["canon_confidence"],
            "derivation_type": "A" if opp["canon_confidence"] >= 4 else "B",
            "salt": salt_for(opp["id"]),
        }
        write_json(f, doc)
        count += 1

    # Update lang file
    lang_path = ROOT / "src" / "main" / "resources" / "assets" / "ergenverse" / "lang" / "en_us.json"
    try:
        with open(lang_path, encoding="utf-8") as f:
            lang = json.load(f)
    except Exception:
        lang = {}

    lang_added = 0
    for opp in RI_CANON_OPPORTUNITIES:
        key = f"opportunity.ergenverse.{opp['id']}"
        if key not in lang:
            lang[key] = opp["name"]
            lang_added += 1

    lang_sorted = dict(sorted(lang.items()))
    with open(lang_path, "w", encoding="utf-8") as f:
        json.dump(lang_sorted, f, ensure_ascii=False, indent=2)
        f.write("\n")

    print(f"  +{count} new RI canon event opportunities")
    print(f"  +{lang_added} new lang entries")
    print("=" * 60)

if __name__ == "__main__":
    main()
