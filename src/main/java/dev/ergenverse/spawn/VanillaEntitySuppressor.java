package dev.ergenverse.spawn;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * VanillaEntitySuppressor — prevents ALL vanilla mobs and animals from
 * spawning in the world.
 *
 * <p>Per the user's directive: "there should be 0 vanilla minecraft blocks
 * or entities, we are making our custom everything." This handler cancels
 * the {@link EntityJoinLevelEvent} for any entity whose type belongs to the
 * {@code minecraft} namespace, with these exceptions:
 *
 * <ul>
 *   <li><b>Players</b> — never cancelled (obviously).</li>
 *   <li><b>Non-living utility entities</b> — item frames, paintings, boats,
 *       minecarts, experience orbs, falling blocks, etc. are NOT cancelled
 *       because they are mechanical conveniences, not "creatures." Cancelling
 *       them would break item frames on walls, boats on water, etc.</li>
 * </ul>
 *
 * <p>Only <b>living</b> vanilla entities (mobs, animals, monsters, villagers,
 * fish, golems, etc.) are cancelled. This removes cows, pigs, sheep, chickens,
 * zombies, skeletons, creepers, spiders, endermen, villagers, iron golems,
 * squids, fish, axolotls, frogs, etc. — the world belongs to the spirit beasts
 * ({@code ergenverse:spirit_rabbit}, {@code spirit_wolf}, {@code spirit_deer},
 * {@code spirit_hawk}, {@code fire_beast}, {@code stone_back_boar}).
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17 APIs only.
 */
@Mod.EventBusSubscriber(modid = Ergenverse.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class VanillaEntitySuppressor {

    private VanillaEntitySuppressor() {}

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (entity == null) return;

        // Never cancel players.
        if (entity instanceof Player) return;

        // Only cancel LIVING vanilla entities (mobs, animals, monsters).
        // This leaves item frames, paintings, boats, minecarts, experience
        // orbs, falling blocks, etc. intact.
        if (!(entity instanceof LivingEntity)) return;

        ResourceLocation typeId = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (typeId == null) return;

        // Only cancel entities from the "minecraft" namespace.
        // Mod entities (ergenverse:*) are allowed.
        if (!"minecraft".equals(typeId.getNamespace())) return;

        // Cancel the join — the entity is removed before it enters the world.
        event.setCanceled(true);
        Ergenverse.LOGGER.debug("[Ergenverse] Suppressed vanilla entity spawn: {}", typeId);
    }
}
