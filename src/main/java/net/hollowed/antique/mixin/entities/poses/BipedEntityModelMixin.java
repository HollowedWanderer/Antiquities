package net.hollowed.antique.mixin.entities.poses;

import net.hollowed.antique.items.components.MyriadToolComponent;
import net.hollowed.antique.util.interfaces.duck.BipedEntityRenderStateAccess;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.hollowed.antique.client.poses.MyriadAxePosing;
import net.hollowed.antique.index.AntiqueDataComponentTypes;
import net.hollowed.antique.index.AntiqueItems;
import net.hollowed.antique.items.MyriadToolItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public class BipedEntityModelMixin {

    @Shadow @Final public ModelPart rightArm;

    @Shadow @Final public ModelPart leftArm;

    @Shadow @Final public ModelPart head;

    @Inject(method = "poseRightArm", at = @At("HEAD"), cancellable = true)
    private void positionCustomRightArm(HumanoidRenderState state, CallbackInfo ci) {
        if (state instanceof BipedEntityRenderStateAccess access && access.antique$getEntity() != null && access.antique$getEntity().getUseItem().getItem() instanceof MyriadToolItem) {
            if (access.antique$getEntity().getUseItem().getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, MyriadToolComponent.DEFAULT_NO_CLOTH).toolBit().is(AntiqueItems.MYRIAD_AXE_HEAD)) {
                MyriadAxePosing.hold(this.rightArm, this.leftArm, this.head, true);
                ci.cancel();
            }
        }
    }

    @Inject(method = "poseLeftArm", at = @At("HEAD"), cancellable = true)
    private void positionCustomLeftArm(HumanoidRenderState state, CallbackInfo ci) {
        if (state instanceof BipedEntityRenderStateAccess access && access.antique$getEntity() != null && access.antique$getEntity().getUseItem().getItem() instanceof MyriadToolItem) {
            if (access.antique$getEntity().getUseItem().getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, MyriadToolComponent.DEFAULT_NO_CLOTH).toolBit().is(AntiqueItems.MYRIAD_AXE_HEAD)) {
                MyriadAxePosing.hold(this.rightArm, this.leftArm, this.head, false);
                ci.cancel();
            }
        }
    }
}
