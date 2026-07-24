package dev.ergenverse.simulation.residence;

import java.util.Collections;
import java.util.List;

/**
 * ResidentProfile — the life-authored source of truth for a residence.
 *
 * <p>This is the user's 2026-07-26 authorship pivot, stated verbatim:
 *
 * <blockquote>
 * You don't author buildings. You author people. […] A residence is not a
 * schematic. It is a simulation object. […] The house is literally generated
 * from the resident. Not the other way around.
 * </blockquote>
 *
 * <p>A ResidentProfile describes a person's life in enough detail that their
 * residence can be <b>derived</b> from it — not placed. The profile lists the
 * person's needs (what their life demands of their home), their habits (how
 * they use space), their inventory (what objects they own), their fears (what
 * defenses they build), their history (what memories are attached to which
 * locations), and their relationships (who else uses this space).
 *
 * <p>The {@link ResidenceManifestBuilder} reads a ResidentProfile and produces
 * a {@link ResidenceManifest} — the semantic description of the house. The
 * manifest is canon; the block placement is derived. If you change the profile,
 * the house changes. If you change the building rules, all houses change
 * consistently. The profile is the authorship layer; the blocks are the
 * rendering layer.
 *
 * <h2>Canon vs simulation</h2>
 *
 * <p>Canon residents (Wang Lin, Old Chen, Wang Lin's parents) carry profiles
 * authored from the novels. Their {@code canonSourced} flag is true. Simulation
 * residents (generic villagers) carry generated profiles that never contradict
 * canon. The distinction matters: a canon resident's profile is law; a
 * simulation resident's profile is a reasonable guess.
 *
 * <h2>What this is NOT</h2>
 *
 * <p>This is NOT the {@link dev.ergenverse.simulation.settlement.ActorProfile}
 * — that class is the cognitive lens (how the actor reasons about a
 * WorldSituation). This class is the <b>domestic</b> profile (what the actor's
 * home contains). They are separate concerns: one actor has both. Wang Lin
 * reasons about wolves via his ActorProfile; his house is derived from his
 * ResidentProfile. The two profiles share an actorId but describe different
 * aspects of the same person.
 *
 * <p>USER-DIRECTED. The 2026-07-26 review explicitly named this inversion and
 * gave Wang Lin, Old Chen, Situ Nan, and Li Muwan as canonical examples of
 * residences that emerge from lives.
 *
 * @param residentId       stable identifier (e.g. "wang_lin", "old_chen")
 * @param displayName      human-readable name
 * @param settlementId     which settlement this resident lives in
 * @param occupation       what they do (e.g. "hidden_cultivator", "farmer", "elder")
 * @param personalityTraits how they approach life (e.g. "cautious", "observant")
 * @param cultivationStyle their cultivation path (e.g. "none", "qi_condensation", "foundation")
 * @param needs            what their life demands of their home — drives room generation
 * @param inventory        semantic objects they own (e.g. "flying_sword", "tea_set")
 * @param fears            what they defend against (e.g. "revealing_strength")
 * @param habits           how they use space (e.g. "observes_from_roof")
 * @param relationships    who else is connected to this residence
 * @param history          memories attached to locations in the residence
 * @param canonSourced     true if this profile is authored from the novels
 */
public record ResidentProfile(
        String residentId,
        String displayName,
        String settlementId,
        String occupation,
        List<String> personalityTraits,
        String cultivationStyle,
        List<NeedCategory> needs,
        List<String> inventory,
        List<String> fears,
        List<String> habits,
        List<String> relationships,
        List<ResidenceMemory> history,
        boolean canonSourced
) {
    public ResidentProfile {
        if (residentId == null || residentId.isBlank()) throw new IllegalArgumentException("residentId");
        if (displayName == null || displayName.isBlank()) throw new IllegalArgumentException("displayName");
        if (settlementId == null || settlementId.isBlank()) throw new IllegalArgumentException("settlementId");
        if (occupation == null || occupation.isBlank()) throw new IllegalArgumentException("occupation");
        personalityTraits = personalityTraits == null ? List.of() : Collections.unmodifiableList(List.copyOf(personalityTraits));
        cultivationStyle = cultivationStyle == null ? "none" : cultivationStyle;
        needs = needs == null ? List.of() : Collections.unmodifiableList(List.copyOf(needs));
        inventory = inventory == null ? List.of() : Collections.unmodifiableList(List.copyOf(inventory));
        fears = fears == null ? List.of() : Collections.unmodifiableList(List.copyOf(fears));
        habits = habits == null ? List.of() : Collections.unmodifiableList(List.copyOf(habits));
        relationships = relationships == null ? List.of() : Collections.unmodifiableList(List.copyOf(relationships));
        history = history == null ? List.of() : Collections.unmodifiableList(List.copyOf(history));
    }

    /** True if this resident has a specific need. */
    public boolean hasNeed(NeedCategory need) {
        return needs.contains(need);
    }

    /** True if this resident has a specific personality trait. */
    public boolean hasTrait(String trait) {
        return personalityTraits.stream().anyMatch(t -> t.equalsIgnoreCase(trait));
    }
}
