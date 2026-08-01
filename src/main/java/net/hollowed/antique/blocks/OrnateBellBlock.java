package net.hollowed.antique.blocks;

import com.mojang.math.OctahedralGroup;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.function.BiConsumer;

import net.hollowed.antique.blocks.entities.OrnateBellBlockEntity;
import net.hollowed.antique.index.AntiqueBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class OrnateBellBlock extends BaseEntityBlock {
	public static final MapCodec<OrnateBellBlock> CODEC = simpleCodec(OrnateBellBlock::new);
	public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
	public static final EnumProperty<BellAttachType> ATTACHMENT = BlockStateProperties.BELL_ATTACHMENT;
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	private static final VoxelShape BELL_SHAPE = Shapes.or(Block.column(8.0, 5.0, 13.0), Block.column(10.0, 2.0, 5.0), Block.column(4.0, 13.0, 16.0), Block.column(2.0, 12.0, 9.0, 11.0), Block.column(2.0, 12.0, 13.0, 15.0));
	private static final Map<Direction.Axis, VoxelShape> SHAPE_CEILING = Shapes.rotateHorizontalAxis(Shapes.or(Shapes.rotate(BELL_SHAPE, OctahedralGroup.BLOCK_ROT_Y_90), Block.box(0.0, 8.0, 6.0, 2.0, 16.0, 10.0), Block.box(14.0, 8.0, 6.0, 16.0, 16.0, 10.0)));
	private static final Map<Direction.Axis, VoxelShape> SHAPE_FLOOR = Shapes.rotateHorizontalAxis(Shapes.or(Shapes.rotate(BELL_SHAPE, OctahedralGroup.BLOCK_ROT_Y_90), Block.box(0.0, 0.0, 6.0, 2.0, 12.0, 10.0), Block.box(14.0, 0.0, 6.0, 16.0, 12.0, 10.0)));
	private static final Map<Direction.Axis, VoxelShape> SHAPE_WALL = Shapes.rotateHorizontalAxis(
		Shapes.or(BELL_SHAPE, Block.box(6.0, 8.0, 0.0, 10.0, 12.0, 2.0), Block.box(6.0, 8.0, 14.0, 10.0, 12.0, 16.0))
	);

	@Override
	public @NonNull MapCodec<OrnateBellBlock> codec() {
		return CODEC;
	}

	public OrnateBellBlock(final BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ATTACHMENT, BellAttachType.FLOOR).setValue(POWERED, false));
	}

	@Override
	protected void neighborChanged(
            final BlockState state, final Level level, final @NonNull BlockPos pos, final @NonNull Block block, @Nullable final Orientation orientation, final boolean movedByPiston
	) {
		boolean signal = level.hasNeighborSignal(pos);
		if (signal != state.getValue(POWERED)) {
			if (signal) {
				this.attemptToRing(level, pos, null);
			}

			level.setBlock(pos, state.setValue(POWERED, signal), 3);
		}
	}

	@Override
	protected void onProjectileHit(final @NonNull Level level, final @NonNull BlockState state, final @NonNull BlockHitResult hitResult, final Projectile projectile) {
		Player playerOwner = projectile.getOwner() instanceof Player player ? player : null;
		this.onHit(level, state, hitResult, playerOwner, true);
	}

	@Override
	protected @NonNull InteractionResult useWithoutItem(final @NonNull BlockState state, final @NonNull Level level, final @NonNull BlockPos pos, final @NonNull Player player, final @NonNull BlockHitResult hitResult) {
		return this.onHit(level, state, hitResult, player, true) ? InteractionResult.SUCCESS : InteractionResult.PASS;
	}

	public boolean onHit(
		final Level level, final BlockState state, final BlockHitResult hitResult, @Nullable final Player player, final boolean requireHitFromCorrectSide
	) {
		Direction direction = hitResult.getDirection();
		BlockPos blockPos = hitResult.getBlockPos();
		boolean properHit = !requireHitFromCorrectSide || this.isProperHit(state, direction, hitResult.getLocation().y - blockPos.getY());
		if (properHit) {
			boolean didRing = this.attemptToRing(player, level, blockPos, direction);
			if (didRing && player != null) {
				player.awardStat(Stats.BELL_RING);
			}

			return true;
		} else {
			return false;
		}
	}

	private boolean isProperHit(final BlockState state, final Direction clickedDirection, final double clickY) {
		if (clickedDirection.getAxis() != Direction.Axis.Y && !(clickY > 0.8124F)) {
			Direction facing = state.getValue(FACING);
			BellAttachType attachType = state.getValue(ATTACHMENT);

			return switch (attachType) {
				case FLOOR, CEILING -> facing.getAxis() == clickedDirection.getAxis();
				case SINGLE_WALL, DOUBLE_WALL -> facing.getAxis() != clickedDirection.getAxis();
            };
		} else {
			return false;
		}
	}

	public void attemptToRing(final Level level, final BlockPos pos, @Nullable final Direction direction) {
		this.attemptToRing(null, level, pos, direction);
	}

	public boolean attemptToRing(@Nullable final Entity ringingEntity, final Level level, final BlockPos pos, @Nullable Direction direction) {
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof OrnateBellBlockEntity bellBlockEntity) {
			if (direction == null) {
				direction = level.getBlockState(pos).getValue(FACING);
			}

			bellBlockEntity.onHit(direction);
			level.playSound(null, pos, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 2.0F, 1.0F);
			level.gameEvent(ringingEntity, GameEvent.BLOCK_CHANGE, pos);
			return true;
		} else {
			return false;
		}
	}

	private VoxelShape getVoxelShape(final BlockState state) {
		Direction facing = state.getValue(FACING);

		return switch (state.getValue(ATTACHMENT)) {
			case FLOOR -> SHAPE_FLOOR.get(facing.getAxis());
			case SINGLE_WALL, DOUBLE_WALL -> SHAPE_WALL.get(facing.getAxis());
            case CEILING -> SHAPE_CEILING.get(facing.getAxis());
		};
	}

	@Override
	protected @NonNull VoxelShape getCollisionShape(final @NonNull BlockState state, final @NonNull BlockGetter level, final @NonNull BlockPos pos, final @NonNull CollisionContext context) {
		return this.getVoxelShape(state);
	}

	@Override
	protected @NonNull VoxelShape getShape(final @NonNull BlockState state, final @NonNull BlockGetter level, final @NonNull BlockPos pos, final @NonNull CollisionContext context) {
		return this.getVoxelShape(state);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(final BlockPlaceContext context) {
		Direction clickedFace = context.getClickedFace();
		BlockPos pos = context.getClickedPos();
		Level level = context.getLevel();
		Direction.Axis axis = clickedFace.getAxis();
		if (axis == Direction.Axis.Y) {
			BlockState state = this.defaultBlockState()
				.setValue(ATTACHMENT, clickedFace == Direction.DOWN ? BellAttachType.CEILING : BellAttachType.FLOOR)
				.setValue(FACING, context.getHorizontalDirection());
			if (state.canSurvive(context.getLevel(), pos)) {
				return state;
			}
		} else {
			boolean doubleAttached = axis == Direction.Axis.X
					&& level.getBlockState(pos.west()).isFaceSturdy(level, pos.west(), Direction.EAST)
					&& level.getBlockState(pos.east()).isFaceSturdy(level, pos.east(), Direction.WEST)
				|| axis == Direction.Axis.Z
					&& level.getBlockState(pos.north()).isFaceSturdy(level, pos.north(), Direction.SOUTH)
					&& level.getBlockState(pos.south()).isFaceSturdy(level, pos.south(), Direction.NORTH);
			BlockState state = this.defaultBlockState()
				.setValue(FACING, clickedFace.getOpposite())
				.setValue(ATTACHMENT, doubleAttached ? BellAttachType.DOUBLE_WALL : BellAttachType.SINGLE_WALL);
			if (state.canSurvive(context.getLevel(), context.getClickedPos())) {
				return state;
			}

			boolean canAttachBelow = level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
			state = state.setValue(ATTACHMENT, canAttachBelow ? BellAttachType.FLOOR : BellAttachType.CEILING);
			if (state.canSurvive(context.getLevel(), context.getClickedPos())) {
				return state;
			}
		}

		return null;
	}

	@Override
	protected void onExplosionHit(
            final @NonNull BlockState state, final @NonNull ServerLevel level, final @NonNull BlockPos pos, final Explosion explosion, final @NonNull BiConsumer<ItemStack, BlockPos> onHit
	) {
		if (explosion.canTriggerBlocks()) {
			this.attemptToRing(level, pos, null);
		}

		super.onExplosionHit(state, level, pos, explosion, onHit);
	}

	@Override
	protected @NonNull BlockState updateShape(
		final BlockState state,
		final @NonNull LevelReader level,
		final @NonNull ScheduledTickAccess ticks,
		final @NonNull BlockPos pos,
		final @NonNull Direction directionToNeighbour,
		final @NonNull BlockPos neighbourPos,
		final @NonNull BlockState neighbourState,
		final @NonNull RandomSource random
	) {
		BellAttachType attachment = state.getValue(ATTACHMENT);
		Direction connectedDirection = getConnectedDirection(state).getOpposite();
		if (connectedDirection == directionToNeighbour && !state.canSurvive(level, pos) && attachment != BellAttachType.DOUBLE_WALL) {
			return Blocks.AIR.defaultBlockState();
		} else {
			if (directionToNeighbour.getAxis() == state.getValue(FACING).getAxis()) {
				if (attachment == BellAttachType.DOUBLE_WALL && !neighbourState.isFaceSturdy(level, neighbourPos, directionToNeighbour)) {
					return state.setValue(ATTACHMENT, BellAttachType.SINGLE_WALL).setValue(FACING, directionToNeighbour.getOpposite());
				}

				if (attachment == BellAttachType.SINGLE_WALL
					&& connectedDirection.getOpposite() == directionToNeighbour
					&& neighbourState.isFaceSturdy(level, neighbourPos, state.getValue(FACING))) {
					return state.setValue(ATTACHMENT, BellAttachType.DOUBLE_WALL);
				}
			}

			return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
		}
	}

	@Override
	protected boolean canSurvive(final @NonNull BlockState state, final @NonNull LevelReader level, final @NonNull BlockPos pos) {
		Direction connectionDir = getConnectedDirection(state).getOpposite();
		return connectionDir == Direction.UP
			? Block.canSupportCenter(level, pos.above(), Direction.DOWN)
			: FaceAttachedHorizontalDirectionalBlock.canAttach(level, pos, connectionDir);
	}

	private static Direction getConnectedDirection(final BlockState state) {
		return switch (state.getValue(ATTACHMENT)) {
			case FLOOR -> Direction.UP;
			case CEILING -> Direction.DOWN;
			default -> state.getValue(FACING).getOpposite();
		};
	}

	@Override
	protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, ATTACHMENT, POWERED);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(final @NonNull BlockPos worldPosition, final @NonNull BlockState blockState) {
		return new OrnateBellBlockEntity(worldPosition, blockState);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final @NonNull Level level, final @NonNull BlockState blockState, final @NonNull BlockEntityType<T> type) {
		return createTickerHelper(type, AntiqueBlockEntities.ORNATE_BELL, level.isClientSide() ? OrnateBellBlockEntity::clientTick : OrnateBellBlockEntity::serverTick);
	}

	@Override
	protected boolean isPathfindable(final @NonNull BlockState state, final @NonNull PathComputationType type) {
		return false;
	}

	@Override
	public @NonNull BlockState rotate(final BlockState state, final Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public @NonNull BlockState mirror(final BlockState state, final Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}
}
