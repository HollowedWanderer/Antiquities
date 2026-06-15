package net.hollowed.antique.client.renderer.cloth;

import net.hollowed.antique.client.cloth.ClothOwner;
import net.minecraft.core.BlockBox;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ClothBody {

    public Vector3d pos;
    public @Nullable Vector3d prevPos;
    public final Vector3d velocity = new Vector3d();

    public ClothBody(Vector3d worldPos) {
        pos = new Vector3d(worldPos);
    }

    public BlockPos blockPos() {
        return BlockPos.containing(pos.x, pos.y, pos.z);
    }

    public void update() {
        pos.add(new Vector3d(velocity).mul(0.96)); // TODO drag
        velocity.zero();
    }

    public void reset(Vector3d pos) {
        prevPos = null;
        this.pos = pos;
        velocity.zero();
    }

    public void containDistance(Vector3d prev, double restLength) {
        Vector3d axis = new Vector3d(pos).sub(prev);
        double dist = axis.length();

        if (dist == 0.0) return;

        double delta = restLength - dist;

        // Normalize the axis and scale by delta for even correction
        Vector3d correction = axis.normalize().mul(delta);

        // Apply the correction
//        if (!isPinned) pos.add(correction);
        pos.add(correction);
    }

    public Vector3d getPos(float tickDelta) {
        return prevPos == null ? new Vector3d(pos) : new Vector3d(prevPos).lerp(pos, tickDelta);
    }

    public Vector3d entityCollisionPerchance(List<Entity> collisionEntities, Entity except) {
        double padding = 0.075;

        Vec3 startPos = new Vec3(pos.x, pos.y, pos.z);

        Map<AABB, Entity> collBoxes = new HashMap<>();
        for (Entity entity : collisionEntities) {
            if (!Objects.equals(entity, except)) {
                collBoxes.put(entity.getBoundingBox(), entity);
            }
        }

        // We'll treat the point as an itty-bitty bounding box
        double x = startPos.x;
        double y = startPos.y;
        double z = startPos.z;

        // Build a small bounding box around the point
        AABB pointBox = new AABB(x - padding, y - padding, z - padding, x + padding, y + padding, z + padding);

        // Try sliding out by checking overlaps
        double dx = 0, dy = 0, dz = 0;
        Vector3d collisionAccel = new Vector3d();
        for (AABB box : collBoxes.keySet()) {
            if (box.intersects(pointBox)) {
                Vec3 vel = collBoxes.get(box).getDeltaMovement();
                collisionAccel = new Vector3d(vel.x, vel.y, vel.z).mul(1.75);

                double xOverlap = getOverlap(pointBox.minX, pointBox.maxX, box.minX, box.maxX);
                double yOverlap = getOverlap(pointBox.minY, pointBox.maxY, box.minY, box.maxY);
                double zOverlap = getOverlap(pointBox.minZ, pointBox.maxZ, box.minZ, box.maxZ);

                // Pick the smallest overlap to push out
                if (Math.abs(xOverlap) < Math.abs(yOverlap) && Math.abs(xOverlap) < Math.abs(zOverlap)) {
                    dx += xOverlap;
                } else if (Math.abs(yOverlap) < Math.abs(zOverlap)) {
                    dy += yOverlap;
                } else {
                    dz += zOverlap;
                }
            }
        }

        if (collisionAccel.length() < 0.15) {
            pos = new Vector3d(x + dx, y + dy, z + dz);
        }
        return new Vector3d(velocity).add(collisionAccel);
    }

    public void slideOutOfBlocks(Level level, ClothOwner owner) {
        double padding = 1.0 / 16;

        BlockPos exceptPos = null;

        if (owner.asEntity() instanceof BlockAttachedEntity block) {
            exceptPos = block.getPos();
        }

        Vec3 startPos = new Vec3(pos.x, pos.y, pos.z);

        // Itty-bitty bounding box
        double x = startPos.x;
        double y = startPos.y;
        double z = startPos.z;

        // Build a small bounding box around the point
        AABB pointBox = new AABB(x - padding, y - padding, z - padding, x + padding, y + padding, z + padding);

        for (BlockPos blockPos : BlockBox.of(BlockPos.containing(pointBox.minX, pointBox.minY, pointBox.minZ), BlockPos.containing(pointBox.maxX, pointBox.maxY, pointBox.maxZ))) {
            if (Objects.equals(blockPos, exceptPos)) {
                continue;
            }

            BlockState state = level.getBlockState(blockPos);

            if (state.isAir()) continue;
            VoxelShape shape = state.getCollisionShape(level, blockPos);
            if (shape.isEmpty()) continue;

            // Convert the shape to world space
            VoxelShape worldShape = shape.move(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            List<AABB> collBoxes = worldShape.toAabbs();

            // Try sliding out by checking overlaps
            for (AABB box : collBoxes) {
                if (box.intersects(pointBox)) {
                    double xOverlap = getOverlap(pointBox.minX, pointBox.maxX, box.minX, box.maxX);
                    double yOverlap = getOverlap(pointBox.minY, pointBox.maxY, box.minY, box.maxY);
                    double zOverlap = getOverlap(pointBox.minZ, pointBox.maxZ, box.minZ, box.maxZ);

                    // Pick the smallest overlap to push out
                    if (Math.abs(xOverlap) < Math.abs(yOverlap) && Math.abs(xOverlap) < Math.abs(zOverlap)) {
                        x += xOverlap;
                        pointBox = pointBox.move(xOverlap, 0, 0);
                    } else if (Math.abs(yOverlap) < Math.abs(zOverlap)) {
                        y += yOverlap;
                        pointBox = pointBox.move(0, yOverlap, 0);
                    } else {
                        z += zOverlap;
                        pointBox = pointBox.move(0, 0, zOverlap);
                    }
                }
            }
        }

        pos.set(x, y, z);
    }

    private double getOverlap(double minA, double maxA, double minB, double maxB) {
        if (maxA <= minB || minA >= maxB) return 0.0;
        double push1 = maxB - minA;
        double push2 = minB - maxA;
        return Math.abs(push1) < Math.abs(push2) ? push1 : push2;
    }

    @Override
    public String toString() {
        return pos.toString(NumberFormat.getInstance());
    }
}
