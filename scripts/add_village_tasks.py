#!/usr/bin/env python3
"""
AUTO-CANON-018: Add village tasks to Wang Family Village NPCs.
Reuses existing NpcSectMissionGoal (Article XXVI) — zero Java changes.
Adds sect_tasks to 4 NPCs using vanilla Minecraft items (mortal economy).
"""
import json, os

NPC_DIR = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                    "src", "main", "resources", "data", "ergenverse", "npcs")

# Village tasks — canon-faithful, mortal items only
# Format matches NpcSectMissionGoal expectations exactly
VILLAGE_TASKS = {
    "npc_wang_tianshui": [
        {
            "id": "carpentry_wood",
            "description": "Wang Tianshui sets down his tools: 'I need oak planks for the governor's commission. Can you bring me some? The family could use the extra copper.'",
            "requires": [{"item": "minecraft:oak_planks", "count": 16}],
            "reward_item": "minecraft:copper_ingot",
            "reward_count": 3
        },
        {
            "id": "carpentry_sticks",
            "description": "Wang Tianshui wipes sawdust from his brow: 'If you can gather sticks for the framework, I will share some of my iron supply. We are running low.'",
            "requires": [{"item": "minecraft:stick", "count": 32}],
            "reward_item": "minecraft:iron_ingot",
            "reward_count": 1
        },
    ],
    "npc_zhou_tingsu": [
        {
            "id": "herb_gathering",
            "description": "Zhou Tingsu clasps her hands worriedly: 'The healer in the market needs wheat and carrots for a poultice. My boy is coughing again. Please, can you help?'",
            "requires": [{"item": "minecraft:wheat", "count": 8}, {"item": "minecraft:carrot", "count": 4}],
            "reward_item": "minecraft:emerald",
            "reward_count": 2
        },
        {
            "id": "mending_supplies",
            "description": "Zhou Tingsu holds up a torn coat: 'Winter is coming and the children need warm clothes. If you could bring string and leather, I can mend everything.'",
            "requires": [{"item": "minecraft:string", "count": 8}, {"item": "minecraft:leather", "count": 2}],
            "reward_item": "minecraft:bread",
            "reward_count": 8
        },
    ],
    "npc_da_niu": [
        {
            "id": "fishing_trip",
            "description": "Da Niu grins broadly: 'Eh heh! I want to go fishing at the river, but the last time I went alone, a wild beast chased me back! Come with me — bring coal for a campfire and I will share whatever we catch!'",
            "requires": [{"item": "minecraft:coal", "count": 4}],
            "reward_item": "minecraft:cooked_cod",
            "reward_count": 5
        },
        {
            "id": "log_carrying",
            "description": "Da Niu flexes his thin arms: 'My grandfather says a real man can carry his own weight in logs! Help me bring oak logs to the carpenter and he might give us something good.'",
            "requires": [{"item": "minecraft:oak_log", "count": 8}],
            "reward_item": "minecraft:wooden_axe",
            "reward_count": 1
        },
    ],
    "npc_zhou_rui": [
        {
            "id": "harvest_help",
            "description": "Zhou Rui sighs: 'The Teng family took half our wheat this season. If you can spare some from your travels, I will trade emeralds for it. We need to rebuild our stock before winter.'",
            "requires": [{"item": "minecraft:wheat", "count": 16}],
            "reward_item": "minecraft:emerald",
            "reward_count": 3
        },
        {
            "id": "market_run",
            "description": "Zhou Rui adjusts his merchant's apron: 'I need glass bottles for the healer's medicine trade, and arrows for the village watch. Bring me both and I will pay well.'",
            "requires": [{"item": "minecraft:glass_bottle", "count": 4}, {"item": "minecraft:arrow", "count": 8}],
            "reward_item": "minecraft:emerald",
            "reward_count": 5
        },
    ],
}

if __name__ == "__main__":
    print("=== AUTO-CANON-018: Village Tasks ===\n")
    updated = 0
    for npc_id, tasks in VILLAGE_TASKS.items():
        path = os.path.join(NPC_DIR, f"{npc_id}.json")
        if not os.path.exists(path):
            print(f"  SKIP: {npc_id}.json not found")
            continue

        with open(path, 'r') as f:
            data = json.load(f)

        if "sect_tasks" in data:
            print(f"  SKIP: {npc_id} already has sect_tasks")
            continue

        data["sect_tasks"] = tasks

        with open(path, 'w') as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
            f.write("\n")

        print(f"  ADD: {npc_id} — {len(tasks)} tasks")
        for t in tasks:
            reqs = ", ".join(f"{r['count']}x {r['item'].split(':')[-1]}" for r in t["requires"])
            print(f"    - {t['id']}: requires [{reqs}] -> {t['reward_count']}x {t['reward_item'].split(':')[-1]}")
        updated += 1

    print(f"\n=== DONE: {updated} NPCs updated with {sum(len(v) for v in VILLAGE_TASKS.values())} total tasks ===")