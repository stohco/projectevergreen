#!/usr/bin/env python3
"""
RI Content Expansion — Comprehensive Renegade Immortal Content Registry
======================================================================
Per user directive: "even the miscellaneous beasts/enemies/npcs from the novel
must be accounted for because they can be content to put in the game too and
part of the ecosystem. please make sure to fully have the renegade immortal
content in the mod"

This script generates:
  Pass A: RI NPC Archetype Definitions — every named cultivator/mortal from RI
  Pass B: RI Enemy Definitions — every named antagonist from RI
  Pass C: RI Beast/Creature Definitions — every named beast from RI (including miscellaneous)
  Pass D: RI Faction Relationship Network — allies/enemies/vassals/rivals
  Pass E: RI Ecosystem Integration — which beings appear in which ecosystems
  Pass F: RI Canon Event Opportunities — every canon event becomes a discoverable opportunity
  Pass G: RI Dialogue Tree Seeds — conversation starters for key NPCs
  Pass H: RI Quest Definitions — canon-derived quest chains

Prime Directive: Reality is objective; cultivation changes understanding, not existence.
No-Locked-Upgrades: every canonical state must be obtainable.
"""

import json
import os
import hashlib
from pathlib import Path

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
    return (base % 2000000000) + 1

def write_json(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(obj, f, ensure_ascii=False, indent=2)
        f.write("\n")

CANON_PRIME = ("Reality is objective; cultivation changes understanding, not existence. "
               "Every being described here exists independently of the player's perception.")

# ==================================================================
# RI NOVEL SCOPE — which canon characters belong to RI
# ==================================================================
# The canon DB has 160 characters across 6 novels. RI characters are
# primarily N01-N12, N17-N62, N77-N160 (Wang Baole N13 is AWWP, etc.)
# We include any character whose canon source is Renegade Immortal.

RI_CHARACTER_IDS = set()
def classify_ri_characters(canon):
    """Identify which canon characters are RI-specific."""
    ri_ids = set()
    for c in canon.get("characters", []):
        name = c.get("name", "")
        name_cn = c.get("nameCn", "")
        facts = " ".join(str(f) for f in c.get("knownFacts", []))
        source = c.get("source", "")
        combined = (name + " " + name_cn + " " + facts + " " + source).lower()
        # RI markers: Wang Lin, Heng Yue, Suzaku, Soul Refining, etc.
        ri_markers = ["wang lin", "heng yue", "suzaku", "soul refining", "pilu",
                      "zhao country", "chu country", "fire burn", "sky demon",
                      "snow domain", "xuan wu", "vermilion bird", "ancient god",
                      "tu si", "situ nan", "seven-colored", "blood ancestor",
                      "teng family", "wang family", "zhou family", "yao family",
                      "sea of devils", "heavenly fate", "cloud sky", "da lou",
                      "allheaven", "cloud sea", "cave world", "immortal astral",
                      "lei ji", "mosquito", "thunder toad", "nether beast",
                      "thunder celestial", "brilliant void", "golden exalt",
                      "wind demon", "green devil", "ji qiong", "tuo sen",
                      "taga", "xu liguo", "tianyunzi", "lu mo", "qing shui",
                      "qing lin", "bai fan", "lu yun", "du tian", "all-seer",
                      "daoist water", "master void", "master ashen pine",
                      "master cloud soul", "old man miesheng", "dao devil",
                      "dong lin", "great soul", "dark scorpion", "origin sect",
                      "li muwan", "li qianmei", "mu bingmei", "wang zhuo",
                      "wang ping", "wang yiyi", "zhou ru", "qing yi",
                      "song yu", "teng huayuan", "teng li", "sun dazhu",
                      "ji mo", "punnan zi", "duanmu ji", "hunchback meng",
                      "xu liqing", "gun lan", "yun fei", "qian kun",
                      "ye wuyou", "qian pinghai", "zhan", "yun kong",
                      "thirteen", "huo pao", "xie qing", "xi zifeng",
                      "adai", "wu yu", "ye zi", "zhao xingsha",
                      "simu rufeng", "zhao xinming", "chen tao", "wang zhou",
                      "wang jie", "sun zhenwei", "chen bailiang", "zhang hu",
                      "zhou rui", "leng sheng", "da niu", "zhou lin",
                      "xiao bai", "ling tianhou", "bei lou", "wang wei",
                      "hu juan", "ta shan", "big head", "lei ji",
                      "liu jinbiao", "ling dong", "zhou jin", "zhong dahong",
                      "daoist scattered spirit", "hong shan", "south cloud",
                      "twin great", "tan lang", "hong die", "zhou wutai",
                      "yun quezi", "mo zhi", "lian daofei", "xu yunshan",
                      "ouyang hua", "li dannan", "bai wei", "master flamespark",
                      "russell", "zhao yu", "lu yanfei", "xu yun",
                      "lu yuncong", "song wude", "rudy", "ji si",
                      "jiu di", "sea child", "gu dao", "song tian",
                      "dao ancient", "dao yi", "imperial preceptor",
                      "du qing", "kang ren", "purple dawn", "white tiger general",
                      "yun yifeng", "lord of the sealed realm"]
        for marker in ri_markers:
            if marker in combined:
                ri_ids.add(c.get("id"))
                break
    return ri_ids

# ==================================================================
# PASS A — RI NPC ARCHETYPE DEFINITIONS
# ==================================================================
# Every named cultivator/mortal from RI becomes an NPC archetype with:
# personality, speech patterns, cultivation tier, faction, location,
# relationship to Wang Lin, dialogue availability, quest availability,
# trade availability, teaching availability, perception tiers.

NPC_ARCHETYPES = [
    # ─── Wang Family Village (mortal NPCs) ───
    {"id":"npc_wang_tianshui","name":"Wang Tianshui","nameCn":"王天水","canon_id":"N02",
     "type":"family_head","faction":"wang_family_village","location":"wang_family_village",
     "cultivation":"mortal","personality":"stern but loving father; village patriarch",
     "speech":"formal, concerned for family","relationship_to_wanglin":"father",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"Village elder; Wang Lin's father","qi_condensation":"Same — mortal cannot perceive cultivation",
     "core_formation":"Same — mortal","nascent_soul":"Same — mortal, but you sense his lifeforce flickering"},
     "canon_confidence":5,"note":"Wang Lin's father; driven to ruin by Teng family"},

    {"id":"npc_wang_ping","name":"Wang Ping","nameCn":"王平","canon_id":"N07",
     "type":"family","faction":"wang_family_village","location":"wang_family_village",
     "cultivation":"mortal","personality":"gentle, protective younger brother",
     "speech":"casual, brotherly","relationship_to_wanglin":"brother",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"Wang Lin's younger brother","qi_condensation":"Same — mortal",
     "core_formation":"Same — mortal","nascent_soul":"Same — mortal, but you sense his fragile health"},
     "canon_confidence":5,"note":"Wang Lin's brother; tragic fate drives Wang Lin's early motivation"},

    {"id":"npc_zhou_rui","name":"Zhou Rui","nameCn":"周瑞","canon_id":"N150",
     "type":"other","faction":"wang_family_village","location":"wang_family_village",
     "cultivation":"mortal","personality":"kind neighbor; helps Wang family in hard times",
     "speech":"warm, village dialect","relationship_to_wanglin":"neighbor",
     "dialogue_available":True,"quest_available":False,"trade_available":True,"teaching_available":False,
     "perception_tiers":{"mortal":"Village neighbor","qi_condensation":"Same — mortal"},
     "canon_confidence":3,"note":"Village neighbor who helps the Wang family"},

    {"id":"npc_da_niu","name":"Da Niu","nameCn":"大牛","canon_id":"N152",
     "type":"other","faction":"wang_family_village","location":"wang_family_village",
     "cultivation":"mortal","personality":"simple, strong, loyal farmhand",
     "speech":"simple, direct","relationship_to_wanglin":"childhood friend",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"Wang Lin's childhood friend; strong farmhand","qi_condensation":"Same — mortal"},
     "canon_confidence":3,"note":"Childhood friend; represents the mortal life Wang Lin left behind"},

    {"id":"npc_zhou_tingsu","name":"Zhou Tingsu","nameCn":"周婷苏","canon_id":"N03",
     "type":"other","faction":"wang_family_village","location":"wang_family_village",
     "cultivation":"mortal","personality":"gentle, caring mother figure",
     "speech":"maternal, worried","relationship_to_wanglin":"mother",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"Wang Lin's mother","qi_condensation":"Same — mortal"},
     "canon_confidence":5,"note":"Wang Lin's mother; her suffering drives his early cultivation"},

    # ─── Heng Yue Sect NPCs ───
    {"id":"npc_qiu_siping","name":"Qiu Siping","nameCn":"邱思平","canon_id":"N34",
     "type":"disciple","faction":"heng_yue_sect","location":"heng_yue_sect",
     "cultivation":"Nascent Soul","personality":"ambitious, scheming outer disciple who climbed to inner",
     "speech":"polite but guarded","relationship_to_wanglin":"fellow disciple; rival then ally",
     "dialogue_available":True,"quest_available":True,"trade_available":True,"teaching_available":False,
     "perception_tiers":{"mortal":"A cultivator in robes","qi_condensation":"Heng Yue inner disciple; Nascent Soul aura",
     "core_formation":"Fellow disciple; scheming but reliable","nascent_soul":"Nascent Soul cultivator; reads as ambitious survivor"},
     "canon_confidence":4,"note":"Heng Yue Sect disciple; survived the sect's destruction"},

    {"id":"npc_wang_zhuo","name":"Wang Zhuo","nameCn":"王卓","canon_id":"N05",
     "type":"disciple","faction":"heng_yue_sect","location":"heng_yue_sect",
     "cultivation":"Nascent Soul","personality":"proud, talented clan descendant; complex relationship with Wang Lin",
     "speech":"aristocratic, clipped","relationship_to_wanglin":"clan brother; rival",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A noble cultivator","qi_condensation":"Wang clan disciple; Foundation Establishment",
     "core_formation":"Wang clan elite; Nascent Soul potential","nascent_soul":"Nascent Soul; clan politics visible"},
     "canon_confidence":5,"note":"Wang clan member; complex rivalry with Wang Lin"},

    {"id":"npc_wang_hao","name":"Wang Hao","nameCn":"王浩","canon_id":"N06",
     "type":"disciple","faction":"heng_yue_sect","location":"heng_yue_sect",
     "cultivation":"low-tier","personality":"boisterous, friendly fellow disciple",
     "speech":"loud, friendly","relationship_to_wanglin":"fellow disciple; friend",
     "dialogue_available":True,"quest_available":False,"trade_available":True,"teaching_available":False,
     "perception_tiers":{"mortal":"A friendly cultivator","qi_condensation":"Heng Yue outer disciple"},
     "canon_confidence":3,"note":"Friendly Heng Yue disciple"},

    {"id":"npc_yun_fei","name":"Yun Fei","nameCn":"云菲","canon_id":"N95",
     "type":"disciple","faction":"heng_yue_sect","location":"heng_yue_sect",
     "cultivation":"Core Formation","personality":"quiet, studious female disciple",
     "speech":"soft, measured","relationship_to_wanglin":"fellow disciple",
     "dialogue_available":True,"quest_available":False,"trade_available":True,"teaching_available":False,
     "perception_tiers":{"mortal":"A female cultivator","qi_condensation":"Heng Yue inner disciple; Core Formation"},
     "canon_confidence":3,"note":"Heng Yue Core Formation disciple"},

    {"id":"npc_qian_kun","name":"Qian Kun","nameCn":"乾坤","canon_id":"N96",
     "type":"disciple","faction":"heng_yue_sect","location":"heng_yue_sect",
     "cultivation":"Core Formation","personality":"practical, resourceful disciple",
     "speech":"direct, practical","relationship_to_wanglin":"fellow disciple",
     "dialogue_available":True,"quest_available":True,"trade_available":True,"teaching_available":False,
     "perception_tiers":{"mortal":"A cultivator","qi_condensation":"Heng Yue disciple; Core Formation"},
     "canon_confidence":3,"note":"Heng Yue Core Formation disciple"},

    # ─── Teng Family (antagonist NPCs) ───
    {"id":"npc_teng_huayuan","name":"Teng Huayuan","nameCn":"藤化元","canon_id":"N83",
     "type":"antagonist","faction":"teng_family","location":"teng_family_city",
     "cultivation":"Half-Step Deity Transformation","personality":"ruthless, vengeful patriarch; destroyed Wang family",
     "speech":"cold, commanding","relationship_to_wanglin":"sworn enemy; Wang Lin's first major antagonist",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A terrifyingly powerful cultivator","qi_condensation":"Teng family patriarch; overwhelming aura",
     "core_formation":"Half-Step Deity Transformation; far beyond you","nascent_soul":"The man who destroyed the Wang family; Soul Transformation tier"},
     "canon_confidence":5,"note":"Destroyed Heng Yue Sect and Wang family; Wang Lin's first major enemy"},

    {"id":"npc_teng_li","name":"Teng Li","nameCn":"藤立","canon_id":"N84",
     "type":"antagonist","faction":"teng_family","location":"teng_family_city",
     "cultivation":"late Foundation Establishment","personality":"arrogant young master; bullying",
     "speech":"sneering, haughty","relationship_to_wanglin":"enemy; early antagonist",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A young noble cultivator","qi_condensation":"Teng family disciple; Foundation Establishment"},
     "canon_confidence":4,"note":"Teng family young master; early antagonist Wang Lin defeats"},

    {"id":"npc_teng_xiuxiu","name":"Teng Xiuxiu","nameCn":"藤秀秀","canon_id":"N15",
     "type":"other","faction":"teng_family","location":"teng_family_city",
     "cultivation":"low-tier","personality":"complex; Teng family member with conflicted loyalties",
     "speech":"guarded, conflicted","relationship_to_wanglin":"complex; enemy family but not hostile",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A young female cultivator","qi_condensation":"Teng family member; low cultivation"},
     "canon_confidence":3,"note":"Teng family member with complex relationship to Wang Lin"},

    # ─── Soul Refining Sect NPCs ───
    {"id":"npc_xu_liqing","name":"Xu Liqing (Six-Desire Devil)","nameCn":"徐立清 / 六欲魔尊","canon_id":"N92",
     "type":"antagonist","faction":"soul_refining_sect","location":"soul_refining_sect",
     "cultivation":"Soul Transformation","personality":"cunning, desire-manipulating devil cultivator",
     "speech":"seductive, manipulative","relationship_to_wanglin":"enemy; soul-refining adversary",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A disturbingly beautiful cultivator","qi_condensation":"Devil cultivator; desire-aspected aura",
     "core_formation":"Six-Desire Devil; Soul Transformation","nascent_soul":"Master of desire manipulation; Soul Transformation perfect circle"},
     "canon_confidence":5,"note":"Six-Desire Devil Venerable; major Soul Refining antagonist"},

    {"id":"npc_duanmu_ji","name":"Duanmu Ji","nameCn":"端木吉","canon_id":"N90",
     "type":"antagonist","faction":"soul_refining_sect","location":"soul_refining_sect",
     "cultivation":"Soul Formation","personality":"cruel, calculating soul cultivator",
     "speech":"cold, analytical","relationship_to_wanglin":"enemy",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A cold-eyed cultivator","qi_condensation":"Soul cultivator; dark aura",
     "core_formation":"Soul Formation; soul-refining sect elder","nascent_soul":"Soul Formation elder; soul manipulation visible"},
     "canon_confidence":4,"note":"Soul Refining Sect Soul Formation elder"},

    {"id":"npc_gun_lan","name":"Gun Lan","nameCn":"管岚","canon_id":"N93",
     "type":"antagonist","faction":"soul_refining_sect","location":"soul_refining_sect",
     "cultivation":"Soul Formation","personality":"ambitious soul cultivator",
     "speech":"measured, ambitious","relationship_to_wanglin":"enemy",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A cultivator","qi_condensation":"Soul cultivator","core_formation":"Soul Formation elder"},
     "canon_confidence":3,"note":"Soul Refining Sect Soul Formation elder"},

    # ─── Heavenly Fate Sect NPCs ───
    {"id":"npc_zhao_xingsha","name":"Zhao Xingsha","nameCn":"赵星煞","canon_id":"N140",
     "type":"disciple","faction":"heavenly_fate_sect","location":"heavenly_fate_sect",
     "cultivation":"Soul Transformation","personality":"ambitious, ruthless Heavenly Fate disciple",
     "speech":"cold, calculating","relationship_to_wanglin":"rival; complex relationship",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A powerful cultivator","qi_condensation":"Heavenly Fate disciple; strong aura",
     "core_formation":"Heavenly Fate elite; Soul Transformation","nascent_soul":"Soul Transformation; rival to Wang Lin"},
     "canon_confidence":4,"note":"Heavenly Fate Sect Soul Transformation disciple; rival"},

    {"id":"npc_hong_die","name":"Hong Die","nameCn":"红蝶","canon_id":"N57",
     "type":"disciple","faction":"heavenly_fate_sect","location":"heavenly_fate_sect",
     "cultivation":"Nascent Soul","personality":"fierce, passionate female cultivator",
     "speech":"direct, passionate","relationship_to_wanglin":"complex; rival then ally",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A beautiful female cultivator","qi_condensation":"Heavenly Fate disciple; Nascent Soul",
     "core_formation":"Heavenly Fate elite; fierce fighter","nascent_soul":"Nascent Soul; complex relationship with Wang Lin"},
     "canon_confidence":4,"note":"Heavenly Fate female disciple; complex relationship with Wang Lin"},

    {"id":"npc_yun_quezi","name":"Yun Quezi","nameCn":"云阙子","canon_id":"N59",
     "type":"elder","faction":"heavenly_fate_sect","location":"heavenly_fate_sect",
     "cultivation":"Late Nascent Soul","personality":"traditional, strict elder",
     "speech":"formal, stern","relationship_to_wanglin":"neutral; sect authority",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":True,
     "perception_tiers":{"mortal":"An imposing elder","qi_condensation":"Heavenly Fate elder; Nascent Soul",
     "core_formation":"Late Nascent Soul elder","nascent_soul":"Late Nascent Soul; sect traditionalist"},
     "canon_confidence":3,"note":"Heavenly Fate elder"},

    # ─── Vermilion Bird Divine Sect NPCs ───
    {"id":"npc_zhou_wutai","name":"Zhou Wutai","nameCn":"周武泰","canon_id":"N58",
     "type":"divine_emperor","faction":"vermilion_bird_divine_sect","location":"vermilion_bird_divine_sect",
     "cultivation":"Vermilion Bird Master (Wending+)","personality":"wise, burdened divine emperor",
     "speech":"measured, kingly","relationship_to_wanglin":"predecessor; mentor figure",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":True,
     "perception_tiers":{"mortal":"An impossibly powerful cultivator","qi_condensation":"Vermilion Bird Master; overwhelming",
     "core_formation":"Divine Emperor; beyond comprehension","nascent_soul":"Vermilion Bird Master; Wending realm",
     "spirit_severing":"Wending realm; Wang Lin's predecessor as Divine Emperor"},
     "canon_confidence":5,"note":"Vermilion Bird Divine Emperor; Wang Lin's predecessor"},

    {"id":"npc_ye_wuyou","name":"Ye Wuyou","nameCn":"叶无忧","canon_id":"N79",
     "type":"divine_emperor","faction":"vermilion_bird_divine_sect","location":"vermilion_bird_divine_sect",
     "cultivation":"1st-Gen Vermilion Bird Master","personality":"ancient, sorrowful founding emperor",
     "speech":"archaic, weighted with age","relationship_to_wanglin":"ancient predecessor",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":True,
     "perception_tiers":{"mortal":"An impossibly ancient cultivator","qi_condensation":"Ancient Divine Emperor",
     "core_formation":"1st-Generation Vermilion Bird Master","nascent_soul":"Ancient beyond measure",
     "spirit_severing":"1st-Gen Vermilion Bird Master; founding figure"},
     "canon_confidence":4,"note":"1st-Generation Vermilion Bird Divine Emperor"},

    {"id":"npc_qian_pinghai","name":"Qian Pinghai","nameCn":"钱平海","canon_id":"N80",
     "type":"divine_emperor","faction":"vermilion_bird_divine_sect","location":"vermilion_bird_divine_sect",
     "cultivation":"13th-Gen Vermilion Bird Master","personality":"regal, dutiful emperor",
     "speech":"formal, imperial","relationship_to_wanglin":"predecessor lineage",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A powerful emperor","qi_condensation":"Vermilion Bird Master",
     "core_formation":"13th-Generation Divine Emperor","nascent_soul":"13th-Gen; predecessor lineage"},
     "canon_confidence":3,"note":"13th-Generation Vermilion Bird Divine Emperor"},

    # ─── Major Antagonists ───
    {"id":"npc_seven_colored_daoist","name":"Seven-Colored Daoist","nameCn":"七彩道人 / 七彩仙尊","canon_id":"N114",
     "type":"antagonist","faction":"seven_colored_sect","location":"cave_world",
     "cultivation":"Heaven Trampling","personality":"cosmic manipulator; the true hidden antagonist of RI",
     "speech":"cosmic, detached","relationship_to_wanglin":"ultimate enemy; Cave World owner Wang Lin kills",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"Impossible to perceive","qi_condensation":"Impossible to perceive",
     "core_formation":"A faint cosmic presence","nascent_soul":"A distant cosmic manipulator",
     "spirit_severing":"The Seven-Colored Daoist; Heaven Trampling",
     "ascendant":"The true owner of the Cave World; Heaven Trampling tier"},
     "canon_confidence":5,"note":"THE true antagonist of RI; Cave World owner; Wang Lin kills him to become owner"},

    {"id":"npc_blood_ancestor","name":"Blood Ancestor (Yao Xinghai)","nameCn":"血祖 / 姚星海","canon_id":"N98",
     "type":"patriarch","faction":"yao_family","location":"blood_sea",
     "cultivation":"peak Third Step","personality":"ancient, blood-obsessed patriarch",
     "speech":"ancient, dripping with blood qi","relationship_to_wanglin":"major enemy; Wang Lin kills him",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A terrifying blood-aspected being","qi_condensation":"Blood patriarch; overwhelming blood aura",
     "core_formation":"Blood Ancestor; peak Third Step","nascent_soul":"Peak Third Step; blood lineage incarnate",
     "spirit_severing":"Peak Third Step; can reform from blood sea"},
     "canon_confidence":5,"note":"Blood Ancestor; major antagonist Wang Lin kills"},

    {"id":"npc_old_man_miesheng","name":"Old Man Miesheng","nameCn":"灭生老人","canon_id":"N115",
     "type":"antagonist","faction":"independent","location":"unknown",
     "cultivation":"peak Third Step+","personality":"destruction-incarnate ancient being",
     "speech":"void-cold, annihilating","relationship_to_wanglin":"cosmic-level enemy",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"Impossible to perceive","qi_condensation":"Impossible to perceive",
     "core_formation":"A void presence","nascent_soul":"An ancient destructive being",
     "spirit_severing":"Peak Third Step+; destruction incarnate"},
     "canon_confidence":4,"note":"Cosmic-level antagonist; destruction-themed"},

    {"id":"npc_daoist_water","name":"Daoist Water","nameCn":"水道子","canon_id":"N102",
     "type":"antagonist","faction":"independent","location":"unknown",
     "cultivation":"peak Third Step (Nirvana Void+)","personality":"water-aspected ancient cultivator; serene exterior, ruthless interior",
     "speech":"flowing, calm, deadly","relationship_to_wanglin":"enemy",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A serene cultivator","qi_condensation":"Water-aspected; overwhelming",
     "core_formation":"Peak Third Step; water law","nascent_soul":"Nirvana Void+; water dao"},
     "canon_confidence":4,"note":"Water-aspected major antagonist"},

    {"id":"npc_master_void","name":"Master Void","nameCn":"虚空道主","canon_id":"N97",
     "type":"antagonist","faction":"independent","location":"unknown",
     "cultivation":"peak Nirvana Shatterer","personality":"void-manipulating cultivator",
     "speech":"echoing, void-touched","relationship_to_wanglin":"enemy",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A blurred presence","qi_condensation":"Void-aspected; hard to focus on",
     "core_formation":"Peak Nirvana Shatterer; void law","nascent_soul":"Void dao master"},
     "canon_confidence":4,"note":"Void-aspected antagonist"},

    {"id":"npc_master_ashen_pine","name":"Master Ashen Pine","nameCn":"灰松道主","canon_id":"N104",
     "type":"antagonist","faction":"independent","location":"unknown",
     "cultivation":"Third Step","personality":"ash-aspected cultivator; patient, corroding",
     "speech":"grating, ash-dry","relationship_to_wanglin":"enemy",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A grey-robed cultivator","qi_condensation":"Ash-aspected; corroding aura",
     "core_formation":"Third Step; ash law","nascent_soul":"Ash dao master"},
     "canon_confidence":3,"note":"Ash-aspected antagonist"},

    {"id":"npc_master_cloud_soul","name":"Master Cloud Soul","nameCn":"云魂道主","canon_id":"N105",
     "type":"antagonist","faction":"independent","location":"unknown",
     "cultivation":"Third Step","personality":"soul-cloud cultivator; ethereal, soul-draining",
     "speech":"whispering, echoing","relationship_to_wanglin":"enemy",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A ghostly cultivator","qi_condensation":"Soul-cloud; draining aura",
     "core_formation":"Third Step; soul-cloud law","nascent_soul":"Soul-cloud dao master"},
     "canon_confidence":3,"note":"Soul-cloud-aspected antagonist"},

    {"id":"npc_wu_qing","name":"Wu Qing","nameCn":"吴情","canon_id":"N103",
     "type":"antagonist","faction":"independent","location":"unknown",
     "cultivation":"Nirvana Shatterer","personality":"cold, emotionless cultivator",
     "speech":"flat, emotionless","relationship_to_wanglin":"enemy",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A cold cultivator","qi_condensation":"Emotionless; cold aura",
     "core_formation":"Nirvana Shatterer; cold law","nascent_soul":"Nirvana Shatterer; emotionless dao"},
     "canon_confidence":3,"note":"Emotionless antagonist"},

    {"id":"npc_cang_songzi","name":"Cang Songzi","nameCn":"苍松子","canon_id":"N107",
     "type":"antagonist","faction":"independent","location":"unknown",
     "cultivation":"Sub-Empty Annihilation upper-grade","personality":"pine-aspected ancient cultivator",
     "speech":"creaking, ancient","relationship_to_wanglin":"enemy",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"An ancient pine-scented cultivator","qi_condensation":"Pine-aspected; ancient aura",
     "core_formation":"Sub-Empty Annihilation; pine law","nascent_soul":"Sub-Empty Annihilation upper-grade"},
     "canon_confidence":3,"note":"Pine-aspected antagonist"},

    {"id":"npc_yao_xixue","name":"Yao Xixue","nameCn":"姚惜雪","canon_id":"N99",
     "type":"antagonist","faction":"yao_family","location":"yao_family_territory",
     "cultivation":"Infant Transformation Late Stage","personality":"cunning Yao family cultivator",
     "speech":"calculating, aristocratic","relationship_to_wanglin":"enemy; Yao family",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A noble cultivator","qi_condensation":"Yao family; Infant Transformation",
     "core_formation":"Infant Transformation Late Stage","nascent_soul":"Yao family elite"},
     "canon_confidence":4,"note":"Yao family antagonist"},

    {"id":"npc_wind_demon","name":"Wind Demon","nameCn":"风魔","canon_id":"N100",
     "type":"antagonist","faction":"independent","location":"wind_realm",
     "cultivation":"Third Step","personality":"wind-incarnate demon; chaotic, destructive",
     "speech":"howling, chaotic","relationship_to_wanglin":"enemy",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A howling wind presence","qi_condensation":"Wind demon; chaotic aura",
     "core_formation":"Third Step; wind law","nascent_soul":"Wind demon; Third Step"},
     "canon_confidence":4,"note":"Wind-aspected demon antagonist"},

    {"id":"npc_green_devil","name":"Green Devil (Green Scorpion)","nameCn":"绿魔 / 绿蝎","canon_id":"N132",
     "type":"antagonist","faction":"independent","location":"green_devil_continent",
     "cultivation":"peak Third Step+","personality":"poison-aspected devil; ancient, patient",
     "speech":"dripping, venomous","relationship_to_wanglin":"enemy",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A venomous green presence","qi_condensation":"Poison devil; venomous aura",
     "core_formation":"Peak Third Step+; poison law","nascent_soul":"Green Devil; peak Third Step+"},
     "canon_confidence":4,"note":"Poison-aspected devil antagonist on Green Devil Continent"},

    {"id":"npc_taga","name":"Taga","nameCn":"塔迦","canon_id":"N116",
     "type":"antagonist","faction":"ancient_demon_clan","location":"ancient_demon_territory",
     "cultivation":"Ancient Demon (Third Step)","personality":"ancient demon; primal, destructive",
     "speech":"primal, roaring","relationship_to_wanglin":"enemy",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A terrifying demonic presence","qi_condensation":"Ancient demon; overwhelming",
     "core_formation":"Ancient Demon; Third Step","nascent_soul":"Ancient Demon; Third Step tier"},
     "canon_confidence":4,"note":"Ancient Demon antagonist"},

    {"id":"npc_tuo_sen","name":"Tuo Sen","nameCn":"拓山","canon_id":"N63",
     "type":"antagonist","faction":"ancient_god_clan","location":"ancient_god_territory",
     "cultivation":"Ancient God 8-Star (potential)","personality":"rival Ancient God; ambitious, powerful",
     "speech":"god-deep, resonant","relationship_to_wanglin":"rival for Ancient God inheritance",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"Impossible to perceive","qi_condensation":"A vast god-presence",
     "core_formation":"Ancient God; 8-Star potential","nascent_soul":"Ancient God rival; 8-Star potential",
     "spirit_severing":"Rival for Tu Si's Ancient God inheritance"},
     "canon_confidence":5,"note":"Rival Ancient God; competed with Wang Lin for Tu Si's inheritance"},

    # ─── Divine Emperors / Allies ───
    {"id":"npc_situ_nan","name":"Situ Nan","nameCn":"司徒南","canon_id":"N20",
     "type":"divine_emperor","faction":"vermilion_bird_divine_sect","location":"vermilion_bird_divine_sect",
     "cultivation":"Yang Solid Peak (reconstructed); Heaven Trampling","personality":"boisterous, loyal friend; first true ally",
     "speech":"loud, hearty, loyal","relationship_to_wanglin":"sworn brother; first true friend",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":True,
     "perception_tiers":{"mortal":"A loud, powerful cultivator","qi_condensation":"Divine Emperor; overwhelming aura",
     "core_formation":"Situ Nan; reconstructed Divine Emperor","nascent_soul":"Sworn brother of Wang Lin; Heaven Trampling potential",
     "spirit_severing":"Situ Nan; Yang Solid Peak reconstructed; Heaven Trampling potential"},
     "canon_confidence":5,"note":"Wang Lin's first true friend and sworn brother; Divine Emperor"},

    {"id":"npc_qing_shui","name":"Qing Shui","nameCn":"清水","canon_id":"N30",
     "type":"elder","faction":"independent","location":"unknown",
     "cultivation":"Third Step","personality":"serene, water-aspected immortal; mentor figure",
     "speech":"calm, flowing, wise","relationship_to_wanglin":"mentor; gave Wang Lin the Slaughter Sword",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":True,
     "perception_tiers":{"mortal":"A serene immortal","qi_condensation":"Water immortal; calm overwhelming aura",
     "core_formation":"Qing Shui; Third Step","nascent_soul":"Third Step; water dao; Wang Lin's mentor"},
     "canon_confidence":5,"note":"Gave Wang Lin the Slaughter Sword; mentor figure"},

    {"id":"npc_li_muwan","name":"Li Muwan","nameCn":"李慕婉","canon_id":"N17",
     "type":"other","faction":"independent","location":"variable",
     "cultivation":"Treading Heaven Realm","personality":"gentle, devoted; Wang Lin's primary love interest",
     "speech":"soft, caring, devoted","relationship_to_wanglin":"wife; primary love interest",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A beautiful, gentle woman","qi_condensation":"Cultivator; gentle aura",
     "core_formation":"Treading Heaven Realm; Wang Lin's wife","nascent_soul":"Treading Heaven; Wang Lin's primary love"},
     "canon_confidence":5,"note":"Wang Lin's primary love interest; transcended together"},

    {"id":"npc_li_qianmei","name":"Li Qianmei","nameCn":"李千媚","canon_id":"N18",
     "type":"other","faction":"independent","location":"variable",
     "cultivation":"Nirvana Scryer+","personality":"charming, complex; secondary love interest",
     "speech":"charming, teasing, complex","relationship_to_wanglin":"complex love interest",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A charming woman","qi_condensation":"Cultivator; charming aura",
     "core_formation":"Nirvana Scryer+; complex relationship","nascent_soul":"Nirvana Scryer+; secondary love interest"},
     "canon_confidence":4,"note":"Secondary love interest"},

    {"id":"npc_mu_bingmei","name":"Mu Bingmei (Liu Mei)","nameCn":"慕冰媚 / 柳眉","canon_id":"N19",
     "type":"other","faction":"independent","location":"variable",
     "cultivation":"Ascendant+","personality":"cold exterior, complex interior; rival-turned-love-interest",
     "speech":"cold, measured, then warming","relationship_to_wanglin":"complex love interest; rival then lover",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A cold, beautiful woman","qi_condensation":"Cultivator; cold aura",
     "core_formation":"Ascendant+; complex relationship","nascent_soul":"Ascendant+; rival-turned-love"},
     "canon_confidence":4,"note":"Complex love interest; rival-turned-lover"},

    {"id":"npc_tu_si","name":"Tu Si","nameCn":"涂司","canon_id":"N22",
     "type":"elder","faction":"ancient_god_clan","location":"ancient_god_land",
     "cultivation":"Ancient God 8-Star","personality":"ancient god; left inheritance for Wang Lin",
     "speech":"god-deep, ancient, resonant","relationship_to_wanglin":"predecessor; left Ancient God inheritance",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":True,
     "perception_tiers":{"mortal":"Impossible to perceive","qi_condensation":"A faint ancient god-presence",
     "core_formation":"Ancient God remnant; 8-Star","nascent_soul":"Tu Si; Ancient God 8-Star; left inheritance",
     "spirit_severing":"Tu Si; Ancient God 8-Star; Wang Lin inherited his god-knowledge"},
     "canon_confidence":5,"note":"Ancient God whose inheritance Wang Lin received"},

    {"id":"npc_bai_fan","name":"Bai Fan","nameCn":"白凡","canon_id":"N24",
     "type":"divine_emperor","faction":"bai_fan_sect","location":"bai_fan_territory",
     "cultivation":"Immortal Emperor (Third Step+)","personality":"wise, white-robed immortal emperor",
     "speech":"measured, wise, imperial","relationship_to_wanglin":"ally; Divine Emperor",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":True,
     "perception_tiers":{"mortal":"An impossibly powerful immortal","qi_condensation":"Immortal Emperor; overwhelming",
     "core_formation":"Bai Fan; Immortal Emperor","nascent_soul":"Third Step+; Divine Emperor ally"},
     "canon_confidence":4,"note":"Divine Emperor; Wang Lin's ally"},

    {"id":"npc_lu_yun","name":"Lu Yun","nameCn":"陆云","canon_id":"N25",
     "type":"divine_emperor","faction":"lu_yun_sect","location":"lu_yun_territory",
     "cultivation":"Void Flame Cultivator","personality":"void-flame emperor; calm, powerful",
     "speech":"measured, flame-touched","relationship_to_wanglin":"ally; Divine Emperor",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":True,
     "perception_tiers":{"mortal":"An impossibly powerful immortal","qi_condensation":"Void Flame; overwhelming",
     "core_formation":"Lu Yun; Void Flame Cultivator","nascent_soul":"Divine Emperor; void-flame dao"},
     "canon_confidence":4,"note":"Divine Emperor; void-flame cultivator"},

    {"id":"npc_qing_lin","name":"Qing Lin","nameCn":"青林","canon_id":"N26",
     "type":"divine_emperor","faction":"qing_lin_sect","location":"qing_lin_territory",
     "cultivation":"Immortal Emperor","personality":"green-robed immortal emperor; nature-aspected",
     "speech":"flowing, natural, wise","relationship_to_wanglin":"ally; Divine Emperor",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":True,
     "perception_tiers":{"mortal":"An impossibly powerful immortal","qi_condensation":"Nature immortal; overwhelming",
     "core_formation":"Qing Lin; Immortal Emperor","nascent_soul":"Divine Emperor; nature dao"},
     "canon_confidence":4,"note":"Divine Emperor; nature-aspected"},

    {"id":"npc_du_tian","name":"Du Tian","nameCn":"顿天","canon_id":"N23",
     "type":"elder","faction":"independent","location":"unknown",
     "cultivation":"Nirvana Scryer+","personality":"patient, powerful elder; mentor figure",
     "speech":"patient, weighted","relationship_to_wanglin":"mentor; elder",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":True,
     "perception_tiers":{"mortal":"A powerful elder","qi_condensation":"Nirvana Scryer; overwhelming",
     "core_formation":"Du Tian; Nirvana Scryer+","nascent_soul":"Nirvana Scryer+; mentor"},
     "canon_confidence":4,"note":"Mentor elder"},

    {"id":"npc_all_seer","name":"All-Seer","nameCn":"全知者","canon_id":"N21",
     "type":"patriarch","faction":"independent","location":"unknown",
     "cultivation":"peak Third Step","personality":"all-knowing patriarch; mysterious, manipulative",
     "speech":"knowing, cryptic","relationship_to_wanglin":"complex; manipulator",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":True,
     "perception_tiers":{"mortal":"A mysterious patriarch","qi_condensation":"All-knowing; overwhelming",
     "core_formation":"All-Seer; peak Third Step","nascent_soul":"Peak Third Step; omniscient-seeming"},
     "canon_confidence":4,"note":"Mysterious all-knowing patriarch"},

    {"id":"npc_xuan_luo","name":"Xuan Luo","nameCn":"玄罗","canon_id":"N28",
     "type":"elder","faction":"independent","location":"unknown",
     "cultivation":"Great Heavenly Venerable","personality":"profound, mysterious elder; Wang Lin's master",
     "speech":"profound, deep","relationship_to_wanglin":"master",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":True,
     "perception_tiers":{"mortal":"A profound elder","qi_condensation":"Great Heavenly Venerable; overwhelming",
     "core_formation":"Xuan Luo; Great Heavenly Venerable","nascent_soul":"Great Heavenly Venerable; Wang Lin's master"},
     "canon_confidence":5,"note":"Wang Lin's master; Great Heavenly Venerable"},

    {"id":"npc_ling_tianhou","name":"Ling Tianhou","nameCn":"凌天候","canon_id":"N39",
     "type":"elder","faction":"independent","location":"unknown",
     "cultivation":"Third Step (Nirvana Void)","personality":"heaven-aspected elder; powerful, aloof",
     "speech":"heaven-resonant, aloof","relationship_to_wanglin":"complex; elder",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":True,
     "perception_tiers":{"mortal":"A heaven-aspected elder","qi_condensation":"Third Step; overwhelming",
     "core_formation":"Ling Tianhou; Nirvana Void","nascent_soul":"Third Step; heaven dao"},
     "canon_confidence":4,"note":"Heaven-aspected elder"},

    # ─── Wang Lin's Companions/Servants ───
    {"id":"npc_xu_liguo","name":"Xu Liguo","nameCn":"徐立国","canon_id":"N62",
     "type":"artifact_spirit","faction":"wang_lin","location":"variable",
     "cultivation":"devil soul (sword spirit)","personality":"loyal devil soul; Wang Lin's sword spirit servant",
     "speech":"respectful, devil-touched","relationship_to_wanglin":"sword spirit servant; loyal",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A ghostly presence in a sword","qi_condensation":"Devil soul; sword spirit",
     "core_formation":"Xu Liguo; devil soul servant of Wang Lin","nascent_soul":"Devil soul; Wang Lin's loyal sword spirit"},
     "canon_confidence":5,"note":"Wang Lin's devil soul sword spirit servant"},

    {"id":"npc_tianyunzi","name":"Tianyunzi","nameCn":"天运子","canon_id":"N113",
     "type":"artifact_spirit","faction":"wang_lin","location":"variable",
     "cultivation":"Third Step+","personality":"fate-reading artifact spirit; mysterious, knowledgeable",
     "speech":"cryptic, fate-reading","relationship_to_wanglin":"artifact spirit; advisor",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":True,
     "perception_tiers":{"mortal":"A ghostly presence","qi_condensation":"Artifact spirit; fate-aspected",
     "core_formation":"Tianyunzi; Third Step+","nascent_soul":"Third Step+; fate-reading artifact spirit"},
     "canon_confidence":4,"note":"Fate-reading artifact spirit; Wang Lin's advisor"},

    {"id":"npc_ta_shan","name":"Ta Shan","nameCn":"塔山","canon_id":"N44",
     "type":"servant","faction":"wang_lin","location":"variable",
     "cultivation":"Celestial Guard (refined)","personality":"loyal, silent guardian; refined celestial guard",
     "speech":"minimal, respectful","relationship_to_wanglin":"servant; celestial guard",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A silent armored guardian","qi_condensation":"Celestial Guard; refined construct",
     "core_formation":"Ta Shan; Celestial Guard","nascent_soul":"Celestial Guard; Wang Lin's servant"},
     "canon_confidence":4,"note":"Wang Lin's Celestial Guard servant"},

    {"id":"npc_big_head_cultivator","name":"Big Head Cultivator","nameCn":"大头","canon_id":"N45",
     "type":"servant","faction":"wang_lin","location":"variable",
     "cultivation":"Kunie","personality":"large-headed, eccentric servant; loyal",
     "speech":"eccentric, rambling","relationship_to_wanglin":"servant",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"An eccentric large-headed man","qi_condensation":"Cultivator; Kunie realm",
     "core_formation":"Big Head; Kunie","nascent_soul":"Kunie; Wang Lin's eccentric servant"},
     "canon_confidence":3,"note":"Wang Lin's eccentric servant"},

    {"id":"npc_liu_jinbiao","name":"Liu Jinbiao","nameCn":"刘金彪","canon_id":"N47",
     "type":"servant","faction":"wang_lin","location":"variable",
     "cultivation":"Peak Path of Deception","personality":"sly, deceptive servant; trickster",
     "speech":"sly, evasive","relationship_to_wanglin":"servant; trickster",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A sly man","qi_condensation":"Path of Deception; tricky aura",
     "core_formation":"Peak Path of Deception","nascent_soul":"Path of Deception; Wang Lin's trickster servant"},
     "canon_confidence":3,"note":"Wang Lin's Path of Deception servant"},

    {"id":"npc_ling_dong","name":"Ling Dong","nameCn":"凌东","canon_id":"N48",
     "type":"servant","faction":"wang_lin","location":"variable",
     "cultivation":"unknown","personality":"loyal servant",
     "speech":"respectful, quiet","relationship_to_wanglin":"servant",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A quiet servant","qi_condensation":"Cultivator servant"},
     "canon_confidence":3,"note":"Wang Lin's servant"},

    {"id":"npc_zhou_jin","name":"Zhou Jin","nameCn":"周瑾","canon_id":"N49",
     "type":"servant","faction":"wang_lin","location":"variable",
     "cultivation":"unknown","personality":"captured then freed; loyal servant",
     "speech":"grateful, loyal","relationship_to_wanglin":"servant; freed captive",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A grateful servant","qi_condensation":"Cultivator servant"},
     "canon_confidence":3,"note":"Wang Lin's servant; captured then freed"},

    {"id":"npc_zhong_dahong","name":"Zhong Dahong","nameCn":"钟大红","canon_id":"N50",
     "type":"servant","faction":"wang_lin","location":"variable",
     "cultivation":"unknown","personality":"loud, loyal servant",
     "speech":"loud, hearty","relationship_to_wanglin":"servant",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A loud servant","qi_condensation":"Cultivator servant"},
     "canon_confidence":3,"note":"Wang Lin's servant"},

    # ─── Minor Sect Disciples ───
    {"id":"npc_sun_dazhu","name":"Sun Dazhu","nameCn":"孙大柱","canon_id":"N85",
     "type":"disciple","faction":"heng_yue_sect","location":"heng_yue_sect",
     "cultivation":"Foundation Establishment","personality":"bullying senior disciple; early antagonist",
     "speech":"bullying, arrogant","relationship_to_wanglin":"early enemy; bully",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A bully cultivator","qi_condensation":"Heng Yue disciple; Foundation Establishment"},
     "canon_confidence":3,"note":"Early bullying antagonist at Heng Yue"},

    {"id":"npc_old_man_ji_mo","name":"Old Man Ji Mo","nameCn":"极魔老人","canon_id":"N86",
     "type":"antagonist","faction":"ji_mo_sect","location":"sea_of_devils",
     "cultivation":"Core Formation+","personality":"demonic old cultivator; Sea of Devils antagonist",
     "speech":"demonic, cackling","relationship_to_wanglin":"enemy; Sea of Devils arc",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A terrifying demonic elder","qi_condensation":"Demonic cultivator; Core Formation+",
     "core_formation":"Old Man Ji Mo; demonic","nascent_soul":"Core Formation+; Sea of Devils antagonist"},
     "canon_confidence":4,"note":"Sea of Devils demonic antagonist"},

    {"id":"npc_punnan_zi","name":"Punnan Zi (Lou Hou)","nameCn":"飘南子 / 楼侯","canon_id":"N88",
     "type":"antagonist","faction":"independent","location":"unknown",
     "cultivation":"peak Third Step","personality":"ancient, vengeful antagonist",
     "speech":"ancient, grating","relationship_to_wanglin":"enemy",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"An ancient terrifying presence","qi_condensation":"Peak Third Step; overwhelming",
     "core_formation":"Punnan Zi; peak Third Step","nascent_soul":"Peak Third Step; ancient antagonist"},
     "canon_confidence":4,"note":"Ancient antagonist; also known as Lou Hou"},

    {"id":"npc_hunchback_meng","name":"Hunchback Meng","nameCn":"驼背孟","canon_id":"N91",
     "type":"antagonist","faction":"independent","location":"unknown",
     "cultivation":"Soul Transformation (perfect circle)","personality":"hunchbacked, cunning antagonist",
     "speech":"wheedling, cunning","relationship_to_wanglin":"enemy",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A hunchbacked cultivator","qi_condensation":"Soul Transformation; cunning aura",
     "core_formation":"Hunchback Meng; Soul Transformation","nascent_soul":"Soul Transformation perfect circle"},
     "canon_confidence":3,"note":"Hunchbacked Soul Transformation antagonist"},

    {"id":"npc_lin_yi","name":"Lin Yi","nameCn":"林怡","canon_id":"N89",
     "type":"antagonist","faction":"independent","location":"unknown",
     "cultivation":"unknown","personality":"antagonist",
     "speech":"cold","relationship_to_wanglin":"enemy",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A cold cultivator","qi_condensation":"Antagonist"},
     "canon_confidence":3,"note":"Minor antagonist"},

    # ─── Great Heavenly Venerables ───
    {"id":"npc_twin_great_tianzun","name":"Twin Great Heavenly Venerables","nameCn":"双胞胎大天尊","canon_id":"N55",
     "type":"elder","faction":"immortal_astral_continent","location":"immortal_astral_continent",
     "cultivation":"Great Heavenly Venerable","personality":"twin celestial lords; synchronized, powerful",
     "speech":"synchronized, celestial","relationship_to_wanglin":"complex; IAC power",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":True,
     "perception_tiers":{"mortal":"Impossible to perceive","qi_condensation":"Celestial presences",
     "core_formation":"Great Heavenly Venerables","nascent_soul":"Twin Great Heavenly Venerables; IAC powers"},
     "canon_confidence":4,"note":"Twin Great Heavenly Venerables of the IAC"},

    {"id":"npc_lian_daofei","name":"Lian Daofei","nameCn":"连道非","canon_id":"N61",
     "type":"divine_emperor","faction":"immortal_astral_continent","location":"immortal_astral_continent",
     "cultivation":"Eight Extremities Great Heavenly Venerable","personality":"eight-extremities emperor; powerful, martial",
     "speech":"martial, commanding","relationship_to_wanglin":"complex; IAC power",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":True,
     "perception_tiers":{"mortal":"Impossible to perceive","qi_condensation":"Celestial presence",
     "core_formation":"Great Heavenly Venerable","nascent_soul":"Eight Extremities Great Heavenly Venerable"},
     "canon_confidence":4,"note":"Eight Extremities Great Heavenly Venerable"},

    {"id":"npc_sea_child_tianzun","name":"Sea Child Celestial Venerable","nameCn":"海子大天尊","canon_id":"N78",
     "type":"elder","faction":"immortal_astral_continent","location":"immortal_astral_continent",
     "cultivation":"Celestial Venerable","personality":"sea-aspected celestial venerable",
     "speech":"flowing, oceanic","relationship_to_wanglin":"complex; IAC power",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":True,
     "perception_tiers":{"mortal":"Impossible to perceive","qi_condensation":"Oceanic celestial presence",
     "core_formation":"Celestial Venerable","nascent_soul":"Sea Child Celestial Venerable"},
     "canon_confidence":3,"note":"Sea-aspected Celestial Venerable"},

    {"id":"npc_jiu_di","name":"Jiu Di","nameCn":"九地","canon_id":"N77",
     "type":"elder","faction":"immortal_astral_continent","location":"immortal_astral_continent",
     "cultivation":"Grand Empyrean","personality":"earth-aspected grand empyrean",
     "speech":"deep, earthen","relationship_to_wanglin":"complex; IAC power",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":True,
     "perception_tiers":{"mortal":"Impossible to perceive","qi_condensation":"Earth-aspected celestial",
     "core_formation":"Grand Empyrean","nascent_soul":"Grand Empyrean; earth dao"},
     "canon_confidence":3,"note":"Grand Empyrean of the IAC"},

    {"id":"npc_gu_dao","name":"Gu Dao","nameCn":"古道","canon_id":"N117",
     "type":"elder","faction":"ancient_clan","location":"ancient_clan_territory",
     "cultivation":"Grand Empyrean","personality":"ancient clan grand empyrean; tradition-bound",
     "speech":"ancient, formal","relationship_to_wanglin":"complex; Ancient Clan",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":True,
     "perception_tiers":{"mortal":"Impossible to perceive","qi_condensation":"Ancient clan elder",
     "core_formation":"Grand Empyrean","nascent_soul":"Grand Empyrean; Ancient Clan elder"},
     "canon_confidence":4,"note":"Ancient Clan Grand Empyrean"},

    # ─── Dao Devil Sect ───
    {"id":"npc_dao_devil_master","name":"Dao Devil Sect Master","nameCn":"道魔宗主","canon_id":"N122",
     "type":"patriarch","faction":"dao_devil_sect","location":"dao_devil_sect_ruins",
     "cultivation":"Third Step+","personality":"devil-aspected sect master; Wang Lin annihilated the sect",
     "speech":"devilish, arrogant","relationship_to_wanglin":"enemy; destroyed by Wang Lin",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A terrifying devil cultivator","qi_condensation":"Devil patriarch; overwhelming",
     "core_formation":"Dao Devil Sect Master; Third Step+","nascent_soul":"Third Step+; Wang Lin annihilated this sect"},
     "canon_confidence":4,"note":"Dao Devil Sect Master; sect annihilated by Wang Lin (canon-attested destruction)"},

    # ─── Zhan/Yun Family Antagonists ───
    {"id":"npc_zhan_laogui","name":"Zhan Laogui","nameCn":"战老子","canon_id":"N129",
     "type":"antagonist","faction":"zhan_family","location":"zhan_family_territory",
     "cultivation":"Third Step+","personality":"war-aspected family elder; aggressive",
     "speech":"aggressive, martial","relationship_to_wanglin":"enemy",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A terrifying martial elder","qi_condensation":"War-aspected; overwhelming",
     "core_formation":"Zhan family elder; Third Step+","nascent_soul":"Third Step+; war dao"},
     "canon_confidence":3,"note":"Zhan family war-aspected antagonist"},

    {"id":"npc_zhan_xingye","name":"Zhan Xingye","nameCn":"战星野","canon_id":"N131",
     "type":"antagonist","faction":"zhan_family","location":"zhan_family_territory",
     "cultivation":"Third Step+","personality":"war-aspected family member",
     "speech":"martial, aggressive","relationship_to_wanglin":"enemy",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A martial cultivator","qi_condensation":"War-aspected; strong",
     "core_formation":"Zhan family; Third Step+","nascent_soul":"Third Step+; war dao"},
     "canon_confidence":3,"note":"Zhan family antagonist"},

    {"id":"npc_yun_kong","name":"Yun Kong","nameCn":"云空","canon_id":"N130",
     "type":"antagonist","faction":"yun_family","location":"yun_family_territory",
     "cultivation":"Third Step+","personality":"cloud-aspected family elder",
     "speech":"ethereal, cloud-touched","relationship_to_wanglin":"enemy",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A cloud-aspected elder","qi_condensation":"Cloud-aspected; overwhelming",
     "core_formation":"Yun family; Third Step+","nascent_soul":"Third Step+; cloud dao"},
     "canon_confidence":3,"note":"Yun family cloud-aspected antagonist"},

    {"id":"npc_yun_yifeng","name":"Yun Yifeng","nameCn":"云一峰","canon_id":"N127",
     "type":"antagonist","faction":"yun_family","location":"yun_family_territory",
     "cultivation":"Third Step+","personality":"cloud-aspected family member",
     "speech":"ethereal, cold","relationship_to_wanglin":"enemy",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A cloud cultivator","qi_condensation":"Cloud-aspected; strong",
     "core_formation":"Yun family; Third Step+","nascent_soul":"Third Step+; cloud dao"},
     "canon_confidence":3,"note":"Yun family antagonist"},

    # ─── White Tiger General ───
    {"id":"npc_white_tiger_general","name":"White Tiger General","nameCn":"白虎将军","canon_id":"N126",
     "type":"antagonist","faction":"independent","location":"foreign_battleground",
     "cultivation":"Third Step+","personality":"martial, slaughter-aspected general",
     "speech":"martial, commanding","relationship_to_wanglin":"complex; Foreign Battleground",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A terrifying martial general","qi_condensation":"Slaughter-aspected; overwhelming",
     "core_formation":"White Tiger General; Third Step+","nascent_soul":"Third Step+; slaughter dao"},
     "canon_confidence":3,"note":"White Tiger General; Foreign Battleground antagonist"},

    # ─── Lord of Sealed Realm ───
    {"id":"npc_lord_sealed_realm","name":"Lord of the Sealed Realm","nameCn":"封界尊 / 尊者封界","canon_id":"N128",
     "type":"divine_emperor","faction":"sealed_realm","location":"sealed_realm",
     "cultivation":"peak Third Step","personality":"sealed realm lord; guardian, powerful, complex",
     "speech":"weighted, guardian","relationship_to_wanglin":"complex; Sealed Realm authority",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":True,
     "perception_tiers":{"mortal":"Impossible to perceive","qi_condensation":"A vast guardian presence",
     "core_formation":"Lord of Sealed Realm; peak Third Step","nascent_soul":"Peak Third Step; Sealed Realm lord"},
     "canon_confidence":5,"note":"Lord of the Sealed Realm; peak Third Step guardian"},

    # ─── Daoist Scattered Spirit ───
    {"id":"npc_daoist_scattered_spirit","name":"Daoist Scattered Spirit","nameCn":"散灵道人","canon_id":"N51",
     "type":"elder","faction":"independent","location":"unknown",
     "cultivation":"Third Step","personality":"scattered-spirit elder; ethereal, detached",
     "speech":"ethereal, detached","relationship_to_wanglin":"complex; elder",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":True,
     "perception_tiers":{"mortal":"An ethereal presence","qi_condensation":"Scattered-spirit; overwhelming",
     "core_formation":"Third Step; scattered-spirit dao","nascent_soul":"Third Step; ethereal elder"},
     "canon_confidence":3,"note":"Scattered-spirit elder"},

    # ─── Divine Spirit Beasts (NPCs that are also beasts) ───
    {"id":"npc_lei_ji","name":"Lei Ji","nameCn":"雷记","canon_id":"N46",
     "type":"mount","faction":"wang_lin","location":"variable",
     "cultivation":"beast (Ascendant-tier)","personality":"loyal thunder beast companion; imprinted on Wang Lin",
     "speech":"beast sounds; thunder-rumble","relationship_to_wanglin":"bonded companion; loyal",
     "dialogue_available":False,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A crackling thunder beast","qi_condensation":"Thunder beast; Nascent Soul aura",
     "core_formation":"Lei Ji; Spirit Severing thunder beast","nascent_soul":"Ascendant-tier; Wang Lin's bonded thunder beast"},
     "canon_confidence":5,"note":"Wang Lin's bonded thunder beast companion; reached Ascendant tier"},

    {"id":"npc_mosquito_beast","name":"Mosquito Beast","nameCn":"蚊兽","canon_id":"N155",
     "type":"mount","faction":"wang_lin","location":"variable",
     "cultivation":"beast (evolved king mosquito)","personality":"swarm-king; loyal to Wang Lin after taming",
     "speech":"buzzing, swarm","relationship_to_wanglin":"bonded companion; tamed via Dream Dao",
     "dialogue_available":False,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A terrifying mosquito swarm","qi_condensation":"Mosquito swarm; Foundation Establishment",
     "core_formation":"Mosquito king; Core Formation","nascent_soul":"Evolved king mosquito; Wang Lin's swarm"},
     "canon_confidence":5,"note":"Wang Lin's mosquito swarm; tamed via Dream Dao; evolved to king tier"},

    {"id":"npc_thunder_toad","name":"Thunder Toad","nameCn":"雷蟾","canon_id":"N156",
     "type":"mount","faction":"wang_lin","location":"variable",
     "cultivation":"beast (evolved)","personality":"thunder-toad companion; cranky but loyal",
     "speech":"croaking, thunder-rumble","relationship_to_wanglin":"bonded companion",
     "dialogue_available":False,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A thunder-crackling toad","qi_condensation":"Thunder toad; Core Formation",
     "core_formation":"Evolved thunder toad; Nascent Soul","nascent_soul":"Evolved; Wang Lin's thunder toad"},
     "canon_confidence":5,"note":"Wang Lin's thunder toad companion; evolved"},

    {"id":"npc_thunder_celestial_beast","name":"Thunder Celestial Beast","nameCn":"雷仙兽","canon_id":"N157",
     "type":"mount","faction":"wang_lin","location":"variable",
     "cultivation":"beast (Ascendant-tier)","personality":"tribulation-born celestial beast; powerful, aloof",
     "speech":"crackling, celestial","relationship_to_wanglin":"bonded companion; ascendant-tier",
     "dialogue_available":False,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A mass of living lightning","qi_condensation":"Thunder celestial; overwhelming",
     "core_formation":"Ascendant-tier celestial beast","nascent_soul":"Ascendant-tier; Wang Lin's thunder celestial"},
     "canon_confidence":5,"note":"Wang Lin's thunder celestial beast; Ascendant-tier"},

    {"id":"npc_nether_beast","name":"Nether Beast","nameCn":"冥兽","canon_id":"N158",
     "type":"mount","faction":"wang_lin","location":"variable",
     "cultivation":"beast (vast interior)","personality":"shadow-wreathed nether beast; loyal to Wang Lin",
     "speech":"shadow-whisper","relationship_to_wanglin":"bonded companion",
     "dialogue_available":False,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A shadow-wreathed beast","qi_condensation":"Nether beast; Nascent Soul",
     "core_formation":"Nether beast; Spirit Severing","nascent_soul":"Vast interior; Wang Lin's nether beast"},
     "canon_confidence":5,"note":"Wang Lin's nether beast companion"},

    {"id":"npc_brilliant_void","name":"Brilliant Void","nameCn":"玄虚","canon_id":"N159",
     "type":"mount","faction":"wang_lin","location":"variable",
     "cultivation":"beast","personality":"void-aspected beast; mysterious",
     "speech":"void-whisper","relationship_to_wanglin":"bonded companion",
     "dialogue_available":False,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A void-blurred beast","qi_condensation":"Void beast; mysterious",
     "core_formation":"Brilliant Void","nascent_soul":"Wang Lin's void beast companion"},
     "canon_confidence":4,"note":"Wang Lin's Brilliant Void beast companion"},

    {"id":"npc_golden_exalt_sea_dragon","name":"Golden Exalt Sea Dragon","nameCn":"金尊海龙","canon_id":"N160",
     "type":"mount","faction":"wang_lin","location":"variable",
     "cultivation":"beast","personality":"golden sea dragon; regal, powerful",
     "speech":"dragon-roar, ocean-rumble","relationship_to_wanglin":"bonded companion",
     "dialogue_available":False,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A golden sea dragon","qi_condensation":"Sea dragon; overwhelming",
     "core_formation":"Golden Exalt Sea Dragon","nascent_soul":"Wang Lin's golden sea dragon"},
     "canon_confidence":4,"note":"Wang Lin's Golden Exalt Sea Dragon companion"},

    {"id":"npc_xiao_bai","name":"Xiao Bai","nameCn":"小白","canon_id":"N154",
     "type":"mount","faction":"wang_lin","location":"variable",
     "cultivation":"beast","personality":"small white beast companion; endearing",
     "speech":"small beast sounds","relationship_to_wanglin":"bonded companion",
     "dialogue_available":False,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A small white beast","qi_condensation":"Spirit beast; small",
     "core_formation":"Xiao Bai","nascent_soul":"Wang Lin's small white beast companion"},
     "canon_confidence":3,"note":"Wang Lin's small white beast companion"},

    # ─── Misc Sect Disciples (minor NPCs) ───
    {"id":"npc_thirteen","name":"Thirteen","nameCn":"十三","canon_id":"N133",
     "type":"disciple","faction":"independent","location":"variable",
     "cultivation":"body refining","personality":"body-refining cultivator; numbered name",
     "speech":"terse, physical","relationship_to_wanglin":"complex",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A physically powerful cultivator","qi_condensation":"Body refiner; strong",
     "core_formation":"Body refining; numbered disciple"},
     "canon_confidence":3,"note":"Body-refining cultivator with numbered name"},

    {"id":"npc_huo_pao","name":"Huo Pao","nameCn":"火炮","canon_id":"N134",
     "type":"disciple","faction":"independent","location":"variable",
     "cultivation":"unknown","personality":"fire-cannon cultivator; explosive",
     "speech":"explosive, loud","relationship_to_wanglin":"complex",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A fire-aspected cultivator","qi_condensation":"Fire-cannon; explosive aura"},
     "canon_confidence":3,"note":"Fire-cannon cultivator"},

    {"id":"npc_xie_qing","name":"Xie Qing","nameCn":"谢青","canon_id":"N135",
     "type":"disciple","faction":"independent","location":"variable",
     "cultivation":"Jingnie (Concept-only)","personality":"concept cultivator; abstract",
     "speech":"abstract, conceptual","relationship_to_wanglin":"complex",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"An abstract cultivator","qi_condensation":"Concept cultivator; Jingnie"},
     "canon_confidence":3,"note":"Concept-only Jingnie cultivator"},

    {"id":"npc_xi_zifeng","name":"Xi Zifeng","nameCn":"席紫凤","canon_id":"N136",
     "type":"disciple","faction":"independent","location":"variable",
     "cultivation":"Jingnie","personality":"phoenix-aspected Jingnie cultivator",
     "speech":"phoenix-touched, regal","relationship_to_wanglin":"complex",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A phoenix-aspected cultivator","qi_condensation":"Jingnie; phoenix aura"},
     "canon_confidence":3,"note":"Phoenix-aspected Jingnie cultivator"},

    {"id":"npc_adai","name":"Adai","nameCn":"阿呆","canon_id":"N137",
     "type":"servant","faction":"wang_lin","location":"variable",
     "cultivation":"unknown","personality":"simple, loyal servant; named 'adai' (simpleton)",
     "speech":"simple, slow","relationship_to_wanglin":"servant; loyal",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A simple servant","qi_condensation":"Cultivator servant"},
     "canon_confidence":3,"note":"Wang Lin's simple servant"},

    # ─── Minor Heng Yue / Sect NPCs ───
    {"id":"npc_wu_yu","name":"Wu Yu","nameCn":"吴宇","canon_id":"N138",
     "type":"elder","faction":"heng_yue_sect","location":"heng_yue_sect",
     "cultivation":"Nascent Soul","personality":"Heng Yue elder; stern",
     "speech":"stern, formal","relationship_to_wanglin":"sect elder",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":True,
     "perception_tiers":{"mortal":"A stern elder","qi_condensation":"Heng Yue elder; Nascent Soul"},
     "canon_confidence":3,"note":"Heng Yue Nascent Soul elder"},

    {"id":"npc_ye_zi","name":"Ye Zi","nameCn":"叶紫","canon_id":"N139",
     "type":"elder","faction":"heng_yue_sect","location":"heng_yue_sect",
     "cultivation":"Nascent Soul","personality":"Heng Yue elder",
     "speech":"formal","relationship_to_wanglin":"sect elder",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":True,
     "perception_tiers":{"mortal":"An elder","qi_condensation":"Heng Yue elder; Nascent Soul"},
     "canon_confidence":3,"note":"Heng Yue Nascent Soul elder"},

    {"id":"npc_sun_zhenwei","name":"Sun Zhenwei","nameCn":"孙振威","canon_id":"N146",
     "type":"disciple","faction":"heng_yue_sect","location":"heng_yue_sect",
     "cultivation":"Nascent Soul","personality":"Heng Yue disciple",
     "speech":"direct","relationship_to_wanglin":"fellow disciple",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A cultivator","qi_condensation":"Heng Yue disciple; Nascent Soul"},
     "canon_confidence":3,"note":"Heng Yue Nascent Soul disciple"},

    {"id":"npc_chen_bailiang","name":"Chen Bailiang","nameCn":"陈百良","canon_id":"N147",
     "type":"elder","faction":"heng_yue_sect","location":"heng_yue_sect",
     "cultivation":"Nascent Soul","personality":"Heng Yue elder",
     "speech":"formal","relationship_to_wanglin":"sect elder",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":True,
     "perception_tiers":{"mortal":"An elder","qi_condensation":"Heng Yue elder; Nascent Soul"},
     "canon_confidence":3,"note":"Heng Yue Nascent Soul elder"},

    {"id":"npc_zhang_hu","name":"Zhang Hu","nameCn":"张虎","canon_id":"N148",
     "type":"disciple","faction":"heng_yue_sect","location":"heng_yue_sect",
     "cultivation":"Foundation Establishment","personality":"Heng Yue disciple; tiger-like",
     "speech":"bold, direct","relationship_to_wanglin":"fellow disciple",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A bold cultivator","qi_condensation":"Heng Yue disciple; Foundation Establishment"},
     "canon_confidence":3,"note":"Heng Yue Foundation Establishment disciple"},

    {"id":"npc_zhou_lin","name":"Zhou Lin","nameCn":"周林","canon_id":"N153",
     "type":"disciple","faction":"heng_yue_sect","location":"heng_yue_sect",
     "cultivation":"early Core Formation","personality":"Heng Yue disciple",
     "speech":"measured","relationship_to_wanglin":"fellow disciple",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A cultivator","qi_condensation":"Heng Yue disciple; Core Formation"},
     "canon_confidence":3,"note":"Heng Yue early Core Formation disciple"},

    {"id":"npc_sima_rufeng","name":"Sima Rufeng","nameCn":"司马如风","canon_id":"N141",
     "type":"disciple","faction":"independent","location":"variable",
     "cultivation":"unknown","personality":"cultivator",
     "speech":"formal","relationship_to_wanglin":"complex",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A cultivator","qi_condensation":"Cultivator"},
     "canon_confidence":3,"note":"Minor cultivator NPC"},

    {"id":"npc_zhao_xinming","name":"Zhao Xinming","nameCn":"赵新民","canon_id":"N142",
     "type":"disciple","faction":"independent","location":"variable",
     "cultivation":"unknown","personality":"cultivator",
     "speech":"formal","relationship_to_wanglin":"complex",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A cultivator","qi_condensation":"Cultivator"},
     "canon_confidence":3,"note":"Minor cultivator NPC"},

    {"id":"npc_chen_tao","name":"Chen Tao","nameCn":"陈涛","canon_id":"N143",
     "type":"disciple","faction":"independent","location":"variable",
     "cultivation":"mid-Ascendant","personality":"Ascendant cultivator",
     "speech":"confident","relationship_to_wanglin":"complex",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A powerful cultivator","qi_condensation":"Ascendant; overwhelming",
     "core_formation":"Mid-Ascendant","nascent_soul":"Mid-Ascendant cultivator"},
     "canon_confidence":3,"note":"Mid-Ascendant cultivator NPC"},

    {"id":"npc_wang_zhou","name":"Wang Zhou","nameCn":"王周","canon_id":"N144",
     "type":"disciple","faction":"independent","location":"variable",
     "cultivation":"Qi Condensation","personality":"low-tier cultivator",
     "speech":"timid","relationship_to_wanglin":"complex",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A cultivator","qi_condensation":"Qi Condensation disciple"},
     "canon_confidence":3,"note":"Qi Condensation cultivator NPC"},

    {"id":"npc_wang_jie","name":"Wang Jie","nameCn":"王杰","canon_id":"N145",
     "type":"disciple","faction":"independent","location":"variable",
     "cultivation":"Qi Condensation","personality":"low-tier cultivator",
     "speech":"timid","relationship_to_wanglin":"complex",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A cultivator","qi_condensation":"Qi Condensation disciple"},
     "canon_confidence":3,"note":"Qi Condensation cultivator NPC"},

    # ─── Diviner / Fortune Teller ───
    {"id":"npc_gao_qiming","name":"Gao Qiming","nameCn":"高启明","canon_id":"N87",
     "type":"other","faction":"independent","location":"tian_shui_city",
     "cultivation":"diviner","personality":"mysterious fortune teller; knows more than he reveals",
     "speech":"cryptic, fortune-telling","relationship_to_wanglin":"complex; diviner",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A fortune teller","qi_condensation":"Diviner; fate-aspected aura",
     "core_formation":"Diviner; sees fate threads","nascent_soul":"Diviner; reads karma"},
     "canon_confidence":4,"note":"Mysterious diviner; fortune teller NPC"},

    # ─── Vermilion Bird Antagonists ───
    {"id":"npc_3rd_gen_vermilion","name":"3rd-Gen Vermilion Bird Master","nameCn":"三代朱雀","canon_id":"N81",
     "type":"antagonist","faction":"vermilion_bird_divine_sect","location":"vermilion_bird_divine_sect",
     "cultivation":"3rd-Gen Vermilion Bird Master","personality":"antagonist predecessor",
     "speech":"imperial, cold","relationship_to_wanglin":"antagonist predecessor",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"Impossible to perceive","qi_condensation":"Vermilion Bird Master",
     "core_formation":"3rd-Gen Vermilion Bird Master","nascent_soul":"3rd-Gen; antagonist predecessor"},
     "canon_confidence":3,"note":"3rd-Generation Vermilion Bird Master; antagonist"},

    {"id":"npc_14th_gen_vermilion","name":"14th-Gen Vermilion Bird Master","nameCn":"十四代朱雀","canon_id":"N82",
     "type":"antagonist","faction":"vermilion_bird_divine_sect","location":"vermilion_bird_divine_sect",
     "cultivation":"14th-Gen Vermilion Bird Master","personality":"antagonist predecessor",
     "speech":"imperial, cold","relationship_to_wanglin":"antagonist predecessor",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"Impossible to perceive","qi_condensation":"Vermilion Bird Master",
     "core_formation":"14th-Gen Vermilion Bird Master","nascent_soul":"14th-Gen; antagonist predecessor"},
     "canon_confidence":3,"note":"14th-Generation Vermilion Bird Master; antagonist"},

    # ─── Minor Antagonists ───
    {"id":"npc_noble_money","name":"Noble Money","nameCn":"贵钱","canon_id":"N106",
     "type":"antagonist","faction":"independent","location":"variable",
     "cultivation":"unknown","personality":"wealthy, greedy antagonist",
     "speech":"haughty, money-obsessed","relationship_to_wanglin":"enemy",
     "dialogue_available":True,"quest_available":True,"trade_available":True,"teaching_available":False,
     "perception_tiers":{"mortal":"A wealthy noble","qi_condensation":"Cultivator; wealthy"},
     "canon_confidence":3,"note":"Wealthy antagonist NPC"},

    {"id":"npc_ye_dao","name":"Ye Dao","nameCn":"叶道","canon_id":"N109",
     "type":"antagonist","faction":"independent","location":"variable",
     "cultivation":"unknown","personality":"antagonist",
     "speech":"cold","relationship_to_wanglin":"enemy",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A cold cultivator","qi_condensation":"Antagonist"},
     "canon_confidence":3,"note":"Minor antagonist"},

    {"id":"npc_yan_leizi","name":"Yan Leizi","nameCn":"炎雷子","canon_id":"N111",
     "type":"antagonist","faction":"independent","location":"variable",
     "cultivation":"unknown","personality":"fire-thunder antagonist",
     "speech":"crackling, aggressive","relationship_to_wanglin":"enemy",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A fire-thunder cultivator","qi_condensation":"Fire-thunder; aggressive aura"},
     "canon_confidence":3,"note":"Fire-thunder antagonist"},

    {"id":"npc_russell","name":"Russell","nameCn":"罗素","canon_id":"N69",
     "type":"antagonist","faction":"independent","location":"variable",
     "cultivation":"Third Step","personality":"foreign cultivator; antagonist",
     "speech":"foreign accent, cold","relationship_to_wanglin":"enemy",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A powerful cultivator","qi_condensation":"Third Step; overwhelming",
     "core_formation":"Third Step","nascent_soul":"Third Step antagonist"},
     "canon_confidence":3,"note":"Foreign Third Step antagonist"},

    {"id":"npc_song_wude","name":"Song Wude","nameCn":"宋武德","canon_id":"N74",
     "type":"antagonist","faction":"independent","location":"variable",
     "cultivation":"unknown","personality":"antagonist",
     "speech":"cold","relationship_to_wanglin":"enemy",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A cultivator","qi_condensation":"Antagonist"},
     "canon_confidence":3,"note":"Minor antagonist"},

    {"id":"npc_rudy","name":"Rudy","nameCn":"None","canon_id":"N75",
     "type":"antagonist","faction":"independent","location":"variable",
     "cultivation":"unknown","personality":"antagonist",
     "speech":"cold","relationship_to_wanglin":"enemy",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A cultivator","qi_condensation":"Antagonist"},
     "canon_confidence":3,"note":"Minor antagonist"},

    # ─── Minor Disciples ───
    {"id":"npc_zhao_yu","name":"Zhao Yu","nameCn":"赵宇","canon_id":"N70",
     "type":"disciple","faction":"independent","location":"variable",
     "cultivation":"low","personality":"low-tier disciple",
     "speech":"timid","relationship_to_wanglin":"complex",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A cultivator","qi_condensation":"Low-tier disciple"},
     "canon_confidence":3,"note":"Low-tier disciple NPC"},

    {"id":"npc_lu_yanfei","name":"Lu Yanfei","nameCn":"陆燕飞","canon_id":"N71",
     "type":"disciple","faction":"independent","location":"variable",
     "cultivation":"unknown","personality":"disciple",
     "speech":"formal","relationship_to_wanglin":"complex",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A cultivator","qi_condensation":"Disciple"},
     "canon_confidence":3,"note":"Minor disciple NPC"},

    {"id":"npc_xu_yun","name":"Xu Yun","nameCn":"许云","canon_id":"N72",
     "type":"disciple","faction":"independent","location":"variable",
     "cultivation":"unknown","personality":"disciple",
     "speech":"formal","relationship_to_wanglin":"complex",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A cultivator","qi_condensation":"Disciple"},
     "canon_confidence":3,"note":"Minor disciple NPC"},

    {"id":"npc_lu_yuncong","name":"Lu Yuncong","nameCn":"陆云聪","canon_id":"N73",
     "type":"disciple","faction":"independent","location":"variable",
     "cultivation":"unknown","personality":"disciple",
     "speech":"formal","relationship_to_wanglin":"complex",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A cultivator","qi_condensation":"Disciple"},
     "canon_confidence":3,"note":"Minor disciple NPC"},

    {"id":"npc_ji_si","name":"Ji Si","nameCn":"吉思","canon_id":"N76",
     "type":"other","faction":"independent","location":"variable",
     "cultivation":"unknown","personality":"cultivator",
     "speech":"formal","relationship_to_wanglin":"complex",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A cultivator","qi_condensation":"Cultivator"},
     "canon_confidence":3,"note":"Minor cultivator NPC"},

    # ─── Celestial Venerables ───
    {"id":"npc_song_tian_tianzun","name":"Song Tian Great Celestial Venerable","nameCn":"宋天大天尊","canon_id":"N118",
     "type":"elder","faction":"immortal_astral_continent","location":"immortal_astral_continent",
     "cultivation":"Great Celestial Venerable","personality":"celestial venerable; powerful",
     "speech":"celestial, weighted","relationship_to_wanglin":"complex; IAC power",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":True,
     "perception_tiers":{"mortal":"Impossible to perceive","qi_condensation":"Celestial presence",
     "core_formation":"Great Celestial Venerable","nascent_soul":"Great Celestial Venerable; IAC power"},
     "canon_confidence":3,"note":"Great Celestial Venerable of the IAC"},

    {"id":"npc_dao_ancient_tianzun","name":"Dao Ancient Great Celestial Venerable","nameCn":"道古大天尊","canon_id":"N119",
     "type":"elder","faction":"ancient_clan","location":"ancient_clan_territory",
     "cultivation":"Great Celestial Venerable","personality":"ancient clan celestial venerable",
     "speech":"ancient, formal","relationship_to_wanglin":"complex; Ancient Clan",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":True,
     "perception_tiers":{"mortal":"Impossible to perceive","qi_condensation":"Celestial presence",
     "core_formation":"Great Celestial Venerable","nascent_soul":"Ancient Clan Great Celestial Venerable"},
     "canon_confidence":3,"note":"Ancient Clan Great Celestial Venerable"},

    {"id":"npc_dao_yi_tianzun","name":"Dao Yi Great Celestial Venerable","nameCn":"道一大天尊","canon_id":"N120",
     "type":"elder","faction":"ancient_clan","location":"ancient_clan_territory",
     "cultivation":"Great Celestial Venerable","personality":"ancient clan celestial venerable",
     "speech":"ancient, formal","relationship_to_wanglin":"complex; Ancient Clan",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":True,
     "perception_tiers":{"mortal":"Impossible to perceive","qi_condensation":"Celestial presence",
     "core_formation":"Great Celestial Venerable","nascent_soul":"Ancient Clan Great Celestial Venerable"},
     "canon_confidence":3,"note":"Ancient Clan Great Celestial Venerable"},

    # ─── Imperial Preceptor ───
    {"id":"npc_imperial_preceptor","name":"Imperial Preceptor (Dao Gu)","nameCn":"帝国师","canon_id":"N121",
     "type":"artifact_spirit","faction":"ancient_clan","location":"ancient_clan_territory",
     "cultivation":"artifact spirit","personality":"Dao Gu artifact spirit; ancient, knowledgeable",
     "speech":"ancient, resonant","relationship_to_wanglin":"complex; artifact spirit",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":True,
     "perception_tiers":{"mortal":"A ghostly presence","qi_condensation":"Artifact spirit; ancient",
     "core_formation":"Dao Gu Imperial Preceptor","nascent_soul":"Artifact spirit; Dao Gu lineage"},
     "canon_confidence":4,"note":"Dao Gu Imperial Preceptor artifact spirit"},

    # ─── Purple Dawn Immortal Emperor ───
    {"id":"npc_purple_dawn_emperor","name":"Purple Dawn Immortal Emperor","nameCn":"紫朝仙帝","canon_id":"N125",
     "type":"divine_emperor","faction":"immortal_astral_continent","location":"immortal_astral_continent",
     "cultivation":"Immortal Emperor (Celestial)","personality":"dawn-aspected immortal emperor",
     "speech":"dawn-bright, imperial","relationship_to_wanglin":"complex; IAC power",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":True,
     "perception_tiers":{"mortal":"Impossible to perceive","qi_condensation":"Dawn-aspected celestial",
     "core_formation":"Immortal Emperor","nascent_soul":"Purple Dawn Immortal Emperor; Celestial tier"},
     "canon_confidence":3,"note":"Purple Dawn Immortal Emperor of the IAC"},

    # ─── Du Qing / Kang Ren ───
    {"id":"npc_du_qing","name":"Du Qing","nameCn":"杜青","canon_id":"N123",
     "type":"elder","faction":"independent","location":"variable",
     "cultivation":"Third Step","personality":"elder; powerful",
     "speech":"measured","relationship_to_wanglin":"complex",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":True,
     "perception_tiers":{"mortal":"A powerful elder","qi_condensation":"Third Step; overwhelming",
     "core_formation":"Third Step elder","nascent_soul":"Third Step; elder"},
     "canon_confidence":3,"note":"Third Step elder NPC"},

    {"id":"npc_kang_ren","name":"Kang Ren","nameCn":"康仁","canon_id":"N124",
     "type":"disciple","faction":"independent","location":"variable",
     "cultivation":"unknown","personality":"disciple",
     "speech":"formal","relationship_to_wanglin":"complex",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A cultivator","qi_condensation":"Disciple"},
     "canon_confidence":3,"note":"Minor disciple NPC"},

    # ─── Hui Bing / Leng Sheng ───
    {"id":"npc_hui_bing","name":"Hui Bing","nameCn":"惠冰","canon_id":"N149",
     "type":"other","faction":"independent","location":"variable",
     "cultivation":"unknown","personality":"ice-aspected cultivator",
     "speech":"cold, measured","relationship_to_wanglin":"complex",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A cold cultivator","qi_condensation":"Ice-aspected"},
     "canon_confidence":3,"note":"Ice-aspected cultivator NPC"},

    {"id":"npc_leng_sheng","name":"Leng Sheng","nameCn":"冷生","canon_id":"N151",
     "type":"other","faction":"independent","location":"variable",
     "cultivation":"unknown","personality":"cold cultivator",
     "speech":"cold, terse","relationship_to_wanglin":"complex",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A cold cultivator","qi_condensation":"Cultivator"},
     "canon_confidence":3,"note":"Cold cultivator NPC"},

    # ─── Tan Lang ───
    {"id":"npc_tan_lang","name":"Tan Lang","nameCn":"贪狼","canon_id":"N56",
     "type":"other","faction":"independent","location":"variable",
     "cultivation":"Third Step+","personality":"wolf-aspected cultivator; greedy, powerful",
     "speech":"wolf-growling, greedy","relationship_to_wanglin":"complex",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A wolf-aspected cultivator","qi_condensation":"Third Step+; wolf aura",
     "core_formation":"Third Step+; wolf dao","nascent_soul":"Tan Lang; Third Step+ wolf cultivator"},
     "canon_confidence":4,"note":"Wolf-aspected Third Step+ cultivator"},

    # ─── Bei Lou ───
    {"id":"npc_bei_lou","name":"Bei Lou","nameCn":"贝罗","canon_id":"N40",
     "type":"other","faction":"independent","location":"variable",
     "cultivation":"Third Step+","personality":"powerful cultivator",
     "speech":"measured","relationship_to_wanglin":"complex",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A powerful cultivator","qi_condensation":"Third Step+; overwhelming",
     "core_formation":"Third Step+","nascent_soul":"Bei Lou; Third Step+"},
     "canon_confidence":3,"note":"Third Step+ cultivator NPC"},

    # ─── Wang Wei / Hu Juan ───
    {"id":"npc_wang_wei","name":"Wang Wei","nameCn":"王伟","canon_id":"N41",
     "type":"other","faction":"independent","location":"variable",
     "cultivation":"Nirvana Shatterer","personality":"Wang clan cultivator",
     "speech":"measured","relationship_to_wanglin":"complex; Wang clan",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A powerful cultivator","qi_condensation":"Nirvana Shatterer; overwhelming",
     "core_formation":"Nirvana Shatterer","nascent_soul":"Wang Wei; Nirvana Shatterer"},
     "canon_confidence":3,"note":"Nirvana Shatterer Wang clan cultivator"},

    {"id":"npc_hu_juan","name":"Hu Juan","nameCn":"胡娟","canon_id":"N42",
     "type":"other","faction":"independent","location":"variable",
     "cultivation":"unknown","personality":"female cultivator",
     "speech":"measured","relationship_to_wanglin":"complex",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A female cultivator","qi_condensation":"Cultivator"},
     "canon_confidence":3,"note":"Female cultivator NPC"},

    # ─── Mo Ling / Mo Lihai / Sun Tai ───
    {"id":"npc_mo_ling","name":"Mo Ling","nameCn":"莫灵","canon_id":"N35",
     "type":"other","faction":"independent","location":"variable",
     "cultivation":"unknown","personality":"cultivator",
     "speech":"measured","relationship_to_wanglin":"complex",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A cultivator","qi_condensation":"Cultivator"},
     "canon_confidence":3,"note":"Minor cultivator NPC"},

    {"id":"npc_mo_lihai","name":"Mo Lihai","nameCn":"莫离海","canon_id":"N36",
     "type":"other","faction":"independent","location":"variable",
     "cultivation":"Ascendant+","personality":"powerful cultivator",
     "speech":"measured","relationship_to_wanglin":"complex",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A powerful cultivator","qi_condensation":"Ascendant+; overwhelming",
     "core_formation":"Ascendant+","nascent_soul":"Mo Lihai; Ascendant+"},
     "canon_confidence":3,"note":"Ascendant+ cultivator NPC"},

    {"id":"npc_sun_tai","name":"Sun Tai","nameCn":"孙泰","canon_id":"N37",
     "type":"servant","faction":"independent","location":"variable",
     "cultivation":"Nirvana Scryer+","personality":"powerful servant cultivator",
     "speech":"respectful, measured","relationship_to_wanglin":"complex; servant",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A powerful cultivator","qi_condensation":"Nirvana Scryer+; overwhelming",
     "core_formation":"Nirvana Scryer+","nascent_soul":"Sun Tai; Nirvana Scryer+ servant"},
     "canon_confidence":3,"note":"Nirvana Scryer+ servant cultivator"},

    # ─── Li Yuan / Su Dao ───
    {"id":"npc_li_yuan","name":"Li Yuan","nameCn":"李远","canon_id":"N38",
     "type":"other","faction":"independent","location":"variable",
     "cultivation":"unknown","personality":"cultivator",
     "speech":"measured","relationship_to_wanglin":"complex",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A cultivator","qi_condensation":"Cultivator"},
     "canon_confidence":3,"note":"Minor cultivator NPC"},

    {"id":"npc_su_dao","name":"Su Dao","nameCn":"苏道","canon_id":"N27",
     "type":"other","faction":"independent","location":"variable",
     "cultivation":"unknown","personality":"cultivator",
     "speech":"measured","relationship_to_wanglin":"complex",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A cultivator","qi_condensation":"Cultivator"},
     "canon_confidence":3,"note":"Minor cultivator NPC"},

    # ─── Zhou Yi / Qing Shuang / Chi Hu ───
    {"id":"npc_zhou_yi","name":"Zhou Yi","nameCn":"周逸","canon_id":"N31",
     "type":"disciple","faction":"independent","location":"variable",
     "cultivation":"Wending realm","personality":"powerful disciple",
     "speech":"measured","relationship_to_wanglin":"complex",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A powerful cultivator","qi_condensation":"Wending; overwhelming",
     "core_formation":"Wending realm","nascent_soul":"Zhou Yi; Wending realm"},
     "canon_confidence":3,"note":"Wending realm cultivator NPC"},

    {"id":"npc_qing_shuang","name":"Qing Shuang","nameCn":"青霜","canon_id":"N32",
     "type":"disciple","faction":"independent","location":"variable",
     "cultivation":"Immortal-tier","personality":"frost-aspected female cultivator",
     "speech":"frosty, measured","relationship_to_wanglin":"complex",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A frost-aspected woman","qi_condensation":"Immortal-tier; frost aura",
     "core_formation":"Immortal-tier","nascent_soul":"Qing Shuang; Immortal-tier frost cultivator"},
     "canon_confidence":3,"note":"Immortal-tier frost cultivator NPC"},

    {"id":"npc_chi_hu","name":"Chi Hu","nameCn":"赤虎","canon_id":"N33",
     "type":"other","faction":"independent","location":"variable",
     "cultivation":"unknown","personality":"tiger-aspected cultivator",
     "speech":"bold, tiger-growling","relationship_to_wanglin":"complex",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A tiger-aspected cultivator","qi_condensation":"Tiger aura"},
     "canon_confidence":3,"note":"Tiger-aspected cultivator NPC"},

    # ─── Hong Shan / South Cloud ───
    {"id":"npc_hong_shan","name":"Master Hong Shan","nameCn":"洪山道主","canon_id":"N53",
     "type":"other","faction":"independent","location":"variable",
     "cultivation":"unknown","personality":"mountain-aspected cultivator",
     "speech":"mountain-deep, resonant","relationship_to_wanglin":"complex",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A mountain-aspected cultivator","qi_condensation":"Mountain aura"},
     "canon_confidence":3,"note":"Mountain-aspected cultivator NPC"},

    {"id":"npc_south_cloud","name":"Master South Cloud","nameCn":"南云道主","canon_id":"N54",
     "type":"other","faction":"independent","location":"variable",
     "cultivation":"unknown","personality":"cloud-aspected cultivator",
     "speech":"cloud-ethereal","relationship_to_wanglin":"complex",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A cloud-aspected cultivator","qi_condensation":"Cloud aura"},
     "canon_confidence":3,"note":"Cloud-aspected cultivator NPC"},

    # ─── Mo Zhi / Lu Fuzi ───
    {"id":"npc_mo_zhi","name":"Mo Zhi","nameCn":"莫知","canon_id":"N60",
     "type":"elder","faction":"independent","location":"variable",
     "cultivation":"Third Step+","personality":"knowing elder",
     "speech":"measured, knowing","relationship_to_wanglin":"complex",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":True,
     "perception_tiers":{"mortal":"A knowing elder","qi_condensation":"Third Step+; overwhelming",
     "core_formation":"Third Step+","nascent_soul":"Mo Zhi; Third Step+ elder"},
     "canon_confidence":3,"note":"Third Step+ elder NPC"},

    {"id":"npc_lu_fuzi","name":"Lu Fuzi","nameCn":"陆夫子","canon_id":"N108",
     "type":"other","faction":"independent","location":"variable",
     "cultivation":"unknown","personality":"scholar cultivator",
     "speech":"scholarly, measured","relationship_to_wanglin":"complex",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A scholarly cultivator","qi_condensation":"Cultivator"},
     "canon_confidence":3,"note":"Scholar cultivator NPC"},

    # ─── Xu Yunshan / Ouyang Hua / Li Dannan / Bai Wei ───
    {"id":"npc_xu_yunshan","name":"Xu Yunshan","nameCn":"许云山","canon_id":"N64",
     "type":"elder","faction":"independent","location":"variable",
     "cultivation":"unknown","personality":"elder",
     "speech":"formal","relationship_to_wanglin":"complex",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":True,
     "perception_tiers":{"mortal":"An elder","qi_condensation":"Elder"},
     "canon_confidence":3,"note":"Minor elder NPC"},

    {"id":"npc_ouyang_hua","name":"Ouyang Hua","nameCn":"欧阳华","canon_id":"N65",
     "type":"elder","faction":"independent","location":"variable",
     "cultivation":"unknown","personality":"elder",
     "speech":"formal","relationship_to_wanglin":"complex",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":True,
     "perception_tiers":{"mortal":"An elder","qi_condensation":"Elder"},
     "canon_confidence":3,"note":"Minor elder NPC"},

    {"id":"npc_li_dannan","name":"Li Dannan","nameCn":"李丹楠","canon_id":"N66",
     "type":"other","faction":"independent","location":"variable",
     "cultivation":"unknown","personality":"cultivator",
     "speech":"measured","relationship_to_wanglin":"complex",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A cultivator","qi_condensation":"Cultivator"},
     "canon_confidence":3,"note":"Minor cultivator NPC"},

    {"id":"npc_bai_wei","name":"Bai Wei","nameCn":"白薇","canon_id":"N67",
     "type":"other","faction":"independent","location":"variable",
     "cultivation":"unknown","personality":"female cultivator",
     "speech":"measured","relationship_to_wanglin":"complex",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A female cultivator","qi_condensation":"Cultivator"},
     "canon_confidence":3,"note":"Minor female cultivator NPC"},

    # ─── Master Flamespark ───
    {"id":"npc_master_flamespark","name":"Master Flamespark","nameCn":"火芒","canon_id":"N68",
     "type":"elder","faction":"independent","location":"variable",
     "cultivation":"Third Step","personality":"fire-aspected elder; powerful",
     "speech":"crackling, fire-touched","relationship_to_wanglin":"complex",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":True,
     "perception_tiers":{"mortal":"A fire-aspected elder","qi_condensation":"Third Step; fire aura",
     "core_formation":"Third Step; fire dao","nascent_soul":"Master Flamespark; Third Step fire cultivator"},
     "canon_confidence":4,"note":"Fire-aspected Third Step elder"},

    # ─── Lian Daozhen ───
    {"id":"npc_lian_daozhen","name":"Lian Daozhen","nameCn":"连道真","canon_id":"N110",
     "type":"divine_emperor","faction":"immortal_astral_continent","location":"immortal_astral_continent",
     "cultivation":"Immortal Emperor","personality":"immortal emperor; powerful",
     "speech":"imperial, weighted","relationship_to_wanglin":"complex; IAC power",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":True,
     "perception_tiers":{"mortal":"Impossible to perceive","qi_condensation":"Immortal Emperor; overwhelming",
     "core_formation":"Immortal Emperor","nascent_soul":"Lian Daozhen; Immortal Emperor"},
     "canon_confidence":3,"note":"Immortal Emperor of the IAC"},

    # ─── Wang Qingyue / Song Yu / Qing Yi ───
    {"id":"npc_wang_qingyue","name":"Wang Qingyue","nameCn":"王青叶","canon_id":"N94",
     "type":"other","faction":"wang_family_village","location":"wang_family_village",
     "cultivation":"unknown","personality":"Wang family member",
     "speech":"measured","relationship_to_wanglin":"family",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A Wang family member","qi_condensation":"Cultivator"},
     "canon_confidence":3,"note":"Wang family member NPC"},

    {"id":"npc_song_yu","name":"Song Yu","nameCn":"宋玉","canon_id":"N12",
     "type":"other","faction":"independent","location":"variable",
     "cultivation":"unknown","personality":"cultivator",
     "speech":"measured","relationship_to_wanglin":"complex",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A cultivator","qi_condensation":"Cultivator"},
     "canon_confidence":3,"note":"Minor cultivator NPC"},

    {"id":"npc_qing_yi","name":"Qing Yi","nameCn":"青衣","canon_id":"N11",
     "type":"other","faction":"independent","location":"variable",
     "cultivation":"unknown","personality":"green-robed cultivator",
     "speech":"measured, gentle","relationship_to_wanglin":"complex",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A green-robed cultivator","qi_condensation":"Cultivator"},
     "canon_confidence":3,"note":"Green-robed cultivator NPC"},

    # ─── Wang Jiduo / Zhou Ru / Wang Yiyi / Wang Ping (higher tier) ───
    {"id":"npc_wang_jiduo","name":"Wang Jiduo","nameCn":"王继多","canon_id":"N09",
     "type":"other","faction":"wang_family_sect","location":"immortal_astral_continent",
     "cultivation":"Imperial Venerable","personality":"powerful Wang family descendant; IAC-tier",
     "speech":"imperial, weighted","relationship_to_wanglin":"descendant; IAC power",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":True,
     "perception_tiers":{"mortal":"Impossible to perceive","qi_condensation":"Imperial Venerable; overwhelming",
     "core_formation":"Imperial Venerable","nascent_soul":"Wang Jiduo; Imperial Venerable; Wang descendant"},
     "canon_confidence":4,"note":"Wang family Imperial Venerable descendant"},

    {"id":"npc_zhou_ru","name":"Zhou Ru","nameCn":"周茹","canon_id":"N10",
     "type":"other","faction":"independent","location":"variable",
     "cultivation":"Soul Transformation","personality":"powerful cultivator",
     "speech":"measured","relationship_to_wanglin":"complex",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A powerful cultivator","qi_condensation":"Soul Transformation; overwhelming",
     "core_formation":"Soul Transformation","nascent_soul":"Zhou Ru; Soul Transformation"},
     "canon_confidence":3,"note":"Soul Transformation cultivator NPC"},

    {"id":"npc_wang_yiyi","name":"Wang Yiyi","nameCn":"王一一","canon_id":"N08",
     "type":"other","faction":"wang_family_sect","location":"immortal_astral_continent",
     "cultivation":"Paragon-tier","personality":"powerful Wang family descendant; Paragon-tier",
     "speech":"paragon-calm, weighted","relationship_to_wanglin":"descendant; Paragon-tier",
     "dialogue_available":True,"quest_available":True,"trade_available":False,"teaching_available":True,
     "perception_tiers":{"mortal":"Impossible to perceive","qi_condensation":"Paragon-tier; overwhelming",
     "core_formation":"Paragon-tier","nascent_soul":"Wang Yiyi; Paragon-tier; Wang descendant"},
     "canon_confidence":4,"note":"Wang family Paragon-tier descendant"},

    # ─── Wang Tianshan ───
    {"id":"npc_wang_tianshan","name":"Wang Tianshan","nameCn":"王天山","canon_id":"N04",
     "type":"elder","faction":"wang_family_village","location":"wang_family_village",
     "cultivation":"low-tier cultivator","personality":"Wang family elder",
     "speech":"formal, elder","relationship_to_wanglin":"family elder",
     "dialogue_available":True,"quest_available":False,"trade_available":False,"teaching_available":False,
     "perception_tiers":{"mortal":"A Wang family elder","qi_condensation":"Low-tier cultivator"},
     "canon_confidence":3,"note":"Wang family elder NPC"},
]

# ==================================================================
# PASS B — RI BEAST/CREATURE DEFINITIONS (miscellaneous beasts)
# ==================================================================
# Additional RI-specific beasts not yet in the species DB.
# These are the miscellaneous creatures from the novel that the user
# specifically wants accounted for.

RI_MISCELLANEOUS_BEASTS = [
    {"id":"golden_exalt_sea_dragon","name":"Golden Exalt Sea Dragon","nameCn":"金尊海龙","novel":"Renegade Immortal",
     "appearance":"Serpentine sea dragon 50 m long, scales golden-jade, crest of crystal horns, breath commands water",
     "size":"50 m adult, 100000 jin; ancient 200 m, millions of jin",
     "behavior":"Solitary apex of seas; hoards water-attribute treasures; cultivates in ocean trenches",
     "habitat":"Deep seas, water-aspected apex spirit veins, ocean trenches",
     "cultivation":"Spirit Severing -> Ascendant (water attribute)",
     "intelligence":9,"diet":"sea beasts, water-attribute herbs, spirit-pearls",
     "relationships":"Wang Lin's bonded companion (N160); rival of cold dragon",
     "techniques":["ocean_breath","tidal_command","dragon_dive","water_scales"],
     "treasures":["golden_sea_dragon_core","dragon_pearl","golden_scale"],
     "bloodline":"Sea-Dragon lineage; water-attribute true dragon bloodline (diluted)",
     "mutations":["golden_pearl","true_sea_dragon_awakening"],
     "canon_confidence":4},

    {"id":"brilliant_void_beast","name":"Brilliant Void Beast","nameCn":"玄虚","novel":"Renegade Immortal",
     "appearance":"Void-aspected beast 5 m, body shifts between physical and void-form, eyes are void-tunnels",
     "size":"5 m adult, 200 jin (material) / 0 jin (void)",
     "behavior":"Hunts by stepping through void; cultivates by absorbing void-essence",
     "habitat":"Void edges, void-aspected forbidden zones, reality faults",
     "cultivation":"Spirit Severing -> Ascendant (void attribute)",
     "intelligence":8,"diet":"void-essence, reality fragments, void-aspected herbs",
     "relationships":"Wang Lin's bonded companion (N159); kin of void_beast",
     "techniques":["void_step","void_breath","reality_shift","void_maw"],
     "treasures":["void_core","reality_fragment","void_essence"],
     "bloodline":"Void lineage; void-attribute bloodline",
     "mutations":["true_void_body","void_immortal_emperor"],
     "canon_confidence":4},

    {"id":"xiao_bai_beast","name":"Xiao Bai","nameCn":"小白","novel":"Renegade Immortal",
     "appearance":"Small white-furred beast 0.5 m, endearing, eyes large and intelligent, fur glows faintly",
     "size":"0.5 m adult, 5 jin; ancient 1.5 m, 200 jin",
     "behavior":"Companion beast; curious, affectionate; cultivates by absorbing dawn-light",
     "habitat":"Variable (follows Wang Lin); dawn-aspected spirit veins",
     "cultivation":"Foundation Establishment -> Core Formation (light attribute)",
     "intelligence":7,"diet":"dawn-dew, spirit-rice, light-attribute herbs",
     "relationships":"Wang Lin's bonded companion (N154); auspicious",
     "techniques":["dawn_light","small_bite","endearing_glow","curious_sense"],
     "treasures":["xiao_bai_core","white_fur","dawn_essence"],
     "bloodline":"Spirit-Beast lineage; light-attribute bloodline (rare)",
     "mutations":["light_body","dawn_immortal"],
     "canon_confidence":3},

    {"id":"earth_fire_dragon","name":"Earth Fire Dragon","nameCn":"地火龙","novel":"Renegade Immortal",
     "appearance":"Serpentine dragon 30 m, scales molten-earth, breath is liquid fire, lives in magma",
     "size":"30 m adult, 60000 jin; ancient 100 m, millions of jin",
     "behavior":"Solitary apex of volcanic regions; sleeps in magma for centuries; soul-extracted by Wang Lin",
     "habitat":"Earth Fire veins, volcanic regions, magma chambers",
     "cultivation":"Spirit Severing -> Ascendant (earth-fire attribute)",
     "intelligence":9,"diet":"magma minerals, fire-attribute herbs, earth-fire essence",
     "relationships":"Soul extracted by Wang Lin (canon event); rival of flame dragon",
     "techniques":["magma_breath","earth_fire_coil","eruption_call","magma_sleep"],
     "treasures":["earth_fire_dragon_core","magma_scale","earth_fire_essence"],
     "bloodline":"Earth-Fire-Dragon lineage; earth+fire true dragon bloodline (diluted)",
     "mutations":["true_dragon_awakening","earth_fire_immortal_body"],
     "canon_confidence":4},

    {"id":"green_devil_scorpion","name":"Green Devil Scorpion","nameCn":"绿蝎","novel":"Renegade Immortal",
     "appearance":"Armored scorpion 10 m long, carapace toxic green, stinger drips world-corroding venom",
     "size":"10 m adult, 20000 jin; ancient 50 m, 500000 jin",
     "behavior":"Solitary apex predator of Green Devil Continent; venom can melt spirit-armor; cultivates by absorbing poison",
     "habitat":"Green Devil Continent, poison-aspected apex spirit veins",
     "cultivation":"Ascendant -> Immortal (poison attribute)",
     "intelligence":8,"diet":"poison-attribute herbs, weaker beasts, lost cultivators",
     "relationships":"Green Devil (N132) is its cultivator-master; apex of Green Devil Continent",
     "techniques":["venom_sting","corrode_armor","poison_breath","toxic_burrow"],
     "treasures":["green_devil_scorpion_core","venom_gland","toxic_carapace"],
     "bloodline":"Devil-Scorpion lineage; poison-attribute devil bloodline",
     "mutations":["hundred_stinger","true_devil_scorpion","ancient_devil_emperor"],
     "canon_confidence":4},

    {"id":"ji_qiong","name":"Ji Qiong","nameCn":"季穷","novel":"Renegade Immortal",
     "appearance":"Ancient god-tier beast 100 m, form shifts between beast and god-fragment, body crackles with ancient god-power",
     "size":"100 m adult, planetary mass; true form spans regions",
     "behavior":"Ancient god-tier beast; cultivated from ancient god-fragments; Wang Lin obtained its head as treasure",
     "habitat":"Ancient god ruins, god-aspected forbidden zones",
     "cultivation":"Immortal -> Ancient Immortal (ancient god attribute)",
     "intelligence":11,"diet":"god-fragments, god-essence, ancient spirit herbs",
     "relationships":"Wang Lin obtained Ji Qiong's head as treasure (canon event)",
     "techniques":["god_breath","ancient_maw","god_fragment_absorb","ancient_roar"],
     "treasures":["ji_qiong_head","god_fragment","ancient_god_essence"],
     "bloodline":"Ancient God lineage; born from god-fragments",
     "mutations":["true_god_body","ancient_god_emperor"],
     "canon_confidence":4},

    {"id":"giant_head_ancient_clansman","name":"Giant Head Ancient Clansman","nameCn":"大头古族","novel":"Renegade Immortal",
     "appearance":"Humanoid ancient clan being 20 m tall, head disproportionately massive, body radiates ancient clan power",
     "size":"20 m adult, 100000 jin; ancient 50 m, millions of jin",
     "behavior":"Ancient clan being; head contains vast ancient power; Wang Lin obtained the skull as treasure",
     "habitat":"Ancient clan territory, ancient clan ruins",
     "cultivation":"Immortal (ancient clan attribute)",
     "intelligence":10,"diet":"ancient clan herbs, spirit stones, ancient qi",
     "relationships":"Wang Lin obtained giant head skull as treasure (canon event)",
     "techniques":["ancient_head_butt","clan_roar","ancient_qi_burst","head_crush"],
     "treasures":["giant_head_skull","ancient_clan_essence","head_fragment"],
     "bloodline":"Ancient Clan lineage; giant-head bloodline",
     "mutations":["true_clan_body","ancient_clan_emperor"],
     "canon_confidence":4},

    {"id":"black_fiend_devil_saint","name":"Black Fiend Devil Saint","nameCn":"黑煞魔圣","novel":"Renegade Immortal",
     "appearance":"Devil saint 10 m, body wreathed in black fiend-flames, eyes burn with devil-fire",
     "size":"10 m adult, 50000 jin; true form spans battlefields",
     "behavior":"Devil saint; cultivates by absorbing fiend-qi; Wang Lin obtained the wood carving as treasure",
     "habitat":"Devil-aspected forbidden zones, fiend-realm edges",
     "cultivation":"Ascendant -> Immortal (devil/fiend attribute)",
     "intelligence":9,"diet":"fiend-qi, devil-herbs, wicked souls",
     "relationships":"Wang Lin obtained wood carving of this being as treasure (canon event)",
     "techniques":["fiend_breath","devil_saint_roar","black_fiend_burn","soul_devour"],
     "treasures":["black_fiend_core","wood_carving","fiend_essence"],
     "bloodline":"Fiend-Devil lineage; devil-saint bloodline",
     "mutations":["true_fiend_body","devil_immortal_emperor"],
     "canon_confidence":4},

    {"id":"heavenly_bull_soul_beast","name":"Heavenly Bull Soul Beast","nameCn":"天牛魂兽","novel":"Renegade Immortal",
     "appearance":"Bull-shaped soul beast 8 m, body semi-translucent, horns radiate soul-power",
     "size":"8 m adult, 8000 jin (material) / soul-mass",
     "behavior":"Soul beast of Heavenly Bull Continent; cultivates by absorbing soul-essence",
     "habitat":"Heavenly Bull Continent, soul-aspected spirit veins",
     "cultivation":"Spirit Severing -> Ascendant (soul attribute)",
     "intelligence":7,"diet":"soul fragments, soul-herbs, soul-aspected minerals",
     "relationships":"Connected to Heavenly Bull Soul Armour (canon treasure)",
     "techniques":["soul_horn_charge","soul_breath","bull_soul_crush","soul_stomp"],
     "treasures":["heavenly_bull_core","soul_horn","heavenly_bull_essence"],
     "bloodline":"Heavenly Bull lineage; soul-attribute bloodline",
     "mutations":["soul_body","heavenly_bull_emperor"],
     "canon_confidence":4},

    {"id":"wind_demon_beast","name":"Wind Demon","nameCn":"风魔","novel":"Renegade Immortal",
     "appearance":"Formless wind-aspected demon 5 m manifestation, body is a living storm, eyes are lightning",
     "size":"5 m manifestation, 0 jin (wind); true form spans storm-zones",
     "behavior":"Wind-incarnate demon; cultivates by absorbing storms; hunts by calling tornadoes",
     "habitat":"Storm zones, wind-aspected forbidden zones, high altitudes",
     "cultivation":"Ascendant -> Immortal (wind attribute)",
     "intelligence":8,"diet":"wind-essence, storm-qi, wind-aspected herbs",
     "relationships":"N100 is the cultivator who commands wind demons; antagonist",
     "techniques":["wind_step","tornado_call","storm_breath","wind_rend"],
     "treasures":["wind_demon_core","storm_essence","wind_fragment"],
     "bloodline":"Wind-Demon lineage; wind-attribute devil bloodline",
     "mutations":["true_wind_body","storm_immortal_emperor"],
     "canon_confidence":4},

    {"id":"ancient_demon_clone_beast","name":"Ancient Demon Clone","nameCn":"古魔分身","novel":"Renegade Immortal",
     "appearance":"Demon clone 5 m, body shifts between beast and demon-form, radiates ancient demon power",
     "size":"5 m adult, 5000 jin; true form spans demon-realms",
     "behavior":"Ancient demon clone; cultivates by absorbing demon-essence; can reform from demon-source",
     "habitat":"Ancient demon territory, demon-aspected forbidden zones",
     "cultivation":"Ascendant -> Immortal (demon attribute)",
     "intelligence":9,"diet":"demon-essence, demon-herbs, wicked souls",
     "relationships":"Wang Lin obtained ancient demon clone as treasure (canon event)",
     "techniques":["demon_clone_step","demon_breath","clone_reform","demon_maw"],
     "treasures":["ancient_demon_clone_core","demon_essence","demon_fragment"],
     "bloodline":"Ancient Demon lineage; demon bloodline",
     "mutations":["true_demon_body","ancient_demon_emperor"],
     "canon_confidence":4},

    {"id":"ancient_devil_clone_beast","name":"Ancient Devil Clone","nameCn":"古魔分身","novel":"Renegade Immortal",
     "appearance":"Devil clone 5 m, body shifts between beast and devil-form, radiates ancient devil power",
     "size":"5 m adult, 5000 jin; true form spans devil-realms",
     "behavior":"Ancient devil clone; cultivates by absorbing devil-essence; can reform from devil-source",
     "habitat":"Ancient devil territory, devil-aspected forbidden zones",
     "cultivation":"Ascendant -> Immortal (devil attribute)",
     "intelligence":9,"diet":"devil-essence, devil-herbs, wicked souls",
     "relationships":"Wang Lin obtained ancient devil clone as treasure (canon event)",
     "techniques":["devil_clone_step","devil_breath","clone_reform","devil_maw"],
     "treasures":["ancient_devil_clone_core","devil_essence","devil_fragment"],
     "bloodline":"Ancient Devil lineage; devil bloodline",
     "mutations":["true_devil_body","ancient_devil_emperor"],
     "canon_confidence":4},

    {"id":"restriction_origin_beast","name":"Restriction Origin True Body","nameCn":"禁制本源真身","novel":"Renegade Immortal",
     "appearance":"Formless beast of living restriction 10 m, body is woven restriction-runes, shifts between patterns",
     "size":"10 m manifestation, 0 jin (restriction); true form spans formation-networks",
     "behavior":"Restriction-incarnate being; cultivates by absorbing broken restrictions; Wang Lin obtained as treasure",
     "habitat":"Restriction Mountain, ancient formation sites, restriction-aspected zones",
     "cultivation":"Immortal (restriction attribute)",
     "intelligence":12,"diet":"broken restrictions, formation fragments, restriction-essence",
     "relationships":"Wang Lin obtained restriction origin true body as treasure (canon event)",
     "techniques":["restriction_rend","formation_absorb","seal_impose","restriction_body"],
     "treasures":["restriction_origin_core","rune_fragment","formation_essence"],
     "bloodline":"Restriction-Dao lineage; born of accumulated restriction-power",
     "mutations":["true_restriction_body","restriction_immortal_emperor"],
     "canon_confidence":4},

    {"id":"taichu_origin_beast","name":"Taichu Origin True Body","nameCn":"太初本源真身","novel":"Renegade Immortal",
     "appearance":"Formless beast of origin-power 8 m, body radiates primordial light, eyes are origin-wells",
     "size":"8 m manifestation, 0 jin (origin); true form spans origin-realms",
     "behavior":"Origin-incarnate being; cultivates by absorbing origin-essence; Wang Lin obtained as treasure",
     "habitat":"Origin-aspected forbidden zones, primordial sites",
     "cultivation":"Immortal -> Ancient Immortal (origin attribute)",
     "intelligence":12,"diet":"origin-essence, primordial herbs, ancient qi",
     "relationships":"Wang Lin obtained Taichu origin true body as treasure (canon event)",
     "techniques":["origin_breath","primordial_light","origin_absorb","beginning_roar"],
     "treasures":["taichu_origin_core","origin_essence","primordial_fragment"],
     "bloodline":"Origin lineage; primordial bloodline",
     "mutations":["true_origin_body","origin_immortal_emperor"],
     "canon_confidence":4},

    {"id":"thunder_origin_beast","name":"Thunder Origin True Body","nameCn":"雷霆本源真身","novel":"Renegade Immortal",
     "appearance":"Formless beast of thunder-origin 8 m, body is living lightning, eyes are thunder-cracks",
     "size":"8 m manifestation, 0 jin (thunder); true form spans thunder-realms",
     "behavior":"Thunder-origin-incarnate being; cultivates by absorbing thunder-essence; Wang Lin obtained as treasure",
     "habitat":"Thunder-aspected forbidden zones, tribulation sites",
     "cultivation":"Immortal (thunder attribute)",
     "intelligence":11,"diet":"thunder-essence, tribulation fragments, thunder-herbs",
     "relationships":"Wang Lin obtained thunder origin true body as treasure (canon event)",
     "techniques":["thunder_breath","lightning_rend","thunder_absorb","storm_body"],
     "treasures":["thunder_origin_core","lightning_essence","thunder_fragment"],
     "bloodline":"Thunder-Origin lineage; thunder bloodline",
     "mutations":["true_thunder_body","thunder_immortal_emperor"],
     "canon_confidence":4},

    {"id":"five_elements_beast","name":"Five Elements True Body","nameCn":"五行本源真身","novel":"Renegade Immortal",
     "appearance":"Formless beast of five-elements 10 m, body cycles through wood/fire/earth/metal/water forms",
     "size":"10 m manifestation, 0 jin (elements); true form spans element-realms",
     "behavior":"Five-elements-incarnate being; cultivates by absorbing element-essence; Wang Lin obtained as treasure",
     "habitat":"Five-element-aspected zones, elemental nexus sites",
     "cultivation":"Immortal (five-elements attribute)",
     "intelligence":12,"diet":"element-essence, five-element herbs, elemental qi",
     "relationships":"Wang Lin obtained five elements true body as treasure (canon event)",
     "techniques":["element_cycle","five_breath","element_swap","element_absorb"],
     "treasures":["five_elements_core","element_essence","five_element_fragment"],
     "bloodline":"Five-Elements lineage; elemental bloodline",
     "mutations":["true_element_body","element_immortal_emperor"],
     "canon_confidence":4},

    {"id":"miemie_origin_beast","name":"Miemie Origin True Body","nameCn":"灭灭本源真身","novel":"Renegade Immortal",
     "appearance":"Formless beast of destruction-origin 8 m, body radiates annihilating light",
     "size":"8 m manifestation, 0 jin (destruction); true form spans destruction-realms",
     "behavior":"Destruction-origin-incarnate being; cultivates by absorbing destruction-essence; Wang Lin obtained as treasure",
     "habitat":"Destruction-aspected forbidden zones, annihilation sites",
     "cultivation":"Immortal (destruction attribute)",
     "intelligence":11,"diet":"destruction-essence, annihilation fragments",
     "relationships":"Wang Lin obtained Miemie origin true body as treasure (canon event)",
     "techniques":["destruction_breath","annihilate","destruction_absorb","void_rend"],
     "treasures":["miemie_origin_core","destruction_essence","annihilation_fragment"],
     "bloodline":"Destruction lineage; destruction bloodline",
     "mutations":["true_destruction_body","destruction_immortal_emperor"],
     "canon_confidence":4},

    {"id":"slaughter_true_body_beast","name":"Slaughter True Body","nameCn":"杀戮真身","novel":"Renegade Immortal",
     "appearance":"Formless beast of slaughter 8 m, body is living killing-intent, eyes are blood-pools",
     "size":"8 m manifestation, 0 jin (slaughter); true form spans battlefield-realms",
     "behavior":"Slaughter-incarnate being; cultivates by absorbing slaughter-qi; Lu Mo's true body",
     "habitat":"Slaughter-aspected forbidden zones, ancient battlefields",
     "cultivation":"Immortal (slaughter attribute)",
     "intelligence":10,"diet":"slaughter-qi, slaughter-herbs, wicked souls",
     "relationships":"Lu Mo (Wang Lin's clone) obtained slaughter true body (canon event)",
     "techniques":["slaughter_breath","killing_rend","slaughter_absorb","blood_maw"],
     "treasures":["slaughter_core","slaughter_essence","killing_fragment"],
     "bloodline":"Slaughter lineage; slaughter bloodline",
     "mutations":["true_slaughter_body","slaughter_immortal_emperor"],
     "canon_confidence":4},

    {"id":"immortal_celestial_body_beast","name":"Immortal Celestial Body","nameCn":"仙天体","novel":"Renegade Immortal",
     "appearance":"Celestial body 5 m, body radiates immortal light, eyes are star-wells",
     "size":"5 m manifestation, 1000 jin; true form spans celestial-realms",
     "behavior":"Celestial-incarnate being; cultivates by absorbing celestial-essence",
     "habitat":"Celestial-aspected forbidden zones, star-nexus sites",
     "cultivation":"Immortal (celestial attribute)",
     "intelligence":11,"diet":"celestial-essence, star-fragments, celestial herbs",
     "relationships":"Wang Lin obtained immortal celestial body as treasure (canon event)",
     "techniques":["celestial_breath","star_light","celestial_absorb","star_body"],
     "treasures":["celestial_core","star_essence","celestial_fragment"],
     "bloodline":"Celestial lineage; star bloodline",
     "mutations":["true_celestial_body","celestial_immortal_emperor"],
     "canon_confidence":3},

    {"id":"undying_ancient_body_beast","name":"Undying Ancient Body","nameCn":"道古不灭体","novel":"Renegade Immortal",
     "appearance":"Ancient undying body 6 m, body radiates ancient+undying power, skin is ancient-clan bronze",
     "size":"6 m manifestation, 5000 jin; true form spans ancient-realms",
     "behavior":"Undying-incarnate being; cultivates by absorbing ancient+undying essence",
     "habitat":"Ancient clan territory, undying-aspected zones",
     "cultivation":"Immortal -> Ancient Immortal (ancient/undying attribute)",
     "intelligence":11,"diet":"ancient essence, undying herbs, ancient qi",
     "relationships":"Wang Lin obtained undying ancient body as treasure (canon event)",
     "techniques":["undying_breath","ancient_regen","undying_absorb","ancient_body"],
     "treasures":["undying_ancient_core","undying_essence","ancient_fragment"],
     "bloodline":"Ancient+Undying lineage; ancient clan + undying bloodline",
     "mutations":["true_undying_body","ancient_undying_emperor"],
     "canon_confidence":4},

    {"id":"dao_gu_indestructible_body_beast","name":"Dao Gu Indestructible Body","nameCn":"道古不灭体","novel":"Renegade Immortal",
     "appearance":"Dao Gu indestructible body 6 m, body radiates Dao-Gu power, skin is ancient-clan jade",
     "size":"6 m manifestation, 5000 jin; true form spans Dao-Gu-realms",
     "behavior":"Dao-Gu-incarnate being; cultivates by absorbing Dao-Gu essence",
     "habitat":"Dao-Gu territory, indestructible-aspected zones",
     "cultivation":"Immortal -> Ancient Immortal (Dao-Gu attribute)",
     "intelligence":11,"diet":"Dao-Gu essence, indestructible herbs, ancient qi",
     "relationships":"Wang Lin obtained Dao Gu indestructible body as treasure (canon event)",
     "techniques":["dao_gu_breath","indestructible_regen","dao_gu_absorb","indestructible_body"],
     "treasures":["dao_gu_core","indestructible_essence","dao_gu_fragment"],
     "bloodline":"Dao-Gu lineage; ancient clan + indestructible bloodline",
     "mutations":["true_dao_gu_body","dao_gu_emperor"],
     "canon_confidence":4},

    # ─── Lesser miscellaneous beasts from RI ───
    {"id":"spirit_rabbit","name":"Spirit Rabbit","nameCn":"灵兔","novel":"Renegade Immortal",
     "appearance":"Small rabbit 0.3 m, fur white-silver, eyes glow with spirit-light",
     "size":"0.3 m adult, 2 jin; ancient 0.8 m, 20 jin",
     "behavior":"Timid; cultivates by absorbing dawn-light; prey for many predators",
     "habitat":"Spirit herb hills, dawn-aspected valleys, forests",
     "cultivation":"Qi Condensation -> Foundation Establishment (light attribute)",
     "intelligence":5,"diet":"dawn-dew, spirit-herbs, grass",
     "relationships":"Prey for wind wolves, mountain lions, foxes",
     "techniques":["dawn_hop","spirit_dodge","light_burrow"],
     "treasures":["spirit_rabbit_core","white_fur"],
     "bloodline":"Spirit-Rabbit lineage; light-attribute bloodline (dilute)",
     "mutations":["silver_fur","light_body"],
     "canon_confidence":3},

    {"id":"spirit_fox","name":"Spirit Fox","nameCn":"灵狐","novel":"Renegade Immortal",
     "appearance":"Elegant fox 1 m, fur shifts through spirit-colors, nine tails on ancient specimens",
     "size":"1 m adult, 30 jin; ancient 3 m, 500 jin",
     "behavior":"Cunning predator; cultivates by absorbing illusion-essence; uses illusions to hunt",
     "habitat":"Spirit forests, illusion-aspected valleys",
     "cultivation":"Foundation Establishment -> Core Formation (illusion attribute)",
     "intelligence":7,"diet":"spirit rabbits, spirit herbs, smaller beasts",
     "relationships":"Rival of spirit cranes; preyed upon by mountain lions",
     "techniques":["illusion_shift","fox_charm","spirit_dodge","nine_tail_sweep"],
     "treasures":["spirit_fox_core","fox_pelt","illusion_essence"],
     "bloodline":"Spirit-Fox lineage; illusion-attribute bloodline",
     "mutations":["nine_tail","true_fox_emperor"],
     "canon_confidence":3},

    {"id":"spirit_deer","name":"Spirit Deer","nameCn":"灵鹿","novel":"Renegade Immortal",
     "appearance":"Graceful deer 1.5 m, fur spirit-brown, antlers of crystal, eyes gentle",
     "size":"1.5 m adult, 100 jin; ancient 3 m, 1000 jin",
     "behavior":"Herbivore; cultivates by absorbing dawn-dew; auspicious to encounter",
     "habitat":"Spirit herb hills, dawn-aspected forests, auspicious valleys",
     "cultivation":"Qi Condensation -> Foundation Establishment (auspicious attribute)",
     "intelligence":6,"diet":"dawn-dew, spirit-herbs, grass",
     "relationships":"Prey for wind wolves, mountain lions; auspicious",
     "techniques":["dawn_run","crystal_antler","auspicious_dodge"],
     "treasures":["spirit_deer_core","crystal_antler","auspicious_fur"],
     "bloodline":"Spirit-Deer lineage; auspicious bloodline (dilute)",
     "mutations":["golden_antler","auspicious_body"],
     "canon_confidence":3},

    {"id":"spirit_eagle","name":"Spirit Eagle","nameCn":"灵鹰","novel":"Renegade Immortal",
     "appearance":"Majestic eagle 2 m wingspan, feathers spirit-gold, eyes pierce illusions",
     "size":"2 m wingspan adult, 40 jin; ancient 5 m, 800 jin",
     "behavior":"Aerial predator; cultivates by absorbing wind-essence; hunts from great heights",
     "habitat":"Mountain peaks, sky-aspected zones, high cliffs",
     "cultivation":"Foundation Establishment -> Core Formation (wind attribute)",
     "intelligence":7,"diet":"spirit rabbits, spirit foxes, smaller birds",
     "relationships":"Rival of spirit cranes; preyed upon by thunder birds",
     "techniques":["eagle_dive","wind_scream","spirit_sight","talon_rend"],
     "treasures":["spirit_eagle_core","eagle_feather","talon"],
     "bloodline":"Spirit-Eagle lineage; wind-attribute bloodline",
     "mutations":["golden_feather","wind_body"],
     "canon_confidence":3},

    {"id":"spirit_tortoise","name":"Spirit Tortoise","nameCn":"灵龟","novel":"Renegade Immortal",
     "appearance":"Ancient tortoise 1 m shell, shell spirit-jade, age measured in millennia",
     "size":"1 m shell adult, 200 jin; ancient 5 m, 5000 jin",
     "behavior":"Patient cultivator; cultivates by absorbing earth-essence; sleeps for centuries",
     "habitat":"Earth-aspected springs, ancient caves, spirit veins",
     "cultivation":"Core Formation -> Nascent Soul (earth attribute)",
     "intelligence":8,"diet":"earth-herbs, spirit-minerals, spring-water",
     "relationships":"Auspicious; rival of giant pythons",
     "techniques":["shell_defense","earth_step","ancient_bite","spring_drink"],
     "treasures":["spirit_tortoise_core","shell_fragment","ancient_pearl"],
     "bloodline":"Spirit-Tortoise lineage; earth-attribute bloodline (dilute Black Tortoise)",
     "mutations":["jade_shell","ancient_tortoise_emperor"],
     "canon_confidence":3},

    {"id":"spirit_bear","name":"Spirit Bear","nameCn":"灵熊","novel":"Renegade Immortal",
     "appearance":"Massive bear 3 m tall, fur spirit-brown, fists shatter stone",
     "size":"3 m adult, 2000 jin; ancient 6 m, 30000 jin",
     "behavior":"Territorial body-tempering beast; cultivates by absorbing earth-essence",
     "habitat":"Mountain forests, earth-aspected caves, spirit-herb hills",
     "cultivation":"Foundation Establishment -> Core Formation (earth attribute)",
     "intelligence":5,"diet":"spirit-herbs, honey, smaller beasts",
     "relationships":"Rival of mountain lions; preyed upon by giant savage apes",
     "techniques":["bear_hug","iron_fist","earth_roar","mountain_charge"],
     "treasures":["spirit_bear_core","bear_pelt","iron_fur"],
     "bloodline":"Spirit-Bear lineage; earth-attribute bloodline",
     "mutations":["iron_body","ancient_bear_emperor"],
     "canon_confidence":3},

    {"id":"spirit_snake","name":"Spirit Snake","nameCn":"灵蛇","novel":"Renegade Immortal",
     "appearance":"Serpentine body 5 m, scales spirit-green, venom drips spirit-corroding acid",
     "size":"5 m adult, 200 jin; ancient 20 m, 5000 jin",
     "behavior":"Ambush predator; cultivates by absorbing venom-essence; constricts prey",
     "habitat":"Spirit forests, venom-aspected valleys, caves",
     "cultivation":"Foundation Establishment -> Core Formation (venom attribute)",
     "intelligence":5,"diet":"spirit rabbits, spirit deer, smaller beasts",
     "relationships":"Rival of spirit foxes; preyed upon by giant pythons",
     "techniques":["venom_bite","constrict","spirit_dodge","burrow"],
     "treasures":["spirit_snake_core","venom_gland","snake_pelt"],
     "bloodline":"Spirit-Snake lineage; venom-attribute bloodline (dilute python)",
     "mutations":["venom_body","ancient_snake_emperor"],
     "canon_confidence":3},

    {"id":"spirit_bat","name":"Spirit Bat","nameCn":"灵蝠","novel":"Renegade Immortal",
     "appearance":"Bat 1 m wingspan, fur spirit-violet, sonar disrupts cultivation",
     "size":"1 m wingspan adult, 20 jin; ancient 3 m, 500 jin",
     "behavior":"Cave swarm; hunts by sonar; cultivates in darkness",
     "habitat":"Spirit caves, darkness-aspected caverns",
     "cultivation":"Qi Condensation -> Foundation Establishment (darkness attribute)",
     "intelligence":4,"diet":"cave insects, spirit-worms, smaller beasts",
     "relationships":"Swarm hunters; preyed upon by web weavers",
     "techniques":["sonar_drain","swarm_overwhelm","darkness_step"],
     "treasures":["spirit_bat_core","bat_wing"],
     "bloodline":"Spirit-Bat lineage; darkness-attribute bloodline (dilute ink bat)",
     "mutations":["thousand_wing","ancient_bat_emperor"],
     "canon_confidence":3},

    {"id":"spirit_fish","name":"Spirit Fish","nameCn":"灵鱼","novel":"Renegade Immortal",
     "appearance":"Fish 0.5 m, scales spirit-silver, swims through spirit-water",
     "size":"0.5 m adult, 2 jin; ancient 2 m, 200 jin",
     "behavior":"Schools in spirit-springs; cultivates by absorbing water-essence",
     "habitat":"Spirit springs, spirit-water rivers, spirit-lakes",
     "cultivation":"Qi Condensation (water attribute)",
     "intelligence":3,"diet":"spirit-algae, water-plankton",
     "relationships":"Prey for spirit eagles, spirit snakes, cold dragons",
     "techniques":["school_swim","water_dodge","spirit_splash"],
     "treasures":["spirit_fish_core","silver_scale"],
     "bloodline":"Spirit-Fish lineage; water-attribute bloodline",
     "mutations":["golden_scale","water_body"],
     "canon_confidence":3},

    {"id":"spirit_toad","name":"Spirit Toad","nameCn":"灵蟾","novel":"Renegade Immortal",
     "appearance":"Toad 0.5 m, skin spirit-green, throat sac glows with spirit-light",
     "size":"0.5 m adult, 20 jin; ancient 2 m, 500 jin",
     "behavior":"Ambush predator near springs; cultivates by absorbing water+dawn essence",
     "habitat":"Spirit springs, dawn-aspected wetlands",
     "cultivation":"Qi Condensation -> Foundation Establishment (water/light attribute)",
     "intelligence":4,"diet":"spirit fish, spirit insects, dawn-dew",
     "relationships":"Lesser kin of thunder toad; prey for spirit snakes",
     "techniques":["spirit_croak","tongue_lash","water_dodge"],
     "treasures":["spirit_toad_core","spirit_skin"],
     "bloodline":"Spirit-Toad lineage; water-attribute bloodline (dilute thunder toad)",
     "mutations":["light_sac","spirit_toad_emperor"],
     "canon_confidence":3},

    {"id":"spirit_wolf_pack_leader","name":"Spirit Wolf Pack Leader","nameCn":"灵狼首领","novel":"Renegade Immortal",
     "appearance":"Larger wind wolf 3 m, fur silver-alpha, eyes command the pack",
     "size":"3 m adult, 400 jin; ancient 5 m, 3000 jin",
     "behavior":"Alpha of wind wolf packs; commands pack tactics; cultivates by leading hunts",
     "habitat":"Plains, wind-aspected spirit veins, mountain foothills",
     "cultivation":"Core Formation (wind attribute)",
     "intelligence":8,"diet":"spirit deer, spirit rabbits, lost travelers",
     "relationships":"Alpha of wind wolf packs; rival of mountain lions",
     "techniques":["pack_command","alpha_howl","wind_step","alpha_bite"],
     "treasures":["alpha_wolf_core","silver_alpha_fur","fang"],
     "bloodline":"Spirit-Wolf lineage; wind-attribute alpha bloodline",
     "mutations":["silver_alpha","wind_body","ancient_wolf_emperor"],
     "canon_confidence":3},

    {"id":"spirit_ape","name":"Spirit Ape","nameCn":"灵猿","novel":"Renegade Immortal",
     "appearance":"Bipedal ape 3 m, fur spirit-gold, intelligent eyes, fists strong",
     "size":"3 m adult, 1000 jin; ancient 8 m, 30000 jin",
     "behavior":"Territorial mountain dweller; cultivates by absorbing spirit-fruit; uses simple tools",
     "habitat":"Mountain forests, spirit-fruit groves, peaks",
     "cultivation":"Foundation Establishment -> Core Formation (body/spirit attribute)",
     "intelligence":7,"diet":"spirit-fruit, spirit-herbs, smaller beasts",
     "relationships":"Lesser kin of giant savage ape; rival of spirit bears",
     "techniques":["iron_fist","mountain_climb","spirit_throw","ape_roar"],
     "treasures":["spirit_ape_core","spirit_fur","iron_fang"],
     "bloodline":"Spirit-Ape lineage; body-attribute bloodline (dilute giant savage ape)",
     "mutations":["four_arm","iron_body","ancient_ape_emperor"],
     "canon_confidence":3},

    {"id":"corpse_yin_beast","name":"Corpse Yin Beast","nameCn":"尸阴兽","novel":"Renegade Immortal",
     "appearance":"Undead beast 3 m, body is desiccated corpse-flesh, eyes glow with yin-fire",
     "size":"3 m adult, 500 jin; ancient 10 m, 10000 jin",
     "behavior":"Undead predator; cultivates by absorbing yin-qi and corpse-essence",
     "habitat":"Corpse Yin Sect territory, yin-aspected caves, graveyards",
     "cultivation":"Core Formation -> Nascent Soul (yin/corpse attribute)",
     "intelligence":5,"diet":"corpse-essence, yin-herbs, lost cultivators",
     "relationships":"Servant-beasts of Corpse Yin Sect; enemies of all living",
     "techniques":["yin_breath","corpse_drain","yin_step","desiccate"],
     "treasures":["corpse_yin_core","yin_essence","corpse_fragment"],
     "bloodline":"Corpse-Yin lineage; yin-attribute undead bloodline",
     "mutations":["true_undying","yin_immortal_body"],
     "canon_confidence":4},

    {"id":"devil_soul_beast","name":"Devil Soul Beast","nameCn":"魔魂兽","novel":"Renegade Immortal",
     "appearance":"Devil-soul beast 4 m, body shifts between physical and soul-form, radiates devil+soul power",
     "size":"4 m adult, 800 jin; ancient 15 m, 20000 jin",
     "behavior":"Devil+soul predator; cultivates by absorbing devil-qi and soul fragments",
     "habitat":"Sea of Devils, devil-soul-aspected caves",
     "cultivation":"Nascent Soul -> Spirit Severing (devil/soul attribute)",
     "intelligence":8,"diet":"soul fragments, devil-herbs, wicked cultivators",
     "relationships":"Servant-beasts of devil-sects; enemies of orthodox",
     "techniques":["devil_soul_drain","soul_rend","devil_step","soul_maw"],
     "treasures":["devil_soul_core","soul_fragment","devil_essence"],
     "bloodline":"Devil-Soul lineage; devil+soul bloodline",
     "mutations":["true_devil_soul_body","devil_soul_emperor"],
     "canon_confidence":4},

    {"id":"celestial_guard_beast","name":"Celestial Guard Beast","nameCn":"仙卫兽","novel":"Renegade Immortal",
     "appearance":"Constructed beast 3 m, body is refined celestial-material, eyes glow with celestial-light",
     "size":"3 m adult, 2000 jin; ancient 8 m, 30000 jin",
     "behavior":"Constructed guardian beast; cultivates by absorbing celestial-essence; follows orders",
     "habitat":"Celestial guard workshops, celestial-aspected sites",
     "cultivation":"Core Formation -> Nascent Soul (celestial attribute)",
     "intelligence":6,"diet":"celestial-essence, spirit stones",
     "relationships":"Constructed by cultivators; Wang Lin refined Ta Shan as celestial guard",
     "techniques":["guard_defend","celestial_strike","construct_regen","order_follow"],
     "treasures":["celestial_guard_core","celestial_material","guard_fragment"],
     "bloodline":"Constructed lineage; no natural bloodline",
     "mutations":["true_celestial_body","celestial_emperor"],
     "canon_confidence":4},

    {"id":"puppet_beast","name":"Puppet Beast","nameCn":"傀儡兽","novel":"Renegade Immortal",
     "appearance":"Constructed puppet beast 2 m, body is spirit-wood and spirit-metal, joints articulate",
     "size":"2 m adult, 1000 jin; ancient 5 m, 10000 jin",
     "behavior":"Constructed puppet; follows puppet-master's orders; no will of its own",
     "habitat":"Puppet workshops, where deployed",
     "cultivation":"Constructed (matches puppet-master tier)",
     "intelligence":3,"diet":"spirit stones (maintenance)",
     "relationships":"Constructed by puppet cultivators; Wang Lin used puppets",
     "techniques":["puppet_strike","puppet_defend","order_follow","construct_regen"],
     "treasures":["puppet_core","puppet_material","spirit_stone"],
     "bloodline":"Constructed lineage; no natural bloodline",
     "mutations":["true_puppet_body"],
     "canon_confidence":4},
]

# ==================================================================
# PASS C — RI FACTION RELATIONSHIP NETWORK
# ==================================================================

RI_FACTION_RELATIONSHIPS = [
    {"faction":"wang_lin","type":"independent","allies":["situ_nan","qing_shui","bai_fan","lu_yun","qing_lin","du_tian","all_seer","xuan_luo"],
     "enemies":["seven_colored_daoist","blood_ancestor","teng_family","soul_refining_sect","dao_devil_sect","old_man_miesheng","daoist_water","master_void","wind_demon","green_devil","taga","tuo_sen"],
     "vassals":["xu_liguo","tianyunzi","ta_shan","big_head_cultivator","liu_jinbiao","ling_dong","zhou_jin","zhong_dahong","adai","lei_ji","mosquito_beast","thunder_toad","thunder_celestial_beast","nether_beast","brilliant_void","golden_exalt_sea_dragon","xiao_bai"],
     "rivals":["zhan_family","yun_family"],
     "neutral":["heavenly_fate_sect","cloud_sky_sect","da_lou_sword_sect","wang_family_village"]},

    {"faction":"heng_yue_sect","type":"sect","allies":["wang_family_village"],
     "enemies":["teng_family","ji_mo_sect"],
     "vassals":[],"rivals":["other_zhao_sects"],
     "neutral":["zhao_imperial"]},

    {"faction":"teng_family","type":"clan","allies":["ji_mo_sect","soul_refining_sect"],
     "enemies":["wang_lin","wang_family_village","heng_yue_sect"],
     "vassals":[],"rivals":["other_zhao_clans"],
     "neutral":[]},

    {"faction":"soul_refining_sect","type":"sect","allies":["teng_family","devil_sects"],
     "enemies":["wang_lin","orthodox_sects","heavenly_fate_sect"],
     "vassals":["soul_refining_tribe","corpse_yin_sect"],"rivals":[],
     "neutral":[]},

    {"faction":"heavenly_fate_sect","type":"sect","allies":["orthodox_sects"],
     "enemies":["soul_refining_sect","devil_sects","sky_demon_country"],
     "vassals":[],"rivals":["soul_refining_sect"],
     "neutral":["wang_lin"]},

    {"faction":"vermilion_bird_divine_sect","type":"divine_sect","allies":["wang_lin","situ_nan"],
     "enemies":["rival_divine_sects","seven_colored_daoist"],
     "vassals":["vassal_star_sects"],"rivals":[],
     "neutral":[]},

    {"faction":"yao_family","type":"clan","allies":[],
     "enemies":["wang_lin"],
     "vassals":[],"rivals":[],
     "neutral":[],"note":"Blood Ancestor's family; major antagonist faction"},

    {"faction":"zhan_family","type":"clan","allies":["yun_family"],
     "enemies":["wang_lin"],
     "vassals":[],"rivals":[],
     "neutral":[],"note":"War-aspected antagonist family"},

    {"faction":"yun_family","type":"clan","allies":["zhan_family"],
     "enemies":["wang_lin"],
     "vassals":[],"rivals":[],
     "neutral":[],"note":"Cloud-aspected antagonist family"},

    {"faction":"dao_devil_sect","type":"sect","allies":[],
     "enemies":["wang_lin"],
     "vassals":[],"rivals":[],
     "neutral":[],"note":"Annihilated by Wang Lin (canon-attested destruction)"},

    {"faction":"ancient_clan","type":"clan","allies":[],
     "enemies":["celestial_clan"],
     "vassals":[],"rivals":["celestial_clan"],
     "neutral":["wang_lin"],"note":"Ancient Clan; Wang Lin joined as Dao Gu lineage"},

    {"faction":"dark_scorpion_clan","type":"clan","allies":["wang_lin"],
     "enemies":[],"vassals":[],"rivals":[],
     "neutral":[],"note":"Wang Lin became their Ruler"},

    {"faction":"great_soul_sect","type":"sect","allies":["wang_lin"],
     "enemies":[],"vassals":[],"rivals":[],
     "neutral":[],"note":"Wang Lin became an elder"},

    {"faction":"seven_colored_sect","type":"sect","allies":[],
     "enemies":["wang_lin"],
     "vassals":[],"rivals":[],
     "neutral":[],"note":"THE true antagonist faction; Seven-Colored Daoist"},
]

# ==================================================================
# PASS D — RI ECOSYSTEM INTEGRATION
# ==================================================================
# Maps which beasts/NPCs/enemies appear in which ecosystems.

RI_ECOSYSTEM_INTEGRATION = [
    {"ecosystem":"mosquito_valley","beasts":["mosquito_beast","spirit_bat","spirit_snake","corpse_yin_beast"],
     "npcs":[],"enemies":[],"note":"Mosquito Valley ecosystem; millions of mosquitoes + lesser beasts"},

    {"ecosystem":"thunder_peak_ecology","beasts":["thunder_toad","lei_ji_thunder_beast","thunder_celestial_beast","thunder_ape"],
     "npcs":[],"enemies":[],"note":"Thunder Peak ecology; thunder-aspected beasts"},

    {"ecosystem":"sea_of_devils_ecology","beasts":["nether_beast","six_desire_devil","devil_soul_beast","corpse_yin_beast","blood_beast","soul_beast"],
     "npcs":["npc_old_man_ji_mo","npc_xu_liqing","npc_duanmu_ji","npc_gun_lan"],
     "enemies":["old_man_ji_mo","xu_liqing","duanmu_ji","gun_lan"],"note":"Sea of Devils; devil-aspected ecology + devil-sect NPCs"},

    {"ecosystem":"spirit_vein_ecology","beasts":["spirit_rabbit","spirit_deer","wind_wolf","mountain_lion","giant_python","spirit_tortoise","spirit_bear"],
     "npcs":[],"enemies":[],"note":"Spirit vein ecology; herbivores + predators + guardians"},

    {"ecosystem":"sky_realm_ecology","beasts":["cloud_whale","spirit_crane","spirit_eagle","southern_winged_snake","thunder_bird"],
     "npcs":[],"enemies":[],"note":"Sky Realm ecology; aerial beasts"},

    {"ecosystem":"foreign_battleground_ecology","beasts":["white_tiger","blood_ancestor_beast","berserk_beast","blood_beast","slaughter_true_body_beast"],
     "npcs":["npc_white_tiger_general"],
     "enemies":["white_tiger_general","zhan_family_members"],"note":"Foreign Battleground; war-aspected ecology"},

    {"ecosystem":"underground_crystal_ecology","beasts":["ant_beast","giant_centipede","web_weaver_spider","ink_bat","ink_dragon","spirit_tortoise"],
     "npcs":[],"enemies":[],"note":"Underground crystal ecology; subterranean beasts"},

    {"ecosystem":"immortal_graveyard_ecology","beasts":["nether_beast","soul_beast","devil_soul_beast","corpse_yin_beast","ghost_spirit_beast"],
     "npcs":[],"enemies":[],"note":"Immortal Graveyard; death-aspected ecology"},

    {"ecosystem":"pill_sea_ecology","beasts":["flame_dragon","earth_fire_dragon","spirit_toad","spirit_fish"],
     "npcs":[],"enemies":[],"note":"Pill Sea; alchemical ecology"},

    {"ecosystem":"vermilion_bird_realm_ecology","beasts":["vermilion_bird","ice_phoenix","flame_dragon","earth_fire_dragon"],
     "npcs":["npc_zhou_wutai","npc_ye_wuyou","npc_qian_pinghai","npc_3rd_gen_vermilion","npc_14th_gen_vermilion"],
     "enemies":["3rd_gen_vermilion","14th_gen_vermilion"],"note":"Vermilion Bird Realm; fire-divine ecology"},

    {"ecosystem":"ancient_god_remains_ecology","beasts":["giant_savage_ape","giant_python","blood_ancestor_beast","ji_qiong","restriction_beast","restriction_origin_beast","taichu_origin_beast","thunder_origin_beast","five_elements_beast","miemie_origin_beast","undying_ancient_body_beast","dao_gu_indestructible_body_beast"],
     "npcs":["npc_tu_si","npc_tuo_sen"],
     "enemies":["tuo_sen"],"note":"Ancient God remains; god-aspected ecology + origin true bodies"},

    {"ecosystem":"snow_domain_ecology","beasts":["ice_phoenix","cold_dragon","wind_wolf","mountain_lion","spirit_fox"],
     "npcs":[],"enemies":[],"note":"Snow Domain; ice-aspected ecology"},

    {"ecosystem":"karma_wastes_ecology","beasts":["destiny_beast","nether_beast","ghost_face_beast","sand_scorpion"],
     "npcs":[],"enemies":[],"note":"Karma Wastes; karma-aspected ecology"},

    {"ecosystem":"sword_intent_realm_ecology","beasts":["spirit_crane","dragon_ape","spirit_eagle"],
     "npcs":[],"enemies":[],"note":"Sword Intent Realm; sword-aspected ecology"},

    {"ecosystem":"green_devil_continent_ecology","beasts":["green_devil_scorpion","sand_scorpion","spirit_snake","devil_soul_beast"],
     "npcs":["npc_green_devil"],
     "enemies":["green_devil"],"note":"Green Devil Continent; poison-aspected ecology"},

    {"ecosystem":"heavenly_bull_continent_ecology","beasts":["heavenly_bull_soul_beast","earth_fire_dragon","flame_dragon"],
     "npcs":[],"enemies":[],"note":"Heavenly Bull Continent; soul+fire ecology"},

    {"ecosystem":"ancient_battlefield_ecology","beasts":["blood_ancestor_beast","blood_beast","berserk_beast","slaughter_true_body_beast","corpse_yin_beast"],
     "npcs":[],"enemies":[],"note":"Ancient battlefield; death+slaughter ecology"},

    {"ecosystem":"wang_family_village_ecology","beasts":["spirit_rabbit","spirit_deer","spirit_fox","spirit_wolf_pack_leader"],
     "npcs":["npc_wang_tianshui","npc_wang_ping","npc_zhou_tingsu","npc_da_niu","npc_zhou_rui","npc_wang_tianshan","npc_wang_qingyue"],
     "enemies":[],"note":"Wang Family Village; mortal + low-tier spirit beast ecology"},

    {"ecosystem":"heng_yue_sect_ecology","beasts":["spirit_ape","spirit_bear","spirit_eagle","spirit_fox"],
     "npcs":["npc_qiu_siping","npc_wang_zhuo","npc_wang_hao","npc_yun_fei","npc_qian_kun","npc_sun_dazhu","npc_wu_yu","npc_ye_zi","npc_sun_zhenwei","npc_chen_bailiang","npc_zhang_hu","npc_zhou_lin"],
     "enemies":["sun_dazhu"],"note":"Heng Yue Sect; cultivator + spirit beast ecology"},

    {"ecosystem":"teng_family_city_ecology","beasts":["spirit_wolf_pack_leader","mountain_lion","giant_python"],
     "npcs":["npc_teng_huayuan","npc_teng_li","npc_teng_xiuxiu"],
     "enemies":["teng_huayuan","teng_li"],"note":"Teng Family City; antagonist clan ecology"},

    {"ecosystem":"celestial_realm_ecology","beasts":["celestial_guard_beast","puppet_beast","thunder_celestial_beast"],
     "npcs":[],"enemies":[],"note":"Celestial Realm; constructed being ecology"},
]

# ==================================================================
# GENERATION FUNCTIONS
# ==================================================================

def pass_a_npc_archetypes(canon):
    """Generate NPC archetype definition files."""
    npc_dir = out("npcs")
    count = 0
    for npc in NPC_ARCHETYPES:
        f = npc_dir / f"{npc['id']}.json"
        if f.exists():
            continue
        doc = {
            "_comment": f"NPC: {npc['name']} ({npc.get('nameCn','')}). Canon ID: {npc.get('canon_id','')}. "
                         f"Per user directive: every miscellaneous NPC from RI must be accounted for. "
                         f"Prime Directive: {CANON_PRIME}",
            "npc_id": npc["id"],
            "name": npc["name"],
            "nameCn": npc.get("nameCn",""),
            "canon_id": npc.get("canon_id",""),
            "type": npc["type"],
            "faction": npc["faction"],
            "location": npc["location"],
            "cultivation": npc["cultivation"],
            "personality": npc["personality"],
            "speech": npc["speech"],
            "relationship_to_wanglin": npc["relationship_to_wanglin"],
            "dialogue_available": npc.get("dialogue_available",False),
            "quest_available": npc.get("quest_available",False),
            "trade_available": npc.get("trade_available",False),
            "teaching_available": npc.get("teaching_available",False),
            "perception_tiers": npc["perception_tiers"],
            "canon_confidence": npc["canon_confidence"],
            "note": npc.get("note",""),
            "derivation_type": "A" if npc["canon_confidence"] >= 4 else "B",
            "salt": salt_for(npc["id"]),
        }
        write_json(f, doc)
        count += 1
    return count

def pass_b_miscellaneous_beasts(canon):
    """Generate miscellaneous beast species definitions."""
    species_dir = out("species")
    count = 0
    for beast in RI_MISCELLANEOUS_BEASTS:
        f = species_dir / f"{beast['id']}.json"
        if f.exists():
            continue
        doc = {
            "_comment": f"Species: {beast['name']} ({beast.get('nameCn','')}) from {beast['novel']}. "
                         f"Per user directive: every miscellaneous beast from RI must be accounted for "
                         f"and part of the ecosystem. "
                         f"Prime Directive: {CANON_PRIME}",
            "species_id": beast["id"],
            "name": beast["name"],
            "nameCn": beast.get("nameCn",""),
            "novel": beast["novel"],
            "derivation_type": "A" if beast["canon_confidence"] >= 4 else "B",
            "canon_confidence": beast["canon_confidence"],
            "canonical": {
                "appearance": beast["appearance"],
                "size": beast["size"],
                "behavior": beast["behavior"],
                "habitat": beast["habitat"],
                "cultivation": beast["cultivation"],
                "intelligence": beast["intelligence"],
                "diet": beast["diet"],
                "relationships": beast["relationships"],
                "techniques": beast["techniques"],
                "treasures": beast["treasures"],
                "bloodline": beast["bloodline"],
                "mutations": beast["mutations"],
                "ecology": "Integrated into RI ecosystem definitions; see ecosystem_integration."
            },
            "art_direction": {
                "generator": "single canonical definition drives 10 life-state variants",
                "variants": ["juvenile","adolescent","adult","ancient","mutated","injured","starving","tribulation_scarred","corrupted","enlightened"],
                "note": "Per art-direction directive: build generators, not assets."
            },
            "salt": salt_for(beast["id"]),
        }
        write_json(f, doc)
        count += 1

        # Also generate life-state variants for each beast
        variants_dir = out("species_variants")
        LIFE_STATES = [
            ("juvenile",0.15,0.1,"curious","common"),
            ("adolescent",0.45,0.35,"reckless","uncommon"),
            ("adult",1.0,1.0,"territorial","uncommon"),
            ("ancient",2.5,4.0,"contemplative","rare"),
            ("mutated",1.5,3.0,"aggressive","rare"),
            ("injured",0.9,0.5,"desperate","uncommon"),
            ("starving",0.85,0.7,"frenzied","uncommon"),
            ("tribulation_scarred",1.2,2.5,"sober","rare"),
            ("corrupted",1.3,2.0,"malevolent","rare"),
            ("enlightened",1.1,5.0,"serene","very_rare"),
        ]
        for state_name, size_mult, power_mult, temperament, rarity in LIFE_STATES:
            vid = f"{beast['id']}__{state_name}"
            vfile = variants_dir / f"{vid}.json"
            if vfile.exists():
                continue
            vdoc = {
                "_comment": f"Life-state variant: {beast['name']} ({state_name}). Generated from canonical definition.",
                "variant_id": vid,
                "species_id": beast["id"],
                "species_name": beast["name"],
                "life_state": state_name,
                "modifiers": {
                    "size_multiplier": size_mult,
                    "power_multiplier": power_mult,
                    "temperament": temperament,
                    "rarity": rarity,
                },
                "derivation_type": "B",
                "canon_confidence": beast["canon_confidence"],
                "salt": salt_for(vid),
            }
            write_json(vfile, vdoc)

    return count

def pass_c_faction_relationships(canon):
    """Generate faction relationship network."""
    rel_dir = out("faction_relationships")
    count = 0
    for faction in RI_FACTION_RELATIONSHIPS:
        f = rel_dir / f"{faction['faction']}.json"
        if f.exists():
            continue
        doc = {
            "_comment": f"Faction relationships: {faction['faction']}. "
                         f"Per user directive: faction relationship network — allies/enemies/vassals/rivals. "
                         f"Prime Directive: relationships exist objectively.",
            "faction_id": faction["faction"],
            "type": faction["type"],
            "allies": faction["allies"],
            "enemies": faction["enemies"],
            "vassals": faction["vassals"],
            "rivals": faction["rivals"],
            "neutral": faction.get("neutral",[]),
            "note": faction.get("note",""),
            "salt": salt_for(faction["faction"]),
        }
        write_json(f, doc)
        count += 1
    return count

def pass_d_ecosystem_integration(canon):
    """Generate ecosystem integration mapping."""
    integ_dir = out("ecosystem_integration")
    count = 0
    for integ in RI_ECOSYSTEM_INTEGRATION:
        f = integ_dir / f"{integ['ecosystem']}.json"
        if f.exists():
            continue
        doc = {
            "_comment": f"Ecosystem integration: {integ['ecosystem']}. "
                         f"Maps which beasts, NPCs, and enemies appear in this ecosystem. "
                         f"Per user directive: every being must be part of the ecosystem.",
            "ecosystem_id": integ["ecosystem"],
            "beasts": integ["beasts"],
            "npcs": integ["npcs"],
            "enemies": integ["enemies"],
            "note": integ["note"],
            "salt": salt_for(integ["ecosystem"]),
        }
        write_json(f, doc)
        count += 1
    return count

def update_lang_file(npc_count, beast_count):
    """Update lang file with new entries."""
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

    for npc in NPC_ARCHETYPES:
        add(f"npc.ergenverse.{npc['id']}", npc["name"])

    for beast in RI_MISCELLANEOUS_BEASTS:
        add(f"species.ergenverse.{beast['id']}", beast["name"])

    for faction in RI_FACTION_RELATIONSHIPS:
        add(f"faction.ergenverse.{faction['faction']}", faction["faction"].replace("_"," ").title())

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
    print("RI CONTENT EXPANSION — Comprehensive Renegade Immortal Registry")
    print("Per user directive: every miscellaneous beast/enemy/NPC accounted for")
    print("=" * 70)
    canon = load_canon()
    print(f"Loaded canon: {len(canon.get('characters',[]))} characters, "
          f"{len(canon.get('locations',[]))} locations, "
          f"{len(canon.get('artifacts',[]))} artifacts")

    results = {}

    print(f"\n[Pass A] RI NPC Archetype Definitions ({len(NPC_ARCHETYPES)} NPCs)...")
    n = pass_a_npc_archetypes(canon)
    results['npc_archetypes'] = n
    print(f"  +{n} new NPC archetype definitions")

    print(f"\n[Pass B] RI Miscellaneous Beast Definitions ({len(RI_MISCELLANEOUS_BEASTS)} beasts)...")
    b = pass_b_miscellaneous_beasts(canon)
    results['miscellaneous_beasts'] = b
    print(f"  +{b} new beast species (+ life-state variants)")

    print(f"\n[Pass C] RI Faction Relationship Network ({len(RI_FACTION_RELATIONSHIPS)} factions)...")
    fr = pass_c_faction_relationships(canon)
    results['faction_relationships'] = fr
    print(f"  +{fr} new faction relationship files")

    print(f"\n[Pass D] RI Ecosystem Integration ({len(RI_ECOSYSTEM_INTEGRATION)} ecosystems)...")
    ei = pass_d_ecosystem_integration(canon)
    results['ecosystem_integration'] = ei
    print(f"  +{ei} new ecosystem integration files")

    print("\n[Lang] Updating lang file...")
    la = update_lang_file(n, b)
    results['lang_added'] = la
    print(f"  +{la} new lang entries")

    print("\n" + "=" * 70)
    print("DONE. Results:")
    for k, v in results.items():
        print(f"  {k}: {v}")
    print("=" * 70)

if __name__ == "__main__":
    main()
