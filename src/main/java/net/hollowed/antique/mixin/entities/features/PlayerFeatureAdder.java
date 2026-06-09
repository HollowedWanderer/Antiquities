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
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(AvatarRenderer.class)
public abstract class PlayerFeatureAdder extends LivingEntityRenderer<@NotNull AbstractClientPlayer, @NotNull AvatarRenderState, @NotNull PlayerModel> {

    @Unique
    private static final Identifier TEXTURE = Antiquities.id("textures/entity/adventure_armor.png");
    @Unique
    private static final Identifier THICK_TEXTURE = Antiquities.id("textures/entity/adventure_armor_thick.png");
    @Unique
    private AdventureArmor<@NotNull AvatarRenderState> armorModel;
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
        if (stack.getTags().toList().contains(TagKey.create(Registries.ITEM, Antiquities.id("two_handed")))) {
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
            matrices.translate(0.325, 0.1, 0);
            if (slim) {
                queue.order(1).submitModelPart(armorModel.leftArmArmor, matrices, RenderTypes.armorCutoutNoCull(TEXTURE), light, OverlayTexture.NO_OVERLAY, null);
                if (player.getItemBySlot(EquipmentSlot.CHEST).hasFoil()) queue.order(2).submitModelPart(armorModel.leftArmArmor, matrices, RenderTypes.armorEntityGlint(), light, OverlayTexture.NO_OVERLAY, null);
            } else {
                queue.order(1).submitModelPart(armorModel.leftArmArmorThick, matrices, RenderTypes.armorCutoutNoCull(THICK_TEXTURE), light, OverlayTexture.NO_OVERLAY, null);
                if (player.getItemBySlot(EquipmentSlot.CHEST).hasFoil()) queue.order(2).submitModelPart(armorModel.leftArmArmorThick, matrices, RenderTypes.armorEntityGlint(), light, OverlayTexture.NO_OVERLAY, null);
            }
        }
    }

    @Inject(method = "renderRightHand", at = @At("TAIL"))
    public void renderArmoredRightArm(PoseStack matrices, SubmitNodeCollector queue, int light, Identifier skinTexture, boolean sleeveVisible, CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        if (player.getItemBySlot(EquipmentSlot.CHEST).getItem() == AntiqueItems.MYRIAD_PAULDRONS) {
            matrices.mulPose(Axis.ZP.rotationDegrees(5));
            matrices.translate(-0.325, 0.1, 0);
            if (slim) {
                queue.order(1).submitModelPart(armorModel.rightArmArmor, matrices, RenderTypes.armorCutoutNoCull(TEXTURE), light, OverlayTexture.NO_OVERLAY, null);
                if (player.getItemBySlot(EquipmentSlot.CHEST).hasFoil()) queue.order(2).submitModelPart(armorModel.rightArmArmor, matrices, RenderTypes.armorEntityGlint(), light, OverlayTexture.NO_OVERLAY, null);
            } else {
                queue.order(1).submitModelPart(armorModel.rightArmArmorThick, matrices, RenderTypes.armorCutoutNoCull(THICK_TEXTURE), light, OverlayTexture.NO_OVERLAY, null);
                if (player.getItemBySlot(EquipmentSlot.CHEST).hasFoil()) queue.order(2).submitModelPart(armorModel.rightArmArmorThick, matrices, RenderTypes.armorEntityGlint(), light, OverlayTexture.NO_OVERLAY, null);
            }
        }
    }
}
