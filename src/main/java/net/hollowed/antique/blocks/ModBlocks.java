package net.hollowed.antique.blocks;

import net.hollowed.antique.Antiquities;
import net.hollowed.antique.blocks.custom.*;
import net.minecraft.block.*;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.ColorCode;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

import java.util.function.Function;

public class ModBlocks {

    public static final Block PEDESTAL = registerBlock("pedestal",
            new PedestalBlock(AbstractBlock.Settings.create().registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Antiquities.MOD_ID, "pedestal")))
                    .sounds(BlockSoundGroup.LODESTONE).strength(1.5F, 6F).nonOpaque().requiresTool()), Rarity.COMMON);

    public static final Block JAR = registerBlock("jar",
            new JarBlock(AbstractBlock.Settings.create().registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Antiquities.MOD_ID, "jar")))
                    .sounds(BlockSoundGroup.DECORATED_POT).strength(0.3F).nonOpaque()), Rarity.COMMON);

    public static final Block OMINOUS_PEDESTAL = registerBlock("ominous_pedestal",
            new OminousPedestalBlock(AbstractBlock.Settings.create().registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Antiquities.MOD_ID, "ominous_pedestal")))
                    .sounds(BlockSoundGroup.LODESTONE).strength(1.5F, 6F).nonOpaque().requiresTool()), Rarity.COMMON);

    public static final Block DYE_TABLE = registerBlock("dye_table",
            new DyeTableBlock(AbstractBlock.Settings.create().registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Antiquities.MOD_ID, "dye_table")))
                    .sounds(BlockSoundGroup.WOOD).strength(2.5F)), Rarity.COMMON);

    public static final Block MYRIAD_CLUSTER = registerBlock("myriad_cluster",
            new AmethystClusterBlock(8, 8, AbstractBlock.Settings.create().registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Antiquities.MOD_ID, "myriad_cluster")))
                    .sounds(BlockSoundGroup.IRON).strength(15F, 600F).requiresTool()), Rarity.COMMON);

    public static final Block MYRIAD_ORE = registerBlock("myriad_ore",
            new Block(AbstractBlock.Settings.create().registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Antiquities.MOD_ID, "myriad_ore")))
                    .sounds(BlockSoundGroup.IRON).strength(25F, 600F).requiresTool()), Rarity.COMMON);

    public static final Block DEEPSLATE_MYRIAD_CLUSTER = registerBlock("deepslate_myriad_cluster",
            new AmethystClusterBlock(8, 8, AbstractBlock.Settings.create().registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Antiquities.MOD_ID, "deepslate_myriad_cluster")))
                    .sounds(BlockSoundGroup.IRON).strength(20F, 600F).requiresTool()), Rarity.COMMON);

    public static final Block DEEPSLATE_MYRIAD_ORE = registerBlock("deepslate_myriad_ore",
            new Block(AbstractBlock.Settings.create().registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Antiquities.MOD_ID, "deepslate_myriad_ore")))
                    .sounds(BlockSoundGroup.IRON).strength(30F, 600F).requiresTool()), Rarity.COMMON);

    public static final Block RAW_MYRIAD_BLOCK = registerBlock("raw_myriad_block",
            new Block(AbstractBlock.Settings.create().registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Antiquities.MOD_ID, "raw_myriad_block")))
                    .sounds(BlockSoundGroup.IRON).strength(30F, 600F).requiresTool()), Rarity.COMMON);

    public static final Block MYRIAD_BLOCK = registerBlock("myriad_block",
            new TarnishingBlock(AbstractBlock.Settings.create().registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Antiquities.MOD_ID, "myriad_block")))
                    .sounds(BlockSoundGroup.IRON).strength(30F, 600F).requiresTool()), Rarity.COMMON);

    public static final Block EXPOSED_MYRIAD_BLOCK = registerBlock("exposed_myriad_block",
            new TarnishingBlock(AbstractBlock.Settings.create().registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Antiquities.MOD_ID, "exposed_myriad_block")))
                    .sounds(BlockSoundGroup.IRON).strength(30F, 600F).requiresTool()), Rarity.COMMON);

    public static final Block WEATHERED_MYRIAD_BLOCK = registerBlock("weathered_myriad_block",
            new TarnishingBlock(AbstractBlock.Settings.create().registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Antiquities.MOD_ID, "weathered_myriad_block")))
                    .sounds(BlockSoundGroup.IRON).strength(30F, 600F).requiresTool()), Rarity.COMMON);

    public static final Block TARNISHED_MYRIAD_BLOCK = registerBlock("tarnished_myriad_block",
            new TarnishingBlock(AbstractBlock.Settings.create().registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Antiquities.MOD_ID, "tarnished_myriad_block")))
                    .sounds(BlockSoundGroup.IRON).strength(30F, 600F).requiresTool()), Rarity.COMMON);

    public static final Block COATED_MYRIAD_BLOCK = registerBlock("coated_myriad_block",
            new Block(AbstractBlock.Settings.create().registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Antiquities.MOD_ID, "coated_myriad_block")))
                    .sounds(BlockSoundGroup.IRON).strength(30F, 600F).requiresTool()), Rarity.COMMON);

    public static final Block COATED_EXPOSED_MYRIAD_BLOCK = registerBlock("coated_exposed_myriad_block",
            new Block(AbstractBlock.Settings.create().registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Antiquities.MOD_ID, "coated_exposed_myriad_block")))
                    .sounds(BlockSoundGroup.IRON).strength(30F, 600F).requiresTool()), Rarity.COMMON);

    public static final Block COATED_WEATHERED_MYRIAD_BLOCK = registerBlock("coated_weathered_myriad_block",
            new Block(AbstractBlock.Settings.create().registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Antiquities.MOD_ID, "coated_weathered_myriad_block")))
                    .sounds(BlockSoundGroup.IRON).strength(30F, 600F).requiresTool()), Rarity.COMMON);

    public static final Block COATED_TARNISHED_MYRIAD_BLOCK = registerBlock("coated_tarnished_myriad_block",
            new Block(AbstractBlock.Settings.create().registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Antiquities.MOD_ID, "coated_tarnished_myriad_block")))
                    .sounds(BlockSoundGroup.IRON).strength(30F, 600F).requiresTool()), Rarity.COMMON);

    public static final Block BLACK_SAND = registerBlock("black_sand",
            new ColoredFallingBlock(new ColorCode(14406560), AbstractBlock.Settings.create().registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Antiquities.MOD_ID, "black_sand")))
                    .sounds(BlockSoundGroup.SAND).strength(1.5F, 6F)), Rarity.COMMON);

    public static final Block IVY = registerBlock(
            "ivy",
            new GlowLichenBlock(AbstractBlock.Settings.create()
                    .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Antiquities.MOD_ID, "ivy")))
                    .mapColor(MapColor.GREEN)
                    .replaceable()
                    .noCollision()
                    .strength(0.2F)
                    .sounds(BlockSoundGroup.CAVE_VINES)
                    .burnable()
                    .pistonBehavior(PistonBehavior.DESTROY)),
            Rarity.COMMON
    );

    public static final Block GILDED_BLACK_SAND = registerBlock("gilded_black_sand",
            new ColoredFallingBlock(new ColorCode(14406560), AbstractBlock.Settings.create().registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Antiquities.MOD_ID, "gilded_black_sand")))
                    .sounds(BlockSoundGroup.SAND).strength(1.5F, 6F)), Rarity.COMMON);

    public static final Block HOLLOW_CORE = registerBlock("hollow_core",
            new HeavyCoreBlock(AbstractBlock.Settings.create().registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Antiquities.MOD_ID, "hollow_core")))
                    .sounds(ModBlockSoundGroup.HOLLOW_CORE).strength(10F, 1200F)
                    .pistonBehavior(PistonBehavior.NORMAL).instrument(NoteBlockInstrument.IRON_XYLOPHONE).mapColor(MapColor.IRON_GRAY)
                    .nonOpaque()), Rarity.UNCOMMON);

    private static Block registerBlock(String name, Block block, Rarity rarity) {
        registerBlockItem(name, block, rarity);
        return Registry.register(Registries.BLOCK, Identifier.of(Antiquities.MOD_ID, name), block);
    }

    private static void registerBlockItem(String name, Block block, Rarity rarity) {
        Registry.register(Registries.ITEM, Identifier.of(Antiquities.MOD_ID, name),
                new BlockItem(block, new Item.Settings()
                        .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Antiquities.MOD_ID, name))).useBlockPrefixedTranslationKey().rarity(rarity)));
    }

    private static Block register(String id, Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings) {
        return register(keyOf(id), factory, settings);
    }

    public static Block register(RegistryKey<Block> key, Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings) {
        Block block = factory.apply(settings.registryKey(key));
        return Registry.register(Registries.BLOCK, key, block);
    }

    public static Block register(RegistryKey<Block> key, AbstractBlock.Settings settings) {
        return register(key, Block::new, settings);
    }

    private static RegistryKey<Block> keyOf(String id) {
        return RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Antiquities.MOD_ID, id));
    }

    private static AbstractBlock.Settings copyLootTable(Block block, boolean copyTranslationKey) {
        AbstractBlock.Settings settings = block.getSettings();
        AbstractBlock.Settings settings2 = AbstractBlock.Settings.create().lootTable(block.getLootTableKey());
        if (copyTranslationKey) {
            settings2 = settings2.overrideTranslationKey(block.getTranslationKey());
        }

        return settings2;
    }

    public static void initialize() {
        Antiquities.LOGGER.info("Antiquities Blocks Initialized");
    }
}
