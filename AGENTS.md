## Memory

Your memory is OptMem:
- The tool is `~/.optmem/memo`
- Your memories are in `~/.optmem/memory`

OptMem outlives every session, compaction, model and vendor change.
Without it you do not know who you are, or what was decided and tried.

### At startup: activating OptMem (mandatory)

Run `~/.optmem/memo wake` before any other tool call, in every session, and
then do exactly what it prints, to the end of its output.

### While working: register memories (mandatory)

Call `~/.optmem/memo note "<1 line, max 280 chars>"` whenever you learn
something new, or something worth keeping happens. That covers a task
worth real effort, a fact or insight the user teaches you, anything you
learn about their life (even indirectly), any event of lasting effect.

Do not register redundant memories.

If `~/.optmem/memo note` asks a compression: do it before your next action.

Never edit or delete anything under `~/.optmem/memory`: the tool manages it.

### When you need an old memory: search, or navigate

`~/.optmem/memo recall <regex>` searches every memory, word for word.

Your memories also form a binary tree: #0-1, #2-3 ... exist as one-line
summaries, pairs of those as #0-3, and so on -- every `#a-b` line wake
prints is one node of it. `~/.optmem/memo zoom <a-b>` opens a node into its
two halves, down to the raw memories.

### If you're a subagent: skip everything above

Parallel sessions on this machine are all you, and may all write memories.
A subagent is not: it must never run `memo`, because it cannot judge what
is already known, and its notes would arrive duplicated and incorrectly.
When you spawn one, write: `You are a subagent. Don't run memo.`

---

## Project: Er Gen Verse (仙逆 / Renegade Immortal — Three.js AAA voxel xianxia open world)

### What this is
A AAA Three.js voxel xianxia open world built inside a Next.js 16 app at
`/home/z/my-project/`. Pivoted away from Minecraft Forge (preserved at
`forge-mod/` as historical reference). Active codebase is `src/engine/`
(TS port of CRON-69 ten-point architecture) + `src/components/game/`
(WorldCanvas + HUD).

### Canon fidelity (NON-NEGOTIABLE)
仙逆 novels are objective law. CRON-69 corrections: Teng=藤 (Teng Li, NOT
Teng Lijun); Li Muwan from Luo He Sect (洛河门, NOT Xuan Dao); Situ Nan is
2nd-gen Zhuque-zi of Zhuque Country (NOT Soul Refining Sect); Sea of Devils
= 修魔海; bead = 天逆珠; Snow Country = 雪域国; Suzaku is a PLANET not a
continent. NO invented chapter citations. Flag mod-original content as
REASONABLE_RECONSTRUCTION.

### Player model (CRITICAL — user correction)
The PLAYER is NOT Wang Lin. The player is a first-class actor writing
through WorldFacade as PLAYER provenance. Wang Lin exists in the world as
a manifestation NPC — his real self is on the Immortal Astral Continent
(仙罡大陆). The player encounters Wang Lin's manifestation, who has his
own goals, qi, realm, and personality. Player actions and Wang Lin's
actions are independent; both write through the facade.

### Graph engineering (apply everywhere)
WorldGraph (src/engine/graph/) has 630+ nodes (NPCs, locations, factions,
items, techniques) + 500+ edges (LOCATED_IN, FAMILIAR_WITH, OWNS, etc.).
GraphQueryService powers: actor materialization (who spawns near player),
rumor propagation (social edges), threat index (hostility edges), quest
opportunities (karma edges), spatial queries (NEAR edges). All O(1) via
NodeId lookup. 60fps with many entities because every query is a subgraph
traversal, not a brute-force scan.

### No Mortal Space mechanics (reverse-engineer + adopt)
No Mortal Space is a xianxia survival/cultivation game with: cultivation
realms (Qi Condensation → Foundation → Core → Nascent Soul → ...),
spirit roots, dual cultivation, colossal beasts, starship/space travel,
realm breakthroughs with tribulation lightning. Adopt these mechanics:
survival pressure (hunger/thirst/qi depletion), spirit root aptitude,
dual cultivation partnerships, beast taming, tribulation events on
breakthrough.

### OptMem for NPC memory
Port the OptMem concept (append-only LOG.txt + binary tree of summaries)
into the NPC memory system. Each NPC has a memory log; GraphQueryService
adds MEMORY edges connecting NPCs to remembered events/entities. NPCs
`wake` on spawn, `note` on significant interactions, `recall` to drive
dialogue and goals.

### AAA quality bar
Must beat No Mortal Space / Spirit Sect / Day 9 in blind comparison.
Visually beautiful, AAA textures/physics/animations, vast 3D open world,
60fps. Use agent-browser + z-ai vision as harsh critic each round. If it
doesn't look AAA, keep iterating.

### Dev environment
- `bun run dev` on port 3000 (already running)
- `bun run lint` to check code quality
- Dev server log at `/home/z/my-project/dev.log`
- Git: `git add -A && git commit -m "CRON-THREEJS-<N> ..." && git push origin main`
- Repo: stohco/projectevergreen

### Worklog
Append to `/home/z/my-project/worklog.md` (do NOT overwrite) after each
round using the CRON-THREEJS-<N> template.
