package net.hollowed.antique.citrus_physics.component.collision.containers;

import net.hollowed.antique.citrus_physics.PhysicsWorld;
import net.hollowed.antique.citrus_physics.component.ActorComponent;
import net.hollowed.antique.citrus_physics.component.collision.colliders.Collider;

import java.util.ArrayList;

public abstract class ColliderContainerComponent {

    public ArrayList<Collider> shapes = new ArrayList<>();

    public abstract void solve(PhysicsWorld physics, ActorComponent actor, double deltaTime);

}
