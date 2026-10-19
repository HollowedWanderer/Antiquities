package net.hollowed.antique.citrus_physics.component.constraint.single;

import net.hollowed.antique.citrus_physics.component.ActorComponent;
import net.hollowed.antique.citrus_physics.component.constraint.SingleBodyConstraint;

public class AirResistanceConstraint extends SingleBodyConstraint {

    public double multiplier;

    public AirResistanceConstraint(double resistance) {
        multiplier = resistance;
    }

    public void solve(ActorComponent actor, double deltaTime) {
        //actor.acceleration.mul(multiplier);
    }
}
