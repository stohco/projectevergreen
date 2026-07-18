package dev.ergenverse.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

/**
 * TalismanCraftC2SPacket — minimal stub. Full implementation will send craft requests to server.
 */
public class TalismanCraftC2SPacket {
    public final String recipeId;
    public final String mode;

    public TalismanCraftC2SPacket(String recipeId, String mode) {
        this.recipeId = recipeId;
        this.mode = mode;
    }

    public TalismanCraftC2SPacket(FriendlyByteBuf buf) {
        this.recipeId = buf.readUtf();
        this.mode = buf.readUtf();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(recipeId != null ? recipeId : "");
        buf.writeUtf(mode != null ? mode : "");
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().setPacketHandled(true);
        return true;
    }
}
