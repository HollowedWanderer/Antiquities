package net.hollowed.antique.mixin.items.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.hollowed.antique.enchantments.EnchantmentListener;
import net.hollowed.antique.index.AntiqueDataComponentTypes;
import net.hollowed.antique.index.AntiqueItems;
import net.hollowed.antique.items.ScepterItem;
import net.hollowed.antique.items.components.MyriadToolComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class FirstPersonItemMixin {

    @Inject(method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V",
            at = @At("HEAD"))
    private void itemTransforms(LivingEntity mob, ItemStack itemStack, ItemDisplayContext type, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, CallbackInfo ci) {
        if (mob instanceof Player player) {
            ItemStack activeStack = player.getUseItem();
            int useTime = player.getTicksUsingItem();
            float tickDelta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
            boolean right = !type.leftHand();
            if (activeStack.getItem() instanceof ScepterItem) {
                if (EnchantmentListener.hasEnchantment(itemStack, "antique:kinematic") && activeStack.equals(itemStack)) {
                    final float totalRotation = getTotalRotation(useTime, tickDelta);
                    poseStack.translate(0, 0, -0.2);
                    poseStack.mulPose(Axis.XP.rotationDegrees(-totalRotation));
                    poseStack.translate(0, 0, 0.1);
                }
            } else {
                if (activeStack.is(AntiqueItems.MYRIAD_TOOL) && activeStack.getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, MyriadToolComponent.DEFAULT_NO_CLOTH).toolBit().is(AntiqueItems.MYRIAD_CLEAVER_BLADE)) {
                    poseStack.translate(right ? 0.2 : -0.2, 0.15, 0);
                    poseStack.mulPose(Axis.XP.rotationDegrees(45));
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