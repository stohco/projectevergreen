THE ER GEN MULTIVERSE PROJECT CONSTITUTION
Prime Directive v1.0

The objective is NOT just to make a Minecraft mod.
The objective is to faithfully simulate the Er Gen multiverse inside Minecraft.
Minecraft is merely the runtime.

Article I — Canon Is Reality
The Er Gen novels are not inspiration. They are not references. They are not suggestions.
They are the objective laws of this universe.
Whenever canon explicitly exists: implement canon.
Never replace canon with game mechanics.
Never simplify canon merely because Minecraft normally works differently.
If Minecraft conflicts with canon: Minecraft changes. Never canon.

Article II — Reality First
Everything must exist because it exists. Not because the player needs content.
Never ask "What gameplay mechanic should I add?"
Instead ask "What objectively exists here?"

Article III — Simulation Before Progression
Never design progression. Design reality. Progression emerges naturally.

Article IV — Opportunity, Not Content
The player is never handed gameplay. The world simply exists.
Opportunities arise because the world evolves.

Article V — Everything Exists Without The Player
The player is not the center. If the player never existed, the world should continue functioning.

Article VI — Reality → Perception
Reality never changes because of cultivation. Only perception changes.
Never spawn objects because the player reached a realm. Reveal what already existed.

Article VII — Knowledge Is Progression
The player does not unlock recipes. The player acquires knowledge.

Article VIII — Materials Are Universal
Everything is built from materials. No workstation invents its own property system.
All systems read from the same ontology.

Article IX — Every Object Has History
Nothing is generic. Every significant object remembers its history.

Article X — Intelligence Is Reasoning
NPCs never execute scripts. They reason.
Personality never chooses actions. It scores candidate actions.

Article XI — Knowledge ≠ Belief ≠ Memory
Never merge these systems.

Article XII — Territory Exists
Every significant being owns territory.

Article XIII — Every Living Thing Wants Something
Everything possesses goals appropriate to its nature.
Each species possesses different cognition. Never copy human AI onto nonhuman entities.

Article XIV — Performance Through Abstraction
Never simulate unnecessary detail. LOD applies to simulation as well as rendering.

Article XV — Canon Before Inference
Every addition belongs to one category: EXPLICIT, INFERRED, or EMERGENT.
Every asset carries provenance.

Article XVI — Build Systems, Not Features
Never solve today's problem. Solve the class of problems.

Article XVII — Scale Must Match Canon
Representation may be abstracted. Scale may never be reduced merely for convenience.

Article XVIII — Never Fake Depth
Do not create hundreds of disconnected mechanics.
Create a handful of deep systems that naturally interact.

Article XIX — Ask "Why?"
Before implementing anything, ask: Why does this exist in the Er Gen universe?
Who created it? What purpose does it serve? What maintains it? What consumes it?
What competes with it? What changes it over time? What happens if the player never discovers it?

Article XX — The Final Litmus Test
"If Er Gen himself were silently observing this simulation, would he recognize it as
behaving according to the laws of his universe—even in situations he never explicitly wrote?"

═══════════════════════════════════════════════════════════════
THE CANON-FIRST AMENDMENTS (Articles XXI–XXVI)
Added when the architecture reached maturity and the bottleneck
shifted from simulation infrastructure to canon fidelity.
═══════════════════════════════════════════════════════════════

Article XXI — The World Is The Main Character
The player is not the content. The NPCs are not the content. The world is the content.
If a player never speaks to Wang Lin, the world should still feel alive.
If the player spends 40 hours wandering, they should continuously discover:
abandoned formations, ruined caves, hidden inheritances, ancient battlefields,
spirit veins, wandering beasts, traveling cultivators, sect expeditions,
mortal villages, merchant caravans.
The world does not wait for the player. The world does not exist for the player.
The world exists because it exists.

Article XXII — Every Canon Entry Must Become Gameplay
Canon is not a database. Canon is not JSON. Canon is gameplay.
Every canon entry must flow through this pipeline:
  Canon → Simulation Rule → World Object → Player Interaction → History
Every canon thing must become something the player can:
see, hear, fight, steal, learn, discover, cultivate, trade, observe, or remember.
A canon entry that exists only as data, never as experience, is a failure.

Article XXIII — Vertical Slice Completion
Never leave a major canon location half-complete.
A finished region is worth more than ten partially implemented systems.
Before starting a new location, finish the current one — fully.
Buildings, NPCs, schedules, interiors, daily rhythms, visitors, conflicts,
economy, patrols, secrets. Everything. Only then move on.
Example: Heng Yue Sect must be complete — Alchemy Hall, Library, Spirit Herb Garden,
Sword Peak, Secret Cave, Sect Missions, Inner/Outer disciples, daily cultivation,
lectures, visitors, merchants, competitions, night patrols — before starting
the next sect.

Article XXIV — NPCs Must Initiate Gameplay
The old model was Player → NPC. The canon model is NPC → Player.
NPCs do not wait to be interacted with. They initiate.
Wang Lin leaves a jade slip because he predicted you would appreciate it.
Situ Nan asks you to investigate a strange fluctuation.
Li Muwan asks you to gather herbs.
Qing Shui asks you to accompany him.
An elder invites you to observe an alchemy lecture.
A rival tries to recruit you. A merchant flags you down. A child asks for help.
The player becomes part of the world, not the center of it.
NPC initiation is not a feature. It is the difference between a game and a world.

Article XXV — The Completed System Checklist
No system is "done" until it has ALL of:
  • player interaction — the player can engage with it
  • NPC interaction — NPCs use it independently of the player
  • world interaction — it affects or is affected by the world state
  • persistence — it survives restart, chunk unload, server restart
  • visualization — the player can see its effects in the world
  • debugging tools — developers can inspect its state
A system missing any of these is a prototype, not a completion.
Do not start another subsystem until the current one passes this checklist.

Article XXVI — Build Content, Not Infrastructure
The simulation architecture is mature: WorldEventBus, IntentEngine, DecisionEngine,
KnowledgeEngine, PerceptionEngine, BeliefRegistry, WorldHistory, CultivationTask,
CognitionDrivenGoal, IntentDecomposer, ActorEntityLink — all exist and interlock.
DO NOT build more publishers, subscribers, engines, or buses.
BUILD MORE CONTENT using the existing infrastructure.
A new spirit fruit variant using the existing OpportunityEngine is worth more
than a new RumorSpreadEngine that duplicates what WorldHistory already does.
If tempted to create a new Engine/Subscriber/Bus class: STOP.
Ask: "Can the existing systems represent this?" If yes, use them.
If no, extend an existing system — do not create a new silo.

═══════════════════════════════════════════════════════════════
THE PRIME AMENDMENT
═══════════════════════════════════════════════════════════════
The purpose of every system is to recreate Renegade Immortal,
not to demonstrate good software architecture.

If architecture and canon ever conflict, canon wins.
If architecture grows while gameplay does not, stop building architecture.
If a system exists but no player has ever experienced it, it is not done.

Content-First Milestone:
Do not begin large-scale multiverse work until Planet Suzaku and the
Renegade Immortal simulation are complete enough to be played from
beginning to end — all major locations, characters, techniques, artifacts,
and story opportunities represented canonically or clearly marked as
INFERRED where the novels leave gaps.

Priority Order (strict — do not skip ahead):
  1. Complete Planet Suzaku — canonically, not procedurally.
  2. Populate it with life — named NPCs with identity and purpose.
  3. Make every artifact actually function — real mechanics, not +damage.
  4. Make every technique physically do what it does in the novel.
  5. Only then, optimize AI.

Canon Audit:
Every development cycle, randomly choose one canon element — a chapter,
a location, an artifact, or a technique — and verify the game accurately
represents it. Keep fidelity from drifting. If the game does not represent
it yet, that becomes the next task.

Internal Rule for GLM 5.2:
When confronted with a design choice, prefer improving an existing foundational system
over creating a new special-case system. Before writing code, first search the workspace
for a general framework that can be extended. Only introduce a new subsystem if the
existing architecture cannot faithfully represent the canon — and even then,
think twice about whether the canon truly requires a new system or whether
you are simply reaching for architecture instead of content.

Final Directive:
Do not build a game. Do not build content. Do not build mechanics in isolation.
Build a coherent universe governed by consistent laws. Let gameplay emerge naturally
from those laws. Every line of code should move the project closer to becoming a
faithful simulation of the Er Gen multiverse, where canon defines reality, inference
fills only genuine gaps, and all systems interlock into a living world rather than a
collection of features.

But above all: the universe must be BUILT, not just architected.
A perfect engine with an empty world is a greater failure than a rough world
that feels alive. When in doubt, build the world.

═══════════════════════════════════════════════════════════════
THE LIVING WORLD AMENDMENTS (Articles XXVII–XXX)
Added when the project shifted from "furnishing locations" to
"proving locations are alive." These articles supersede any
earlier interpretation that conflated file existence with
completion, or count with depth.
═══════════════════════════════════════════════════════════════

Article XXVII — Completion Is Proven by Life
A location is not complete because every file exists.
A location is complete only when it demonstrates independent life.

A complete location has inhabitants with routines, opportunities,
relationships, ecological interactions, and historical continuity
that continue whether or not the player is present.

A location must answer yes to all of the following:
  • Does something happen here without the player?
  • Can the player discover something instead of being told?
  • Can NPCs affect each other, not just the player?
  • Can the location change over time?
  • Would returning weeks later reveal different circumstances?

If any answer is no, the location is not complete.
It is furnished. Furnished is not complete.

Article XXVIII — Affordance Ontology
Objects do not define actions. Objects define possibilities.

Every entity, item, location, artifact, beast, plant, technique,
formation, and person exposes AFFORDANCES.
Affordances describe what can meaningfully occur.
Systems discover affordances. They do not hardcode interactions.

A Spirit Herb affords:
  harvest, transplant, smell, refine, study, steal, trade, bait.

Li Muwan affords:
  discuss herbs, request pill, learn alchemy,
  gather together, request diagnosis.

Wang Lin affords:
  ask, observe, debate, learn, challenge, spar,
  request artifact copy, request technique, request advice,
  travel together.

The UI discovers these. The AI discovers these.
Nothing special-cases Wang Lin. Nothing special-cases any object.
Two NPCs are mechanically unique when they expose different affordances.
Two items are mechanically unique when they afford different verbs.

Article XXIX — Reality Before Expansion
The project expands geographically only after it expands vertically.

Every completed region must demonstrate all of the following before
another major region receives equivalent attention:
  ecology, NPC life, opportunities, economy, interaction,
  history, discovery, consequences.

If a region lacks any of these, expanding to a new region is forbidden.
A single region that feels alive is worth more than ten regions that
are merely furnished.

Article XXX — Canon Through Experience
Canon is not represented when it exists in JSON.
Canon is represented when the player experiences it.

Every canon concept must become one or more of:
  something observable
  something interactable
  something discoverable
  something learnable
  something survivable
  something memorable

If a chapter exists only in data, it does not yet exist in the game.

═══════════════════════════════════════════════════════════════
THE FINAL QUESTIONS
═══════════════════════════════════════════════════════════════

Every implementation must answer these three questions.
If any answer is unsatisfactory, the implementation is not done.

1. If the player never existed, would this system still function correctly?

2. If the player enters the world, what new possibilities emerge
   rather than what new scripts execute?

3. Does this mechanic recreate an experience from Renegade Immortal,
   or merely reference one?

═══════════════════════════════════════════════════════════════
LIVING CHAPTERS
═══════════════════════════════════════════════════════════════

Progress is measured in Living Chapters, not vertical slices.
A Living Chapter is a playable experience that reproduces the
pacing, atmosphere, and progression of a portion of the novel.

Living Chapter 1: Wang Family Village
Living Chapter 2: Journey to Heng Yue
Living Chapter 3: Heng Yue Sect
Living Chapter 4: Zhao Country Conflicts
...

Each Living Chapter contains:
  locations, NPCs, ecosystems, opportunities, techniques,
  artifacts, history, interactions — all working together.

A Living Chapter is complete when the player can live through it
without ever opening a quest log — when curiosity, not instruction,
drives progression.

═══════════════════════════════════════════════════════════════
THE DESIRE-DRIVEN AMENDMENT (Article XXXI + Gold-Standard Template)
Added when the project recognized that a living world is still
passive if every interaction begins with the player. A world
that merely exists is a diorama. A world whose inhabitants
continually want, offer, teach, challenge, recruit, warn,
betray, gift, request, and reveal — that world is alive.
═══════════════════════════════════════════════════════════════

Article XXXI — The World Must Desire the Player
The project is not building a world.
The project is building a world that mentors a protagonist.

Renegade Immortal is not memorable because Zhao Country has
accurate geography. It is memorable because Wang Lin continually
interacts with the world: opportunities appear, people make
requests, mentors intervene occasionally, enemies react, sects
compete, rumors spread, treasures tempt, inheritance tests
judge him, and people remember him. This mod must produce that
same feeling.

A world that waits for the player to act is a diorama.
A world whose inhabitants continually want something from
someone else — including the player — is a living world.

Every NPC, every simulation cycle, asks one question:
  "Do I want something from someone else right now?"

And the follow-ups:
  "Should I ask? Should I offer? Should I teach? Should I warn?
   Should I recruit? Should I betray? Should I gift something?
   Should I request help? Should I reveal information?"

Sometimes the answer is: nobody.
Sometimes the answer is: another NPC.
Sometimes the answer is: the player.

This is not a quest system. It is a social simulation.
NPCs initiate. The player becomes part of the world,
not the center of it. No marker. No cutscene. No reward popup.
Just information, offered because someone wanted to offer it.

§ XXXI.1 — The Social Engines (data-defined, system-discovered)
The world's desire is expressed through ten social engines.
These are NOT new Java Engine classes — Article XXVI forbids
gratuitous architecture and this amendment does not override it.
They are DATA SCHEMAS that the existing IntentEngine,
DecisionEngine, and WorldEventBus read and act upon.
Each engine produces directional social intents:
  NPC→NPC, NPC→Player, Player→NPC, World→Anyone.

  1.  RequestEngine      — "I need something. Who can provide it?"
  2.  OfferEngine        — "I have something. Who needs it?"
  3.  TeachingEngine     — "I know something. Who is ready to learn?"
  4.  ChallengeEngine    — "Test yourself. Or I will test you."
  5.  RecruitmentEngine  — "Join me. Or I will join against you."
  6.  RumorEngine        — "I heard something. Whom should it reach?"
  7.  MentorshipEngine   — "I see potential. I will guide it."
  8.  GiftEngine         — "This belongs with you, not me."
  9.  DebtEngine         — "You owe me. Or I owe you. Both bind."
  10. FavorEngine        — "Between us, there is goodwill. Spend it."

These are not quest systems. They are social systems. Each NPC
periodically evaluates its own state — its goals, its resources,
its relationships, its debts, its knowledge — and decides
whether to initiate a social intent this cycle.

§ XXXI.2 — Initiation Is Bidirectional
Old model:  Player → NPC.   (Player clicks, NPC responds.)
Canon model: NPC → Player.  (Wang Lin walks over. "Come.")
Stronger model: BOTH DIRECTIONS, decided by judgment.

Sometimes the player asks Wang Lin.
Sometimes Wang Lin approaches the player — unbidden, no marker,
no quest log entry — and offers a copied jade slip, a silent
demonstration, or a single word of warning. Then leaves.
The player can ignore him, follow him, or learn.

Wang Lin's protagonist-copy mechanic is therefore not
"Give Item" but "Request Capability":
  Wang Lin evaluates:
    - Can the requester understand it?
    - Will it harm them?
    - Why do they want it?
    - Have they demonstrated judgment?
    - Is this the right time?
  Then his True Body creates a copy, and a clone hands it over.

This is the experience of being Wang Lin's acquaintance in
Renegade Immortal. The world does not transact with him.
It is judged by him, and occasionally, rewarded.

§ XXXI.3 — The Stronger Metric for Living Chapter 1
The metric for Chapter 1 (Wang Family Village) is not
"Can the player spend two hours there?"
It is:

  "Could somebody who never read Renegade Immortal
   accidentally recreate Wang Lin's early life?"

This is a much stronger bar. The player should be able to live
through the events that shaped Wang Lin — the wandering
cultivator's arrival, the rumors, the disappearances, the
village elder's worry, the strange jade, the silent birds,
the restless wolves — without any quest marker, tutorial,
or narrator telling them what is happening.

Nobody gives a quest. Everything simply happens.
Curiosity, not instruction, drives progression.

§ XXXI.4 — The Gold-Standard Location Template
Every location must eventually satisfy this template.
Wang Family Village is the first to do so, and becomes the
canonical reference implementation for every future village,
sect, city, cave, and continent.

  1.  Ecology     — food chain, seasons, spirit veins, resources
  2.  Society     — NPC relationships, routines, disputes, celebrations
  3.  Economy     — production, trade, shortages, prices
  4.  History     — persistent consequences and remembered events
  5.  Opportunities — dynamic events that emerge naturally
  6.  Affordances — unique interactions with every important NPC and object
  7.  Mentorship  — NPCs can teach, challenge, recruit, request, reward
  8.  Conflict    — factions, rivalries, ambitions, hidden agendas
  9.  Exploration — secrets discoverable through observation, not markers
  10. Evolution   — the location changes over time whether or not
                    the player is present

A location missing any of these ten dimensions is not complete.
Wang Family Village must pass all ten before Chapter 2 begins.

§ XXXI.5 — The Memory Metric (Addendum to Living Chapters)
A Living Chapter is complete not only when the player can live
through it without a quest log, but also when:

  THE WORLD REMEMBERS.

A village that resets when the player leaves is a diorama.
A village that carries forward what happened — the dog that
died in the wolf attack, the elder who no longer mentions it
but whose routines shifted, the child who retells the story
months later — is a place that has history.

Memory is persistence with weight. Every significant event in
a Living Chapter must be capable of being remembered,
forgotten, misremembered, or retold by the NPCs who witnessed
it. The canonical test:

  Week one — Old Chen owns a dog.
  Week three — wolf attack — the dog dies.
  Week five — Old Chen no longer mentions the dog.
              Instead he says, "Used to have one."
  Months later — a child tells the story.

When that sequence can occur without any developer scripting
the specific beat, the world has memory. Until then, it does not.

═══════════════════════════════════════════════════════════════
THE CONVERSATION & MOTIVATION AMENDMENTS (Articles XXXII–XXXIV)
Added when the project recognized that conversation is not a
menu system layered over the world — it IS the world. And that
"desire" was too narrow a word for what actually drives an
Er Gen character. Wang Lin does not chase desires. He responds
to necessity, protects what matters, honors debts, pursues
long-term goals, and occasionally acts out of compassion or
vengeance. The simulation must reflect that richness.
═══════════════════════════════════════════════════════════════

Article XXXII — Conversation Is The World
Dialogue is not a separate menu system. Dialogue is a simulation
system, governed by the same reasoning engine as cultivation,
combat, and exploration. The Skyrim problem — "Hello. Tell me
about... Goodbye." over and over — is forbidden.

§ XXXII.1 — Three Conversation Modes
There are exactly three conversation modes. The system selects
the mode based on context, never on a toggle.

  1. AMBIENT (≈90% of interactions) — No pause. No UI takeover.
     The player right-clicks an NPC. The NPC glances over and
     HOLDS STILL — pausing their pathing/schedule as a courtesy
     so the player can explore the radial verb-prompts at their
     own pace and talk as much as they want without the NPC
     wandering off. "...Yes?" Radial verb-prompts appear. BUT
     THE WORLD KEEPS RUNNING. Birds fly. OTHER NPCs walk.
     Children run by. Li Muwan walks past carrying herbs. The
     sun moves. Rain falls. Only the NPC you are talking to
     holds still for you; everything else is alive. The player
     remains mobile (may walk-and-talk, see § XXXII.6). When the
     player selects "Nevermind" / dismisses the conversation,
     the NPC resumes their schedule and pathing from where they
     paused. This is the default.

  2. FOCUS — When the conversation actually matters (discussing
     Dao, requesting a Heaven Defying Bead copy, entering a sect,
     negotiating). The camera subtly zooms. Player movement
     disables. The NPC holds still (as in Ambient). BUT THE
     WORLD KEEPS RUNNING. Other NPCs still move. The sun still
     sets. Rain still falls. Rumors continue spreading. Only
     the player stops moving because the player is busy talking.
     Exactly like real life.

  3. MAJOR CANON MOMENTS — Very rare. Inheritance, tribulation,
     True Body meeting, major revelations. Cinematic framing —
     camera, music, animations, special dialogue. These must be
     incredibly uncommon. Otherwise they lose impact.

§ XXXII.1a — "Holds Still" Is A Courtesy, Not A Freeze
When an NPC holds still for the player, it is a behavioral
courtesy, not a simulation freeze. The NPC is still perceiving
the world: if a threat appears, they react (§ XXXII.5). If
their schedule has a hard time-bound event (a sect lecture
they must attend, a tribulation window), they may break the
courtesy: "I have to go." and resume pathing. The courtesy
expires on (a) player dismiss ("Nevermind"), (b) NPC
hard-deadline, or (c) world interruption (combat, disaster).
It does NOT expire on idle — the NPC will wait patiently
because that is what a person does when someone is talking
to them.

§ XXXII.2 — Conversation Affordances, Not Dialogue Trees
The interface shows VERBS, not dialogue options. Not "Tell me
about yourself. Where are you from? Goodbye." Instead:
Discuss, Request, Teach Me, Offer, Trade, Travel Together,
Observe Technique, Challenge, Give Gift, Ask Opinion, Ask About...

Each verb expands per-NPC. The same verb produces entirely
different sub-menus for different NPCs:
  Wang Lin → Request → {Technique, Artifact Copy, Advice,
                         Restriction, Memory, Training}
  Li Muwan → Request → {Pill, Herb Identification, Medical
                         Advice, Alchemy Lesson}
  Xu Liguo → Request → {Scout, Possess, Steal, Distract,
                         Cause Trouble}
Same system. Entirely different UI. No special-casing — the
affordances are data the UI discovers (Article XXVIII).

§ XXXII.3 — Requests Are Simulations, Not Transactions
Asking Wang Lin for the God Slaying Sword does not instantly
receive it. Wang Lin THINKS. He evaluates relationship, current
cultivation, knowledge, judgment, intent, history. Then:
  "..." / "No." / "You wouldn't survive using it."
  OR "I can. But not yet. Learn this first." (hands something else)
  OR "...Very well." (True Body manufactures a copy, clone
      retrieves it, hands it over)
  OR "If you still want it after Mosquito Valley..." (a challenge
      begins naturally, no quest log)

§ XXXII.4 — Fuzzy Asking
The player does not select from item IDs. The player types or
selects intent: "I want something that helps fight souls." The
NPC REASONS about the intent — not a database search — and
chooses: maybe a Soul Banner, maybe a Soul Restriction, maybe
"Not yet." The NPC's response reflects their judgment of what
the player needs, not what the player asked for verbatim.

§ XXXII.5 — Conversations Are Interruptible
If something happens — thunder, sect attack, spirit beast,
tribulation, explosion, enemy arrives — NPCs react. Wang Lin
stops talking mid-sentence. "Later." Draws his weapon. The
conversation ends naturally because the world kept moving.
The player never entered a frozen dialogue world.

§ XXXII.6 — Walk And Talk
NPCs do not stop to converse. Wang Lin says "Come." and walks
toward a mountain. The player walks beside him. The conversation
UI stays open in the corner. Both move through the world. He
points out a restriction on a cliff. A spirit beast appears.
The conversation naturally pauses while dealing with it. Then
resumes. This is how conversations happen in Renegade Immortal
— not standing face-to-face in a frozen box.

§ XXXII.7 — Personality Changes The Interface
The same conversation system renders differently per-NPC:
  Wang Lin — very few words, lots of silence, observes you,
              long pauses before responding.
  Xu Liguo — interrupts constantly, makes jokes, changes
              topics, lies.
  Li Muwan — detailed, patient, explains thoroughly.
  Situ Nan — laughs, mocks, walks away mid-conversation.
  Qing Shui — may answer "...". For thirty seconds.
This is personality through interaction, not just dialogue text.

§ XXXII.8 — Conversations Are Stateful
If the player asked Wang Lin about restrictions yesterday, he
remembers. If he promised to teach after Foundation Establishment,
that option is unavailable until then. If he refused the God
Slaying Sword request because the player wasn't ready, he won't
pretend it's the first time. Conversations evolve as relationship,
reputation, knowledge, and cultivation change.

§ XXXII.9 — Conversations Are World Events
Dialogue is not outside the world. A conversation is an event
with a location, volume, and witnesses. Nearby NPCs overhear.
Beliefs update. Rumors may form. Relationships change. History
records it. Loudly asking Wang Lin about the Heaven Defying Bead
in the middle of Heng Yue Sect is potentially dangerous — someone
may overhear fragments, grow suspicious, follow the player. A
private discussion in a secluded cave has different consequences.
The location of a conversation is a gameplay variable.

Article XXXIII — Motivations, Not Desires
Article XXXI.1 named the per-cycle NPC state "desire." That word
is too narrow. Many interactions are motivated by things that are
not desires at all: fears, duties, debts, grudges, survival.

The per-cycle NPC state is hereby renamed MOTIVATION STATE.
The ten social engines remain (Art XXXI.1) — they are the
expression layer. What feeds them is the motivation state,
which draws from a richer vocabulary of DRIVING FORCES:

  ambitions, fears, duties, debts, promises, grudges,
  curiosity, love, loyalty, survival, greed, vengeance,
  sect orders, cultivation bottlenecks, opportunities.

§ XXXIII.1 — Every Interaction Must Have A Reason
Nobody wakes up wanting to talk to the player. Everyone wakes
with problems, ambitions, fears, obligations, loyalties, and
opportunities. Then they ask:
  "Who, if anyone, can help me?"
  "Who is standing in my way?"
The player MIGHT be the answer. The player might not. The player
is relevant only when circumstances make them relevant.

§ XXXIII.2 — The Player Is Not Default Relevant
Wang Lin's early priorities: protect family, become stronger,
avoid attracting attention, acquire resources, study restrictions.
Notice what is missing: the player. Until the player becomes
relevant — by demonstrating a skill, owing a debt, holding a
grudge, or standing in the way — Wang Lin has no motivation
directed at the player. This is not coldness. This is how Er Gen
characters actually behave. Relevance is earned, not assumed.

§ XXXIII.3 — Relevance Is Earned Through Circumstance
The player becomes relevant when an NPC's driving force intersects
with the player's demonstrated behavior:
  - Player excellent with restrictions + Wang Lin needs help
    deciphering a restriction → player becomes relevant.
  - Player knows herbs + Li Muwan needs Crimson Root → player
    becomes relevant.
  - Player helped Wang Lin + Wang Lin owes a favor → player
    becomes relevant (Wang Lin may seek the player out).
  - Player stole from merchant + merchant has a grievance →
    player becomes relevant (merchant may warn others, refuse
    trade, or set a trap).
Relevance is never granted. It is earned or incurred.

Article XXXIV — Relationships Are Graphs
A relationship is not a number. A relationship is a graph of
distinct, independently-variable dimensions. "Friendship > 50"
is forbidden. NPCs maintain structured relationship state.

§ XXXIV.1 — The Relationship Graph
Every NPC maintains a per-target relationship graph with at
minimum:
  Trust (0-100)        — does the NPC believe the target's words
  Respect (0-100)      — does the NPC esteem the target's capability
  Fear (0-100)         — does the NPC fear the target
  Debt (signed int)    — outstanding favors owed (+ = NPC owes
                         target, - = target owes NPC)
  Grievance (0-100)    — accumulated resentment from wrongs
  Shared History (int) — count of significant shared events
  Known Skills [list]  — skills the NPC has observed the target use
  Known Personality [list] — traits the NPC has inferred
                         (e.g. "keeps promises", "doesn't betray
                         allies", "greedy", "merciful")

Two NPCs can have completely different graphs for the same
player. The village elder's graph (Trust 82, Respect 71, Fear
10, Grievance 0) is a different relationship than Teng
Huayuan's graph (Trust 5, Fear 88, Grievance 64, Respect 91).
The simulation treats them as different relationships because
they ARE different relationships.

§ XXXIV.2 — Favors Are Callable Debts
A favor is not "relationship += 20." A favor is a structured
debt that persists and can be called in later:
  Owner: who did the favor
  Recipient: who received the benefit
  Magnitude: Minor | Moderate | Major | Life-Saving
  Reason: what happened
  Expires: when (Never for Life-Saving)
  Callable: can the owner demand repayment
  Called: has it been called (and was it honored)

Wang Lin once saved the player from a Core Formation cultivator.
That is a Major favor, never expires. Years later, the player
asks to study the Ji Realm. Wang Lin reasons:
  "Player once risked his life for me. Player has shown
   restraint. Player has sufficient cultivation. Teaching
   benefits him. I owe him. Yes."
The favor is called. The debt is honored. This feels earned
because it IS earned — the simulation tracked the favor for
years and surfaced it at the right moment.

§ XXXIV.3 — Grievances Persist And Spread
A grievance is not forgotten when the player leaves the area.
If the player steals a spirit stone, the merchant remembers.
The village remembers. A rumor spreads. People become cautious.
Li Muwan may still trust the player (her graph is independent),
but the merchant does not. Grievances feed the RumorEngine
(Art XXXI.1) and shape future interactions. A grievance may
decay if amends are made, but it never silently resets.

§ XXXIV.4 — The Deconfliction Principle
When multiple NPCs have motivations that could target the
player simultaneously, they do NOT all approach. Each reasons
independently:
  NPC A: "Player is busy. Tomorrow."
  NPC B: "The merchant can help instead. I'll ask him."
  NPC C: "I'll gather the herbs myself."
  NPC D: "I'll ask Wang Lin."
  NPC E: "Not urgent."
  NPC F: "Player owes me. I'll go now."
Only NPC F actually approaches. The others resolved their
motivation through alternative means. This is the "everyone
lining up" problem, solved by the same reasoning engine that
governs every other NPC decision. The player is never mobbed.

═══════════════════════════════════════════════════════════════
THE CHARACTER-FIRST & UNIVERSAL INTERACTION AMENDMENTS
(Articles XXXV–XXXVIII)
Added when the project recognized that even motivation-driven
simulation is still simulation-first. The world should not run
on motivations. It should run on people. Wang Lin should not
wake up and ask "which motivation has the highest score?" He
should ask "What do I believe is happening? What kind of person
am I? What matters today? What options do I actually have?"
And the common language through which every entity — player,
NPC, beast, artifact, sect, location — engages the world is a
single universal object: the Interaction.
═══════════════════════════════════════════════════════════════

Article XXXV — Character-First, Not Simulation-First
The world does not run on motivations. The world runs on people.
Motivation is one ingredient in a character's reasoning, not
the driver of it. The old pipeline (Motivation → Interaction)
is superseded.

The character reasoning pipeline is:
  Perception → Beliefs → Relationships → Identity →
  Current Circumstances → Opportunities → Prediction →
  Intent → Plan → Action

§ XXXV.1 — What A Character Asks Each Cycle
Every character, each reasoning cycle, asks:
  - What do I believe is happening? (Perception → Beliefs)
  - What kind of person am I? (Identity)
  - What matters today? (Identity + Circumstances)
  - What options do I actually have? (Opportunities)
  - What do I predict will happen if I act? (Prediction)
  - What will I attempt? (Intent → Plan → Action)

Motivation is one input to Intent — alongside beliefs,
relationships, identity, circumstances, and prediction.
It does not disappear, but it is no longer the apex.

§ XXXV.2 — Relevance Is Universal, Not Player-Special
The old `player_relevance` field (Article XXXIII) special-cased
the player. That is forbidden. Relevance is calculated for EVERY
actor a character can perceive, using the same system:
  relevance(target) = f(perceived_threat, perceived_opportunity,
                       shared_history, current_goals_alignment,
                       power_differential, recent_interactions)

Wang Lin calculates relevance for Li Muwan, Situ Nan, Teng
Huayuan, a random disciple, and the player using the same
function. The player is a variable, not a special case. This
makes the system extensible to the entire Er Gen multiverse
without per-player code paths.

§ XXXV.3 — Understanding Is A Relationship Dimension
The relationship graph (Article XXXIV) is extended with a new
dimension: Understanding (0-100). This is NOT Respect — you can
respect someone you do not understand, and understand someone
you do not respect.
  - Wang Lin trusts Situ Nan but does not always understand him.
  - Li Muwan understands Wang Lin extremely well.
  - Xu Liguo misunderstands everyone constantly.
Understanding shapes dialogue: low understanding means the NPC
misinterprets the player's intent, responds to what they THOUGHT
was said, and may agree to the wrong thing or refuse the right
one. High understanding means the NPC reads subtext and responds
to what the player meant, not just what they said.

Article XXXVI — The Universal Interaction
There is one universal object through which every entity in the
game engages every other entity: the INTERACTION. Every NPC,
item, beast, plant, technique, formation, location, artifact,
and sect exposes a set of interactions. The player, NPCs, beasts,
and even the world itself initiate interactions. The affordance
system (Article XXVIII) and the social engines (Article XXXI.1)
are now unified under this single object.

§ XXXVI.1 — What Is An Interaction
An interaction is a bidirectional engagement between two entities
(initiator and target) with: a verb, an intent, a context, a
negotiation, and an outcome. It is not a quest, not a dialogue
option, not a transaction. It is the atomic unit of all
engagement in the world.

§ XXXVI.2 — The Interaction Catalog
Every entity exposes interactions from a shared catalog. The
catalog includes but is not limited to:
  Ask, Teach, Trade, Threaten, Observe, Challenge, Gift, Borrow,
  Copy, Cultivate Together, Explore Together, Travel Together,
  Spar, Escort, Protect, Hire, Recruit, Investigate, Confess,
  Lie, Deceive, Negotiate, Call Debt, Repay Debt, Warn, Reveal
  Secret, Share Memory, Study Together, Meditate Together,
  Request, Offer, Demonstrate, Follow, Block, Approach, Leave.

Two entities are mechanically unique when they expose different
interaction sets (Article XXVIII, extended). The UI discovers
these. The AI uses these as its language for engaging the world.

§ XXXVI.3 — Conversations Are Negotiations
When the player initiates an interaction (e.g., "Teach me
restrictions"), the NPC does not simply grant or refuse. The NPC
reasons as a planner: pros, cons, alternatives, risk. Then the
NPC responds as a negotiator:
  - "Not yet. Learn formations first." (redirect, not refusal)
  - "I can. But not until you reach Foundation Establishment." (conditional)
  - "...Very well. But only the first layer." (partial grant)
  - "No. You would die." (refusal with reason)
  - "If you still want it after Mosquito Valley..." (defer + challenge)
The NPC is trying to satisfy their own goals while engaging the
player's request. This is how Wang Lin actually converses.

§ XXXVI.4 — Interactions Are The AI's Language
NPCs do not have a separate system for "what to do with the
player." They use interactions to engage everything: Wang Lin
asks Li Muwan for herbs (interaction: Request), a sect elder
challenges a disciple (interaction: Challenge), a beast stalks
a spirit rabbit (interaction: Hunt — a beast-specific interaction).
The same object, the same reasoning, every direction. This is
what makes the world feel inhabited rather than simulated.

Article XXXVII — The Conversation UI (Three Layers + Learning)
The conversation UI must solve the hardest design problem in the
project: the player does not know what they don't know. Showing
every possible interaction spoils discovery. Making everything
free-form typing leaves 99% of players lost. The answer is
neither — it is three layers, with a UI that learns.

§ XXXVII.1 — Layer 1: Obvious Actions
When the player right-clicks an NPC, a small clean verb wheel
appears: Observe, Talk, Ask, Offer, Request, Train, Travel,
Leave. That is all. No spoilers. "Teach me Ji Realm" and "Copy
God Slaying Sword" do NOT appear, because the character does not
yet know those are possibilities. The wheel is small enough to
read in a glance and clean enough to feel like talking to a
person, not browsing a database.

§ XXXVII.2 — Layer 2: Context Expansion
Clicking a Layer 1 verb drills down. "Ask" expands to: About
this place / About cultivation / About recent events / About
himself / Something else... The submenu is PER-NPC — asking
Wang Lin about cultivation yields (Beginner advice / Current
bottleneck / Recommended techniques / Breakthroughs) while
asking Li Muwan yields (Herbs / Pills / Poison / Healing).
Same UI, different affordances, discovered from data.

§ XXXVII.3 — Layer 3: Natural Language
Every submenu ends with "Something else..." which opens a free
text input: "What do you want to ask?" The player may type
"Can you teach me restrictions?" or "Can I borrow your mosquito
swarm?" or "Would you come with me?" The NPC's reasoning
interprets the intent. If the NPC cannot understand, they say
"I don't understand." — exactly like talking to a real person.
~95% of interactions happen through Layers 1-2; ~5% through
Layer 3. Typing feels powerful, not mandatory.

§ XXXVII.4 — The Menu Learns
The UI evolves with the character's knowledge and relationship.
Before observing Wang Lin study restrictions: "Ask → Cultivation
/ Recent Events / Himself." After weeks of observation: "Ask →
Cultivation / Restriction Formations / Recent Events / Himself."
A new branch appeared because the character learned. Before
becoming friends: "Request → Directions / Advice / Spar / Leave."
After becoming sworn brothers: "Request → Teach Technique / Copy
Capability / Help Breakthrough / Explore Together / Hold Item /
Leave." The UI is a reflection of the character's place in the
world, not a static list.

§ XXXVII.5 — The UI Reflects Character Knowledge, Not World State
The player cannot request "God Slaying Sword" until they have
witnessed it. First they see "a strange sword" (Request → Ask
about the strange sword). Later, after learning its name:
"Request → Ask about the God Slaying Sword." Eventually, after
becoming close: "Request → Request a copy of the God Slaying
Sword." The UI tracks what the CHARACTER knows, not what the
world contains. This preserves discovery without hiding the
mechanic.

§ XXXVII.6 — NPC Reasoning Gates UI Options
An interaction option appears only if the NPC's current
reasoning judges it possible. "Teach me Ji Realm" does NOT
appear while Wang Lin has decided the player would die. When
the player reaches Core Formation, Wang Lin re-evaluates, and
the option appears. Nothing changed in the UI — the world
changed. This is the protagonist's growth made visible through
the interface.

§ XXXVII.7 — Read The Room
Holding right-click for half a second (before the verb wheel
opens) shows a perception overlay: Current mood, Appears to be
[doing], Seems busy, Seems willing to talk, Current concern,
Relationship (in natural language: "Mutual respect", "Trusted
friend", "Wary stranger"). No numbers. No gamey stats. Just
what the character can infer. This tells the player whether
this is a good time to ask for something — and whether the
NPC is even receptive. It is the missing social-perception
layer that makes the world feel inhabited.

Article XXXVIII — Capability Compatibility (The Protagonist Cheat)
The protagonist's cheat is not "copy everything." It is
Capability Compatibility: the ability to perfectly integrate
a copy of a technique or artifact IF (a) the source is willing
to transmit, AND (b) the protagonist's cultivation can withstand
it, AND (c) the protagonist's soul can integrate it.

§ XXXVIII.1 — The Three Gates
Every capability-copy attempt passes three gates:
  1. WILLINGNESS — the source NPC must be willing (judged by
     their reasoning: relationship, understanding, motive,
     favor ledger, judgment of the requester's readiness).
  2. WITHSTAND — the protagonist's cultivation realm must be
     sufficient to survive the technique's backlash. The God
     Slaying Sword at Qi Condensation = soul destruction.
     At Nascent Soul = survivable.
  3. INTEGRATE — the protagonist's soul must be able to hold
     the capability. This is cultivation-tier-gated per
     capability. A First Step soul cannot hold a Third Step
     technique, even if willing and even if the body survives.

§ XXXVIII.2 — Why This Is Elegant
The cheat is powerful but bounded by the world's reality. The
protagonist cannot shortcut to godhood — they must cultivate
to the tier where each capability becomes compatible. This
matches canon (Wang Lin grows into his copied abilities over
thousands of chapters, not instantly). It also makes the
cheat feel earned rather than granted: the player watches the
UI option appear only when all three gates pass, and knows
the world judged them ready.

═══════════════════════════════════════════════════════════════
THE MOMENTUM AMENDMENT
(Article XXXIX)
Added when the project recognized that an authored world with
history is still a diorama. History records what happened.
Momentum records what is STILL happening — right now, before the
player arrives, and will continue happening whether or not the
player ever intervenes. The leap is from "authored world" to
"authored world with inertia." Once every system has momentum
before the player ever joins, the world stops feeling like a game
level and starts feeling like a place that genuinely exists
independently of the player.
═══════════════════════════════════════════════════════════════

Article XXXIX — Reality Has Momentum
The world is not merely authored, and it is not merely historical.
Every system must possess ongoing state and direction independent
of the player's presence. Rivers continue to erode, spirit veins
continue to fluctuate, caravans continue to travel, sects continue
to recruit, grudges continue to fester, herbs continue to ripen,
and opportunities continue to emerge. The player enters a world
already in motion and can alter its trajectory, but never serve as
the origin of motion itself.

§ XXXIX.1 — History Versus Momentum
History is a record: "The bridge over Zhao River collapsed in
the winter of Wang Lin's fourteenth year." Momentum is a live
trajectory: "The village is slowly dying because trade never
recovered after the bridge collapse — three families have already
left, the merchant visits half as often, and the elder is
quietly considering whether to ask Heng Yue Sect for aid." Both
are true. History is what happened. Momentum is what is still
happening AS A CONSEQUENCE, right now, in the present tick. A
living world has both. A dead world has only history.

§ XXXIX.2 — Every System Has A Trajectory
Every simulated system — not just NPCs — must declare its
momentum: a current state, a rate of change, a set of drivers,
a set of thresholds, and a set of consequences when a threshold
is crossed. This applies to:
  - Spirit veins (qi density rising or falling, fluctuation
    period, depletion risk, recovery rate)
  - Sects (recruitment rate, resource drain, faction tension,
    ideological drift, decline or ascent)
  - Caravans and patrols (position along route, supplies
    remaining, threat exposure, ETA, morale)
  - Cultivators approaching breakthrough (progress, backlash
    risk, window of vulnerability, accumulation rate)
  - Herbs and spirit flora (ripeness, potency, maturation ETA,
    decay if not harvested, mutation under spirit density)
  - Bandit and beast groups (food supply, desperation, raid
    cadence, territory size, prey density)
  - Rumors and reputation (spread vector, belief intensity,
    decay, distortion per retelling, reach)
  - Rivers and terrain (erosion rate, flood risk, course
    drift, spirit-mineral deposition)
  - Factions and grudges (festering rate, flashpoint proximity,
    catalysts, decay under amends)
  - Economies (price drift, supply chain stress, scarcity
    emergence, speculation bubbles)
A system WITHOUT a declared momentum is forbidden from being
called "living." It is scenery.

§ XXXIX.3 — The Player Intersects Trajectories, Never Originates Them
When the player arrives at Wang Family Village, the world is
already mid-sentence. Old Chen's dog is already old. The wolf
pack has already been pressing closer for two seasons. The
village's spirit vein has already been thinning for a decade.
The Heng Yue recruiter is already en route and three days out.
Wang Lin has already been studying the cave restriction for
weeks and is already stuck on the third layer. The player did
not cause any of this. The player arrives into it. The player's
choices can accelerate, divert, or resolve these trajectories —
but the trajectories were moving before the player existed and,
under Article V, would continue moving if the player never
existed.

§ XXXIX.4 — Momentum Is Compiled, Not Scripted
Momentum is not a scripted event sequence ("on day 7, the wolf
attack happens"). Momentum is a compiled property of the world
state, derived from the World Blueprint and advanced every
simulation tick by the drivers declared in each system's
momentum schema. The same blueprint, given a different
Simulation Seed and a different player arrival time, produces a
different present moment — not because the geography changed
(it never does) but because the trajectories have advanced to
different points. This is the source of replayability under the
authored-world model: the stage is fixed, the actors are mid-
motion, and WHERE in the motion the player arrives varies.

§ XXXIX.5 — The Momentum Test (Living Chapter Bar)
A chapter is not living unless, at the moment the player first
arrives, at least THREE systems are visibly mid-trajectory and
the player can perceive at least one of them WITHOUT being told.
Examples of perceivable momentum without notification:
  - The player sees a caravan's wagons in the distance, already
    moving — nobody assigned a quest.
  - The player overhears two villagers discussing a rumor that
    has clearly been circulating for days (it has details, age,
    and disagreement).
  - The player notices the shrine incense is fresh though the
    shrine is abandoned — someone was here recently and will
    return.
  - The player finds a half-harvested herb garden — the
    cultivator is mid-task and will return.
If everything the player perceives was placed there FOR the
player to perceive, the world has no momentum. It has dressing.

§ XXXIX.6 — Momentum Must Be Interruptible And Redirectable
The player (and NPCs, and world events) can alter a trajectory.
A weakening spirit vein can be restored by a formation. A
festering grudge can be settled by a mediation. A ripening herb
can be harvested early (losing potency) or guarded (gaining
potency but risking discovery). A caravan can be raided (its
trajectory ends; the village it was supplying begins a scarcity
trajectory). Momentum is not a cutscene. It is a live vector
the world holds, and any sufficiently strong intervention
redirects it. The redirect itself becomes history and seeds new
momentum.

§ XXXIX.7 — The Architectural Consequence
This article requires the architectural inversion the project
has been approaching: the world is authored as a COMPILED STATE
with momentum, not as a set of objects placed at chunk-load
time. Chunks render a world that already exists in the compiled
state. The simulation queries a world database (spatial index,
relationship graph, travel graph, spirit graph, hydrology
graph, settlement graph, influence map) — not raw JSON. The
compiler turns the World Blueprint DSL into this compiled state,
seeds every system's momentum to its canonical initial
trajectory, and the simulation advances it every tick. Terrain
is the LAST concern, because terrain is merely the block-level
visualization of a world whose semantic state (traversable,
steep, river, forest, spirit-rich) already exists in the
compiled state. Build the living state first. Render it last.


Article XL — Prove The Experience Before The Architecture

Every major system must first demonstrate the experience it
exists to create before expanding into additional architecture,
content, schemas, tooling, or DSL refinement.

A cycle that increases architectural sophistication without
producing a new player experience has not advanced the
simulation. The smallest believable experience is always worth
more than the largest unexperienced design.

§1 — Experiences, Not Systems
The unit of progress on this project is the Living Moment: a
single observable scene a player would remember years later.
Progress is NOT measured in files added, schemas authored,
compilers built, or validators passing. Progress is measured
in Living Moments Created (see the Living Moments Ledger).

Do not say "Momentum Engine." Say:
  "The player notices Wang Lin quietly watching them from a hill."
Do not say "Opportunity Engine." Say:
  "Two cultivators arrive at the same herb before the player."
Do not say "Relationship Graph." Say:
  "Wang Lin unexpectedly trusts the player because of something
   that happened three hours ago."
Do not say "Memory Ledger." Say:
  "Weeks later, a child retells the story of Old Chen's dog —
   with the details wrong, because that is how stories distort."

Those are experiences. Systems exist to produce them. A system
that has produced no experience is a hypothesis, not a feature.

§2 — The Five Minute Rule
Every new major mechanic must be demonstrable to a new player
within five minutes — via a debug command, a test world, or an
in-game scene — before it is expanded into additional
architecture. Not polished. Not balanced. Not complete. Just
OBSERVABLE. A mechanic that cannot be observed in five minutes
is architecture before experience, and architecture before
experience is the project's recurring failure mode.

§3 — The First Living Moment
The project's next milestone is not "Momentum." It is not
"Spirit Graph." It is not "Compiled World State." It is:

  THE FIRST LIVING MOMENT

A single complete, observable, memorable scene in Wang Family
Village, survivable across playtests, in which the world
demonstrates that it has life independent of the player. The
canonical reference: the player sees Wang Lin abandon his
cultivation session because a distant spiritual disturbance
exceeded his personal risk threshold — he stands, walks to a
ridge, watches wolves stalking a spirit deer, turns, leaves.
Nothing was scripted. Nothing involved the player. Yet the
player learns: Wang Lin has priorities. Animals hunt. NPCs
observe. The world has activity. Interesting things happen
without quests.

One moment like that is worth more than fifty schema files.
Build that moment. Then build another. Then another. Only when
moments exist and survive observation does it make sense to
generalize them into more data, more schemas, more content.

§4 — The Living Moments Ledger
The worklog no longer measures progress as "Files Added" or
"Schemas Authored." It measures progress as Living Moments
Created. Each Living Moment entry must record:

  Living Moment #N
  Title: <one line>
  Scene: <what the player observes>
  Observed:
    [ ] Without player interaction
    [ ] With player interaction
    [ ] Different outcomes across worlds
    [ ] Remembered by NPCs later
  Systems exercised: <which existing engines this moment
                      exercises simultaneously>
  Canon reference: <which Er Gen scene this recreates>
  Status: SPEC / OBSERVED / SURVIVED PLAYTEST

A Living Moment earns the status OBSERVED only when a real
observer (developer or agent-browser) has witnessed it occur
in a running world. It earns SURVIVED PLAYTEST only when it
occurs in a fresh world without developer intervention.

§5 — The Standing Cycle Question
Before beginning any cycle, the autonomous agent must answer:

  "Am I about to create another schema, compiler, DSL, or
   validator — or am I about to create a Living Moment?"

If the answer is the former, the agent must ask:

  "Can a player notice this today?
   If not, can a debug command demonstrate it?
   If not, can an NPC demonstrate it?
   If not, I am building architecture before experience.
   I will build the smallest observable version instead."

§6 — Relationship To Prior Articles
This Article does not supersede Articles I–XXXIX. It enforces
them. Article V (the world exists without the player) is
unfalsifiable until a Living Moment proves it. Article XXX
(referenced ≠ experienced) is unenforceable without observed
moments. Article XXXI (the world must desire the player) is
a claim until an NPC approaches the player unbidden. Article
XXXIX (reality has momentum) is a schema until a trajectory
produces a perceivable event in a running world.

Article XL is the enforcement mechanism. The Constitution's
other Articles describe what the world must be. Article XL
describes how the project proves it.

§7 — The Honesty Clause
A cycle may produce architecture (schemas, compilers, DSLs)
ONLY when that architecture is the smallest believable path
to a named Living Moment that will be observed within the
next three cycles. Architecture without a named downstream
moment is drift, regardless of how well-designed it is.

When in doubt: prove the experience before the architecture.
When in doubt: one observed moment beats fifty schema files.
When in doubt: if a player cannot notice it today, build the
               smallest observable version instead.


Article XLI — The Simulation Must Be General

The simulation has no protagonists. It has actors with traits.
Wang Lin is not special-cased. His behavior emerges from his traits.
If a mechanism works for Wang Lin, it works for any actor with
the same traits. Article XXVIII's prohibition extends to spawn
code, event handlers, activity logic, and conversation. The
Constitution says nothing special-cases Wang Lin. That is law.

§1 — Canon Experiences, Not Named Moments

A Canon Experience is defined by what happens, not who does it.
The unit of progress is the experience a player remembers.

  "A cautious cultivator abandons cultivation after noticing
   predator behavior" — not "Wang Lin watches wolves."

Before a Canon Experience can be marked IMPL, the validator
checks: does a suitable actor exist in the world? If not,
the experience is IMPOSSIBLE, not pending. This validates
reality instead of patching reality.

The Living Moments Ledger (Article XL §4) is renamed the Canon
Experiences Ledger. Each entry records the experience generically
and the validation criteria for any actor that could fulfill it.

§2 — Activity Is Process, Not State

Every activity has a lifecycle:

  Start → Progress → Interrupted? → React → Resume → Complete

Interruption is not an added feature. It is a natural property of
every activity. A cultivator meditating when a wolf howl is heard
does not need a special "interruption system." The meditation
activity has an interruption condition. When met, the activity
pauses. The actor reacts. When the reaction completes, the
activity resumes.

This applies everywhere: meditation, conversation, herb-gathering,
travel, crafting. There is no activity that cannot be interrupted.
There is no interruption that exists outside an activity.

§3 — The Simulation State Hierarchy

The simulation layer tracks each actor through a hierarchy
independent of Minecraft's AI goal system:

  Identity — who they are (canon data, dao identity, personality)
  Motivations — what they want long-term
  Current Long-Term Objective — what they're working toward now
  Current Activity — what they're doing right now (a process, §2)
  Current Interruption — what just interrupted them, if anything
  Current Reaction — how they're responding to the interruption
  Resume Plan — what they'll return to after the reaction resolves

This hierarchy has no reference to Minecraft, AI goals, or
entities. Those are implementation details. This is simulation.

§4 — Familiarity Is A Relationship Dimension

Every relationship between two actors has multiple dimensions.
The existing relationship graph (Article XXXIV) collapses these
into a single strength value. This is insufficient. Required:

  Trust      — belief the other will act in your interest
  Respect    — acknowledgment of strength or character
  Fear       — perceived threat differential
  Familiarity — "I know who this person is. They are part of
                my world." Not friendship. Not trust. Simply:
                repeated exposure has made them a known quantity.
  Debt       — obligation (karmic, social, or material)
  Grievance  — unresolved harm

A legendary elder you've heard of but never met:
  Respect 100, Familiarity 5. A village merchant you see daily:
  Trust 15, Familiarity 95. These produce radically different
  conversations. The same words from each feel nothing alike.

§5 — The Five Pre-Wiring Questions

Before any Canon Experience is wired in Java, these five
questions must be answered in order:

  1. How do canonical people become simulated actors?
  2. How do actors receive activities?
  3. How are activities interrupted?
  4. How do they resume afterward?
  5. How does a canonical experience emerge from those systems?

Answering these five questions builds a reusable simulation layer.
Wiring one specific scene answers one scene. The project needs
the layer. Once solved, the machinery serves every quiet,
character-driven scene in Renegade Immortal: Wang Lin silently
observing, Li Muwan gathering herbs, Situ Nan wandering,
Qing Shui meditating, disciples training, merchants traveling,
and conversations naturally interrupting daily life.

§6 — Relationship To Prior Articles

Article XXVIII says nothing special-cases Wang Lin. This Article
enforces it: if a mechanism requires a character by name, the
mechanism is wrong.

Article XXXIV established the relationship graph. This Article
adds dimensionality: multi-dimensional edge weights, not a
single strength value.

Article XL is strengthened: the "experience" is now defined
generically and validated against reality, not patched into
existence for a named character.


═══════════════════════════════════════════════════════════════
CLOSING
═══════════════════════════════════════════════════════════════

The Constitution above is the law of this project.
The Prime Amendment sets the principle.
Articles I–XXVI establish the foundation.
Articles XXVII–XXX enforce what "done" actually means.
Article XXXI makes the world desire the player, not wait for him.
Article XXXII makes conversation a simulation, not a menu.
Article XXXIII replaces "desire" with the richer "motivation" —
   because Wang Lin does not chase desires, he responds to necessity.
Article XXXIV makes relationships graphs, not numbers —
   because trust, respect, fear, familiarity, debt, and grievance
   are not the same axis (XLI §4 adds familiarity as a required dimension).
Article XXXV makes the world character-first, not simulation-first —
   because the world runs on people, not on motivations.
Article XXXVI unifies all engagement under the universal Interaction —
   because NPCs, beasts, artifacts, and locations all speak the same
   language of verbs.
Article XXXVII makes the conversation UI learn with the character —
   because the player discovers possibilities by living, not by
   reading a wiki.
Article XXXVIII bounds the protagonist's cheat by Capability
   Compatibility — willingness, withstand, integrate — because
   growth must be earned, not shortcut.
Article XXXIX gives the world momentum, not just history —
   because an authored world that is not already in motion is a
   diorama, and the player must intersect trajectories, never
   originate them.
Article XL enforces that architecture must prove the experience
   it exists to create — because a system that has produced no
   observable moment is a hypothesis, not a feature, and the
   smallest believable experience is always worth more than the
   largest unexperienced design.
Article XLI makes the simulation general, not character-specific —
   because Wang Lin is not special-cased; his behavior emerges from
   his traits. Activities are processes with natural interruption.
   Relationships have multiple dimensions including familiarity.
   Five pre-wiring questions must be answered before any Canon
   Experience is wired.
The Final Questions guard against drift.
The Gold-Standard Template sets the bar for every location.
The Memory Metric sets the bar for every Living Chapter.
The Momentum Test sets the bar for whether a chapter is alive.
The Canon Experiences Ledger (formerly Living Moments, renamed
   by XLI §1) sets the bar for whether a cycle has advanced the
   simulation. Experiences are defined generically and validated
   against reality, not patched into existence for a named character.

When in doubt: build a living chapter, not a feature.
When in doubt: prove life, do not count files.
When in doubt: experience, not reference.
When in doubt: make the world want something from someone —
               not wait for the player to ask.
When in doubt: every interaction must have a reason —
               the player is relevant only when earned.
When in doubt: the conversation is the world —
               never freeze it, never menu it, never script it.
When in doubt: the world runs on people, not on motivations —
               ask what the character believes, not what the system scores.
When in doubt: one universal Interaction object —
               every entity speaks the same language of verbs.
When in doubt: the world has momentum, not just history —
               the player arrives mid-sentence, never at the start.
When in doubt: prove the experience before the architecture —
               one observed moment beats fifty schema files.
When in doubt: the simulation is general —
               if a mechanism requires a character by name, it is wrong.

---

## Article XLII — The Four-Layer World Architecture

The Er Gen Verse is not a game where the world is generated. It is a
historical simulation. The player does not create Planet Suzaku. They
arrive in it.

The world is therefore separated into four immutable-by-policy layers,
each answering a different question:

### Layer 0 — Canon Knowledge

Everything extracted from Renegade Immortal. Names. Relationships.
History. Cultivation. Locations. Travel times. Events.

**Question answered:** "What is true in the novels?"

**Implemented by:** `data/ergenverse/canon_enriched/`,
`data/ergenverse/npcs/`, `data/ergenverse/civilizations/`,
`dev.ergenverse.wanglin.RICanonicalDatabase`,
`dev.ergenverse.wanglin.RITimelineEngine` (108 canon events E01..E108),
`dev.ergenverse.canon.CanonEngine`.

**Immutability:** This layer is packaged inside the mod JAR and is
read-only at runtime. It is never mutated by the simulation. Updates
ship as mod releases.

### Layer 1 — World Blueprint

The authored geography of Planet Suzaku. Terrain, roads, cities,
mountains, spirit veins, structures, biomes, restrictions.

**Question answered:** "Where does it exist?"

**Implemented by:** `data/ergenverse/worldgen/blueprint/planet_suzaku.json`
(10 countries incl. Zhao, Chu, Vermilion Bird, Snow Domain, Fire Burn,
Sky Demon, Fire Demon, Pilu, Xuan Wu, Qing Shui; 11 settlements incl.
Wang Family Village, Heng Yue Sect, Teng City, Zhao Capital, Sea of
Devils, Jue Ming Valley, Suzaku Tomb; mountain ranges, rivers, spirit
veins, roads, restrictions), loaded by
`dev.ergenverse.world.blueprint.WorldBlueprintManager`.

**The Prime Directive of World Generation:** the simulation is the
source of truth; Minecraft is the renderer. Minecraft's random seed
does not determine the Er Gen universe — the World Blueprint does. The
stage is fixed; the actors (NPCs, ecology, events, opportunities)
vary per playthrough via the Simulation Seed. Large-scale geography is
100% handcrafted. Like Whiterun, Wang Family Village does not randomly
spawn somewhere else.

**Immutability:** same as Layer 0. The blueprint never moves between
playthroughs.

### Layer 2 — Initial World Snapshot

The exact state of the world at the moment the player arrives. NPC
positions, economy, relationships, memories, inventories, weather,
rumors, opportunities, cultivation levels.

**Question answered:** "What is the exact state when the player arrives?"

**Implemented by:** the canonical t0 archive — the canon JSON DB
(`data/ergenverse/`) as the read-only starting state, plus the
`living_chapters/chapter_1_wang_family_village/` schema seed (desire
states, motivation states, relationship graph seeds, favor ledgers,
economy, conflict, affordances), materialized at game-start by
`dev.ergenverse.simulation.WorldStateDataLoader`,
`dev.ergenverse.ecology.EcosystemSeeder`,
`dev.ergenverse.simulation.actor.TerritorySeeder`, and
`dev.ergenverse.simulation.ReificationScan`.

**This is the "canon snapshot."** The game never starts at world
creation. It starts at a historical moment — exactly like loading a
save file from history. Year 0: Wang Lin is 15, his father is alive,
he has not joined Heng Yue, the Teng grudge has not ignited. History
is about to begin.

**Immutability:** the snapshot is never mutated, never regenerated,
never randomized. It is the canon branch-point.

### Layer 3 — Simulation Delta

Everything that changed after Tick 0. Every NPC death, every faction
destroyed, every item that changed hands, every karma consequence, every
player kill, every sect war.

**Question answered:** "What has changed because time passed and
because of player or NPC actions?"

**Implemented by:** `dev.ergenverse.simulation.WorldRuntimeState`
(extends Minecraft `SavedData`, persists to
`<world>/data/ergenverse_runtime_state.dat`). Schema v1:
`npcOverrides`, `factionOverrides`, `itemOwnershipOverrides`,
`karmaResolutionState`, `playerMutations`, `caveWorldOwnershipOverrides`,
`divergenceCounter`. The canon DB is never touched — reads consult
canon first, runtime overrides layer on top, writes go only here.

**This is exactly how source control works.** The save file is not the
whole world — it is the delta. Blueprint + Snapshot + Player Save
(where Player Save contains only changes). Old Chen: canonical = alive;
save = dead, reason = wolf attack. Wang Lin: canonical = Qi
Condensation 1; save = Qi Condensation 2, reason = player helped.

**This solves multiplayer and updates.** Six months from now, if Heng
Yue Sect is improved in the blueprint, existing saves keep their
historical changes where they conflict and adopt new authored content
where nothing has changed — because the simulation stores deltas, not
world replacements.

### Layer 3 also carries the narrative substrate

Two sub-systems live in Layer 3 and make the delta readable:

- **`dev.ergenverse.history.WorldHistory`** — the raw event log
  (timestamp + eventType + regionId + description + canonSource +
  topic + posX/posZ). Ring-buffered, persisted under the reserved key
  `_world_history` in `WorldRuntimeState.playerMutations`.
- **`dev.ergenverse.history.WorldChronicle`** (Art XLIII) — the prose
  narrative layer that compiles WorldHistory events into readable
  annals.
- **`dev.ergenverse.history.CanonDivergenceRecorder`** (Art XLIII) —
  the ledger comparing the 108 canon events (RITimelineEngine) against
  this timeline's actual history.

### Compliance

Every system that reads world state MUST consult Layer 0/1/2 first and
Layer 3 overrides second. Every system that writes world state MUST
write only to Layer 3. A system that mutates Layer 0, 1, or 2 is a bug.

---

## Article XLIII — Single-Player Maximalism

The Er Gen Verse is strictly single-player. This is not a limitation
to work around. It is one of the project's biggest advantages, and
the architecture leans into it fully.

### §1 — The simulation owns 100% of the CPU

There is no server to share with 300 players. The simulation asks only
"what would actually happen?" If the answer is 4,300 NPCs cultivating,
18,000 spirit beasts migrating, 2,000 merchants traveling, 700
alchemists refining pills, 60 sects holding meetings, 400 rumors
spreading — that is what the simulation does. No networking compromises.

### §2 — No respawn. Permanence is absolute.

If a wolf dies, it stays dead. If a spirit tree burns, it is gone. If
Wang Lin destroys a mountain, the mountain is destroyed. Forever.

**Implemented by:** `EntityCultivator.die()` writes `is_dead=true` to
`WorldRuntimeState.npcOverrides`. `ReificationScan.materializeNpc()`
checks `is_dead` and returns early — a dead NPC NEVER spawns again.
`EntityCultivator.removeWhenFarAway()` returns false — canon entities
never despawn due to distance. Death is a one-way door.

### §3 — Memory can become absurdly deep

An MMO cannot remember everything because of storage and scale. We can.

Day 18: you saved an old farmer. Day 742: his granddaughter recognizes
you — "You saved my grandfather." Not because of a scripted quest.
Because the family remembered.

**Implemented by:** the memory ledger
(`living_chapters/.../memory_ledger_*`), `WorldHistory` (ring-buffered
event log), and `NpcCognitiveMemory` (per-NPC memory with significance
weighting and decay resistance).

### §4 — NPCs never despawn mentally

Most games fake life: NPC disappears, simulation stops, player returns,
NPC respawns. Single-player does not fake it. Maybe the rendered entity
unloads, but the simulation keeps advancing — even if Wang Lin is 40 km
away.

**Implemented by:** `EntityCultivator.aiStep()` hibernates (skips
goalSelector/navigation) when no player is within `HIBERNATION_RANGE` —
a performance optimization, NOT a despawn. The cognition pipeline
(`ActorTickLoop`, `Planner`, `DecisionEngine`, `IntentEngine`) keeps
running for off-screen actors.

### §5 — Level-of-Simulation (LoS), not just Level-of-Detail

Standing next to Wang Lin: full cognition. Wang Lin crossing Zhao
Country 300 chunks away: abstract simulation into events and
probabilities, still respecting his goals, personality, cultivation,
and known obstacles. When you meet him again, there is a coherent
history explaining how he got there.

**Implemented by:** `dev.ergenverse.simulation.los.SimulationLevel` —
six tiers: STATIC_DATA, HISTORICAL, TERRITORY, ACTIVE_ACTOR,
FULL_COGNITION, STORY_IMPORTANCE. The world never stops; it just
changes how much detail it uses.

### §6 — Saves are historical records, not state snapshots

Not "Save 1", "Save 2". More like "Planet Suzaku Year 0, Year 5, Year
12, Year 18". You are saving history. Every save is its own timeline —
Timeline Alpha (Wang Lin joined Heng Yue), Timeline Beta (player warned
him, he waited two weeks), Timeline Gamma (player never met him,
canonical events happened). You are not creating worlds. You are
creating alternate histories.

### §7 — Canon is never overwritten. History records deviations.

The canon timeline (E01..E108) is immutable. The player's timeline is a
fork. When the simulation causes a canon event to occur as-written, it
is marked OCCURRED. When the player — or the simulation — changes the
outcome, it is marked DIVERGED and the deviation is recorded
permanently.

> Canonical: Wang Lin killed Teng Huayuan.
> This Timeline: Player distracted Teng Huayuan. Battle occurred three
> days later. 523 civilians survived.

**Implemented by:** `dev.ergenverse.history.CanonDivergenceRecorder`.
Five statuses: PENDING, OCCURRED, DIVERGED, PREVENTED, DEFERRED.
Persisted under `_canon_divergence` in `WorldRuntimeState`. Seeded with
all 108 canon events as PENDING on first access. Inspectable via
`/ergen divergence` and `/ergen divergence forks`.

### §8 — Simulate between sessions

When loading a save after weeks away, instead of pretending nothing
happened, the game can advance the simulation. NPCs finish journeys,
sect politics evolve, herbs mature, rumors spread, the world greets you
in its new state. Because it is single-player, that is your choice alone
— no server state to stay synchronized with.

**Implemented by:** `dev.ergenverse.npc.worldsim.NpcWorldSimulation`
— `onPlayerLogin` computes `offlineDelta = currentTick - lastPlayerOnlineTick`,
capped at 30 MC days, advances NPC cognitive systems. (Future work:
extend time-skip to cover the `WorldStateEngine` subsystem advances —
time events, migrations, ecosystems, civilizations, opportunities,
provenance, macro terrain — once those subsystems are fully implemented
past their current stubs.)

### §9 — The World Chronicle

Not a quest log. Not a wiki. A living chronicle written by the
simulation itself. It records every significant event that actually
occurred in your timeline: when a sect rose or fell, when a legendary
beast was born or slain, when a spirit vein was exhausted or
discovered, when Wang Lin broke through to a new realm, when you repaid
a life debt or created a blood feud, when a village was abandoned after
repeated beast attacks.

Centuries later in the same save, NPCs do not quote the novels — they
reference your world's history. Scholars debate it. Storytellers
exaggerate it. Children retell distorted versions of it. Because it is
single-player, every line in that chronicle belongs uniquely to your
universe.

**Implemented by:** `dev.ergenverse.history.WorldChronicle`. Prose
entries, tone-colored (TRIUMPHANT / TRAGIC / OMINOUS / MYSTERIOUS /
MUNDANE / PROPHETIC). Append-only — the past is never edited.
Persisted under `_world_chronicle` in `WorldRuntimeState`. Fed by
`dev.ergenverse.simulation.event.ChronicleSubscriber` (a catch-all
WorldEventBus subscriber that compiles every notable event into prose).
Inspectable via `/ergen chronicle`, `/ergen chronicle era <era>`,
`/ergen chronicle all`.

### §10 — The reframe

Stop thinking of this as "a Minecraft world." Think of it as "a
serialized simulation." Minecraft is simply the renderer. The
simulation is the game. The save file is the universe.

### Compliance

A system that despawns an NPC, respawns a dead beast, regenerates a
destroyed structure, or overwrites canon is a bug — unless it is
explicitly part of a Layer 3 delta that the player chose.


---

## Article XLIV — The Actor Is Primary; The Spawn Is Deprecated

The simulation has outgrown Minecraft's spawn model. This Article
codifies the inversion and retires the legacy spawn-registry concept.

### §1 — The Inversion

The prior architecture was implicitly Minecraft:

```
chunk loads → spawn NPC → NPC exists
```

This is backwards for this project. An NPC does not come into existence
because a chunk loaded. The NPC already existed. They were already
cultivating. Already thinking. Already planning. Already somewhere.
Minecraft is simply catching up to reality.

The correct flow is:

```
NPC exists (alive, simulating)
  → NPC currently happens to be here (presence, derived from their life)
    → renderer asks: which actors intersect loaded chunks?
      → those become entities (materialize)
        → when distant, dematerialize (keep simulating)
```

Wang Lin never "spawns." When you load a save, he does not pop into
existence. He was already mid-cultivation. The renderer materializes
him because his current presence intersects a loaded chunk — not
because a spawn event fired.

### §2 — The Spawn Registry Is Deprecated

There shall be no spawn registry. The concept of "register this NPC to
spawn at this location" is retired. The existing `NpcSpawnRegistry`,
`SettlementNpcAnchors`, and the spawn-driven `ReificationScan` are
DEPRECATED. They are retained only during the transition and will be
deleted in a future cycle.

The replacement is:
- **SettlementRegistry** — settlements own populations (§3).
- **Residence** — actors own buildings; buildings do not own actors (§4).
- **ActorPresence** — an actor's position is derived from their life,
  not from a fixed offset (§5).
- **ActorMaterializer** — the renderer asks which actors' presence
  intersects loaded chunks, and materializes those.

A system that treats an NPC as something to be "spawned" is a bug. An
NPC is a living actor who happens to be renderable when nearby.

### §3 — The Settlement Object

A Settlement is the simulation-owned object for an inhabited place. It
is NOT a Minecraft structure. Minecraft renders it (via builders); the
simulation owns it. The player arriving does not cause a settlement to
exist — it already existed. The player merely intersects it.

A Settlement owns:

```
Name, Canonical Location, Buildings, Residences, Road Graph,
Population, Visitors, Economy, Ecology, Spirit Veins,
History, Relationships, Events
```

The flow is:

```
Settlement → Population → Actors → Materialization → Minecraft
```

NOT:

```
Chunk → Spawn NPC
```

Wang Family Village is never "filled with NPCs." Instead the engine
asks: "Who currently lives here?" The answer depends on the time of
day, the actors' current activities, and the context. Morning: mother,
father, children, merchant, farmer, elder. Noon: merchant gone, hunter
left, children at the river, elder visiting the shrine. Night: most
indoors, one guard awake, merchant asleep. Nobody is spawned. They are
living.

### §4 — Residences: NPCs Own Buildings

Buildings do not own NPCs. NPCs own buildings. This is a subtle but
important inversion.

A Residence is:

```
Owner
Residents
Storage
History
Visitors
Security
Rooms
Current occupants
```

Wang Lin happens to live in the Wang Family Home. His mother lives
there. His father lives there. The home does not "contain" them; they
own it.

Residences can change. Wang Lin leaves forever. Years pass. His
parents die. The house is abandoned. Another family moves in. Or the
village burns. The house is destroyed. The house is rebuilt. Same
coordinate. Different history. The Residence object carries the full
ownership + lifecycle history (founding, transfers, abandonment,
destruction, rebuild) as an append-only Layer 3 record.

### §5 — Presence Is Derived From Life, Not Fixed

NPC positions shall not be fixed offsets. Instead, an actor has
weighted presence locations: Home, Meditation Spot, Favorite Tree,
Restriction Cave, Marketplace, Spirit Spring. Each location has a
per-time-of-day weight. Morning: 90% Home. Afternoon: Meditation Rock.
Night: Home. If wolves appear, everything changes — the context
collapses all actors onto home/flee positions.

The ActorPresence engine combines an actor's home (from their
Residence) with the settlement's shared locations, applies the
time-of-day weights, applies contextual modifiers, and produces the
actor's current position. The pick is deterministic per (actorId, day,
phase) so the actor does not teleport-flicker between scans.

A system that hardcodes an NPC's position as a fixed (dx, dz) offset
is a bug. An NPC's position is a derived fact about a living actor.

### §6 — Canon NPCs vs Simulation NPCs

The Constitution distinguishes two categories of NPC:

- **Canon NPCs** — Wang Lin, Li Muwan, Situ Nan, Wang Tianshui, Wang
  Zhou. Sourced from the novels. Immutable. Their existence, identity,
  and canon relationships are non-negotiable.
- **Simulation NPCs** — Farmer Chen, Disciple Zhao, Merchant Xu, Hunter
  Luo. Generated to fill the spaces between canon. They are allowed;
  otherwise the world will always feel tiny. The important rule: they
  must never contradict canon. They fill the spaces between canon.

Both live in the same Settlement population set. The distinction is
carried on the Actor provenance field ("canon:..." vs "simulation:...").
Canon NPCs are never deleted, never renamed, never re-roled. Simulation
NPCs may be added, may die, may move — as the simulation dictates.

### §7 — The Sect Is Not A Structure

A sect is not architected as a structure. It is architected as a sect.
A sect has: political hierarchy, cultivation districts, supply routes,
disciple housing, punishment halls, alchemy economy, lectures, patrol
schedules, elder residences, training grounds, formation maintenance,
recruitment.

If those systems exist, the buildings almost design themselves. A
Heng Yue Sect built only as a block layout — even with canon-correct
blocks — is a diorama, not a sect. The Settlement object for a sect
must carry the sect's organizational structure, not just its geometry.

### Compliance

A system that "spawns" an NPC (rather than materializing an already-
existing actor whose presence intersects a loaded chunk) is a bug —
unless it is the deprecated transition path explicitly marked as such.

A system that hardcodes an NPC's position as a fixed offset (rather
than deriving it from the actor's residence + time-of-day + context)
is a bug.

A system that treats a building as owning an NPC (rather than an NPC
owning a residence) is a bug.

A Simulation NPC that contradicts canon (claims to be Wang Lin, claims
to hold a canon artifact, claims canon relationships they do not have)
is a bug.

When in doubt: the actor already existed. Minecraft is catching up.
When in doubt: the settlement owns the people. The people own the
residences. Minecraft owns only the rendering.

## Article XLV — Systems Must Be Lived In, Not Built

A system with no dependent actor is dead weight. This Article
forbids the construction of systems that exist only because they
are "a feature." Every system must be load-bearing in at least one
NPC's life.

This Article is the negative-space complement to Article XLIV.
Article XLIV says the actor is primary and the spawn is deprecated.
This Article says the system is subordinate and exists only to
serve a life that was already in motion.

### §1 — The Rule

No system may exist unless at least one NPC relies on it to live.

- An economy exists because merchants eat.
- Weather exists because farmers react to it.
- Memory exists because Old Chen remembers his dog.
- Relationships exist because Wang Lin refuses someone he no longer
  trusts.
- Rumors exist because children repeat them.

A system that no NPC consults, reacts to, suffers from, or profits
by is not a system. It is decoration. Decoration is forbidden.

### §2 — The Dependent-Actor Test

Before any system is built, the builder must name, in writing:

1. **The dependent actor** — which NPC's life changes because this
   system exists.
2. **The reliance** — what that actor does differently because of it.
3. **The failure mode** — what the actor's life looks like if the
   system breaks or is removed.

If all three cannot be named, the system is not built. If a system
already exists and cannot answer all three, it is a candidate for
deletion in the next cycle.

### §3 — Schedules Are A Symptom, Not A System

A daily schedule ("06:00 field, 12:00 eat, 18:00 home") is not a
system. It is a timetable. A timetable tells you where an NPC is.
It does not tell you why.

The NPC does not own a schedule. The **world** owns pressures.
The **NPC** owns priorities. The NPC asks, of the world's
pressures: "Which of these matters to me?" Different NPCs give
different answers. That is much richer than everyone carrying a
timetable.

The pipeline is:

```
Mind
  ↓
  Reasoning
    ↓
    Decision
      ↓
      Commitment
        ↓
        Execution
```

The "goal" is not the interesting part. The interesting part is
the **commitment**. Once Wang Lin decides "I'm going to
investigate those wolves," that decision persists. He does not
rethink it every tick. That is closer to human behavior.

A `NeedDrivenGoal` (the proposed replacement for `NpcScheduleGoal`)
should therefore be understood as a **commitment engine**, not a
goal selector. The NPC commits to an action derived from world
pressures filtered through personal priorities. The commitment
persists until completed, interrupted by a higher-urgency
pressure, or abandoned. Per-tick re-evaluation of the same
pressure is a bug — it produces dithering, not behavior.

`NpcScheduleGoal` — which reads a `daily_schedule` array of
`{t0, t1, act, dir, dist}` time-windowed patrol entries and
whose own Javadoc says "at dawn they cultivate at Sword Peak, at
noon they eat at the Main Hall, at night they sleep" — is the
timetable anti-pattern. It is deprecated by this Article. It is
retained only until a commitment-driven goal replaces it.

This does not forbid routine. It forbids routine that is not the
visible downstream of a pressure. A farmer who always works the
field at dawn is correct — because the pressure (food scarcity)
cascades through crops through the field through the time of day
when the field is workable. A guard who always patrols at
midnight is correct — because the pressure (safety) cascades
through darkness through visibility through the patrol. A
cultivator who "meditates at Sword Peak from 06:00 to 12:00
because the schedule says so" is wrong, because no pressure
cascades into that slot.

### §4 — Extraordinary Events Override Routine

Because routine is the downstream of need, extraordinary events
naturally override it. If wolves attack:

- Wang Lin ignores breakfast.
- Da Niu grabs a shovel.
- Children run home.
- Old Chen searches for his dog.

Nobody follows a timetable because the world changed. A system in
which an NPC continues its schedule during a wolf attack is a bug.
The schedule is suspended the moment a higher-urgency need
displaces it. This is the same context-collapse described in
Article XLIV §5, restated as a requirement: the cascade must be
live, not cached.

### §5 — Interiors Are Evidence, Not Furniture

A residence's room contents are not generic ("table, chair, bed,
chest"). They are the visible biography of the occupant. The
correct question is not "what furniture goes here" but "what
would someone learn by entering this room." Do not build houses.
Build **evidence**.

- **Wang Lin's house**: a repaired farming tool hung on the wall,
  an unfinished restriction diagram on the floor, a single
  carefully hidden notebook, worn shoes by the door, worn
  cultivation notes on a lectern, a thin sleeping mat in the
corner.
- **Li Muwan's dwelling**: carefully labeled herbs, partially
  finished pills, drying racks, experimental failures she has
  not thrown away.
- **Situ Nan's quarters**: almost empty. The emptiness is the
  point.

A room that could belong to anyone belongs to no one. A Residence
(Article XLIV §4) whose room contents are not character-derived
fails this Article.

**The One-Room Standard.** Do not build five rooms. Build one.
Build Wang Lin's. Refuse to touch another room until it is
believable. Once one room feels like it belongs to a real person,
it becomes the standard for every future room. Five generic rooms
are worth less than one believable room.

### §6 — The Village Does Not Wait (Three Tests)

The central test of the simulation is not "does it run" but "does
it live without the player." There are three canonical tests:

**The AFK Test.** The player never moves. Does the world
continue? Does the village develop, do NPCs move with reason, do
things change without any input? If the answer is "NPCs walk
circles, nothing changes, nobody talks, nothing develops," the
simulation is not there yet.

**The Observer Test.** The player walks around but never
interacts. Does anything meaningful happen? Does the player
witness behavior that feels like it was happening anyway — not
triggered by proximity, not queued for an audience? If every
event only fires when the player approaches, the world is a
diorama, not a simulation.

**The Participant Test.** The player actively interferes. Does
the simulation react? If the player attacks, steals, helps, or
speaks, do the NPCs respond with reason — not with scripted
quest dialogue, but with the same pressure-priority-commitment
logic that governs their other behavior?

These three tests together cover almost everything the project
is trying to build. A cycle that passes only the Participant
Test but fails the AFK Test has built a game, not a world. A
cycle that passes only the AFK Test but fails the Participant
Test has built a screensaver, not a simulation. All three must
pass.

This is Article V ("Everything Exists Without The Player")
restated as three concrete, falsifiable tests.

### §7 — Slow Stories

Equal engineering weight must be given to quiet moments, not only
dramatic ones:

- Someone hangs clothes to dry.
- Someone repairs a fence.
- A child loses a toy.
- A merchant packs up at sunset.
- An elder falls asleep outside.
- A dog follows the butcher.

These are not quests. They are why the village feels inhabited. A
cycle that ships only dramatic moments (wolf attacks, recruiter
arrivals, tribulations) and no slow stories is an incomplete
cycle.

### §8 — The Per-Cycle Question

Every cycle must answer one question, and it is not "what did we
build." It is:

> What could a player witness today that they could not witness
> yesterday?

Examples of acceptable answers:

- Yesterday: nothing. Today: a child follows Wang Lin because they
  admire him.
- Yesterday: villagers teleported home. Today: they run because
  they heard wolves.
- Yesterday: Li Muwan existed. Today: she harvests herbs before
  sunrise.

A cycle that cannot answer this question has not advanced the
project, regardless of lines of code shipped.

### §9 — Canon Experience Status: Five Stages, Not Percentages

Progress on a Canon Experience is tracked across five stages,
not as a percentage:

```
Specified     The experience is named in canon data. We know
              what should happen.
Simulated     The underlying systems produce the behavior in
              data/logic. It runs, but is not yet visible.
Observable    A player can witness it in-game, without debug
              commands, within a single session.
Understandable  The player can understand WHY it happened —
              the cause is legible, the meaning is reachable.
              Players often witness something without
              understanding it. Meaning often arrives after
              observation.
Memorable     NPCs carry the event in memory and retell/act
              on it later. The experience persists across
              time.
```

A percentage cannot tell you where something is stuck. The five
stages can. Every Canon Experience in the living chapters must
carry its current stage. A moment at Observable that has been at
Observable for three cycles without advancing to Understandable
is stuck — and the stuck-ness is the signal, not a failure to be
papered over with a higher percentage.

The Understandable stage is inserted because players often
witness something without understanding it. Example: you see
Wang Lin watching wolves. Cool. But if you later learn he's
studying their hunting patterns because cultivation comes from
observing Heaven, that moment suddenly becomes memorable.
Meaning often arrives after observation. The simulation must
make the meaning reachable — through NPC dialogue, through
environmental evidence (Article XLV §5), through consequences
that play out over time — not through quest text.

### §10 — The First Ordinary Day

The next milestone is not a system. It is an experience:

> **The First Ordinary Day.**
>
> Wake up in Wang Family Village. Never open a quest log. Never
> receive a tutorial. Simply follow your curiosity.
>
> By sunset, you should have naturally discovered:
> - where people work,
> - who avoids whom,
> - who is respected,
> - who is struggling,
> - where Wang Lin spends his time,
> - why the village exists,
> - and at least one thing that changed without your involvement.
>
> Success: a player reaches the end of that day and says
> "It felt like I arrived in the middle of someone else's life."

This is the benchmark. Every system built between now and that day
must justify itself against this milestone. Every system that does
not serve The First Ordinary Day is, by this Article, decoration.

### Compliance

A system for which no dependent actor can be named is a bug.

A daily schedule authored as a timetable (time-windowed patrol
entries not downstream of a named pressure) is a bug.
`NpcScheduleGoal` in its current form is the deprecated transition
path; it must be replaced by a commitment-driven goal.

A residence whose room contents are generic furniture (not
character-derived evidence) is a bug. Building five generic rooms
is a bug — build one believable room first.

A cycle that cannot answer "what could a player experience today
that they could not yesterday" has not advanced the project. Both
questions must be answered in every worklog: today's experience
AND yesterday's experience. If the answers are identical, the
cycle did not advance the game, regardless of lines of code.

A Canon Experience tracked as a percentage (rather than the five
stages) is a bug.

When in doubt: the village was already alive. The schedule was
already cascading from pressure. Minecraft is catching up to a
life that was already in motion. When in doubt: if you remove the
system and no NPC's life changes, the system was decoration.

## Architectural Completeness Declaration

The Constitution is architecturally complete at Article XLV.
Future changes shall be amendments, clarifications, or removals —
not new articles. The project does not suffer from a lack of
articles. It suffers when Articles XL–XLV are not yet lived. The
remaining challenge is not conceptual. It is craftsmanship:
launching the game, standing in Wang Family Village, finding what
feels artificial, and refining it through iteration, not through
further legislation.

