package net.hollowed.antique.mixin.entities.features;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.hollowed.antique.util.interfaces.duck.ClothAccess;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class ClothTickMixin {

    @Inject(method = "renderLevel", at = @At("TAIL"))
    private void tickClothManagers(GraphicsResourceAllocator graphicsResourceAllocator, DeltaTracker deltaTracker, boolean bl, Camera camera, Matrix4f matrix4f, Matrix4f matrix4f2, Matrix4f matrix4f3, GpuBufferSlice gpuBufferSlice, Vector4f vector4f, boolean bl2, CallbackInfo ci) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level instanceof ClothAccess clothAccess) {
            clothAccess.antique$tickManagers();
        }
    }
}
