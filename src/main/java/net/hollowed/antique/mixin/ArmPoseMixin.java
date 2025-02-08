package net.hollowed.antique.mixin;

import net.hollowed.antique.client.poses.BipedEntityRenderStateAccess;
import net.hollowed.antique.client.poses.MyriadAxeBitPosing;
import net.hollowed.antique.client.poses.MyriadAxePosing;
import net.hollowed.antique.component.ModComponents;
import net.hollowed.antique.items.custom.MyriadToolBitItem;
import net.hollowed.antique.items.custom.MyriadToolItem;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(BipedEntityModel.class)
public class ArmPoseMixin {

    @Shadow @Final public ModelPart rightArm;

    @Shadow @Final public ModelPart leftArm;

    @Shadow @Final public ModelPart head;

    @Inject(method = "positionRightArm", at = @At("HEAD"), cancellable = true)
    private void positionCustomRightArm(BipedEntityRenderState state, BipedEntityModel.ArmPose armPose, CallbackInfo ci) {
        if (state instanceof BipedEntityRenderStateAccess access && access.antique$getEntity().getActiveItem().getItem() instanceof MyriadToolItem) {
            if (Objects.requireNonNull(access.antique$getEntity().getActiveItem().get(ModComponents.INTEGER_PROPERTY)) == 2) {
                MyriadAxePosing.hold(this.rightArm, this.leftArm, this.head, true);
                ci.cancel();
            }
        }

        if (state instanceof BipedEntityRenderStateAccess access && access.antique$getEntity().getActiveItem().getItem() instanceof MyriadToolBitItem item) {
            if (item.getId() == 2) {
                Hand hand = access.antique$getEntity().getActiveHand();
                if (hand.equals(Hand.MAIN_HAND) && access.antique$getEntity().getMainArm().equals(Arm.RIGHT)) {
                    MyriadAxeBitPosing.hold(this.rightArm);
                    ci.cancel();
                } else if (hand.equals(Hand.OFF_HAND) && access.antique$getEntity().getMainArm().equals(Arm.LEFT)) {
                    MyriadAxeBitPosing.hold(this.rightArm);
                    ci.cancel();
                }
            }
        }
    }

    @Inject(method = "positionLeftArm", at = @At("HEAD"), cancellable = true)
    private void positionCustomLeftArm(BipedEntityRenderState state, BipedEntityModel.ArmPose armPose, CallbackInfo ci) {
        if (state instanceof BipedEntityRenderStateAccess access && access.antique$getEntity().getActiveItem().getItem() instanceof MyriadToolItem) {
            if (Objects.requireNonNull(access.antique$getEntity().getActiveItem().get(ModComponents.INTEGER_PROPERTY)) == 2) {
                MyriadAxePosing.hold(this.rightArm, this.leftArm, this.head, false);
                ci.cancel();
            }
        }

        if (state instanceof BipedEntityRenderStateAccess access && access.antique$getEntity().getActiveItem().getItem() instanceof MyriadToolBitItem item) {
            if (item.getId() == 2) {
                Hand hand = access.antique$getEntity().getActiveHand();
                if (hand.equals(Hand.MAIN_HAND) && access.antique$getEntity().getMainArm().equals(Arm.LEFT)) {
                    MyriadAxeBitPosing.hold(this.leftArm);
                    ci.cancel();
                } else if (hand.equals(Hand.OFF_HAND) && access.antique$getEntity().getMainArm().equals(Arm.RIGHT)) {
                    MyriadAxeBitPosing.hold(this.leftArm);
                    ci.cancel();
                }
            }
        }
    }
}
