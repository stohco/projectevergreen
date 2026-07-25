package dev.ergenverse.runtime.worldgen;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import com.mojang.serialization.Codec;

/**
 * Registry for custom chunk generators used by the Er Gen Verse.
 *
 * <p><b>Registration pattern:</b> Minecraft 1.20.1 stores chunk generators
 * in the {@link Registries#CHUNK_GENERATOR} registry (path:
 * {@code worldgen/chunk_generator}). Each generator has a {@link Codec} that
 * Minecraft uses to deserialize the generator from the dimension JSON and
 * from saved world data.
 *
 * <p><b>Current generators:</b>
 * <ul>
 *   <li>{@link #BLUEPRINT} — the {@link BlueprintChunkGenerator} for
 *       Planet Suzaku. Replaces {@code minecraft:noise} with canon-aware
 *       terrain that is shaped by the blueprint's geography.</li>
 * </ul>
 *
 * <p><b>Usage:</b> The dimension JSON references the generator type:
 * <pre>{@code
 * {
 *   "generator": {
 *     "type": "ergenverse:blueprint",
 *     "biome_source": { ... }
 *   }
 * }
 * }</pre>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class ErgenverseChunkGenerators {

    /** DeferredRegister for the CHUNK_GENERATOR registry. */
    public static final DeferredRegister<Codec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, Ergenverse.MOD_ID);

    /**
     * The Blueprint Chunk Generator — canon-aware terrain for Planet Suzaku.
     * Registered as {@code ergenverse:blueprint}.
     */
    public static final RegistryObject<Codec<? extends ChunkGenerator>> BLUEPRINT =
            CHUNK_GENERATORS.register("blueprint", () -> BlueprintChunkGenerator.CODEC);

    private ErgenverseChunkGenerators() {}

    /**
     * Register all chunk generators with the mod event bus.
     * Called from {@link dev.ergenverse.core.Ergenverse} constructor.
     */
    public static void register(IEventBus modEventBus) {
        CHUNK_GENERATORS.register(modEventBus);
        Ergenverse.LOGGER.info("[Ergenverse] Chunk generators registered: blueprint");
    }
}
