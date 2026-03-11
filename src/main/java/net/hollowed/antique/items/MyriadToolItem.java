package net.hollowed.antique.items;

import net.hollowed.antique.Antiquities;
import net.hollowed.antique.index.AntiqueDataComponentTypes;
import net.hollowed.antique.index.AntiqueItems;
import net.hollowed.antique.items.components.MyriadToolComponent;
import net.hollowed.antique.util.resources.ClientClothData;
import net.hollowed.antique.util.resources.ClothSkinData;
import net.hollowed.combatamenities.util.items.CAComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class MyriadToolItem extends Item {

    public MyriadToolItem(Properties settings) {
        super(settings);
    }

    /*
        Stack setting functions
     */

    public static ItemAttributeModifiers createAttributeModifiers(double damage, double attackSpeed, double reach) {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, damage - 1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -4 + attackSpeed, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(Identifier.withDefaultNamespace("base_attack_range"), reach, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build();
    }

    @Override
    public boolean overrideStackedOnOther(@NotNull ItemStack stack, Slot slot, @NotNull ClickAction clickType, @NotNull Player player) {
        ItemStack storedStack = getStoredStack(stack);
        ItemStack otherStack = slot.getItem();
        if (clickType == ClickAction.SECONDARY) {
            if (otherStack.isEmpty()) {

                // Remove the internal selected stack :3
                if (!storedStack.isEmpty()) {
                    slot.setByPlayer(storedStack.copy());
                    storedStack = ItemStack.EMPTY;
                    player.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 1.0F);
                    setStoredStack(stack, storedStack);
                    return true;
                }
            }
        } else {
            if (otherStack.is(AntiqueItems.CLOTH)) {
                slot.setByPlayer(swapCloth(player, stack, otherStack));
                return true;
            }

            if (otherStack.is(AntiqueItems.CLOTH_PATTERN) && !stack.getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, Antiquities.getDefaultMyriadTool()).clothType().isEmpty()) {
                addPattern(player, stack, otherStack);
                return true;
            }

            if (otherStack.isEmpty()) {
                return false;
            }

            // Check if the item being added is invalid
            if (isInvalidItem(otherStack)) {
                return false;
            }

            if (storedStack.isEmpty()) {
                storedStack = otherStack.split(otherStack.getCount());
                player.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 1.0F);
                setStoredStack(stack, storedStack); // Re-set without empty stacks

                // Clear the cursor stack after adding an item to the tool
                slot.setByPlayer(ItemStack.EMPTY);
                return true;
            }
        }
        return super.overrideStackedOnOther(stack, slot, clickType, player);
    }

    @Override
    public boolean overrideOtherStackedOnMe(@NotNull ItemStack stack, @NotNull ItemStack otherStack, @NotNull Slot slot, @NotNull ClickAction clickType, @NotNull Player player, @NotNull SlotAccess cursorStackReference) {
        ItemStack storedStack = getStoredStack(stack);
        if (clickType == ClickAction.SECONDARY) {
            if (otherStack.isEmpty()) {

                // :3
                if (!storedStack.isEmpty()) {
                    cursorStackReference.set(storedStack.copy());
                    storedStack = ItemStack.EMPTY;
                    player.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 1.0F, 1.0F);
                    setStoredStack(stack, storedStack);
                    return true;
                } else if (!stack.getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, Antiquities.getDefaultMyriadTool()).clothType().isEmpty()) {
                    cursorStackReference.set(swapCloth(player, stack, otherStack));
                    return true;
                }
            }
        } else {
            if (otherStack.is(AntiqueItems.CLOTH)) {
                cursorStackReference.set(swapCloth(player, stack, otherStack));
                return true;
            }

            if (otherStack.is(AntiqueItems.CLOTH_PATTERN) && !stack.getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, Antiquities.getDefaultMyriadTool()).clothType().isEmpty()) {
                addPattern(player, stack, otherStack);
                return true;
            }

            if (cursorStackReference.get().isEmpty()) {
                return false;
            }

            if (isInvalidItem(otherStack)) {
                return false;
            }

            ItemStack temp = getStoredStack(stack);
            storedStack = otherStack.split(otherStack.getCount());
            player.playSound(SoundEvents.BUNDLE_INSERT, 1.0F, 1.0F);
            setStoredStack(stack, storedStack);
            cursorStackReference.set(temp);
            return true;
        }
        return super.overrideOtherStackedOnMe(stack, otherStack, slot, clickType, player, cursorStackReference);
    }

    private void addPattern(Player player, ItemStack toolStack, ItemStack patternStack) {
        MyriadToolComponent component = toolStack.getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, Antiquities.getDefaultMyriadTool());

        if (ClientClothData.getTransform(component.clothType()).overlay()) {
            String pattern = "item.antique.cloth_pattern";
            Component text = patternStack.getOrDefault(DataComponents.ITEM_NAME, Component.translatable("item.antique.cloth_pattern"));
            if (text.getContents() instanceof TranslatableContents translatable) {
                pattern = translatable.getKey();
            }
            pattern = pattern.substring(pattern.indexOf(".") + 1);
            pattern = pattern.replace(".", ":");
            pattern = pattern.substring(0, pattern.indexOf("_"));

            toolStack.set(AntiqueDataComponentTypes.MYRIAD_TOOL, new MyriadToolComponent(
                    component.toolBit(),
                    component.clothType(),
                    pattern,
                    component.clothColor(),
                    patternStack.getOrDefault(DataComponents.DYED_COLOR, new DyedItemColor(0xFFFFFF)).rgb()
            ));
            toolStack.set(CAComponents.BOOLEAN_PROPERTY, patternStack.getOrDefault(CAComponents.BOOLEAN_PROPERTY, false));

            player.playSound(SoundEvents.DYE_USE, 1.0F, 1.0F);
        } else {
            player.playSound(SoundEvents.BUNDLE_INSERT_FAIL, 1.0F, 1.0F);
        }
    }

    private ItemStack swapCloth(Player player, ItemStack toolStack, ItemStack clothStack) {
        MyriadToolComponent component = toolStack.getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, Antiquities.getDefaultMyriadTool());

        ClothSkinData.ClothSubData toolData = ClientClothData.getTransform(String.valueOf(component.clothType()));
        boolean remove = false;

        if (!clothStack.isEmpty()) {
            String model = "item.antique.cloth";
            Component text = clothStack.getOrDefault(DataComponents.ITEM_NAME, Component.translatable("item.antique.cloth"));
            if (text.getContents() instanceof TranslatableContents translatable) {
                model = translatable.getKey();
            }
            model = model.substring(model.indexOf(".") + 1).replace(".", ":");
            DyedItemColor clothColor = clothStack.getOrDefault(DataComponents.DYED_COLOR, new DyedItemColor(0xD43B69));

            if (String.valueOf(component.clothType()).isEmpty()) remove = true;
            ClothSkinData.ClothSubData clothData = ClientClothData.getTransform(model);
            int intValue = 0;
            try {
                if (!clothData.hex().isBlank()) {
                    intValue = Integer.parseInt(clothData.hex(), 16);
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid hexadecimal string format: " + e.getMessage());
            }

            if (toolData.dyeable()) {
                clothStack.set(DataComponents.DYED_COLOR, new DyedItemColor(component.clothColor()));
            } else {
                clothStack.remove(DataComponents.DYED_COLOR);
            }

            toolStack.set(AntiqueDataComponentTypes.MYRIAD_TOOL, new MyriadToolComponent(
                    component.toolBit(),
                    model,
                    component.clothPattern(),
                    clothData.dyeable() ? clothColor.rgb() : intValue,
                    component.patternColor()
            ));

        } else {
            toolStack.set(AntiqueDataComponentTypes.MYRIAD_TOOL, new MyriadToolComponent(
                    component.toolBit(),
                    "",
                    component.clothPattern(),
                    0xFFFFFF,
                    component.patternColor()
            ));

            clothStack = AntiqueItems.CLOTH.getDefaultInstance();

            if (toolData.dyeable()) {
                clothStack.set(DataComponents.DYED_COLOR, new DyedItemColor(component.clothColor()));
            } else {
                clothStack.remove(DataComponents.DYED_COLOR);
            }
        }

        clothStack.set(DataComponents.ITEM_NAME, Component.translatable("item." + String.valueOf(component.clothType()).replace(":", ".")));

        component = toolStack.getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, Antiquities.getDefaultMyriadTool());

        toolStack.set(AntiqueDataComponentTypes.MYRIAD_TOOL, new MyriadToolComponent(
                component.toolBit(),
                component.clothType(),
                "",
                component.clothColor(),
                0xFFFFFF
        ));
        player.playSound(SoundEvents.BUNDLE_INSERT, 1.0F, 1.0F);

        return remove ? ItemStack.EMPTY : clothStack;
    }

    public static boolean isInvalidItem(ItemStack stack) {
        Item item = stack.getItem();
        return !(item instanceof MyriadToolBitItem);
    }

    public static ItemStack getStoredStack(ItemStack tool) {
        return tool.getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, Antiquities.getDefaultMyriadTool()).toolBit();
    }

    public static void setStoredStack(ItemStack tool, ItemStack newStack) {
        MyriadToolComponent component = tool.getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, Antiquities.getDefaultMyriadTool());
        if (newStack.getItem() instanceof MyriadToolBitItem item) {
            item.setToolAttributes(tool);
        } else {
            tool.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
                    .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 2.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -2.2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .build());
            tool.remove(DataComponents.TOOL);
            tool.remove(DataComponents.WEAPON);
            tool.remove(net.hollowed.combatamenities.util.items.CAComponents.INTEGER_PROPERTY);
        }
        tool.set(AntiqueDataComponentTypes.MYRIAD_TOOL, new MyriadToolComponent(newStack, component.clothType(), component.clothPattern(), component.clothColor(), component.patternColor()));
        if (!newStack.isEmpty()) {
            String rawId = BuiltInRegistries.ITEM.getKey(newStack.getItem()).toString();
            Identifier identifier = Identifier.parse(rawId.substring(0, rawId.lastIndexOf("_")));
            tool.set(DataComponents.ITEM_MODEL, identifier);
        } else {
            tool.set(DataComponents.ITEM_MODEL, Antiquities.id("myriad_tool"));
        }
    }

    /*
        Tool functionality
     */

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        if (context.getItemInHand().getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, Antiquities.getDefaultMyriadTool()).toolBit().getItem() instanceof MyriadToolBitItem item) {
            return item.toolUseOnBlock(context);
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, @NotNull Level world, @NotNull LivingEntity user, int remainingUseTicks) {
        if (stack.getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, Antiquities.getDefaultMyriadTool()).toolBit().getItem() instanceof MyriadToolBitItem item) {
            return item.toolOnStoppedUsing(stack, world, user, remainingUseTicks);
        }
        return super.releaseUsing(stack, world, user, remainingUseTicks);
    }

    @Override
    public @NotNull ItemUseAnimation getUseAnimation(ItemStack stack) {
        if (stack.getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, Antiquities.getDefaultMyriadTool()).toolBit().getItem() instanceof MyriadToolBitItem item) {
            return item.toolGetUseAction(stack);
        }
        return super.getUseAnimation(stack);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level world, Player user, @NotNull InteractionHand hand) {
        if (user.getItemInHand(hand).getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, Antiquities.getDefaultMyriadTool()).toolBit().getItem() instanceof MyriadToolBitItem item) {
            return item.toolUse(world, user, hand);
        }
        return InteractionResult.PASS;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity user) {
        return 72000;
    }
}