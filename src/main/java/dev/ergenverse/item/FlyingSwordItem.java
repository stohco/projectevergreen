package dev.ergenverse.item;

import dev.ergenverse.cultivation.CultivationCapability;
import dev.ergenverse.cultivation.CultivationState;
import dev.ergenverse.cultivation.RealmId;
import dev.ergenverse.entity.FlyingSwordProjectileEntity;
import dev.ergenverse.item.sword.SwordEffectType;
import dev.ergenverse.simulation.event.EnergyType;
import dev.ergenverse.simulation.event.WorldEventBus;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;


import java.util.EnumSet;
import java.util.Set;

/**
 * FlyingSwordItem — a flying sword (飞剑) with canon-faithful mechanics.
 *
 * <p>Extends SwordItem so left-click melee works (standard sword damage).
 * Right-click launches a FlyingSwordProjectile toward the player's look direction —
 * a streaking blade of qi that homes on the player's last-hurt target, deals damage
 * scaled by cultivation realm, and returns to the owner.
 *
 * <p>Canon: flying swords are the signature weapon of cultivators. Wang Lin's
 * Wealth Flying Sword, Core Treasure Sword, Dark Green Flying Sword, Blood
 * Slaughter Sword — each is a sentient weapon that flies out and returns.
 *
 * <p>CRON-COMPLETIONIST-55 upgrades:
 * <ul>
 *   <li>Custom SpiritIronTier replaces placeholder Tiers.IRON</li>
 *   <li>Projectile damage scales with cultivation realm: base × (1.0 + realmStage × 0.5)</li>
 *   <li>Per-sword effects via SwordEffectType (teleport, lifesteal, poison, restriction)</li>
 *   <li>NBT persistence for sword effects and spirit name</li>
 *   <li>WorldEventBus publication for "player.sword_launched"</li>
 * </ul>
 */
public class FlyingSwordItem extends SwordItem {

    /** The base projectile damage before cultivation scaling. */
    private final float projectileDamage;

    /** The supernatural effects this sword applies on hit. */
    private final Set<SwordEffectType> swordEffects = EnumSet.noneOf(SwordEffectType.class);

    /**
     * Create a FlyingSwordItem with base projectile damage and properties.
     *
     * @param projectileDamage base projectile damage (scaled by realm on launch)
     * @param props item properties
     */
    public FlyingSwordItem(float projectileDamage, Properties props) {
        super(SpiritIronTier.INSTANCE, 3, -2.0F, props);
        this.projectileDamage = projectileDamage;
    }

    /**
     * Create a FlyingSwordItem with base damage, effects, and properties.
     *
     * @param projectileDamage base projectile damage (scaled by realm on launch)
     * @param effect the sword effect type
     * @param props item properties
     */
    public FlyingSwordItem(float projectileDamage, SwordEffectType effect, Properties props) {
        super(SpiritIronTier.INSTANCE, 3, -2.0F, props);
        this.projectileDamage = projectileDamage;
        if (effect != null && effect != SwordEffectType.NONE) {
            this.swordEffects.add(effect);
        }
    }

    /**
     * Create a FlyingSwordItem with base damage, multiple effects, and properties.
     *
     * @param projectileDamage base projectile damage (scaled by realm on launch)
     * @param effects the sword effect types
     * @param props item properties
     */
    public FlyingSwordItem(float projectileDamage, Set<SwordEffectType> effects, Properties props) {
        super(SpiritIronTier.INSTANCE, 3, -2.0F, props);
        this.projectileDamage = projectileDamage;
        if (effects != null) {
            this.swordEffects.addAll(effects);
        }
    }

    /** Get the set of sword effects for this flying sword. */
    public Set<SwordEffectType> getSwordEffects() {
        return swordEffects;
    }

    /** Check whether this sword has a specific effect type. */
    public boolean hasSwordEffect(SwordEffectType type) {
        return swordEffects.contains(type);
    }

    /** Get the primary (first) effect, or NONE. */
    public SwordEffectType getPrimaryEffect() {
        return swordEffects.isEmpty() ? SwordEffectType.NONE : swordEffects.iterator().next();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // ── Cultivation-scaled damage ──
        float scaledDamage = projectileDamage;
        int realmStage = 0;
        CultivationState state = CultivationCapability.get(player).resolve().orElse(null);
        if (state != null) {
            realmStage = state.getCurrentRealm().order;
            // projectileDamage × (1.0 + realmStage × 0.5)
            // Mortal (0): 1.0x, Qi Condensation (1): 1.5x, Foundation (2): 2.0x, etc.
            scaledDamage = projectileDamage * (1.0F + realmStage * 0.5F);
        }

        // Launch the flying sword projectile
        if (!level.isClientSide && level instanceof ServerLevel sl) {
            FlyingSwordProjectileEntity projectile = new FlyingSwordProjectileEntity(level, player);
            // Start at the player's eye position
            projectile.setPos(player.getEyePosition());
            // Velocity in the look direction
            Vec3 look = player.getLookAngle();
            projectile.shoot(look.x, look.y, look.z, 2.0F, 1.0F);
            projectile.setDamage(scaledDamage);

            // Store the item's NBT (effect type, spirit name) on the projectile
            // so effects persist and the returned item retains its identity
            CompoundTag swordData = new CompoundTag();
            // Persist effect type from the item definition
            SwordEffectType primary = getPrimaryEffect();
            swordData.putString("SwordEffect", primary.getName());
            // CRON-COMPLETIONIST-57: Persist registry name so buildReturnItem recreates
            // the CORRECT sword type (not always wealth_flying_sword).
            swordData.putString("SwordRegistryName",
                    net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(this).toString());
            // Persist spirit name from item NBT if present
            if (stack.hasTag() && stack.getTag().contains("SwordSpirit")) {
                swordData.putString("SwordSpirit", stack.getTag().getString("SwordSpirit"));
            }
            // Store display name for return item
            if (stack.hasCustomHoverName()) {
                swordData.putString("SwordDisplayName", stack.getHoverName().getString());
            }
            projectile.setSwordData(swordData);

            sl.addFreshEntity(projectile);

            // Play sword-swish sound
            player.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 1.5F);

            // ── Wire sword launch through WorldEventBus ──
            String swordName = stack.hasCustomHoverName()
                    ? stack.getHoverName().getString()
                    : "Flying Sword";
            WorldEventBus.publish("player.sword_launched",
                    EnergyType.QI, player.blockPosition(),
                    0.5f, player.getName().getString() + " launched " + swordName,
                    "canon:flying_sword", sl.getGameTime());
        }

        // Cooldown (shorter if the sword has a "sword spirit" NBT)
        int cooldown = stack.hasTag() && stack.getTag().contains("SwordSpirit") ? 20 : 30;
        player.getCooldowns().addCooldown(this, cooldown);

        // Cost 1 durability per launch
        stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void verifyTagAfterLoad(CompoundTag tag) {
        // Ensure NBT tags exist for persistence
        super.verifyTagAfterLoad(tag);
        // Write default effect type if not present
        if (!tag.contains("SwordEffect")) {
            tag.putString("SwordEffect", getPrimaryEffect().getName());
        }
    }
}
