package dev.ergenverse.wanglin;

import dev.ergenverse.canon.Provenance;
import dev.ergenverse.core.Ergenverse;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * ItemEvolutionChain — the <b>Historical State Machine</b> for a Wang Lin arsenal item.
 *
 * <h2>REWRITTEN — Three-Layer Architecture (Canon Layer)</h2>
 * <p>This class has been rewritten per the user's architectural directive. It is now
 * a <b>canon-only Historical State Machine</b>, NOT an XP upgrade chain. The changes:
 * <ul>
 *   <li><b>DELETED:</b> the {@code bridgingAlternatives} field. There are no
 *       "bridging alternatives" in canon. If a primary canon material is exhausted,
 *       that is a gap recorded in {@link dev.ergenverse.core.ErgenConfig#CANON_STRICT_MODE},
 *       NOT an excuse to invent a substitute.</li>
 *   <li><b>DELETED:</b> the {@code validateNoDeadEnds()} enforcement. The old
 *       "no locked upgrades" directive has been REPLACED with the honest
 *       formulation: <i>"Every canonical state must be obtainable."</i> An item
 *       with one canon state has one state. Nothing is missing.</li>
 *   <li><b>ADDED:</b> a {@link Provenance} on every state — recording the source
 *       novel, chapter(s), EXPLICIT/INFERRED attestation, confidence (1-5), and
 *       any ambiguities.</li>
 *   <li><b>ADDED:</b> the {@code canonEvent} is now the PRIMARY driver of state
 *       transitions. States change because events happen, not because XP is reached.</li>
 *   <li><b>MOVED:</b> the Manifestation Gift / Bridging Policy concepts to the
 *       Simulation layer ({@link dev.ergenverse.simulation.affinity}). They are
 *       no longer referenced here.</li>
 * </ul>
 *
 * <h2>What this class IS now</h2>
 * <p>An ordered list of {@link CanonState} records — each a single canon-documented
 * form the item took during the Renegade Immortal narrative. Stage 0 is the state
 * when Wang Lin first acquired the item. Each subsequent stage represents a
 * canon-documented transformation tied to a specific narrative event.
 *
 * <h2>What this class is NOT</h2>
 * <ul>
 *   <li>NOT a progression guide. We do not add "Peak" / "Awakened" / "Ascended"
 *       stages Er Gen never wrote.</li>
 *   <li>NOT always multi-stage. Most items have exactly ONE canon state. A
 *       one-stage chain means "this item existed in one form, period."</li>
 *   <li>NOT always walkable to a "max" state. If an item has one state, that
 *       one state IS the complete item.</li>
 * </ul>
 *
 * <h2>Examples of canon multi-state histories</h2>
 * <ul>
 *   <li><b>Core-Treasure Sword → Dark Green Flying Sword</b>: a documented
 *       blood-refinement evolution (one transformation, two states).</li>
 *   <li><b>Fragment Stamp → 18-Hell Celestial Sealing Stamp</b>: refined from
 *       a fragment via divine tribulation (one transformation, two states).</li>
 *   <li><b>Soul Lasher + Karma Domain → Karma Whip</b>: a fusion (one
 *       transformation, two states — the pre-fusion whip is a separate item).</li>
 * </ul>
 * <p>Items with NO documented transformation (the majority) have a single-state
 * history or no chain at all.
 */
public class ItemEvolutionChain {

    /** The cultivation realm associated with a canon state (contextual, not a gate). */
    public enum RequiredRealm {
        MORTAL(0, "Mortal"),
        QI_CONDENSATION(1, "Qi Condensation"),
        FOUNDATION_ESTABLISHMENT(2, "Foundation Establishment"),
        CORE_FORMATION(3, "Core Formation"),
        NASCENT_SOUL(4, "Nascent Soul"),
        SOUL_FORMATION(5, "Soul Formation"),
        SOUL_TRANSFORMATION(6, "Soul Transformation"),
        ASCENDANT(7, "Ascendant"),
        ILLUSORY_YIN(8, "Illusory Yin"),
        CRIMSON_YIN(9, "Crimson Yin"),
        ASPIRANT(10, "Aspirant Celestial"),
        CELESTIAL(11, "Celestial"),
        CELESTIAL_LORD(12, "Celestial Lord"),
        VOID_TREADING(13, "Void Treading"),
        ARCHAID(14, "Archaid"),
        THIRD_STEP(15, "Third Step"),
        FOURTH_STEP(16, "Fourth Step"),
        FIFTH_STEP(17, "Fifth Step / Transcendence");

        public final int tier;
        public final String label;
        RequiredRealm(int tier, String label) { this.tier = tier; this.label = label; }
    }

    /**
     * A single canon-documented state of an arsenal item.
     *
     * <p>Each state is tied to a specific canon event (the narrative moment when
     * the item entered this state). The {@link Provenance} records exactly where
     * in the novel this state is attested.
     *
     * <p>There is NO "bridgingAlternatives" field. If the canon materials for a
     * state are exhausted at edge-of-canon, that is a recorded gap, not an
     * invented substitute. The Simulation-layer Affinity System (future) may
     * provide alternative paths, but those live in Layer 2, never here.
     */
    public record CanonState(
            int stateIndex,
            String stateName,           // canonical name of this form
            String stateNameCn,         // canonical Chinese name
            RequiredRealm contextRealm, // the realm Wang Lin was at when this state was attained (context, not a gate)
            String canonEvent,          // the narrative event that produced this state
            Provenance provenance,      // source novel, chapters, EXPLICIT/INFERRED, confidence, ambiguities
            String description
    ) {
        public CanonState {
            if (stateName == null || stateName.isBlank()) {
                throw new IllegalArgumentException("CanonState requires a stateName");
            }
            if (provenance == null) {
                throw new IllegalArgumentException("CanonState requires a Provenance — every canon fact must cite its source");
            }
            if (contextRealm == null) contextRealm = RequiredRealm.MORTAL;
            if (canonEvent == null) canonEvent = "";
            if (stateNameCn == null) stateNameCn = "";
            if (description == null) description = "";
        }

        /** A human-readable summary for tooltips. */
        public String summary() {
            StringBuilder sb = new StringBuilder();
            if (!canonEvent.isBlank()) {
                sb.append("Event: ").append(canonEvent);
            }
            if (contextRealm != RequiredRealm.MORTAL) {
                if (!sb.isEmpty()) sb.append(" | ");
                sb.append("Realm: ").append(contextRealm.label);
            }
            return sb.toString();
        }
    }

    private final String chainId;
    private final String itemName;
    private final List<CanonState> states;

    public ItemEvolutionChain(String chainId, String itemName, List<CanonState> states) {
        if (chainId == null || chainId.isBlank()) {
            throw new IllegalArgumentException("ItemEvolutionChain requires a chainId");
        }
        if (states == null || states.isEmpty()) {
            throw new IllegalArgumentException("Chain '" + chainId + "' must have at least one canon state");
        }
        this.chainId = chainId;
        this.itemName = itemName;
        this.states = new ArrayList<>(states);
        // NOTE: No validateNoDeadEnds(). A single-state chain is a complete, valid chain.
    }

    public String chainId() { return chainId; }
    public String itemName() { return itemName; }

    /** The highest state index. For a single-state chain, this is 0. */
    public int getMaxStage() { return states.size() - 1; }

    /** Whether this chain has more than one canon-documented state. */
    public boolean hasMultipleStates() { return states.size() > 1; }

    public CanonState getStage(int index) {
        if (index < 0 || index >= states.size()) return null;
        return states.get(index);
    }

    public List<CanonState> allStages() { return new ArrayList<>(states); }

    /**
     * Whether a stack can transition to the next canon state.
     * Returns true only if: the chain has multiple states AND the stack is not
     * already at the last documented state.
     * <p>NOTE: This returns false for single-state chains — there is nothing to
     * transition TO. That is correct, not a bug.
     */
    public boolean canEvolve(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (!hasMultipleStates()) return false;
        int current = stack.getOrCreateTag().getInt(WangLinItem.NBT_EVOLUTION_STAGE);
        return current < getMaxStage();
    }

    /**
     * Transition a stack to the next canon state. This applies the state change;
     * the actual in-world cause (a canon event replay, a player refinement, or a
     * Simulation-layer affinity path) is handled elsewhere.
     */
    public boolean evolve(ItemStack stack) {
        if (!canEvolve(stack)) return false;
        int current = stack.getOrCreateTag().getInt(WangLinItem.NBT_EVOLUTION_STAGE);
        int next = current + 1;
        stack.getOrCreateTag().putInt(WangLinItem.NBT_EVOLUTION_STAGE, next);
        if (next >= getMaxStage()) {
            stack.getOrCreateTag().putBoolean(WangLinItem.NBT_MAX_EVOLVED, true);
        }
        Ergenverse.LOGGER.info("[Canon] '{}' state transition: {} → {} ({})",
                itemName, current, next, getStage(next).stateName());
        return true;
    }

    /**
     * Set a stack directly to a specific canon state. Used by creative-mode /
     * admin commands and (future) the Simulation-layer Affinity System.
     */
    public boolean evolveTo(ItemStack stack, int targetState) {
        if (targetState < 0 || targetState > getMaxStage()) return false;
        stack.getOrCreateTag().putInt(WangLinItem.NBT_EVOLUTION_STAGE, targetState);
        if (targetState >= getMaxStage() && hasMultipleStates()) {
            stack.getOrCreateTag().putBoolean(WangLinItem.NBT_MAX_EVOLVED, true);
        } else {
            stack.getOrCreateTag().remove(WangLinItem.NBT_MAX_EVOLVED);
        }
        return true;
    }

    // ── Builder for fluent canon-history construction ─────────────────

    public static Builder builder(String chainId, String itemName) {
        return new Builder(chainId, itemName);
    }

    public static class Builder {
        private final String chainId;
        private final String itemName;
        private final List<CanonState> states = new ArrayList<>();

        Builder(String chainId, String itemName) {
            this.chainId = chainId;
            this.itemName = itemName;
        }

        /**
         * Add the acquisition state (state 0) — the form the item was in when
         * Wang Lin first obtained it. Requires a Provenance citing the acquisition.
         */
        public Builder acquisitionState(String name, String nameCn, RequiredRealm realm,
                                        String canonEvent, Provenance provenance, String description) {
            states.add(new CanonState(0, name, nameCn, realm, canonEvent, provenance, description));
            return this;
        }

        /**
         * Add a subsequent canon-documented state — a transformation that Er Gen
         * explicitly wrote. Requires a Provenance citing the transformation event.
         */
        public Builder canonState(String name, String nameCn, RequiredRealm realm,
                                  String canonEvent, Provenance provenance, String description) {
            int idx = states.size();
            states.add(new CanonState(idx, name, nameCn, realm, canonEvent, provenance, description));
            return this;
        }

        public ItemEvolutionChain build() {
            if (states.isEmpty()) {
                throw new IllegalArgumentException("Chain '" + chainId + "' has no canon states");
            }
            return new ItemEvolutionChain(chainId, itemName, states);
        }
    }
}
