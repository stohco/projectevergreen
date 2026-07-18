package dev.ergenverse.material;

import dev.ergenverse.material.MaterialProperties.Element;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MaterialRegistry — maps Minecraft items/blocks to their MaterialProperties.
 *
 * <p>Every item that can be used in crafting has MaterialProperties registered here.
 * Crafting stations query this registry to get the properties of any ingredient.
 *
 * <p>Pre-populated with canon materials from the Er Gen novels:
 * <ul>
 *   <li>Ores: Iron Sand, Cold-Iron, Spirit-Iron, Star-Iron, Thunder-Iron, Nether-Iron</li>
 *   <li>Beast Parts: Beast Bone, Beast Core, Wolf Core, Rabbit Blood, Dragon Blood</li>
 *   <li>Herbs: Qi-Gathering Grass, Fire-Bloom Lotus, Snow-Heart Herb, etc.</li>
 *   <li>Spiritual: Spirit Stone, Jade, Formation Flag blanks</li>
 * </ul>
 */
public final class MaterialRegistry {

    private static final Map<ResourceLocation, MaterialProperties> REGISTRY = new ConcurrentHashMap<>();

    private MaterialRegistry() {}

    /**
     * Register material properties for an item.
     */
    public static void register(ResourceLocation itemId, MaterialProperties props) {
        REGISTRY.put(itemId, props);
    }

    /**
     * Get material properties for an item. Returns default mundane properties if not registered.
     */
    public static MaterialProperties get(ResourceLocation itemId) {
        return REGISTRY.getOrDefault(itemId, new MaterialProperties());
    }

    /**
     * Get material properties for an item by namespace:path string.
     */
    public static MaterialProperties get(String itemId) {
        return get(new ResourceLocation(itemId));
    }

    /**
     * Check if an item has registered material properties.
     */
    public static boolean hasProperties(ResourceLocation itemId) {
        return REGISTRY.containsKey(itemId);
    }

    /**
     * Get all registered material items.
     */
    public static Set<ResourceLocation> getAllRegistered() {
        return Collections.unmodifiableSet(REGISTRY.keySet());
    }

    /**
     * Seed all canon material properties.
     * Called at mod init.
     */
    public static void seedDefaults() {
        if (!REGISTRY.isEmpty()) return;

        String modId = "ergenverse";

        // ═══ Spirit Ores ═══
        register(new ResourceLocation(modId, "iron_sand"),
                new MaterialProperties().density(7.8).hardness(4.0).brittleness(0.2).elasticity(0.1)
                        .qiConductivity(0.05).element(Element.METAL, 0.8).origin("natural"));

        register(new ResourceLocation(modId, "cold_iron_ingot"),
                new MaterialProperties().density(7.5).hardness(5.5).brittleness(0.15).elasticity(0.1)
                        .qiConductivity(0.2).element(Element.METAL, 0.7).element(Element.WATER, 0.4)
                        .element(Element.YIN, 0.3).origin("natural"));

        register(new ResourceLocation(modId, "spirit_iron_ingot"),
                new MaterialProperties().density(7.0).hardness(6.0).brittleness(0.1).elasticity(0.15)
                        .qiConductivity(0.5).soulConductivity(0.1)
                        .element(Element.METAL, 0.7).element(Element.YANG, 0.2).origin("spirit_vein"));

        // ═══ Spirit Stones ═══
        register(new ResourceLocation(modId, "spirit_stone"),
                new MaterialProperties().density(2.5).hardness(7.0).brittleness(0.4).elasticity(0.05)
                        .qiConductivity(0.9).divineSenseConductivity(0.3)
                        .restrictionAffinity(0.5).daoResonance(0.1)
                        .element(Element.YANG, 0.3).element(Element.LIFE, 0.2)
                        .origin("spirit_vein").ageYears(100));

        register(new ResourceLocation(modId, "spirit_stone_fragment"),
                new MaterialProperties().density(2.5).hardness(7.0).brittleness(0.6).elasticity(0.05)
                        .qiConductivity(0.7).element(Element.YANG, 0.2).origin("spirit_vein"));

        // ═══ Beast Parts ═══
        register(new ResourceLocation(modId, "beast_bone"),
                new MaterialProperties().density(2.0).hardness(4.5).brittleness(0.5).elasticity(0.1)
                        .qiConductivity(0.15).bloodAffinity(0.3)
                        .element(Element.EARTH, 0.3).element(Element.DEATH, 0.1).origin("beast"));

        register(new ResourceLocation(modId, "beast_core"),
                new MaterialProperties().density(3.0).hardness(5.0).brittleness(0.3).elasticity(0.2)
                        .qiConductivity(0.7).soulConductivity(0.4).bloodAffinity(0.6)
                        .daoResonance(0.1)
                        .element(Element.LIFE, 0.4).element(Element.YANG, 0.3)
                        .origin("beast").ageYears(10));

        register(new ResourceLocation(modId, "wolf_core"),
                new MaterialProperties().density(3.0).hardness(5.0).brittleness(0.3).elasticity(0.2)
                        .qiConductivity(0.6).soulConductivity(0.3).bloodAffinity(0.5)
                        .element(Element.METAL, 0.3).element(Element.WIND, 0.2)
                        .origin("beast"));

        register(new ResourceLocation(modId, "rabbit_blood_essence"),
                new MaterialProperties().density(1.1).hardness(0.1).brittleness(0.0).elasticity(0.9)
                        .qiConductivity(0.3).bloodAffinity(0.8).soulConductivity(0.1)
                        .element(Element.LIFE, 0.5).element(Element.WOOD, 0.2).element(Element.LIFE, 0.3)
                        .origin("beast"));

        // ═══ Crafting Materials ═══
        register(new ResourceLocation(modId, "spirit_ink"),
                new MaterialProperties().density(1.3).hardness(0.0).brittleness(0.0).elasticity(0.8)
                        .qiConductivity(0.5).soulConductivity(0.2).restrictionAffinity(0.4)
                        .element(Element.WATER, 0.3).element(Element.YIN, 0.2)
                        .origin("refined"));

        register(new ResourceLocation(modId, "formation_flag_blank"),
                new MaterialProperties().density(0.5).hardness(1.0).brittleness(0.2).elasticity(0.7)
                        .qiConductivity(0.3).restrictionAffinity(0.6)
                        .element(Element.WOOD, 0.3).element(Element.WIND, 0.2)
                        .origin("crafted"));

        register(new ResourceLocation(modId, "talisman_paper_blank"),
                new MaterialProperties().density(0.3).hardness(0.5).brittleness(0.3).elasticity(0.5)
                        .qiConductivity(0.4).soulConductivity(0.15)
                        .element(Element.WOOD, 0.4).origin("crafted"));

        register(new ResourceLocation(modId, "jade_slip_blank"),
                new MaterialProperties().density(3.3).hardness(7.5).brittleness(0.2).elasticity(0.05)
                        .qiConductivity(0.8).soulConductivity(0.5).divineSenseConductivity(0.6)
                        .restrictionAffinity(0.7).daoResonance(0.2)
                        .element(Element.EARTH, 0.3).element(Element.YANG, 0.2)
                        .origin("mined").ageYears(50));

        register(new ResourceLocation(modId, "meditation_mat"),
                new MaterialProperties().density(0.4).hardness(0.5).brittleness(0.1).elasticity(0.6)
                        .qiConductivity(0.3).element(Element.WOOD, 0.3).element(Element.EARTH, 0.2)
                        .origin("crafted"));

        register(new ResourceLocation(modId, "cave_dwelling_core"),
                new MaterialProperties().density(4.0).hardness(8.0).brittleness(0.1).elasticity(0.05)
                        .qiConductivity(0.9).soulConductivity(0.7).divineSenseConductivity(0.8)
                        .restrictionAffinity(0.8).daoResonance(0.4)
                        .element(Element.SPACE, 0.5).element(Element.TIME, 0.2)
                        .origin("ancient").ageYears(10000));

        // ═══ Pills (as materials for further crafting) ═══
        register(new ResourceLocation(modId, "qi_gathering_pill"),
                new MaterialProperties().density(1.5).hardness(0.5).brittleness(0.7).elasticity(0.1)
                        .qiConductivity(0.8).element(Element.LIFE, 0.3).element(Element.YANG, 0.2)
                        .origin("alchemy"));

        register(new ResourceLocation(modId, "foundation_pill"),
                new MaterialProperties().density(1.5).hardness(0.5).brittleness(0.7).elasticity(0.1)
                        .qiConductivity(0.9).soulConductivity(0.3)
                        .element(Element.LIFE, 0.4).element(Element.FIRE, 0.2).element(Element.EARTH, 0.2)
                        .origin("alchemy"));

        register(new ResourceLocation(modId, "purification_pill"),
                new MaterialProperties().density(1.5).hardness(0.5).brittleness(0.7).elasticity(0.1)
                        .qiConductivity(0.7).element(Element.WATER, 0.4).element(Element.YIN, 0.3)
                        .origin("alchemy"));

        register(new ResourceLocation(modId, "soul_mending_pill"),
                new MaterialProperties().density(1.5).hardness(0.5).brittleness(0.7).elasticity(0.1)
                        .qiConductivity(0.6).soulConductivity(0.8)
                        .element(Element.LIFE, 0.3).element(Element.YANG, 0.3)
                        .origin("alchemy").ageYears(1));

        // ═══ Spirit Herb Seeds ═══
        register(new ResourceLocation(modId, "spirit_herb_seed"),
                new MaterialProperties().density(0.2).hardness(0.1).brittleness(0.8).elasticity(0.2)
                        .qiConductivity(0.1).element(Element.WOOD, 0.5).element(Element.LIFE, 0.3)
                        .origin("natural"));

        net.minecraft.server.MinecraftServer server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            dev.ergenverse.core.Ergenverse.LOGGER.info("[Ergenverse] MaterialRegistry: seeded {} material definitions", REGISTRY.size());
        }
    }

    /**
     * Get a status report.
     */
    public static String getStatusReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("Material Registry: ").append(REGISTRY.size()).append(" materials registered\n");
        for (Map.Entry<ResourceLocation, MaterialProperties> e : REGISTRY.entrySet()) {
            sb.append("  ").append(e.getKey()).append(" → ").append(e.getValue()).append("\n");
        }
        return sb.toString();
    }
}
