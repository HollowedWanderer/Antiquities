package net.hollowed.antique.mixin.entities.living.player;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hollowed.antique.Antiquities;
import net.hollowed.antique.index.AntiqueEffects;
import net.hollowed.antique.index.AntiqueItems;
import net.hollowed.antique.networking.WallJumpPacketPayload;
import net.hollowed.antique.util.interfaces.duck.FastAir;
import net.hollowed.antique.util.ItemHoldingUtil;
import net.hollowed.antique.util.MovementUtilsClass;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Attackable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class WallJumpMixin extends Entity implements Attackable {

    @Unique
    private static final EntityDataAccessor<Vector3fc> MOVEMENT_INPUT = SynchedEntityData.defineId(WallJumpMixin.class, EntityDataSerializers.VECTOR3);

    @Shadow protected abstract float getFrictionInfluencedSpeed(float slipperiness);

    @Shadow protected boolean jumping;

    @Shadow public abstract boolean onClimbable();

    @Shadow public abstract void setSprinting(boolean sprinting);

    @Shadow protected abstract float getJumpPower();

    @Shadow public float xxa;

    public WallJumpMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Unique
    private int jumpingCooldown1;

    @Unique
    private int coyoteJumpCooldown;

    @Unique
    private int coyoteJumpTicks;

    @Unique
    private int coyoteWallJumpTicks;

    @Unique
    private boolean ran = false;

    @Unique
    private boolean canWallJump = true;

    @Unique
    private Vec3 playerVelocity = new Vec3(0, 0, 0);

    @Unique
    private Vec3 startTickPosition = new Vec3(0, 0, 0);

    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    public void damage(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (this.playerVelocity.length() > 4 && source.is(DamageTypes.FALL) && !ran) {
            LivingEntity self = (LivingEntity) (Object) this;

            float reducedAmount = amount * 0.5F;

            this.ran = true;
            self.hurtServer(world, source, reducedAmount);
            cir.setReturnValue(false);
        } else {
            this.ran = false;
        }
    }

    @SuppressWarnings("all")
    @Inject(method = "handleRelativeFrictionAndCalculateMovement", at = @At("HEAD"), cancellable = true)
    private void applyMovementInput(Vec3 movementInput, float slipperiness, CallbackInfoReturnable<Vec3> cir) {
        if ((LivingEntity) (Object) this instanceof Player player) {
            this.entityData.set(MOVEMENT_INPUT, new Vector3f((float) movementInput.x, (float) movementInput.y, (float) movementInput.z));

            if (ItemHoldingUtil.isHoldingItem(player, Identifier.fromNamespaceAndPath(Antiquities.MOD_ID, "walljumper")) && this.coyoteWallJumpTicks > 0) {

                this.moveRelative(this.getFrictionInfluencedSpeed(slipperiness), movementInput);
                this.setDeltaMovement(MovementUtilsClass.applyAxeClimbingSpeed(this.getDeltaMovement(), (LivingEntity) (Object) this));
                this.move(MoverType.SELF, this.getDeltaMovement());
                Vec3 vec3d = this.getDeltaMovement();
                if (this.onClimbable() && ItemHoldingUtil.isHoldingItem(player, Identifier.fromNamespaceAndPath(Antiquities.MOD_ID, "walljumper"))) {
                    if (!this.jumping && !this.isShiftKeyDown()) {
                        vec3d = new Vec3(vec3d.x, 0, vec3d.z);
                    } else if (!this.jumping) {
                        vec3d = new Vec3(vec3d.x * 0.4, vec3d.y, vec3d.z * 0.4);
                    }
                }

                cir.setReturnValue(vec3d);
            }
        }
    }

    @Inject(method = "defineSynchedData", at = @At("HEAD"))
    protected void initDataTracker(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(MOVEMENT_INPUT, new Vector3f());
    }

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void onJumpWhileClimbing(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (startTickPosition == null) {
            startTickPosition = entity.position();
        }

        Vec3 endTickPosition = entity.position();

        if (!startTickPosition.equals(endTickPosition)) {
            playerVelocity = endTickPosition.subtract(startTickPosition);
        }

        startTickPosition = endTickPosition;

        if (!entity.onGround() && entity.isShiftKeyDown() && entity.getDeltaMovement().y < 0 && entity.fallDistance > 15 && entity.getDeltaMovement().length() < 10 && entity instanceof Player player && !player.getAbilities().flying) {
            if ((!player.hasEffect(AntiqueEffects.BOUNCE_EFFECT) && !player.hasEffect(AntiqueEffects.VOLATILE_BOUNCE_EFFECT)) || player.isCreative()) {
                if (entity.isHolding(AntiqueItems.MYRIAD_AXE_HEAD) && !this.onClimbable()) {
                    entity.push(new Vec3(0, -0.25, 0));
                } else if (!entity.isHolding(AntiqueItems.MYRIAD_AXE_HEAD)) {
                    entity.push(new Vec3(0, -0.25, 0));
                }
            }
        }

        if (this.isSprinting()) {
            this.xxa *= 1.5F;
        }

        if (this.coyoteJumpTicks > 0) {
            this.coyoteJumpTicks--;
        }

        if (this.onGround()) {
            this.coyoteJumpTicks = 4;
            this.coyoteWallJumpTicks = 0;
        }

        if (this.coyoteWallJumpTicks > 0) {
            this.coyoteWallJumpTicks--;
        }

        if (this.onClimbable()) {
            this.coyoteWallJumpTicks = 3;
        }

        if (this.coyoteJumpCooldown > 0) {
            this.coyoteJumpCooldown--;
        }

        if (entity instanceof Player player) {
            if ((player.horizontalCollision || player.onGround()) && this.jumpingCooldown1 <= 1) this.canWallJump = true;

            if (ItemHoldingUtil.isHoldingItem(player, Identifier.fromNamespaceAndPath(Antiquities.MOD_ID, "walljumper")) && jumping && (this.horizontalCollision || this.coyoteWallJumpTicks > 0) && this.jumpingCooldown1 == 0 && this.canWallJump) {
                this.canWallJump = false;

                if (player instanceof LocalPlayer) {
                    ClientPlayNetworking.send(new WallJumpPacketPayload(entity.getId()));
                }

                Vec3 pushVector = new Vec3(0, 0.7, 0);

                AABB box = entity.getBoundingBox();
                double offset = 0.35;

                boolean collidingWest = MovementUtilsClass.collidesWithSolidBlock(entity.level(), box.move(-offset, 0, 0), entity);
                boolean collidingEast = MovementUtilsClass.collidesWithSolidBlock(entity.level(), box.move(offset, 0, 0), entity);
                boolean collidingNorth = MovementUtilsClass.collidesWithSolidBlock(entity.level(), box.move(0, 0, -offset), entity);
                boolean collidingSouth = MovementUtilsClass.collidesWithSolidBlock(entity.level(), box.move(0, 0, offset), entity);

                AABB ledgeBox = box.setMaxY(box.maxY - 0.5);

                boolean ledgeWest = !MovementUtilsClass.collidesWithSolidBlock(entity.level(), ledgeBox.move(-offset, 1.75, 0), entity);
                boolean ledgeEast = !MovementUtilsClass.collidesWithSolidBlock(entity.level(), ledgeBox.move(offset, 1.75, 0), entity);
                boolean ledgeNorth = !MovementUtilsClass.collidesWithSolidBlock(entity.level(), ledgeBox.move(0, 1.75, -offset), entity);
                boolean ledgeSouth = !MovementUtilsClass.collidesWithSolidBlock(entity.level(), ledgeBox.move(0, 1.75, offset), entity);

                float correctedYaw = player.getYRot();

                while (correctedYaw > 360) {
                    correctedYaw -= 360;
                }
                while (correctedYaw < 0) {
                    correctedYaw += 360;
                }

                boolean lookingNorth = correctedYaw > 135 && correctedYaw < 225;
                boolean lookingEast = correctedYaw > 225 && correctedYaw < 315;
                boolean lookingSouth = correctedYaw < 45 || correctedYaw > 315;
                boolean lookingWest = correctedYaw > 45 && correctedYaw < 135;

                Vec3 moveInput = new Vec3(this.entityData.get(MOVEMENT_INPUT));

                boolean movingForward = moveInput.horizontal().z > 0;

                if (collidingWest) {
                    pushVector = pushVector.add(lookingWest && movingForward ? 0.5 : 0.3, 0, 0); // Push east
                    if (ledgeWest && lookingWest) {
                        pushVector = pushVector.multiply(0, entity.isShiftKeyDown() ? 1 : 1.35, 0).add(this.getDeltaMovement());
                    }
                }
                if (collidingEast) {
                    pushVector = pushVector.add(lookingEast && movingForward ? -0.5 : -0.3, 0, 0); // Push west
                    if (ledgeEast && lookingEast) {
                        pushVector = pushVector.multiply(0, entity.isShiftKeyDown() ? 1 : 1.35, 0).add(this.getDeltaMovement());
                    }
                }
                if (collidingNorth) {
                    pushVector = pushVector.add(0, 0, lookingNorth && movingForward ? 0.5 : 0.3); // Push south
                    if (ledgeNorth && lookingNorth) {
                        pushVector = pushVector.multiply(0, entity.isShiftKeyDown() ? 1 : 1.35, 0).add(this.getDeltaMovement());
                    }
                }
                if (collidingSouth) {
                    pushVector = pushVector.add(0, 0, lookingSouth && movingForward ? -0.5 : -0.3); // Push north
                    if (ledgeSouth && lookingSouth) {
                        pushVector = pushVector.multiply(0, entity.isShiftKeyDown() ? 1 : 1.35, 0).add(this.getDeltaMovement());
                    }
                }

                pushVector = pushVector.multiply(1, Math.abs(1.0 / Math.max(2.5 * entity.getDeltaMovement().y, 1.0)), 1);
                this.setDeltaMovement(entity.getDeltaMovement().add(pushVector));
                this.jumpingCooldown1 = 7;
                entity.needsSync = true;
                if (entity instanceof FastAir access) {
                    access.antique$setFast(true);
                }
            }
        }
        if (entity.onGround()) {
            this.jumpingCooldown1 = entity.isSwimming() ? 5 : 7;
            if (entity instanceof FastAir access) {
                access.antique$setFast(false);
            }
        }
        if (this.jumpingCooldown1 > 0) {
            this.jumpingCooldown1--;
            if (this.jumpingCooldown1 == 2) {
                if (entity instanceof FastAir access) {
                    access.antique$setFast(false);
                }
            }
        }

        if (this.coyoteJumpTicks > 0 && this.jumping && this.coyoteJumpCooldown == 0 && !this.onGround() && (!this.isInWater() || this.isUnderWater())) {
            this.coyoteJumpCooldown = 6;
            this.coyoteJump();
        }
    }

    @Inject(method = "canGlide", at = @At("HEAD"), cancellable = true)
    protected void canGlide(CallbackInfoReturnable<Boolean> cir) {
        if (this.coyoteWallJumpTicks > 0 || this.coyoteJumpTicks > 0) {
            cir.setReturnValue(false);
        }
    }

    @Unique
    public void coyoteJump() {
        float f = this.getJumpPower() * 1.15F;
        if (!(f <= 1.0E-5F)) {
            Vec3 vec3d = this.getDeltaMovement();
            this.setDeltaMovement(vec3d.x, Math.max(f, vec3d.y), vec3d.z);
            if (this.isSprinting()) {
                float g = this.getYRot() * (float) (Math.PI / 180.0);
                this.addDeltaMovement(new Vec3((double)(-Mth.sin(g)) * 0.2, 0.0, (double)Mth.cos(g) * 0.2));
            }

            this.needsSync = true;
        }
    }

    @Inject(method = "jumpFromGround", at = @At("HEAD"))
    public void jumpCooldown(CallbackInfo ci) {
        this.coyoteJumpCooldown = 6;
    }
}
