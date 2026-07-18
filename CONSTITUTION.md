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
