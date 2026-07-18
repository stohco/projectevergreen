package dev.ergenverse.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client → Server: request a breakthrough attempt.
 *
 * <p>Sent when the player presses the breakthrough keybind. The server
 * validates all conditions (qi, comprehension, karma, heart demon risk)
 * via {@link dev.ergenverse.cultivation.CultivationEvents#attemptBreakthrough}
 * and responds with narrative messages + tribulation or failure.
 *
 * <p>Packet is empty — the server reads the player's CultivationState
 * capability directly. No payload needed.
 */
public class BreakthroughRequestC2SPacket {

    public BreakthroughRequestC2SPacket() {}

    public BreakthroughRequestC2SPacket(FriendlyByteBuf buf) {
        // No payload — the server reads capability state directly.
    }

    public void encode(FriendlyByteBuf buf) {
        // No payload.
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = ctx.get().getSender();
            if (player != null) {
                dev.ergenverse.cultivation.CultivationEvents.attemptBreakthrough(player);
            }
        });
        ctx.get().setPacketHandled(true);
        return true;
    }
}