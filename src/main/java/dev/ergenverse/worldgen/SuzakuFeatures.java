package dev.ergenverse.worldgen;
import dev.ergenverse.core.Ergenverse;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
public class SuzakuFeatures {
    public static final DeferredRegister<?> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, Ergenverse.MOD_ID);
    public static void register(IEventBus bus) { FEATURES.register(bus); }
}
