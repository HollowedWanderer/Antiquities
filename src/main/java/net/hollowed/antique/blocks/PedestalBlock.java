package net.hollowed.antique.blocks;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.hollowed.antique.index.AntiqueBlockEntities;
import net.hollowed.antique.blocks.entities.PedestalBlockEntity;
import net.hollowed.antique.mixin.accessors.MobEntitySoundAccessor;
import net.hollowed.antique.networking.PedestalPacketPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

public class PedestalBlock extends BaseEntityBlock implements EntityBlock, SimpleWaterloggedBlock {

    public static final MapCodec<PedestalBlock> CODEC = simpleCodec(PedestalBlock::new);
    public static final List<BlockPos> POWER_PROVIDER_OFFSETS = BlockPos.betweenClosedStream(-2, 0, -2, 2, 1, 2).filter((pos) -> Math.abs(pos.getX()) == 2 || Math.abs(pos.getZ()) == 2).map(BlockPos::immutable).toList();

    public static final BooleanProperty HELD_ITEM = BooleanProperty.create("held_item");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public static final EnumProperty<@NotNull PillarPart> PART = EnumProperty.create("part", PillarPart.class);

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (!world.isClientSide()) {
            return type == AntiqueBlockEntities.PEDESTAL_BLOCK_ENTITY
                    ? (world1, _, _, blockEntity) ->
                    PedestalBlockEntity.tick(world1, (PedestalBlockEntity) blockEntity)
                    : null;
        }
        return null;
    }


    public static final VoxelShape SHAPE_DEFAULT = Stream.of(
            Block.box(1, 0, 1, 15, 3, 15),
            Block.box(3, 3, 3, 13, 13, 13),
            Block.box(1, 13, 1, 15, 16, 15)
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
    public static final VoxelShape SHAPE_TOP = Stream.of(
            Block.box(3, 0, 3, 13, 13, 13),
            Block.box(1, 13, 1, 15, 16, 15)
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
    public static final VoxelShape SHAPE_MIDDLE = java.util.Optional.of(
            Block.box(3, 0, 3, 13, 16, 13)
    ).get();
    public static final VoxelShape SHAPE_BOTTOM = Stream.of(
            Block.box(1, 0, 1, 15, 3, 15),
            Block.box(3, 3, 3, 13, 16, 13)
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

    public PedestalBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any().setValue(HELD_ITEM, false).setValue(WATERLOGGED, false));
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    private PillarPart getPillarPart(Level world, BlockPos pos) {
        BlockState stateBelow = world.getBlockState(pos.below());
        BlockState stateAbove = world.getBlockState(pos.above());

        boolean isSameBelow = stateBelow.getBlock() instanceof PedestalBlock;
        boolean isSameAbove = stateAbove.getBlock() instanceof PedestalBlock;

        if (isSameBelow && isSameAbove) {
            return PillarPart.MIDDLE;
        } else if (isSameBelow) {
            return PillarPart.TOP;
        } else if (isSameAbove) {
            return PillarPart.BOTTOM;
        } else {
            return PillarPart.DEFAULT;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, @NotNull BlockState> builder) {
        builder.add(HELD_ITEM, WATERLOGGED, PART);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Level world = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        BlockState stateBelow = world.getBlockState(pos.below());

        if (stateBelow.getBlock() instanceof PedestalBlock) {
            BlockEntity entityBelow = world.getBlockEntity(pos.below());
            if (entityBelow instanceof PedestalBlockEntity) {
                ItemStack stack = ((PedestalBlockEntity) entityBelow).getItem(0);
                if (!stack.isEmpty()) {
                    return null;
                }
            }
        }

        FluidState fluidState = ctx.getLevel().getFluidState(ctx.getClickedPos());
        boolean waterlogged = fluidState.getType() == Fluids.WATER;
        return this.defaultBlockState().setValue(HELD_ITEM, false).setValue(WATERLOGGED, waterlogged).setValue(PART, this.getPillarPart(world, pos));
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void setPlacedBy(@NotNull Level world, @NotNull BlockPos pos, @NotNull BlockState state, LivingEntity placer, @NotNull ItemStack itemStack) {
        super.setPlacedBy(world, pos, state, placer, itemStack);

        BlockEntity blockEntity = world.getBlockEntity(pos);

        if (!world.isClientSide()) {
            if (blockEntity instanceof PedestalBlockEntity pedestalBlockEntity) {
                ServerLevel serverWorld = (ServerLevel) world;
                for (ServerPlayer serverPlayerEntity : serverWorld.players()) {
                    ServerPlayNetworking.send(serverPlayerEntity, new PedestalPacketPayload(pos, pedestalBlockEntity.getItems().getFirst()));
                }
            }
        }
        world.setBlock(pos, state.setValue(PART, this.getPillarPart(world, pos)), 3);
    }

    public static boolean canAccessPowerProvider(Level world, BlockPos tablePos, BlockPos providerOffset) {
        return world.getBlockState(tablePos.offset(providerOffset)).is(BlockTags.ENCHANTMENT_POWER_PROVIDER) && world.getBlockState(tablePos.offset(providerOffset.getX() / 2, providerOffset.getY(), providerOffset.getZ() / 2)).is(BlockTags.ENCHANTMENT_POWER_TRANSMITTER);
    }

    @Override
    public void animateTick(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull RandomSource random) {
        super.animateTick(state, world, pos, random);

        BlockEntity entity = world.getBlockEntity(pos);

        if (entity instanceof PedestalBlockEntity pedestalBlockEntity) {
            if (!pedestalBlockEntity.getItems().getFirst().isEmpty()
                    && pedestalBlockEntity.getItems().getFirst().isEnchanted()) {
                for (BlockPos blockPos : POWER_PROVIDER_OFFSETS) {
                    if (random.nextInt(16) == 0 && canAccessPowerProvider(world, pos, blockPos)) {
                        world.addParticle(ParticleTypes.ENCHANT, (double) pos.getX() + 0.5, (double) pos.getY() + 2.75, (double) pos.getZ() + 0.5, (double) ((float) blockPos.getX() + random.nextFloat()) - 0.5, (float) blockPos.getY() - random.nextFloat() - 1.0F, (double) ((float) blockPos.getZ() + random.nextFloat()) - 0.5);
                    }
                }
            }
        }
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter view, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return getShape(state);
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter view, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return getShape(state);
    }

    private VoxelShape getShape(BlockState state) {
        PillarPart part = state.getValue(PART);

        return switch (part) {
            case TOP -> SHAPE_TOP;
            case MIDDLE -> SHAPE_MIDDLE;
            case BOTTOM -> SHAPE_BOTTOM;
            default -> SHAPE_DEFAULT;
        };
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new PedestalBlockEntity(pos, state);
    }

    @Override
    protected @NotNull InteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof PedestalBlockEntity pedestalEntity && state.getValue(PART) != PillarPart.BOTTOM && state.getValue(PART) != PillarPart.MIDDLE && !level.getBlockState(pos.above()).isCollisionShapeFullBlock(level, pos.above())) {
            ItemStack currentPedestalItem = pedestalEntity.getItem(0);
            ItemStack handItem = player.getItemInHand(hand);

            if (!handItem.isEmpty() && handItem.getCount() > 1 && !currentPedestalItem.isEmpty()) {
                return InteractionResult.FAIL;
            }

            boolean itemChanged = false;

            if (!currentPedestalItem.isEmpty()) {
                player.setItemInHand(hand, currentPedestalItem);
                pedestalEntity.setItem(0, ItemStack.EMPTY);
                itemChanged = true;
            }

            ItemStack newItem = handItem.copy();
            newItem.setCount(1);

            if (!handItem.isEmpty()) {
                pedestalEntity.setItem(0, newItem);
                handItem.shrink(1);
                itemChanged = true;

                if (newItem.getItem() instanceof SpawnEggItem) {
                    EntityType<?> entityType = SpawnEggItem.getType(newItem);
                    if (entityType != null && !level.isClientSide()) {
                        Entity entity1 = entityType.create(level, EntitySpawnReason.MOB_SUMMONED);
                        if (entity1 instanceof Mob mobEntity) {
                            SoundEvent ambientSound = ((MobEntitySoundAccessor) mobEntity).invokeGetAmbientSound();
                            if (ambientSound != null) {
                                level.playSound(null, pos, ambientSound, mobEntity.getSoundSource());
                            }
                        }
                    }
                }
            }

            if (itemChanged && !level.isClientSide()) {
                pedestalEntity.setChanged();
                level.playSound(null, pos, SoundEvents.SUSPICIOUS_SAND_PLACE, SoundSource.BLOCKS, 0.75F, 1.0F);
                level.playSound(null, pos, SoundEvents.LODESTONE_PLACE, SoundSource.BLOCKS, 0.75F, 1.0F);
                ((ServerLevel) level).getChunkSource().blockChanged(pos);
                level.setBlock(pos, state.setValue(PedestalBlock.HELD_ITEM, !pedestalEntity.getItem(0).isEmpty()), Block.UPDATE_ALL);
                level.updateNeighborsAt(pos, state.getBlock(), null);

                ServerLevel serverWorld = (ServerLevel) level;
                for (ServerPlayer serverPlayerEntity : serverWorld.players()) {
                    ServerPlayNetworking.send(serverPlayerEntity, new PedestalPacketPayload(pedestalEntity.getBlockPos(), newItem));
                }
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    protected void affectNeighborsAfterRemoval(@NotNull BlockState state, ServerLevel world, @NotNull BlockPos pos, boolean moved) {
        if (state != world.getBlockState(pos)) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof PedestalBlockEntity) {
                ((PedestalBlockEntity) blockEntity).drops();
            }
        }
        super.affectNeighborsAfterRemoval(state, world, pos, moved);
    }

    @Override
    public boolean hasAnalogOutputSignal(@NotNull BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(@NotNull BlockState state, Level world, @NotNull BlockPos pos, @NotNull Direction direction) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof PedestalBlockEntity) {
            ItemStack stack = ((PedestalBlockEntity) blockEntity).getItem(0);
            switch (stack.getRarity()) {
                case Rarity.COMMON -> {
                    return !stack.isEmpty() ? 1 : 0;
                }
                case Rarity.UNCOMMON -> {
                    return !stack.isEmpty() ? 2 : 0;
                }
                case Rarity.RARE -> {
                    return !stack.isEmpty() ? 3 : 0;
                }
                case Rarity.EPIC -> {
                    return !stack.isEmpty() ? 4 : 0;
                }
            }
        }
        return 0;
    }

    @Override
    public @NotNull FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected @NotNull BlockState updateShape(BlockState state, @NotNull LevelReader world, @NotNull ScheduledTickAccess tickView, @NotNull BlockPos pos, @NotNull Direction direction, @NotNull BlockPos neighborPos, @NotNull BlockState neighborState, @NotNull RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            tickView.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }

        if (direction.getAxis() == Direction.Axis.Y) {
            return state.setValue(PART, this.getPillarPart((Level) world, pos));
        }

        return super.updateShape(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    public enum PillarPart implements StringRepresentable {
        BOTTOM("bottom"),
        MIDDLE("middle"),
        TOP("top"),
        DEFAULT("default");

        private final String name;

        PillarPart(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}