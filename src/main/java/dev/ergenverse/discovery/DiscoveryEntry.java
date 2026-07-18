package dev.ergenverse.discovery;

/**
 * Data class for a single discoverable recipe entry.
 * Used by the Unified Discovery UI to show what the player can craft.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public class DiscoveryEntry {

    public final String systemName;     // e.g. "Alchemy", "Artifact Forge", "Puppet Refining"
    public final String recipeName;     // e.g. "Qi Gathering Pill", "Copper Celestial Guard"
    public final String outputItemName; // display name of output item
    public final boolean canCraft;      // true if player has materials + meets gates
    public final String blocker;        // if !canCraft, what's blocking (realm, materials, karma, etc.)
    public final String[] missingItems; // names of missing input items
    public final String workstation;    // block name needed (e.g. "Pill Furnace", "Puppet Platform")

    public DiscoveryEntry(String systemName, String recipeName, String outputItemName,
                          boolean canCraft, String blocker, String[] missingItems, String workstation) {
        this.systemName = systemName;
        this.recipeName = recipeName;
        this.outputItemName = outputItemName;
        this.canCraft = canCraft;
        this.blocker = blocker;
        this.missingItems = missingItems;
        this.workstation = workstation;
    }
}