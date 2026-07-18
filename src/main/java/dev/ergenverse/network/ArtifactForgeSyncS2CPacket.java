package dev.ergenverse.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

/**
 * ArtifactForgeSyncS2CPacket — syncs forge progress, incomplete flag, and mode.
 */
public class ArtifactForgeSyncS2CPacket {
    public final float progress;
    public final boolean incomplete;
    public final String mode;

    public ArtifactForgeSyncS2CPacket(float progress, boolean incomplete, String mode) {
        this.progress = progress;
        this.incomplete = incomplete;
        this.mode = mode;
    }

    public ArtifactForgeSyncS2CPacket(FriendlyByteBuf buf) {
        this.progress = buf.readFloat();
        this.incomplete = buf.readBoolean();
        this.mode = buf.readUtf();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeFloat(progress);
        buf.writeBoolean(incomplete);
        buf.writeUtf(mode != null ? mode : "");
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().setPacketHandled(true);
        return true;
    }
}
