package dev.ergenverse.simulation.cognition;

import java.util.ArrayList;
import java.util.List;

/**
 * SocialState — the actor's social graph snapshot for cognition.
 *
 * <p>Tracked: allies, enemies, sect membership, sworn brothers, master/disciples,
 * territory ownership, debts owed, debts owed-to, reputation snapshots.
 *
 * <p>This is a value-only summary; the full relationship state lives in
 * {@link ReputationRegistry}.
 */
public final class SocialState {

    public String factionId;            // sect/clan/settlement id, or null
    public String factionRole;          // "disciple", "elder", "patriarch", "rogue"...
    public boolean hasTerritory;
    public boolean hasMaster;
    public boolean hasDisciples;
    public boolean hasSwornBrothers;

    public final List<String> allies   = new ArrayList<>();
    public final List<String> enemies  = new ArrayList<>();
    public final List<String> debtsOwedTo = new ArrayList<>();  // people we owe
    public final List<String> debtsOwedBy = new ArrayList<>();  // people who owe us

    public SocialState() {
        this.factionId = null;
        this.factionRole = "rogue";
        this.hasTerritory = false;
        this.hasMaster = false;
        this.hasDisciples = false;
        this.hasSwornBrothers = false;
    }

    public boolean hasEnemy() {
        return !enemies.isEmpty();
    }

    public boolean hasAlly() {
        return !allies.isEmpty();
    }

    public boolean inFaction() {
        return factionId != null && !"rogue".equals(factionRole);
    }

    public boolean isSectLeader() {
        return "patriarch".equals(factionRole) || "sect_leader".equals(factionRole);
    }
}
