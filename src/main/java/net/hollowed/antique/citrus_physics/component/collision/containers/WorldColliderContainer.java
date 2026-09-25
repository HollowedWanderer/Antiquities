package net.hollowed.antique.citrus_physics.component.collision.containers;

import net.hollowed.antique.citrus_physics.PhysicsWorld;
import net.hollowed.antique.citrus_physics.component.ActorComponent;
import net.hollowed.antique.citrus_physics.component.collision.colliders.BoxCollider;
import net.hollowed.antique.citrus_physics.component.collision.colliders.Collider;
import net.hollowed.antique.util.math.conversion.JomlToMC;
import net.hollowed.antique.util.math.conversion.McToJoml;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class WorldColliderContainer extends ColliderContainerComponent {

    public void solve(PhysicsWorld physics, ActorComponent actor, double deltaTime) {
        var ecsWorld = physics.ecsWorld;

        for (Collider shape : shapes) {
            var world = physics.world;
            var bounds = shape.bounds;

            for (BlockPos blockPos : BlockPos.betweenClosed(bounds.move(JomlToMC.fromVector3d(actor.position)))) {
                var state = world.getBlockState(blockPos);
                var centerPos = new Vec3(blockPos).add(0.5, 0.5, 0.5);
                var voxelShape = state.getCollisionShape(world, blockPos);

                if(voxelShape.isEmpty()) continue;

                var collisionShape = voxelShape.bounds();
                var fakeCollider = new BoxCollider(collisionShape);

                var collisionResult = shape.getResult(actor.position, McToJoml.fromVec3(centerPos), fakeCollider);
                collisionResult.ifPresent(result -> shape.SolveCollisionResult(result, actor, Optional.empty()));
            }
        }
    }

}
