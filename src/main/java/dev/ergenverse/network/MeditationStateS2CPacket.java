package dev.ergenverse.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server → Client: meditation visual feedback.
 *
 * <p>Tells the client to toggle the meditation overlay (closed-eye
 * particle effect + slowness + HUD meditation indicator). This is a
 * lightweight boolean packet — the full state sync carries isMeditating
 * too, but this fires immediately on toggle for responsive feedback.
 */
public class MeditationStateS2CPacket {

    public final boolean meditating;

    public MeditationStateS2CPacket(boolean meditating) {
        this.meditating = meditating;
    }

    public MeditationStateS2CPacket(FriendlyByteBuf buf) {
        this.meditating = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(meditating);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientCultivationCache.setMeditatingVisual(meditating);
        });
        ctx.get().setPacketHandled(true);
        return true;
    }
}