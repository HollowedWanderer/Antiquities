package net.hollowed.antique.items;

import dev.dominion.ecs.api.Entity;
import net.hollowed.antique.citrus_physics.PhysicsWorld;
import net.hollowed.antique.citrus_physics.component.ActorComponent;
import net.hollowed.antique.citrus_physics.component.collision.colliders.SphereCollider;
import net.hollowed.antique.citrus_physics.component.constraint.multi.FixedDistanceConstraint;
import net.hollowed.antique.citrus_physics.component.constraint.single.GravityConstraint;
import net.hollowed.antique.citrus_physics.component.constraint.single.StaticConstraint;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.jspecify.annotations.NonNull;

public class TestStick extends Item {

    public TestStick(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult use(@NonNull Level level, @NonNull Player player, @NonNull InteractionHand hand) {

        var physics = PhysicsWorld.getFromWorld(level);

        Vector2ic size = new Vector2i(32, 32);
        double scale = 0.5;

        Vector3dc leftPos = new Vector3d(player.getX() + 5, player.getY(), player.getZ() + 5);
        Vector3dc rightPos = new Vector3d(player.getX() + (size.x() * scale) + 5, player.getY(), player.getZ() + scale + 5);

        Entity[][] entities = new Entity[size.x()][size.y()];

        for (int x = 0; x < size.x(); x++) {
            for (int y = 0; y < size.y(); y++) {
                Vector3d position = leftPos.add((x * scale), (y * scale), 0.0, new Vector3d());

                entities[x][y] = physics.createEntity().add(new ActorComponent(position, scale));

                physics.addConstraint(entities[x][y], new GravityConstraint(new Vector3d(0.0,-1,0.0)));
//                physics.addParticleCollider(entities[x][y], new SphereCollider(scale * 0.5));
                var collider = new SphereCollider(scale * 0.5);
                physics.addWorldCollider(entities[x][y], collider);
                physics.addEntityCollider(entities[x][y], collider);
            }
        }

        // Freeze corners
        physics.addConstraint(entities[0][0], new StaticConstraint(new Vector3d(leftPos)));
        physics.addConstraint(entities[size.x()-1][0], new StaticConstraint(new Vector3d(rightPos)));

        for (int x = 0; x < size.x() - 1; x++) {
            for (int y = 0; y < size.y() - 1; y++) {
                var entity = entities[x][y];

                var entityNext = entities[x + 1][y];
                var next = new FixedDistanceConstraint(entityNext, scale);

                var entityUnder = entities[x][y + 1];
                var under = new FixedDistanceConstraint(entityUnder, scale);

                physics.addConstraint(entity, next, under);
            }
        }

        return super.use(level, player, hand);
    }
}
