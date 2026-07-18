#!/usr/bin/env python3
"""
Build ri_canon_factions_enriched.json by deep-extracting faction/sect data
from CANON_RI_CIVILIZATION.md (Layers 4 of the RI World Bible).

Output schema (per faction):
  id, name, nameCn, type, alignment, headquarters, peakRealm,
  canonConfidence, leader (current + previous), alliedFactions,
  enemyFactions, knownMembers, sectStructure, chapterReferences

Each faction tagged with BridgingPolicy confidence tier (A/B/C/D/F).
"""
import json
import os
from pathlib import Path

OUT_PATH = Path("/home/z/my-project/forge-mod/ri_canon_factions_enriched.json")

# ----- Faction data: 18 fully-detailed (CIV-01..CIV-18) + 27 briefs (CIV-brief-01..27) -----

FACTIONS = [
    # ===== Part 2 — Fully Detailed Factions (18) =====
    {
        "id": "CIV-01",
        "name": "Heng Yue Sect",
        "nameCn": "恒岳派",
        "fid": "F01",
        "type": "sect",
        "subtype": "orthodox_cultivation_sect",
        "alignment": "righteous",
        "headquarters": "Country of Zhao, Planet Suzaku",
        "peakRealm": "Soul Formation (Huang Long Zhenren's secret identity)",
        "canonConfidence": 5,
        "confidenceTier": "A",
        "leader": {
            "current": "Huang Long Zhenren (黄龙真人) — secretly 5th-Gen Vermilion Bird Divine Emperor Lu Yun (陆云)",
            "previous": None,
            "fate": "Died after returning from Cultivation Alliance HQ"
        },
        "alliedFactions": ["Vermilion Bird Country (vassal/hierarchy)", "Xuan Dao Sect (neutral neighbor)"],
        "enemyFactions": ["Teng Clan (hostile; Teng Huayuan exterminated Wang Family Village)"],
        "knownMembers": [
            "Huang Long Zhenren / Lu Yun (patriarch, secretly 5th-Gen Vermilion Bird Divine Emperor)",
            "Wang Hao (core disciple, spared during Teng Clan massacre)",
            "Wang Zhuo (core disciple, spared during Teng Clan massacre)",
            "Wang Lin (legacy disciple, failed all 3 entrance tests)",
            "Wang Zhou (long-braided hair outer disciple, failed all 3 tests)",
            "Wang Jie (failed all 3 entrance tests)",
            "Zhang Hu (early friend of Wang Lin; later became bandit)",
            "Sun Dazhu (early antagonist; Zhang Hu fell in with him)"
        ],
        "sectStructure": {
            "patriarchCount": 1,
            "eldersCount": "2-4 (unnamed, Foundation Establishment to Core Formation)",
            "coreDisciples": "5-15 (Foundation Establishment)",
            "innerDisciples": "20-50 (late Qi Condensation)",
            "outerDisciples": "50-200 (early-to-mid Qi Condensation)",
            "mortalServants": "30-100 (logistics, herb gathering)",
            "internalDivisions": "No explicit internal divisions. SECRET structure: patriarch is actually the 5th-Gen Vermilion Bird in disguise.",
            "spiritVeins": "1-3 minor veins (Zhao Country is low-spirit-energy)"
        },
        "chapterReferences": [
            "CANON_RI_CIVILIZATION.md CIV-01 (lines 77-141)",
            "CANON_RI_COMPLETE_WORLD.md L253-256, L367-374, L2301, L2712",
            "Timeline E11 (Wang Lin's entrance test)"
        ],
        "canonEvents": [
            "Wang Lin's entrance test - failed all 3 tests",
            "Wang Lin becomes legacy disciple",
            "Teng Clan exterminates Wang Family Village - Heng Yue does not intervene (too weak)",
            "Wang Lin returns as Soul Formation to bury the dead",
            "Lu Yun dies after returning from Cultivation Alliance - Heng Yue loses its secret master"
        ]
    },
    {
        "id": "CIV-02",
        "name": "Soul Refining Sect",
        "nameCn": "炼魂宗",
        "fid": "F03",
        "type": "sect",
        "subtype": "demonic_cultivation_sect",
        "alignment": "demonic",
        "headquarters": "Pilu Kingdom, Planet Suzaku",
        "peakRealm": "Nirvana Scryer+ (Dun Tian)",
        "canonConfidence": 5,
        "confidenceTier": "A",
        "leader": {
            "current": "Dun Tian (敦天 / 顿天) — Nirvana Scryer+; self-erased consciousness to become a soul within Ten Billion Soul Banner",
            "previous": None,
            "fate": "Self-erased; becomes a soul in the Ten Billion Soul Banner"
        },
        "alliedFactions": ["Wang Lin (master→inheritor)", "Soul Refining Tribe (CIV-14, heritage successor)"],
        "enemyFactions": ["Neighbor sects (Dun Tian stole celestial jades from them)"],
        "knownMembers": [
            "Dun Tian (敦天) — ancestor/founder, Nirvana Scryer+",
            "Nian Tian (念天) — Dun Tian's senior brother",
            "Ouyang Zi (欧阳子) — mentioned",
            "Wang Lin — inheritor; became 'Ancestor of the Soul Refining Tribe'"
        ],
        "sectStructure": {
            "ancestor": "1 (Dun Tian) — Nirvana Scryer+",
            "seniorBrothersSisters": "1-3 (Nian Tian, Ouyang Zi, possibly others)",
            "directDisciples": "3-8 (soul-refining path requires high talent)",
            "outerPractitioners": "10-30 (assist in soul extraction/sealing operations)",
            "mortalServants": "20-50 (maintain sect grounds in Pilu Kingdom)",
            "guardianTreasure": "Ten Billion Soul Flag / Ten Billion Soul Banner (十亿魂旗)",
            "coreMethod": "Three Parts: Soul Refining (炼魂) → Soul Extracting → Soul Sealing"
        },
        "chapterReferences": [
            "CANON_RI_CIVILIZATION.md CIV-02 (lines 144-216)",
            "CANON_RI_COMPLETE_WORLD.md N23",
            "CANON_RI_CHARACTER_DECISIONS.md CD-16",
            "CANON_RI_COMPLETE_TECHNIQUES.md L869-889, L1458, L1464, L1466"
        ],
        "canonEvents": [
            "Dun Tian's 3 gifts to Wang Lin: (1) clone → Nascent Soul peak, (2) true body → 3-Star Ancient God, (3) Ten Billion Soul Banner + inheritance",
            "Dun Tian self-erases to become soul in the Banner",
            "Wang Lin inherits the sect, promises to elevate to 6th level",
            "Wang Lin's Soul Transformation breakthrough using the Billion Soul Flag",
            "Soul Refining Tribe restores Wang Lin's One Billion Soul Flag"
        ]
    },
    {
        "id": "CIV-03",
        "name": "Corpse Yin Sect",
        "nameCn": "尸阴宗",
        "fid": "F04",
        "type": "sect",
        "subtype": "demonic_cultivation_sect",
        "alignment": "demonic",
        "headquarters": "Planet Suzaku (Sea of Devils region)",
        "peakRealm": "Nascent Soul+ (multiple elders)",
        "canonConfidence": 5,
        "confidenceTier": "A",
        "leader": {
            "current": "Unnamed (likely early Soul Formation or peak Nascent Soul) [Type C]",
            "previous": None,
            "fate": "Multiple members killed by Wang Lin"
        },
        "alliedFactions": [],
        "enemyFactions": ["Wang Lin (brief disciple → enemy)"],
        "knownMembers": [
            "Wu Yu (吴宇) — Nascent Soul elder; only Nascent Soul remained alive",
            "Ye Zi (叶紫) — Nascent Soul elder; gave Wang Lin immortal cave; made him slice soul sliver",
            "Adai (阿呆) — blue-skinned man; Wu Yu's servant; 9 talismans on body",
            "Sun Tai — taken by Wang Lin as servant",
            "Lei Ji (雷记) — taken by Wang Lin as mount"
        ],
        "sectStructure": {
            "patriarch": "1 (unnamed, likely early Soul Formation or peak Nascent Soul) [C]",
            "nascentSoulElders": "3-5 (Wu Yu, Ye Zi, plus 1-3 others) [B]",
            "coreFormationMasters": "10-20 (manage corpse refinement operations) [C]",
            "coreDisciples": "30-50 (learn corpse refinement, Yin arts) [C]",
            "innerDisciples": "50-100 (assist in corpse harvesting) [C]",
            "outerDisciples": "100-300 (basic cultivation, logistics) [C]",
            "servantsSlaves": "50-200 (including blue-skinned servants like Adai) [C]",
            "corpseArmy": "Variable - refined corpses puppeted for combat"
        },
        "chapterReferences": [
            "CANON_RI_CIVILIZATION.md CIV-03 (lines 218-280)",
            "CANON_RI_COMPLETE_WORLD.md L385-392 (Nan Dou City trade hub)",
            "ECOLOGY L735-736 (resentful spirit sites)"
        ],
        "canonEvents": [
            "Adai leads Wang Lin to Wu Yu's Nascent Soul in Forest of Distorted Divine Sense",
            "Wang Lin enters Corpse Yin Sect; Ye Zi gives immortal cave",
            "Wang Lin attacks members, takes back soul sliver",
            "Wang Lin kills many members when they use Wang family for resentful spirits"
        ]
    },
    {
        "id": "CIV-04",
        "name": "Fighting Evil Sect",
        "nameCn": "斗邪宗",
        "fid": "F07",
        "type": "sect",
        "subtype": "demonic_cultivation_sect",
        "alignment": "demonic",
        "headquarters": "Sea of Devils, Planet Suzaku",
        "peakRealm": "Soul Formation (Sect Leader)",
        "canonConfidence": 5,
        "confidenceTier": "A",
        "leader": {
            "current": "Unnamed — Soul Formation; killed by Wang Lin",
            "previous": None,
            "fate": "Killed by Wang Lin; sect taken control of, then released/abandoned"
        },
        "alliedFactions": [],
        "enemyFactions": ["Wang Lin (enemy → conquered)"],
        "knownMembers": [
            "Unnamed Sect Leader — Soul Formation; killed by Wang Lin",
            "10 Core Formation cultivators (core disciples) — all killed by Wang Lin"
        ],
        "sectStructure": {
            "sectLeader": "1 — Soul Formation",
            "elders": "2-4 (Nascent Soul, unnamed) [C]",
            "coreDisciples": "10+ (10 killed by Wang Lin, so at minimum 10) [A]",
            "innerDisciples": "30-80 (Foundation Establishment) [C]",
            "outerDisciples": "100-300 (Qi Condensation) [C]",
            "mortalServants": "50-150 (logistics in Sea of Devils districts) [C]",
            "signatureSystem": "Ten Thousand Devil Hundred Day Kill Order (万魔百日杀令) — paid assassination contracts"
        },
        "chapterReferences": [
            "CANON_RI_CIVILIZATION.md CIV-04 (lines 282-332)",
            "CANON_RI_COMPLETE_WORLD.md L385-392",
            "ECOLOGY L735-736 (massacre site becomes Core Formation-tier resentful spirit site)"
        ],
        "canonEvents": [
            "10 Core Formation cultivators chase Wang Lin → Wang Lin breaks through to Core Formation in his immortal cave",
            "Wang Lin kills the Sect Leader and takes control of the sect",
            "Wang Lin releases/abandons the sect",
            "Fighting Evil Sect massacre site becomes a location with Core Formation-tier resentful spirits"
        ]
    },
    {
        "id": "CIV-05",
        "name": "Vermilion Bird Country",
        "nameCn": "朱雀国",
        "fid": "F41",
        "type": "nation_state",
        "subtype": "cultivation_nation_state",
        "alignment": "righteous",
        "headquarters": "Planet Suzaku",
        "peakRealm": "Vermilion Bird Divine Emperor (Void Flame Cultivator)",
        "canonConfidence": 5,
        "confidenceTier": "A",
        "leader": {
            "current": "Wang Lin (6th-Gen Divine Emperor)",
            "previous": "Lu Yun (5th-Gen); Situ Nan (2nd-Gen); 3rd-Gen betrayer; 13th-Gen Qian Pinghai; 14th-Gen (unnamed); 15th-Gen Zhou Wutai",
            "fate": "Wang Lin eventually returns rein to Azure Dragon Divine Emperor"
        },
        "alliedFactions": ["Four Divine Sect (CIV-06, parent organization)", "Cultivation Alliance (supra-national authority)"],
        "enemyFactions": [],
        "knownMembers": [
            "Wang Lin (6th-Gen Divine Emperor)",
            "Lu Yun / 5th-Gen Vermilion Bird",
            "Situ Nan (2nd-Gen)",
            "3rd-Gen Vermilion Bird (betrayer of Situ Nan with Tan Lang)",
            "Qian Pinghai (13th-Gen)",
            "14th-Gen Vermilion Bird (severed Situ Nan's arm; attacked Soul Refining Sect)",
            "Zhou Wutai (15th-Gen; Wang Lin transferred position to him)",
            "Ye Wuyou (1st-Gen Vermilion Bird Master)",
            "Hong Die (core disciple)"
        ],
        "sectStructure": {
            "model": "Nation-State (NOT sect model)",
            "sovereign": "Vermilion Bird Divine Emperor",
            "highOfficials": "Hong Die (core disciple)",
            "regionalGovernors": "Unnamed - manage the 11 countries of Planet Suzaku [C]",
            "militaryCommanders": "Unnamed [C]",
            "cityAdministrators": "Tian Shui City, Teng Family City, etc.",
            "cultivationPopulation": "Estimated ~100,000 cultivators across all 11 countries",
            "mortalPopulation": "500M-1.5B in Vermilion Bird Country alone",
            "level": "Level 6 (raised by Situ Nan)"
        },
        "chapterReferences": [
            "CANON_RI_CIVILIZATION.md CIV-05 (lines 335-392)",
            "CANON_RI_COMPLETE_WORLD.md L31",
            "ECOLOGY L404-408, L443-445, L490-491"
        ],
        "canonEvents": [
            "Vermilion Bird lineage generational succession (1st through 15th-Gen documented)",
            "Wang Lin ascends as 6th-Gen Divine Emperor",
            "Wang Lin eventually returns rein to Azure Dragon Divine Emperor"
        ]
    },
    {
        "id": "CIV-06",
        "name": "Four Divine Sect / Four Sacred Sect",
        "nameCn": "四神宗",
        "fid": "F10",
        "type": "coalition",
        "subtype": "divine_beast_coalition",
        "alignment": "righteous",
        "headquarters": "Vermilion Bird Starfield",
        "peakRealm": "Peak Third Step+ (multiple Divine Emperors)",
        "canonConfidence": 5,
        "confidenceTier": "A",
        "leader": {
            "current": "Four Divine Emperors (coalition)",
            "previous": None,
            "fate": "Wang Lin eventually returns rein to Azure Dragon Divine Emperor"
        },
        "alliedFactions": ["Vermilion Bird Divine Sect (CIV-07, member sub-organization)"],
        "enemyFactions": [],
        "knownMembers": [
            "Azure Dragon Divine Emperor",
            "Vermilion Bird Divine Emperor (Wang Lin, 6th-Gen)",
            "White Tiger Divine Emperor",
            "Black Tortoise Divine Emperor"
        ],
        "sectStructure": {
            "model": "Coalition of four divine beast sects",
            "divineEmperors": 4,
            "subSectLeaders": "Each divine beast line has its own sect (e.g., Vermilion Bird Divine Sect CIV-07)"
        },
        "chapterReferences": [
            "CANON_RI_CIVILIZATION.md CIV-06 (lines 394-422)"
        ],
        "canonEvents": [
            "Wang Lin eventually returns the rein to Azure Dragon Divine Emperor"
        ]
    },
    {
        "id": "CIV-07",
        "name": "Vermilion Bird Divine Sect",
        "nameCn": "朱雀神宗",
        "fid": "F11",
        "type": "sect",
        "subtype": "divine_beast_cultivation_sect",
        "alignment": "righteous",
        "headquarters": "Vermilion Bird Starfield",
        "peakRealm": "Void Flame Cultivator (Vermilion Bird Divine Emperor)",
        "canonConfidence": 5,
        "confidenceTier": "A",
        "leader": {
            "current": "Wang Lin (6th-Gen Vermilion Bird Divine Emperor)",
            "previous": "Lu Yun (5th-Gen); Situ Nan (2nd-Gen); 3rd-Gen betrayer",
            "fate": "Wang Lin eventually returns rein to Azure Dragon"
        },
        "alliedFactions": ["Four Divine Sect (CIV-06, parent)", "Wang Lin"],
        "enemyFactions": [],
        "knownMembers": [
            "Wang Lin (6th-Gen)",
            "Lu Yun (5th-Gen)",
            "Situ Nan (2nd-Gen)"
        ],
        "sectStructure": {
            "keyTechniques": [
                "Vermilion Bird Burning Heaven Art (朱雀焚天功)",
                "Vermilion Bird Nine Mysterious Transformations (朱雀九玄变) - 9 transformations",
                "Vermilion Bird Mark - 4 awakening tiers: Red → White → Blue → Black → 9-Color → Ethereal",
                "War Spirit Print - upgraded to Dao Spell Ch. 1542",
                "Dao of Power / Rebound Force - Black Tortoise method taught by 2nd-Gen"
            ],
            "divineEmperor": "1 (Void Flame Cultivator)"
        },
        "chapterReferences": [
            "CANON_RI_CIVILIZATION.md CIV-07 (lines 425-451)",
            "CANON_RI_COMPLETE_WORLD.md L1018-1019",
            "CANON_RI_COMPLETE_TECHNIQUES.md L25-32, L1725-1733, L1747, L1784"
        ],
        "canonEvents": [
            "Lu Yun taught Wang Lin the Vermilion Bird Nine Mysterious Transformations",
            "4th Vermilion Bird Mark Awakening = Ethereal Fire (unique to Wang Lin)",
            "War Spirit Print upgraded to Dao Spell (Ch. 1542)"
        ]
    },
    {
        "id": "CIV-08",
        "name": "Heavenly Fate Sect",
        "nameCn": "天运宗",
        "fid": "F05",
        "type": "sect",
        "subtype": "divination_fate_sect",
        "alignment": "neutral (ruled by antagonist All-Seer)",
        "headquarters": "Planet Tian Yun",
        "peakRealm": "Peak Third Step (All-Seer, suppressed by seal to Heaven Blight)",
        "canonConfidence": 5,
        "confidenceTier": "A",
        "leader": {
            "current": "All-Seer (全知者) — peak Third Step, suppressed by seal to Heaven Blight; killed by Wang Lin ~Year 300",
            "previous": None,
            "fate": "Killed by Wang Lin"
        },
        "alliedFactions": [],
        "enemyFactions": ["Wang Lin (recruited as tool → enemy → destroyer)"],
        "knownMembers": [
            "All-Seer (全知者) — leader",
            "Tianyunzi (天运子) — clone of All-Seer AND artifact spirit of Realm-Defining Compass / Heaven Defying Bead",
            "Zhao Xingsha (Soul Transformation, purple division rival)",
            "Sima Rufeng (purple division)",
            "Zhao Xinming (4th sister, purple division)",
            "Chen Tao (6th brother, mid-Ascendant, purple division)",
            "Wang Lin (7th purple disciple)",
            "Bai Wei (purple division)",
            "Yao Xinghai / Blood Ancestor (Red Division leader - separate entry)",
            "7 Division Leaders (one per color: red/orange/yellow/green/blue/cyan/purple)"
        ],
        "sectStructure": {
            "leader": "All-Seer (peak Third Step, suppressed to Heaven Blight)",
            "cloneArtifactSpirit": "Tianyunzi (clone of All-Seer + artifact spirit of Realm-Defining Compass)",
            "colorDivisions": [
                {"name": "1st Division", "color": "Red (红)", "notableMembers": ["Yao Xinghai (Blood Ancestor)"]},
                {"name": "2nd Division", "color": "Orange (橙)"},
                {"name": "3rd Division", "color": "Yellow (黄)"},
                {"name": "4th Division", "color": "Green (绿)"},
                {"name": "5th Division", "color": "Blue (蓝)"},
                {"name": "6th Division", "color": "Cyan (青)"},
                {"name": "7th Division", "color": "Purple (紫)", "notableMembers": ["Wang Lin (7th disciple)", "Zhao Xingsha", "Sima Rufeng", "Zhao Xinming (4th sister)", "Chen Tao (6th brother)", "Bai Wei"]}
            ],
            "spiritVeins": "7 supreme + ~50-100 mid + ~500-1000 minor (one supreme per division + 1 planetary supreme)",
            "divisionLeaders": "7 (1 per color, likely Nirvana Cleanser to Nirvana Scryer) [C]",
            "divisionElders": "14-28 (2-4 per division, Nirvana Shatterer to Nirvana Cleanser) [C]",
            "coreDisciples": "70-140 (10-20 per division, Soul Transformation to Ascendant) [C]",
            "innerDisciples": "700-1400 (100-200 per division) [C]",
            "outerDisciples": "7000-14000 (1000-2000 per division) [C]",
            "mortalServants": "50,000-200,000 [C]"
        },
        "chapterReferences": [
            "CANON_RI_CIVILIZATION.md CIV-08 (lines 454-529)",
            "CANON_RI_COMPLETE_WORLD.md N21, N113",
            "CANON_RI_TIMELINE.md E20-E22 (~Year 100, ~Year 200-300)",
            "WangLinAntagonists.java ALL_SEER",
            "CANON_RI_CHARACTER_DECISIONS.md CD-04, CD-05, CD-30",
            "Ch. 493 (Wang Lin joins as 7th purple disciple)"
        ],
        "canonEvents": [
            "Wang Lin joins as 7th purple disciple (~Year 100, Ch. 493)",
            "All-Seer's birthday banquet - Wang Lin battles Zhao Xingsha, Zhao Xinming, Chen Tao",
            "All-Seer's possession plot against Wang Lin (~Year 200-300)",
            "Wang Lin kills All-Seer (~Year 300)",
            "After All-Seer's death, the 7 divisions persist as sub-faction ruins"
        ]
    },
    {
        "id": "CIV-09",
        "name": "Yao Family",
        "nameCn": "姚家",
        "fid": "F36",
        "type": "clan",
        "subtype": "bloodline_clan",
        "alignment": "antagonist",
        "headquarters": "Southern Domain, Allheaven Star System (also co-resident on Planet Tian Yun)",
        "peakRealm": "Third Step (Blood Ancestor Yao Xinghai)",
        "canonConfidence": 5,
        "confidenceTier": "A",
        "leader": {
            "current": "Yao Xinghai (姚星海) — Third Step Blood Ancestor; killed by Wang Lin",
            "previous": None,
            "fate": "Killed by Wang Lin in Thunder Immortal Realm"
        },
        "alliedFactions": ["Heavenly Fate Sect (CIV-08, co-resident on Tian Yun)"],
        "enemyFactions": ["Wang Lin (enemy → annihilated)"],
        "knownMembers": [
            "Yao Xinghai (姚星海) — Blood Ancestor, Third Step",
            "Yao Xixue (姚冰雪) — daughter; amnesiac; connected to Blood Soul Pill"
        ],
        "sectStructure": {
            "bloodAncestor": "1 (Yao Xinghai) - Third Step",
            "elders": "5-10 (Nirvana tier, unnamed) [C]",
            "coreFamilyMembers": "20-50 (Soul Transformation to Nirvana) [C]",
            "branchFamilyMembers": "100-300 (Nascent Soul to Soul Transformation) [C]",
            "retainersServants": "500-2000 (cultivators + mortals) [C]",
            "signaturePill": "Blood Soul Pill - 'resurrection pill' (N98)"
        },
        "chapterReferences": [
            "CANON_RI_CIVILIZATION.md CIV-09 (lines 532-579)",
            "CANON_RI_COMPLETE_WORLD.md N98, N99",
            "CANON_RI_TIMELINE.md E94",
            "CANON_RI_CHARACTER_DECISIONS.md CD-07, CD-09"
        ],
        "canonEvents": [
            "Yao Family sends kill-order on Wang Lin",
            "Wang Lin destroys multiple planets chasing the Yao Family",
            "Yao Xinghai (Blood Ancestor) killed by Wang Lin",
            "Yao Xixue used Blood Soul Pill to be reborn; sacrificed body to Wind Demon; memories devoured → amnesia",
            "Wang Lin released Blood Ancestor's remnant soul; amnesiac Yao Xixue departed with it"
        ]
    },
    {
        "id": "CIV-10",
        "name": "Ancient Clan",
        "nameCn": "古族",
        "fid": "F40",
        "type": "primordial_race",
        "subtype": "ancient_clan",
        "alignment": "neutral",
        "headquarters": "Immortal Astral Continent (IAC)",
        "peakRealm": "Great Heavenly Venerable (Xuan Luo); Ancient Ancestor above all",
        "canonConfidence": 5,
        "confidenceTier": "A",
        "leader": {
            "current": "Ancient Ancestor (above Great Heavenly Venerable)",
            "previous": None,
            "fate": "Wang Lin eventually becomes #1 in the clan"
        },
        "alliedFactions": [],
        "enemyFactions": ["Celestial Clan (仙族, rival race)"],
        "knownMembers": [
            "Ancient Ancestor (above Great Heavenly Venerable)",
            "Xuan Luo (玄罗) — Dao Gu lineage, Wang Lin's master; Great Heavenly Venerable",
            "Dao Ancient Great Celestial Venerable",
            "Dao Yi Great Celestial Venerable",
            "Sea Child Celestial Venerable",
            "Jiu Di (Grand Empyrean)",
            "Song Tian",
            "Yun Yifeng",
            "Purple Dawn Immortal Emperor",
            "Ye Mo (Ancient God inheritance)",
            "Tu Si (8-star Ancient God, deceased)",
            "Tuo Sen (Tu Si's demonic thought)",
            "Ji Du (Wang Lin's godson, Primordial Ancient lineage)",
            "Wang Lin (Dao Gu lineage; main God + Devil + Demon hybrid)"
        ],
        "sectStructure": {
            "threeLineages": [
                {"name": "Dao Gu (古道)", "starType": "God Star", "specialization": "Physical body cultivation, ancient god power", "keyFigures": ["Xuan Luo (master)", "Wang Lin"]},
                {"name": "Dao Yi (古意)", "starType": "Devil Star", "specialization": "Devil cultivation, destructive power", "keyFigures": ["Dao Yi Great Celestial Venerable"]},
                {"name": "Primordial Ancient", "starType": "Demon Star", "specialization": "Demon cultivation, raw power", "keyFigures": ["Ji Du (Wang Lin's godson)"]}
            ],
            "heritageSites": [
                "Ancient Tomb (Yi Si Puppet, Heaven-Splitting Axe, Eternal Wood Spirit, Fog Devil Lance, Ancient Breath Leaves 99-piece set, Emperor Furnace, Ye Mo's left eye and right arm)",
                "Ancient Clan Ancestral Temple (Lou Hou's soul sealed here; Soul Blood 2 drops)",
                "Five Flowers Eight Gates (Dong Lin Female Ancient God encounter)"
            ],
            "spiritVeins": "440+ on the IAC (7 continents) - richest vein network in the Cave World",
            "mortalPopulation": "1-10 trillion on the IAC - largest Joss Flame base"
        },
        "chapterReferences": [
            "CANON_RI_CIVILIZATION.md CIV-10 (lines 582-634)",
            "CANON_RI_COMPLETE_WORLD.md N28, N117",
            "CANON_RI_CHARACTER_DECISIONS.md CD-13, CD-18, CD-26 (Tuo Sen, Xuan Luo, Lian Daofei)",
            "ECOLOGY L1409, L1418-1425, L1422-1423, L1449, L1458, L1654-1675"
        ],
        "canonEvents": [
            "Xuan Luo refuses Ancient Dao Great Heavenly Venerable's offer",
            "Xuan Luo fought Dao Yi Great Heavenly Venerable over Primordial God Realm fragment at Seven Paths Sect entrance (indirectly causing Cave World's birth)",
            "Xuan Luo entered Cave World to accept Wang Lin as only disciple",
            "Wang Lin eventually defeats both Gu Dao and Ancient Dao to become #1"
        ]
    },
    {
        "id": "CIV-11",
        "name": "Teng Clan",
        "nameCn": "藤族",
        "fid": "F28",
        "type": "clan",
        "subtype": "cultivation_clan",
        "alignment": "antagonist",
        "headquarters": "Teng Family City, Country of Zhao, Planet Suzaku",
        "peakRealm": "Half-Step Deity Transformation (Teng Huayuan, elevated by Piao Nanzi)",
        "canonConfidence": 5,
        "confidenceTier": "A",
        "leader": {
            "current": "Teng Huayuan (藤化元) — Half-Step Deity Transformation (Late Nascent Soul, elevated by Piao Nanzi); killed by Wang Lin",
            "previous": None,
            "fate": "Killed by Wang Lin; soul refined into a demon"
        },
        "alliedFactions": ["Piao Nanzi (patron, sealed demon)", "Vermilion Bird Country (subject clan)"],
        "enemyFactions": ["Wang Clan / Wang Family Village (EXTERMINATED)", "Wang Lin (first major enemy)", "Heng Yue Sect (dominated)"],
        "knownMembers": [
            "Teng Huayuan (藤化元) — patriarch; Half-Step Deity Transformation; killed by Wang Lin",
            "Jimo Elder (吉莫 Elder) — guest elder",
            "Teng One through Teng Nine — Nine Great Nascent Souls; all killed and refined into demon-puppets by Wang Lin",
            "Teng Li (藤离) — great-great-grandson; killed by Wang Lin at Tian Shui City",
            "Teng Xiuxiu",
            "Gao Qiming — diviner (hired tool, not clan member)"
        ],
        "sectStructure": {
            "patriarch": "1 (Teng Huayuan) - Half-Step Deity Transformation",
            "guestElders": "1-3 (Jimo Elder named) [A]",
            "nineGreatNascentSouls": "9 (Teng One through Teng Nine) - elite combat force",
            "coreFormationMasters": "20-50 [C]",
            "foundationEstablishment": "50-150 [C]",
            "qiCondensationMortals": "500-2000 (Teng Family City population: ~50,000-150,000 total)",
            "selectionSystem": "Nine Great Nascent Souls succession system"
        },
        "chapterReferences": [
            "CANON_RI_CIVILIZATION.md CIV-11 (lines 637-693)",
            "CANON_RI_COMPLETE_WORLD.md N83",
            "CANON_RI_TIMELINE.md",
            "CANON_RI_CHARACTER_DECISIONS.md CD-08",
            "WangLinAntagonists.java TENG_HUAYUAN",
            "ECOLOGY L298-299, L310-316, L321-325"
        ],
        "canonEvents": [
            "Teng Huayuan exterminates Wang Family Village",
            "Wang Lin kills Teng Li at Tian Shui City, steals Foundation Establishment",
            "Wang Lin's 'Kill and Destroy the Heart': hunts Teng descendants, builds human-head tower",
            "All 9 Nascent Soul cultivators killed and refined into demon-puppets",
            "Teng Huayuan slain by Wang Lin",
            "Teng Family City becomes a depleted ruin"
        ]
    },
    {
        "id": "CIV-12",
        "name": "Cloud Sky Sect",
        "nameCn": "云天宗",
        "fid": "F02",
        "type": "sect",
        "subtype": "orthodox_cultivation_sect",
        "alignment": "righteous",
        "headquarters": "Chu Country, Planet Suzaku",
        "peakRealm": "Nascent Soul (Li Muwan, Chen Bailiang)",
        "canonConfidence": 5,
        "confidenceTier": "A",
        "leader": {
            "current": "Li Muwan (李慕婉) — Nascent Soul; Wang Lin killed Sun Zhenwei at her wedding; Wang Lin became Sect Master; handed position to Li Muwan",
            "previous": "Wang Lin (briefly, killed Sun Zhenwei and took sect master position)",
            "fate": "Li Muwan dies; soul preserved 700 years; resurrected by Wang Lin"
        },
        "alliedFactions": ["Wang Lin (love interest's sect)"],
        "enemyFactions": [],
        "knownMembers": [
            "Li Muwan (李慕婉) — Elder → Sect Master; Nascent Soul",
            "Chen Bailiang — Elder; killed",
            "Sun Zhenwei — Suitor; killed by Wang Lin at Li Muwan's wedding",
            "Zhou Lin — 9th-gen disciple"
        ],
        "sectStructure": {
            "sectMaster": "Li Muwan - Nascent Soul",
            "elders": "3-6 (Chen Bailiang was one; Nascent Soul or peak Core Formation) [C]",
            "coreDisciples": "10-20 (Foundation Establishment to Core Formation) [C]",
            "innerDisciples": "30-80 (Qi Condensation to Foundation Establishment) [C]",
            "outerDisciples": "50-150 [C]",
            "specialization": "Cloud arts, flight techniques, alchemy (Li Muwan)"
        },
        "chapterReferences": [
            "CANON_RI_CIVILIZATION.md CIV-12 (lines 696-738)",
            "CANON_RI_COMPLETE_WORLD.md N17",
            "CANON_RI_CHARACTER_DECISIONS.md CD-22"
        ],
        "canonEvents": [
            "Wang Lin killed Sun Zhenwei at Li Muwan's wedding",
            "Wang Lin became Sect Master briefly; handed position to Li Muwan",
            "Li Muwan fails Nascent Soul formation multiple times; finally formed it but died",
            "Wang Lin placed her soul in Heaven Defying Bead for 700 years",
            "Finally resurrected by Wang Lin at 4th Step"
        ]
    },
    {
        "id": "CIV-13",
        "name": "Great Soul Sect",
        "nameCn": "大魂宗",
        "fid": "F13",
        "type": "sect",
        "subtype": "soul_cultivation_sect",
        "alignment": "neutral",
        "headquarters": "Heavenly Bull Continent, Immortal Astral Continent",
        "peakRealm": "Third Step+",
        "canonConfidence": 5,
        "confidenceTier": "A",
        "leader": {
            "current": "Unnamed",
            "previous": None,
            "fate": "Active"
        },
        "alliedFactions": ["Wang Lin (becomes elder)"],
        "enemyFactions": [],
        "knownMembers": [
            "Wang Lin — Elder (Wang Lin becomes an elder of the Great Soul Sect)"
        ],
        "sectStructure": {
            "wangLinPosition": "Elder",
            "keyArtifacts": [
                "Ghostly Sail (source of Wang Lin's Restriction Essence bloodlines)",
                "Tianniu Pearl",
                "Fire Element Five Elements Armor"
            ],
            "keyTechniques": [
                "Heavenly Fate Finger - divination; half of Mourning Death Clan's Dao spell",
                "Ancestor-summoning spell",
                "Main spell with 9 levels"
            ],
            "earthFireMainVein": "At Great Soul Sect location; Wang Lin devours it → Fire Essence True Body",
            "fireVeins": "120+ on Heavenly Bull Continent"
        },
        "chapterReferences": [
            "CANON_RI_CIVILIZATION.md CIV-13 (lines 741-784)",
            "CANON_RI_COMPLETE_TECHNIQUES.md L257, L304, L319, L822",
            "ECOLOGY L1409, L1458, L1555"
        ],
        "canonEvents": [
            "Wang Lin becomes elder",
            "Wang Lin receives Three Rites",
            "Wang Lin devours Earth Fire main vein → Fire Essence True Body"
        ]
    },
    {
        "id": "CIV-14",
        "name": "Soul Refining Tribe",
        "nameCn": "炼魂部族",
        "fid": "F26",
        "type": "tribe",
        "subtype": "sect_like_tribe",
        "alignment": "righteous (Wang Lin's lineage)",
        "headquarters": "East Demon Spirit Sea, Planet Tian Yun",
        "peakRealm": "Body refining (Thirteen, Huo Pao)",
        "canonConfidence": 4,
        "confidenceTier": "A",
        "leader": {
            "current": "Ouyang Hua (欧阳华) — Chief",
            "previous": None,
            "fate": "Active"
        },
        "alliedFactions": ["Wang Lin (revered as Ancestor)", "Soul Refining Sect (CIV-02, heritage predecessor)"],
        "enemyFactions": [],
        "knownMembers": [
            "Ouyang Hua (欧阳华) — Chief",
            "Thirteen (十三 / Shi San) — originally Mountain Valley Tribe native; Wang Lin's 1st disciple",
            "Huo Pao (火炮) — Wang Lin's disciple"
        ],
        "sectStructure": {
            "origin": "Mountain Valley Tribe (山谷部落) → Wang Lin taught Soul Refining heritage → became Soul Refining Tribe",
            "population": "Over 1 million people",
            "specialization": "Soul Refining Sect heritage; body refining",
            "soulBanners": "Restored Wang Lin's One Billion Soul Flag"
        },
        "chapterReferences": [
            "CANON_RI_CIVILIZATION.md CIV-14 (lines 787-827)",
            "CANON_RI_COMPLETE_WORLD.md L441-444, L2521, L2523",
            "ECOLOGY L809-812, L1810-1821"
        ],
        "canonEvents": [
            "Wang Lin taught Soul Refining heritage to Mountain Valley Tribe",
            "Tribe became Soul Refining Tribe (1M+ people)",
            "Tribe restored Wang Lin's One Billion Soul Flag",
            "Wang Lin is regarded as their founding ancestor"
        ]
    },
    {
        "id": "CIV-15",
        "name": "Origin Sect / Guiyuan Sect",
        "nameCn": "归一宗",
        "fid": "F12",
        "type": "sect",
        "subtype": "cultivation_sect",
        "alignment": "neutral",
        "headquarters": "Cloud Sea Star System",
        "peakRealm": "Rank 6 (eventually)",
        "canonConfidence": 4,
        "confidenceTier": "A",
        "leader": {
            "current": "Unnamed",
            "previous": None,
            "fate": "Active"
        },
        "alliedFactions": [],
        "enemyFactions": [],
        "knownMembers": [
            "Lu Yanfei",
            "Xu Yun",
            "Zhao Yu",
            "Lu Yuncong",
            "Song Wude (killed)",
            "Rudy (killed)",
            "Wang Lin (alias 'Ceng Niu' 曾牛 — lived as mortal village doctor, joined, won ranking tournament)"
        ],
        "sectStructure": {
            "specialization": "Earth element, fusion arts",
            "wangLinAlias": "Ceng Niu (曾牛) - mortal village doctor identity"
        },
        "chapterReferences": [
            "CANON_RI_CIVILIZATION.md CIV-15 (lines 830-858)"
        ],
        "canonEvents": [
            "Wang Lin lives as mortal village doctor (mortal life cycle)",
            "Wang Lin joins as 'Ceng Niu'",
            "Wang Lin wins ranking tournament for the sect",
            "Song Wude and Rudy killed"
        ]
    },
    {
        "id": "CIV-16",
        "name": "Da Lou Sword Sect",
        "nameCn": "大罗剑宗",
        "fid": "F09",
        "type": "sect",
        "subtype": "sword_cultivation_sect",
        "alignment": "righteous",
        "headquarters": "Cloud Sea Star System",
        "peakRealm": "Third Step (Ling Tianhou — Nirvana Void)",
        "canonConfidence": 4,
        "confidenceTier": "A",
        "leader": {
            "current": "Ling Tianhou (凌天候) — Nirvana Void (Third Step)",
            "previous": None,
            "fate": "Alive"
        },
        "alliedFactions": ["Wang Lin (after 3-sword-strike test)"],
        "enemyFactions": ["All-Seer (helped defend Wang Lin against him)"],
        "knownMembers": [
            "Ling Tianhou (凌天候) — Elder / Sect Master; Nirvana Void",
            "3 Elders — killed by Wang Lin on Earth Planet"
        ],
        "sectStructure": {
            "elderSectMaster": "Ling Tianhou (Nirvana Void)",
            "signatureTest": "3 sword strikes test",
            "specialization": "Sword cultivation"
        },
        "chapterReferences": [
            "CANON_RI_CIVILIZATION.md CIV-16 (lines 861-887)",
            "CANON_RI_COMPLETE_WORLD.md N39",
            "CANON_RI_CHARACTER_DECISIONS.md CD-25"
        ],
        "canonEvents": [
            "Wang Lin killed 3 elders on Earth Planet",
            "Ling Tianhou challenged Wang Lin to take 3 sword strikes (Wang Lin survived all 3)",
            "Ling Tianhou helped defend Wang Lin against All-Seer",
            "Allied during the East Demon Spirit Sea arc",
            "Helped with the void Moongazer Serpent incident"
        ]
    },
    {
        "id": "CIV-17",
        "name": "Cultivation Alliance",
        "nameCn": "修真联盟",
        "fid": "F43",
        "type": "alliance",
        "subtype": "inter_sect_governing_body",
        "alignment": "neutral (governing)",
        "headquarters": "Alliance Star System",
        "peakRealm": "Third Step+ (multiple elders)",
        "canonConfidence": 4,
        "confidenceTier": "A",
        "leader": {
            "current": "Unnamed",
            "previous": None,
            "fate": "Active"
        },
        "alliedFactions": ["Vermilion Bird Country (oversees)"],
        "enemyFactions": [],
        "knownMembers": [
            "Mo Zhi — Alliance emissary",
            "Lu Yun (5th-Gen Vermilion Bird) — infiltrated Alliance HQ"
        ],
        "sectStructure": {
            "emissary": "Mo Zhi",
            "infiltrator": "Lu Yun (5th-Gen Vermilion Bird) infiltrated HQ",
            "keyDetail": "The Alliance 'went crazy for' the Heaven Defying Pearl - aware of its significance"
        },
        "chapterReferences": [
            "CANON_RI_CIVILIZATION.md CIV-17 (lines 890-920)",
            "CANON_RI_COMPLETE_WORLD.md L2712"
        ],
        "canonEvents": [
            "Alliance sought the Heaven Defying Pearl",
            "Lu Yun (5th-Gen Vermilion Bird) infiltrated Alliance HQ",
            "All-Seer also infiltrated Alliance HQ"
        ]
    },
    {
        "id": "CIV-18",
        "name": "Seven Paths Sect",
        "nameCn": "七道宗",
        "fid": "F44",
        "type": "creator_sect",
        "subtype": "cosmic_level_entity",
        "alignment": "beyond_alignment",
        "headquarters": "Cave World (cosmic)",
        "peakRealm": "Heaven Trampling (Seven-Colored Daoist)",
        "canonConfidence": 5,
        "confidenceTier": "A",
        "leader": {
            "current": "Seven-Colored Daoist (七彩道人 / 七彩仙尊) — Cave World's CREATOR and OWNER",
            "previous": None,
            "fate": "Killed by Wang Lin (end of RI canon)"
        },
        "alliedFactions": [],
        "enemyFactions": ["Wang Lin (livestock → killer)",
                          "All cultivators inside the Cave World (unknowing livestock)"],
        "knownMembers": [
            "Seven-Colored Daoist (七彩道人) — Founder / Cave World's creator-owner",
            "Three Souls and Seven Spirits (created by Seven-Colored Daoist): Situ Nan (Green Soul), Qing Shui (Slaughter Soul), Tan Lang (Yellow Soul), Xie Qing (Third Soul), and others"
        ],
        "sectStructure": {
            "founder": "Seven-Colored Daoist (cosmic-level entity, NOT a normal sect master)",
            "keyDetail": "NOT a normal sect. Created the Cave World itself as a Joss Flame farm.",
            "signatureTools": "108 Seven-Colored Divine Sky Nails (designed to kill Third Step experts)",
            "conceptualStructure": "Three Souls and Seven Spirits underpins the Cave World's spiritual structure"
        },
        "chapterReferences": [
            "CANON_RI_CIVILIZATION.md CIV-18 (lines 923-945)",
            "CANON_RI_COMPLETE_WORLD.md N114",
            "CANON_RI_CHARACTER_DECISIONS.md CD-01",
            "CANON_RI_TIMELINE.md E02-E04, E30",
            "ECOLOGY L1106-1107"
        ],
        "canonEvents": [
            "Seven-Colored Daoist created the Cave World (~100,000 years before Wang Lin's birth) [E02]",
            "Created the Three Souls and Seven Spirits as fragments of himself [E03]",
            "Bestowed the Heaven-Defying Pearl to the Realm-Sealing Supreme as proof of authority [E04]",
            "Maintained the Joss Flame harvest for ~100,000 years",
            "Wang Lin kills him at the end of RI [E30, ~Year 1500]"
        ]
    },

    # ===== Part 3 — Additional Faction Briefs (27) =====
    {
        "id": "CIV-brief-01",
        "name": "Xuan Dao Sect",
        "nameCn": "玄道宗",
        "fid": "F06",
        "type": "sect",
        "subtype": "orthodox_sect",
        "alignment": "righteous",
        "headquarters": "Country of Zhao, Planet Suzaku",
        "peakRealm": "Unnamed (Soul Formation possible)",
        "canonConfidence": 4,
        "confidenceTier": "B",
        "leader": {"current": "Unnamed", "previous": None, "fate": "Active"},
        "alliedFactions": ["Heng Yue Sect (neutral neighbor)"],
        "enemyFactions": [],
        "knownMembers": [],
        "sectStructure": {
            "keyEvent": "Wang Lin broke through to Soul Formation on a mountain near Xuan Dao Sect",
            "modRole": "Geographic landmark — the mountain where Wang Lin achieved Soul Formation"
        },
        "chapterReferences": [
            "CANON_RI_CIVILIZATION.md CIV-brief-01",
            "CANON_RI_COMPLETE_WORLD.md L2301"
        ],
        "canonEvents": ["Wang Lin broke through to Soul Formation on a mountain near Xuan Dao Sect"]
    },
    {
        "id": "CIV-brief-02",
        "name": "Luo He Sect",
        "nameCn": "罗河宗",
        "fid": "F15",
        "type": "sect",
        "subtype": "orthodox_sect",
        "alignment": "righteous",
        "headquarters": "Fire Burn Country, Planet Suzaku",
        "peakRealm": "Core Formation+ (Li Qiqing)",
        "canonConfidence": 4,
        "confidenceTier": "B",
        "leader": {"current": "Unnamed", "previous": None, "fate": "Active"},
        "alliedFactions": [],
        "enemyFactions": [],
        "knownMembers": ["Li Qiqing (elite disciple)"],
        "sectStructure": {"modRole": "Li Muwan's backstory connection (Li Muwan's original sect)"},
        "chapterReferences": ["CANON_RI_CIVILIZATION.md CIV-brief-02"],
        "canonEvents": []
    },
    {
        "id": "CIV-brief-03",
        "name": "Tian Yu Sect",
        "nameCn": "天羽宗",
        "fid": "F16",
        "type": "sect",
        "subtype": "cultivation_sect",
        "alignment": "neutral",
        "headquarters": "Planet Suzaku",
        "peakRealm": "Unnamed",
        "canonConfidence": 3,
        "confidenceTier": "C",
        "leader": {"current": "Unnamed", "previous": None, "fate": "Active"},
        "alliedFactions": [],
        "enemyFactions": [],
        "knownMembers": ["Qian Feng (gave Kunji Whip to Hong Die)"],
        "sectStructure": {
            "specialization": "Kunji Whip (heavy treasure)",
            "modRole": "Source of the Kunji Whip artifact"
        },
        "chapterReferences": ["CANON_RI_CIVILIZATION.md CIV-brief-03"],
        "canonEvents": ["Qian Feng gave Kunji Whip to Hong Die; after Hong Die's death, Wang Lin obtained it"]
    },
    {
        "id": "CIV-brief-04",
        "name": "Qihuang Sect",
        "nameCn": "岐黄宗",
        "fid": "F21",
        "type": "sect",
        "subtype": "medical_herbal_sect",
        "alignment": "righteous",
        "headquarters": "Planet Suzaku",
        "peakRealm": "Core Formation+ (Yun Fei)",
        "canonConfidence": 4,
        "confidenceTier": "C",
        "leader": {"current": "Unnamed", "previous": None, "fate": "Active"},
        "alliedFactions": [],
        "enemyFactions": ["Wang Lin (Yun Fei killed by Wang Lin's devil)"],
        "knownMembers": ["Yun Fei (successor) — killed by Wang Lin's devil"],
        "sectStructure": {
            "specialization": "'Qi-Huang' medicine — herbal/medical practices",
            "modRole": "Herbal pill source; medical services"
        },
        "chapterReferences": ["CANON_RI_CIVILIZATION.md CIV-brief-04"],
        "canonEvents": ["Yun Fei (successor) killed by Wang Lin's devil"]
    },
    {
        "id": "CIV-brief-05",
        "name": "Poison Palace",
        "nameCn": "毒宫",
        "fid": "F22",
        "type": "sect",
        "subtype": "demonic_sect",
        "alignment": "demonic",
        "headquarters": "Planet Suzaku",
        "peakRealm": "Core Formation+ (Qian Kun)",
        "canonConfidence": 4,
        "confidenceTier": "C",
        "leader": {"current": "Unnamed", "previous": None, "fate": "Active"},
        "alliedFactions": [],
        "enemyFactions": ["Wang Lin (Qian Kun killed by Wang Lin)"],
        "knownMembers": ["Qian Kun (disciple) — killed by Wang Lin"],
        "sectStructure": {
            "specialization": "Poison cultivation",
            "modRole": "Poison-themed enemy faction"
        },
        "chapterReferences": ["CANON_RI_CIVILIZATION.md CIV-brief-05"],
        "canonEvents": ["Qian Kun (disciple) killed by Wang Lin"]
    },
    {
        "id": "CIV-brief-06",
        "name": "Dao Devil Sect / Dao Demon Sect",
        "nameCn": "道魔宗",
        "fid": "F23",
        "type": "sect",
        "subtype": "demonic_sect",
        "alignment": "demonic",
        "headquarters": "Mengtu Province / Green Devil Continent, IAC",
        "peakRealm": "Third Step+ (Sect Master)",
        "canonConfidence": 5,
        "confidenceTier": "A",
        "leader": {
            "current": "Dao Devil Sect Master — Third Step+; annihilated by Wang Lin (~Year 1600)",
            "previous": None,
            "fate": "Annihilated by Wang Lin"
        },
        "alliedFactions": [],
        "enemyFactions": ["Wang Lin (sect master captured Wang Lin as sacrifice → reversed → annihilated)"],
        "knownMembers": [
            "Dao Devil Sect Master (N122) — Third Step+",
            "Ji Si (subordinate, implanted Essences into Wang Lin)"
        ],
        "sectStructure": {
            "specialization": "Dao Devil Great Hand Seal; Green Devil Scorpion resurrection",
            "modRole": "Late-game demonic sect on the IAC. DESTROYED by Wang Lin - ruins only"
        },
        "chapterReferences": [
            "CANON_RI_CIVILIZATION.md CIV-brief-06",
            "CANON_RI_COMPLETE_WORLD.md N122, N132",
            "CANON_RI_CHARACTER_DECISIONS.md CD-12",
            "ECOLOGY L1449"
        ],
        "canonEvents": [
            "Sect Master captured Wang Lin as Green Devil sacrifice",
            "Wang Lin reversed ritual",
            "Wang Lin annihilated the sect",
            "Wang Lin devoured the Dao Demon Sect Master (~Year 1600)"
        ]
    },
    {
        "id": "CIV-brief-07",
        "name": "Canglong Sect / Azure Dragon Sect",
        "nameCn": "苍龙宗",
        "fid": "F24",
        "type": "sect",
        "subtype": "cultivation_sect",
        "alignment": "neutral",
        "headquarters": "Tianniu Province, IAC",
        "peakRealm": "Third Step (Du Qing — ancestor)",
        "canonConfidence": 4,
        "confidenceTier": "B",
        "leader": {"current": "Du Qing (ancestor)", "previous": None, "fate": "Active"},
        "alliedFactions": [],
        "enemyFactions": [],
        "knownMembers": ["Du Qing (ancestor)"],
        "sectStructure": {
            "specialization": "Earth Fire Dragon cultivation",
            "modRole": "Earth Fire Dragon source on the IAC"
        },
        "chapterReferences": [
            "CANON_RI_CIVILIZATION.md CIV-brief-07",
            "ECOLOGY L1432-1433"
        ],
        "canonEvents": ["Wang Lin extracted Earth Fire Dragon's soul here"]
    },
    {
        "id": "CIV-brief-08",
        "name": "Dong Lin Sect",
        "nameCn": "东林宗",
        "fid": "F25",
        "type": "sect",
        "subtype": "cultivation_sect",
        "alignment": "neutral",
        "headquarters": "Dong Lin Pool area, IAC",
        "peakRealm": "Third Step+ (ancient god-tier entity sealed beneath)",
        "canonConfidence": 4,
        "confidenceTier": "B",
        "leader": {"current": "Unknown (sect destroyed before Wang Lin's arrival)", "previous": None, "fate": "DESTROYED"},
        "alliedFactions": [],
        "enemyFactions": [],
        "knownMembers": [],
        "sectStructure": {
            "modRole": "Ruin site on the IAC. Tianyunzi connection."
        },
        "chapterReferences": ["CANON_RI_CIVILIZATION.md CIV-brief-08"],
        "canonEvents": ["DESTROYED before Wang Lin's arrival", "Wang Lin found Tianyunzi's handwriting here"]
    },
    {
        "id": "CIV-brief-09",
        "name": "Wang Clan",
        "nameCn": "王族",
        "fid": "F27",
        "type": "clan",
        "subtype": "mortal_clan",
        "alignment": "neutral",
        "headquarters": "Wang Family Village, Country of Zhao",
        "peakRealm": "mortal (originally)",
        "canonConfidence": 5,
        "confidenceTier": "A",
        "leader": {"current": "Wang Tianshui (father) / Wang Lin (post-restoration)", "previous": None, "fate": "EXTERMINATED then restored"},
        "alliedFactions": ["Wang Lin"],
        "enemyFactions": ["Teng Clan (exterminated them)"],
        "knownMembers": [
            "Wang Tianshui (father)",
            "Zhou Tingsu (mother)",
            "Fourth Uncle (Wang Tianshan)",
            "Wang Hao",
            "Wang Zhuo",
            "Wang Lin"
        ],
        "sectStructure": {
            "specialization": "Carpentry (originally)",
            "modRole": "Wang Lin's mortal origins. The village massacre is the inciting event of his cultivation journey."
        },
        "chapterReferences": [
            "CANON_RI_CIVILIZATION.md CIV-brief-09",
            "CANON_RI_COMPLETE_WORLD.md N01, N03"
        ],
        "canonEvents": [
            "EXTERMINATED by Teng Huayuan",
            "Later restored through Wang Lin's influence"
        ]
    },
    {
        "id": "CIV-brief-10",
        "name": "Tattoo Clan",
        "nameCn": "纹族",
        "fid": "F29",
        "type": "clan",
        "subtype": "specialized_clan",
        "alignment": "neutral",
        "headquarters": "Planet Suzaku (region unspecified)",
        "peakRealm": "Unnamed",
        "canonConfidence": 3,
        "confidenceTier": "C",
        "leader": {"current": "Unnamed", "previous": None, "fate": "Active"},
        "alliedFactions": [],
        "enemyFactions": [],
        "knownMembers": [],
        "sectStructure": {
            "specialization": "Tattoo-based cultivation (Beast Skin Tattoo, Tattoo Talisman Speed Boost)",
            "modRole": "Unique cultivation method source"
        },
        "chapterReferences": ["CANON_RI_CIVILIZATION.md CIV-brief-10"],
        "canonEvents": []
    },
    {
        "id": "CIV-brief-11",
        "name": "Forsaken Immortal Clan",
        "nameCn": "遗仙族",
        "fid": "F30",
        "type": "clan",
        "subtype": "hidden_clan",
        "alignment": "neutral",
        "headquarters": "Vermilion Bird Star (hidden)",
        "peakRealm": "Late Nascent Soul (Yun Quezi — 2nd Ancestor)",
        "canonConfidence": 4,
        "confidenceTier": "B",
        "leader": {"current": "Yun Quezi (2nd Ancestor)", "previous": None, "fate": "Active"},
        "alliedFactions": [],
        "enemyFactions": [],
        "knownMembers": [
            "Yun Quezi (2nd Ancestor)",
            "Qian Pinghai (13th-Gen Vermilion Bird)"
        ],
        "sectStructure": {
            "specialization": "Curse mastery, reincarnation manipulation",
            "modRole": "Curse/reincarnation mechanics source. Hidden faction."
        },
        "chapterReferences": ["CANON_RI_CIVILIZATION.md CIV-brief-11"],
        "canonEvents": []
    },
    {
        "id": "CIV-brief-12",
        "name": "Moon Devourer Clan",
        "nameCn": "吞月族",
        "fid": "F31",
        "type": "clan",
        "subtype": "cultivation_clan",
        "alignment": "antagonist",
        "headquarters": "IAC region (unspecified)",
        "peakRealm": "Third Step",
        "canonConfidence": 4,
        "confidenceTier": "B",
        "leader": {"current": "Unnamed", "previous": None, "fate": "Active"},
        "alliedFactions": [],
        "enemyFactions": ["Wang Lin (interrupted his Ancient God recognition)"],
        "knownMembers": ["Mysterious youth + old man (sent by clan)"],
        "sectStructure": {
            "specialization": "Moon-themed cultivation",
            "modRole": "Antagonist clan encountered during Ancient God recognition arc"
        },
        "chapterReferences": ["CANON_RI_CIVILIZATION.md CIV-brief-12"],
        "canonEvents": ["Sent mysterious youth + old man to interrupt Wang Lin's Ancient God recognition; beaten back"]
    },
    {
        "id": "CIV-brief-13",
        "name": "Scatter Thunder Clan / Flash Thunder Clan",
        "nameCn": "散雷族",
        "fid": "F32",
        "type": "clan",
        "subtype": "thunder_cultivation_clan",
        "alignment": "antagonist",
        "headquarters": "IAC region (unspecified)",
        "peakRealm": "Third Step (5th Heaven Blight head elder)",
        "canonConfidence": 4,
        "confidenceTier": "A",
        "leader": {
            "current": "Sect Leader — killed by Wang Lin",
            "previous": None,
            "fate": "DESTROYED by Wang Lin"
        },
        "alliedFactions": [],
        "enemyFactions": ["Wang Lin"],
        "knownMembers": ["5th Heaven Blight head elder (killed)"],
        "sectStructure": {
            "specialization": "Thunder cultivation; Eternal Thunderbolt",
            "modRole": "Thunder cultivation faction; source of Eternal Thunderbolt. DESTROYED by Wang Lin."
        },
        "chapterReferences": [
            "CANON_RI_CIVILIZATION.md CIV-brief-13",
            "CANON_RI_COMPLETE_TECHNIQUES.md L1515"
        ],
        "canonEvents": [
            "Wang Lin killed their Sect Leader and 5th Heaven Blight head elder",
            "Wang Lin devoured 8 ancient thunder dragons",
            "Eternal Thunderbolt absorbed → Wang Lin reached Nirvana Shatterer late stage"
        ]
    },
    {
        "id": "CIV-brief-14",
        "name": "Fire Sparrow Clan",
        "nameCn": "火雀族",
        "fid": "F33",
        "type": "clan",
        "subtype": "cultivation_clan",
        "alignment": "neutral",
        "headquarters": "Vermilion Bird Starfield",
        "peakRealm": "Unnamed",
        "canonConfidence": 4,
        "confidenceTier": "C",
        "leader": {"current": "Unnamed", "previous": None, "fate": "Active"},
        "alliedFactions": [],
        "enemyFactions": [],
        "knownMembers": ["3rd-Gen Evil Sparrow (mentioned)"],
        "sectStructure": {
            "specialization": "Fire sparrow cultivation",
            "modRole": "Vermilion Bird Starfield sub-faction"
        },
        "chapterReferences": ["CANON_RI_CIVILIZATION.md CIV-brief-14"],
        "canonEvents": []
    },
    {
        "id": "CIV-brief-15",
        "name": "Giant Demon Clan",
        "nameCn": "巨魔族",
        "fid": "F34",
        "type": "clan",
        "subtype": "cultivation_clan",
        "alignment": "neutral",
        "headquarters": "IAC region (unspecified)",
        "peakRealm": "Unnamed",
        "canonConfidence": 4,
        "confidenceTier": "B",
        "leader": {"current": "Unnamed", "previous": "Ancestor (killed for Ancient God's Blood)", "fate": "Active"},
        "alliedFactions": ["Wang Lin (Chi Hu gave Wang Lin the Star Compass)"],
        "enemyFactions": [],
        "knownMembers": ["Chi Hu (gave Wang Lin the Star Compass)"],
        "sectStructure": {
            "specialization": "Ancient God's Blood",
            "modRole": "Ancient God's Blood source; Star Compass origin"
        },
        "chapterReferences": ["CANON_RI_CIVILIZATION.md CIV-brief-15"],
        "canonEvents": ["Their Ancestor was killed for Ancient God's Blood", "Chi Hu gave Wang Lin the Star Compass"]
    },
    {
        "id": "CIV-brief-16",
        "name": "Dark Scorpion Clan",
        "nameCn": "暗蝎族",
        "fid": "F35",
        "type": "clan",
        "subtype": "cultivation_clan",
        "alignment": "Wang Lin-aligned",
        "headquarters": "IAC region (unspecified)",
        "peakRealm": "Unnamed",
        "canonConfidence": 4,
        "confidenceTier": "A",
        "leader": {"current": "Wang Lin (Ruler)", "previous": None, "fate": "Wang Lin as Ruler"},
        "alliedFactions": ["Wang Lin"],
        "enemyFactions": [],
        "knownMembers": ["Wang Lin ('Ruler of the Dark Scorpion Clan')"],
        "sectStructure": {
            "specialization": "Scorpion cultivation",
            "modRole": "Player/Wang Lin-ruled clan. Unique political relationship."
        },
        "chapterReferences": ["CANON_RI_CIVILIZATION.md CIV-brief-16"],
        "canonEvents": ["Wang Lin is 'Ruler of the Dark Scorpion Clan'"]
    },
    {
        "id": "CIV-brief-17",
        "name": "Zhen Family / Zhan Family",
        "nameCn": "甄家/战家",
        "fid": "F37",
        "type": "clan",
        "subtype": "cultivation_clan",
        "alignment": "neutral",
        "headquarters": "IAC",
        "peakRealm": "Third Step (Zhan Xingye)",
        "canonConfidence": 4,
        "confidenceTier": "B",
        "leader": {"current": "Unnamed", "previous": None, "fate": "Active"},
        "alliedFactions": [],
        "enemyFactions": [],
        "knownMembers": ["Zhan Xingye", "Zhan Laogui"],
        "sectStructure": {
            "specialization": "Battle Scroll (3 scrolls merged into golden 'Battle' word by Wang Lin)",
            "modRole": "Battle Scroll artifact source on the IAC"
        },
        "chapterReferences": ["CANON_RI_CIVILIZATION.md CIV-brief-17"],
        "canonEvents": ["Wang Lin merged 3 Battle Scrolls into golden 'Battle' word"]
    },
    {
        "id": "CIV-brief-18",
        "name": "Celestial Clan",
        "nameCn": "仙族",
        "fid": "F39",
        "type": "primordial_race",
        "subtype": "celestial_race",
        "alignment": "neutral",
        "headquarters": "IAC",
        "peakRealm": "Grand Empyrean (Immortal Ancestor)",
        "canonConfidence": 4,
        "confidenceTier": "B",
        "leader": {"current": "Immortal Ancestor (Grand Empyrean)", "previous": None, "fate": "Active"},
        "alliedFactions": [],
        "enemyFactions": ["Ancient Clan (CIV-10, rival race)"],
        "knownMembers": [
            "Immortal Ancestor",
            "Dao Yi Great Celestial Venerable",
            "Sea Child Celestial Venerable",
            "Jiu Di (Grand Empyrean)",
            "Song Tian",
            "Yun Yifeng",
            "Purple Dawn Immortal Emperor"
        ],
        "sectStructure": {
            "specialization": "Immortal/celestial cultivation; Nine Suns (originally)",
            "keyDetail": "Wang Lin = '49th Ascendant Empyrean'; Grand Empyrean Xuan Luo's disciple",
            "modRole": "Ancient Clan's rival race on the IAC; Nine Suns system"
        },
        "chapterReferences": ["CANON_RI_CIVILIZATION.md CIV-brief-18"],
        "canonEvents": []
    },
    {
        "id": "CIV-brief-19",
        "name": "Great Wang Dynasty",
        "nameCn": "大王朝",
        "fid": "F42",
        "type": "dynasty",
        "subtype": "mortal_and_cultivation_dynasty",
        "alignment": "Wang Lin-aligned",
        "headquarters": "Planet Suzaku",
        "peakRealm": "Wang Lin-tier (founder)",
        "canonConfidence": 4,
        "confidenceTier": "B",
        "leader": {"current": "Wang Lin (founder)", "previous": None, "fate": "Active"},
        "alliedFactions": ["Wang Lin"],
        "enemyFactions": [],
        "knownMembers": ["Wang Lin"],
        "sectStructure": {
            "keyDetail": "Wang Lin's dynasty",
            "modRole": "Player-built or Wang Lin-built political entity on Planet Suzaku"
        },
        "chapterReferences": ["CANON_RI_CIVILIZATION.md CIV-brief-19"],
        "canonEvents": []
    },
    {
        "id": "CIV-brief-20",
        "name": "Everlasting Sect",
        "nameCn": "永恒宗",
        "fid": "F17",
        "type": "sect",
        "subtype": "blood_sword_sect",
        "alignment": "neutral",
        "headquarters": "Cloud Sea Star System",
        "peakRealm": "Third Step+ (Blood Sword)",
        "canonConfidence": 4,
        "confidenceTier": "B",
        "leader": {"current": "Blood Sword", "previous": None, "fate": "Active"},
        "alliedFactions": ["Wang Lin (about to represent them in rank 8 tournament before Rank 9 God Sect canceled it)"],
        "enemyFactions": [],
        "knownMembers": ["Blood Sword"],
        "sectStructure": {
            "specialization": "Blood-element sword cultivation",
            "modRole": "Cloud Sea Star System faction; blood sword path"
        },
        "chapterReferences": ["CANON_RI_CIVILIZATION.md CIV-brief-20"],
        "canonEvents": ["Wang Lin was about to represent them in rank 8 tournament before Rank 9 God Sect canceled it"]
    },
    {
        "id": "CIV-brief-21",
        "name": "Ghost Sect",
        "nameCn": "鬼宗",
        "fid": "F18",
        "type": "sect",
        "subtype": "demonic_sect",
        "alignment": "demonic",
        "headquarters": "Cloud Sea Star System",
        "peakRealm": "Unnamed",
        "canonConfidence": 4,
        "confidenceTier": "B",
        "leader": {"current": "Unnamed", "previous": None, "fate": "Active"},
        "alliedFactions": [],
        "enemyFactions": ["Wang Lin (sent Li Qianmei deep into spatial realm)"],
        "knownMembers": [],
        "sectStructure": {
            "specialization": "Ghost/spirit cultivation",
            "modRole": "Antagonist faction on Cloud Sea; Li Qianmei connection"
        },
        "chapterReferences": ["CANON_RI_CIVILIZATION.md CIV-brief-21"],
        "canonEvents": ["Sent Li Qianmei deep into a spatial realm with powerful beasts"]
    },
    {
        "id": "CIV-brief-22",
        "name": "Rank 9 God Sect",
        "nameCn": "九等神宗",
        "fid": "F19",
        "type": "sect",
        "subtype": "high_tier_sect",
        "alignment": "antagonist",
        "headquarters": "Cloud Sea Star System",
        "peakRealm": "Third Step+ (Daoist Water / Shui Daozi)",
        "canonConfidence": 5,
        "confidenceTier": "A",
        "leader": {
            "current": "Daoist Water (Shui Daozi) — peak Third Step (Nirvana Void+); from the Outer Realm; killed by Wang Lin (~Year 500-600, Ch. 1509)",
            "previous": None,
            "fate": "Daoist Water killed by Wang Lin"
        },
        "alliedFactions": [],
        "enemyFactions": ["Wang Lin (sensed Lord of the Sealed Realm's aura on Wang Lin → attacked)"],
        "knownMembers": ["Daoist Water (Shui Daozi) — from the Outer Realm"],
        "sectStructure": {
            "specialization": "Rank 9 cultivation",
            "modRole": "Major antagonist on Cloud Sea Star System; Outer Realm connection"
        },
        "chapterReferences": [
            "CANON_RI_CIVILIZATION.md CIV-brief-22",
            "CANON_RI_COMPLETE_WORLD.md N102",
            "CANON_RI_TIMELINE.md E59, E79",
            "CANON_RI_CHARACTER_DECISIONS.md CD-06",
            "Ch. 1509"
        ],
        "canonEvents": [
            "Sensing Lord of the Sealed Realm's aura on Wang Lin → attacked",
            "Lord of the Sealed Realm's spirit appeared, severely injured Daoist Water",
            "Wang Lin turned to stone",
            "Li Qianmei's 10-year blood anointment saved Wang Lin",
            "Daoist Water killed by Wang Lin (~Year 500-600, Ch. 1509)"
        ]
    },
    {
        "id": "CIV-brief-23",
        "name": "Treasured Jade Sect",
        "nameCn": "宝玉宗",
        "fid": "F20",
        "type": "sect",
        "subtype": "merchant_auction_sect",
        "alignment": "neutral",
        "headquarters": "Cloud Sea Star System",
        "peakRealm": "Third Step+ (multiple Nirvana Shatterer old monsters)",
        "canonConfidence": 4,
        "confidenceTier": "B",
        "leader": {"current": "Unnamed (multiple Nirvana Shatterer old monsters)", "previous": None, "fate": "Active"},
        "alliedFactions": [],
        "enemyFactions": [],
        "knownMembers": [],
        "sectStructure": {
            "specialization": "Auctions, treasure exchanges",
            "modRole": "Auction house; trade hub on Cloud Sea"
        },
        "chapterReferences": ["CANON_RI_CIVILIZATION.md CIV-brief-23"],
        "canonEvents": ["Li Qianmei invited Wang Lin to auction", "Wang Lin participated in secret exchange"]
    },
    {
        "id": "CIV-brief-24",
        "name": "Blue Silk Clan",
        "nameCn": "蓝丝族",
        "fid": "F08",
        "type": "clan",
        "subtype": "cultivation_clan",
        "alignment": "neutral",
        "headquarters": "Blue Silk Clan Star Domain",
        "peakRealm": "Dao Master (Blue Dream) — Void Tribulant+",
        "canonConfidence": 4,
        "confidenceTier": "B",
        "leader": {"current": "Dao Master Blue Dream (蓝梦道主)", "previous": None, "fate": "Active"},
        "alliedFactions": ["Wang Lin (taught by Blue Dream)"],
        "enemyFactions": [],
        "knownMembers": [
            "Blue Dream (蓝梦道主) — Dao Master, Void Tribulant+",
            "Li Qianmei (daughter)"
        ],
        "sectStructure": {
            "specialization": "Dao Art Fusion, Light Shadow Shield, Overturn Heaven Seal",
            "modRole": "High-tier clan; source of Dao Art Fusion technique"
        },
        "chapterReferences": [
            "CANON_RI_CIVILIZATION.md CIV-brief-24",
            "CANON_RI_COMPLETE_WORLD.md N16, N29",
            "CANON_RI_CHARACTER_DECISIONS.md CD-19"
        ],
        "canonEvents": ["Wang Lin learned Dao Art Fusion + Light Shadow Shield from Blue Dream"]
    },
    {
        "id": "CIV-brief-25",
        "name": "Chosen Immortal Clan",
        "nameCn": "遗仙族",
        "fid": "F38",
        "type": "clan",
        "subtype": "immortal_clan",
        "alignment": "neutral (enslaved)",
        "headquarters": "Thunder Celestial Realm",
        "peakRealm": "Unnamed",
        "canonConfidence": 4,
        "confidenceTier": "B",
        "leader": {"current": "Unnamed", "previous": None, "fate": "Freed by Wang Lin"},
        "alliedFactions": ["Wang Lin (helped them escape)"],
        "enemyFactions": [],
        "knownMembers": [],
        "sectStructure": {
            "modRole": "Rescue quest faction in Thunder Celestial Realm"
        },
        "chapterReferences": ["CANON_RI_CIVILIZATION.md CIV-brief-25"],
        "canonEvents": ["Enslaved in Thunder Celestial Realm", "Wang Lin helped them escape"]
    },
    {
        "id": "CIV-brief-26",
        "name": "Wu Xuan Country",
        "nameCn": "吴玄国",
        "fid": "F45",
        "type": "nation_state",
        "subtype": "mortal_nation_state",
        "alignment": "neutral",
        "headquarters": "IAC",
        "peakRealm": "mortal (with reincarnated Situ Nan as Grand Marshal)",
        "canonConfidence": 4,
        "confidenceTier": "B",
        "leader": {"current": "Grand Marshal 'Si Nan' (Situ Nan reincarnated)", "previous": None, "fate": "Active"},
        "alliedFactions": ["Situ Nan (reincarnated as Si Nan, Southern Prince)"],
        "enemyFactions": [],
        "knownMembers": ["Si Nan / Situ Nan (Grand Marshal, Southern Prince)"],
        "sectStructure": {
            "modRole": "Situ Nan's reincarnation location; mortal nation on the IAC"
        },
        "chapterReferences": [
            "CANON_RI_CIVILIZATION.md CIV-brief-26",
            "CANON_RI_COMPLETE_WORLD.md N20",
            "CANON_RI_CHARACTER_DECISIONS.md CD-14"
        ],
        "canonEvents": ["Situ Nan reincarnated here as Grand Marshal 'Si Nan' (Southern Prince)"]
    },
    {
        "id": "CIV-brief-27",
        "name": "War Shrine Sect",
        "nameCn": "战神殿",
        "fid": "F14",
        "type": "sect",
        "subtype": "cultivation_sect",
        "alignment": "righteous",
        "headquarters": "Planet Suzaku",
        "peakRealm": "Unnamed",
        "canonConfidence": 4,
        "confidenceTier": "B",
        "leader": {"current": "Unnamed", "previous": None, "fate": "Active"},
        "alliedFactions": ["Wang Lin (former Direct Disciple)"],
        "enemyFactions": [],
        "knownMembers": ["Wang Lin (former Direct Disciple)"],
        "sectStructure": {
            "specialization": "Divine Path Technique (source of Wang Lin's avatar ability); war/spirit refinement",
            "keyDetail": "Wang Lin was a former Direct Disciple",
            "modRole": "Source of Divine Path (avatar) technique. Wang Lin's early background."
        },
        "chapterReferences": ["CANON_RI_CIVILIZATION.md CIV-brief-27"],
        "canonEvents": ["Wang Lin was a former Direct Disciple"]
    }
]


def build_output():
    return {
        "metadata": {
            "source": "Renegade Immortal (仙逆) by Er Gen",
            "extractedFrom": "CANON_RI_CIVILIZATION.md (Layer 4: Civilization Simulation, 1,361 lines)",
            "version": "1.0",
            "generated": "2026-07-15",
            "task": "EXTRACT-CIV-NPC Part A — Deep-extract faction/sect data",
            "totalFactions": len(FACTIONS),
            "fullyDetailedCount": sum(1 for f in FACTIONS if f["id"].startswith("CIV-") and not f["id"].startswith("CIV-brief")),
            "briefCount": sum(1 for f in FACTIONS if f["id"].startswith("CIV-brief")),
            "confidenceTierKey": {
                "A": "CANON_CONCRETE (conf 5) — explicitly stated in the novels or wiki",
                "B": "CANON_IMPLIED (conf 4) — must logically exist given canon facts",
                "C": "REASONABLE_RECONSTRUCTION (conf 3) — likely true, derived from cultivation systems and ecology",
                "D": "SPECULATION (conf 1-2) — possible but unverified; never overwrites canon",
                "F": "FORBIDDEN — contradicts known canon or user corrections"
            },
            "primeDirective": "Reality is objective; cultivation changes understanding, not existence. Every mortal in the Cave World is unknowing livestock for the Joss Flame economy. Never fill empty space with generic fantasy content."
        },
        "factions": FACTIONS,
        "crossFactionEconomy": {
            "currencyHierarchy": [
                {"currency": "Low-Grade Spirit Stones", "tier": "Mortal-tier to Qi Condensation", "usedBy": "All factions", "source": "Basic mining/gathering"},
                {"currency": "Mid-Grade Spirit Stones", "tier": "Foundation Establishment to Core Formation", "usedBy": "Mid-tier sects", "source": "Spirit vein refinement"},
                {"currency": "High-Grade Spirit Stones", "tier": "Nascent Soul to Soul Transformation", "usedBy": "Major sects, nations", "source": "Major vein extraction"},
                {"currency": "Celestial Jades", "tier": "Soul Transformation to Ascendant", "usedBy": "Soul Refining Sect, Heavenly Fate Sect", "source": "Rare vein deposits, theft"},
                {"currency": "Immortal Jades", "tier": "Nirvana Shatterer to Third Step", "usedBy": "Allheaven/Cloud Sea factions", "source": "Immortal realm deposits"},
                {"currency": "Origin Crystals", "tier": "Domain comprehension", "usedBy": "IAC factions", "source": "Extreme-rarity resource"}
            ],
            "pillEconomy": [
                {"pill": "Qi-Gathering Pill", "tier": "1-3 stage", "producer": "Any sect with alchemists", "consumer": "Qi Condensation disciples"},
                {"pill": "Foundation Establishment Pill", "tier": "4-6 stage", "producer": "Mid-tier sects", "consumer": "Foundation Establishment breakthrough"},
                {"pill": "Cold Dan", "tier": "7-8 stage", "producer": "Sea of Devils / Underworld Ascension", "consumer": "Golden Core formation success"},
                {"pill": "Blood Soul Pill", "tier": "9+ stage", "producer": "Yao Family (Blood Ancestor)", "consumer": "'Resurrection' — high tier"},
                {"pill": "Distant Heaven Pill", "tier": "7+ stage", "producer": "Purchased at Nan Dou City", "consumer": "Core Formation+ cultivators"},
                {"pill": "11-stage Pills", "tier": "11-stage", "producer": "IAC sects", "consumer": "Domain comprehension"}
            ],
            "tradeNetworkFlow": "Mortal Villages → (herbs, labor, Joss Flame) → Outer Disciples → (processed herbs, basic pills) → Inner Sects → (mid-tier pills, spirit stones) → Major Sects → (high-tier pills, artifacts, celestial jades) → Star System Hubs → (immortal jades, origin crystals) → IAC Center → (all resources converge) → [30% Joss Flame siphon → Seven-Colored Daoist]"
        },
        "regionalPowerStructures": {
            "zhaoCountry_earlyGame": {
                "location": "Planet Suzaku, Early Game",
                "structure": {
                    "Vermilion Bird Country (Level 6, Sovereign)": {
                        "Zhao Country (region)": ["Heng Yue Sect (orthodox, small)", "Xuan Dao Sect (orthodox, neighbor)", "Teng Clan (antagonist, DESTROYED)", "Wang Clan (mortal, EXTERMINATED, later restored)", "Tian Shui City (trade hub)"],
                        "Chu Country (region)": ["Cloud Sky Sect (orthodox, Nascent Soul, Li Muwan)"],
                        "Pilu Kingdom (region)": ["Soul Refining Sect (demonic, Nirvana Scryer, heritage transferred)"]
                    }
                }
            },
            "seaOfDevils_midGame": {
                "location": "Planet Suzaku, Mid Game",
                "structure": {
                    "Sea of Devils (lawless region, 14+ districts)": [
                        "Fighting Evil Sect (demonic, Soul Formation leader, DESTROYED by Wang Lin)",
                        "Corpse Yin Sect (demonic, multiple Nascent Soul elders)",
                        "Soul Refining Sect (demonic, Nirvana Scryer, heritage transferred out)",
                        "Nan Dou City (trade hub)",
                        "Forest of Distorted Divine Sense (hazard zone)"
                    ]
                }
            },
            "planetTianYun_midGame": {
                "location": "Mid Game",
                "structure": {
                    "Planet Tian Yun (rank-7 cultivation planet)": [
                        "Heavenly Fate Sect (7 color divisions, All-Seer, KILLED by Wang Lin): Red, Orange, Yellow, Green, Blue, Cyan, Purple (Wang Lin's division)",
                        "Yao Family (antagonist, Blood Ancestor Third Step, DESTROYED)",
                        "Soul Refining Tribe (righteous, 1M+ people, Wang Lin's heritage) — East Demon Spirit Sea",
                        "Immortal Monarch's Cave Mansion (ruin/site)"
                    ]
                }
            },
            "cloudSeaStarSystem_lateGame": {
                "location": "Late Game",
                "structure": [
                    "Da Lou Sword Sect (righteous, Third Step, Ling Tianhou)",
                    "Origin Sect / Guiyuan Sect (neutral, rank 6)",
                    "Rank 9 God Sect (Third Step, Daoist Water from Outer Realm, KILLED)",
                    "Treasured Jade Sect (auction house, Third Step+ old monsters)",
                    "Everlasting Sect (Third Step+, Blood Sword)",
                    "Ghost Sect (demonic)",
                    "Seven-Colored Realm (108 nails, Seven-Colored Daoist's tools)"
                ]
            },
            "iac_endgame": {
                "location": "Endgame, Cave World",
                "structure": {
                    "Immortal Astral Continent (7 continents, 440+ spirit veins)": [
                        "Ancient Clan (Dao Gu / Dao Yi / Primordial Ancient lineages) with Ancient Tomb, Ancestral Temple, Heavenly Bull Continent (Great Soul Sect, Canglong Sect, 120+ Fire Veins)",
                        "Celestial Clan (rival race, Grand Empyrean) with Nine Suns",
                        "Dao Devil Sect (DESTROYED by Wang Lin)",
                        "Dong Lin Sect (DESTROYED before Wang Lin)",
                        "Scatter Thunder Clan (DESTROYED by Wang Lin)",
                        "Zhen/Zhan Family (Battle Scroll)",
                        "Dark Scorpion Clan (Wang Lin as Ruler)",
                        "Forsaken Immortal Clan (hidden, curse mastery)",
                        "Wu Xuan Country (Situ Nan's reincarnation)"
                    ]
                }
            }
        },
        "sectLifecycleModel": {
            "formation": "A new sect forms when a cultivator reaches a realm significantly higher than the local baseline, attracts disciples by offering protection and teachings, claims a spirit vein or territory, and establishes a reputation. Example: Wang Lin's Soul Refining Tribe formed when he taught Soul Refining heritage to the Mountain Valley Tribe.",
            "growth": "A sect grows by producing higher-realm disciples (increasing peak realm ceiling), claiming more spirit veins and territory, establishing trade relationships, building alliances or vassal relationships, and accumulating Joss Flame through mortal population control.",
            "decline": "A sect declines by death or departure of patriarch/strongest cultivator, loss of spirit veins to rivals or natural depletion, internal power struggles, external attack (Teng Clan, Fighting Evil Sect, Dao Devil Sect), or loss of disciple talent pipeline.",
            "destructionPatterns": [
                {"type": "Total Annihilation", "example": "Dao Devil Sect (Wang Lin reversed the Green Devil sacrifice ritual and annihilated them)", "confidence": "A"},
                {"type": "Leadership Assassination + Collapse", "example": "Fighting Evil Sect (Wang Lin killed the leader, released the sect)", "confidence": "A"},
                {"type": "Clan Extermination", "example": "Teng Clan (Wang Lin killed all 9 Nascent Souls + patriarch); Wang Clan (Teng Huayuan's massacre)", "confidence": "A"},
                {"type": "Pre-Arrival Destruction", "example": "Dong Lin Sect (destroyed before Wang Lin arrived)", "confidence": "A"}
            ],
            "heritageTransferPaths": [
                "Soul Refining Sect → Wang Lin → Soul Refining Tribe (technique + banner) [A]",
                "Vermilion Bird Line (generational succession, 1st → 2nd → ... → 6th/Wang Lin → 15th/Zhou Wutai) [A]",
                "Great Soul Sect → Wang Lin (Ghostly Sail → Restriction Essence bloodlines) [A]",
                "Blue Silk Clan → Wang Lin (Dao Art Fusion, Light Shadow Shield from Blue Dream) [A]"
            ]
        },
        "forbiddenEntities": [
            "'Ten Thousand Demons Sect' → Use Sky Demon Country + Ancient Demon City [F]",
            "'Xue Yue' → Use Snow Domain Country (雪域国) [F]",
            "'Heavenly Demon City' → Use Ancient Demon City (古魔城) [F]",
            "'Suzaku Continent' → Planet Suzaku (朱雀星) is a PLANET, not a continent [F]",
            "'Heavenly Fate Continent' → Planet Tian Yun (天运星) is a PLANET, not a continent [F]",
            "'Ancient Emperor's inheritance' → Use Immortal Monarch's Cave Mansion [F]"
        ]
    }


def main():
    output = build_output()
    OUT_PATH.parent.mkdir(parents=True, exist_ok=True)
    with open(OUT_PATH, "w", encoding="utf-8") as f:
        json.dump(output, f, indent=2, ensure_ascii=False)
    print(f"Wrote {OUT_PATH}")
    print(f"  Total factions: {output['metadata']['totalFactions']}")
    print(f"  Fully detailed: {output['metadata']['fullyDetailedCount']}")
    print(f"  Briefs: {output['metadata']['briefCount']}")
    print(f"  File size: {OUT_PATH.stat().st_size:,} bytes")


if __name__ == "__main__":
    main()
