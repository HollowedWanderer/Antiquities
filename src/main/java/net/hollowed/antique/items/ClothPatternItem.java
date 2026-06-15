package net.hollowed.antique.items;

import net.hollowed.antique.util.ClothUtil;
import net.hollowed.antique.util.resources.ClothPatternData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
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
        if (clickType == ClickAction.PRIMARY) {
            if (otherStack.is(Items.INK_SAC) || otherStack.is(Items.GLOW_INK_SAC)) {
                addInk(player, stack, otherStack);
                return true;
            }
        }

        return super.overrideOtherStackedOnMe(stack, otherStack, slot, clickType, player, cursorStackReference);
    }

    @Override
    public @NonNull InteractionResult useOn(@NonNull UseOnContext context) {
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        Level level = context.getLevel();

        if (player != null && stack.getOrDefault(DataComponents.DYED_COLOR, new DyedItemColor(0xFFFFFF)).rgb() != 0xFFFFFF) {

            BlockPos pos = context.getClickedPos();
            Direction dir = context.getClickedFace();
            BlockPos sidePos = pos.relative(dir);

            if (!level.mayInteract(player, pos) || !player.mayUseItemAt(sidePos, dir, stack)) {
                return InteractionResult.FAIL;
            }

            BlockState state = level.getBlockState(pos);

            if (state.is(Blocks.WATER_CAULDRON)) {
                int waterLevel = state.getValue(LayeredCauldronBlock.LEVEL);

                if (waterLevel > 0) {
                    if (waterLevel == 1) {
                        level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), Block.UPDATE_ALL);
                    } else {
                        level.setBlock(pos, state.setValue(LayeredCauldronBlock.LEVEL, waterLevel - 1), Block.UPDATE_ALL);
                    }

                    stack.set(DataComponents.DYED_COLOR, new DyedItemColor(0xFFFFFF));

                    return InteractionResult.SUCCESS;
                }
            }
        }

        return super.useOn(context);
    }

    private void addInk(Player player, ItemStack clothStack, ItemStack inkStack) {
        boolean glow = inkStack.is(Items.GLOW_INK_SAC);
        if (glow != ClothUtil.getClothPatternGlowing(clothStack)) {
            ClothUtil.setClothPatternGlowing(clothStack, glow);
            player.playSound(inkStack.is(Items.GLOW_INK_SAC) ? SoundEvents.GLOW_INK_SAC_USE : SoundEvents.INK_SAC_USE, 1.0F, 1.0F);
            inkStack.consume(1, player);
        }
    }
}
