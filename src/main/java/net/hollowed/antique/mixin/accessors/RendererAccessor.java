package net.hollowed.antique.mixin.accessors;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface RendererAccessor {
    @Invoker("getFov")
    float getCameraFov(Camera camera, float f, boolean bl);
}
