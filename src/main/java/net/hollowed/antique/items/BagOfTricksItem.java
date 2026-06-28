package net.hollowed.antique.items;

import net.hollowed.antique.Antiquities;
import net.hollowed.antique.index.AntiqueDataComponentTypes;
import net.hollowed.antique.items.tooltips.BagOfTricksTooltipData;
import net.hollowed.combatamenities.util.items.CAComponents;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BagOfTricksItem extends Item {
    public static final int MAX_STACKS = 8;
    public static int index = 0;
    public static List<ItemStack> lastContents = null;

    private static final int FULL_ITEM_BAR_COLOR = ARGB.colorFromFloat(1.0F, 1.0F, 0.33F, 0.33F);
    private static final int ITEM_BAR_COLOR = ARGB.colorFromFloat(1.0F, 0.44F, 0.53F, 1.0F);

    public BagOfTricksItem(Properties settings) {
        super(settings);
    }

    public boolean isBarVisible(@NotNull ItemStack stack) {
        List<ItemStack> storedStacks = new ArrayList<>(getStoredStacks(stack));  // Create a mutable copy of the list
        return !storedStacks.isEmpty();
    }

    public int getBarWidth(@NotNull ItemStack stack) {
        List<ItemStack> storedStacks = new ArrayList<>(getStoredStacks(stack));  // Create a mutable copy of the list

        int maxStacks = 8;
        return Math.round((float) storedStacks.size() / maxStacks * 13);
    }

    public int getBarColor(@NotNull ItemStack stack) {
        List<ItemStack> storedStacks = new ArrayList<>(getStoredStacks(stack));  // Create a mutable copy of the list
        return storedStacks.size() == 8 ? FULL_ITEM_BAR_COLOR : ITEM_BAR_COLOR;
    }

    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        TooltipDisplay tooltipDisplayComponent = stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
        return !tooltipDisplayComponent.shows(AntiqueDataComponentTypes.SATCHEL_STACK)
                ? Optional.empty()
                : Optional.ofNullable(stack.get(AntiqueDataComponentTypes.SATCHEL_STACK)).map(items -> new BagOfTricksTooltipData(items, stack));
    }

    @Override
    public void onDestroyed(ItemEntity entity) {
        List<ItemStack> contents = entity.getItem().get(AntiqueDataComponentTypes.SATCHEL_STACK);
        if (contents != null) {
            entity.getItem().set(AntiqueDataComponentTypes.SATCHEL_STACK, List.of());
            ItemUtils.onContainerDestroyed(entity, contents.stream());
        }
    }

    @Override
    public boolean allowComponentsUpdateAnimation(@NotNull Player player, @NotNull InteractionHand hand, @NotNull ItemStack oldStack, @NotNull ItemStack newStack) {
        return false;
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
            ItemStack remainder = addToStoredStacks(storedStacks, otherStack);
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

            if (isInvalidItem(otherStack)) {
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

    @Override
    public @NotNull InteractionResult use(@NotNull Level world, Player user, @NotNull InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        List<ItemStack> stacks = stack.getOrDefault(AntiqueDataComponentTypes.SATCHEL_STACK, List.of());
        if (stacks.isEmpty() || stacks.getFirst().isEmpty()) return InteractionResult.PASS;

        if (stacks.getFirst().getItem() instanceof ProjectileItem projectileItem) {
            Projectile entity = projectileItem.asProjectile(world, user.getEyePosition(), stacks.getFirst(), Direction.DOWN);
            entity.setOwner(user);
            entity.setPos(user.getX(), user.getEyeY() - 0.10000000149011612, user.getZ());

            float f = -Mth.sin(user.getYRot() * 0.017453292F) * Mth.cos(user.getXRot() * 0.017453292F);
            float g = -Mth.sin(user.getXRot() * 0.017453292F);
            float h = Mth.cos(user.getYRot() * 0.017453292F) * Mth.cos(user.getXRot() * 0.017453292F);
            Vec3 throwDir = new Vec3(f, g, h).normalize();
            Vec3 playerVel = user.getDeltaMovement().normalize();
            double dot = throwDir.dot(playerVel); // Value between -1 and 1
            float strength = 1.0F;
            if (dot > 0.1) {
                strength += (float) (dot * 2.0);
            }
            entity.shoot(f, g, h, strength, 1.0F);
            world.addFreshEntity(entity);

            stacks.getFirst().consume(1, user);
            BagOfTricksItem.setStoredStacks(stack, stacks);
            return InteractionResult.SUCCESS;
        }
        return super.use(world, user, hand);
    }

    public static boolean hasSelectedStack(ItemStack stack) {
        return stack.getOrDefault(CAComponents.INTEGER_PROPERTY, -1) != -1;
    }

    public boolean isInvalidItem(ItemStack stack) {
        return !stack.is(TagKey.create(Registries.ITEM, Antiquities.id("bag_projectiles")));
    }

    public static List<ItemStack> getStoredStacks(ItemStack satchel) {
        List<ItemStack> storedStacks = satchel.get(AntiqueDataComponentTypes.SATCHEL_STACK);
        return storedStacks != null ? storedStacks : new ArrayList<>();
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        BagOfTricksItem.index = index;
    }

    public static void setInternalIndex(ItemStack stack, int internalIndex) {
        stack.set(CAComponents.INTEGER_PROPERTY, internalIndex);
    }

    public static int getInternalIndex(ItemStack stack) {
        return stack.getOrDefault(CAComponents.INTEGER_PROPERTY, -1);
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