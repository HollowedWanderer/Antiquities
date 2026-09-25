package net.hollowed.antique.citrus_physics.component.collision;

import net.hollowed.antique.citrus_physics.component.collision.colliders.BoxCollider;
import net.hollowed.antique.citrus_physics.component.collision.colliders.SphereCollider;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class CollisionHelper {

    public static CollisionResult collideAABBSphere(Vector3dc boxPos, BoxCollider box, Vector3dc spherePos, SphereCollider sphere) { return collideSphereAABB(spherePos, sphere, boxPos, box); }
    public static CollisionResult collideSphereAABB(Vector3dc spherePos, SphereCollider sphere, Vector3dc boxPos, BoxCollider box) {
        double closestX = Math.max(box.bounds.minX + boxPos.x(), Math.min(spherePos.x(), box.bounds.maxX + boxPos.x()));
        double closestY = Math.max(box.bounds.minY + boxPos.y(), Math.min(spherePos.y(), box.bounds.maxY + boxPos.y()));
        double closestZ = Math.max(box.bounds.minZ + boxPos.z(), Math.min(spherePos.z(), box.bounds.maxZ + boxPos.z()));
        Vector3d collisionPoint = new Vector3d(closestX, closestY, closestZ);

        Vector3d resolutionVector = new Vector3d(spherePos).sub(collisionPoint);
        resolutionVector.normalize().mul(sphere.radius - spherePos.distance(collisionPoint));

        return new CollisionResult(collisionPoint, resolutionVector);

//        double distanceSquared = spherePos.distanceSquared(boxPos);
//        double radiusSum = sphere.radius + (box.bounds.getAverageSideLength() * 0.5);
//        if (distanceSquared > (radiusSum * radiusSum)) return null;
//
//        Vector3d resolutionVector = new Vector3d(boxPos).sub(spherePos);
//        resolutionVector.normalize().mul(radiusSum - Math.sqrt(distanceSquared));
//
//        return new CollisionResult(new Vector3d(spherePos).lerp(boxPos, 0.5), resolutionVector);

    }

    public static CollisionResult collidePointAABB(Vector3dc pointPos, Vector3dc boxPos, BoxCollider box){ return collideAABBPoint(boxPos, box, pointPos); }
    public static CollisionResult collideAABBPoint(Vector3dc boxPos, BoxCollider box, Vector3dc pointPos) {
        boolean inside = (pointPos.x() >= box.bounds.minX + boxPos.x() && pointPos.x() <= box.bounds.maxX + boxPos.x() &&
                pointPos.y() >= box.bounds.minY + boxPos.y() && pointPos.y() <= box.bounds.maxY + boxPos.y() &&
                pointPos.z() >= box.bounds.minZ + boxPos.z() && pointPos.z() <= box.bounds.maxZ + boxPos.z());
        if (!inside) return null;
        return new CollisionResult(new Vector3d(pointPos), new Vector3d());
    }

    public static CollisionResult collidePointSphere(Vector3dc pointPos, Vector3dc spherePos, SphereCollider sphere){ return collideSpherePoint(spherePos, sphere, pointPos); }
    public static CollisionResult collideSpherePoint(Vector3dc spherePos, SphereCollider sphere, Vector3dc pointPos) {
        double distanceSquared = spherePos.distanceSquared(pointPos);
        if (distanceSquared > sphere.radius * sphere.radius) return null;

        Vector3d resolutionVector = new Vector3d(pointPos).sub(spherePos);
        resolutionVector.normalize().mul(sphere.radius - Math.sqrt(distanceSquared));

        return new CollisionResult(new Vector3d(pointPos), resolutionVector);
    }

    public static CollisionResult collideSphereSphere(Vector3dc pos1, SphereCollider sphere1, Vector3dc pos2, SphereCollider sphere2) {
        double distanceSquared = pos1.distanceSquared(pos2);
        double radiusSum = sphere1.radius + sphere2.radius;
        if (distanceSquared > radiusSum * radiusSum) return null;

        Vector3d resolutionVector = new Vector3d(pos2).sub(pos1);
        resolutionVector.normalize().mul(radiusSum - Math.sqrt(distanceSquared));

        return new CollisionResult(new Vector3d(pos1).lerp(pos2, 0.5), resolutionVector);
    }

    public static CollisionResult collideAABBAABB(Vector3dc pos1, BoxCollider box1, Vector3dc pos2, BoxCollider box2) {
        double overlapX = Math.min(box1.bounds.maxX + pos1.x(), box2.bounds.maxX + pos2.x()) - Math.max(box1.bounds.minX + pos1.x(), box2.bounds.minX + pos2.x());
        double overlapY = Math.min(box1.bounds.maxY + pos1.y(), box2.bounds.maxY + pos2.y()) - Math.max(box1.bounds.minY + pos1.y(), box2.bounds.minY + pos2.y());
        double overlapZ = Math.min(box1.bounds.maxZ + pos1.z(), box2.bounds.maxZ + pos2.z()) - Math.max(box1.bounds.minZ + pos1.z(), box2.bounds.minZ + pos2.z());

        if (overlapX <= 0 || overlapY <= 0 || overlapZ <= 0) return null;

        Vector3d resolutionVector = new Vector3d(overlapX, overlapY, overlapZ);
        Vector3d collisionPoint = new Vector3d(
                (Math.max(box1.bounds.minX + pos1.x(), box2.bounds.minX + pos2.x()) + Math.min(box1.bounds.maxX + pos1.x(), box2.bounds.maxX + pos2.x())) / 2,
                (Math.max(box1.bounds.minY + pos1.y(), box2.bounds.minY + pos2.y()) + Math.min(box1.bounds.maxY + pos1.y(), box2.bounds.maxY + pos2.y())) / 2,
                (Math.max(box1.bounds.minZ + pos1.z(), box2.bounds.minZ + pos2.z()) + Math.min(box1.bounds.maxZ + pos1.z(), box2.bounds.maxZ + pos2.z())) / 2
        );

        return new CollisionResult(collisionPoint, resolutionVector);
    }

}
