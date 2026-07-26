package dev.ergenverse.runtime.delta;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.Optional;

/**
 * BlockStateCodec — serialize and parse {@link BlockState} to/from the
 * {@code "minecraft:chest[facing=north,waterlogged=false]"} string format.
 *
 * <p><b>CRON-COMPLETIONIST-94 — PROPERTY-AWARE BLOCK STATE PARSING.</b>
 *
 * <p>Prior to CRON-94, the {@link dev.ergenverse.runtime.layer.WorldFacade}
 * and {@link dev.ergenverse.runtime.worldgen.BlueprintChunkGenerator} each
 * had a private {@code resolveBlockState(String blockId)} that called
 * {@link Block#defaultBlockState()} — discarding all property overrides
 * (facing, half, shape, waterlogged, etc.). This meant player-placed chests,
 * stairs, slabs, doors, fences, buttons, levers, signs, beds, repeaters,
 * comparators, pistons, dispensers, droppers, hoppers, and observers all
 * reverted to their default facing/state on chunk reload — a real persistence
 * bug that had been carried over since CRON-91.
 *
 * <p>CRON-94 closes this gap by introducing this shared codec. The codec:
 * <ul>
 *   <li>{@link #serialize(BlockState)} — converts a {@link BlockState} to the
 *       canonical string format {@code "namespace:path[prop=val,...]"} via
 *       {@link BlockState#toString()} (which Mojang's {@code StateHolder}
 *       already formats correctly).</li>
 *   <li>{@link #parse(String)} — parses a string in the same format back to
 *       a {@link BlockState}, applying each {@code prop=val} pair via
 *       {@link BlockState#setValue(Property, Comparable)}. Returns the
 *       default state if the string has no properties, or if any property
 *       is unknown/invalid (defensive — keeps the world loading even if a
 *       mod block's properties changed between saves).</li>
 * </ul>
 *
 * <p><b>Format specification:</b>
 * <pre>
 *   blockStateString := blockId [ "[" propertyList "]" ]
 *   blockId          := namespace ":" path        (e.g. "minecraft:chest")
 *   propertyList     := property ( "," property )*
 *   property         := propertyName "=" value    (e.g. "facing=north")
 * </pre>
 *
 * <p>This matches the format produced by {@link BlockState#toString()} and
 * accepted by vanilla's {@code /setblock} command. Examples:
 * <ul>
 *   <li>{@code "minecraft:stone"} — no properties, default state</li>
 *   <li>{@code "minecraft:chest[facing=north]"} — chest facing north</li>
 *   <li>{@code "minecraft:oak_stairs[facing=east,half=top,shape=straight,waterlogged=false]"}</li>
 *   <li>{@code "minecraft:air"} — air (no properties)</li>
 * </ul>
 *
 * <p><b>Backward compatibility:</b> old journal entries that store only the
 * block id (e.g. {@code "minecraft:chest"}) parse correctly — they produce
 * the default state, exactly as the pre-CRON-94 {@code resolveBlockState}
 * did. This means CRON-94 is a pure upgrade: existing saves continue to
 * work, and new placements capture the full state. The only behavioral
 * change is that NEW player placements (after CRON-94) will preserve their
 * facing/state across reload.
 *
 * <p><b>Error handling:</b> {@link #parse(String)} is defensive — it NEVER
 * throws. If the block id is unknown, it returns {@code null}. If a property
 * name is unknown or the value is invalid for that property, the property
 * is silently skipped and parsing continues with the remaining properties.
 * This ensures a corrupted or version-mismatched journal entry doesn't
 * crash the chunk materializer — the worst case is a block with the wrong
 * facing, not a crashed world.
 *
 * <p><b>Why a shared codec (not two private copies):</b> the pre-CRON-94
 * design had {@code resolveBlockState} duplicated in WorldFacade and
 * BlueprintChunkGenerator, with a comment saying the duplication was
 * intentional "to avoid pulling WorldFacade into the chunk-gen dependency
 * graph." CRON-94 consolidates them into this codec, which is in the
 * {@code delta} package — a dependency of BOTH WorldFacade and the chunk
 * generator (both already import {@link BlockChangeDelta}). The codec has
 * no dependency on WorldFacade, so the chunk-gen purity is preserved.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
 *
 * @see dev.ergenverse.runtime.layer.WorldFacade#resolveBlockState
 * @see dev.ergenverse.runtime.worldgen.BlueprintChunkGenerator#resolveBlockState
 */
public final class BlockStateCodec {

    private BlockStateCodec() {}

    /**
     * Serialize a {@link BlockState} to the canonical string format.
     *
     * <p>Delegates to {@link BlockState#toString()}, which Mojang's
     * {@code StateHolder} formats as {@code "namespace:path[prop=val,...]"}.
     * For states with no properties (e.g. stone, air), the output is just
     * {@code "namespace:path"}.
     *
     * <p>This is the inverse of {@link #parse(String)} —
     * {@code parse(serialize(state))} returns a state equal to {@code state}
     * (same block, same property values).
     *
     * @param state the block state to serialize (must not be null)
     * @return the canonical string, e.g. {@code "minecraft:chest[facing=north]"}
     */
    public static String serialize(BlockState state) {
        // BlockState.toString() delegates to StateHolder.toString(), which
        // produces the canonical "namespace:path[prop=val,...]" format.
        // This is the same format /setblock uses and accepts.
        return state.toString();
    }

    /**
     * Parse a block state string to a {@link BlockState}.
     *
     * <p>Accepts both bare ids ({@code "minecraft:stone"}) and full state
     * strings ({@code "minecraft:chest[facing=north,waterlogged=false]"}).
     * Returns the default state if no properties are specified. Returns
     * {@code null} if the block id is unknown or unresolvable.
     *
     * <p><b>Defensive:</b> never throws. Unknown property names and invalid
     * values are silently skipped — the remaining properties are still
     * applied. This ensures a corrupted journal entry doesn't crash the
     * chunk materializer.
     *
     * @param blockStateString the string to parse (may be null or empty —
     *                         returns null in that case)
     * @return the resolved {@link BlockState}, or null if the block id is
     *         unknown
     */
    public static BlockState parse(String blockStateString) {
        if (blockStateString == null || blockStateString.isEmpty()) return null;

        try {
            // Split into block id and optional [properties] part.
            // Format: "minecraft:chest[facing=north,waterlogged=false]"
            //         or "minecraft:stone" (no properties)
            String idPart;
            String propsPart = null;

            int bracketIdx = blockStateString.indexOf('[');
            if (bracketIdx >= 0) {
                idPart = blockStateString.substring(0, bracketIdx);
                int closeIdx = blockStateString.indexOf(']', bracketIdx);
                if (closeIdx > bracketIdx + 1) {
                    propsPart = blockStateString.substring(bracketIdx + 1, closeIdx);
                }
                // If closeIdx == bracketIdx + 1, that's "minecraft:chest[]" —
                // treat as no properties (empty propsPart, default state).
            } else {
                idPart = blockStateString;
            }

            // Resolve the block id to a Block via the Forge registry.
            ResourceLocation rl;
            try {
                rl = new ResourceLocation(idPart);
            } catch (Throwable t) {
                Ergenverse.LOGGER.debug("[Ergenverse] BlockStateCodec: invalid block id '{}': {}",
                        idPart, t.getMessage());
                return null;
            }
            Block block = ForgeRegistries.BLOCKS.getValue(rl);
            if (block == null) {
                Ergenverse.LOGGER.debug("[Ergenverse] BlockStateCodec: unknown block '{}'", idPart);
                return null;
            }

            BlockState state = block.defaultBlockState();
            if (propsPart == null || propsPart.isEmpty()) {
                return state;
            }

            // Parse "facing=north,waterlogged=false" → apply each pair.
            StateDefinition<Block, BlockState> definition = block.getStateDefinition();
            String[] pairs = propsPart.split(",");
            for (String pair : pairs) {
                String trimmed = pair.trim();
                if (trimmed.isEmpty()) continue;
                int eqIdx = trimmed.indexOf('=');
                if (eqIdx <= 0 || eqIdx >= trimmed.length() - 1) {
                    Ergenverse.LOGGER.debug("[Ergenverse] BlockStateCodec: skipping malformed property '{}'", trimmed);
                    continue;
                }
                String propName = trimmed.substring(0, eqIdx).trim();
                String valueName = trimmed.substring(eqIdx + 1).trim();

                Property<?> prop = definition.getProperty(propName);
                if (prop == null) {
                    Ergenverse.LOGGER.debug("[Ergenverse] BlockStateCodec: unknown property '{}' on block '{}'",
                            propName, idPart);
                    continue;
                }
                Optional<?> value = prop.getValue(valueName);
                if (value.isEmpty()) {
                    Ergenverse.LOGGER.debug("[Ergenverse] BlockStateCodec: invalid value '{}' for property '{}' on block '{}'",
                            valueName, propName, idPart);
                    continue;
                }
                // CRON-94: capture-helper pattern — Java's type inference can't
                // unify Property<?> with the Comparable<?> from getValue, so we
                // route through a generic method that captures the type at the
                // call site. This is the standard Mojang pattern (see
                // BlockStateParser.setValue).
                state = applyPropertyValue(state, prop, value.get());
            }

            return state;
        } catch (Throwable t) {
            Ergenverse.LOGGER.debug("[Ergenverse] BlockStateCodec: failed to parse '{}': {}",
                    blockStateString, t.getMessage());
            return null;
        }
    }

    /**
     * Type-safe helper to apply a {@link Property} value to a {@link BlockState}.
     *
     * <p>Java's type inference can't unify {@code Property<?>} with the
     * {@code Comparable<?>} returned by {@link Property#getValue} — the two
     * wildcards capture independently. This generic method captures the type
     * parameter {@code T} at the call site, binding both the property and the
     * value to the same {@code T}. The unchecked cast is safe because
     * {@link Property#getValue} guarantees it returns a value of the property's
     * own value type.
     *
     * @param <T>   the comparable type
     * @param state the block state to mutate
     * @param prop  the property to set
     * @param value the value (must be a valid value for this property)
     * @return the new block state with the property set
     */
    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> BlockState applyPropertyValue(BlockState state,
                                                                            Property<?> prop,
                                                                            Object value) {
        // Unchecked cast — safe because getValue() returns the property's own
        // value type. The cast is the standard pattern used by Mojang's
        // BlockStateParser (see its `setValue` helper).
        Property<T> typedProp = (Property<T>) prop;
        T typedValue = (T) value;
        return state.setValue(typedProp, typedValue);
    }
}
