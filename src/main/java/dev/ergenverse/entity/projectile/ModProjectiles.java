package dev.ergenverse.entity.projectile;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * ModProjectiles — DeferredRegister for projectile entity types.
 *
 * <p>Central registration point so the main mod class can register all
 * projectiles with one call: {@code ModProjectiles.PROJECTILES.register(modEventBus)}.
 */
public final class ModProjectiles {

    private ModProjectiles() {}

    public static final DeferredRegister<EntityType<?>> PROJECTILES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Ergenverse.MOD_ID);

    /** The flying-sword projectile — a streaking blade of qi launched by FlyingSwordItem. */
    public static final RegistryObject<EntityType<dev.ergenverse.entity.FlyingSwordProjectileEntity>> FLYING_SWORD =
            PROJECTILES.register("flying_sword", () ->
                    EntityType.Builder.<dev.ergenverse.entity.FlyingSwordProjectileEntity>of(
                            dev.ergenverse.entity.FlyingSwordProjectileEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .build("flying_sword"));
}
