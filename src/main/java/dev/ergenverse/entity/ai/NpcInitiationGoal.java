package dev.ergenverse.entity.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.EntityCultivator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * NpcInitiationGoal — Article XXIV: NPCs Must Initiate Gameplay.
 *
 * <p>The old model was Player right-clicks NPC. The canon model is
 * NPC approaches Player and speaks FIRST. This goal makes specific
 * NPCs send a canon-faithful initiation line to nearby players
 * without being interacted with.
 *
 * <h2>How it works</h2>
 * <ul>
 *   <li>When a player comes within {@link #APPROACH_RANGE} blocks,
 *       the NPC sends one of its {@code initiation_lines} (from
 *       its NPC JSON data) as an action bar message.</li>
 *   <li>After sending, a {@link #COOLDOWN_TICKS} cooldown prevents
 *       spam. The NPC won't initiate again until the player leaves
 *       and re-enters range, or the cooldown expires.</li>
 *   <li>If the NPC has no {@code initiation_lines} in its data,
 *       this goal never activates (canUse returns false).</li>
 * </ul>
 *
 * <h2>Data source</h2>
 * <p>Initiation lines are read from the NPC's JSON file under the
 * {@code "initiation_lines"} array. Each element is a string:
 * <pre>{
 *   "initiation_lines": [
 *     "You there. New to the sect?",
 *     "The elder is giving a lecture at the Alchemy Courtyard. You should attend."
 *   ]
 * }</pre>
 *
 * <h2>Article XXVI compliance</h2>
 * <p>NO new engines, subscribers, or buses. This is a single Minecraft
 * Goal class that reads existing NPC data and uses the existing
 * sendSystemMessage infrastructure (same as NpcDialogueTickHandler).
 *
 * <p><b>Provenance:</b> INFERRED from Article XXIV examples:
 * "Wang Lin leaves a jade slip", "Situ Nan asks for help",
 * "An elder invites you to observe a lecture."
 */
public class NpcInitiationGoal extends Goal {

    /** How close a player must be for the NPC to initiate. */
    private static final double APPROACH_RANGE = 8.0;

    /** Minimum ticks between initiation messages to the same player. */
    private static final int COOLDOWN_TICKS = 6000; // 5 minutes

    private final EntityCultivator cultivator;

    /** Cached initiation lines from NPC data (empty = goal never activates). */
    private final List<String> initiationLines = new ArrayList<>();

    /** Cached night-specific initiation lines (used during dark hours). */
    private final List<String> nightLines = new ArrayList<>();

    /** Per-player cooldown tracking: player UUID string → last initiation tick. */
    private final java.util.Map<String, Long> playerCooldowns = new java.util.HashMap<>();

    /** True once lines have been loaded from NPC data. */
    private boolean linesLoaded = false;

    /** MC day ticks. Night in MC is approximately timeOfDay 12000-24000. */
    private static final long MC_DAY_TICKS = 24000L;

    public NpcInitiationGoal(EntityCultivator cultivator) {
        this.cultivator = cultivator;
        // This goal controls look direction when initiating
        this.setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (cultivator.level().isClientSide) return false;
        if (!loadLinesIfNeeded()) return false;

        // Use night_lines during dark hours if available
        List<String> activeLines = getActiveLines();
        if (activeLines.isEmpty()) return false;

        // Find a player in range who isn't on cooldown
        return findEligiblePlayer() != null;
    }

    @Override
    public void start() {
        ServerPlayer target = findEligiblePlayer();
        if (target == null) return;

        // Pick a random line from the active set (day or night)
        List<String> activeLines = getActiveLines();
        String line = activeLines.get(
                cultivator.getRandom().nextInt(activeLines.size()));

        // Send as action bar message (same channel as right-click dialogue)
        target.sendSystemMessage(
                Component.literal(
                        "\u00A7e<" + cultivator.getDisplayNameCn() + "> " + line + "\u00A7r"),
                true); // action bar

        // Face the player
        cultivator.getLookControl().setLookAt(target);

        // Record cooldown
        playerCooldowns.put(target.getUUID().toString(),
                cultivator.level().getGameTime());

        Ergenverse.LOGGER.debug("[NpcInitiation] {} -> {}: \"{}\"",
                cultivator.getCharacterId(), target.getName().getString(), line);
    }

    @Override
    public boolean canContinueToUse() {
        // One-shot: send the message and stop
        return false;
    }

    /**
     * Load initiation lines from the NPC's canon data (lazy, once).
     * Also loads {@code night_lines} for time-aware dialogue.
     * Returns false if the NPC data couldn't be loaded.
     */
    private boolean loadLinesIfNeeded() {
        if (linesLoaded) return true;

        String characterId = cultivator.getCharacterId();
        if (characterId == null || characterId.isEmpty()) {
            linesLoaded = true;
            return false;
        }

        try {
            com.google.gson.JsonObject data =
                    dev.ergenverse.simulation.WorldStateDataLoader.getEntry(
                            "npcs", characterId);
            if (data != null) {
                // Load daytime initiation lines
                if (data.has("initiation_lines")
                        && data.get("initiation_lines").isJsonArray()) {
                    JsonArray arr = data.getAsJsonArray("initiation_lines");
                    for (JsonElement elem : arr) {
                        if (elem.isJsonPrimitive() && elem.getAsJsonPrimitive().isString()) {
                            initiationLines.add(elem.getAsString());
                        }
                    }
                }
                // Load night-specific initiation lines
                if (data.has("night_lines")
                        && data.get("night_lines").isJsonArray()) {
                    JsonArray arr = data.getAsJsonArray("night_lines");
                    for (JsonElement elem : arr) {
                        if (elem.isJsonPrimitive() && elem.getAsJsonPrimitive().isString()) {
                            nightLines.add(elem.getAsString());
                        }
                    }
                }
            }
        } catch (Exception e) {
            Ergenverse.LOGGER.debug("[NpcInitiation] Failed to load lines for {}: {}",
                    characterId, e.getMessage());
        }

        linesLoaded = true;
        return true;
    }

    /**
     * Get the active line set based on time of day.
     * During night (MC time 12000-24000), prefers night_lines if available.
     * Otherwise falls back to initiation_lines.
     */
    private List<String> getActiveLines() {
        long timeOfDay = cultivator.level().getGameTime() % MC_DAY_TICKS;
        boolean isNight = timeOfDay >= 12000L;
        if (isNight && !nightLines.isEmpty()) {
            return nightLines;
        }
        return initiationLines;
    }

    /**
     * Find a player within approach range who isn't on cooldown.
     */
    private ServerPlayer findEligiblePlayer() {
        long currentTick = cultivator.level().getGameTime();

        for (ServerPlayer player : cultivator.level().getEntitiesOfClass(
                ServerPlayer.class,
                cultivator.getBoundingBox().inflate(APPROACH_RANGE))) {
            String uuid = player.getUUID().toString();
            Long lastInitiation = playerCooldowns.get(uuid);
            if (lastInitiation == null
                    || (currentTick - lastInitiation) >= COOLDOWN_TICKS) {
                return player;
            }
        }
        return null;
    }
}