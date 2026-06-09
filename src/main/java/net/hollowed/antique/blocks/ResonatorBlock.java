package net.hollowed.antique.blocks;

import com.mojang.serialization.MapCodec;
import net.hollowed.antique.blocks.entities.ResonatorBlockEntity;
import net.hollowed.antique.index.AntiqueBlockEntities;
import net.hollowed.antique.util.shockwave.ShockwaveManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.ticks.TickPriority;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ResonatorBlock extends DirectionalBlock implements EntityBlock {
    public static final IntegerProperty CHARGE = IntegerProperty.create("charge", 0, 15);
    public static final MapCodec<ResonatorBlock> CODEC = simpleCodec(ResonatorBlock::new);

    public ResonatorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            @NonNull Level level,
            @NonNull BlockState state,
            @NonNull BlockEntityType<T> type
    ) {
        return (level1, pos, state1, entity) -> {
            if (level1 instanceof ServerLevel serverLevel) {
                ResonatorBlockEntity entity1 = (ResonatorBlockEntity) entity;

                if (entity1.shockwave != null && entity1.shockwave.tick(serverLevel)) {
                    entity1.shockwave = null;
                }
            }
        };
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new ResonatorBlockEntity(pos, state);
    }

    @Override
    protected @NonNull MapCodec<? extends ResonatorBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(@NonNull BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(FACING, context.getClickedFace().getOpposite())
                .setValue(CHARGE, 0);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NonNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, CHARGE);
    }

    protected int getInputSignal(Level level, BlockPos pos, BlockState state) {
        Direction dir = state.getValue(FACING).getOpposite();
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
    protected void tick(
            @NonNull BlockState state,
            @NonNull ServerLevel level,
            @NonNull BlockPos pos,
            @NonNull RandomSource randomSource
    ) {
        ResonatorBlockEntity entity = level.getBlockEntity(pos, AntiqueBlockEntities.RESONATOR_BLOCK_ENTITY).orElseThrow();
        entity.shockwave = new ShockwaveManager(pos, state.getValue(FACING), 32f, 1); // TODO
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
        //if (getInputSignal(level, pos, state) > 0) {
            level.scheduleTick(pos, this, 2, TickPriority.VERY_HIGH);
        //}
    }

    @Override
    public void setPlacedBy(
            @NonNull Level level,
            @NonNull BlockPos pos,
            @NonNull BlockState state,
            @Nullable LivingEntity livingEntity,
            @NonNull ItemStack itemStack
    ) {
        //if (getInputSignal(level, pos, state) > 0) {
            level.scheduleTick(pos, this, 2, TickPriority.VERY_HIGH);
        //}
    }
}
