package dev.ergenverse.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Server → Client: divine sense pulse result.
 *
 * <p>Sent in response to {@link DivineSensePulseC2SPacket}. Contains
 * the radius, soul power, NPC reactions, and perceived concealed objects.
 * Also includes perception results for nearby EntityCultivator NPCs.
 *
 * <p>The client renders these results in the
 * {@link dev.ergenverse.client.DivineSenseHudOverlay} for 5 seconds
 * (decays via tick counter).
 */
public class DivineSenseResultS2CPacket {

    public final int radius;
    public final long sSense;
    public final List<NpcReactionData> npcReactions;
    public final List<PerceivedObjectData> perceivedObjects;
    public final boolean soulFractureInflicted;
    public final List<PerceivedEntityData> perceivedEntities;

    public DivineSenseResultS2CPacket() {
        this(0, 0, List.of(), List.of(), false, List.of());
    }

    public DivineSenseResultS2CPacket(int radius, long sSense,
                                       List<DivineSensePulseC2SPacket.PerceivedEntity> entities,
                                       List<dev.ergenverse.perception.DivineSense.NpcReaction> reactions,
                                       List<dev.ergenverse.perception.DivineSense.PerceivedObject> objects,
                                       boolean soulFracture) {
        this.radius = radius;
        this.sSense = sSense;
        this.soulFractureInflicted = soulFracture;

        // Convert NPC reactions
        this.npcReactions = new ArrayList<>();
        for (var r : reactions) {
            npcReactions.add(new NpcReactionData(
                    r.npcName, r.outcome, r.detail, r.soulFractureRisk));
        }

        // Convert perceived objects
        this.perceivedObjects = new ArrayList<>();
        for (var o : objects) {
            perceivedObjects.add(new PerceivedObjectData(
                    o.name, o.kind, o.reward, o.coords));
        }

        // Convert perceived entities
        this.perceivedEntities = new ArrayList<>();
        for (var e : entities) {
            perceivedEntities.add(new PerceivedEntityData(
                    e.characterId, e.perceivedName, e.perceivedDescription,
                    e.concealed, e.canInteract, e.entityId));
        }
    }

    // Simplified constructor for DivineSense.PulseResult directly
    public DivineSenseResultS2CPacket(int radius, long sSense,
                                       List<dev.ergenverse.perception.DivineSense.NpcReaction> reactions,
                                       List<dev.ergenverse.perception.DivineSense.PerceivedObject> objects,
                                       boolean soulFracture,
                                       List<DivineSensePulseC2SPacket.PerceivedEntity> entities) {
        this(radius, sSense, entities, reactions, objects, soulFracture);
    }

    public DivineSenseResultS2CPacket(FriendlyByteBuf buf) {
        this.radius = buf.readVarInt();
        this.sSense = buf.readVarLong();

        int reactionCount = buf.readVarInt();
        this.npcReactions = new ArrayList<>(reactionCount);
        for (int i = 0; i < reactionCount; i++) {
            npcReactions.add(new NpcReactionData(buf));
        }

        int objectCount = buf.readVarInt();
        this.perceivedObjects = new ArrayList<>(objectCount);
        for (int i = 0; i < objectCount; i++) {
            perceivedObjects.add(new PerceivedObjectData(buf));
        }

        this.soulFractureInflicted = buf.readBoolean();

        int entityCount = buf.readVarInt();
        this.perceivedEntities = new ArrayList<>(entityCount);
        for (int i = 0; i < entityCount; i++) {
            perceivedEntities.add(new PerceivedEntityData(buf));
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(radius);
        buf.writeVarLong(sSense);

        buf.writeVarInt(npcReactions.size());
        for (var r : npcReactions) r.encode(buf);

        buf.writeVarInt(perceivedObjects.size());
        for (var o : perceivedObjects) o.encode(buf);

        buf.writeBoolean(soulFractureInflicted);

        buf.writeVarInt(perceivedEntities.size());
        for (var e : perceivedEntities) e.encode(buf);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientCultivationCache.onDivineSenseResult(this);
        });
        ctx.get().setPacketHandled(true);
        return true;
    }

    // ─── Nested data classes for network serialization ────────────

    public static final class NpcReactionData {
        public final String npcName;
        public final String outcome;   // intercepted | suppressed | unmasked | ally_alerted | unnoticed
        public final String detail;
        public final double soulFractureRisk;

        public NpcReactionData(String npcName, String outcome,
                               String detail, double soulFractureRisk) {
            this.npcName = npcName;
            this.outcome = outcome;
            this.detail = detail;
            this.soulFractureRisk = soulFractureRisk;
        }

        public NpcReactionData(FriendlyByteBuf buf) {
            this.npcName = buf.readUtf(128);
            this.outcome = buf.readUtf(64);
            this.detail = buf.readUtf(512);
            this.soulFractureRisk = buf.readDouble();
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeUtf(npcName, 128);
            buf.writeUtf(outcome, 64);
            buf.writeUtf(detail, 512);
            buf.writeDouble(soulFractureRisk);
        }
    }

    public static final class PerceivedObjectData {
        public final String name;
        public final String kind;
        public final String reward;
        public final String coords;

        public PerceivedObjectData(String name, String kind, String reward, String coords) {
            this.name = name;
            this.kind = kind;
            this.reward = reward;
            this.coords = coords;
        }

        public PerceivedObjectData(FriendlyByteBuf buf) {
            this.name = buf.readUtf(128);
            this.kind = buf.readUtf(64);
            this.reward = buf.readUtf(256);
            this.coords = buf.readUtf(64);
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeUtf(name, 128);
            buf.writeUtf(kind, 64);
            buf.writeUtf(reward, 256);
            buf.writeUtf(coords, 64);
        }
    }

    public static final class PerceivedEntityData {
        public final String characterId;
        public final String perceivedName;
        public final String perceivedDescription;
        public final boolean concealed;
        public final boolean canInteract;
        public final int entityId;

        public PerceivedEntityData(String characterId, String perceivedName,
                                   String perceivedDescription, boolean concealed,
                                   boolean canInteract, int entityId) {
            this.characterId = characterId;
            this.perceivedName = perceivedName;
            this.perceivedDescription = perceivedDescription;
            this.concealed = concealed;
            this.canInteract = canInteract;
            this.entityId = entityId;
        }

        public PerceivedEntityData(FriendlyByteBuf buf) {
            this.characterId = buf.readUtf(64);
            this.perceivedName = buf.readUtf(128);
            this.perceivedDescription = buf.readUtf(512);
            this.concealed = buf.readBoolean();
            this.canInteract = buf.readBoolean();
            this.entityId = buf.readVarInt();
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeUtf(characterId, 64);
            buf.writeUtf(perceivedName, 128);
            buf.writeUtf(perceivedDescription, 512);
            buf.writeBoolean(concealed);
            buf.writeBoolean(canInteract);
            buf.writeVarInt(entityId);
        }
    }
}