package net.hollowed.antique.mixin.entities.features;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.hollowed.antique.Antiquities;
import net.hollowed.antique.index.AntiqueEntityLayers;
import net.hollowed.antique.client.armor.models.AdventureArmor;
import net.hollowed.antique.index.AntiqueItems;
import net.hollowed.antique.items.MyriadToolItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("all") // this class is still broken - fix please
@Mixin(AvatarRenderer.class)
public abstract class PlayerFeatureAdder extends LivingEntityRenderer<AbstractClientPlayer, AvatarRenderState, PlayerModel> {

    @Unique
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Antiquities.MOD_ID, "textures/entity/adventure_armor.png");
    @Unique
    private static final Identifier THICK_TEXTURE = Identifier.fromNamespaceAndPath(Antiquities.MOD_ID, "textures/entity/adventure_armor_thick.png");
    @Unique
    private AdventureArmor<AvatarRenderState> armorModel;
    @Unique
    private boolean slim = false;

    public PlayerFeatureAdder(EntityRendererProvider.Context ctx, PlayerModel model, float shadowRadius) {
        super(ctx, model, shadowRadius);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void addCustomFeature(EntityRendererProvider.Context ctx, boolean slim, CallbackInfo ci) {
        this.slim = slim;
        this.armorModel = new AdventureArmor<>(ctx.getModelSet().bakeLayer(AntiqueEntityLayers.ADVENTURE_ARMOR));
    }

    @Inject(method = "getArmPose(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/client/model/HumanoidModel$ArmPose;", at = @At("HEAD"), cancellable = true)
    private static void getArmPose(Avatar player, ItemStack stack, InteractionHand hand, CallbackInfoReturnable<HumanoidModel.ArmPose> cir) {
        if (stack.getTags().toList().contains(TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Antiquities.MOD_ID, "two_handed")))) {
            if (!player.isUsingItem() && !player.swinging && !player.isShiftKeyDown()) {
                cir.setReturnValue(HumanoidModel.ArmPose.CROSSBOW_CHARGE);
            } else if (player.isShiftKeyDown() || player.swinging) {
                cir.setReturnValue(HumanoidModel.ArmPose.CROSSBOW_HOLD);
            }
        }
        if (stack.getItem() instanceof MyriadToolItem) {
            if (player.isShiftKeyDown() && !player.isUsingItem()) {
                cir.setReturnValue(HumanoidModel.ArmPose.BLOCK);
            } else if (!player.isUsingItem()) {
                cir.setReturnValue(HumanoidModel.ArmPose.BRUSH);
            }
        }
    }

    @Inject(method = "renderLeftHand", at = @At("TAIL"))
    public void renderArmoredLeftArm(PoseStack matrices, SubmitNodeCollector queue, int light, Identifier skinTexture, boolean sleeveVisible, CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        if (player.getItemBySlot(EquipmentSlot.CHEST).getItem() == AntiqueItems.MYRIAD_PAULDRONS) {
            matrices.mulPose(Axis.ZP.rotationDegrees(-5));
            matrices.translate(0.075, 0, 0);
            if (slim) {
//                VertexConsumer vertexConsumer = ItemRenderer.getArmorGlintConsumer(vertexConsumers, RenderLayer.getArmorCutoutNoCull(TEXTURE),
//                        player.getEquippedStack(EquipmentSlot.CHEST).hasGlint());
//                armorModel.leftArm.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV);
            } else {
                matrices.translate(-0.075, 0, 0);
//                VertexConsumer vertexConsumer = ItemRenderer.getArmorGlintConsumer(vertexConsumers, RenderLayer.getArmorCutoutNoCull(THICK_TEXTURE),
//                        player.getEquippedStack(EquipmentSlot.CHEST).hasGlint());
//                armorModel.leftArmThick.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV);
            }
        }
    }

    @Inject(method = "renderRightHand", at = @At("TAIL"))
    public void renderArmoredRightArm(PoseStack matrices, SubmitNodeCollector queue, int light, Identifier skinTexture, boolean sleeveVisible, CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        if (player.getItemBySlot(EquipmentSlot.CHEST).getItem() == AntiqueItems.MYRIAD_PAULDRONS) {
            matrices.mulPose(Axis.ZP.rotationDegrees(5));
            matrices.translate(-0.075, 0, 0);
            if (slim) {
                // TODO: just add this back
//                VertexConsumer vertexConsumer = ItemRenderer.getArmorGlintConsumer(vertexConsumers, RenderLayer.getArmorCutoutNoCull(TEXTURE),
//                        player.getEquippedStack(EquipmentSlot.CHEST).hasGlint());
//                armorModel.rightArm.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV);
            } else {
                matrices.translate(0.05, 0, 0);
//                VertexConsumer vertexConsumer = ItemRenderer.getArmorGlintConsumer(vertexConsumers, RenderLayer.getArmorCutoutNoCull(THICK_TEXTURE),
//                        player.getEquippedStack(EquipmentSlot.CHEST).hasGlint());
//                armorModel.rightArmThick.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV);
            }
        }
    }
}
