package net.hollowed.antique.citrus_physics.component.collision.colliders;

import net.hollowed.antique.citrus_physics.component.collision.CollisionHelper;
import net.hollowed.antique.citrus_physics.component.collision.CollisionResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.Optional;

public class BoxCollider extends Collider {

    public BoxCollider(Vector3d size) {
        bounds = new AABB(Vec3.ZERO, new Vec3(size.x, size.y, size.z));
    }
    public BoxCollider(AABB copy) {
        bounds = new AABB(Vec3.ZERO, new Vec3(copy.getXsize(), copy.getYsize(), copy.getZsize()));
    }

    public ColliderType getType() { return ColliderType.BOX; }

    public Optional<CollisionResult> getResult(Vector3dc pos, Vector3dc otherPos, Collider otherCollider) {
        switch (otherCollider.getType()) {
            case SPHERE -> { return Optional.ofNullable(CollisionHelper.collideAABBSphere(pos, this, otherPos, (SphereCollider) otherCollider)); }
            case BOX -> { return Optional.ofNullable(CollisionHelper.collideAABBAABB(pos, this, otherPos, (BoxCollider) otherCollider)); }
            case POINT -> { return Optional.ofNullable(CollisionHelper.collideAABBPoint(pos, this, otherPos)); }
        }

        return Optional.empty();
    }
}
