package net.hollowed.antique.citrus_physics.component.collision.colliders;

import net.hollowed.antique.citrus_physics.component.ActorComponent;
import net.hollowed.antique.citrus_physics.component.collision.CollisionResult;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.Optional;

public abstract class Collider {

    public AABB bounds;

    public abstract ColliderType getType();

    public void solve(ActorComponent actor, Optional<ActorComponent> optionalOtherActor, Vector3dc otherPos, Collider otherCollider) {
        var optionalCollisionResult = getResult(new Vector3d(actor.position), new Vector3d(otherPos), otherCollider);
        optionalCollisionResult.ifPresent(collisionResult -> SolveCollisionResult(collisionResult, actor, optionalOtherActor));
    }

    public void SolveCollisionResult(CollisionResult collisionResult, ActorComponent actor, Optional<ActorComponent> optionalOtherActor) {

        // TODO: this should be changed to include mass
        var moveAmountA = 0.5;
        var moveAmountB = 1 - moveAmountA;

        // This does not need to effect B since B is empty... Dummy...
        if(optionalOtherActor.isEmpty()) moveAmountA = 1;

        actor.position.add(collisionResult.resolutionVector.mul(moveAmountA, new Vector3d()));

        if(optionalOtherActor.isPresent()) {
            var otherActor = optionalOtherActor.get();
            otherActor.position.sub(collisionResult.resolutionVector.mul(moveAmountB, new Vector3d()));
        }
    }

    public abstract  Optional<CollisionResult> getResult(Vector3dc pos, Vector3dc otherPos, Collider otherCollider);

    public enum ColliderType {
        BOX,
        POINT,
        SPHERE
    }
}
