package net.hollowed.antique.citrus_physics.component.constraint.single;

import net.hollowed.antique.citrus_physics.component.ActorComponent;
import net.hollowed.antique.citrus_physics.component.constraint.SingleBodyConstraint;
import org.joml.Vector3d;

public class GravityConstraint extends SingleBodyConstraint {

    public Vector3d gravity = new Vector3d(0, -1, 0);

    public GravityConstraint(float accel) {
        gravity.mul(accel);
    }

    public void solve(ActorComponent actor, double deltaTime) {
        actor.addForce(gravity.div(actor.invMass));
    }
}
