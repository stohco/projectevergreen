package dev.ergenverse.history;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.simulation.WorldRuntimeState;
import dev.ergenverse.wanglin.RITimelineEngine;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CanonDivergenceRecorder — the ledger of how THIS timeline diverges from the novel.
 *
 * <p><b>Canon is never overwritten. History records deviations.</b>
 *
 * <p>Per Article XLII (the Four-Layer World Architecture) and Article XLIII
 * (Single-Player Maximalism), the Renegade Immortal canon timeline (E01..E108
 * from {@link RITimelineEngine}) is the <em>immutable canonical sequence</em>.
 * The player's timeline is a <em>fork</em>. When the simulation causes a canon
 * event to occur as-written, it is marked {@link CanonStatus#OCCURRED}. When
 * the player — or the simulation itself — changes the outcome, it is marked
 * {@link CanonStatus#DIVERGED} and the deviation is recorded permanently.
 *
 * <h2>Example</h2>
 * <blockquote>
 *   Canon Event E14 — "Wang Lin's Revenge on the Teng Clan"
 *   <br><b>Canonical:</b> Wang Lin annihilates the Teng family. Teng Huayuan dies.
 *   <br><b>This Timeline:</b> The player distracted Teng Huayuan during the siege.
 *   The battle occurred three days later. 523 civilians survived who would
 *   otherwise have perished. The Teng family still fell — but Wang Lin's
 *   revenge cost him a piece of his dao heart.
 *   <br><b>Reason:</b> Player intervention at the south gate.
 *   <br><b>Status:</b> DIVERGED.
 * </blockquote>
 *
 * <p>This is exactly how source control works: the canon timeline is
 * {@code main}; every save is a branch. Conflicts are recorded, not
 * overwritten. Six months from now, if the canon timeline is enriched with
 * new detail, existing saves keep their historical divergences where they
 * conflict and adopt new authored content where nothing has changed.
 *
 * <h2>The four statuses</h2>
 * <ul>
 *   <li>{@link CanonStatus#PENDING} — the canon event has not yet occurred in
 *       this timeline. The simulation may still cause it, or the player may
 *       prevent/divert it.</li>
 *   <li>{@link CanonStatus#OCCURRED} — the event happened, essentially
 *       as-written. The canon timeline held.</li>
 *   <li>{@link CanonStatus#DIVERGED} — the event happened, but with a
 *       materially different outcome. The fork is recorded.</li>
 *   <li>{@link CanonStatus#PREVENTED} — the event did not happen, and cannot
 *       happen (e.g. the protagonist died before reaching it). The timeline
 *       has permanently diverged at this node.</li>
 *   <li>{@link CanonStatus#DEFERRED} — the event has not happened yet, but
 *       the conditions that would trigger it have been delayed (e.g. Wang Lin
 *       is still in Qi Condensation at age 30 because the player kept him
 *       busy). Not yet a divergence — a delay.</li>
 * </ul>
 *
 * <h2>Persistence</h2>
 * <p>Stored in {@link WorldRuntimeState} under the reserved key
 * {@code "_canon_divergence"}. Survives restart. The full set of 108 canon
 * events is seeded as PENDING on first access.
 *
 * <p><b>Constitution compliance:</b> Art XLII (Layer 3 — Simulation Delta),
 * Art XLIII (saves are alternate histories, not world recreations).
 */
public final class CanonDivergenceRecorder {

    private static final String TAG_DIVERGENCE = "_canon_divergence";
    private static final String TAG_RECORDS = "records";
    private static final String TAG_CANON_ID = "canon_id";
    private static final String TAG_CANON_TITLE = "canon_title";
    private static final String TAG_CANON_DESC = "canon_desc";
    private static final String TAG_STATUS = "status";
    private static final String TAG_ACTUAL_TICK = "actual_tick";
    private static final String TAG_ACTUAL_DESC = "actual_desc";
    private static final String TAG_REASON = "reason";
    private static final String TAG_ACTOR = "actor";

    /** The status of a canon event in this timeline. */
    public enum CanonStatus {
        PENDING,    // has not yet occurred
        OCCURRED,   // occurred essentially as canon
        DIVERGED,   // occurred, but with a different outcome
        PREVENTED,  // did not occur, and cannot
        DEFERRED    // delayed, not yet a divergence
    }

    /** One record per canon event — its canonical form vs this timeline's actual. */
    public record DivergenceRecord(
            String canonEventId,     // "E13", "E14", ...
            String canonTitle,       // the novel's title for this event
            String canonDescription, // what was SUPPOSED to happen
            CanonStatus status,
            long actualTick,         // when it actually happened (0 if not yet)
            String actualDescription,// what actually happened
            String divergenceReason, // why it diverged (if it did)
            String actorId           // who caused the divergence ("player", "wang_lin", "simulation", ...)
    ) {
        public DivergenceRecord {
            if (canonEventId == null) throw new IllegalArgumentException("canonEventId");
            if (canonTitle == null) canonTitle = "";
            if (canonDescription == null) canonDescription = "";
            if (status == null) status = CanonStatus.PENDING;
            if (actualDescription == null) actualDescription = "";
            if (divergenceReason == null) divergenceReason = "";
            if (actorId == null) actorId = "";
        }
    }

    private final Map<String, DivergenceRecord> records = new HashMap<>();
    private static CanonDivergenceRecorder instance = null;

    private CanonDivergenceRecorder() {}

    // ═══════════════════════════════════════════════════════════════════
    //  Recording
    // ═══════════════════════════════════════════════════════════════════

    /** Mark a canon event as having occurred essentially as-written. */
    public void markOccurred(String canonEventId, String actualDescription) {
        DivergenceRecord existing = records.get(canonEventId);
        String title = existing != null ? existing.canonTitle() : canonEventId;
        String canonDesc = existing != null ? existing.canonDescription() : "";
        records.put(canonEventId, new DivergenceRecord(
                canonEventId, title, canonDesc, CanonStatus.OCCURRED,
                System.currentTimeMillis(), actualDescription, "", ""));
    }

    /**
     * Mark a canon event as having occurred — but with a materially different outcome.
     * This is the central operation of the divergence ledger.
     *
     * @param canonEventId     e.g. "E14"
     * @param canonical        what was supposed to happen (the novel's version)
     * @param actual           what actually happened in this timeline
     * @param reason           why it diverged
     * @param actorId          who caused the divergence
     */
    public void markDiverged(String canonEventId, String canonical, String actual,
                              String reason, String actorId) {
        DivergenceRecord existing = records.get(canonEventId);
        String title = existing != null ? existing.canonTitle() : canonEventId;
        records.put(canonEventId, new DivergenceRecord(
                canonEventId, title, canonical, CanonStatus.DIVERGED,
                System.currentTimeMillis(), actual, reason, actorId));
    }

    /** Mark a canon event as permanently prevented — it cannot occur in this timeline. */
    public void markPrevented(String canonEventId, String reason, String actorId) {
        DivergenceRecord existing = records.get(canonEventId);
        String title = existing != null ? existing.canonTitle() : canonEventId;
        String canonDesc = existing != null ? existing.canonDescription() : "";
        records.put(canonEventId, new DivergenceRecord(
                canonEventId, title, canonDesc, CanonStatus.PREVENTED,
                0, "Did not occur.", reason, actorId));
    }

    /** Mark a canon event as deferred — delayed, but not yet a divergence. */
    public void markDeferred(String canonEventId, String reason) {
        DivergenceRecord existing = records.get(canonEventId);
        if (existing == null) return;
        records.put(canonEventId, new DivergenceRecord(
                existing.canonEventId(), existing.canonTitle(), existing.canonDescription(),
                CanonStatus.DEFERRED, 0, "", reason, ""));
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Query
    // ═══════════════════════════════════════════════════════════════════

    public DivergenceRecord status(String canonEventId) {
        return records.get(canonEventId);
    }

    public List<DivergenceRecord> all() {
        List<DivergenceRecord> r = new ArrayList<>(records.values());
        r.sort((a, b) -> a.canonEventId.compareTo(b.canonEventId));
        return Collections.unmodifiableList(r);
    }

    /** Only the records where the timeline has genuinely forked. */
    public List<DivergenceRecord> diverged() {
        List<DivergenceRecord> r = new ArrayList<>();
        for (DivergenceRecord rec : records.values()) {
            if (rec.status == CanonStatus.DIVERGED || rec.status == CanonStatus.PREVENTED) {
                r.add(rec);
            }
        }
        r.sort((a, b) -> a.canonEventId.compareTo(b.canonEventId));
        return Collections.unmodifiableList(r);
    }

    public int countByStatus(CanonStatus s) {
        int n = 0;
        for (DivergenceRecord rec : records.values()) if (rec.status == s) n++;
        return n;
    }

    public int divergenceCount() {
        return countByStatus(CanonStatus.DIVERGED) + countByStatus(CanonStatus.PREVENTED);
    }

    public int totalCanonEvents() { return records.size(); }

    // ═══════════════════════════════════════════════════════════════════
    //  Rendering
    // ═══════════════════════════════════════════════════════════════════

    /** Render a summary report: how many events occurred, diverged, prevented, pending. */
    public Component renderReport() {
        MutableComponent root = Component.literal("§6§l=== Canon Divergence Report ===§r\n");
        root.append(Component.literal("§7Total canon events tracked: §f" + totalCanonEvents() + "§r\n"));
        root.append(Component.literal("§aOccurred as-written: §f" + countByStatus(CanonStatus.OCCURRED) + "§r\n"));
        root.append(Component.literal("§cDiverged: §f" + countByStatus(CanonStatus.DIVERGED) + "§r\n"));
        root.append(Component.literal("§4Prevented: §f" + countByStatus(CanonStatus.PREVENTED) + "§r\n"));
        root.append(Component.literal("§eDeferred: §f" + countByStatus(CanonStatus.DEFERRED) + "§r\n"));
        root.append(Component.literal("§7Pending: §f" + countByStatus(CanonStatus.PENDING) + "§r\n"));
        root.append(Component.literal("§d§lTotal divergences from canon: " + divergenceCount() + "§r"));
        return root;
    }

    /** Render the full list of divergences (DIVERGED + PREVENTED only). */
    public Component renderDivergences() {
        MutableComponent root = Component.literal("§6§l=== Timeline Forks ===§r\n");
        List<DivergenceRecord> forks = diverged();
        if (forks.isEmpty()) {
            root.append(Component.literal("§7§oThe timeline has not yet diverged from canon. "
                    + "What happens next is still the novel's story — for now.§r"));
            return root;
        }
        for (DivergenceRecord rec : forks) {
            root.append(renderRecord(rec)).append(Component.literal("\n"));
        }
        return root;
    }

    /** Render one record in full — the canonical-vs-actual comparison the user described. */
    public Component renderRecord(DivergenceRecord rec) {
        String statusColor = switch (rec.status) {
            case OCCURRED -> "§a";
            case DIVERGED -> "§c";
            case PREVENTED -> "§4";
            case DEFERRED -> "§e";
            case PENDING -> "§7";
        };
        MutableComponent c = Component.literal("");
        c.append(Component.literal(statusColor + "§l[" + rec.canonEventId + "] " + rec.canonTitle + "§r\n"));
        c.append(Component.literal("§7§oCanonical:§r §f" + rec.canonDescription + "§r\n"));
        if (rec.status == CanonStatus.DIVERGED || rec.status == CanonStatus.OCCURRED) {
            c.append(Component.literal("§7§oThis Timeline:§r §f" + rec.actualDescription + "§r\n"));
        }
        if (!rec.divergenceReason.isEmpty()) {
            c.append(Component.literal("§7§oReason:§r §f" + rec.divergenceReason + "§r\n"));
        }
        if (!rec.actorId.isEmpty()) {
            c.append(Component.literal("§7§oCaused by:§r §f" + rec.actorId + "§r\n"));
        }
        c.append(Component.literal(statusColor + "§oStatus: " + rec.status + "§r"));
        return c;
    }

    public Component renderEvent(String canonEventId) {
        DivergenceRecord rec = records.get(canonEventId);
        if (rec == null) {
            return Component.literal("§cNo canon event with id '" + canonEventId + "' is tracked.§r");
        }
        return renderRecord(rec);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Persistence
    // ═══════════════════════════════════════════════════════════════════

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (DivergenceRecord rec : records.values()) {
            CompoundTag r = new CompoundTag();
            r.putString(TAG_CANON_ID, rec.canonEventId);
            r.putString(TAG_CANON_TITLE, rec.canonTitle);
            r.putString(TAG_CANON_DESC, rec.canonDescription);
            r.putString(TAG_STATUS, rec.status.name());
            r.putLong(TAG_ACTUAL_TICK, rec.actualTick);
            r.putString(TAG_ACTUAL_DESC, rec.actualDescription);
            r.putString(TAG_REASON, rec.divergenceReason);
            r.putString(TAG_ACTOR, rec.actorId);
            list.add(r);
        }
        tag.put(TAG_RECORDS, list);
        return tag;
    }

    public static CanonDivergenceRecorder load(CompoundTag tag) {
        CanonDivergenceRecorder cdr = new CanonDivergenceRecorder();
        if (tag == null) return cdr;
        ListTag list = tag.getList(TAG_RECORDS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag r = list.getCompound(i);
            CanonStatus status = CanonStatus.PENDING;
            try { status = CanonStatus.valueOf(r.getString(TAG_STATUS)); }
            catch (IllegalArgumentException ignored) {}
            cdr.records.put(r.getString(TAG_CANON_ID), new DivergenceRecord(
                    r.getString(TAG_CANON_ID),
                    r.getString(TAG_CANON_TITLE),
                    r.getString(TAG_CANON_DESC),
                    status,
                    r.getLong(TAG_ACTUAL_TICK),
                    r.getString(TAG_ACTUAL_DESC),
                    r.getString(TAG_REASON),
                    r.getString(TAG_ACTOR)
            ));
        }
        return cdr;
    }

    // ─── World-global access ──────────────────────────────────────────

    /**
     * Get the world-global divergence recorder. On first access, seeds ALL
     * 108 canon events from {@link RITimelineEngine} as PENDING.
     */
    public static CanonDivergenceRecorder get(ServerLevel level) {
        if (instance != null) return instance;
        WorldRuntimeState runtime = WorldRuntimeState.get(level);
        CompoundTag tag = runtime.getPlayerMutation(TAG_DIVERGENCE);
        instance = load(tag);
        // Seed any canon events not yet present (e.g. new events added to
        // RITimelineEngine in a mod update — they start as PENDING).
        boolean seeded = false;
        for (RITimelineEngine.TimelineEvent te : RITimelineEngine.ALL_EVENTS) {
            if (!instance.records.containsKey(te.id)) {
                String canonDesc = te.consequences != null && !te.consequences.isEmpty()
                        ? te.consequences : te.title;
                instance.records.put(te.id, new DivergenceRecord(
                        te.id, te.title, canonDesc, CanonStatus.PENDING,
                        0, "", "", ""));
                seeded = true;
            }
        }
        if (seeded) {
            Ergenverse.LOGGER.info("[CanonDivergenceRecorder] Seeded {} canon events as PENDING.",
                    instance.records.size());
        }
        return instance;
    }

    public static void persist(ServerLevel level) {
        if (instance == null) return;
        WorldRuntimeState runtime = WorldRuntimeState.get(level);
        CompoundTag tag = runtime.getPlayerMutation(TAG_DIVERGENCE);
        tag.put(TAG_RECORDS, instance.save().getList(TAG_RECORDS, Tag.TAG_LIST));
        runtime.updatePlayerMutation(TAG_DIVERGENCE, tag);
    }

    /** Mark + persist in one call. */
    public static void recordDivergence(ServerLevel level, String canonEventId,
                                          String canonical, String actual,
                                          String reason, String actorId) {
        CanonDivergenceRecorder cdr = get(level);
        cdr.markDiverged(canonEventId, canonical, actual, reason, actorId);
        persist(level);
    }

    /** Mark occurred + persist in one call. */
    public static void recordOccurred(ServerLevel level, String canonEventId, String actualDescription) {
        CanonDivergenceRecorder cdr = get(level);
        cdr.markOccurred(canonEventId, actualDescription);
        persist(level);
    }

    public static void clearCache() { instance = null; }

    public String getStatusReport() {
        return "CanonDivergenceRecorder{events=" + records.size()
                + ", diverged=" + divergenceCount() + "}";
    }
}
