package dev.ergenverse.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * DeferredSpawnEggItem — a spawn egg that defers EntityType resolution.
 *
 * <p>Minecraft's {@link SpawnEggItem} requires an {@code EntityType<?>} at
 * construction time. In Forge 1.20.1, the ITEM registry fires BEFORE the
 * ENTITY_TYPE registry, so {@code RegistryObject.get()} is null during item
 * registration. This class wraps the entity type in a {@link Supplier} and
 * resolves it lazily on first use (well after both registries are populated).
 *
 * <p>All behaviour (right-click to spawn, colour tinting, creative tab
 * registration) is delegated to the superclass once the type is resolved.
 */
public class DeferredSpawnEggItem extends SpawnEggItem {

    private final Supplier<EntityType<? extends Mob>> entityTypeSupplier;
    private volatile EntityType<? extends Mob> resolvedType;

    public DeferredSpawnEggItem(Supplier<EntityType<? extends Mob>> entityTypeSupplier,
                                int primaryColor, int secondaryColor,
                                Item.Properties properties) {
        // Superclass needs a dummy type during construction.
        // It is never used because we override getDefaultType().
        super(null, primaryColor, secondaryColor, properties);
        this.entityTypeSupplier = entityTypeSupplier;
    }

    @Override
    protected EntityType<?> getDefaultType() {
        if (resolvedType == null) {
            resolvedType = entityTypeSupplier.get();
            if (resolvedType == null) {
                throw new IllegalStateException("DeferredSpawnEggItem entity type supplier returned null");
            }
        }
        return resolvedType;
    }
}
