package dev.ergenverse.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class FormationSyncS2CPacket {
    public final float progress;
    public final boolean incomplete;

    public FormationSyncS2CPacket(float progress, boolean incomplete) {
        this.progress = progress;
        this.incomplete = incomplete;
    }

    public FormationSyncS2CPacket(FriendlyByteBuf buf) {
        this.progress = buf.readFloat();
        this.incomplete = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeFloat(progress);
        buf.writeBoolean(incomplete);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().setPacketHandled(true);
        return true;
    }
}
