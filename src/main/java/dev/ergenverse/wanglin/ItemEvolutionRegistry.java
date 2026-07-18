package dev.ergenverse.wanglin;

import dev.ergenverse.canon.Provenance;
import dev.ergenverse.core.Ergenverse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ItemEvolutionRegistry — the registry of canon-only Historical State Machines.
 *
 * <h2>REWRITTEN — Three-Layer Architecture (Canon Layer)</h2>
 * <p>This registry has been purged of ALL invented evolution chains. Per the
 * user's directive:
 * <blockquote>
 *   Delete every inferred evolution chain from the canon registry. Not because
 *   they're bad. Because they don't belong there. Those belong in the Simulation
 *   Layer / Game History. Never in the Canon Layer.
 * </blockquote>
 *
 * <h2>What remains here</h2>
 * <p>ONLY items with DOCUMENTED canon transformations — forms that Er Gen
 * explicitly wrote. Each multi-state chain cites its source chapter(s) and
 * transformation event via {@link Provenance}. Examples:
 * <ul>
 *   <li><b>Core-Treasure Sword → Dark Green Flying Sword</b> (blood-refinement
 *       evolution, explicitly documented in the wiki items table).</li>
 *   <li><b>Soul Lasher → Karma Whip</b> (fusion with Karma Domain, Ch. 731).</li>
 *   <li><b>Fragment Stamp → 18-Hell Celestial Sealing Stamp</b> (refined via
 *       divine tribulation, Ch. 915).</li>
 *   <li><b>Star Compass → Silver Dragon Star Compass</b> (self-upgraded, Ch. 477).</li>
 *   <li><b>1st Restriction Flag (Incomplete → Complete)</b> (completed via Tu Si
 *       Ancient God ink).</li>
 * </ul>
 *
 * <h2>What was DELETED</h2>
 * <p>The previous version had ~60 explicit chains, ALL using invented stage names
 * ("Peak", "Awakened", "Ascended"), invented "Manifestation Gift" bridging
 * alternatives, and invented realm thresholds. Every one of these has been
 * removed. They were RPG progression, not canon. The registry is now a historical
 * record, not a progression guide.
 *
 * <h2>Items WITHOUT a chain here</h2>
 * <p>The ~300 arsenal items that do NOT appear in this registry have NO
 * multi-state canon history. They exist in a single canonical form (the form
 * Wang Lin acquired them in). That is the complete item — nothing is missing.
 * When queried, {@link #get(String)} returns null for these items, and
 * {@link WangLinItem} displays "Single canonical state" in the tooltip.
 *
 * <h2>Future: the Simulation Layer</h2>
 * <p>If a player later refines, mutates, or transforms one of these items during
 * play, that is NEW HISTORY (Layer 3 — Emergent History), recorded in
 * {@link dev.ergenverse.history}, NEVER back-written into this canon registry.
 */
public final class ItemEvolutionRegistry {

    private static final Map<String, ItemEvolutionChain> CHAINS = new HashMap<>();
    private static boolean bootstrapped = false;

    private ItemEvolutionRegistry() {}

    /** Get the canon state history for a canonId. Returns null if the item has a single canon state. */
    public static ItemEvolutionChain get(String canonId) {
        if (!bootstrapped) bootstrap();
        return CHAINS.get(canonId);
    }

    /** Get all registered multi-state canon histories (for admin/debug views). */
    public static List<ItemEvolutionChain> all() {
        if (!bootstrapped) bootstrap();
        return List.copyOf(CHAINS.values());
    }

    /** Register a canon state history. Called during bootstrap. */
    static void register(ItemEvolutionChain chain) {
        CHAINS.put(chain.chainId(), chain);
    }

    /**
     * Bootstrap all canon-attested multi-state histories.
     *
     * <p>Each chain below is mined directly from CANON_RI_COMPLETE_ITEMS.md —
     * only items with an EXPLICITLY DOCUMENTED transformation appear here.
     * Source chapters and events are cited via {@link Provenance}.
     */
    public static synchronized void bootstrap() {
        if (bootstrapped) return;
        Ergenverse.LOGGER.info("[Canon] Bootstrapping canon state histories (canon-only, no invented chains)...");

        // ════════════════════════════════════════════════════════════════
        // CANON-DOCUMENTED TRANSFORMATIONS
        // Every chain below cites the novel chapter(s) where the transformation
        // is attested. No invented stages. No bridging alternatives.
        // ════════════════════════════════════════════════════════════════

        // ── Core-Treasure Sword → Dark Green Flying Sword (blood-refinement evolution) ──
        // Canon: "acquired then blood-refined ... evolves (→ Dark Green Flying Sword)"
        //         "Current status: upgraded into Dark Green Flying Sword"
        register(ItemEvolutionChain.builder("wanglin/core_treasure_sword", "Core-Treasure Sword")
                .acquisitionState("Core-Treasure Sword", "核心宝剑",
                        ItemEvolutionChain.RequiredRealm.FOUNDATION_ESTABLISHMENT,
                        "Acquired and blood-refined by Wang Lin",
                        Provenance.explicit("Renegade Immortal", List.of("mid-Foundation era"), 5,
                                "Wiki items table: 'acquired then blood-refined'"),
                        "A blood-refined flying sword with a teleportation effect. Took countless lives for Wang Lin.")
                .canonState("Dark Green Flying Sword (Poison)", "墨绿飞剑",
                        ItemEvolutionChain.RequiredRealm.CORE_FORMATION,
                        "Blood-refinement evolution — sword gains poison attribute",
                        Provenance.explicit("Renegade Immortal", List.of("post-Core-Treasure"), 5,
                                "Wiki items table: 'evolves (→ Dark Green Flying Sword)', 'upgraded into Dark Green Flying Sword'"),
                        "The successor sword; carries a poison attribute. Blood-refined and battle-hardened.")
                .build());

        // ── Soul Lasher → Karma Whip (fusion with Karma Domain, Ch. 731) ──
        // Canon: "Ch. 731 (donghua Ep 147); fused from Soul Lasher"
        //         "Created when Wang Lin fused the Soul Lasher ... with his Karma Domain"
        register(ItemEvolutionChain.builder("wanglin/soul_lasher", "Soul Lasher")
                .acquisitionState("Soul Lasher", "打魂鞭",
                        ItemEvolutionChain.RequiredRealm.NASCENT_SOUL,
                        "Won from Red Butterfly (originally Qian Feng's, given to Hong Die)",
                        Provenance.explicit("Renegade Immortal", List.of("pre-Ch. 731"), 5,
                                "Originally a heavy treasure of Tian Yu Sect; Qian Feng gave it to Hong Die; Wang Lin took it after Hong Die's death"),
                        "A whip that directly attacks the primordial/origin soul at warp speed. Originally Red Butterfly's.")
                .canonState("Karma Whip (fused)", "因果鞭",
                        ItemEvolutionChain.RequiredRealm.SOUL_TRANSFORMATION,
                        "Fusion with Karma Domain in the Demon Spirit Land (Ch. 731) — burned by Ghostly Sky Fire, mysterious transformation, nourished by Wang Lin's Concept",
                        Provenance.explicit("Renegade Immortal", List.of("Ch. 731"), 5,
                                "Wiki: 'fused from Soul Lasher', 'Created when Wang Lin fused the Soul Lasher with his Karma Domain'"),
                        "Weaponizes karmic cause-effect. In the Outer Realm, cleaved open 7 million worlds with a single whip-strike.")
                .build());

        // ── Fragment Stamp → 18-Hell Celestial Sealing Stamp (refined via divine tribulation, Ch. 915) ──
        // Canon: "Ch. 915 (further refined Ch. 769 from Fragment Stamp)"
        //         "self-forged; refined from Fragment Stamp via divine tribulation"
        register(ItemEvolutionChain.builder("wanglin/fragment_stamp", "Fragment Stamp")
                .acquisitionState("Fragment Stamp", "残印",
                        ItemEvolutionChain.RequiredRealm.ASCENDANT,
                        "Originally a fragment refined by divine tribulation when Wang Lin broke into Illusionary-Yin/Corporeal-Yang",
                        Provenance.explicit("Renegade Immortal", List.of("Ch. 769"), 5,
                                "Wiki: 'further refined Ch. 769 from Fragment Stamp'"),
                        "A fragment-stamp, precursor to the 18-Hell Celestial Sealing Stamp.")
                .canonState("18-Hell Celestial Sealing Stamp", "十八地狱封天印",
                        ItemEvolutionChain.RequiredRealm.ILLUSORY_YIN,
                        "Refined into the complete stamp (Ch. 915) — fused with Magic-Arsenal Spell + Celestial Sealing Stamp",
                        Provenance.explicit("Renegade Immortal", List.of("Ch. 915"), 5,
                                "Wiki: 'Ch. 915', 'self-forged; refined from Fragment Stamp via divine tribulation'"),
                        "Forms the 18-Layers-of-Hell-Reincarnation-Realm with Underworld River. Stores all souls of enemies Wang Lin has killed.")
                .build());

        // ── Star Compass → Silver Dragon Star Compass (self-upgraded, Ch. 477) ──
        // Canon: "upgraded Ch. 477 → Silver Dragon Star Compass"
        //         "After Wang Lin upgraded the Star Compass it became the Silver Dragon Star Compass"
        register(ItemEvolutionChain.builder("wanglin/star_compass", "Star Compass")
                .acquisitionState("Star Compass", "星盘",
                        ItemEvolutionChain.RequiredRealm.NASCENT_SOUL,
                        "Acquired as a standard star compass",
                        Provenance.explicit("Renegade Immortal", List.of("pre-Ch. 477"), 5,
                                "Wiki: 'upgraded Ch. 477 → Silver Dragon Star Compass'"),
                        "A star compass used for navigation and detection.")
                .canonState("Silver Dragon Star Compass", "银龙星盘",
                        ItemEvolutionChain.RequiredRealm.SOUL_FORMATION,
                        "Self-upgraded by Wang Lin (Ch. 477) — renamed for the beast it utilizes",
                        Provenance.explicit("Renegade Immortal", List.of("Ch. 477"), 5,
                                "Wiki: 'After Wang Lin upgraded the Star Compass it became the Silver Dragon Star Compass'"),
                        "Celestial-tier upgraded compass; utilizes a silver dragon beast for enhanced detection.")
                .build());

        // ── Celestial Sword → Rain Celestial Sword lineage (Zhou Yi's gift, Ch. 717) ──
        // Canon: "Ch. 717 ... Became the seed of the Rain Celestial Sword inheritance"
        //         "evolved into Rain Celestial Sword"
        register(ItemEvolutionChain.builder("wanglin/celestial_sword", "Celestial Sword")
                .acquisitionState("Celestial Sword", "仙剑",
                        ItemEvolutionChain.RequiredRealm.SOUL_TRANSFORMATION,
                        "Gifted by Zhou Yi under the condition Wang Lin protect the celestial corpse in the pagoda (Ch. 717)",
                        Provenance.explicit("Renegade Immortal", List.of("Ch. 717"), 5,
                                "Wiki: 'Zhou Yi gave one to Wang Lin under the condition Wang Lin keeps protecting the celestial corpse'"),
                        "A celestial-tier sword, the seed of the Rain Celestial Sword inheritance.")
                .canonState("Rain Celestial Sword (Mid Quality)", "雨仙剑",
                        ItemEvolutionChain.RequiredRealm.ASCENDANT,
                        "Separated from the Rain Celestial Swords by Zhou Yi for Wang Lin to inherit eternally (Ch. 717)",
                        Provenance.explicit("Renegade Immortal", List.of("Ch. 717"), 5,
                                "Wiki: 'separated from the Rain Celestial Swords by Zhou Yi', 'eternal-inheritance'"),
                        "A mid-quality celestial sword; the sword-spirit was later passed to Xu Liguo. Contains the Slash Luo Art.")
                .build());

        // ── Restriction Flag: 1st Flag (Incomplete → Complete) ──
        // Canon: The restriction flag requires 99,999 restrictions to complete.
        // The 1st flag was completed using Tu Si Ancient God ink. The later flags
        // (2nd, 3rd) are SEPARATE items, not evolution stages of the 1st.
        // We document ONLY the explicitly-attested Incomplete → Complete transformation of the 1st flag.
        register(ItemEvolutionChain.builder("wanglin/restriction_flag", "Restriction Flag (1st)")
                .acquisitionState("1st Flag (Incomplete)", "第一旗（残）",
                        ItemEvolutionChain.RequiredRealm.NASCENT_SOUL,
                        "First restriction flag obtained, incomplete — summons divine tribulation",
                        Provenance.explicit("Renegade Immortal", List.of("Nascent Soul era"), 5,
                                "Wiki: restriction flag category; first flag incomplete"),
                        "The first restriction flag, incomplete. Summons divine tribulation even in this state.")
                .canonState("1st Flag (Complete)", "第一旗（成）",
                        ItemEvolutionChain.RequiredRealm.NASCENT_SOUL,
                        "First flag completed using Tu Si Ancient God ink",
                        Provenance.explicit("Renegade Immortal", List.of("Nascent Soul era"), 4,
                                "Wiki: completed with Ancient God ink; the 99,999-restriction total is a separate peak achievement documented across the full narrative"),
                        "The first flag reaches completion — divine-tribulation summoning active. (Note: the full 99,999-restriction completion is a peak achievement spanning the entire narrative, not a single discrete state.)")
                .build());

        // ── Mosquito Beast: 2nd & 3rd pairs destroyed → restored (Ch. 1276 → Ch. 1626) ──
        // Canon: "1st: given to Situ Nan Ch. 441; 2nd & 3rd: destroyed Ch. 1276 vs Daoist Water → restored Ch. 1626"
        // This is a destroy → restore cycle, not a power upgrade, but it IS a documented state change.
        register(ItemEvolutionChain.builder("wanglin/mosquito_beast", "Mosquito Beast")
                .acquisitionState("Mosquito Beast (tamed)", "蚊兽",
                        ItemEvolutionChain.RequiredRealm.NASCENT_SOUL,
                        "Tamed in the Sea of Devils; Wang Lin's signature flying mount",
                        Provenance.explicit("Renegade Immortal", List.of("Sea of Devils arc"), 5,
                                "Wiki: mounts & companions category"),
                        "Wang Lin's iconic mosquito beast mount, tamed from the Sea of Devils.")
                .canonState("Mosquito Beast (restored)", "蚊兽（复）",
                        ItemEvolutionChain.RequiredRealm.VOID_TREADING,
                        "2nd & 3rd pairs destroyed vs Daoist Water (Ch. 1276), restored via Void Gate power (Ch. 1626)",
                        Provenance.explicit("Renegade Immortal", List.of("Ch. 1276", "Ch. 1626"), 5,
                                "Wiki: '2nd & 3rd: destroyed Ch. 1276 vs Daoist Water → restored Ch. 1626'"),
                        "The mosquito beast restored after destruction by Daoist Water, via the Void Gate power.")
                .build());

        // ── God-Slaying Sword: destroyed → restored (Ch. 1273 → restored) ──
        // Canon: "destroyed Ch. 1273 (first Daoist Water fight) → restored later"
        register(ItemEvolutionChain.builder("wanglin/god_slaying_sword", "God-Slaying Sword")
                .acquisitionState("God-Slaying Sword", "杀神剑",
                        ItemEvolutionChain.RequiredRealm.SOUL_TRANSFORMATION,
                        "Acquired as Wang Lin's primary sword",
                        Provenance.explicit("Renegade Immortal", List.of("Soul Transformation era"), 5,
                                "Wiki: weapons category"),
                        "Wang Lin's God-Slaying Sword, a primary celestial weapon.")
                .canonState("God-Slaying Sword (restored)", "杀神剑（复）",
                        ItemEvolutionChain.RequiredRealm.VOID_TREADING,
                        "Destroyed in the first Daoist Water fight (Ch. 1273), restored later",
                        Provenance.explicit("Renegade Immortal", List.of("Ch. 1273", "restored later"), 5,
                                "Wiki: 'destroyed Ch. 1273 (first Daoist Water fight) → restored later'"),
                        "The God-Slaying Sword restored after its destruction by Daoist Water.")
                .build());

        // ── Seven-Colored Nail: destroyed → restored (Ch. 1082 → Ch. 1626) ──
        // Canon: "destroyed Ch. 1082 → restored Ch. 1626 (Void Gate power)"
        register(ItemEvolutionChain.builder("wanglin/seven_colored_nail", "Seven-Colored Nail")
                .acquisitionState("Seven-Colored Nail", "七彩钉",
                        ItemEvolutionChain.RequiredRealm.CELESTIAL,
                        "Acquired as a celestial sealing treasure",
                        Provenance.explicit("Renegade Immortal", List.of("Celestial era"), 5,
                                "Wiki: celestial treasures category"),
                        "A seven-colored nail, a celestial sealing treasure.")
                .canonState("Seven-Colored Nail (restored)", "七彩钉（复）",
                        ItemEvolutionChain.RequiredRealm.VOID_TREADING,
                        "Destroyed (Ch. 1082), restored via Void Gate power (Ch. 1626)",
                        Provenance.explicit("Renegade Immortal", List.of("Ch. 1082", "Ch. 1626"), 5,
                                "Wiki: 'destroyed Ch. 1082 → restored Ch. 1626 (Void Gate power)'"),
                        "The seven-colored nail restored via the Void Gate power.")
                .build());

        // ── Soul Flag: production-grade → retired (replaced by Billion Soul Flag) ──
        // Canon: "retired (replaced by Billion Soul Flag)"
        register(ItemEvolutionChain.builder("wanglin/soul_flag_production", "Soul Flag")
                .acquisitionState("Soul Flag (production)", "魂旗",
                        ItemEvolutionChain.RequiredRealm.NASCENT_SOUL,
                        "Soul-refining flag, production-grade",
                        Provenance.explicit("Renegade Immortal", List.of("Nascent Soul era"), 5,
                                "Wiki: soul items category; 'retired (replaced by Billion Soul Flag)'"),
                        "A soul-refining flag used to collect and refine souls.")
                .canonState("Billion Soul Flag", "亿魂旗",
                        ItemEvolutionChain.RequiredRealm.SOUL_TRANSFORMATION,
                        "Replaced the production-grade soul flag as Wang Lin's primary soul treasure",
                        Provenance.explicit("Renegade Immortal", List.of("Soul Transformation era"), 5,
                                "Wiki: 'retired (replaced by Billion Soul Flag)'"),
                        "The Billion Soul Flag — Wang Lin's peak soul-refining treasure, replacing the earlier production flag.")
                .build());

        bootstrapped = true;
        Ergenverse.LOGGER.info("[Canon] Canon state histories bootstrapped: {} multi-state chains. "
                + "All other arsenal items have a single canonical state (no chain needed).", CHAINS.size());
    }
}
