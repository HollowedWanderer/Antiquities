package net.hollowed.antique.blocks.entities;

import net.hollowed.antique.index.AntiqueBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;

public class OrnateBellBlockEntity extends BlockEntity {
	private static final int DURATION = 50;
	public int ticks;
	public boolean shaking;
	public Direction clickDirection;
	public boolean blazing;

	public OrnateBellBlockEntity(final BlockPos worldPosition, final BlockState blockState) {
		super(AntiqueBlockEntities.ORNATE_BELL, worldPosition, blockState);
	}

	@Override
	protected void saveAdditional(@NonNull ValueOutput output) {
		output.putBoolean("Blazing", this.blazing);
		super.saveAdditional(output);
	}

	@Override
	protected void loadAdditional(@NonNull ValueInput input) {
		this.blazing = input.getBooleanOr("Blazing", false);
		super.loadAdditional(input);
	}

	@Override
	public boolean triggerEvent(final int b0, final int b1) {
		if (b0 == 1) {
			this.clickDirection = Direction.from3DDataValue(b1);
			this.ticks = 0;
			this.shaking = true;
			this.blazing = !this.blazing;
			return true;
		} else {
			return super.triggerEvent(b0, b1);
		}
	}

	@SuppressWarnings("unused")
	private static void tick(
			final Level level, final BlockPos pos, final BlockState state, final OrnateBellBlockEntity entity
	) {
		if (entity.shaking) {
			entity.ticks++;
		}

		if (entity.ticks >= DURATION) {
			entity.shaking = false;
			entity.ticks = 0;
		}
	}

	public static void clientTick(final Level level, final BlockPos pos, final BlockState state, final OrnateBellBlockEntity entity) {
		tick(level, pos, state, entity);
	}

	public static void serverTick(final Level level, final BlockPos pos, final BlockState state, final OrnateBellBlockEntity entity) {
		tick(level, pos, state, entity);
	}

	public void onHit(final Direction clickDirection) {
		BlockPos bellPos = this.getBlockPos();
		this.clickDirection = clickDirection;
		if (this.shaking) {
			this.ticks = 0;
		} else {
			this.shaking = true;
		}

        if (this.level != null) {
			this.level.blockEvent(bellPos, this.getBlockState().getBlock(), 1, clickDirection.get3DDataValue());
		}
	}
}
