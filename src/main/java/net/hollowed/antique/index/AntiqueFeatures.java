package net.hollowed.antique.index;

import com.mojang.datafixers.util.Pair;
import net.hollowed.antique.Antiquities;
import net.hollowed.antique.worldgen.features.BasicBuddingOreFeature;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.feature.BlockReplacement;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.templatesystem.HeightMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import java.util.List;

public class AntiqueFeatures {

    public static final ResourceKey<Feature> ORE_MYRIAD = createKey("ore_myriad");

    public static void initialize() {
        //Registry.register(BuiltInRegistries.FEATURE_TYPE, Antiquities.id("ore_budding"), BasicBuddingOreFeature.CODEC);
    }

    public static ResourceKey<Feature> createKey(final String name) {
        return ResourceKey.create(Registries.FEATURE, Antiquities.id(name));
    }

    public static void bootstrap(final BootstrapContext<Feature> context) {
        RuleTest waterOrAirReplaceables = RuleTest.anyOf(
                new TagMatchTest(AntiqueBlockTags.WATER_OR_AIR)
        );
        RuleTest stoneOreReplaceables = RuleTest.either(
                new TagMatchTest(BlockTags.HEIGHT_SPECIFIC_ORE_REPLACEABLES), HeightMatchTest.min(0), new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES)
        );
        RuleTest deepslateOreReplaceables = RuleTest.either(
                new TagMatchTest(BlockTags.HEIGHT_SPECIFIC_ORE_REPLACEABLES), HeightMatchTest.max(8), new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES)
        );

        List<Pair<BlockReplacement, BlockReplacement>> myriadTargets = List.of(
                new Pair<>(BlockReplacement.replace(stoneOreReplaceables, AntiqueBlocks.MYRIAD_ORE.defaultBlockState()), BlockReplacement.replace(waterOrAirReplaceables, AntiqueBlocks.MYRIAD_CLUSTER.defaultBlockState())),
                new Pair<>(BlockReplacement.replace(deepslateOreReplaceables, AntiqueBlocks.DEEPSLATE_MYRIAD_ORE.defaultBlockState()), BlockReplacement.replace(waterOrAirReplaceables, AntiqueBlocks.DEEPSLATE_MYRIAD_CLUSTER.defaultBlockState()))
        );

        context.register(ORE_MYRIAD, new BasicBuddingOreFeature(myriadTargets, 9, 0));
    }
}
