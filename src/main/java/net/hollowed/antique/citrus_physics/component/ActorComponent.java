package net.hollowed.antique.citrus_physics.component;

import org.joml.Vector3d;

public class ActorComponent {

    public Vector3d position;
    public Vector3d velocity;
    public float invMass;

    public Vector3d accumForce;

    public ActorComponent(Vector3d initialPosition, float mass) {
        this.invMass = 1 / mass;
        this.position = new Vector3d(initialPosition);
        this.velocity = new Vector3d();
        this.accumForce = new Vector3d();
    }

    public ActorComponent(Vector3d initialPosition) {
        this.invMass = 0;
        this.position = new Vector3d(initialPosition);
        this.velocity = new Vector3d();
        this.accumForce = new Vector3d();
    }

    public void addForce(Vector3d force) {
        this.accumForce.add(force);
    }

    public void setAcceleration(Vector3d acceleration) {
        this.accumForce = acceleration.div(this.invMass, new Vector3d());
    }

    public float mass() {
        return invMass == 0 ? 0 : 1 / invMass;
    }
}
