package dev.ergenverse.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class RestrictionSyncS2CPacket {
    public final float progress;
    public final int layer;

    public RestrictionSyncS2CPacket(float progress, int layer) {
        this.progress = progress;
        this.layer = layer;
    }

    public RestrictionSyncS2CPacket(FriendlyByteBuf buf) {
        this.progress = buf.readFloat();
        this.layer = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeFloat(progress);
        buf.writeInt(layer);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().setPacketHandled(true);
        return true;
    }
}
