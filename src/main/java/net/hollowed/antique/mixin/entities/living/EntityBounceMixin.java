package net.hollowed.antique.mixin.entities.living;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.floats.FloatArraySet;
import it.unimi.dsi.fastutil.floats.FloatArrays;
import it.unimi.dsi.fastutil.floats.FloatSet;
import net.hollowed.antique.index.AntiqueEffects;
import net.hollowed.antique.util.interfaces.duck.FastAir;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Entity.class)
public abstract class EntityBounceMixin implements FastAir {

    @Shadow
    public abstract Vec3 getDeltaMovement();

    @Shadow
    public abstract void setDeltaMovement(double xd, double yd, double zd);

    @Shadow
    public abstract void setOnGround(boolean onGround);

    @Shadow public abstract AABB getBoundingBox();

    @Shadow public abstract Level level();

    @Shadow public abstract float maxUpStep();

    @Shadow public abstract boolean onGround();

    @Shadow private Level level;

    @Shadow public abstract boolean isSpectator();

    @Shadow public abstract boolean isInWater();

    @Shadow public abstract boolean isUnderWater();

    @Unique
    private boolean isFastAir;

    @Shadow private Vec3 position;
    @Unique
    private int tickCounter = 0; // Counter to track ticks

    @Unique
    private Vec3 lastTickPosition; // To store the entity's position at the end of the last tick

    public void antique$setFast(boolean bool) {
        this.isFastAir = bool;
    }

    public boolean antique$getFast() {
        return this.isFastAir;
    }

    /**
     * Capture the entity's position at the start of each tick (head inject).
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void captureStartPosition(CallbackInfo ci) {
        // Update lastTickPosition before the tick modifies entity position
        if (lastTickPosition == null) {
            lastTickPosition = this.position; // Initialize to current position on first tick
        }
    }

    /**
     * Apply the bounce effect and calculate the simulated velocity (tail inject).
     */
    @Inject(method = "tick", at = @At("TAIL"))
    private void applyBounceEffect(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof LivingEntity livingEntity) {
            Vec3 velocity = this.getDeltaMovement();
            Vec3 adjustedVec = this.adjustMovementForCollisions(velocity);

            boolean X = !Mth.equal(velocity.x, adjustedVec.x);
            boolean Z = !Mth.equal(velocity.z, adjustedVec.z);
            boolean Y = !Mth.equal(velocity.y, adjustedVec.y);

            if (livingEntity.hasEffect(AntiqueEffects.BOUNCE_EFFECT) || livingEntity.hasEffect(AntiqueEffects.VOLATILE_BOUNCE_EFFECT) || this.isFastAir && !this.isSpectator()) {
                if ((Object) this instanceof Player player && player.isFallFlying()) {
                    return;
                }

                double airResistanceCounter = 1.05;

                if (!this.onGround()) {
                    // Reduce air resistance
                    this.setDeltaMovement(velocity.x * airResistanceCounter, velocity.y, velocity.z * airResistanceCounter); // Slightly reduce resistance
                }

                if (this.isFastAir) {
                    return;
                }

                // Bounce logic
                if (X) {
                    this.setOnGround(false);
                    this.setDeltaMovement(-velocity.x, velocity.y, velocity.z);
                }
                if (Z) {
                    this.setOnGround(false);
                    this.setDeltaMovement(velocity.x, velocity.y, -velocity.z);
                }

                if (Y && velocity.length() > 0.4) {
                    this.setOnGround(false);
                    this.setDeltaMovement(velocity.x, -velocity.y * 0.8 + 0.125, velocity.z);
                }
                if (this.isInWater() && !this.isUnderWater()) {
                    this.setOnGround(false);
                    this.setDeltaMovement(velocity.x, -velocity.y * 0.99, velocity.z);
                }

                // Increment the tick counter and spawn particles every 10 ticks
                tickCounter++;

                // Calculate simulated velocity using last tick position
                Vec3 simulatedVelocity = this.position.subtract(lastTickPosition);

                if (tickCounter >= 5 && simulatedVelocity.length() > 0.4) {
                    tickCounter = 0; // Reset counter
                    spawnParticleRing(entity, simulatedVelocity);
                } else if (tickCounter >= 10 && simulatedVelocity.length() > 0.2) {
                    tickCounter = 0; // Reset counter
                    spawnParticleRing(entity, simulatedVelocity);
                }
            }
        }

        // Update lastTickPosition for use in the next tick
        lastTickPosition = this.position;
    }

    @Unique
    private void spawnParticleRing(Entity entity, Vec3 velocity) {
        Level world = entity.level();
        Vec3 entityPos = entity.position();
        Vec3 normalizedVelocity = velocity.normalize();

        int particleCount = 32; // Number of particles in the ring
        double radius = 0.75; // Radius of the particle ring

        // Define the white dust particle effect with a size of 1
        ParticleOptions whiteDustParticle = new DustParticleOptions(0xFFFFFF, 1.0f);

        // Find a perpendicular vector to the velocity to act as the "up" vector for the ring's plane
        Vec3 upVector = Math.abs(normalizedVelocity.y) < 0.999f ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
        Vec3 axis1 = normalizedVelocity.cross(upVector).normalize(); // Perpendicular to velocity
        Vec3 axis2 = normalizedVelocity.cross(axis1).normalize(); // Perpendicular to both velocity and axis1

        for (int i = 0; i < particleCount; i++) {
            double angle = (2 * Math.PI / particleCount) * i;
            double xOffset = Math.cos(angle) * radius;
            double zOffset = Math.sin(angle) * radius;

            // Position the particle along the ring, rotated to align with velocity
            Vec3 particleOffset = axis1.scale(xOffset).add(axis2.scale(zOffset));
            Vec3 particlePos = entityPos.add(particleOffset);

            // Spawn the particle at the entity's position
            if (world instanceof ServerLevel serverWorld) {
                serverWorld.sendParticles(whiteDustParticle, particlePos.x, particlePos.y + 1, particlePos.z, 1, 0, 0, 0, 0);
            }
        }
    }

    @SuppressWarnings("all")
    @Unique
    private Vec3 adjustMovementForCollisions(Vec3 movement) {
        AABB box = this.getBoundingBox();
        List<VoxelShape> list = this.level().getEntityCollisions((Entity) (Object) this, box.expandTowards(movement));
        Vec3 vec3d = movement.lengthSqr() == 0.0 ? movement : adjustMovementForCollisions((Entity) (Object) this, movement, box, this.level(), list);
        boolean bl = movement.x != vec3d.x;
        boolean bl2 = movement.y != vec3d.y;
        boolean bl3 = movement.z != vec3d.z;
        boolean bl4 = bl2 && movement.y < 0.0;
        if (this.maxUpStep() > 0.0F && (bl4 || this.onGround()) && (bl || bl3)) {
            AABB box2 = bl4 ? box.move(0.0, vec3d.y, 0.0) : box;
            AABB box3 = box2.expandTowards(movement.x, this.maxUpStep(), movement.z);
            if (!bl4) {
                box3 = box3.expandTowards(0.0, -9.999999747378752E-6, 0.0);
            }

            List<VoxelShape> list2 = findCollisionsForMovement((Entity) (Object) this, this.level, list, box3);
            float f = (float)vec3d.y;
            float[] fs = collectStepHeights(box2, list2, this.maxUpStep(), f);

            for (float g : fs) {
                Vec3 vec3d2 = adjustMovementForCollisions(new Vec3(movement.x, g, movement.z), box2, list2);
                if (vec3d2.horizontalDistanceSqr() > vec3d.horizontalDistanceSqr()) {
                    double d = box.minY - box2.minY;
                    return vec3d2.add(0.0, -d, 0.0);
                }
            }
        }

        return vec3d;
    }

    @Unique
    private Vec3 adjustMovementForCollisions(@Nullable Entity entity, Vec3 movement, AABB entityBoundingBox, Level world, List<VoxelShape> collisions) {
        List<VoxelShape> list = findCollisionsForMovement(entity, world, collisions, entityBoundingBox.expandTowards(movement));
        return adjustMovementForCollisions(movement, entityBoundingBox, list);
    }

    @Unique
    private static Vec3 adjustMovementForCollisions(Vec3 movement, AABB entityBoundingBox, List<VoxelShape> collisions) {
        if (collisions.isEmpty()) {
            return movement;
        } else {
            double d = movement.x;
            double e = movement.y;
            double f = movement.z;
            if (e != 0.0) {
                e = Shapes.collide(Direction.Axis.Y, entityBoundingBox, collisions, e);
                if (e != 0.0) {
                    entityBoundingBox = entityBoundingBox.move(0.0, e, 0.0);
                }
            }

            boolean bl = Math.abs(d) < Math.abs(f);
            if (bl && f != 0.0) {
                f = Shapes.collide(Direction.Axis.Z, entityBoundingBox, collisions, f);
                if (f != 0.0) {
                    entityBoundingBox = entityBoundingBox.move(0.0, 0.0, f);
                }
            }

            if (d != 0.0) {
                d = Shapes.collide(Direction.Axis.X, entityBoundingBox, collisions, d);
                if (!bl && d != 0.0) {
                    entityBoundingBox = entityBoundingBox.move(d, 0.0, 0.0);
                }
            }

            if (!bl && f != 0.0) {
                f = Shapes.collide(Direction.Axis.Z, entityBoundingBox, collisions, f);
            }

            return new Vec3(d, e, f);
        }
    }

    @Unique
    private static float[] collectStepHeights(AABB collisionBox, List<VoxelShape> collisions, float f, float stepHeight) {
        FloatSet floatSet = new FloatArraySet(4);

        for (VoxelShape voxelShape : collisions) {
            DoubleList doubleList = voxelShape.getCoords(Direction.Axis.Y);

            for (double d : doubleList) {
                float g = (float) (d - collisionBox.minY);
                if (!(g < 0.0F) && g != stepHeight) {
                    if (g > f) {
                        break;
                    }

                    floatSet.add(g);
                }
            }
        }

        float[] fs = floatSet.toFloatArray();
        FloatArrays.unstableSort(fs);
        return fs;
    }

    @Unique
    private static List<VoxelShape> findCollisionsForMovement(@Nullable Entity entity, Level world, List<VoxelShape> regularCollisions, AABB movingEntityBoundingBox) {
        ImmutableList.Builder<VoxelShape> builder = ImmutableList.builderWithExpectedSize(regularCollisions.size() + 1);
        if (!regularCollisions.isEmpty()) {
            builder.addAll(regularCollisions);
        }

        WorldBorder worldBorder = world.getWorldBorder();
        boolean bl = entity != null && worldBorder.isInsideCloseToBorder(entity, movingEntityBoundingBox);
        if (bl) {
            builder.add(worldBorder.getCollisionShape());
        }

        builder.addAll(world.getBlockCollisions(entity, movingEntityBoundingBox));
        return builder.build();
    }
}
