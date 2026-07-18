# Design Proposal — Divine Sense Dual-Mode + Technique Wheel + Terraforming

## The Problem

Divine Sense in Er Gen is not just a radar pulse. It's:
1. A **snapshot pulse** (press once, perceive everything in radius)
2. A **continuous active sense** (hold, keep it active, turn off on release)
3. A **manipulation force** (rip veins from underground, move mountains, lift objects)

All three already exist in our design docs and Next.js code, but I forgot #2 and #3 when porting to Java. This proposal ties them together.

---

## Part 1 — The Dual-Mode Divine Sense Button

### Click (snapshot pulse)
- **Already built.** `DivineSense.pulse()` returns a `PulseResult` (radius, reactions, perceived objects, soul-fracture risk).
- One frame of computation. No ongoing cost.
- This is the "I want to know what's around me RIGHT NOW" button.

### Hold (continuous active sense)
- Player holds the key down → divine sense stays active.
- Player releases → divine sense turns off.
- **Mechanically:** while held, the engine re-pulses every N ticks (configurable, default 20 ticks = 1 second). Each re-pulse:
  - Drains a small amount of S_sense (soul strain accumulates)
  - Updates the perceived-objects list (things that moved into/out of radius)
  - Re-checks NPC confrontation (new NPCs entering radius get confronted)
  - Renders a persistent overlay (HUD border tint, perceived entities highlighted)
- **Soul strain:** continuous mode drains S_sense at ~1% per second. If S_sense drops below 50% of max, the player gets the `soul_fracture` status (blinds divine sense, must brew Soul-Mending Elixir). This prevents infinite hold.
- **Why this works with the anti-lag formula:** each re-pulse is still a snapshot (O(registered objects in radius)), just repeated. At 1 pulse/second, that's 60 pulses/minute — trivially cheap.

### Implementation
```
KeyDown(G):
  if not held:
    pulse() // immediate snapshot
    startContinuousMode() // begin ticking

KeyTick(G held):
  every 20 ticks:
    pulse() // re-snapshot
    drainSoulStrain()

KeyUp(G):
  stopContinuousMode()
```

The continuous mode is just a scheduled re-pulse with a soul-strain cost. No new architecture — just a tick scheduler.

---

## Part 2 — The Technique Wheel (Hold G → Category → Sub-Technique → Release → Cast)

### The existing design (from docs/ergen-knowledge-base.md)
> Hold key → **Category Wheel** (Sword Arts · Body Arts · Movement · Divine Sense · Formations · Alchemy · Talismans · Artifacts · Flying Sword · Summons) → **Sub-Wheel** (the techniques in that category) → release → **cast**. Favorite techniques pin to a quick-access inner ring.

### The conflict: G is both "divine sense pulse" AND "technique wheel"

**Resolution:** G is the technique wheel key. The divine sense snapshot pulse is a technique *inside* the Divine Sense category. So:

- **Tap G (quick press < 200ms):** quick-cast the last divine sense technique used (default: Pulse). This is the "snapshot pulse" button — fast access without opening the wheel.
- **Hold G (> 200ms):** open the technique wheel. Mouse toward a category → sub-wheel appears → mouse toward a technique → release G → cast.

This gives both:
1. **Fast snapshot** (tap G) — what the user wants for the click-once pulse
2. **Full technique access** (hold G) — the wheel for all techniques including divine sense manipulation

### The Divine Sense sub-wheel techniques

When the player holds G and selects "Divine Sense," the sub-wheel shows:

| Technique | Realm Gate | Cost | What it does |
|---|---|---|---|
| **Pulse** (snapshot) | Qi Condensation | Small S_sense | The snapshot pulse we already built |
| **Continuous Sense** (hold) | Qi Condensation | S_sense/sec | The dual-mode hold — actually this is the "hold G" behavior itself, not a separate technique |
| **Earth Sense** | Foundation | Medium S_sense | Sense through solid ground — perceive underground formations/veins/caves within radius |
| **Vein Extraction** | Nascent Soul | Large S_sense + Qi | Rip a spirit vein directly from underground. Voxel factorization with `terraform` operator. The vein becomes a harvestable item. |
| **Object Lifting** | Foundation | S_sense + Qi | Telekinesis on a targeted block/entity. Move it with mouse aim. Sustained cost while holding. |
| **Mountain Moving** | Soul Formation | Massive S_sense + Qi | Lift and relocate a volume of physical blocks. Requires F_destruct >> R_voxel. The mountain moves; it is not destroyed. |
| **Killing Intent Detection** | Foundation | Passive | Detect hostile intent directed at you within radius. Passive — always on once learned. |
| **Soul Search** (forbidden) | Nascent Soul | S_sense + Karma | Steal a technique from an NPC's mind. High karma cost, heart-demon risk. Already built. |
| **Karma Vision** | Nascent Soul | S_sense + 20y cooldown | See karmic threads. Snapshot pulse with long cooldown. Already built. |
| **Spatial Crack Detection** | Transcendence | Massive S_sense | Find spatial cracks to other dimensions. Enables cross-branch travel. |

### Favorites
- Techniques the player marks as "favorite" pin to the inner quick-ring.
- The inner ring has ~8 slots. Mouse toward a favorite → release → cast. No need to drill into categories.
- The snapshot Pulse is a favorite by default.

---

## Part 3 — Smart Terraforming / Terrain Moving System

### The existing Voxel Factorization Engine (already built in soul.ts)

```
F_destruct = (B_base × P_player × C_tech × Q_artifact) / (S_eff + 1)²
R_voxel    = μ_mat × (1 + (σ_world × L_world) / 10)
If F_destruct >= R_voxel → blocks vaporize/terraform/freeze per the operator
```

- `B_base`: technique grade base (mortal=5, magical=15, spirit=50, immortal=200, dao=1000)
- `P_player`: absolute tier of the player's realm + 1
- `C_tech`: technique comprehension factor (1 - comprehensionDifficulty) × 1.5 + 0.5
- `Q_artifact`: artifact quality multiplier (1-3)
- `S_eff`: effective suppression = max(0, L_world - P_player) - suppressBypass
- `μ_mat`: material hardness (air=0, dirt=2, wood=3, stone=8, iron_ore=14, obsidian=25, bedrock=100, jade=30)
- `σ_world`: world law strength (fragile=0.3, low=0.6, medium=1.0, high=1.6, absolute=2.5)
- `L_world`: world law tier

### How this applies to divine sense terraforming

The block operator determines what happens to blocks caught in the technique's voxel geometry:
- `vaporize` (destruction intent) — blocks are destroyed
- `terraform` (speed/summoning/transformation intent) — blocks are **moved/reshaped**, not destroyed
- `conceal` (concealment intent) — blocks are hidden
- `freeze` (control intent) — blocks are frozen
- `seal` — blocks are sealed
- `purify` (protection/healing intent) — blocks are purified

For divine sense manipulation techniques, the operator is `terraform`. The difference between `vaporize` and `terraform`:
- `vaporize`: F_destruct >= R_voxel → block disappears (dropped as item or destroyed)
- `terraform`: F_destruct >= R_voxel × 2 (higher threshold) → block is moved to a target location, not destroyed

### Vein Extraction (rip spirit veins from underground)

**How it works:**
1. Player casts Vein Extraction (a divine sense technique, Nascent Soul+).
2. The technique targets the **Spiritual Layer** — specifically, `spirit_vein` objects in `ChunkLayerData.spiritualLayer`.
3. The engine finds the nearest spirit vein within the technique's radius.
4. The spirit vein has an `ObjectiveNature` with a `trueRank` (vein grade) and exists in the Spiritual Layer.
5. **Voxel factorization check:** `F_destruct` (player's divine sense force) vs `R_voxel` (the vein's spiritual hardness, derived from its rank and the world law).
6. If `F_destruct >= R_voxel × 2` (terraform threshold): the vein is extracted. It becomes a physical item (Spirit Vein Core) that the player can harvest. The Spiritual Layer entry is removed; the Physical Layer gets a "depleted vein" marker.
7. If `F_destruct >= R_voxel` but `< R_voxel × 2`: partial extraction. The vein is damaged but not fully extracted. Some Qi leaks to the surface (ambient Qi boost in the area for a time).
8. If `F_destruct < R_voxel`: failure. The player's divine sense rebounds — small S_sense drain, possible `soul_fracture` if the vein is much stronger.

**Why this feels like Er Gen:** Wang Lin rips veins from the earth with his divine sense. The vein was always there (in the Spiritual Layer). The player couldn't touch it at mortal tier. At Nascent Soul, they can — if their divine sense is strong enough. The vein doesn't "spawn" when the player levels up; it was always there.

### Mountain Moving (telekinetic terrain manipulation)

**How it works:**
1. Player casts Mountain Moving (Soul Formation+).
2. The player targets a volume of physical blocks (mountain) by aiming at it.
3. The engine computes the total `R_voxel` for the volume: sum of all blocks' `μ_mat × (1 + (σ_world × L_world) / 10)`.
4. **Voxel factorization check:** `F_destruct` vs total `R_voxel × 2` (terraform threshold for a volume).
5. If success: the entire volume is lifted. The player can then aim where to place it. The blocks move as a group (not individually — this is one operation). On placement, the blocks settle into the new location.
6. The cost is massive: large Qi drain + S_sense drain. The player cannot do this casually.
7. **Consequences:** moving a mountain reshapes the world. Ecosystems shift (the valley the mountain was in is now exposed; the new location is buried). Sects notice. NPC fishermen panic if the mountain was near the sea. World Pulse events fire.

**Scale gating:** the `scale` field on the technique determines the maximum volume. A Soul Formation technique might move a hill. An Ascendant technique moves a mountain. A Transcendent technique moves a mountain range.

### Object Lifting (targeted telekinesis)

**How it works:**
1. Player casts Object Lifting (Foundation+).
2. Player targets a single block or entity (not a volume).
3. The block/entity is lifted and follows the player's aim.
4. **Sustained cost:** Qi + S_sense drain per tick while held.
5. On release: the object drops (physics) or is placed (if the player chooses to place it).
6. **Voxel factorization:** single-block `F_destruct` vs `R_voxel`. Foundation can lift dirt/wood. Nascent Soul can lift stone. Soul Formation can lift obsidian. Bedrock is immune (μ_mat=100 — only Transcendence can lift bedrock, and even then it's a `vaporize` not a `terraform`).

---

## Part 4 — The Full Technique Wheel Integration

### All 10 categories and their block operators

| Category | Primary Block Operator | Example Techniques |
|---|---|---|
| **Sword Arts** | `vaporize` / `shatter` | Flying Sword Strike, Heaven-Slashing Sword, Sword Domain |
| **Body Arts** | `terraform` (self) | Body Tempering, Iron Skin, Blood Qi Surge |
| **Movement** | `terraform` (self-position) | Flying Sword Movement, Wind Step, Cloud Sprint |
| **Divine Sense** | `terraform` / `conceal` / `purify` | Pulse, Earth Sense, Vein Extraction, Mountain Moving, Object Lifting, Soul Search, Karma Vision |
| **Formations** | `seal` / `freeze` | Sect-Protecting Array, Restriction Formation, Transport Array |
| **Alchemy** | `purify` (items, not blocks) | Pill Refinement, Herb Extraction, Living Pill (Bai Xiaochun) |
| **Talismans** | `conceal` / `ignite` | Talisman of Warding, Fire Talisman, Concealment Talisman |
| **Artifacts** | `vaporize` / `shatter` | Sentient Sword Strike, Artifact Overclock, Soul Brand Crack |
| **Flying Sword** | `vaporize` (in flight path) | Cruise Flight, Sprint Burst, Sword Rain |
| **Summons** | `terraform` (spawn point) | Beast Summon, Spirit Manifestation, Ancestor Call |

### The wheel UI in Minecraft

**Hold G → Category Wheel appears (radial, 10 sectors):**
```
        [Sword]
   [Summons]   [Body]
[Artifacts]         [Movement]
[Flying]      G      [Divine Sense]
[Sword]             [Formations]
   [Talismans]  [Alchemy]
        [???
```

**Mouse toward "Divine Sense" → Sub-Wheel appears (radial, techniques in that category):**
```
        [Pulse]
  [Karma Vision]  [Earth Sense]
[Spatial Crack]        [Vein Extract]
[Soul Search]    G    [Object Lift]
  [Mountain Move]  [Killing Intent]
        [Continuous]
```

**Inner ring (favorites):** up to 8 favorited techniques from any category, accessible without drilling.

**Release G → cast the highlighted technique.**

### Implementation in Forge

The wheel is a `Screen` subclass:
- `keyPressed(G)` → open the screen
- The screen renders the radial wheel using `DrawContext`
- Mouse position determines the highlighted sector
- `keyReleased(G)` → close the screen and cast the highlighted technique

For the dual-mode divine sense:
- If G is tapped (< 200ms) and the last technique used was a divine sense technique → quick-cast it (the snapshot pulse)
- If G is held (> 200ms) → open the wheel

This means the snapshot pulse is accessible two ways:
1. Tap G (quick-cast last divine sense technique — defaults to Pulse)
2. Hold G → Divine Sense → Pulse → release (full wheel access)

---

## Part 5 — What I Need to Build (in priority order)

1. **Update DivineSense.java** to support dual-mode (snapshot + continuous hold)
2. **Build the TechniqueWheel screen** (Forge `Screen` subclass, radial UI, G keybind)
3. **Port the Voxel Factorization Engine** to Java (F_destruct / R_voxel / block operators)
4. **Build the divine sense manipulation techniques:**
   - Vein Extraction (targets Spiritual Layer spirit veins, terraform operator)
   - Mountain Moving (targets Physical Layer volumes, terraform operator, massive cost)
   - Object Lifting (targets single blocks/entities, sustained cost)
   - Earth Sense (perceive through ground, Spiritual Layer underground)
5. **Build the technique registry** — all techniques across all 10 categories, with their voxel geometry, block operator, scale, and cost
6. **Wire the voxel factorization into block interaction** — when a technique is cast, compute F_destruct vs R_voxel for the targeted blocks, apply the operator

### What I will NOT do
- I will not make divine sense techniques "spawn" things. Veins exist in the Spiritual Layer whether or not the player can perceive them. Vein Extraction pulls an existing vein into the Physical Layer; it does not create one.
- I will not make Mountain Moving a "teleport the blocks" operation. The blocks move through space (with a visible animation), and the world responds (ecology shifts, NPCs react, World Pulse fires).
- I will not forget the Voxel Factorization Engine. It already has the `terraform` operator. I just need to port it and apply it to divine sense techniques.
