package net.hollowed.antique.citrus_physics.component.constraint;

import net.hollowed.antique.citrus_physics.component.ActorComponent;

public abstract class Constraint {

    public abstract void solve(ActorComponent actor, double deltaTime);

}
