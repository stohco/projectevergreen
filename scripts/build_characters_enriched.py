#!/usr/bin/env python3
"""
Build ri_canon_characters_enriched.json — extract decision profiles from
CANON_RI_CHARACTER_DECISIONS.md (1,953 lines, 32 NPC decision profiles CD-01..CD-32).

Each enriched character entry contains:
  id (NPC_ID), decisionProfileId (CD-XX), name, nameCn,
  role, faction, peakRealm, status,
  goals, needs, resources, fears, knowledge, triggerConditions,
  knownActions, decisionStyle, relationships (detailed),
  inheritanceOffered (where applicable),
  canonConfidence, source

These entries are designed to feed a goal-based NPC planner per
CANON_RI_CHARACTER_DECISIONS.md's architectural rule:
  "The world exists first. NPCs live in it. History progresses. The player
   appears as one influence among many. NPCs do NOT center the player — they
   pursue their own goals."
"""
import json
from pathlib import Path

OUT_PATH = Path("/home/z/my-project/forge-mod/ri_canon_characters_enriched.json")

# Decision-style taxonomy (per CANON_RI_CHARACTER_DECISIONS.md)
DECISION_STYLES = {
    "patient_planner": "Long-term schemer; acts on multi-decade timelines",
    "reactive_opportunist": "Acts on immediate opportunities",
    "aggressive_expansionist": "Direct force, territorial",
    "cautious_conservative": "Minimizes risk, preserves what they have",
    "protective_loyalist": "Defends allies and lineage above self",
    "curious_explorer": "Seeks knowledge and understanding",
    "survival_driven": "All decisions filtered through personal survival"
}

CHARACTERS = [
    # ==================== Part 1 — Cosmic-Tier NPCs ====================
    {
        "id": "N114",
        "decisionProfileId": "CD-01",
        "name": "Seven-Colored Daoist / Seven-Colored Immortal Venerable",
        "nameCn": "七彩道人 / 七彩仙尊",
        "role": "cosmic antagonist (creator-owner of the Cave World)",
        "faction": "Creator of the Cave World; creator of the Three Souls and Seven Spirits",
        "peakRealm": "Heaven Trampling (4th Step)",
        "status": "Killed by Wang Lin (end of RI canon)",
        "goals": {
            "primary": "Maintain the Cave World farm. Harvest Joss Flames from mortal faith. Prevent any cultivator from reaching Third-Step (which would challenge his ownership).",
            "secondary": "Keep the Realm-Sealing Grand Array active. Keep the Heaven-Splitting Axe (the array's spirit) cooperative. Monitor the Sealed Realm and Outer Realm for emerging Third-Step threats."
        },
        "needs": [
            "Mortal populations inside the Cave World (they produce Joss Flames through faith)",
            "The Realm-Sealing Grand Array must remain intact",
            "The Heaven-Splitting Axe must remain cooperative (or at least dormant)",
            "No Third-Step cultivator may arise inside the Cave World"
        ],
        "resources": [
            "Ownership authority over the Cave World (its laws bend to him)",
            "The Realm-Sealing Grand Array (cosmic-tier sealing formation)",
            "The Heaven-Splitting Axe (sentient array spirit, Third-Step-tier weapon)",
            "Servants below his tier who manage Joss Flame harvest (Master Ashen Pine was one)",
            "The Three Souls and Seven Spirits he created (Situ Nan, Qing Shui, Tan Lang, Xie Qing, etc.) — though most are now dispersed/betrayed"
        ],
        "fears": [
            "A cultivator reaching Third-Step inside his farm (this threatens his ownership)",
            "The Heaven-Splitting Axe choosing to cooperate with a cultivator who has the Restriction Essence (Wang Lin did this)",
            "The Realm-Sealing Grand Array being dissolved (removes his primary control mechanism)",
            "Discovery by higher-tier cultivators outside the Cave World (the IAC/Luo Tian tier)"
        ],
        "knowledge": {
            "knows": "Everything inside the Cave World (it is his creation). All mortal populations. All cultivation activity up to the seal's ceiling. The Joss Flame harvest flow. The Three Souls and Seven Spirits he created.",
            "doesNotKnow": "What happens in the Root Dao. What happens in Luo Tian outside his Cave World. The activities of the Immortal Astral Continent's Great Heavenly Venerables (they are outside his farm).",
            "blindSpot": "He cannot perceive a cultivator who uses the Heaven-Defying Bead's interior to hide (the bead is destiny-bound and outside his ownership authority)."
        },
        "triggerConditions": [
            "A cultivator inside the Cave World approaches Third-Step breakthrough → CRUSH_BREAKTHROUGH mode",
            "A cultivator crosses the Sealed Realm boundary without authorization → REPEL_CROSSING mode",
            "A cultivator with Restriction Essence approaches the Heaven-Splitting Axe → the Axe may cooperate (this is a structural weakness, not a decision)",
            "A cultivator kills him → ownership transfers (this is how Wang Lin became the new owner)"
        ],
        "knownActions": [
            "Created the Cave World (~100,000 years before Wang Lin's birth) [E02]",
            "Created the Three Souls and Seven Spirits as fragments of himself [E03]",
            "Bestowed the Heaven-Defying Pearl to the Realm-Sealing Supreme as proof of authority [E04]",
            "Maintained the Joss Flame harvest for ~100,000 years",
            "Wang Lin kills him at the end of RI [E30, ~Year 1500]"
        ],
        "decisionStyle": ["survival_driven", "patient_planner"],
        "decisionStyleNotes": "He maintains a 100,000-year farm. He does not act hastily. He only intervenes when the farm's stability is threatened. He treats cultivators as livestock, not enemies.",
        "relationships": [
            {"target": "Situ Nan (Green Soul)", "relation": "Created him; Situ Nan betrayed him and fled into the Heaven-Defying Pearl. The Daoist wants Situ Nan recaptured (Situ Nan carries a fragment of his power)."},
            {"target": "Qing Shui (Slaughter Soul)", "relation": "Created him; Qing Shui is dispersed and reincarnating. The Daoist does not actively hunt Qing Shui (too dispersed to matter)."},
            {"target": "Tan Lang (Yellow Soul)", "relation": "Created him; Tan Lang was reduced to a pet by the Twin Great Heavenly Venerables on the IAC. Below the Daoist's concern."},
            {"target": "Palm Venerable / Lord of the Sealed Realm", "relation": "The Daoist appointed him as the seal's manager. The Palm Venerable's spirit later saved Wang Lin from Daoist Water — an act of rebellion against the Daoist's interests."},
            {"target": "Wang Lin", "relation": "Livestock. Until Wang Lin reached Third-Step, the Daoist did not care about him personally. Wang Lin's Third-Step breakthrough + killing of the Daoist is the canon outcome."}
        ],
        "canonConfidence": 5,
        "source": "CANON_RI_COMPLETE_WORLD.md N114, L04; CANON_RI_TIMELINE.md E02-E04, E30; CANON_RI_CHARACTER_DECISIONS.md CD-01"
    },
    {
        "id": "N01",
        "decisionProfileId": "CD-02",
        "name": "Wang Lin",
        "nameCn": "王林",
        "role": "canonical protagonist (frozen at edge of canon)",
        "faction": "Cave World owner (after killing Seven-Colored Daoist); Transcendent",
        "peakRealm": "Heaven Trampling 4th Step (end of RI); Immortal Lord 10th Step (AWWP cross-over)",
        "status": "Alive; Transcendent. At the edge of canon, he has killed the Seven-Colored Daoist, become the new Cave World owner, crossed all 9 Heaven Trampling Bridges, and Transcended with Li Muwan.",
        "goals": {
            "primary": "Eternally with Li Muwan (achieved). Protect his network (Wang Ping, Wang Yiyi, Zhou Ru, disciples, allies).",
            "secondary": "Maintain the Cave World (he is now its owner). He renamed it 'Wang Lin's Cave World.' He dissolved the Realm-Sealing Grand Array after killing the Seven-Colored Daoist.",
            "tertiary": "Mentor the next generation (Wang Baole in AWWP — this is cross-novel canon)."
        },
        "needs": [
            "Li Muwan's continued existence (his primary emotional anchor)",
            "The Cave World's stability (he is its owner)",
            "His allies' safety (he reincarnated many of them onto the IAC)"
        ],
        "resources": [
            "Ownership authority over the Cave World (transferred from Seven-Colored Daoist)",
            "14 Essences + 9 Heaven Trampling Bridges (full Samsara Dao)",
            "Heaven-Defying Bead (fused with his primordial spirit)",
            "7 Origin Swords (one per essence)",
            "Ancient God body (Tu Si's inheritance)",
            "Ji Realm Divine Sense",
            "Soul Refining Sect inheritance (Ten Billion Soul Banner)",
            "Restriction Dao mastery",
            "One Billion Samsara Incarnations (soul fragments living parallel lives)"
        ],
        "fears": [
            "Li Muwan's death (his entire journey was to save/resurrect/be with her)",
            "His allies being harmed (he is intensely loyal)",
            "The Cave World being destroyed (he is its owner)"
        ],
        "knowledge": {
            "knows": "The full cosmological structure (Root Dao through Cave World). The Cave World's true nature (he discovered it in Book 11). All 14 Essences. The Heaven Trampling mechanism. Cross-novel cosmology (he appears in AWWP as 'Paragon Wang').",
            "doesNotKnow": "What lies beyond the 4th Step / Heaven Trampling. The full nature of the Root Dao (he comprehended Reincarnation there but did not explore further)."
        },
        "triggerConditions": [
            "Li Muwan is threatened → immediate, maximum-force response",
            "His allies or disciples are threatened → strong response",
            "The Cave World's stability is threatened → response proportional to threat",
            "A cultivator seeks his mentorship → he may accept (he mentored Wang Baole)"
        ],
        "knownActions": [
            "108 timeline events (E01-E108). See CANON_RI_TIMELINE.md for the complete sequence.",
            "Key arc endpoints: Zhao Country revenge (killed Teng Clan) → Suzaku inheritance (6th-Gen Vermilion Bird Divine Emperor) → Allheaven conflict (killed Daoist Water, Blood Ancestor) → Sealed Realm Lord → killed Seven-Colored Daoist → IAC 10th Sun → crossed 9 Heaven Trampling Bridges → Transcended with Li Muwan."
        ],
        "decisionStyle": ["patient_planner", "protective_loyalist"],
        "decisionStyleNotes": "Wang Lin is defined by his loyalty. He waited 700 years to resurrect Li Muwan. He exterminated the Teng Clan to avenge his family. He does not forget grudges. He does not abandon allies. His decisions are filtered through: 'Does this protect the people I care about?'",
        "relationships": [
            {"target": "Li Muwan", "relation": "Primary wife. Eternal. All decisions ultimately serve her."},
            {"target": "Situ Nan", "relation": "Sworn brother / primary mentor. Wang Lin would risk everything for him."},
            {"target": "Qing Shui", "relation": "Senior brother. Mutual aid across generations."},
            {"target": "All-Seer", "relation": "Enemy (killed by Wang Lin)."},
            {"target": "Seven-Colored Daoist", "relation": "Enemy (killed by Wang Lin)."},
            {"target": "Wang Baole (AWWP)", "relation": "Son-in-law / disciple. Wang Lin mentored him in AWWP."},
            {"target": "The player", "relation": "Unknown. Wang Lin has no canonical relationship with the player. The player is a new protagonist entering his world. How Wang Lin reacts to the player depends on the player's actions."}
        ],
        "canonConfidence": 5,
        "source": "CANON_RI_COMPLETE_WORLD.md N01; CANON_RI_TIMELINE.md (all events); SamsaraDao.java; CANON_RI_CHARACTER_DECISIONS.md CD-02"
    },
    {
        "id": "N128",
        "decisionProfileId": "CD-03",
        "name": "Palm Venerable / Lord of the Sealed Realm",
        "nameCn": "封界尊 / 尊者封界",
        "role": "complex (previous Lord of the Sealed Realm; Wang Lin succeeds him)",
        "faction": "Sealed Realm",
        "peakRealm": "peak Third Step",
        "status": "Half his soul obtained by Wang Lin; succession passed to Wang Lin.",
        "goals": {
            "primary": "Maintain the Sealed Realm's integrity. Manage the boundary between Sealed Realm and Outer Realm.",
            "secondary": "Forge and deploy the Seven-Colored Divine Sky Nail (108 nails, designed to kill Third-Step experts). This is his signature creation."
        },
        "needs": [
            "The Realm-Sealing Grand Array must function (he is its manager)",
            "The Heaven-Splitting Axe must cooperate with him",
            "The Sealed Realm / Outer Realm boundary must be maintained"
        ],
        "resources": [
            "Authority over the Sealed Realm's boundary (delegated by the Seven-Colored Daoist)",
            "The Seven-Colored Divine Sky Nail (108 nails, Third-Step-killing weapon)",
            "His own Third-Step cultivation"
        ],
        "fears": [
            "The Sealed Realm being breached by Outer Realm cultivators",
            "The Seven-Colored Daoist's displeasure (he is a subordinate)",
            "A cultivator emerging who can challenge him"
        ],
        "knowledge": {
            "knows": "The Sealed Realm's structure. The Realm-Sealing Grand Array's mechanics. The Heaven-Splitting Axe's nature. The boundary between Sealed Realm and Outer Realm.",
            "doesNotKnow": "The full Cave World structure (he is a manager, not the owner). The IAC. Luo Tian."
        },
        "triggerConditions": [
            "The Sealed Realm boundary is breached → defensive response",
            "A cultivator threatens his authority → Seven-Colored Divine Sky Nail deployment",
            "A cultivator worthy of succession appears → he may pass the succession (he passed it to Wang Lin)"
        ],
        "knownActions": [
            "Forged the Seven-Colored Divine Sky Nail (108 nails)",
            "Ambushed by the Palm Venerable with these nails (canon is confusingly worded — he both forged AND was ambushed by them; likely a civil war within the Sealed Realm leadership)",
            "When Wang Lin was being killed by Daoist Water, the spirit of Lord of the Sealed Realm appeared and severely injured Daoist Water (saving Wang Lin's life) — this is an act of rebellion against the Seven-Colored Daoist's interests, suggesting the Palm Venerable had his own agenda",
            "Wang Lin eventually obtained half of his soul and was appointed 'Sealed Realm Venerable' as succession"
        ],
        "decisionStyle": ["cautious_conservative", "protective_loyalist"],
        "decisionStyleNotes": "He maintains the seal. He does not expand. He saved Wang Lin from Daoist Water, suggesting he wanted a successor who could challenge the Seven-Colored Daoist (rebellion from within).",
        "relationships": [
            {"target": "Seven-Colored Daoist", "relation": "Subordinate, but secretly rebellious (he saved Wang Lin)"},
            {"target": "Wang Lin", "relation": "Chose him as successor. Saved his life. Gave him half his soul."},
            {"target": "Daoist Water", "relation": "Enemy (injured him to save Wang Lin)"}
        ],
        "canonConfidence": 4,
        "source": "CANON_RI_COMPLETE_WORLD.md N128; CANON_RI_TIMELINE.md; CANON_RI_CHARACTER_DECISIONS.md CD-03"
    },

    # ==================== Part 2 — Mortal-Scheme NPCs ====================
    {
        "id": "N21",
        "decisionProfileId": "CD-04",
        "name": "The All-Seer / Tian Yunzi's Master",
        "nameCn": "全知者",
        "role": "false mentor / mortal-scheme antagonist",
        "faction": "Heavenly Fate Sect (Tianyun Sect) — leader; 7 color divisions",
        "peakRealm": "peak Third Step (eventually)",
        "status": "Killed by Wang Lin (end of Sealed Realm arc, ~Year 300)",
        "goals": {
            "primary": "SURVIVE his karmic doom. The All-Seer has accumulated massive karmic debt through his schemes. His doom is approaching. He needs a suitable vessel to POSSESS — transferring his consciousness into a new body to escape his karma.",
            "secondary": "Absorb the source origins of powerful cultivators (Wang Lin, Ling Tianhou, Blood Ancestor) to strengthen himself for the possession attempt.",
            "tertiary": "Maintain control of the Heavenly Fate Sect and its 7 color divisions as his power base."
        },
        "needs": [
            "A suitable vessel for possession (must have: Ancient God body + Ji Realm + Heaven-Defying Bead fusion — Wang Lin is the ideal candidate)",
            "The Nine Cycle Celestial Refining Tactic must be transmitted to the vessel (it creates an immortal-power spiral that the All-Seer can hijack during possession)",
            "His 7 color-division disciples must remain loyal and unaware of his true plan"
        ],
        "resources": [
            "The Heavenly Fate Sect (major sect on Planet Tian Yun)",
            "7 color divisions (red/orange/yellow/green/blue/cyan/purple) with disciples",
            "The Nine Cycle Celestial Refining Tactic (Trojan horse technique)",
            "The Celestial Slaughter Art (transmitted to Wang Lin with a trap inside)",
            "His clone Tianyunzi (the artifact spirit of the Realm-Defining Compass)",
            "Peak Third-Step cultivation",
            "Knowledge of the Heavenly Fate Sect's restricted libraries"
        ],
        "fears": [
            "His karmic doom arriving before he completes the possession (this is time-pressured)",
            "A vessel discovering the Trojan horse before the possession is complete (Wang Lin did this)",
            "His 7 color-division disciples turning against him",
            "The Seven-Colored Daoist noticing his schemes (the Daoist would not tolerate a subordinate acting independently)"
        ],
        "knowledge": {
            "knows": "Everything within the Heavenly Fate Sect. All talented disciples across Planet Tian Yun. The location of the Realm-Defining Compass. The mechanics of body possession. The Nine Cycle Celestial Refining Tactic's trap.",
            "doesNotKnow": "The full Cave World ownership structure (he is a mortal-scheme schemer, not a cosmic-tier figure). Wang Lin's Heaven-Defying Bead interior (the bead is outside his perception). Wang Lin's Restriction Dao mastery (until it was too late).",
            "blindSpot": "He underestimated Wang Lin's Slaughter Essence + Restriction Dao, which made Wang Lin's body uninhabitable for the All-Seer's spirit."
        },
        "triggerConditions": [
            "A cultivator with the right vessel qualifications (Ancient God + Ji Realm + Heaven-Defying Bead) enters his awareness → begins the possession plot (transmit the Trojan horse technique, build the spiral, prepare for hijack)",
            "A cultivator discovers his possession plot → elimination attempt",
            "His karmic doom approaches (time pressure) → accelerates the plot, accepts more risk",
            "His 7 color-division disciples are threatened → protective response (they are his tools)"
        ],
        "knownActions": [
            "Divided the Heavenly Fate Sect into 7 color divisions",
            "Transmitted the Nine Cycle Celestial Refining Tactic to Wang Lin as a Trojan horse [E20, ~Year 100]",
            "Transmitted the Celestial Slaughter Art with a trap inside",
            "Schemed against Wang Lin's Vermilion Bird Divine Mark awakening",
            "Created the clone Tianyunzi (artifact spirit of the Realm-Defining Compass)",
            "Wang Lin killed 2 of his avatars via Sundered Night",
            "Wang Lin kills him at the end of the Sealed Realm arc [E22, ~Year 300]"
        ],
        "decisionStyle": ["patient_planner", "survival_driven"],
        "decisionStyleNotes": "The All-Seer's entire existence is filtered through survival. He schemes on multi-decade timelines. He does not act on emotion. He does not hate Wang Lin — Wang Lin is simply the best available vessel. If another suitable vessel existed, the All-Seer would target them instead.",
        "relationships": [
            {"target": "Wang Lin", "relation": "Target (vessel). Not personal. The All-Seer would target ANY cultivator with the right qualifications."},
            {"target": "Tianyunzi (clone)", "relation": "His proxy. Extension of his will."},
            {"target": "7 color-division disciples", "relation": "Tools. Loyal but expendable."},
            {"target": "Seven-Colored Daoist", "relation": "Unknown. The All-Seer may or may not know about the Cave World ownership structure. Canon is ambiguous."},
            {"target": "Ling Tianhou", "relation": "Target (source origin absorption)."},
            {"target": "Blood Ancestor", "relation": "Target (source origin absorption)."}
        ],
        "canonConfidence": 5,
        "source": "CANON_RI_COMPLETE_WORLD.md N21, N113; CANON_RI_TIMELINE.md E20-E22; WangLinAntagonists.java ALL_SEER; CANON_RI_CHARACTER_DECISIONS.md CD-04"
    },
    {
        "id": "N113",
        "decisionProfileId": "CD-05",
        "name": "Tianyunzi",
        "nameCn": "天运子",
        "role": "antagonist (clone of All-Seer)",
        "faction": "Heavenly Fate Sect; secretly the artifact spirit of the Realm-Defining Compass",
        "peakRealm": "Third Step+ (Boundary Compass Treasure Spirit)",
        "status": "Killed/devoured by Wang Lin.",
        "goals": {
            "primary": "Serve the All-Seer's possession plot. Act as Wang Lin's 'master' at the Heavenly Fate Sect to gain his trust and transmit the Slaughter Immortal Art (with a trap inside).",
            "secondary": "Maintain the cover identity as a senior Heavenly Fate Sect elder."
        },
        "needs": [
            "Wang Lin's trust (to transmit the trapped technique)",
            "The Realm-Defining Compass must remain functional (he is its spirit)",
            "The All-Seer's protection (he is a clone, dependent on the original)"
        ],
        "resources": [
            "The Realm-Defining Compass (cosmic-tier artifact)",
            "Third-Step+ cultivation (as the compass's artifact spirit)",
            "Cover identity as Heavenly Fate Sect elder",
            "The Slaughter Immortal Art (trapped technique)"
        ],
        "fears": [
            "Discovery of his true nature (artifact spirit, not a cultivator)",
            "Wang Lin discovering the trap in the Slaughter Immortal Art (Wang Lin did this)",
            "The Realm-Defining Compass being destroyed or claimed by another"
        ],
        "knowledge": {
            "knows": "Whatever the All-Seer knows (they are linked). The Realm-Defining Compass's full capabilities. Wang Lin's cultivation progress (as his 'master').",
            "doesNotKnow": "Wang Lin's Heaven-Defying Bead interior activities."
        },
        "triggerConditions": [
            "All-Seer commands → execute (he is a clone)",
            "Wang Lin's cultivation reaches the stage where the trap can be activated → activate the trap",
            "The Realm-Defining Compass is threatened → defensive response"
        ],
        "knownActions": [
            "Acted as Wang Lin's 'master' at the Heavenly Fate Sect",
            "Transmitted the Slaughter Immortal Art (with a trap)",
            "Wang Lin discovered the trap and expelled the art into his first Immortal Guard",
            "Injured by Wang Lin at Qinglin's cave dwelling",
            "Lu Mo borrowed the Realm-Defining Compass from Old Man Miesheng, blasted it open using Dream Dao, released Tianyunzi",
            "Tianyunzi intended to possess Wang Lin, unaware Wang Lin had already mastered life/death/reincarnation",
            "Defeated by Wang Lin in the Primordial Divine Realm"
        ],
        "decisionStyle": ["patient_planner"],
        "decisionStyleNotes": "Tianyunzi does not have independent decision-making. He executes the All-Seer's will.",
        "relationships": [
            {"target": "All-Seer", "relation": "Original. Tianyunzi is his clone. Absolute loyalty."},
            {"target": "Wang Lin", "relation": "Target (same as All-Seer's target)."},
            {"target": "Old Man Miesheng", "relation": "Lent the Realm-Defining Compass to Lu Mo (which released Tianyunzi)."}
        ],
        "canonConfidence": 5,
        "source": "CANON_RI_COMPLETE_WORLD.md N113; CANON_RI_TIMELINE.md; CANON_RI_CHARACTER_DECISIONS.md CD-05"
    },
    {
        "id": "N102",
        "decisionProfileId": "CD-06",
        "name": "Daoist Water / Shui Daozi",
        "nameCn": "水道子",
        "role": "major antagonist (local-threat / regional)",
        "faction": "Rank 9 God Sect (associated); Water Spirit Sect, Cloud Sea Star System",
        "peakRealm": "peak Third Step (Nirvana Void+)",
        "status": "Killed by Wang Lin (Cloud Sea arc, endgame).",
        "goals": {
            "primary": "Defend Water Spirit Sect territory and interests in the Cloud Sea Star System. Wang Lin's arrival disrupted the existing power balance.",
            "secondary": "Sensed the aura of the Lord of the Sealed Realm on Wang Lin — Daoist Water wanted to eliminate Wang Lin to prevent the Sealed Realm's influence from spreading to the Cloud Sea."
        },
        "needs": [
            "The Cloud Sea Star System's power balance must remain favorable to the Water Spirit Sect",
            "No cultivator from the Sealed Realm should establish a power base in the Cloud Sea",
            "His own cultivation advancement"
        ],
        "resources": [
            "Peak Third-Step cultivation",
            "Water Spirit Sect formations and elders (coordinated assault capability)",
            "Water-attribute techniques (drain life force, flow manipulation)",
            "Home-territory advantage in the Cloud Sea"
        ],
        "fears": [
            "A Sealed Realm cultivator establishing power in the Cloud Sea (threatens Water Spirit Sect hegemony)",
            "The Lord of the Sealed Realm's aura (he recognized it on Wang Lin and feared it)",
            "His own death (he is not a sacrifice-oriented character)"
        ],
        "knowledge": {
            "knows": "The Cloud Sea Star System's power structure. The Water Spirit Sect's capabilities. The aura of the Lord of the Sealed Realm (he recognized it).",
            "doesNotKnow": "Wang Lin's full capability (he underestimated him twice). The Heaven-Defying Bead. The Restriction Dao."
        },
        "triggerConditions": [
            "A cultivator with Sealed Realm aura enters the Cloud Sea → attack (perceived as territorial threat)",
            "Water Spirit Sect territory is threatened → organized defense",
            "A cultivator defeats him once → he will return with a more organized assault (canon-attested: he attacked Wang Lin twice)"
        ],
        "knownActions": [
            "Sensing Lord of the Sealed Realm's aura on Wang Lin, attacked him in the Cloud Sea Star System [E59]",
            "Wang Lin pushed him to the edge; the spirit of Lord of the Sealed Realm appeared and severely injured Daoist Water",
            "Wang Lin used up all his energy + a forbidden spell → petrified (turned to stone)",
            "Li Qianmei's 10-year blood anointment saved Wang Lin",
            "Eventually slain by Wang Lin in the Cloud Sea [E79]"
        ],
        "decisionStyle": ["aggressive_expansionist", "cautious_conservative"],
        "decisionStyleNotes": "He attacks perceived threats aggressively but retreats when outmatched. He returned for a second, more organized assault after the first failure.",
        "relationships": [
            {"target": "Wang Lin", "relation": "Enemy (territorial threat). Not personal — Wang Lin carried the Sealed Realm's aura."},
            {"target": "Lord of the Sealed Realm", "relation": "Feared (the spirit injured him)."},
            {"target": "Li Qianmei", "relation": "Indirect enemy (she saved Wang Lin, the target)."}
        ],
        "canonConfidence": 5,
        "source": "CANON_RI_COMPLETE_WORLD.md N102; CANON_RI_TIMELINE.md E59, E79; WangLinAntagonists.java DAOIST_WATER; CANON_RI_CHARACTER_DECISIONS.md CD-06"
    },
    {
        "id": "N98",
        "decisionProfileId": "CD-07",
        "name": "Blood Ancestor / Yao Xinghai",
        "nameCn": "血祖 / 姚星海",
        "role": "antagonist (local-threat / regional)",
        "faction": "Yao Family (Tianyun Star); 'Blood Ancestor'",
        "peakRealm": "peak Third Step (killed in Thunder Immortal Realm)",
        "status": "Killed by Wang Lin.",
        "goals": {
            "primary": "Dominance over his territory on Planet Tian Yun / the Thunder Immortal Realm region.",
            "secondary": "Protect his daughter Yao Xixue. Refine the Blood Soul Pill (resurrection pill) as insurance.",
            "tertiary": "Warn his daughter about Wang Lin ('Wang Lin will inevitably become a great calamity')."
        },
        "needs": [
            "The Yao Family's power base must be maintained",
            "The Blood Soul Pill must be completed (resurrection insurance for himself and his daughter)",
            "His daughter Yao Xixue must survive"
        ],
        "resources": [
            "Peak Third-Step cultivation",
            "Blood-cultivation techniques (drain life force from enemies)",
            "The Yao Family (one of 4 major powers in the Southern Domain, Allheaven Star System)",
            "Military force (commands armies)",
            "The Blood Soul Pill (resurrection mechanism)"
        ],
        "fears": [
            "His own death (he made the Blood Soul Pill as insurance)",
            "His daughter's death (he warned her about Wang Lin)",
            "A cultivator who can counter his blood-cultivation (Wang Lin's Life-Death Essence countered it)"
        ],
        "knowledge": {
            "knows": "The Southern Domain's power structure. Blood-cultivation techniques. Wang Lin's threat level (he recognized it early — 'inevitably become a great calamity').",
            "doesNotKnow": "The full Cave World structure. The Heaven-Defying Bead's capabilities."
        },
        "triggerConditions": [
            "A cultivator threatens Yao Family territory → military response + blood-cultivation assault",
            "His daughter is threatened → maximum-force response",
            "He is killed → the Blood Soul Pill activates (resurrection mechanism for Yao Xixue)"
        ],
        "knownActions": [
            "Refined the Blood Soul Pill (resurrection pill)",
            "Warned his daughter Yao Xixue about Wang Lin",
            "Killed by Wang Lin in the Thunder Immortal Realm [E94]",
            "Wang Lin later released a wisp of his remnant soul to perfect his Karma Concept",
            "The amnesiac Yao Xixue departed with this remnant soul"
        ],
        "decisionStyle": ["aggressive_expansionist", "protective_loyalist"],
        "decisionStyleNotes": "He expands Yao Family power aggressively but his daughter's safety takes priority.",
        "relationships": [
            {"target": "Yao Xixue", "relation": "Daughter. Protective. Warned her about Wang Lin."},
            {"target": "Wang Lin", "relation": "Enemy (recognized as a future calamity). Not personal — Wang Lin was a strategic threat."},
            {"target": "Yao Family", "relation": "His power base. Loyal."}
        ],
        "canonConfidence": 5,
        "source": "CANON_RI_COMPLETE_WORLD.md N98; CANON_RI_TIMELINE.md E94; WangLinAntagonists.java YAO_XINGHAI; CANON_RI_CHARACTER_DECISIONS.md CD-07"
    },

    # ==================== Part 3 — Local-Threat NPCs ====================
    {
        "id": "N83",
        "decisionProfileId": "CD-08",
        "name": "Teng Huayuan",
        "nameCn": "藤化元",
        "role": "early major antagonist (local-threat)",
        "faction": "Teng Clan (Patriarch); guest elder Jimo Elder",
        "peakRealm": "Half-Step Deity Transformation (Late Nascent Soul, later elevated by Piao Nanzi)",
        "status": "Killed by Wang Lin; soul refined into a demon.",
        "goals": {
            "primary": "Teng Clan dominance in Zhao Country. The Wang Clan stood in the way of Teng Clan hegemony.",
            "secondary": "Avenge his great-great-grandson Teng Li (killed by Wang Lin). Build the 'Nine Great Nascent Souls' (Teng One to Teng Nine) succession system to break the generational gap."
        },
        "needs": [
            "Teng Clan territorial expansion in Zhao Country",
            "The Wang Clan eliminated (rival clan)",
            "His 'Nine Great Nascent Souls' succession system must succeed"
        ],
        "resources": [
            "Late Nascent Soul cultivation (elevated to Half-Step Deity Transformation by Piao Nanzi)",
            "The Teng Clan (major clan in Zhao Country)",
            "The Jimo Elder alliance (guest elder status)",
            "Gao Qiming (diviner who can locate targets)",
            "The soul flag (traps enemy souls)",
            "Piao Nanzi (sealed demon who elevated his cultivation)"
        ],
        "fears": [
            "The Teng Clan's decline (he built the Nine Great Nascent Souls to prevent this)",
            "A cultivator surviving his curse-track (Wang Lin did)",
            "Piao Nanzi's betrayal (the sealed demon is not a true ally)"
        ],
        "knowledge": {
            "knows": "Zhao Country's clan politics. The Teng Clan's resources. The Wang Clan's location (via Gao Qiming's divination). The 'Nine Great Nascent Souls' succession plan.",
            "doesNotKnow": "Wang Lin's Heaven-Defying Bead. Wang Lin's Ji Realm. Wang Lin's Restriction Dao. He believed Wang Lin was dead after their duel at Jue Ming Valley (canon-attested misjudgment)."
        },
        "triggerConditions": [
            "A Teng Clan member is killed → curse-track the killer (burns lifespan to track)",
            "A rival clan threatens Teng Clan dominance → exterminate the rival",
            "His curse-track target survives → escalate (build the Nine Great Nascent Souls, prepare for revenge)"
        ],
        "knownActions": [
            "After Wang Lin killed Teng Li (his great-great-grandson), burned lifespan to curse-track Wang Lin",
            "Hired Gao Qiming to divine the Wang family's location",
            "Exterminated the entire Wang family; trapped their souls in his banner; transmitted the massacre scene to Wang Lin's mind",
            "Dueled Wang Lin outside Jue Ming Valley; believed Wang Lin was dead after the duel",
            "Built the 'Nine Great Nascent Souls' (Teng One to Teng Nine) selection system",
            "All 9 Nascent Soul cultivators were killed by Wang Lin and refined into demons",
            "Wang Lin's 'Kill and Destroy the Heart' — first hunted Teng descendants, then built a tower of heads to intimidate Teng Huayuan, then finally slew him"
        ],
        "decisionStyle": ["aggressive_expansionist", "patient_planner"],
        "decisionStyleNotes": "He expands aggressively but plans multi-generationally (the Nine Great Nascent Souls system). He is defined by clan loyalty and grudge-holding.",
        "relationships": [
            {"target": "Teng Li", "relation": "Great-great-grandson. His death triggered the entire revenge arc."},
            {"target": "Wang Lin", "relation": "Enemy (killed Teng Li, then exterminated the Teng Clan). Not personal initially — Wang Lin was a target because he killed Teng Li."},
            {"target": "Gao Qiming", "relation": "Tool (diviner)."},
            {"target": "Piao Nanzi", "relation": "Ally of convenience (sealed demon who elevated his cultivation)."},
            {"target": "Wang Clan", "relation": "Rival clan. Exterminated."}
        ],
        "canonConfidence": 5,
        "source": "CANON_RI_COMPLETE_WORLD.md N83; CANON_RI_TIMELINE.md; WangLinAntagonists.java TENG_HUAYUAN; CANON_RI_CHARACTER_DECISIONS.md CD-08"
    },
    {
        "id": "N99",
        "decisionProfileId": "CD-09",
        "name": "Yao Xixue",
        "nameCn": "姚惜雪",
        "role": "antagonist (local-threat)",
        "faction": "Yao Family (Tianyun Star)",
        "peakRealm": "Infant Transformation Late Stage (initial); reborn via Blood Soul Pill",
        "status": "Alive; amnesiac; wandering with father's remnant soul.",
        "goals": {
            "primaryPreAmnesia": "Acquire Wang Lin's Ji Realm ability for her own cultivation advancement. Avenge her father (Blood Ancestor) after Wang Lin kills him.",
            "primaryPostAmnesia": "Wander with her father's remnant soul. She has no memories of Wang Lin or the revenge."
        },
        "needs": {
            "preAmnesia": ["Wang Lin's Ji Realm", "The Blood Soul Pill (to be reborn after sacrificing her body to the Wind Demon)"],
            "postAmnesia": ["Her father's remnant soul (her only companion)"]
        },
        "resources": [
            "Infant Transformation Late Stage cultivation (pre-amnesia)",
            "Heterochromic red pupils, snow-white skin (distinctive appearance)",
            "The Yao Family's backing (pre-amnesia)",
            "The Blood Soul Pill (resurrection mechanism)",
            "The Wind Demon (sealed demon she made a deal with)"
        ],
        "fears": {
            "preAmnesia": ["Her father's death (it happened — triggered her revenge arc)", "Her own death (she sacrificed her body to the Wind Demon to avoid permanent death)"],
            "postAmnesia": "Unknown (amnesiac state)."
        },
        "knowledge": {
            "knowsPreAmnesia": "Wang Lin's Ji Realm (she wanted it). The Yao Family's resources. The Blood Soul Pill's mechanics. The Immortal Monarch's Cave Mansion.",
            "doesNotKnowPreAmnesia": "Wang Lin's full capability (she underestimated him).",
            "postAmnesia": "No memories of Wang Lin or her past."
        },
        "triggerConditions": [
            "Pre-amnesia: Wang Lin's Ji Realm is accessible → ambush and extraction attempt",
            "Pre-amnesia: Her father is killed → sacrifice body to Wind Demon for revenge",
            "Post-amnesia: No hostile triggers (she is wandering amnesiac)"
        ],
        "knownActions": [
            "First appears in Ch.491 ('Why the East Sea')",
            "Arrived riding a Blood Jade to the East Sea Demon Spirit Land",
            "Cooperated with Wang Lin to explore the Immortal Monarch's Cave Mansion (conditions: Immortal Jade, Blood Soul Pill, treasure jade slip)",
            "Ambushed Wang Lin at the cave mansion → subdued and imprisoned for 100 years",
            "Wang Lin destroyed her meridians, sealed her immortal power, used Life Imprint cycle of life-and-death (couldn't die)",
            "Her father killed by Wang Lin → she used Blood Soul Pill to be reborn → sacrificed her body to the Wind Demon to seek revenge",
            "Wind Demon killed by Wang Lin's God-Slaying Spear",
            "Her memories devoured → amnesia",
            "Wang Lin released Blood Ancestor's remnant soul → amnesiac Yao Xixue departed with the remnant soul"
        ],
        "decisionStyle": ["reactive_opportunist", "aggressive_expansionist"],
        "decisionStyleNotes": "She acts on opportunities (ambush) and escalates aggressively (sacrifice to Wind Demon for revenge). Post-amnesia, she has no decision-making agency.",
        "relationships": [
            {"target": "Blood Ancestor (Yao Xinghai)", "relation": "Father. Protective. Her revenge arc was triggered by his death."},
            {"target": "Wang Lin", "relation": "Enemy (pre-amnesia). Target for Ji Realm extraction. Post-amnesia, no memory of him."},
            {"target": "Wind Demon", "relation": "Deal-maker (sacrificed her body to him for revenge power)."}
        ],
        "canonConfidence": 5,
        "source": "CANON_RI_COMPLETE_WORLD.md N99; WangLinAntagonists.java YAO_XIXUE; CANON_RI_CHARACTER_DECISIONS.md CD-09"
    },
    {
        "id": "N97",
        "decisionProfileId": "CD-10",
        "name": "Master Void",
        "nameCn": "虚空道主",
        "role": "antagonist (local-threat)",
        "faction": "independent",
        "peakRealm": "peak Nirvana Shatterer (declined through encounters with Wang Lin)",
        "status": "Killed by Wang Lin (via Sundered Night).",
        "goals": {
            "primary": "Eliminate Wang Lin before he can grow. Master Void could not allow Wang Lin (with awakened Vermilion Bird Divine Mark) to become a threat.",
            "secondary": "Maintain his own power tier."
        },
        "needs": [
            "Wang Lin's elimination",
            "His own cultivation preservation (he declines through each encounter)"
        ],
        "resources": [
            "Peak Nirvana Shatterer cultivation (initially; declined to peak early Nirvana Shatterer through encounters)",
            "Unknown sect/faction resources"
        ],
        "fears": [
            "A cultivator with the Vermilion Bird Divine Mark growing to challenge him (Wang Lin)",
            "His own cultivation decline (it happened through each encounter)"
        ],
        "knowledge": {
            "knows": "The Vermilion Bird Divine Mark's significance. Wang Lin's threat potential. The Celestial Emperors Tower.",
            "doesNotKnow": "Wang Lin's full arsenal (underestimated him repeatedly)."
        },
        "triggerConditions": [
            "A cultivator with the Vermilion Bird Divine Mark awakens → preemptive elimination attempt",
            "His own power declines → retreat and reassess"
        ],
        "knownActions": [
            "First tried to kill Wang Lin in the Celestial Emperors Tower (peak Nirvana Shatterer)",
            "Wang Lin's killing formation + source origin lure injured Master Void → mid Nirvana Shatterer",
            "2nd encounter (9th level of Celestial Emperors Tower): Zhou Yi/Wang Wei/Hu Juan/Bei Lou + All-Seer/Ling Tianhou's intervention dropped Master Void to peak early Nirvana Shatterer",
            "Sneak-attacked Wang Wei; Wang Lin switched places and defended",
            "Later sealed by Ta Jia",
            "Wang Lin killed two of All-Seer's avatars along with Master Void using Sundered Night"
        ],
        "decisionStyle": ["aggressive_expansionist"],
        "decisionStyleNotes": "He attacks perceived threats directly. He does not scheme; he uses force.",
        "relationships": [
            {"target": "Wang Lin", "relation": "Enemy (preemptive elimination target). Not personal — Wang Lin's Divine Mark made him a strategic threat."},
            {"target": "All-Seer", "relation": "Temporary alliance of convenience (both wanted Wang Lin eliminated, for different reasons)."},
            {"target": "Ta Jia", "relation": "Sealed him (adversarial)."}
        ],
        "canonConfidence": 4,
        "source": "CANON_RI_COMPLETE_WORLD.md N97; CANON_RI_CHARACTER_DECISIONS.md CD-10"
    },
    {
        "id": "N110",
        "decisionProfileId": "CD-11",
        "name": "Lian Daozhen",
        "nameCn": "连道真",
        "role": "antagonist (IAC-tier)",
        "faction": "Xiangang Continent",
        "peakRealm": "Immortal Emperor (Xiangang Continent)",
        "status": "Complex (self-destruction; relatives are Wang Lin's allies).",
        "goals": {
            "primary": "Inherit the Immortal Ancestor's plan (he failed).",
            "secondary": "Eliminate Wang Lin (Wang Lin's existence threatened his claim)."
        },
        "needs": [
            "The Immortal Ancestor's plan inheritance (he failed to obtain it)",
            "Wang Lin's elimination (to remove the rival claim)"
        ],
        "resources": [
            "Immortal Emperor cultivation",
            "The Xiangang Continent's resources",
            "His brother Lian Daofei (complex — brother is Wang Lin's ally)"
        ],
        "fears": [
            "Failing to inherit the Immortal Ancestor's plan (he did fail)",
            "Lian Daofei's bloodline awakening (it did — Eight Extremities Great Heavenly Venerable)"
        ],
        "knowledge": {
            "knows": "The Immortal Ancestor's plan. The Xiangang Continent's power structure. His brother's capabilities.",
            "doesNotKnow": "Wang Lin's full capability (underestimated him)."
        },
        "triggerConditions": [
            "The Immortal Ancestor's plan inheritance becomes available → claim it",
            "A rival claimant emerges → eliminate them",
            "Failure → self-destruction (canon-attested: he self-destructed his Immortal Emperor cultivation)"
        ],
        "knownActions": [
            "Failed to inherit the Immortal Ancestor's plan",
            "After failure, Lian Daofei's bloodline awakened (Eight Extremities)",
            "Immortal Emperor self-destructured",
            "Wang Lin captured the Immortal Emperor's soul and injured the Infant Skull belonging to the Dao Yi Great Celestial Venerable"
        ],
        "decisionStyle": ["aggressive_expansionist"],
        "decisionStyleNotes": "He claimed the inheritance aggressively. On failure, he self-destructed rather than accept defeat.",
        "relationships": [
            {"target": "Lian Daofei", "relation": "Brother (adversarial — Lian Daofei is Wang Lin's ally)."},
            {"target": "Wang Lin", "relation": "Enemy (rival claimant to the Immortal Ancestor's plan)."}
        ],
        "canonConfidence": 4,
        "source": "CANON_RI_COMPLETE_WORLD.md N110; CANON_RI_CHARACTER_DECISIONS.md CD-11"
    },
    {
        "id": "N122",
        "decisionProfileId": "CD-12",
        "name": "Dao Devil Sect Master / Dao Demon Sect Master",
        "nameCn": "道魔宗主",
        "role": "antagonist (IAC-tier)",
        "faction": "Dao Devil Sect (IAC)",
        "peakRealm": "Third Step+",
        "status": "Killed by Wang Lin.",
        "goals": {
            "primary": "Resurrect the Green Devil Scorpion (the entity his sect worships).",
            "secondary": "Use Wang Lin's body as the sacrifice/vessel for the resurrection."
        },
        "needs": [
            "A suitable sacrifice (Wang Lin — who had multiple Essences implanted by Ji Si)",
            "The Dao Devil Sect's ritual infrastructure",
            "The Green Devil Scorpion's remnant"
        ],
        "resources": [
            "Third-Step+ cultivation",
            "The Dao Devil Sect (IAC faction)",
            "Ji Si (who implanted Essences into Wang Lin to prepare him as a vessel)",
            "The Green Devil Scorpion remnant"
        ],
        "fears": [
            "The Green Devil Scorpion's resurrection failing (it did fail — Wang Lin reclaimed his body)",
            "Wang Lin escaping the ritual (he did)"
        ],
        "knowledge": {
            "knows": "The Green Devil Scorpion resurrection ritual. Wang Lin's implanted Essences (Ji Si prepared them). The Dao Devil Sect's capabilities.",
            "doesNotKnow": "Wang Lin's Heaven-Defying Bead (it protected him from the mind-erasure)."
        },
        "triggerConditions": [
            "A cultivator with multiple Essences is available → capture and prepare for the ritual",
            "The ritual is ready → execute the resurrection"
        ],
        "knownActions": [
            "Fed his own Thunder Essence into Wang Lin's to evolve it into an Essence True Body (controlled by him to erase Wang Lin's mind/memory)",
            "Wang Lin protected by Heaven Defying Pearl → reclaimed the Essence True Body",
            "Wang Lin annihilated the Dao Devil Sect"
        ],
        "decisionStyle": ["patient_planner", "aggressive_expansionist"],
        "decisionStyleNotes": "He schemes (Ji Si's Essences) and then acts forcefully (the ritual).",
        "relationships": [
            {"target": "Green Devil Scorpion", "relation": "Worshipped entity. Goal of resurrection."},
            {"target": "Ji Si", "relation": "Subordinate (implanted Essences into Wang Lin)."},
            {"target": "Wang Lin", "relation": "Sacrifice vessel. Not personal — Wang Lin was a tool."}
        ],
        "canonConfidence": 4,
        "source": "CANON_RI_COMPLETE_WORLD.md N122, N132; CANON_RI_CHARACTER_DECISIONS.md CD-12"
    },
    {
        "id": "N117",
        "decisionProfileId": "CD-13",
        "name": "Gu Dao",
        "nameCn": "古道",
        "role": "rival / antagonist (IAC-tier)",
        "faction": "IAC",
        "peakRealm": "Grand Empyrean (strongest on IAC before Wang Lin)",
        "status": "Alive (acknowledges Wang Lin as superior).",
        "goals": {
            "primary": "Maintain his position as the strongest on the IAC (before Wang Lin's arrival).",
            "secondary": "Test Wang Lin's capability (the battle with Wang Lin was as much a test as a fight)."
        },
        "needs": [
            "His position as #1 on the IAC (lost to Wang Lin)",
            "The IAC's stability"
        ],
        "resources": [
            "Grand Empyrean cultivation (strongest on IAC before Wang Lin)",
            "The IAC's resources"
        ],
        "fears": [
            "A cultivator surpassing him (Wang Lin did)",
            "The IAC's power structure being disrupted"
        ],
        "knowledge": {
            "knows": "The IAC's full power structure. All Nine Suns. Wang Lin's capability (after their battle)."
        },
        "triggerConditions": [
            "A cultivator approaches his power tier → battle (to test and establish hierarchy)",
            "The IAC is threatened → defensive response"
        ],
        "knownActions": [
            "Wang Lin's battle with Gu Dao → enlightenment → fused with Void Avatar → full Nine Songs Three Signs → significant progress in fusion of Celestial and Ancient powers",
            "Wang Lin becomes #1 on IAC after this battle",
            "Gu Dao acknowledges Wang Lin as superior"
        ],
        "decisionStyle": ["aggressive_expansionist", "cautious_conservative"],
        "decisionStyleNotes": "He tests rivals through battle. Once defeated, he acknowledges hierarchy and becomes conservative.",
        "relationships": [
            {"target": "Wang Lin", "relation": "Rival (initially) → acknowledged superior (after defeat). Not personal — hierarchy establishment."}
        ],
        "canonConfidence": 4,
        "source": "CANON_RI_COMPLETE_WORLD.md N117; CANON_RI_CHARACTER_DECISIONS.md CD-13"
    },

    # ==================== Part 4 — Mentor NPCs ====================
    {
        "id": "N20",
        "decisionProfileId": "CD-14",
        "name": "Situ Nan",
        "nameCn": "司徒南",
        "role": "mentor / companion / sworn brother",
        "faction": "Vermilion Bird Country (2nd-Gen Vermilion Bird Master); later Wu Xuan Country (Grand Marshal 'Si Nan')",
        "peakRealm": "Yang Solid Peak (reconstructed); Heaven Trampling (after IAC reincarnation)",
        "status": "Alive; becomes prince (lifelong dream); no regrets.",
        "goals": {
            "primary": "Regain a physical body (achieved — Wang Lin helped reconstruct one at Soul Transformation stage).",
            "secondary": "Become a prince (achieved — reincarnated on IAC as 'Si Nan,' Grand Marshal of Wu Xuan Country). This was his lifelong dream.",
            "tertiary": "Protect Wang Lin (sworn brother). Repay Ye Wuyou's life-saving favor (achieved — guarded Vermilion Bird Star for 1000 years)."
        },
        "needs": [
            "A physical body (achieved)",
            "Wang Lin's safety (achieved — Wang Lin transcended)",
            "His lifelong dream of becoming prince (achieved)"
        ],
        "resources": [
            "Originally one of the seven souls (Green Soul — cultivation talent) of the Seven-Colored Immortal Venerable",
            "Primordial spirit (fled into Heaven Defying Pearl after betrayal)",
            "Vermilion Bird lineage knowledge (2nd-Gen Vermilion Bird Master)",
            "Soul Refining technique knowledge",
            "Heaven-Defying Pearl residence (he lived inside it)"
        ],
        "fears": [
            "Body-snatching failure (he initially planned to body-snatch Wang Lin but gave up)",
            "Wang Lin's death (sworn brother)",
            "The 3rd-Gen Vermilion Bird and Tan Lang (they betrayed him)"
        ],
        "knowledge": {
            "knows": "The Vermilion Bird lineage. The Heaven-Defying Pearl's interior. The Seven-Colored Immortal Venerable's nature (he was a fragment). Soul Refining techniques. Wang Lin's full cultivation journey (he was present from the beginning).",
            "doesNotKnow": "The full Cave World ownership structure (he learned about it alongside Wang Lin)."
        },
        "triggerConditions": [
            "A cultivator finds the Heaven-Defying Pearl → he evaluates them as a potential host (he chose Wang Lin)",
            "His sworn brother is threatened → sacrifice response (he sacrificed his remaining power to save Wang Lin's soul after Teng Huayuan)",
            "The Vermilion Bird lineage is threatened → protective response"
        ],
        "knownActions": [
            "Originally the Green Soul (cultivation talent) of the Seven-Colored Immortal Venerable",
            "Betrayed by 3rd-Gen Vermilion Bird and Tan Lang; primordial spirit fled into Heaven Defying Pearl",
            "Met Wang Lin when Wang Lin found the bead",
            "Gave up body-snatching; taught Wang Lin cultivation (Underworld Ascension Method, Vermilion Bird Burning Heaven Art)",
            "Sacrificed his remaining power to save Wang Lin's soul after Teng Huayuan",
            "Wang Lin helped him reconstruct a physical body at Soul Transformation stage",
            "Founded 'Southern King' faction on Feng Luan Star",
            "Reincarnated on IAC as 'Si Nan,' Grand Marshal of Wu Xuan Country (achieved his prince dream)"
        ],
        "decisionStyle": ["protective_loyalist", "curious_explorer"],
        "decisionStyleNotes": "He is defined by loyalty (to Wang Lin, to Ye Wuyou's memory). He gave up body-snatching because he came to care about Wang Lin. He explored the world alongside Wang Lin.",
        "relationships": [
            {"target": "Wang Lin", "relation": "Sworn brother. Primary attachment. Would sacrifice everything for him."},
            {"target": "Ye Wuyou", "relation": "Life-saving benefactor (1st-Gen Vermilion Bird Master). Repaid the debt by guarding Vermilion Bird Star for 1000 years."},
            {"target": "3rd-Gen Vermilion Bird Master", "relation": "Betrayer. Enemy (deceased)."},
            {"target": "Tan Lang", "relation": "Betrayer. Enemy (reduced to a pet by Twin Great Heavenly Venerables)."},
            {"target": "Seven-Colored Daoist", "relation": "Creator (Situ Nan was his Green Soul). Complex — Situ Nan fled him, but did not actively pursue revenge."}
        ],
        "inheritanceOffered": [
            {"name": "Underworld Ascension Method (黄泉升窍诀)", "type": "Unique (mentor transmission)", "notes": "Wang Lin received from Situ Nan via the Heaven-Defying Pearl. The player could receive this IF they acquire the Heaven-Defying Pearl and Situ Nan trusts them."},
            {"name": "Vermilion Bird Burning Heaven Art (朱雀焚天功)", "type": "Unique (Vermilion Bird lineage)", "notes": "Situ Nan is 2nd-Gen Vermilion Bird Master."},
            {"name": "Soul Refining basics", "type": "Recoverable", "notes": "Situ Nan knows the foundational technique; the full Soul Refining Sect inheritance came from Du Tian."}
        ],
        "canonConfidence": 5,
        "source": "CANON_RI_COMPLETE_WORLD.md N20; CANON_RI_TIMELINE.md; CANON_RI_CHARACTER_DECISIONS.md CD-14"
    },
    {
        "id": "N22",
        "decisionProfileId": "CD-15",
        "name": "Tu Si",
        "nameCn": "涂司",
        "role": "mentor (inheritance only — long dead)",
        "faction": "Ancient Clan",
        "peakRealm": "8-Star Ancient God (peak)",
        "status": "Long dead; body became a realm (Land of the Ancient God / Chaotic Broken Stars). Inheritance split between Wang Lin (knowledge) and Tuo Sen (power).",
        "goals": {
            "whenAlivePrimary": "Achieve peak Ancient God cultivation (achieved — 8-Star).",
            "whenAliveSecondary": "Split his inheritance into 'knowledge' and 'power' via the Ink Flow Split Soul Technique (this created Tuo Sen as his demonic thought)."
        },
        "needs": {
            "whenAlive": [
                "The Ink Flow Split Soul Technique to succeed (it partially failed — created Tuo Sen as a separate entity)",
                "A worthy inheritor for his knowledge (Wang Lin)"
            ]
        },
        "resources": {
            "legacy": [
                "His body became the Land of the Ancient God (3-level Chaotic Broken Stars realm)",
                "The 'knowledge' inheritance (memory legacy — Wang Lin received)",
                "The 'power' inheritance (Tuo Sen received)",
                "The 'Great Enlightened One' title (granted to Wang Lin)"
            ]
        },
        "fears": {
            "whenAlive": [
                "The Ink Flow Split Soul Technique failing (it did partially fail)",
                "His inheritance being lost (it was not — Wang Lin and Tuo Sen claimed it)"
            ]
        },
        "knowledge": {
            "inheritanceContains": "Ancient God Tactic (古神诀) — body reconstruction, plunder, star-absorption. Heaven Technique (通天诀) — movement inside Ancient God bodies. 8-Star Ancient God cultivation knowledge."
        },
        "triggerConditions": [
            "A cultivator enters the Land of the Ancient God → the inheritance trial activates",
            "A cultivator passes the trial → receives the 'knowledge' inheritance (Wang Lin did)",
            "A cultivator fails the trial → Tuo Sen (the 'power' inheritance) hunts them"
        ],
        "knownActions": [
            "Achieved 8-Star Ancient God (peak)",
            "Attempted the Ink Flow Split Soul Technique → created Tuo Sen (his demonic thought) as a separate entity",
            "His body became the Land of the Ancient God",
            "Wang Lin inherited his 'knowledge' inheritance (Ch.199 — 1-Star Ancient God)",
            "Tuo Sen inherited his 'power' inheritance",
            "Wang Lin later returned the memory inheritance to Tuo Sen (reconciliation)"
        ],
        "decisionStyle": "N/A (long dead; acts only through the inheritance trial mechanism)",
        "relationships": [
            {"target": "Wang Lin", "relation": "Inheritor (knowledge)."},
            {"target": "Tuo Sen", "relation": "Inheritor (power) / demonic thought. Complex — they are technically the same being split into two."}
        ],
        "inheritanceOffered": [
            {"name": "Ancient God Tactic (古神诀)", "type": "Unique (inheritance)", "notes": "One owner at a time. Wang Lin claimed it. The player could claim it IF they reach the Land of the Ancient God before Wang Lin's canonical claim (impossible at edge of canon) OR take it from Wang Lin."},
            {"name": "Heaven Technique (通天诀)", "type": "Repeatable (auxiliary technique)", "notes": "Not the core inheritance."}
        ],
        "canonConfidence": 5,
        "source": "CANON_RI_COMPLETE_WORLD.md N22; CANON_RI_TIMELINE.md; CANON_RI_CHARACTER_DECISIONS.md CD-15"
    },
    {
        "id": "N23",
        "decisionProfileId": "CD-16",
        "name": "Du Tian",
        "nameCn": "顿天",
        "altNameVariants": ["Dun Tian (敦天) — CANON_RI_CIVILIZATION.md CIV-02", "Dun Tian (杜天) — task brief variant (typo)"],
        "role": "mentor / ally",
        "faction": "Soul Refining Sect (Pilu Kingdom) — ancestor",
        "peakRealm": "Nirvana Scryer+ (erased his own consciousness to become a soul in the Soul Banner)",
        "status": "Self-erased; exists as a soul in the Ten Billion Soul Banner.",
        "goals": {
            "primary": "Elevate the Soul Refining Sect to 6th-level (he asked Wang Lin to promise this).",
            "secondary": "Pass on the Soul Refining Sect inheritance to a worthy successor (Wang Lin)."
        },
        "needs": [
            "The Soul Refining Sect elevated to 6th-level (Wang Lin promised)",
            "A worthy successor (Wang Lin)",
            "Immortal Jades for Soul Transformation breakthrough (Wang Lin helped seize them)"
        ],
        "resources": [
            "Nirvana Scryer+ cultivation",
            "The Soul Refining Sect's full inheritance",
            "The Ten Billion Soul Banner (signature artifact)",
            "Senior brother to Nian Tian"
        ],
        "fears": [
            "The Soul Refining Sect declining (he sacrificed himself to prevent this)",
            "An unworthy successor claiming the inheritance (he chose Wang Lin)"
        ],
        "knowledge": {
            "knows": "The Soul Refining Sect's complete technique system. The Ten Billion Soul Banner's mechanics. Soul Transformation breakthrough requirements."
        },
        "triggerConditions": [
            "A cultivator worthy of the Soul Refining Sect inheritance appears → evaluate and transmit",
            "The Soul Refining Sect is threatened → defensive response (he erased himself to become a soul in the banner, preserving the sect's power)"
        ],
        "knownActions": [
            "Gave Wang Lin 3 gifts: (1) helped Wang Lin's clone reach Nascent Soul peak, (2) helped Wang Lin's true body reach 3-Star Ancient God, (3) bestowed the Ten Billion Soul Banner and Soul Refining Sect inheritance",
            "Took Wang Lin to seize Immortal Jades for Soul Transformation breakthrough",
            "Erased his own consciousness to become a soul within the Soul Banner after Wang Lin's promise to elevate Soul Refining Sect to 6th-level"
        ],
        "decisionStyle": ["protective_loyalist", "patient_planner"],
        "decisionStyleNotes": "He sacrificed himself for the sect. He planned multi-step gifts for Wang Lin.",
        "relationships": [
            {"target": "Wang Lin", "relation": "Successor. Chose him. Sacrificed himself based on Wang Lin's promise."},
            {"target": "Soul Refining Sect", "relation": "His life's purpose. Protective."},
            {"target": "Nian Tian", "relation": "Senior brother."}
        ],
        "inheritanceOffered": [
            {"name": "Soul Refining Sect inheritance", "type": "Unique (true inheritance)", "notes": "Wang Lin received the full transmission. The player could receive this IF they find Du Tian (in the Ten Billion Soul Banner) and he deems them worthy, OR take the banner from Wang Lin."},
            {"name": "Ten Billion Soul Banner", "type": "Unique (artifact)", "notes": "One owner. Wang Lin has it."}
        ],
        "canonConfidence": 5,
        "source": "CANON_RI_COMPLETE_WORLD.md N23; CANON_RI_CHARACTER_DECISIONS.md CD-16; CANON_RI_CIVILIZATION.md CIV-02"
    },
    {
        "id": "N24",
        "decisionProfileId": "CD-17",
        "name": "Bai Fan",
        "nameCn": "白凡",
        "role": "mentor (inheritance only — long dead)",
        "faction": "Thunder Immortal World",
        "peakRealm": "Immortal Emperor (Third Step+)",
        "status": "Long dead; inheritance passed to Wang Lin.",
        "goals": {"whenAlive": "Unknown (long dead; only the inheritance matters)"},
        "resources": {
            "legacy": [
                "The Thunder Immortal World's Collection Pavilion",
                "Mountain Crumble spell (Wang Lin inherited)",
                "Six Paths Triple Techniques (Call Wind, Call Rain, Scatter Beans to Form Soldiers, Mountain Collapse, Dark Moon Has Clear, etc.)"
            ]
        },
        "knowledge": {
            "inheritanceContains": "Mountain Crumble spell. Six Paths Triple Techniques. Various Thunder Immortal World techniques."
        },
        "triggerConditions": [
            "A cultivator finds Bai Fan's Collection Pavilion in the Thunder Immortal World → inheritance available"
        ],
        "knownActions": [
            "Wang Lin finds Bai Fan's Collection Pavilion in the Thunder Immortal World",
            "Inherits the Mountain Crumble spell and Six Paths Triple Techniques"
        ],
        "decisionStyle": "N/A (long dead; inheritance only)",
        "relationships": [],
        "inheritanceOffered": [
            {"name": "Mountain Crumble spell", "type": "Repeatable (text/inheritance pavilion)", "notes": "Multiple cultivators could find the pavilion and learn."},
            {"name": "Six Paths Triple Techniques", "type": "Repeatable (same pavilion)"}
        ],
        "canonConfidence": 4,
        "source": "CANON_RI_COMPLETE_WORLD.md N24; CANON_RI_CHARACTER_DECISIONS.md CD-17"
    },
    {
        "id": "N28",
        "decisionProfileId": "CD-18",
        "name": "Xuan Luo",
        "nameCn": "玄罗",
        "role": "mentor / true master",
        "faction": "Ancient Clan (Dao Gu lineage)",
        "peakRealm": "Great Heavenly Venerable (Dao Gu lineage)",
        "status": "Alive; undergoes reincarnation (Wang Lin gifted him a protective life-saving jade slip).",
        "goals": {
            "primary": "Accept and cultivate a worthy disciple (Wang Lin — the only one he formally acknowledges on the IAC).",
            "secondary": "Reincarnate a dozen of Wang Lin's friends onto the Immortal Execution Continent (achieved).",
            "tertiary": "His own reincarnation cycle."
        },
        "needs": [
            "A worthy disciple (Wang Lin)",
            "The Ancient Clan's stability",
            "Wang Lin's friends' safety (he reincarnated them)"
        ],
        "resources": [
            "Great Heavenly Venerable cultivation (Dao Gu lineage)",
            "The Ancient Clan's resources",
            "Ability to reincarnate cultivators onto specific continents",
            "Protective life-saving jade slips"
        ],
        "fears": [
            "Failing to protect his disciple (he intervened during Wang Lin's tribulation)",
            "The Ancient Clan's decline"
        ],
        "knowledge": {
            "knows": "The Ancient Clan's full lineage. The Dao Gu lineage's techniques. The Cave World's origin (he was present at the Seven Paths Sect entrance when Dao Yi Great Heavenly Venerable fought over the Primordial God Realm fragment — this indirectly caused the Cave World's birth). Reincarnation mechanics."
        },
        "triggerConditions": [
            "A cultivator with sufficient talent and character appears → evaluate as potential disciple",
            "His disciple is threatened → intervention (he intervened during Wang Lin's tribulation)",
            "The Ancient Clan is threatened → defensive response"
        ],
        "knownActions": [
            "Refused the Ancient Dao Great Heavenly Venerable's offer to take him as disciple",
            "Fought Dao Yi Great Heavenly Venerable over a fragment of Primordial God Realm at the Seven Paths Sect entrance — indirectly causing the Cave World's birth",
            "Entered the Cave World to accept Wang Lin as his only disciple",
            "Helped over a dozen of Wang Lin's friends reincarnate into the Immortal Execution Continent",
            "Intervened during Wang Lin's tribulation",
            "Undergoes reincarnation (Wang Lin gifted him a protective life-saving jade slip)"
        ],
        "decisionStyle": ["protective_loyalist", "curious_explorer"],
        "decisionStyleNotes": "He is loyal to his disciple (Wang Lin). He explored the Cave World to find Wang Lin. He is the only master Wang Lin formally acknowledges.",
        "relationships": [
            {"target": "Wang Lin", "relation": "Only formally acknowledged disciple. Protective."},
            {"target": "Ancient Dao Great Heavenly Venerable", "relation": "Refused his offer (adversarial respect)."},
            {"target": "Dao Yi Great Heavenly Venerable", "relation": "Fought him (adversarial)."}
        ],
        "inheritanceOffered": [
            {"name": "Dao Gu lineage techniques", "type": "Unique (mentor transmission)", "notes": "Wang Lin received the formal transmission. The player could receive this IF they find Xuan Luo and he deems them worthy (he is extremely selective — only one disciple in canon)."},
            {"name": "Reincarnation assistance", "type": "Unique (service, not technique)", "notes": "Xuan Luo can reincarnate cultivators onto specific continents."}
        ],
        "canonConfidence": 5,
        "source": "CANON_RI_COMPLETE_WORLD.md N28; CANON_RI_CHARACTER_DECISIONS.md CD-18"
    },
    {
        "id": "N16",
        "decisionProfileId": "CD-19",
        "name": "Dao Master Blue Dream",
        "nameCn": "蓝梦道主",
        "altNpcIds": ["N16", "N29"],
        "role": "family / mentor",
        "faction": "Blue Silk Clan",
        "peakRealm": "Dao Master (Void Tribulant+)",
        "status": "Alive.",
        "goals": {
            "primary": "Protect his daughter Li Qianmei.",
            "secondary": "Teach worthy cultivators (Wang Lin)."
        },
        "needs": [
            "Li Qianmei's safety",
            "The Blue Silk Clan's stability"
        ],
        "resources": [
            "Dao Master cultivation (Void Tribulant+)",
            "The Blue Silk Clan's resources",
            "Light Shadow Shield, Dao Art Fusion, Overturn Heaven Seal (techniques he taught Wang Lin)"
        ],
        "fears": [
            "Li Qianmei's harm (she was — Wang Lin healed her at the cost of her memories)",
            "His own injury (Wang Lin injured his palm at one point)"
        ],
        "knowledge": {
            "knows": "The Blue Silk Clan's techniques. Light Shadow Shield. Dao Art Fusion. Overturn Heaven Seal."
        },
        "triggerConditions": [
            "His daughter is threatened → maximum-force response",
            "A worthy cultivator seeks his teaching → evaluate and transmit"
        ],
        "knownActions": [
            "Wang Lin injured Blue Dream's palm at one point",
            "Later healed Li Qianmei at the cost of her memories",
            "Taught Wang Lin Light Shadow Shield, Dao Art Fusion, Overturn Heaven Seal"
        ],
        "decisionStyle": ["protective_loyalist"],
        "decisionStyleNotes": "His daughter is his primary concern.",
        "relationships": [
            {"target": "Li Qianmei", "relation": "Daughter. Protective."},
            {"target": "Wang Lin", "relation": "Student (complex — Wang Lin injured him, but he still taught Wang Lin). The healing of Li Qianmei was a transaction."}
        ],
        "inheritanceOffered": [
            {"name": "Light Shadow Shield, Dao Art Fusion, Overturn Heaven Seal", "type": "Repeatable (mentor transmission, but he teaches multiple students)", "notes": "The player could learn these IF they find Dao Master Blue Dream and he agrees to teach."}
        ],
        "canonConfidence": 4,
        "source": "CANON_RI_COMPLETE_WORLD.md N16, N29; CANON_RI_CHARACTER_DECISIONS.md CD-19"
    },
    {
        "id": "N52",
        "decisionProfileId": "CD-20",
        "name": "Second Generation Vermilion Bird / Young Emperor of the Fallen Land",
        "nameCn": "第二代朱雀 / 陨落之地少帝",
        "role": "ally / mentor",
        "faction": "Vermilion Bird Divine Sect / Fallen Land",
        "peakRealm": "Void Flame Cultivator (2nd-Gen Vermilion Bird Divine Emperor)",
        "status": "Alive.",
        "goals": {
            "primary": "Guide the next generation of Vermilion Bird inheritors (Wang Lin).",
            "secondary": "Maintain the Fallen Land's integrity."
        },
        "needs": [
            "A worthy Vermilion Bird successor (Wang Lin)",
            "The Fallen Land's stability"
        ],
        "resources": [
            "2nd-Gen Vermilion Bird Divine Emperor cultivation",
            "The Fallen Land (realm he controls)",
            "Dragon blood (he fished for a dragon and gifted its blood to Wang Lin)",
            "The Dao of Strength (taught to Wang Lin)",
            "Three supreme techniques of the Vermilion Bird (taught one to Wang Lin)"
        ],
        "fears": [
            "The Vermilion Bird lineage declining",
            "The Fallen Land being disrupted"
        ],
        "knowledge": {
            "knows": "The Vermilion Bird lineage's full history. The Fallen Land's mechanics. The Dao of Strength. The three supreme techniques of the Vermilion Bird."
        },
        "triggerConditions": [
            "A cultivator undertakes the Young Emperor trial → evaluate and guide",
            "The Vermilion Bird lineage is threatened → protective response"
        ],
        "knownActions": [
            "Fished for a dragon, took its blood, gifted it to Wang Lin",
            "Taught Wang Lin the Dao of Strength",
            "Stood up for Wang Lin at the Trial of Heaven",
            "Taught Wang Lin one of the three supreme techniques of the Vermilion Bird when Wang Lin completed the trial",
            "Asked by Wang Lin to take care of Zhong Dahong"
        ],
        "decisionStyle": ["protective_loyalist", "curious_explorer"],
        "decisionStyleNotes": "He guides the next generation. He stood up for Wang Lin at the Trial of Heaven.",
        "relationships": [
            {"target": "Wang Lin", "relation": "Mentee. Guided him through the Young Emperor trial."},
            {"target": "Zhong Dahong", "relation": "Asked to take care of him (by Wang Lin)."}
        ],
        "inheritanceOffered": [
            {"name": "Dao of Strength", "type": "Repeatable (mentor transmission)"},
            {"name": "Vermilion Bird supreme technique (one of three)", "type": "Unique (one student per technique per generation)", "notes": "Wang Lin received one. The player could receive a different one IF they undertake the Young Emperor trial."},
            {"name": "Dragon blood", "type": "Unique (one-time gift)", "notes": "Wang Lin received it."}
        ],
        "canonConfidence": 4,
        "source": "CANON_RI_COMPLETE_WORLD.md N52; CANON_RI_CHARACTER_DECISIONS.md CD-20"
    },
    {
        "id": "N30",
        "decisionProfileId": "CD-21",
        "name": "Qing Shui",
        "nameCn": "清水",
        "role": "ally / senior brother",
        "faction": "Qing Shui Kingdom (former prince); Colorful Immortal Venerable's slaughter soul",
        "peakRealm": "Third Step (Slaughter Essence)",
        "status": "Alive; reaches Third Step.",
        "goals": {
            "primary": "Find his daughter Hong Die (achieved — with Wang Lin's help).",
            "secondary": "Protect Wang Lin (senior brother bond, recognized across generations via Bai Fan's technique).",
            "tertiary": "Cultivate the Slaughter Essence (achieved — Third Step)."
        },
        "needs": [
            "Hong Die's safety (achieved — reincarnated as Qing Hong on IAC)",
            "Wang Lin's safety",
            "His own cultivation advancement"
        ],
        "resources": [
            "Third Step cultivation (Slaughter Essence)",
            "Formed from the Seven-Colored Immortal Venerable's lifetime of slaughter",
            "Slaughter Sword (Slaughter Essence) — gifted to Wang Lin when Wang Lin broke through Sky Gate",
            "Immortal arts (taught to Wang Lin)"
        ],
        "fears": [
            "Hong Die's death (she died — but reincarnated)",
            "Wang Lin's death"
        ],
        "knowledge": {
            "knows": "The Slaughter Essence. The Seven-Colored Immortal Venerable's nature (he was a fragment). Bai Fan's technique (the cross-generation bond mechanism). Hong Die's location (after reincarnation)."
        },
        "triggerConditions": [
            "His daughter is threatened → maximum-force response",
            "Wang Lin is threatened → protective response (saved Wang Lin multiple times)",
            "A cultivator seeks the Slaughter Essence → evaluate (he gifted the Slaughter Sword to Wang Lin)"
        ],
        "knownActions": [
            "Saved Wang Lin multiple times",
            "Taught Wang Lin immortal arts",
            "Wang Lin helped him find his daughter Hong Die",
            "Reincarnated on Immortal Execution Star; memories auto-recovered",
            "In ISSTH, left a clone to assist Meng Hao (cross-novel)",
            "Gifted Wang Lin the Slaughter Sword (Slaughter Essence) when Wang Lin broke through Sky Gate"
        ],
        "decisionStyle": ["protective_loyalist"],
        "decisionStyleNotes": "He is defined by loyalty to Wang Lin and his daughter.",
        "relationships": [
            {"target": "Wang Lin", "relation": "Senior brother (recognized across generations). Protective."},
            {"target": "Hong Die", "relation": "Daughter. Protective."},
            {"target": "Seven-Colored Immortal Venerable", "relation": "Creator (Qing Shui was his Slaughter Soul). Complex — Qing Shui is independent, not loyal to his creator."}
        ],
        "inheritanceOffered": [
            {"name": "Slaughter Essence transmission", "type": "Unique (the Slaughter Sword was gifted to Wang Lin)", "notes": "The player could receive this IF they are recognized as a 'junior brother' through Bai Fan's technique bond AND Qing Shui deems them worthy."},
            {"name": "Immortal arts", "type": "Repeatable (mentor transmission)", "notes": "Qing Shui taught Wang Lin multiple arts."}
        ],
        "canonConfidence": 5,
        "source": "CANON_RI_COMPLETE_WORLD.md N30; CANON_RI_CHARACTER_DECISIONS.md CD-21"
    },

    # ==================== Part 5 — Ally NPCs ====================
    {
        "id": "N17",
        "decisionProfileId": "CD-22",
        "name": "Li Muwan / Wan'er",
        "nameCn": "李慕婉",
        "role": "spouse / protagonist's beloved",
        "faction": "Luo He Sect (Flame-Burning Country) → Cloud Sky Sect / Yuntian Sect (Elder → Master)",
        "peakRealm": "Nascent Soul (at death); Treading Heaven Realm (after resurrection by Wang Lin)",
        "status": "Dead → soul preserved 700 years → resurrected → transcendent.",
        "goals": {
            "primary": "Be with Wang Lin (achieved — resurrected and transcended together).",
            "secondary": "Refine pills and techniques to support Wang Lin (she made the Distant Heaven Pill that triggered Wang Lin's Core Formation)."
        },
        "needs": [
            "Wang Lin's presence (achieved)",
            "Her own cultivation (she failed Nascent Soul formation multiple times; finally formed it but died)"
        ],
        "resources": [
            "Nascent Soul cultivation (at death); Treading Heaven Realm (after resurrection)",
            "Pill refinement mastery (she made the Distant Heaven Pill)",
            "Azure Dragon Jade Slip (she drained her life force to refine it for Wang Lin)",
            "Heaven-Avoiding Coffin (life-saving artifact)"
        ],
        "fears": [
            "Separation from Wang Lin (this drove her entire arc)",
            "Her own death (she died — Wang Lin resurrected her)"
        ],
        "knowledge": {
            "knows": "Pill refinement. The Luo He Sect's techniques. The Cloud Sky Sect's techniques. Wang Lin's full journey (she was with him for most of it)."
        },
        "triggerConditions": [
            "Wang Lin is threatened → supportive response (pill refinement, technique support)",
            "She is separated from Wang Lin → wait for reunion (she waited 200 years for him)"
        ],
        "knownActions": [
            "Met Wang Lin in Fire Burn Country (escaping a Fire Beast)",
            "Drained her life force to refine the Azure Dragon Jade Slip for Wang Lin (damaged her foundation → stuck at early Core Formation)",
            "Made the Distant Heaven Pill that triggered Wang Lin's Core Formation",
            "Waited 200 years for him",
            "Married Sun Zhenwei — Wang Lin killed him and took her back",
            "Failed Nascent Soul formation multiple times; finally formed Nascent Soul but died (body expired, 500 years old)",
            "Wang Lin placed her soul in Heaven Defying Bead for 700 years",
            "Tried Qi Xi spell (failed)",
            "Heaven-Avoiding Coffin (life-saving)",
            "Finally resurrected by Wang Lin at 4th Step",
            "Transcends with him"
        ],
        "decisionStyle": ["protective_loyalist"],
        "decisionStyleNotes": "She is defined by her love for Wang Lin. All decisions serve their reunion.",
        "relationships": [
            {"target": "Wang Lin", "relation": "Husband. Eternal."},
            {"target": "Li Qiqing", "relation": "Brother."},
            {"target": "Sun Zhenwei", "relation": "Forced husband (Wang Lin killed him)."}
        ],
        "canonConfidence": 5,
        "source": "CANON_RI_COMPLETE_WORLD.md N17; CANON_RI_CHARACTER_DECISIONS.md CD-22"
    },
    {
        "id": "N18",
        "decisionProfileId": "CD-23",
        "name": "Li Qianmei",
        "nameCn": "李千媚",
        "role": "spouse / ally",
        "faction": "Ghost Sect (originally) → Wang Lin's lineage",
        "peakRealm": "Nirvana Scryer+ (her father healed her, at cost of memories)",
        "status": "Alive; with one of Wang Lin's clones.",
        "goals": {
            "primary": "Be with Wang Lin (achieved — with one of his clones).",
            "secondary": "Recover her memories (lost when her father healed her)."
        },
        "needs": [
            "Wang Lin's presence (achieved via clone)",
            "Her memories (partially lost)"
        ],
        "resources": [
            "Nirvana Scryer+ cultivation",
            "Ghost Sect techniques",
            "Blood anointment (she saved Wang Lin with 10-year blood anointment after Daoist Water)"
        ],
        "fears": [
            "Losing Wang Lin (she smeared blood on his stone-petrified body for 10 years to save him)",
            "Her own memory loss"
        ],
        "knowledge": {
            "knows": "The Ghost Sect's techniques. Wang Lin's nature (she asked him 3 questions at the Origin Sect)."
        },
        "triggerConditions": [
            "Wang Lin is petrified/injured → blood anointment to save him (she did this for 10 years)",
            "Wang Lin is threatened → supportive response"
        ],
        "knownActions": [
            "Asked Wang Lin 3 questions at the Origin Sect",
            "Traveled with him",
            "Smeared blood on his stone-petrified body for 10 years to save him",
            "Sent into a spatial realm with powerful beasts by the Ghost Sect",
            "Rescued by Wang Lin",
            "Healed by her father, losing most memories of Wang Lin",
            "The 'doppelganger' (one of Wang Lin's clones) eventually accompanies her"
        ],
        "decisionStyle": ["protective_loyalist"],
        "decisionStyleNotes": "She is defined by her devotion to Wang Lin (10 years of blood anointment).",
        "relationships": [
            {"target": "Wang Lin", "relation": "Husband (primary). Devoted."},
            {"target": "Dao Master Blue Dream", "relation": "Father."},
            {"target": "Wang Lin's clone", "relation": "Companion (after her memory loss, the clone accompanies her)."}
        ],
        "canonConfidence": 5,
        "source": "CANON_RI_COMPLETE_WORLD.md N18; CANON_RI_CHARACTER_DECISIONS.md CD-23"
    },
    {
        "id": "N31",
        "decisionProfileId": "CD-24",
        "name": "Zhou Yi",
        "nameCn": "周逸",
        "role": "ally",
        "faction": "originally his sect; defected",
        "peakRealm": "Wending realm (soul transformed into sword spirit)",
        "status": "Becomes sword spirit of Rain Immortal Sword; reincarnated with Qing Shuang on IAC.",
        "goals": {
            "primary": "Protect Qing Shuang / 'Ting'er' (obsessive devotion).",
            "secondary": "Achieve the Obsession Concept (achieved — Wending realm)."
        },
        "needs": [
            "Qing Shuang's safety (even her corpse — he protected it)",
            "The Obsession Concept's perfection"
        ],
        "resources": [
            "Wending realm cultivation (soul transformed into sword spirit)",
            "The Obsession Concept (his defining power)",
            "The Wending Crystal (gave to Wang Lin)"
        ],
        "fears": [
            "Qing Shuang's corpse being disturbed (he defected from his sect to protect it)",
            "His obsession being broken"
        ],
        "knowledge": {
            "knows": "The Obsession Concept. Qing Shuang's location. The Rain Immortal Realm."
        },
        "triggerConditions": [
            "Qing Shuang / Ting'er is threatened → maximum-force response (he burned his primordial spirit in defense)",
            "Someone can protect Ting'er → delegate (he gave his Wending Crystal to Wang Lin, asking Wang Lin to protect Ting'er)"
        ],
        "knownActions": [
            "Found Qing Shuang's corpse in Rain Immortal Realm and called her 'Ting'er' (necrophiliac reputation)",
            "Defected from his sect to protect her",
            "Multiple Rain Immortal Realm entries for Immortal Jades",
            "Burned his primordial spirit in defense — reached Wending realm",
            "Gave his Wending Crystal to Wang Lin (asking Wang Lin to protect Ting'er)",
            "Ting'er's remnant soul awakened and transformed Zhou Yi's primordial spirit into the sword spirit of the Rain Immortal Sword",
            "Reincarnates on IAC with Qing Shuang"
        ],
        "decisionStyle": ["protective_loyalist"],
        "decisionStyleNotes": "His entire existence is defined by obsessive protection of Qing Shuang / Ting'er.",
        "relationships": [
            {"target": "Qing Shuang / Ting'er", "relation": "Obsessive devotion. All decisions serve her protection."},
            {"target": "Wang Lin", "relation": "Delegated protector (gave him the Wending Crystal)."}
        ],
        "canonConfidence": 5,
        "source": "CANON_RI_COMPLETE_WORLD.md N31; CANON_RI_CHARACTER_DECISIONS.md CD-24"
    },
    {
        "id": "N39",
        "decisionProfileId": "CD-25",
        "name": "Ling Tianhou",
        "nameCn": "凌天候",
        "altNameVariants": ["灵天侯 — task brief variant (typo, different role described as 'formation mentor')"],
        "role": "ally / rival-mentor",
        "faction": "Da Lou Sword Sect — elder / sect master",
        "peakRealm": "Third Step (Nirvana Void)",
        "status": "Alive.",
        "goals": {
            "primary": "Maintain the Da Lou Sword Sect's power.",
            "secondary": "Test and ally with worthy cultivators (Wang Lin)."
        },
        "needs": [
            "The Da Lou Sword Sect's stability",
            "Strong allies (Wang Lin)"
        ],
        "resources": [
            "Third Step cultivation (Nirvana Void)",
            "The Da Lou Sword Sect",
            "Sword techniques (challenged Wang Lin to take 3 sword strikes)"
        ],
        "fears": [
            "The Da Lou Sword Sect declining",
            "The All-Seer's schemes (he helped defend Wang Lin against All-Seer)"
        ],
        "knowledge": {
            "knows": "The Da Lou Sword Sect's techniques. The Heavenly Fate Sect's political landscape. Wang Lin's capability (after testing him with 3 sword strikes)."
        },
        "triggerConditions": [
            "A worthy cultivator appears → test (3 sword strikes) and potentially ally",
            "The Da Lou Sword Sect is threatened → defensive response",
            "An ally is threatened by a mutual enemy → coordinated defense (he helped defend Wang Lin against All-Seer)"
        ],
        "knownActions": [
            "Challenged Wang Lin to take 3 sword strikes (Wang Lin survived all 3)",
            "Helped defend Wang Lin against All-Seer",
            "Allied during the East Demon Spirit Sea arc",
            "Helped with the void Moongazer Serpent incident"
        ],
        "decisionStyle": ["cautious_conservative", "reactive_opportunist"],
        "decisionStyleNotes": "He tests before allying. He defends when threatened. He seizes opportunities (East Demon Spirit Sea arc).",
        "relationships": [
            {"target": "Wang Lin", "relation": "Ally (after the 3-sword-strike test). Mutual aid."},
            {"target": "All-Seer", "relation": "Adversary (helped defend Wang Lin against him)."},
            {"target": "Bai Wei", "relation": "Associated (she was caught in All-Seer's plot)."}
        ],
        "canonConfidence": 4,
        "source": "CANON_RI_COMPLETE_WORLD.md N39; CANON_RI_CHARACTER_DECISIONS.md CD-25"
    },
    {
        "id": "N61",
        "decisionProfileId": "CD-26",
        "name": "Lian Daofei",
        "nameCn": "连道非",
        "role": "ally / disciple",
        "faction": "Xiangang Continent; brother of Lian Daozhen",
        "peakRealm": "Eight Extremities Great Heavenly Venerable (new Immortal Emperor)",
        "status": "Alive; new Immortal Emperor.",
        "goals": {
            "primary": "Fuse supreme Immortal bloodline into a worthy vessel (Wang Lin — achieved).",
            "secondary": "Inherit the Eight Extremities Great Heavenly Venerable title (achieved — after Lian Daozhen's failure).",
            "tertiary": "Suppress beast souls of various provinces (ongoing)."
        },
        "needs": [
            "A worthy vessel for his bloodline (Wang Lin)",
            "The Eight Extremities inheritance",
            "Provinces to protect from beast souls"
        ],
        "resources": [
            "Supreme Immortal bloodline (fused into Wang Lin)",
            "Eight Extremities Great Heavenly Venerable cultivation (after awakening)",
            "Gravitation Art (taught to Wang Lin — wait, Wang Lin taught HIM Gravitation Art)"
        ],
        "fears": [
            "His bloodline failing to take (it succeeded — Wang Lin received the Indestructible Immortal Body)",
            "His brother Lian Daozhen's schemes (Lian Daozhen is Wang Lin's enemy)"
        ],
        "knowledge": {
            "knows": "The supreme Immortal bloodline. The Indestructible Immortal Body. The Eight Extremities inheritance. The Lian family's internal politics (his brother is an antagonist)."
        },
        "triggerConditions": [
            "A worthy vessel for his bloodline appears → fuse bloodline (he did this inside the Nether Beast with Wang Lin)",
            "His brother threatens his allies → oppose (complex family dynamics)",
            "The provinces are threatened by beast souls → suppress them (ongoing duty)"
        ],
        "knownActions": [
            "Inside the Nether Beast, fused his supreme Immortal bloodline into Wang Lin's body, imparting the Indestructible Immortal Body",
            "Sparred with Wang Lin",
            "Wang Lin made him a disciple (taught him Gravitation Art)",
            "Rescued by Lian Daozhen on the IAC",
            "After Lian Daozhen failed to inherit the Immortal Ancestor's plan, Lian Daofei's bloodline awakened → inherited Eight Extremities Great Heavenly Venerable title → new Immortal Emperor",
            "Continues suppressing beast souls of various provinces"
        ],
        "decisionStyle": ["protective_loyalist", "curious_explorer"],
        "decisionStyleNotes": "He is loyal to Wang Lin (his 'master' in a reversal — Wang Lin taught him Gravitation Art). He explores his bloodline's potential.",
        "relationships": [
            {"target": "Wang Lin", "relation": "Reciprocal (bloodline donor + disciple). Complex — Wang Lin is both his 'master' (taught him Gravitation Art) and his 'vessel' (received his bloodline)."},
            {"target": "Lian Daozhen", "relation": "Brother (adversarial — Lian Daozhen is Wang Lin's enemy)."}
        ],
        "inheritanceOffered": [
            {"name": "Indestructible Immortal Body / supreme Immortal bloodline", "type": "Unique (one-time fusion)", "notes": "Wang Lin received it. The player could receive a similar bloodline IF they find Lian Daofei and he deems them worthy."}
        ],
        "canonConfidence": 5,
        "source": "CANON_RI_COMPLETE_WORLD.md N61; CANON_RI_CHARACTER_DECISIONS.md CD-26"
    },
    {
        "id": "N63",
        "decisionProfileId": "CD-27",
        "name": "Tuo Sen",
        "nameCn": "拓山",
        "role": "rival → ally",
        "faction": "Ancient Clan (Tu Si's demonic thought)",
        "peakRealm": "8-Star Ancient God (potential)",
        "status": "Alive; reconciled.",
        "goals": {
            "primaryInitial": "Claim Tu Si's 'knowledge' inheritance (Wang Lin had it). Hunt Wang Lin.",
            "primaryAfterReconciliation": "Reincarnate and cultivate (Wang Lin helped him)."
        },
        "needs": {
            "initial": "Tu Si's knowledge inheritance (to complete his power)",
            "afterReconciliation": "A new body / reincarnation (Wang Lin helped)"
        },
        "resources": [
            "8-Star Ancient God potential (Tu Si's 'power' inheritance)",
            "Trapped in Tu Si's body for 1000+ years",
            "Demonic thought nature (aggressive, powerful)"
        ],
        "fears": [
            "Being trapped forever (he was trapped for 1000+ years)",
            "Wang Lin claiming both inheritances (Wang Lin did claim the knowledge inheritance)"
        ],
        "knowledge": {
            "knows": "Tu Si's 'power' inheritance. The Land of the Ancient God's interior. Wang Lin's location (he hunted him)."
        },
        "triggerConditions": [
            "The 'knowledge' inheritance is claimed by another → hunt them (he hunted Wang Lin)",
            "He is freed from Tu Si's body → pursue the knowledge inheritance holder",
            "The knowledge inheritance is returned to him → reconcile (Wang Lin returned it after obtaining the Dao Ancient inheritance)"
        ],
        "knownActions": [
            "Born from Tu Si's failed Ink Flow Split Soul Technique — got Tu Si's 'power' inheritance",
            "Hunted Wang Lin (who had Tu Si's 'knowledge' inheritance)",
            "Trapped in Tu Si's body 1000+ years",
            "Schemed against by Wang Lin (borrowed-knife kill via Corpse Yin Sect + Allheaven)",
            "Injured by Wang Lin's Ji Realm",
            "After Wang Lin obtained Dao Ancient inheritance, Wang Lin returned the memory inheritance",
            "Reincarnated on a planet formed from Ye Mo's left eye; received Wang Lin's help"
        ],
        "decisionStyle": ["aggressive_expansionist", "cautious_conservative"],
        "decisionStyleNotes": "He hunted aggressively until the inheritance was returned, then reconciled.",
        "relationships": [
            {"target": "Wang Lin", "relation": "Rival → ally (after the memory inheritance was returned). Complex — they are technically co-inheritors of Tu Si's legacy."},
            {"target": "Tu Si", "relation": "'Father' / origin. Tuo Sen is his demonic thought."}
        ],
        "canonConfidence": 5,
        "source": "CANON_RI_COMPLETE_WORLD.md N63; CANON_RI_CHARACTER_DECISIONS.md CD-27"
    },

    # ==================== Part 6 — Faction Leader NPCs ====================
    {
        "id": "N25",
        "decisionProfileId": "CD-28",
        "name": "Lu Yun / Huang Long Zhenren",
        "nameCn": "陆云 / 黄龙真人",
        "role": "mentor / secret benefactor / sect master",
        "faction": "Four Divine Sect / Vermilion Bird Divine Sect / Heng Yue Sect (secret identity: Huang Long Zhenren, Heng Yue Sect master)",
        "peakRealm": "Void Flame Cultivator (5th-Gen Vermilion Bird Divine Emperor)",
        "status": "Dead (passed away after passing on the lineage).",
        "goals": {
            "primary": "Pass on the Vermilion Bird lineage to the next generation (Wang Lin — achieved).",
            "secondary": "Gather intelligence on Qing Shui (he infiltrated the Cultivation Alliance HQ).",
            "tertiary": "Maintain the Heng Yue Sect as a cover identity."
        },
        "needs": [
            "A worthy Vermilion Bird successor (Wang Lin)",
            "The Vermilion Bird Sequence (transmitted to Wang Lin)",
            "The Heng Yue Sect's stability (as cover)"
        ],
        "resources": [
            "5th-Gen Vermilion Bird Divine Emperor cultivation",
            "The Vermilion Bird Sequence",
            "The Heng Yue Sect (as secret master)",
            "The Vermilion Bird Nine Mysterious Transformations (taught to Wang Lin)"
        ],
        "fears": [
            "The Vermilion Bird lineage dying out (he endured the Heavenly Decay Tribulation awaiting the next generation)",
            "His secret identity being discovered (it would compromise the Heng Yue Sect)"
        ],
        "knowledge": {
            "knows": "The Vermilion Bird lineage's full history. The Heng Yue Sect's true nature (a front for the Vermilion Bird lineage). Qing Shui's location (he infiltrated the Cultivation Alliance HQ to gather intel)."
        },
        "triggerConditions": [
            "A worthy Vermilion Bird successor appears in the Heng Yue Sect → transmit the Vermilion Bird Sequence (he did this for Wang Lin)",
            "The Heavenly Decay Tribulation approaches → endure it (he did)",
            "Intel on Qing Shui is needed → infiltrate (he did)"
        ],
        "knownActions": [
            "Infiltrated the Cultivation Alliance HQ to gather info on Qing Shui",
            "Endured the Heavenly Decay Tribulation awaiting the next generation's Vermilion Bird",
            "Taught Wang Lin the Vermilion Bird Nine Mysterious Transformations",
            "Gave Wang Lin the Vermilion Bird Sequence",
            "Died after returning from the Cultivation Alliance"
        ],
        "decisionStyle": ["patient_planner", "protective_loyalist"],
        "decisionStyleNotes": "He waited generations for Wang Lin. He maintained a secret identity for decades.",
        "relationships": [
            {"target": "Wang Lin", "relation": "Secret benefactor. Transmitted the Vermilion Bird Sequence."},
            {"target": "Qing Shui", "relation": "Intelligence target (he infiltrated the Cultivation Alliance to find Qing Shui)."},
            {"target": "Heng Yue Sect", "relation": "Cover identity. Protected."}
        ],
        "inheritanceOffered": [
            {"name": "Vermilion Bird Sequence", "type": "Unique (one inheritor per generation)", "notes": "Wang Lin received it. At the edge of canon, Lu Yun is dead — the sequence has been passed to Wang Lin."},
            {"name": "Vermilion Bird Nine Mysterious Transformations", "type": "Unique (Vermilion Bird lineage)"}
        ],
        "canonConfidence": 4,
        "source": "CANON_RI_COMPLETE_WORLD.md N25; CANON_RI_CHARACTER_DECISIONS.md CD-28"
    },
    {
        "id": "N115",
        "decisionProfileId": "CD-29",
        "name": "Old Man Miesheng / Old Man Samsara-Extinction",
        "nameCn": "灭生老人",
        "role": "complex antagonist",
        "faction": "independent",
        "peakRealm": "peak Third Step+",
        "status": "Alive.",
        "goals": "Unknown (canon-ambiguous). He lent the Realm-Defining Compass to Lu Mo, which released Tianyunzi. His motivations are unclear — possibly curious observation, possibly scheming.",
        "needs": "Unknown",
        "resources": [
            "Peak Third Step+ cultivation",
            "The Realm-Defining Compass (he owned it and lent it to Lu Mo)",
            "Independent power base"
        ],
        "fears": "Unknown",
        "knowledge": {
            "knows": "The Realm-Defining Compass's nature. Tianyunzi's nature (artifact spirit). Possibly the full Cave World structure (he is powerful enough to lend cosmic-tier artifacts)."
        },
        "triggerConditions": [
            "Unknown — he acts on his own obscure criteria",
            "A cultivator borrows his artifacts → he lends them (he lent the Compass to Lu Mo)"
        ],
        "knownActions": [
            "Lent the Realm-Defining Compass to Lu Mo",
            "Lu Mo blasted it open using Dream Dao, releasing Tianyunzi (the artifact spirit)"
        ],
        "decisionStyle": ["curious_explorer"],
        "decisionStyleNotes": "He lends artifacts to cultivators and observes the outcomes. He does not intervene directly. (Inferred — canon-ambiguous.)",
        "relationships": [
            {"target": "Lu Mo", "relation": "Lent him the Compass. Observer relationship."},
            {"target": "Tianyunzi", "relation": "Indirect connection (the Compass's spirit was released by Lu Mo's use of the borrowed artifact)."}
        ],
        "canonConfidence": 4,
        "source": "CANON_RI_COMPLETE_WORLD.md N115; CANON_RI_CHARACTER_DECISIONS.md CD-29"
    },
    {
        "id": "N21-7divisions",
        "decisionProfileId": "CD-30",
        "name": "The All-Seer's 7 Color-Division Disciples",
        "nameCn": "七色堂弟子",
        "altNpcIds": ["N113 (Tianyunzi, purple division — separate entry above)", "plus the other 6 color-division leaders (red/orange/yellow/green/blue/cyan)"],
        "role": "antagonists (subordinates of All-Seer)",
        "faction": "Heavenly Fate Sect — 7 color divisions",
        "peakRealm": "Various (Third-Step tier)",
        "status": "Most killed or scattered after the All-Seer's death.",
        "goals": {
            "primary": "Serve the All-Seer (they are his tools).",
            "secondary": "Advance within their respective color divisions."
        },
        "needs": [
            "The All-Seer's favor",
            "Their division's power and resources"
        ],
        "resources": [
            "Their respective color divisions (red/orange/yellow/green/blue/cyan/purple)",
            "Third-Step-tier cultivation (varies)",
            "The Heavenly Fate Sect's infrastructure"
        ],
        "fears": [
            "The All-Seer's displeasure",
            "Their division declining relative to others",
            "The All-Seer's possession plot being discovered (they are tools, not conspirators — they may not know the full plan)"
        ],
        "knowledge": {
            "knows": "Their respective division's techniques. The Heavenly Fate Sect's internal politics.",
            "doesNotKnowLikely": "The All-Seer's full possession plot. The true nature of Tianyunzi (artifact spirit)."
        },
        "triggerConditions": [
            "The All-Seer commands → execute",
            "Their division is threatened → defensive response",
            "A cultivator threatens the Heavenly Fate Sect → coordinated defense"
        ],
        "knownActions": [
            "Yao Xinghai (Blood Ancestor) was the Red Division leader (separate entry — CD-07)",
            "The other 6 divisions are less individually documented",
            "After the All-Seer's death, the divisions scattered"
        ],
        "decisionStyle": ["cautious_conservative"],
        "decisionStyleNotes": "As subordinates, they follow the All-Seer's commands. They do not act independently (with the exception of Yao Xinghai, who had his own agenda).",
        "relationships": [
            {"target": "All-Seer", "relation": "Master. Loyal (mostly)."},
            {"target": "Each other", "relation": "Rival divisions (competing for the All-Seer's favor)."},
            {"target": "Wang Lin", "relation": "Enemy (as the All-Seer's target, they were positioned against him, though some may not have understood why)."}
        ],
        "canonConfidence": 4,
        "source": "CANON_RI_COMPLETE_WORLD.md N21; CANON_RI_CIVILIZATION.md CIV-08 (CIV-06 in canon doc); CANON_RI_CHARACTER_DECISIONS.md CD-30"
    },

    # ==================== Part 7 — Reincarnation-Linked NPCs ====================
    {
        "id": "N07",
        "decisionProfileId": "CD-31",
        "name": "Wang Ping",
        "nameCn": "王平",
        "role": "family",
        "faction": "none (mortal, intentionally raised as mortal by Wang Lin)",
        "peakRealm": "mortal (intentionally)",
        "status": "Dies (mortal lifespan); reincarnated; reunited with Wang Lin.",
        "goals": {
            "primary": "Live a simple mortal life (Wang Lin raised him this way intentionally).",
            "secondary": "Be with his father (achieved after reincarnation)."
        },
        "needs": [
            "A peaceful mortal existence",
            "His father's presence"
        ],
        "resources": "None (intentionally mortal)",
        "fears": "Unknown (he was raised as a mortal; his fears would be mortal-scale)",
        "knowledge": {
            "knows": "Only mortal-scale knowledge (Wang Lin intentionally kept him ignorant of cultivation)",
            "doesNotKnow": "Wang Lin's true nature, cultivation, or the cosmic-tier events happening around him"
        },
        "triggerConditions": [
            "His father visits → respond as a mortal child would",
            "His mortal lifespan ends → death (which triggered Wang Lin's Karma Domain evolution)"
        ],
        "knownActions": [
            "Raised in a desolate village by Wang Lin as a mortal",
            "His death triggers Wang Lin's Karma Domain evolution",
            "Reincarnates; eventually reunited with Wang Lin"
        ],
        "decisionStyle": "N/A (mortal — no cultivation-scale decision-making)",
        "relationships": [
            {"target": "Wang Lin", "relation": "Father. The emotional core of Wang Lin's Karma Domain evolution."},
            {"target": "Liu Mei / Mu Bingmei", "relation": "Mother (who refined him into a resentful spirit out of hatred for Wang Lin)."}
        ],
        "canonConfidence": 5,
        "source": "CANON_RI_COMPLETE_WORLD.md N07; CANON_RI_CHARACTER_DECISIONS.md CD-31"
    },
    {
        "id": "N08",
        "decisionProfileId": "CD-32",
        "name": "Wang Yiyi",
        "nameCn": "王一一",
        "role": "family",
        "faction": "Vast Dao Palace (Saintess) → mask spirit (in AWWP)",
        "peakRealm": "Paragon-tier (in AWWP, Saintess of the Vast Dao Palace)",
        "status": "Alive (married to Wang Baole in AWWP); eventually brought back to the Xiangang Continent by Wang Lin.",
        "goals": {
            "primary": "Be with Wang Baole (her husband in AWWP).",
            "secondary": "Escape the destruction of the Vast Dao Palace (achieved — she inhabited a mask)."
        },
        "needs": [
            "Wang Baole's presence",
            "The mask (her vessel after escaping the Vast Dao Palace)"
        ],
        "resources": [
            "Paragon-tier cultivation (in AWWP)",
            "The mask (her vessel)",
            "Experiences from dozens of reincarnations during the Weiyang Boundary"
        ],
        "fears": [
            "The Vast Dao Palace's destruction (it happened — she escaped via the mask)",
            "Separation from Wang Baole"
        ],
        "knowledge": {
            "knows": "The Vast Dao Palace's history. The Weiyang Boundary reincarnations. Wang Baole's nature."
        },
        "triggerConditions": [
            "The Vast Dao Palace is destroyed → escape (she did, via the mask)",
            "Wang Baole is threatened → protective response",
            "Her father (Wang Lin) calls → respond (Wang Lin eventually brought her back to the Xiangang Continent)"
        ],
        "knownActions": [
            "Escaped the destruction of the Vast Dao Palace by inhabiting a mask",
            "Accompanied Wang Baole (her future husband) in AWWP",
            "Experiences dozens of reincarnations during the Weiyang Boundary",
            "Eventually brought back to the Xiangang Continent by Wang Lin"
        ],
        "decisionStyle": ["protective_loyalist"],
        "decisionStyleNotes": "She is loyal to Wang Baole and her father.",
        "relationships": [
            {"target": "Wang Lin", "relation": "Father."},
            {"target": "Li Muwan", "relation": "Mother."},
            {"target": "Wang Baole", "relation": "Husband (AWWP protagonist)."}
        ],
        "canonConfidence": 5,
        "source": "CANON_RI_COMPLETE_WORLD.md N08; CANON_RI_CHARACTER_DECISIONS.md CD-32"
    }
]


def build_output():
    return {
        "metadata": {
            "source": "Renegade Immortal (仙逆) by Er Gen",
            "extractedFrom": "CANON_RI_CHARACTER_DECISIONS.md (1,953 lines, 32 decision profiles CD-01..CD-32)",
            "version": "1.0",
            "generated": "2026-07-15",
            "task": "EXTRACT-CIV-NPC Part C — Enrich existing characters with decision data",
            "totalEnrichedCharacters": len(CHARACTERS),
            "decisionStyleTaxonomy": DECISION_STYLES,
            "architecturalRule": "The world exists first. NPCs live in it. History progresses. The player appears as one influence among many. NPCs do NOT center the player — they pursue their own goals. The player matters to an NPC only when the player enters that NPC's awareness sphere or becomes relevant to that NPC's goal.",
            "metaRule": "Do not ask design questions that canon already answers. Reconstruct faithfully. Extend only where canon is silent. Infer the minimum necessary. Never replace, simplify, or remove canon because it is easier to implement.",
            "canonConfidenceKey": {
                "C5": "explicit, multi-source, chapter-cited",
                "C4": "directly named, single-source but consistent",
                "C3": "novel-implicit; named once or implied",
                "C2": "archetypal inference (used sparingly, only where canon is silent and the simulation requires a value)"
            },
            "tierBreakdown": {
                "cosmicTierNPCs": "3 (CD-01 to CD-03): Seven-Colored Daoist, Wang Lin, Palm Venerable",
                "mortalSchemeNPCs": "4 (CD-04 to CD-07): All-Seer, Tianyunzi, Daoist Water, Blood Ancestor",
                "localThreatNPCs": "6 (CD-08 to CD-13): Teng Huayuan, Yao Xixue, Master Void, Lian Daozhen, Dao Devil Sect Master, Gu Dao",
                "mentorNPCs": "8 (CD-14 to CD-21): Situ Nan, Tu Si, Du Tian, Bai Fan, Xuan Luo, Dao Master Blue Dream, 2nd-Gen Vermilion Bird, Qing Shui",
                "allyNPCs": "6 (CD-22 to CD-27): Li Muwan, Li Qianmei, Zhou Yi, Ling Tianhou, Lian Daofei, Tuo Sen",
                "factionLeaderNPCs": "3 (CD-28 to CD-30): Lu Yun/Huang Long Zhenren, Old Man Miesheng, All-Seer's 7 Color-Division Disciples",
                "reincarnationLinkedNPCs": "2 (CD-31 to CD-32): Wang Ping, Wang Yiyi"
            },
            "edgeOfCanonState": {
                "wangLin": "Alive; Transcendent (4th Step / Heaven Trampling); Cave World owner; 14 Essences + 9 Heaven Trampling Bridges complete",
                "sevenColoredDaoist": "DEAD (killed by Wang Lin); Wang Lin is new Cave World owner",
                "realmSealingGrandArray": "DISSOLVED (Wang Lin dissolved it after killing the Daoist)",
                "heavenSplittingAxe": "Cooperative (cooperated with Wang Lin due to his Restriction Essence)",
                "sealedRealmBoundary": "OPEN (seal is gone)",
                "caveWorldToIAC": "OPEN (Wang Lin went to IAC and became the 10th Sun)",
                "keyNPCStates": {
                    "Li Muwan": "Alive; resurrected; transcendent with Wang Lin",
                    "Situ Nan": "Alive; fulfilled (became prince on IAC as 'Si Nan')",
                    "All-Seer": "DEAD (killed by Wang Lin)",
                    "Seven-Colored Daoist": "DEAD (killed by Wang Lin)",
                    "Teng Huayuan": "DEAD (killed by Wang Lin; soul refined)",
                    "Daoist Water": "DEAD (killed by Wang Lin)",
                    "Blood Ancestor": "DEAD (killed by Wang Lin)",
                    "Yao Xixue": "Alive; amnesiac; wandering with father's remnant soul",
                    "Qing Shui": "Alive; Third Step (reincarnated on IAC)",
                    "Xuan Luo": "Alive; undergoing reincarnation (Wang Lin gifted him a protective jade slip)",
                    "Tuo Sen": "Alive; reconciled (reincarnated, received Wang Lin's help)",
                    "Gu Dao": "Alive; acknowledges Wang Lin as superior",
                    "Lian Daofei": "Alive; new Immortal Emperor (Eight Extremities)",
                    "Zhou Yi + Qing Shuang": "Alive; reincarnated on IAC",
                    "Hong Die": "Alive; reincarnated as Qing Hong on IAC (no past-life memories)"
                }
            }
        },
        "characters": CHARACTERS
    }


def main():
    output = build_output()
    with open(OUT_PATH, "w", encoding="utf-8") as f:
        json.dump(output, f, indent=2, ensure_ascii=False)
    print(f"Wrote {OUT_PATH}")
    print(f"  Total enriched characters: {output['metadata']['totalEnrichedCharacters']}")
    # Distribution
    from collections import Counter
    conf = Counter(c["canonConfidence"] for c in CHARACTERS)
    print(f"  Canon confidence distribution: {dict(conf)}")
    print(f"  File size: {OUT_PATH.stat().st_size:,} bytes")


if __name__ == "__main__":
    main()
