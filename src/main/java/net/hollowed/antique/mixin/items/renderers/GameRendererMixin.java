package net.hollowed.antique.mixin.items.renderers;

import net.hollowed.antique.util.interfaces.duck.ClothAccess;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(
            method = "renderLevel",
            at = @At("HEAD")
    )
    private void renderLevel(DeltaTracker deltaTracker, CallbackInfo ci) {
        if (Minecraft.getInstance().level instanceof ClothAccess access) {
            access.antique$startFrames();
        }
    }
}
