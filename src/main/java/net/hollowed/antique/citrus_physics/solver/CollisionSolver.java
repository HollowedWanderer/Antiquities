package net.hollowed.antique.citrus_physics.solver;

import dev.dominion.ecs.api.Dominion;
import net.hollowed.antique.citrus_physics.PhysicsWorld;
import net.hollowed.antique.citrus_physics.component.ActorComponent;
import net.hollowed.antique.citrus_physics.component.collision.containers.ColliderContainerComponent;
import net.hollowed.antique.citrus_physics.component.collision.containers.EntityColliderContainer;
import net.hollowed.antique.citrus_physics.component.collision.containers.ParticleColliderContainer;
import net.hollowed.antique.citrus_physics.component.collision.containers.WorldColliderContainer;

public class CollisionSolver extends Solver {

    public CollisionSolver(PhysicsWorld world, Dominion ecs) { super(world, ecs); }

    public void solve(double deltaTime) {
        for (int i = 0; i < PhysicsWorld.COLLISION_RESOLUTION; i++) {
            ecsWorld.findEntitiesWith(ActorComponent.class, EntityColliderContainer.class).stream().forEach(result -> runForType(result.comp1(), result.comp2(), deltaTime));
            ecsWorld.findEntitiesWith(ActorComponent.class, WorldColliderContainer.class).stream().forEach(result -> runForType(result.comp1(), result.comp2(), deltaTime));
            ecsWorld.findEntitiesWith(ActorComponent.class, ParticleColliderContainer.class).stream().forEach(result -> runForType(result.comp1(), result.comp2(), deltaTime));
        }
    }

    public void runForType(ActorComponent actorComponent, ColliderContainerComponent colliderComponent, double deltaTime) {
        colliderComponent.solve(world, actorComponent, deltaTime / PhysicsWorld.COLLISION_RESOLUTION);
    }

}
