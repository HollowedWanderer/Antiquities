package net.hollowed.antique.mixin.entities.features;

import com.mojang.blaze3d.vertex.PoseStack;
import net.hollowed.antique.Antiquities;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CapeLayer.class)
public abstract class CapeMixin {

    @Shadow public abstract void submit(PoseStack matrixStack, SubmitNodeCollector orderedRenderCommandQueue, int i, AvatarRenderState playerEntityRenderState, float f, float g);

    @Unique
    private boolean ran;

    @Inject(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V", at = @At("HEAD"), cancellable = true)
    public void render(PoseStack matrixStack, SubmitNodeCollector orderedRenderCommandQueue, int i, AvatarRenderState playerEntityRenderState, float f, float g, CallbackInfo ci) {
        if (!ran) {
            ran = true;
            matrixStack.pushPose();
            if (playerEntityRenderState.chestEquipment.getTags().toList().contains(TagKey.create(Registries.ITEM, Antiquities.id("chest_armor")))) {
                matrixStack.translate(0.0F, -0.053125F, 0.06875F);
            }
            this.submit(matrixStack, orderedRenderCommandQueue, i, playerEntityRenderState, f, g);
            matrixStack.popPose();
            ci.cancel();
        } else {
            ran = false;
        }
    }
}
