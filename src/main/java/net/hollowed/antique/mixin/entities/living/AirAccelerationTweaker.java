package net.hollowed.antique.mixin.entities.living;

import net.hollowed.antique.index.AntiqueEffects;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(LivingEntity.class)
public abstract class AirAccelerationTweaker extends Entity {

    @Shadow public abstract boolean hasEffect(Holder<MobEffect> effect);

    @Shadow public abstract boolean hurtServer(@NotNull ServerLevel level, @NotNull DamageSource source, float damage);

    @Shadow public abstract boolean isAutoSpinAttack();

    @Shadow
    public abstract @Nullable MobEffectInstance getEffect(Holder<MobEffect> effect);

    public AirAccelerationTweaker(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(method = "travelInAir", at = @At("HEAD"))
    private void boostHorizontalAcceleration(Vec3 input, CallbackInfo ci) {
        Entity entity = this;

        if ((LivingEntity) (Object) this instanceof Player player && !entity.onGround() && player.getFallFlyingTicks() == 0 && !(player.getAbilities().flying || player.isSpectator())) {
            double horizontalBoost = 0.01;
            if (this.isSprinting()) horizontalBoost = 0.015;
            if (this.isAutoSpinAttack()) horizontalBoost = 0.125;
            if (this.hasEffect(AntiqueEffects.BOUNCE_EFFECT)) horizontalBoost = 0.15;
            double maxHorizontalSpeed = 0.55;
            if (this.isSprinting()) maxHorizontalSpeed = 0.95;

            Vec3 newVelocity = getVec3d(input, entity, horizontalBoost);

            double horizontalSpeed = Math.sqrt(newVelocity.x * newVelocity.x + newVelocity.z * newVelocity.z);
            if (horizontalSpeed > maxHorizontalSpeed) {
                double scale = maxHorizontalSpeed / horizontalSpeed;
                newVelocity = new Vec3(newVelocity.x * scale, newVelocity.y, newVelocity.z * scale);
            }

            if (this.hasEffect(AntiqueEffects.BOUNCE_EFFECT)) {
                this.setDeltaMovement(newVelocity);
            } else if (input.length() > 0.1) {
                this.setDeltaMovement(newVelocity);
            }
        }
    }

    @Unique
    private Vec3 getVec3d(Vec3 movementInput, Entity entity, double horizontalBoost) {
        float yaw = entity.getYRot();
        double yawRad = Math.toRadians(yaw);

        double globalX = movementInput.x * Math.cos(yawRad) - movementInput.z * Math.sin(yawRad);
        double globalZ = movementInput.x * Math.sin(yawRad) + movementInput.z * Math.cos(yawRad);

        Vec3 boostedInput = new Vec3(globalX * horizontalBoost, 0, globalZ * horizontalBoost);

        Vec3 currentVelocity = this.getDeltaMovement();
        return currentVelocity.add(boostedInput);
    }

    @Inject(method = "travelInFluid", at = @At("HEAD"))
    private void boostHorizontalAccelerationInWater(Vec3 input, CallbackInfo ci) {
        Entity entity = this;

        if ((LivingEntity) (Object) this instanceof Player player && !entity.onGround() && player.getFallFlyingTicks() == 0 && !(player.getAbilities().flying || player.isSpectator())) {
            double horizontalBoost = 0.01;
            if (this.isSprinting()) horizontalBoost = 0.011;
            if (this.isAutoSpinAttack()) horizontalBoost = 0.125;
            if (this.hasEffect(AntiqueEffects.BOUNCE_EFFECT)) horizontalBoost = 0.15;
            double maxHorizontalSpeed = 0.75;
            if (this.isSprinting()) maxHorizontalSpeed = 1.15;

            Vec3 newVelocity = getVec3d(input, entity, horizontalBoost);

            double horizontalSpeed = Math.sqrt(newVelocity.x * newVelocity.x + newVelocity.z * newVelocity.z);
            if (horizontalSpeed > maxHorizontalSpeed) {
                double scale = maxHorizontalSpeed / horizontalSpeed;
                newVelocity = new Vec3(newVelocity.x * scale, newVelocity.y, newVelocity.z * scale);
            }

            if (this.hasEffect(AntiqueEffects.BOUNCE_EFFECT)) {
                this.setDeltaMovement(newVelocity);
            } else if (input.length() > 0.1) {
                this.setDeltaMovement(newVelocity);
            }
        }
    }

    @Inject(method = "getJumpBoostPower", at = @At("HEAD"), cancellable = true)
    public void jumpModifier(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(this.hasEffect(MobEffects.JUMP_BOOST) ? 0.2F * (Objects.requireNonNull(this.getEffect(MobEffects.JUMP_BOOST)).getAmplifier() + 1.0F) : 0.0F);
    }
}
