#!/usr/bin/env python3
"""
AUTO-CANON-031: Add 9 INFERRED NPCs to Teng Family City.
Per Article XXI (world is main character), XXIII (vertical slice), XXIV (NPCs initiate).
Teng Family City has 11 structures but only 3 NPCs — the city feels dead.
These INFERRED NPCs fill functional roles implied by canon: the Teng family
is the dominant political/military force in Zhao Country's mortal world.
They oppress villages, control trade, and have dark secrets (smuggler tunnels).

Economy design (circulating):
  Player gathers raw materials (wheat/coal/iron) → trades to servants/merchants for emeralds
  → uses emeralds to buy info from smuggler/tavern keeper → gets better rewards from Xiuxiu
  → cycles back to gather more materials for Teng Huayuan/Teng Li's demands.
"""

import json
import os

NPC_DIR = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources", "data", "ergenverse", "npcs")

def make_npc(npc_id, name, nameCn, npc_type, faction, location, cultivation,
             personality, speech, canon_note, derivation_type, initiation_lines,
             daily_schedule, sect_tasks, canon_confidence=1, relationship="INFERRED"):
    return {
        "_comment": f"NPC: {name} ({nameCn}). INFERRED from canon context: {canon_note}. Derivation: {derivation_type} = INFERRED. Per Article XV, this NPC fills a role implied by canon but not explicitly named.",
        "npc_id": npc_id,
        "name": name,
        "nameCn": nameCn,
        "canon_id": f"I-{npc_id}",
        "type": npc_type,
        "faction": faction,
        "location": location,
        "cultivation": cultivation,
        "personality": personality,
        "speech": speech,
        "relationship_to_wanglin": relationship,
        "dialogue_available": True,
        "quest_available": len(sect_tasks) > 0,
        "trade_available": False,
        "teaching_available": False,
        "perception_tiers": {
            "mortal": f"A {personality.split(',')[0]} {npc_type}",
            "qi_condensation": f"{name}, {faction} {npc_type}"
        },
        "canon_confidence": canon_confidence,
        "note": f"INFERRED: {canon_note}",
        "derivation_type": derivation_type,
        "salt": hash(npc_id) % 2147483647,
        "dao_heart": {
            "stability": 50,
            "cracks": [],
            "last_tested_tick": None,
            "note": "0-100. A cracked Dao heart blocks breakthrough regardless of Qi."
        },
        "soul_state": "none",
        "tribulation_debt": 0,
        "_xianxia_schema": 1,
        "initiation_lines": initiation_lines,
        "daily_schedule": daily_schedule,
        "sect_tasks": sect_tasks
    }

NPCS = [
    # 1. Teng Family Guard Captain — patrols city gate, enforces curfew
    make_npc(
        npc_id="npc_teng_guard_captain",
        name="Teng Guard Captain",
        nameCn="藤家护卫统领",
        npc_type="guard",
        faction="teng_family",
        location="teng_family_city",
        cultivation="mid Foundation Establishment",
        personality="stern, disciplined, loyal to Teng Huayuan",
        speech="blunt, authoritative",
        canon_note="The Teng family is the dominant military force in Zhao Country. A city of this size and importance would have a guard captain overseeing security, curfews, and tribute enforcement. The city gate structure implies a garrison.",
        derivation_type="I",
        initiation_lines=[
            "Halt. State your name and business in Teng Family City. Strangers are watched here.",
            "The patriarch's orders are clear: no unauthorized cultivators beyond the market district after dusk. I enforce that.",
            "You look capable enough. The warehouse district needs extra hands moving tribute shipments. Speak to the Dock Foreman if you want to earn coin.",
            "The smuggler tunnels? There are no smuggler tunnels. I suggest you stop asking questions.",
        ],
        daily_schedule=[
            {"t0": 0, "t1": 2000, "act": "sleeping", "dir": None, "dist": 0},
            {"t0": 2000, "t1": 4000, "act": "wandering", "dir": "north", "dist": 8},   # patrol to city gate
            {"t0": 4000, "t1": 5000, "act": "eating", "dir": None, "dist": 0},
            {"t0": 5000, "t1": 8000, "act": "wandering", "dir": "east", "dist": 12},   # patrol market
            {"t0": 8000, "t1": 9000, "act": "eating", "dir": None, "dist": 0},
            {"t0": 9000, "t1": 12000, "act": "wandering", "dir": "south", "dist": 10}, # patrol docks
            {"t0": 12000, "t1": 24000, "act": "sleeping", "dir": None, "dist": 0},
        ],
        sect_tasks=[
            {
                "id": "patrol_supplies",
                "description": "The Guard Captain crosses his arms: 'My men need leather armor patches and arrows for the night patrol. Bring me leather and arrows and I will pay you from the garrison fund.'",
                "requires": [{"item": "minecraft:leather", "count": 8}, {"item": "minecraft:arrow", "count": 16}],
                "reward_item": "minecraft:emerald", "reward_count": 4
            },
            {
                "id": "curfew_report",
                "description": "The Guard Captain frowns: 'There have been unauthorized movements near the warehouse at night. Bring me coal and torches — we need to light the patrol route. Report anything suspicious.'",
                "requires": [{"item": "minecraft:coal", "count": 16}, {"item": "minecraft:torch", "count": 8}],
                "reward_item": "minecraft:iron_ingot", "reward_count": 4
            },
        ],
    ),

    # 2. Teng Family Merchant — market district
    make_npc(
        npc_id="npc_teng_merchant",
        name="Teng Family Merchant",
        nameCn="藤家商贩",
        npc_type="merchant",
        faction="teng_family",
        location="teng_family_city",
        cultivation="mortal",
        personality="shrewd, opportunistic, respectful to power",
        speech="salesman-like, obsequious to cultivators",
        canon_note="The Teng family controls trade in Zhao Country. The market district structure implies commerce. A merchant NPC provides the player with a trading partner and economic node in the city.",
        derivation_type="I",
        initiation_lines=[
            "Welcome, welcome! Fine goods from across Zhao Country — iron from the northern mines, wheat from the southern villages. What catches your eye?",
            "The Teng family takes a modest commission on all trade, of course. But my prices remain fair. Fair for both of us, heh.",
            "I heard the dock foreman needs help with a shipment. Heavy crates of... well, he did not say. Paid well though.",
            "You look like someone who travels. I might have a special item — if you bring me the right materials.",
        ],
        daily_schedule=[
            {"t0": 0, "t1": 2000, "act": "sleeping", "dir": None, "dist": 0},
            {"t0": 2000, "t1": 3000, "act": "wandering", "dir": "south", "dist": 5},  # walk to market
            {"t0": 3000, "t1": 5000, "act": "wandering", "dir": "east", "dist": 8},  # market stall area
            {"t0": 5000, "t1": 6000, "act": "eating", "dir": None, "dist": 0},
            {"t0": 6000, "t1": 10000, "act": "wandering", "dir": "west", "dist": 10}, # market to warehouse
            {"t0": 10000, "t1": 11000, "act": "eating", "dir": None, "dist": 0},
            {"t0": 11000, "t1": 12000, "act": "wandering", "dir": "north", "dist": 6},
            {"t0": 12000, "t1": 24000, "act": "sleeping", "dir": None, "dist": 0},
        ],
        sect_tasks=[
            {
                "id": "supply_run",
                "description": "The merchant rubs his hands: 'I need wheat and coal for a trade caravan departing tomorrow. The northern villages owe tribute but are late. Help me fill the gap and I will pay you in emeralds.'",
                "requires": [{"item": "minecraft:wheat", "count": 24}, {"item": "minecraft:coal", "count": 8}],
                "reward_item": "minecraft:emerald", "reward_count": 8
            },
            {
                "id": "special_order",
                "description": "The merchant leans closer: 'A cultivator from out of town requested iron and gold. I am short on both. Bring me the materials and I will give you a cut of the profit — and information about who the buyer is.'",
                "requires": [{"item": "minecraft:iron_ingot", "count": 8}, {"item": "minecraft:gold_ingot", "count": 4}],
                "reward_item": "minecraft:emerald", "reward_count": 10
            },
        ],
    ),

    # 3. Teng Family Servant — governor mansion
    make_npc(
        npc_id="npc_teng_servant",
        name="Teng Family Servant",
        nameCn="藤家仆人",
        npc_type="servant",
        faction="teng_family",
        location="teng_family_city",
        cultivation="mortal",
        personality="obedient, fearful, secretly observant",
        speech="subservient, hushed",
        canon_note="Teng Huayuan's governor mansion would have servants maintaining the household. In RI, servants often overhear secrets and provide subtle information to the player about the family's activities.",
        derivation_type="I",
        initiation_lines=[
            "You... you wish to speak with me? I am merely a servant of the Teng household. I should not dawdle.",
            "The patriarch has been meeting with someone in the cultivation quarter. Late at night. I am not supposed to notice such things.",
            "If you need food, the warehouse has bread. But do not take too much — the quartermaster counts everything.",
            "Young master Teng Li has been in a foul temper lately. Someone bested him, I think. He trains twice as hard now.",
        ],
        daily_schedule=[
            {"t0": 0, "t1": 1000, "act": "sleeping", "dir": None, "dist": 0},
            {"t0": 1000, "t1": 3000, "act": "wandering", "dir": "north", "dist": 10},  # mansion to warehouse
            {"t0": 3000, "t1": 4000, "act": "eating", "dir": None, "dist": 0},
            {"t0": 4000, "t1": 7000, "act": "wandering", "dir": "east", "dist": 8},   # mansion grounds
            {"t0": 7000, "t1": 8000, "act": "eating", "dir": None, "dist": 0},
            {"t0": 8000, "t1": 11000, "act": "wandering", "dir": "south", "dist": 12}, # collect supplies
            {"t0": 11000, "t1": 24000, "act": "sleeping", "dir": None, "dist": 0},
        ],
        sect_tasks=[
            {
                "id": "mansion_supplies",
                "description": "The servant bows: 'The household needs bread and wheat for the patriarch's evening meal. The warehouse quartermaster is busy — could you bring the supplies directly to me? I will compensate you.'",
                "requires": [{"item": "minecraft:bread", "count": 8}, {"item": "minecraft:wheat", "count": 16}],
                "reward_item": "minecraft:emerald", "reward_count": 3
            },
            {
                "id": "cleaning_supplies",
                "description": "The servant whispers: 'I need glass and sticks for repairs in the mansion. A certain... incident occurred in the west wing. The less said, the better. Bring me the materials.'",
                "requires": [{"item": "minecraft:glass", "count": 8}, {"item": "minecraft:stick", "count": 16}],
                "reward_item": "minecraft:emerald", "reward_count": 4
            },
        ],
    ),

    # 4. Mortal Beggar — mortal quarter, hints at oppression
    make_npc(
        npc_id="npc_teng_beggar",
        name="Old Wei the Beggar",
        nameCn="老乞丐魏",
        npc_type="mortal",
        faction="none",
        location="teng_family_city",
        cultivation="none",
        personality="broken, bitter, surprisingly observant",
        speech="rambling, cryptic",
        canon_note="The Teng family oppresses the mortal population. The mortal quarter would have those displaced by Teng policies. A beggar provides the player with ground-level perspective on the family's cruelty, and can hint at secrets (smuggler tunnels, hidden wealth).",
        derivation_type="I",
        initiation_lines=[
            "Spare a coin? No? Then spare a moment. The Teng family took everything from my village. Same as yours, I wager.",
            "The temple district... they say the ancestors disapprove of what the current generation does. But what do ancestors know about gold and iron?",
            "I know every crack in this city. Every tunnel. Every hidden door. But knowledge is all I have left.",
            "You carry a sword. The guards do not like armed strangers. Be careful in the residential district — that is where the family sleeps.",
        ],
        daily_schedule=[
            {"t0": 0, "t1": 3000, "act": "sleeping", "dir": None, "dist": 0},
            {"t0": 3000, "t1": 6000, "act": "wandering", "dir": "north", "dist": 6},   # beg in market
            {"t0": 6000, "t1": 7000, "act": "eating", "dir": None, "dist": 0},
            {"t0": 7000, "t1": 10000, "act": "wandering", "dir": "east", "dist": 8},  # beg near temple
            {"t0": 10000, "t1": 11000, "act": "eating", "dir": None, "dist": 0},
            {"t0": 11000, "t1": 13000, "act": "wandering", "dir": "south", "dist": 5}, # return to mortal quarter
            {"t0": 13000, "t1": 24000, "act": "sleeping", "dir": None, "dist": 0},
        ],
        sect_tasks=[
            {
                "id": "beggar_food",
                "description": "Old Wei's sunken eyes brighten: 'You... you would help me? Bring bread. Just bread. And I will tell you something the guards do not want you to know — where the family hides their real wealth.'",
                "requires": [{"item": "minecraft:bread", "count": 8}],
                "reward_item": "minecraft:emerald", "reward_count": 2
            },
            {
                "id": "beggar_coal",
                "description": "Old Wei shivers: 'The nights are cold in the mortal quarter. Coal for a fire. Please. In return... I overheard the guards talking about a tunnel entrance near the warehouse. The smugglers move goods at midnight.'",
                "requires": [{"item": "minecraft:coal", "count": 8}],
                "reward_item": "minecraft:emerald", "reward_count": 3
            },
        ],
    ),

    # 5. Teng Family Cultivator — cultivator quarter
    make_npc(
        npc_id="npc_teng_cultivator_guard",
        name="Teng Family Cultivator",
        nameCn="藤家修士",
        npc_type="enforcer",
        faction="teng_family",
        location="teng_family_city",
        cultivation="early Core Formation",
        personality="arrogant, condescending, paranoid",
        speech="dismissive, threatening",
        canon_note="The Teng family employs cultivators as enforcers. Teng Huayuan is Half-Step Deity Transformation, so he would have subordinates at Core Formation and below. The cultivator quarter structure implies multiple cultivators reside here.",
        derivation_type="I",
        initiation_lines=[
            "This is the cultivator quarter. Mortals and weak cultivators have no business here. Move along.",
            "The patriarch grants us cultivation resources from the family treasury. You would do well not to covet what you cannot protect.",
            "I am refining my Core Formation. One more breakthrough and I will be sent on a mission to the outer territories. The villages there have been... uncooperative.",
            "The spirit stones in this city are not for outsiders. If I catch you near the treasury, I will not bother with warnings.",
        ],
        daily_schedule=[
            {"t0": 0, "t1": 1000, "act": "sleeping", "dir": None, "dist": 0},
            {"t0": 1000, "t1": 4000, "act": "wandering", "dir": "north", "dist": 15},  # cultivate near mansion
            {"t0": 4000, "t1": 5000, "act": "eating", "dir": None, "dist": 0},
            {"t0": 5000, "t1": 8000, "act": "wandering", "dir": "east", "dist": 10},   # patrol
            {"t0": 8000, "t1": 9000, "act": "eating", "dir": None, "dist": 0},
            {"t0": 9000, "t1": 12000, "act": "wandering", "dir": "west", "dist": 12},  # cultivate
            {"t0": 12000, "t1": 24000, "act": "sleeping", "dir": None, "dist": 0},
        ],
        sect_tasks=[
            {
                "id": "cultivation_materials",
                "description": "The cultivator scowls: 'My breakthrough requires spirit stones and iron. The family supply is late. Bring me what I need and I will reward you. Do not make me ask twice.'",
                "requires": [{"item": "minecraft:iron_ingot", "count": 12}, {"item": "minecraft:emerald", "count": 4}],
                "reward_item": "minecraft:gold_ingot", "reward_count": 5
            },
            {
                "id": "enforcer_weapons",
                "description": "The cultivator looks you over: 'The enforcer squad needs new arrows and coal for signal fires. We are expecting trouble from the western villages. Bring the supplies.'",
                "requires": [{"item": "minecraft:arrow", "count": 24}, {"item": "minecraft:coal", "count": 12}],
                "reward_item": "minecraft:emerald", "reward_count": 6
            },
        ],
    ),

    # 6. Dock Foreman — port docks
    make_npc(
        npc_id="npc_teng_dock_foreman",
        name="Dock Foreman Chen",
        nameCn="码头管事陈",
        npc_type="laborer",
        faction="teng_family",
        location="teng_family_city",
        cultivation="mortal",
        personality="gruff, overworked, honest",
        speech="direct, no-nonsense",
        canon_note="Teng Family City has a port/docks structure. The Teng family controls trade routes. A dock foreman manages shipments of tribute, goods, and contraband. This connects the player to the trade economy and potentially the smuggler tunnels.",
        derivation_type="I",
        initiation_lines=[
            "Another pair of hands? Good. The next shipment arrives at dawn and the warehouse is already overflowing. Move crates if you want to eat.",
            "The Teng family trades in everything — wheat, iron, wood, stone. What they do not say is that some crates are heavier than they should be.",
            "The merchant in the market district owes me a favor. If you work here, I will put in a good word. Better prices.",
            "Stay away from the lower dock after dark. That is all I will say about that.",
        ],
        daily_schedule=[
            {"t0": 0, "t1": 1000, "act": "sleeping", "dir": None, "dist": 0},
            {"t0": 1000, "t1": 3000, "act": "wandering", "dir": "south", "dist": 10},  # docks
            {"t0": 3000, "t1": 4000, "act": "eating", "dir": None, "dist": 0},
            {"t0": 4000, "t1": 8000, "act": "wandering", "dir": "east", "dist": 12},   # docks to warehouse
            {"t0": 8000, "t1": 9000, "act": "eating", "dir": None, "dist": 0},
            {"t0": 9000, "t1": 11000, "act": "wandering", "dir": "north", "dist": 8},  # return
            {"t0": 11000, "t1": 24000, "act": "sleeping", "dir": None, "dist": 0},
        ],
        sect_tasks=[
            {
                "id": "crate_transport",
                "description": "The foreman points to a stack of crates: 'These need to go to the warehouse district. But I need planks and sticks to repair the broken pallets first. Bring me the materials.'",
                "requires": [{"item": "minecraft:planks", "count": 16}, {"item": "minecraft:stick", "count": 24}],
                "reward_item": "minecraft:emerald", "reward_count": 5
            },
            {
                "id": "shipment_loading",
                "description": "The foreman wipes his brow: 'A barge needs loading. Iron and coal for the northern trade route. Heavy work, good pay. Bring what you can carry.'",
                "requires": [{"item": "minecraft:iron_ingot", "count": 6}, {"item": "minecraft:coal", "count": 16}],
                "reward_item": "minecraft:emerald", "reward_count": 7
            },
        ],
    ),

    # 7. Tavern Keeper — tavern district, rumor source
    make_npc(
        npc_id="npc_teng_tavern_keeper",
        name="Tavern Keeper Mei",
        nameCn="酒馆老板娘梅",
        npc_type="commoner",
        faction="none",
        location="teng_family_city",
        cultivation="mortal",
        personality="warm, gossipy, perceptive",
        speech="friendly, knowing",
        canon_note="Every city has a tavern where information flows. In RI, taverns are where Wang Lin gathers intelligence. The tavern district structure demands a keeper who hears everything and shares carefully — a key NPC for the player to learn about the Teng family's activities.",
        derivation_type="I",
        initiation_lines=[
            "Sit down, sit down! You look like you have traveled far. The first drink is on the house — consider it an investment. Travelers always bring the best stories.",
            "Guards come in here every night. They drink too much and talk too much. Last week one mentioned something about 'the underground operation.' He clammed up when he saw me listening.",
            "A young woman comes in sometimes — Teng family, but different from the others. She asks about the mortal quarter. Worried about someone, maybe.",
            "The merchant across the way? He is not as honest as he pretends. I have seen him meeting with men who do not look like traders.",
        ],
        daily_schedule=[
            {"t0": 0, "t1": 2000, "act": "sleeping", "dir": None, "dist": 0},
            {"t0": 2000, "t1": 3000, "act": "wandering", "dir": "east", "dist": 5},   # prep tavern
            {"t0": 3000, "t1": 8000, "act": "wandering", "dir": "south", "dist": 8},   # serve customers
            {"t0": 8000, "t1": 9000, "act": "eating", "dir": None, "dist": 0},
            {"t0": 9000, "t1": 12000, "act": "wandering", "dir": "west", "dist": 10},  # late crowd
            {"t0": 12000, "t1": 24000, "act": "sleeping", "dir": None, "dist": 0},
        ],
        sect_tasks=[
            {
                "id": "tavern_supplies",
                "description": "Tavern Keeper Mei smiles: 'Running low on wheat for bread and coal for the hearth. If you keep the tavern warm and fed, the customers stay longer — and they talk more. Bring me supplies and I will share what I have heard.'",
                "requires": [{"item": "minecraft:wheat", "count": 16}, {"item": "minecraft:coal", "count": 8}],
                "reward_item": "minecraft:emerald", "reward_count": 4
            },
            {
                "id": "information_trade",
                "description": "Mei leans in close: 'I know things. Guard rotations, merchant schedules, who is meeting whom. But information has a price — bring me paper and ink_sac so I can write it down safely. What I know is worth far more than what it costs.'",
                "requires": [{"item": "minecraft:paper", "count": 4}, {"item": "minecraft:ink_sac", "count": 2}],
                "reward_item": "minecraft:emerald", "reward_count": 8
            },
        ],
    ),

    # 8. Temple Priest — temple district, ancestor worship
    make_npc(
        npc_id="npc_teng_temple_priest",
        name="Temple Priest Liu",
        nameCn="庙祝刘",
        npc_type="priest",
        faction="none",
        location="teng_family_city",
        cultivation="mortal",
        personality="pious, haunted, quietly defiant",
        speech="measured, philosophical",
        canon_note="The Teng family has a temple district for ancestor worship. In Chinese xianxia culture, temples serve as spiritual centers where the community honors ancestors. A priest provides the player with lore about the Teng family's history and foreshadows their downfall.",
        derivation_type="I",
        initiation_lines=[
            "You come to the temple? Most visitors seek fortune, not wisdom. But you are welcome regardless. The ancestors judge intent, not cultivation.",
            "The Teng family built this temple three generations ago, when they first rose to power. The founder was... a different man from the current patriarch.",
            "I have read the ancestral tablets. There are warnings inscribed on the oldest ones. Warnings about arrogance and the heavens. The current family does not read them.",
            "A young cultivator visited last month. He asked about the restrictions beneath the city. I told him nothing — some knowledge is too dangerous for the living.",
        ],
        daily_schedule=[
            {"t0": 0, "t1": 2000, "act": "sleeping", "dir": None, "dist": 0},
            {"t0": 2000, "t1": 5000, "act": "wandering", "dir": "north", "dist": 6},   # temple duties
            {"t0": 5000, "t1": 6000, "act": "eating", "dir": None, "dist": 0},
            {"t0": 6000, "t1": 9000, "act": "wandering", "dir": "west", "dist": 8},    # tend temple grounds
            {"t0": 9000, "t1": 10000, "act": "eating", "dir": None, "dist": 0},
            {"t0": 10000, "t1": 12000, "act": "wandering", "dir": "south", "dist": 10}, # evening prayers
            {"t0": 12000, "t1": 24000, "act": "sleeping", "dir": None, "dist": 0},
        ],
        sect_tasks=[
            {
                "id": "temple_repair",
                "description": "Priest Liu sighs: 'The temple needs repairs. The family has neglected it for years. Bring me planks and glass to fix the ancestral shrine. The ancestors deserve better than a crumbling roof.'",
                "requires": [{"item": "minecraft:planks", "count": 12}, {"item": "minecraft:glass", "count": 4}],
                "reward_item": "minecraft:emerald", "reward_count": 3
            },
            {
                "id": "incense_materials",
                "description": "Priest Liu nods: 'I need coal for the incense braziers and wheat for the offering bread. The ancestors must not be forgotten — even if the Teng family has forgotten their own teachings.'",
                "requires": [{"item": "minecraft:coal", "count": 8}, {"item": "minecraft:wheat", "count": 8}],
                "reward_item": "minecraft:book", "reward_count": 2
            },
        ],
    ),

    # 9. Smuggler — smuggler tunnels, stealth gameplay
    make_npc(
        npc_id="npc_teng_smuggler",
        name="One-Eared Zhou",
        nameCn="独耳周",
        npc_type="rogue",
        faction="none",
        location="teng_family_city",
        cultivation="late Qi Condensation",
        personality="cunning, paranoid, mercenary",
        speech="whispered, transactional",
        canon_note="The Teng family's smuggler tunnels imply an underground economy. A smuggler NPC provides the player with access to illicit goods, information about the Teng family's corruption, and potentially illegal cultivation resources. In RI, the Teng family's hidden dealings are central to their villainy.",
        derivation_type="I",
        initiation_lines=[
            "You found the tunnels. Impressive. Or stupid. Time will tell which. What do you want? I deal in goods and information — both have a price.",
            "The Teng family pretends these tunnels do not exist. But half their wealth flows through here. Tax-free. Untraceable. The patriarch is not as noble as he claims.",
            "I have items you will not find in the market district. Rare things. Dangerous things. But I need materials first — gold and iron move fast down here.",
            "Stay off the main tunnel after midnight. That is when the family's cultivators make their... personal deliveries. You do not want to see what they carry.",
        ],
        daily_schedule=[
            {"t0": 0, "t1": 4000, "act": "sleeping", "dir": None, "dist": 0},
            {"t0": 4000, "t1": 6000, "act": "wandering", "dir": "north", "dist": 8},   # check tunnels
            {"t0": 6000, "t1": 7000, "act": "eating", "dir": None, "dist": 0},
            {"t0": 7000, "t1": 10000, "act": "wandering", "dir": "east", "dist": 10},  # smuggling route
            {"t0": 10000, "t1": 11000, "act": "eating", "dir": None, "dist": 0},
            {"t0": 11000, "t1": 14000, "act": "wandering", "dir": "south", "dist": 6}, # late operations
            {"t0": 14000, "t1": 24000, "act": "sleeping", "dir": None, "dist": 0},
        ],
        sect_tasks=[
            {
                "id": "smuggler_goods",
                "description": "One-Eared Zhou grins: 'I have a buyer who wants gold and emeralds. Bring me the goods and I will give you a cut. And maybe... a map. A map of places the Teng family does not want found.'",
                "requires": [{"item": "minecraft:gold_ingot", "count": 4}, {"item": "minecraft:emerald", "count": 6}],
                "reward_item": "minecraft:emerald", "reward_count": 12
            },
            {
                "id": "tunnel_expansion",
                "description": "Zhou narrows his one good eye: 'We are extending the tunnel toward the warehouse district. Need pickaxes — iron and stone. Discreetly, of course. The guards cannot know.'",
                "requires": [{"item": "minecraft:iron_ingot", "count": 8}, {"item": "minecraft:cobblestone", "count": 32}],
                "reward_item": "minecraft:emerald", "reward_count": 8
            },
        ],
    ),
]

def main():
    os.makedirs(NPC_DIR, exist_ok=True)
    written = 0
    for npc in NPCS:
        path = os.path.join(NPC_DIR, f"{npc['npc_id']}.json")
        with open(path, "w", encoding="utf-8") as f:
            json.dump(npc, f, indent=2, ensure_ascii=False)
        written += 1
        print(f"  Wrote: {npc['npc_id']} ({npc['name']}) — {len(npc['initiation_lines'])} lines, {len(npc['daily_schedule'])} schedule, {len(npc['sect_tasks'])} tasks")
    print(f"\nTotal: {written} INFERRED NPCs added to Teng Family City")
    print(f"City now has 3 canon + {written} INFERRED = {3+written} NPCs")

if __name__ == "__main__":
    main()