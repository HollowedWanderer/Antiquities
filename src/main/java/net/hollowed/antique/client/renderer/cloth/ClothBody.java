package net.hollowed.antique.client.renderer.cloth;

import net.hollowed.antique.entities.parts.MyriadShovelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClothBody {

    Vector3d pos;
    Vector3d posCache;
    Vector3d accel = new Vector3d();
    boolean isPinned = false;

    public ClothBody(Vector3d worldPos) {
        pos = new Vector3d(worldPos);
        posCache = new Vector3d(worldPos);
    }

    public void update(double delta) {
        Vector3d velocity = new Vector3d(pos).sub(posCache).mul(0.96); // Apply drag here
        posCache.set(pos);
        Vector3d accelerationTerm = new Vector3d(accel).mul(delta * 0.5);
        if (!isPinned) {
            pos.add(velocity).add(accelerationTerm);
        }
        accel.zero();
    }

    public void containDistance(ClothBody other, double restLength) {
        Vector3d axis = new Vector3d(pos).sub(other.pos);
        double dist = axis.length();

        if (dist == 0.0) return;

        double delta = restLength - dist;

        // Normalize the axis and scale by delta * 0.5 for even correction
        Vector3d correction = axis.normalize().mul(delta * 0.7);

        // Apply the correction
//        if (!isPinned) pos.add(correction);
        other.pos.sub(correction);
    }

    public Vector3d getPos() {
        return new Vector3d(pos);
    }

    public Vector3d entityCollisionPerchance(ClientLevel world, Entity except) {
        double padding = 0.075;

        Vec3 startPos = new Vec3(pos.x, pos.y, pos.z);

        Map<AABB, Entity> collBoxes = new HashMap<>();
        for (Entity entity : world.getEntities(except, new AABB(startPos.subtract(0.1), startPos.add(0.1)))) {
            if (!(entity instanceof MyriadShovelPart)) {
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

        if (!isPinned && collisionAccel.length() < 0.15) {
            pos = new Vector3d(x + dx, y + dy, z + dz);
        }
        return new Vector3d(accel).add(collisionAccel);
    }

    public void slideOutOfBlocks(ClientLevel world) {
        double padding = 0.00075;

        Vec3 startPos = new Vec3(pos.x, pos.y, pos.z);
        BlockPos blockPos = BlockPos.containing(startPos);
        BlockState state = world.getBlockState(blockPos);

        // If there's no collision shape, just return the original point
        if (state.isAir()) return;
        VoxelShape shape = state.getCollisionShape(world, blockPos);
        if (shape.isEmpty()) return;

        // Convert the shape to world space
        VoxelShape worldShape = shape.move(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        List<AABB> collBoxes = worldShape.toAabbs();

        // We'll treat the point as an itty-bitty bounding box
        double x = startPos.x;
        double y = startPos.y;
        double z = startPos.z;

        // Build a small bounding box around the point
        AABB pointBox = new AABB(x - padding, y - padding, z - padding, x + padding, y + padding, z + padding);

        // Try sliding out by checking overlaps
        double dx = 0, dy = 0, dz = 0;
        for (AABB box : collBoxes) {
            if (box.intersects(pointBox)) {
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

        if (!isPinned) {
            pos = new Vector3d(x + dx, y + dy, z + dz);
        }
    }

    // Helper to find overlap between two intervals
    private double getOverlap(double minA, double maxA, double minB, double maxB) {
        if (maxA <= minB || minA >= maxB) return 0.0;  // no overlap
        double push1 = maxB - minA;  // push forward
        double push2 = minB - maxA;  // push backward
        return Math.abs(push1) < Math.abs(push2) ? push1 : push2;
    }
}
