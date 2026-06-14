package net.hollowed.antique.items;

import net.hollowed.antique.index.AntiqueDataComponentTypes;
import net.hollowed.antique.util.ClothUtil;
import net.hollowed.antique.util.resources.ClothSkinData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.NonNull;

public class ClothItem extends Item {
    public ClothItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean overrideOtherStackedOnMe(@NonNull ItemStack itemStack, @NonNull ItemStack otherStack, @NonNull Slot slot, @NonNull ClickAction clickAction, @NonNull Player player, @NonNull SlotAccess slotAccess) {
        if (clickAction == ClickAction.SECONDARY) {
            if (otherStack.getItem() instanceof ClothPatternItem) {
                ClothUtil.setClothPattern(itemStack, ClothUtil.getClothPattern(otherStack));
                player.playSound(SoundEvents.BOOK_PAGE_TURN, 1.0F, 1.0F); // TODO better sound
                return true;
            }
        }

        return super.overrideOtherStackedOnMe(itemStack, otherStack, slot, clickAction, player, slotAccess);
    }

    @Override
    public @NonNull InteractionResult use(@NonNull Level level, @NonNull Player player, @NonNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.getOrDefault(DataComponents.DYED_COLOR, new DyedItemColor(0xFFFFFF)).rgb() != 0xFFFFFF || stack.get(AntiqueDataComponentTypes.CLOTH_PATTERN_TYPE) != null || stack.get(AntiqueDataComponentTypes.CLOTH_PATTERN_COLOR) != null) {
            BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);

            if (hit.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = hit.getBlockPos();
                Direction dir = hit.getDirection();
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
                        stack.remove(AntiqueDataComponentTypes.CLOTH_PATTERN_TYPE);
                        stack.remove(AntiqueDataComponentTypes.CLOTH_PATTERN_COLOR);

                        return InteractionResult.SUCCESS;
                    }
                }
            }
        }

        return super.use(level, player, hand);
    }

    @Override
    public @NonNull Component getName(@NonNull ItemStack stack) {
        ResourceKey<ClothSkinData> cloth = stack.get(AntiqueDataComponentTypes.CLOTH_TYPE);

        if (cloth == null) {
            return super.getName(stack);
        }

        return Component.translatable(ClothSkinData.getTranslationKey(cloth));
    }
}
