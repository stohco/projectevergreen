package dev.ergenverse.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server → Client: tribulation bolt VFX notification.
 *
 * <p>Tells the client to render a tribulation lightning strike at the
 * given position. The server spawns a real {@code LightningBolt} entity
 * for damage, but this packet gives the client the exact coordinates
 * for a more dramatic visual (screen flash, sound amplification)
 * without relying solely on the entity's random offset.
 *
 * <p>Also carries the bolt index and total count so the client can
 * show "Bolt 3/14" on the HUD.
 */
public class TribulationBoltS2CPacket {

    public final double x;
    public final double y;
    public final double z;
    public final int boltIndex;
    public final int totalBolts;

    public TribulationBoltS2CPacket(double x, double y, double z, int boltIndex, int totalBolts) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.boltIndex = boltIndex;
        this.totalBolts = totalBolts;
    }

    public TribulationBoltS2CPacket(FriendlyByteBuf buf) {
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.boltIndex = buf.readVarInt();
        this.totalBolts = buf.readVarInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeVarInt(boltIndex);
        buf.writeVarInt(totalBolts);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientCultivationCache.onTribulationBolt(this);
        });
        ctx.get().setPacketHandled(true);
        return true;
    }
}