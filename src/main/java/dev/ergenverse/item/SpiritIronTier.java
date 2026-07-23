package dev.ergenverse.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;

/**
 * SpiritIronTier — a custom Tier for flying swords (飞剑).
 *
 * <p>Canon: flying swords are forged from spirit iron (灵铁), not mundane
 * iron. They are swift in attack (speed 8.0), have moderate base damage
 * (5.0, amplified by cultivation scaling in FlyingSwordItem), and are
 * durable enough for repeated qi-launches (900 durability). Their mining
 * capability is equivalent to iron tier — they cut through stone, not
 * obsidian.
 *
 * <p>Replaces the placeholder Tiers.IRON used in early FlyingSwordItem.
 */
public enum SpiritIronTier implements Tier {
    INSTANCE;

    private static final TagKey<Block> TAG = TagKey.create(
            Registries.BLOCK,
            new ResourceLocation("forge", "blocksneeds_iron_tool"));

    private final TagKey<Item> repairTag = TagKey.create(
            Registries.ITEM,
            new ResourceLocation("forge", "ingots/iron"));

    @Override
    public int getUses() {
        return 900;
    }

    @Override
    public float getSpeed() {
        return 8.0F;
    }

    @Override
    public float getAttackDamageBonus() {
        return 5.0F;
    }

    @Override
    public int getLevel() {
        return 2; // IRON-equivalent mining level
    }

    @Override
    public TagKey<Block> getTag() {
        return TAG;
    }

    @Override
    public int getEnchantmentValue() {
        return 15;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(repairTag); // TagKey<Item>
    }
}
