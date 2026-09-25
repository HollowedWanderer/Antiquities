package net.hollowed.antique.citrus_physics.component.constraint;

import dev.dominion.ecs.api.Entity;

public abstract class DoubleBodyConstraint extends Constraint {

    public Entity other;

    public DoubleBodyConstraint(Entity other) {this.other = other;}

}
