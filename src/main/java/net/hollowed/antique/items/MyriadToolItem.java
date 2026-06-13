package net.hollowed.antique.items;

import net.hollowed.antique.Antiquities;
import net.hollowed.antique.index.AntiqueDataComponentTypes;
import net.hollowed.antique.index.AntiqueItems;
import net.hollowed.antique.util.resources.ClothInstance;
import net.hollowed.antique.items.components.MyriadToolComponent;
import net.hollowed.antique.util.resources.ClothPatternData;
import net.hollowed.antique.util.resources.ClothSkinData;
import net.hollowed.combatamenities.util.items.CAComponents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
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
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

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
                    setToolBit(stack, storedStack);
                    return true;
                }
            }
        } else {
            if (otherStack.get(AntiqueDataComponentTypes.CLOTH_TYPE) != null || otherStack.get(AntiqueDataComponentTypes.CLOTH_PATTERN_TYPE) != null) {
                Optional<ItemStack> clothResult = swapCloth(player, stack, otherStack);

                if (clothResult.isPresent()) {
                    slot.setByPlayer(clothResult.get());
                    player.playSound(SoundEvents.BUNDLE_INSERT, 1.0F, 1.0F);
                    return true;
                }
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
                setToolBit(stack, storedStack); // Re-set without empty stacks

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
                    setToolBit(stack, storedStack);
                    return true;
                } else if (stack.getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, MyriadToolComponent.DEFAULT_NO_CLOTH).cloth().isPresent()) {
                    cursorStackReference.set(swapCloth(player, stack, otherStack).orElse(otherStack));
                    player.playSound(SoundEvents.BUNDLE_INSERT, 1.0F, 1.0F);
                    return true;
                }
            }
        } else {
            if (otherStack.get(AntiqueDataComponentTypes.CLOTH_TYPE) != null || otherStack.get(AntiqueDataComponentTypes.CLOTH_PATTERN_TYPE) != null) {
                Optional<ItemStack> clothResult = swapCloth(player, stack, otherStack);

                if (clothResult.isPresent()) {
                    cursorStackReference.set(clothResult.get());
                    player.playSound(SoundEvents.BUNDLE_INSERT, 1.0F, 1.0F);
                    return true;
                }
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
            setToolBit(stack, storedStack);
            cursorStackReference.set(temp);
            return true;
        }
        return super.overrideOtherStackedOnMe(stack, otherStack, slot, clickType, player, cursorStackReference);
    }

    private Optional<ItemStack> swapCloth(Player player, ItemStack toolStack, ItemStack insertStack) {
        if (toolStack.isEmpty() && insertStack.isEmpty()) {
            return Optional.empty();
        }

        MyriadToolComponent tool = toolStack.getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, MyriadToolComponent.DEFAULT_NO_CLOTH);

        if (insertStack.isEmpty()) {
            return Optional.of(tool.removeClothOrPattern(toolStack));
        } else {
            return Optional.ofNullable(insertStack.get(AntiqueDataComponentTypes.CLOTH_TYPE)).map(skin -> {
                ItemStack ret = tool.removeCloth(null);

                toolStack.set(AntiqueDataComponentTypes.MYRIAD_TOOL, tool.withCloth(Optional.of(tool.cloth().map(cloth -> cloth.withCloth(insertStack)).orElseGet(() -> new ClothInstance(insertStack)))));

                return ret;
            }).or(() -> Optional.ofNullable(insertStack.get(AntiqueDataComponentTypes.CLOTH_PATTERN_TYPE)).flatMap(pattern -> tool.cloth().flatMap(cloth -> {
                if (!ClothSkinData.get(cloth.cloth(), player.level().registryAccess()).patternable()) {
                    return Optional.empty();
                }

                ItemStack ret = tool.removePattern(null);

                toolStack.set(AntiqueDataComponentTypes.MYRIAD_TOOL, tool.withCloth(Optional.of(cloth.withPattern(insertStack))));

                return Optional.of(ret);
            })));
        }
    }

    public static boolean isInvalidItem(ItemStack stack) {
        Item item = stack.getItem();
        return !(item instanceof MyriadToolBitItem);
    }

    public static ItemStack getStoredStack(ItemStack tool) {
        return tool.getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, MyriadToolComponent.DEFAULT_NO_CLOTH).toolBit();
    }

    public static void setToolBit(ItemStack toolStack, ItemStack toolBit) {
        MyriadToolComponent component = toolStack.getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, MyriadToolComponent.DEFAULT_NO_CLOTH);

        if (toolBit.getItem() instanceof MyriadToolBitItem item) {
            item.setToolAttributes(toolStack);
        } else {
            toolStack.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
                    .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 2.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -2.2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .build());
            toolStack.remove(DataComponents.TOOL);
            toolStack.remove(DataComponents.WEAPON);
            toolStack.remove(CAComponents.INTEGER_PROPERTY);
        }

        toolStack.set(AntiqueDataComponentTypes.MYRIAD_TOOL, component.withToolBit(toolBit));

        if (!toolBit.isEmpty()) {
            String rawId = BuiltInRegistries.ITEM.getKey(toolBit.getItem()).toString();
            Identifier identifier = Identifier.parse(rawId.substring(0, rawId.lastIndexOf("_")));
            toolStack.set(DataComponents.ITEM_MODEL, identifier);
        } else {
            toolStack.set(DataComponents.ITEM_MODEL, Antiquities.id("myriad_tool"));
        }
    }

    /*
        Tool functionality
     */

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        if (context.getItemInHand().getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, MyriadToolComponent.DEFAULT_NO_CLOTH).toolBit().getItem() instanceof MyriadToolBitItem item) {
            return item.toolUseOnBlock(context);
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, @NotNull Level world, @NotNull LivingEntity user, int remainingUseTicks) {
        if (stack.getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, MyriadToolComponent.DEFAULT_NO_CLOTH).toolBit().getItem() instanceof MyriadToolBitItem item) {
            return item.toolOnStoppedUsing(stack, world, user, remainingUseTicks);
        }
        return super.releaseUsing(stack, world, user, remainingUseTicks);
    }

    @Override
    public @NotNull ItemUseAnimation getUseAnimation(ItemStack stack) {
        if (stack.getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, MyriadToolComponent.DEFAULT_NO_CLOTH).toolBit().getItem() instanceof MyriadToolBitItem item) {
            return item.toolGetUseAction(stack);
        }
        return super.getUseAnimation(stack);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level world, Player user, @NotNull InteractionHand hand) {
        if (user.getItemInHand(hand).getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, MyriadToolComponent.DEFAULT_NO_CLOTH).toolBit().getItem() instanceof MyriadToolBitItem item) {
            return item.toolUse(world, user, hand);
        }
        return InteractionResult.PASS;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity user) {
        return 72000;
    }
}