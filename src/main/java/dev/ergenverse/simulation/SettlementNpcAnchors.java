package dev.ergenverse.simulation;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.spawn.WangFamilyVillageBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SettlementNpcAnchors — maps settlement IDs to fixed NPC spawn positions
 * within each settlement's footprint.
 *
 * <h2>DEPRECATED — Constitution Article XLIV</h2>
 * <p><b>This class is DEPRECATED.</b> Per Article XLIV (§5): "NPC positions
 * shall not be fixed offsets... A system that hardcodes an NPC's position as
 * a fixed (dx, dz) offset is a bug. An NPC's position is a derived fact
 * about a living actor." This class is retained ONLY during the transition
 * and WILL BE DELETED in a future cycle.
 *
 * <p>The replacement is {@link dev.ergenverse.simulation.settlement.SettlementRegistry}
 * + {@link dev.ergenverse.simulation.settlement.Residence} + {@link
 * dev.ergenverse.simulation.settlement.ActorPresence}. Under the new model,
 * an NPC's position is DERIVED from their life (residence + time-of-day +
 * activity + context), not a fixed offset. The NPC is at home in the morning,
 * at the meditation rock in the afternoon, at the market at midday — their
 * position changes with their life, not with a hardcoded coordinate.
 *
 * <p>Do not add new anchors here. New NPCs should be registered as residents
 * of a {@link dev.ergenverse.simulation.settlement.Residence} within a
 * {@link dev.ergenverse.simulation.settlement.Settlement}.
 *
 * <p><b>Problem solved (legacy):</b> The prior {@link ReificationScan} spawned
 * NPCs at a hash-derived offset from the PLAYER's position. This meant NPCs
 * spawned randomly throughout the biome, never at their canonical home. The
 * player arrived at Wang Family Village and found a ghost town.
 *
 * <p><b>Solution (legacy):</b> Each settlement has a fixed center coordinate.
 * This registry stores per-NPC offsets from that center. When a player
 * approaches a settlement, ReificationScan spawns NPCs at their anchored
 * positions instead of random biome offsets.
 *
 * <p><b>Per Art. XXII:</b> "A canon entry that exists only as data, never as
 * experience, is a failure." This registry bridges the gap — NPCs become
 * visible, positioned, alive.
 *
 * <p><b>Per Art. XXVI:</b> "BUILD MORE CONTENT using the existing
 * infrastructure." This is NOT a new Engine or Bus. It is a static data map
 * that the existing ReificationScan reads.
 *
 * <p><b>Coordinate contract:</b> Offsets are (dx, dz) from the settlement
 * center. The Y coordinate is always resolved via surface heightmap at
 * spawn time. This ensures NPCs stand on the ground regardless of terrain.
 *
 * <p><b>Provenance:</b> Anchor positions are INFERRED from building layouts
 * in WangFamilyVillageBuilder. Each NPC is placed at the doorway or center
 * of the building they canonically inhabit.
 */
@Deprecated
public final class SettlementNpcAnchors {

    /** An anchored NPC spawn point. */
    public record NpcAnchor(String characterId, int offsetX, int offsetZ) {}

    /** Map: settlementId → list of anchored NPCs. */
    private static final Map<String, List<NpcAnchor>> ANCHORS = new HashMap<>();

    private SettlementNpcAnchors() {}

    /**
     * Initialize all settlement NPC anchors. Called once on server start,
     * after {@link NpcSpawnRegistry#initialize()}.
     */
    public static void initialize() {
        registerWangFamilyVillage();
        registerHengYueSect();

        int total = ANCHORS.values().stream().mapToInt(List::size).sum();
        Ergenverse.LOGGER.info("[SettlementNpcAnchors] Registered {} anchored NPCs across {} settlements.",
                total, ANCHORS.size());
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Wang Family Village (3842, ?, -1184)
    // ═══════════════════════════════════════════════════════════════════
    //
    //  Building layout (from WangFamilyVillageBuilder, offsets from center):
    //    Wang Family Home: (cx-18, cz-18), 7x5 house
    //    Elder Home:        (cx+10, cz-18), 7x7 house
    //    Commoner W1:       (cx-18, cz-4)
    //    Commoner W2:       (cx-18, cz+6)
    //    Commoner E1:       (cx+6, cz-4)
    //    Commoner E2:       (cx+6, cz+6)
    //    Commoner NW1:      (cx-10, cz-28)
    //    Commoner NW2:      (cx+2, cz-28)
    //    Commoner NE1:      (cx+16, cz-28)
    //    Commoner SW1:      (cx-10, cz+12)
    //    Commoner SW2:      (cx+2, cz+12)
    //    Commoner SE1:      (cx+16, cz+12)
    //    Herb Garden:       (cx-28, cz-10), 10x8
    //    North Farms:       (cx-18..cx+18, cz-35)
    //    South Farms:       (cx-12..cx+12, cz+20)

    private static void registerWangFamilyVillage() {
        String settlementId = "wang_family_village";

        // ── Wang Tianshui (王天水) — family patriarch, father of Wang Lin ──
        // Canon: He lives in the Wang Family Home. Place at the doorway.
        // Home is at (cx-18, cz-18), 7x5. Door at south face: (cx-18+3, cz-18+5) = (-15, -13)
        add(settlementId, "npc_wang_tianshui", -15, -13);

        // ── Wang Qingyue (王青叶) — Wang Lin's mother ──
        // Canon: Lives in the Wang Family Home with Tianshui. Place near the
        // herb garden side of the home (west face).
        // Home NW corner: (-18, -18). Herb garden is at (-28, -10).
        // Place at home's west side: (-18, -16)
        add(settlementId, "npc_wang_qingyue", -18, -16);

        // ── Wang Wei (王伟) — energetic village boy, wants to pass aptitude test ──
        // Canon: A young boy who practices martial forms in the village.
        // Place in the central plaza where he can practice openly.
        add(settlementId, "npc_wang_wei", 2, 2);

        // ── Wang Zhou (王周) — village elder ──
        // Canon: The respected elder. Lives in the Elder Home (NE quadrant).
        // Home at (cx+10, cz-18), 7x7. Door at south face: (cx+10+3, cz-18+7) = (13, -11)
        add(settlementId, "npc_wang_zhou", 13, -11);

        // ── Wang Ping (王平) — village child ──
        // Canon: One of the village children. Place near a commoner home.
        // Commoner W1 at (cx-18, cz-4), 5x5. Door: (cx-18+2, cz-4+5) = (-16, 1)
        add(settlementId, "npc_wang_ping", -16, 1);

        // ── Wang Yiyi (王一依) — village girl ──
        // Canon: Village child, often near Wang family. Place near the herb garden.
        add(settlementId, "npc_wang_yiyi", -26, -8);

        // ── Wang Tianshan (王天山) — Wang family member ──
        // Canon: Extended family. Place at commoner home E1.
        // E1 at (cx+6, cz-4), 5x5. Door: (cx+6+2, cz-4+5) = (8, 1)
        add(settlementId, "npc_wang_tianshan", 8, 1);

        // ── Zhou Tingsu (周婷素) — married into village ──
        // Canon: Married into the Wang family. Place at commoner home SW2.
        // SW2 at (cx+2, cz+12), 5x5. Door: (cx+2+2, cz+12+5) = (4, 17)
        add(settlementId, "npc_zhou_tingsu", 4, 17);

        // ── Da Niu (大牛) — village laborer ──
        // Canon: Strong village laborer. Place near the north farms.
        add(settlementId, "npc_da_niu", 10, -30);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Heng Yue Sect (5400, ?, -1900)
    // ═══════════════════════════════════════════════════════════════════
    // TODO: Add anchors once HengYueSectBuilder is complete and positions are known.
    // For now, register a few key NPCs at the sect center (0,0 offset).
    private static void registerHengYueSect() {
        String settlementId = "heng_yue_sect";

        // Sect disciples at approximate positions within the sect grounds.
        // The HengYueSectBuilder places districts relative to center.
        // These will be adjusted when the full builder layout is finalized.
        add(settlementId, "npc_qiu_siping", 0, 5);       // Senior disciple near plaza
        add(settlementId, "npc_wang_zhuo", -5, 3);         // Fellow disciple
        add(settlementId, "npc_wang_hao", 5, 8);          // Friendly disciple
        add(settlementId, "npc_sun_dazhu", -8, 10);       // Rival disciple
    }

    /**
     * Add an anchored NPC to a settlement.
     */
    private static void add(String settlementId, String characterId, int offsetX, int offsetZ) {
        ANCHORS.computeIfAbsent(settlementId, k -> new ArrayList<>())
                .add(new NpcAnchor(characterId, offsetX, offsetZ));
    }

    /**
     * Get all anchored NPCs for a settlement.
     *
     * @param settlementId the canonical settlement ID (e.g. "wang_family_village")
     * @return unmodifiable list of anchored NPCs, or empty list if none registered
     */
    public static List<NpcAnchor> getAnchorsForSettlement(String settlementId) {
        List<NpcAnchor> list = ANCHORS.get(settlementId);
        return list != null ? Collections.unmodifiableList(list) : Collections.emptyList();
    }

    /**
     * Check whether a character ID has an anchor in any settlement.
     *
     * @param characterId the NPC character ID
     * @return true if this NPC has a fixed anchor position
     */
    public static boolean hasAnchor(String characterId) {
        for (List<NpcAnchor> list : ANCHORS.values()) {
            for (NpcAnchor a : list) {
                if (a.characterId().equals(characterId)) return true;
            }
        }
        return false;
    }

    /** Clear the registry. Called on world unload. */
    public static void clear() {
        ANCHORS.clear();
    }
}
