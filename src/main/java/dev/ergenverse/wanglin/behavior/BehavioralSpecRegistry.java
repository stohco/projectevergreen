package dev.ergenverse.wanglin.behavior;

import dev.ergenverse.core.Ergenverse;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * BehavioralSpecRegistry — the registry of all canonical behavioral specs.
 *
 * <p>Loads the 12+ signature-item behavioral specs at bootstrap and exposes them
 * for query by canonicalId. The specs are the reverse-engineered observable
 * mechanics the user demanded: acquisition, lore, prerequisites, cost, activation,
 * range, scaling, counters, weaknesses, environmental effects, visual description,
 * states, system interactions, and Minecraft implementation notes.
 */
public final class BehavioralSpecRegistry {

    private BehavioralSpecRegistry() {}

    private static final Map<String, BehavioralSpec> SPECS = new LinkedHashMap<>();
    private static volatile boolean bootstrapped = false;

    public static synchronized void bootstrap() {
        if (bootstrapped) return;
        bootstrapped = true;

        Ergenverse.LOGGER.info("[BehavioralSpecRegistry] ════════════════════════════════════════════");
        Ergenverse.LOGGER.info("[BehavioralSpecRegistry]  Bootstrapping behavioral specifications...");
        Ergenverse.LOGGER.info("[BehavioralSpecRegistry]  (Reverse-engineered observable mechanics)");
        Ergenverse.LOGGER.info("[BehavioralSpecRegistry] ════════════════════════════════════════════");

        register(RestrictionFlagSpec.SPEC);
        register(SoulRefiningSpec.SPEC);
        register(HeavenDefyingBeadSpec.SPEC);
        register(QingLinInheritanceSpec.SPEC);
        register(SituNanInheritanceSpec.SPEC);
        register(TuSiRemnantsSpec.SPEC);
        register(HeavenTramplingBridgesSpec.SPEC);
        register(WangLinFlyingSwordsSpec.SPEC);
        register(DevilArmorSpec.SPEC);
        register(WangLinStorageTreasuresSpec.SPEC);
        register(WangLinPuppetsSpec.SPEC);
        register(WangLinAncientGodPowersSpec.SPEC);
        register(KarmaWhipSpec.SPEC);

        Ergenverse.LOGGER.info("[BehavioralSpecRegistry] ──────────────────────────────────────");
        Ergenverse.LOGGER.info("[BehavioralSpecRegistry]  {} behavioral specs loaded.", SPECS.size());
        Ergenverse.LOGGER.info("[BehavioralSpecRegistry]  Specs: {}", String.join(", ", SPECS.keySet()));
        Ergenverse.LOGGER.info("[BehavioralSpecRegistry]  Each spec decomposes a signature item into:");
        Ergenverse.LOGGER.info("[BehavioralSpecRegistry]    acquisition, lore, prerequisites, cost, activation,");
        Ergenverse.LOGGER.info("[BehavioralSpecRegistry]    range, scaling, counters, weaknesses, env effects,");
        Ergenverse.LOGGER.info("[BehavioralSpecRegistry]    visual, states, interactions, MC impl notes.");
        Ergenverse.LOGGER.info("[BehavioralSpecRegistry] ════════════════════════════════════════════");
    }

    private static void register(BehavioralSpec spec) {
        if (spec == null) return;
        if (SPECS.containsKey(spec.canonicalId())) {
            throw new IllegalStateException("Duplicate BehavioralSpec id '" + spec.canonicalId() + "'");
        }
        SPECS.put(spec.canonicalId(), spec);
    }

    /** Look up a spec by canonicalId. Returns null if not present. */
    public static BehavioralSpec get(String canonicalId) {
        if (!bootstrapped) bootstrap();
        return SPECS.get(canonicalId);
    }

    /** All specs (immutable). */
    public static List<BehavioralSpec> all() {
        if (!bootstrapped) bootstrap();
        return List.copyOf(SPECS.values());
    }

    /** Count. */
    public static int size() {
        if (!bootstrapped) bootstrap();
        return SPECS.size();
    }
}
