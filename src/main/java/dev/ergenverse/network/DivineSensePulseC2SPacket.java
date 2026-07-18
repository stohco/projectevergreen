package dev.ergenverse.network;

import dev.ergenverse.cultivation.CultivationCapability;
import dev.ergenverse.cultivation.RealmId;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.perception.DivineSense;
import dev.ergenverse.perception.Objective;
import dev.ergenverse.perception.ObjectiveNature;
import dev.ergenverse.perception.ObserverContext;
import dev.ergenverse.perception.PerceptionEngine;
import dev.ergenverse.perception.PerceptionResult;
import dev.ergenverse.world.WorldLaws;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Client → Server: request a divine sense pulse.
 *
 * <p>Sent when the player presses the Divine Sense keybind (V).
 * The server computes the pulse (radius scan, NPC confrontation,
 * concealed object detection) and sends back a
 * {@link DivineSenseResultS2CPacket}.
 *
 * <p>No payload — the server reads the player's CultivationCapability
 * to compute S_sense, radius, and world-law tier.
 */
public class DivineSensePulseC2SPacket {

    public DivineSensePulseC2SPacket() {}

    public DivineSensePulseC2SPacket(FriendlyByteBuf buf) {}

    public void encode(FriendlyByteBuf buf) {}

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            var stateOpt = CultivationCapability.get(player);
            if (!stateOpt.isPresent()) return;
            var state = stateOpt.resolve().get();

            RealmId realm = state.getCurrentRealm();
            long sSense = DivineSense.soulPowerTotal(realm, 0.0, 0L);

            // Get world-law tier at the player's position (default 0)
            int worldLawTier = WorldLaws.getLawTierAt(player.serverLevel(), player.blockPosition());

            int radius = DivineSense.radius(sSense, worldLawTier, 16);

            // ── Scan for NPC cultivators in range ──
            List<DivineSense.NpcContext> nearbyNpcs = new ArrayList<>();
            List<PerceivedEntity> perceivedEntities = new ArrayList<>();

            for (EntityCultivator entity : player.serverLevel().getEntitiesOfClass(
                    EntityCultivator.class,
                    player.getBoundingBox().inflate(radius))) {
                RealmId npcRealm = parseRealm(entity.getCultivationRealm());

                nearbyNpcs.add(new DivineSense.NpcContext(
                        entity.getCharacterId(),
                        entity.getDisplayNameCn(),
                        "cultivator",
                        npcRealm,
                        0.0, 0L,
                        0.0, 0.0, 0.0
                ));

                // ── Run PerceptionEngine for this entity ──
                // Build enriched ObjectiveNature using PerceptionEvents' enrichment logic
                ObjectiveNature nature = dev.ergenverse.perception.PerceptionEvents
                        .buildEnrichedNatureFor(entity.getCharacterId(), entity, npcRealm);
                ObserverContext observer = new ObserverContext(
                        realm, state.getDaoComprehension(), true,
                        java.util.Set.of(), 1.0, false, false, false);

                Objective objective = new Objective() {
                    @Override
                    public ObjectiveNature nature() { return nature; }
                    @Override
                    public PerceptionResult perceive(ObserverContext obs) {
                        return PerceptionEngine.perceive(this, obs);
                    }
                };
                PerceptionResult perception = PerceptionEngine.perceive(objective, observer);

                perceivedEntities.add(new PerceivedEntity(
                        entity.getCharacterId(),
                        perception.perceivedName,
                        perception.perceivedDescription,
                        perception.concealed,
                        perception.canInteract,
                        entity.getId()
                ));
            }

            // ── Scan for concealed objects (spirit vein blocks) ──
            List<DivineSense.ConcealedObjectContext> concealed = new ArrayList<>();
            var center = player.blockPosition();
            int r = Math.min(radius, 48); // cap scan range to prevent lag
            boolean foundVein = false;

            for (int dx = -r; dx <= r && !foundVein; dx += 4) {
                for (int dy = -(r / 2); dy <= (r / 2) && !foundVein; dy += 4) {
                    for (int dz = -r; dz <= r && !foundVein; dz += 4) {
                        if (dx * dx + dy * dy + dz * dz > r * r) continue;
                        var pos = center.offset(dx, dy, dz);
                        var block = player.serverLevel().getBlockState(pos).getBlock();

                        if (block == Blocks.NETHER_QUARTZ_ORE || block == Blocks.CALCITE) {
                            concealed.add(new DivineSense.ConcealedObjectContext(
                                    "Spirit Vein",
                                    "spirit_vein",
                                    DivineSense.camouflageFor("spirit_vein"),
                                    "Meditate near this to accelerate cultivation",
                                    pos.getX() + "," + pos.getY() + "," + pos.getZ()
                            ));
                            foundVein = true; // Report one per pulse to avoid spam
                        }
                    }
                }
            }

            // ── Compute the divine sense pulse ──
            WorldLaws laws = WorldLaws.named("pulse_neutral").build();

            DivineSense.PulseResult pulseResult = DivineSense.pulse(
                    realm, sSense, worldLawTier,
                    nearbyNpcs, concealed, laws, new java.util.Random());

            // ── Send results to client ──
            var resultPacket = new DivineSenseResultS2CPacket(
                    pulseResult.radius, pulseResult.sSense,
                    pulseResult.reactions, pulseResult.perceivedObjects,
                    pulseResult.soulFractureInflicted, perceivedEntities);
            ERNetwork.getChannel().send(
                    PacketDistributor.PLAYER.with(() -> player),
                    resultPacket);
        });
        ctx.get().setPacketHandled(true);
        return true;
    }

    /** Helper to parse realm string to RealmId. */
    private static RealmId parseRealm(String realmStr) {
        if (realmStr == null || realmStr.isEmpty()) return RealmId.MORTAL;
        try {
            return RealmId.valueOf(realmStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return RealmId.MORTAL;
        }
    }

    /** A perceived entity — the server's perception result for one NPC. */
    public static final class PerceivedEntity {
        public final String characterId;
        public final String perceivedName;
        public final String perceivedDescription;
        public final boolean concealed;
        public final boolean canInteract;
        public final int entityId;

        public PerceivedEntity(String characterId, String perceivedName,
                               String perceivedDescription, boolean concealed,
                               boolean canInteract, int entityId) {
            this.characterId = characterId;
            this.perceivedName = perceivedName;
            this.perceivedDescription = perceivedDescription;
            this.concealed = concealed;
            this.canInteract = canInteract;
            this.entityId = entityId;
        }
    }
}