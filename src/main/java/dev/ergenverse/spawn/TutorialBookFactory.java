package dev.ergenverse.spawn;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * TutorialBookFactory — creates the "Ergenverse: A Beginner's Guide" written
 * book given to the player on first join.
 *
 * <p>The book contains:
 * <ol>
 *   <li>Welcome + what this mod is.</li>
 *   <li>Keybinds (B = breakthrough, V = divine sense).</li>
 *   <li>Your first steps (meditate at the spirit vein, gather qi, eat pills).</li>
 *   <li>The Wang Family Village guide (what's in each house).</li>
 *   <li>Danger (spirit beasts, karma, tribulation).</li>
 *   <li>Where to go next (explore, find herbs, seek a sect).</li>
 * </ol>
 *
 * <p>Uses vanilla {@link Items#WRITTEN_BOOK} with pre-filled NBT pages so no
 * custom item/screen registration is needed.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17 APIs only.
 */
public final class TutorialBookFactory {

    private TutorialBookFactory() {}

    private static final String TITLE = "Ergenverse: A Beginner's Guide";
    private static final String AUTHOR = "The Heavens";

    /**
     * Create the tutorial book ItemStack with all pages filled.
     */
    public static ItemStack create() {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        CompoundTag tag = book.getOrCreateTag();
        tag.putString("author", AUTHOR);
        tag.putString("title", TITLE);
        tag.putBoolean("resolved", true);

        ListTag pages = new ListTag();
        pages.add(StringTag.valueOf(Component.Serializer.toJson(page1())));
        pages.add(StringTag.valueOf(Component.Serializer.toJson(page2())));
        pages.add(StringTag.valueOf(Component.Serializer.toJson(page3())));
        pages.add(StringTag.valueOf(Component.Serializer.toJson(page4())));
        pages.add(StringTag.valueOf(Component.Serializer.toJson(page5())));
        pages.add(StringTag.valueOf(Component.Serializer.toJson(page6())));
        tag.put("pages", pages);

        return book;
    }

    // ── Page 1: Welcome ──────────────────────────────────────────────
    private static Component page1() {
        return comp()
                .append(line("ERGENVERSE", ChatFormatting.GOLD, true))
                .append(line("A Living Cultivation World", ChatFormatting.AQUA, false))
                .append(blank())
                .append(line("You stand in the Wang Family", ChatFormatting.WHITE))
                .append(line("Village, a mortal settlement at", ChatFormatting.WHITE))
                .append(line("the foot of the Suzaku mountains.", ChatFormatting.WHITE))
                .append(blank())
                .append(line("This is not vanilla Minecraft.", ChatFormatting.YELLOW))
                .append(line("Every block you see was placed", ChatFormatting.YELLOW))
                .append(line("by the heavens. The world has", ChatFormatting.YELLOW))
                .append(line("existed for ten thousand years", ChatFormatting.YELLOW))
                .append(line("before you arrived.", ChatFormatting.YELLOW));
    }

    // ── Page 2: Keybinds ─────────────────────────────────────────────
    private static Component page2() {
        return comp()
                .append(line("KEYBINDS", ChatFormatting.GOLD, true))
                .append(blank())
                .append(line("B - Breakthrough attempt", ChatFormatting.AQUA, true))
                .append(line("  When your Qi is full, press B", ChatFormatting.WHITE))
                .append(line("  to attempt a realm breakthrough.", ChatFormatting.WHITE))
                .append(line("  Failure may damage your soul.", ChatFormatting.RED))
                .append(blank())
                .append(line("V - Divine Sense pulse", ChatFormatting.AQUA, true))
                .append(line("  Scan nearby entities and", ChatFormatting.WHITE))
                .append(line("  concealed objects. Reveals", ChatFormatting.WHITE))
                .append(line("  what a mortal cannot see.", ChatFormatting.WHITE))
                .append(blank())
                .append(line("(Rebind in Options > Controls)", ChatFormatting.DARK_GRAY));
    }

    // ── Page 3: First steps ──────────────────────────────────────────
    private static Component page3() {
        return comp()
                .append(line("YOUR FIRST STEPS", ChatFormatting.GOLD, true))
                .append(blank())
                .append(line("1. Find the SPIRIT VEIN", ChatFormatting.AQUA, true))
                .append(line("  The glowing stone at the", ChatFormatting.WHITE))
                .append(line("  plaza center. Stand near it.", ChatFormatting.WHITE))
                .append(blank())
                .append(line("2. MEDITATE", ChatFormatting.AQUA, true))
                .append(line("  Crouch near the spirit vein", ChatFormatting.WHITE))
                .append(line("  to gather Qi. Your Qi bar", ChatFormatting.WHITE))
                .append(line("  fills over time.", ChatFormatting.WHITE))
                .append(blank())
                .append(line("3. EAT PILLS", ChatFormatting.AQUA, true))
                .append(line("  Qi Gathering Pills (in the", ChatFormatting.WHITE))
                .append(line("  Storage chest) speed up", ChatFormatting.WHITE))
                .append(line("  cultivation.", ChatFormatting.WHITE));
    }

    // ── Page 4: Village guide ────────────────────────────────────────
    private static Component page4() {
        return comp()
                .append(line("THE VILLAGE", ChatFormatting.GOLD, true))
                .append(blank())
                .append(line("Plaza center:", ChatFormatting.AQUA, true))
                .append(line("  Spirit Vein Stone + 4", ChatFormatting.WHITE))
                .append(line("  Formation Core Stones.", ChatFormatting.WHITE))
                .append(blank())
                .append(line("NW - Alchemy Pavilion", ChatFormatting.AQUA, true))
                .append(line("  Holds the Alchemy Furnace.", ChatFormatting.WHITE))
                .append(blank())
                .append(line("NE - Formation Hall", ChatFormatting.AQUA, true))
                .append(line("  Holds a Formation Flag Base.", ChatFormatting.WHITE))
                .append(blank())
                .append(line("SW - Storage", ChatFormatting.AQUA, true))
                .append(line("  Chest with starter gear.", ChatFormatting.WHITE))
                .append(blank())
                .append(line("SE - Your Dwelling", ChatFormatting.AQUA, true))
                .append(line("  A bed awaits you.", ChatFormatting.WHITE));
    }

    // ── Page 5: Danger ───────────────────────────────────────────────
    private static Component page5() {
        return comp()
                .append(line("DANGER", ChatFormatting.RED, true))
                .append(blank())
                .append(line("SPIRIT BEASTS", ChatFormatting.GOLD, true))
                .append(line("  Spirit Rabbits, Wolves, Deer,", ChatFormatting.WHITE))
                .append(line("  Hawks roam the wilds. As a", ChatFormatting.WHITE))
                .append(line("  mortal, you cannot defeat", ChatFormatting.WHITE))
                .append(line("  even a rabbit in combat.", ChatFormatting.RED))
                .append(blank())
                .append(line("KARMA", ChatFormatting.GOLD, true))
                .append(line("  Every harmful act increases", ChatFormatting.WHITE))
                .append(line("  your karma. High karma brings", ChatFormatting.WHITE))
                .append(line("  tribulation lightning.", ChatFormatting.RED))
                .append(blank())
                .append(line("Stay in the village. Cultivate.", ChatFormatting.YELLOW))
                .append(line("The world will come to you.", ChatFormatting.YELLOW));
    }

    // ── Page 6: Where to go next ─────────────────────────────────────
    private static Component page6() {
        return comp()
                .append(line("THE PATH FORWARD", ChatFormatting.GOLD, true))
                .append(blank())
                .append(line("Once you reach Qi Condensation:", ChatFormatting.AQUA))
                .append(blank())
                .append(line("- Gather herbs from the garden", ChatFormatting.WHITE))
                .append(line("  and the wilds.", ChatFormatting.WHITE))
                .append(blank())
                .append(line("- Use the Alchemy Furnace to", ChatFormatting.WHITE))
                .append(line("  refine pills.", ChatFormatting.WHITE))
                .append(blank())
                .append(line("- Seek the Heng Yue Sect in", ChatFormatting.WHITE))
                .append(line("  the mountains. They recruit", ChatFormatting.WHITE))
                .append(line("  mortals with talent.", ChatFormatting.WHITE))
                .append(blank())
                .append(blank())
                .append(line("The world desires you.", ChatFormatting.LIGHT_PURPLE, true))
                .append(line("Will you answer?", ChatFormatting.LIGHT_PURPLE, true));
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private static MutableComponent comp() {
        return Component.empty();
    }

    private static MutableComponent line(String text) {
        return Component.literal(text).append(Component.literal("\n"));
    }

    private static MutableComponent line(String text, ChatFormatting color) {
        return Component.literal(text).withStyle(color).append(Component.literal("\n"));
    }

    private static MutableComponent line(String text, ChatFormatting color, boolean bold) {
        MutableComponent c = Component.literal(text).withStyle(color);
        if (bold) c = c.withStyle(ChatFormatting.BOLD);
        return c.append(Component.literal("\n"));
    }

    private static MutableComponent blank() {
        return Component.literal("\n");
    }
}
