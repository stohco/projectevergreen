package dev.ergenverse.cultivation;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
// NOTE: If Forge 65 removes LazyOptional in favor of standard Optional,
// replace LazyOptional<T> with Optional<T> throughout this file,
// and update getCapability() return types accordingly.
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Forge capability registration for cultivation state.
 *
 * <p>Attaches {@link CultivationState} to every player entity via
 * the Forge capability system. The capability persists across
 * save/load through NBT serialization.
 *
 * <h2>Registration Pattern (Forge 52+ / MC 1.21.x)</h2>
 * <p>The modern Forge capability pattern uses:
 * <ul>
 *   <li>{@code CapabilityManager.get(new CapabilityToken<>(){})} for
 *       capability instance creation</li>
 *   <li>{@link RegisterCapabilitiesEvent} for capability type registration</li>
 *   <li>{@link AttachCapabilitiesEvent.Entity} for attaching the
 *       provider to player entities</li>
 *   <li>{@link ICapabilitySerializable} for NBT persistence</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * // Get cultivation state from a player (server side)
 * CultivationState state = CultivationCapability.get(player).orElse(null);
 * if (state != null) {
 *     state.addQi(50.0);
 * }
 * }</pre>
 *
 * <p>The capability is registered on the mod event bus (for
 * RegisterCapabilitiesEvent) and the Forge event bus (for
 * AttachCapabilitiesEvent).
 */
public final class CultivationCapability {

    private CultivationCapability() {}

    /** The capability key, used to identify this capability in NBT and provider lookups. */
    public static final ResourceLocation CAP_KEY =
            new ResourceLocation(Ergenverse.MOD_ID, "cultivation_state");

    /**
     * The Capability instance. Uses the modern {@link CapabilityToken}
     * pattern instead of the deprecated {@code @CapabilityInject}.
     */
    public static final Capability<CultivationState> CULTIVATION_STATE =
            CapabilityManager.get(new CapabilityToken<>() {});

    /**
     * Register the capability type. Called from {@link RegisterCapabilitiesEvent}.
     *
     * <p>This must be called during the mod lifecycle's capability
     * registration phase. In the mod entry point, register this on
     * the mod event bus:
     * <pre>{@code
     * modEventBus.addListener(CultivationCapability::register);
     * }</pre>
     */
    public static void register(RegisterCapabilitiesEvent event) {
        event.register(CultivationState.class);
        Ergenverse.LOGGER.info("[Ergenverse] Cultivation capability registered.");
    }

    /**
     * Get the cultivation state from a player entity.
     *
     * @param player the player entity
     * @return a LazyOptional that may contain the CultivationState
     */
    public static LazyOptional<CultivationState> get(Player player) {
        return player.getCapability(CULTIVATION_STATE);
    }

    /**
     * Get the cultivation state, throwing if absent (for contexts where
     * it must exist, e.g., server-side player tick).
     *
     * @param player the player entity
     * @return the CultivationState (never null)
     * @throws IllegalStateException if the capability is not attached
     */
    public static CultivationState getOrThrow(Player player) {
        return get(player).orElseThrow(() ->
                new IllegalStateException("CultivationState capability missing from player " + player.getName().getString()));
    }

    // ─── Provider ─────────────────────────────────────────────────────

    /**
     * The capability provider that attaches CultivationState to player entities.
     *
     * <p>Implements {@code ICapabilitySerializable<CompoundTag>} so that
     * Forge automatically calls serialize/deserialize during save/load.
     *
     * <p>The provider is constructed once per entity attachment and
     * holds a single {@link CultivationState} instance for that player.
     */
    // NOTE: If Forge 65 removes ICapabilitySerializable, rewrite to implement
    // ICapabilityProvider + INBTSerializable<CompoundTag> separately.
    public static class Provider implements ICapabilitySerializable<CompoundTag> {

        private final CultivationState state = new CultivationState();
        private final LazyOptional<CultivationState> lazy = LazyOptional.of(() -> state);

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            if (cap == CULTIVATION_STATE) {
                return lazy.cast();
            }
            return LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            return state.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            state.deserializeNBT(tag);
        }
    }

    // ─── Event handler (registered on Forge bus) ──────────────────────

    /**
     * Forge event subscriber that attaches the cultivation capability
     * to player entities.
     *
     * <p>Register on the Forge event bus:
     * <pre>{@code
     * MinecraftForge.EVENT_BUS.register(CultivationCapability.AttachHandler.class);
     * }</pre>
     *
     * <p>Only players receive the cultivation capability. NPCs, mobs,
     * and other entities use their own systems.
     */
    @Mod.EventBusSubscriber(modid = Ergenverse.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class AttachHandler {

        @SubscribeEvent
        public static void onAttachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof Player) {
                event.addCapability(CAP_KEY, new Provider());
            }
        }
    }
}