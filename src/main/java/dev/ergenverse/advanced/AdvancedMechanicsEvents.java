package dev.ergenverse.advanced;

import dev.ergenverse.cultivation.CultivationCapability;
import dev.ergenverse.cultivation.CultivationState;
import dev.ergenverse.cultivation.RealmId;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.wanglin.CaveWorldOwnership;
import dev.ergenverse.wanglin.JossFlameEconomy;
import dev.ergenverse.wanglin.RealmSealingGrandArray;
import dev.ergenverse.wanglin.SamsaraDao;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * Server-side event handler for the 4 advanced mechanics systems:
 * <ol>
 *   <li><b>Samsara Dao</b> — essence comprehension tracking + Heaven Trampling check</li>
 *   <li><b>Joss Flame Economy</b> — daily harvest loop + perception-based reveals</li>
 *   <li><b>Cave World Ownership</b> — cultivation ceiling enforcement on breakthrough</li>
 *   <li><b>Realm-Sealing Grand Array</b> — seal state queries + boundary enforcement</li>
 * </ol>
 *
 * <h2>Prime Directive Compliance</h2>
 * <p>These systems are OBJECTIVE. They exist whether the player knows about them or not.
 * The Joss Flame economy runs every day regardless of perception. The cultivation ceiling
 * suppresses breakthroughs regardless of awareness. Cultivation reveals these systems;
 * it does not create them.
 *
 * <h2>Wiring Points</h2>
 * <ul>
 *   <li><b>Loop E (Ergenverse.onServerTick)</b> — daily Joss Flame tick (every 24000 ticks)</li>
 *   <li><b>PlayerLoggedInEvent</b> — perception-based system reveals</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = Ergenverse.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AdvancedMechanicsEvents {

    private AdvancedMechanicsEvents() {}

    /** Joss Flame tick counter — ticks once per MC day. */
    private static long lastJossTickDay = -1;

    // ─── 1. Joss Flame Economy — daily harvest loop ───────────────────

    /**
     * Tick the Joss Flame economy once per MC day.
     * Called from Ergenverse.onServerTick Loop E (every 24000 ticks).
     *
     * <p>Per canon: the harvest loop runs regardless of player perception.
     * Mortal villages generate Joss Flames every day. The owner harvests
     * 30% of all refined flames. Uncollected flames dissipate at 40%/day.
     *
     * @param overworld the server overworld level
     * @param dayTick the current day tick (gameTime / 24000)
     */
    public static void tickJossFlameEconomy(ServerLevel overworld, long dayTick) {
        if (dayTick == lastJossTickDay) return;
        lastJossTickDay = dayTick;

        // Determine if the Cave World owner (Seven-Colored Daoist) is still active.
        // Per canon: the owner is active until killed by the player.
        boolean ownerActive = CaveWorldOwnership.CAVE_WORLD.ownerId != null
                && RealmSealingGrandArray.isArrayActive();

        double ownerHarvest = JossFlameEconomy.tickDay(ownerActive);

        if (ownerHarvest > 0) {
            Ergenverse.LOGGER.debug("[Ergenverse] JossFlameEconomy: owner harvested {} total flames today (day {})",
                    String.format("%.2f", ownerHarvest), dayTick);
        }
    }

    // ─── 2. Cultivation ceiling enforcement ───────────────────────────

    /**
     * Check whether a player's breakthrough attempt is blocked by the
     * world's cultivation ceiling.
     *
     * <p>Per canon: the Realm-Sealing Grand Array prevents Third-Step
     * breakthroughs inside the Sealed Realm. The Heaven-Defying Bead
     * provides a one-tier exemption. Killing the owner dissolves the seal.
     *
     * @param player the player attempting breakthrough
     * @param targetRealm the realm they are trying to reach
     * @return a blocking message if blocked, null if allowed
     */
    public static String checkCultivationCeiling(ServerPlayer player, RealmId targetRealm) {
        // Determine which world layer the player is in.
        // Planet Suzaku (overworld) and Cave World void are both inside the Cave World.
        // Immortal Astral Continent is outside — free of ownership.
        String worldLayerId = "cave_world"; // default: any dimension inside the Cave World
        String dimPath = player.level().dimension().location().getPath();
        if (dimPath.contains("immortal_astral")) {
            worldLayerId = "immortal_astral_continent";
        } else if (dimPath.contains("foreign_battleground")) {
            worldLayerId = "immortal_astral_continent"; // outside the Cave World
        }

        if (!CaveWorldOwnership.isPlayerFarmed(worldLayerId)) return null;

        CultivationState state = CultivationCapability.getOrThrow(player);
        boolean hasBead = state.hasHeavenDefyingBead(player.getInventory());

        if (!CaveWorldOwnership.canBreakthroughTo(worldLayerId, targetRealm.order, hasBead)) {
            CaveWorldOwnership.Ownership ownership = CaveWorldOwnership.getOwnership(worldLayerId);
            return String.format(
                "\u00A7c\u00A7lThe seal crushes your breakthrough attempt!\u00A7r\n" +
                "\u00A77The %s (ceiling: tier %d) prevents ascension beyond %s.\n" +
                "\u00A78The %s's array enforces this law across the entire world.\n" +
                "\u00A77%s",
                ownership.worldLayerName,
                ownership.cultivationCeilingAbsoluteTier,
                RealmId.byOrder(ownership.cultivationCeilingAbsoluteTier).name,
                ownership.sealArrayId != null ? "Realm-Sealing Grand Array" : "owner's power",
                hasBead ? "\u00A7eThe Heaven-Defying Bead pulses — but even it cannot defy this seal at this tier." :
                          "\u00A77You need extraordinary means to bypass this seal."
            );
        }

        // Check Realm-Sealing Grand Array specifically for Sealed Realm
        if ("sealed_realm".equals(worldLayerId) && RealmSealingGrandArray.isArrayActive()) {
            if (!RealmSealingGrandArray.canBreakthroughInsideSealedRealm(
                    targetRealm.order, hasBead, false)) {
                return String.format(
                    "\u00A7c\u00A7lThe Heaven-Splitting Axe's seal resists!\u00A7r\n" +
                    "\u00A77The Realm-Sealing Grand Array's spirit blocks your path.\n" +
                    "\u00A77Only those who comprehend the Restriction Essence may earn its cooperation."
                );
            }
        }

        return null;
    }

    // ─── 3. Perception-based system reveals ───────────────────────────

    /**
     * Send Joss Flame awareness message to qualifying players on login
     * and on realm-up.
     *
     * <p>Per canon: Soul Formation+ cultivators can perceive Joss Flames.
     * Nirvana Scryer+ can perceive the ownership loop (they are livestock).
     */
    public static void checkJossFlameRevelation(ServerPlayer player) {
        CultivationState state = CultivationCapability.getOrThrow(player);
        String realmId = state.getCurrentRealm().name.toLowerCase().replace(" ", "_");

        if (JossFlameEconomy.canPerceiveJossFlames(realmId)) {
            player.sendSystemMessage(Component.literal(
                "\u00A76\u00A7l\u2728 A subtle shimmer catches your eye...\u00A7r"));
            player.sendSystemMessage(Component.literal(
                "\u00A7dYou perceive faint, golden flames rising from the places where " +
                "mortals gather. These are \u00A7eJoss Flames\u00A7d — the energy of " +
                "mortal faith, made tangible."));
            player.sendSystemMessage(Component.literal(
                "\u00A78Someone is collecting these flames. You sense a vast, " +
                "invisible mechanism at work..."));

            dev.ergenverse.history.HistoryManager.onDiscovery(player,
                    "joss_flame_perception",
                    "Perceived Joss Flames for the first time at " + state.getCurrentRealm().name,
                    player.level().getGameTime());
        }

        if (JossFlameEconomy.canPerceiveOwnershipLoop(realmId)) {
            player.sendSystemMessage(Component.literal(""));
            player.sendSystemMessage(Component.literal(
                "\u00A7c\u00A7l\u26A0 The truth reveals itself...\u00A7r"));
            player.sendSystemMessage(Component.literal(
                "\u00A74The Joss Flames are being siphoned. Every world you have " +
                "ever known is a farm. The cultivators within are livestock — " +
                "their cultivation generates faith, their faith feeds the flames, " +
                "and the flames are harvested by \u00A7c\u00A7lthe owner\u00A74."));
            player.sendSystemMessage(Component.literal(
                "\u00A78The Realm-Sealing Grand Array is not protection — " +
                "it is a pen wall. The cultivation ceiling is not natural law — " +
                "it is a slaughter threshold."));

            dev.ergenverse.history.HistoryManager.onDiscovery(player,
                    "joss_ownership_revelation",
                    "Realized the Cave World is a Joss Flame farm at " + state.getCurrentRealm().name,
                    player.level().getGameTime());
        }
    }

    /**
     * Send Cave World ownership status on login.
     * Only shown to Nascent Soul+ (they have enough cultivation to
     * begin suspecting something is wrong with the world's structure).
     */
    public static void checkCaveWorldStatus(ServerPlayer player) {
        CultivationState state = CultivationCapability.getOrThrow(player);
        if (state.getCurrentRealm().order < RealmId.NASCENT_SOUL.order) return;

        String dimPath = player.level().dimension().location().getPath();
        String worldLayerId = "cave_world"; // default: inside the Cave World
        if (dimPath.contains("immortal_astral") || dimPath.contains("foreign_battleground")) {
            worldLayerId = "immortal_astral_continent"; // outside Cave World = free
        }

        CaveWorldOwnership.Ownership ownership = CaveWorldOwnership.getOwnership(worldLayerId);
        if (ownership == null || ownership.isFree()) return;

        if (ownership.sealActive) {
            player.sendSystemMessage(Component.literal(
                "\u00A78You sense an invisible pressure pervading the world. " +
                "Something vast and ancient limits the height of cultivation here. " +
                "The ceiling feels... intentional."));
        } else {
            player.sendSystemMessage(Component.literal(
                "\u00A7aThe invisible pressure that once suffocated this world is gone. " +
                "The seal has been dissolved."));
        }
    }

    // ─── 4. Status display methods (for command) ─────────────────────

    /**
     * Get the current status of the Realm-Sealing Grand Array for display.
     */
    public static String getSealStatus() {
        if (!RealmSealingGrandArray.isArrayActive()) {
            return "\u00A7a\u00A7lDISSOLVED\u00A7r — The seal is gone. The Cave World is unified.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("\u00A7c\u00A7lACTIVE\u00A7r\n");
        sb.append(String.format("  Spirit: \u00A7b%s\u00A7r — %s\n",
                RealmSealingGrandArray.getSpiritState().name(),
                RealmSealingGrandArray.getSpiritState().description));
        sb.append(String.format("  Mode: \u00A7b%s\u00A7r — %s",
                RealmSealingGrandArray.getEnforcementMode().name(),
                RealmSealingGrandArray.getEnforcementMode().description));
        return sb.toString();
    }

    /**
     * Get the player's Samsara Dao progress for display.
     */
    public static String getSamsaraStatus(ServerPlayer player) {
        CultivationState state = CultivationCapability.getOrThrow(player);
        boolean[] essences = state.getEssencesComprehended();

        StringBuilder sb = new StringBuilder();
        sb.append("\u00A75\u00A7lSamsara Dao — The 14 Essences\u00A7r\n");

        int comprehended = 0;
        SamsaraDao.Essence[] allEssences = SamsaraDao.Essence.values();
        for (int i = 0; i < allEssences.length && i < essences.length; i++) {
            SamsaraDao.Essence e = allEssences[i];
            boolean done = essences[i];
            if (done) comprehended++;
            sb.append(String.format("  %s %d. %s \u00A77(%s) \u00A78— %s\n",
                    done ? "\u00A7a\u2713" : "\u00A77\u2717",
                    e.taxonomyOrder,
                    done ? "\u00A7f" + e.name : "\u00A78???",
                    e.category.name(),
                    done ? e.description : "Not yet comprehended."));
        }

        sb.append(String.format("\n  Progress: \u00A7e%d/14\u00A7r Essences comprehended.\n", comprehended));

        if (SamsaraDao.isHeavenTramplingAchieved(essences)) {
            sb.append("\n  \u00A7a\u00A7l\u2728 HEAVEN TRAMPLING ACHIEVED! \u00A7r\n");
            sb.append("  \u00A7fAll 14 Essences comprehended. You have transcended the bridge system.");
        } else if (comprehended >= 8) {
            sb.append("  \u00A7eThe path to Heaven Trampling grows clearer...");
        }

        // Show Wang Lin's completion order as reference
        sb.append("\n  \u00A78Wang Lin's completion order: ");
        List<SamsaraDao.Essence> order = SamsaraDao.WANG_LIN_COMPLETION_ORDER;
        for (int i = 0; i < order.size(); i++) {
            sb.append(order.get(i).nameCn);
            if (i < order.size() - 1) sb.append("\u00A77 \u2192 ");
        }

        // Resonance pairs
        sb.append("\n\n  \u00A78Resonance pairs: ");
        boolean first = true;
        for (int i = 0; i < allEssences.length; i++) {
            for (int j = i + 1; j < allEssences.length; j++) {
                if (SamsaraDao.essencesResonate(allEssences[i], allEssences[j])) {
                    if (!first) sb.append(", ");
                    sb.append(allEssences[i].nameCn).append("-").append(allEssences[j].nameCn);
                    first = false;
                }
            }
        }

        return sb.toString();
    }

    /**
     * Get the current Joss Flame economy status for display.
     */
    public static String getJossStatus(ServerPlayer player) {
        CultivationState state = CultivationCapability.getOrThrow(player);
        String realmId = state.getCurrentRealm().name.toLowerCase().replace(" ", "_");
        boolean canPerceive = JossFlameEconomy.canPerceiveJossFlames(realmId);
        boolean canSeeLoop = JossFlameEconomy.canPerceiveOwnershipLoop(realmId);

        StringBuilder sb = new StringBuilder();
        sb.append("\u00A76\u00A7lJoss Flame Economy — The Harvest Loop\u00A7r\n");

        if (!canPerceive) {
            sb.append("  \u00A77You cannot perceive Joss Flames at your current cultivation.\n");
            sb.append("  \u00A78Requires: Soul Formation or above.");
            return sb.toString();
        }

        // Show the 4 stages
        sb.append("  \u00A7eStage 1 — GENERATION:\u00A7r Mortal worship generates Joss Flames at temples.\n");
        sb.append("  \u00A7eStage 2 — COLLECTION:\u00A7r Cultivators gather flames into vessels.\n");
        sb.append("  \u00A7eStage 3 — REFINEMENT:\u00A7r Flames refined into pure cultivation energy.\n");
        sb.append("  \u00A7eStage 4 — HARVEST:\u00A7r ");
        if (RealmSealingGrandArray.isArrayActive()) {
            sb.append(String.format("\u00A7cThe owner harvests \u00A7e%.0f%%\u00A7c of all refined flames.\n",
                    JossFlameEconomy.OWNER_HARVEST_PERCENTAGE * 100));
        } else {
            sb.append("\u00A7aThe owner is dead. No harvest is siphoned.\n");
        }

        if (canSeeLoop) {
            sb.append("\n  \u00A7c\u00A7lTHE TRUTH:\u00A7r You are livestock. Every cultivator in this world is.\n");
            sb.append("  \u00A74The Joss Flames you generate through your cultivation feed the owner.\n");
            sb.append("  \u00A74Break the seal. Kill the owner. Free the world.");
        } else {
            sb.append("\n  \u00A78You sense there is more to this system than meets the eye...");
        }

        return sb.toString();
    }

    /**
     * Get the Cave World ownership status for display.
     */
    public static String getCaveWorldStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("\u00A7c\u00A7lCave World Ownership — The Cosmological Truth\u00A7r\n\n");

        String[] worldIds = {"cave_world", "sealed_realm", "outer_realm", "immortal_astral_continent"};
        for (String wid : worldIds) {
            CaveWorldOwnership.Ownership o = CaveWorldOwnership.getOwnership(wid);
            if (o == null) continue;

            sb.append(String.format("  \u00A7e%s\u00A7r\n", o.worldLayerName));
            if (o.isFree()) {
                sb.append("    \u00A7aStatus: FREE\u00A7r — No owner.\n");
            } else {
                sb.append(String.format("    \u00A7cOwner: \u00A7f%s\u00A7r (%s)\n", o.ownerName, o.ownerRealmId));
                sb.append(String.format("    \u00A78Origin: %s\n", o.originReason));
                sb.append(String.format("    \u00A78Ceiling: Tier %d (%s)\n",
                        o.cultivationCeilingAbsoluteTier,
                        o.cultivationCeilingAbsoluteTier == Integer.MAX_VALUE ? "none" :
                                RealmId.byOrder(o.cultivationCeilingAbsoluteTier).name));
                sb.append(String.format("    Seal: %s\n",
                        o.sealActive ? "\u00A7cACTIVE" : "\u00A7aDISSOLVED"));
                if (o.sealArrayId != null) {
                    sb.append(String.format("    \u00A78Array: %s\n", o.sealArrayId));
                }
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    // ─── Forge Events ─────────────────────────────────────────────────

    /**
     * On player login, check if their cultivation reveals any advanced mechanics.
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        // Delay by 60 ticks (3 sec) so the player has time to load
        sp.server.tell(new net.minecraft.server.TickTask(
            sp.server.getTickCount() + 60,
            () -> {
                checkJossFlameRevelation(sp);
                checkCaveWorldStatus(sp);
            }
        ));
    }
}