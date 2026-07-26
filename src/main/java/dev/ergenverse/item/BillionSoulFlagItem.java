package dev.ergenverse.item;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.List;

/**
 * BillionSoulFlagItem — Wang Lin's signature soul weapon (十亿魂幡 / 亿魂幡).
 *
 * <p><b>Canon (仙逆 / Renegade Immortal):</b> The Billion Soul Flag is the Soul
 * Refining Sect's guardian treasure, gifted to Wang Lin by Dun Tian (a Soul
 * Refining Sect predecessor). At its peak it contains 37 main souls (each with
 * its own consciousness — the flag is sentient) plus 1 billion ordinary souls.
 * It is a pseudo-immortal / inheritance-tier treasure. Wang Lin uses it to
 * release soul storms that devastate enemies, and the flag grows stronger as
 * more souls are refined into it. The flag was damaged when most souls
 * self-destructed vs Tuo Sen, and was later repaired via the Gate of Emptiness.
 *
 * <p><b>Source:</b> wiki-attested (mid-Nascent Soul era); Fandom wiki. Artifact
 * ID I51. Provenance data in
 * {@code data/ergenverse/provenance/billion_soul_flag___ten_billion_soul_banner.json}.
 *
 * <p><b>Mechanics (distinguished from SoulGourdItem):</b>
 * <ul>
 *   <li><b>Passive absorption</b> — when the player kills any living entity while
 *       holding the flag (either hand), the soul is absorbed. Bosses/tougher
 *       entities yield "main souls" (worth 10x power). No manual corpse-clicking
 *       required (unlike SoulGourdItem).</li>
 *   <li><b>Permanent refinement</b> — souls are REFINED into the flag. They are
 *       NOT consumed on release. The flag grows permanently stronger with each
 *       kill. (SoulGourdItem consumes souls on release — it's a temporary tool.)</li>
 *   <li><b>Soul storm (right-click)</b> — releases a wave of soul energy in a
 *       16-block radius. Damage scales with {@code sqrt(totalPower)}. Applies
 *       Wither II to all hit. Does NOT consume souls — they're permanent power.
 *       Costs 10 durability per release.</li>
 *   <li><b>Soul sense (shift+right-click)</b> — pulses spiritual sense outward,
 *       applying Glowing to all entities within 32 blocks for 5 seconds. Costs
 *       1 durability. Canon: Wang Lin's spiritual sense is enhanced by the flag.</li>
 *   <li><b>Tier progression</b> — the flag's name and tooltip color change as
 *       souls accumulate (Empty → Soul → Hundred → Thousand → Myriad → Billion).
 *       The iconic "Billion Soul Flag (十亿魂幡)" name is earned at max capacity.</li>
 *   <li><b>Durability</b> — 2000 uses. Repairable with beast_core (each restores
 *       50 durability) via anvil. Canon: the flag was repaired after damage.</li>
 *   <li><b>Capacity</b> — 100,000 ordinary souls (gameplay cap; canon holds 1
 *       billion). Plus up to 37 main souls (canon-accurate cap).</li>
 * </ul>
 *
 * <p><b>Self-critique:</b> The 100K cap (vs canon's 1 billion) is a gameplay
 * concession — storing 1 billion NBT entries would crash the JVM. The tier
 * names preserve the canon progression. The "37 main souls" cap is canon-exact.
 * The passive absorption chance (50% base) is a balance choice — canon implies
 * the flag absorbs ALL souls, but 100% absorption would trivialize combat.
 * SoulGourdItem and BillionSoulFlagItem now coexist with distinct mechanics
 * (gourd = temporary burst, flag = permanent growth) — this is canon-faithful
 * since Wang Lin uses both for different purposes.
 *
 * <p>CRON-COMPLETIONIST-89: New functional item. Previously billion_soul_flag
 * was a display-only WangLinItem with no mechanics — Wang Lin's most iconic
 * soul weapon was unlaunchable.
 */
public class BillionSoulFlagItem extends Item {

    // ── Constants ────────────────────────────────────────────────────
    public static final int MAX_ORDINARY_SOULS = 100_000;
    public static final int MAX_MAIN_SOULS = 37;
    public static final int STORM_RADIUS = 16;
    public static final int SENSE_RADIUS = 32;
    public static final int SENSE_DURATION = 100; // 5 seconds
    public static final int STORM_COOLDOWN = 60; // 3 seconds
    public static final int SENSE_COOLDOWN = 100; // 5 seconds
    public static final int STORM_DURABILITY_COST = 10;
    public static final int SENSE_DURABILITY_COST = 1;
    public static final double BASE_ABSORPTION_CHANCE = 0.50;

    // NBT keys
    private static final String NBT_ORDINARY_SOULS = "OrdinarySouls";
    private static final String NBT_MAIN_SOULS = "MainSouls";
    private static final String NBT_TOTAL_POWER = "TotalPower";
    private static final String NBT_SOUL_LOG = "SoulLog"; // last 5 absorbed soul names
    private static final String NBT_SOUL_NAME = "Name";
    private static final String NBT_SOUL_POWER = "Power";
    private static final String NBT_SOUL_IS_MAIN = "IsMain";

    // ── Constructor ──────────────────────────────────────────────────
    public BillionSoulFlagItem(Properties props) {
        // props already carries .durability(2000) from registration.
        // In MC 1.20.1, durability() forces maxStackSize=1 — do NOT call stacksTo().
        super(props.rarity(Rarity.EPIC));
    }

    // ── Right-click behavior ─────────────────────────────────────────
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack flag = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(flag);
        }

        if (player.isShiftKeyDown()) {
            return soulSense(level, player, hand, flag);
        } else {
            return soulStorm(level, player, hand, flag);
        }
    }

    // ── Soul Storm: AoE damage scaling with accumulated souls ────────
    private InteractionResultHolder<ItemStack> soulStorm(Level level, Player player,
                                                          InteractionHand hand, ItemStack flag) {
        if (!(level instanceof ServerLevel sl)) {
            return InteractionResultHolder.success(flag);
        }

        // Cooldown check
        if (player.getCooldowns().isOnCooldown(this)) {
            player.sendSystemMessage(Component.literal("\u00A77The flag's souls are still recovering...")
                    .withStyle(ChatFormatting.GRAY));
            return InteractionResultHolder.fail(flag);
        }

        int ordinary = getOrdinarySoulCount(flag);
        int main = getMainSoulCount(flag);
        if (ordinary == 0 && main == 0) {
            player.sendSystemMessage(Component.literal("\u00A7cThe flag is empty. Kill entities while holding it to absorb souls.")
                    .withStyle(ChatFormatting.RED));
            return InteractionResultHolder.fail(flag);
        }

        float totalPower = getTotalPower(flag);
        // Damage formula: 5.0 base + sqrt(power) * 0.5
        // At 0 power: 5.0 (minimal)
        // At 100 power (10 ordinary souls): 10.0
        // At 1000 power: 20.9
        // At 10000 power: 55.0
        // At 100000 power (max ordinary): 163.2
        // Main souls add 10x power each, so 37 main souls = 37000 power = +96.2 damage
        float stormDamage = 5.0F + (float) Math.sqrt(totalPower) * 0.5F;

        // Find all hostile entities within storm radius
        AABB area = player.getBoundingBox().inflate(STORM_RADIUS);
        List<LivingEntity> targets = sl.getEntitiesOfClass(LivingEntity.class, area,
                e -> e.isAlive() && e != player);

        if (targets.isEmpty()) {
            player.sendSystemMessage(Component.literal("\u00A77No enemies within soul storm range ("
                    + STORM_RADIUS + " blocks).").withStyle(ChatFormatting.GRAY));
            return InteractionResultHolder.success(flag);
        }

        // Distribute damage — total storm damage is split across targets,
        // but each target takes at least 50% of the per-target damage
        // (canon: soul storm saturates an area, not focused on one target).
        float perTargetDamage = Math.max(stormDamage / targets.size(),
                (float)(stormDamage * 0.5F / Math.sqrt(targets.size())));

        int killed = 0;
        for (LivingEntity target : targets) {
            target.hurt(sl.damageSources().playerAttack(player), perTargetDamage);
            // Wither II for 4 seconds — soul corrosion
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 1));
            // Slowness — soul pressure
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0));

            if (target.isDeadOrDying()) {
                killed++;
            }
        }

        // Visual + audio: massive soul storm
        sl.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
                player.getX(), player.getY() + 1, player.getZ(),
                80, STORM_RADIUS * 0.5, 3.0, STORM_RADIUS * 0.5, 0.15);
        sl.sendParticles(net.minecraft.core.particles.ParticleTypes.REVERSE_PORTAL,
                player.getX(), player.getY() + 1, player.getZ(),
                50, STORM_RADIUS * 0.4, 2.5, STORM_RADIUS * 0.4, 0.08);
        // Ring of soul particles expanding outward
        for (int ring = 0; ring < 3; ring++) {
            double radius = 4.0 + ring * 4.0;
            int count = 12 + ring * 6;
            for (int i = 0; i < count; i++) {
                double angle = (i / (double) count) * Math.PI * 2;
                double ox = Math.cos(angle) * radius;
                double oz = Math.sin(angle) * radius;
                sl.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL,
                        player.getX() + ox, player.getY() + 0.5, player.getZ() + oz,
                        2, 0.1, 0.5, 0.1, 0.0);
            }
        }

        level.playSound(null, player.blockPosition(),
                SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.5f, 0.4f);
        level.playSound(null, player.blockPosition(),
                SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 1.0f, 0.6f);

        // Cost: durability + cooldown. Souls are NOT consumed (permanent refinement).
        flag.hurtAndBreak(STORM_DURABILITY_COST, player, p -> p.broadcastBreakEvent(hand));
        player.getCooldowns().addCooldown(this, STORM_COOLDOWN);

        player.sendSystemMessage(Component.literal(
                "\u00A7d\u2726 Soul Storm released! \u00A77" + targets.size() + " enemies hit for "
                        + String.format("%.1f", perTargetDamage) + " each. "
                        + killed + " slain.").withStyle(ChatFormatting.LIGHT_PURPLE));

        // CRON-89: wire through the event bus so the simulation observes the storm.
        if (player instanceof ServerPlayer sp) {
            dev.ergenverse.simulation.action.SimulationActions.spellCast(
                    sp, "Billion Soul Flag: Soul Storm", "soul_weapon",
                    1.0f, dev.ergenverse.simulation.event.ActionDescriptors.Visibility.LOCAL);
        }

        return InteractionResultHolder.sidedSuccess(flag, level.isClientSide);
    }

    // ── Soul Sense: reveal nearby entities ───────────────────────────
    private InteractionResultHolder<ItemStack> soulSense(Level level, Player player,
                                                          InteractionHand hand, ItemStack flag) {
        if (!(level instanceof ServerLevel sl)) {
            return InteractionResultHolder.success(flag);
        }

        if (player.getCooldowns().isOnCooldown(this)) {
            player.sendSystemMessage(Component.literal("\u00A77Spiritual sense still recovering...")
                    .withStyle(ChatFormatting.GRAY));
            return InteractionResultHolder.fail(flag);
        }

        AABB area = player.getBoundingBox().inflate(SENSE_RADIUS);
        List<LivingEntity> entities = sl.getEntitiesOfClass(LivingEntity.class, area,
                e -> e.isAlive() && e != player);

        for (LivingEntity entity : entities) {
            entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, SENSE_DURATION, 0));
        }

        // Visual: pulse of end rod particles outward
        for (int ring = 0; ring < 4; ring++) {
            double radius = 4.0 + ring * 7.0;
            int count = 16 + ring * 8;
            for (int i = 0; i < count; i++) {
                double angle = (i / (double) count) * Math.PI * 2;
                double ox = Math.cos(angle) * radius;
                double oz = Math.sin(angle) * radius;
                sl.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,
                        player.getX() + ox, player.getY() + 1.0, player.getZ() + oz,
                        1, 0.0, 0.0, 0.0, 0.0);
            }
        }

        level.playSound(null, player.blockPosition(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8f, 1.5f);

        flag.hurtAndBreak(SENSE_DURABILITY_COST, player, p -> p.broadcastBreakEvent(hand));
        player.getCooldowns().addCooldown(this, SENSE_COOLDOWN);

        player.sendSystemMessage(Component.literal(
                "\u00A7b\u2726 Spiritual sense pulse: \u00A77" + entities.size()
                        + " entities revealed within " + SENSE_RADIUS + " blocks.")
                .withStyle(ChatFormatting.AQUA));

        return InteractionResultHolder.sidedSuccess(flag, level.isClientSide);
    }

    // ── Soul absorption (called by BillionSoulFlagEventHandler) ──────

    /**
     * Attempt to absorb the soul of a dying entity into the flag.
     *
     * <p>Called by {@link BillionSoulFlagEventHandler} on LivingDeathEvent when
     * the killer is holding this item. The absorption chance is 50% base,
     * increasing by +1% per 2000 souls already stored (up to 95%). Boss
     * entities (health &gt; 50) always yield a "main soul" on absorption.
     *
     * @param flag    the flag ItemStack (in the killer's hand)
     * @param victim  the dying entity
     * @param sl      the server level
     * @return true if a soul was absorbed
     */
    public boolean absorbSoul(ItemStack flag, LivingEntity victim, ServerLevel sl) {
        int ordinary = getOrdinarySoulCount(flag);
        int main = getMainSoulCount(flag);

        // Check capacity
        boolean mainSoulCandidate = victim.getMaxHealth() >= 50.0F;
        if (mainSoulCandidate && main >= MAX_MAIN_SOULS) {
            // Main soul capacity full — absorb as ordinary instead
            mainSoulCandidate = false;
        }
        if (!mainSoulCandidate && ordinary >= MAX_ORDINARY_SOULS) {
            return false; // flag is full
        }

        // Absorption chance: base 50% + 1% per 2000 souls, capped at 95%
        double chance = BASE_ABSORPTION_CHANCE
                + Math.min(0.45, (ordinary + main * 100) / 2000.0 * 0.01);
        if (sl.getRandom().nextDouble() > chance) {
            return false; // soul escaped
        }

        // Calculate soul power
        float soulPower = Math.max(1.0F, victim.getMaxHealth() / 4.0F);
        if (mainSoulCandidate) {
            soulPower *= 10.0F; // main souls are 10x more powerful
        }

        // Write to NBT
        CompoundTag tag = flag.getOrCreateTag();
        if (mainSoulCandidate) {
            tag.putInt(NBT_MAIN_SOULS, main + 1);
        } else {
            tag.putInt(NBT_ORDINARY_SOULS, ordinary + 1);
        }
        tag.putFloat(NBT_TOTAL_POWER, getTotalPower(flag) + soulPower);

        // Log the soul name (keep last 5 for tooltip)
        ListTag log = tag.getList(NBT_SOUL_LOG, Tag.TAG_COMPOUND);
        CompoundTag entry = new CompoundTag();
        entry.putString(NBT_SOUL_NAME, victim.getDisplayName().getString());
        entry.putFloat(NBT_SOUL_POWER, soulPower);
        entry.putBoolean(NBT_SOUL_IS_MAIN, mainSoulCandidate);
        log.add(entry);
        while (log.size() > 5) {
            log.remove(0);
        }
        tag.put(NBT_SOUL_LOG, log);

        // Visual: soul particles flowing from victim to flag holder
        sl.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
                victim.getX(), victim.getY() + victim.getBbHeight() * 0.5, victim.getZ(),
                mainSoulCandidate ? 20 : 8, 0.3, 0.3, 0.3, 0.05);

        return true;
    }

    // ── NBT helpers ──────────────────────────────────────────────────

    public int getOrdinarySoulCount(ItemStack flag) {
        return flag.getOrCreateTag().getInt(NBT_ORDINARY_SOULS);
    }

    public int getMainSoulCount(ItemStack flag) {
        return flag.getOrCreateTag().getInt(NBT_MAIN_SOULS);
    }

    public float getTotalPower(ItemStack flag) {
        return flag.getOrCreateTag().getFloat(NBT_TOTAL_POWER);
    }

    public int getTotalSoulCount(ItemStack flag) {
        return getOrdinarySoulCount(flag) + getMainSoulCount(flag);
    }

    /**
     * Get the tier name based on total soul count.
     *
     * <p>Canon progression: the flag is named differently as it grows stronger.
     * The iconic "Billion Soul Flag (十亿魂幡)" name is earned at max capacity.
     */
    public String getTierName(ItemStack flag) {
        int total = getTotalSoulCount(flag);
        if (total == 0) return "Empty Soul Flag";
        if (total < 100) return "Soul Flag 魂幡";
        if (total < 1000) return "Hundred Soul Flag 百魂幡";
        if (total < 10000) return "Thousand Soul Flag 千魂幡";
        if (total < 100000) return "Myriad Soul Flag 万魂幡";
        return "Billion Soul Flag 十亿魂幡";
    }

    /**
     * Get the tier color for tooltip display.
     */
    public ChatFormatting getTierColor(ItemStack flag) {
        int total = getTotalSoulCount(flag);
        if (total == 0) return ChatFormatting.GRAY;
        if (total < 100) return ChatFormatting.WHITE;
        if (total < 1000) return ChatFormatting.GREEN;
        if (total < 10000) return ChatFormatting.AQUA;
        if (total < 100000) return ChatFormatting.LIGHT_PURPLE;
        return ChatFormatting.GOLD;
    }

    // ── Tooltip ──────────────────────────────────────────────────────

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        int ordinary = getOrdinarySoulCount(stack);
        int main = getMainSoulCount(stack);
        float power = getTotalPower(stack);
        String tierName = getTierName(stack);
        ChatFormatting tierColor = getTierColor(stack);

        tooltip.add(Component.literal(tierName).withStyle(tierColor));
        tooltip.add(Component.literal("\u00A77Soul Refining Sect guardian treasure")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal(""));

        tooltip.add(Component.literal("\u00A7cOrdinary Souls: \u00A7f" + ordinary + "/" + MAX_ORDINARY_SOULS)
                .withStyle(ChatFormatting.RED));
        tooltip.add(Component.literal("\u00A75Main Souls: \u00A7f" + main + "/" + MAX_MAIN_SOULS)
                .withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.literal("\u00A7bTotal Power: \u00A7f" + String.format("%.1f", power))
                .withStyle(ChatFormatting.BLUE));

        // Damage preview
        float stormDamage = 5.0F + (float) Math.sqrt(power) * 0.5F;
        tooltip.add(Component.literal("\u00A7eStorm Damage: \u00A7f~" + String.format("%.1f", stormDamage)
                + " \u00A78(per target, split)")
                .withStyle(ChatFormatting.YELLOW));

        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("\u00A7aRight-click: \u00A77Soul Storm (16-block AoE)")
                .withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.literal("\u00A7aShift+Right-click: \u00A77Soul Sense (32-block reveal)")
                .withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.literal("\u00A78Passive: absorbs souls on kill while held")
                .withStyle(ChatFormatting.DARK_GRAY));

        // Show recent soul absorptions
        if (ordinary + main > 0) {
            CompoundTag tag = stack.getOrCreateTag();
            ListTag log = tag.getList(NBT_SOUL_LOG, Tag.TAG_COMPOUND);
            if (!log.isEmpty()) {
                tooltip.add(Component.literal(""));
                tooltip.add(Component.literal("\u00A78Recent souls refined:")
                        .withStyle(ChatFormatting.DARK_GRAY));
                for (int i = log.size() - 1; i >= 0 && i >= log.size() - 3; i--) {
                    CompoundTag entry = log.getCompound(i);
                    String name = entry.getString(NBT_SOUL_NAME);
                    boolean isMain = entry.getBoolean(NBT_SOUL_IS_MAIN);
                    String prefix = isMain ? "\u00A75\u2605 " : "\u00A77\u2022 ";
                    tooltip.add(Component.literal(prefix + name)
                            .withStyle(isMain ? ChatFormatting.DARK_PURPLE : ChatFormatting.GRAY));
                }
            }
        }
    }

    // ── Foil (shimmer) for the epic rarity ───────────────────────────

    @Override
    public boolean isFoil(ItemStack stack) {
        return getTotalSoulCount(stack) > 0;
    }

    // ── Repairable with beast_core ───────────────────────────────────

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        // Repairable with beast_core (each restores 50 durability via anvil).
        // Canon: the flag was repaired after damage (via Gate of Emptiness;
        // gameplay simplification: beast cores as repair material).
        return repair.is(dev.ergenverse.item.ErgenverseItems.BEAST_CORE.get());
    }
}
