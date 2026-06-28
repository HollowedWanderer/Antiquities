package net.hollowed.antique.mixin.items.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.hollowed.antique.enchantments.EnchantmentListener;
import net.hollowed.antique.items.ScepterItem;
import net.hollowed.combatamenities.util.interfaces.PlayerEntityRenderStateAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerItemInHandLayer.class)
public class ThirdPersonItemMixin<S extends AvatarRenderState, M extends EntityModel<S> & ArmedModel<S> & HeadedModel> extends ItemInHandLayer<S, @NotNull M> {

    public ThirdPersonItemMixin(RenderLayerParent<S, @NotNull M> featureRendererContext) {
        super(featureRendererContext);
    }

    @Inject(method = "submitArmWithItem(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lnet/minecraft/client/renderer/item/ItemStackRenderState;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V",
            at = @At("HEAD"), cancellable = true)
    private void spin(S state, ItemStackRenderState item, ItemStack itemStack, HumanoidArm arm, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, CallbackInfo ci) {

        if (state instanceof PlayerEntityRenderStateAccess) {
            Entity entity = ((PlayerEntityRenderStateAccess) state).combat_Amenities$getPlayerEntity();
            if (entity instanceof Player player && player.getUseItem().getItem() instanceof ScepterItem) {
                int useTime = player.getTicksUsingItem();
                float tickDelta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);

                ItemStack stack = player.getUseItem();

                if (EnchantmentListener.hasEnchantment(stack, "antique:kinematic")) {
                    final float totalRotation = getTotalRotation(useTime, tickDelta);

                    poseStack.pushPose();
                    poseStack.mulPose(Axis.ZP.rotationDegrees(-10));
                    poseStack.translate(0, -0.2, 0.2);
                    poseStack.translate(0, 0.6, -0.3);

                    poseStack.mulPose(Axis.XP.rotationDegrees(totalRotation));

                    poseStack.translate(-0.2, -0.6, 0.3);
                    this.getParentModel().translateToHand(state, arm, poseStack);
                    poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
                    poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                    boolean bl = arm == HumanoidArm.LEFT;
                    poseStack.translate((float)(bl ? -1 : 1) / 16.0F, 0.125F, -0.625F);
                    item.submit(poseStack, submitNodeCollector, lightCoords, 0, state.outlineColor);
                    ci.cancel();
                    poseStack.popPose();
                }
            }
        }
    }

    @Unique
    private static float getTotalRotation(int useTime, float tickDelta) {
        float maxAngularVelocity = 120.0F;
        float accelerationFactor = 0.004F;

        float progress = Math.min(1.0F, (useTime + tickDelta) * accelerationFactor);
        float easedVelocity = maxAngularVelocity * (1 - (float) Math.pow(1 - progress, 3));

        return (useTime + tickDelta) * easedVelocity;
    }
}