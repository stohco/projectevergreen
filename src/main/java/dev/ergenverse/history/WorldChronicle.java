package dev.ergenverse.history;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.simulation.WorldRuntimeState;
import dev.ergenverse.simulation.event.WorldEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * WorldChronicle — the living history of this playthrough, written by the simulation itself.
 *
 * <p><b>Not a quest log. Not a wiki. A chronicle.</b>
 *
 * <p>Per Article XLII (the Four-Layer World Architecture) and Article XLIII
 * (Single-Player Maximalism), every significant event that actually occurs
 * in <em>this</em> timeline is compiled into a prose annal. Centuries later
 * in the same save, NPCs do not quote the novels — they reference <em>your
 * world's</em> history. Scholars debate it. Storytellers exaggerate it.
 * Children retell distorted versions of it.
 *
 * <p>The Chronicle sits atop {@link WorldHistory} (the raw event log) the way
 * a historian sits atop a chronicle of dates: it selects, it phrases, it
 * tones. Where WorldHistory records {@code "npc.death @tick 41203 wang_zhou"},
 * the Chronicle records:
 * <blockquote>
 *   <i>Year 0, early autumn. Wang Zhou, father of Wang Lin, was slain by a
 *   wolf at the south bend of the village. His son was fourteen. The village
 *   remembered. The simulation remembered. Nothing would undo it.</i>
 * </blockquote>
 *
 * <h2>Immutability of the past</h2>
 * <p>Once written, a Chronicle entry is never edited. History is append-only.
 * If later events recontextualize an earlier one (the wolf was actually a
 * disguised Teng servant), a <em>new</em> entry is written that references
 * the old — the original record stands. This is how real history works.
 *
 * <h2>Persistence</h2>
 * <p>Stored in {@link WorldRuntimeState} under the reserved key
 * {@code "_world_chronicle"} (mirroring {@link WorldHistory}'s
 * {@code "_world_history"} pattern). Survives server restart, chunk unload,
 * and world save/load. Loaded on demand, persisted on every append.
 *
 * <h2>Tone</h2>
 * <p>The Chronicle is not neutral. It is written with the gravity of a
 * cultivation-world historian: triumphant for breakthroughs, tragic for
 * deaths, ominous for sect wars, mysterious for inheritances. This tone is
 * what makes it <em>read</em> like a chronicle and not a debug log.
 *
 * <p><b>Constitution compliance:</b> Art III (simulation before progression),
 * Art V (exists without the player), Art XLII (Layer 3 — Simulation Delta),
 * Art XLIII (single-player maximalism: saves are historical records).
 */
public final class WorldChronicle {

    private static final String TAG_CHRONICLE = "_world_chronicle";
    private static final String TAG_ENTRIES = "entries";
    private static final String TAG_TIMESTAMP = "timestamp";
    private static final String TAG_YEAR = "year";
    private static final String TAG_ERA = "era";
    private static final String TAG_ENTRY_TYPE = "entry_type";
    private static final String TAG_TITLE = "title";
    private static final String TAG_NARRATIVE = "narrative";
    private static final String TAG_TONE = "tone";
    private static final String TAG_REGION_ID = "region_id";
    private static final String TAG_CANON_SOURCE = "canon_source";

    /** 1 MC day = 24000 ticks. 1 sim year = 365 days. */
    public static final long TICKS_PER_DAY = 24000L;
    public static final long TICKS_PER_YEAR = TICKS_PER_DAY * 365L;

    /** Cap to prevent unbounded growth across centuries of play. */
    private static final int MAX_ENTRIES = 5000;

    /** The narrative tone of an entry — determines how it is phrased and rendered. */
    public enum ChronicleTone {
        TRIUMPHANT,  // breakthroughs, inheritances claimed, sects founded
        TRAGIC,      // deaths, village abandonment, sect falls
        OMINOUS,     // wars declared, spirit veins exhausted, restrictions weakening
        MYSTERIOUS,  // discoveries, inheritance sites, ancient residues
        MUNDANE,     // trade, travel, daily life
        PROPHETIC    // divergence from canon — the timeline has forked
    }

    /** A single chronicle entry — one paragraph of the world's history. */
    public record ChronicleEntry(
            long timestamp,
            long year,
            String era,
            String entryType,
            String title,
            String narrative,
            ChronicleTone tone,
            String regionId,
            String canonSource
    ) {
        public ChronicleEntry {
            if (entryType == null) throw new IllegalArgumentException("entryType");
            if (title == null) throw new IllegalArgumentException("title");
            if (narrative == null) throw new IllegalArgumentException("narrative");
            if (era == null) era = "PRESENT";
            if (regionId == null) regionId = "";
            if (canonSource == null) canonSource = "";
            if (tone == null) tone = ChronicleTone.MUNDANE;
        }
    }

    private final List<ChronicleEntry> entries = new ArrayList<>();
    private static WorldChronicle instance = null;

    private WorldChronicle() {}

    // ═══════════════════════════════════════════════════════════════════
    //  Recording
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Append a chronicle entry. Append-only — never edits prior entries.
     * Trims to {@link #MAX_ENTRIES} (oldest dropped) if exceeded.
     */
    public void record(ChronicleEntry entry) {
        entries.add(entry);
        while (entries.size() > MAX_ENTRIES) {
            entries.remove(0);
        }
    }

    /**
     * Compile a WorldEventBus event into a Chronicle entry and append it.
     * This is the primary path by which the simulation writes its own history.
     *
     * <p>The event's topic determines the entryType and tone; the description
     * is woven into a prose narrative. Canon-sourced events are flagged so
     * the reader can distinguish "this happened because the novel said so"
     * from "this happened because the world actually did it."
     */
    public void recordFromBusEvent(WorldEvent event) {
        if (event == null) return;
        long tick = event.timestamp();
        long year = tick / TICKS_PER_YEAR;
        String topic = event.topic() == null ? "" : event.topic();
        String desc = event.description() == null ? "" : event.description();
        String canon = event.canonSource() == null ? "" : event.canonSource();

        CompiledEntry ce = compile(topic, desc, canon, year);
        String era = canon.isEmpty() ? "DIVERGENT" : inferEraFromCanon(canon);
        String region = event.isGlobal() ? "global" : ("r_" + event.pos().getX() / 256
                + "_" + event.pos().getZ() / 256);
        record(new ChronicleEntry(tick, year, era, ce.entryType, ce.title,
                ce.narrative, ce.tone, region, canon));
    }

    /** Seed the chronicle with the canonical t₀ opening — the world at the moment the player arrives. */
    public void seedOpening(long tick) {
        long year = tick / TICKS_PER_YEAR;
        record(new ChronicleEntry(tick, year, "EARLY_LIFE", "T0_SNAPSHOT",
                "The World Stirs",
                "Year 0. Wang Lin is a mortal youth of fifteen summers in Wang Family Village, "
                        + "on the southeast fringe of Zhao Country. His father Wang Zhou is alive. "
                        + "He has not yet joined Heng Yue Sect. The Teng family grudge has not ignited. "
                        + "The Sea of Devils churns in silence. The Suzaku Tomb waits, sealed. "
                        + "History is about to begin — and what happens next belongs to this timeline alone.",
                ChronicleTone.PROPHETIC, "wang_family_village", "RI Ch.1"));
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Query
    // ═══════════════════════════════════════════════════════════════════

    public List<ChronicleEntry> all() {
        return Collections.unmodifiableList(entries);
    }

    public List<ChronicleEntry> recent(int n) {
        if (n <= 0 || entries.isEmpty()) return List.of();
        int start = Math.max(0, entries.size() - n);
        return Collections.unmodifiableList(new ArrayList<>(entries.subList(start, entries.size())));
    }

    public List<ChronicleEntry> forEra(String era) {
        if (era == null) return List.of();
        List<ChronicleEntry> r = new ArrayList<>();
        for (ChronicleEntry e : entries) if (era.equals(e.era)) r.add(e);
        return Collections.unmodifiableList(r);
    }

    public List<ChronicleEntry> forRegion(String regionId) {
        if (regionId == null) return List.of();
        List<ChronicleEntry> r = new ArrayList<>();
        for (ChronicleEntry e : entries) if (regionId.equals(e.regionId)) r.add(e);
        return Collections.unmodifiableList(r);
    }

    public int size() { return entries.size(); }

    // ═══════════════════════════════════════════════════════════════════
    //  Rendering to chat Components
    // ═══════════════════════════════════════════════════════════════════

    /** Render the N most recent entries as a chat Component (newest last). */
    public Component renderRecent(int n) {
        MutableComponent root = Component.literal("§6§l=== World Chronicle ===§r\n");
        List<ChronicleEntry> slice = recent(n);
        if (slice.isEmpty()) {
            root.append(Component.literal("§7§oThe chronicle is empty. The world has not yet stirred.§r"));
            return root;
        }
        for (ChronicleEntry e : slice) {
            root.append(renderEntry(e)).append(Component.literal("\n"));
        }
        root.append(Component.literal("§8" + entries.size() + " entries recorded.§r"));
        return root;
    }

    /** Render all entries for a given era. */
    public Component renderEra(String era) {
        MutableComponent root = Component.literal("§6§l=== Chronicle: " + era + " ===§r\n");
        List<ChronicleEntry> slice = forEra(era);
        if (slice.isEmpty()) {
            root.append(Component.literal("§7§oNo entries for this era.§r"));
            return root;
        }
        for (ChronicleEntry e : slice) {
            root.append(renderEntry(e)).append(Component.literal("\n"));
        }
        return root;
    }

    /** Render one entry with tone-appropriate coloring. */
    private Component renderEntry(ChronicleEntry e) {
        String toneColor = switch (e.tone) {
            case TRIUMPHANT -> "§e";   // gold
            case TRAGIC -> "§c";       // red
            case OMINOUS -> "§5";      // dark purple
            case MYSTERIOUS -> "§9";   // blue
            case PROPHETIC -> "§d";    // light purple
            case MUNDANE -> "§7";      // gray
        };
        MutableComponent c = Component.literal("");
        c.append(Component.literal(toneColor + "§l[Year " + e.year + "] " + e.title + "§r\n"));
        c.append(Component.literal(toneColor + "§o" + e.narrative + "§r"));
        if (!e.canonSource.isEmpty()) {
            c.append(Component.literal("\n§8§o  — " + e.canonSource + "§r"));
        }
        return c;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Compilation (topic → entryType + tone + prose)
    // ═══════════════════════════════════════════════════════════════════

    private record CompiledEntry(String entryType, String title, String narrative, ChronicleTone tone) {}

    private CompiledEntry compile(String topic, String desc, String canon, long year) {
        // Default
        String entryType = "WORLD_EVENT";
        ChronicleTone tone = ChronicleTone.MUNDANE;
        String title = "An Event Occurred";

        if (topic.startsWith("npc.breakthrough")) {
            entryType = "NPC_BREAKTHROUGH";
            tone = ChronicleTone.TRIUMPHANT;
            title = "A Cultivator Breaks Through";
        } else if (topic.startsWith("npc.death")) {
            entryType = "NPC_DEATH";
            tone = ChronicleTone.TRAGIC;
            title = "A Death in the World";
        } else if (topic.startsWith("sect.founded") || topic.startsWith("sect.rise")) {
            entryType = "SECT_RISE";
            tone = ChronicleTone.TRIUMPHANT;
            title = "A Sect Rises";
        } else if (topic.startsWith("sect.destroyed") || topic.startsWith("sect.fall")) {
            entryType = "SECT_FALL";
            tone = ChronicleTone.TRAGIC;
            title = "A Sect Falls";
        } else if (topic.startsWith("sect.war_declared") || topic.startsWith("sect.war")) {
            entryType = "WAR_DECLARED";
            tone = ChronicleTone.OMINOUS;
            title = "War Is Declared";
        } else if (topic.startsWith("spirit_vein.discovered")) {
            entryType = "SPIRIT_VEIN_DISCOVERED";
            tone = ChronicleTone.MYSTERIOUS;
            title = "A Spirit Vein Is Found";
        } else if (topic.startsWith("spirit_vein.weakened") || topic.startsWith("spirit_vein.exhausted")) {
            entryType = "SPIRIT_VEIN_EXHAUSTED";
            tone = ChronicleTone.OMINOUS;
            title = "A Spirit Vein Wanes";
        } else if (topic.startsWith("beast.legendary") || topic.startsWith("beast.born")) {
            entryType = "LEGENDARY_BEAST_BORN";
            tone = ChronicleTone.MYSTERIOUS;
            title = "A Legendary Beast Is Born";
        } else if (topic.startsWith("beast.slain") || topic.startsWith("beast.killed")) {
            entryType = "LEGENDARY_BEAST_SLAIN";
            tone = ChronicleTone.TRIUMPHANT;
            title = "A Legendary Beast Is Slain";
        } else if (topic.startsWith("inheritance.claimed") || topic.startsWith("inheritance.seized")) {
            entryType = "INHERITANCE_CLAIMED";
            tone = ChronicleTone.MYSTERIOUS;
            title = "An Inheritance Is Claimed";
        } else if (topic.startsWith("restriction.broken") || topic.startsWith("restriction.shattered")) {
            entryType = "RESTRICTION_BROKEN";
            tone = ChronicleTone.OMINOUS;
            title = "A Restriction Falls";
        } else if (topic.startsWith("village.abandoned") || topic.startsWith("village.fall")) {
            entryType = "VILLAGE_ABANDONED";
            tone = ChronicleTone.TRAGIC;
            title = "A Village Is Abandoned";
        } else if (topic.startsWith("karma.blood_feud") || topic.startsWith("karma.feud")) {
            entryType = "BLOOD_FEUD_CREATED";
            tone = ChronicleTone.OMINOUS;
            title = "A Blood Feud Begins";
        } else if (topic.startsWith("karma.life_debt") || topic.startsWith("karma.debt_repaid")) {
            entryType = "LIFE_DEBT_REPAID";
            tone = ChronicleTone.TRIUMPHANT;
            title = "A Life Debt Is Settled";
        } else if (topic.startsWith("wang_lin")) {
            entryType = "WANG_LIN";
            tone = ChronicleTone.PROPHETIC;
            title = "Wang Lin";
        }

        String narrative = weaveNarrative(entryType, desc, year);
        return new CompiledEntry(entryType, title, narrative, tone);
    }

    private String weaveNarrative(String entryType, String desc, long year) {
        if (desc == null || desc.isBlank()) {
            return "Year " + year + ". Something occurred that the world will remember.";
        }
        // Light prose framing — we don't rewrite the description, we frame it.
        return switch (entryType) {
            case "NPC_DEATH" -> "Year " + year + ". " + desc
                    + " The dead do not return. The simulation records this, and moves on.";
            case "NPC_BREAKTHROUGH" -> "Year " + year + ". " + desc
                    + " The heavens trembled, and a new tier of power walked the world.";
            case "SECT_FALL" -> "Year " + year + ". " + desc
                    + " What stood for centuries is no more. The survivors scatter; the ruins await.";
            case "SECT_RISE" -> "Year " + year + ". " + desc
                    + " A new power plants its banner, and the regional balance shifts.";
            case "WAR_DECLARED" -> "Year " + year + ". " + desc
                    + " Neither side will yield. The coming battles will be written here.";
            case "SPIRIT_VEIN_DISCOVERED" -> "Year " + year + ". " + desc
                    + " What was hidden is now known — and what is known is coveted.";
            case "SPIRIT_VEIN_EXHAUSTED" -> "Year " + year + ". " + desc
                    + " The well runs dry. The sects that depended on it must adapt or fall.";
            case "INHERITANCE_CLAIMED" -> "Year " + year + ". " + desc
                    + " A legacy millennia in the waiting has found a new bearer.";
            case "VILLAGE_ABANDONED" -> "Year " + year + ". " + desc
                    + " The hearths are cold. The names fade. The land forgets slowly.";
            case "BLOOD_FEUD_CREATED" -> "Year " + year + ". " + desc
                    + " A grudge is forged that will outlive both who made it.";
            case "LIFE_DEBT_REPAID" -> "Year " + year + ". " + desc
                    + " A debt that bound two souls is settled. The ledger closes.";
            default -> "Year " + year + ". " + desc;
        };
    }

    private String inferEraFromCanon(String canon) {
        if (canon == null) return "PRESENT";
        String c = canon.toLowerCase();
        if (c.contains("ancient")) return "ANCIENT_ERA";
        if (c.contains("early life") || c.contains("ch.1") || c.contains("ch.2") || c.contains("ch.3"))
            return "EARLY_LIFE";
        if (c.contains("zhao country") || c.contains("ch.4") || c.contains("ch.5") || c.contains("ch.6"))
            return "ZHAO_COUNTRY_ARC";
        if (c.contains("suzaku") || c.contains("inheritance"))
            return "SUZAKU_INHERITANCE_ARC";
        if (c.contains("heavenly fate") || c.contains("heavenly_fate"))
            return "HEAVENLY_FATE_ARC";
        if (c.contains("allheaven") || c.contains("all_heaven"))
            return "ALLHEAVEN_ARC";
        return "PRESENT";
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Persistence (mirrors WorldHistory's pattern)
    // ═══════════════════════════════════════════════════════════════════

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (ChronicleEntry e : entries) {
            CompoundTag entry = new CompoundTag();
            entry.putLong(TAG_TIMESTAMP, e.timestamp);
            entry.putLong(TAG_YEAR, e.year);
            entry.putString(TAG_ERA, e.era);
            entry.putString(TAG_ENTRY_TYPE, e.entryType);
            entry.putString(TAG_TITLE, e.title);
            entry.putString(TAG_NARRATIVE, e.narrative);
            entry.putString(TAG_TONE, e.tone.name());
            entry.putString(TAG_REGION_ID, e.regionId);
            entry.putString(TAG_CANON_SOURCE, e.canonSource);
            list.add(entry);
        }
        tag.put(TAG_ENTRIES, list);
        return tag;
    }

    public static WorldChronicle load(CompoundTag tag) {
        WorldChronicle wc = new WorldChronicle();
        if (tag == null) return wc;
        ListTag list = tag.getList(TAG_ENTRIES, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag e = list.getCompound(i);
            ChronicleTone tone = ChronicleTone.MUNDANE;
            try { tone = ChronicleTone.valueOf(e.getString(TAG_TONE)); }
            catch (IllegalArgumentException ignored) {}
            wc.entries.add(new ChronicleEntry(
                    e.getLong(TAG_TIMESTAMP),
                    e.getLong(TAG_YEAR),
                    e.getString(TAG_ERA),
                    e.getString(TAG_ENTRY_TYPE),
                    e.getString(TAG_TITLE),
                    e.getString(TAG_NARRATIVE),
                    tone,
                    e.getString(TAG_REGION_ID),
                    e.getString(TAG_CANON_SOURCE)
            ));
        }
        return wc;
    }

    // ─── World-global access ──────────────────────────────────────────

    /** Get the world-global chronicle, loading from WorldRuntimeState if needed. */
    public static WorldChronicle get(ServerLevel level) {
        if (instance != null) return instance;
        WorldRuntimeState runtime = WorldRuntimeState.get(level);
        CompoundTag tag = runtime.getPlayerMutation(TAG_CHRONICLE);
        instance = load(tag);
        return instance;
    }

    /** Persist the chronicle to WorldRuntimeState. */
    public static void persist(ServerLevel level) {
        if (instance == null) return;
        WorldRuntimeState runtime = WorldRuntimeState.get(level);
        CompoundTag tag = runtime.getPlayerMutation(TAG_CHRONICLE);
        tag.put(TAG_ENTRIES, instance.save().getList(TAG_ENTRIES, Tag.TAG_LIST));
        runtime.updatePlayerMutation(TAG_CHRONICLE, tag);
    }

    /** Record a bus event into the chronicle and persist. Called by ChronicleSubscriber. */
    public static void recordBusEvent(ServerLevel level, WorldEvent event) {
        WorldChronicle wc = get(level);
        wc.recordFromBusEvent(event);
        persist(level);
    }

    /** Clear the cached instance (on world unload). */
    public static void clearCache() { instance = null; }

    /** A short status string for debug. */
    public String getStatusReport() {
        return "WorldChronicle{entries=" + entries.size() + "}";
    }
}
