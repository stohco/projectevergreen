# Design Proposal — Heaven and Earth Manipulation System

## The Correction

**G is exclusively the technique wheel.** Divine sense snapshot/hold is a separate key (let's say `V` for Vision/perception, configurable). The two systems are distinct:

- **V key** = Divine Sense (tap = snapshot pulse, hold = continuous active sense)
- **G key** = Technique Wheel (hold → category → sub-technique → release → cast)

Divine sense manipulation techniques (Vein Extraction, Object Lifting, etc.) live INSIDE the technique wheel under the "Divine Sense" category. But the raw perception pulse/hold is its own key.

---

## Part 1 — The Universal Object Properties

Per your suggestion: every object in the world — from a pebble to a continent — has the same underlying properties. This is the foundation of the Heaven and Earth Manipulation system.

```java
public final class WorldObject {
    // ─── Physical properties ───
    public double physicalMass;          // weight/volume in kg-equivalent
    public String material;              // dirt, stone, obsidian, jade, spirit_stone, bedrock...

    // ─── Spiritual properties ───
    public double spiritualMass;         // spiritual weight (a spirit vein: high; a dirt block: 0)
    public boolean hasSoul;              // does this object have a soul? (mountains, ancient trees, spirit beasts)
    public String soulNature;            // if hasSoul: "mountain soul", "river spirit", "ancient tree will"

    // ─── Dao properties ───
    public double daoAnchoring;          // how deeply is this object tied to a Dao? (0 = none, 1 = pure Dao manifestation)
    public String daoAffinity;           // which Dao? (earth, water, fire, seal, karma...)
    public double historicalDaoImprint;  // has a powerful cultivator imprinted this? (0 = no, 1 = Paragon-level imprint)

    // ─── World properties ───
    public double worldLawResistance;    // how hard the world's laws resist changing this object (0-1)
    public double heavenlyResistance;    // does the world's heaven actively resist? (0 = no, 1 = sealed world resists)

    // ─── Ownership properties ───
    public String ownerId;               // null = unowned; otherwise sect/NPC/protagonist id
    public double formationAnchoring;    // is this object anchored by a formation? (0 = no, 1 = sect-protecting array)

    // ─── Karmic properties ───
    public double karmicSignificance;    // 0 = ordinary pebble, 1 = Wang Lin's birthplace
    public String karmicNote;            // why is this karmically significant?
}
```

### Examples

| Object | Physical Mass | Spiritual Mass | Dao Anchoring | World Law Res. | Formation Anchoring | Karmic Sig. |
|---|---|---|---|---|---|---|
| A pebble | 0.1 kg | 0 | 0 | 0.1 | 0 | 0 |
| A dirt block | 1 kg | 0 | 0 | 0.1 | 0 | 0 |
| A stone block | 8 kg | 0 | 0 | 0.2 | 0 | 0 |
| An ancient tree | 500 kg | 10 | 0.3 (wood dao) | 0.3 | 0 | 0.1 |
| A spirit vein | 0 (spiritual) | 500 | 0.7 (earth dao) | 0.5 | 0 | 0.2 |
| A mountain | 10^9 kg | 100 | 0.4 (earth dao) | 0.6 | 0 | 0.1 |
| A sect's mountain | 10^9 kg | 100 | 0.4 | 0.6 | 0.8 (sect array) | 0.3 |
| Wang Lin's birthplace | 10^6 kg | 50 | 0.2 | 0.4 | 0 | 1.0 |
| Bai Fan's Mountain Crumble imprint | 10^9 kg | 500 | 0.9 | 0.8 | 0 | 0.7 |

---

## Part 2 — The "Can I Move This?" Comparison

### Player Capability (the "can I" side)

```java
public final class ManipulationCapability {
    public double telekineticForce;     // f(realm, currentQi) — raw power
    public double divineSenseStrength;  // S_sense — perception/precision
    public double daoCompatibility;     // how well the player's Dao matches the object's daoAffinity
    public double techniqueMultiplier;  // the technique's power (grade × comprehension)
    public double treasureBonus;        // artifacts that boost manipulation
    public double formationBonus;       // if standing in a helpful formation
}
```

### The Comparison

```
Player Capability = telekineticForce + divineSenseStrength + daoCompatibility
                    + techniqueMultiplier + treasureBonus + formationBonus

Object Resistance = physicalMass + spiritualMass + daoAnchoring
                    + worldLawResistance + formationAnchoring + ownerResistance
                    + historicalDaoImprint + heavenlyResistance + karmicSignificance

If Capability > Resistance        → success (clean move)
If Capability > Resistance × 0.7  → partial (barely budges, or fractures)
If Capability ≤ Resistance × 0.7  → failure (backlash: Qi drain, divine sense strain, possible soul fracture)
```

### Why this preserves Er Gen's exceptional protagonists

An **ordinary Soul Formation cultivator** trying to move a mountain:
- telekineticForce: ~30,000 (Soul Formation S_realm)
- divineSenseStrength: ~30,000
- daoCompatibility: ~0.3 (if they have some Earth Dao)
- techniqueMultiplier: ~2 (spirit-grade technique)
- treasureBonus: 0
- formationBonus: 0
- **Total: ~63,000**

Mountain Resistance:
- physicalMass: 10^9 kg equivalent (scaled to ~50,000 in resistance units)
- spiritualMass: 100
- daoAnchoring: 0.4 × 1000 = 400
- worldLawResistance: 0.6 × 1000 = 600
- **Total: ~51,000**

Result: 63,000 > 51,000 → the ordinary Soul Formation **barely** moves a modest mountain. Success, but costly.

An **exceptional Soul Formation cultivator** with a legendary earth-moving technique and a treasure:
- telekineticForce: ~30,000
- divineSenseStrength: ~50,000 (highly tempered soul)
- daoCompatibility: ~0.9 (deep Earth Dao comprehension)
- techniqueMultiplier: ~10 (dao-grade technique)
- treasureBonus: ~5,000 (restriction flag)
- **Total: ~130,000**

Result: 130,000 > 51,000 × 2 → **clean move**, relocates the mountain with room to spare.

A **Third Step cultivator**:
- telekineticForce: ~1,000,000
- **Total: ~1,000,000+**

Result: can move mountain ranges.

A **Fourth Step cultivator**:
- telekineticForce: ~100,000,000
- **Total: ~100,000,000+**

Result: can alter the geography of a world.

**No hardcoded realms.** The equation handles it naturally. An exceptional lower-realm cultivator with the right technique and treasure can outperform an ordinary higher-realm cultivator — exactly as Er Gen writes it.

---

## Part 3 — The Three Manipulation Types

Per your analysis of Er Gen's examples, there are three distinct kinds of mountain interaction. They are NOT interchangeable. A cultivator strong in one may be weak in another.

### Type 1: Physical Manipulation

**What it is:** moving the physical mass of an object. Brute force telekinesis.

**Er Gen examples:**
- Lifting a boulder with divine sense
- Moving a mountain by raw power

**What it requires:**
- Telekinetic Force > Physical Mass + World Law Resistance
- Current Qi must sustain the lift
- Divine Sense provides precision (otherwise the object crumbles)

**What it does NOT require:**
- Dao compatibility (you can move a mountain without Earth Dao if you're strong enough)
- Specialized methods (raw power works)

**Failure mode:** the object barely budges, or partially fractures (pieces break off). Qi drains rapidly. No spiritual consequence.

### Type 2: Spiritual Manipulation

**What it is:** interacting with the spiritual essence of an object — extracting spirit veins, moving mountain souls, awakening mountain spirits.

**Er Gen examples:**
- Greed extracting a mountain's soul (Second Step associated)
- Wang Lin extracting spirit veins
- Awakening a dormant mountain spirit

**What it requires:**
- Divine Sense Strength > Spiritual Mass + Formation Anchoring
- Specialized method (a technique that interacts with souls/spirits — NOT raw power)
- Perception (you must perceive the spiritual essence first)

**What it does NOT require:**
- Raw telekinetic force (you're not lifting the physical mountain)
- High Qi (the cost is divine sense strain, not Qi)

**Failure mode:** the spiritual essence slips away, or the object's soul resists (a mountain spirit may attack). Divine sense strain accumulates. If the object has a formation anchor (sect's mountain), the formation may alert its owner.

**Key insight:** raw cultivation alone does NOT grant spiritual manipulation. A Soul Formation brute-force cultivator with no perception training cannot extract a spirit vein. A Nascent Soul cultivator with deep divine sense and the right technique can. This is why Wang Lin (with his Heaven-Defying Bead's perception) could do spiritual manipulation early.

### Type 3: Dao Manipulation

**What it is:** transforming the object's fundamental nature — Mountain Crumble, mountain becomes treasure, mountain becomes formation, mountain becomes seal.

**Er Gen examples:**
- Wang Lin's restriction mountain (early First Step — used restriction techniques to shrink/refine a mountain into a portable treasure)
- Bai Fan's Mountain Crumble (treats the mountain as having a body AND a soul; highest level forms a mountain from Divine Sense itself)
- Grand Empyrean Song Tian's Origin Mountain (refined into a treasure whose suppression overwhelms laws)

**What it requires:**
- Dao Compatibility > Dao Anchoring + Historical Dao Imprint
- Deep comprehension of the relevant Dao (Earth Dao for mountains, Water Dao for oceans)
- A technique that transforms (NOT brute force, NOT just perception)

**What it does NOT require:**
- Raw telekinetic force (you're not moving the mountain — you're changing what it IS)
- High divine sense (the transformation is Dao-driven, not perception-driven)

**Failure mode:** the Dao rebells. The object resists transformation. Backlash: Dao heart wavers, comprehension may drop, heart demon risk. If the object has a Historical Dao Imprint (Bai Fan's imprint), the imprint's creator may notice you.

**Key insight:** this is how Wang Lin shrank a mountain at early First Step. His restriction technique (a Dao method) was strong enough to overcome the mountain's Dao anchoring, even though his raw power was far below "mountain moving" tier. Technique + Dao > brute force.

### The three types are independent

A cultivator might be:
- **Strong Physical, weak Spiritual:** can lift a mountain but can't extract its vein
- **Strong Spiritual, weak Physical:** can perceive and extract the vein but can't lift the rock
- **Strong Dao, weak Physical:** can transform the mountain into a seal but can't literally move it
- **All three:** a true master (Wang Lin at Transcendence)

This creates a rich interaction space. The player chooses their approach based on their strengths.

---

## Part 4 — The Shape System (Engineered Suggestions)

You asked me to engineer suggestions for how the player adjusts the shape of what they're lifting/moving. The challenge: the player should be able to "grab what they want, how much they want, how it's shaped" — not just mining, but breaking ground with technique-shaped patterns.

### Suggestion A: Technique-Determined Shape (the "Dao Hand")

**How it works:** The shape of your selection is determined by your technique's voxel geometry. Your Dao forms a virtual "hand" whose shape matches your path:
- **Sword Dao** → thin slice (NARROW_SLICE) — you cut a thin plane of blocks
- **Earth Dao** → vertical cylinder (DESCENDING_CYLINDER) — you grip a column of earth
- **Fire Dao** → radial burst (RADIAL_BURST) — you grab a spherical volume
- **Seal Dao** → locked chunk (LOCKING_CHUNK) — you freeze a cube of space
- **Wood Dao** → rising pillar (RISING_PILLAR) — you lift a growing column
- **Water Dao** → tidal wave (TIDAL_WAVE) — you sweep a horizontal layer
- **Divine Sense Dao** → expanding dome (EXPANDING_DOME) — you perceive and grip a growing sphere

**Scroll wheel:** adjusts SCALE (how big the hand is). One scroll = +1 block radius/height.

**Why this is Er Gen:** your Dao literally shapes how you interact with the world. A sword cultivator and an earth cultivator grab mountains differently — because their Daos are different. This is the deepest integration of the Dao system into gameplay.

**Pros:** deeply thematic, ties shape to the cultivator's path, no separate UI needed
**Cons:** less precise control — you can't choose an arbitrary shape, only your Dao's shape

### Suggestion B: Preset Shape Palette (the "Formation Seal")

**How it works:** The player has a palette of preset shapes (cube, sphere, cylinder, cone, disk, wedge, line, cross). A hotbar-like UI (or a sub-wheel) lets them pick the shape. Scroll wheel adjusts scale.

**Why this is Er Gen:** the shapes are "formation seals" — the cultivator imprints a seal pattern on reality, and the matching volume is selected. This is how Wang Lin's restriction techniques work (he imposes a pattern, reality obeys).

**Pros:** precise control, familiar UI pattern, can choose any shape regardless of Dao
**Cons:** less thematic — a fire cultivator can use a cube shape, which feels less Dao-integrated

### Suggestion C: Two-Point Bounding Box (the "Restriction Frame")

**How it works:** The player clicks two corners to define a bounding box (like Minecraft's structure block). The box can be resized with scroll wheel on each axis. The selected volume is what gets moved.

**Why this is Er Gen:** this is exactly how Wang Lin's restriction mountains work — he defines a restriction frame around the mountain, then lifts the frame (with the mountain inside it).

**Pros:** very precise, handles arbitrary shapes (any bounding box)
**Cons:** slow to set up, feels more like a building tool than a cultivation technique

### Suggestion D: Freeform Sculpting (the "Divine Sense Hand")

**How it works:** The player enters a "sculpt mode" (hold the technique key). A ghostly hand appears at the crosshair. The player moves the mouse to "paint" the selection — each block the hand passes over is added to the selection. Scroll adjusts hand size. A separate key cycles the hand between "add" and "remove" mode (to carve out unwanted blocks from the selection).

**Why this is Er Gen:** the cultivator's divine sense literally shapes the selection by touch. Wang Lin's divine sense is described as a hand that can grip, tear, and shape — this is that, mechanically.

**Pros:** maximum flexibility, feels like manual cultivation, very immersive
**Cons:** complex to implement, slow for large volumes

### My Recommendation: Hybrid (A + D)

**Primary shape: Technique-Determined (Suggestion A).** The default shape matches your Dao — a sword cultivator cuts slices, an earth cultivator grips cylinders. This is the default and requires no extra input. Scroll adjusts scale.

**Refinement mode: Freeform Sculpting (Suggestion D).** Hold a modifier key (e.g., Shift) while in manipulation mode to enter "refine" mode. The divine sense hand appears, and you can paint-add or paint-remove blocks from the selection. This lets the player carve out exactly what they want — "I want THIS part of the mountain, but not THAT part."

**Why this hybrid:**
1. By default, your Dao shapes your interaction (thematic, fast)
2. When you need precision, you switch to refine mode (flexible, immersive)
3. The scroll wheel always controls scale (one universal control)
4. No menu diving — everything is done in-world with the crosshair

### The Controls (full mapping)

| Input | Action |
|---|---|
| Hold V | Divine sense continuous active (perception only) |
| Tap V | Divine sense snapshot pulse |
| Hold G | Open technique wheel → select manipulation technique → release to cast |
| Scroll wheel | Adjust scale of the manipulation (bigger/smaller selection) |
| Shift + hold cast key | Enter refine mode (freeform sculpting with divine sense hand) |
| Left click (in refine mode) | Add blocks to selection |
| Right click (in refine mode) | Remove blocks from selection |
| Middle click (in refine mode) | Cycle hand shape (for when you want a different shape than your Dao default) |
| R | Rotate the selection (for non-symmetric shapes) |
| Enter / Left click (cast) | Execute the manipulation (move/extract/transform) |
| Esc | Cancel the manipulation |

---

## Part 5 — The "Move" vs "Break" Distinction

You said: "instead of mining the ground, I want to be able to break it with my techniques, in whatever erratic way my techniques are shaped."

This is a key insight. The manipulation system is NOT mining. Mining is: target one block, break it, get the drop. Manipulation is: target a VOLUME (shaped by your technique), and do one of three things:

1. **Move** — relocate the entire volume to a new position (Physical manipulation, terraform operator)
2. **Break** — shatter the volume along your technique's shape (Physical manipulation, vaporize/shatter operator). The blocks break, but in the SHAPE of your technique — a sword cut leaves a thin slice, a fire burst leaves a crater.
3. **Extract** — pull the spiritual essence out of the volume (Spiritual manipulation). The physical blocks remain; the spirit vein/soul is pulled free.
4. **Transform** — change the nature of the volume (Dao manipulation). Mountain becomes seal. Stone becomes jade. Dirt becomes spirit soil.

The operator (vaporize/terraform/conceal/freeze/seal/shatter/ignite/purify) determines which of these happens. The shape (voxel geometry) determines the footprint. The scale (scroll wheel) determines the size. The manipulation type (physical/spiritual/dao) determines which capability/resistance comparison is used.

### "Erratic" shapes

You mentioned wanting techniques to break ground "in whatever erratic way my techniques are shaped." This is already handled by the voxel geometry system — but we can extend it:

- **Chaotic techniques** (slaughter dao, void dao) get irregular geometries — the shape is randomized within the technique's footprint. A slaughter-dao sword cut doesn't leave a clean slice; it leaves a jagged, cracked wound in the earth.
- **Ordered techniques** (formation dao, seal dao) get clean geometries — perfect cubes, precise cylinders.
- **Natural techniques** (wood dao, water dao) get organic geometries — branching patterns, flowing curves.

This means a slaughter cultivator and a formation cultivator interact with the ground VERY differently, even at the same power level. The slaughter cultivator's "break" is erratic and destructive. The formation cultivator's "break" is clean and architectural.

---

## Part 6 — Implementation Plan

### Phase 1: Core system
1. **`WorldObject` class** — the universal object properties (physical mass, spiritual mass, dao anchoring, etc.)
2. **`ManipulationCapability` class** — the player's capability (telekinetic force, divine sense, dao compatibility, technique, treasure, formation)
3. **`HeavenAndEarthManipulation` engine** — the comparison equation. Three methods: `attemptPhysical()`, `attemptSpiritual()`, `attemptDao()`. Each returns a `ManipulationResult` (success/partial/failure + consequences).
4. **`SelectionShape` system** — technique-determined shapes (Suggestion A) + scroll-scale adjustment + refine mode (Suggestion D)

### Phase 2: Techniques
5. **Port the technique wheel** — 10 categories, sub-wheels, favorites ring. G key.
6. **Implement the 3 manipulation types as techniques:**
   - Physical: Object Lifting, Mountain Breaking (vaporize), Mountain Moving (terraform)
   - Spiritual: Vein Extraction, Soul Extraction, Spirit Awakening
   - Dao: Mountain Crumble, Mountain-to-Seal, Mountain-to-Treasure
7. **Implement the shape system** — each technique has a voxel geometry; scroll adjusts scale; Shift enters refine mode

### Phase 3: World integration
8. **Attach `WorldObject` properties to every block/entity** — blocks get physical mass + material; spirit veins get spiritual mass; mountains get dao anchoring; sect blocks get formation anchoring; protagonist-related blocks get karmic significance
9. **Wire the comparison into block interaction** — when a manipulation technique is cast, compute capability vs resistance, apply the result
10. **Failure consequences** — backlash (Qi drain, divine sense strain, soul fracture, Dao heart wavering, heart demon risk)

### What I will NOT do
- I will NOT hardcode "Soul Formation = can move mountains." The equation handles it.
- I will NOT make the three manipulation types interchangeable. They are distinct.
- I will NOT make the shape arbitrary. It's tied to your Dao (with refine mode for precision).
- I will NOT conflate G (technique wheel) with V (divine sense) again.
