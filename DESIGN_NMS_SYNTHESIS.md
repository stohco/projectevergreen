# No Mortal Space ↔ Er Gen Verse — Systems Synthesis

## NMS Systems (researched from Steam, YouTube, Reddit)

### 1. Cultivation Progression
- **6 cultivation stages, each with 10 levels** (60 total levels)
- Start as a mortal survivor gathering wood and stone
- Evolve into an OverDeity who views civilizations as ants
- Each stage offers a "completely unique gameplay experience"
- Power fantasy progression — the world changes as you grow

### 2. Survival Crafting
- Voxel-based open world
- Gather resources (wood, stone, herbs, beast materials)
- Craft tools, weapons, formations, pills
- Build structures (huts, workshops, cultivation chambers)

### 3. Combat
- Action combat system (real-time, not turn-based)
- Sword techniques with visual effects
- Qi-based ranged attacks
- Beast combat (colossal bosses)
- Power scaling: mortal struggles vs. god-tier devastation

### 4. Flying / Sword Flight
- Cultivators can fly via sword (御剑飞行)
- Scale transitions: ground-level mortal → sky-traveling cultivator
- Aerial combat at higher realms
- Eventually: starship/space travel at OverDeity tier

### 5. Beast Ecology
- Colossal beasts roam the world
- Beasts have ecology (food webs, habitats, behaviors)
- Beast taming at higher cultivation
- Beast materials used for crafting

### 6. Dual Cultivation
- Partner with another cultivator
- Mutual qi cycling boosts both partners
- Canon xianxia mechanic (Wang Lin + Li Muwan)

### 7. Voxel Destruction
- "Level forests and shatter" with OverDeity power
- Terrain is destructible at high cultivation tiers
- World reshaping as a gameplay mechanic

## Er Gen Canon Data (from our JSONs)

### Available Data:
| Source | Entries | Content |
|--------|---------|---------|
| ri_canon_database.json | 632 | 160 chars + 80 locations + 178 artifacts + 214 techniques |
| ri_canon_beast_ecology.json | 21 species | Beast species with regions, food webs, canon confidence |
| ri_canon_herbs.json | 32 herbs | Spirit herbs with regions and contexts |
| ri_canon_spirit_veins.json | 9 veins | Spirit veins with tiers (minor/main/supreme) |
| ri_canon_techniques_enriched.json | 184 | Techniques with chapter learned, how learned, effects |
| ri_canon_artifacts_enriched.json | 177 | Artifacts with chapter obtained, chapter lost |
| ri_canon_factions_enriched.json | — | Sects and factions |
| ri_canon_characters_enriched.json | 160 | NPCs with relationships, realm, affiliation |

### Canon Realms (from REALMS_REFERENCE.ts):
Mortal → Qi Condensation → Foundation Establishment → Core Formation →
Nascent Soul → Soul Transformation → Ascendant → Illusory Yin →
Nirvana Scraper → Arcane → ... → Heaven Trampling

### Canon Zhao Country Herbs (from ri_canon_herbs.json):
- Qi-Gathering Grass (赵国) — mortal-tier, gathers ambient qi
- Foundation-Root Vine (赵国) — aids Foundation breakthrough
- Sword-Edge Moss (赵国) — sharpens sword qi

## Synthesis: NMS Mechanics × Er Gen Canon

### System 1: Cultivation Progression
**NMS**: 6 stages × 10 levels = 60 levels
**Er Gen**: 11+ realms (Mortal → Heaven Trampling) with sub-levels
**Synthesis**: Use Er Gen's canon realm system (not NMS's 6 stages). Each
realm unlocks new mechanics:
- Mortal: gathering, basic crafting, no qi
- Qi Condensation: sense qi, basic meditation, first techniques
- Foundation: sword flight, formation basics, pill refining
- Core Formation: combat techniques, beast taming
- Nascent Soul: domain formation, spatial manipulation
- Soul Transformation: terrain manipulation, tribulation survival
- Ascendant+: cosmic-scale power, voxel destruction, star travel

### System 2: Survival Crafting
**NMS**: gather wood/stone → craft tools
**Er Gen**: gather herbs (32 canon herbs) → refine pills (alchemy)
**Synthesis**: Use Er Gen's canon herb system. Zhao Country has:
- Qi-Gathering Grass (for Qi Condensation pills)
- Foundation-Root Vine (for Foundation breakthrough pills)
- Sword-Edge Moss (for sword qi enhancement)
Beast materials from 21 canon species → craft artifacts (178 canon items)

### System 3: Combat
**NMS**: action combat with sword techniques + qi ranged attacks
**Er Gen**: 214 canon techniques with effects, origin, how learned
**Synthesis**: Implement canon techniques as combat skills:
- Ji Realm Divine Sense (Ch. 127) — divine sense as a weapon
- Soul Piercing Eyes (Ch. 179) — offensive divine sense
- Mountain Crumble (Bai Fan, Ch. 1105) — formation creates physical mass
Each technique has a voxel geometry (NARROW_SLICE, EXPANDING_DOME, etc.)
per DESIGN_HITBOXES_AND_FORMATIONS.md

### System 4: Flying
**NMS**: sword flight at mid-tier, starship at OverDeity
**Er Gen**: 御剑飞行 (sword flight) at Foundation+, spatial travel at Ascendant+
**Synthesis**: Foundation realm unlocks sword flight (already implemented —
F key, costs 2 qi/sec). Ascendant+ unlocks spatial step (teleport).
Heaven Trampling unlocks star system travel.

### System 5: Beast Ecology
**NMS**: colossal beasts with ecology
**Er Gen**: 21 canon beast species with regions and food webs
**Synthesis**: Spawn canon beasts in their canon regions:
- Iron-Feathered Hawk (Zhao Country) — flying beast
- Stone-Backed Boar (Zhao Country) — ground beast
- Teng-Clan War Hound (Zhao Country) — Teng family beast
- Mosquito Beast (Wind Celestial Realm) — swarm beast
Beasts drop materials for crafting (beast core = 灵兽内丹)

### System 6: Dual Cultivation
**NMS**: dual cultivation partnerships
**Er Gen**: Wang Lin + Li Muwan (love interest, Luo He Sect)
**Synthesis**: Form dual cultivation partnership with an NPC. Both must
consent (graph edge: ALLIED_WITH + meta.dual_cultivation). Triples qi
regen when meditating together. Canon partner for player: Li Muwan
(from Luo He Sect, NOT Xuan Dao — CRON-69 correction).

### System 7: Voxel Destruction
**NMS**: OverDeity can "level forests and shatter"
**Er Gen**: Wang Lin remakes Planet Suzaku at Heaven Trampling
**Synthesis**: Terrain is smooth (RBF heightmap) for mortals. At Soul
Transformation+, the player can deform terrain (switch to voxel mode
for edited regions). At Heaven Trailing, the player can reshape
continents. Per DESIGN_HEAVEN_AND_EARTH_MANIPULATION.md: every object
has physical mass, spiritual mass, world law resistance. The player's
manipulation capability must exceed the object's resistance to change it.

## Math Foundations (frontier)

### Terrain (already implemented):
```
h(x,z) = Σᵢ aᵢ · φ(‖(x,z)−cᵢ‖) + r(x,z)
```
Wendland C² RBF with canon control points.

### Qi Field (to implement):
```
q(x,t) = scalar concentration
u(x,t) = flow vector field
∂q/∂t = -∇·(uq) + D∇²q + S(x,t) - R(x,t)
```
Where S = spirit vein sources, R = cultivator consumption, D = diffusion.

### Cultivation Breakthrough:
```
P(breakthrough) = f(qiPool, comprehension, spiritRoot, karma, tribulation survival)
```
Tribulation is a survival event: player must survive N lightning strikes
where damage scales with realm gap.

### Collision (implemented):
Ray-based: cast 8 rays + movement ray. Push back to hit surface.
Doorways naturally walkable (no mesh in gap). Closed doors block
(collidable = true, toggled on open).

### NPC Memory (OptMem port):
```
memory(npc, t) = wake(npc.log) + note(event) + recall(query)
```
Each NPC has an append-only log. GraphQueryService adds MEMORY edges.
NPCs recall memories to drive dialogue and AI goals.
