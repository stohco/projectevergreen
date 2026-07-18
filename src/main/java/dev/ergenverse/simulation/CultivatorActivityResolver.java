package dev.ergenverse.simulation;

/**
 * CultivatorActivityResolver — the Layer-2 simulation function that decides
 * what an NPC cultivator is doing right now.
 *
 * <p><b>This is the "simulation decides" half of the Reification V3 pipeline:</b>
 * <blockquote>
 *   simulation decides he should meditate → ReificationScan updates his
 *   entity → player sees him sitting in meditation with particle effects.
 * </blockquote>
 *
 * <p>The decision is <b>deterministic</b> given {@code (characterId, tick)}.
 * This is critical: it means every client and the server agree on what an
 * NPC is doing <i>without needing extra network packets</i>. The
 * {@code character_id} is already synced via SynchedEntityData, and the
 * game time is already synced. With a deterministic resolver, both sides
 * compute the same activity — no extra bandwidth.
 *
 * <h2>Why not just random?</h2>
 * <p>If the activity were random per-tick, the entity would flicker between
 * meditating and wandering every tick — visually broken. If it were random
 * per-NPC-instance, two clients would disagree on what the NPC is doing
 * (one sees meditation, the other sees wandering) — desync. Deterministic
 * per-(characterId, time-window) is the correct shape: stable within a
 * window, varies across characters, varies across time.
 *
 * <h2>Schedule design (V1 POC — Wang Tiangui)</h2>
 * <p>Wang Tiangui is the V3 POC character. His schedule is the simplest
 * demonstration of "the simulation has a pulse the player can observe":
 * <ul>
 *   <li><b>Dawn meditation</b> (MC time 0–2000, roughly 6am–8am):
 *       he sits in meditation. The player walking past at dawn sees him
 *       meditating with qi-gathering particles.</li>
 *   <li><b>Daytime wandering</b> (MC time 2000–12000): he wanders his
 *       home area, pausing to look around. The player sees a mortal
 *       going about his day.</li>
 *   <li><b>Dusk meditation</b> (MC time 12000–14000, roughly 6pm–8pm):
 *       he sits in meditation again.</li>
 *   <li><b>Night wandering</b> (MC time 14000–24000): he wanders.</li>
 * </ul>
 *
 * <p>This schedule is <b>Layer 2 simulation design</b>, not Layer 1 canon.
 * The novel never specifies Wang Tiangui's daily routine — but a cultivator
 * (even a mortal who knows the posture) meditating at dawn and dusk is
 * simulation-faithful to the genre. The Bridging Policy permits this kind
 * of design where canon is silent.
 *
 * <h2>Breakthrough schedule (V1 POC)</h2>
 * <p>Breakthroughs are rare events — once per simulated week per character
 * at most, and only when they would otherwise be meditating. For v1 this
 * is a visual flag only (no actual realm change), to demonstrate the
 * "break through" leg of the V3 spec without requiring the full
 * breakthrough engine.
 *
 * <h2>Default schedule (non-POC characters)</h2>
 * <p>For characters other than {@code wang_tiangui}, the resolver uses a
 * deterministic hash of (characterId, time-window) to pick an activity.
 * 25% of windows meditate, 75% wander. Breakthroughs are 1-in-240 windows.
 * This means the same character is consistent across clients, but different
 * characters have different patterns — visual variety without per-NPC canon.
 *
 * <h2>Time windows</h2>
 * <p>The simulation runs on a 100-tick window (5 seconds at 20 TPS). Within
 * a window, the activity is stable — the entity doesn't flicker. Across
 * windows, the activity may change — the entity transitions smoothly
 * because the ReificationScan updates the entity's state at the same
 * 100-tick cadence.
 *
 * <h2>Thread safety</h2>
 * <p>All methods are pure functions of (characterId, tick). Stateless.
 * Safe to call from any thread.
 */
public final class CultivatorActivityResolver {

    private CultivatorActivityResolver() {}

    /** Length of a stable activity window in ticks. 100 = 5 seconds at 20 TPS. */
    public static final long ACTIVITY_WINDOW_TICKS = 100L;

    /** Length of one Minecraft day in ticks. 24000 = 20 minutes real time. */
    public static final long MC_DAY_TICKS = 24000L;

    /**
     * Resolve what activity the given NPC should be doing at the given tick.
     *
     * <p>Pure function — same (characterId, tick) always returns the same
     * activity, on any thread, on any client. This is what makes the
     * simulation observable <i>without</i> extra network traffic.
     *
     * @param characterId the canon character ID (e.g. "wang_tiangui"). If
     *        null or empty, defaults to wandering.
     * @param tick the server's game time (level.getGameTime())
     * @return the activity; never null
     */
    public static CultivatorActivity resolveActivity(String characterId, long tick) {
        if (characterId == null || characterId.isEmpty()) {
            return CultivatorActivity.WANDERING;
        }

        // ── V1 POC: Wang Tiangui has an explicit canon-flavored schedule ──
        if ("wang_tiangui".equals(characterId)) {
            return resolveWangTiangui(tick);
        }

        // ── Default schedule: deterministic hash per (characterId, window) ──
        long window = tick / ACTIVITY_WINDOW_TICKS;
        int hash = stableHash(characterId, window);
        int bucket = (hash & 0x7FFFFFFF) % 1000;

        // 1-in-24 windows (≈ once per 2 minutes per character): breakthrough attempt
        // (only fires when the character would otherwise meditate — see below)
        boolean breakthroughWindow = (bucket < 1000 / 24); // ~41 of 1000 buckets

        // 25% of windows: meditating. Otherwise: wandering.
        if (bucket < 250) {
            // This character is in a meditation window.
            // 1-in-24 meditation windows become breakthrough windows instead.
            if (breakthroughWindow) {
                return CultivatorActivity.BREAKING_THROUGH;
            }
            return CultivatorActivity.MEDITATING;
        }
        return CultivatorActivity.WANDERING;
    }

    /**
     * Wang Tiangui's V1 POC schedule.
     *
     * <p>Dawn meditation (1000–2000), daytime wandering (2000–12000),
     * dusk meditation (12000–14000), night wandering (14000–24000).
     *
     * <p>One breakthrough window per simulated week: when (tick / 24000)
     * mod 7 == 3 AND we're in a dawn meditation window. This means once
     * per 7 MC days, at dawn, Wang Tiangui attempts a breakthrough —
     * visible as a dramatic particle burst. (v1: visual only, no realm
     * change.)
     */
    private static CultivatorActivity resolveWangTiangui(long tick) {
        long timeOfDay = tick % MC_DAY_TICKS;
        long day = tick / MC_DAY_TICKS;

        boolean isDawnMeditation = timeOfDay >= 1000L && timeOfDay < 2000L;
        boolean isDuskMeditation = timeOfDay >= 12000L && timeOfDay < 14000L;

        if (isDawnMeditation) {
            // Once per 7 MC days, at dawn, Wang Tiangui attempts a breakthrough.
            // (Visual only in v1 — no realm change. Demonstrates the "break
            // through" leg of the V3 spec without requiring the full
            // breakthrough engine.)
            if (day % 7L == 3L) {
                return CultivatorActivity.BREAKING_THROUGH;
            }
            return CultivatorActivity.MEDITATING;
        }
        if (isDuskMeditation) {
            return CultivatorActivity.MEDITATING;
        }
        return CultivatorActivity.WANDERING;
    }

    /**
     * A stable, well-distributed hash of (string, long) that returns the
     * same int on every JVM (unlike String.hashCode, which is documented
     * as implementation-dependent but in practice is stable for HotSpot —
     * but we don't want to depend on that for cross-client simulation
     * agreement).
     *
     * <p>Uses FNV-1a 32-bit. Same algorithm on every platform.
     */
    private static int stableHash(String s, long window) {
        int h = 0x811C9DC5; // FNV offset basis
        for (int i = 0; i < s.length(); i++) {
            h ^= s.charAt(i);
            h *= 0x01000193; // FNV prime
        }
        // Mix in the window (long) — 4 bytes at a time
        h ^= (int) (window & 0xFF);
        h *= 0x01000193;
        h ^= (int) ((window >>> 8) & 0xFF);
        h *= 0x01000193;
        h ^= (int) ((window >>> 16) & 0xFF);
        h *= 0x01000193;
        h ^= (int) ((window >>> 24) & 0xFF);
        h *= 0x01000193;
        return h;
    }
}
