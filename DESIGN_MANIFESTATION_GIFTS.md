# Design — Manifestation Gifts

> **The interaction mechanic between the player and the protagonist manifestations.**
> The protagonist keeps their original. The player receives an EXACT COPY — the same
> item, duplicated. Not an equivalent, not a parallel, not a redirect. The protagonist's
> original is never removed or diminished. The player's copy is functionally identical.
> The manifestations continue living after canon, so new items, discoveries, and
> adventures become part of the shared simulation.

---

## 0. The Core Philosophy (user directive, corrected)

Three non-negotiable principles:

1. **Canon originals remain theirs.** Wang Lin's Heaven-Defying Bead stays Wang Lin's.
   Meng Hao's copper mirror stays Meng Hao's. We do not loot protagonists.
2. **The player receives an EXACT COPY.** Not an equivalent, not a parallel, not a
   redirect. When the player earns Wang Lin's trust and requests the Karma Whip,
   they receive an exact duplicate of the Karma Whip. Wang Lin keeps his. The player
   gets one too. Same item. Two copies. The affinity system is the *prerequisite*,
   not the *currency*. A manifestation decides to give based on an in-universe
   four-question evaluation — not a shop transaction.
3. **Post-canon possessions are simpler.** Anything the manifestation acquires *during
   the simulation* belongs to that manifestation, and may flow to the player through
   genuine friendship.

---

## 1. Two Categories of Possessions

### 1A. Canonical Arsenal

Everything the protagonist possessed at the end of their novel.

| Sub-category | Examples | Transfer mechanism |
|---|---|---|
| Techniques | Wang Lin's 14 Essences, Meng Hao's 8 Hexes, Bai Xiaochun's Undying Codex | **Teaching.** A protagonist teaching you a technique does not diminish their own mastery. Learning directly from them is the most lore-friendly path. |
| Treasures | Wang Lin's Heaven-Defying Bead, Karma Whip, 7 Origin Swords; Meng Hao's Copper Mirror, Blood Demon Grand Magic | **Exact copy.** The original remains theirs. The player receives an identical duplicate — the same item, duplicated. Not a teaching redirect, not an equivalent, not a parallel. Two copies of the real thing. |
| Pets / Companion beasts | Wang Lin's Thunder Toad, Mosquito; Bai Xiaochun's Undying Live Forever Seal creatures | **Taming method, offspring, or successor.** The original bond is preserved. The player learns the taming art, earns a related creature's trust, or receives an offspring where the species biology allows. |
| Inheritances | Tu Si's legacy, Bai Fan's Collection Pavilion, the Restriction Mountain trial | **Exact copy where applicable.** If the inheritance is a physical item (a treasure, a technique scroll), the player receives an exact copy. If the inheritance is comprehension-based (an Essence), the player still must comprehend it themselves — but the protagonist can teach the path. |
| Storage items | Wang Lin's storage pouch | **Exact copy of canon contents.** The protagonist's canonical storage is duplicated — the player receives copies of everything inside. Post-canon contents are gifted normally. |
| Artifacts / forged gear | Wang Lin's Immortal Guards, Meng Hao's Woodpit Pan | **Exact copy.** The player receives a duplicate of the artifact. The protagonist keeps theirs. |

**Rule:** the original always remains theirs. The player receives an EXACT COPY —
the same item, duplicated. The affinity system gives the player a way to earn the
protagonist's willingness to give, not to steal or extract. The four-question evaluation
determines whether the gift happens; the result is always the real item (a copy),
never a redirect or substitute.

### 1B. Post-Canon Possessions

Everything the manifestation acquires *during the game's simulation* — after the edge
of canon.

| Sub-category | Examples | Transfer mechanism |
|---|---|---|
| Herbs | Spirit herbs the manifestation harvests while travelling | Gift, trade, or carry-request |
| Beast cores | Cores from beasts the manifestation slays | Gift, trade, or carry-request |
| Spirit stones | Stones the manifestation earns or mines | Gift, trade, or carry-request |
| Forged treasures | Items the manifestation crafts post-canon | Gift, trade, or carry-request |
| Techniques | New techniques the manifestation develops post-canon | Teaching (genuine mentorship) |
| Crafting materials | Ores, woods, essences gathered post-canon | Gift, trade, or carry-request |

**Rule:** these belong to the manifestation. If the relationship is strong enough, the
manifestation may give, trade, ask the player to carry, or let the player take them
because they trust the player. This feels like genuine friendship, not a game mechanic.

---

## 2. The Gift Decision Engine

> *"Instead of 'spend 50 affinity → remove item,' I'd make it an in-universe decision
> by the protagonist."*

When the player requests an item (or the manifestation considers offering one), the
manifestation's AI evaluates four questions:

### The Four Evaluation Questions

| # | Question | What it tests | Failure outcome |
|---|---|---|---|
| 1 | **"Does he still need this?"** | The manifestation's current attachment to the item. A canonical treasure tied to their identity (the Bead, the Mirror) → always needed. A post-canon herb → rarely needed. | Refuses. "I still need this." |
| 2 | **"Is the player a trusted ally?"** | The relationship depth. Not just a number — has the player fought alongside them, shared meals, defended their interests, proven loyalty over time? | Refuses. "I don't know you well enough." |
| 3 | **"Would giving this away help the player?"** | The manifestation's judgment of whether the player can actually use the item (realm-appropriate, dao-compatible, not dangerous to wield). | Refuses. "You're not ready for this." |
| 4 | **"Does this fit my personality?"** | The personality filter (see §3). Wang Lin rarely volunteers; Bai Xiaochun hands things out constantly; Meng Hao bargains first; Su Ming values understanding over gifts. | Refuses in-character, or transforms the offer (bargain, lesson instead of item, etc.) |

### Decision Logic

```
IF all four questions pass:
    → manifestation gives / teaches / entrusts the item
    → the gift is accompanied by in-character dialogue
    → affinity increases (the act of giving deepens the bond)

IF question 1 fails (still needs it):
    → For IDENTITY-TIED canon items: Q1 does NOT fail. The protagonist keeps the original
      AND gives an exact copy. The question "does he still need this?" is answered by
      "yes, he needs the original — but a copy costs him nothing."
    → For POST-CANON items the manifestation is actively using: Q1 may genuinely fail.
      "I'm using this right now. Come back later."

IF question 2 fails (not trusted enough):
    → "Prove yourself first."
    → no offer; the player must deepen the relationship through shared experience

IF question 3 fails (not ready):
    → "Your realm cannot bear this. Return when you are stronger."
    → the offer is held open; the player may re-request at a higher tier

IF question 4 fails (personality mismatch):
    → personality-specific refusal:
        - Wang Lin: silence (the offer was never going to be volunteered)
        - Meng Hao: "Everything has a price. What will you give me?"
        - Bai Xiaochun: "Take it! Take it! I made too many anyway!" (gives regardless)
        - Su Ming: "Do you understand what this is? Tell me, and it is yours."
        - Xu Qing: silent evaluation, then a single sentence of judgment
```

**Affinity is the prerequisite, not the currency.** You cannot "spend" affinity to
extract items. Affinity unlocks the *possibility* of a gift; the manifestation's
four-question evaluation decides whether the gift actually happens.

---

## 3. Per-Protagonist Personality Profiles

> *"Different protagonists should behave differently. This could become one of the
> coolest systems in the game."*

The underlying system is the same (four-question evaluation). Each protagonist's
*personality* shapes how the four questions are weighted and how the outcome is
expressed.

### Wang Lin — The Reserved Mentor

- **Canon:** Renegade Immortal. Stoic, ruthless to enemies, fiercely loyal to the few
  he loves. Lost his clan, his parents, his first love (temporarily), his son (to time).
  Trusts almost no one. When he *does* trust, he entrusts incredibly valuable things.
- **Gift behavior:** Rarely volunteers anything. Will not hand you things constantly.
  But if you have truly earned his trust — through years of shared experience, through
  defending what he cares about, through proving your dao — he will entrust you with
  techniques, with exact copies of his treasures, with knowledge that could change
  your cultivation forever.
- **Question weighting:** Q2 (trust) is weighted 2×. Q4 (personality) almost always
  filters out impulsive giving.
- **Signature line:** *"Take it. You've earned it."* (rare; weighty when it comes)

### Meng Hao — The Bargainer

- **Canon:** I Shall Seal the Heavens. Commercially minded (the "Lord Fifth" era, the
  Spirit Stone Society, the auction arc). Believes everything has a price. Generous to
  friends, but always bargains first — it's a form of respect.
- **Gift behavior:** Will bargain with you before giving. The bargain itself is the
  relationship-building. If you drive a hard bargain, he respects you more. If you
  accept his opening price, he thinks less of you.
- **Question weighting:** Q4 (personality) transforms the interaction into a bargain
  sequence. Q2 (trust) determines whether the bargain is fair or predatory.
- **Signature line:** *"Everything has a price. What will you give me?"*

### Bai Xiaochun — The Generous Fool

- **Canon:** A Will Eternal. Fearful of death, obsessed with pills, accidentally
  generous. Makes too many pills and hands them out. Genuinely loves his friends.
  Chatty, dramatic, emotionally transparent.
- **Gift behavior:** Hands you things constantly. Made too many pills? Have some. Found
  a shiny rock? It's yours now. The filter is almost entirely Q3 (would this help the
  player) — he'll give you things that are dangerous because he didn't check.
- **Question weighting:** Q4 (personality) almost never filters (he gives freely).
  Q3 (readiness) is the primary gate, but Bai Xiaochun is sloppy about checking it.
- **Signature line:** *"Take it! Take it! I made too many anyway! Don't die, okay?!"*

### Su Ming — The Understanding-Seeker

- **Canon:** Pursuit of the Truth. Values comprehension over material things. Will
  give you a treasure freely if you can demonstrate you *understand* it. Values
  questions over answers. Quiet, intense, philosophical.
- **Gift behavior:** May value deep understanding over material gifts. Before giving,
  asks you to explain what the item is, what it means, why you want it. If your answer
  shows comprehension, the item is yours. If not, he teaches instead of giving.
- **Question weighting:** Q4 (personality) transforms the interaction into a
  comprehension test. Q3 (readiness) is judged by understanding, not realm.
- **Signature line:** *"Tell me what this is. Tell me, and it is yours."*

### Xu Qing — The Silent Judge

- **Canon:** Beyond the Timescape. Pragmatic, ruthless when needed, economical with
  words. Judges people by their actions, not their words. Has seen too much to be
  impressed by cultivation realm alone.
- **Gift behavior:** Silent evaluation. Watches you. When you request something, he
  considers in silence, then delivers a single sentence of judgment. Yes or no, with
  minimal explanation. The weight is in what he *doesn't* say.
- **Question weighting:** All four questions weighted evenly, but the *expression* is
  always terse. Q2 (trust) is evaluated by observed actions, not affinity number.
- **Signature line:** *"...Take it."* (the ellipsis is the judgment)

### Future protagonists

The system is extensible. When Wang Baole (AWWP) and other protagonists are added,
they get a PersonalityProfile entry with their own weighting and signature behavior.

---

## 4. Canonical Techniques — Special Case

> *"A protagonist teaching you a technique doesn't diminish their own mastery. If you've
> earned their respect, learning directly from them is one of the most lore-friendly
> ways to obtain it."*

Techniques are the *easiest* category to transfer, because teaching costs the teacher
nothing.

| Technique class | Transfer path | Affinity threshold |
|---|---|---|
| Cultivation methods (the 14 Essences) | Direct teaching, but the player must still comprehend the law independently. Wang Lin can explain Slaughter Essence; the player must still experience slaughter to crystallize it. | High (the manifestation must judge the player ready) |
| Combat arts (Origin Swords, Hexes) | Teaching + sparring. The manifestation demonstrates; the player copies; the manifestation corrects. | Medium |
| Utility arts (Restriction Flag refining, Dream Dao) | Lecture + practice. The manifestation explains the theory; the player practices under supervision. | Medium |
| Signature divine abilities (Body Fixation Art) | Teaching only to trusted disciples. Very rare. The manifestation must judge the player's character. | Very high |

**Key rule:** teaching is not downloading. The manifestation explains; the player must
still cultivate, comprehend, and practice. The manifestation accelerates the path; it
does not replace the work.

---

## 5. Canonical Beasts and Companions — Special Case

> *"If a protagonist has a companion beast, the original remains with them. Your
> progression might instead involve: learning the taming method, earning the companion's
> trust, receiving an offspring or successor, or another lore-consistent mechanism."*

| Beast | Canon owner | Player path |
|---|---|---|
| Thunder Toad | Wang Lin (sacrificed for the 9 Accompanying Thunders) | The Scatter Thunder Clan (devastated but extant) may have related thunder-attribute beasts. The taming method is learnable. |
| Mosquito | Wang Lin (the Billion Soul Banner's companion) | The Mosquito's species biology is unclear; offspring may not be possible. The player can befriend a parallel individual. |
| Lord Fifth | Meng Hao (the Copper Mirror's parrot) | Lord Fifth is bound to the Copper Mirror (stays with Meng Hao). The player cannot receive Lord Fifth. The player can receive a parrot-like beast and name it themselves. |
| Undying Live Forever Seal creatures | Bai Xiaochun | The seal method is teachable. Bai Xiaochun will enthusiastically teach it (and probably has spare creatures). |

**Key rule:** the original bond is preserved. The player's path is always a parallel
or successor relationship, never a transfer of the canonical bond.

---

## 6. What We Explicitly Avoid

### No "pay affinity to steal"

> *"I would avoid 'pay affinity to steal.' ... Instead of 'spend 50 affinity → remove
> item,' I'd make it an in-universe decision by the protagonist."*

The system does NOT implement affinity-as-currency. You cannot extract Wang Lin's Bead
by grinding affinity to 100. The four-question evaluation is the gate; affinity is the
prerequisite to even ask.

### No canonical item removal

The original always remains with its canonical owner. The player receives an EXACT
COPY — an identical duplicate of the item. The protagonist keeps theirs; the player
gets one too. Never a redirect, never a substitute, never "go forge your own."

### No generic gift behavior

Every protagonist behaves differently per their personality. Wang Lin does not hand
you things; Bai Xiaochun does. The system is one engine; the expression is five
distinct characters (and extensible).

### No frozen-at-canon simulation

> *"The manifestations continue living after canon, so new items, discoveries, and
> adventures become part of the shared simulation rather than something frozen at the
> novel's ending."*

The manifestations are alive. They adventure, harvest, craft, and discover during the
simulation. Their post-canon possessions are real and can flow to the player through
genuine friendship.

---

## 7. Implementation Notes

### Data model

- `GiftCategory` enum: `CANONICAL_TECHNIQUE`, `CANONICAL_TREASURE`, `CANONICAL_BEAST`,
  `CANONICAL_INHERITANCE`, `POST_CANON_HERB`, `POST_CANON_CORE`, `POST_CANON_STONE`,
  `POST_CANON_FORGED`, `POST_CANON_TECHNIQUE`, `POST_CANON_MATERIAL`.
- `GiftRecord` record: `id`, `protagonistId`, `category`, `name`, `nameCn`,
  `canonOriginId` (nullable, for canonical items), `personalityFilter` (which question
  is the primary gate), `affinityThreshold`, `realmGate` (minimum player tier),
  `daoCompatibility` (required dao affinities, if any), `transferDialogue` (the line
  spoken when the gift is given).
- `PersonalityProfile` record: `protagonistId`, `q1Weight`, `q2Weight`, `q3Weight`,
  `q4Weight`, `volunteerRate` (how often the manifestation offers unprompted),
  `bargainBehavior` (NONE / FAIR / PREDATORY / COMPREHENSION_TEST), `signatureLine`.
- `GiftDecision` record: the outcome of the four-question evaluation (`OFFERED` /
  `REFUSED_STILL_NEEDED` / `REFUSED_NOT_TRUSTED` / `REFUSED_NOT_READY` /
  `REFUSED_PERSONALITY` / `REDIRECTED_TO_TEACHING` / `REDIRECTED_TO_BARGAIN`).

### Evaluation engine

```
evaluateGift(protagonistId, giftId, playerState):
    gift = GiftRecord.lookup(giftId)
    profile = PersonalityProfile.lookup(protagonistId)
    affinity = playerState.getAffinity(protagonistId)

    # Prerequisite: affinity must meet threshold
    if affinity < gift.affinityThreshold:
        return GiftDecision.REFUSED_NOT_TRUSTED

    # Question 1: Does he still need this?
    # ANSWER: Yes, and he keeps it. The player gets an EXACT COPY.
    # Q1 never blocks for identity-tied items — the protagonist duplicates it.
    # Fall through to Q2/Q3/Q4.

    # Question 2: Is the player a trusted ally?
    if affinity < profile.trustThreshold(gift.category):
        return GiftDecision.REFUSED_NOT_TRUSTED

    # Question 3: Would giving this help the player?
    if playerState.realmTier < gift.realmGate:
        return GiftDecision.REFUSED_NOT_READY
    if gift.daoCompatibility and not playerState.hasDao(gift.daoCompatibility):
        return GiftDecision.REFUSED_NOT_READY

    # Question 4: Does this fit my personality?
    outcome = profile.evaluatePersonality(gift, playerState)
    if outcome == PersonalityOutcome.BARGAIN:
        return GiftDecision.REDIRECTED_TO_BARGAIN
    if outcome == PersonalityOutcome.COMPREHENSION_TEST:
        return GiftDecision.REDIRECTED_TO_COMPREHENSION
    if outcome == PersonalityOutcome.REFUSE:
        return GiftDecision.REFUSED_PERSONALITY

    return GiftDecision.OFFERED  # → player receives an EXACT COPY
```

### Dialogue

Every gift, refusal, and redirect is accompanied by in-character dialogue. The
dialogue is stored in the `GiftRecord` and `PersonalityProfile` (signature lines,
refusal lines, bargain-opening lines, comprehension-test prompts).

---

## 8. Relationship to Existing Systems

| Existing system | Relationship |
|---|---|
| `RIEdgeOfCanonState.ManifestationCompanion` | The companion that *comments* on locations. This gift system is the companion's *interaction* layer — what happens when the player asks the companion for help. The companion is still NOT a quest-giver; gifts are player-initiated or organically offered, never quest-rewarded. |
| `RIEdgeOfCanonState.InheritanceRecord` (14 records, 12-question classification) | The inheritance records classify *whether* an inheritance is accessible. This gift system is the *mechanism* by which accessible inheritances are actually transferred as exact copies. |
| `SamsaraDao` (14 Essences + 9 Bridges) | The Essences are canonical techniques. This gift system governs whether Wang Lin will *teach* an Essence to the player — but the player must still comprehend the law independently (per §4). |
| `HeavenDefyingBead` | The Bead is Wang Lin's. It is `canonicallyTiedToIdentity = true`. The player receives an EXACT COPY when affinity and trust are high enough. No redirect, no "go forge your own." The copy is the same bead. |
| `BridgingPolicy` | All gift content is CANON_CONCRETE (the canonical items) or CANON_IMPLIED (the personality profiles derived from canonical behavior). No SPECULATION-tier gifts. |

---

## 9. Canon Confidence

| Content | Confidence | Source |
|---|---|---|
| Canonical item ownership | CANON_CONCRETE (5) | CANON_RI_COMPLETE_ITEMS.md, CANON_RI_COMPLETE_TECHNIQUES.md |
| Personality profiles (Wang Lin reserved, Meng Hao bargainer, Bai Xiaochun generous, Su Ming understanding-seeker, Xu Qing silent judge) | CANON_IMPLIED (4) | Derived from each protagonist's canonical behavior across their novels |
| Four-question evaluation framework | REASONABLE_RECONSTRUCTION (3) | Game mechanic derived from the user's design directive (not canon, but canon-compatible) |
| Post-canon possessions | SPECULATION_D2 (2) | By definition post-canon; specific items are generated by the simulation, not pre-coded |
| Gift dialogue (signature lines) | CANON_IMPLIED (4) | Paraphrased from canonical speech patterns; not direct quotes unless cited |

---

## 10. The Vision

This system makes the manifestations *characters*, not vending machines. You don't grind
affinity to extract Wang Lin's arsenal. You live alongside Wang Lin, earn his trust over
years of shared experience, and one day — if you've proven yourself — he quietly hands
you an exact copy of the Heaven-Defying Bead and says: *"Take this. It is the same bead.
Mine stays with me. Yours is yours."*

That feels like interacting with a living character. That is the goal.
