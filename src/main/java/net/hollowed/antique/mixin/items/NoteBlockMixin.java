package net.hollowed.antique.mixin.items;

import net.hollowed.antique.index.AntiqueDataComponentTypes;
import net.hollowed.antique.items.components.AmethystForkComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoteBlock.class)
public abstract class NoteBlockMixin {
    @Shadow
    protected abstract void playNote(@Nullable Entity entity, BlockState blockState, Level level, BlockPos blockPos);

    @Inject(
            method = "useItemOn",
            at = @At("HEAD"),
            cancellable = true
    )
    private void useItemOn(
            ItemStack itemStack,
            BlockState blockState,
            Level level,
            BlockPos blockPos,
            Player player,
            InteractionHand interactionHand,
            BlockHitResult blockHitResult,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (!level.isClientSide()) {
            AmethystForkComponent component = itemStack.get(AntiqueDataComponentTypes.AMETHYST_FORK);

            if (component != null) {
                blockState = blockState.setValue(BlockStateProperties.NOTE, component.note());
                level.setBlock(blockPos, blockState, Block.UPDATE_ALL);
                playNote(player, blockState, level, blockPos);
                player.awardStat(Stats.TUNE_NOTEBLOCK);
                cir.setReturnValue(InteractionResult.SUCCESS);
            }
        }
    }
}
