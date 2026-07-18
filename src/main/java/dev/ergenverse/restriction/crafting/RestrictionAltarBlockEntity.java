package dev.ergenverse.restriction.crafting;

import dev.ergenverse.block.ErgenverseBlocks;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.cultivation.CultivationCapability;
import dev.ergenverse.cultivation.CultivationState;
import dev.ergenverse.cultivation.RealmId;
import dev.ergenverse.network.ERNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

/**
 * Block entity for the Restriction Altar (\u7981\u5236\u53f0) — Wang Lin's
 * signature deep-crafting workstation for inscribing restrictions onto
 * Restriction Flags.
 *
 * <p>3-slot inventory via Forge's {@link ItemStackHandler}:
 * <ul>
 *   <li>Slot 0: Restriction Flag (the item being inscribed, holds NBT count)</li>
 *   <li>Slot 1: Inkstone tool (consumed 1 charge per 5s session)</li>
 *   <li>Slot 2: Output (same flag, updated count — flag stays in slot 0, updated in place)</li>
 * </ul>
 *
 * <p>Per DESIGN_UNIFIED_CRAFTING.md \u00a73.5 + \u00a713.2:
 * <ul>
 *   <li>5-second inscription session (100 ticks).</li>
 *   <li>9 layers \u00d7 11,111 = 99,999 total restrictions.</li>
 *   <li>Progress per session scales with realm: QiCond +500, Foundation +1500,
 *       CoreFormation +5000, SoulFormation +15000, Ascendant +50000.</li>
 *   <li>Divine sense multiplier: \u00d71.5 if DS >= realm tier, \u00d72.0 if DS > realm tier.</li>
 *   <li>Breakthrough bonus: auto +5k base on each major breakthrough (handled elsewhere).</li>
 *   <li>Inkstone consumed 1 charge per session; falls back to 10 SP if no inkstone.</li>
 * </ul>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public class RestrictionAltarBlockEntity extends BlockEntity implements MenuProvider, Container {

    public static final int CONTAINER_SIZE = 3;
    public static final int SLOT_FLAG = 0;
    public static final int SLOT_INKSTONE = 1;
    public static final int SLOT_OUTPUT = 2;

    /** 5 seconds per inscription session per \u00a713.2. */
    private static final int SESSION_TICKS = 100;

    /** 9 layers mapping to cultivation realms. */
    public static final int TOTAL_LAYERS = 9;
    public static final int RESTRICTIONS_PER_LAYER = 11111;
    public static final int TOTAL_RESTRICTIONS = 99999;

    /** Realm-ordered layer thresholds. Index = layer number (0-8). */
    private static final RealmId[] LAYER_REALMS = {
            RealmId.QI_CONDENSATION,  // Layer 1: Yellow (\u9ec4\u7ea7)
            RealmId.FOUNDATION,       // Layer 2: Earth (\u5730\u7ea7)
            RealmId.CORE_FORMATION,   // Layer 3: Mystery (\u7384\u7ea7)
            RealmId.SOUL_FORMATION,   // Layer 4: Heaven (\u5929\u7ea7)
            RealmId.ASCENDANT,        // Layer 5: Void (\u865a\u7ea7)
            RealmId.CORPOREAL_YANG,   // Layer 6: Abstract (\u65e0\u7ea7) entry
            RealmId.NIRVANA_SCRYER,   // Layer 7: Abstract deep
            RealmId.NIRVANA_CLEANSER, // Layer 8: Abstract deeper
            RealmId.SPIRIT_SEIZER    // Layer 9: Abstract pinnacle
    };

    /** Base progress per 5s session, per layer (QiCond=0, Foundation=1, ...). */
    private static final int[] BASE_PROGRESS = {
            500,    // Qi Condensation
            1500,   // Foundation
            5000,   // Core Formation
            15000,  // Soul Formation
            50000,  // Ascendant
            99999,  // Spirit Severing+ — instant layer clear
            99999,
            99999,
            99999
    };

    private final ItemStackHandler inventory = new ItemStackHandler(CONTAINER_SIZE) {
        @Override
        protected void onContentsChanged(int slot) { setChanged(); }
    };

    private int sessionTimer;
    private boolean inscribing;
    @Nullable private ServerPlayer inscribingPlayer;

    public RestrictionAltarBlockEntity(BlockPos pos, BlockState state) {
        super(dev.ergenverse.block.ErgenverseBlocks.RESTRICTION_ALTAR_ENTITY.get(), pos, state);
    }

    // ─── NBT ──────────────────────────────────────────────────────────

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", inventory.serializeNBT());
        tag.putInt("SessionTimer", sessionTimer);
        tag.putBoolean("Inscribing", inscribing);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Inventory")) inventory.deserializeNBT(tag.getCompound("Inventory"));
        sessionTimer = tag.getInt("SessionTimer");
        inscribing = tag.getBoolean("Inscribing");
    }

    // ─── Layer / Grade helpers ────────────────────────────────────────

    /**
     * Get the flag's current restriction count from NBT.
     * Returns 0 if the flag slot is empty or has no tag.
     */
    public int getFlagCount() {
        ItemStack flag = inventory.getStackInSlot(SLOT_FLAG);
        if (flag.isEmpty()) return 0;
        CompoundTag tag = flag.getTag();
        return tag != null ? tag.getInt("restriction_count") : 0;
    }

    /**
     * Get the current layer (0-8) based on restriction count.
     * Layer = min(count / 11111, 8).
     */
    public static int getLayer(int count) {
        return Math.min(count / RESTRICTIONS_PER_LAYER, TOTAL_LAYERS - 1);
    }

    /**
     * Get the grade name for a layer.
     */
    public static String getGradeName(int layer) {
        return switch (layer) {
            case 0 -> "Yellow (\u9ec4\u7ea7)";
            case 1 -> "Earth (\u5730\u7ea7)";
            case 2 -> "Mystery (\u7384\u7ea7)";
            case 3 -> "Heaven (\u5929\u7ea7)";
            case 4 -> "Void (\u865a\u7ea7)";
            default -> "Abstract (\u65e0\u7ea7)";
        };
    }

    /**
     * Get the progress within the current layer (0 to 11110).
     */
    public int getLayerProgress(int count) {
        return count % RESTRICTIONS_PER_LAYER;
    }

    /**
     * Get the progress within the current layer as a fraction (0.0-1.0).
     */
    public float getLayerProgressFraction(int count) {
        return (float) getLayerProgress(count) / (float) RESTRICTIONS_PER_LAYER;
    }

    // ─── Inscription Session ────────────────────────────────────────

    /**
     * Start an inscription session. Validates the player can inscribe
     * (has flag, meets realm requirement for current layer, has inkstone or SP).
     */
    public boolean startSession(ServerPlayer player) {
        if (inscribing) return false;

        ItemStack flag = inventory.getStackInSlot(SLOT_FLAG);
        if (flag.isEmpty()) {
            player.sendSystemMessage(Component.literal("\u00A7cPlace a Restriction Flag on the altar."));
            return false;
        }

        int count = getFlagCount();
        if (count >= TOTAL_RESTRICTIONS) {
            player.sendSystemMessage(Component.literal("\u00A7aThis Restriction Flag is already complete (99,999 / 99,999)."));
            return false;
        }

        // Check realm gate for current layer
        int currentLayer = getLayer(count);
        CultivationState state = CultivationCapability.get(player).orElse(null);
        if (state == null) return false;

        RealmId requiredRealm = LAYER_REALMS[currentLayer];
        if (!state.getCurrentRealm().isAtLeast(requiredRealm)) {
            player.sendSystemMessage(Component.literal(
                    "\u00A7cLayer " + (currentLayer + 1) + " requires realm: " + requiredRealm.name));
            return false;
        }

        // Check inkstone or SP
        ItemStack inkstone = inventory.getStackInSlot(SLOT_INKSTONE);
        boolean hasInkstone = !inkstone.isEmpty();
        if (!hasInkstone && state.getQi() < 0.05) {
            player.sendSystemMessage(Component.literal(
                    "\u00A7cNeed an Inkstone or sufficient Qi to inscribe."));
            return false;
        }

        this.sessionTimer = 0;
        this.inscribing = true;
        this.inscribingPlayer = player;
        setChanged();
        return true;
    }

    public void tickSession(Level level) {
        if (!inscribing) return;
        sessionTimer++;

        if (sessionTimer >= SESSION_TICKS) {
            completeSession(level);
        }
        setChanged();
        syncProgress();
    }

    private void completeSession(Level level) {
        ServerPlayer player = inscribingPlayer;
        inscribing = false;
        inscribingPlayer = null;
        sessionTimer = 0;

        if (player == null || level == null) return;

        ItemStack flag = inventory.getStackInSlot(SLOT_FLAG);
        if (flag.isEmpty()) return;

        int count = getFlagCount();
        int layer = getLayer(count);

        // Calculate progress with realm scaling
        CultivationState state = CultivationCapability.get(player).orElse(null);
        int base = BASE_PROGRESS[layer];
        int progress = base;

        // Divine sense multiplier
        if (state != null) {
            double ds = state.getDivineSense();
            // DS tier vs realm tier comparison (simplified: 0-1 DS mapped to tiers)
            int playerTier = state.getCurrentRealm().ordinal();
            int dsTier = (int)(ds * 10); // 0-10 scale
            if (dsTier > playerTier + 2) {
                progress = (int)(base * 2.0); // \u00d72.0
            } else if (dsTier >= playerTier) {
                progress = (int)(base * 1.5); // \u00d71.5
            }

            // Consume Qi if no inkstone
            ItemStack inkstone = inventory.getStackInSlot(SLOT_INKSTONE);
            if (inkstone.isEmpty()) {
                state.consumeQi(0.05);
            } else {
                // Consume 1 inkstone charge (damage it)
                if (inkstone.isDamageableItem()) {
                    inkstone.setDamageValue(inkstone.getDamageValue() + 1);
                    if (inkstone.getDamageValue() >= inkstone.getMaxDamage()) {
                        inventory.setStackInSlot(SLOT_INKSTONE, ItemStack.EMPTY);
                    }
                } else {
                    inkstone.shrink(1);
                    if (inkstone.isEmpty()) inventory.setStackInSlot(SLOT_INKSTONE, ItemStack.EMPTY);
                }
            }
        }

        // Apply progress, capped at total and layer boundary
        int newCount = Math.min(count + progress, TOTAL_RESTRICTIONS);
        CompoundTag tag = flag.getOrCreateTag();
        int oldLayer = getLayer(count);
        tag.putInt("restriction_count", newCount);
        flag.setTag(tag);

        // Layer-up notification
        int newLayer = getLayer(newCount);
        if (newLayer > oldLayer && newLayer < TOTAL_LAYERS) {
            player.sendSystemMessage(Component.literal(
                    "\u00A76Restriction Flag advanced to Layer " + (newLayer + 1) +
                            " — " + getGradeName(newLayer) + " grade!"));
        }

        if (newCount >= TOTAL_RESTRICTIONS) {
            player.sendSystemMessage(Component.literal(
                    "\u00A7a\u00A7lRestriction Flag complete! 99,999 / 99,999. The flag is at full power."));
        }

        setChanged();
        syncProgress();
    }

    private void syncProgress() {
        if (level == null || level.isClientSide()) return;
        float progress = inscribing
                ? Math.min(1.0f, (float) sessionTimer / (float) SESSION_TICKS)
                : 0.0f;
        int count = getFlagCount();
        int layer = getLayer(count);
        // TODO: sync disabled
    }

    private java.util.List<ServerPlayer> getViewerPlayers() {
        java.util.List<ServerPlayer> viewers = new java.util.ArrayList<>();
        if (level == null || level.isClientSide()) return viewers;
        for (Player p : level.players()) {
            if (p instanceof ServerPlayer sp
                    && sp.containerMenu instanceof RestrictionAltarMenu menu
                    && menu.getAltar() == this) {
                viewers.add(sp);
            }
        }
        return viewers;
    }

    // ─── Accessors ────────────────────────────────────────────────────

    public float getSessionProgress() {
        return inscribing ? Math.min(1.0f, (float) sessionTimer / (float) SESSION_TICKS) : 0.0f;
    }
    public boolean isInscribing() { return inscribing; }
    public ItemStackHandler getInventory() { return inventory; }

    public void dropContents(Level level, BlockPos pos) {
        for (int i = 0; i < CONTAINER_SIZE; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
                inventory.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

    // ── Container contract ───────────────────────────────────────────

    @Override public int getContainerSize() { return CONTAINER_SIZE; }
    @Override public boolean isEmpty() { for (int i = 0; i < CONTAINER_SIZE; i++) if (!inventory.getStackInSlot(i).isEmpty()) return false; return true; }
    @Override public ItemStack getItem(int slot) { return inventory.getStackInSlot(slot); }
    @Override public ItemStack removeItem(int slot, int amount) { ItemStack s = inventory.getStackInSlot(slot); if (s.isEmpty()) return ItemStack.EMPTY; ItemStack e = s.split(amount); inventory.setStackInSlot(slot, s); setChanged(); return e; }
    @Override public ItemStack removeItemNoUpdate(int slot) { ItemStack s = inventory.getStackInSlot(slot); inventory.setStackInSlot(slot, ItemStack.EMPTY); return s; }
    @Override public void setItem(int slot, ItemStack stack) { inventory.setStackInSlot(slot, stack); setChanged(); }
    @Override public void clearContent() { for (int i = 0; i < CONTAINER_SIZE; i++) inventory.setStackInSlot(i, ItemStack.EMPTY); }
    @Override public boolean stillValid(Player player) { if (level == null) return false; if (level.getBlockEntity(worldPosition) != this) return false; return player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0; }

    // ── MenuProvider contract ──────────────────────────────────────

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.ergenverse.restriction_altar");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
        return new RestrictionAltarMenu(containerId, playerInv, this);
    }
}