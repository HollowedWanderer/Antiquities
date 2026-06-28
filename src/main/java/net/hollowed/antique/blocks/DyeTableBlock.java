package net.hollowed.antique.blocks;

import net.hollowed.antique.blocks.screens.DyeingScreenHandler;
import net.hollowed.antique.index.AntiqueStats;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class DyeTableBlock extends Block {
    private static final Component TITLE = Component.translatable("container.antique.dyeing");

    public DyeTableBlock(BlockBehaviour.Properties settings) {
        super(settings);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level world, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
        if (!world.isClientSide()) {
            player.openMenu(state.getMenuProvider(world, pos));
            player.awardStat(AntiqueStats.INTERACT_WITH_DYE_TABLE);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected MenuProvider getMenuProvider(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos) {
        return new SimpleMenuProvider((syncId, inventory, _) -> new DyeingScreenHandler(syncId, inventory, ContainerLevelAccess.create(world, pos)), TITLE);
    }
}
