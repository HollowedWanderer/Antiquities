package net.hollowed.antique.citrus_physics.component;

import org.joml.Vector3d;

public class ActorComponent {

    public Vector3d position;
    public Vector3d positionCache;
    public Vector3d acceleration;
    public double mass;

    public ActorComponent(Vector3d initialPosition, double mass) {
        this.mass = mass;
        this.position = new Vector3d(initialPosition);
        this.positionCache = new Vector3d(initialPosition);
        this.acceleration = new Vector3d();
    }


}
