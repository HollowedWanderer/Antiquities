package net.hollowed.antique.mixin.ext;

import com.llamalad7.mixinextras.sugar.Local;
import net.hollowed.antique.Antiquities;
import net.hollowed.antique.util.interfaces.duck.ArmedRenderStateAccess;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public class InventoryScreenMixin {

    @Inject(
            method = "extractEntityInInventoryFollowsMouse",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;entity(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;FLorg/joml/Vector3fc;Lorg/joml/Quaternionfc;Lorg/joml/Quaternionfc;IIII)V"
            )
    )
    private static void renderEntityInInventoryFollowsMouse(
            GuiGraphicsExtractor graphics,
            int x0,
            int y0,
            int x1,
            int y1,
            int size,
            float offsetY,
            float mouseX,
            float mouseY,
            LivingEntity entity,
            CallbackInfo ci,
            @Local(name = "renderState") EntityRenderState renderState
    ) {
        if (renderState instanceof ArmedRenderStateAccess access) {
            access.antique$setClothId(Antiquities.id("inventory"));
        }
    }
}
