package net.hollowed.antique.citrus_physics.component.collision.containers;

import net.hollowed.antique.citrus_physics.PhysicsWorld;
import net.hollowed.antique.citrus_physics.component.ActorComponent;
import net.hollowed.antique.citrus_physics.component.collision.colliders.Collider;

import java.util.Optional;

public class ParticleColliderContainer extends ColliderContainerComponent {

    public void solve(PhysicsWorld physics, ActorComponent actor, double deltaTime) {
        var ecsWorld = physics.ecsWorld;
        ecsWorld.findEntitiesWith(ActorComponent.class, ParticleColliderContainer.class).stream().forEach(result -> {
            var otherActor = result.comp1();
            if(otherActor != actor && otherActor != null && !otherActor.position.equals(actor.position)) {
                var container = result.comp2();

                if(container != null) {
                    for (Collider shape : shapes) {
                        for (Collider otherShape : container.shapes) {
                            shape.solve(actor, Optional.of(otherActor), otherActor.position, otherShape);
                        }
                    }
                }
            }
        });
    }

}
