package net.hollowed.antique.mixin.entities.states;

import net.hollowed.antique.util.interfaces.duck.BipedEntityRenderStateAccess;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedEntityRenderer.class)
public class BipedEntityRendererMixin {

    @Inject(method = "updateBipedRenderState", at = @At("HEAD"))
    private static void updateBipedRenderState(LivingEntity entity, BipedEntityRenderState state, float tickDelta, ItemModelManager itemModelResolver, CallbackInfo ci) {
        if (state instanceof BipedEntityRenderStateAccess access) {
            access.antique$setEntity(entity);
        }
    }
}
