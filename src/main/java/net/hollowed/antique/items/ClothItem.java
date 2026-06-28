package net.hollowed.antique.items;

import net.hollowed.antique.config.AntiquitiesConfig;
import net.hollowed.antique.entities.ClothEntity;
import net.hollowed.antique.index.AntiqueDataComponentTypes;
import net.hollowed.antique.index.AntiqueEntities;
import net.hollowed.antique.util.ClothUtil;
import net.hollowed.antique.util.resources.ClothSkinData;
import net.hollowed.antique.util.resources.SewnClothPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
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
import net.minecraft.world.level.gameevent.GameEvent;
import org.jspecify.annotations.NonNull;

public class ClothItem extends Item {
    public ClothItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean overrideOtherStackedOnMe(@NonNull ItemStack itemStack, @NonNull ItemStack otherStack, @NonNull Slot slot, @NonNull ClickAction clickAction, @NonNull Player player, @NonNull SlotAccess slotAccess) {
        if (clickAction == ClickAction.PRIMARY) {
            if (otherStack.getItem() instanceof ClothPatternItem && ClothUtil.getClothPatterns(itemStack).size() < AntiquitiesConfig.MAX_CLOTH_PATTERNS) {
                if (ClothUtil.getClothData(itemStack, player.level().registryAccess()).map(skin -> skin.value().patternable()).orElse(false)) {
                    ClothUtil.addClothPattern(itemStack, new SewnClothPattern(
                            ClothUtil.getClothPattern(otherStack).orElseThrow(),
                            ClothUtil.getClothPatternColor(otherStack),
                            ClothUtil.getClothPatternGlowing(otherStack)
                    ));
                    player.playSound(SoundEvents.BOOK_PAGE_TURN, 1.0F, 1.0F); // TODO better sound
                }
                return true;
            }
            if (otherStack.is(Items.INK_SAC) || otherStack.is(Items.GLOW_INK_SAC)) {
                if (itemStack.get(AntiqueDataComponentTypes.CLOTH_PATTERN_TYPE) != null) {
                    addInk(player, itemStack, otherStack);
                }
                return true;
            }
        }

        return super.overrideOtherStackedOnMe(itemStack, otherStack, slot, clickAction, player, slotAccess);
    }

    @Override
    public @NonNull InteractionResult useOn(@NonNull UseOnContext context) {
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        Level level = context.getLevel();

        BlockPos pos = context.getClickedPos();
        Direction dir = context.getClickedFace();
        BlockPos sidePos = pos.relative(dir);

        if (player == null) {
            return InteractionResult.FAIL;
        }

        if (!level.mayInteract(player, pos) || !player.mayUseItemAt(sidePos, dir, stack)) {
            return InteractionResult.FAIL;
        }

        BlockState state = level.getBlockState(pos);

        if (state.is(BlockTags.FENCES)) {
            ClothEntity entity = new ClothEntity(AntiqueEntities.CLOTH, level, pos.immutable(), stack.copyWithCount(1));
            entity.setPos(pos.getX(), pos.getY(), pos.getZ());
            level.addFreshEntity(entity);

            if (!player.hasInfiniteMaterials()) {
                stack.shrink(1);
            }

            entity.playSound(SoundEvents.LEAD_TIED, 1, 1);
            level.gameEvent(GameEvent.BLOCK_ATTACH, pos, GameEvent.Context.of(player));

            return InteractionResult.SUCCESS;
        }

        if (
                stack.getOrDefault(DataComponents.DYED_COLOR, new DyedItemColor(0xFFFFFF)).rgb() != 0xFFFFFF
                        || stack.get(AntiqueDataComponentTypes.SEWN_CLOTHS) != null
                        || stack.get(AntiqueDataComponentTypes.CLOTH_PATTERN_COLOR) != null
        ) {
            if (state.is(Blocks.WATER_CAULDRON)) {
                int waterLevel = state.getValue(LayeredCauldronBlock.LEVEL);

                if (waterLevel > 0) {
                    if (!player.hasInfiniteMaterials()) {
                        if (waterLevel == 1) {
                            level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), Block.UPDATE_ALL);
                        } else {
                            level.setBlock(pos, state.setValue(LayeredCauldronBlock.LEVEL, waterLevel - 1), Block.UPDATE_ALL);
                        }
                    }

                    stack.remove(DataComponents.DYED_COLOR);
                    stack.remove(AntiqueDataComponentTypes.SEWN_CLOTHS);
                    stack.remove(AntiqueDataComponentTypes.CLOTH_PATTERN_COLOR);

                    level.playSound(player, pos, SoundEvents.VILLAGER_WORK_LEATHERWORKER, SoundSource.BLOCKS, 1, 1);

                    return InteractionResult.SUCCESS;
                }
            }
        }

        return super.useOn(context);
    }

    @Override
    public @NonNull Component getName(@NonNull ItemStack stack) {
        ResourceKey<ClothSkinData> cloth = stack.get(AntiqueDataComponentTypes.CLOTH_TYPE);

        if (cloth == null) {
            return super.getName(stack);
        }

        return Component.translatable(ClothSkinData.getTranslationKey(cloth));
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
