package dev.ergenverse.simulation.residence;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.runtime.WorldRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

/**
 * WangLinHomeBuilder — the specific hand-crafted builder for Wang Lin's
 * childhood home in Wang Family Village.
 *
 * <p>Per Article XLVII: residences are authored from lives. This builder
 * materializes Wang Lin's home from his {@link ResidentProfile}, through
 * the {@link ResidenceManifestBuilder}, to actual Minecraft blocks via the
 * {@link BlockPlacementEngine}.
 *
 * <p>Per the user's canonical description:
 * <blockquote>
 * Very sparse. No decorations. Carefully maintained. Nothing unnecessary.
 * Storage hidden. Observation point on roof.
 * </blockquote>
 *
 * <p>This is NOT procedural generation. The floor plan is explicitly coded
 * in the BlockPlacementEngine. The manifest provides the semantic content
 * (which rooms, which objects, why they exist). The engine provides the
 * geometry (room sizes, wall positions, furniture placement).
 *
 * <h2>Hand-Authored Floor Plan</h2>
 * <pre>
 *       N
 *   [MeditRm][Bedroom ][Roof/Observation]
 *   [Hidden  ][Bedroom ]
 *   [Workshop][Kitchen ][Entry  ]
 *           [Kitchen ][Storage]
 *           [        ][Courtyard  ]
 *                    E→ (door faces east)
 * </pre>
 *
 * <p>The origin is the northwest corner of the Meditation Room.
 * Total footprint: approximately 13 blocks east × 5 blocks south × 4 blocks tall.
 *
 * <h2>Invocation</h2>
 * <pre>
 *   WangLinHomeBuilder.build(level, villageOrigin.offset(0, 64, 0));
 * </pre>
 *
 * <h2>CRON-74 — Provenance-Aware Rebuild Guard</h2>
 * <p>Every block placement (including the marker) flows through
 * {@link BlockPlacementEngine#setBlock} → {@code WorldRuntime.get().world().setSimulationBlock(...)},
 * so the entire residence is journaled under {@link dev.ergenverse.runtime.Provenance#SIMULATION}.
 * The marker block (CHISELED_STONE_BRICKS at {@code origin.offset(0, 5, 0)} — above the
 * roof, out of sight) is also journaled, so on save+reload the chunk-materializer
 * replays it and {@link #isAlreadyBuilt} correctly returns true — preventing
 * double-builds whether triggered by command, chunk-load, or quest event.
 *
 * <p>The guard is a <b>marker-block</b> check, not a journal query, because the
 * residence's blocks ARE the journal — querying the journal for "any SIMULATION
 * delta at origin" would always return true after the first build, even if the
 * player later demolished the residence. The marker block, by contrast, is a
 * single authoritative sentinel: if it's gone (player broke it, or a
 * PLAYER delta removed it), the residence is considered unbuilt and can be
 * rebuilt.
 */
public final class WangLinHomeBuilder {

    /** Wang Lin's canonical resident ID. */
    public static final String RESIDENT_ID = "wang_lin";

    /** Wang Lin's settlement ID. */
    public static final String SETTLEMENT_ID = "wang_family_village";

    /**
     * The marker block used by {@link #isAlreadyBuilt}. CHISELED_STONE_BRICKS is
     * chosen because it does not appear in the humble oak/cobblestone residence,
     * making the check unambiguous.
     */
    private static final net.minecraft.world.level.block.state.BlockState MARKER =
            Blocks.CHISELED_STONE_BRICKS.defaultBlockState();

    /** Offset from origin to the marker position (above the roof, y=5). */
    private static final BlockPos MARKER_OFFSET = new BlockPos(0, 5, 0);

    private WangLinHomeBuilder() {}

    /**
     * Build Wang Lin's childhood home at the given origin.
     *
     * <p>The origin is the northwest corner of the meditation room (the
     * westernmost room). The entry door faces east.
     *
     * <p>This method:
     * <ol>
     *   <li>Loads Wang Lin's profile from the classpath resources</li>
     *   <li>Builds the ResidenceManifest via ResidenceManifestBuilder</li>
     *   <li>Places the blocks via BlockPlacementEngine</li>
     * </ol>
     *
     * @param level  the server level
     * @param origin the northwest corner of the residence
     */
    public static void build(ServerLevel level, BlockPos origin) {
        // CRON-74: Provenance-aware rebuild guard — if the marker is present,
        // the residence has already been built (and journaled). Skip.
        if (isAlreadyBuilt(level, origin)) {
            Ergenverse.LOGGER.debug("[WangLinHomeBuilder] Already built at {} — skipping", origin);
            return;
        }

        Ergenverse.LOGGER.info("[WangLinHomeBuilder] Building Wang Lin's home at {}", origin);

        // Step 1: Load profile from classpath
        ResidentProfile profile = ResidentProfileLoader.loadOne(SETTLEMENT_ID, RESIDENT_ID);

        if (profile == null) {
            Ergenverse.LOGGER.warn("[WangLinHomeBuilder] Profile not found, using hardcoded fallback");
            profile = hardcodedWangLinProfile();
        }

        Ergenverse.LOGGER.info("[WangLinHomeBuilder] Profile loaded: {} ({}, {} needs)",
                profile.displayName(), profile.occupation(), profile.needs().size());

        // Step 2: Build the manifest
        ResidenceManifest manifest = ResidenceManifestBuilder.build(profile);

        Ergenverse.LOGGER.info("[WangLinHomeBuilder] Manifest built: {} rooms, {} objects",
                manifest.roomCount(), manifest.objectCount());

        // Step 3: Place blocks (all journaled under SIMULATION via BlockPlacementEngine.setBlock)
        BlockPlacementEngine.placeResidence(level, origin, manifest);

        // Step 4: Place the marker block (also journaled under SIMULATION) so
        // isAlreadyBuilt returns true on subsequent calls and on save+reload.
        placeMarker(level, origin);

        Ergenverse.LOGGER.info("[WangLinHomeBuilder] Wang Lin's home complete. Reasoning: {}",
                manifest.manifestReasoning());
    }

    /**
     * Check if Wang Lin's home has already been built at the given origin by
     * testing for the marker block at {@code origin.offset(0, 5, 0)}.
     *
     * <p>The marker is journaled under SIMULATION, so on save+reload the
     * chunk-materializer replays it and this method correctly returns true.
     * If the player breaks the marker (via a PLAYER delta), this returns false
     * and the residence can be rebuilt.
     *
     * @param level  the server level
     * @param origin the northwest corner of the residence
     * @return true if the marker block is present
     */
    public static boolean isAlreadyBuilt(ServerLevel level, BlockPos origin) {
        BlockPos markerPos = origin.offset(MARKER_OFFSET);
        return level.getBlockState(markerPos).getBlock() == MARKER.getBlock();
    }

    /**
     * Place the marker block via the WorldFacade (SIMULATION provenance) so it
     * is journaled and persists across save/load.
     */
    private static void placeMarker(ServerLevel level, BlockPos origin) {
        BlockPos markerPos = origin.offset(MARKER_OFFSET);
        try {
            WorldRuntime rt = WorldRuntime.get();
            if (rt.isInitialized() && rt.suzakuLevel() == level) {
                rt.world().setSimulationBlock(
                        markerPos.getX(), markerPos.getY(), markerPos.getZ(),
                        "minecraft:chiseled_stone_bricks");
                return;
            }
        } catch (Throwable t) {
            Ergenverse.LOGGER.debug("[WangLinHomeBuilder] Facade write failed for marker at {}: {}",
                    markerPos, t.getMessage());
        }
        // Fallback: direct write (non-Suzaku level or runtime not initialized)
        level.setBlock(markerPos, MARKER, 3);
    }

    /**
     * Hardcoded fallback profile if JSON loading fails.
     * This ensures the home can always be placed even if data packs are missing.
     * Matches the JSON in wang_lin.json exactly.
     */
    private static ResidentProfile hardcodedWangLinProfile() {
        return new ResidentProfile(
                RESIDENT_ID,
                "Wang Lin",
                SETTLEMENT_ID,
                "hidden_cultivator",
                List.of("cautious", "observant", "concealment_first", "sparse", "patient", "determined"),
                "qi_condensation",
                List.of(NeedCategory.BASIC_SHELTER, NeedCategory.KITCHEN, NeedCategory.STORAGE,
                        NeedCategory.CULTIVATION_SPACE, NeedCategory.OBSERVATION,
                        NeedCategory.CONCEALMENT, NeedCategory.DEFENSE, NeedCategory.WORKSHOP),
                List.of("flying_sword", "jade_slip", "tea_set"),
                List.of("revealing_strength", "losing_family", "being_discovered_by_sect"),
                List.of("observes_from_roof", "maintains_weapons", "checks_escape_routes",
                        "sleeps_within_reach_of_weapon"),
                List.of("son_of:wang_tianlong", "son_of:wang_mother", "neighbor:old_chen",
                        "student_of:heng_yue_sect"),
                List.of(
                        new ResidenceMemory("Wang Lin repaired the fence after the spring storm",
                                "courtyard_south_fence", "inferred", "pride"),
                        new ResidenceMemory("Wang Lin first sensed qi while meditating here",
                                "cultivation_chamber", "canon", "awe"),
                        new ResidenceMemory("Wang Lin concealed his flying sword beneath the floor here",
                                "hidden_stash", "inferred", "tension")
                ),
                true
        );
    }
}
