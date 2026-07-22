#!/usr/bin/env python3
"""Add daily_schedule arrays to Heng Yue Sect NPC JSON files.

MC time: 0=sunrise(6am) 6000=noon 12000=sunset 18000=midnight 24000=next sunrise
"""
import json, os, glob, sys

NPC_DIR = "/home/z/my-project/forge-mod/src/main/resources/data/ergenverse/npcs/"

S = {
    "inner_disciple": [
        {"t0":0,"t1":2000,"act":"cultivating","dir":"north","dist":20},
        {"t0":2000,"t1":3000,"act":"eating","dir":None,"dist":0},
        {"t0":3000,"t1":6000,"act":"patrolling","dir":"east","dist":30},
        {"t0":6000,"t1":8000,"act":"cultivating","dir":"north","dist":20},
        {"t0":8000,"t1":10000,"act":"cultivating","dir":"north","dist":20},
        {"t0":10000,"t1":12000,"act":"eating","dir":None,"dist":0},
        {"t0":12000,"t1":13000,"act":"wandering","dir":None,"dist":0},
        {"t0":13000,"t1":14000,"act":"cultivating","dir":"north","dist":20},
        {"t0":14000,"t1":23000,"act":"sleeping","dir":"south","dist":10},
        {"t0":23000,"t1":24000,"act":"patrolling","dir":"east","dist":15},
    ],
    "studious_scholar": [
        {"t0":0,"t1":2000,"act":"cultivating","dir":"north","dist":20},
        {"t0":2000,"t1":3000,"act":"eating","dir":None,"dist":0},
        {"t0":3000,"t1":6000,"act":"studying","dir":"east","dist":15},
        {"t0":6000,"t1":8000,"act":"studying","dir":"east","dist":15},
        {"t0":8000,"t1":10000,"act":"cultivating","dir":"north","dist":20},
        {"t0":10000,"t1":12000,"act":"eating","dir":None,"dist":0},
        {"t0":12000,"t1":14000,"act":"cultivating","dir":"north","dist":20},
        {"t0":14000,"t1":23000,"act":"studying","dir":"east","dist":15},
        {"t0":23000,"t1":24000,"act":"cultivating","dir":"north","dist":20},
    ],
    "bully_patroller": [
        {"t0":0,"t1":2000,"act":"cultivating","dir":"north","dist":20},
        {"t0":2000,"t1":3000,"act":"eating","dir":None,"dist":0},
        {"t0":3000,"t1":6000,"act":"patrolling","dir":"east","dist":30},
        {"t0":6000,"t1":8000,"act":"patrolling","dir":"south","dist":30},
        {"t0":8000,"t1":10000,"act":"sparring","dir":"east","dist":30},
        {"t0":10000,"t1":12000,"act":"eating","dir":None,"dist":0},
        {"t0":12000,"t1":13000,"act":"wandering","dir":None,"dist":0},
        {"0":12000,"1":14000,"act":"cultivating","dir":"north","dist":20},
        {"0":13000,"1":14000,"act":"sleeping","dir":"south","dist":10},
        {"0":23000,"1":24000,"act":"patrolling","dir":"east","dist":15},
    ],
    "night_guard": [
        {"t0":0,"t1":2000,"act":"sleeping","dir":None,"dist":0},
        {"t0":2000,"t1":12000,"act":"wandering","dir":None,"dist":0},
        {"t0":12000,"t1":13000,"act":"eating","dir":None,"dist":0},
        {"t0":13000,"t1":14000,"act":"cultivating","dir":"north","dist":20},
        {"t0":14000,"t1":23000,"act":"sleeping","dir":None,"dist":0},
        {"t0":23000,"1":24000,"act":"patrolling","dir":"north","dist":20},
    ],
}

NPC_SCHEDULES = {
    "npc_wang_zhuo": "inner_disciple",
    "npc_zhang_hu": "outer_disciple_trainee",
    "npc_qiu_siping": "studious_scholar",
    "npc_sun_dazhu": "bully_patroller",
    "npc_wang_hao": "outer_disciple_trainee",
    "npc_zhou_lin": "inner_disciple",
    "npc_sun_zhenwei": "inner_disciple",
    "npc_yun_fei": "inner_disciple",
    "npc_ye_zi": "outer_disciple_trainee",
    "npc_chen_bailiang": "outer_disciple_trainee",
    "npc_zhang_tian": "night_guard",
}

modified = 0
for npc_id, schedule_key in NPC_SCHEDULES.items():
    path = os.path.join(NPC_DIR, f"{npc_id}.json")
    if not os.path.exists(path):
        continue
    with open(path, 'r') as f:
        data = json.load(f)
    if "daily_schedule" in data:
        continue
    schedule = SCHEDULES.get(schedule_key)
    if schedule is None:
        continue
    data["daily_schedule"] = schedule
    with open(path, 'w') as f:
        json.dump(data, f, indent=2)
    modified += 1

print(f"  Scheduled {modified} Heng Yue NPCs")