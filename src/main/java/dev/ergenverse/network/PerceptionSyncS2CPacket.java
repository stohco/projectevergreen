package dev.ergenverse.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Server → Client: periodic perception sync for nearby entities.
 *
 * <p>Sent every ~2 seconds by {@link dev.ergenverse.perception.PerceptionEvents}
 * to keep the client's perception cache fresh. The client uses this cached
 * data for name tag rendering instead of re-running PerceptionEngine every
 * frame (which would be too expensive).
 *
 * <p>Each entry contains the entity ID, perceived name, perceived description,
 * concealment status, and interaction ability — all computed server-side
 * by the PerceptionEngine with the player's ObserverContext.
 */
public class PerceptionSyncS2CPacket {

    public final List<EntityPerception> entries;

    public PerceptionSyncS2CPacket(List<EntityPerception> entries) {
        this.entries = entries;
    }

    public PerceptionSyncS2CPacket(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        this.entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            entries.add(new EntityPerception(buf));
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entries.size());
        for (var entry : entries) {
            entry.encode(buf);
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientCultivationCache.onPerceptionSync(this);
        });
        ctx.get().setPacketHandled(true);
        return true;
    }

    /** One entity's perception data as seen by the receiving player. */
    public static final class EntityPerception {
        public final int entityId;
        public final String perceivedName;
        public final String perceivedDescription;
        public final boolean concealed;
        public final boolean canInteract;

        public EntityPerception(int entityId, String perceivedName,
                               String perceivedDescription, boolean concealed,
                               boolean canInteract) {
            this.entityId = entityId;
            this.perceivedName = perceivedName;
            this.perceivedDescription = perceivedDescription;
            this.concealed = concealed;
            this.canInteract = canInteract;
        }

        public EntityPerception(FriendlyByteBuf buf) {
            this.entityId = buf.readVarInt();
            this.perceivedName = buf.readUtf(128);
            this.perceivedDescription = buf.readUtf(512);
            this.concealed = buf.readBoolean();
            this.canInteract = buf.readBoolean();
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeVarInt(entityId);
            buf.writeUtf(perceivedName, 128);
            buf.writeUtf(perceivedDescription, 512);
            buf.writeBoolean(concealed);
            buf.writeBoolean(canInteract);
        }
    }
}