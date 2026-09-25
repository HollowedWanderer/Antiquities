package net.hollowed.antique.citrus_physics.component.constraint.multi;

import dev.dominion.ecs.api.Entity;
import net.hollowed.antique.citrus_physics.component.ActorComponent;
import net.hollowed.antique.citrus_physics.component.constraint.DoubleBodyConstraint;
import org.joml.Vector3d;

public class FixedDistanceConstraint extends DoubleBodyConstraint {

    public double distance;

    public FixedDistanceConstraint(Entity other, double distance) {
        super(other);
        this.distance = distance;
    }

    public void solve(ActorComponent actor, double deltaTime) {
        var otherActor = other.get(ActorComponent.class);

        if(actor.position.distance(otherActor.position) > distance) {
            Vector3d axis = actor.position.sub(otherActor.position, new Vector3d());
            double dist = axis.length();
            Vector3d norm = axis.div(dist, new Vector3d());
            double delta = distance - dist;
            actor.position.add(norm.mul(0.5 * delta, new Vector3d()));
            otherActor.position.sub(norm.mul(0.5 * delta, new Vector3d()));
        }
    }

}
