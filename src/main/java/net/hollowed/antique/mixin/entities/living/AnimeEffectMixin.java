package net.hollowed.antique.mixin.entities.living;

import net.hollowed.antique.index.AntiqueEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.hollowed.antique.index.AntiqueDamageTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(LivingEntity.class)
public abstract class AnimeEffectMixin extends Entity {

    public AnimeEffectMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Shadow private long lastDamageStamp;

    @Shadow public abstract boolean hurtServer(@NotNull ServerLevel level, @NotNull DamageSource source, float damage);

    @Shadow public abstract @Nullable LivingEntity getLastHurtByMob();

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        LivingEntity user = (LivingEntity) (Object) this;
        Level world = user.level();

        this.setNoGravity(user.hasEffect(AntiqueEffects.ANIME_EFFECT));
        if (user.hasEffect(AntiqueEffects.ANIME_EFFECT) && !world.isClientSide()) {
            int radius = (int) (user.getBoundingBox().getXsize() + 2) / 2 + Objects.requireNonNull(user.getEffect(AntiqueEffects.ANIME_EFFECT)).getAmplifier();

            // First sphere: around the entity
            BlockPos center = user.blockPosition();
            breakSphere(world, center, radius);
            breakSphere(world, center.above(), radius);

            // Interpolate multiple points between lastPos and currentPos
            Vec3 from = this.xo != 0 || this.yo != 0 || this.zo != 0 ? new Vec3(xo, yo, zo) : user.position();
            Vec3 to = user.position();
            int steps = (int) (from.distanceTo(to) * 4); // 4 steps per block for decent resolution

            for (int i = 0; i <= steps; i++) {
                double t = (double) i / steps;
                Vec3 point = from.lerp(to, t);
                BlockPos pos = new BlockPos((int) point.x, (int) point.y, (int) point.z);
                breakSphere(world, pos, radius);
                breakSphere(world, pos.above(), radius);
            }

        }
    }

    @Unique
    private void breakSphere(Level world, BlockPos center, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    double distance = Math.sqrt(x * x + y * y + z * z);
                    if (distance <= radius) {
                        BlockPos pos = center.offset(x, y, z);
                        if (!world.getBlockState(pos).isAir() && world.getBlockState(pos).getBlock().getExplosionResistance() < 500) {
                            float distanceToPlayer = 0;
                            for (Player player : world.players()) {
                                distanceToPlayer = player.distanceTo(this);
                            }

                            if (distanceToPlayer < 16) {
                                world.destroyBlock(pos, false);
                            } else {
                                world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                            }

                            if (world instanceof ServerLevel serverWorld) {
                                if (this.lastDamageStamp > 1) {
                                    this.hurtServer(serverWorld, AntiqueDamageTypes.of(world, AntiqueDamageTypes.WALL_SLAM, this.getLastHurtByMob()), 8);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
