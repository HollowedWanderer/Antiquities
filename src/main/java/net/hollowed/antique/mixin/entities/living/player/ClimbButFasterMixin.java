package net.hollowed.antique.mixin.entities.living.player;

import net.hollowed.antique.entities.parts.MyriadShovelPart;
import net.minecraft.world.entity.Attackable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class ClimbButFasterMixin extends Entity implements Attackable {

    @Shadow protected abstract Vec3 handleOnClimbable(Vec3 delta);

    @Shadow protected abstract float getFrictionInfluencedSpeed(float blockFriction);

    @Shadow protected boolean jumping;

    @Shadow public abstract boolean onClimbable();

    public ClimbButFasterMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(method = "handleRelativeFrictionAndCalculateMovement", at = @At("HEAD"), cancellable = true)
    private void applyMovementInput(Vec3 input, float friction, CallbackInfoReturnable<Vec3> cir) {
        boolean isCollidingWithSpecificEntity = this.level()
                .getEntities(this, this.getBoundingBox().inflate(0.1))
                .stream()
                .anyMatch(entity -> entity instanceof MyriadShovelPart);

        if (isCollidingWithSpecificEntity && this.horizontalCollision) {

            this.moveRelative(this.getFrictionInfluencedSpeed(friction), input);
            this.setDeltaMovement(this.handleOnClimbable(this.getDeltaMovement()));
            this.move(MoverType.SELF, this.getDeltaMovement());
            Vec3 vec3d = this.getDeltaMovement();
            if ((this.horizontalCollision || this.jumping)
                    && (this.onClimbable() || this.getInBlockState().is(Blocks.POWDER_SNOW) && PowderSnowBlock.canEntityWalkOnPowderSnow(this))) {
                vec3d = new Vec3(vec3d.x, 0.4, vec3d.z);
            }

            cir.setReturnValue(vec3d);
        }
    }
}
