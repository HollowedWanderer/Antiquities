package net.hollowed.antique.entities.ai;

import net.hollowed.antique.blocks.entities.OrnateBellBlockEntity;
import net.hollowed.antique.index.AntiqueBlocks;
import net.hollowed.combatamenities.index.CAParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3i;
import org.jspecify.annotations.NonNull;

import java.util.EnumSet;

public class RemoveBellGoal extends MoveToBlockGoal {
    private final Block blockToRemove;
    private final Mob removerMob;
    private int ticksSinceReachedGoal;
    private int maxStayTicks;

    public RemoveBellGoal(final Block blockToRemove, final PathfinderMob mob, final double speedModifier, final int verticalSearchRange) {
        super(mob, speedModifier, 32, verticalSearchRange);
        this.blockToRemove = blockToRemove;
        this.removerMob = mob;
        this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (this.nextStartTick > 0) {
            this.nextStartTick--;
            return false;
        } else if (this.findNearestBlock()) {
            this.nextStartTick = 5;
            return true;
        } else {
            this.nextStartTick = 5;
            return false;
        }
    }

    @Override
    public void stop() {
        super.stop();
        this.removerMob.fallDistance = 1.0;
    }

    @Override
    public void start() {
        super.start();
        this.ticksSinceReachedGoal = 0;
        this.maxStayTicks = this.mob.getRandom().nextInt(this.mob.getRandom().nextInt(1200) + 1200) + 1200;
    }

    public void playDestroyProgressSound(final LevelAccessor level, final BlockPos pos) {
    }

    public void playBreakSound(final Level level, final BlockPos pos) {
    }

    @Override
    public boolean canContinueToUse() {
        return this.mob.hurtTime == 0 && this.tryTicks >= -this.maxStayTicks && this.tryTicks <= 1200 && this.isValidContinueTarget(this.mob.level(), this.blockPos);
    }

    @Override
    public void tick() {
        double scale = 0.5;
        this.mob.getLookControl().setLookAt(new Vec3(this.blockPos).add(scale, scale, scale));

        Level level = this.removerMob.level();
        BlockPos mobPos = this.removerMob.blockPosition();
        BlockPos eatPos = null;

        RandomSource random = this.removerMob.getRandom();

        boolean reachedTarget = false;
        BlockPos moveToTarget = this.getMoveToTarget();
        if (mobPos.toMutable().distance(moveToTarget.toMutable()) >= 2.5) {
            this.tryTicks++;
            if (this.shouldRecalculatePath()) {
                this.mob.getNavigation().moveTo(this.getMoveToTarget().getX() + 0.5, this.getMoveToTarget().getY(), this.getMoveToTarget().getZ() + 0.5, 0, this.speedModifier);
            }
        } else {
            reachedTarget = true;
            this.tryTicks--;
        }

        if (mobPos.toMutable().distance(this.blockPos.toMutable()) < 2.5) eatPos = this.blockPos;
        if (reachedTarget && eatPos != null) {
            if (this.ticksSinceReachedGoal > 0) {
                Vec3 movement = this.removerMob.getDeltaMovement();
                this.removerMob.setDeltaMovement(movement.x, movement.y, movement.z);
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            new ItemParticleOption(ParticleTypes.ITEM, AntiqueBlocks.ORNATE_BELL.asItem()),
                            eatPos.getX() + 0.5,
                            eatPos.getY() + 0.9,
                            eatPos.getZ() + 0.5,
                            3,
                            (random.nextFloat() - 0.5) * 0.08,
                            (random.nextFloat() - 0.5) * 0.08,
                            (random.nextFloat() - 0.5) * 0.08,
                            0.1F
                    );
                }
            }

            if (this.ticksSinceReachedGoal % 8 == 0) this.removerMob.swing(InteractionHand.MAIN_HAND);
            if (this.ticksSinceReachedGoal % 2 == 0) {
                if (this.ticksSinceReachedGoal % 6 == 0) {
                    this.playDestroyProgressSound(level, this.blockPos);
                }
            }

            if (this.ticksSinceReachedGoal > 60) {
                level.removeBlock(eatPos, false);
                if (level instanceof ServerLevel serverLevel) {
                    for (int i = 0; i < 10; i++) {
                        double xa = random.nextGaussian() * 0.02;
                        double ya = random.nextGaussian() * 0.02;
                        double za = random.nextGaussian() * 0.02;
                        serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, eatPos.getX() + 0.5, eatPos.getY() + 0.5, eatPos.getZ() + 0.5, 1, xa, ya, za, 0.05F);
                    }

                    serverLevel.sendParticles(CAParticles.RING, eatPos.getX() + 0.5, eatPos.getY() + 0.5, eatPos.getZ() + 0.5, 1, 0, 0, 0, 0.0F);

                    this.playBreakSound(level, eatPos);
                    serverLevel.sendParticles(
                            new ItemParticleOption(ParticleTypes.ITEM, AntiqueBlocks.ORNATE_BELL.asItem()),
                            eatPos.getX() + 0.5,
                            eatPos.getY() + 1.1,
                            eatPos.getZ() + 0.5,
                            32,
                            (random.nextFloat() - 0.5) * 0.45,
                            (random.nextFloat() - 0.5) * 0.45,
                            (random.nextFloat() - 0.5) * 0.45,
                            0.15F
                    );
                }
            }

            this.ticksSinceReachedGoal++;
        }
    }

    @Override
    protected void moveMobToBlock() {
        this.mob.getNavigation().moveTo(this.getMoveToTarget().getX() + 0.5, this.getMoveToTarget().getY(), this.getMoveToTarget().getZ() + 0.5, 0, this.speedModifier);
    }

    protected @NonNull BlockPos getMoveToTarget() {
        Vector3i mobVec3i = this.removerMob.blockPosition().toMutable();
        Vector3i blockVec3i = this.blockPos.toMutable();
        Vector3i distance = mobVec3i.sub(blockVec3i);
        Vector3i target = new Vector3i(Math.clamp(distance.x, -1, 1), 0, Math.clamp(distance.z, -1, 1));
        return this.blockPos.offset(target.x, 0, target.z);
    }

    @Override
    protected boolean isValidTarget(final LevelReader level, final BlockPos pos) {
        ChunkAccess chunk = level.getChunk(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()), ChunkStatus.FULL, false);
        return chunk != null && chunk.getBlockState(pos).is(this.blockToRemove) && chunk.getBlockEntity(pos) instanceof OrnateBellBlockEntity bell && bell.shaking;
    }

    protected boolean isValidContinueTarget(final LevelReader level, final BlockPos pos) {
        ChunkAccess chunk = level.getChunk(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()), ChunkStatus.FULL, false);
        return chunk != null && chunk.getBlockState(pos).is(this.blockToRemove);
    }
}