package net.hollowed.antique.citrus_physics.component.constraint.single;

import net.hollowed.antique.citrus_physics.component.ActorComponent;
import net.hollowed.antique.citrus_physics.component.constraint.SingleBodyConstraint;
import org.joml.Vector3d;

public class StaticConstraint extends SingleBodyConstraint {

    public Vector3d position;

    public StaticConstraint(Vector3d lockPos) {
        position = lockPos;
    }

    public void solve(ActorComponent actor, double deltaTime) {
        actor.position = new Vector3d(position);
    }
}
