/**
 * <h1>The Four-Layer Artifact Usage Model</h1>
 *
 * <p><b>Never make cultivation treasures binary.</b> A cultivator does not either
 * "have access" or "not have access" to a treasure. Every treasure exists on a
 * spectrum:
 * <pre>
 *   physical possession  &rarr;  passive influence  &rarr;  partial activation
 *        &rarr;  full authority  &rarr;  Dao-level manifestation
 * </pre>
 * Requirements restrict the <i>expression</i> of a treasure's power, not the
 * treasure's existence. This is how <i>Renegade Immortal</i> treats treasures:
 * Wang Lin repeatedly relies on treasures above his realm, and they do not
 * automatically reject or destroy him. What changes is how much of the treasure's
 * power he can actually express.
 *
 * <h2>The Five Layers</h2>
 * <h3>Layer 1 &mdash; Physical Use ("Can I swing it?")</h3>
 * <p>Almost always yes. A mortal who finds an ancient divine sword can hold it,
 * swing it, stab someone with it. To them it is just a (possibly very heavy)
 * sword. The treasure's deeper abilities are dormant. This layer is
 * <b>always available</b> to anyone who physically holds the item.
 *
 * <h3>Layer 2 &mdash; Passive Properties</h3>
 * <p>Some effects exist without activation. A high-level sword is made of sharper
 * material, is tougher than normal metal, emits spiritual pressure, self-repairs,
 * suppresses nearby auras. A cultivator below the treasure's level may benefit
 * slightly from these passive properties simply by holding it.
 *
 * <h3>Layer 3 &mdash; Spiritual Activation</h3>
 * <p>Now the treasure actually functions. Requires some combination of: qi,
 * spiritual sense (divine sense), blood refinement, cultivation realm, and
 * compatibility with the treasure. A flying sword at Foundation: inject qi,
 * minor sword aura. At Nascent Soul: fly it, divine-sense control, activate
 * formations. This layer is <b>gated by the wielder's capacity</b>, not a
 * binary realm check.
 *
 * <h3>Layer 4 &mdash; Full Authority</h3>
 * <p>The owner reaches the treasure's intended level. The treasure's full
 * documented abilities are available because the wielder and treasure operate
 * at the same conceptual level.
 *
 * <h3>Layer 5 &mdash; Dao-Level Manifestation</h3>
 * <p>The owner and the treasure share the same Dao. They understand its laws,
 * supply its energy natively, and unleash its true power. User and treasure
 * are one. For treasures with a bound spirit, this is where spirit and master
 * are fully unified.
 *
 * <h2>Contextual Backlash (NOT Binary Damage)</h2>
 * <p>A weaker cultivator holding a powerful treasure is <i>not</i> automatically
 * destroyed. Backlash occurs only when the wielder <b>forces the treasure beyond
 * their capability</b>:
 * <ul>
 *   <li>Swinging a heavy divine sword &mdash; fine (just tiring).</li>
 *   <li>Forcing open a sealed Third Step restriction &mdash; meridian damage,
 *       soul injury, backlash.</li>
 *   <li>Forcing a treasure spirit to obey &mdash; rejection, retaliation,
 *       loss of control.</li>
 *   <li>Overdrawing energy the treasure demands but the wielder cannot supply
 *       &mdash; exhaustion, qi depletion, possible cultivation regression.</li>
 * </ul>
 *
 * <h2>Layer Placement (Three-Layer Architecture)</h2>
 * <p>This package is <b>Layer 2 &mdash; Simulation</b>. It defines <i>how a
 * cultivator interacts with a treasure</i>, which is a universe rule, not a
 * canon fact. The canon facts (what a treasure IS, its documented states) live
 * in {@link dev.ergenverse.canon} (Layer 1). Per-artifact usage profiles carry
 * {@link dev.ergenverse.canon.Provenance} citations for canon-grounded thresholds,
 * which is a valid cross-layer reference (Simulation may read Canon; Canon must
 * never import Simulation).
 *
 * <h2>The Golden Rule (user directive)</h2>
 * <blockquote>
 *   Never make cultivation treasures binary. A cultivator does not either "have
 *   access" or "not have access." Every treasure exists on a spectrum: physical
 *   possession &rarr; passive influence &rarr; partial activation &rarr; full
 *   authority &rarr; Dao-level manifestation. Requirements should restrict
 *   expression of power, not erase the item's existence.
 * </blockquote>
 *
 * @see dev.ergenverse.simulation.artifact.ArtifactUsageEngine
 * @see dev.ergenverse.simulation.artifact.ArtifactUsageProfile
 */
package dev.ergenverse.simulation.artifact;