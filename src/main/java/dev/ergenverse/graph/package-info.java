/**
 * <h1>WorldGraph — the Living Knowledge Graph</h1>
 *
 * <h2>Architecture: Hybrid (Option C)</h2>
 * <p>This package implements <b>Layer 2/3</b> of the Ergenverse three-layer
 * architecture. It is a <b>mutable property graph</b> that sits alongside
 * (not replacing) the Layer 1 canon registry in
 * {@code dev.ergenverse.wanglin.registry}.
 *
 * <h3>Layer 1 (Canon, immutable) — WangLinMasterRegistry</h3>
 * <p>482+ CanonicalEntry records. Loaded once at boot. Never modified by
 * simulation. Provides canon metadata (provenance, ownership, transferability).
 *
 * <h3>Layer 2 (Simulation, mutable) — WorldGraph</h3>
 * <p>This package. A directed, typed property graph where nodes represent
 * every entity in the simulated world and edges represent every relationship.
 * Nodes that have a Layer 1 canon entry reference it via {@code registryId};
 * simulation-generated nodes (random NPCs, spawned beasts, player-crafted
 * items) have no registry entry and stand alone.
 *
 * <h3>Layer 3 (History, append-only) — edges with timestamps</h3>
 * <p>Every change to Layer 2 writes a history edge. The full provenance of
 * any object is a path query on the graph.
 *
 * <h2>Why a separate graph (not an upgrade to the registry)?</h2>
 * <p>The registry is a <b>tagged catalog</b> — it classifies canon facts by
 * category and filters by tag. A graph is a <b>relationship engine</b> — it
 * connects entities by typed edges and supports traversal, path-finding, and
 * subgraph extraction. These are different access patterns with different
 * performance characteristics and different mutability requirements. Forcing
 * graph semantics into a catalog creates a confused data model. Keeping them
 * separate lets each do what it does best.
 *
 * <h2>The Integration Point</h2>
 * <p>Every engine (Cultivation, Artifact, Perception, Alchemy, Beast, Economy,
 * etc.) reads from and writes to this graph. The graph is the <b>single
 * integration point</b> — engines don't call each other directly. This means:
 * <ul>
 *   <li>Adding a new engine = adding new node/edge types + components</li>
 *   <li>Querying "what artifacts use soul power?" = graph query, not engine call</li>
 *   <li>Per-object provenance = path query on the node's history edges</li>
 *   <li>NPC AI state = node component, not a separate AI system</li>
 * </ul>
 *
 * <h2>Prime Directive</h2>
 * <p>The graph records what <b>is</b>. It does not fabricate. Every edge
 * carries a Provenance. Simulation-generated edges are marked SIMULATION,
 * not CANON. The graph is honest about the difference between "Er Gen wrote
 * this" and "the simulation created this."
 *
 * @see WorldGraph    the graph implementation
 * @see Node          a node in the graph
 * @see Edge          a typed, directed edge
 * @see EdgeType      all edge types (OWNS, TEACHES, LOCATED_IN, ...)
 * @see NodeType      all node types (ARTIFACT, NPC, LOCATION, ...)
 * @see Component     the component interface for node data
 */
package dev.ergenverse.graph;