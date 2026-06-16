package net.hollowed.antique.client.renderer.cloth;

import net.hollowed.antique.client.cloth.ClothOwner;
import net.hollowed.antique.util.resources.ClothSkinData;
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
import org.joml.Vector3f;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ClothBody {

    public Vector3f pos;
    public @Nullable Vector3f prevPos;
    public Vector3f renderPos;
    public final Vector3f velocity = new Vector3f();

    public ClothBody(Vector3f worldPos) {
        pos = new Vector3f(worldPos);
        renderPos = new Vector3f(worldPos);
    }

    public BlockPos blockPos() {
        return BlockPos.containing(pos.x, pos.y, pos.z);
    }

    public void update() {
        pos.add(new Vector3f(velocity).mul(0.96f)); // TODO drag
        velocity.zero();
    }

    public void reset(Vector3f pos) {
        prevPos = null;
        this.pos = pos;
        velocity.zero();
    }

    public void containDistance(Vector3f prev, float tickDelta, float restLength) {
        Vector3f dir = new Vector3f(getPos(tickDelta)).sub(prev);

        if (dir.equals(0, 0, 0)) {
            return;
        }

        renderPos = new Vector3f(prev).add(dir.normalize(restLength));
    }

    public void containDistance(Vector3f prev, float restLength) {
        Vector3f dir = new Vector3f(pos).sub(prev);

        if (dir.equals(0, 0, 0)) {
            return;
        }

        pos.set(prev).add(dir.normalize(restLength));
    }

    public Vector3f getPos(float tickDelta) {
        return prevPos == null ? new Vector3f(pos) : new Vector3f(prevPos).lerp(pos, tickDelta);
    }

    public void slideOutOfEntities(List<Entity> collisionEntities, Entity except, ClothSkinData skin) {
        Map<AABB, Entity> collBoxes = new HashMap<>();
        for (Entity entity : collisionEntities) {
            if (!Objects.equals(entity, except)) {
                collBoxes.put(entity.getBoundingBox(), entity);
            }
        }

        // Build a small bounding box around the point
        AABB pointBox = new AABB(pos.x - skin.width(), pos.y - skin.width(), pos.z - skin.width(), pos.x + skin.width(), pos.y + skin.width(), pos.z + skin.width());

        for (AABB box : collBoxes.keySet()) {
            if (box.intersects(pointBox)) {
                float xOverlap = getOverlap(pointBox.minX, pointBox.maxX, box.minX, box.maxX);
                float yOverlap = getOverlap(pointBox.minY, pointBox.maxY, box.minY, box.maxY);
                float zOverlap = getOverlap(pointBox.minZ, pointBox.maxZ, box.minZ, box.maxZ);

                // Pick the smallest overlap to push out
                if (Math.abs(xOverlap) < Math.abs(yOverlap) && Math.abs(xOverlap) < Math.abs(zOverlap)) {
                    pos.add(xOverlap, 0, 0);
                    pointBox = pointBox.move(xOverlap, 0, 0);
                } else if (Math.abs(yOverlap) < Math.abs(zOverlap)) {
                    pos.add(0, yOverlap, 0);
                    pointBox = pointBox.move(0, yOverlap, 0);
                } else {
                    pos.add(0, 0, zOverlap);
                    pointBox = pointBox.move(0, 0, zOverlap);
                }
            }
        }
    }

    public void slideOutOfBlocks(Level level, ClothOwner owner, ClothSkinData skin) {
        BlockPos exceptPos = null;

        if (owner.asEntity() instanceof BlockAttachedEntity block) {
            exceptPos = block.getPos();
        }

        Vec3 startPos = new Vec3(pos.x, pos.y, pos.z);

        // Build a small bounding box around the point
        AABB pointBox = new AABB(startPos.x - skin.width(), startPos.y - skin.width(), startPos.z - skin.width(), startPos.x + skin.width(), startPos.y + skin.width(), startPos.z + skin.width());

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
                    float xOverlap = getOverlap(pointBox.minX, pointBox.maxX, box.minX, box.maxX);
                    float yOverlap = getOverlap(pointBox.minY, pointBox.maxY, box.minY, box.maxY);
                    float zOverlap = getOverlap(pointBox.minZ, pointBox.maxZ, box.minZ, box.maxZ);

                    if (Math.abs(xOverlap) < Math.abs(yOverlap) && Math.abs(xOverlap) < Math.abs(zOverlap)) {
                        pos.add(xOverlap, 0, 0);
                        pointBox = pointBox.move(xOverlap, 0, 0);
                    } else if (Math.abs(yOverlap) < Math.abs(zOverlap)) {
                        pos.add(0, yOverlap, 0);
                        pointBox = pointBox.move(0, yOverlap, 0);
                    } else {
                        pos.add(0, 0, zOverlap);
                        pointBox = pointBox.move(0, 0, zOverlap);
                    }
                }
            }
        }
    }

    private float getOverlap(double minA, double maxA, double minB, double maxB) {
        if (maxA <= minB || minA >= maxB) return 0f;
        double push1 = maxB - minA;
        double push2 = minB - maxA;
        return (float) (Math.abs(push1) < Math.abs(push2) ? push1 : push2);
    }

    @Override
    public String toString() {
        return pos.toString(NumberFormat.getInstance());
    }
}
