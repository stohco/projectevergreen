package dev.ergenverse.npc.rumor;

import java.util.Random;

/**
 * RumorDistortion — per-hop content morphing per PROJECT_MASTER.md 6.11.
 *
 * <p>Each time a rumor passes through a carrier (NPC), the content is
 * distorted. The distortion model from 6.11:
 * <pre>
 *   Hop 0 (source): "A spirit fruit ripened in Mosquito Valley"
 *   Hop 1: "Strange lights in Mosquito Valley."
 *   Hop 2: "A treasure was born in the eastern canyons."
 *   Hop 3: "An immortal inheritance has appeared somewhere near Zhao."
 *   Hop 4: "Wang Lin has returned." (totally fabricated but believed)
 * </pre>
 *
 * <h2>Distortion dimensions</h2>
 * <p>Each hop can distort the rumor along multiple axes independently:
 * <ul>
 *   <li><b>Content escalation</b> — "spirit fruit" becomes "treasure" becomes
 *       "immortal inheritance." The magnitude of the event increases.</li>
 *   <li><b>Location drift</b> — "Mosquito Valley" becomes "eastern canyons"
 *       becomes "somewhere near Zhao." The location becomes vaguer.</li>
 *   <li><b>Entity fabrication</b> — unrelated entities get inserted. "Wang Lin
 *       has returned" is entirely fabricated from a spirit fruit event.</li>
 *   <li><b>Detail loss</b> — specifics are replaced with vagueness. "Spirit
 *       fruit" becomes "strange lights."</li>
 *   <li><b>Confabulation</b> — new details that never happened are added.</li>
 * </ul>
 *
 * <h2>Actor-class distortion rates</h2>
 * <p>Per 6.11, different actor classes distort at different rates:
 * <ul>
 *   <li>MORTAL: high distortion (direct witnesses but poor understanding of
 *       cultivation phenomena — they describe what they see, not what it is)</li>
 *   <li>MERCHANT: moderate (trade-route awareness, but selfish filtering)</li>
 *   <li>SECT_SCOUT: low (trained observers, spiritual perception)</li>
 *   <li>SECT_ELDER: very low (filtered, analyzed — but may intentionally
 *       distort for political reasons)</li>
 *   <li>WANDERING_CULTIVATOR: high (hearsay, fragmentary divinations)</li>
 *   <li>PLAYER: receives whatever distorted version reaches them</li>
 * </ul>
 */
public final class RumorDistortion {

    private RumorDistortion() {}

    // ─── Distortion rate per actor class (0.0 = perfect relay, 1.0 = total fabrication) ──
    private static final double[] DISTORTION_RATE = {
        0.25,  // MORTAL — high distortion
        0.20,  // MERCHANT — moderate
        0.08,  // SECT_SCOUT — low (trained observer)
        0.05,  // SECT_ELDER — very low (but may politically filter)
        0.30,  // WANDERING_CULTIVATOR — high (hearsay)
        0.00   // PLAYER — terminal node, doesn't propagate further
    };

    // ─── Content escalation phrases (ordered: low → high magnitude) ──
    private static final String[][] ESCALATION_PATTERNS = {
        // Origin type: SPIRIT_EVENT
        {"strange lights", "spiritual fluctuation", "rare treasure", "immortal treasure",
         "ancient inheritance", "heavenly inheritance", "dao manifestation"},
        // Origin type: COMBAT_EVENT
        {"disturbance", "fight", "battle between cultivators", "sect-level conflict",
         "war between factions", "tribulation battle", "heavenly tribulation"},
        // Origin type: FACTION_EVENT
        {"movement", "patrol", "sect mobilization", "faction war preparation",
         "alliance formation", "declaration of war", "continental war"},
        // Origin type: PLAYER_ACTION
        {"someone", "a cultivator", "a talented disciple", "a rising genius",
         "a heaven-defying prodigy", "an ancient soul reincarnated", "Wang Lin returned"},
        // Origin type: ENVIRONMENTAL
        {"unusual weather", "spiritual anomaly", "spirit vein activation",
         "world law disturbance", "dimensional rift", "realm collapse", "heavenly tribulation sign"},
        // Origin type: WORLD_LAW
        {"strange feeling", "restriction fluctuation", "seal weakening",
         "forbidden zone cracking", "ancient array failing", "realm boundary dissolving",
         "world collapse imminent"},
        // Origin type: DISINFORMATION (escalates differently — stays political)
        {"whispers", "rumors", "conspiracy", "hidden plot",
         "sect betrayal", "demonic infiltration", "ancient devil awakening"},
        // Origin type: OTHER
        {"something", "something unusual", "something important",
         "something significant", "something world-shaking", "something heaven-defying",
         "a sign of the apocalypse"}
    };

    // ─── Location drift phrases (ordered: specific → vague) ──
    private static final String[] LOCATION_VAGUENESS = {
        "in %s",                    // hop 0: specific location
        "near %s",                  // hop 1: nearby
        "somewhere around %s",      // hop 2: general area
        "in the eastern region",    // hop 3: vague direction
        "somewhere in Zhao",        // hop 4: wrong region entirely
        "somewhere in the cultivation world", // hop 5: meaningless
        "in this world"             // hop 6: completely unhelpful
    };

    // ─── Confabulation fragments (fabricated details) ──
    private static final String[] CONFABULATIONS = {
        "under a blood moon",
        "accompanied by thunder",
        "with a phoenix cry",
        "during the third watch",
        "near an ancient tomb",
        "where an old man was seen meditating",
        "that caused all beasts to flee",
        "that shook the earth for miles"
    };

    // ═══════════════════════════════════════════════════════════════
    //  Public API
    // ═══════════════════════════════════════════════════════════════

    /**
     * Apply one hop of distortion to a rumor.
     *
     * @param rumor       the current rumor state (before this hop)
     * @param actor       the actor class carrying the rumor this hop
     * @param random      the random source (from level.getRandom())
     * @return a new Rumor with distorted content and reduced veracity
     */
    public static Rumor applyHop(Rumor rumor, Rumor.ActorClass actor, Random random) {
        int actorIndex = actor.ordinal();
        double distortionRate = DISTORTION_RATE[Math.min(actorIndex, DISTORTION_RATE.length - 1)];

        // Base veracity decay
        double newVeracity = rumor.getVeracity() * (1.0 - Rumor.BASE_VERACITY_DECAY - distortionRate * 0.5);
        newVeracity = Math.max(0.0, newVeracity);

        // Content escalation
        String newContent = escalateContent(rumor, distortionRate, random);

        // Location drift
        String newLocation = driftLocation(rumor.getLocationHint(), rumor.getHopCount() + 1);

        // Possible confabulation injection
        if (random.nextDouble() < distortionRate * 0.4 && rumor.getHopCount() >= 2) {
            newContent += " " + CONFABULATIONS[random.nextInt(CONFABULATIONS.length)];
        }

        // Sect elders may add political spin
        if (actor == Rumor.ActorClass.SECT_ELDER && random.nextDouble() < 0.3) {
            newContent = addPoliticalSpin(newContent, random);
        }

        long currentTick = rumor.getLastPropagatedTick(); // caller will set the real tick
        return rumor.withHop(newContent, newVeracity, actor, newLocation, currentTick);
    }

    /**
     * Get the propagation delay (in ticks) for an actor class.
     * Per 6.11 table: mortals=hours, merchants=days, scouts=hours-days,
     * elders=days-weeks, wanderers=days.
     *
     * <p>1 in-game hour = 1000 ticks (at 20 TPS, 1 tick = 3 seconds game time
     * is not right — actually 1 day = 24000 ticks, so 1 hour ≈ 1000 ticks).
     */
    public static long getPropagationDelayTicks(Rumor.ActorClass actor, Random random) {
        int baseHours;
        switch (actor) {
            case MORTAL -> baseHours = 2 + random.nextInt(6);        // 2-8 hours
            case MERCHANT -> baseHours = 12 + random.nextInt(36);    // 12-48 hours
            case SECT_SCOUT -> baseHours = 1 + random.nextInt(12);   // 1-13 hours
            case SECT_ELDER -> baseHours = 24 + random.nextInt(168); // 1-8 days
            case WANDERING_CULTIVATOR -> baseHours = 6 + random.nextInt(24); // 6-30 hours
            default -> baseHours = 12;
        }
        return baseHours * 1000L; // hours to ticks
    }

    /**
     * Get the number of hops an actor class typically propagates per tick cycle.
     * Scouts reach more people; elders reach fewer but more important ones.
     */
    public static int getSpreadCount(Rumor.ActorClass actor, Random random) {
        return switch (actor) {
            case MORTAL -> 1 + (random.nextDouble() < 0.3 ? 1 : 0);           // 1-2
            case MERCHANT -> 2 + (random.nextDouble() < 0.2 ? 1 : 0);        // 2-3 (trade routes)
            case SECT_SCOUT -> 1;                                               // 1 (reports to superior)
            case SECT_ELDER -> 1 + (random.nextDouble() < 0.1 ? 1 : 0);       // 1-2 (selective)
            case WANDERING_CULTIVATOR -> 1 + (random.nextDouble() < 0.4 ? 1 : 0); // 1-2 (inns)
            default -> 1;
        };
    }

    // ═══════════════════════════════════════════════════════════════
    //  Internal distortion logic
    // ═══════════════════════════════════════════════════════════════

    /**
     * Escalate the rumor content based on distortion rate and hop count.
     * Higher distortion rate = more escalation. Higher hop count = more escalation.
     */
    private static String escalateContent(Rumor rumor, double distortionRate, Random random) {
        int originIdx = Math.min(rumor.getOriginType().ordinal(), ESCALATION_PATTERNS.length - 1);
        String[] pattern = ESCALATION_PATTERNS[originIdx];

        // How far up the escalation ladder to jump (0 = same level, pattern.length-1 = max)
        double escalationChance = distortionRate * 0.6 + (rumor.getHopCount() * 0.1);
        escalationChance = Math.min(escalationChance, 0.95);

        int currentLevel = estimateCurrentLevel(rumor.getCurrentContent(), pattern);
        int newLevel = currentLevel;

        if (random.nextDouble() < escalationChance) {
            // Escalate by 1-2 levels
            int jump = 1 + (random.nextDouble() < 0.3 ? 1 : 0);
            newLevel = Math.min(currentLevel + jump, pattern.length - 1);
        }

        // Small chance of de-escalation (rumor gets "corrected" partially)
        if (random.nextDouble() < 0.05 && currentLevel > 1) {
            newLevel = currentLevel - 1;
        }

        // Pick the phrase at the new level, then weave it into a sentence
        String escalatedPhrase = pattern[newLevel];
        return weaveSentence(escalatedPhrase, rumor.getLocationHint(), rumor.getOriginType(), random);
    }

    /**
     * Estimate which escalation level the current content is at.
     * Simple heuristic: find the highest-level pattern phrase that appears in the content.
     */
    private static int estimateCurrentLevel(String content, String[] pattern) {
        int level = 0;
        for (int i = 0; i < pattern.length; i++) {
            if (content.toLowerCase().contains(pattern[i].toLowerCase())) {
                level = i;
            }
        }
        return level;
    }

    /**
     * Weave an escalated phrase into a natural-sounding sentence.
     */
    private static String weaveSentence(String phrase, String location,
                                         Rumor.OriginType origin, Random random) {
        String locationStr = (location != null && !location.isEmpty())
                ? " " + String.format(LOCATION_VAGUENESS[0], location)
                : "";

        String[] templates = switch (origin) {
            case SPIRIT_EVENT -> new String[]{
                "There are reports of " + phrase + locationStr + ".",
                "I heard " + phrase + " was seen" + locationStr + ".",
                "People say " + phrase + " appeared" + locationStr + ".",
                "Someone mentioned " + phrase + locationStr + "."
            };
            case COMBAT_EVENT -> new String[]{
                "Word is " + phrase + " occurred" + locationStr + ".",
                "I heard there was " + phrase + locationStr + ".",
                "Rumors of " + phrase + " are spreading" + locationStr + "."
            };
            case FACTION_EVENT -> new String[]{
                "I heard " + phrase + " is happening" + locationStr + ".",
                "There are whispers of " + phrase + locationStr + ".",
                "Someone said " + phrase + " was noticed" + locationStr + "."
            };
            case PLAYER_ACTION -> new String[]{
                "They say " + phrase + " was spotted" + locationStr + ".",
                "I heard " + phrase + " passed through" + locationStr + ".",
                "People are talking about " + phrase + locationStr + "."
            };
            case ENVIRONMENTAL -> new String[]{
                "Strange — " + phrase + locationStr + ".",
                "I heard about " + phrase + locationStr + ".",
                "There are reports of " + phrase + locationStr + "."
            };
            case WORLD_LAW -> new String[]{
                "Something is wrong — " + phrase + locationStr + ".",
                "I heard " + phrase + locationStr + ".",
                "Elders are concerned about " + phrase + locationStr + "."
            };
            case DISINFORMATION -> new String[]{
                "Between you and me, " + phrase + locationStr + ".",
                "I shouldn't say, but " + phrase + locationStr + ".",
                "People are saying " + phrase + locationStr + "."
            };
            default -> new String[]{
                "I heard " + phrase + locationStr + ".",
                "Something about " + phrase + locationStr + "."
            };
        };

        return templates[random.nextInt(templates.length)];
    }

    /**
     * Drift the location hint toward vagueness as hops increase.
     */
    private static String driftLocation(String currentHint, int hopCount) {
        if (currentHint == null || currentHint.isEmpty()) return "unknown location";
        int idx = Math.min(hopCount, LOCATION_VAGUENESS.length - 1);
        // For hop 0, keep the specific location. For higher hops, apply vagueness templates.
        // But the template uses %s — so we extract the base location from the current hint.
        String baseLocation = extractBaseLocation(currentHint);
        return String.format(LOCATION_VAGUENESS[idx], baseLocation);
    }

    /**
     * Extract the base location name from a location hint that may already
     * have vagueness prefixes like "near X" or "somewhere around X".
     */
    private static String extractBaseLocation(String hint) {
        // Strip common prefixes
        String[] prefixes = {"somewhere around ", "somewhere in ", "somewhere near ",
                "near ", "in the ", "around ", "in "};
        String lower = hint.toLowerCase();
        for (String prefix : prefixes) {
            if (lower.startsWith(prefix)) {
                return hint.substring(prefix.length());
            }
        }
        return hint;
    }

    /**
     * Add political spin — sect elders reframe rumors to serve their interests.
     */
    private static String addPoliticalSpin(String content, Random random) {
        String[] spins = {
            " Our sect should investigate this.",
            " This could be an opportunity for us.",
            " If this is true, we must act before the other sects.",
            " This concerns our territory — we cannot ignore it.",
            " The elders should discuss this at once.",
            " This may be a test from the heavens."
        };
        return content + spins[random.nextInt(spins.length)];
    }
}