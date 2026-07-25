package dev.ergenverse.runtime;

/**
 * Provenance — where a piece of world state <i>came from</i>.
 *
 * <p><b>Architectural directive (CRON-69, point 2):</b> "BlockOwner shouldn't
 * really be ownership. This is subtle. CANON / SIMULATION / PLAYER isn't really
 * ownership. It's <b>provenance</b>. I'd rename it. For example
 * {@code enum Provenance}. Because no one 'owns' a mountain. The enum is
 * describing <i>where the current state came from.</i>"
 *
 * <p>This replaces the former {@code BlockOwner}. The three values describe the
 * three layers of the world-as-Git model:
 *
 * <ul>
 *   <li>{@link #CANON} — the immutable {@link PlanetSuzakuBlueprint}. This state
 *       was hand-authored. It never changes. If a position is CANON, no layer
 *       above it has touched it.</li>
 *   <li>{@link #SIMULATION} — runtime simulation deltas. This state was produced
 *       by the simulation: a beast harvested an herb, a storm broke a roof, a
 *       sect extended its walls. Saved with the world; empty on a fresh save.</li>
 *   <li>{@link #PLAYER} — player edits. This state was produced by the player:
 *       mined, placed, built, destroyed. Saved with the world; empty on a fresh
 *       save. Highest priority — the player's reality wins.</li>
 * </ul>
 *
 * <p>Resolution order is <b>PLAYER &gt; SIMULATION &gt; CANON</b>, expressed by
 * {@link #overrides(Provenance)} (higher ordinal = higher priority). This is
 * the same semantics as Git: the blueprint is the initial commit; simulation
 * and player changes are deltas layered on top. The blueprint is NEVER rewritten.
 *
 * <p>Note: this enum now applies to <b>all</b> world state (blocks, actors,
 * relationships, inventories…), not just blocks. Every {@link dev.ergenverse.runtime.delta.WorldDelta}
 * carries a Provenance. The name reflects that.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public enum Provenance {
    /**
     * Layer 1 — Immutable canon. The {@link PlanetSuzakuBlueprint} defines what
     * exists at day 0. This never changes. If state is CANON, no simulation or
     * player delta has touched it.
     */
    CANON,

    /**
     * Layer 2 — Mutable simulation state. Produced by the simulation: beasts,
     * weather, sects, spirit-vein depletion. Persisted in the save, never
     * written back to the blueprint.
     */
    SIMULATION,

    /**
     * Layer 3 — Player edits. Produced by the player. Persisted in the save.
     * Highest priority — the player's reality always wins.
     */
    PLAYER;

    /**
     * Priority resolution. Higher ordinal = higher priority.
     * {@code PLAYER.overrides(SIMULATION)} is true; {@code CANON.overvides(PLAYER)} is false.
     */
    public boolean overrides(Provenance other) {
        return this.ordinal() > other.ordinal();
    }
}
