package net.hollowed.antique.citrus_physics.solver;

import dev.dominion.ecs.api.Dominion;
import net.hollowed.antique.citrus_physics.PhysicsWorld;
import net.hollowed.antique.citrus_physics.component.ActorComponent;
import net.hollowed.antique.citrus_physics.component.constraint.ConstraintComponent;

public class ConstraintSolver extends Solver {

    public ConstraintSolver(PhysicsWorld world, Dominion ecs) { super(world, ecs); }

    public void solve(double deltaTime) {
        for (int i = 0; i < PhysicsWorld.CONSTRAINT_RESOLUTION; i++) {
            ecsWorld.findEntitiesWith(ActorComponent.class, ConstraintComponent.class).stream().forEach(result -> {
                result.comp2().solve(result.comp1(), deltaTime / PhysicsWorld.CONSTRAINT_RESOLUTION);
            });
        }
    }

}
