package net.hollowed.antique.citrus_physics.solver;

import dev.dominion.ecs.api.Dominion;
import net.hollowed.antique.citrus_physics.PhysicsWorld;
import net.hollowed.antique.citrus_physics.component.ActorComponent;
import org.joml.Vector3d;

public class GenericsSolver extends Solver {

    public GenericsSolver(PhysicsWorld world, Dominion ecs) { super(world, ecs); }

    public void solve(double deltaTime) {

        ecsWorld.findEntitiesWith(ActorComponent.class).stream().forEach(result -> {
            ActorComponent actor = result.comp();

            Vector3d acceleration = actor.accumForce.mul(actor.invMass, new Vector3d());

            actor.accumForce = new Vector3d();

            actor.velocity.add(acceleration.mul(deltaTime, new Vector3d()));
            actor.position.add(actor.velocity.mul(deltaTime, new Vector3d()));
        });
    }

}
