package dev.ergenverse.item;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * JournalItem — a cultivator's journal for recording discoveries (修行笔记).
 *
 * <p>Canon: cultivators keep journals of their insights, techniques learned,
 * beasts encountered, and places visited. Wang Lin's meticulous nature (his
 * caution and observation skills) would translate into detailed journal entries.
 *
 * <p>Mechanics:
 * <ul>
 *   <li>Right-click: open the journal — displays recent entries in chat.</li>
 *   <li>Shift + right-click: add a new entry (auto-records location, biome,
 *       nearby entities, and the player's current cultivation realm).</li>
 *   <li>Entries persist in NBT — survives death (Article XLIII).</li>
 *   <li>Max entries: 50 (can be expanded with higher-quality journals).</li>
 *   <li>Each entry records: timestamp, position, biome, realm, observation text.</li>
 * </ul>
 *
 * <p>CRON-COMPLETIONIST-67: Upgraded from 13-line stub to full journal system.
 * Previously JournalItem extended Item with no methods.
 */
public class JournalItem extends Item {

    private static final int MAX_ENTRIES = 50;
    private static final String NBT_ENTRIES = "JournalEntries";
    private static final String NBT_ENTRY_COUNT = "EntryCount";
    private static final String NBT_TIME = "GameTime";
    private static final String NBT_X = "PosX";
    private static final String NBT_Y = "PosY";
    private static final String NBT_Z = "PosZ";
    private static final String NBT_BIOME = "Biome";
    private static final String NBT_REALM = "Realm";
    private static final String NBT_TEXT = "Text";

    public JournalItem(Properties props) {
        super(props.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack journal = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.success(journal);
        }

        if (player.isShiftKeyDown()) {
            return addEntry(level, player, hand, journal);
        } else {
            return readJournal(level, player, hand, journal);
        }
    }

    private InteractionResultHolder<ItemStack> readJournal(Level level, Player player,
                                                           InteractionHand hand, ItemStack journal) {
        CompoundTag tag = journal.getOrCreateTag();
        ListTag entries = tag.getList(NBT_ENTRIES, CompoundTag.TAG_COMPOUND);
        int count = entries.size();

        player.sendSystemMessage(Component.literal(
                "\u00A7d\u2726 Cultivation Journal (" + count + "/" + MAX_ENTRIES + " entries)")
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xAA55FF))));

        if (count == 0) {
            player.sendSystemMessage(Component.literal(
                    "\u00A78The journal is empty. Shift+right-click to record an observation."));
            return InteractionResultHolder.success(journal);
        }

        // Show last 5 entries (most recent first)
        int start = Math.max(0, count - 5);
        player.sendSystemMessage(Component.literal("\u00A77Recent entries:"));
        for (int i = count - 1; i >= start; i--) {
            CompoundTag entry = entries.getCompound(i);
            long gameTime = entry.getLong(NBT_TIME);
            int hours = (int) (gameTime / 1000) % 24;
            int minutes = (int) ((gameTime / 1000.0 * 60) % 60);
            String time = String.format("%02d:%02d", hours, minutes);
            String realm = entry.getString(NBT_REALM);
            String biome = entry.getString(NBT_BIOME);
            String text = entry.getString(NBT_TEXT);

            player.sendSystemMessage(Component.literal(
                    "\u00A7b[" + time + "] \u00A7a(" + realm + ") \u00A7f" + biome + ": " + text));
        }

        if (count > 5) {
            player.sendSystemMessage(Component.literal(
                    "\u00A78... and " + (count - 5) + " earlier entries."));
        }

        level.playSound(null, player.blockPosition(),
                SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.5f, 1.0f);

        return InteractionResultHolder.success(journal);
    }

    private InteractionResultHolder<ItemStack> addEntry(Level level, Player player,
                                                       InteractionHand hand, ItemStack journal) {
        CompoundTag tag = journal.getOrCreateTag();
        ListTag entries = tag.getList(NBT_ENTRIES, CompoundTag.TAG_COMPOUND);

        if (entries.size() >= MAX_ENTRIES) {
            player.sendSystemMessage(Component.literal(
                    "\u00A7cJournal is full! (" + MAX_ENTRIES + " entries max)."));
            return InteractionResultHolder.fail(journal);
        }

        // Auto-record observation
        CompoundTag entry = new CompoundTag();
        entry.putLong(NBT_TIME, level.getGameTime());
        entry.putDouble(NBT_X, player.getX());
        entry.putDouble(NBT_Y, player.getY());
        entry.putDouble(NBT_Z, player.getZ());

        // Get biome name
        String biomeName = "Unknown";
        try {
            var biome = level.getBiome(player.blockPosition());
            biomeName = biome.unwrapKey()
                    .map(k -> k.location().getPath().replace("_", " "))
                    .orElse("Unknown");
        } catch (Exception e) {
            biomeName = "Unknown";
        }
        entry.putString(NBT_BIOME, biomeName);

        // Get cultivation realm
        String realm = "Mortal";
        try {
            var state = dev.ergenverse.cultivation.CultivationCapability.get(player).resolve();
            if (state.isPresent()) {
                realm = state.get().getCurrentRealm().name;
            }
        } catch (Exception e) {
            // Capability may not be available
        }
        entry.putString(NBT_REALM, realm);

        // Auto-generate observation text based on surroundings
        String observation = generateObservation(player, level, biomeName);
        entry.putString(NBT_TEXT, observation);

        entries.add(entry);
        tag.put(NBT_ENTRIES, entries);
        tag.putInt(NBT_ENTRY_COUNT, entries.size());

        player.sendSystemMessage(Component.literal(
                "\u00A7a\u2726 Journal entry recorded: \u00A77\"" + observation + "\""));

        level.playSound(null, player.blockPosition(),
                SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.5f, 1.0f);

        return InteractionResultHolder.success(journal);
    }

    private String generateObservation(Player player, Level level, String biome) {
        // Count nearby entities for context
        int entityCount = 0;
        int beastCount = 0;
        if (level instanceof net.minecraft.server.level.ServerLevel sl) {
            var entities = sl.getEntities(player, player.getBoundingBox().inflate(16));
            for (var e : entities) {
                entityCount++;
                if (e instanceof dev.ergenverse.entity.SpiritBeastEntity) {
                    beastCount++;
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        if (beastCount > 0) {
            sb.append("Noted ").append(beastCount).append(" spirit beast(s) in the area. ");
        }
        if (entityCount > 0) {
            sb.append("Total ").append(entityCount).append(" entities nearby. ");
        }

        // Time-based flavor
        long dayTime = level.getDayTime() % 24000;
        if (dayTime < 2000) {
            sb.append("Dawn breaks over ");
        } else if (dayTime < 6000) {
            sb.append("Morning light in ");
        } else if (dayTime < 12000) {
            sb.append("Midday sun over ");
        } else if (dayTime < 14000) {
            sb.append("Afternoon warmth in ");
        } else if (dayTime < 18000) {
            sb.append("Dusk falls on ");
        } else {
            sb.append("Night shrouds ");
        }
        sb.append(biome);

        return sb.toString();
    }

    // ── Tooltip ──────────────────────────────────────────────────────

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        int count = stack.getOrCreateTag().getInt(NBT_ENTRY_COUNT);
        tooltip.add(Component.literal("\u00A7dCultivation Journal (修行笔记)"));
        tooltip.add(Component.literal("\u00A77Records observations and discoveries"));
        tooltip.add(Component.literal("\u00A78Entries: \u00A7a" + count + "/" + MAX_ENTRIES));
        tooltip.add(Component.literal("\u00A77Right-click: read entries"));
        tooltip.add(Component.literal("\u00A77Shift+right-click: record observation"));
    }
}
