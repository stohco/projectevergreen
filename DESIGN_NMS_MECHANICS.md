# No Mortal Space — Reverse-Engineered Mechanics for Er Gen Verse

## Source
No Mortal Space (无妄红尘) is a xianxia survival/cultivation game with:
- Cultivation realms (Qi Condensation → Foundation → Core → Nascent Soul → ...)
- Spirit roots (aptitude determines cultivation speed)
- Dual cultivation partnerships
- Colossal beasts (boss encounters)
- Starship/space travel (late game)
- Realm breakthroughs with tribulation lightning (天劫)
- Survival pressure (hunger, thirst, qi depletion)
- Quests and exploration

## Adopted Mechanics for Er Gen Verse

### 1. Cultivation Survival (PRIORITY: HIGH)
- **Hunger**: player must eat (spirit fruit, beast meat, pills). Hunger
  debuff reduces qi regen by 50% at 0 hunger.
- **Thirst**: player must drink (spring water, dew, tea). Thirst debuff
  reduces movement speed by 30% at 0 thirst.
- **Qi Depletion**: qi naturally depletes at 0.5 qi/s (breathing). Combat
  and flight cost qi. Meditation regens qi at 3x rate.
- **Fatigue**: prolonged activity without rest reduces all stats by 10%
  per in-game hour. Rest (sleep in bed) resets fatigue.

### 2. Spirit Root System (PRIORITY: HIGH)
- 5 elements: Metal (金), Wood (木), Water (水), Fire (火), Earth (土)
- Plus Void (空) — rare, mod-original, for the player's unique path
- Aptitude 1-10 (10 = perfect single root = fastest cultivation)
- Multi-root cultivators cultivate slower but have more versatility
- Spirit root determines technique affinity (fire root → fire techniques
  are 2x effective, water techniques are 0.5x)

### 3. Dual Cultivation (PRIORITY: MEDIUM)
- Two cultivators can form a dual cultivation partnership (道侣)
- Both must consent (graph edge: ALLIED_WITH + meta.dual_cultivation = true)
- Dual cultivation triples qi regen for both when meditating together
- Canon: Wang Lin + Li Muwan (love interest, Luo He Sect)

### 4. Colossal Beasts (PRIORITY: HIGH)
- Boss-tier beasts spawn at canon-attested locations
- Sea of Devils (修魔海): sea beasts
- Snow Domain (雪域国): frost beasts
- Ancient Demon City (古魔城): demon beasts
- Beasts drop: beast core (灵兽内丹), spirit stones, rare materials
- Beast cores can be absorbed to boost cultivation (risky — backlash)

### 5. Starship Travel (PRIORITY: LOW — late game)
- Late-game cultivators (Nascent Soul+) can craft flying ships (飞舟)
- Ships enable travel between Planet Suzaku and other planets
- Canon: Wang Lin eventually leaves Planet Suzaku for the Sea of Devils
  star system, then the Immortal Astral Continent

### 6. Realm Breakthrough + Tribulation (PRIORITY: HIGH)
- When qi pool fills + enough karma accumulated → breakthrough attempt
- Breakthrough triggers tribulation lightning (天劫)
- Tribulation: 3 waves of lightning strikes, player must survive
- Success: realm advances, maxQi 4x, new techniques unlock
- Failure: cultivation deviation (走火入魔), qi pool halves, health -50%
- Canon: Wang Lin's breakthroughs are major novel events

### 7. Quest System (PRIORITY: MEDIUM)
- Graph-driven: GraphQueryService.naturalNext(npc) surfaces karmic edges
- Quests are NOT scripted — they emerge from the graph's karma structure
- Example: Wang Lin has KARMIC_DEBT to Teng Li → "Defeat Teng Li" quest
- Example: Li Muwan has FAMILIAR_WITH Wang Lin → "Find Li Muwan" quest
- Completing a quest resolves the karmic edge (write-back to graph)

## Graph Engineering Application

Every NMS mechanic is powered by the WorldGraph:

| Mechanic | Graph Query | Edge Types |
|----------|------------|------------|
| NPC spawning | whoExistsAt(location) | LOCATED_IN |
| Threat detection | threatsNearSettlement(loc) | HOSTILE_TO, NEAR |
| Dual cultivation | socialConnections(npc) | ALLIED_WITH, FAMILIAR_WITH |
| Quest generation | naturalNext(npc) | KARMIC_DEBT, GRUDGE |
| Item ownership | ownsWhat(npc) | OWNS |
| Technique knowledge | outEdges(npc, KNOWS) | KNOWS |
| Rumor propagation | traverse(npc, 2, FAMILIAR_WITH) | FAMILIAR_WITH |
| Beast spawns | nodesByType('npc') + meta.beast | — |

## OptMem for NPC Memory

Each NPC has a persistent memory log (OptMem concept):
- **wake**: on spawn, NPC reads recent memories (last N entries)
- **note**: on significant interaction (player talks, combat, quest)
- **recall**: NPC searches memory to drive dialogue and goals
- Memories are stored as MEMORY edges in the WorldGraph:
  `MEMORY: npc → event_node` with weight = importance

This gives NPCs persistent personality across save/load. Wang Lin
remembers if the player helped him or betrayed him. Teng Li remembers
if the player defeated him (and holds a grudge — GRUDGE edge).

## Canon Fidelity Notes

- Wang Lin starts as a mortal in a remote Zhao Country village (canon)
- Player starts as a Qi Condensation traveler (mod-original — player is
  NOT Wang Lin, player is a separate first-class actor)
- Teng Li (藤厉) is the young antagonist (canon, CRON-69 correction)
- Li Muwan is from Luo He Sect (洛河门, canon, CRON-69 correction)
- Situ Nan is 2nd-gen Zhuque-zi of Zhuque Country (canon, CRON-69 correction)
- Sea of Devils = 修魔海 (canon)
- Bead = 天逆珠 (canon)
- Snow Country = 雪域国 (canon)
- Suzaku is a PLANET, not a continent (canon, CRON-69 correction)
- NO invented chapter citations. All mechanics are genre-grounded
  interpretations of xianxia conventions attested in the novel.
