package net.hollowed.antique.index;

import net.hollowed.antique.Antiquities;
import net.hollowed.antique.items.*;
import net.hollowed.combatamenities.util.items.CAComponents;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.InstrumentComponent;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.block.Block;
import java.util.List;
import java.util.function.Function;

public interface AntiqueItems {

    HolderGetter<Block> registryEntryLookup = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);

    Item RAW_MYRIAD = register("raw_myriad", Item::new);
    Item MYRIAD_INGOT = register("myriad_ingot", Item::new);
    Item CLOTH = register("cloth", settings -> new Item(settings
            .component(DataComponents.DYED_COLOR, new DyedItemColor(0xD43B69))
            .component(AntiqueDataComponentTypes.STICKY_TOOLTIP, true)
    ));
    Item CLOTH_PATTERN = register("cloth_pattern", settings -> new ClothPatternItem(settings.stacksTo(1)
            .component(AntiqueDataComponentTypes.STICKY_TOOLTIP, true)
    ));
    Item MIRAGE_SILK = register("mirage_silk", settings -> new Item(settings.rarity(Rarity.RARE)));
    Item BAG_OF_TRICKS = register("bag_of_tricks", settings -> new BagOfTricksItem(settings.stacksTo(1)
            .component(CAComponents.INTEGER_PROPERTY, -1)
            .component(AntiqueDataComponentTypes.SATCHEL_STACK, List.of())
            .component(AntiqueDataComponentTypes.COUNTER, 2)
            .rarity(Rarity.RARE)
    ));
    Item SMOKE_BOMB = register("smoke_bomb", settings -> new SmokeBombItem(settings.stacksTo(16)));
    Item MYRIAD_PAULDRONS = register("myriad_pauldrons", settings -> new MyriadPauldronsItem(AntiqueArmorMaterials.ADVENTURE, ArmorType.CHESTPLATE, settings.stacksTo(1)
            .rarity(Rarity.UNCOMMON)
            .fireResistant()
    ));
    Item SATCHEL = register("satchel", settings -> new SatchelItem(AntiqueArmorMaterials.ADVENTURE, ArmorType.LEGGINGS, settings.stacksTo(1)
            .component(CAComponents.INTEGER_PROPERTY, -1)
            .component(AntiqueDataComponentTypes.SATCHEL_STACK, List.of())
            .component(AntiqueDataComponentTypes.COUNTER, 2)
            .rarity(Rarity.UNCOMMON)
            .fireResistant()
    ));
    Item FUR_BOOTS = register("fur_boots", settings -> new FurBootsItem(AntiqueArmorMaterials.ADVENTURE, ArmorType.BOOTS, settings.stacksTo(1)
            .rarity(Rarity.UNCOMMON)
            .fireResistant()
    ));
    Item SCEPTER = register("scepter", settings -> new ScepterItem(settings.stacksTo(1)
            .attributes(ScepterItem.createAttributeModifiers())
            .enchantable(10)
            .rarity(Rarity.UNCOMMON)
            .durability(500)
    ));
    @SuppressWarnings("unused")
    Item COPPER_GLACE = register("copper_glace", settings -> new Glace(settings.stacksTo(1)
            .attributes(Glace.copperAttributes())
            .enchantable(10)
            .rarity(Rarity.UNCOMMON)
            .durability(313)
    ));
    @SuppressWarnings("unused")
    Item QUARRY_GLACE = register("quarry_glace", settings -> new Glace(settings.stacksTo(1)
            .attributes(Glace.copperAttributes())
            .enchantable(10)
            .rarity(Rarity.UNCOMMON)
            .durability(313)
    ));
    @SuppressWarnings("unused")
    Item PROSPECTOR = register("prospector", settings -> new Glace(settings.stacksTo(1)
            .attributes(Glace.copperAttributes())
            .enchantable(10)
            .rarity(Rarity.UNCOMMON)
            .durability(313)
    ));
    Item CRYOSCYTHE = register("cryoscythe", settings -> new DeathItem(settings.stacksTo(1)
            .attributes(DeathItem.createAttributeModifiers())
            .enchantable(10).rarity(Rarity.EPIC)
            .durability(2031)
            .fireResistant()
    ));
    Item MYRIAD_TOOL = register("myriad_tool", settings -> new MyriadToolItem(settings.stacksTo(1)
            .attributes(MyriadToolItem.createAttributeModifiers(3, 1.8, 0))
            .component(AntiqueDataComponentTypes.MYRIAD_TOOL, Antiquities.getDefaultMyriadTool())
            .component(AntiqueDataComponentTypes.STICKY_TOOLTIP, true)
            .component(DataComponents.UNBREAKABLE, Unit.INSTANCE)
            .enchantable(10)
            .rarity(Rarity.UNCOMMON)
            .fireResistant()
    ));
    Item MYRIAD_PICK_HEAD = register("myriad_mattock_head", settings -> new MyriadMattockBit(settings.stacksTo(1)
            .attributes(MyriadToolItem.createAttributeModifiers(3, 2, 0))
            .component(DataComponents.TOOL, ShearsItem.createToolProperties())
            .component(DataComponents.UNBREAKABLE, Unit.INSTANCE)
            .enchantable(10)
            .rarity(Rarity.UNCOMMON)
            .fireResistant()
    ));
    Item MYRIAD_AXE_HEAD = register("myriad_axe_head", settings -> new MyriadAxeBit(settings.stacksTo(1)
            .attributes(MyriadToolItem.createAttributeModifiers(4, 1.7, 0))
            .component(DataComponents.UNBREAKABLE, Unit.INSTANCE)
            .enchantable(10)
            .rarity(Rarity.UNCOMMON)
            .fireResistant()
    ));
    Item MYRIAD_SHOVEL_HEAD = register("myriad_shovel_head", settings -> new MyriadShovelBit(settings.stacksTo(1)
            .attributes(MyriadToolItem.createAttributeModifiers(5, 2.2, 0))
            .component(DataComponents.UNBREAKABLE, Unit.INSTANCE)
            .enchantable(10)
            .rarity(Rarity.UNCOMMON)
            .fireResistant()
    ));
    Item MYRIAD_CLEAVER_BLADE = register("myriad_cleaver_blade", settings -> new MyriadCleaverBit(settings.stacksTo(1)
            .attributes(MyriadToolItem.createAttributeModifiers(6, 1.6, 0))
            .component(DataComponents.UNBREAKABLE, Unit.INSTANCE)
            .enchantable(10)
            .rarity(Rarity.UNCOMMON)
            .fireResistant()
    ));
    @SuppressWarnings("deprecation")
    Item WARHORN = register("warhorn", settings -> new InstrumentItem(settings.stacksTo(1)
            .component(DataComponents.INSTRUMENT, new InstrumentComponent(AntiqueInstruments.WARHORN))
            .rarity(Rarity.UNCOMMON)
    ));
    Item ILLUSIONER_SPAWN_EGG = register("illusioner_spawn_egg", settings -> new SpawnEggItem(settings.stacksTo(64)
            .spawnEgg(AntiqueEntities.ILLUSIONER)
    ));

    static Item register(String id, Function<Item.Properties, Item> factory) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Antiquities.id(id));
        Item item = factory.apply(new Item.Properties().setId(key));
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    static void initialize() {}
}
