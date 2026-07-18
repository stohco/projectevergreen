package dev.ergenverse.formation;

import dev.ergenverse.core.WorldPhilosophy;
import dev.ergenverse.cultivation.RealmId;

import java.util.UUID;

/**
 * An active formation instance — placed in the world, protecting a volume
 * or applied directly to an object.
 *
 * <p>Per the {@link WorldPhilosophy} and the user's corrections:
 *
 * <h2>Correction 1: Defensive formations are BUBBLES</h2>
 *
 * <p>A defensive formation is essentially a bubble that lets allies in and
 * keeps enemies out. The physical blocks inside the formation's area are
 * PROTECTED — you cannot break them while the formation is up. The flags
 * are the anchor (the power source), but they're INSIDE the bubble — you
 * can't reach them to break them unless the formation lets you in or you
 * overpower it.
 *
 * <p>This means:
 * <ul>
 *   <li>Mortals CANNOT break blocks inside a defensive formation's area.</li>
 *   <li>Enemies CANNOT enter the formation's area (they're repelled, weakened, or destroyed).</li>
 *   <li>Allies pass through freely and can interact with blocks inside.</li>
 *   <li>To break the formation, you must either:
 *     <ul>
 *       <li>Overpower it from outside (capability > formation's spiritual resistance)</li>
 *       <li>Be recognized as an ally (walk in and break the core/flags directly)</li>
 *       <li>Use a formation-breaking technique (Restriction-Breaking Ancient Mirror, etc.)</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <h2>Correction 2: Seals make objects as strong as the seal</h2>
 *
 * <p>When a formation/talisman is applied DIRECTLY ON an object (not around
 * a region, but ON the object itself), the object's resistance becomes
 * the seal's resistance. Wang Lin puts a restriction on a storage bag →
 * the bag is as hard to break as the restriction. A sealing stamp on a
 * door → breaking the door requires breaking the seal.
 *
 * <p>This means:
 * <ul>
 *   <li>An object with a seal on it has resistance = max(object's own resistance, seal's resistance).</li>
 *   <li>Breaking the object requires breaking the seal first.</li>
 *   <li>Once the seal is broken, the object reverts to its own resistance.</li>
 *   <li>The seal's power is determined by the formation/talisman's grade + the sealer's cultivation.</li>
 * </ul>
 *
 * <h2>Two application modes</h2>
 *
 * <p>Formations can be applied in two ways:
 *
 * <ol>
 *   <li><b>REGIONAL</b> — the formation protects a volume (a sect, a cave, a
 *       mountain). Everything inside the volume is protected. This is the
 *       "bubble" mode. The formation has a center, a radius, and an
 *       allegiance (who counts as ally vs enemy).</li>
 *   <li><b>OBJECT-BOUND</b> — the formation is applied directly to a single
 *       object (a door, a storage bag, a sword). The object's resistance
 *       becomes the seal's resistance. This is the "seal" mode.</li>
 * </ol>
 */
public final class ActiveFormation {

    /** How this formation instance is applied. */
    public enum ApplicationMode {
        /** Protects a volume — the "bubble." Everything inside is protected. */
        REGIONAL,
        /** Applied directly on an object — the object's resistance becomes the seal's. */
        OBJECT_BOUND
    }

    // ─── Common fields ───────────────────────────────────────────────

    public final UUID instanceId;
    public final Formation definition;
    public final ApplicationMode mode;

    /** Who placed this formation (sect id, NPC id, player id, or null for world-gen). */
    public final String ownerId;

    /** The sealer's cultivation realm when the formation was placed — affects power. */
    public final RealmId sealerRealm;

    /** The formation's allegiance list (who counts as ally). Empty = everyone is enemy. */
    public final java.util.Set<String> allyIds;

    /** The formation's current integrity (0-1). At 0, the formation breaks. */
    public double integrity;

    // ─── REGIONAL mode fields ────────────────────────────────────────

    /** Center of the protected volume (block coordinates). Null for OBJECT_BOUND. */
    public final long[] center;

    /** Radius of the protected volume (in blocks). Null for OBJECT_BOUND. */
    public final int radius;

    // ─── OBJECT_BOUND mode fields ────────────────────────────────────

    /** The object this seal is applied to (block coordinates or entity id). Null for REGIONAL. */
    public final String boundTargetId;

    // ─── Constructor for REGIONAL mode ───────────────────────────────

    public ActiveFormation(Formation definition, String ownerId, RealmId sealerRealm,
                           java.util.Set<String> allyIds,
                           long centerX, long centerY, long centerZ, int radius) {
        this.instanceId = UUID.randomUUID();
        this.definition = definition;
        this.mode = ApplicationMode.REGIONAL;
        this.ownerId = ownerId;
        this.sealerRealm = sealerRealm;
        this.allyIds = java.util.Collections.unmodifiableSet(new java.util.HashSet<>(allyIds));
        this.integrity = 1.0;
        this.center = new long[]{centerX, centerY, centerZ};
        this.radius = radius;
        this.boundTargetId = null;
    }

    // ─── Constructor for OBJECT_BOUND mode ───────────────────────────

    public ActiveFormation(Formation definition, String ownerId, RealmId sealerRealm,
                           String boundTargetId) {
        this.instanceId = UUID.randomUUID();
        this.definition = definition;
        this.mode = ApplicationMode.OBJECT_BOUND;
        this.ownerId = ownerId;
        this.sealerRealm = sealerRealm;
        this.allyIds = java.util.Collections.emptySet();
        this.integrity = 1.0;
        this.center = null;
        this.radius = 0;
        this.boundTargetId = boundTargetId;
    }

    // ─── Bubble protection (REGIONAL mode) ───────────────────────────

    /**
     * Is the given position inside this formation's protected volume?
     *
     * <p>Only applies to REGIONAL mode formations.
     */
    public boolean containsPosition(long x, long y, long z) {
        if (mode != ApplicationMode.REGIONAL || center == null) return false;
        long dx = x - center[0];
        long dy = y - center[1];
        long dz = z - center[2];
        return dx * dx + dy * dy + dz * dz <= (long) radius * radius;
    }

    /**
     * Can the given entity break blocks inside this formation's volume?
     *
     * <p>Per the user's correction: a defensive formation is a bubble.
     * Mortals CANNOT break blocks inside. Enemies CANNOT break blocks inside.
     * Only allies (those in the allyIds list) can interact with blocks inside.
     *
     * <p>To break blocks inside without being an ally, the entity must
     * OVERPOWER the formation first — their manipulation capability must
     * exceed the formation's spiritual resistance.
     *
     * @param entityId   the entity trying to break blocks
     * @param entityRealm the entity's cultivation realm
     * @return true if the entity can break blocks inside the formation
     */
    public boolean canBreakBlocksInside(String entityId, RealmId entityRealm) {
        // Allies can always interact inside the formation
        if (allyIds.contains(entityId)) return true;

        // The formation's protection scales with its grade + sealer's realm
        int formationPower = definition.grade.power + sealerRealm.order;
        int entityPower = entityRealm.order;

        // If the entity's realm exceeds the formation's power, they can force through
        // (This is how high-tier cultivators breach sect arrays)
        return entityPower > formationPower;
    }

    /**
     * Can the given entity ENTER the formation's volume?
     *
     * <p>Defensive formations repel enemies. An enemy trying to enter is:
     * - Repelled (pushed back) if the formation is much stronger
     * - Weakened (debuffed) if the formation is somewhat stronger
     * - Destroyed (killed) if the formation is overwhelmingly stronger
     * - Allowed through if the entity is stronger than the formation
     *
     * <p>Allies pass through freely.
     */
    public EntryResult canEnter(String entityId, RealmId entityRealm) {
        if (allyIds.contains(entityId)) return EntryResult.ALLOWED;

        int formationPower = definition.grade.power + sealerRealm.order;
        int entityPower = entityRealm.order;

        if (entityPower > formationPower + 2) return EntryResult.ALLOWED; // overpowers
        if (entityPower > formationPower) return EntryResult.WEAKENED;    // barely gets through, debuffed
        if (entityPower == formationPower) return EntryResult.REPELLED;   // pushed back
        if (entityPower >= formationPower - 2) return EntryResult.DAMAGED; // takes damage on entry
        return EntryResult.DESTROYED; // killed on entry
    }

    public enum EntryResult {
        ALLOWED,    // passes through freely
        WEAKENED,   // passes through but debuffed
        REPELLED,   // pushed back, cannot enter
        DAMAGED,    // takes significant damage on entry
        DESTROYED   // killed on entry
    }

    // ─── Seal protection (OBJECT_BOUND mode) ─────────────────────────

    /**
     * Get the effective resistance of an object that has this seal applied to it.
     *
     * <p>Per the user's correction: "talismans/formations applied directly on
     * something make that thing as strong as the seal."
     *
     * <p>When a seal is applied to an object, the object's resistance becomes:
     * <pre>
     *   effectiveResistance = max(object's own resistance, seal's resistance)
     * </pre>
     *
     * <p>The seal's resistance is determined by:
     * - The formation/talisman's grade (mortal=5, spirit=50, heaven=200, dao=1000, immortal=5000)
     * - The sealer's cultivation realm (higher realm = stronger seal)
     * - The formation's spiritual resistance value
     *
     * <p>Breaking the object requires breaking the seal first. Once the seal's
     * integrity drops to 0, the seal breaks and the object reverts to its own
     * resistance.
     *
     * @param objectOwnResistance the object's own resistance (without the seal)
     * @return the effective resistance (the higher of the object's own and the seal's)
     */
    public double effectiveResistance(double objectOwnResistance) {
        if (mode != ApplicationMode.OBJECT_BOUND) return objectOwnResistance;

        double sealResistance = definition.spiritualResistance
                              * (1.0 + sealerRealm.order * 0.5)
                              * integrity;

        return Math.max(objectOwnResistance, sealResistance);
    }

    /**
     * Apply damage to the seal (when someone attempts to break the sealed object).
     *
     * <p>Damage to the sealed object is absorbed by the seal first. Only when
     * the seal's integrity reaches 0 does the object take damage.
     *
     * @param damage the damage attempted
     * @return the leftover damage that passes through to the object (0 if fully absorbed)
     */
    public double damageSeal(double damage) {
        if (mode != ApplicationMode.OBJECT_BOUND || integrity <= 0) return damage;

        double sealHP = definition.spiritualResistance * (1.0 + sealerRealm.order * 0.5);
        double absorbed = Math.min(damage, sealHP * integrity);
        integrity -= absorbed / sealHP;
        integrity = Math.max(0, integrity);

        return damage - absorbed;  // leftover passes to the object
    }

    /**
     * Is the seal still active?
     */
    public boolean isIntact() {
        return integrity > 0;
    }

    // ─── Formation integrity (both modes) ────────────────────────────

    /**
     * Apply damage to the formation itself (when someone attacks the formation).
     *
     * <p>For REGIONAL formations: this is what happens when a cultivator
     * attacks the sect array from outside. The formation's integrity drops.
     * At 0, the formation breaks and the bubble collapses.
     *
     * <p>For OBJECT_BOUND formations: this is the same as damageSeal().
     *
     * @param attackerCapability the attacker's manipulation capability
     * @return true if the formation broke from this attack
     */
    public boolean attackFormation(double attackerCapability) {
        double formationResistance = definition.spiritualResistance
                                   * (1.0 + sealerRealm.order * 0.5);

        if (attackerCapability > formationResistance) {
            integrity -= (attackerCapability - formationResistance) / formationResistance;
            integrity = Math.max(0, integrity);
            return integrity <= 0;
        }
        // Insufficient force — the formation holds
        return false;
    }
}
