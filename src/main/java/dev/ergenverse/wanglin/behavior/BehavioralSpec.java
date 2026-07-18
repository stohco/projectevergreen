package dev.ergenverse.wanglin.behavior;

import dev.ergenverse.canon.Provenance;

import java.util.List;

/**
 * BehavioralSpec — the universal record for every signature item's observable mechanics.
 *
 * <p>Decomposes a canonical item / technique / inheritance into:
 * <ul>
 *   <li><b>Identity</b> — canonicalId, displayName, displayNameCn, provenance</li>
 *   <li><b>Lore</b> — acquisition, lore (backstory)</li>
 *   <li><b>Prerequisites</b> — cultivation realm, divine sense, materials</li>
 *   <li><b>Cost</b> — {@link ResourceCost} (qi, soul, divine sense, joss flame, life force)</li>
 *   <li><b>Activation</b> — {@link ActivationModel}</li>
 *   <li><b>Range</b> — {@link RangeModel}</li>
 *   <li><b>Scaling</b> — {@link ScalingModel}</li>
 *   <li><b>Counters</b> — what defeats it</li>
 *   <li><b>Weaknesses</b> — exploitable flaws</li>
 *   <li><b>Environmental effects</b> — {@link EnvironmentalEffect}</li>
 *   <li><b>Visual</b> — {@link VisualDescription}</li>
 *   <li><b>States</b> — {@link StateDescription} (idle / activated / damaged / spirit-manifested)</li>
 *   <li><b>Interactions</b> — {@link SystemInteraction}</li>
 *   <li><b>MC impl notes</b> — Minecraft implementation guidance</li>
 *   <li><b>Usage audience</b> — npcUsage (who in canon wields it), playerUsage (how a player uses it
 *       in the mod), aiUsage (how an AI-controlled NPC uses it). These three fields close the gap
 *       the user flagged in the Task RI-FORGE-BEHAVIOR-DEPTH spec: every BehavioralSpec must
 *       explicitly answer "NPC Usage / Player Usage / AI Usage".</li>
 * </ul>
 *
 * <p>Per the Prime Directive: where canon is silent, mark UNKNOWN. Never invent.
 */
public record BehavioralSpec(
        String canonicalId,
        String displayName,
        String displayNameCn,
        Provenance provenance,
        String acquisition,
        String lore,
        List<String> prerequisites,
        ResourceCost cost,
        ActivationModel activation,
        RangeModel range,
        ScalingModel scaling,
        List<String> counters,
        List<String> weaknesses,
        List<EnvironmentalEffect> environmentalEffects,
        VisualDescription visual,
        List<StateDescription> states,
        List<SystemInteraction> interactions,
        String minecraftImplNotes,
        String npcUsage,
        String playerUsage,
        String aiUsage
) {
    public BehavioralSpec {
        if (canonicalId == null || canonicalId.isBlank()) {
            throw new IllegalArgumentException("BehavioralSpec requires a canonicalId");
        }
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("BehavioralSpec '" + canonicalId + "' requires a displayName");
        }
        if (provenance == null) {
            throw new IllegalArgumentException("BehavioralSpec '" + canonicalId + "' requires a Provenance");
        }
        if (acquisition == null) acquisition = "UNKNOWN — canon silent on acquisition";
        if (lore == null) lore = "UNKNOWN — canon silent on lore";
        if (prerequisites == null) prerequisites = List.of();
        if (cost == null) cost = ResourceCost.UNKNOWN;
        if (activation == null) activation = ActivationModel.UNKNOWN;
        if (range == null) range = RangeModel.UNKNOWN;
        if (scaling == null) scaling = ScalingModel.UNKNOWN;
        if (counters == null) counters = List.of();
        if (weaknesses == null) weaknesses = List.of();
        if (environmentalEffects == null) environmentalEffects = List.of();
        if (visual == null) visual = VisualDescription.UNKNOWN;
        if (states == null) states = List.of();
        if (interactions == null) interactions = List.of();
        if (minecraftImplNotes == null) minecraftImplNotes = "";
        if (displayNameCn == null) displayNameCn = "";
        if (npcUsage == null) npcUsage = "UNKNOWN — canon silent on NPC usage";
        if (playerUsage == null) playerUsage = "UNKNOWN — player usage not specified";
        if (aiUsage == null) aiUsage = "UNKNOWN — AI usage not specified";
    }
}
