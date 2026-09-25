package net.hollowed.antique.mixin.physics;

import net.hollowed.antique.citrus_physics.PhysicsWorld;
import net.hollowed.antique.citrus_physics.component.ActorComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.ARGB;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Math.abs;
import static java.lang.Math.clamp;

@Mixin(EntityHitboxDebugRenderer.class)
public class EntityHitboxDebugRendererMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "emitGizmos", at = @At("HEAD"))
    private void emitGizmos(double camX, double camY, double camZ, DebugValueAccess debugValues, Frustum frustum, float partialTicks, CallbackInfo ci) {
        if (this.minecraft.level != null) {
            PhysicsWorld physicsWorld = PhysicsWorld.getFromWorld(this.minecraft.level);
            AtomicInteger renderTokens = new AtomicInteger(50000); // In short how many particles can render at once

            physicsWorld.ecsWorld.findEntitiesWith(ActorComponent.class).stream().forEach(result -> {
                if (renderTokens.get() > 0) {
                    ActorComponent actor = result.comp();

                    var position = new Vec3(actor.position.x, actor.position.y, actor.position.z);
                    var halfSize = new Vec3(actor.mass, actor.mass, actor.mass).multiply(0.5, 0.5, 0.5).multiply(0.25, 0.25, 0.25);

                    var box = new AABB(position.add(halfSize.multiply(-1, -1, -1)), position.add(halfSize));
                    var vel = actor.position.sub(actor.positionCache, new Vector3d()).mul(20);

                    int r = ARGB.red((int) (255 * clamp(abs(vel.x), 0.0, 1.0)));
                    int g = ARGB.green((int) (255 * clamp(abs(vel.y), 0.0, 1.0)));
                    int b = ARGB.blue((int) (255 * clamp(abs(vel.z), 0.0, 1.0)));
                    int argb = ARGB.color(255, r, g, b);

                    Gizmos.cuboid(box, GizmoStyle.stroke(argb, 5), false);

                    renderTokens.addAndGet(-1);
                }
            });
        }
    }
}
