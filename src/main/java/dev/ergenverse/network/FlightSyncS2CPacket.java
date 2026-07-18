package dev.ergenverse.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class FlightSyncS2CPacket {
    public final boolean flying;

    public FlightSyncS2CPacket(boolean flying) {
        this.flying = flying;
    }

    public FlightSyncS2CPacket(FriendlyByteBuf buf) {
        this.flying = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(flying);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().setPacketHandled(true);
        return true;
    }
}
