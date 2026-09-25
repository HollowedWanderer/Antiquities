package net.hollowed.antique.citrus_physics.solver;

import dev.dominion.ecs.api.Dominion;
import net.hollowed.antique.citrus_physics.PhysicsWorld;

public abstract class Solver {

    Dominion ecsWorld;
    PhysicsWorld world;

    public Solver(PhysicsWorld world, Dominion ecs) {
        this.world = world;
        this.ecsWorld = ecs;
    }

    public abstract void solve(double deltaTime);

}
