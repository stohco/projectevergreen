#!/usr/bin/env python3
"""
AUTO-CANON-040: Xuan Dao Sect (玄道宗) completion.
20 component loot tables + 12 INFERRED NPCs with schedules + tasks.

Xuan Dao Sect theme: Orthodox Daoist cultivation, scholarly pursuit of the Dao,
formation/restriction mastery, ink-and-paper arts, jade slip scholarship.
Contrasts with dark sects (Corpse Yin, Soul Refining) and fate-focused (Heavenly Fate).
Referenced by Nan Dou City cultivator (rogue from Xuan Dao) and Heavenly Fate array master.

Loot signature: paper/ink_sac/book (scholarship), redstone (formations), lapis_lazuli (Dao blue).
Wealth gradient: LOW (gate/plaza/dorms/herb/spring/trials) -> MED (library/alchemy/sword/beast/array/puppet/cave)
-> HIGH (underground/secret/inner/treasury/sword_tomb) -> MAX (ancestor/core_formation).
"""

import json, os

BASE = "/home/z/my-project/forge-mod/src/main/resources/data/ergenverse"
LOOT_DIR = os.path.join(BASE, "loot_tables/chests")
NPC_DIR = os.path.join(BASE, "npcs")

COMPONENTS = [
    "xuan_dao_sect_outer_gate","xuan_dao_sect_main_plaza","xuan_dao_sect_disciple_dormitories",
    "xuan_dao_sect_spirit_herb_garden","xuan_dao_sect_spirit_spring","xuan_dao_sect_trial_grounds",
    "xuan_dao_sect_library","xuan_dao_sect_alchemy_courtyard","xuan_dao_sect_sword_peak",
    "xuan_dao_sect_spirit_beast_pens","xuan_dao_sect_array_hall","xuan_dao_sect_puppet_workshop",
    "xuan_dao_sect_mountain_cave","xuan_dao_sect_underground_passage","xuan_dao_sect_secret_pavilion",
    "xuan_dao_sect_inner_sect","xuan_dao_sect_hidden_treasury","xuan_dao_sect_sword_tomb",
    "xuan_dao_sect_ancestor_hall","xuan_dao_sect_core_formation_hall",
]

def item(w, name, cmin=1, cmax=1):
    f = [{"function":"minecraft:set_count","count":{"min":cmin,"max":cmax}}] if cmin!=cmax or cmin!=1 else []
    return {"type":"minecraft:item","weight":w,"name":name,"functions":f}
def empty(w): return {"type":"minecraft:empty","weight":w}
def loot(comment, pools):
    return {"_comment":comment,"type":"minecraft:chest","pools":[
        {"rolls":{"min":p[0],"max":p[1]},"entries":p[2]} for p in pools]}

# Signature: paper, ink_sac, book, redstone, lapis_lazuli (scholarly Daoist blue)
T = {
    "xuan_dao_sect_outer_gate": loot("Xuan Dao Sect — outer_gate. Scholarly guard post. Daoist calligraphy scrolls.",[
        (2,4,[item(10,"minecraft:arrow",4,12),item(8,"minecraft:paper",2,5),item(6,"minecraft:coal",2,5),
              item(5,"minecraft:bread",1,3),item(4,"minecraft:redstone",2,4),item(3,"minecraft:ink_sac",1,2)]),
        (1,2,[item(5,"minecraft:stick",4,8),item(3,"minecraft:lapis_lazuli",1,3)])]),
    "xuan_dao_sect_main_plaza": loot("Xuan Dao Sect — main_plaza. Open debate and lecture area.",[
        (1,2,[item(8,"minecraft:paper",2,5),item(6,"minecraft:bread",1,3),item(5,"minecraft:redstone",1,3),
              item(4,"minecraft:ink_sac",1,2),item(3,"minecraft:lapis_lazuli",1,2)]),
        (1,1,[item(3,"minecraft:book"),item(2,"minecraft:coal",2,4)])]),
    "xuan_dao_sect_disciple_dormitories": loot("Xuan Dao Sect — disciple_dormitories. Daoist study quarters.",[
        (1,3,[item(8,"minecraft:paper",2,6),item(6,"minecraft:bread",1,4),item(5,"minecraft:ink_sac",2,4),
              item(4,"minecraft:coal",2,5),item(3,"minecraft:redstone",1,3),item(2,"minecraft:lapis_lazuli",1,2)])]),
    "xuan_dao_sect_spirit_herb_garden": loot("Xuan Dao Sect — spirit_herb_garden. Dao-attributed herbs.",[
        (2,4,[item(8,"minecraft:wheat",2,6),item(6,"minecraft:bone_meal",2,5),item(5,"minecraft:paper",1,3),
              item(4,"minecraft:redstone",1,3),item(3,"minecraft:glass_bottle",1,3),item(2,"minecraft:lapis_lazuli",1,2)])]),
    "xuan_dao_sect_spirit_spring": loot("Xuan Dao Sect — spirit_spring. Dao-purified spring.",[
        (1,3,[item(8,"minecraft:glass_bottle",1,3),item(6,"minecraft:lapis_lazuli",2,5),
              item(5,"minecraft:paper",2,4),item(4,"minecraft:redstone",1,3),item(2,"minecraft:glowstone_dust",1,2)])]),
    "xuan_dao_sect_trial_grounds": loot("Xuan Dao Sect — trial_grounds. Formation comprehension trials.",[
        (2,4,[item(8,"minecraft:redstone",3,8),item(6,"minecraft:arrow",4,10),item(5,"minecraft:paper",2,5),
              item(4,"minecraft:ink_sac",1,3),item(3,"minecraft:iron_ingot",1,2)]),
        (1,2,[item(4,"minecraft:emerald",1,3),item(3,"minecraft:lapis_lazuli",2,4)])]),
    "xuan_dao_sect_library": loot("Xuan Dao Sect — library. The sect's greatest treasure: Daoist scholarship.",[
        (2,4,[item(8,"minecraft:paper",3,7),item(7,"minecraft:ink_sac",2,5),item(6,"minecraft:book",2,4),
              item(5,"minecraft:redstone",2,4),item(4,"minecraft:lapis_lazuli",2,4)]),
        (1,2,[item(5,"minecraft:emerald",1,3),item(3,"minecraft:enchanted_book"),item(2,"minecraft:writable_book")])]),
    "xuan_dao_sect_alchemy_courtyard": loot("Xuan Dao Sect — alchemy_courtyard. Dao-essence pill refinement.",[
        (2,4,[item(7,"minecraft:paper",2,5),item(6,"minecraft:glass_bottle",2,5),item(5,"minecraft:coal",2,5),
              item(4,"minecraft:redstone",2,4),item(3,"minecraft:lapis_lazuli",1,3),item(2,"minecraft:blaze_powder",1,2)]),
        (1,2,[item(4,"minecraft:emerald",1,3),item(2,"minecraft:ghast_tear"),item(1,"ergenverse:spirit_stone",1,1)])]),
    "xuan_dao_sect_sword_peak": loot("Xuan Dao Sect — sword_peak. Dao-calligraphy sword art. INFERRED.",[
        (2,4,[item(7,"minecraft:iron_ingot",2,5),item(6,"minecraft:redstone",2,4),item(5,"minecraft:ink_sac",2,4),
              item(4,"minecraft:coal",2,4),item(3,"minecraft:lapis_lazuli",2,4),item(2,"minecraft:iron_sword")]),
        (1,2,[item(4,"minecraft:emerald",2,4),item(2,"minecraft:enchanted_book"),item(1,"minecraft:book")])]),
    "xuan_dao_sect_spirit_beast_pens": loot("Xuan Dao Sect — spirit_beast_pens. Dao-harmonized beasts.",[
        (2,4,[item(7,"minecraft:leather",2,5),item(6,"minecraft:redstone",2,4),item(5,"minecraft:paper",2,4),
              item(4,"minecraft:iron_ingot",1,3),item(3,"minecraft:lead",1,3),item(2,"minecraft:lapis_lazuli",1,3)]),
        (1,2,[item(4,"minecraft:emerald",1,3),item(2,"minecraft:compass"),item(1,"minecraft:string",2,6)])]),
    "xuan_dao_sect_array_hall": loot("Xuan Dao Sect — array_hall. The sect's core strength: formation mastery.",[
        (2,4,[item(8,"minecraft:redstone",4,10),item(6,"minecraft:obsidian",1,3),item(5,"minecraft:ink_sac",2,4),
              item(4,"minecraft:paper",2,5),item(3,"minecraft:lapis_lazuli",2,4),item(2,"minecraft:ender_pearl",1,1)]),
        (1,2,[item(5,"minecraft:emerald",2,5),item(2,"ergenverse:jade_slip"),item(1,"minecraft:enchanted_book")])]),
    "xuan_dao_sect_puppet_workshop": loot("Xuan Dao Sect — puppet_workshop. Dao-scripted constructs. INFERRED.",[
        (2,4,[item(7,"minecraft:redstone",3,7),item(6,"minecraft:paper",2,5),item(5,"minecraft:iron_ingot",2,4),
              item(4,"minecraft:ink_sac",2,4),item(3,"minecraft:lapis_lazuli",2,4),item(3,"minecraft:string",2,6)]),
        (1,2,[item(4,"minecraft:emerald",2,4),item(2,"minecraft:book"),item(1,"minecraft:redstone",4,8)])]),
    "xuan_dao_sect_mountain_cave": loot("Xuan Dao Sect — mountain_cave. Hidden Dao meditation retreat.",[
        (2,4,[item(7,"minecraft:paper",3,7),item(6,"minecraft:ink_sac",2,5),item(5,"minecraft:redstone",2,5),
              item(4,"minecraft:coal",2,5),item(3,"minecraft:lapis_lazuli",2,4),item(2,"minecraft:obsidian",1,2)]),
        (1,2,[item(3,"minecraft:emerald",2,5),item(2,"minecraft:enchanted_book"),item(1,"ergenverse:jade_slip")])]),
    "xuan_dao_sect_underground_passage": loot("Xuan Dao Sect — underground_passage. Restricted scroll storage.",[
        (3,5,[item(7,"minecraft:redstone",5,10),item(6,"minecraft:ink_sac",3,7),item(5,"minecraft:paper",3,6),
              item(4,"minecraft:obsidian",2,4),item(3,"minecraft:emerald",2,5),item(3,"minecraft:lapis_lazuli",3,6)]),
        (1,3,[item(4,"ergenverse:jade_slip",1,1),item(3,"minecraft:enchanted_book"),
              item(2,"ergenverse:spirit_stone",1,1),item(1,"minecraft:ender_pearl",1,2)])]),
    "xuan_dao_sect_secret_pavilion": loot("Xuan Dao Sect — secret_pavilion. Hidden Dao comprehension techniques.",[
        (3,5,[item(6,"minecraft:ink_sac",3,6),item(6,"minecraft:redstone",3,6),item(5,"minecraft:book",2,4),
              item(4,"minecraft:obsidian",2,4),item(3,"minecraft:emerald",2,5)]),
        (1,3,[item(4,"ergenverse:jade_slip",1,1),item(3,"minecraft:enchanted_book"),
              item(2,"ergenverse:spirit_stone",1,1),item(1,"ergenverse:dao_fragment")])]),
    "xuan_dao_sect_inner_sect": loot("Xuan Dao Sect — inner_sect. Elder scholarly quarters.",[
        (3,5,[item(6,"minecraft:ink_sac",3,6),item(5,"minecraft:redstone",3,6),item(5,"minecraft:paper",3,6),
              item(4,"minecraft:emerald",3,7),item(3,"minecraft:obsidian",2,4),item(2,"minecraft:gold_ingot",1,3)]),
        (1,3,[item(4,"ergenverse:jade_slip",1,2),item(3,"ergenverse:spirit_stone",1,1),
              item(2,"minecraft:enchanted_book"),item(1,"ergenverse:dao_fragment")])]),
    "xuan_dao_sect_hidden_treasury": loot("Xuan Dao Sect — hidden_treasury. Sect wealth. Scholarly treasures.",[
        (3,6,[item(5,"minecraft:emerald",3,8),item(5,"minecraft:ink_sac",4,8),item(4,"minecraft:gold_ingot",2,5),
              item(4,"minecraft:redstone",3,6),item(3,"minecraft:obsidian",2,4),item(2,"minecraft:diamond",1,1)]),
        (2,3,[item(4,"ergenverse:spirit_stone",1,2),item(3,"ergenverse:jade_slip",1,1),
              item(2,"minecraft:enchanted_book"),item(1,"ergenverse:dao_fragment"),item(1,"ergenverse:spirit_stone_mid",1,1)])]),
    "xuan_dao_sect_sword_tomb": loot("Xuan Dao Sect — sword_tomb. Weapons of Daoist predecessors. INFERRED.",[
        (3,5,[item(6,"minecraft:iron_ingot",3,6),item(5,"minecraft:ink_sac",2,5),item(5,"minecraft:redstone",2,5),
              item(4,"minecraft:emerald",2,5),item(3,"minecraft:obsidian",2,4),item(2,"minecraft:lapis_lazuli",3,6)]),
        (1,3,[item(3,"ergenverse:jade_slip",1,1),item(2,"minecraft:enchanted_book"),
              item(2,"minecraft:ender_pearl",1,2),item(1,"ergenverse:spirit_stone",1,1)])]),
    "xuan_dao_sect_ancestor_hall": loot("Xuan Dao Sect — ancestor_hall. Xuan Dao Ancestor's shrine. Dao convergence.",[
        (3,6,[item(5,"minecraft:emerald",4,10),item(5,"minecraft:ink_sac",5,10),item(4,"minecraft:obsidian",3,6),
              item(4,"minecraft:redstone",3,6),item(3,"minecraft:gold_ingot",2,5),item(2,"minecraft:diamond",1,2)]),
        (2,3,[item(4,"ergenverse:spirit_stone",1,3),item(3,"ergenverse:jade_slip",1,2),
              item(2,"ergenverse:dao_fragment"),item(2,"minecraft:enchanted_book"),item(1,"ergenverse:spirit_stone_mid",1,2)]),
        (0,2,[item(2,"ergenverse:jade_slip"),item(1,"ergenverse:spirit_stone_high",1,2),empty(5)])]),
    "xuan_dao_sect_core_formation_hall": loot("Xuan Dao Sect — core_formation_hall. Advanced formation research.",[
        (3,6,[item(5,"minecraft:emerald",4,10),item(5,"minecraft:redstone",5,10),item(4,"minecraft:obsidian",3,6),
              item(4,"minecraft:ink_sac",4,8),item(3,"minecraft:gold_ingot",2,5),item(2,"minecraft:diamond",1,2)]),
        (2,3,[item(4,"ergenverse:spirit_stone",2,4),item(3,"ergenverse:jade_slip",1,2),
              item(2,"ergenverse:dao_fragment"),item(2,"minecraft:enchanted_book"),item(1,"ergenverse:spirit_stone_mid",1,2)]),
        (0,2,[item(2,"ergenverse:jade_slip"),item(1,"ergenverse:spirit_stone_high",1,2),empty(6)])]),
}

NPCS = [
    {"npc_id":"npc_xd_outer_disciple_mu","name":"Outer Disciple Mu","nameCn":"外门弟子慕",
     "canon_id":None,"type":"sect_disciple","faction":"xuan_dao_sect","location":"xuan_dao_sect_outer_gate",
     "cultivation":"CONDENSATION early","personality":"earnest, calligraphy-obsessed, quotes the Dao",
     "speech":"formal, slightly pretentious, references ancient texts","relationship_to_wanglin":"neutral",
     "dialogue_available":True,"quest_available":True,"trade_available":True,"teaching_available":False,
     "canon_confidence":0,"derivation_type":"I","salt":400101,
     "initiation_lines":["The Xuan Dao Sect seeks the Profound Way through scholarship, not violence. Though we are not incapable of the latter.",
        "Every formation begins with a single stroke. The ancestor said: 'The Dao is in the ink.'",
        "A cultivator from Nan Dou City came here seeking to join. We tested his formation comprehension. He failed.","The guard post needs supplies. If you wish to enter, bring paper and ink. We value preparation."],
     "daily_schedule":[{"time":"0600","action":"patrol","location":"xuan_dao_sect_outer_gate","duration":120},
        {"time":"0800","action":"stand_guard","location":"xuan_dao_sect_outer_gate","duration":240},
        {"time":"1200","action":"eat","location":"xuan_dao_sect_main_plaza","duration":60},
        {"time":"1300","action":"patrol","location":"xuan_dao_sect_main_plaza","duration":120},
        {"time":"1500","action":"train","location":"xuan_dao_sect_trial_grounds","duration":120},
        {"time":"1700","action":"stand_guard","location":"xuan_dao_sect_outer_gate","duration":180},
        {"time":"2000","action":"rest","location":"xuan_dao_sect_disciple_dormitories","duration":600}],
     "sect_tasks":[{"task_id":"gate_scholarship_supplies","name":"Gate Scholarship Supplies",
         "description":"The outer gate guard needs paper and ink for the formation inspection logs.",
         "required_items":{"minecraft:arrow":8,"minecraft:paper":6},"rewards":{"minecraft:emerald":5,"minecraft:book":1}},
        {"task_id":"formation_inspection_log","name":"Formation Inspection Log",
         "description":"Record all formation fluctuations at the gate. Bring ink and paper for the logbook.",
         "required_items":{"minecraft:paper":5,"minecraft:ink_sac":3},"rewards":{"minecraft:emerald":4,"minecraft:experience_bottle":1}}]},

    {"npc_id":"npc_xd_formation_master_jing","name":"Formation Master Jing","nameCn":"阵法大师静",
     "canon_id":None,"type":"sect_elder","faction":"xuan_dao_sect","location":"xuan_dao_sect_array_hall",
     "cultivation":"NASCENT_SOUL early","personality":"patient, sees the world as interconnected patterns",
     "speech":"methodical, uses metaphors of weaving and calligraphy","relationship_to_wanglin":"neutral",
     "dialogue_available":True,"quest_available":True,"trade_available":True,"teaching_available":False,
     "canon_confidence":0,"derivation_type":"I","salt":400102,
     "initiation_lines":["A formation is a question written in redstone and obsidian. The world answers if you ask correctly.",
        "The Heavenly Fate Sect sent an array student here once. His technique was brilliant but rigid. I sent him back to think more freely.",
        "Every formation has a center stroke. Find it, and the entire pattern reveals itself. Miss it, and you are lost in the lines.",
        "I need redstone and obsidian for the perimeter formation. The western quadrant is weakening."],
     "daily_schedule":[{"time":"0500","action":"inspect","location":"xuan_dao_sect_array_hall","duration":180},
        {"time":"0800","action":"repair","location":"xuan_dao_sect_outer_gate","duration":120},
        {"time":"1000","action":"work","location":"xuan_dao_sect_array_hall","duration":240},
        {"time":"1400","action":"eat","location":"xuan_dao_sect_main_plaza","duration":60},
        {"time":"1500","action":"inspect","location":"xuan_dao_sect_underground_passage","duration":120},
        {"time":"1700","action":"work","location":"xuan_dao_sect_array_hall","duration":180},
        {"time":"2000","action":"rest","location":"xuan_dao_sect_inner_sect","duration":600}],
     "sect_tasks":[{"task_id":"perimeter_formation_repair","name":"Perimeter Formation Repair",
         "description":"The western quadrant needs redstone and obsidian for the Dao-thread formation repair.",
         "required_items":{"minecraft:redstone":12,"minecraft:obsidian":6},"rewards":{"ergenverse:jade_slip":1,"minecraft:enchanted_book":1}},
        {"task_id":"formation_ink_supplies","name":"Formation Ink Supplies",
         "description":"I need ink and ender pearls to draw the spatial anchor inscriptions.",
         "required_items":{"minecraft:ink_sac":5,"minecraft:ender_pearl":2},"rewards":{"minecraft:emerald":8,"ergenverse:jade_slip":1}}]},

    {"npc_id":"npc_xd_scholar_li","name":"Senior Scholar Li","nameCn":"藏经阁主李",
     "canon_id":None,"type":"sect_elder","faction":"xuan_dao_sect","location":"xuan_dao_sect_library",
     "cultivation":"NASCENT_SOUL early","personality":"reverent toward texts, dismissive of people",
     "speech":"erudite, quotes scrolls, pedantic","relationship_to_wanglin":"neutral",
     "dialogue_available":True,"quest_available":True,"trade_available":True,"teaching_available":False,
     "canon_confidence":0,"derivation_type":"I","salt":400103,
     "initiation_lines":["The library holds five thousand scrolls. I have read four thousand nine hundred and twelve. The remaining eighty-eight are restricted.",
        "Ink is the blood of scholarship. Paper is its skin. Together they preserve the Dao for eternity.",
        "A rogue cultivator from this sect was seen in Nan Dou City. He left without permission. His name has been struck from the records.",
        "Bring me paper and ink, and I will share what the scrolls have taught us about the Dao."],
     "daily_schedule":[{"time":"0600","action":"organize","location":"xuan_dao_sect_library","duration":240},
        {"time":"1000","action":"study","location":"xuan_dao_sect_library","duration":180},
        {"time":"1300","action":"eat","location":"xuan_dao_sect_main_plaza","duration":60},
        {"time":"1400","action":"teach","location":"xuan_dao_sect_main_plaza","duration":120},
        {"time":"1600","action":"study","location":"xuan_dao_sect_library","duration":240},
        {"time":"2000","action":"rest","location":"xuan_dao_sect_disciple_dormitories","duration":600}],
     "sect_tasks":[{"task_id":"scroll_copying_dao","name":"Scroll Copying: Dao Comprehension",
         "description":"Copy the Dao comprehension scrolls for the inner elders. The ink must be precise.",
         "required_items":{"minecraft:paper":8,"minecraft:ink_sac":5},"rewards":{"minecraft:book":2,"minecraft:emerald":6}},
        {"task_id":"restricted_scroll_recovery","name":"Restricted Scroll Recovery",
         "description":"A scroll was removed from the restricted section. Return it with proper compensation.",
         "required_items":{"minecraft:book":2,"minecraft:emerald":4},"rewards":{"minecraft:enchanted_book":1,"ergenverse:jade_slip":1}}]},

    {"npc_id":"npc_xd_alchemist_bai","name":"Dao Alchemist Bai","nameCn":"丹师白",
     "canon_id":None,"type":"sect_elder","faction":"xuan_dao_sect","location":"xuan_dao_sect_alchemy_courtyard",
     "cultivation":"NASCENT_SOUL mid","personality":"philosophical about alchemy, sees pills as micro-formations",
     "speech":"reflective, draws parallels between alchemy and the Dao","relationship_to_wanglin":"neutral",
     "dialogue_available":True,"quest_available":True,"trade_available":True,"teaching_available":False,
     "canon_confidence":0,"derivation_type":"I","salt":400104,
     "initiation_lines":["A pill is a formation you swallow. The ingredients are the redstone. The cauldron is the obsidian. The result is comprehension.",
        "The Dao-essence Pill requires lapis and spring water. The blue color is not decorative. It is structural.",
        "The Soul Refining Sect wanted our pill formula. We offered it freely. Their corruption ruined the ingredients.",
        "Bring me bottles and coal. The furnace does not burn with flame. It burns with comprehension."],
     "daily_schedule":[{"time":"0500","action":"gather","location":"xuan_dao_sect_spirit_herb_garden","duration":120},
        {"time":"0700","action":"work","location":"xuan_dao_sect_alchemy_courtyard","duration":300},
        {"time":"1200","action":"eat","location":"xuan_dao_sect_main_plaza","duration":60},
        {"time":"1300","action":"work","location":"xuan_dao_sect_alchemy_courtyard","duration":300},
        {"time":"1800","action":"study","location":"xuan_dao_sect_library","duration":120},
        {"time":"2000","action":"work","location":"xuan_dao_sect_alchemy_courtyard","duration":180},
        {"time":"2300","action":"rest","location":"xuan_dao_sect_inner_sect","duration":420}],
     "sect_tasks":[{"task_id":"dao_furnace_fuel","name":"Dao Furnace Fuel",
         "description":"The Dao-essence furnace needs glass bottles and coal for the comprehension-burning cycle.",
         "required_items":{"minecraft:glass_bottle":5,"minecraft:coal":8},"rewards":{"minecraft:experience_bottle":2,"minecraft:emerald":7}},
        {"task_id":"lapis_pill_ingredients","name":"Lapis Pill Ingredients",
         "description":"The Dao-essence Pill needs lapis lazuli and blaze powder for the blue-structure binding.",
         "required_items":{"minecraft:lapis_lazuli":6,"minecraft:blaze_powder":3},"rewards":{"minecraft:enchanted_book":1,"ergenverse:jade_slip":1}}]},

    {"npc_id":"npc_xd_sword_calligrapher_feng","name":"Sword Calligrapher Feng","nameCn":"剑书圣凤",
     "canon_id":None,"type":"sect_elder","faction":"xuan_dao_sect","location":"xuan_dao_sect_sword_tomb",
     "cultivation":"NASCENT_SOUL late","personality":"treats sword as brush, violence as calligraphy",
     "speech":"artistic, references strokes and composition","relationship_to_wanglin":"neutral",
     "dialogue_available":True,"quest_available":True,"trade_available":True,"teaching_available":False,
     "canon_confidence":0,"derivation_type":"I","salt":400105,
     "initiation_lines":["Every sword stroke is a brush stroke. Every battle is a composition. The difference between art and death is intention.",
        "The swords here were not forged. They were written. Their edges are the ancestor's calligraphy made steel.",
        "One sword in the tomb has no inscription. It was the ancestor's first. He could not bring himself to improve it.",
        "Bring me iron and ink. I am composing a new sword-form. The ink guides the metal."],
     "daily_schedule":[{"time":"0500","action":"meditate","location":"xuan_dao_sect_sword_tomb","duration":180},
        {"time":"0800","action":"maintain","location":"xuan_dao_sect_sword_tomb","duration":300},
        {"time":"1300","action":"eat","location":"xuan_dao_sect_main_plaza","duration":60},
        {"time":"1400","action":"maintain","location":"xuan_dao_sect_sword_tomb","duration":240},
        {"time":"1800","action":"practice","location":"xuan_dao_sect_sword_peak","duration":120},
        {"time":"2000","action":"meditate","location":"xuan_dao_sect_sword_tomb","duration":180},
        {"time":"2300","action":"rest","location":"xuan_dao_sect_sword_tomb","duration":420}],
     "sect_tasks":[{"task_id":"sword_composition_ink","name":"Sword Composition Ink",
         "description":"I need iron and ink for the new sword-form composition. The ink binds the metal's intent.",
         "required_items":{"minecraft:iron_ingot":6,"minecraft:ink_sac":6},"rewards":{"ergenverse:jade_slip":2,"minecraft:enchanted_book":1}},
        {"task_id":"tomb_preservation","name":"Tomb Preservation Materials",
         "description":"The sword tomb needs lapis and gold for the Dao-preservation ritual.",
         "required_items":{"minecraft:lapis_lazuli":8,"minecraft:gold_ingot":4},"rewards":{"ergenverse:spirit_stone":2,"minecraft:enchanted_book":1}}]},

    {"npc_id":"npc_xd_beast_harmonizer_he","name":"Beast Harmonizer He","nameCn":"灵兽调和师何",
     "canon_id":None,"type":"sect_disciple","faction":"xuan_dao_sect","location":"xuan_dao_sect_spirit_beast_pens",
     "cultivation":"CONDENSATION late","personality":"calm, believes beasts have Dao nature too",
     "speech":"gentle, philosophical about harmony","relationship_to_wanglin":"neutral",
     "dialogue_available":True,"quest_available":True,"trade_available":True,"teaching_available":False,
     "canon_confidence":0,"derivation_type":"I","salt":400106,
     "initiation_lines":["These beasts do not serve us. They walk the Dao alongside us. The leash is merely a suggestion.",
        "The ink-wolves in the eastern pen were bred here. Their howls form formation patterns. The ancestor was delighted.",
        "A beast that has achieved Dao comprehension will sit still for hours, staring at a single redstone line. I have seen it.",
        "Bring me redstone and leather. The harmonization collars need adjustment."],
     "daily_schedule":[{"time":"0600","action":"work","location":"xuan_dao_sect_spirit_beast_pens","duration":300},
        {"time":"1100","action":"inspect","location":"xuan_dao_sect_mountain_cave","duration":120},
        {"time":"1300","action":"eat","location":"xuan_dao_sect_main_plaza","duration":60},
        {"time":"1400","action":"work","location":"xuan_dao_sect_spirit_beast_pens","duration":240},
        {"time":"1800","action":"deliver","location":"xuan_dao_sect_alchemy_courtyard","duration":60},
        {"time":"1900","action":"rest","location":"xuan_dao_sect_disciple_dormitories","duration":660}],
     "sect_tasks":[{"task_id":"harmonization_collar","name":"Harmonization Collar Repair",
         "description":"The beast collars need redstone and leather for the Dao-harmonization adjustment.",
         "required_items":{"minecraft:redstone":8,"minecraft:leather":5},"rewards":{"minecraft:emerald":7,"ergenverse:jade_slip":1}},
        {"task_id":"beast_herbs","name":"Beast Herb Delivery",
         "description":"Bring bone meal and wheat from the garden for the Dao-sensitive beasts.",
         "required_items":{"minecraft:bone_meal":6,"minecraft:wheat":8},"rewards":{"minecraft:emerald":5,"minecraft:lapis_lazuli":4}}]},

    {"npc_id":"npc_xd_trial_inscriber_song","name":"Trial Inscriber Song","nameCn":"试炼官宋",
     "canon_id":None,"type":"sect_elder","faction":"xuan_dao_sect","location":"xuan_dao_sect_trial_grounds",
     "cultivation":"NASCENT_SOUL mid","personality":"methodical, records everything in formation patterns",
     "speech":"administrative, references statistics and patterns","relationship_to_wanglin":"neutral",
     "dialogue_available":True,"quest_available":True,"trade_available":True,"teaching_available":False,
     "canon_confidence":0,"derivation_type":"I","salt":400107,
     "initiation_lines":["The trial has three stages: comprehend, inscribe, activate. Most fail at activation. The formation collapses.",
        "Sixty-seven percent of disciples pass the comprehension stage. Thirty-one percent pass inscription. Eight percent activate.",
        "A disciple from Heng Yue Sect attempted our trials once. She passed comprehension, failed inscription. She was gracious about it.",
        "The trial grounds need redstone and arrows. The comprehension array consumes both."],
     "daily_schedule":[{"time":"0700","action":"oversee","location":"xuan_dao_sect_trial_grounds","duration":240},
        {"time":"1100","action":"evaluate","location":"xuan_dao_sect_main_plaza","duration":120},
        {"time":"1300","action":"eat","location":"xuan_dao_sect_main_plaza","duration":60},
        {"time":"1400","action":"oversee","location":"xuan_dao_sect_trial_grounds","duration":240},
        {"time":"1800","action":"report","location":"xuan_dao_sect_inner_sect","duration":120},
        {"time":"2000","action":"rest","location":"xuan_dao_sect_disciple_dormitories","duration":600}],
     "sect_tasks":[{"task_id":"trial_array_maintenance","name":"Trial Array Maintenance",
         "description":"The trial formation arrays need redstone and lapis for the comprehension stage.",
         "required_items":{"minecraft:redstone":10,"minecraft:lapis_lazuli":6},"rewards":{"minecraft:emerald":8,"minecraft:experience_bottle":2}},
        {"task_id":"trial_record_compilation","name":"Trial Record Compilation",
         "description":"Compile trial results. Bring paper and ink for the formation-pattern logs.",
         "required_items":{"minecraft:paper":8,"minecraft:ink_sac":5},"rewards":{"minecraft:emerald":6,"minecraft:enchanted_book":1}}]},

    {"npc_id":"npc_xd_underground_scribe_yuan","name":"Underground Scribe Yuan","nameCn":"地下书吏袁",
     "canon_id":None,"type":"sect_disciple","faction":"xuan_dao_sect","location":"xuan_dao_sect_underground_passage",
     "cultivation":"FOUNDATION_ESTABLISHMENT late","personality":"obsessive note-taker, writes in the dark",
     "speech":"muttering, reads aloud what he writes, slightly unhinged","relationship_to_wanglin":"neutral",
     "dialogue_available":True,"quest_available":True,"trade_available":True,"teaching_available":False,
     "canon_confidence":0,"derivation_type":"I","salt":400108,
     "initiation_lines":["You found the underground passage. I come here to write where no one can read over my shoulder.",
        "The restricted scrolls down here are not dangerous. They are merely... incomplete. The ancestor died before finishing them.",
        "I have been copying the forbidden formation diagrams for three years. I am on scroll seven hundred and twelve. There are three thousand.",
        "Bring me paper and ink. I will share something interesting from the restricted section."],
     "daily_schedule":[{"time":"0800","action":"idle","location":"xuan_dao_sect_underground_passage","duration":180},
        {"time":"1100","action":"research","location":"xuan_dao_sect_underground_passage","duration":120},
        {"time":"1300","action":"eat","location":"xuan_dao_sect_main_plaza","duration":60},
        {"time":"1400","action":"idle","location":"xuan_dao_sect_underground_passage","duration":180},
        {"time":"1700","action":"research","location":"xuan_dao_sect_underground_passage","duration":120},
        {"time":"1900","action":"rest","location":"xuan_dao_sect_disciple_dormitories","duration":660}],
     "sect_tasks":[{"task_id":"forbidden_copying","name":"Forbidden Diagram Copying",
         "description":"I need paper and ink to continue copying the ancestor's unfinished formation diagrams.",
         "required_items":{"minecraft:paper":10,"minecraft:ink_sac":6},"rewards":{"ergenverse:jade_slip":2,"minecraft:emerald":7}},
        {"task_id":"hidden_scroll_access","name":"Hidden Scroll Access",
         "description":"I can show you the ancestor's final unfinished scroll. The price is emeralds and gold.",
         "required_items":{"minecraft:gold_ingot":3,"minecraft:emerald":8},"rewards":{"ergenverse:spirit_stone":2,"ergenverse:jade_slip":2}}]},

    {"npc_id":"npc_xd_ancestor_curator_chen","name":"Ancestor Curator Chen","nameCn":"祖师殿主陈",
     "canon_id":None,"type":"sect_elder","faction":"xuan_dao_sect","location":"xuan_dao_sect_ancestor_hall",
     "cultivation":"NASCENT_SOUL peak","personality":"serene, embodies Daoist tranquility, rarely speaks first",
     "speech":"sparse, each word deliberate, pauses between sentences","relationship_to_wanglin":"neutral",
     "dialogue_available":True,"quest_available":True,"trade_available":True,"teaching_available":False,
     "canon_confidence":0,"derivation_type":"I","salt":400109,
     "initiation_lines":["...The ancestor wrote one scroll before founding this sect. It says: 'The Dao is a question. The answer is silence.'",
        "Three thousand years. The ancestor's comprehension has not diminished. If anything, the silence has deepened.",
        "A visitor came from the Heng Yue Sect once. He stood before the ancestor's shrine for three days without speaking. When he left, he was crying.",
        "Bring obsidian and gold. The shrine maintenance requires devotion. The Dao rewards patience."],
     "daily_schedule":[{"time":"0500","action":"meditate","location":"xuan_dao_sect_ancestor_hall","duration":240},
        {"time":"0900","action":"ritual","location":"xuan_dao_sect_ancestor_hall","duration":180},
        {"time":"1200","action":"eat","location":"xuan_dao_sect_inner_sect","duration":60},
        {"time":"1300","action":"maintain","location":"xuan_dao_sect_ancestor_hall","duration":240},
        {"time":"1700","action":"ritual","location":"xuan_dao_sect_ancestor_hall","duration":180},
        {"time":"2000","action":"meditate","location":"xuan_dao_sect_ancestor_hall","duration":300},
        {"time":"0100","action":"rest","location":"xuan_dao_sect_ancestor_hall","duration":300}],
     "sect_tasks":[{"task_id":"ancestor_shrine_devotion","name":"Ancestor Shrine Devotion",
         "description":"The ancestor's shrine requires gold and obsidian for the weekly Dao-contemplation ritual.",
         "required_items":{"minecraft:gold_ingot":4,"minecraft:obsidian":6},"rewards":{"ergenverse:spirit_stone":3,"minecraft:emerald":6}},
        {"task_id":"ancestor_scroll_preservation","name":"Ancestor Scroll Preservation",
         "description":"Preserve the ancestor's original scroll. The ink is fading and needs renewal.",
         "required_items":{"minecraft:ink_sac":8,"minecraft:paper":6,"minecraft:book":2},"rewards":{"ergenverse:jade_slip":2,"ergenverse:spirit_stone":2}}]},

    {"npc_id":"npc_xd_inner_scribe_wu","name":"Inner Scribe Wu","nameCn":"内门书吏吴",
     "canon_id":None,"type":"sect_elder","faction":"xuan_dao_sect","location":"xuan_dao_sect_core_formation_hall",
     "cultivation":"NASCENT_SOUL peak","personality":"intellectually ambitious, believes writing can reshape reality",
     "speech":"theoretical, references abstract Dao concepts, occasionally visionary","relationship_to_wanglin":"neutral",
     "dialogue_available":True,"quest_available":True,"trade_available":True,"teaching_available":False,
     "canon_confidence":0,"derivation_type":"I","salt":400110,
     "initiation_lines":["The ancestor's final theory: if a formation is written with perfect comprehension, it becomes permanent. Self-sustaining. Alive.",
        "I am close to proving this. I need spirit stones to power the experimental array. And patience. Mostly patience.",
        "The Corpse Yin Sect writes with bone. The Heavenly Fate Sect writes with starlight. We write with ink. All three are correct. None are complete.",
        "Bring me spirit stones and emeralds. I am conducting research that could redefine formation theory."],
     "daily_schedule":[{"time":"0500","action":"research","location":"xuan_dao_sect_core_formation_hall","duration":300},
        {"time":"1000","action":"inspect","location":"xuan_dao_sect_inner_sect","duration":120},
        {"time":"1200","action":"meet","location":"xuan_dao_sect_secret_pavilion","duration":120},
        {"time":"1400","action":"research","location":"xuan_dao_sect_core_formation_hall","duration":240},
        {"time":"1800","action":"inspect","location":"xuan_dao_sect_hidden_treasury","duration":120},
        {"time":"2000","action":"research","location":"xuan_dao_sect_core_formation_hall","duration":180},
        {"time":"2300","action":"rest","location":"xuan_dao_sect_inner_sect","duration":420}],
     "sect_tasks":[{"task_id":"formation_research","name":"Formation Research Materials",
         "description":"I need spirit stones and emeralds to power the self-sustaining formation experiment.",
         "required_items":{"ergenverse:spirit_stone":3,"minecraft:emerald":8},"rewards":{"minecraft:enchanted_book":2,"ergenverse:jade_slip":2}},
        {"task_id":"obsidian_dao_conduit","name":"Obsidian Dao Conduit",
         "description":"The experimental array needs obsidian conduits to channel the Dao-comprehension flow.",
         "required_items":{"minecraft:obsidian":10,"minecraft:redstone":12,"minecraft:ender_pearl":3},"rewards":{"ergenverse:jade_slip":2,"ergenverse:spirit_stone":3}}]},

    {"npc_id":"npc_xd_herb_tender_qing","name":"Herb Tender Qing","nameCn":"药园管事清",
     "canon_id":None,"type":"sect_disciple","faction":"xuan_dao_sect","location":"xuan_dao_sect_spirit_herb_garden",
     "cultivation":"FOUNDATION_ESTABLISHMENT mid","personality":"peaceful, sees gardening as Dao cultivation",
     "speech":"gentle, botanical, draws parallels between growth and comprehension","relationship_to_wanglin":"neutral",
     "dialogue_available":True,"quest_available":True,"trade_available":True,"teaching_available":False,
     "canon_confidence":0,"derivation_type":"I","salt":400111,
     "initiation_lines":["Every herb grows toward comprehension. Some faster than others. The Dao-blue lotus has been growing for four hundred years.",
        "Bone meal nourishes the roots. But only spring water nourishes the comprehension. The difference is the Dao.",
        "The Alchemist needs the Dao-blue lotus petals. They only fall when a cultivator with high comprehension walks past. It has been two years since the last fall.",
        "Bring me bone meal and wheat. The herbs need feeding."],
     "daily_schedule":[{"time":"0600","action":"tend","location":"xuan_dao_sect_spirit_herb_garden","duration":300},
        {"time":"1100","action":"gather","location":"xuan_dao_sect_spirit_spring","duration":120},
        {"time":"1300","action":"eat","location":"xuan_dao_sect_main_plaza","duration":60},
        {"time":"1400","action":"tend","location":"xuan_dao_sect_spirit_herb_garden","duration":240},
        {"time":"1800","action":"deliver","location":"xuan_dao_sect_alchemy_courtyard","duration":60},
        {"time":"1900","action":"rest","location":"xuan_dao_sect_disciple_dormitories","duration":660}],
     "sect_tasks":[{"task_id":"garden_dao_soil","name":"Garden Dao-Soil Nourishment",
         "description":"The Dao-attributed herbs need bone meal and wheat to maintain comprehension-rich soil.",
         "required_items":{"minecraft:bone_meal":6,"minecraft:wheat":8},"rewards":{"minecraft:emerald":5,"minecraft:lapis_lazuli":3}},
        {"task_id":"herb_delivery_alchemist","name":"Herb Delivery to Alchemist",
         "description":"The Alchemist needs fresh herbs and bottles from the garden and spring.",
         "required_items":{"minecraft:glass_bottle":3,"minecraft:bone_meal":4},"rewards":{"minecraft:emerald":6,"minecraft:experience_bottle":1}}]},
]


def main():
    os.makedirs(LOOT_DIR, exist_ok=True)
    os.makedirs(NPC_DIR, exist_ok=True)
    lc = nc = 0
    for name, data in T.items():
        with open(os.path.join(LOOT_DIR, name+".json"),"w",encoding="utf-8") as f:
            json.dump(data,f,indent=2,ensure_ascii=False)
        lc += 1; print(f"  [LOOT] {name}.json")
    for npc in NPCS:
        npc["_xianxia_schema"] = 1
        with open(os.path.join(NPC_DIR, npc["npc_id"]+".json"),"w",encoding="utf-8") as f:
            json.dump(npc,f,indent=2,ensure_ascii=False)
        nc += 1; print(f"  [NPC]  {npc['npc_id']}.json")
    print(f"\nDone: {lc} loot tables + {nc} NPCs for Xuan Dao Sect.")

if __name__ == "__main__":
    main()