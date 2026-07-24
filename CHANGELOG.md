# Ergenverse — Changelog

All notable changes to the Ergenverse Forge mod are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning 2.0.0](https://semver.org/spec/v2.0.0.html)
with an `-alpha` pre-release suffix while in development.

---

## Versioning Policy (established 2026-07-24)

**Every commit set that changes observable behavior gets a version bump, a git
tag, a GitHub release, and a rebuilt JAR — in the same commit.** This policy
was not enforced prior to v0.1.3-alpha; the retroactive tags below document
the historical behavior boundaries that should have been versioned at the time.

| Change type | Bump | Example |
|---|---|---|
| Crash fix / load-blocking bug | patch | `0.1.0-alpha` → `0.1.1-alpha` |
| New items / blocks / loot content | patch | `0.1.1-alpha` → `0.1.2-alpha` |
| Custom mechanics, new systems | minor | `0.1.x-alpha` → `0.2.0-alpha` |
| World-save incompatibility / recipe break | minor + changelog warning | → `0.3.0-alpha` |
| API or registry-name breaking change | major (out of 0.x) | → `1.0.0-alpha` |

If the version string in `gradle.properties` / `mods.toml` has not moved, the
change is not done.

---

## [0.1.3-alpha] — 2026-07-24

### Added
- **CHANGELOG.md** — this file. Establishes the versioning policy going forward.
- **30 missing item registrations** in `ErgenverseItems`:
  - Spirit stone currency: `spirit_stone_low`, `spirit_stone_mid`, `spirit_stone_high`, `immortal_stone`
  - Beast cores / essences: `ancient_god_core`, `ancient_god_bone`, `azure_dragon_core`, `cloud_whale_core`, `lei_ji_core`, `nether_core`, `thunder_toad_core`, `soul_fragment`, `soul_lasher`, `spirit_vein_essence`, `blood_essence`, `dragon_scale`, `nine_color_flame`, `tribulation_fragment`, `dao_fragment`
  - Equipment / artifacts: `spirit_armor`, `heaven_fan`, `star_sealing_flag`, `soul_refining_flag`, `cave_world_key`, `starry_sky_token`, `eighteen_hell_stamp`, `vermilion_emperor_seal`, `storage_pouch`, `cultivation_mat`, `flying_sword`
- **24 placeholder textures** for the new items so they don't render as missing-texture purple-black.

### Fixed
- **103 loot table parse errors → 0.** Three root causes, all MC 1.20.1 format-migration bugs:
  - 21 tables used invalid `rolls` format `{rolls:N, min_rolls:N, max_rolls:N}` → corrected to `{min:N, max:N}`.
  - 124 `set_count` / `set_damage` functions used array `count: [min, max]` → corrected to object `{min, max}`.
  - 6 recipe files referenced stale item names (`blank_talisman_paper` → `talisman_paper_blank`, etc.).
  - `foreign_void_rift.json` used `minecraft:empty` as an item name → converted to entry type `minecraft:empty`.
  - `minecraft:sticks` → `minecraft:stick`, `minecraft:planks` → `minecraft:oak_planks`.
- **spirit_stone_wall blockstate** — 324 model bakery warnings eliminated. MC 1.20.1 wall side properties use `none`/`low`/`tall`, not `true`/`false`. Changed `"north": "true"` to OR conditions matching both `low` and `tall` for all four sides. The `up` property correctly remains `true`/`false`.
- **Registry freeze crash from duplicate item names.** 4 items (`vermilion_bird_feather`, `ji_realm`, `karma_whip`, `heaven_defying_bead`) were registered by both `ErgenverseItems` and the `WangLinItems` arsenal (which strips path prefixes). Removed the duplicates from `ErgenverseItems`; the arsenal versions win. Added comments explaining why.
- **spirit_stone duplicate** (caught before crash). `ErgenverseBlocks.registerSimple("spirit_stone")` already creates a block item; the manual `SPIRIT_STONE` registration in `ErgenverseItems` was removed.

### Changed
- **Version discipline established.** `gradle.properties` `mod_version` and `mods.toml` `version` now track the latest tag. Retroactive tags `v0.1.1-alpha` and `v0.1.2-alpha` created at the historical behavior boundaries that should have been versioned at the time.

### Known Limitations (carried forward honestly)
- All 30 new items are generic `new Item(new Item.Properties().rarity(...))` — no custom right-click mechanics, durability, or effects appropriate to each item's canon function. This is a content-stub for loot-table unblocking, not a mechanics pass.
- Loot tables now parse but mostly drop vanilla items (emeralds, diamonds) — should drop ergenverse items in a future content pass.
- Client rendering verified only through model baking in a headless llvmpipe environment. Title screen, world creation, and in-game rendering on a real GPU are still unverified.
- Other wall/fence blockstates may have the same MC version-migration issue as `spirit_stone_wall`; only that one was audited.

---

## [0.1.2-alpha] — 2026-07-24 (retroactive tag at `a74c82c`)

**Retroactive tag.** The source at this commit still reads `version="0.1.0-alpha"`
because version discipline was not enforced at the time. This tag marks the
commit where the client first reached the main menu.

### Fixed
- **ERKeybinds `IllegalArgumentException` — client main menu crash.** The class was annotated `@Mod.EventBusSubscriber(bus = Bus.MOD)` but `InputEvent.Key` is a FORGE bus event, not a MOD bus event. A single class cannot serve both buses. Split into two nested classes:
  - `ModBusEvents` (`bus = Bus.MOD`) — handles `RegisterKeyMappingsEvent`
  - `ForgeBusEvents` (`bus = Bus.FORGE`) — handles `InputEvent.Key`
- **68 chest loot tables missing `type: minecraft:chest`.** Added the missing type field.

### Note
This was the single most important fix in the project's history. Before this
commit, the mod was literally unplayable client-side — it crashed during mod
loading, before the title screen. The server smoke test passed because
keybind classes are `Dist.CLIENT` only. 70+ prior CRON rounds shipped a mod
that no player could ever launch.

---

## [0.1.1-alpha] — 2026-07-24 (retroactive tag at `6ce50b6`)

**Retroactive tag.** The source at this commit still reads `version="0.1.0-alpha"`
because version discipline was not enforced at the time. This tag marks the
commit where the dedicated server first booted successfully.

### Fixed
- **`SoulGourdItem` `stacksTo(1)` + `durability(500)` conflict.** In MC 1.20.1, `durability()` implicitly forces `maxStackSize=1`; calling `stacksTo()` on top throws `Unable to have damage AND stack`. Removed `.stacksTo(1)` from the constructor; kept `.durability(500)` from registration.
- **`WangLinItems` / `ErgenverseItems` `soul_gourd` duplicate registration.** The WangLin arsenal manifest lists `ergen:wanglin/soul_gourd`, which `ManifestEntry.registryName()` strips to `soul_gourd`. Two DeferredRegisters claiming the same name → override maps to `air` → registry sync crash. Added `ERGENVERSE_ITEMS_OWNED_NAMES` skip-set (`soul_gourd`, `storage_ring`, `cultivation_journal`, `beast_core`) so the arsenal defers to `ErgenverseItems` for items with real mechanics.
- **Startup crash + biome spawn errors.** Biome spawn keys used `minSize`/`maxSize` instead of MC 1.20.1's `minCount`/`maxCount`.

### Note
This was the first time the mod ever ran successfully — `Done (48.953s)!` on
the dedicated server. Prior to this, every boot attempt crashed during item
registration or registry freeze.

---

## [0.1.0-alpha] — 2026-07-20 (existing tag at `f40264c`)

Baseline "mod loads" build. 814 classes, 13MB JAR. See the existing GitHub
release notes for the 5 bootstrap-audit launch fixes that made the mod load
at all.

### Note
This is the only pre-v0.1.3-alpha tag that was created at the correct time.
All commits between this tag and v0.1.3-alpha should have been versioned
incrementally but were not — see the retroactive tags above.
