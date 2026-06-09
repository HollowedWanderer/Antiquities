package net.hollowed.antique.mixin.entities.living.player;

import net.hollowed.antique.Antiquities;
import net.hollowed.antique.entities.parts.MyriadShovelPart;
import net.hollowed.antique.util.ItemHoldingUtil;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class ClimbableShovelMixin extends LivingEntity {

    @Unique
    private int coyoteTicks;

    @Unique
    private int lastGroundTime;

    @Unique
    private static final EntityDataAccessor<Byte> SPIDER_FLAGS = SynchedEntityData.defineId(ClimbableShovelMixin.class, EntityDataSerializers.BYTE);

    protected ClimbableShovelMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "onClimbable", at = @At("RETURN"), cancellable = true)
    public void isClimbing(CallbackInfoReturnable<Boolean> cir) {
        if (this.isClimbingWall()) {
            cir.setReturnValue(true);
        }
    }

    @Unique
    public boolean isClimbingWall() {
        return (this.entityData.get(SPIDER_FLAGS) & 1) != 0;
    }

    @Unique
    public void setClimbingWall(boolean climbing) {
        byte b = this.entityData.get(SPIDER_FLAGS);
        if (climbing) {
            b = (byte) (b | 1);
        } else {
            b = (byte) (b & -2);
        }

        this.entityData.set(SPIDER_FLAGS, b);
    }

    @Inject(method = "defineSynchedData", at = @At("HEAD"))
    protected void initDataTracker(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(SPIDER_FLAGS, (byte) 0);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        if (!this.level().isClientSide()) {
            if (this.horizontalCollision && ItemHoldingUtil.isHoldingItem(this, Antiquities.id("walljumper"))) {
                this.coyoteTicks = 3;
            }
            if (this.onGround()) {
                this.lastGroundTime = 5;
            }

            boolean isCollidingWithSpecificEntity = this.level()
                    .getEntities(this, this.getBoundingBox().inflate(0.01))
                    .stream()
                    .anyMatch(entity -> entity instanceof MyriadShovelPart);

            if (isCollidingWithSpecificEntity && this.horizontalCollision && !this.stuckSpeedMultiplier.equals(new Vec3(0.05, 0.01, 0.05))) {
                this.setClimbingWall(true);
            } else {
                this.setClimbingWall(ItemHoldingUtil.isHoldingItem(this, Antiquities.id("walljumper")) && (this.horizontalCollision || this.coyoteTicks > 0) && !this.onGround() && !isCollidingWithSpecificEntity && this.lastGroundTime == 0 && this.getDeltaMovement().y <= 0);
            }

            if (this.coyoteTicks > 0) {
                this.coyoteTicks--;
            }

            if (this.lastGroundTime > 0) {
                this.lastGroundTime--;
            }
        }
    }
}

