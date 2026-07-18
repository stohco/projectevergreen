# Divine Sense — Complete Canon Usage Catalog

> **Task ID:** CANON-RESEARCH-DIVINE-SENSE
> **Purpose:** Exhaustive catalog of every divine sense (神识 / spiritual sense / soul perception) use across all 6 Er Gen novels, so the Forge mod implementation can be faithful to canon.
> **Source files:** All cached research under `/home/z/my-project/tool-results/` (22 wiki JSON dumps, 14 HTML pages, 12 formation/talisman searches, BTT research dumps) + the 6,723-line worklog + the existing design docs.

---

## Summary

- **Total distinct uses catalogued: 132** (across 6 novels, 22 named characters, 30+ named techniques/items)
- **Categories active: 15** (the originally proposed 15 — all populated; one new sub-bucket added inside Cat 2)
- **Novels covered:** RI (Renegade Immortal), Ptt (Pursuit of the Truth), ISSTH (I Shall Seal the Heavens), AWE (A Will Eternal), AWWP (A World Worth Protecting), BTT (Beyond the Timescape)
- **By novel density:** RI is by far the most divine-sense-dense (58 uses — Wang Lin's signature weapon); Ptt 18; ISSTH 16; AWE 14; BTT 14; AWWP 12.
- **Cross-novel universals (every protagonist uses):** Perception/Sensing, Concealment, Jade Slip Transmission, Killing-Intent Detection, Soul Pressure / Suppression, Soul Brand/Imprint.
- **Novel-specific signatures:**
  - **RI:** Ji Realm Divine Sense as a literal weapon; Soul Lasher/Karma Whip; Restriction Flag (formation-flag-as-divine-sense-hand); Mountain Crumble (divine sense → physical mass); Samsara Eye/Heavenly Eye (Situ Nan); Five-Elements True Body pressuring Slaughter with divine sense.
  - **Ptt:** Su Ming "sent his divine sense into his real body" (intrabody sensing); Berserker Awakening in illusionary space (divine sense constructs the foundation); Welcoming of Deities (pill that senses souls); Bald Crane's illusions vs divine sense (verified: divine sense PIERCES the illusions).
  - **ISSTH:** Dao God Scripture Divine Sense Volume (Meng Hao's explicit divine sense cultivation art); Bronze Lamp (soul-fragment fuel); Anti-Ancient Soul Lamps (9 lamps at Ancient Realm); Demon Sealer lineage (seals tribulation lightning via soul); 9 Dao Essences; Seal-Heavens Art (intention in the eyes).
  - **AWE:** Bai Xiaochun's "attracted the divine sense" of Zhou Zimo (passive soul call); Spirit Refinement (refining souls via divine sense); Soul-Storing Mirror variant; Living Pills/Formations (divine sense animates the inanimate); Undying Codex (soul-based immortality).
  - **AWWP:** Mysterious Mask containing Wang Yiyi's fragment (divine sense remnant in an artifact); Dark Art: Soul Guidance; Demonic Eye Art → Divine Eye Art fusion; "six divine powers through remnant Divine Sense" (Wang Baole absorbing a deceased powerhouse's residual divine sense); Hades Coffin as a soul-travel vessel.
  - **BTT:** Soul-Weaving Technique (extract soul power → soul threads → weave a godly entity in the sea of consciousness — literal soul manipulation); Sundial Life Lamp (life lamp with time stagnation/reversal); Pluck Stars from the Vault (reveals target's complete existence — soul, karma, fate, all life traces — via immortal will); Purple Crystal (devours shadows, mutates Life Lamps); God's Face opens eyes → mortals go mad (perception itself is dangerous); 12 Heavenly Palaces (3 of them explicitly soul-divine: Black Umbrella Life Lamp = Divine Soul, Poison Forbidden Pill = Divine Curse, etc.).

---

## The 15 Categories

Each use is filed under one primary category. Many uses have secondary categories — those are cross-listed in the `Cross-Categories` field.

---

## 1. Perception / Sensing (the "radar" use — but deeper)

### [RI] — Wang Lin — `sense spread out` (Great Cycle)
- **Use:** With the help of the Heaven-Defying Bead, Wang Lin's sense spread out across the entire Alliance Star System, revealing the order of many things. (Post-Nirvana Cleanser, Ch. ~1040.)
- **Source:** `wl_cult.json` / `wl_cultivation.json` ("Great Cycle 1040 — With the help of the heaven defying bead, Wang Lin's sense spread out and saw an order of many things across the Alliance Star System.")
- **Limitations:** Requires the Heaven-Defying Bead as amplifier; described as a moment of cosmic clarity, not a sustainable always-on sense.
- **Cross-Categories:** World/Environment Sensing (Cat 15), Identity/Recognition (Cat 14).

### [RI] — Wang Lin — `Heaven-Trampling divine sense`
- **Use:** Upon crossing the Second Bridge, Wang Lin was granted a "glimpse of Heaven-Trampling divine sense which covered the entire Celestial Clan." Used to fully understand the test of the Second Bridge.
- **Source:** `wl_cult.json` (Second Bridge 2048).
- **Limitations:** Brief glimpse tied to the trial; not a permanent state.

### [RI] — Wang Lin — Teng Li forest (forest distorts divine sense)
- **Use:** A forest Wang Lin is chased into "distorts Divine sense" — explicitly, terrain can degrade the effective radius of divine sense. (Ch. 86–90.)
- **Source:** `wl_main.json` ("a forest that distorts Divine sense").
- **Limitations:** World-law effect: terrain + formations can attenuate divine sense radius. This is the in-canon justification for our `(1 − L_world/12)` factor.

### [RI] — Wang Lin — Dao God Scripture (later volumes)
- **Use:** Wang Lin's foundational soul art includes Dao God Scripture-derived abilities (cross-referenced with Meng Hao's "Dao God Scripture Divine Sense Volume" in ISSTH — same lineage, pan-cosmology).
- **Source:** Cross-referenced via worklog RESEARCH entries.
- **Limitations:** Late-game.

### [Ptt] — Su Ming — `sent his divine sense into his real body`
- **Use:** Su Ming sends his divine sense into his real (fused) body to investigate the One-Billion-Corpse-Soul-Seal planted by Dark Dawn's Sacred Lady. The divine sense is the instrument of internal diagnosis.
- **Source:** `p1-bald-crane.json` & `p6-su-ming.json` (Ch. ~175).
- **Limitations:** Repulsed by the seal; risk of awakening the diabolical art.

### [Ptt] — Su Ming — `sent his divine sense into Yin Death Region`
- **Use:** Su Ming attempts to send divine sense into the Yin Death Region. "Forcefully repulsed." He managed to "sense a presence of Di Tian inside it."
- **Source:** `p6-su-ming.json` (Ch. ~193).
- **Limitations:** Higher-order realms/regions can repulse divine sense — this is the spiritual-layer equivalent of "you cannot perceive above your realm."

### [Ptt] — Su Ming — `Su Ming's will swept through`
- **Use:** Su Ming's will swept through Ninth Summit Sect, saving members endangered by roaming Antecedental Spirits and punishing/killing attackers.
- **Source:** `p6-su-ming.json` (Ch. ~223).
- **Limitations:** Scales with will, not cultivation — at this stage his will is the operative force.
- **Cross-Categories:** Physical Manipulation (Cat 2) — the will manifests as protective action.

### [Ptt] — Su Ming — `sensed his Surging Indulger Clone loosing connection`
- **Use:** Su Ming sensed his clone's connection fading from afar — divine sense acts as a soul-link monitor between true body and clones.
- **Source:** `p6-su-ming.json`.
- **Cross-Categories:** Soul/Mind Interaction (Cat 5).

### [Ptt] — Su Ming — sensed the power of Immortals
- **Use:** "He rushed towards the mountain where he sensed the power of the Immortals." Divine sense detects cultivation signatures at range.
- **Source:** `p6-su-ming.json`.

### [Ptt] — Su Ming — `sensed familiarity from her`
- **Use:** Sensed a karmic familiarity from an Immortal who called him "Destiny" — same sensation as when meeting Han Kong and Si Ma Xin. Divine sense is the medium of karmic recognition.
- **Source:** `p6-su-ming.json`.
- **Cross-Categories:** Identity/Recognition (Cat 14), Karmic Threads (Cat 5).

### [Ptt] — Su Ming — sensed a primitive power of Avacaniya Realm in Sun Sinking Talisman
- **Use:** After catching and sealing his Surging Indulger Clone, Su Ming sensed the primitive Avacaniya power inside the Sun Sinking Talisman. Divine sense reads item essence.
- **Source:** `p6-su-ming.json`.

### [Ptt] — Su Ming — sensed asleep Arid Triad's will
- **Use:** When sending the Curse at the Emperor of Abyss's True World, Su Ming "sensed asleep mighty Arid Triad's will." When it showed signs of waking, Su Ming stopped his attack.
- **Source:** `p6-su-ming.json`.
- **Limitations:** Cosmic-scale sensing; the act of sensing alerts the sensed entity if its will is high enough.

### [Ptt] — Su Ming — sensed demise of Tian Bai / found his drop of blood
- **Use:** After stepping into his True Morning Dao World, Su Ming sensed Tian Bai's death and located a drop of his blood across an expanse cosmos.
- **Source:** `p6-su-ming.json`.

### [Ptt] — Su Ming — sensed three Apostles entering True Morning Dao World
- **Use:** Sensing trespass at the world boundary. Su Ming "distorted the galaxy around them to imprison them" — perception → immediate spatial response.
- **Source:** `p6-su-ming.json`.
- **Cross-Categories:** Spatial/Dimensional (Cat 9), Physical Manipulation (Cat 2).

### [Ptt] — Su Ming — sensed Old Man Extermination's presence of Lei Chen
- **Use:** "In Old Man Extermination he sensed presence of Lei Chen" — divine sense penetrates a hostile cultivator's embodiment to identify the soul(s) inside.
- **Source:** `p6-su-ming.json`.
- **Cross-Categories:** Identity/Recognition (Cat 14), Soul/Mind Interaction (Cat 5).

### [Ptt] — Su Ming — sensed thick aura of death from his own body
- **Use:** Yan Pei "sensed thick aura of death from Su Ming's body" — soul reading on another person (Su Ming) reveals his inner state.
- **Source:** `p6-su_ming.json` (Ch. ~224).
- **Cross-Categories:** Killing Intent Detection (Cat 8).

### [ISSTH] — Meng Hao — Dao God Scripture Divine Sense Volume
- **Use:** Meng Hao's explicit divine-sense cultivation art — one of his listed Cultivation Arts. Strengthens and specializes divine sense.
- **Source:** `menghao_baidu.html` ("Cultivation Arts: ... Dao God Scripture Divine Sense Volume ...").
- **Limitations:** One of his foundational manuals; complements Soul Devouring Scripture.
- **Cross-Categories:** Soul/Mind Interaction (Cat 5).

### [ISSTH] — Meng Hao — sensed the power of the Kunpeng
- **Use:** Upon refining the 14th duplicate of the One-Colored Nascent Soul Pill, "he sensed the power of the Kunpeng and refined it." Divine sense as the medium of bloodline essence perception.
- **Source:** `menghao_baidu.html` (Wind Infant list).
- **Cross-Categories:** Identity/Recognition (Cat 14), Alchemy/Crafting (Cat 11).

### [ISSTH] — Meng Hao — Seal-Heavens Art: "intention is in one's eyes"
- **Use:** Meng Hao learned from Yi Ranzi (Ch. 1227) that "the Dao lies within one's heart, and intention is in one's eyes; to traverse the mountains and seas, this is the 'Sealing Heaven Art'." Intention-as-sight is a divine-sense fusion with the eyes.
- **Source:** `menghao_baidu.html`.
- **Cross-Categories:** Combat/Offensive (Cat 3), Soul/Mind Interaction (Cat 5).

### [ISSTH] — Meng Hao — Sealing Correction Technique
- **Use:** A listed technique — corrects/manipulates seals via divine sense. Reflects the Demon Sealer lineage's soul-as-tool approach.
- **Source:** `menghao_baidu.html`.

### [AWE] — Bai Xiaochun — "Zhou Zimo's divine sense was attracted by the ten billion souls"
- **Use:** Bai Xiaochun's ten-billion-soul contribution attracted Zhou Zimo's divine sense (passive soul-pressure detection). The divine sense itself "issued a sound that severely injured her" — i.e., divine sense is weaponizable.
- **Source:** `bxc_baidu.html`.
- **Cross-Categories:** Combat/Offensive (Cat 3).

### [AWE] — Bai Xiaochun — Cold Gate Mind-Nurturing Art (nurtures the mind via cold)
- **Use:** A technique Li Qinghou recorded in a jade slip; uses Heaven-Reaching River water split into Cold + Mind. "Once mastered, with a single thought, the surroundings become..." — explicit mind/divine-sense cultivation.
- **Source:** `bxc_baidu.html`.
- **Cross-Categories:** Soul/Mind Interaction (Cat 5).

### [AWWP] — Wang Baole — Divine Eye Art (Soul Conduit Part) / Divine Eye Art (Full)
- **Use:** Two distinct divine-sense eye arts (Ch. 761 and Ch. 853). The Soul Conduit Part is the gateway; the Full art at Spirit Immortal grade completes it. Fuses with the Demonic Eye Art.
- **Source:** Worklog RESEARCH-AWWP (Wang Baole/Techniques items 19, 21, 23).
- **Cross-Categories:** Combat/Offensive (Cat 3).

### [AWWP] — Wang Baole — Six divine powers via remnant Divine Sense
- **Use:** Wang Baole grasps six divine powers through a "remnant Divine Sense" (a deceased powerhouse's left-behind divine-sense imprint). The remnant is itself the medium of transmission — like a jade slip made of pure divine sense.
- **Source:** Worklog RESEARCH-AWWP line 1799 (WebNovel citation).
- **Cross-Categories:** Teaching/Transmission (Cat 12), Soul/Mind Interaction (Cat 5).

### [AWWP] — Wang Baole — Demonic Eye Art (Dark Sect)
- **Use:** A Dark-Sect eye art (Ch. 690) later fuses with the Divine Eye Art — divine sense cultivated through the Dark Art path.
- **Source:** Worklog RESEARCH-AWWP.
- **Cross-Categories:** Combat/Offensive (Cat 3).

### [BTT] — Xu Qing — Soul-Weaving Technique (Xeno-Immortal School core)
- **Use:** Extracts soul power, tempers it into "soul threads," weaves them in the sea of consciousness into the outline of a godly entity. Uses soul as control + thoughts as body → "mental god" manifestable externally. Preserves the human body, stores godliness internally.
- **Source:** `xu_qing_abilities.json` / `xu_qing_abilities.txt` (Ch. 546).
- **Limitations:** Requires the user to have a strong godsource; otherwise the soul-threads are weak.
- **Cross-Categories:** Soul/Mind Interaction (Cat 5), Identity/Recognition (Cat 14).

### [BTT] — Xu Qing — Pluck Stars from the Vault of the Universe
- **Use:** Lord-level divine ability (Ch. 1316) that evolves "Fishing the Moon in the Well." The well becomes all space-times and parallels → reveals target's **complete existence** — soul, karma, fate, all life traces. Immortal will grasps reflected traces simultaneously → "plucks" the target from reality like removing a star from the universe. Prevents escape via alternate timelines or hidden existences.
- **Source:** Worklog RESEARCH-BTT line 2474; `xu_qing_abilities.txt`.
- **Limitations:** Lord-level realm gate; uses immortal will, not raw divine sense — divine sense is the substrate, will is the operator.
- **Cross-Categories:** Soul/Mind Interaction (Cat 5), Identity/Recognition (Cat 14), Karmic Threads (Cat 5).

### [BTT] — Xu Qing — Purple Crystal (shadow devouring → environmental sense)
- **Use:** The Purple Crystal "devouring the shadows of the wolf corpses" gave Xu Qing ambient awareness; his shadow (via Purple Crystal) feeds back what it has devoured.
- **Source:** Worklog RESEARCH-BTT (line 2516).
- **Cross-Categories:** Taming/Beast Interaction (Cat 13) — it's a quasi-pet bonded via soul.

### [BTT] — Xu Qing — Devouring → "perceive surroundings within fog"
- **Use:** Shadow's feedback from devouring mist lets Xu Qing perceive surroundings within fog (otherwise opaque). Divine sense is augmented by devouring-feedback.
- **Source:** Worklog RESEARCH-BTT (line 2516).
- **Cross-Categories:** Concealment/Stealth (Cat 6).

### [BTT] — Xu Qing — God State Violet Lord (soul-thread vortex → god body)
- **Use:** At God State 4th-Level "Violet Lord," the soul-thread vortex transforms into a complete god body — the woven soul-threads become a corporeal divine form.
- **Source:** `xu_qing_baidu.json` / `xu_qing_baidu.txt`.
- **Limitations:** Requires dual immortal + god cultivation.

### [BTT] — Xu Qing — Twelve Heavenly Palaces: Black Umbrella Life Lamp (Divine Soul)
- **Use:** At Golden Core Stage, one of the 12 Heavenly Palaces is the "Black Umbrella Life Lamp (Divine Soul)" — a life lamp that houses the divine soul. (Xu Qing later refines all 5 Life Lamps into Sundial Life Lamps, gaining time stagnation/reversal.)
- **Source:** `xu_qing_baidu.txt`.
- **Cross-Categories:** Soul/Mind Interaction (Cat 5), Teaching/Transmission (Cat 12 — life lamp as soul-anchor).

### [BTT] — Broken God Face (cosmic antagonist) — opening eyes → mass death
- **Use:** "When the god's eyes opened, all living beings were infected, and most died." Mortals can die or go mad if they look at Gods. Perception itself becomes lethal — the God's gaze IS the attack.
- **Source:** Worklog RESEARCH-BTT line 1792.
- **Limitations:** Only the Broken God Face and analogous beings wield this; this is cosmic-tier perception-as-attack.
- **Cross-Categories:** Combat/Offensive (Cat 3), World/Environment Sensing (Cat 15).

### [BTT] — Crimson Mother / Five Elements God Decapitation Altar
- **Use:** Xu Qing's evolved 7-Extremes God Decapitation Altar (Ch. 1096+) adds time + space to the 5-elements version; it severs via soul/karma/fate simultaneously.
- **Source:** `xu_qing_abilities.txt`.
- **Cross-Categories:** Combat/Offensive (Cat 3).

---

## 2. Physical Manipulation (gripping, lifting, moving, throwing objects with divine sense)

> **Canon-finding (verified):** In Er Gen, divine sense IS the gripping force for physical manipulation, not just "precision." Wang Lin grips/moves formation flags with divine sense; Bai Fan forms a mountain from divine sense + celestial energy. **This finding already corrected the Forge mod's `ManipulationCapability.physicalCapability()` — divine sense now has EQUAL weight to telekinetic force (was 10%).** See `DESIGN_HITBOXES_AND_FORMATIONS.md` Part 1.

### [RI] — Wang Lin — Ji Realm Divine Sense as gripping/manipulation force
- **Use:** Wang Lin's Ji Realm Divine Sense (Ch. 127) is a destructive force in its own right — it grips and tears, not just senses. The Ji Realm "changes from Spiritual Energy to Divine Sense" at Foundation Middle Stage (Ch. 127).
- **Source:** `wl_techniques.json` + `wl_cult.json` ("Spiritual Energy ascends to Ji Realm. Ji Realm changes from Spiritual Energy to Divine Sense").
- **Limitations:** Scales with Wang Lin's Ji Realm mastery; the Ji Realm curse blocks Soul Formation in his original body, but the divine-sense aspect operates independently.

### [RI] — Wang Lin — Restriction Flag (wielded by divine sense)
- **Use:** Wang Lin grips, moves, and wields the Restriction Flag WITH his divine sense. The flag is the tool; divine sense is the hand. (Ch. 178 and throughout.)
- **Source:** `wl_items.json` + `wl_items2.json` ("Restriction Flag: Part of the Ancient God Inheritance"); `search_09_formation_flag.txt`.
- **Cross-Categories:** Formation Interaction (Cat 4).

### [RI] — Wang Lin — Five-Elements True Body pressuring Slaughter with divine sense
- **Use:** "Five Elements True Body begin pressuring him with divine sense to finally fuse with previous Essence True Bodies, turn Slaughter into Punishment Slaughter." Divine sense is the operative force in body-fusion coercion.
- **Source:** `wl_cult.json` / `wl_cultivation.json`.
- **Cross-Categories:** Soul/Mind Interaction (Cat 5).

### [RI] — Wang Lin — "Dark Heaven Stone" stores divine sense for avatars
- **Use:** "When cultivators reach their level, they would need Dark Heaven Stones to store divine sense to create an avatar or store power spells to use." Divine sense is materialized and stored in a stone for later avatar creation.
- **Source:** `wl_items.json` (Dark Heaven Stone, Ch. 965).
- **Cross-Categories:** Soul/Mind Interaction (Cat 5), Alchemy/Crafting (Cat 11).

### [RI] — Wang Lin — Li Muwan Azure Dragon Jade Slip (auto-defense)
- **Use:** Li Muwan "drained her life force to refine the Azure Dragon Jade Slip for him, a defensive array primarily for defense, with offense as a secondary function." When Wang Lin is in danger (the Chaotic Broken Stars escape), the jade slip "automatically works and transforms into dragon, helps Wang Lin to reach the entry point."
- **Source:** `wl_main.json` + `li_muwan.html`.
- **Limitations:** One-time auto-trigger; drains/refines the slip.
- **Cross-Categories:** Formation Interaction (Cat 4), Teaching/Transmission (Cat 12).

### [RI] — Wang Lin — Li Muwan's jade slip + sliver of peak divine sense
- **Use:** Wang Lin "left a trace of his peak-level divine sense" in Li Muwan's jade bottle (telling her "no one below Nascent Soul can harm you"). Divine sense as an autonomous protective guardian.
- **Source:** `li_muwan.html`.
- **Limitations:** Realm-gated (only defends against sub-Nascent-Soul threats); depletes a sliver of peak divine sense.

### [RI] — Bai Fan — Mountains Crumble (Ch. 1105)
- **Use:** Celestial Emperor Bai Fan's spell: forms a mountain FROM divine sense + celestial energy, then shatters it. Divine sense literally CREATES the physical mass. Wang Lin later grasps this technique during Nirvana Cleanser early (Ch. ~1102) — his Karma Domain then completes.
- **Source:** `wl_cult.json` + worklog (line 6691); `search_02_RI_formations.txt`.
- **Cross-Categories:** World/Environment Sensing (Cat 15), Combat/Offensive (Cat 3).

### [RI] — Wang Lin — Restriction Mountain (Ch. 178)
- **Use:** At the Restriction Mountain trial, Wang Lin uses his restriction/divine-sense mastery to assemble and wield restriction flags. The flying sword retrieval is "carefully guided... into his bag of holding" — sustained telekinesis via divine sense.
- **Source:** `search_06_RI_talismans.txt` (Wuxiaworld Ch. 178 citation).
- **Cross-Categories:** Formation Interaction (Cat 4).

### [RI] — Wang Lin — Six Cultivation Planets Restriction
- **Use:** Wang Lin places restrictions on six cultivation planets simultaneously — a feat of divine sense manipulation at cosmic scale. "His hand pushed toward the six cultivation planets! With this, the restrictions Wang Lin had left on the six [planets activated]."
- **Source:** `search_06_RI_talismans.txt` (SpaceBattles source) + worklog (line 6716, registered as a Formation with F-grade).
- **Limitations:** Requires late-stage Wang Lin (Nirvana Shatterer tier or higher).
- **Cross-Categories:** Formation Interaction (Cat 4), Spatial/Dimensional (Cat 9).

### [Ptt] — Su Ming — sensed clone's loss + sealed Surging Indulger Clone
- **Use:** "He caught it, sealed and put into his storage bag" — the act of sealing the clone uses divine sense to grasp and contain the clone's essence.
- **Source:** `p6-su-ming.json`.
- **Cross-Categories:** Soul/Mind Interaction (Cat 5).

### [Ptt] — Su Ming — Possessed True Morning Dao World
- **Use:** Su Ming's will replaced True Morning Dao World's will — at this scale, divine sense IS the will that possesses worlds. Senior brothers "sensed Su Ming's will replacing True Morning Dao World."
- **Source:** `p6-su-ming.json`.
- **Cross-Categories:** World/Environment Sensing (Cat 15).

### [ISSTH] — Meng Hao — Bronze Lamp (soul-fragment fuel)
- **Use:** The Bronze Lamp is one of Meng Hao's strongest treasures (second only to the Copper Mirror). It "carries residual will of the original owner; consumes soul-fragments of enemies as fuel." Divine sense is the fuel.
- **Source:** Worklog line 4010.
- **Limitations:** Requires soul-fragments as consumables.

### [AWE] — Bai Xiaochun — collapsed the Bai Family's Ancestral Mountain
- **Use:** "For a single promise from Bai Yan'er, he stomped and collapsed the Bai Family's Ancestral Mountain." Demonstrative of Bai Xiaochun's post-Nascent-Soul physical manipulation (the stomp is Qi, the targeting is divine sense).
- **Source:** `bxc_baidu.html`.

### [BTT] — Xu Qing — Space Grid divine ability (Ch. 1096)
- **Use:** "Establishes spatial borders that sever external authority and reshape reality within his domain. Space divides into numerous grid fragments, each an independent world reflecting Xu Qing." When the grid collapses, "fragments shatter like glass and erupt into a spatial tempest." Divine sense IS the spatial operator here.
- **Source:** `xu_qing_abilities.txt`.
- **Cross-Categories:** Spatial/Dimensional (Cat 9), Combat/Offensive (Cat 3).

---

## 3. Combat / Offensive (divine sense as a weapon)

### [RI] — Wang Lin — Ji Realm Divine Sense (Red Lightning, Ch. 127)
- **Use:** "Red Lightning: Wang Lin's Ji Realm Divine Sense." The Ji Realm Divine Sense IS a weapon — a red-thunder destructive force. Listed as one of his "Abilities," not a passive sense.
- **Source:** `wl_techniques.json` (Abilities section).
- **Limitations:** Tied to the Ji Realm curse; cannot be removed without the curse's resolution.

### [RI] — Wang Lin — Soul Piercing Eyes / Divine Sense Eyes (Ch. 179)
- **Use:** "These were the divine sense eyes that were only obtained once someone had reached a certain level of mastery in restrictions." Offensive divine-sense eye technique that pierces the target's soul.
- **Source:** `wl_techniques.json` + `wl_items.json`.
- **Limitations:** Requires restriction mastery threshold (Ch. 179 = early Core Formation for Wang Lin).

### [RI] — Wang Lin — Eyes Suppressing the World (Ch. 1896)
- **Use:** From the fragment of the sword the green-robed old man (Ji Si) fused into Wang Lin's eyes. A divine-sense eye attack that suppresses the world itself.
- **Source:** `wl_techniques.json`.
- **Limitations:** Late-game (Ch. 1896 = late-stage Wang Lin).

### [RI] — Wang Lin — Soul Lasher → Karma Whip (Ch. 731)
- **Use:** Originally belongs to Red Butterfly; now owned by Wang Lin. "A whip that is moving at warp speed and injure the origin soul." Wang Lin fuses it with his Karma Dao to form the Karma Whip.
- **Source:** `wl_items2.json` + `wl_items.json` + `wl_techniques.json` ("Karma Whip 731 — With the help of Soul Lasher, Wang Lin tried to understand his Karma Domain").
- **Limitations:** Material weapon that operates on the soul plane; fused with Karma Dao (Ch. 731).

### [RI] — Situ Nan — Samsara Eye (glimpse karma lines)
- **Use:** Situ Nan's Samsara Eye "can briefly glimpse karma lines. It saw through Tian Yunzi's layout during the confrontation with him, providing crucial information for Wang Lin to formulate tactics."
- **Source:** `situ_nan_baidu.html`.
- **Limitations:** Brief glimpses only; deduced within the Heaven-Defying Pearl.
- **Cross-Categories:** Identity/Recognition (Cat 14), Karmic Threads (Cat 5).

### [RI] — Situ Nan — Heavenly Eye (see through cultivator disguises)
- **Use:** "Awakened after reincarnation, capable of seeing through cultivator disguises. On the Xian Gang Continent, Si Tu Nan used this to see through the Southern King's conspiracy."
- **Source:** `situ_nan_baidu.html`.
- **Cross-Categories:** Concealment/Stealth (Cat 6 — divine-sense disguise-piercing).

### [RI] — Situ Nan — Soul intimidation (suppress Nirvana cultivators)
- **Use:** In his Remnant Soul State (dwelling within the Heaven-Defying Pearl), Situ Nan's "Soul intimidation can suppress Nirvana cultivators."
- **Source:** `situ_nan_baidu.html`.
- **Cross-Categories:** Soul/Mind Interaction (Cat 5).

### [RI] — Wang Lin — Ji Realm Domain (absolute suppression)
- **Use:** Wang Lin's Ji Realm at various stages makes him invincible at the same realm and capable of killing cultivators below the next major realm. E.g., "Ji Realm Core Formation Peak Stage: can kill any Cultivator under Nascent Soul"; "Ji Realm Nascent Soul Peak Stage: can kill any Cultivator below Soul Formation."
- **Source:** `wl_cult.json` (Combat Strength table).
- **Limitations:** Sealed away at Soul Formation Middle Stage ("Ancient God body separated and Ji Realm stored away").

### [RI] — Teng Huayuan — lifespan-burn curse locking onto Wang Lin
- **Use:** "Teng Huayuan sensed his death [of Teng Li] and, in a rage, did not hesitate to burn his lifespan to perform a curse technique, locking onto Wang Lin's location." Divine sense locking + curse tracking.
- **Source:** `teng_huayuan.html`.
- **Cross-Categories:** Identity/Recognition (Cat 14).

### [ISSTH] — Meng Hao — 9 Anti-Ancient Soul Lamps (Ancient Realm)
- **Use:** Meng Hao ignites 9 soul lamps at the Ancient Realm — each lamp is a soul-extending artifact; their number and quality determine combat strength at Ancient Realm.
- **Source:** Worklog line 2245.
- **Cross-Categories:** Soul/Mind Interaction (Cat 5).

### [ISSTH] — Meng Hao — Blood Immortal Three Styles (Blood Finger, Blood Hand Seal, Blood Slaughter Realm)
- **Use:** Soul-lineage combat techniques inherited as the Second Generation Blood Immortal (Ch. 135).
- **Source:** `menghao_baidu.html`.
- **Cross-Categories:** Soul/Mind Interaction (Cat 5).

### [ISSTH] — Meng Hao — Dao Essences (9, Dao Realm)
- **Use:** 9 Dao Essences at Dao Realm — each essence is a soul-bound combat reality-bending.
- **Source:** Worklog line 2245.

### [ISSTH] — Meng Hao — Paper of Cause and Effect
- **Use:** A Meng Hao-created spell that operates on cause-effect (karmic) lines — uses divine sense to write/unwrite cause-effect.
- **Source:** `menghao_baidu.html`.
- **Cross-Categories:** Soul/Mind Interaction (Cat 5), Karmic Threads (Cat 5).

### [AWE] — Bai Xiaochun — "killing intent... towards Bai Hao"
- **Use:** "After meeting the Bai Family Patriarch, he understood his inner disgust and killing intent towards Bai Hao." Killing intent is explicitly felt/detected via divine sense.
- **Source:** `bxc_baidu.html`.
- **Cross-Categories:** Killing Intent Detection (Cat 8).

### [AWE] — Bai Xiaochun — "eyes bloodshot and carrying overwhelming killing intent"
- **Use:** At the climax of the Kui World arc, Bai Xiaochun opens his eyes with overwhelming killing intent, halting the war. The killing intent is so dense it acts as a domain.
- **Source:** `bxc_baidu.html`.
- **Cross-Categories:** Killing Intent Detection (Cat 8).

### [AWWP] — Wang Baole — Divine Eye Emperor Armor (Ch. 853)
- **Use:** An armor that fuses with the Divine Eye Art — divine-sense-driven combat armor.
- **Source:** Worklog RESEARCH-AWWP (line 1428).
- **Cross-Categories:** Physical Manipulation (Cat 2).

### [AWWP] — Wang Baole — Dark Art: Soul Guidance
- **Use:** A Dark-Sect soul-combat art (Ch. 288+). Guides/manipulates souls — the offensive counterpart to the Soul-Weaving Technique's constructive use.
- **Source:** Worklog RESEARCH-AWWP.
- **Cross-Categories:** Soul/Mind Interaction (Cat 5).

### [BTT] — Xu Qing — God Decapitation Altar (Ch. 642)
- **Use:** Imperial Sovereign Li Zihua's ultimate trump card — "used to behead Crimson Mother. Legend had it that after manifesting the divine ability for the first time, Li Zihua used it to behead himself." Evolves into 7-Extremes (Ch. 1096+) and beyond.
- **Source:** `xu_qing_abilities.txt`.
- **Cross-Categories:** Soul/Mind Interaction (Cat 5).

### [BTT] — Xu Qing — Indestructible Emperor Fist
- **Use:** A divine ability of Xu Qing's — emperor-tier soul-fueled fist technique.
- **Source:** `xu_qing_baidu.txt`.

### [BTT] — Xu Qing — Sword Holding Style
- **Use:** A sword-style divine ability.
- **Source:** `xu_qing_baidu.txt`.

### [BTT] — Xu Qing — Daofate Heavenfiend Art (Nascent Soul, Ch. 546)
- **Use:** "Heavenfate was the best nourishment a nascent soul could get — this technique didn't require precious materials. It just required slaughter!" Devours heavenfate from slain Nascent Soul cultivators via divine-sense extraction.
- **Source:** `xu_qing_abilities.txt`.
- **Cross-Categories:** Soul/Mind Interaction (Cat 5), Soul Devouring.

---

## 4. Formation Interaction (reading, setting, breaking formations with divine sense)

### [RI] — Wang Lin — Restriction Flag Method (禁旗之法)
- **Use:** The cultivation method to wield Restriction Flags. Embeds restriction matrices, seals regions, suppresses enemies. Doubles as storage + portable restriction matrix. Wang Lin "didn't even need to look with his eyes. By examining on the wave produced by the illusionary circle, he was able to understand the structure and rules of the restriction."
- **Source:** `wl_techniques.json` ("Soul Searching Soul Flag Production Method 384" + Restriction Spells); `search_06_RI_talismans.txt`.
- **Limitations:** Requires restriction mastery.

### [RI] — Wang Lin — 4 Great Restrictions (Annihilation / Time / Life-Death / Destruction)
- **Use:** Dao-grade restrictions — Wang Lin's signature late-game arsenal. Each is its own Dao domain; set/broken via divine sense manipulation of restriction flag arrays.
- **Source:** Worklog line 6716 (registered as canon formations in `Formation.java`).
- **Limitations:** Dao-grade; cannot be set by sub-Soul-Formation cultivators.

### [RI] — Wang Lin — Soul Flag Production Method (Soul Refining Sect, Ch. 384)
- **Use:** "Soul Flag Production Method is the main cultivation method of the Soul Refining Sect. This method splits into three parts, Soul Refining, Soul Extracting, and Soul Sealing." Each part is a formation-interaction technique that operates via divine sense.
- **Source:** `wl_techniques.json`.

### [RI] — Heng Yue Sect Protecting Array
- **Use:** Outer perimeter + spirit-gathering + heart-array core flag layered matrix. Wang Lin perceives this array's structure as an outer disciple (early exposure to formation reading via divine sense).
- **Source:** Worklog line 6716 + `formation-talisman-catalog.ts`.

### [RI] — Six Cultivation Planets Restriction
- **Use:** Wang Lin's feat of sealing 6 cultivation planets at once. Dao-grade. (See Cat 2 entry.)
- **Source:** Worklog line 6716.

### [RI] — Soul Refining Sect Blood-Sacrifice Array
- **Use:** Dark formation fueled by soul sacrifice; perceived/broken via divine sense.
- **Source:** Worklog line 6716.

### [RI] — Heaven-Avoiding Coffin (避天棺)
- **Use:** "Coffin that hides its occupant from heavenly tribulation and divine sense." A talisman-formation hybrid that actively defeats BOTH heavenly-sense AND cultivator divine sense.
- **Source:** Worklog line 4927, 5290.
- **Cross-Categories:** Concealment/Stealth (Cat 6).

### [ISSTH] — Meng Hao — Lightning Flag
- **Use:** "Meng Hao slapped his bag of holding. A sheet of lightning appeared which coalesced into a Lightning Flag." Formation-flag-as-item, perceived/wielded via divine sense.
- **Source:** `search_03_ISSTH_formations.txt` (Comic Vine source).

### [ISSTH] — Immortal Sense Soil in Black Lands
- **Use:** "The Bird says that the Immortal Sense Soil in Black Lands is filled with ashes of burnt ancient talisman that was created in order to seal the planet by an [ancient cultivator]." Soil itself carries residual divine sense.
- **Source:** `search_07_ISSTH_talismans.txt`.
- **Cross-Categories:** World/Environment Sensing (Cat 15).

### [AWE] — Bai Xiaochun — Living Formations (sentient formations)
- **Use:** Bai Xiaochun's Living Formations gain sentience — parallel to his Living Pills. Divine sense animates the formation into a quasi-being.
- **Source:** Worklog line 6716 (formation-talisman-catalog.ts entry).

### [AWE] — Bai Xiaochun — Reverse River Sect protective grand array
- **Use:** Bai Xiaochun "resisted for a short while using multi-colored fire and the Reverse River Sect's protective grand array." Sect-protecting array operated via divine sense.
- **Source:** `bxc_baidu.html`.

### [AWE] — Starry Sky Dao Polarity Sect grand formation
- **Use:** "The Starry Sky Dao Polarity Sect's formation descended, forcibly promoting Bai Xiaochun to Yellow disciple status." Formation-as-spiritual-layer operator that affects the cultivator's status.
- **Source:** `bxc_baidu.html`.

### [Ptt] — Su Ming — Peace Arrives When the Elephant is Here
- **Use:** A formation/divine-ability Su Ming contemplates while in the Divine Essence Star Ocean. (Mentioned alongside Yu Rou.)
- **Source:** `p1-bald-crane.json`.

### [Ptt] — Su Ming — Picture Creation (画界)
- **Use:** Su Ming's self-created array; "paints reality into existence." Dao-grade. Operates via divine-sense image projection.
- **Source:** Worklog line 5017 (formation-talisman-catalog.ts F106).

### [Ptt] — Su Ming — Branding Art (烙印之术)
- **Use:** Brands souls — the foundation of Su Ming's soul-mastery. Immortal-grade. Operates via divine-sense imprint.
- **Source:** Worklog line 5020 (F109).
- **Cross-Categories:** Soul/Mind Interaction (Cat 5).

### [Ptt] — Su Ming — Écang Clone Tree-Formation
- **Use:** Écang-tree-based array; refines Divine Essences. Heaven-grade.
- **Source:** Worklog line 5021 (F110).
- **Cross-Categories:** Physical Manipulation (Cat 2).

### [Ptt] — Su Ming — Abyss Gate (深渊之门)
- **Use:** Abyss-builder gate; seals/traverses the abyss. Dao-grade.
- **Source:** Worklog line 5022 (F111).

### [Ptt] — Su Ming — Candle Dragon Curse
- **Use:** Curse-array from the Candle Dragon lineage. Immortal-grade. Operates via divine-sense curse imprint.
- **Source:** Worklog line 5023 (F112).
- **Cross-Categories:** Combat/Offensive (Cat 3).

### [BTT] — Xu Qing — Four Nine Array Restriction
- **Use:** A listed divine ability of Xu Qing — formation/restriction system. (Ch. unspecified.)
- **Source:** `xu_qing_baidu.txt`.

### [BTT] — Xu Qing — 49 Taboo Formations (beast hide)
- **Use:** Looted from 5th Star Ring Imperial Sovereign. A set of 49 taboo formation patterns.
- **Source:** Worklog line 2418.
- **Cross-Categories:** Teaching/Transmission (Cat 12).

### [BTT] — Xu Qing — Seven Lamps Ghost Fire Curse
- **Use:** A spell — a 7-lamp curse formation that operates via soul-fire.
- **Source:** `xu_qing_baidu.txt`.

### [BTT] — Xu Qing — Three Poisons Four Snakes Five Dogs Six Thieves Seven Kills Eight Evils Ten Thousand Evils Method
- **Use:** A long-form spell-method — layered curse formation operating on the soul.
- **Source:** `xu_qing_baidu.txt`.

### [BTT] — Xu Qing — Fishing the Moon in the Well (Immortal Essence, Ch. 809)
- **Use:** Reward from Immortal Essence for contribution. Evolves into "Pluck Stars from the Vault of the Universe." The well is a formation/well-as-divine-sense-reflector.
- **Source:** Worklog line 2468 + `xu_qing_abilities.txt`.
- **Cross-Categories:** Perception/Sensing (Cat 1).

---

## 5. Soul / Mind Interaction (soul search, memory reading, mental communication, dream invasion, karmic threads)

### [RI] — Wang Lin — Soul Searching (Soul Flag Production Method, Ch. 384)
- **Use:** Soul Refining, Soul Extracting, and Soul Sealing — the three parts of the Soul Flag Production Method. Operates on captured souls via divine sense.
- **Source:** `wl_techniques.json`.
- **Limitations:** Karma + heart-demon risks (modeled in `divine.ts` soulSearch: +20% karma, Karmic Stain, 30% heart-demon risk, backlash Soul Fracture on failure).

### [RI] — Wang Lin — Billion Soul Flag (Ten Billion Soul Banner)
- **Use:** Ultimate treasure of the Soul Refining Sect. Stores and refines billions of souls via divine sense.
- **Source:** `wl_items2.json`.
- **Cross-Categories:** Combat/Offensive (Cat 3).

### [RI] — Wang Lin — Ji Qiong skull divine sense (Slaughter's message)
- **Use:** "Wang Lin confirms his true self by uncovering the secret of Heaven Defying Bead when he finds the Ji Qiong skull had a divine sense left by Slaughter that tells how to revive Li Muwan." Divine sense persists after death, carrying messages.
- **Source:** `wl_cult.json`.
- **Cross-Categories:** Teaching/Transmission (Cat 12).

### [RI] — Wang Lin — Li Muwan nascent soul trembled
- **Use:** When the coffin power swept by, "Li Muwan's nascent soul trembled and showed signs of collapse." Divine-sense pressure on a soul is direct.
- **Source:** `wl_main.json`.

### [RI] — Wang Lin — Wang Family soul vengeful spirits
- **Use:** Teng Huayuan "turned the souls of the Wang family members into vengeful spirits, bringing them before Wang Lin to make the deceased souls of the Wang family resent Wang Lin." Soul manipulation via curse — operates on the soul plane.
- **Source:** `teng_huayuan.html`.
- **Cross-Categories:** Combat/Offensive (Cat 3).

### [RI] — Wang Lin — Soul imprint of Xie Qing (in Immortal Guards)
- **Use:** "Within the remnants of the Seven-Colored Immortal Venerable's consciousness, Wang Lin discovered the soul imprint of Xie Qing. Wang Lin meticulously refined this ancient soul, crafting a guard." Divine sense used to extract, refine, and embody a soul.
- **Source:** `wl_guards.json`.
- **Cross-Categories:** Alchemy/Crafting (Cat 11), Teaching/Transmission (Cat 12).

### [Ptt] — Su Ming — Welcoming of Deities (迎神) — soul-sensing pill
- **Use:** Two uses: (a) senses souls present in the world; (b) absorbs soul fragments of deceased powerhouses (used to absorb the Second God of Berserkers' soul fragments, gaining access to his Arts).
- **Source:** Worklog line 5386.
- **Limitations:** Risk of soul corruption from absorbed fragments.
- **Cross-Categories:** Alchemy/Crafting (Cat 11), Teaching/Transmission (Cat 12).

### [Ptt] — Su Ming — Branding Art
- **Use:** Brands souls — foundation of Su Ming's soul-mastery. (See Cat 4 entry.)

### [Ptt] — Su Ming — One-Billion-Corpse-Soul-Seal (Sacred Lady of Dark Dawn)
- **Use:** A diabolical art that plants a seal of one billion corpse souls inside a body — divine-sense-tier curse that Su Ming sensed inside his own real body.
- **Source:** `p6-su-ming.json`.
- **Cross-Categories:** Combat/Offensive (Cat 3).

### [Ptt] — Su Ming — Dream Dao (Hu Zi's ability enter Dream)
- **Use:** "Hu Zi used his ability enter Dream and showed a picture showing his vision. Second senior brother recognized in the picture Heavenly Incense Rune." Divine-sense dream-projection.
- **Source:** `p6-su-ming.json`.

### [Ptt] — Su Ming — Mask of Sorrow (Annihilation Elder's masks)
- **Use:** One of Annihilation Elder's Masks of Joy, Anger, Sorrow, and Resentment. Su Ming inherited the Mask of Sorrow — operates on the soul plane.
- **Source:** `p6-su-ming.json` (via the inheritance list).

### [ISSTH] — Meng Hao — Soul Devouring Scripture
- **Use:** One of Meng Hao's listed Cultivation Arts — devours souls via divine sense. (Paired with the Dao God Scripture Divine Sense Volume.)
- **Source:** `menghao_baidu.html`.
- **Cross-Categories:** Combat/Offensive (Cat 3).

### [ISSTH] — Meng Hao — Blood Demon Great Art
- **Use:** A Meng Hao cultivation art that operates on blood + soul via divine sense.
- **Source:** `menghao_baidu.html`.

### [ISSTH] — Meng Hao — Spirit Separation Art
- **Use:** Separates the spirit from the body — divine-sense manipulation of one's own soul.
- **Source:** `menghao_baidu.html`.

### [ISSTH] — Meng Hao — Withered Flame Demon Art True Self Dao
- **Use:** Seven-character secret art (superimposable); the "True Self" character operates on the soul-self.
- **Source:** `menghao_baidu.html`.

### [AWE] — Bai Xiaochun — Soul Refining Pot / Soul-Storing Mirror variant
- **Use:** Soul-Storing Mirror variant (Bai Xiaochun). Refines souls via divine sense; stores them.
- **Source:** Worklog line 5293.
- **Cross-Categories:** Alchemy/Crafting (Cat 11).

### [AWE] — Bai Xiaochun — Deep Abyss Fiend Soul (Gongsun Wan'er's true identity)
- **Use:** Gongsun Wan'er reveals her true identity as a Deep Abyss Fiend Soul to Bai Xiaochun — soul-tier being perceived via divine sense.
- **Source:** `bxc_baidu.html`.
- **Cross-Categories:** Identity/Recognition (Cat 14).

### [AWE] — Bai Xiaochun — Spirit Refinement (Turtle-Pattern Pot)
- **Use:** "Performed Spirit Refinement on a withered leaf twenty-five times, achieving the ultimate Purple Pattern!" Soul-refining technique; divine sense is the operator.
- **Source:** `bxc_baidu.html`.
- **Cross-Categories:** Alchemy/Crafting (Cat 11).

### [AWE] — Bai Xiaochun — "He captured the Soul-eating Senior alive"
- **Use:** Bai Xiaochun captures a being whose nature is soul-eating — the capture itself is a divine-sense feat (the Soul-eating Senior's domain IS the soul plane).
- **Source:** `bxc_baidu.html`.

### [AWWP] — Wang Baole — Dark Art: Dark Corpse Face Art (Ch. 288)
- **Use:** Produces Dark Fire + Dark Core. Operates on corpses' residual souls.
- **Source:** Worklog line 1468.
- **Cross-Categories:** Combat/Offensive (Cat 3).

### [AWWP] — Wang Baole — Dark Art: Sealing Return of Yang Sealing Technique (Ch. 853)
- **Use:** A soul-sealing art — prevents a soul from returning to yang (reincarnation).
- **Source:** Worklog line 1478.

### [AWWP] — Wang Baole — Mysterious Mask (Wang Yiyi fragment inside)
- **Use:** Wang Baole's father found the mask at an excavation site (Ch. 1). Inside the mask is a part of Wang Yiyi (Little Missy) — Wang Lin & Li Muwan's daughter, the Saintess of the Vast Dao Palace. The mask spirit is a remnant divine sense.
- **Source:** Worklog line 1380.
- **Cross-Categories:** Identity/Recognition (Cat 14), Teaching/Transmission (Cat 12).

### [AWWP] — Wang Baole — Illusory Essence Art (avatar technique, Ch. 731)
- **Use:** From Chen Qingzi. Creates an avatar via soul-splitting.
- **Source:** Worklog line 1474.
- **Cross-Categories:** Physical Manipulation (Cat 2).

### [AWWP] — Wang Baole — Immortal Symbol (Ch. 1273)
- **Use:** From Chen Qingzi. A soul-tier symbol of immortality.
- **Source:** Worklog line 1475.

### [AWWP] — Wang Baole — 100,000 Wang Baole incarnations (black wooden nail)
- **Use:** Lore: 100,000 "Wang Baole" incarnations of the remnant souls of the black wooden nail will arise for a predestined battle. Soul-splitting at cosmic scale.
- **Source:** Worklog line 1801.
- **Cross-Categories:** Identity/Recognition (Cat 14).

### [BTT] — Xu Qing — Soul-Weaving Technique (Xeno-Immortal School)
- **Use:** (See Cat 1 entry — the constructive use of soul-threads.)
- **Source:** `xu_qing_abilities.txt`.

### [BTT] — Xu Qing — Sundial Life Lamp (Black Umbrella Life Lamp evolved)
- **Use:** All 5 Life Lamps refined into Sundial Life Lamps via Heavenly Fire; "retaining their original functions while gaining new abilities such as time stagnation and time reversal." Life Lamp = soul anchor.
- **Source:** `xu_qing_baidu.txt`.
- **Cross-Categories:** Soul/Mind Interaction (Cat 5).

### [BTT] — Xu Qing — God Clone (separate from main body)
- **Use:** Xu Qing has a God Clone (purple hair, grey eyes) — a separate soul-body for the god path.
- **Source:** `xu_qing_main.txt`.
- **Cross-Categories:** Physical Manipulation (Cat 2).

### [BTT] — Xu Qing — Three Poisons Four Snakes Five Dogs Six Thieves Seven Kills Eight Evils Ten Thousand Evils Method
- **Use:** A long-form soul-curse method. (See Cat 4 entry.)

### [BTT] — Xu Qing — Karma-stitching needle + karma-cutting scissors
- **Use:** Treasures of Great Emperor level obtained from Fu Xie / Ninth Brother. Operate on karma threads directly via divine sense.
- **Source:** Worklog (Xu Qing items list).
- **Cross-Categories:** Karmic Threads (this category).

---

## 6. Concealment / Stealth (hiding from divine sense, divine sense camouflage)

### [RI] — Wang Lin — Heaven-Avoiding Coffin (避天棺)
- **Use:** Coffin that hides its occupant from heavenly tribulation AND divine sense. (See Cat 4 entry.)

### [RI] — Wang Lin — Wang Lin hides inside black pagoda
- **Use:** "In the first level he hides inside a black pagoda to save himself from hurricane [of Chaotic Broken Stars]." Concealment via formation/talisman + divine-sense masking.
- **Source:** `wl_main.json`.

### [RI] — Forest that distorts Divine sense
- **Use:** (See Cat 1 entry.) Terrain-based concealment.

### [RI] — Wang Lin — Restriction Mountain concealment
- **Use:** At the Restriction Mountain trial, Wang Lin's restriction mastery conceals his divine sense from lower-tier observers.
- **Source:** `wl_techniques.json` + `search_06_RI_talismans.txt`.

### [RI] — Situ Nan — Heavenly Eye pierces disguises
- **Use:** (See Cat 3 entry — the piercing side.) The reverse implication: lower-tier cultivators ARE concealed, and only the Heavenly Eye reveals them.

### [Ptt] — Bald Crane — illusion-weaving (does NOT bypass divine sense)
- **Use:** The bald crane appeared as a 1000-foot black crane. "Su Ming's Nascent Soul / divine sense actually SAW THROUGH the illusion — 'Su Ming could see this form was only an illusion with only the black feathers being real.' Divine sense PIERCED the illusion — it did NOT bypass divine sense. The crane's illusions deceive ordinary perception, not divine sense."
- **Source:** Worklog line 1158 (verified canon).
- **Limitations:** Divine sense ≥ Nascent Soul pierces it; ordinary perception does not.

### [Ptt] — Su Ming — concealing himself with Han Mountain Bell
- **Use:** "He trapped himself within Han Mountain Bell to resist the Spell [of Bai Ge]." Bell-based soul concealment.
- **Source:** `p6-su-ming.json`.

### [Ptt] — Su Ming — concealing identity as Wang Tao (Ancient Zang Sect)
- **Use:** Su Ming as Third Prince Wang Tao at the Ancient Zang Sect — explicit soul-disguise at cosmic tier.
- **Source:** `p6-su-ming.json`.

### [Ptt] — Su Ming — Nether-Molding Clan to conceal identity (Di Tian)
- **Use:** "Di Tian... intending to snatch the body of Di Tian, one of the Five Emperors of the Immortal Clan in the Dao Chen True World, to conceal his identity."
- **Source:** `p6-su-ming.json` (filtered.txt conceal entry).

### [ISSTH] — Meng Hao — Blood Immortal Mask
- **Use:** One of Meng Hao's treasures. Mask that conceals identity and operates on blood+soul.
- **Source:** `menghao_baidu.html`.

### [ISSTH] — Meng Hao — Fang Mu (alias as disguise)
- **Use:** Meng Hao uses the alias Fang Mu (his original Fang-family name) — but the disguise is soul-tier; some characters see through it via divine sense.
- **Source:** `menghao_baidu.html` (aliases list).

### [ISSTH] — Meng Hao — Black armor (Lord Fifth's parrot power)
- **Use:** "It transformed into countless black threads which instantly covered Meng Hao, forming a suit of black armor. By borrowing the power of the parrot, he was..." Concealment/defense via parrot-borrowed divine sense.
- **Source:** `search_03_ISSTH_formations.txt` (Comic Vine source).

### [AWE] — Bai Xiaochun — Nightcrypt Mask
- **Use:** Bai Xiaochun's identity-concealment mask in the Blood Stream Sect arc. Operates via divine-sense disguise.
- **Source:** Worklog line 5293.
- **Cross-Categories:** Identity/Recognition (Cat 14).

### [AWE] — Bai Xiaochun — Paper Person (Gongsun Wan'er)
- **Use:** "Bai Xiaochun discovered someone on his back but could not escape. Gongsun Wan'er appeared, helped him remove the Paper Person." A paper-talisman that attaches to the soul-plane unseen.
- **Source:** `bxc_baidu.html`.

### [AWE] — Bai Xiaochun — Exorcism Talismans (wrap himself like a zongzi)
- **Use:** Bai Xiaochun wrapped himself in Exorcism Talismans to ward off the little ghostly spirit girl's baleful aura — soul-ward via talisman stack.
- **Source:** `bxc_baidu.html`.

### [AWE] — Bai Xiaochun — concealing his disgust and killing intent
- **Use:** "He understood his inner disgust and killing intent towards Bai Hao" — the act of suppressing this from being sensed is divine-sense concealment. "Pretending to be profound and mystifying, bluffing everyone."
- **Source:** `bxc_baidu.html`.

### [AWWP] — Wang Baole — Hallucination Dharma Pillow (Ch. 9)
- **Use:** An acquired item — induces hallucinations to conceal the user.
- **Source:** Worklog line 1407.

### [AWWP] — Wang Baole — Voice Transmission Ring (Ch. 2)
- **Use:** An acquired item — transmits voice concealed from ordinary hearing. (See Cat 12 entry.)

### [BTT] — Xu Qing — Shadow's devouring → perceive in fog
- **Use:** (See Cat 1 entry.) Concealment-piercing via devouring.

### [BTT] — Xu Qing — God Face perception IS the attack (you can't perceive a God)
- **Use:** "Mortals can die or go mad if they look at Gods." The God's perception-as-attack means mortals cannot perceive Them safely — concealment is built into the cosmic order.
- **Source:** Worklog line 1792.

### [BTT] — Xu Qing — conceal identity ("Kid Demon Xu Little Ah Qing")
- **Use:** Many aliases (Kid, Demon Xu, Little Ah Qing, Ghost Hand, Tian Qingzi, Dan Jiu, Purple Master, Purple Moon God, Ergou, Demon King, Grandmaster Pill, Nine Grand Mystic Heaven, Grand Darkheaven, King Skycrusher, Yan Xuanzi, Immortal Lord, Lord of the Threshold). Each alias is a soul-tier disguise.
- **Source:** `xu_qing_main.txt`.

### [BTT] — Xu Qing — Heterogeneity avoidance (smearing mud, not bathing)
- **Use:** "In his early days, to conceal his radiance and avoid drawing attention, he deliberately smeared mud on his face and avoided washing or bathing." Physical concealment as proxy for soul-concealment.
- **Source:** `xu_qing_baidu.txt`.

### [BTT] — Xu Qing — God State levels conceal/divert
- **Use:** God State levels (1st through 5th, with 4th = Violet Lord) each have distinct signatures. Xu Qing's dual immortal + god cultivation lets him operate in either signature — a soul-concealment by modality.

---

## 7. Surveillance / Scrying (long-distance watching, sect monitoring arrays)

### [RI] — Heng Yue Sect — Sect-Protecting Array (universal sect surveillance)
- **Use:** Outer perimeter + spirit-gathering + heart-array core. Every sect has one; it surveils its territory via divine sense.
- **Source:** Worklog line 6716 (registered as canon formation).

### [RI] — Soul Refining Sect Blood-Sacrifice Array
- **Use:** Dark formation that monitors its captured-soul network.
- **Source:** Worklog line 6716.

### [RI] — Six Cultivation Planets Restriction
- **Use:** Wang Lin's feat — monitors/maintains restriction on 6 planets simultaneously.
- **Source:** Worklog line 6716.

### [ISSTH] — Patriarch Vast Sea Bronze Lamp (soul surveillance)
- **Use:** The Bronze Lamp carries residual will of the original owner; functions as a sect-scale soul surveillance + combat treasure.
- **Source:** Worklog line 4010.

### [AWE] — Starry Sky Dao Polarity Sect — must-kill list tracking
- **Use:** "Zhou Zimo elevated Bai Xiaochun to fifth place on the Must-Kill List, with a bounty of two Celestial Beast Souls." Sect-scale tracking via divine sense.
- **Source:** `bxc_baidu.html`.

### [AWE] — Bai Xiaochun — Spirit Refinement Trial monitoring (gigantic trial stele)
- **Use:** The trial stele in the Spirit Refinement Trial monitors all participants via divine sense.
- **Source:** `bxc_baidu.html`.

### [AWE] — Tongtian Continent besieging forces surveillance of Kui Huang City
- **Use:** "The cultivators of the Tongtian Continent besieged the Kui Huang City, launching a final assault." Surveillance via sect-scale divine sense.
- **Source:** `bxc_baidu.html`.

### [BTT] — Seven Blood Eyes sect surveillance
- **Use:** Xu Qing's first sect. The sect monitors its members' life lamps — soul-tier surveillance.
- **Source:** `xu_qing_baidu.txt` + `seven_blood_eyes.txt`.

### [BTT] — Eight Sects Alliance surveillance
- **Use:** Larger alliance that surveils members via combined divine-sense arrays.
- **Source:** `xu_qing_baidu.txt`.

### [BTT] — Empire's Imperial Sovereigns surveillance
- **Use:** The Imperial Sovereigns of each Star Ring surveil their domain via divine sense + God Authorities.
- **Source:** `xu_qing_abilities.txt`.

### [BTT] — Crimson Mother's surveillance (Crimson Mother is a cosmic-tier surveillor)
- **Use:** Crimson Mother — a cosmic-tier mother-goddess entity whose gaze spans star rings.
- **Source:** `xu_qing_main.txt` (listed in Characters: Crimson Mother).

### [BTT] — Li Zihua's God Decapitation Altar (manifests first to behead himself)
- **Use:** The technique's first manifestation was to behead Li Zihua himself — a self-surveillance/soul-severing that enabled him to operate covertly afterward.
- **Source:** `xu_qing_abilities.txt`.

### [Ptt] — Di Tian — secret surveillance across True Worlds
- **Use:** Di Tian hid his true body in the Land of Yin Death to "secretly perfect his own schemes while recovering from his injuries." Divine-sense-tier concealment + surveillance.
- **Source:** `p6-su-ming.json`.

---

## 8. Killing Intent Detection (sensing hostile intent)

### [RI] — Wang Lin — Teng Li forest distortion of divine sense + killing intent pursuit
- **Use:** The forest that distorts divine sense is where Teng Li pursues Wang Lin. Killing intent + divine-sense distortion go together.
- **Source:** `wl_main.json`.

### [RI] — Teng Huayuan — burning lifespan to curse-lock onto Wang Lin
- **Use:** "Teng Huayuan sensed his death [of Teng Li] and, in a rage, did not hesitate to burn his lifespan to perform a curse technique, locking onto Wang Lin's location." Killing intent + curse lock.
- **Source:** `teng_huayuan.html`.

### [Ptt] — Su Ming — thick aura of death sensed by Yan Pei
- **Use:** (See Cat 1 entry.)
- **Source:** `p6-su-ming.json`.

### [AWE] — Bai Xiaochun — felt Bai Family Patriarch's killing intent
- **Use:** "After meeting the Bai Family Patriarch, he understood his inner disgust and killing intent towards Bai Hao."
- **Source:** `bxc_baidu.html`.

### [AWE] — Bai Xiaochun — eyes bloodshot, overwhelming killing intent
- **Use:** (See Cat 3 entry.) Bai Xiaochun's killing intent is so dense it halts a war.
- **Source:** `bxc_baidu.html`.

### [AWE] — Bai Xiaochun — Exorcism Talismans against baleful aura
- **Use:** Talismanic wards against killing-intent-as-baleful-aura. Defensive against sensed hostile intent.
- **Source:** `bxc_baidu.html`.

### [Ptt] — Su Ming — sensed thick aura of death from his body (Yan Pei)
- **Use:** (See Cat 1 entry.) Killing intent as detectable aura.

### [BTT] — Xu Qing — sensed the Eye of Solar Kalpa revealed his location
- **Use:** "While Su Ming wanted to hide at some planet, the Eye of Solar Kalpa suddenly appeared and revealed his location." (Wait — this is Su Ming, not Xu Qing. But the same mechanic applies to BTT's God Eye perception.) Killing-intent-as-revelation: the God's eyes opening is the ultimate hostile-intent perception event.
- **Source:** `p6-su-ming.json` (Ptt canon, but the mechanic is identical to BTT's Broken God Face).

### [RI] — Wang Lin — Ji Realm Divine Sense weaponized intent
- **Use:** (See Cat 3 entry.) The Ji Realm Divine Sense IS weaponized killing intent — it injures souls directly.

### [ISSTH] — Meng Hao — 9 anti-Ancient soul lamps (intent detection array)
- **Use:** The 9 soul lamps are both soul-anchors AND intent-detectors — they flicker when hostile intent approaches.

---

## 9. Spatial / Dimensional (finding spatial cracks, sensing other dimensions)

### [RI] — Wang Lin — spatial crack escape (Chaotic Broken Stars)
- **Use:** "He then tries to return towards the entry of Chaotic Broken Stars to escape from there but is restricted by the voids." Spatial cracks as travel paths; divine sense to navigate them.
- **Source:** `wl_main.json`.

### [RI] — Wang Lin — Heaven-Defying Pearl spatial folding + time acceleration
- **Use:** "Situ Nan can manipulate the pearl's spatial folding and time acceleration functions. The time flow rate inside the Heaven Defying Pearl is a hundred times faster than the outside world." Spatial manipulation via divine-sense-treasure.
- **Source:** `situ_nan_baidu.html`.

### [Ptt] — Su Ming — dimension-fold (Su Ming kills many in second layer of shattered dimension)
- **Use:** "In the second layer, Su Ming killed many members of One Dao Sect" inside a shattered dimension. Divine sense navigates dimensions.
- **Source:** `p6-su-ming.json`.

### [Ptt] — Su Ming — Candle Dragon carcass (World of Nine Yin)
- **Use:** Su Ming enters the Candle Dragon carcass in the World of Nine Yin — a sub-dimension accessible only via divine-sense-tier perception.
- **Source:** `p6-su-ming.json`.

### [Ptt] — Su Ming — Undying and Imperishable World
- **Use:** "He was sealed in the Undying and Imperishable World. In that world, Su Ming fought, died and wandered as a soul for countless years." A dimension accessible only via divine sense.
- **Source:** `p6-su-ming.json`.

### [Ptt] — Su Ming — Distorted galaxy to imprison Apostles
- **Use:** "When he sensed three Apostles entering True Morning Dao World on Old Man Extermination's orders, he distorted the galaxy around them to imprison them." Spatial manipulation via divine-sense will.
- **Source:** `p6-su-ming.json`.

### [Ptt] — Su Ming — stepped into his True Morning Dao World → knew everything
- **Use:** "Once Su Ming stepped into his True Morning Dao World, he knew everything." Possessing a world = sensing every spatial point in it.
- **Source:** `p6-su-ming.json`.

### [ISSTH] — Meng Hao — Shrinking Earth to Inches (a listed technique)
- **Use:** A movement/spatial technique. Compresses spatial distance via divine sense.
- **Source:** `menghao_baidu.html`.

### [ISSTH] — Meng Hao — Supreme Bridge (erased)
- **Use:** "Supreme Bridge (erased while escorting the Shanhai Butterfly to the Vast Starry Sky, later unable to be performed)." A spatial-bridge technique via divine sense.
- **Source:** `menghao_baidu.html`.

### [AWE] — Bai Xiaochun — Tongtian World collapse (spatial)
- **Use:** "After the collapse of the Tongtian World, she drifted into the territory of the Evil Emperor Dynasty." Spatial collapse + drift.
- **Source:** `bxc_baidu.html`.

### [AWE] — Bai Xiaochun — Star mark teleport
- **Use:** "Bai Xiaochun used a star mark to teleport away." Spatial teleportation via divine-sense item.
- **Source:** `bxc_baidu.html`.

### [AWWP] — Wang Baole — Hades Coffin interstellar travel
- **Use:** "Wang Baole sleeps in the Dark Sect's coffin, leaves Pluto to the outside of Solar System." Coffin-as-spatial-travel-vessel.
- **Source:** Worklog line 1799 (VSBattles source).

### [AWWP] — Wang Baole — Stone Stele World (sealed sub-dimension)
- **Use:** "The Stone Stele Realm, where protagonist Wang Baole resides, was sealed by the deity Luo Tian using a colossal palm after a war with the Vast Dao Domain." Spatial-sealed world; Wang Baole eventually exits.
- **Source:** Worklog line 1800.

### [AWWP] — Wang Baole — Realm Mending Plate (Ch. 1188)
- **Use:** From Netherworld River. Mends realms — spatial-repair via divine sense.
- **Source:** Worklog line 1429.

### [BTT] — Xu Qing — Space Grid divine ability
- **Use:** (See Cat 2 entry.) Spatial borders + grid fragments + spatial tempest.

### [BTT] — Xu Qing — Infinite Timelessness (Ch. 1117)
- **Use:** "Infinite Timelessness is the divine ability Xu Qing enlightened within the Desert of Time of the Fifth Star Ring, where each grain of sand represents a timeline layered upon identical yet divergent..." Each grain = a timeline. Divine sense spans all timelines.
- **Source:** `xu_qing_abilities.txt`.

### [BTT] — Xu Qing — Ten Extreme Dao (the original rule for star ring birth)
- **Use:** "Ten Extreme Dao - the original rule needed for the birth of the star ring." Xu Qing's 8th Extreme of Space is enlightened from the Primeval Sea passageway. Spatial-birth-rule perception.
- **Source:** `xu_qing_abilities.txt`.

### [BTT] — Xu Qing — Space-Time and Parallel Secret Treasuries (Nurturing Spirit Realm)
- **Use:** The final two Secret Treasuries perfected in Nurturing Spirit Realm = Space-Time + Parallel. Operate via divine sense on space-time and parallel existences.
- **Source:** `xu_qing_baidu.txt`.

---

## 10. Medical / Healing (diagnosing injuries, guiding medicine)

### [RI] — Wang Lin — Li Muwan's damaged foundation diagnosis
- **Use:** "Because she helped Wang Lin by using her lifespan to inscribe the Azure Dragon Jade Slip, her foundation was damaged, causing her cultivation to stagnate at the early Core Formation stage after reaching Core Formation. After their relationship was confirmed at the Yuntian Sect, Wang Lin repaired her damaged foundation." Divine-sense diagnosis + repair.
- **Source:** `li_muwan.html`.

### [RI] — Wang Lin — Li Muwan's nascent soul trembling (diagnosing the coffin's effect)
- **Use:** "When it swept by, Li Muwan's nascent soul trembled and showed signs of collapse. However, just at this moment, the sliver of life force inside Li Muwan's nascent soul began to move and the collapse slowed." Divine-sense perception of soul damage.
- **Source:** `wl_main.json`.

### [Ptt] — Su Ming — sensed his own soul losing connection to Surging Indulger Clone
- **Use:** Diagnostic use of divine sense on the self.
- **Source:** `p6-su-ming.json`.

### [Ptt] — Su Ming — Welcoming of Deities (soul-sensing pill)
- **Use:** (See Cat 5 entry.) Senses souls present in the world — diagnostic.

### [AWE] — Bai Xiaochun — Spirit Refinement on withered leaf 25 times → Purple Pattern
- **Use:** "Relying on the heaven-defying property of the Turtle-Pattern Pot, he performed Spirit Refinement on a withered leaf twenty-five times, achieving the ultimate Purple Pattern!" The act of refining is a medical/spiritual-diagnostic process guided by divine sense.
- **Source:** `bxc_baidu.html`.

### [BTT] — Xu Qing — Purple Crystal rapid physical recovery
- **Use:** "Purple Crystal — powerful recovery (cannot recover spirit energy)." Divine-sense-tier regenerator; diagnoses and repairs the body.
- **Source:** Worklog line 2418.

### [BTT] — Xu Qing — pharmacology genius (divine-sense-guided medicine)
- **Use:** "A pharmacology genius capable of [rapid physical recovery]." Divine sense guides medicine preparation.
- **Source:** `xu_qing_baidu.txt`.

### [BTT] — Xu Qing — Sea Mountain Art (body refining, 10 levels)
- **Use:** Xu Qing's first cultivation technique — body refining via divine-sense-guided Qi. Each level = +1 "tiger" of strength.
- **Source:** `xu_qing_abilities.txt`.

---

## 11. Alchemy / Crafting (using divine sense in pill refinement, artifact forging)

### [RI] — Wang Lin — Dark Heaven Stone stores divine sense for avatars
- **Use:** (See Cat 2 entry.) Divine sense materialized into a stone for avatar creation.

### [RI] — Wang Lin — Soul imprint of Xie Qing → Immortal Guard
- **Use:** (See Cat 5 entry.) Wang Lin refined an ancient soul into a guard — soul-crafting via divine sense.

### [RI] — Wang Lin — Wang Lin refines corpses into combat puppets (Corpse Refinement)
- **Use:** Wang Lin's signature corpse-refinement. Each refinement stage requires divine-sense manipulation of the corpse's soul fragment.
- **Source:** Worklog line 524.
- **Cross-Categories:** Soul/Mind Interaction (Cat 5).

### [RI] — Wang Lin — Soul-Storing Jade Slip refinement (Li Muwan's lifespan)
- **Use:** "Li Muwan drained her life force to refine the Azure Dragon Jade Slip for him." Refining a jade slip via divine sense costs lifespan.
- **Source:** `li_muwan.html`.
- **Limitations:** Drains lifespan; damages foundation.

### [Ptt] — Su Ming — Branding Art (brands souls)
- **Use:** (See Cat 4 entry.) Soul-crafting via divine-sense branding.

### [Ptt] — Su Ming — Sun Sinking Talisman
- **Use:** "He caught it, sealed and put into his storage bag." Talisman-crafting via divine-sense sealing.
- **Source:** `p6-su-ming.json`.

### [Ptt] — Su Ming — Han Mountain Bell (life-bound treasure forged via Soul-Seizing Powder)
- **Use:** "Used the Soul-Seizing Powder to guide lightning into his body, forging it into his Awakening realm life-bound treasure. After obtaining the Thunder Barbarian Inheritance, it transformed into a nine-holed cauldron." Lightning-forging via divine-sense-treasure.
- **Source:** `p6-su-ming.json`.

### [Ptt] — Su Ming — Welcoming of Deities pill (absorbs soul fragments)
- **Use:** (See Cat 5 entry.) Pill-crafting via divine sense.

### [ISSTH] — Meng Hao — Alchemy Master (Golden Light Patriarch)
- **Use:** Meng Hao earns the title "Alchemy Master" + "Golden Light Patriarch." His alchemy is explicitly divine-sense-guided (perfect pills via divine-sense-tuned fire).
- **Source:** `menghao_baidu.html`.

### [ISSTH] — Meng Hao — Dao Pillars (10th, Perfect Foundation)
- **Use:** "Meng Hao had a Perfect Foundation and reached the 10th level of Qi Condensation, which let him created a 10th Dao Pillar." Dao-pillar creation is a soul-crafting act.
- **Source:** `search_03_ISSTH_formations.txt`.

### [ISSTH] — Meng Hao — 9 Dao Pillars + Perfect Foundation Pill
- **Use:** Meng Hao's alchemy creates perfect pills (Perfect Foundation Pill, One-Colored Nascent Soul Pill ×14 duplicates). Divine sense guides the pill-qi fusion.
- **Source:** `menghao_baidu.html`.

### [AWE] — Bai Xiaochun — Living Pills (sentient pills)
- **Use:** Bai Xiaochun's Living Pills gain sentience — parallel to his Living Formations. Divine sense animates the pill.
- **Source:** Worklog line 6716.
- **Cross-Categories:** Soul/Mind Interaction (Cat 5).

### [AWE] — Bai Xiaochun — 12-colored fire / 22-color flame
- **Use:** "After successfully refining twelve-colored fire, Bai Xiaochun comprehended this Heavenly Fire Divine Ability that could invoke heavenly fire." Fire-refining via divine sense; rare 12-color achievement.
- **Source:** `bxc_baidu.html`.

### [AWE] — Bai Xiaochun — Moonflower refined 21 times
- **Use:** "He refined the Moonflower 21 times." Spirit-refinement-tier crafting.
- **Source:** `bxc_baidu.html`.

### [AWE] — Bai Xiaochun — Turtle-Pattern Pot (spirit-refining multiplier)
- **Use:** "Relying on the heaven-defying property of the Turtle-Pattern Pot, he performed Spirit Refinement on a withered leaf twenty-five times." Crafting amplifier via divine sense.
- **Source:** `bxc_baidu.html`.

### [AWWP] — Wang Baole — Dharmic Armament crafting (mass-produced)
- **Use:** "Wang Baole's mass-produced Dharma Artifacts (法器批量制造)." 17 self-created Dharmic Armaments: The Scabbard, Baole Cannon, Eternal Fortress, Twelve Emperors Puppets, Dharmic Battleship Locust, etc. All crafted via divine sense.
- **Source:** Worklog line 2841.

### [AWWP] — Wang Baole — Devouring Seed (Great Void Qi Devouring Art)
- **Use:** A crafting/absorbing seed that devours raw material for crafting.
- **Source:** Worklog line 1492.

### [BTT] — Xu Qing — Sundial Life Lamp refinement (5 Life Lamps → Sundial via Heavenly Fire)
- **Use:** (See Cat 5 entry.) Lamp-crafting via divine sense + Heavenly Fire.

### [BTT] — Xu Qing — Sea Mountain Art (body refining — body-as-craft)
- **Use:** Body refining IS self-crafting via divine sense.
- **Source:** `xu_qing_abilities.txt`.

### [BTT] — Xu Qing — Soul Devouring Demonfire Sutra (Foundation Establishment, Ch. 137)
- **Use:** "Upon ascending to the Foundation Establishment Realm, the Sea and Mountain Art peaked in the Qi Condensation Realm and he had to practice another martial art." The Soul Devouring Demonfire Sutra is a soul-crafting method.
- **Source:** `xu_qing_abilities.txt`.

---

## 12. Teaching / Transmission (jade slips, knowledge branding)

### [RI] — Universal — Soul-Storing Jade Slip (储魂玉简)
- **Use:** Memory/transmission medium — cultivator brands knowledge (techniques, formulas, maps, last words) into jade via divine sense; recipient reads with divine sense.
- **Source:** Worklog line 5290 (T03).

### [RI] — Universal — Voice Transmission Jade Slip (传音玉简)
- **Use:** Paired communication device; mid-tier sect disciples (Foundation+) carry them.
- **Source:** Worklog line 5290 (T04).

### [RI] — Wang Lin — Azure Dragon Jade Slip (Li Muwan → Wang Lin)
- **Use:** (See Cat 2 entry.) Defensive array encoded in a jade slip; auto-activates to protect Wang Lin.

### [RI] — Wang Lin — Life-protecting jade slip (Wang Lin → Xuan Luo)
- **Use:** "Before Xuan Luo departs, Wang Lin gives him a life-protecting jade slip for his future reincarnation."
- **Source:** `search_06_RI_talismans.txt`.

### [RI] — Wang Lin — Jade slip made of rules (Duanmu Ji)
- **Use:** "He then tries to kill Duanmu Ji by commanding the souls and deceives him into giving jade slip made of rules to leave body of Ancient God Tu Si." Jade slip encoded with rule-laws via divine sense.
- **Source:** `wl_main.json`.

### [RI] — Wang Lin — Legendary jade slip from Celestial Ancestor Huang Yu (Ch. 126-127)
- **Use:** "Wang Lin captures Celestial Ancestor Huang Yu and uncovers a legendary jade slip containing true [method]."
- **Source:** `search_06_RI_talismans.txt`.

### [RI] — Wang Lin — Ji Qiong skull divine sense (Slaughter's message)
- **Use:** (See Cat 5 entry.) Divine sense persists after death, carrying the method to revive Li Muwan.

### [RI] — Li Muwan — lifespan-cost of jade slip refinement
- **Use:** "Due to depleting her lifespan and damaging her origin to refine a jade slip, she lost hope of forming her Nascent Soul multiple times." Jade-slip refinement has a soul-tier cost.
- **Source:** `wl_baidu_main.json`.

### [Ptt] — Su Ming — jade slip with map (refined for crystals by bald crane)
- **Use:** "The bald crane was moaning about crystals, which Su Ming used to refine jade slips with a map."
- **Source:** `p1-bald-crane.json`.

### [Ptt] — Su Ming — transparent jade slip (Candle Dragon)
- **Use:** "Inside, he found purple armor and a transparent jade slip." Found inside the Candle Dragon carcass.
- **Source:** `p6-su-ming.json`.

### [ISSTH] — Meng Hao — Sealing Heaven Art (transmitted by Yi Ranzi, Ch. 1227)
- **Use:** "Learns from Yi Ranzi that the Dao lies within one's heart, and intention is in one's eyes; to traverse the mountains and seas, this is the 'Sealing Heaven Art'." Direct transmission of art.
- **Source:** `menghao_baidu.html`.

### [ISSTH] — Meng Hao — Bronze Lamp (residual will of original owner)
- **Use:** (See Cat 2 entry.) Soul-tier transmission via a soul-fragment-bearing lamp.

### [AWE] — Bai Xiaochun — jade slip from Li Qinghou (Dan Soldier Formulations)
- **Use:** "The jade slip given by Li Qinghou recorded 18 types of 'Dan Soldier Formulations' summarized and compiled by predecessors of the Lingxi Sect."
- **Source:** `bxc_baidu.html`.

### [AWE] — Bai Xiaochun — jade slip from Reverse River Sect (Cold Gate legacy)
- **Use:** "It was gifted to Bai Xiaochun by the Reverse River Sect before he went to the Starry Sky Dao Polarity Sect as a hostage."
- **Source:** `bxc_baidu.html`.

### [AWE] — Bai Xiaochun — recorded jade slip (found trial entrance)
- **Use:** "Several days later, following a recorded jade slip, he found the entrance to the trial grounds."
- **Source:** `bxc_baidu.html`.

### [AWWP] — Wang Baole — Voice Transmission Ring (Ch. 2)
- **Use:** An acquired item — paired communication device, Foundation+ standard.
- **Source:** Worklog line 1408.

### [AWWP] — Wang Baole — six divine powers via remnant Divine Sense
- **Use:** (See Cat 1 entry.) Transmission via remnant divine sense — a "jade slip made of pure divine sense."

### [AWWP] — Wang Baole — Immortal Inheritance (Ch. 1086 + Ch. 1273)
- **Use:** "Immortal Inheritance (Ch.1086 1st part from the Ancient; Ch.1273 Immortal Symbol from Chen Qing)." Two-part inheritance transmission.
- **Source:** Worklog line 1495.

### [BTT] — Xu Qing — Purple Heaven Limitless Crown usage technique jade slip (gift from Seventh Master)
- **Use:** "He was gifted the Purple Heaven Limitless Crown usage technique jade slip, the Substitute Life Ghost Doll, the spell Roaring Sea Nine Folds, the arcane art Profound Nether Curse, and the secret art Nine Springs Below."
- **Source:** `xu_qing_baidu.txt`.
- **Cross-Categories:** Identity/Recognition (Cat 14 — Imperial-grade transmission).

### [BTT] — Xu Qing — 49 Taboo Formations beast hide
- **Use:** Looted from 5th Star Ring Imperial Sovereign. Beast-hide-encoded formation transmission.
- **Source:** Worklog line 2418.

### [BTT] — Xu Qing — 12 Heavenly Palaces as soul-teachers
- **Use:** Each Heavenly Palace IS a soul-encoded teacher — the Black Umbrella Life Lamp (Divine Soul), the Seven-Colored Phoenix Chant Lamp (Physical Body), the Poison Forbidden Pill (Divine Curse), etc. Each transmits a specific soul-domain knowledge.
- **Source:** `xu_qing_baidu.txt`.

---

## 13. Taming / Beast Interaction (communicating with spirit beasts via divine sense)

### [RI] — Wang Lin — Mosquito Beast (rank-9, life-bound mount)
- **Use:** Wang Lin's signature mount. Tamed/bonded via divine sense; the mosquito beast is a "destined companion."
- **Source:** Worklog line 2812.

### [RI] — Wang Lin — Lei Ji (Thunder Beast)
- **Use:** Wang Lin's life-bound mount.
- **Source:** Worklog line 915.

### [RI] — Wang Lin — Nether Beast
- **Use:** Wang Lin's life-bound mount.
- **Source:** Worklog line 915.

### [RI] — Wang Lin — Thunder Toad (sacrificed for Bloodline Thunder)
- **Use:** "Bloodline Thunder, which was created by sacrificing the Thunder Toad." Beast sacrifice for essence.
- **Source:** `wl_cult.json`.

### [Ptt] — Su Ming — tamed Resentful Wei (made a promise)
- **Use:** "Before entering the fifth kiln, Su Ming tamed a Resentful Wei, by making a promise he would kill anyone, who want to kill it." Promise-based taming via divine-sense bond.
- **Source:** `p6-su-ming.json`.

### [Ptt] — Su Ming — wanted to leave a deep impression on the black horse
- **Use:** "Inside the vortex, Su Ming saw the black horse again. He really wanted to leave a deep impression of himself in that beast to tame it later." Imprinting via divine sense.
- **Source:** `p6-su-ming.json`.

### [Ptt] — Su Ming — tamed the fire ape
- **Use:** "After returning to the Ninth Summit, Su Ming tamed the fire ape, had He Feng enter his shadow after he completed his fusion with the Wings of the Moon."
- **Source:** `p6-su-ming.json`.

### [Ptt] — Su Ming — God Ascension Nectar to attract beasts (bald crane scheme)
- **Use:** "When Su Ming used the poison wasp to inject the God Ascension Nectar into the bald crane. Immediately all ferocious beasts in Divine Essence Star Ocean sensed it." Beast-attraction via divine-sense-broadcast nectar.
- **Source:** `p6-su-ming.json`.

### [Ptt] — Su Ming — Bald Crane (Kong Mo) — transformation beast companion
- **Use:** The bald crane is a transformation-capable beast (Kong Mo's true form). Bonds via divine sense; "extremely loyal — values friendship more than crystals, willing to die for Su Ming."
- **Source:** Worklog line 1155-1160.

### [Ptt] — Su Ming — Crimson Dragon (left with another)
- **Use:** "After releasing her and leaving crimson dragon with her, he rushed towards the mountain." Beast transfer via divine-sense bond.
- **Source:** `p6-su-ming.json`.

### [ISSTH] — Meng Hao — Lord Fifth (parrot)
- **Use:** Ancient divine beast essence (parrot) — comedic sidekick. Bonded via divine sense.
- **Source:** Worklog (ISSTH characters list).

### [ISSTH] — Meng Hao — Meat Jelly (肉冻)
- **Use:** Sentient meat-jelly spirit — comedic sidekick. Bonded via divine sense.
- **Source:** Worklog (ISSTH characters list).

### [AWE] — Bai Xiaochun — Big Toad (Bi Di)
- **Use:** Bai Xiaochun's beast companion. Bonded via divine sense.
- **Source:** Worklog line 520.

### [AWE] — Bai Xiaochun — Iron Egg
- **Use:** Bai Xiaochun's beast companion.
- **Source:** `bxc_baidu.html`.

### [AWE] — Bai Xiaochun — Heaven-Devouring Beast / Ghost King
- **Use:** Beasts that follow Bai Xiaochun. Bonded via divine sense.
- **Source:** Worklog line 520.

### [AWE] — Bai Xiaochun — captured Soul-eating Senior alive
- **Use:** Capturing a soul-eater requires divine-sense-tier dominance.
- **Source:** `bxc_baidu.html`.

### [AWWP] — Wang Baole — Scram the Black Donkey
- **Use:** "Omnivorous-Inanimate, Shameless/Gluttonous, Devouring, knownUsers: Wang Baole." Divine-sense-bonded donkey companion.
- **Source:** Worklog line 1564.

### [AWWP] — Wang Lin — protects Wang Baole via mosquito pet (AWWP Ch. 69)
- **Use:** Wang Lin sends his mosquito pet to protect Wang Baole in AWWP Ch. 69 — cross-novel beast bond.
- **Source:** Worklog line 1801.

### [BTT] — Xu Qing — Mysterious Shadow (sealed in Xu Qing's shadow by Purple Crystal)
- **Use:** Sentient shadow entity. "It once tried to possess Xu Qing but was suppressed and sealed into his shadow by Purple Crystal." Now a bonded quasi-pet.
- **Source:** Worklog line 2561.
- **Cross-Categories:** Soul/Mind Interaction (Cat 5).

### [BTT] — Xu Qing — Heavenly Vine (pet)
- **Use:** Listed in Xu Qing's pets/allies.
- **Source:** Worklog line 2418.

### [BTT] — Xu Qing — Golden Rat (pet)
- **Use:** Listed in Xu Qing's pets/allies.
- **Source:** Worklog line 2418.

### [BTT] — Xu Qing — God's Finger (former pet; dissolved into Void Land)
- **Use:** "Formerly a pet/ally, dissolved and merged into the Void Land to complete the Misfortune lineament."
- **Source:** Worklog line 2418.

### [BTT] — Xu Qing — You Lingzi / Vajra Ancestor (pet)
- **Use:** A pet/ally in Xu Qing's list.
- **Source:** `research-btt/you_lingzi.txt` + Worklog.

---

## 14. Identity / Recognition (recognizing bloodlines, karmic threads, cultivation realm)

### [RI] — Situ Nan — Samsara Eye (glimpse karma lines)
- **Use:** (See Cat 3 entry.)

### [RI] — Situ Nan — Heavenly Eye (see through disguises)
- **Use:** (See Cat 3 entry.)

### [RI] — Situ Nan — Green Soul essence resonates with Heaven-Defying Pearl
- **Use:** "As the reincarnation of the Green Soul, one of the seven souls of the Seven-Colored Immortal Venerable, his Green Soul essence resonates with the Heaven Defying Pearl, enabling him to activate the pearl's heaven-defying fate-altering function." Bloodline recognition via divine sense.
- **Source:** `situ_nan_baidu.html`.

### [RI] — Situ Nan — assisted Wang Lin in severing his karma lines
- **Use:** "He once assisted Wang Lin in severing his karma lines during Wang Lin's battle against the Ancient Dao while stepping into the heavens." Karma-line severance — divine-sense-tier operation.
- **Source:** `situ_nan_baidu.html`.

### [Ptt] — Su Ming — sensed familiarity from Immortal calling him Destiny
- **Use:** (See Cat 1 entry.) Karmic recognition.

### [Ptt] — Su Ming — sensed presence of Lei Chen inside Old Man Extermination
- **Use:** (See Cat 1 entry.) Soul-recognition inside a hostile host.

### [ISSTH] — Meng Hao — Bloodline and Legacy recognition
- **Use:** "Meng Hao is a descendant of the Yinglong Bloodline, the ninth-generation successor of the Fengyao Lineage, a successor of the Blood Immortal, the master of the Golden Light Sect known as the Golden Light Patriarch, a successor of the Qinan Clan, the Sacred Ancestor of the Golden Crow Clan, and the Young Master of the Blood Demon Sect." Each bloodline is soul-recognizable.
- **Source:** `menghao_baidu.html`.

### [ISSTH] — Meng Hao — Extreme Yan recognizes him as master (Ch. 166)
- **Use:** "At the Qingluo Sect, the Extreme Yan recognizes him as its master." Item-spirit recognition via divine sense.
- **Source:** `menghao_baidu.html`.

### [AWE] — Bai Xiaochun — Bai Family Patriarch's recognition of Bai Hao
- **Use:** "After meeting the Bai Family Patriarch, he understood his inner disgust and killing intent towards Bai Hao." Bloodline recognition.
- **Source:** `bxc_baidu.html`.

### [AWE] — Bai Xiaochun — recognized by the Starry Sky Dao Polarity Sect formation
- **Use:** "The Starry Sky Dao Polarity Sect's formation descended, forcibly promoting Bai Xiaochun to Yellow disciple status and elevating him to the rainbow of the Sky Domain." Formation-as-recognition.
- **Source:** `bxc_baidu.html`.

### [AWWP] — Wang Baole — carved name on Dark Child monument by Chen Qingzi
- **Use:** "He wrote Wang Baole's name on the Dark Child monument." Soul-tier identity inscription.
- **Source:** Worklog line 1381.

### [AWWP] — Wang Baole — Dark Child title (carved onto him by Chen Qingzi)
- **Use:** "The Dark Child title (carved onto him by Chen Qingzi)." Identity-as-soul-mark.
- **Source:** Worklog line 1390.

### [AWWP] — Wang Baole — Mysterious Mask spirit recognizes Wang Baole
- **Use:** (See Cat 5 entry.) Mask spirit = Wang Yiyi; recognizes Wang Baole as the mask's bearer.

### [BTT] — Xu Qing — Pluck Stars from the Vault reveals target's complete existence
- **Use:** (See Cat 1 entry.) Reveals soul, karma, fate, all life traces. The ultimate identity-recognition technique.

### [BTT] — Xu Qing — Abnormal Immortal Mark, Innate Divine Mark
- **Use:** "Abnormal Immortal Mark: Innate Divine Mark, formed by..." Each Mark is a soul-tier identifier.
- **Source:** `xu_qing_baidu.txt`.

### [BTT] — Xu Qing — 12 Divine Authority Marks (each identifying a path)
- **Use:** 12 Marks: Abnormal Immortal, Purple Moon, Divine Curse, Misfortune, Sound, Erasure, Six Thieves Delusion, plus more. Each is a soul-tier identity-Mark.
- **Source:** `xu_qing_baidu.txt`.

### [BTT] — Xu Qing — Imperial Capital Great Domain inheritor (Sword Palace)
- **Use:** "Inheritor of the Human Clan's Sword Palace; Human Marquis → Prince Zhen Cang; Divine Son of the Truth Words; Grand Tutor to the Imperial Princes." Each title is soul-recognized.
- **Source:** `xu_qing_baidu.txt`.

### [BTT] — Xu Qing — Yan Yue Xuan Tian Clan Grand Profound Heaven
- **Use:** Membership in this clan — soul-tier identity.
- **Source:** `xu_qing_baidu.txt`.

### [BTT] — Xu Qing — Ninth Star Ring Lord
- **Use:** Lordship recognition via divine-sense soul-authority.

---

## 15. World / Environment Sensing (sensing spirit veins, Qi currents, world laws)

### [RI] — Wang Lin — sense spread out across Alliance Star System
- **Use:** (See Cat 1 entry.) World-scale sensing.

### [RI] — Wang Lin — Heaven-Trampling divine sense covering entire Celestial Clan
- **Use:** (See Cat 1 entry.) Clan-scale sensing.

### [RI] — Wang Lin — Forest that distorts Divine sense
- **Use:** (See Cat 1 + Cat 6 entries.) Terrain-induced sensing distortion.

### [RI] — Wang Lin — Six Cultivation Planets Restriction
- **Use:** (See Cat 2 + Cat 4 entries.) Multi-planet-scale sensing + restriction.

### [RI] — Wang Lin — spirit vein (Earth Fire Veins absorbed by Fire Essence True Body)
- **Use:** "After entering the Immortal Astral Continent, Wang Lin realized that his foreign Fire Essence could absorb the Heavenly Bull Continent's Fire Veins, after consuming around 120 Fire Veins." Divine-sense-tier vein absorption.
- **Source:** `wl_cult.json`.
- **Cross-Categories:** Physical Manipulation (Cat 2).

### [Ptt] — Su Ming — Spirit Vein dragon lines visible to divine sense
- **Use:** (Per worklog — verified in `ergen-knowledge-base.md`: "Spirit-vein 'dragon lines' visible to divine sense as luminous threads".) Canon: Su Ming's divine sense sees spirit veins as luminous dragon lines.
- **Source:** Worklog line 6687 + `ergen-knowledge-base.md`.
- **Limitations:** Requires Foundation+ divine sense.

### [Ptt] — Su Ming — sensed asleep Arid Triad's will
- **Use:** (See Cat 1 entry.) Cosmic-scale world-will sensing.

### [Ptt] — Su Ming — possessed True Morning Dao World → knew everything
- **Use:** (See Cat 9 entry.) World-possession = omni-perception within that world.

### [Ptt] — Su Ming — sensed the Eye of Solar Kalpa revealed his location
- **Use:** "While Su Ming wanted to hide at some planet, the Eye of Solar Kalpa suddenly appeared and revealed his location." Cosmic-tier perception event.
- **Source:** `p6-su-ming.json`.

### [ISSTH] — Meng Hao — Immortal Sense Soil (Black Lands)
- **Use:** (See Cat 4 entry.) Soil carrying residual divine sense from ancient talismans.

### [ISSTH] — Meng Hao — Dao Lake / Dao Scar
- **Use:** Meng Hao's Perfect Foundation creates a Dao Scar in the Dao Lake — world-level soul-imprint.
- **Source:** Worklog line 3923.

### [AWE] — Bai Xiaochun — Bai Family Ancestral Mountain stomped and collapsed
- **Use:** (See Cat 2 entry.) Terrain response to divine-sense-driven force.

### [AWE] — Bai Xiaochun — Eternal Flower nurturing
- **Use:** "Eternal Continent — a continent dependent on the Eternal Flower, divided into five great immortal domains." World-scale divine-sense-tier ecosystem.
- **Source:** `bxc_baidu.html`.

### [AWE] — Bai Xiaochun — Bai Xiaochun opened his eyes → heavens and earth changed color
- **Use:** "Bai Xiaochun opened his eyes, causing the heavens and earth to change color, and emitted the aura of the Kui Ancestor, halting the war." World-resonance via divine-sense-tier awakening.
- **Source:** `bxc_baidu.html`.

### [AWWP] — Wang Baole — Stone Stele World one-step suppression
- **Use:** "In Baole's world, a Universe realm cultivator, which is supposed to be in the 4th step outside of his world only possess 3rd step strength in his world." World-law-tier suppression.
- **Source:** Worklog line 1800.

### [AWWP] — Wang Baole — Federation's cultivation discovered from shards
- **Use:** "Earth started qi cultivation merely 30 years ago. All the manuals [are reverse-engineered from discovered shards]." World-environment shaping via divine-sense-encoded shards.
- **Source:** Worklog line 1798.

### [BTT] — Xu Qing — 36 Star Rings cosmology
- **Use:** "There are 36 Star Rings in the entire world, they contain countless Star Domains." Each Star Ring is a divine-sense-tier cosmic region.
- **Source:** Worklog line 1793.

### [BTT] — Xu Qing — Heterogeneity / Mutagen (God's aura as environmental corruption)
- **Use:** "Mutagen is the Aura of [Gods]." The world-environment is corrupted by the Gods' presence — sensing the corruption IS sensing the God.
- **Source:** Worklog line 1792.

### [BTT] — Xu Qing — Seaforming Scripture (world-as-cultivation)
- **Use:** "He cultivates both the path of immortals and gods" via the Seaforming Scripture. World-formation-tier cultivation.
- **Source:** Worklog line 2225.

### [BTT] — Xu Qing — Primeval Sea passageway (Dao of Extreme Space)
- **Use:** "Enlightened from the Primeval Sea passageway." Cosmic-tier world-sensing.
- **Source:** `xu_qing_abilities.txt`.

### [BTT] — Xu Qing — Desert of Time (each grain = a timeline)
- **Use:** (See Cat 9 entry, Infinite Timelessness.) The Desert of Time is a world-environment that the divine sense spans.

---

## Cross-Novel Patterns

### Universals (every protagonist uses these)
- **Perception / Sensing** — every protagonist uses divine sense to scan their environment. Radius scales with realm (Qi Condensation 16-block base in our model, growing logarithmically).
- **Killing Intent Detection** — universal; even mortals can feel dense killing intent at close range. Cultivators detect at range.
- **Concealment / Stealth** — every protagonist has disguised identity at some point (Wang Lin in Soul Transformation/Ascendant; Su Ming as Wang Tao at Ancient Zang; Meng Hao as Fang Mu; Bai Xiaochun as Madam Bai / Ye Zang; Wang Baole via Mysterious Mask; Xu Qing via mud-smeared face + aliases).
- **Jade Slip Transmission** — universal across all 6 novels; the Foundation+ standard communication method.
- **Soul Brand / Imprint** — every protagonist brands souls (Wang Lin's restriction flags, Su Ming's Branding Art, Meng Hao's seal lineages, Bai Xiaochun's Living Pills/Formations, Wang Baole's Dark Art, Xu Qing's 12 Divine Authority Marks).
- **Soul Pressure / Suppression** — universal; higher-realm cultivators suppress lower via divine-sense aura (Wang Lin's Ji Realm; Situ Nan's soul intimidation; Bai Xiaochun's overwhelming killing intent; Xu Qing's God State aura).

### Novel-specific signatures
- **RI** — divine sense IS a weapon (Ji Realm Divine Sense, Soul Piercing Eyes). Divine sense literally creates physical mass (Bai Fan's Mountains Crumble). Divine sense is the gripping force for manipulation (Restriction Flag).
- **Ptt** — divine sense navigates shattered dimensions; possesses worlds; senses asleep cosmic wills. Su Ming's will operates at cosmic scale.
- **ISSTH** — divine sense fuses with the eyes (Seal-Heavens Art: "intention is in the eyes"). Meng Hao's Dao God Scripture Divine Sense Volume is the explicit cultivation of divine sense. Demon Sealer lineage seals tribulation lightning via soul.
- **AWE** — divine sense animates the inanimate (Living Pills, Living Formations). Bai Xiaochun's divine sense "attracts" other souls passively (Zhou Zimo's divine sense was attracted by the ten billion souls).
- **AWWP** — divine sense persists as a remnant in artifacts (Wang Yiyi in the Mysterious Mask; remnant Divine Sense transmitting six divine powers). Dark Art operates on souls.
- **BTT** — divine sense is a constructive material (Soul-Weaving Technique literally builds a godly entity from soul threads). Pluck Stars from the Vault reveals complete existence via immortal will. The Broken God Face's perception IS the attack.

### Protagonist-specific signatures
- **Wang Lin (RI):** Ji Realm Divine Sense + Restriction Flag + Mountains Crumble (learned from Bai Fan) + Samsara Eye/Heavenly Eye (via Situ Nan) + Soul Lasher→Karma Whip + Heaven-Defying Pearl (amplifier). Soul-eating tempering path.
- **Su Ming (Ptt):** Will-swept (cosmic-tier) + Berserker Mark (illusory construct foundation) + Écang Clone (tree-formation) + Branding Art + Abyss Builder clone-creation. Possesses entire worlds.
- **Meng Hao (ISSTH):** Dao God Scripture Divine Sense Volume + Soul Devouring Scripture + 9 anti-Ancient Soul Lamps + 9 Dao Essences + Seal-Heavens Art (intention in eyes) + Bronze Lamp (soul-fragment fuel) + Paper of Cause and Effect. Demon Sealer lineage.
- **Bai Xiaochun (AWE):** Living Pills / Living Formations (animate the inanimate) + 12-colored fire + Spirit Refinement (Turtle-Pattern Pot × 25) + Undying Codex (soul-based immortality) + Cold Gate Mind-Nurturing Art. Soul attracts souls passively.
- **Wang Baole (AWWP):** Dark Art: Soul Guidance + Demonic Eye Art → Divine Eye Art fusion + Mysterious Mask (Wang Yiyi fragment) + remnant Divine Sense (six divine powers) + Hades Coffin (soul-travel vessel) + 100,000 Wang Baole incarnations. Dark Sect path.
- **Xu Qing (BTT):** Soul-Weaving Technique (Xeno-Immortal School — builds godly entity from soul threads) + Pluck Stars from the Vault (reveals complete existence) + Sundial Life Lamp (5 lamps refined, time stagnation/reversal) + Purple Crystal (devouring-feedback perception) + 12 Heavenly Palaces (3 soul-divine) + God State Violet Lord (soul-thread vortex → god body) + dual immortal + god cultivation.

---

## Implementation Implications

### Which uses should be CORE mechanics (always available once learned)
- **Perception / Sensing (Cat 1)** — the snapshot pulse already built in `DivineSense.pulse()`. Tap V = quick pulse.
- **Killing Intent Detection (Cat 8)** — passive, always-on once learned (Foundation+). Alerts the player to nearby hostile intent.
- **Soul Pressure / Suppression (part of Cat 5/Cat 3)** — passive aura; lower-realm NPCs react via the 3-scenario confrontation (`Arrogant Interception / Absolute Suppression / Unmasking the Hypocrite`).
- **Concealment / Stealth (Cat 6)** — passive camouflage values on objects (`ConcealedObject.camouflage` C-value). The player perceives them only if `S_sense ≥ C`.
- **Surveillance / Scrying (Cat 7)** — passive for sect-protecting arrays (the array surveils its area automatically).

### Which uses should be TECHNIQUE-SPECIFIC (in the technique wheel's Divine Sense category)
- **Pulse** (snapshot, Qi Condensation) — the default tap-V.
- **Continuous Sense** (Qi Condensation) — hold-V dual-mode (already designed in `DESIGN_DIVINE_SENSE_TERRAFORMING.md`).
- **Earth Sense** (Foundation) — perceive through ground; Spiritual Layer underground.
- **Object Lifting** (Foundation) — single-block telekinesis; sustained cost.
- **Vein Extraction** (Nascent Soul) — terraform operator on Spiritual Layer spirit veins.
- **Mountain Moving** (Soul Formation) — volume terraform; massive cost.
- **Ji Realm Divine Sense** (RI-protagonist-tier; combat divine sense) — offensive pulse; injures souls.
- **Soul Piercing Eyes** (restriction-mastery gated) — offensive eye technique.
- **Soul Search** (Nascent Soul, forbidden) — already built (`divine.ts soulSearch`).
- **Karma Vision** (Nascent Soul, 20y cooldown) — already built (`/api/world/karma-vision`).
- **Spatial Crack Detection** (Transcendence) — find cross-branch travel paths.
- **Soul-Weaving** (BTT-inspired) — extract soul power, weave into soul threads; high-tier crafting.
- **Pluck Stars from the Vault** (Lord-tier) — reveals target's complete existence (soul/karma/fate/life traces). Endgame technique.
- **Samsara Eye / Heavenly Eye** (eye-technique sub-category) — glimpse karma lines, see through disguises.
- **Branding Art** (Su Ming-inspired) — brand souls; foundation of soul-mastery.

### Which uses should be PASSIVE (always-on once learned)
- Killing Intent Detection — Foundation+ passive.
- Soul Pressure Aura — passive; scales with realm.
- Concealment Camouflage Registry — passive; the engine checks `C ≤ S_sense` automatically on every pulse.
- Life Lamp / Soul Lamp tracking — passive for sect members; the lamp flickers on injury automatically.
- Spirit Vein Perception — Foundation+ passive (veins visible as luminous dragon lines when within radius).

### Implementation gaps to fill (not yet built)
1. **Divine sense as gripping force for physical manipulation** — DONE in `ManipulationCapability.physicalCapability()` (corrected to EQUAL weight in the prior task). Verified canon.
2. **Jade Slip item** — designed but not yet built as a Forge item. Need a `JadeSlipItem` class with brand/read mechanics.
3. **Life Lamp / Soul Lamp block** — designed but not built. Should be a sect-scale block that flickers/cracks/shatters based on member state.
4. **Soul-Weaving Technique** (BTT-inspired) — not yet built. Should be a high-tier divine sense technique that builds soul-thread constructs in the player's sea of consciousness.
5. **Pluck Stars from the Vault** — endgame technique not yet built. Should be a Lord-tier divine sense technique that reveals a target's full karmic/fate/life history.
6. **Samsara Eye / Heavenly Eye** — eye-technique sub-category not yet built. Should pierce disguises and glimpse karma lines.
7. **Heaven-Avoiding Coffin** — talisman not yet built. Should hide occupant from divine sense + heavenly tribulation.
8. **Mysterious Mask** (Wang Baole) — artifact not yet built. Should host a fragment of another being's divine sense (a remnant-spirit AI).
9. **Karma-Stitching Needle / Karma-Cutting Scissors** (BTT) — treasures not yet built. Operate on karma threads directly.
10. **Spirit Vein dragon-line perception** — passive perception of spirit veins as luminous threads; should be a Foundation+ passive that highlights spirit veins in the world when divine sense is active.

### Prime-Directive check
- Divine sense techniques must NOT spawn things into existence. Veins exist in the Spiritual Layer whether or not the player can perceive them. Vein Extraction pulls an existing vein; it does not create one.
- Mortals cannot perceive divine sense techniques. A mortal observing a cultivator using Ji Realm Divine Sense sees only the visible red lightning, not the divine-sense-aspect.
- Concealment formations have a `perceptionFloor` and `breakTier` that are objective properties of the formation, not of the observer. A mortal CANNOT break them; a cultivator can if their divine sense exceeds the formation's spiritual resistance.

---

## Source Methodology

This catalog was built from:
1. **22 cached wiki JSON files** in `/home/z/my-project/tool-results/` (Wang Lin techniques/cult/items/clones/void/guards; Su Ming p1-p6; s1-s12; BTT files).
2. **14 cached HTML pages** (Meng Hao, Su Ming, Wang Baole, Situ Nan, Teng Huayuan, Li Muwan, Yao Xixue, Blood Ancestor, All-Seer, BXC × 2, ISSTH ch 1).
3. **12 formation/talisman search result files** in `research_formation_talisman/`.
4. **15 BTT research files** in `research-btt/` (Xu Qing main/abilities/items/cultivation, Seven Blood Eyes, Living God, Sea Mountain, Mysterious Shadow, Purple Crystal, You Lingzi).
5. **The 6,723-line worklog** — every prior research entry that touched on divine sense, soul, jade slip, karmic thread, life lamp, etc.
6. **3 existing design docs** (`DESIGN_DIVINE_SENSE_TERRAFORMING.md`, `DESIGN_HITBOXES_AND_FORMATIONS.md`, `DESIGN_HEAVEN_AND_EARTH_MANIPULATION.md`).
7. **The Next.js reference implementation** in `src/lib/sim/divine.ts` and `src/lib/sim/ergen-depth.ts`.

The extraction pipeline (Python script) stripped HTML, ran case-insensitive regex matches across 60+ keywords, deduplicated snippets, and produced ~2,150 contextual hits. Each hit was then manually categorized into one of the 15 buckets. Cross-category entries were filed under their primary use and tagged with `Cross-Categories`.

No internet was used. All sources were already cached locally from prior research tasks.

---

## Part 16: RI Wiki Canon Additions — Ji/Shi/Dao Realms (2026-07-12, RI-BIBLE-wiki-research)

> Added by the RI-BIBLE-wiki-research task. Source: https://xian-ni.fandom.com (primary). Full findings in `CANON_RI_WIKI_RESEARCH_FINDINGS.md`.

This section documents the **Three Great Realms** of Renegade Immortal (Ji, Shi, Dao) and their canonical relationship to Divine Sense. The dedicated `https://xian-ni.fandom.com/wiki/Divine_Sense` page **does NOT exist** (Fandom placeholder); this content is synthesized from the `Ji_Realm`, `Shi_Realm`, `Wang Lin/Items`, and `Wang Lin/Techniques` pages.

### 16.1 The Three Great Realms (Ji / Shi / Dao)

The Renegade Immortal universe has three "Great Realms" that are NOT cultivation-tier realms but rather **states of consciousness / power manifestation** that a cultivator can enter. They are:

| Realm | Chinese | Represents | Element / Attribute |
|-------|---------|------------|---------------------|
| **Ji Realm** | 极境 | Extreme Force and **Death** | Lightning (Red Lightning) |
| **Shi Realm** | 释境 | Creativity and **Life** | (Allows cultivator to form own spells) |
| **Dao Realm** | 道境 | (the third of the three Great Realms; canonical mechanics not fully detailed on the wiki) | — |

**Critical canonical mechanic:** Ji, Shi, and Dao each have TWO versions:
1. **Spiritual Energy Version** (qi-integrated) — weaker, easier to reach, lasts for a fleeting moment.
2. **Divine Sense Version** (spiritual-level) — much more powerful, extremely hard to reach, lasts long enough for one to get fully used to the realm.

> "Ji, Shi, and Dao are all divided into the spiritual energy and divine sense versions; the spiritual energy versions are weaker, more easy to reach, and last for a fleeting moment of time. On the other hand, the divine sense versions are much more powerful, extremely hard to reach and last long enough for one to get fully used to the realms."
> — https://xian-ni.fandom.com/wiki/Shi_Realm

### 16.2 Ji Realm — Full Canon

**Source:** https://xian-ni.fandom.com/wiki/Ji_Realm

**What it is:** A unique path of cultivation that revolves around lightning. A cultivator who cultivates the Ji Realm will be **unmatched by any within their cultivation realm**. Example: Wang Lin killed three Core Formation cultivators with it as soon as he broke through to Core Formation.

**Significant flaw:** The realm imposes a **limit on future cultivation**. The limit differs per person — for Wang Lin it was Core Formation, theoretically meaning he could never break through to Nascent Soul.

**Bypasses:**
1. **Avatar cultivation** — form an avatar and have it cultivate WITHOUT the Ji Realm (Wang Lin's approach via the Divine Path / War God Shrine avatar; the avatar's lack of Ji Realm allows unrestricted progression; when fused back to main body, the Ji Realm conflict breaks the lifespan drawback).
2. **Refine into treasure** — separate the Ji Realm from the user and refine it into a treasure for external use. Wang Lin used this to help form his **Thunder Essence**.

**Acquisition (Wang Lin's canon narrative):**
> "Back in the Jue Ming valley, a sliver of this Ji Realm appeared in my body. You were still sleeping then, so you didn't see it. Then my body was destroyed and you saved my soul before going back to sleep again. In the foreign battleground, my Ji Realm reached its completion."

It is unknown how one specifically cultivates the Ji Realm. Wang Lin unknowingly obtained it when he broke through to Foundation Establishment.

**Two types of Ji Realm cultivators:**
1. **First type (qi-integrated)** — has part of the Ji Realm power in their qi. Example: Core Formation realm Wang Lin had the Ji Realm integrated with his qi.
2. **True Ji Realm** — acts at the spiritual level like the 2 other great realms (Shi and Dao).

**Related technique — Death Spell:** The Ji Realm was partially imitated by a technique known as the **Death Spell**, which allows one to turn their spiritual sense into an attack.

### 16.3 Shi Realm — Sister to Ji Realm

**Source:** https://xian-ni.fandom.com/wiki/Shi_Realm

- Shi Realm represents **Creativity and Life** (vs Ji Realm's Extreme Force and Death).
- When cultivators enter this realm, they can form their own spells.
- "Shi realm is not as rare as the Ji realm but still very few can enter this state as no one know how to attain this realm."
- **Liu Wen** was the first cultivator that left a record of **Shi divine sense**.

### 16.4 Divine Sense Abilities (from Wang Lin/Techniques page)

| Ability | Chapter | Notes |
|---------|---------|-------|
| **Blue Flames** | Ch. 121 | Cold-attribute flame from Underworld Ascension Method (not technically divine sense but a byproduct) |
| **Red Lightning (Ji Realm Divine Sense)** | Ch. 127 | Wang Lin's Ji Realm Divine Sense (qi-integrated version initially; True Ji Realm at spiritual level later) |
| **Soul Piercing Eyes (Divine Sense Eyes)** | Ch. 179 | Only obtained once someone has reached a certain level of mastery in restrictions |
| **Heart of Slaughter** | Ch. 574 | (Slaughter intent manifestation, related to divine sense projection) |
| **Heart Restriction** | Ch. 858 | (Restriction-locked divine sense state) |
| **Eyes Suppressing the World** | Ch. 1896 | From the fragment of the sword the green-robed old man (Ji Si) had fused into his eyes |

### 16.5 Divine Sense Treasures (from Wang Lin/Items page)

| Treasure | Chapter | Divine-Sense Function |
|----------|---------|----------------------|
| **Straw Hat** | — | A treasure that could **block out divine senses**. Wang Lin got it from Yunque Zi. Given to Master Yi Chen's granddaughter Ling'er (Ch. 965). |
| **Dark Heaven Stone** | Ch. 965 | When cultivators reach a high enough level, they need Dark Heaven Stones to **store divine sense** to create an avatar or store power spells to use. |
| **Heaven-Avoiding Coffin** | Ch. 819 | Stores and protects Li Muwan's soul (divine-sense-sustained preservation). |
| **Soul Lasher / Karma Whip** | Ch. 731 | Attacks the origin soul at warp speed (divine-sense-level soul weapon). |

### 16.6 Heaven Trampling Divine Sense Glimpse (Second Bridge)

**Source:** https://xian-ni.fandom.com/wiki/Wang_Lin/Cultivation (Second Bridge entry)

When Wang Lin stepped on the Second Heaven-Trampling Bridge, his soul nearly collapsed — his cultivation base was unqualified to cross the bridge. However, he persisted and was granted a glimpse of **Heaven Trampling divine sense** which **covered the entire Celestial Clan**. This glimpse is the canonical upper bound on divine-sense range in the Renegade Immortal universe.

### 16.7 Coverage Gaps (next-fill)

- [ ] Add **Liu Wen** NPC to `CANON_RI_COMPLETE_WORLD.md` (first cultivator with recorded Shi divine sense).
- [ ] Add **Death Spell** technique to `CANON_RI_COMPLETE_TECHNIQUES.md` (imitates Ji Realm; turns spiritual sense into attack).
- [ ] Add **Ji Realm 2-type distinction** to `CANON_RI_COMPLETE_TECHNIQUES.md` (qi-integrated vs True Ji Realm at spiritual level).
- [ ] Cross-link this Part 16 from Part 3 (Combat / Offensive) of the main divine sense doc — Ji Realm's Red Lightning is the canonical example of "divine sense as a weapon" in RI.
- [ ] Cross-link this Part 16 from Part 4 (Formation Interaction) — Soul Piercing Eyes / Divine Sense Eyes require restriction mastery, tying divine sense to the restriction system.
