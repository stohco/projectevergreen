package dev.ergenverse.entity.ai;

import dev.ergenverse.simulation.actor.BeastIntelligence;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;

/**
 * BeastIntelligenceGoalFactory — the bridge from the 7-tier BeastIntelligence
 * simulation enum to actual Minecraft entity AI goals.
 *
 * <p>Prior to this class, BeastIntelligence was defined but NEVER consulted by
 * any entity's registerGoals(). A SPIRIT-tier beast and an INSTINCT-tier beast
 * had identical AI. This factory applies tier-appropriate goals cumulatively:
 *
 * <ul>
 *   <li><b>INSTINCT(0)</b> — basic survival: flee when hurt, wander, eat. No combat.</li>
 *   <li><b>AWARE(1)</b> — + light patrol, alertness to nearby threats.</li>
 *   <li><b>CUNNING(2)</b> — + ambush prey (hide + leap), flee from stronger, counter weak.</li>
 *   <li><b>SPIRIT(3)</b> — + territory defense (attack intruders), rest to recover.</li>
 *   <li><b>DEMON(4)</b> — + cultivation rest, counterattack with elemental burst.</li>
 *   <li><b>ANCIENT(5)</b> — + domain patrol (large radius), intimidate (roar).</li>
 *   <li><b>OLD_MONSTER(6)</b> — + strategic retreat when low, devastating AoE counter.</li>
 * </ul>
 *
 * <p>Each tier ADDS to the previous (cumulative). Goals are added at priorities
 * 2-5 (0-1 reserved for float/panic by the entity itself).
 *
 * <p>Constitution: the 7-tier system is the single-player-maximalism answer to
 * "every beast should behave according to its spiritual nature, not a generic AI."
 */
public final class BeastIntelligenceGoalFactory {

    private BeastIntelligenceGoalFactory() {}

    /**
     * Apply tier-appropriate goals to a beast's goalSelector + targetSelector.
     * Call from the entity's registerGoals() AFTER the entity has added its own
     * type-specific base goals (float, panic, melee, etc.).
     *
     * @param mob       the beast entity
     * @param tier      the beast's intelligence tier
     * @param goals     the entity's goalSelector
     * @param targets   the entity's targetSelector
     */
    public static void applyBeastGoals(PathfinderMob mob, BeastIntelligence tier,
                                        GoalSelector goals, GoalSelector targets) {
        // ── INSTINCT(0): basic survival only — no added goals (entity handles flee/wander) ──
        if (tier.ordinal() < BeastIntelligence.AWARE.ordinal()) return;

        // ── AWARE(1): + hurt retaliation + alertness ──
        targets.addGoal(2, new HurtByTargetGoal(mob));

        if (tier.ordinal() < BeastIntelligence.CUNNING.ordinal()) return;

        // ── CUNNING(2): + ambush prey (weaker mobs) + smart flee ──
        goals.addGoal(3, new SpiritBeastAmbushGoal(mob, 1.4D));
        goals.addGoal(4, new BeastSmartFleeGoal(mob, tier, 1.3D));

        if (tier.ordinal() < BeastIntelligence.SPIRIT.ordinal()) return;

        // ── SPIRIT(3): + territory patrol + rest recovery + target players who trespass ──
        goals.addGoal(3, new SpiritBeastTerritoryPatrolGoal(mob, tier, 1.0D));
        goals.addGoal(5, new BeastRestRecoverGoal(mob, tier));
        targets.addGoal(3, new NearestAttackableTargetGoal<>(mob, Player.class, true,
                e -> e.distanceToSqr(mob) < territoryRadiusSq(tier)));

        if (tier.ordinal() < BeastIntelligence.DEMON.ordinal()) return;

        // ── DEMON(4): + stronger rest (qi-gathering particles) — patrol radius grows ──
        // (TerritoryPatrolGoal already scales radius with tier; no new goal class needed.)

        if (tier.ordinal() < BeastIntelligence.ANCIENT.ordinal()) return;

        // ── ANCIENT(5): + intimidation (handled inside TerritoryPatrolGoal's roar) ──

        if (tier.ordinal() < BeastIntelligence.OLD_MONSTER.ordinal()) return;

        // ── OLD_MONSTER(6): + devastating counter when cornered (in BeastSmartFleeGoal) ──
    }

    /** Territory radius scales with tier. SPIRIT=16, DEMON=24, ANCIENT=40, OLD_MONSTER=64. */
    public static double territoryRadius(BeastIntelligence tier) {
        return switch (tier) {
            case SPIRIT -> 16.0D;
            case DEMON -> 24.0D;
            case ANCIENT -> 40.0D;
            case OLD_MONSTER -> 64.0D;
            default -> 12.0D;
        };
    }

    public static double territoryRadiusSq(BeastIntelligence tier) {
        double r = territoryRadius(tier);
        return r * r;
    }

    /**
     * Map a 0-6 cultivation tier int (stored on SpiritBeastEntity) to a BeastIntelligence.
     * Tier 0-1 = INSTINCT, 2 = AWARE, 3 = CUNNING, 4 = SPIRIT, 5 = DEMON, 6+ = ANCIENT/OLD_MONSTER.
     */
    public static BeastIntelligence tierFromInt(int cultivationTier) {
        if (cultivationTier <= 1) return BeastIntelligence.INSTINCT;
        if (cultivationTier == 2) return BeastIntelligence.AWARE;
        if (cultivationTier == 3) return BeastIntelligence.CUNNING;
        if (cultivationTier == 4) return BeastIntelligence.SPIRIT;
        if (cultivationTier == 5) return BeastIntelligence.DEMON;
        if (cultivationTier == 6) return BeastIntelligence.ANCIENT;
        return BeastIntelligence.OLD_MONSTER;
    }
}
