package net.hollowed.antique.mixin.entities.poses;

import net.hollowed.antique.Antiquities;
import net.hollowed.antique.items.MyriadToolItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HumanoidMobRenderer.class)
public class BipedEntityRendererMixin<T extends Mob> {

    @Inject(method = "getArmPose", at = @At("HEAD"), cancellable = true)
    private void getArmPose(T player, HumanoidArm arm, CallbackInfoReturnable<HumanoidModel.ArmPose> cir) {
        ItemStack itemStack = player.getItemHeldByArm(arm);
        if (itemStack.getTags().toList().contains(TagKey.create(Registries.ITEM, Antiquities.id("two_handed")))) {
            if (!player.isUsingItem() && !player.swinging && !player.isShiftKeyDown()) {
                cir.setReturnValue(HumanoidModel.ArmPose.CROSSBOW_CHARGE);
            } else if (player.isShiftKeyDown() || player.swinging) {
                cir.setReturnValue(HumanoidModel.ArmPose.CROSSBOW_HOLD);
            }
        }
        if (itemStack.getItem() instanceof MyriadToolItem) {
            if (player.isShiftKeyDown() && !player.isUsingItem()) {
                cir.setReturnValue(HumanoidModel.ArmPose.BLOCK);
            } else if (!player.isUsingItem()) {
                cir.setReturnValue(HumanoidModel.ArmPose.BRUSH);
            }
        }
    }
}
