package net.hollowed.antique.citrus_physics.component.collision;

import org.joml.Vector3d;

public class CollisionResult {

    public Vector3d collisionPoint;
    public Vector3d resolutionVector; // the direction of collision, move each body half this

    public CollisionResult(Vector3d collisionPoint, Vector3d resolutionVector) {
        this.collisionPoint = collisionPoint;
        this.resolutionVector = resolutionVector;
    }
}
