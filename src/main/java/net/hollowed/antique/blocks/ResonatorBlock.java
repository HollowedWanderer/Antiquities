package net.hollowed.antique.blocks;

import com.mojang.serialization.MapCodec;
import net.hollowed.antique.util.shockwave.Shockwave;
import net.hollowed.antique.util.shockwave.ShockwaveManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ResonatorBlock extends Block implements SimpleWaterloggedBlock {
    public static final IntegerProperty CHARGE = IntegerProperty.create("charge", 0, 15);
    public static final MapCodec<ResonatorBlock> CODEC = simpleCodec(ResonatorBlock::new);
    private static final EnumProperty<FrontAndTop> ORIENTATION = BlockStateProperties.ORIENTATION;
    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public ResonatorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.getStateDefinition().any()
                        .setValue(ORIENTATION, FrontAndTop.NORTH_UP)
                        .setValue(WATERLOGGED, false)
                        .setValue(POWERED, false)
                        .setValue(CHARGE, 0)
        );
    }

    @Override
    protected @NonNull MapCodec<? extends ResonatorBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(@NonNull BlockPlaceContext context) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        Direction direction = context.getNearestLookingDirection().getOpposite();
        Direction direction2 = switch (direction) {
            case DOWN -> context.getHorizontalDirection().getOpposite();
            case UP -> context.getHorizontalDirection();
            case NORTH, SOUTH, WEST, EAST -> Direction.UP;
        };

        return defaultBlockState()
                .setValue(ORIENTATION, FrontAndTop.fromFrontAndTop(direction, direction2))
                .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER)
                .setValue(POWERED, false)
                .setValue(CHARGE, 0);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NonNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ORIENTATION, CHARGE, WATERLOGGED, POWERED);
    }

    protected int getInputSignal(Level level, BlockPos pos, BlockState state) {
        Direction dir = state.getValue(ORIENTATION).front().getOpposite();
        BlockPos otherPos = pos.relative(dir);
        int signal = level.getSignal(otherPos, dir);

         if (signal >= 15) {
            return signal;
        } else {
            BlockState otherState = level.getBlockState(otherPos);
            return Math.max(signal, otherState.is(Blocks.REDSTONE_WIRE) ? otherState.getValue(RedStoneWireBlock.POWER) : 0);
        }
    }

    @Override
    protected void neighborChanged(
            @NonNull BlockState state,
            @NonNull Level level,
            @NonNull BlockPos pos,
            @NonNull Block block,
            @Nullable Orientation orientation,
            boolean bl
    ) {
        if (getInputSignal(level, pos, state) > 0) {
            if (!state.getValue(POWERED)) {
                if (level instanceof ServerLevel serverLevel) {
                    ShockwaveManager.createShockwave(new Shockwave(pos, state.getValue(ORIENTATION).front(), 2.5f, 24f, 2), serverLevel);
                }
                level.setBlock(pos, state.setValue(POWERED, true), Block.UPDATE_ALL);
            }
        } else {
            level.setBlock(pos, state.setValue(POWERED, false), Block.UPDATE_ALL);
        }
    }

    @Override
    public void setPlacedBy(
            @NonNull Level level,
            @NonNull BlockPos pos,
            @NonNull BlockState state,
            @Nullable LivingEntity livingEntity,
            @NonNull ItemStack itemStack
    ) {
        if (getInputSignal(level, pos, state) > 0) {
            if (!state.getValue(POWERED)) {
                if (level instanceof ServerLevel serverLevel) {
                    ShockwaveManager.createShockwave(new Shockwave(pos, state.getValue(ORIENTATION).front(), 2.5f, 24f, 2), serverLevel);
                }
                level.setBlock(pos, state.setValue(POWERED, true), Block.UPDATE_ALL);
            }
        } else {
            level.setBlock(pos, state.setValue(POWERED, false), Block.UPDATE_ALL);
        }
    }

    @Override
    protected @NonNull BlockState updateShape(
            BlockState blockState,
            @NonNull LevelReader levelReader,
            @NonNull ScheduledTickAccess scheduledTickAccess,
            @NonNull BlockPos blockPos,
            @NonNull Direction direction,
            @NonNull BlockPos blockPos2,
            @NonNull BlockState blockState2,
            @NonNull RandomSource randomSource
    ) {
        if (blockState.getValue(WATERLOGGED)) {
            scheduledTickAccess.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelReader));
        }

        return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
    }

    @Override
    protected @NonNull FluidState getFluidState(BlockState blockState) {
        return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(true) : super.getFluidState(blockState);
    }
}
