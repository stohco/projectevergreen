package dev.ergenverse.simulation.settlement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Residence — a place where actors live. Per Article XLIV (the user's
 * directive): "Buildings don't own NPCs. NPCs own buildings. That's a subtle
 * but important inversion."
 *
 * <p>A Residence has an <b>owner</b> (the head of household) and a list of
 * <b>residents</b> (family members, servants, lodgers). Ownership can
 * transfer. A residence can be abandoned (owner null, building intact) or
 * destroyed (building gone). The full ownership history is retained.
 *
 * <h2>Why this matters</h2>
 * <p>The prior architecture attached NPCs to buildings (a fixed spawn offset
 * per building). That is backwards. A residence is a lifecycle object:
 * <ul>
 *   <li>Wang Lin lives here with his parents.</li>
 *   <li>Wang Lin leaves for Heng Yue forever. His parents remain.</li>
 *   <li>Years pass. His parents die. The house is abandoned.</li>
 *   <li>Another family moves in. Same coordinate, different history.</li>
 *   <li>The village burns. The house is destroyed.</li>
 *   <li>The house is rebuilt. Same coordinate, third history.</li>
 * </ul>
 *
 * <p>The building's coordinate is fixed (Layer 1 Blueprint). The residence —
 * who owns it, who lives in it, whether it stands — is Layer 3 Delta. This
 * class is the Layer 3 representation.
 *
 * <h2>Footprint</h2>
 * <p>The footprint is a rectangle (minX/minZ to maxX/maxZ) in settlement-local
 * offsets. The {@link ActorPresence} engine uses the footprint center as the
 * "home" presence location for the owner and residents.
 */
public final class Residence {

    /** Stable identifier (e.g. "wang_family_home"). */
    public final String id;

    /** Owning settlement id. */
    public final String settlementId;

    /** Human-readable label (e.g. "Wang Family Home"). */
    public final String label;

    /** Footprint min X (settlement-local offset). */
    public final int footprintMinX;
    /** Footprint min Z (settlement-local offset). */
    public final int footprintMinZ;
    /** Footprint max X (settlement-local offset). */
    public final int footprintMaxX;
    /** Footprint max Z (settlement-local offset). */
    public final int footprintMaxZ;

    /** Current owner actor id. Null when abandoned or destroyed. */
    private String ownerActorId;

    /** Residents (actor ids) — includes the owner. */
    private final List<String> residentActorIds = new ArrayList<>();

    /** Full ownership + lifecycle history (append-only, Layer 3). */
    private final List<OwnershipRecord> ownershipHistory = new ArrayList<>();

    /** True when the building is physically destroyed (burned, collapsed). */
    private boolean destroyed;

    public Residence(String id, String settlementId, String label,
                     int fMinX, int fMinZ, int fMaxX, int fMaxZ,
                     String initialOwner, long foundingTick) {
        this.id = id;
        this.settlementId = settlementId;
        this.label = label;
        this.footprintMinX = fMinX;
        this.footprintMinZ = fMinZ;
        this.footprintMaxX = fMaxX;
        this.footprintMaxZ = fMaxZ;
        this.ownerActorId = initialOwner;
        this.ownershipHistory.add(new OwnershipRecord(initialOwner, foundingTick, "founding"));
        if (initialOwner != null) {
            this.residentActorIds.add(initialOwner);
        }
    }

    /** The current owner, or null if abandoned/destroyed. */
    public String getOwner() { return ownerActorId; }

    /** Unmodifiable view of current residents. */
    public List<String> getResidents() {
        return Collections.unmodifiableList(residentActorIds);
    }

    /** True if the building is physically gone. */
    public boolean isDestroyed() { return destroyed; }

    /** True if the building stands but has no owner. */
    public boolean isAbandoned() { return ownerActorId == null && !destroyed; }

    /** Full ownership + lifecycle history. */
    public List<OwnershipRecord> getOwnershipHistory() {
        return Collections.unmodifiableList(ownershipHistory);
    }

    /** Add a resident (does not change ownership). */
    public void addResident(String actorId) {
        if (!residentActorIds.contains(actorId)) {
            residentActorIds.add(actorId);
        }
    }

    /** Remove a resident (e.g. moved out, died). Does not change ownership. */
    public void removeResident(String actorId) {
        residentActorIds.remove(actorId);
    }

    /**
     * Transfer ownership to a new actor. The prior owner is recorded in
     * history. The new owner is added to residents if not already present.
     */
    public void transferOwnership(String newOwner, long tick, String reason) {
        this.ownerActorId = newOwner;
        if (newOwner != null && !residentActorIds.contains(newOwner)) {
            residentActorIds.add(newOwner);
        }
        this.ownershipHistory.add(new OwnershipRecord(newOwner, tick, reason));
    }

    /** Abandon the residence (owner leaves, building stands). */
    public void abandon(long tick, String reason) {
        this.ownerActorId = null;
        this.ownershipHistory.add(new OwnershipRecord(null, tick, "abandoned: " + reason));
    }

    /** Destroy the residence (building gone). Clears owner. */
    public void destroy(long tick, String reason) {
        this.destroyed = true;
        this.ownerActorId = null;
        this.residentActorIds.clear();
        this.ownershipHistory.add(new OwnershipRecord(null, tick, "destroyed: " + reason));
    }

    /** Rebuild the residence after destruction, with a new (or returning) owner. */
    public void rebuild(String newOwner, long tick, String reason) {
        this.destroyed = false;
        this.ownerActorId = newOwner;
        this.residentActorIds.clear();
        if (newOwner != null) this.residentActorIds.add(newOwner);
        this.ownershipHistory.add(new OwnershipRecord(newOwner, tick, "rebuilt: " + reason));
    }

    /** The footprint center X (settlement-local) — used as the home presence point. */
    public int centerX() { return (footprintMinX + footprintMaxX) / 2; }

    /** The footprint center Z (settlement-local) — used as the home presence point. */
    public int centerZ() { return (footprintMinZ + footprintMaxZ) / 2; }

    /** An immutable ownership/lifecycle record. */
    public record OwnershipRecord(String owner, long tick, String reason) {}
}
