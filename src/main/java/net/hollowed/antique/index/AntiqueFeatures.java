package net.hollowed.antique.index;

import net.hollowed.antique.worldgen.features.MyriadOreFeature;
import net.hollowed.antique.worldgen.features.MyriadOreFeatureConfig;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import org.jetbrains.annotations.NotNull;

public class AntiqueFeatures {

    @SuppressWarnings("unused")
    public static final Feature<@NotNull MyriadOreFeatureConfig> MYRIAD_ORE = register("antique:myriad_ore", new MyriadOreFeature(MyriadOreFeatureConfig.CODEC));

    @SuppressWarnings("all")
    private static <C extends FeatureConfiguration, F extends Feature<C>> F register(String name, F feature) {
        return Registry.register(BuiltInRegistries.FEATURE, name, feature);
    }

    public static void initialize() {}
}
