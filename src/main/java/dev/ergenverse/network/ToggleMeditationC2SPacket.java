package dev.ergenverse.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client → Server: toggle meditation at the targeted block position.
 *
 * <p>Sent when the player sneak-right-clicks a spirit vein block
 * ({@code quartz_ore}). The server validates the block type and
 * player state before toggling meditation.
 *
 * <p>Carries the target block position so the server can verify the
 * player is actually near a spirit vein.
 */
public class ToggleMeditationC2SPacket {

    public final int x;
    public final int y;
    public final int z;

    public ToggleMeditationC2SPacket(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public ToggleMeditationC2SPacket(FriendlyByteBuf buf) {
        this.x = buf.readVarInt();
        this.y = buf.readVarInt();
        this.z = buf.readVarInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(x);
        buf.writeVarInt(y);
        buf.writeVarInt(z);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = ctx.get().getSender();
            if (player != null) {
                dev.ergenverse.cultivation.MeditationHandler.toggleMeditation(
                        player, new net.minecraft.core.BlockPos(x, y, z));
            }
        });
        ctx.get().setPacketHandled(true);
        return true;
    }
}