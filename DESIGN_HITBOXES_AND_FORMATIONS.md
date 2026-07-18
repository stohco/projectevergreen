# Design Proposal — Attack Hitbox Matching Animation + Formation/Talisman System

## Part 1 — Divine Sense in Physical Manipulation (CORRECTED)

### The Er Gen evidence

From the cached Wang Lin techniques wiki data:
- **Ji Realm Divine Sense** (Ch. 127) — Wang Lin's divine sense IS a weapon. It's not "precision" — it's a destructive force in its own right.
- **Soul Piercing Eyes (Divine Sense Eyes)** (Ch. 179) — divine sense used offensively, piercing targets.
- **Restriction Flags** — Wang Lin grips, moves, and wields formation flags WITH his divine sense. The flag is the tool; divine sense is the hand.
- **Mountains Crumble** (Bai Fan's spell, Ch. 1105) — forms a mountain from Divine Sense and celestial energy, then shatters it. Divine sense literally CREATES the physical mass here.

### The correction

My previous formula had divine sense at only 10% (just "precision"). That's wrong. In Er Gen, **divine sense IS the gripping force** — it's the "hand" that grips and moves objects. Qi/realm provides the raw power behind the grip, but divine sense is the mechanism.

**New formula (already applied to `ManipulationCapability.physicalCapability()`):**
```
physicalCapability = telekineticForce × (1 + technique)      // raw power
                   + divineSenseStrength × (1 + technique)    // the gripping force (EQUAL weight)
                   + treasureBonus + formationBonus
                   + telekineticForce × daoCompat × 0.3
                   + divineSenseStrength × daoCompat × 0.5    // Dao matters more for the grip
```

Divine sense now has EQUAL weight to telekinetic force in physical manipulation. This means:
- A cultivator with high divine sense but lower realm (Wang Lin with soul-eating) can manipulate objects effectively
- The S_tempering path (soul-eating) directly boosts physical manipulation
- The Ji Realm Divine Sense is correctly modeled as a manipulation force, not just "precision"

---

## Part 2 — Attack Hitboxes Matching Animations

### The problem

You said: "if I'm doing a slashing move that has sword effects that goes in a horizontal way, the thing being cut isn't going to be cut vertically."

This is about the **voxel geometry** system. We already have 9 geometries (NARROW_SLICE, DESCENDING_CYLINDER, EXPANDING_DOME, RADIAL_BURST, LOCKING_CHUNK, RISING_PILLAR, TIDAL_WAVE, STARFALL_CONE, FROZEN_FIELD). The problem: these geometries don't currently encode ORIENTATION. A NARROW_SLICE could be horizontal, vertical, or diagonal — and the hitbox must match the animation.

### The solution: oriented voxel geometry

Each technique has THREE properties that together define the hitbox:

1. **VoxelGeometry** — the SHAPE (slice, cylinder, dome, burst, chunk, pillar, wave, cone, field)
2. **Orientation** — the PLANE/AXIS the shape is aligned to
3. **Direction** — where the technique travels (from caster toward target)

### The Orientation enum

```java
public enum VoxelOrientation {
    HORIZONTAL,     // flat — parallel to the ground (a horizontal sword slash)
    VERTICAL,       // upright — perpendicular to the ground (a vertical cleave)
    DIAGONAL_RISING,// diagonal, rising from left to right
    DIAGONAL_FALLING,// diagonal, falling from left to right
    OMNI            // symmetric — orientation doesn't matter (spheres, cubes)
}
```

### How it works per geometry

| Geometry | Orientation matters? | Example |
|---|---|---|
| NARROW_SLICE | YES — horizontal slice vs vertical slice are completely different | Horizontal sword slash cuts a wide flat plane; vertical cleave cuts a tall thin plane |
| DESCENDING_CYLINDER | No (always vertical) | Earth technique — column going down |
| EXPANDING_DOME | No (symmetric) | Divine sense pulse — sphere |
| RADIAL_BURST | No (symmetric) | Fire explosion — sphere |
| LOCKING_CHUNK | No (cube, but could orient) | Seal technique — frozen cube |
| RISING_PILLAR | No (always vertical) | Wood technique — column going up |
| TIDAL_WAVE | YES — horizontal wave vs vertical wave | Water sweep is horizontal; a tsunami could be vertical |
| STARFALL_CONE | YES — cone direction matters | Light from above (downward cone) vs light from below (upward cone) |
| FROZEN_FIELD | No (flat) | Ice — flat frozen area |

### The animation must match

When a technique is cast:
1. The technique's `voxelGeometry` + `orientation` + `direction` define the hitbox
2. The animation system renders the visual effect in the SAME shape and orientation
3. The damage applies ONLY to blocks/entities in the hitbox

**Example — horizontal sword slash:**
- Geometry: NARROW_SLICE
- Orientation: HORIZONTAL
- Direction: from player forward
- Animation: a horizontal sword arc effect, sweeping left-to-right at chest height
- Hitbox: a thin horizontal plane, 1 block tall, 5 blocks wide, 3 blocks deep
- A tree in front of the player gets cut HORIZONTALLY (the trunk slices at chest height, not vertically split)

**Example — vertical sword cleave:**
- Geometry: NARROW_SLICE
- Orientation: VERTICAL
- Direction: from player forward and down
- Animation: a vertical sword slash, top-to-bottom
- Hitbox: a thin vertical plane, 5 blocks tall, 1 block wide, 3 blocks deep
- A tree in front gets cut VERTICALLY (split down the middle, not sliced at chest height)

### Implementation

The technique definition gets two new fields:
```java
public class Technique {
    public VoxelGeometry voxelGeometry;   // shape
    public VoxelOrientation orientation;  // plane/axis
    public Direction direction;            // travel direction (Forge's Direction enum)
    public BlockOperator blockOperator;   // what happens to hit blocks
    // ...
}
```

The hitbox computation:
```java
public List<BlockPos> computeHitbox(Technique tech, BlockPos origin, Vec3 aim) {
    // 1. Get the base shape from voxelGeometry + scale
    // 2. Orient it per orientation (rotate the shape)
    // 3. Position it per direction + aim
    // 4. Return the list of affected block positions
}
```

The animation:
```java
public void renderTechnique(Technique tech, Vec3 origin, Vec3 aim) {
    // Render the visual effect in the SAME shape and orientation as the hitbox
    // The animation IS the hitbox visualization
}
```

### Why this matters

This makes combat feel real. A horizontal slash and a vertical cleave are DIFFERENT attacks with different hitboxes, even if they're both "sword techniques." The player can choose:
- Horizontal slash → hit multiple enemies in a line, cut a tree at chest height
- Vertical cleave → hit one enemy hard, split a tree down the middle
- Diagonal rising → hit enemies from low to high, cut a tree at an angle

The animation tells you what the hitbox is. No more "I slashed horizontally but the block broke vertically."

---

## Part 3 — Formation and Talisman System

### The Er Gen source material (from cached wiki + catalog)

From the existing `formation-talisman-catalog.ts` (100 entries) and the wiki research:

**Formations** have these types: defensive, offensive, trapping, transport, sealing, surveillance, illusion, soul, alchemy_aux, hybrid. Grades: mortal → spirit → earth → heaven → dao → immortal.

**Key canon examples:**
- Wang Lin's **Restriction Flag** (禁幡) — a portable formation-flag artifact. Embeds restriction matrices, seals regions, suppresses enemies. Doubles as storage + portable restriction matrix.
- **Restriction Flag Method** (禁旗之法) — the cultivation method to wield Restriction Flags.
- The **4 Great Restrictions**: Annihilation, Time, Life-Death, Destruction (all Dao-grade).
- **Heng Yue Sect Protecting Array** — outer perimeter + spirit-gathering + heart-array core flag layered matrix.
- **Six Cultivation Planets Restriction** — seals 6 planets at once.
- **Sect-Protecting Array** (universal) — every sect has one. Layered defense with spirit-vein power source, flag-based perimeter, heart-array core.
- Bai Xiaochun's **Living Formations** — formations that gain sentience (parallel to his Living Pills).
- **Formation flags** — physical objects placed at formation nodes (from the noveltranslations forum: "Some would have formation disks or flags at each of the corners to form it").

**Talismans** have these types: life_lantern, jade_slip, formation_flag, soul_banner, sealing_stamp, mirror, paper_talisman, pearl_bead, cauldron_wok, coffin, bell, compass, mask, other.

**Key canon examples:**
- **Heaven-Avoiding Coffin** (避天棺) — hides occupant from heavenly tribulation and divine sense.
- **Soul-Storing Jade Slip** (储魂玉简) — brands knowledge via divine sense.
- **Soul Banner** (Wang Lin's Billion Soul Flag) — stores and refines souls.
- **Sealing Stamp** — various sealing talismans.
- **Paper talismans** — one-use consumable talismans (warding, fire, concealment, etc.).

### The implementation options I'm considering

I see THREE main approaches. Each has trade-offs. I'll present all three and recommend one.

#### Option A: Block-Based Formations (physical placement)

**How it works:** Formations are built by placing physical blocks (formation flag blocks, array core blocks, spirit vein blocks) in specific patterns. The formation activates when the pattern is complete.

- A **defensive formation** = a ring of formation flag blocks around a sect, with an array core block in the center, powered by a spirit vein block beneath.
- A **restriction formation** = a grid of restriction flag blocks in a specific geometric pattern.
- A **transport formation** = two transport array blocks at different locations, linked via a jade slip.

**Pros:**
- Deeply immersive — you literally build the formation in the world
- Formations have physical presence (can be found, destroyed, modified)
- Canon-accurate — Er Gen formations ARE physical things (flags, cores, veins)
- Other players/NPCs can see and interact with formations
- A mortal can stumble onto a formation's physical components (but can't activate/perceive the spiritual layer of it)

**Cons:**
- Complex to implement (pattern recognition, multi-block structures)
- Formations are static (hard to move once placed)
- Requires world-gen to place canon formations at canon locations

#### Option B: Item-Based Formations (talisman-like)

**How it works:** Formations are items (a "Formation Flag" item, a "Restriction Matrix" item). Right-click to activate; the formation effect appears around the player or at the target location.

- A **Restriction Flag** item — right-click to plant it; it creates a restriction zone around itself.
- A **Sect-Protecting Array** item — right-click to activate; it shields a radius around the player.
- A **Transport Array** item — pair two items; right-click one to teleport to the other.

**Pros:**
- Simple to implement (items are well-understood in Forge)
- Portable — formations move with the player
- Canon-accurate for items like the Restriction Flag (which IS a portable artifact)
- Easy to give to NPCs (an elder carries the sect's protecting array flag)

**Cons:**
- Less immersive — formations don't have physical presence in the world
- Can't stumble onto a formation's physical components
- Doesn't capture the "build a formation" fantasy

#### Option C: Hybrid (blocks + items, my recommendation)

**How it works:** Formations use BOTH blocks and items, depending on the formation type:

1. **Sect-scale formations (defensive, transport, alchemy_aux)** = BLOCK-BASED. These are permanent structures. A sect's protecting array is a ring of formation flag blocks + an array core block + a spirit vein block. These generate at canon sect locations during world-gen. The player can find them, study them, modify them, or destroy them (if strong enough to overcome the formation's resistance).

2. **Personal/portable formations (restriction, sealing, surveillance)** = ITEM-BASED. Wang Lin's Restriction Flag is an item he carries and plants. When planted, it creates a temporary formation zone. The player crafts or loots these items and uses them dynamically.

3. **Talisman formations (paper talismans, jade slips, soul banners)** = ITEM-BASED (consumable or persistent). One-use paper talismans break on activation. Persistent talismans (soul banners, coffins, compasses) are carried and activated repeatedly.

**The formation flag block (the key hybrid element):**
- A special block that, when placed, registers a formation node in the Spiritual Layer
- Different flag types (restriction flag, transport flag, defensive flag, etc.)
- When enough flags are placed in the correct pattern, the formation activates
- The formation exists in the Spiritual Layer — mortals see only the flags (physical); cultivators see the full formation (spiritual)
- A mortal can break the flag block (physical), but the formation's spiritual anchor may persist or shift (per the Prime Directive)

**The formation core block:**
- The heart of a multi-block formation
- Stores the formation's "blueprint" (what pattern of flags it needs)
- When the pattern is complete, the core activates the formation
- The core is powered by a spirit vein (either beneath it, or via a spirit stone item input)

### How formations interact with the three-layer world model

This is the key integration point:

1. **Physical Layer:** the flag blocks, core blocks, and spirit vein blocks. Mortals can see and break these.
2. **Spiritual Layer:** the formation's active effect (the defensive shield, the restriction zone, the transport link). Requires cultivation to perceive. Mortals cannot break the spiritual layer — only the physical flags.
3. **Dao Layer:** the formation's "blueprint" or "law" — the underlying principle that makes it work. A formation master can read this; others cannot.

**Per the Prime Directive:** if a mortal breaks a flag block, the physical block is gone, but the formation's spiritual anchor may persist (it shifts to another flag, or hovers in spiritual space until a new flag is placed). The formation is NOT destroyed unless its spiritual anchor is broken — which requires a cultivator with sufficient divine sense and the right technique.

### How talismans work

Talismans are simpler — they're items with spiritual effects:

1. **Paper talismans (consumable):** right-click to activate. The talisman burns away and the effect fires (a fireball, a concealment veil, a warding shield). The effect exists in the Spiritual Layer.

2. **Persistent talismans (soul banners, coffins, compasses, mirrors):** items with durability or charge. Right-click to activate; the effect persists while the talisman has charge. These are often bound to the owner (via blood refinement or soul branding).

3. **Jade slips:** right-click to read. The slip contains a technique or knowledge, branded via divine sense. Reading it requires sufficient divine sense — a mortal holding a jade slip feels nothing; a Qi Condensation cultivator can read the surface; a Foundation cultivator can read the full content.

### The crafting / acquisition system

Per the Prime Directive, formations and talismans don't "spawn because the player leveled up." They're acquired through:

1. **World-gen placement:** canon formations exist at canon locations (Heng Yue Sect's protecting array, the Soul Refining Sect's blood-sacrifice array, etc.). The player can find, study, and potentially claim them.

2. **NPC crafting:** sect elders and formation masters craft formations and talismans. The player can buy, steal, or inherit them.

3. **Inheritance:** some formations and talismans are rewards from inheritance vaults (which exist in the Spiritual Layer, requiring cultivation to perceive and enter).

4. **Player crafting:** a player who has learned a formation method (like Wang Lin's Restriction Flag Method) can craft formation flags and assemble formations. This requires:
   - The method (learned from a jade slip or teacher)
   - Materials (spirit beast hide for paper talismans, spirit stone for flag cores, specific herbs for ink)
   - Sufficient comprehension (the formation's comprehension difficulty)
   - Sufficient cultivation (the formation's realm gate)

### The formation method as a "technique"

Per the existing design, formation methods are techniques in the "Formations" category of the technique wheel. When the player selects a formation method and casts it:
- If they have formation flags in their inventory, the flags are planted in the correct pattern
- If they're standing in an existing formation (with the right core), they can modify or activate it
- If they have the materials, they can craft a new formation flag

This ties formations into the technique wheel (G key) — no separate UI needed.

### Recommended implementation priority

1. **Formation flag block** — the physical block that anchors a formation node
2. **Formation core block** — the heart of a multi-block formation
3. **The 4 Great Restrictions** (Wang Lin's signature) — as item-based formations the player can acquire
4. **Paper talisman items** — the simplest talisman type (consumable)
5. **Sect-Protecting Array** — as a block-based formation that generates at sect locations
6. **Jade slip item** — for technique/knowledge transfer
7. **Soul banner item** — Wang Lin's signature talisman

### What I will NOT do

- I will NOT make formations "spawn because the player reached realm X." Formations exist in the world (physical + spiritual layers) regardless of the player.
- I will NOT make talismans craftable without the method, materials, and comprehension. The player must earn them.
- I will NOT make formations breakable by mortals. The physical flags can be broken, but the spiritual anchor requires cultivation to affect.
- I will NOT make all formations item-based or all block-based. The hybrid approach (blocks for sect-scale, items for personal/portable) matches the canon.
