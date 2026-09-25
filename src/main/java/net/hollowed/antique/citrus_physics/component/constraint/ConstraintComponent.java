package net.hollowed.antique.citrus_physics.component.constraint;

import net.hollowed.antique.citrus_physics.component.ActorComponent;

import java.util.ArrayList;
import java.util.Arrays;

public class ConstraintComponent {

    public ArrayList<Constraint> constraints = new ArrayList<>();

    public ConstraintComponent(Constraint... constraints) {
        this.constraints.addAll(Arrays.asList(constraints));
    }

    public void solve(ActorComponent actor, double deltaTime) {
        for (Constraint constraint : constraints) {
            constraint.solve(actor, deltaTime);
        }
    }

}
