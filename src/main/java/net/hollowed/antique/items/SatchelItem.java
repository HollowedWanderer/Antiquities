package net.hollowed.antique.items;

import net.hollowed.antique.enchantments.EnchantmentListener;
import net.hollowed.antique.index.AdventureArmorMaterial;
import net.hollowed.antique.index.AntiqueDataComponentTypes;
import net.hollowed.antique.items.tooltips.SatchelTooltipData;
import net.hollowed.combatamenities.util.items.CAComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ARGB;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SatchelItem extends Item {
    public static final int MAX_STACKS = 8;
    public static int index = 0;
    public static List<ItemStack> lastContents = null;

    private static final int FULL_ITEM_BAR_COLOR = ARGB.colorFromFloat(1.0F, 1.0F, 0.33F, 0.33F);
    private static final int ITEM_BAR_COLOR = ARGB.colorFromFloat(1.0F, 0.44F, 0.53F, 1.0F);

    public SatchelItem(AdventureArmorMaterial material, ArmorType type, Properties settings) {
        super(material.applySettings(settings, type));
    }

    public boolean isBarVisible(@NotNull ItemStack stack) {
        List<ItemStack> storedStacks = new ArrayList<>(getStoredStacks(stack));
        return !storedStacks.isEmpty();
    }

    public int getBarWidth(@NotNull ItemStack stack) {
        List<ItemStack> storedStacks = new ArrayList<>(getStoredStacks(stack));

        int maxStacks = 8;
        return Math.round((float) storedStacks.size() / maxStacks * 13);
    }

    public int getBarColor(@NotNull ItemStack stack) {
        List<ItemStack> storedStacks = new ArrayList<>(getStoredStacks(stack));
        return storedStacks.size() == 8 ? FULL_ITEM_BAR_COLOR : ITEM_BAR_COLOR;
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level world, Player user, @NotNull InteractionHand hand) {
        if (EnchantmentListener.hasEnchantment(user.getItemInHand(hand), "antique:jumbling")) {
            return InteractionResult.FAIL;
        }
        return super.use(world, user, hand);
    }

    public @NotNull InteractionResult useOn(UseOnContext context) {
        InteractionHand hand = context.getHand();
        Player player = context.getPlayer();
        ItemStack stack = Objects.requireNonNull(player).getItemInHand(hand);

        if (EnchantmentListener.hasEnchantment(stack, "antique:jumbling")) {
            List<ItemStack> satchelStacks = stack.getOrDefault(AntiqueDataComponentTypes.SATCHEL_STACK, List.of());
            if (!satchelStacks.isEmpty()) {
                List<ItemStack> blockItems = new ArrayList<>(satchelStacks.stream()
                        .filter(itemStack -> itemStack.getItem() instanceof BlockItem)
                        .toList());

                if (!blockItems.isEmpty()) {
                    boolean hasNonBlockItem = blockItems.stream()
                            .anyMatch(itemStack -> !(itemStack.getItem() instanceof BlockItem));
                    if (hasNonBlockItem) {
                        return InteractionResult.PASS;
                    }

                    int randomIndex = new Random().nextInt(blockItems.size());

                    ItemStack selectedBlockStack = blockItems.get(randomIndex);
                    if (selectedBlockStack != null) {
                        boolean canPlace = placeBlockFromSatchel(context, selectedBlockStack);
                        int counter = 0;
                        while (!canPlace && counter < blockItems.size()) {
                            counter++;
                            randomIndex += randomIndex == blockItems.size() - 1 ? -(blockItems.size() - 1) : 1;
                            selectedBlockStack = blockItems.get(randomIndex);
                            canPlace = placeBlockFromSatchel(context, selectedBlockStack);
                        }
                        if (!canPlace) {
                            return InteractionResult.FAIL;
                        }
                        blockItems.set(randomIndex, selectedBlockStack.copyWithCount(selectedBlockStack.getCount() - (player.isCreative() ? 0 : 1)));
                        if (blockItems.get(randomIndex).isEmpty()) blockItems.remove(randomIndex);
                        stack.set(AntiqueDataComponentTypes.SATCHEL_STACK, blockItems);
                    }
                }
            }
            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.PASS;
        }
    }

    public boolean placeBlockFromSatchel(UseOnContext context, ItemStack stack) {
        BlockItem blockItem = (BlockItem) stack.getItem();
        BlockPlaceContext placementContext = new BlockPlaceContext(context);
        if (!placementContext.getLevel().isClientSide()) {
            InteractionResult result = blockItem.place(placementContext);
            BlockPos pos = placementContext.getClickedPos();
            BlockState state = placementContext.getLevel().getBlockState(pos);
            SoundType sound = state.getSoundType();

            if (result.consumesAction()) {
                placementContext.getLevel().playSound(null, pos, sound.getPlaceSound(), SoundSource.BLOCKS,
                        (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
                Objects.requireNonNull(context.getPlayer()).swing(context.getHand(), true);
                return true;
            }
        }
        return false;
    }

    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        TooltipDisplay tooltipDisplayComponent = stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
        return !tooltipDisplayComponent.shows(AntiqueDataComponentTypes.SATCHEL_STACK)
                ? Optional.empty()
                : Optional.ofNullable(stack.get(AntiqueDataComponentTypes.SATCHEL_STACK)).map(items -> new SatchelTooltipData(items, stack));
    }

    @Override
    public void onDestroyed(ItemEntity entity) {
        List<ItemStack> contents = entity.getItem().get(AntiqueDataComponentTypes.SATCHEL_STACK);
        if (contents != null) {
            entity.getItem().set(AntiqueDataComponentTypes.SATCHEL_STACK, List.of());
            ItemUtils.onContainerDestroyed(entity, contents);
        }
    }

    @Override
    public boolean allowComponentsUpdateAnimation(@NotNull Player player, @NotNull InteractionHand hand, @NotNull ItemStack oldStack, @NotNull ItemStack newStack) {
        return false;
    }

    @Override
    public void inventoryTick(ItemStack stack, @NotNull ServerLevel world, @NotNull Entity entity, @Nullable EquipmentSlot slot) {
        if (stack.getOrDefault(AntiqueDataComponentTypes.COUNTER, 2) < 1) {
            stack.set(AntiqueDataComponentTypes.COUNTER, stack.getOrDefault(AntiqueDataComponentTypes.COUNTER, 1) + 1);
        } else {
            setInternalIndex(stack, -1);
        }
        super.inventoryTick(stack, world, entity, slot);
    }

    @Override
    public boolean overrideStackedOnOther(@NotNull ItemStack stack, Slot slot, @NotNull ClickAction clickType, @NotNull Player player) {
        List<ItemStack> storedStacks = new ArrayList<>(getStoredStacks(stack));  // Create a mutable copy of the list
        ItemStack otherStack = slot.getItem();

        if (clickType == ClickAction.SECONDARY) {
            if (otherStack.isEmpty()) {

                // Remove the internal selected stack :3
                if (!storedStacks.isEmpty() && !storedStacks.getFirst().isEmpty()) {
                    slot.setByPlayer(storedStacks.getFirst().copy());
                    storedStacks.set(0, ItemStack.EMPTY);
                    player.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 1.0F);
                    setStoredStacks(stack, storedStacks);
                    return true;
                }
            }
        } else {
            if (otherStack.isEmpty()) {
                return false;
            }

            if (isInvalidItem(otherStack)) {
                player.playSound(SoundEvents.BUNDLE_INSERT_FAIL, 0.8F, 1.0F);
                return true;
            }

            for (int i = 0; i < MAX_STACKS; i++) {
                if (i >= storedStacks.size()) {
                    storedStacks.add(ItemStack.EMPTY);
                }
            }

            int otherStackCount = otherStack.getCount();
            ItemStack remainder = addToStoredStacks(storedStacks, otherStack.copy());
            slot.safeTake(otherStackCount, otherStackCount - remainder.getCount(), player);
            if (remainder.getCount() == otherStackCount) {
                player.playSound(SoundEvents.BUNDLE_INSERT_FAIL, 0.8F, 1.0F);
            } else {
                player.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 1.0F);
            }
            setStoredStacks(stack, storedStacks);
            return true;
        }
        return super.overrideStackedOnOther(stack, slot, clickType, player);
    }

    @Override
    public boolean overrideOtherStackedOnMe(@NotNull ItemStack stack, @NotNull ItemStack otherStack, @NotNull Slot slot, @NotNull ClickAction clickType, @NotNull Player player, @NotNull SlotAccess cursorStackReference) {
        List<ItemStack> storedStacks = new ArrayList<>(getStoredStacks(stack));  // Create a mutable copy of the list

        if (clickType == ClickAction.SECONDARY) {
            if (!slot.allowModification(player)) {
                return true;
            }

            if (otherStack.isEmpty()) {

                int index = !hasSelectedStack(stack) ? 0 : getInternalIndex(stack);

                // Remove the internal selected stack :3
                if (!storedStacks.isEmpty() && !storedStacks.get(index).isEmpty()) {
                    cursorStackReference.set(storedStacks.get(index).copy());
                    storedStacks.set(index, ItemStack.EMPTY);
                    player.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 1.0F, 1.0F);
                    setStoredStacks(stack, storedStacks);
                    return true;
                }
            }
        } else {
            if (cursorStackReference.get().isEmpty()) {
                return false;
            }

            if (isInvalidItem(otherStack) || !slot.allowModification(player)) {
                player.playSound(SoundEvents.BUNDLE_INSERT_FAIL, 1.0F, 1.0F);
                return true;
            }

            for (int i = 0; i < MAX_STACKS; i++) {
                if (i >= storedStacks.size()) {
                    storedStacks.add(ItemStack.EMPTY);
                }
            }

            int otherStackCount = otherStack.getCount();
            ItemStack remainder = addToStoredStacks(storedStacks, otherStack);
            if (remainder.getCount() == otherStackCount) {
                player.playSound(SoundEvents.BUNDLE_INSERT_FAIL, 1.0F, 1.0F);
            } else {
                player.playSound(SoundEvents.BUNDLE_INSERT, 1.0F, 1.0F);
            }
            setStoredStacks(stack, storedStacks);
            return true;
        }
        return super.overrideOtherStackedOnMe(stack, otherStack, slot, clickType, player, cursorStackReference);
    }

    public static boolean hasSelectedStack(ItemStack stack) {
        return stack.getOrDefault(CAComponents.INTEGER_PROPERTY, -1) != -1;
    }

    public boolean isInvalidItem(ItemStack stack) {
        // Check if the item is a satchel, or shulker box
        Item item = stack.getItem();
        return item instanceof SatchelItem || item.getDescriptionId().contains("shulker_box");
    }

    public static ItemStack addToStoredStacks(List<ItemStack> storedStacks, ItemStack stack) {

        for (int i = 0; i < storedStacks.size(); i++) {
            ItemStack storedStack = storedStacks.get(i);
            int stackRoom = storedStack.getMaxStackSize() - storedStack.getCount();
            if (stackRoom > 0 && !storedStack.isEmpty()) {
                if (ItemStack.isSameItemSameComponents(storedStack, stack)) {
                    int stackAmount = Math.min(stack.getCount(), stackRoom);
                    storedStack.grow(stackAmount);
                    stack.shrink(stackAmount);
                    storedStacks.set(i, storedStack);

                    if (!stack.isEmpty()) {
                        if (storedStacks.size() < MAX_STACKS) {
                            storedStacks.add(stack.copy());
                            stack.setCount(0);
                            return ItemStack.EMPTY;
                        } else if (storedStacks.contains(ItemStack.EMPTY)) {
                            storedStacks.set(storedStacks.indexOf(ItemStack.EMPTY), stack.copy());
                            stack.setCount(0);
                            return ItemStack.EMPTY;
                        } else {
                            for (ItemStack checkStack : storedStacks) {
                                if (checkStack.getCount() < checkStack.getMaxStackSize() && ItemStack.isSameItemSameComponents(checkStack, stack)) {
                                    return addToStoredStacks(storedStacks, stack);
                                }
                            }
                        }
                    }
                }
            } else if (storedStack.isEmpty()) {
                storedStacks.set(i, stack.copy());
                stack.setCount(0);
                return ItemStack.EMPTY;
            }
        }

        if (storedStacks.size() < MAX_STACKS && storedStacks.stream().noneMatch(ItemStack::isEmpty)) {
            storedStacks.add(stack.copy());
            stack.setCount(0);
            return ItemStack.EMPTY;
        }

        return stack;
    }

    public static List<ItemStack> getStoredStacks(ItemStack satchel) {
        List<ItemStack> storedStacks = satchel.get(AntiqueDataComponentTypes.SATCHEL_STACK);
        return storedStacks != null ? storedStacks : new ArrayList<>();
    }

    public ItemStack getSelectedStack(ItemStack stack) {
        if (!Objects.requireNonNull(stack.get(AntiqueDataComponentTypes.SATCHEL_STACK)).isEmpty()
                && index < Objects.requireNonNull(stack.get(AntiqueDataComponentTypes.SATCHEL_STACK)).size()) {
            return Objects.requireNonNull(stack.get(AntiqueDataComponentTypes.SATCHEL_STACK)).get(index);
        }
        return ItemStack.EMPTY;
    }

    public void setSlot(ItemStack satchel, ItemStack otherStack) {
        List<ItemStack> storedStacks = new ArrayList<>(getStoredStacks(satchel));
        if (!Objects.requireNonNull(satchel.get(AntiqueDataComponentTypes.SATCHEL_STACK)).isEmpty()
                && index < Objects.requireNonNull(satchel.get(AntiqueDataComponentTypes.SATCHEL_STACK)).size()) {
            storedStacks.set(index, otherStack);
        } else if (Objects.requireNonNull(satchel.get(AntiqueDataComponentTypes.SATCHEL_STACK)).size() < 8 && index >= Objects.requireNonNull(satchel.get(AntiqueDataComponentTypes.SATCHEL_STACK)).size()) {
            storedStacks.add(otherStack);
        }
        setStoredStacks(satchel, storedStacks);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        SatchelItem.index = index;
    }

    public static void setInternalIndex(ItemStack stack, int internalIndex) {
        stack.set(CAComponents.INTEGER_PROPERTY, internalIndex);
    }

    public static int getInternalIndex(ItemStack stack) {
        return stack.getOrDefault(CAComponents.INTEGER_PROPERTY, -1);
    }

    public static void setStoredStacks(ItemStack satchel, List<ItemStack> stacks) {
        List<ItemStack> filteredStacks = new ArrayList<>();
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                filteredStacks.add(stack.copy());
            }
        }

        satchel.set(AntiqueDataComponentTypes.SATCHEL_STACK, filteredStacks);
    }
}