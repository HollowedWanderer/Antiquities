package net.hollowed.antique.mixin.ext;

import com.llamalad7.mixinextras.sugar.Local;
import net.hollowed.antique.Antiquities;
import net.hollowed.antique.util.interfaces.duck.ArmedRenderStateAccess;
import net.minecraft.client.gui.GuiGraphics;
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
            method = "renderEntityInInventoryFollowsMouse",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;submitEntityRenderState(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;FLorg/joml/Vector3f;Lorg/joml/Quaternionf;Lorg/joml/Quaternionf;IIII)V"
            )
    )
    private static void renderEntityInInventoryFollowsMouse(
            GuiGraphics guiGraphics,
            int i,
            int j,
            int k,
            int l,
            int m,
            float f,
            float g,
            float h,
            LivingEntity livingEntity,
            CallbackInfo ci,
            @Local EntityRenderState entityRenderState
    ) {
        if (entityRenderState instanceof ArmedRenderStateAccess access) {
            access.antique$setClothId(Antiquities.id("inventory"));
        }
    }
}
