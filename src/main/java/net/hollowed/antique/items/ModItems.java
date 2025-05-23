package net.hollowed.antique.items;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.hollowed.antique.Antiquities;
import net.hollowed.antique.component.ModComponents;
import net.hollowed.antique.entities.ModEntities;
import net.hollowed.antique.items.custom.*;
import net.hollowed.antique.items.custom.myriadStaff.MyriadStaffItem;
import net.minecraft.block.Block;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.InstrumentComponent;
import net.minecraft.item.*;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.registry.*;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.Unit;

import java.util.List;
import java.util.Objects;

public class ModItems {

    public static RegistryEntryLookup<Block> registryEntryLookup = Registries.createEntryLookup(Registries.BLOCK);

    public static final Item DIAMOND_GREATSWORD = registerItem("diamond_greatsword", new GreatswordItem(ModToolMaterial.DIAMOND, 4.0F, -2.7F, 0.5F, 0.5F, new Item.Settings()
            .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Antiquities.MOD_ID, "diamond_greatsword")))
            .maxCount(1)
    ));

    public static final Item EXPLOSIVE_SPEAR = registerItem("explosive_spear", new ExplosiveSpearItem(new Item.Settings()
            .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Antiquities.MOD_ID, "explosive_spear")))
            .maxCount(1)
    ));

    public static final Item NETHERITE_PAULDRONS = registerItem("netherite_pauldrons", new NetheritePauldronsItem(ModArmorMaterials.ADVENTURE, EquipmentType.CHESTPLATE, new Item.Settings()
            .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Antiquities.MOD_ID, "netherite_pauldrons")))
            .maxCount(1).fireproof()
    ));

    public static final Item SATCHEL = registerItem("satchel", new SatchelItem(
            new Item.Settings().armor(ModArmorMaterials.ADVENTURE_BASIC, EquipmentType.LEGGINGS)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Antiquities.MOD_ID, "satchel")))
                    .component(ModComponents.SATCHEL_STACK, List.of())
                    .maxCount(1)
                    .fireproof()
    ));

    public static final Item FUR_BOOTS = registerItem("fur_boots", new FurBootsItem(ModArmorMaterials.ADVENTURE, EquipmentType.BOOTS, new Item.Settings()
            .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Antiquities.MOD_ID, "fur_boots")))
            .maxCount(1).fireproof()
    ));

    public static final Item SCEPTER = registerItem("scepter", new VelocityTransferMaceItem(new Item.Settings()
            .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Antiquities.MOD_ID, "scepter")))
            .maxCount(1).attributeModifiers(VelocityTransferMaceItem.createAttributeModifiers()).maxDamage(500).enchantable(10).rarity(Rarity.UNCOMMON)
    ));

    public static final Item PALE_WARDENS_GREATSWORD = registerItem("pale_wardens_greatsword", new Item(new Item.Settings().sword(ToolMaterial.NETHERITE, 1.0F, -2.4F)
            .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Antiquities.MOD_ID, "pale_wardens_greatsword")))
            .maxCount(1).attributeModifiers(VelocityTransferMaceItem.createAttributeModifiers()).maxDamage(2031).enchantable(10).rarity(Rarity.RARE)
    ));

    public static final Item REVERENCE = registerItem("reverence", new ReverenceItem(new Item.Settings()
            .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Antiquities.MOD_ID, "reverence")))
            .maxCount(1).attributeModifiers(ReverenceItem.createAttributeModifiers()).maxDamage(2031).enchantable(10).rarity(Rarity.EPIC)
            .fireproof()
    ));

    public static final Item IRREVERENT = registerItem("irreverence", new DeathItem(new Item.Settings()
            .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Antiquities.MOD_ID, "irreverence")))
            .maxCount(1).attributeModifiers(DeathItem.createAttributeModifiers()).maxDamage(2031).enchantable(10).rarity(Rarity.EPIC)
            .fireproof()
    ));

    public static final Item MYRIAD_TOOL = registerItem("myriad_tool", new MyriadToolItem(new Item.Settings()
            .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Antiquities.MOD_ID, "myriad_tool")))
            .maxCount(1).attributeModifiers(MyriadToolItem.createAttributeModifiers(4, 1.8, 0.25)).enchantable(10).rarity(Rarity.UNCOMMON).fireproof()
            .component(ModComponents.MYRIAD_STACK, ItemStack.EMPTY)
            .component(net.hollowed.combatamenities.util.items.ModComponents.INTEGER_PROPERTY, 0)
            .component(DataComponentTypes.UNBREAKABLE, Unit.INSTANCE)
            .component(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0xd43b69))
    ));

    public static final Item MYRIAD_STAFF = registerItem("myriad_staff", new MyriadStaffItem(new Item.Settings()
            .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Antiquities.MOD_ID, "myriad_staff")))
            .maxCount(1).attributeModifiers(MyriadToolItem.createAttributeModifiers(4, 1.8, 0.25)).enchantable(10).rarity(Rarity.UNCOMMON).fireproof()
            .component(ModComponents.MYRIAD_STACK, ItemStack.EMPTY)
            .component(DataComponentTypes.UNBREAKABLE, Unit.INSTANCE)
            .component(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0xd43b69))
    ));

    public static final Item MYRIAD_PICK_HEAD = registerItem("myriad_mattock_head", new MyriadToolBitItem(new Item.Settings()
            .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Antiquities.MOD_ID, "myriad_mattock_head")))
            .maxCount(1).attributeModifiers(MyriadToolItem.createAttributeModifiers(3, 2, 0.25)).enchantable(10).rarity(Rarity.UNCOMMON)
            .fireproof().component(DataComponentTypes.UNBREAKABLE, Unit.INSTANCE).component(DataComponentTypes.TOOL, ShearsItem.createToolComponent()), 1
    ));

    public static final Item MYRIAD_AXE_HEAD = registerItem("myriad_axe_head", new MyriadToolBitItem(new Item.Settings()
            .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Antiquities.MOD_ID, "myriad_axe_head")))
            .maxCount(1).attributeModifiers(MyriadToolItem.createAttributeModifiers(4, 1.7, 0.25)).enchantable(10).rarity(Rarity.UNCOMMON)
            .fireproof().component(DataComponentTypes.UNBREAKABLE, Unit.INSTANCE), 2
    ));

    public static final Item MYRIAD_SHOVEL_HEAD = registerItem("myriad_shovel_head", new MyriadToolBitItem(new Item.Settings()
            .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Antiquities.MOD_ID, "myriad_shovel_head")))
            .maxCount(1).attributeModifiers(MyriadToolItem.createAttributeModifiers(2, 2.2, 0.25)).enchantable(10).rarity(Rarity.UNCOMMON)
            .fireproof().component(DataComponentTypes.UNBREAKABLE, Unit.INSTANCE), 3
    ));

    public static final Item MYRIAD_CLEAVER_BLADE = registerItem("myriad_cleaver_blade", new MyriadToolBitItem(new Item.Settings()
            .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Antiquities.MOD_ID, "myriad_cleaver_blade")))
            .maxCount(1).attributeModifiers(MyriadToolItem.createAttributeModifiers(6, 1.6, 0.25)).enchantable(10).rarity(Rarity.UNCOMMON)
            .fireproof().component(DataComponentTypes.UNBREAKABLE, Unit.INSTANCE), 4
    ));

    public static final Item MYRIAD_CLAW = registerItem("myriad_claw", new MyriadToolBitItem(new Item.Settings()
            .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Antiquities.MOD_ID, "myriad_claw")))
            .maxCount(1).attributeModifiers(MyriadToolItem.createAttributeModifiers(4, 1.8, 0.25)).enchantable(10).rarity(Rarity.UNCOMMON)
            .fireproof().component(DataComponentTypes.UNBREAKABLE, Unit.INSTANCE), 5
    ));

    @SuppressWarnings("unused")
    public static final Item DORMANT_REVERENCE = registerItem("dormant_reverence", new ReverenceItem(new Item.Settings()
            .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Antiquities.MOD_ID, "dormant_reverence")))
            .maxCount(1).attributeModifiers(ReverenceItem.createAttributeModifiers()).maxDamage(2031).enchantable(10).rarity(Rarity.EPIC)
    ));

    @SuppressWarnings("all")
    public static final Item WARHORN = registerItem("warhorn", new GoatHornItem(new Item.Settings()
            .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Antiquities.MOD_ID, "warhorn")))
            .maxCount(1).rarity(Rarity.UNCOMMON)
            .component(DataComponentTypes.INSTRUMENT, new InstrumentComponent(ModInstruments.WARHORN))
    ));

    public static final Item PALE_WARDEN_STATUE = registerItem("pale_warden_statue", new PaleWardenSpawnEggItem(ModEntities.PALE_WARDEN, new Item.Settings()
            .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Antiquities.MOD_ID, "pale_warden_statue")))
            .maxCount(64)
    ));

    public static final Item ILLUSIONER_SPAWN_EGG = registerItem("illusioner_spawn_egg", new SpawnEggItem(ModEntities.ILLUSIONER, new Item.Settings()
            .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Antiquities.MOD_ID, "illusioner_spawn_egg")))
            .maxCount(64)
    ));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(Antiquities.MOD_ID, name), item);
    }

    public static void initialize() {
        Antiquities.LOGGER.info("Antiquities Items Initialized");

        ItemTooltipCallback.EVENT.register((itemStack, tooltipContext, tooltipType, list) -> {
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
                if (itemStack.get(net.hollowed.combatamenities.util.items.ModComponents.INTEGER_PROPERTY) != null) {
                    int toolInt = Objects.requireNonNull(itemStack.get(net.hollowed.combatamenities.util.items.ModComponents.INTEGER_PROPERTY));

                    assert Formatting.GRAY.getColorValue() != null;
                    Text line = Text.translatable("item.antique.myriad_tool.no_tool").withColor(Formatting.GRAY.getColorValue());

                    switch (toolInt) {
                        case 1 -> line = Text.translatable("item.antique.myriad_tool.mattock").withColor(Formatting.GRAY.getColorValue());
                        case 2 -> line = Text.translatable("item.antique.myriad_tool.axe").withColor(Formatting.GRAY.getColorValue());
                        case 3 -> line = Text.translatable("item.antique.myriad_tool.shovel").withColor(Formatting.GRAY.getColorValue());
                        case 4 -> line = Text.translatable("item.antique.myriad_tool.cleaver").withColor(Formatting.GRAY.getColorValue());
                    }

                    // Add to tooltip
                    list.add(1, line);
                }
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
                text = text.copy().append(itemStack.getOrDefault(ModComponents.MYRIAD_STACK, ItemStack.EMPTY).getItemName());
                text = text.copy().append(" -");
                list.add(1, text.getString().equals(" - Air -") ? Text.translatable("item.antique.myriad_staff.empty") : text);
            }
        });
    }
}
