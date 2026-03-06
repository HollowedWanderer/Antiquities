package net.hollowed.antique.index;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.hollowed.antique.Antiquities;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class AntiquePlacedFeatures {

    public static final ResourceKey<PlacedFeature> MYRIAD_ORE_MIDDLE_KEY = ResourceKey.create(Registries.PLACED_FEATURE, Antiquities.id("ore_myriad_middle"));
    public static final ResourceKey<PlacedFeature> MYRIAD_ORE_SMALL_KEY = ResourceKey.create(Registries.PLACED_FEATURE, Antiquities.id("ore_myriad_small"));
    public static final ResourceKey<PlacedFeature> MYRIAD_ORE_UPPER_KEY = ResourceKey.create(Registries.PLACED_FEATURE, Antiquities.id("ore_myriad_upper"));
    
    public static void initialize() {
        BiomeModifications.addFeature(BiomeSelectors.tag(AntiqueBiomeTags.IS_WINDSWEPT), GenerationStep.Decoration.UNDERGROUND_ORES, MYRIAD_ORE_MIDDLE_KEY);
        BiomeModifications.addFeature(BiomeSelectors.tag(AntiqueBiomeTags.IS_WINDSWEPT), GenerationStep.Decoration.UNDERGROUND_ORES, MYRIAD_ORE_SMALL_KEY);
        BiomeModifications.addFeature(BiomeSelectors.tag(AntiqueBiomeTags.IS_WINDSWEPT), GenerationStep.Decoration.UNDERGROUND_ORES, MYRIAD_ORE_UPPER_KEY);
    }
}
