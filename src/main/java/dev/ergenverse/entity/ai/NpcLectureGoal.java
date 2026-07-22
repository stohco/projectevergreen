package dev.ergenverse.entity.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.simulation.WorldStateDataLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * NpcLectureGoal — Article XXIV: NPCs Must Initiate Gameplay.
 *
 * <p>Makes elder NPCs proactively invite nearby players to attend
 * lectures during scheduled time windows. Lectures are defined in the
 * NPC's JSON data under the {@code "lectures"} array.
 *
 * <h2>Canon basis</h2>
 * <p>In RI chapters 3-8, Wang Lin regularly attends lectures given by
 * Heng Yue Sect elders. This is a core part of sect daily life. Elders
 * teach cultivation techniques, explain Dao concepts, and disciples
 * gather to listen. Before this goal, no lecture experience existed
 * despite Qiu Siping's initiation line referencing "the alchemy lecture."
 *
 * <h2>How it works</h2>
 * <ul>
 *   <li>Loads {@code lectures} from NPC JSON. Each has time_start, time_end,
 *       topic, location, description, insight, and optional reward.</li>
 *   <li>During the lecture's time window, when a player enters 10-block range,
 *       the NPC sends an action bar invitation with the lecture topic and location.</li>
 *   <li>Writes {@code ergenverse_lecture_invite_<npcId>} to player NBT
 *       so the player can right-click to attend.</li>
 *   <li>60-second cooldown between invites to the same player.</li>
 *   <li>One-shot: fires once per player per cooldown period.</li>
 * </ul>
 *
 * <h2>Priority design</h2>
 * <p>Priority 2 (same tier as NpcInitiationGoal and NpcSectMissionGoal).
 * All three use Flag.LOOK only. MC goal scheduler naturally alternates
 * between them, giving the player varied NPC behavior.
 *
 * <h2>Article XXVI compliance</h2>
 * <p>NO new engines, subscribers, or buses. Single Goal class reading
 * existing NPC data via WorldStateDataLoader.
 *
 * <p><b>Provenance:</b> EXPLICIT from RI Ch.3-8 (Wang Lin attending
 * elder lectures as part of daily sect life).
 */
public class NpcLectureGoal extends Goal {

    private static final double APPROACH_RANGE = 10.0;
    private static final int COOLDOWN_TICKS = 1200; // 60 seconds
    private static final long MC_DAY_TICKS = 24000L;

    private final EntityCultivator cultivator;

    private final List<LectureData> lectures = new ArrayList<>();
    private boolean lecturesLoaded = false;
    private final java.util.Map<String, Long> playerCooldowns = new java.util.HashMap<>();

    public NpcLectureGoal(EntityCultivator cultivator) {
        this.cultivator = cultivator;
        this.setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (cultivator.level().isClientSide) return false;
        if (!loadLecturesIfNeeded()) return false;
        if (lectures.isEmpty()) return false;

        long timeOfDay = cultivator.level().getGameTime() % MC_DAY_TICKS;

        // Find a lecture active right now
        LectureData activeLecture = findActiveLecture(timeOfDay);
        if (activeLecture == null) return false;

        // Find a player in range who isn't on cooldown and hasn't attended this lecture
        return findEligiblePlayer(activeLecture) != null;
    }

    @Override
    public void start() {
        long timeOfDay = cultivator.level().getGameTime() % MC_DAY_TICKS;
        LectureData activeLecture = findActiveLecture(timeOfDay);
        if (activeLecture == null) return;

        ServerPlayer target = findEligiblePlayer(activeLecture);
        if (target == null) return;

        // Send invitation as action bar
        String inviteMsg = "\u00A76\u00A7l[Lecture]\u00A7r \u00A7e"
                + cultivator.getDisplayNameCn()
                + "\u00A7r\u00A77: \"I am giving a lecture on \u00A7f"
                + activeLecture.topic
                + "\u00A77 at the \u00A7b"
                + activeLecture.location
                + "\u00A77. Right-click me to attend.\"";
        target.sendSystemMessage(Component.literal(inviteMsg), true);

        // Face the player
        cultivator.getLookControl().setLookAt(target);

        // Write invite to player NBT
        CompoundTag data = target.getPersistentData();
        CompoundTag inviteTag = new CompoundTag();
        inviteTag.putString("npc_id", cultivator.getCharacterId());
        inviteTag.putString("lecture_id", activeLecture.id);
        inviteTag.putString("topic", activeLecture.topic);
        data.put("ergenverse_lecture_invite_" + cultivator.getCharacterId(), inviteTag);

        // Record cooldown
        playerCooldowns.put(target.getUUID().toString(),
                cultivator.level().getGameTime());

        Ergenverse.LOGGER.info("[NpcLecture] {} invites {} to lecture '{}'",
                cultivator.getCharacterId(), target.getName().getString(),
                activeLecture.id);
    }

    @Override
    public boolean canContinueToUse() {
        return false; // one-shot
    }

    // ── Lecture loading ─────────────────────────────────────────────

    private boolean loadLecturesIfNeeded() {
        if (lecturesLoaded) return !lectures.isEmpty();

        lecturesLoaded = true;
        String characterId = cultivator.getCharacterId();
        if (characterId == null || characterId.isEmpty()) return false;

        try {
            JsonObject data = WorldStateDataLoader.getEntry("npcs", characterId);
            if (data == null || !data.has("lectures")
                    || !data.get("lectures").isJsonArray()) {
                return false;
            }
            JsonArray arr = data.getAsJsonArray("lectures");
            for (JsonElement elem : arr) {
                if (!elem.isJsonObject()) continue;
                JsonObject obj = elem.getAsJsonObject();
                LectureData lecture = LectureData.fromJson(obj);
                if (lecture != null) lectures.add(lecture);
            }
            Ergenverse.LOGGER.info("[NpcLecture] Loaded {} lectures for {}",
                    lectures.size(), characterId);
            return !lectures.isEmpty();
        } catch (Exception e) {
            Ergenverse.LOGGER.debug("[NpcLecture] Failed to load lectures for {}: {}",
                    characterId, e.getMessage());
            return false;
        }
    }

    // ── Active lecture lookup ──────────────────────────────────────

    private LectureData findActiveLecture(long timeOfDay) {
        for (LectureData lecture : lectures) {
            if (timeOfDay >= lecture.timeStart && timeOfDay < lecture.timeEnd) {
                return lecture;
            }
        }
        return null;
    }

    // ── Player eligibility ─────────────────────────────────────────

    private ServerPlayer findEligiblePlayer(LectureData lecture) {
        long currentTick = cultivator.level().getGameTime();

        for (ServerPlayer player : cultivator.level().getEntitiesOfClass(
                ServerPlayer.class,
                cultivator.getBoundingBox().inflate(APPROACH_RANGE))) {
            String uuid = player.getUUID().toString();

            // Check cooldown
            Long lastInvite = playerCooldowns.get(uuid);
            if (lastInvite != null && (currentTick - lastInvite) < COOLDOWN_TICKS) {
                continue;
            }

            // Check if player already attended this lecture
            CompoundTag playerData = player.getPersistentData();
            String attendedKey = "ergenverse_lecture_attended_" + lecture.id;
            if (playerData.getBoolean(attendedKey)) {
                continue;
            }

            // Check if player already has a pending invite from this NPC
            String npcId = cultivator.getCharacterId();
            if (playerData.contains("ergenverse_lecture_invite_" + npcId)) {
                continue; // already invited, waiting for right-click
            }

            return player;
        }
        return null;
    }

    // ── Inner data class ───────────────────────────────────────────

    static final class LectureData {
        final String id;
        final String topic;
        final int timeStart;
        final int timeEnd;
        final String location;
        final String description;
        final String insight;
        final String rewardItem;
        final int rewardCount;

        LectureData(String id, String topic, int timeStart, int timeEnd,
                     String location, String description, String insight,
                     String rewardItem, int rewardCount) {
            this.id = id;
            this.topic = topic;
            this.timeStart = timeStart;
            this.timeEnd = timeEnd;
            this.location = location;
            this.description = description;
            this.insight = insight;
            this.rewardItem = rewardItem;
            this.rewardCount = rewardCount;
        }

        @org.jetbrains.annotations.Nullable
        static LectureData fromJson(JsonObject obj) {
            try {
                String id = obj.has("id") ? obj.get("id").getAsString() : null;
                String topic = obj.has("topic") ? obj.get("topic").getAsString() : "Unknown Topic";
                int timeStart = obj.has("time_start") ? obj.get("time_start").getAsInt() : 0;
                int timeEnd = obj.has("time_end") ? obj.get("time_end").getAsInt() : 24000;
                String location = obj.has("location") ? obj.get("location").getAsString() : "the main hall";
                String description = obj.has("description") ? obj.get("description").getAsString() : "";
                String insight = obj.has("insight") ? obj.get("insight").getAsString() : "";
                String rewardItem = obj.has("reward_item") ? obj.get("reward_item").getAsString() : null;
                int rewardCount = obj.has("reward_count") ? obj.get("reward_count").getAsInt() : 0;
                if (id == null || id.isEmpty()) return null;
                return new LectureData(id, topic, timeStart, timeEnd,
                        location, description, insight, rewardItem, rewardCount);
            } catch (Exception e) {
                return null;
            }
        }
    }
}