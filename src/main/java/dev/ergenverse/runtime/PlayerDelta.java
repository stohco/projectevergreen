package dev.ergenverse.runtime;

/**
 * PlayerDelta — Layer 3: player modifications.
 *
 * <p><b>Contract (2026-07-26 directive):</b> "Then the player adds another
 * layer. Mine stone. Cut tree. Build house. Destroy mountain. Dig tunnel.
 * Plant herbs. Create formation. Kill beasts. Start sect. Destroy village.
 * Whatever. Again, this doesn't modify the blueprint. It modifies the save."
 *
 * <p>Examples of player changes:
 * <ul>
 *   <li>Player mines stone → stone block becomes air (PLAYER)</li>
 *   <li>Player places a block → air becomes that block (PLAYER)</li>
 *   <li>Player destroys a building → building blocks become air (PLAYER)</li>
 *   <li>Player builds a house → air becomes house blocks (PLAYER)</li>
 *   <li>Player plants herbs → air/dirt becomes herb block (PLAYER)</li>
 *   <li>Player creates a formation → blocks become formation blocks (PLAYER)</li>
 * </ul>
 *
 * <p>The player delta is saved with the world. On new save creation, it is
 * empty — every playthrough starts from pure canon. The player has complete
 * freedom to mine, build, terraform, destroy, and create. Nothing in the
 * blueprint prevents this. The blueprint simply isn't rewritten.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class PlayerDelta extends BlockDelta {

    public PlayerDelta() {
        super(BlockOwner.PLAYER);
    }

    /**
     * Record a player-caused block change.
     *
     * <p>This is called when the player mines, places, or destroys a block.
     * The change is recorded in the player delta and persists in the save.
     *
     * <p>Examples:
     * <ul>
     *   <li>Player mines stone at (x,y,z): set(x, y, z, "minecraft:air")</li>
     *   <li>Player places spirit stone at (x,y,z): set(x, y, z, "ergenverse:spirit_stone")</li>
     *   <li>Player destroys a wall: set(x, y, z, "minecraft:air")</li>
     * </ul>
     */
    @Override
    public void set(int x, int y, int z, String blockId) {
        super.set(x, y, z, blockId);
    }
}
