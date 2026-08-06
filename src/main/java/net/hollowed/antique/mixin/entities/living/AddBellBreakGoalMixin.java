package net.hollowed.antique.mixin.entities.living;

import net.hollowed.antique.entities.ai.RemoveBellGoal;
import net.hollowed.antique.index.AntiqueBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {
        Zombie.class,
        Spider.class,
        Ravager.class,
        AbstractSkeleton.class,
        EnderMan.class,
        Silverfish.class,
        Endermite.class
})
public class AddBellBreakGoalMixin extends PathfinderMob {

    protected AddBellBreakGoalMixin(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Inject(method = "registerGoals", at = @At("HEAD"))
    private void registerGoals(CallbackInfo ci) {
        this.goalSelector.addGoal(0, new AttackBellGoal(this, 1.1, 24));

    }

    public class AttackBellGoal extends RemoveBellGoal {
        public AttackBellGoal(final PathfinderMob mob, final double speedModifier, final int verticalSearchRange) {
            super(AntiqueBlocks.ORNATE_BELL, mob, speedModifier, verticalSearchRange);
        }

        @Override
        public void playDestroyProgressSound(final LevelAccessor level, final @NonNull BlockPos pos) {
            level.playSound(null, pos, SoundEvents.VAULT_HIT, SoundSource.HOSTILE, 0.5F, 0.9F + AddBellBreakGoalMixin.this.random.nextFloat() * 0.2F);
        }

        @Override
        public void playBreakSound(final Level level, final @NonNull BlockPos pos) {
            level.playSound(null, pos, SoundEvents.VAULT_BREAK, SoundSource.BLOCKS, 0.7F, 0.9F + level.getRandom().nextFloat() * 0.2F);
        }

        @Override
        public double acceptedDistance() {
            return 1.5;
        }
    }
}
