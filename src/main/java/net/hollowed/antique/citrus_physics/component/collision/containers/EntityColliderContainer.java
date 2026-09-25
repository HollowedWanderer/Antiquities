package net.hollowed.antique.citrus_physics.component.collision.containers;

import net.hollowed.antique.citrus_physics.PhysicsWorld;
import net.hollowed.antique.citrus_physics.component.ActorComponent;
import net.hollowed.antique.citrus_physics.component.collision.colliders.BoxCollider;
import net.hollowed.antique.citrus_physics.component.collision.colliders.Collider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

import java.util.Optional;

public class EntityColliderContainer extends ColliderContainerComponent {

    private Entity except = null;

    public EntityColliderContainer() {}

    public EntityColliderContainer(Entity except) {
        this.except = except;
    }

    public void solve(PhysicsWorld physics, ActorComponent actor, double deltaTime) {
        for (Collider shape : shapes) {
            var box = new AABB(shape.bounds.minX, shape.bounds.minY, shape.bounds.minZ, shape.bounds.maxX, shape.bounds.maxY, shape.bounds.maxZ).move(new Vec3(actor.position.x(), actor.position.y(), actor.position.z()));
            for (Entity entity : physics.world.getEntities(this.except, box)) {
                var entityShape = new BoxCollider(entity.getBoundingBox());
                var center = entity.getBoundingBox().getCenter();
                shape.solve(actor, Optional.empty(), new Vector3d(center.x, center.y, center.z), entityShape);
            }
        }
    }

}
