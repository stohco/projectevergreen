package dev.ergenverse.core;

/**
 * The Foundational Philosophy of Ergenverse.
 *
 * <p>This class exists only to encode the design philosophy in a place
 * where every developer (human or AI) will read it before touching the
 * simulation. Every system in this mod must obey these principles. If
 * a feature contradicts them, the feature is wrong — not the principles.
 *
 * <hr>
 *
 * <h2>The Prime Directive</h2>
 *
 * <blockquote>
 *   Never hide or reveal objects because of the player's level. Hide or
 *   reveal interactions according to the laws of the world. The world is
 *   objective and exists independently of the player. Cultivation increases
 *   a character's ability to perceive, understand, and interact with deeper
 *   layers of reality — it does not create or replace reality. Every spirit
 *   beast, formation, herb, ruin, and Dao phenomenon must exist where it
 *   is because of ecological, historical, cosmological, and spiritual
 *   causes rooted in the canon.
 * </blockquote>
 *
 * <h2>1. Reality is Objective</h2>
 *
 * <p>The world exists independently of the player. If there is a Fifth
 * Rank spirit rabbit living in a spirit valley, then:
 *
 * <ul>
 *   <li>A mortal sees a Fifth Rank spirit rabbit.</li>
 *   <li>A Qi Condensation cultivator sees a Fifth Rank spirit rabbit.</li>
 *   <li>Wang Lin sees a Fifth Rank spirit rabbit.</li>
 * </ul>
 *
 * <p>The difference is not what they see. It is what they <b>understand</b>.
 *
 * <ul>
 *   <li>A mortal thinks: "That's the biggest rabbit I've ever seen..."</li>
 *   <li>A cultivator immediately recognizes: "Spirit Beast. Leave. Now."</li>
 *   <li>A beast tamer notices: "Its bloodline is unusually pure."</li>
 *   <li>A Fourth Step cultivator notices: "Its ancestor lineage traces back
 *       to an extinct divine hare."</li>
 * </ul>
 *
 * <p>The rabbit itself never changes.
 *
 * <h2>2. Hidden Things Are Hidden Because of World Laws, Not Rendering</h2>
 *
 * <p>A formation is not invisible because the game doesn't draw it. It is
 * invisible because:
 *
 * <ul>
 *   <li>it exists partially in another layer of reality (the Spiritual
 *       Layer, not the Physical Layer);</li>
 *   <li>it is concealed by Dao;</li>
 *   <li>it suppresses perception;</li>
 *   <li>it requires sufficient divine sense to resolve;</li>
 *   <li>it intentionally rejects weaker observers.</li>
 * </ul>
 *
 * <p>A mortal mining a mountain wall with an iron pickaxe does not break
 * the formation inside — because the formation is not actually embedded
 * in the physical rock. The visible mountain is only the physical shell.
 * The formation occupies spiritual space. Without sufficient cultivation:
 *
 * <ul>
 *   <li>you can't perceive it;</li>
 *   <li>you can't touch it;</li>
 *   <li>you can't damage it.</li>
 * </ul>
 *
 * <p>Even if the mountain physically collapses, the formation may remain
 * intact or simply shift according to its own laws.
 *
 * <h2>3. Three Layers of Reality</h2>
 *
 * <p>Every chunk contains three overlapping layers:
 *
 * <ul>
 *   <li><b>Physical Layer</b> — trees, stone, water, animals, villages.
 *       Everyone occupies and perceives this layer.</li>
 *   <li><b>Spiritual Layer</b> — spirit veins, Qi currents, formations,
 *       spirit herbs, beast territories. Exists independently. Requires
 *       cultivation to perceive and interact.</li>
 *   <li><b>Dao Layer</b> — karmic traces, historical imprints, residual
 *       will, ancient Dao, heavenly laws. The deepest layer. Requires
 *       high cultivation.</li>
 * </ul>
 *
 * <p>Everyone occupies the physical layer. Not everyone can interact with
 * the higher layers. This solves the "TNT can't break the formation"
 * problem naturally.
 *
 * <h2>4. Perception Unlocks Understanding, Not Existence</h2>
 *
 * <p>Not: "I reached Foundation Establishment, so now the cave appeared."
 *
 * <p>Instead: the cave always existed. But before, you noticed "cold
 * wind." Later, you notice "Qi flowing into the cliff." Later, "that's a
 * concealed entrance." Later, "there's an ancient formation hiding the
 * door." Later, "this cave belongs to a Third Step cultivator."
 *
 * <p>Reality never changed. Your interpretation became more complete.
 *
 * <h2>5. Divine Sense Is a Transformative Mechanic</h2>
 *
 * <p>In Er Gen, Divine Sense is almost another sense entirely. It's not
 * just "radar." It becomes:
 *
 * <ul>
 *   <li>reading formations;</li>
 *   <li>identifying herbs;</li>
 *   <li>locating beasts;</li>
 *   <li>sensing hidden cultivators;</li>
 *   <li>detecting killing intent;</li>
 *   <li>finding spatial cracks.</li>
 * </ul>
 *
 * <p>At mortal level, you see trees on a mountain. At Nascent Soul, you
 * sweep your Divine Sense across hundreds of kilometers and immediately
 * know: there are three Nascent Soul cultivators nearby; an ancient beast
 * is sleeping beneath the lake; someone recently fought here; there's a
 * concealed cave; the spirit vein is weakening. That feels transformative
 * without changing the world.
 *
 * <h2>6. The World Obeys Ecological Logic</h2>
 *
 * <p>Every spirit beast exists because of an ecosystem:
 *
 * <pre>
 *   Spirit Vein
 *     ↓
 *   High-Qi Plants
 *     ↓
 *   Spirit Herbivores
 *     ↓
 *   Spirit Predators
 *     ↓
 *   Ancient Apex Beast
 *     ↓
 *   Sect patrols
 *     ↓
 *   Cultivator hunting parties
 *     ↓
 *   Merchant caravans
 * </pre>
 *
 * <p>If the player exterminates all the herbivores, eventually predators
 * starve, herbs spread differently, sects notice, alchemists lose
 * ingredients. The ecosystem evolves.
 *
 * <h2>7. World Laws Make Locations Feel Authored</h2>
 *
 * <p>Every location knows why it exists. Example:
 *
 * <pre>
 *   Location: Ancient Sea
 *   Reason: Created when an ancient battle split the continent.
 *   World Laws:
 *     Water Dao dominant.
 *     Space unstable.
 *     Lightning suppressed.
 *   Consequences:
 *     Specific herbs grow.
 *     Specific beasts evolve.
 *     Certain techniques are stronger.
 *     Certain formations fail.
 * </pre>
 *
 * <p>Now the world starts feeling authored rather than procedurally
 * generated.
 *
 * <h2>What This Means for Implementation</h2>
 *
 * <ol>
 *   <li>Never write code that "spawns a thing because the player reached
 *       level X." Things exist because of ecology, history, cosmology,
 *       and spiritual causes — all rooted in canon.</li>
 *   <li>Never write code that "renders a different model at tier X."
 *       Instead, the entity is objective. The observer's understanding
 *       (delivered via tooltips, divine-sense overlays, or NPC dialogue)
 *       changes — the entity does not.</li>
 *   <li>Concealment is a property of the thing being concealed (its
 *       formation, its Dao), not a property of the observer. The observer
 *       needs sufficient perception to penetrate the concealment — the
 *       concealment doesn't disappear when the observer arrives.</li>
 *   <li>Mortals can't damage spiritual things. A mortal pickaxe hits
 *       physical blocks. A Foundation-tier cultivator's pickaxe can hit
 *       spiritual things because their cultivation extends into that
 *       layer. The pickaxe is the same; the cultivator is different.</li>
 *   <li>Ecology is causal. If you remove a species, the food web
 *       responds. The simulation must track populations and let them
 *       collapse naturally.</li>
 *   <li>World laws are derived from a location's origin, not rolled
 *       randomly. The Ancient Sea exists because of an ancient battle;
 *       therefore Water Dao is dominant and Lightning is suppressed.
 *       Those laws then determine what herbs, beasts, techniques, and
 *       formations make sense there.</li>
 * </ol>
 *
 * <p>Wang Lin didn't discover amazing places because the universe spawned
 * them for him; he discovered them because he became capable of surviving,
 * recognizing, or interacting with places that had always existed. This
 * mod must preserve that distinction.
 */
public final class WorldPhilosophy {
    private WorldPhilosophy() {}

    public static final String PRIME_DIRECTIVE =
        "Never hide or reveal objects because of the player's level. Hide or reveal interactions " +
        "according to the laws of the world. The world is objective and exists independently of " +
        "the player. Cultivation increases a character's ability to perceive, understand, and " +
        "interact with deeper layers of reality — it does not create or replace reality. Every " +
        "spirit beast, formation, herb, ruin, and Dao phenomenon must exist where it is because " +
        "of ecological, historical, cosmological, and spiritual causes rooted in the canon.";
}
