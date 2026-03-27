package net.hollowed.antique.index;

import net.hollowed.antique.Antiquities;
import net.hollowed.antique.blocks.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GlowLichenBlock;
import net.minecraft.world.level.block.HeavyCoreBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import java.util.function.Function;

public interface AntiqueBlocks {

    Block PEDESTAL = register("pedestal", settings -> new PedestalBlock(settings
            .strength(1.5F, 6F)
            .sound(SoundType.LODESTONE)
            .requiresCorrectToolForDrops()
            .noOcclusion()
            ), Rarity.COMMON
    );
    Block RESONATOR = register("resonator", settings -> new Block(settings
            .strength(5F, 5F)
            .sound(SoundType.COPPER)
            .requiresCorrectToolForDrops()
            .noOcclusion()
            ), Rarity.COMMON
    );
    Block JAR = register("jar", settings -> new JarBlock(settings
            .sound(SoundType.DECORATED_POT)
            .strength(0.3F)
            .noOcclusion()
            ), Rarity.COMMON
    );
    Block DYE_TABLE = register("dye_table", settings -> new DyeTableBlock(settings
            .sound(SoundType.WOOD)
            .strength(2.5F)
            ), Rarity.COMMON
    );
    Block MYRIAD_CLUSTER = register("myriad_cluster", settings -> new AmethystClusterBlock(8, 8, settings
            .strength(15F, 600F)
            .sound(SoundType.IRON)
            .requiresCorrectToolForDrops()
            ), Rarity.COMMON
    );
    Block MYRIAD_ORE = register("myriad_ore", settings -> new Block(settings
            .strength(25F, 600F)
            .sound(SoundType.IRON)
            .requiresCorrectToolForDrops()
            ), Rarity.COMMON
    );
    Block DEEPSLATE_MYRIAD_CLUSTER = register("deepslate_myriad_cluster", settings -> new AmethystClusterBlock(8, 8, settings
            .strength(20F, 600F)
            .sound(SoundType.IRON)
            .requiresCorrectToolForDrops()
            ), Rarity.COMMON
    );
    Block DEEPSLATE_MYRIAD_ORE = register("deepslate_myriad_ore", settings -> new Block(settings
            .strength(30F, 600F)
            .sound(SoundType.IRON)
            .requiresCorrectToolForDrops()
            ), Rarity.COMMON
    );
    Block RAW_MYRIAD_BLOCK = register("raw_myriad_block", settings -> new Block(settings
            .strength(30F, 600F)
            .sound(SoundType.IRON)
            .requiresCorrectToolForDrops()
            ), Rarity.COMMON
    );
    Block MYRIAD_BLOCK = register("myriad_block", settings -> new TarnishingBlock(settings
            .strength(30F, 600F)
            .sound(SoundType.IRON)
            .requiresCorrectToolForDrops()
            ), Rarity.COMMON
    );
    Block EXPOSED_MYRIAD_BLOCK = register("exposed_myriad_block", settings -> new TarnishingBlock(settings
            .strength(30F, 600F)
            .sound(SoundType.IRON)
            .requiresCorrectToolForDrops()
            ), Rarity.COMMON
    );
    Block WEATHERED_MYRIAD_BLOCK = register("weathered_myriad_block", settings -> new TarnishingBlock(settings
            .strength(30F, 600F)
            .sound(SoundType.IRON)
            .requiresCorrectToolForDrops()
            ), Rarity.COMMON
    );
    Block TARNISHED_MYRIAD_BLOCK = register("tarnished_myriad_block", settings -> new TarnishingBlock(settings
            .strength(30F, 600F)
            .sound(SoundType.IRON)
            .requiresCorrectToolForDrops()
            ), Rarity.COMMON
    );
    Block COATED_MYRIAD_BLOCK = register("coated_myriad_block", settings -> new Block(settings
            .strength(30F, 600F)
            .sound(SoundType.IRON)
            .requiresCorrectToolForDrops()
            ), Rarity.COMMON
    );
    Block COATED_EXPOSED_MYRIAD_BLOCK = register("coated_exposed_myriad_block", settings -> new Block(settings
            .strength(30F, 600F)
            .sound(SoundType.IRON)
            .requiresCorrectToolForDrops()
            ), Rarity.COMMON
    );
    Block COATED_WEATHERED_MYRIAD_BLOCK = register("coated_weathered_myriad_block", settings -> new Block(settings
            .strength(30F, 600F)
            .sound(SoundType.IRON)
            .requiresCorrectToolForDrops()
            ), Rarity.COMMON
    );
    Block COATED_TARNISHED_MYRIAD_BLOCK = register("coated_tarnished_myriad_block", settings -> new Block(settings
            .strength(30F, 600F)
            .sound(SoundType.IRON)
            .requiresCorrectToolForDrops()
            ), Rarity.COMMON
    );
    Block IVY = register("ivy", settings -> new GlowLichenBlock(settings
            .pushReaction(PushReaction.DESTROY)
            .sound(SoundType.CAVE_VINES)
            .mapColor(MapColor.COLOR_GREEN)
            .strength(0.2F)
            .replaceable()
            .noCollision()
            .ignitedByLava()
            ), Rarity.COMMON
    );
    Block HOLLOW_CORE = register("hollow_core", settings -> new HeavyCoreBlock(settings
            .instrument(NoteBlockInstrument.IRON_XYLOPHONE)
            .sound(AntiqueBlockSoundGroup.HOLLOW_CORE)
            .strength(10F, 1200F)
            .pushReaction(PushReaction.NORMAL)
            .mapColor(MapColor.METAL)
            .noOcclusion()
            ), Rarity.UNCOMMON
    );

    static Block register(String id, Function<BlockBehaviour.Properties, Block> factory, Rarity rarity) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Antiquities.id(id));
        Block block = factory.apply(BlockBehaviour.Properties.of().setId(key));
        registerBlockItem(id, block, rarity);
        return Registry.register(BuiltInRegistries.BLOCK, key, block);
    }

    private static void registerBlockItem(String name, Block block, Rarity rarity) {
        Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(Antiquities.MOD_ID, name),
                new BlockItem(block, new Item.Properties()
                        .setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Antiquities.MOD_ID, name)))
                        .useBlockDescriptionPrefix()
                        .rarity(rarity)
                ));
    }

    static void initialize() {}
}
