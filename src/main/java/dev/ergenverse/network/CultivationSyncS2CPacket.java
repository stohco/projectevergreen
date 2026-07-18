package dev.ergenverse.network;

import dev.ergenverse.cultivation.CultivationState;
import dev.ergenverse.cultivation.RealmId;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Server → Client: full cultivation state snapshot.
 *
 * <p>Sent on player login and whenever the server's CultivationState
 * changes meaningfully (realm change, tribulation start/end, qi crosses
 * 10% threshold). The client stores this in a static cache for HUD
 * rendering and perception checks — it NEVER writes back.
 *
 * <p>Compact binary encoding: realm as varint, doubles as floats
 * (qi/breakthrough/karma/divineSense are 0-1; precision loss is
 * acceptable for HUD display). Techniques list as length-prefixed
 * string array.
 */
public class CultivationSyncS2CPacket {

    public final int realmOrder;
    public final String realmName;
    public final float qi;
    public final float maxQi;
    public final float karma;
    public final float breakthroughProgress;
    public final float divineSense;
    public final float bloodRefinement;
    public final float heartDemonRisk;
    public final float lifeForce;
    public final boolean tribulationPending;
    public final boolean isMeditating;
    public final List<String> techniques;
    public final List<String> daoKeys;
    public final List<Float> daoValues;

    public CultivationSyncS2CPacket(CultivationState state, boolean isMeditating) {
        RealmId realm = state.getCurrentRealm();
        this.realmOrder = realm.order;
        this.realmName = realm.name;
        this.qi = (float) state.getQi();
        this.maxQi = (float) state.getMaxQi();
        this.karma = (float) state.getKarma();
        this.breakthroughProgress = (float) state.getBreakthroughProgress();
        this.divineSense = (float) state.getDivineSense();
        this.bloodRefinement = (float) state.getBloodRefinement();
        this.heartDemonRisk = (float) state.getHeartDemonRisk();
        this.lifeForce = (float) (state.getLifeForce() / 365.25);
        this.tribulationPending = state.isTribulationPending();
        this.isMeditating = isMeditating;
        this.techniques = List.copyOf(state.getCurrentTechniques());
        var daoMap = state.getDaoComprehension();
        this.daoKeys = new ArrayList<>(daoMap.size());
        this.daoValues = new ArrayList<>(daoMap.size());
        for (var entry : daoMap.entrySet()) {
            daoKeys.add(entry.getKey());
            daoValues.add((float) (double) entry.getValue());
        }
    }

    public CultivationSyncS2CPacket(FriendlyByteBuf buf) {
        this.realmOrder = buf.readVarInt();
        this.realmName = buf.readUtf(64);
        this.qi = buf.readFloat();
        this.maxQi = buf.readFloat();
        this.karma = buf.readFloat();
        this.breakthroughProgress = buf.readFloat();
        this.divineSense = buf.readFloat();
        this.bloodRefinement = buf.readFloat();
        this.heartDemonRisk = buf.readFloat();
        this.lifeForce = buf.readFloat();
        this.tribulationPending = buf.readBoolean();
        this.isMeditating = buf.readBoolean();
        int techCount = buf.readVarInt();
        this.techniques = new ArrayList<>(techCount);
        for (int i = 0; i < techCount; i++) {
            techniques.add(buf.readUtf(64));
        }
        int daoCount = buf.readVarInt();
        this.daoKeys = new ArrayList<>(daoCount);
        this.daoValues = new ArrayList<>(daoCount);
        for (int i = 0; i < daoCount; i++) {
            daoKeys.add(buf.readUtf(64));
            daoValues.add(buf.readFloat());
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(realmOrder);
        buf.writeUtf(realmName, 64);
        buf.writeFloat(qi);
        buf.writeFloat(maxQi);
        buf.writeFloat(karma);
        buf.writeFloat(breakthroughProgress);
        buf.writeFloat(divineSense);
        buf.writeFloat(bloodRefinement);
        buf.writeFloat(heartDemonRisk);
        buf.writeFloat(lifeForce);
        buf.writeBoolean(tribulationPending);
        buf.writeBoolean(isMeditating);
        buf.writeVarInt(techniques.size());
        for (String tech : techniques) {
            buf.writeUtf(tech, 64);
        }
        buf.writeVarInt(daoKeys.size());
        for (int i = 0; i < daoKeys.size(); i++) {
            buf.writeUtf(daoKeys.get(i), 64);
            buf.writeFloat(daoValues.get(i));
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientCultivationCache.receiveSync(this);
        });
        ctx.get().setPacketHandled(true);
        return true;
    }
}