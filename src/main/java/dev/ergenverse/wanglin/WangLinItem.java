package dev.ergenverse.wanglin;

import dev.ergenverse.canon.Provenance;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.wanglin.registry.CanonicalEntry;
import dev.ergenverse.wanglin.registry.WangLinMasterRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * WangLinItem — the Forge {@link Item} subclass for every piece of Wang Lin's arsenal.
 *
 * <h2>Three-Layer Architecture — Layer 1: Canon</h2>
 * <p>This class is the Minecraft-facing wrapper around a canon artifact. It carries
 * the canon metadata (canonId, names, category, provenance) and the NBT-backed
 * <b>canon state index</b> (which canon-documented form this stack is currently in).
 *
 * <p>Every registered item Wang Lin encountered, owned, or wielded across the full
 * Renegade Immortal narrative is a {@code WangLinItem}. Most items have a SINGLE
 * canon state (the form Wang Lin acquired them in). Only items with DOCUMENTED
 * transformations have a multi-state {@link ItemEvolutionChain}.
 *
 * <h2>The Canon State (NBT-backed)</h2>
 * <p>Each stack carries an {@code Ergen.CanonState} integer in its NBT (default 0).
 * For single-state items, this is always 0 and never changes — the item is complete
 * as-is. For multi-state items, this indexes into the {@link ItemEvolutionChain}.
 *
 * <h2>"No Locked Upgrades" — REWRITTEN</h2>
 * <p>The old directive has been replaced with the honest formulation:
 * <blockquote>Every canonical state must be obtainable.</blockquote>
 * If an item has 3 canon states, the player can eventually reach all 3. If it has
 * 1 canon state, that is the complete item — nothing is missing. We do NOT invent
 * "Peak" / "Awakened" / "Ascended" stages Er Gen never wrote.
 *
 * <h2>Tooltip</h2>
 * <p>The tooltip shows: canonical name (CN + EN), category, canon confidence (the
 * {@link Provenance}), current canon state / documented states, the canon event
 * that produced the current state, and the source citation. This gives the player
 * full visibility into every item's canon identity and provenance.
 *
 * <h2>Prime Directive Compliance</h2>
 * <p>The item itself is objective — it exists in the world regardless of who
 * perceives it. What changes with cultivation is the player's <i>understanding</i>
 * of the item (handled by the perception system, not by swapping the item).
 */
public class WangLinItem extends Item {

    /** The arsenal category — determines creative-tab placement. */
    public enum ArsenalCategory {
        TECHNIQUE("Technique", ChatFormatting.AQUA),
        ARTIFACT("Artifact", ChatFormatting.LIGHT_PURPLE),
        ESSENCE("Essence", ChatFormatting.GOLD),
        DOMAIN("Dao Domain", ChatFormatting.DARK_PURPLE),
        PET("Spirit Beast", ChatFormatting.GREEN),
        CLONE("Clone / Avatar", ChatFormatting.DARK_AQUA),
        COMPANION("Companion", ChatFormatting.YELLOW),
        FORMATION("Formation", ChatFormatting.DARK_GREEN),
        SPELL("Spell", ChatFormatting.BLUE),
        TREASURE("Treasure", ChatFormatting.DARK_RED),
        MISCELLANEOUS("Miscellaneous", ChatFormatting.GRAY);

        public final String label;
        public final ChatFormatting color;
        ArsenalCategory(String label, ChatFormatting color) { this.label = label; this.color = color; }
    }

    private final String canonId;
    private final String nameEn;
    private final String nameCn;
    private final ArsenalCategory category;
    private final int canonConfidence;        // 1-5 per Provenance
    private final String evolutionChainId;    // links to ItemEvolutionChain (may be null for single-state items)
    private final float unlockThreshold;      // 0..1 narrative mastery point
    private final String source;              // canon source citation

    public WangLinItem(Properties properties,
                       String canonId,
                       String nameEn,
                       String nameCn,
                       ArsenalCategory category,
                       int canonConfidence,
                       String evolutionChainId,
                       float unlockThreshold,
                       String source) {
        super(properties);
        this.canonId = canonId;
        this.nameEn = nameEn;
        this.nameCn = nameCn;
        this.category = category;
        this.canonConfidence = Math.max(1, Math.min(5, canonConfidence));
        this.evolutionChainId = evolutionChainId != null ? evolutionChainId : canonId;
        this.unlockThreshold = unlockThreshold;
        this.source = source;
    }

    // ── Canon metadata accessors ──────────────────────────────────────

    public String canonId() { return canonId; }
    public String nameEn() { return nameEn; }
    public String nameCn() { return nameCn; }
    public ArsenalCategory category() { return category; }
    public int canonConfidence() { return canonConfidence; }
    public String evolutionChainId() { return evolutionChainId; }
    public float unlockThreshold() { return unlockThreshold; }
    public String source() { return source; }

    // ── Canon state (NBT-backed) ──────────────────────────────────────

    /** The NBT key storing the current canon state index. */
    public static final String NBT_EVOLUTION_STAGE = "Ergen.CanonState";
    /** The NBT key storing whether this item is at its last documented canon state. */
    public static final String NBT_MAX_EVOLVED = "Ergen.AtLastDocumentedState";

    /** Get the current canon state index of a stack. 0 = acquisition state. */
    public int getEvolutionStage(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) return 0;
        return stack.getTag().getInt(NBT_EVOLUTION_STAGE);
    }

    /** Set the canon state index on a stack. Called by {@link ItemEvolutionChain#evolve}. */
    public void setEvolutionStage(ItemStack stack, int stage) {
        stack.getOrCreateTag().putInt(NBT_EVOLUTION_STAGE, Math.max(0, stage));
        ItemEvolutionChain chain = ItemEvolutionRegistry.get(evolutionChainId);
        if (chain != null && stage >= chain.getMaxStage()) {
            stack.getOrCreateTag().putBoolean(NBT_MAX_EVOLVED, true);
        }
    }

    /** Whether this stack is at its last documented canon state. */
    public boolean isMaxEvolved(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) return false;
        return stack.getTag().getBoolean(NBT_MAX_EVOLVED);
    }

    // ── Tooltip ───────────────────────────────────────────────────────

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        // Canon name (CN + EN)
        if (nameCn != null && !nameCn.isEmpty()) {
            tooltip.add(Component.literal(nameCn).withStyle(ChatFormatting.GOLD)
                    .append(Component.literal("  " + nameEn).withStyle(ChatFormatting.GRAY)));
        } else {
            tooltip.add(Component.literal(nameEn).withStyle(ChatFormatting.GOLD));
        }

        // Category + canon confidence
        tooltip.add(Component.literal("Category: ")
                .withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(category.label).withStyle(category.color)));
        tooltip.add(Component.literal("Canon Confidence: ")
                .withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(confidenceLabel(canonConfidence))
                        .withStyle(confidenceColor(canonConfidence))));

        // Canon state — the new event-driven framing
        ItemEvolutionChain chain = ItemEvolutionRegistry.get(evolutionChainId);
        if (chain != null && chain.hasMultipleStates()) {
            // Multi-state canon history
            int state = getEvolutionStage(stack);
            int max = chain.getMaxStage();
            ChatFormatting stateColor = (state >= max) ? ChatFormatting.GOLD : ChatFormatting.AQUA;
            tooltip.add(Component.literal("Canon State: ")
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal((state + 1) + " / " + (max + 1) + " documented")
                            .withStyle(stateColor)));
            ItemEvolutionChain.CanonState current = chain.getStage(state);
            if (current != null) {
                tooltip.add(Component.literal("  ▸ ").withStyle(ChatFormatting.DARK_GRAY)
                        .append(Component.literal(current.stateName()).withStyle(ChatFormatting.AQUA)));
                if (!current.canonEvent().isBlank()) {
                    tooltip.add(Component.literal("  ▸ Event: " + current.canonEvent())
                            .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
                }
                // Provenance citation
                tooltip.add(Component.literal("  ▸ " + current.provenance().citation())
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
            if (state < max) {
                ItemEvolutionChain.CanonState next = chain.getStage(state + 1);
                if (next != null) {
                    tooltip.add(Component.literal("Next documented state: ").withStyle(ChatFormatting.DARK_GRAY)
                            .append(Component.literal(next.stateName()).withStyle(ChatFormatting.YELLOW)));
                    if (!next.canonEvent().isBlank()) {
                        tooltip.add(Component.literal("  ▸ Event: " + next.canonEvent())
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
                    }
                }
            }
        } else {
            // Single canonical state — the item is complete as-is
            tooltip.add(Component.literal("Canon State: ")
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal("single documented form").withStyle(ChatFormatting.GOLD)));
            tooltip.add(Component.literal("  (This item exists in one canonical form. ")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                    .append(Component.literal("No invented upgrades.)")
                            .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)));
        }

        // Source citation
        if (source != null && !source.isEmpty()) {
            tooltip.add(Component.literal("Source: " + source).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }

        // Canon ID (for developers / commands)
        if (flag.isAdvanced()) {
            tooltip.add(Component.literal("CanonID: " + canonId).withStyle(ChatFormatting.DARK_GRAY));
        }

        // ── Wang Lin Master Registry enrichment ─────────────────────
        // If this item has a corresponding entry in the canonical knowledge
        // graph, append the registry's canon citation, canon summary, ownership
        // state, and behavioral-spec availability flag. This gives every arsenal
        // item a canon-grounded tooltip that references the full knowledge graph
        // (482+ entries) and the 13+ reverse-engineered behavioral specs.
        CanonicalEntry entry = WangLinMasterRegistry.lookup(canonId);
        if (entry != null) {
            tooltip.add(Component.literal("")
                    .withStyle(ChatFormatting.DARK_GRAY));
            // Canon citation — the source-novel chapter attestation, e.g.
            // "Canon: RI Ch. 8 [EXPLICIT 5/5]" (gray italic, per task spec).
            tooltip.add(Component.literal("Canon: " + entry.provenance().citation())
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            // Canon summary from the knowledge graph
            if (!entry.canonSummary().isEmpty()) {
                // Truncate to 2 lines max for tooltip readability
                String summary = entry.canonSummary();
                if (summary.length() > 120) {
                    summary = summary.substring(0, 117) + "...";
                }
                tooltip.add(Component.literal(summary)
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            }
            // Ownership state
            tooltip.add(Component.literal("Ownership: ")
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal(entry.ownership().description)
                            .withStyle(ChatFormatting.GOLD)));
            // Behavioral spec availability — subtle indicator that this entry
            // has a reverse-engineered observable-mechanics spec attached.
            if (entry.hasBehaviorSpec()) {
                tooltip.add(Component.literal("Behavioral spec available")
                        .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC));
            }
            // Registry citation
            tooltip.add(Component.literal("Registry: " + entry.citation())
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static String confidenceLabel(int conf) {
        return switch (conf) {
            case 5 -> "5/5 Direct Canon";
            case 4 -> "4/5 Canonical Implication";
            case 3 -> "3/5 Reasonable Reconstruction";
            case 2 -> "2/5 Speculation";
            default -> "1/5 Low Confidence";
        };
    }

    private static ChatFormatting confidenceColor(int conf) {
        return switch (conf) {
            case 5 -> ChatFormatting.GREEN;
            case 4 -> ChatFormatting.DARK_GREEN;
            case 3 -> ChatFormatting.YELLOW;
            case 2 -> ChatFormatting.GOLD;
            default -> ChatFormatting.RED;
        };
    }
}
