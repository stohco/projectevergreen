package dev.ergenverse.spawn;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.WorldData;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * DeterministicSeedHandler — forces a fixed canon world seed so every new
 * world generates identical terrain.
 *
 * <p><b>Canon rationale (Article I — Canon Is Reality):</b> The Er Gen Verse
 * is a single, objective world. Wang Family Village is always at (3842, ?, -1184)
 * on Planet Suzaku. The Sea of Devils is always to the east. The Forest of
 * Distorted Divine Sense is always in the northeast. This geographic consistency
 * REQUIRES a deterministic seed — a random seed would place Zhao Country's
 * mountains in different locations each playthrough, breaking canon.
 *
 * <p><b>Implementation:</b> On {@link ServerAboutToStartEvent} (fires before
 * any chunks generate), we force the world seed to the canon value via
 * reflection. This works for single-player (the only supported mode per
 * Article XLIII) because:
 * <ul>
 *   <li>The integrated server's level data is mutable at this stage.</li>
 *   <li>No chunks have been generated yet on a new world.</li>
 *   <li>The chunk generator reads the seed when generating new chunks, so
 *       all terrain after this point uses the canon seed.</li>
 * </ul>
 *
 * <p>The canon seed is derived from the string "Suzaku" (朱雀). This gives a
 * stable, named seed that produces consistent geography.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
 */
@Mod.EventBusSubscriber(modid = Ergenverse.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DeterministicSeedHandler {

    private DeterministicSeedHandler() {}

    /**
     * The canon world seed. Derived from "Suzaku".hashCode() — a stable,
     * named value that ensures identical terrain generation every playthrough.
     *
     * <p>This seed is what makes the Er Gen Verse a single, objective world
     * rather than a randomly-generated sandbox. Per Article I: "The Er Gen
     * novels are not inspiration. They are the objective laws of this
     * universe." A random seed would violate canon by producing different
     * geography each time.
     */
    public static final long CANON_SEED = ((long) "Suzaku".hashCode() << 32)
            | ((long) "PlanetSuzaku".hashCode() & 0xFFFFFFFFL);

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        MinecraftServer server = event.getServer();
        if (server == null) return;

        try {
            WorldData worldData = server.getWorldData();
            if (worldData == null) {
                Ergenverse.LOGGER.warn("[Ergenverse] Canon Seed: WorldData is null. Skipping seed enforcement.");
                return;
            }

            long currentSeed = readSeed(worldData);

            if (currentSeed != CANON_SEED) {
                Ergenverse.LOGGER.info(
                        "[Ergenverse] Canon Seed Enforcement: current seed {} differs from canon seed {}. Forcing canon seed.",
                        currentSeed, CANON_SEED);

                boolean forced = forceWorldSeed(worldData, CANON_SEED);

                if (forced) {
                    Ergenverse.LOGGER.info("[Ergenverse] Canon seed {} enforced. All terrain will match canon geography.",
                            CANON_SEED);
                } else {
                    Ergenverse.LOGGER.warn("[Ergenverse] Canon seed could not be forced via reflection. " +
                            "The world will use the original random seed. The hand-built village and fixed " +
                            "spawn point still ensure a consistent player experience.");
                }
            } else {
                Ergenverse.LOGGER.info("[Ergenverse] Canon seed already in use ({}). No enforcement needed.",
                        CANON_SEED);
            }
        } catch (Exception e) {
            Ergenverse.LOGGER.error("[Ergenverse] Failed to enforce canon seed: {}", e.getMessage(), e);
        }
    }

    /**
     * Read the current world seed via reflection. Tries multiple known field
     * paths across MC 1.20.x:
     * <ul>
     *   <li>WorldData.worldOptions.seed (1.20.1+)</li>
     *   <li>WorldData.worldGenSettings().seed() (older 1.20)</li>
     * </ul>
     */
    private static long readSeed(WorldData worldData) {
        // Try WorldData.worldOptions.seed
        try {
            Field optionsField = findField(worldData.getClass(), "worldOptions");
            if (optionsField != null) {
                optionsField.setAccessible(true);
                Object options = optionsField.get(worldData);
                if (options != null) {
                    Field seedField = findField(options.getClass(), "seed");
                    if (seedField != null) {
                        seedField.setAccessible(true);
                        return seedField.getLong(options);
                    }
                    // Try seed() method
                    Method seedMethod = findMethod(options.getClass(), "seed");
                    if (seedMethod != null) {
                        seedMethod.setAccessible(true);
                        return (long) seedMethod.invoke(options);
                    }
                }
            }
        } catch (Throwable ignored) {}

        // Try worldGenSettings().seed()
        try {
            Method wgsMethod = findMethod(worldData.getClass(), "worldGenSettings");
            if (wgsMethod != null) {
                wgsMethod.setAccessible(true);
                Object wgs = wgsMethod.invoke(worldData);
                if (wgs != null) {
                    Method seedMethod = findMethod(wgs.getClass(), "seed");
                    if (seedMethod != null) {
                        seedMethod.setAccessible(true);
                        return (long) seedMethod.invoke(wgs);
                    }
                }
            }
        } catch (Throwable ignored) {}

        return 0L; // unknown
    }

    /**
     * Force the world seed via reflection. Returns true on success.
     */
    private static boolean forceWorldSeed(WorldData worldData, long newSeed) {
        try {
            // Path 1: WorldData.worldOptions.seed
            Field optionsField = findField(worldData.getClass(), "worldOptions");
            if (optionsField != null) {
                optionsField.setAccessible(true);
                Object options = optionsField.get(worldData);
                if (options != null) {
                    Field seedField = findField(options.getClass(), "seed");
                    if (seedField != null) {
                        seedField.setAccessible(true);
                        unsafePutLong(options, seedField, newSeed);
                        Ergenverse.LOGGER.info("[Ergenverse] Seed forced via worldOptions.seed field.");
                        return true;
                    }
                }
            }

            // Path 2: WorldData.worldGenSettings().seed()
            Method wgsMethod = findMethod(worldData.getClass(), "worldGenSettings");
            if (wgsMethod != null) {
                wgsMethod.setAccessible(true);
                Object wgs = wgsMethod.invoke(worldData);
                if (wgs != null) {
                    Field seedField = findField(wgs.getClass(), "seed");
                    if (seedField != null) {
                        seedField.setAccessible(true);
                        unsafePutLong(wgs, seedField, newSeed);
                        Ergenverse.LOGGER.info("[Ergenverse] Seed forced via worldGenSettings.seed field.");
                        return true;
                    }
                }
            }
        } catch (Throwable t) {
            Ergenverse.LOGGER.warn("[Ergenverse] Seed forcing failed: {}", t.getMessage());
        }
        return false;
    }

    // ── Reflection helpers ──

    private static Field findField(Class<?> clazz, String name) {
        Class<?> c = clazz;
        while (c != null && c != Object.class) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                c = c.getSuperclass();
            }
        }
        return null;
    }

    private static Method findMethod(Class<?> clazz, String name) {
        Class<?> c = clazz;
        while (c != null && c != Object.class) {
            try {
                for (Method m : c.getDeclaredMethods()) {
                    if (m.getName().equals(name) && m.getParameterCount() == 0) {
                        return m;
                    }
                }
            } catch (Throwable ignored) {}
            c = c.getSuperclass();
        }
        return null;
    }

    /**
     * Set a (possibly final) long field using sun.misc.Unsafe. Falls back to
     * Field.setLong if Unsafe is unavailable.
     */
    private static void unsafePutLong(Object obj, Field field, long value) throws IllegalAccessException {
        try {
            Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);
            long offset = unsafe.objectFieldOffset(field);
            unsafe.putLong(obj, offset, value);
        } catch (Throwable t) {
            // Fallback: direct set (may fail on final fields, but worth trying)
            field.setLong(obj, value);
        }
    }
}
