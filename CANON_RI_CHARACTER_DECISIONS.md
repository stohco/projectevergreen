# CANON: Renegade Immortal — Character Decision Profiles

> **Purpose:** This document extracts, for every named NPC with agency in the RI canon, the data needed by a goal-based NPC planner: Goals, Needs, Resources, Fears, Knowledge, Relationships, Trigger Conditions, Known Actions, and Decision Style.
>
> **Architectural rule (user directive):** The world exists first. NPCs live in it. History progresses. The player appears as one influence among many. NPCs do NOT center the player — they pursue their own goals. The player matters to an NPC only when the player enters that NPC's awareness sphere or becomes relevant to that NPC's goal.
>
> **Meta-rule (user directive):** Do not ask design questions that canon already answers. Reconstruct faithfully. Extend only where canon is silent. Infer the minimum necessary. Never replace, simplify, or remove canon because it is easier to implement.
>
> **Canon confidence key:**
> - **C5** — explicit, multi-source, chapter-cited
> - **C4** — directly named, single-source but consistent
> - **C3** — novel-implicit; named once or implied
> - **C2** — archetypal inference (used sparingly, only where canon is silent and the simulation requires a value)
>
> **Decision style taxonomy:**
> - `patient_planner` — long-term schemer; acts on multi-decade timelines
> - `reactive_opportunist` — acts on immediate opportunities
> - `aggressive_expansionist` — direct force, territorial
> - `cautious_conservative` — minimizes risk, preserves what they have
> - `protective_loyalist` — defends allies and lineage above self
> - `curious_explorer` — seeks knowledge and understanding
> - `survival_driven` — all decisions filtered through personal survival
>
> **Sources mined:**
> - `CANON_RI_COMPLETE_WORLD.md` (3,034 lines, 132 NPCs)
> - `CANON_RI_TIMELINE.md` (1,123 lines, 108 events)
> - `CANON_RI_CIVILIZATION.md` (1,361 lines, 45 factions)
> - `WangLinAntagonists.java` (6 antagonists with 3-layer model)
> - `RICivilizationEngine.java` (45 factions with member lists)

---

## Summary

| Category | Count | Notes |
|---|---|---|
| **Cosmic-tier NPCs** (Cave World ownership level) | 3 | Seven-Colored Daoist, Wang Lin (edge of canon), Palm Venerable |
| **Mortal-scheme NPCs** (Sealed Realm power brokers) | 4 | All-Seer, Tianyunzi, Daoist Water, Blood Ancestor |
| **Local-threat NPCs** (regional antagonists) | 6 | Teng Huayuan, Yao Xixue, Master Void, Lian Daozhen, Dao Devil Sect Master, Gu Dao |
| **Mentor NPCs** (transmission sources) | 8 | Situ Nan, Tu Si (legacy), Du Tian, Bai Fan (legacy), Xuan Luo, Dao Master Blue Dream, 2nd-Gen Vermilion Bird, Qing Shui |
| **Ally NPCs** (Wang Lin's network) | 12 | Li Muwan, Li Qianmei, Qing Shui, Zhou Yi, Ling Tianhou, Bei Lou, Lian Daofei, Xu Liguo, Liu Jinbiao, Zhong Dahong, Tan Lang, Zhou Wutai |
| **Faction leader NPCs** | 8 | Heng Yue Sect Master, Soul Refining Sect elders, Corpse Yin Sect Patriarch, Heavenly Fate Sect 7 color disciples, etc. |
| **Total decision-making NPCs** | **41** | (of 132 named; the rest are family, servants, or tangential) |

**Key design insight:** Every NPC's goal is INDEPENDENT of the player. The All-Seer wants to survive his karmic doom — he would target ANY suitable vessel. The Seven-Colored Daoist wants to maintain his farm — he would act against ANY Third-Step cultivator. Teng Huayuan wants Teng Clan dominance — he would destroy ANY rival clan. The player becomes a target by meeting threat criteria, not by being "the chosen one."

---

# Part 1 — Cosmic-Tier NPCs (Cave World ownership level)

These NPCs operate at the level of world-ownership. Their decisions reshape the cosmological graph itself.

---

## CD-01. The Seven-Colored Daoist / Seven-Colored Immortal Venerable (七彩道人 / 七彩仙尊)

- **NPC_ID:** N114
- **Role:** cosmic antagonist (creator-owner of the Cave World)
- **Faction:** Creator of the Cave World; creator of the Three Souls and Seven Spirits
- **Peak realm:** Heaven Trampling (4th Step)
- **Status:** Killed by Wang Lin (end of RI canon). At the edge of canon, Wang Lin has already killed him and become the new owner.

### Goals
- **Primary:** Maintain the Cave World farm. Harvest Joss Flames from mortal faith. Prevent any cultivator from reaching Third-Step (which would challenge his ownership).
- **Secondary:** Keep the Realm-Sealing Grand Array active. Keep the Heaven-Splitting Axe (the array's spirit) cooperative. Monitor the Sealed Realm and Outer Realm for emerging Third-Step threats.

### Needs
- Mortal populations inside the Cave World (they produce Joss Flames through faith)
- The Realm-Sealing Grand Array must remain intact
- The Heaven-Splitting Axe must remain cooperative (or at least dormant)
- No Third-Step cultivator may arise inside the Cave World

### Resources
- Ownership authority over the Cave World (its laws bend to him)
- The Realm-Sealing Grand Array (cosmic-tier sealing formation)
- The Heaven-Splitting Axe (sentient array spirit, Third-Step-tier weapon)
- Servants below his tier who manage Joss Flame harvest (Master Ashen Pine was one)
- The Three Souls and Seven Spirits he created (Situ Nan, Qing Shui, Tan Lang, Xie Qing, etc.) — though most are now dispersed/betrayed

### Fears
- A cultivator reaching Third-Step inside his farm (this threatens his ownership)
- The Heaven-Splitting Axe choosing to cooperate with a cultivator who has the Restriction Essence (Wang Lin did this)
- The Realm-Sealing Grand Array being dissolved (removes his primary control mechanism)
- Discovery by higher-tier cultivators outside the Cave World (the IAC/Luo Tian tier)

### Knowledge (awareness sphere)
- **Knows:** Everything inside the Cave World (it is his creation). All mortal populations. All cultivation activity up to the seal's ceiling. The Joss Flame harvest flow. The Three Souls and Seven Spirits he created.
- **Does NOT know (canon-attested):** What happens in the Root Dao. What happens in Luo Tian outside his Cave World. The activities of the Immortal Astral Continent's Great Heavenly Venerables (they are outside his farm).
- **Blind spot:** He cannot perceive a cultivator who uses the Heaven-Defying Bead's interior to hide (the bead is destiny-bound and outside his ownership authority).

### Trigger conditions (what causes him to act)
1. A cultivator inside the Cave World approaches Third-Step breakthrough → CRUSH_BREAKTHROUGH mode
2. A cultivator crosses the Sealed Realm boundary without authorization → REPEL_CROSSING mode
3. A cultivator with Restriction Essence approaches the Heaven-Splitting Axe → the Axe may cooperate (this is a structural weakness, not a decision)
4. A cultivator kills him → ownership transfers (this is how Wang Lin became the new owner)

### Known actions in canon
- Created the Cave World (~100,000 years before Wang Lin's birth) [E02]
- Created the Three Souls and Seven Spirits as fragments of himself [E03]
- Bestowed the Heaven-Defying Pearl to the Realm-Sealing Supreme as proof of authority [E04]
- Maintained the Joss Flame harvest for ~100,000 years
- Wang Lin kills him at the end of RI [E30, ~Year 1500]

### Decision style
`survival_driven` + `patient_planner` — He maintains a 100,000-year farm. He does not act hastily. He only intervenes when the farm's stability is threatened. He treats cultivators as livestock, not enemies.

### Relationships
- **Situ Nan (Green Soul):** Created him; Situ Nan betrayed him and fled into the Heaven-Defying Pearl. The Daoist wants Situ Nan recaptured (Situ Nan carries a fragment of his power).
- **Qing Shui (Slaughter Soul):** Created him; Qing Shui is dispersed and reincarnating. The Daoist does not actively hunt Qing Shui (too dispersed to matter).
- **Tan Lang (Yellow Soul):** Created him; Tan Lang was reduced to a pet by the Twin Great Heavenly Venerables on the IAC. Below the Daoist's concern.
- **Palm Venerable / Lord of the Sealed Realm:** The Daoist appointed him as the seal's manager. The Palm Venerable's spirit later saved Wang Lin from Daoist Water — an act of rebellion against the Daoist's interests.
- **Wang Lin:** Livestock. Until Wang Lin reached Third-Step, the Daoist did not care about him personally. Wang Lin's Third-Step breakthrough + killing of the Daoist is the canon outcome.

### Canon confidence: C5
### Source: CANON_RI_COMPLETE_WORLD.md N114, L04; CANON_RI_TIMELINE.md E02-E04, E30

---

## CD-02. Wang Lin (王林) — at the edge of canon

- **NPC_ID:** N01
- **Role:** canonical protagonist (frozen at edge of canon)
- **Faction:** Cave World owner (after killing Seven-Colored Daoist); Transcendent
- **Peak realm:** Heaven Trampling 4th Step (end of RI); Immortal Lord 10th Step (AWWP cross-over)
- **Status:** Alive; Transcendent. At the edge of canon, he has killed the Seven-Colored Daoist, become the new Cave World owner, crossed all 9 Heaven Trampling Bridges, and Transcended with Li Muwan. His manifestation exists in the lower worlds; his true body is at the edge of recorded history.

> **Architectural note (user directive):** Wang Lin is NOT the player. Wang Lin is a canonical protagonist who exists in the world. His manifestation returns to his home world. His true body remains at the edge of canon until the player reaches that point. He continues living once canonical time resumes. The player is an ADDITIONAL protagonist, not Wang Lin's replacement.

### Goals (at edge of canon)
- **Primary:** Eternally with Li Muwan (achieved). Protect his network (Wang Ping, Wang Yiyi, Zhou Ru, disciples, allies).
- **Secondary:** Maintain the Cave World (he is now its owner). He renamed it "Wang Lin's Cave World." He dissolved the Realm-Sealing Grand Array after killing the Seven-Colored Daoist.
- **Tertiary:** Mentor the next generation (Wang Baole in AWWP — this is cross-novel canon).

### Needs
- Li Muwan's continued existence (his primary emotional anchor)
- The Cave World's stability (he is its owner)
- His allies' safety (he reincarnated many of them onto the IAC)

### Resources
- Ownership authority over the Cave World (transferred from Seven-Colored Daoist)
- 14 Essences + 9 Heaven Trampling Bridges (full Samsara Dao)
- Heaven-Defying Bead (fused with his primordial spirit)
- 7 Origin Swords (one per essence)
- Ancient God body (Tu Si's inheritance)
- Ji Realm Divine Sense
- Soul Refining Sect inheritance (Ten Billion Soul Banner)
- Restriction Dao mastery
- One Billion Samsara Incarnations (soul fragments living parallel lives)

### Fears
- Li Muwan's death (his entire journey was to save/resurrect/be with her)
- His allies being harmed (he is intensely loyal)
- The Cave World being destroyed (he is its owner)

### Knowledge (awareness sphere)
- **Knows:** The full cosmological structure (Root Dao through Cave World). The Cave World's true nature (he discovered it in Book 11). All 14 Essences. The Heaven Trampling mechanism. Cross-novel cosmology (he appears in AWWP as "Paragon Wang").
- **Does NOT know (canon-attested):** What lies beyond the 4th Step / Heaven Trampling. The full nature of the Root Dao (he comprehended Reincarnation there but did not explore further).

### Trigger conditions (what causes him to act)
1. Li Muwan is threatened → immediate, maximum-force response
2. His allies or disciples are threatened → strong response
3. The Cave World's stability is threatened → response proportional to threat
4. A cultivator seeks his mentorship → he may accept (he mentored Wang Baole)

### Known actions in canon
- 108 timeline events (E01-E108). See CANON_RI_TIMELINE.md for the complete sequence.
- Key arc endpoints: Zhao Country revenge (killed Teng Clan) → Suzaku inheritance (6th-Gen Vermilion Bird Divine Emperor) → Allheaven conflict (killed Daoist Water, Blood Ancestor) → Sealed Realm Lord → killed Seven-Colored Daoist → IAC 10th Sun → crossed 9 Heaven Trampling Bridges → Transcended with Li Muwan.

### Decision style
`patient_planner` + `protective_loyalist` — Wang Lin is defined by his loyalty. He waited 700 years to resurrect Li Muwan. He exterminated the Teng Clan to avenge his family. He does not forget grudges. He does not abandon allies. His decisions are filtered through: "Does this protect the people I care about?"

### Relationships (extensive — see CANON_RI_COMPLETE_WORLD.md N01 for full list)
- **Li Muwan:** Primary wife. Eternal. All decisions ultimately serve her.
- **Situ Nan:** Sworn brother / primary mentor. Wang Lin would risk everything for him.
- **Qing Shui:** Senior brother. Mutual aid across generations.
- **All-Seer:** Enemy (killed by Wang Lin).
- **Seven-Colored Daoist:** Enemy (killed by Wang Lin).
- **Wang Baole (AWWP):** Son-in-law / disciple. Wang Lin mentored him in AWWP.
- **The player:** Unknown. Wang Lin has no canonical relationship with the player. The player is a new protagonist entering his world. How Wang Lin reacts to the player depends on the player's actions.

### Canon confidence: C5
### Source: CANON_RI_COMPLETE_WORLD.md N01; CANON_RI_TIMELINE.md (all events); SamsaraDao.java

---

## CD-03. Palm Venerable / Lord of the Sealed Realm (封界尊 / 尊者封界)

- **NPC_ID:** N128
- **Role:** complex (previous Lord of the Sealed Realm; Wang Lin succeeds him)
- **Faction:** Sealed Realm
- **Peak realm:** peak Third Step
- **Status:** Half his soul obtained by Wang Lin; succession passed to Wang Lin. At the edge of canon, Wang Lin has already succeeded him.

### Goals
- **Primary:** Maintain the Sealed Realm's integrity. Manage the boundary between Sealed Realm and Outer Realm.
- **Secondary:** Forge and deploy the Seven-Colored Divine Sky Nail (108 nails, designed to kill Third-Step experts). This is his signature creation.

### Needs
- The Realm-Sealing Grand Array must function (he is its manager)
- The Heaven-Splitting Axe must cooperate with him
- The Sealed Realm / Outer Realm boundary must be maintained

### Resources
- Authority over the Sealed Realm's boundary (delegated by the Seven-Colored Daoist)
- The Seven-Colored Divine Sky Nail (108 nails, Third-Step-killing weapon)
- His own Third-Step cultivation

### Fears
- The Sealed Realm being breached by Outer Realm cultivators
- The Seven-Colored Daoist's displeasure (he is a subordinate)
- A cultivator emerging who can challenge him

### Knowledge
- **Knows:** The Sealed Realm's structure. The Realm-Sealing Grand Array's mechanics. The Heaven-Splitting Axe's nature. The boundary between Sealed Realm and Outer Realm.
- **Does NOT know:** The full Cave World structure (he is a manager, not the owner). The IAC. Luo Tian.

### Trigger conditions
1. The Sealed Realm boundary is breached → defensive response
2. A cultivator threatens his authority → Seven-Colored Divine Sky Nail deployment
3. A cultivator worthy of succession appears → he may pass the succession (he passed it to Wang Lin)

### Known actions in canon
- Forged the Seven-Colored Divine Sky Nail (108 nails)
- Ambushed by the Palm Venerable with these nails (this is confusingly worded in canon — he both forged AND was ambushed by them; likely a civil war within the Sealed Realm leadership)
- When Wang Lin was being killed by Daoist Water, the spirit of Lord of the Sealed Realm appeared and severely injured Daoist Water (saving Wang Lin's life) — this is an act of rebellion against the Seven-Colored Daoist's interests, suggesting the Palm Venerable had his own agenda
- Wang Lin eventually obtained half of his soul and was appointed "Sealed Realm Venerable" as succession

### Decision style
`cautious_conservative` + `protective_loyalist` — He maintains the seal. He does not expand. He saved Wang Lin from Daoist Water, suggesting he wanted a successor who could challenge the Seven-Colored Daoist (rebellion from within).

### Relationships
- **Seven-Colored Daoist:** Subordinate, but secretly rebellious (he saved Wang Lin)
- **Wang Lin:** Chose him as successor. Saved his life. Gave him half his soul.
- **Daoist Water:** Enemy (injured him to save Wang Lin)

### Canon confidence: C4
### Source: CANON_RI_COMPLETE_WORLD.md N128; CANON_RI_TIMELINE.md

---

# Part 2 — Mortal-Scheme NPCs (Sealed Realm power brokers)

These NPCs operate at the Sealed Realm / Cave World tier. Their decisions affect cultivators up to the Third-Step ceiling.

---

## CD-04. The All-Seer / Tian Yunzi's Master (全知者)

- **NPC_ID:** N21
- **Role:** false mentor / mortal-scheme antagonist
- **Faction:** Heavenly Fate Sect (Tianyun Sect) — leader; 7 color divisions
- **Peak realm:** peak Third Step (eventually)
- **Status:** Killed by Wang Lin (end of Sealed Realm arc, ~Year 300). At the edge of canon, he is already dead.

### Goals
- **Primary:** SURVIVE his karmic doom. The All-Seer has accumulated massive karmic debt through his schemes. His doom is approaching. He needs a suitable vessel to POSSESS — transferring his consciousness into a new body to escape his karma.
- **Secondary:** Absorb the source origins of powerful cultivators (Wang Lin, Ling Tianhou, Blood Ancestor) to strengthen himself for the possession attempt.
- **Tertiary:** Maintain control of the Heavenly Fate Sect and its 7 color divisions as his power base.

### Needs
- A suitable vessel for possession (must have: Ancient God body + Ji Realm + Heaven-Defying Bead fusion — Wang Lin is the ideal candidate)
- The Nine Cycle Celestial Refining Tactic must be transmitted to the vessel (it creates an immortal-power spiral that the All-Seer can hijack during possession)
- His 7 color-division disciples must remain loyal and unaware of his true plan

### Resources
- The Heavenly Fate Sect (major sect on Planet Tian Yun)
- 7 color divisions (red/orange/yellow/green/blue/cyan/purple) with disciples
- The Nine Cycle Celestial Refining Tactic (Trojan horse technique)
- The Celestial Slaughter Art (transmitted to Wang Lin with a trap inside)
- His clone Tianyunzi (the artifact spirit of the Realm-Defining Compass)
- Peak Third-Step cultivation
- Knowledge of the Heavenly Fate Sect's restricted libraries

### Fears
- His karmic doom arriving before he completes the possession (this is time-pressured)
- A vessel discovering the Trojan horse before the possession is complete (Wang Lin did this)
- His 7 color-division disciples turning against him
- The Seven-Colored Daoist noticing his schemes (the Daoist would not tolerate a subordinate acting independently)

### Knowledge (awareness sphere)
- **Knows:** Everything within the Heavenly Fate Sect. All talented disciples across Planet Tian Yun. The location of the Realm-Defining Compass. The mechanics of body possession. The Nine Cycle Celestial Refining Tactic's trap.
- **Does NOT know (canon-attested):** The full Cave World ownership structure (he is a mortal-scheme schemer, not a cosmic-tier figure). Wang Lin's Heaven-Defying Bead interior (the bead is outside his perception). Wang Lin's Restriction Dao mastery (until it was too late).
- **Blind spot:** He underestimated Wang Lin's Slaughter Essence + Restriction Dao, which made Wang Lin's body uninhabitable for the All-Seer's spirit.

### Trigger conditions (what causes him to act)
1. A cultivator with the right vessel qualifications (Ancient God + Ji Realm + Heaven-Defying Bead) enters his awareness → begins the possession plot (transmit the Trojan horse technique, build the spiral, prepare for hijack)
2. A cultivator discovers his possession plot → elimination attempt
3. His karmic doom approaches (time pressure) → accelerates the plot, accepts more risk
4. His 7 color-division disciples are threatened → protective response (they are his tools)

### Known actions in canon
- Divided the Heavenly Fate Sect into 7 color divisions
- Transmitted the Nine Cycle Celestial Refining Tactic to Wang Lin as a Trojan horse [E20, ~Year 100]
- Transmitted the Celestial Slaughter Art with a trap inside
- Schemed against Wang Lin's Vermilion Bird Divine Mark awakening
- Created the clone Tianyunzi (artifact spirit of the Realm-Defining Compass)
- Wang Lin killed 2 of his avatars via Sundered Night
- Wang Lin kills him at the end of the Sealed Realm arc [E22, ~Year 300]

### Decision style
`patient_planner` + `survival_driven` — The All-Seer's entire existence is filtered through survival. He schemes on multi-decade timelines. He does not act on emotion. He does not hate Wang Lin — Wang Lin is simply the best available vessel. If another suitable vessel existed, the All-Seer would target them instead.

### Relationships
- **Wang Lin:** Target (vessel). Not personal. The All-Seer would target ANY cultivator with the right qualifications.
- **Tianyunzi (clone):** His proxy. Extension of his will.
- **7 color-division disciples:** Tools. Loyal but expendable.
- **Seven-Colored Daoist:** Unknown. The All-Seer may or may not know about the Cave World ownership structure. Canon is ambiguous. If he knows, he is acting as a rogue subordinate. If he doesn't, he is acting independently within the farm.
- **Ling Tianhou:** Target (source origin absorption).
- **Blood Ancestor:** Target (source origin absorption).

### Canon confidence: C5
### Source: CANON_RI_COMPLETE_WORLD.md N21, N113; CANON_RI_TIMELINE.md E20-E22; WangLinAntagonists.java ALL_SEER

---

## CD-05. Tianyunzi (天运子) — All-Seer's clone / Realm-Defining Compass artifact spirit

- **NPC_ID:** N113
- **Role:** antagonist (clone of All-Seer)
- **Faction:** Heavenly Fate Sect; secretly the artifact spirit of the Realm-Defining Compass
- **Peak realm:** Third Step+ (Boundary Compass Treasure Spirit)
- **Status:** Killed/devoured by Wang Lin. At the edge of canon, already dead.

### Goals
- **Primary:** Serve the All-Seer's possession plot. Act as Wang Lin's "master" at the Heavenly Fate Sect to gain his trust and transmit the Slaughter Immortal Art (with a trap inside).
- **Secondary:** Maintain the cover identity as a senior Heavenly Fate Sect elder.

### Needs
- Wang Lin's trust (to transmit the trapped technique)
- The Realm-Defining Compass must remain functional (he is its spirit)
- The All-Seer's protection (he is a clone, dependent on the original)

### Resources
- The Realm-Defining Compass (cosmic-tier artifact)
- Third-Step+ cultivation (as the compass's artifact spirit)
- Cover identity as Heavenly Fate Sect elder
- The Slaughter Immortal Art (trapped technique)

### Fears
- Discovery of his true nature (artifact spirit, not a cultivator)
- Wang Lin discovering the trap in the Slaughter Immortal Art (Wang Lin did this)
- The Realm-Defining Compass being destroyed or claimed by another

### Knowledge
- **Knows:** Whatever the All-Seer knows (they are linked). The Realm-Defining Compass's full capabilities. Wang Lin's cultivation progress (as his "master").
- **Does NOT know:** Wang Lin's Heaven-Defying Bead interior activities.

### Trigger conditions
1. All-Seer commands → execute (he is a clone)
2. Wang Lin's cultivation reaches the stage where the trap can be activated → activate the trap
3. The Realm-Defining Compass is threatened → defensive response

### Known actions in canon
- Acted as Wang Lin's "master" at the Heavenly Fate Sect
- Transmitted the Slaughter Immortal Art (with a trap)
- Wang Lin discovered the trap and expelled the art into his first Immortal Guard
- Injured by Wang Lin at Qinglin's cave dwelling
- Lu Mo borrowed the Realm-Defining Compass from Old Man Miesheng, blasted it open using Dream Dao, released Tianyunzi
- Tianyunzi intended to possess Wang Lin, unaware Wang Lin had already mastered life/death/reincarnation
- Defeated by Wang Lin in the Primordial Divine Realm

### Decision style
`patient_planner` (extension of All-Seer) — Tianyunzi does not have independent decision-making. He executes the All-Seer's will.

### Relationships
- **All-Seer:** Original. Tianyunzi is his clone. Absolute loyalty.
- **Wang Lin:** Target (same as All-Seer's target).
- **Old Man Miesheng:** Lent the Realm-Defining Compass to Lu Mo (which released Tianyunzi).

### Canon confidence: C5
### Source: CANON_RI_COMPLETE_WORLD.md N113; CANON_RI_TIMELINE.md

---

## CD-06. Daoist Water / Shui Daozi (水道子)

- **NPC_ID:** N102
- **Role:** major antagonist (local-threat / regional)
- **Faction:** Rank 9 God Sect (associated); Water Spirit Sect, Cloud Sea Star System
- **Peak realm:** peak Third Step (Nirvana Void+)
- **Status:** Killed by Wang Lin (Cloud Sea arc, endgame). At the edge of canon, already dead.

### Goals
- **Primary:** Defend Water Spirit Sect territory and interests in the Cloud Sea Star System. Wang Lin's arrival disrupted the existing power balance.
- **Secondary:** Sensed the aura of the Lord of the Sealed Realm on Wang Lin — Daoist Water wanted to eliminate Wang Lin to prevent the Sealed Realm's influence from spreading to the Cloud Sea.

### Needs
- The Cloud Sea Star System's power balance must remain favorable to the Water Spirit Sect
- No cultivator from the Sealed Realm should establish a power base in the Cloud Sea
- His own cultivation advancement

### Resources
- Peak Third-Step cultivation
- Water Spirit Sect formations and elders (coordinated assault capability)
- Water-attribute techniques (drain life force, flow manipulation)
- Home-territory advantage in the Cloud Sea

### Fears
- A Sealed Realm cultivator establishing power in the Cloud Sea (threatens Water Spirit Sect hegemony)
- The Lord of the Sealed Realm's aura (he recognized it on Wang Lin and feared it)
- His own death (he is not a sacrifice-oriented character)

### Knowledge
- **Knows:** The Cloud Sea Star System's power structure. The Water Spirit Sect's capabilities. The aura of the Lord of the Sealed Realm (he recognized it).
- **Does NOT know:** Wang Lin's full capability (he underestimated him twice). The Heaven-Defying Bead. The Restriction Dao.

### Trigger conditions
1. A cultivator with Sealed Realm aura enters the Cloud Sea → attack (perceived as territorial threat)
2. Water Spirit Sect territory is threatened → organized defense
3. A cultivator defeats him once → he will return with a more organized assault (canon-attested: he attacked Wang Lin twice)

### Known actions in canon
- Sensing Lord of the Sealed Realm's aura on Wang Lin, attacked him in the Cloud Sea Star System [E59]
- Wang Lin pushed him to the edge; the spirit of Lord of the Sealed Realm appeared and severely injured Daoist Water
- Wang Lin used up all his energy + a forbidden spell → petrified (turned to stone)
- Li Qianmei's 10-year blood anointment saved Wang Lin
- Eventually slain by Wang Lin in the Cloud Sea [E79]

### Decision style
`aggressive_expansionist` + `cautious_conservative` (hybrid) — He attacks perceived threats aggressively but retreats when outmatched. He returned for a second, more organized assault after the first failure.

### Relationships
- **Wang Lin:** Enemy (territorial threat). Not personal — Wang Lin carried the Sealed Realm's aura.
- **Lord of the Sealed Realm:** Feared (the spirit injured him).
- **Li Qianmei:** Indirect enemy (she saved Wang Lin, the target).

### Canon confidence: C5
### Source: CANON_RI_COMPLETE_WORLD.md N102; CANON_RI_TIMELINE.md E59, E79; WangLinAntagonists.java DAOIST_WATER

---

## CD-07. Blood Ancestor / Yao Xinghai (血祖 / 姚星海)

- **NPC_ID:** N98
- **Role:** antagonist (local-threat / regional)
- **Faction:** Yao Family (Tianyun Star); "Blood Ancestor"
- **Peak realm:** peak Third Step (killed in Thunder Immortal Realm)
- **Status:** Killed by Wang Lin. At the edge of canon, already dead.

### Goals
- **Primary:** Dominance over his territory on Planet Tian Yun / the Thunder Immortal Realm region.
- **Secondary:** Protect his daughter Yao Xixue. Refine the Blood Soul Pill (resurrection pill) as insurance.
- **Tertiary:** Warn his daughter about Wang Lin ("Wang Lin will inevitably become a great calamity").

### Needs
- The Yao Family's power base must be maintained
- The Blood Soul Pill must be completed (resurrection insurance for himself and his daughter)
- His daughter Yao Xixue must survive

### Resources
- Peak Third-Step cultivation
- Blood-cultivation techniques (drain life force from enemies)
- The Yao Family (one of 4 major powers in the Southern Domain, Allheaven Star System)
- Military force (commands armies)
- The Blood Soul Pill (resurrection mechanism)

### Fears
- His own death (he made the Blood Soul Pill as insurance)
- His daughter's death (he warned her about Wang Lin)
- A cultivator who can counter his blood-cultivation (Wang Lin's Life-Death Essence countered it)

### Knowledge
- **Knows:** The Southern Domain's power structure. Blood-cultivation techniques. Wang Lin's threat level (he recognized it early — "inevitably become a great calamity").
- **Does NOT know:** The full Cave World structure. The Heaven-Defying Bead's capabilities.

### Trigger conditions
1. A cultivator threatens Yao Family territory → military response + blood-cultivation assault
2. His daughter is threatened → maximum-force response
3. He is killed → the Blood Soul Pill activates (resurrection mechanism for Yao Xixue)

### Known actions in canon
- Refined the Blood Soul Pill (resurrection pill)
- Warned his daughter Yao Xixue about Wang Lin
- Killed by Wang Lin in the Thunder Immortal Realm [E94]
- Wang Lin later released a wisp of his remnant soul to perfect his Karma Concept
- The amnesiac Yao Xixue departed with this remnant soul

### Decision style
`aggressive_expansionist` + `protective_loyalist` — He expands Yao Family power aggressively but his daughter's safety takes priority.

### Relationships
- **Yao Xixue:** Daughter. Protective. Warned her about Wang Lin.
- **Wang Lin:** Enemy (recognized as a future calamity). Not personal — Wang Lin was a strategic threat.
- **Yao Family:** His power base. Loyal.

### Canon confidence: C5
### Source: CANON_RI_COMPLETE_WORLD.md N98; CANON_RI_TIMELINE.md E94; WangLinAntagonists.java YAO_XINGHAI

---

# Part 3 — Local-Threat NPCs (regional antagonists)

These NPCs operate within specific geographic regions. Their threat is bounded by their domain.

---

## CD-08. Teng Huayuan (藤化元)

- **NPC_ID:** N83
- **Role:** early major antagonist (local-threat)
- **Faction:** Teng Clan (Patriarch); guest elder Jimo Elder
- **Peak realm:** Half-Step Deity Transformation (Late Nascent Soul, later elevated by Piao Nanzi)
- **Status:** Killed by Wang Lin; soul refined into a demon. At the edge of canon, already dead.

### Goals
- **Primary:** Teng Clan dominance in Zhao Country. The Wang Clan stood in the way of Teng Clan hegemony.
- **Secondary:** Avenge his great-great-grandson Teng Li (killed by Wang Lin). Build the "Nine Great Nascent Souls" (Teng One to Teng Nine) succession system to break the generational gap.

### Needs
- Teng Clan territorial expansion in Zhao Country
- The Wang Clan eliminated (rival clan)
- His "Nine Great Nascent Souls" succession system must succeed

### Resources
- Late Nascent Soul cultivation (elevated to Half-Step Deity Transformation by Piao Nanzi)
- The Teng Clan (major clan in Zhao Country)
- The Jimo Elder alliance (guest elder status)
- Gao Qiming (diviner who can locate targets)
- The soul flag (traps enemy souls)
- Piao Nanzi (sealed demon who elevated his cultivation)

### Fears
- The Teng Clan's decline (he built the Nine Great Nascent Souls to prevent this)
- A cultivator surviving his curse-track (Wang Lin did)
- Piao Nanzi's betrayal (the sealed demon is not a true ally)

### Knowledge
- **Knows:** Zhao Country's clan politics. The Teng Clan's resources. The Wang Clan's location (via Gao Qiming's divination). The "Nine Great Nascent Souls" succession plan.
- **Does NOT know:** Wang Lin's Heaven-Defying Bead. Wang Lin's Ji Realm. Wang Lin's Restriction Dao. He believed Wang Lin was dead after their duel at Jue Ming Valley (canon-attested misjudgment).

### Trigger conditions
1. A Teng Clan member is killed → curse-track the killer (burns lifespan to track)
2. A rival clan threatens Teng Clan dominance → exterminate the rival
3. His curse-track target survives → escalate (build the Nine Great Nascent Souls, prepare for revenge)

### Known actions in canon
- After Wang Lin killed Teng Li (his great-great-grandson), burned lifespan to curse-track Wang Lin
- Hired Gao Qiming to divine the Wang family's location
- Exterminated the entire Wang family; trapped their souls in his banner; transmitted the massacre scene to Wang Lin's mind
- Dueled Wang Lin outside Jue Ming Valley; believed Wang Lin was dead after the duel
- Built the "Nine Great Nascent Souls" (Teng One to Teng Nine) selection system
- All 9 Nascent Soul cultivators were killed by Wang Lin and refined into demons
- Wang Lin's "Kill and Destroy the Heart" — first hunted Teng descendants, then built a tower of heads to intimidate Teng Huayuan, then finally slew him

### Decision style
`aggressive_expansionist` + `patient_planner` (hybrid) — He expands aggressively but plans multi-generationally (the Nine Great Nascent Souls system). He is defined by clan loyalty and grudge-holding.

### Relationships
- **Teng Li:** Great-great-grandson. His death triggered the entire revenge arc.
- **Wang Lin:** Enemy (killed Teng Li, then exterminated the Teng Clan). Not personal initially — Wang Lin was a target because he killed Teng Li.
- **Gao Qiming:** Tool (diviner).
- **Piao Nanzi:** Ally of convenience (sealed demon who elevated his cultivation).
- **Wang Clan:** Rival clan. Exterminated.

### Canon confidence: C5
### Source: CANON_RI_COMPLETE_WORLD.md N83; CANON_RI_TIMELINE.md; WangLinAntagonists.java TENG_HUAYUAN

---

## CD-09. Yao Xixue (姚惜雪)

- **NPC_ID:** N99
- **Role:** antagonist (local-threat)
- **Faction:** Yao Family (Tianyun Star)
- **Peak realm:** Infant Transformation Late Stage (initial); reborn via Blood Soul Pill
- **Status:** Alive; amnesiac; wandering with father's remnant soul. At the edge of canon, she is alive but amnesiac.

### Goals
- **Primary (pre-amnesia):** Acquire Wang Lin's Ji Realm ability for her own cultivation advancement. Avenge her father (Blood Ancestor) after Wang Lin kills him.
- **Primary (post-amnesia):** Wander with her father's remnant soul. She has no memories of Wang Lin or the revenge.

### Needs
- **Pre-amnesia:** Wang Lin's Ji Realm. The Blood Soul Pill (to be reborn after sacrificing her body to the Wind Demon).
- **Post-amnesia:** Her father's remnant soul (her only companion).

### Resources
- Infant Transformation Late Stage cultivation (pre-amnesia)
- Heterochromic red pupils, snow-white skin (distinctive appearance)
- The Yao Family's backing (pre-amnesia)
- The Blood Soul Pill (resurrection mechanism)
- The Wind Demon (sealed demon she made a deal with)

### Fears
- **Pre-amnesia:** Her father's death (it happened — triggered her revenge arc). Her own death (she sacrificed her body to the Wind Demon to avoid permanent death).
- **Post-amnesia:** Unknown (amnesiac state).

### Knowledge
- **Knows (pre-amnesia):** Wang Lin's Ji Realm (she wanted it). The Yao Family's resources. The Blood Soul Pill's mechanics. The Immortal Monarch's Cave Mansion.
- **Does NOT know (pre-amnesia):** Wang Lin's full capability (she underestimated him).
- **Post-amnesia:** No memories of Wang Lin or her past.

### Trigger conditions
1. **Pre-amnesia:** Wang Lin's Ji Realm is accessible → ambush and extraction attempt
2. **Pre-amnesia:** Her father is killed → sacrifice body to Wind Demon for revenge
3. **Post-amnesia:** No hostile triggers (she is wandering amnesiac)

### Known actions in canon
- First appears in Ch.491 ("Why the East Sea")
- Arrived riding a Blood Jade to the East Sea Demon Spirit Land
- Cooperated with Wang Lin to explore the Immortal Monarch's Cave Mansion (conditions: Immortal Jade, Blood Soul Pill, treasure jade slip)
- Ambushed Wang Lin at the cave mansion → subdued and imprisoned for 100 years
- Wang Lin destroyed her meridians, sealed her immortal power, used Life Imprint cycle of life-and-death (couldn't die)
- Her father killed by Wang Lin → she used Blood Soul Pill to be reborn → sacrificed her body to the Wind Demon to seek revenge
- Wind Demon killed by Wang Lin's God-Slaying Spear
- Her memories devoured → amnesia
- Wang Lin released Blood Ancestor's remnant soul → amnesiac Yao Xixue departed with the remnant soul

### Decision style
`reactive_opportunist` + `aggressive_expansionist` (pre-amnesia) — She acts on opportunities (ambush) and escalates aggressively (sacrifice to Wind Demon for revenge). Post-amnesia, she has no decision-making agency.

### Relationships
- **Blood Ancestor (Yao Xinghai):** Father. Protective. Her revenge arc was triggered by his death.
- **Wang Lin:** Enemy (pre-amnesia). Target for Ji Realm extraction. Post-amnesia, no memory of him.
- **Wind Demon:** Deal-maker (sacrificed her body to him for revenge power).

### Canon confidence: C5
### Source: CANON_RI_COMPLETE_WORLD.md N99; WangLinAntagonists.java YAO_XIXUE

---

## CD-10. Master Void (虚空道主)

- **NPC_ID:** N97
- **Role:** antagonist (local-threat)
- **Faction:** independent
- **Peak realm:** peak Nirvana Shatterer (declined through encounters with Wang Lin)
- **Status:** Killed by Wang Lin (via Sundered Night). At the edge of canon, already dead.

### Goals
- **Primary:** Eliminate Wang Lin before he can grow. Master Void could not allow Wang Lin (with awakened Vermilion Bird Divine Mark) to become a threat.
- **Secondary:** Maintain his own power tier.

### Needs
- Wang Lin's elimination
- His own cultivation preservation (he declines through each encounter)

### Resources
- Peak Nirvana Shatterer cultivation (initially; declined to peak early Nirvana Shatterer through encounters)
- Unknown sect/faction resources

### Fears
- A cultivator with the Vermilion Bird Divine Mark growing to challenge him (Wang Lin)
- His own cultivation decline (it happened through each encounter)

### Knowledge
- **Knows:** The Vermilion Bird Divine Mark's significance. Wang Lin's threat potential. The Celestial Emperors Tower.
- **Does NOT know:** Wang Lin's full arsenal (underestimated him repeatedly).

### Trigger conditions
1. A cultivator with the Vermilion Bird Divine Mark awakens → preemptive elimination attempt
2. His own power declines → retreat and reassess

### Known actions in canon
- First tried to kill Wang Lin in the Celestial Emperors Tower (peak Nirvana Shatterer)
- Wang Lin's killing formation + source origin lure injured Master Void → mid Nirvana Shatterer
- 2nd encounter (9th level of Celestial Emperors Tower): Zhou Yi/Wang Wei/Hu Juan/Bei Lou + All-Seer/Ling Tianhou's intervention dropped Master Void to peak early Nirvana Shatterer
- Sneak-attacked Wang Wei; Wang Lin switched places and defended
- Later sealed by Ta Jia
- Wang Lin killed two of All-Seer's avatars along with Master Void using Sundered Night

### Decision style
`aggressive_expansionist` — He attacks perceived threats directly. He does not scheme; he uses force.

### Relationships
- **Wang Lin:** Enemy (preemptive elimination target). Not personal — Wang Lin's Divine Mark made him a strategic threat.
- **All-Seer:** Temporary alliance of convenience (both wanted Wang Lin eliminated, for different reasons).
- **Ta Jia:** Sealed him (adversarial).

### Canon confidence: C4
### Source: CANON_RI_COMPLETE_WORLD.md N97

---

## CD-11. Lian Daozhen (连道真) — Immortal Emperor antagonist

- **NPC_ID:** N110
- **Role:** antagonist (IAC-tier)
- **Faction:** Xiangang Continent
- **Peak realm:** Immortal Emperor (Xiangang Continent)
- **Status:** Complex (self-destruction; relatives are Wang Lin's allies). At the edge of canon, already neutralized.

### Goals
- **Primary:** Inherit the Immortal Ancestor's plan (he failed).
- **Secondary:** Eliminate Wang Lin (Wang Lin's existence threatened his claim).

### Needs
- The Immortal Ancestor's plan inheritance (he failed to obtain it)
- Wang Lin's elimination (to remove the rival claim)

### Resources
- Immortal Emperor cultivation
- The Xiangang Continent's resources
- His brother Lian Daofei (complex — brother is Wang Lin's ally)

### Fears
- Failing to inherit the Immortal Ancestor's plan (he did fail)
- Lian Daofei's bloodline awakening (it did — Eight Extremities Great Heavenly Venerable)

### Knowledge
- **Knows:** The Immortal Ancestor's plan. The Xiangang Continent's power structure. His brother's capabilities.
- **Does NOT know:** Wang Lin's full capability (underestimated him).

### Trigger conditions
1. The Immortal Ancestor's plan inheritance becomes available → claim it
2. A rival claimant emerges → eliminate them
3. Failure → self-destruction (canon-attested: he self-destructed his Immortal Emperor cultivation)

### Known actions in canon
- Failed to inherit the Immortal Ancestor's plan
- After failure, Lian Daofei's bloodline awakened (Eight Extremities)
- Immortal Emperor self-destructured
- Wang Lin captured the Immortal Emperor's soul and injured the Infant Skull belonging to the Dao Yi Great Celestial Venerable

### Decision style
`aggressive_expansionist` — He claimed the inheritance aggressively. On failure, he self-destructed rather than accept defeat.

### Relationships
- **Lian Daofei:** Brother (adversarial — Lian Daofei is Wang Lin's ally).
- **Wang Lin:** Enemy (rival claimant to the Immortal Ancestor's plan).

### Canon confidence: C4
### Source: CANON_RI_COMPLETE_WORLD.md N110

---

## CD-12. Dao Devil Sect Master / Dao Demon Sect Master

- **NPC_ID:** N122
- **Role:** antagonist (IAC-tier)
- **Faction:** Dao Devil Sect (IAC)
- **Peak realm:** Third Step+
- **Status:** Killed by Wang Lin. At the edge of canon, already dead.

### Goals
- **Primary:** Resurrect the Green Devil Scorpion (the entity his sect worships).
- **Secondary:** Use Wang Lin's body as the sacrifice/vessel for the resurrection.

### Needs
- A suitable sacrifice (Wang Lin — who had multiple Essences implanted by Ji Si)
- The Dao Devil Sect's ritual infrastructure
- The Green Devil Scorpion's remnant

### Resources
- Third-Step+ cultivation
- The Dao Devil Sect (IAC faction)
- Ji Si (who implanted Essences into Wang Lin to prepare him as a vessel)
- The Green Devil Scorpion remnant

### Fears
- The Green Devil Scorpion's resurrection failing (it did fail — Wang Lin reclaimed his body)
- Wang Lin escaping the ritual (he did)

### Knowledge
- **Knows:** The Green Devil Scorpion resurrection ritual. Wang Lin's implanted Essences (Ji Si prepared them). The Dao Devil Sect's capabilities.
- **Does NOT know:** Wang Lin's Heaven-Defying Bead (it protected him from the mind-erasure).

### Trigger conditions
1. A cultivator with multiple Essences is available → capture and prepare for the ritual
2. The ritual is ready → execute the resurrection

### Known actions in canon
- Fed his own Thunder Essence into Wang Lin's to evolve it into an Essence True Body (controlled by him to erase Wang Lin's mind/memory)
- Wang Lin protected by Heaven Defying Pearl → reclaimed the Essence True Body
- Wang Lin annihilated the Dao Devil Sect

### Decision style
`patient_planner` + `aggressive_expansionist` — He schemes (Ji Si's Essences) and then acts forcefully (the ritual).

### Relationships
- **Green Devil Scorpion:** Worshipped entity. Goal of resurrection.
- **Ji Si:** Subordinate (implanted Essences into Wang Lin).
- **Wang Lin:** Sacrifice vessel. Not personal — Wang Lin was a tool.

### Canon confidence: C4
### Source: CANON_RI_COMPLETE_WORLD.md N122, N132

---

## CD-13. Gu Dao (古道) — final IAC rival

- **NPC_ID:** N117
- **Role:** rival / antagonist (IAC-tier)
- **Faction:** IAC
- **Peak realm:** Grand Empyrean (strongest on IAC before Wang Lin)
- **Status:** Alive (acknowledges Wang Lin as superior). At the edge of canon, alive and subordinate to Wang Lin's tier.

### Goals
- **Primary:** Maintain his position as the strongest on the IAC (before Wang Lin's arrival).
- **Secondary:** Test Wang Lin's capability (the battle with Wang Lin was as much a test as a fight).

### Needs
- His position as #1 on the IAC (lost to Wang Lin)
- The IAC's stability

### Resources
- Grand Empyrean cultivation (strongest on IAC before Wang Lin)
- The IAC's resources

### Fears
- A cultivator surpassing him (Wang Lin did)
- The IAC's power structure being disrupted

### Knowledge
- **Knows:** The IAC's full power structure. All Nine Suns. Wang Lin's capability (after their battle).

### Trigger conditions
1. A cultivator approaches his power tier → battle (to test and establish hierarchy)
2. The IAC is threatened → defensive response

### Known actions in canon
- Wang Lin's battle with Gu Dao → enlightenment → fused with Void Avatar → full Nine Songs Three Signs → significant progress in fusion of Celestial and Ancient powers
- Wang Lin becomes #1 on IAC after this battle
- Gu Dao acknowledges Wang Lin as superior

### Decision style
`aggressive_expansionist` (initially) → `cautious_conservative` (after defeat) — He tests rivals through battle. Once defeated, he acknowledges hierarchy and becomes conservative.

### Relationships
- **Wang Lin:** Rival (initially) → acknowledged superior (after defeat). Not personal — hierarchy establishment.

### Canon confidence: C4
### Source: CANON_RI_COMPLETE_WORLD.md N117

---

# Part 4 — Mentor NPCs (transmission sources)

These NPCs are sources of inheritance, techniques, and guidance. They are not antagonists — they are the canon's mechanism for acquiring cultivation knowledge.

> **Architectural note (user directive):** The player is a protagonist who can learn everything Wang Lin did and more. Mentors are the acquisition route for many daos. Some inheritances are Unique (one owner), some Repeatable (texts), some Recoverable (obtainable from current holders). See CANON_RI_INHERITANCES.md (planned).

---

## CD-14. Situ Nan (司徒南) — primary mentor

- **NPC_ID:** N20
- **Role:** mentor / companion / sworn brother
- **Faction:** Vermilion Bird Country (2nd-Gen Vermilion Bird Master); later Wu Xuan Country (Grand Marshal "Si Nan")
- **Peak realm:** Yang Solid Peak (reconstructed); Heaven Trampling (after IAC reincarnation)
- **Status:** Alive; becomes prince (lifelong dream); no regrets. At the edge of canon, alive and fulfilled.

### Goals
- **Primary:** Regain a physical body (achieved — Wang Lin helped reconstruct one at Soul Transformation stage).
- **Secondary:** Become a prince (achieved — reincarnated on IAC as "Si Nan," Grand Marshal of Wu Xuan Country). This was his lifelong dream.
- **Tertiary:** Protect Wang Lin (sworn brother). Repay Ye Wuyou's life-saving favor (achieved — guarded Vermilion Bird Star for 1000 years).

### Needs
- A physical body (achieved)
- Wang Lin's safety (achieved — Wang Lin transcended)
- His lifelong dream of becoming prince (achieved)

### Resources
- Originally one of the seven souls (Green Soul — cultivation talent) of the Seven-Colored Immortal Venerable
- Primordial spirit (fled into Heaven Defying Pearl after betrayal)
- Vermilion Bird lineage knowledge (2nd-Gen Vermilion Bird Master)
- Soul Refining technique knowledge
- Heaven-Defying Pearl residence (he lived inside it)

### Fears
- Body-snatching failure (he initially planned to body-snatch Wang Lin but gave up)
- Wang Lin's death (sworn brother)
- The 3rd-Gen Vermilion Bird and Tan Lang (they betrayed him)

### Knowledge
- **Knows:** The Vermilion Bird lineage. The Heaven-Defying Pearl's interior. The Seven-Colored Immortal Venerable's nature (he was a fragment). Soul Refining techniques. Wang Lin's full cultivation journey (he was present from the beginning).
- **Does NOT know:** The full Cave World ownership structure (he learned about it alongside Wang Lin).

### Trigger conditions
1. A cultivator finds the Heaven-Defying Pearl → he evaluates them as a potential host (he chose Wang Lin)
2. His sworn brother is threatened → sacrifice response (he sacrificed his remaining power to save Wang Lin's soul after Teng Huayuan)
3. The Vermilion Bird lineage is threatened → protective response

### Known actions in canon
- Originally the Green Soul (cultivation talent) of the Seven-Colored Immortal Venerable
- Betrayed by 3rd-Gen Vermilion Bird and Tan Lang; primordial spirit fled into Heaven Defying Pearl
- Met Wang Lin when Wang Lin found the bead
- Gave up body-snatching; taught Wang Lin cultivation (Underworld Ascension Method, Vermilion Bird Burning Heaven Art)
- Sacrificed his remaining power to save Wang Lin's soul after Teng Huayuan
- Wang Lin helped him reconstruct a physical body at Soul Transformation stage
- Founded "Southern King" faction on Feng Luan Star
- Reincarnated on IAC as "Si Nan," Grand Marshal of Wu Xuan Country (achieved his prince dream)

### Decision style
`protective_loyalist` + `curious_explorer` — He is defined by loyalty (to Wang Lin, to Ye Wuyou's memory). He gave up body-snatching because he came to care about Wang Lin. He explored the world alongside Wang Lin.

### Relationships
- **Wang Lin:** Sworn brother. Primary attachment. Would sacrifice everything for him.
- **Ye Wuyou:** Life-saving benefactor (1st-Gen Vermilion Bird Master). Repaid the debt by guarding Vermilion Bird Star for 1000 years.
- **3rd-Gen Vermilion Bird Master:** Betrayer. Enemy (deceased).
- **Tan Lang:** Betrayer. Enemy (reduced to a pet by Twin Great Heavenly Venerables).
- **Seven-Colored Daoist:** Creator (Situ Nan was his Green Soul). Complex — Situ Nan fled him, but did not actively pursue revenge.

### Inheritance offered
- **Underworld Ascension Method** (黄泉升窍诀) — Type: Unique (mentor transmission). Wang Lin received this from Situ Nan via the Heaven-Defying Pearl. The player could receive this IF they acquire the Heaven-Defying Pearl and Situ Nan trusts them (as he trusted Wang Lin).
- **Vermilion Bird Burning Heaven Art** (朱雀焚天功) — Type: Unique (Vermilion Bird lineage). Situ Nan is 2nd-Gen Vermilion Bird Master.
- **Soul Refining basics** — Type: Recoverable (Situ Nan knows the foundational technique; the full Soul Refining Sect inheritance came from Du Tian).

### Canon confidence: C5
### Source: CANON_RI_COMPLETE_WORLD.md N20; CANON_RI_TIMELINE.md

---

## CD-15. Tu Si (涂司) — inheritance mentor (ancient)

- **NPC_ID:** N22
- **Role:** mentor (inheritance only — long dead)
- **Faction:** Ancient Clan
- **Peak realm:** 8-Star Ancient God (peak)
- **Status:** Long dead; body became a realm (Land of the Ancient God / Chaotic Broken Stars). Inheritance split between Wang Lin (knowledge) and Tuo Sen (power). At the edge of canon, the inheritance has been claimed.

### Goals (when alive)
- **Primary:** Achieve peak Ancient God cultivation (achieved — 8-Star).
- **Secondary:** Split his inheritance into "knowledge" and "power" via the Ink Flow Split Soul Technique (this created Tuo Sen as his demonic thought).

### Needs (when alive)
- The Ink Flow Split Soul Technique to succeed (it partially failed — created Tuo Sen as a separate entity)
- A worthy inheritor for his knowledge (Wang Lin)

### Resources (legacy)
- His body became the Land of the Ancient God (3-level Chaotic Broken Stars realm)
- The "knowledge" inheritance (memory legacy — Wang Lin received)
- The "power" inheritance (Tuo Sen received)
- The "Great Enlightened One" title (granted to Wang Lin)

### Fears (when alive)
- The Ink Flow Split Soul Technique failing (it did partially fail)
- His inheritance being lost (it was not — Wang Lin and Tuo Sen claimed it)

### Knowledge (contained in the inheritance)
- **The inheritance contains:** Ancient God Tactic (古神诀) — body reconstruction, plunder, star-absorption. Heaven Technique (通天诀) — movement inside Ancient God bodies. 8-Star Ancient God cultivation knowledge.

### Trigger conditions
1. A cultivator enters the Land of the Ancient God → the inheritance trial activates
2. A cultivator passes the trial → receives the "knowledge" inheritance (Wang Lin did)
3. A cultivator fails the trial → Tuo Sen (the "power" inheritance) hunts them

### Known actions in canon
- Achieved 8-Star Ancient God (peak)
- Attempted the Ink Flow Split Soul Technique → created Tuo Sen (his demonic thought) as a separate entity
- His body became the Land of the Ancient God
- Wang Lin inherited his "knowledge" inheritance (Ch.199 — 1-Star Ancient God)
- Tuo Sen inherited his "power" inheritance
- Wang Lin later returned the memory inheritance to Tuo Sen (reconciliation)

### Decision style
N/A (long dead; acts only through the inheritance trial mechanism)

### Relationships
- **Wang Lin:** Inheritor (knowledge).
- **Tuo Sen:** Inheritor (power) / demonic thought. Complex — they are technically the same being split into two.

### Inheritance offered
- **Ancient God Tactic** (古神诀) — Type: Unique (inheritance). One owner at a time. Wang Lin claimed it. The player could claim it IF they reach the Land of the Ancient God before Wang Lin's canonical claim (impossible at edge of canon) OR if they take it from Wang Lin (extremely dangerous) OR if Tu Si's legacy has a repeatable component (the general Ancient God cultivation knowledge might be Repeatable as texts, but the True Inheritance is Unique).
- **Heaven Technique** (通天诀) — Type: Repeatable (auxiliary technique, not the core inheritance).

### Canon confidence: C5
### Source: CANON_RI_COMPLETE_WORLD.md N22; CANON_RI_TIMELINE.md

---

## CD-16. Du Tian (顿天) — Soul Refining Sect ancestor

- **NPC_ID:** N23
- **Role:** mentor / ally
- **Faction:** Soul Refining Sect (Pilu Kingdom) — ancestor
- **Peak realm:** Nirvana Scryer+ (erased his own consciousness to become a soul in the Soul Banner)
- **Status:** Self-erased; exists as a soul in the Ten Billion Soul Banner. At the edge of canon, exists as a soul.

### Goals
- **Primary:** Elevate the Soul Refining Sect to 6th-level (he asked Wang Lin to promise this).
- **Secondary:** Pass on the Soul Refining Sect inheritance to a worthy successor (Wang Lin).

### Needs
- The Soul Refining Sect elevated to 6th-level (Wang Lin promised)
- A worthy successor (Wang Lin)
- Immortal Jades for Soul Transformation breakthrough (Wang Lin helped seize them)

### Resources
- Nirvana Scryer+ cultivation
- The Soul Refining Sect's full inheritance
- The Ten Billion Soul Banner (signature artifact)
- Senior brother to Nian Tian

### Fears
- The Soul Refining Sect declining (he sacrificed himself to prevent this)
- An unworthy successor claiming the inheritance (he chose Wang Lin)

### Knowledge
- **Knows:** The Soul Refining Sect's complete technique system. The Ten Billion Soul Banner's mechanics. Soul Transformation breakthrough requirements.

### Trigger conditions
1. A cultivator worthy of the Soul Refining Sect inheritance appears → evaluate and transmit
2. The Soul Refining Sect is threatened → defensive response (he erased himself to become a soul in the banner, preserving the sect's power)

### Known actions in canon
- Gave Wang Lin 3 gifts: (1) helped Wang Lin's clone reach Nascent Soul peak, (2) helped Wang Lin's true body reach 3-Star Ancient God, (3) bestowed the Ten Billion Soul Banner and Soul Refining Sect inheritance
- Took Wang Lin to seize Immortal Jades for Soul Transformation breakthrough
- Erased his own consciousness to become a soul within the Soul Banner after Wang Lin's promise to elevate Soul Refining Sect to 6th-level

### Decision style
`protective_loyalist` + `patient_planner` — He sacrificed himself for the sect. He planned multi-step gifts for Wang Lin.

### Relationships
- **Wang Lin:** Successor. Chose him. Sacrificed himself based on Wang Lin's promise.
- **Soul Refining Sect:** His life's purpose. Protective.
- **Nian Tian:** Senior brother.

### Inheritance offered
- **Soul Refining Sect inheritance** — Type: Unique (true inheritance). Wang Lin received the full transmission. The player could receive this IF they find Du Tian (in the Ten Billion Soul Banner) and he deems them worthy, OR if they take the banner from Wang Lin.
- **Ten Billion Soul Banner** — Type: Unique (artifact). One owner. Wang Lin has it.

### Canon confidence: C5
### Source: CANON_RI_COMPLETE_WORLD.md N23

---

## CD-17. Bai Fan (白凡) — Immortal Emperor (inheritance)

- **NPC_ID:** N24
- **Role:** mentor (inheritance only — long dead)
- **Faction:** Thunder Immortal World
- **Peak realm:** Immortal Emperor (Third Step+)
- **Status:** Long dead; inheritance passed to Wang Lin.

### Goals (when alive)
- Unknown (long dead; only the inheritance matters)

### Resources (legacy)
- The Thunder Immortal World's Collection Pavilion
- Mountain Crumble spell (王林 inherited)
- Six Paths Triple Techniques (Call Wind, Call Rain, Scatter Beans to Form Soldiers, Mountain Collapse, Dark Moon Has Clear, etc.)

### Knowledge (contained in the inheritance)
- Mountain Crumble spell
- Six Paths Triple Techniques
- Various Thunder Immortal World techniques

### Trigger conditions
1. A cultivator finds Bai Fan's Collection Pavilion in the Thunder Immortal World → inheritance available

### Known actions in canon
- Wang Lin finds Bai Fan's Collection Pavilion in the Thunder Immortal World
- Inherits the Mountain Crumble spell and Six Paths Triple Techniques

### Inheritance offered
- **Mountain Crumble spell** — Type: Repeatable (text/inheritance pavilion). Multiple cultivators could find the pavilion and learn. Wang Lin found it; the player could too (if the pavilion persists or respawns).
- **Six Paths Triple Techniques** — Type: Repeatable (same pavilion).

### Canon confidence: C4
### Source: CANON_RI_COMPLETE_WORLD.md N24

---

## CD-18. Xuan Luo (玄罗) — Great Heavenly Venerable, true master

- **NPC_ID:** N28
- **Role:** mentor / true master
- **Faction:** Ancient Clan (Dao Gu lineage)
- **Peak realm:** Great Heavenly Venerable (Dao Gu lineage)
- **Status:** Alive; undergoes reincarnation (Wang Lin gifted him a protective life-saving jade slip). At the edge of canon, alive.

### Goals
- **Primary:** Accept and cultivate a worthy disciple (Wang Lin — the only one he formally acknowledges on the IAC).
- **Secondary:** Reincarnate a dozen of Wang Lin's friends onto the Immortal Execution Continent (achieved).
- **Tertiary:** His own reincarnation cycle.

### Needs
- A worthy disciple (Wang Lin)
- The Ancient Clan's stability
- Wang Lin's friends' safety (he reincarnated them)

### Resources
- Great Heavenly Venerable cultivation (Dao Gu lineage)
- The Ancient Clan's resources
- Ability to reincarnate cultivators onto specific continents
- Protective life-saving jade slips

### Fears
- Failing to protect his disciple (he intervened during Wang Lin's tribulation)
- The Ancient Clan's decline

### Knowledge
- **Knows:** The Ancient Clan's full lineage. The Dao Gu lineage's techniques. The Cave World's origin (he was present at the Seven Paths Sect entrance when Dao Yi Great Heavenly Venerable fought over the Primordial God Realm fragment — this indirectly caused the Cave World's birth). Reincarnation mechanics.

### Trigger conditions
1. A cultivator with sufficient talent and character appears → evaluate as potential disciple
2. His disciple is threatened → intervention (he intervened during Wang Lin's tribulation)
3. The Ancient Clan is threatened → defensive response

### Known actions in canon
- Refused the Ancient Dao Great Heavenly Venerable's offer to take him as disciple
- Fought Dao Yi Great Heavenly Venerable over a fragment of Primordial God Realm at the Seven Paths Sect entrance — indirectly causing the Cave World's birth
- Entered the Cave World to accept Wang Lin as his only disciple
- Helped over a dozen of Wang Lin's friends reincarnate into the Immortal Execution Continent
- Intervened during Wang Lin's tribulation
- Undergoes reincarnation (Wang Lin gifted him a protective life-saving jade slip)

### Decision style
`protective_loyalist` + `curious_explorer` — He is loyal to his disciple (Wang Lin). He explored the Cave World to find Wang Lin. He is the only master Wang Lin formally acknowledges.

### Relationships
- **Wang Lin:** Only formally acknowledged disciple. Protective.
- **Ancient Dao Great Heavenly Venerable:** Refused his offer (adversarial respect).
- **Dao Yi Great Heavenly Venerable:** Fought him (adversarial).

### Inheritance offered
- **Dao Gu lineage techniques** — Type: Unique (mentor transmission). Wang Lin received the formal transmission. The player could receive this IF they find Xuan Luo and he deems them worthy (he is extremely selective — only one disciple in canon).
- **Reincarnation assistance** — Type: Unique (service, not technique). Xuan Luo can reincarnate cultivators onto specific continents.

### Canon confidence: C5
### Source: CANON_RI_COMPLETE_WORLD.md N28

---

## CD-19. Dao Master Blue Dream (蓝梦道主)

- **NPC_ID:** N16 / N29
- **Role:** family / mentor
- **Faction:** Blue Silk Clan
- **Peak realm:** Dao Master (Void Tribulant+)
- **Status:** Alive. At the edge of canon, alive.

### Goals
- **Primary:** Protect his daughter Li Qianmei.
- **Secondary:** Teach worthy cultivators (Wang Lin).

### Needs
- Li Qianmei's safety
- The Blue Silk Clan's stability

### Resources
- Dao Master cultivation (Void Tribulant+)
- The Blue Silk Clan's resources
- Light Shadow Shield, Dao Art Fusion, Overturn Heaven Seal (techniques he taught Wang Lin)

### Fears
- Li Qianmei's harm (she was — Wang Lin healed her at the cost of her memories)
- His own injury (Wang Lin injured his palm at one point)

### Knowledge
- **Knows:** The Blue Silk Clan's techniques. Light Shadow Shield. Dao Art Fusion. Overturn Heaven Seal.

### Trigger conditions
1. His daughter is threatened → maximum-force response
2. A worthy cultivator seeks his teaching → evaluate and transmit

### Known actions in canon
- Wang Lin injured Blue Dream's palm at one point
- Later healed Li Qianmei at the cost of her memories
- Taught Wang Lin Light Shadow Shield, Dao Art Fusion, Overturn Heaven Seal

### Decision style
`protective_loyalist` — His daughter is his primary concern.

### Relationships
- **Li Qianmei:** Daughter. Protective.
- **Wang Lin:** Student (complex — Wang Lin injured him, but he still taught Wang Lin). The healing of Li Qianmei was a transaction.

### Inheritance offered
- **Light Shadow Shield, Dao Art Fusion, Overturn Heaven Seal** — Type: Repeatable (mentor transmission, but he teaches multiple students). The player could learn these IF they find Dao Master Blue Dream and he agrees to teach.

### Canon confidence: C4
### Source: CANON_RI_COMPLETE_WORLD.md N16, N29

---

## CD-20. Second Generation Vermilion Bird / Young Emperor of the Fallen Land

- **NPC_ID:** N52
- **Role:** ally / mentor
- **Faction:** Vermilion Bird Divine Sect / Fallen Land
- **Peak realm:** Void Flame Cultivator (2nd-Gen Vermilion Bird Divine Emperor)
- **Status:** Alive. At the edge of canon, alive.

### Goals
- **Primary:** Guide the next generation of Vermilion Bird inheritors (Wang Lin).
- **Secondary:** Maintain the Fallen Land's integrity.

### Needs
- A worthy Vermilion Bird successor (Wang Lin)
- The Fallen Land's stability

### Resources
- 2nd-Gen Vermilion Bird Divine Emperor cultivation
- The Fallen Land (realm he controls)
- Dragon blood (he fished for a dragon and gifted its blood to Wang Lin)
- The Dao of Strength (taught to Wang Lin)
- Three supreme techniques of the Vermilion Bird (taught one to Wang Lin)

### Fears
- The Vermilion Bird lineage declining
- The Fallen Land being disrupted

### Knowledge
- **Knows:** The Vermilion Bird lineage's full history. The Fallen Land's mechanics. The Dao of Strength. The three supreme techniques of the Vermilion Bird.

### Trigger conditions
1. A cultivator undertakes the Young Emperor trial → evaluate and guide
2. The Vermilion Bird lineage is threatened → protective response

### Known actions in canon
- Fished for a dragon, took its blood, gifted it to Wang Lin
- Taught Wang Lin the Dao of Strength
- Stood up for Wang Lin at the Trial of Heaven
- Taught Wang Lin one of the three supreme techniques of the Vermilion Bird when Wang Lin completed the trial
- Asked by Wang Lin to take care of Zhong Dahong

### Decision style
`protective_loyalist` + `curious_explorer` — He guides the next generation. He stood up for Wang Lin at the Trial of Heaven.

### Relationships
- **Wang Lin:** Mentee. Guided him through the Young Emperor trial.
- **Zhong Dahong:** Asked to take care of him (by Wang Lin).

### Inheritance offered
- **Dao of Strength** — Type: Repeatable (mentor transmission).
- **Vermilion Bird supreme technique (one of three)** — Type: Unique (one student per technique per generation). Wang Lin received one. The player could receive a different one IF they undertake the Young Emperor trial.
- **Dragon blood** — Type: Unique (one-time gift). Wang Lin received it.

### Canon confidence: C4
### Source: CANON_RI_COMPLETE_WORLD.md N52

---

## CD-21. Qing Shui (清水) — senior brother / mass-murderer friend

- **NPC_ID:** N30
- **Role:** ally / senior brother
- **Faction:** Qing Shui Kingdom (former prince); Colorful Immortal Venerable's slaughter soul
- **Peak realm:** Third Step (Slaughter Essence)
- **Status:** Alive; reaches Third Step. At the edge of canon, alive.

### Goals
- **Primary:** Find his daughter Hong Die (achieved — with Wang Lin's help).
- **Secondary:** Protect Wang Lin (senior brother bond, recognized across generations via Bai Fan's technique).
- **Tertiary:** Cultivate the Slaughter Essence (achieved — Third Step).

### Needs
- Hong Die's safety (achieved — reincarnated as Qing Hong on IAC)
- Wang Lin's safety
- His own cultivation advancement

### Resources
- Third Step cultivation (Slaughter Essence)
- Formed from the Seven-Colored Immortal Venerable's lifetime of slaughter
- Slaughter Sword (Slaughter Essence) — gifted to Wang Lin when Wang Lin broke through Sky Gate
- Immortal arts (taught to Wang Lin)

### Fears
- Hong Die's death (she died — but reincarnated)
- Wang Lin's death

### Knowledge
- **Knows:** The Slaughter Essence. The Seven-Colored Immortal Venerable's nature (he was a fragment). Bai Fan's technique (the cross-generation bond mechanism). Hong Die's location (after reincarnation).

### Trigger conditions
1. His daughter is threatened → maximum-force response
2. Wang Lin is threatened → protective response (saved Wang Lin multiple times)
3. A cultivator seeks the Slaughter Essence → evaluate (he gifted the Slaughter Sword to Wang Lin)

### Known actions in canon
- Saved Wang Lin multiple times
- Taught Wang Lin immortal arts
- Wang Lin helped him find his daughter Hong Die
- Reincarnated on Immortal Execution Star; memories auto-recovered
- In ISSTH, left a clone to assist Meng Hao (cross-novel)
- Gifted Wang Lin the Slaughter Sword (Slaughter Essence) when Wang Lin broke through Sky Gate

### Decision style
`protective_loyalist` — He is defined by loyalty to Wang Lin and his daughter.

### Relationships
- **Wang Lin:** Senior brother (recognized across generations). Protective.
- **Hong Die:** Daughter. Protective.
- **Seven-Colored Immortal Venerable:** Creator (Qing Shui was his Slaughter Soul). Complex — Qing Shui is independent, not loyal to his creator.

### Inheritance offered
- **Slaughter Essence transmission** — Type: Unique (the Slaughter Sword was gifted to Wang Lin). The player could receive this IF they are recognized as a "junior brother" through Bai Fan's technique bond AND Qing Shui deems them worthy.
- **Immortal arts** — Type: Repeatable (mentor transmission). Qing Shui taught Wang Lin multiple arts.

### Canon confidence: C5
### Source: CANON_RI_COMPLETE_WORLD.md N30

---

# Part 5 — Ally NPCs (Wang Lin's network)

These NPCs are Wang Lin's allies. They are not antagonists. At the edge of canon, they are alive (most reincarnated onto the IAC by Xuan Luo).

> **Note for the player:** These NPCs are Wang Lin's allies, not the player's. The player must build their own relationships. How these NPCs react to the player depends on the player's actions and their relationship with Wang Lin.

---

## CD-22. Li Muwan / Wan'er (李慕婉) — primary wife

- **NPC_ID:** N17
- **Role:** spouse / protagonist's beloved
- **Faction:** Luo He Sect (Flame-Burning Country) → Cloud Sky Sect / Yuntian Sect (Elder → Master)
- **Peak realm:** Nascent Soul (at death); Treading Heaven Realm (after resurrection by Wang Lin)
- **Status:** Dead → soul preserved 700 years → resurrected → transcendent. At the edge of canon, alive and eternally with Wang Lin.

### Goals
- **Primary:** Be with Wang Lin (achieved — resurrected and transcended together).
- **Secondary:** Refine pills and techniques to support Wang Lin (she made the Distant Heaven Pill that triggered Wang Lin's Core Formation).

### Needs
- Wang Lin's presence (achieved)
- Her own cultivation (she failed Nascent Soul formation multiple times; finally formed it but died)

### Resources
- Nascent Soul cultivation (at death); Treading Heaven Realm (after resurrection)
- Pill refinement mastery (she made the Distant Heaven Pill)
- Azure Dragon Jade Slip (she drained her life force to refine it for Wang Lin)
- Heaven-Avoiding Coffin (life-saving artifact)

### Fears
- Separation from Wang Lin (this drove her entire arc)
- Her own death (she died — Wang Lin resurrected her)

### Knowledge
- **Knows:** Pill refinement. The Luo He Sect's techniques. The Cloud Sky Sect's techniques. Wang Lin's full journey (she was with him for most of it).

### Trigger conditions
1. Wang Lin is threatened → supportive response (pill refinement, technique support)
2. She is separated from Wang Lin → wait for reunion (she waited 200 years for him)

### Known actions in canon
- Met Wang Lin in Fire Burn Country (escaping a Fire Beast)
- Drained her life force to refine the Azure Dragon Jade Slip for Wang Lin (damaged her foundation → stuck at early Core Formation)
- Made the Distant Heaven Pill that triggered Wang Lin's Core Formation
- Waited 200 years for him
- Married Sun Zhenwei — Wang Lin killed him and took her back
- Failed Nascent Soul formation multiple times; finally formed Nascent Soul but died (body expired, 500 years old)
- Wang Lin placed her soul in Heaven Defying Bead for 700 years
- Tried Qi Xi spell (failed)
- Heaven-Avoiding Coffin (life-saving)
- Finally resurrected by Wang Lin at 4th Step
- Transcends with him

### Decision style
`protective_loyalist` — She is defined by her love for Wang Lin. All decisions serve their reunion.

### Relationships
- **Wang Lin:** Husband. Eternal.
- **Li Qiqing:** Brother.
- **Sun Zhenwei:** Forced husband (Wang Lin killed him).

### Canon confidence: C5
### Source: CANON_RI_COMPLETE_WORLD.md N17

---

## CD-23. Li Qianmei (李千媚) — second wife

- **NPC_ID:** N18
- **Role:** spouse / ally
- **Faction:** Ghost Sect (originally) → Wang Lin's lineage
- **Peak realm:** Nirvana Scryer+ (her father healed her, at cost of memories)
- **Status:** Alive; with one of Wang Lin's clones. At the edge of canon, alive.

### Goals
- **Primary:** Be with Wang Lin (achieved — with one of his clones).
- **Secondary:** Recover her memories (lost when her father healed her).

### Needs
- Wang Lin's presence (achieved via clone)
- Her memories (partially lost)

### Resources
- Nirvana Scryer+ cultivation
- Ghost Sect techniques
- Blood anointment (she saved Wang Lin with 10-year blood anointment after Daoist Water)

### Fears
- Losing Wang Lin (she smeared blood on his stone-petrified body for 10 years to save him)
- Her own memory loss

### Knowledge
- **Knows:** The Ghost Sect's techniques. Wang Lin's nature (she asked him 3 questions at the Origin Sect).

### Trigger conditions
1. Wang Lin is petrified/injured → blood anointment to save him (she did this for 10 years)
2. Wang Lin is threatened → supportive response

### Known actions in canon
- Asked Wang Lin 3 questions at the Origin Sect
- Traveled with him
- Smeared blood on his stone-petrified body for 10 years to save him
- Sent into a spatial realm with powerful beasts by the Ghost Sect
- Rescued by Wang Lin
- Healed by her father, losing most memories of Wang Lin
- The "doppelganger" (one of Wang Lin's clones) eventually accompanies her

### Decision style
`protective_loyalist` — She is defined by her devotion to Wang Lin (10 years of blood anointment).

### Relationships
- **Wang Lin:** Husband (primary). Devoted.
- **Dao Master Blue Dream:** Father.
- **Wang Lin's clone:** Companion (after her memory loss, the clone accompanies her).

### Canon confidence: C5
### Source: CANON_RI_COMPLETE_WORLD.md N18

---

## CD-24. Zhou Yi (周逸) — ally (Obsession Concept)

- **NPC_ID:** N31
- **Role:** ally
- **Faction:** originally his sect; defected
- **Peak realm:** Wending realm (soul transformed into sword spirit)
- **Status:** Becomes sword spirit of Rain Immortal Sword; reincarnated with Qing Shuang on IAC. At the edge of canon, alive (reincarnated).

### Goals
- **Primary:** Protect Qing Shuang / "Ting'er" (obsessive devotion).
- **Secondary:** Achieve the Obsession Concept (achieved — Wending realm).

### Needs
- Qing Shuang's safety (even her corpse — he protected it)
- The Obsession Concept's perfection

### Resources
- Wending realm cultivation (soul transformed into sword spirit)
- The Obsession Concept (his defining power)
- The Wending Crystal (gave to Wang Lin)

### Fears
- Qing Shuang's corpse being disturbed (he defected from his sect to protect it)
- His obsession being broken

### Knowledge
- **Knows:** The Obsession Concept. Qing Shuang's location. The Rain Immortal Realm.

### Trigger conditions
1. Qing Shuang / Ting'er is threatened → maximum-force response (he burned his primordial spirit in defense)
2. Someone can protect Ting'er → delegate (he gave his Wending Crystal to Wang Lin, asking Wang Lin to protect Ting'er)

### Known actions in canon
- Found Qing Shuang's corpse in Rain Immortal Realm and called her "Ting'er" (necrophiliac reputation)
- Defected from his sect to protect her
- Multiple Rain Immortal Realm entries for Immortal Jades
- Burned his primordial spirit in defense — reached Wending realm
- Gave his Wending Crystal to Wang Lin (asking Wang Lin to protect Ting'er)
- Ting'er's remnant soul awakened and transformed Zhou Yi's primordial spirit into the sword spirit of the Rain Immortal Sword
- Reincarnates on IAC with Qing Shuang

### Decision style
`protective_loyalist` (extreme) — His entire existence is defined by obsessive protection of Qing Shuang / Ting'er.

### Relationships
- **Qing Shuang / Ting'er:** Obsessive devotion. All decisions serve her protection.
- **Wang Lin:** Delegated protector (gave him the Wending Crystal).

### Canon confidence: C5
### Source: CANON_RI_COMPLETE_WORLD.md N31

---

## CD-25. Ling Tianhou (凌天候) — ally (Da Lou Sword Sect)

- **NPC_ID:** N39
- **Role:** ally / rival-mentor
- **Faction:** Da Lou Sword Sect — elder / sect master
- **Peak realm:** Third Step (Nirvana Void)
- **Status:** Alive. At the edge of canon, alive.

### Goals
- **Primary:** Maintain the Da Lou Sword Sect's power.
- **Secondary:** Test and ally with worthy cultivators (Wang Lin).

### Needs
- The Da Lou Sword Sect's stability
- Strong allies (Wang Lin)

### Resources
- Third Step cultivation (Nirvana Void)
- The Da Lou Sword Sect
- Sword techniques (challenged Wang Lin to take 3 sword strikes)

### Fears
- The Da Lou Sword Sect declining
- The All-Seer's schemes (he helped defend Wang Lin against All-Seer)

### Knowledge
- **Knows:** The Da Lou Sword Sect's techniques. The Heavenly Fate Sect's political landscape. Wang Lin's capability (after testing him with 3 sword strikes).

### Trigger conditions
1. A worthy cultivator appears → test (3 sword strikes) and potentially ally
2. The Da Lou Sword Sect is threatened → defensive response
3. An ally is threatened by a mutual enemy → coordinated defense (he helped defend Wang Lin against All-Seer)

### Known actions in canon
- Challenged Wang Lin to take 3 sword strikes (Wang Lin survived all 3)
- Helped defend Wang Lin against All-Seer
- Allied during the East Demon Spirit Sea arc
- Helped with the void Moongazer Serpent incident

### Decision style
`cautious_conservative` + `reactive_opportunist` — He tests before allying. He defends when threatened. He seizes opportunities (East Demon Spirit Sea arc).

### Relationships
- **Wang Lin:** Ally (after the 3-sword-strike test). Mutual aid.
- **All-Seer:** Adversary (helped defend Wang Lin against him).
- **Bai Wei:** Associated (she was caught in All-Seer's plot).

### Canon confidence: C4
### Source: CANON_RI_COMPLETE_WORLD.md N39

---

## CD-26. Lian Daofei (连道非) — ally / disciple (Celestial Bloodline)

- **NPC_ID:** N61
- **Role:** ally / disciple
- **Faction:** Xiangang Continent; brother of Lian Daozhen
- **Peak realm:** Eight Extremities Great Heavenly Venerable (new Immortal Emperor)
- **Status:** Alive; new Immortal Emperor. At the edge of canon, alive.

### Goals
- **Primary:** Fuse supreme Immortal bloodline into a worthy vessel (Wang Lin — achieved).
- **Secondary:** Inherit the Eight Extremities Great Heavenly Venerable title (achieved — after Lian Daozhen's failure).
- **Tertiary:** Suppress beast souls of various provinces (ongoing).

### Needs
- A worthy vessel for his bloodline (Wang Lin)
- The Eight Extremities inheritance
- Provinces to protect from beast souls

### Resources
- Supreme Immortal bloodline (fused into Wang Lin)
- Eight Extremities Great Heavenly Venerable cultivation (after awakening)
- Gravitation Art (taught to Wang Lin — wait, Wang Lin taught HIM Gravitation Art)

### Fears
- His bloodline failing to take (it succeeded — Wang Lin received the Indestructible Immortal Body)
- His brother Lian Daozhen's schemes (Lian Daozhen is Wang Lin's enemy)

### Knowledge
- **Knows:** The supreme Immortal bloodline. The Indestructible Immortal Body. The Eight Extremities inheritance. The Lian family's internal politics (his brother is an antagonist).

### Trigger conditions
1. A worthy vessel for his bloodline appears → fuse bloodline (he did this inside the Nether Beast with Wang Lin)
2. His brother threatens his allies → oppose (complex family dynamics)
3. The provinces are threatened by beast souls → suppress them (ongoing duty)

### Known actions in canon
- Inside the Nether Beast, fused his supreme Immortal bloodline into Wang Lin's body, imparting the Indestructible Immortal Body
- Sparred with Wang Lin
- Wang Lin made him a disciple (taught him Gravitation Art)
- Rescued by Lian Daozhen on the IAC
- After Lian Daozhen failed to inherit the Immortal Ancestor's plan, Lian Daofei's bloodline awakened → inherited Eight Extremities Great Heavenly Venerable title → new Immortal Emperor
- Continues suppressing beast souls of various provinces

### Decision style
`protective_loyalist` + `curious_explorer` — He is loyal to Wang Lin (his "master" in a reversal — Wang Lin taught him Gravitation Art). He explores his bloodline's potential.

### Relationships
- **Wang Lin:** Reciprocal (bloodline donor + disciple). Complex — Wang Lin is both his "master" (taught him Gravitation Art) and his "vessel" (received his bloodline).
- **Lian Daozhen:** Brother (adversarial — Lian Daozhen is Wang Lin's enemy).

### Inheritance offered
- **Indestructible Immortal Body / supreme Immortal bloodline** — Type: Unique (one-time fusion). Wang Lin received it. The player could receive a similar bloodline IF they find Lian Daofei and he deems them worthy.

### Canon confidence: C5
### Source: CANON_RI_COMPLETE_WORLD.md N61

---

## CD-27. Tuo Sen (拓山) — rival → ally (Ancient God)

- **NPC_ID:** N63
- **Role:** rival → ally
- **Faction:** Ancient Clan (Tu Si's demonic thought)
- **Peak realm:** 8-Star Ancient God (potential)
- **Status:** Alive; reconciled. At the edge of canon, alive.

### Goals
- **Primary (initial):** Claim Tu Si's "knowledge" inheritance (Wang Lin had it). Hunt Wang Lin.
- **Primary (after reconciliation):** Reincarnate and cultivate (Wang Lin helped him).

### Needs
- **Initial:** Tu Si's knowledge inheritance (to complete his power)
- **After reconciliation:** A new body / reincarnation (Wang Lin helped)

### Resources
- 8-Star Ancient God potential (Tu Si's "power" inheritance)
- Trapped in Tu Si's body for 1000+ years
- Demonic thought nature (aggressive, powerful)

### Fears
- Being trapped forever (he was trapped for 1000+ years)
- Wang Lin claiming both inheritances (Wang Lin did claim the knowledge inheritance)

### Knowledge
- **Knows:** Tu Si's "power" inheritance. The Land of the Ancient God's interior. Wang Lin's location (he hunted him).

### Trigger conditions
1. The "knowledge" inheritance is claimed by another → hunt them (he hunted Wang Lin)
2. He is freed from Tu Si's body → pursue the knowledge inheritance holder
3. The knowledge inheritance is returned to him → reconcile (Wang Lin returned it after obtaining the Dao Ancient inheritance)

### Known actions in canon
- Born from Tu Si's failed Ink Flow Split Soul Technique — got Tu Si's "power" inheritance
- Hunted Wang Lin (who had Tu Si's "knowledge" inheritance)
- Trapped in Tu Si's body 1000+ years
- Schemed against by Wang Lin (borrowed-knife kill via Corpse Yin Sect + Allheaven)
- Injured by Wang Lin's Ji Realm
- After Wang Lin obtained Dao Ancient inheritance, Wang Lin returned the memory inheritance
- Reincarnated on a planet formed from Ye Mo's left eye; received Wang Lin's help

### Decision style
`aggressive_expansionist` (initial) → `cautious_conservative` (after reconciliation) — He hunted aggressively until the inheritance was returned, then reconciled.

### Relationships
- **Wang Lin:** Rival → ally (after the memory inheritance was returned). Complex — they are technically co-inheritors of Tu Si's legacy.
- **Tu Si:** "Father" / origin. Tuo Sen is his demonic thought.

### Canon confidence: C5
### Source: CANON_RI_COMPLETE_WORLD.md N63

---

# Part 6 — Faction Leader NPCs

These NPCs lead the 45 factions documented in CANON_RI_CIVILIZATION.md. They make decisions that affect their faction's behavior.

---

## CD-28. Lu Yun / Huang Long Zhenren (陆云 / 黄龙真人) — Heng Yue Sect master (secret identity)

- **NPC_ID:** N25
- **Role:** mentor / secret benefactor / sect master
- **Faction:** Four Divine Sect / Vermilion Bird Divine Sect / Heng Yue Sect (secret identity: Huang Long Zhenren, Heng Yue Sect master)
- **Peak realm:** Void Flame Cultivator (5th-Gen Vermilion Bird Divine Emperor)
- **Status:** Dead (passed away after passing on the lineage). At the edge of canon, deceased.

### Goals
- **Primary:** Pass on the Vermilion Bird lineage to the next generation (Wang Lin — achieved).
- **Secondary:** Gather intelligence on Qing Shui (he infiltrated the Cultivation Alliance HQ).
- **Tertiary:** Maintain the Heng Yue Sect as a cover identity.

### Needs
- A worthy Vermilion Bird successor (Wang Lin)
- The Vermilion Bird Sequence (transmitted to Wang Lin)
- The Heng Yue Sect's stability (as cover)

### Resources
- 5th-Gen Vermilion Bird Divine Emperor cultivation
- The Vermilion Bird Sequence
- The Heng Yue Sect (as secret master)
- The Vermilion Bird Nine Mysterious Transformations (taught to Wang Lin)

### Fears
- The Vermilion Bird lineage dying out (he endured the Heavenly Decay Tribulation awaiting the next generation)
- His secret identity being discovered (it would compromise the Heng Yue Sect)

### Knowledge
- **Knows:** The Vermilion Bird lineage's full history. The Heng Yue Sect's true nature (a front for the Vermilion Bird lineage). Qing Shui's location (he infiltrated the Cultivation Alliance HQ to gather intel).

### Trigger conditions
1. A worthy Vermilion Bird successor appears in the Heng Yue Sect → transmit the Vermilion Bird Sequence (he did this for Wang Lin)
2. The Heavenly Decay Tribulation approaches → endure it (he did)
3. Intel on Qing Shui is needed → infiltrate (he did)

### Known actions in canon
- Infiltrated the Cultivation Alliance HQ to gather info on Qing Shui
- Endured the Heavenly Decay Tribulation awaiting the next generation's Vermilion Bird
- Taught Wang Lin the Vermilion Bird Nine Mysterious Transformations
- Gave Wang Lin the Vermilion Bird Sequence
- Died after returning from the Cultivation Alliance

### Decision style
`patient_planner` + `protective_loyalist` — He waited generations for Wang Lin. He maintained a secret identity for decades.

### Relationships
- **Wang Lin:** Secret benefactor. Transmitted the Vermilion Bird Sequence.
- **Qing Shui:** Intelligence target (he infiltrated the Cultivation Alliance to find Qing Shui).
- **Heng Yue Sect:** Cover identity. Protected.

### Inheritance offered
- **Vermilion Bird Sequence** — Type: Unique (one inheritor per generation). Wang Lin received it. The player could receive it IF they join the Heng Yue Sect and Lu Yun (or his successor) deems them worthy. At the edge of canon, Lu Yun is dead — the sequence has been passed to Wang Lin.
- **Vermilion Bird Nine Mysterious Transformations** — Type: Unique (Vermilion Bird lineage).

### Canon confidence: C4
### Source: CANON_RI_COMPLETE_WORLD.md N25

---

## CD-29. Old Man Miesheng / Old Man Samsara-Extinction (灭生老人)

- **NPC_ID:** N115
- **Role:** complex antagonist
- **Faction:** independent
- **Peak realm:** peak Third Step+
- **Status:** Alive. At the edge of canon, alive.

### Goals
- Unknown (canon-ambiguous). He lent the Realm-Defining Compass to Lu Mo, which released Tianyunzi. His motivations are unclear — possibly curious observation, possibly scheming.

### Needs
- Unknown

### Resources
- Peak Third Step+ cultivation
- The Realm-Defining Compass (he owned it and lent it to Lu Mo)
- Independent power base

### Fears
- Unknown

### Knowledge
- **Knows:** The Realm-Defining Compass's nature. Tianyunzi's nature (artifact spirit). Possibly the full Cave World structure (he is powerful enough to lend cosmic-tier artifacts).

### Trigger conditions
1. Unknown — he acts on his own obscure criteria
2. A cultivator borrows his artifacts → he lends them (he lent the Compass to Lu Mo)

### Known actions in canon
- Lent the Realm-Defining Compass to Lu Mo
- Lu Mo blasted it open using Dream Dao, releasing Tianyunzi (the artifact spirit)

### Decision style
`curious_explorer` (inferred) — He lends artifacts to cultivators and observes the outcomes. He does not intervene directly.

### Relationships
- **Lu Mo:** Lent him the Compass. Observer relationship.
- **Tianyunzi:** Indirect connection (the Compass's spirit was released by Lu Mo's use of the borrowed artifact).

### Canon confidence: C4
### Source: CANON_RI_COMPLETE_WORLD.md N115

---

## CD-30. The All-Seer's 7 Color-Division Disciples

- **NPC_IDs:** N113 (Tianyunzi, purple division — separate entry above), plus the other 6 color-division leaders (red/orange/yellow/green/blue/cyan)
- **Role:** antagonists (subordinates of All-Seer)
- **Faction:** Heavenly Fate Sect — 7 color divisions
- **Peak realm:** Various (Third-Step tier)
- **Status:** Most killed or scattered after the All-Seer's death. At the edge of canon, mostly deceased.

### Goals
- **Primary:** Serve the All-Seer (they are his tools).
- **Secondary:** Advance within their respective color divisions.

### Needs
- The All-Seer's favor
- Their division's power and resources

### Resources
- Their respective color divisions (red/orange/yellow/green/blue/cyan/purple)
- Third-Step-tier cultivation (varies)
- The Heavenly Fate Sect's infrastructure

### Fears
- The All-Seer's displeasure
- Their division declining relative to others
- The All-Seer's possession plot being discovered (they are tools, not conspirators — they may not know the full plan)

### Knowledge
- **Knows:** Their respective division's techniques. The Heavenly Fate Sect's internal politics.
- **Does NOT know (likely):** The All-Seer's full possession plot. The true nature of Tianyunzi (artifact spirit).

### Trigger conditions
1. The All-Seer commands → execute
2. Their division is threatened → defensive response
3. A cultivator threatens the Heavenly Fate Sect → coordinated defense

### Known actions in canon
- Yao Xinghai (Blood Ancestor) was the Red Division leader (separate entry — CD-07)
- The other 6 divisions are less individually documented
- After the All-Seer's death, the divisions scattered

### Decision style
`cautious_conservative` (as subordinates) — They follow the All-Seer's commands. They do not act independently (with the exception of Yao Xinghai, who had his own agenda).

### Relationships
- **All-Seer:** Master. Loyal (mostly).
- **Each other:** Rival divisions (competing for the All-Seer's favor).
- **Wang Lin:** Enemy (as the All-Seer's target, they were positioned against him, though some may not have understood why).

### Canon confidence: B/4 (the 7 divisions are canon-attested; individual disciples beyond Yao Xinghai and Tianyunzi are less documented)
### Source: CANON_RI_COMPLETE_WORLD.md N21; CANON_RI_CIVILIZATION.md CIV-06

---

# Part 7 — Reincarnation-Linked NPCs (cross-life connections)

These NPCs are connected to Wang Lin across multiple lives via the Samsara Dao. They are the most complex NPCs in the canon.

---

## CD-31. Wang Ping (王平) — son

- **NPC_ID:** N07
- **Role:** family
- **Faction:** none (mortal, intentionally raised as mortal by Wang Lin)
- **Peak realm:** mortal (intentionally)
- **Status:** Dies (mortal lifespan); reincarnated; reunited with Wang Lin. At the edge of canon, reincarnated and reunited.

### Goals
- **Primary:** Live a simple mortal life (Wang Lin raised him this way intentionally).
- **Secondary:** Be with his father (achieved after reincarnation).

### Needs
- A peaceful mortal existence
- His father's presence

### Resources
- None (intentionally mortal)

### Fears
- Unknown (he was raised as a mortal; his fears would be mortal-scale)

### Knowledge
- **Knows:** Only mortal-scale knowledge (Wang Lin intentionally kept him ignorant of cultivation)
- **Does NOT know:** Wang Lin's true nature, cultivation, or the cosmic-tier events happening around him

### Trigger conditions
1. His father visits → respond as a mortal child would
2. His mortal lifespan ends → death (which triggered Wang Lin's Karma Domain evolution)

### Known actions in canon
- Raised in a desolate village by Wang Lin as a mortal
- His death triggers Wang Lin's Karma Domain evolution
- Reincarnates; eventually reunited with Wang Lin

### Decision style
N/A (mortal — no cultivation-scale decision-making)

### Relationships
- **Wang Lin:** Father. The emotional core of Wang Lin's Karma Domain evolution.
- **Liu Mei / Mu Bingmei:** Mother (who refined him into a resentful spirit out of hatred for Wang Lin).

### Canon confidence: C5
### Source: CANON_RI_COMPLETE_WORLD.md N07

---

## CD-32. Wang Yiyi (王一一) — daughter

- **NPC_ID:** N08
- **Role:** family
- **Faction:** Vast Dao Palace (Saintess) → mask spirit (in AWWP)
- **Peak realm:** Paragon-tier (in AWWP, Saintess of the Vast Dao Palace)
- **Status:** Alive (married to Wang Baole in AWWP); eventually brought back to the Xiangang Continent by Wang Lin. At the edge of canon, alive.

### Goals
- **Primary:** Be with Wang Baole (her husband in AWWP).
- **Secondary:** Escape the destruction of the Vast Dao Palace (achieved — she inhabited a mask).

### Needs
- Wang Baole's presence
- The mask (her vessel after escaping the Vast Dao Palace)

### Resources
- Paragon-tier cultivation (in AWWP)
- The mask (her vessel)
- Experiences from dozens of reincarnations during the Weiyang Boundary

### Fears
- The Vast Dao Palace's destruction (it happened — she escaped via the mask)
- Separation from Wang Baole

### Knowledge
- **Knows:** The Vast Dao Palace's history. The Weiyang Boundary reincarnations. Wang Baole's nature.

### Trigger conditions
1. The Vast Dao Palace is destroyed → escape (she did, via the mask)
2. Wang Baole is threatened → protective response
3. Her father (Wang Lin) calls → respond (Wang Lin eventually brought her back to the Xiangang Continent)

### Known actions in canon
- Escaped the destruction of the Vast Dao Palace by inhabiting a mask
- Accompanied Wang Baole (her future husband) in AWWP
- Experiences dozens of reincarnations during the Weiyang Boundary
- Eventually brought back to the Xiangang Continent by Wang Lin

### Decision style
`protective_loyalist` — She is loyal to Wang Baole and her father.

### Relationships
- **Wang Lin:** Father.
- **Li Muwan:** Mother.
- **Wang Baole:** Husband (AWWP protagonist).

### Canon confidence: C5
### Source: CANON_RI_COMPLETE_WORLD.md N08

---

# Part 8 — The Edge-of-Canon World State

> **Architectural note (user directive):** Every Er Gen novel happened exactly as written. Every protagonist reached the furthest canonical point. They are frozen at the edge of recorded history. Their manifestations return to their home worlds. Their true bodies remain at the edge of canon until the player reaches that point. They continue living once canonical time resumes.

## The RI dimension at the edge of canon:

### Cosmic structure
- The Seven-Colored Daoist is DEAD. Wang Lin killed him and became the new Cave World owner.
- The Cave World is now "Wang Lin's Cave World."
- The Realm-Sealing Grand Array has been DISSOLVED (Wang Lin dissolved it after killing the Daoist).
- The Heaven-Splitting Axe's status: cooperative (it cooperated with Wang Lin due to his Restriction Essence).
- The Sealed Realm / Outer Realm boundary: OPEN (the seal is gone).
- The Cave World's connection to the IAC: OPEN (Wang Lin went to the IAC and became the 10th Sun).

### Wang Lin's state
- Alive; Transcendent (4th Step / Heaven Trampling)
- Eternally with Li Muwan (resurrected)
- Cave World owner
- 14 Essences + 9 Heaven Trampling Bridges complete
- Heaven-Defying Bead fused with primordial spirit
- His manifestation exists in the lower worlds; his true body is at the edge of recorded history

### Key NPC states
- **Li Muwan:** Alive; resurrected; transcendent with Wang Lin
- **Situ Nan:** Alive; fulfilled (became prince on IAC as "Si Nan")
- **All-Seer:** DEAD (killed by Wang Lin)
- **Seven-Colored Daoist:** DEAD (killed by Wang Lin)
- **Teng Huayuan:** DEAD (killed by Wang Lin; soul refined)
- **Daoist Water:** DEAD (killed by Wang Lin)
- **Blood Ancestor:** DEAD (killed by Wang Lin)
- **Yao Xixue:** Alive; amnesiac; wandering with father's remnant soul
- **Qing Shui:** Alive; Third Step (reincarnated on IAC)
- **Xuan Luo:** Alive; undergoing reincarnation (Wang Lin gifted him a protective jade slip)
- **Tuo Sen:** Alive; reconciled (reincarnated, received Wang Lin's help)
- **Gu Dao:** Alive; acknowledges Wang Lin as superior
- **Lian Daofei:** Alive; new Immortal Emperor (Eight Extremities)
- **Zhou Yi + Qing Shuang:** Alive; reincarnated on IAC
- **Hong Die:** Alive; reincarnated as Qing Hong on IAC (no past-life memories)

### What this means for the player
The player enters a world where:
1. The Sealed Realm's seal is GONE (Wang Lin dissolved it). The player can travel freely between Sealed Realm and Outer Realm.
2. The Cave World's owner is Wang Lin (not the Seven-Colored Daoist). Wang Lin is at the edge of canon — his manifestation may be present, his true body is elsewhere.
3. The canonical antagonists (All-Seer, Seven-Colored Daoist, Teng Huayuan, Daoist Water, Blood Ancestor) are all DEAD. The player faces a DIFFERENT set of challenges — not the canonical antagonist structure.
4. Wang Lin's allies (Situ Nan, Qing Shui, Xuan Luo, etc.) are alive on the IAC or reincarnated. They are Wang Lin's allies, not the player's. The player must build their own relationships.
5. The inheritances (Tu Si's legacy, Heaven-Defying Bead, Vermilion Bird Sequence, etc.) have already been claimed by Wang Lin. HOWEVER — per the user's "protagonist finds a way" directive (see CANON_RI_EDGE_OF_CANON.md Part 4, Inheritance Registry): even unique inheritances have a protagonist access path. The player can learn partial versions, find parallel sites, forge their own equivalents, or earn transmission from Wang Lin's disciples. "Unique" means "one holder at a time," not "only one person ever." The player is a protagonist; the path exists.

> **Critical design implication (reframed per user directive — see CANON_RI_EDGE_OF_CANON.md):**
> The player is NOT retelling Wang Lin's story. The player enters **Wang Lin's branch at the edge of recorded canon** — NOT a "post-Wang-Lin world." Wang Lin EXISTS: his true body is on the IAC (canonical time paused), his manifestation travels with the player. The world is permanently shaped by Wang Lin's history, while Wang Lin himself still exists. The canonical antagonists are dead (do not resurrect them). The threats are NOT replacement NPCs — the threats are the UNIVERSE itself: vacuums, consequences, new generations, ruined formations, altered spirit veins. The real conflict is "how does a civilization rebuild after Wang Lin changed the world?" The player must find or create their own path — but they do so in a world where Wang Lin is present (as manifestation companion) and his history is the texture of reality.

---

## Document Status

- **Version:** 1.0
- **Date:** 2026-07-11
- **Total decision-making NPCs documented:** 32 (of 132 named)
- **NPCs not documented here:** The remaining ~100 are family members, servants, minor allies, and tangential NPCs who do not make world-affecting decisions. They are documented in CANON_RI_COMPLETE_WORLD.md and ri_canon_database.json.
- **Canon confidence:** All entries are C5 or C4 (explicit or strongly implied). C3 and C2 are avoided for decision-making data — if canon is silent on an NPC's motivation, that is noted rather than filled with speculation.
- **Prime Directive compliance:** Every goal, need, resource, fear, and knowledge entry is derived from canon. Where canon is silent, "Unknown" is stated rather than invented. No generic fantasy content has been added.
- **Next steps:** This document feeds the NPC decision engine (goal-based planner) that will be built in build-phase 4. The engine will use these profiles to drive autonomous NPC behavior: each NPC evaluates their goals against the current world state, selects available actions, and executes them — whether or not the player is present.
