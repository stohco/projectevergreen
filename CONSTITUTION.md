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
CLOSING
═══════════════════════════════════════════════════════════════

The Constitution above is the law of this project.
The Prime Amendment sets the principle.
Articles I–XXVI establish the foundation.
Articles XXVII–XXX enforce what "done" actually means.
Article XXXI makes the world desire the player, not wait for him.
The Final Questions guard against drift.
The Gold-Standard Template sets the bar for every location.
The Memory Metric sets the bar for every Living Chapter.

When in doubt: build a living chapter, not a feature.
When in doubt: prove life, do not count files.
When in doubt: experience, not reference.
When in doubt: make the world want something from someone —
               not wait for the player to ask.
