package net.hollowed.antique.index;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.hollowed.antique.Antiquities;
import net.hollowed.antique.items.*;
import net.hollowed.combatamenities.util.items.ModComponents;
import net.minecraft.block.Block;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.InstrumentComponent;
import net.minecraft.item.*;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.registry.*;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;
import net.minecraft.util.Unit;

import java.util.List;
import java.util.function.Function;

public interface AntiqueItems {

    RegistryEntryLookup<Block> registryEntryLookup = Registries.createEntryLookup(Registries.BLOCK);

    Item RAW_MYRIAD = register("raw_myriad", Item::new);
    Item MYRIAD_INGOT = register("myriad_ingot", Item::new);
    Item SILK = register("silk", Item::new);
    Item MIRAGE_SILK = register("mirage_silk", settings -> new Item(settings.rarity(Rarity.RARE)));
    Item BAG_OF_TRICKS = register("bag_of_tricks", settings -> new BagOfTricksItem(settings.maxCount(1)
            .component(ModComponents.INTEGER_PROPERTY, -1)
            .component(AntiqueComponents.SATCHEL_STACK, List.of())
            .component(AntiqueComponents.COUNTER, 2)
            .rarity(Rarity.RARE)
    ));
    Item SMOKE_BOMB = register("smoke_bomb", settings -> new SmokeBombItem(settings.maxCount(16)));
    Item NETHERITE_PAULDRONS = register("netherite_pauldrons", settings -> new NetheritePauldronsItem(AntiqueArmorMaterials.ADVENTURE, EquipmentType.CHESTPLATE, settings.maxCount(1)
            .rarity(Rarity.UNCOMMON)
            .fireproof()
    ));
    Item SATCHEL = register("satchel", settings -> new SatchelItem(settings.maxCount(1)
            .armor(AntiqueArmorMaterials.ADVENTURE_BASIC, EquipmentType.LEGGINGS)
            .component(ModComponents.INTEGER_PROPERTY, -1)
            .component(AntiqueComponents.SATCHEL_STACK, List.of())
            .component(AntiqueComponents.COUNTER, 2)
            .rarity(Rarity.UNCOMMON)
            .fireproof()
    ));
    Item FUR_BOOTS = register("fur_boots", settings -> new FurBootsItem(AntiqueArmorMaterials.ADVENTURE, EquipmentType.BOOTS, settings.maxCount(1)
            .rarity(Rarity.UNCOMMON)
            .fireproof()
    ));
    Item SCEPTER = register("scepter", settings -> new VelocityTransferMaceItem(settings.maxCount(1)
            .attributeModifiers(VelocityTransferMaceItem.createAttributeModifiers())
            .enchantable(10)
            .rarity(Rarity.UNCOMMON)
            .maxDamage(500)
    ));
    Item PALE_WARDENS_GREATSWORD = register("pale_wardens_greatsword", settings -> new Item(settings.maxCount(1)
            .attributeModifiers(VelocityTransferMaceItem.createAttributeModifiers())
            .sword(ToolMaterial.NETHERITE, 1.0F, -2.4F)
            .enchantable(10)
            .rarity(Rarity.RARE)
            .maxDamage(2031)
    ));
    Item REVERENCE = register("reverence", settings -> new ReverenceItem(settings.maxCount(1)
            .attributeModifiers(ReverenceItem.createAttributeModifiers())
            .enchantable(10)
            .rarity(Rarity.EPIC)
            .maxDamage(2031)
            .fireproof()
    ));
    Item IRREVERENT = register("irreverence", settings -> new DeathItem(settings.maxCount(1)
            .attributeModifiers(DeathItem.createAttributeModifiers())
            .enchantable(10).rarity(Rarity.EPIC)
            .maxDamage(2031)
            .fireproof()
    ));
    Item MYRIAD_TOOL = register("myriad_tool", settings -> new MyriadToolItem(settings.maxCount(1)
            .attributeModifiers(MyriadToolItem.createAttributeModifiers(4, 1.8, 0.25))
            .component(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0xd43b69))
            .component(AntiqueComponents.MYRIAD_STACK, ItemStack.EMPTY)
            .component(DataComponentTypes.UNBREAKABLE, Unit.INSTANCE)
            .enchantable(10)
            .rarity(Rarity.UNCOMMON)
            .fireproof()
    ));
    Item MYRIAD_STAFF = register("myriad_staff", settings -> new MyriadStaffItem(settings.maxCount(1)
            .attributeModifiers(MyriadToolItem.createAttributeModifiers(4, 1.8, 0.25))
            .component(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0xd43b69))
            .component(AntiqueComponents.MYRIAD_STACK, ItemStack.EMPTY)
            .component(DataComponentTypes.UNBREAKABLE, Unit.INSTANCE)
            .enchantable(10)
            .rarity(Rarity.UNCOMMON)
            .fireproof()
    ));
    Item MYRIAD_PICK_HEAD = register("myriad_mattock_head", settings -> new MyriadMattockBit(settings.maxCount(1)
            .attributeModifiers(MyriadToolItem.createAttributeModifiers(3, 2, 0.25))
            .component(DataComponentTypes.TOOL, ShearsItem.createToolComponent())
            .component(DataComponentTypes.UNBREAKABLE, Unit.INSTANCE)
            .enchantable(10)
            .rarity(Rarity.UNCOMMON)
            .fireproof()
    ));
    Item MYRIAD_AXE_HEAD = register("myriad_axe_head", settings -> new MyriadAxeBit(settings.maxCount(1)
            .attributeModifiers(MyriadToolItem.createAttributeModifiers(4, 1.7, 0.25))
            .component(DataComponentTypes.UNBREAKABLE, Unit.INSTANCE)
            .enchantable(10)
            .rarity(Rarity.UNCOMMON)
            .fireproof()
    ));
    Item MYRIAD_SHOVEL_HEAD = register("myriad_shovel_head", settings -> new MyriadShovelBit(settings.maxCount(1)
            .attributeModifiers(MyriadToolItem.createAttributeModifiers(5, 2.2, 0.25))
            .component(DataComponentTypes.UNBREAKABLE, Unit.INSTANCE)
            .enchantable(10)
            .rarity(Rarity.UNCOMMON)
            .fireproof()
    ));
    Item MYRIAD_CLEAVER_BLADE = register("myriad_cleaver_blade", settings -> new MyriadCleaverBit(settings.maxCount(1)
            .attributeModifiers(MyriadToolItem.createAttributeModifiers(6, 1.6, 0.25))
            .component(DataComponentTypes.UNBREAKABLE, Unit.INSTANCE)
            .enchantable(10)
            .rarity(Rarity.UNCOMMON)
            .fireproof()
    ));
    Item MYRIAD_CLAW = register("myriad_claw", settings -> new MyriadClawBit(settings.maxCount(1)
            .attributeModifiers(MyriadToolItem.createAttributeModifiers(4, 1.8, 0.25))
            .component(DataComponentTypes.UNBREAKABLE, Unit.INSTANCE)
            .enchantable(10)
            .rarity(Rarity.UNCOMMON)
            .fireproof()
    ));
    @SuppressWarnings("unused")
    Item DORMANT_REVERENCE = register("dormant_reverence", settings -> new ReverenceItem(settings.maxCount(1)
            .attributeModifiers(ReverenceItem.createAttributeModifiers())
            .enchantable(10)
            .rarity(Rarity.EPIC)
            .maxDamage(2031)
    ));
    @SuppressWarnings("all")
    Item WARHORN = register("warhorn", settings -> new GoatHornItem(settings.maxCount(1)
            .component(DataComponentTypes.INSTRUMENT, new InstrumentComponent(AntiqueInstruments.WARHORN))
            .rarity(Rarity.UNCOMMON)
    ));
    @SuppressWarnings("unused")
    Item PALE_WARDEN_STATUE = register("pale_warden_statue", settings -> new PaleWardenSpawnEggItem(AntiqueEntities.PALE_WARDEN, settings.maxCount(64)));
    Item ILLUSIONER_SPAWN_EGG = register("illusioner_spawn_egg", settings -> new SpawnEggItem(AntiqueEntities.ILLUSIONER, settings.maxCount(64)));

    static Item register(String id, Function<Item.Settings, Item> factory) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, Antiquities.id(id));
        Item item = factory.apply(new Item.Settings().registryKey(key));
        return Registry.register(Registries.ITEM, key, item);
    }

    static void initialize() {
        Antiquities.LOGGER.info("Antiquities Items Initialized");

        ItemTooltipCallback.EVENT.register((itemStack, tooltipContext, tooltipType, list) -> {
            if (itemStack.isOf(AntiqueItems.MYRIAD_TOOL) || itemStack.isOf(MYRIAD_STAFF)) {
                int toRemove = -1;
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).toString().contains("dyed")) {
                        toRemove = i;
                    }
                }
                if (toRemove != -1) list.remove(toRemove);
            }
            if (itemStack.isOf(MYRIAD_CLAW)) {
                list.add(1, Text.translatable("item.antique.myriad_claw.tooltip"));
            }
            if (itemStack.isOf(MYRIAD_TOOL)) {
                int toRemove = -1;
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).toString().contains("item.color")) {
                        toRemove = i;
                    }
                }
                if (toRemove != -1) list.remove(toRemove);
                Text line = Text.translatable("item.antique.myriad_tool.no_tool").withColor(11184810);

                if (!itemStack.getOrDefault(AntiqueComponents.MYRIAD_STACK, ItemStack.EMPTY).isEmpty()) {
                    String string = itemStack.getOrDefault(AntiqueComponents.MYRIAD_STACK, ItemStack.EMPTY).getItem().getTranslationKey();
                    string = string.substring(20);
                    string = "item.antique.myriad_tool." + string.substring(0, string.indexOf("_"));
                    line = Text.translatable(string).withColor(11184810);
                }

                // Add to tooltip
                list.add(1, line);
            }
            if (itemStack.isOf(MYRIAD_STAFF)) {
                int toRemove = -1;
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).toString().contains("item.color")) {
                        toRemove = i;
                    }
                }
                if (toRemove != -1) list.remove(toRemove);
                Text text = Text.literal(" - ");
                text = text.copy().append(itemStack.getOrDefault(AntiqueComponents.MYRIAD_STACK, ItemStack.EMPTY).getItemName());
                text = text.copy().append(" -");
                list.add(1, text.getString().equals(" - Air -") ? Text.translatable("item.antique.myriad_staff.empty") : text);
            }
        });
    }
}
