package net.hollowed.antique.mixin.entities.living.player;

import net.hollowed.antique.index.AntiqueItems;
import net.minecraft.client.Camera;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow private float xRot;
    @Shadow private float yRot;

    @Shadow @Final private static Vector3fc FORWARDS;
    @Shadow @Final private static Vector3fc UP;
    @Shadow @Final private static Vector3fc LEFT;
    @Shadow @Final private Quaternionf rotation;
    @Shadow @Final private Vector3f forwards;
    @Shadow @Final private Vector3f up;
    @Shadow @Final private Vector3f left;

    @Shadow
    private boolean detached;

    @Shadow
    private @Nullable Entity entity;

    @Unique
    private float roll = 0.0F;

    @Inject(method = "alignWithEntity", at = @At("TAIL"))
    public void update(float partialTicks, CallbackInfo ci) {
        Entity entity = this.entity;
        if (entity instanceof AbstractClientPlayer player) {
            AABB box = player.getBoundingBox();
            double offset = 0.1;

            boolean collidingWest = collidesWithSolidBlock(player.level(), box.move(-offset, 0, 0), player);
            boolean collidingEast = collidesWithSolidBlock(player.level(), box.move(offset, 0, 0), player);
            boolean collidingNorth = collidesWithSolidBlock(player.level(), box.move(0, 0, -offset), player);
            boolean collidingSouth = collidesWithSolidBlock(player.level(), box.move(0, 0, offset), player);

            float correctedYaw = this.yRot;

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

            float targetRoll = 0.0F;

            if (player.onClimbable() && (this.xRot < 30 && this.xRot > -30) && !this.detached && !player.onGround() && player.isHolding(AntiqueItems.MYRIAD_AXE_HEAD)) {
                if (collidingNorth) {
                    if (lookingEast) {
                        targetRoll = -15;
                    } else if (lookingWest) {
                        targetRoll = 15;
                    } else {
                        targetRoll = 0;
                    }
                } else if (collidingEast) {
                    if (lookingSouth) {
                        targetRoll = -15;
                    } else if (lookingNorth) {
                        targetRoll = 15;
                    } else {
                        targetRoll = 0;
                    }
                } else if (collidingSouth) {
                    if (lookingWest) {
                        targetRoll = -15;
                    } else if (lookingEast) {
                        targetRoll = 15;
                    } else {
                        targetRoll = 0;
                    }
                } else if (collidingWest) {
                    if (lookingNorth) {
                        targetRoll = -15;
                    } else if (lookingSouth) {
                        targetRoll = 15;
                    } else {
                        targetRoll = 0;
                    }
                }
            }

            this.roll += (targetRoll - this.roll) * 0.05F;
            this.setRotationWithRoll(this.yRot, this.xRot, this.roll);
        }
    }

    @Unique
    private void setRotationWithRoll(float yaw, float pitch, float roll) {
        this.xRot = pitch;
        this.yRot = yaw;
        this.roll = roll;
        this.rotation.rotationYXZ((float) Math.PI - yaw * (float) (Math.PI / 180.0), -pitch * (float) (Math.PI / 180.0), roll * (float) (Math.PI / 180.0));
        FORWARDS.rotate(this.rotation, this.forwards);
        UP.rotate(this.rotation, this.up);
        LEFT.rotate(this.rotation, this.left);
    }

    @Unique
    private boolean collidesWithSolidBlock(Level world, AABB box, Entity entity) {
        return world.getBlockCollisions(entity, box).iterator().hasNext();
    }
}