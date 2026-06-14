package net.hollowed.antique.items;

import net.hollowed.antique.util.ClothUtil;
import net.hollowed.antique.util.resources.ClothPatternData;
import net.hollowed.combatamenities.util.items.CAComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class ClothPatternItem extends Item {
    public ClothPatternItem(Properties settings) {
        super(settings);
    }

    @Override
    public @NonNull Component getName(@NonNull ItemStack stack) {
        return ClothUtil.getClothPattern(stack)
                .<Component>map(cloth -> Component.translatable(ClothPatternData.getTranslationKey(cloth)))
                .orElseGet(() -> super.getName(stack));
    }

    @Override
    public boolean overrideOtherStackedOnMe(@NotNull ItemStack stack, @NotNull ItemStack otherStack, @NotNull Slot slot, @NotNull ClickAction clickType, @NotNull Player player, @NotNull SlotAccess cursorStackReference) {
        if (clickType == ClickAction.SECONDARY) {
            if (otherStack.is(Items.INK_SAC) || otherStack.is(Items.GLOW_INK_SAC)) {
                addInk(player, stack, otherStack);
                return true;
            }
        }

        return super.overrideOtherStackedOnMe(stack, otherStack, slot, clickType, player, cursorStackReference);
    }

    private void addInk(Player player, ItemStack patternStack, ItemStack inkStack) {
        patternStack.set(CAComponents.BOOLEAN_PROPERTY, inkStack.is(Items.GLOW_INK_SAC));
        player.playSound(inkStack.is(Items.GLOW_INK_SAC) ? SoundEvents.GLOW_INK_SAC_USE : SoundEvents.INK_SAC_USE, 1.0F, 1.0F);
        inkStack.consume(1, player);
    }
}
