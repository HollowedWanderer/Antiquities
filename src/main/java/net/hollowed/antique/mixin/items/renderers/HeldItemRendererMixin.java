package net.hollowed.antique.mixin.items.renderers;

import net.hollowed.antique.Antiquities;
import net.hollowed.antique.client.cloth.ClothOwner;
import net.hollowed.antique.client.renderer.cloth.ClothManager;
import net.hollowed.antique.index.AntiqueDataComponentTypes;
import net.hollowed.antique.index.AntiqueItems;
import net.hollowed.antique.items.components.MyriadToolComponent;
import net.hollowed.antique.util.ClothUtil;
import net.hollowed.antique.util.interfaces.duck.ArmedRenderStateAccess;
import net.hollowed.antique.util.resources.ClothSkinData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import java.util.Optional;

@Mixin(ItemInHandLayer.class)
public abstract class HeldItemRendererMixin<S extends ArmedEntityRenderState, M extends EntityModel<S> & ArmedModel<S>> extends RenderLayer<S, @NotNull M> {

    public HeldItemRendererMixin(RenderLayerParent<S, @NotNull M> context) {
        super(context);
    }

    @Inject(method = "submitArmWithItem", at = @At("HEAD"))
    public void renderItem(S state, ItemStackRenderState item, ItemStack itemStack, HumanoidArm arm, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, CallbackInfo ci) {
        if (state instanceof ArmedRenderStateAccess access) {
            poseStack.pushPose();
            this.getParentModel().translateToHand(state, arm, poseStack);
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            boolean bl = arm == HumanoidArm.LEFT;
            poseStack.translate((float)(bl ? -1 : 1) / 16.0F, 0.125F, -0.625F);
            poseStack.translate(0, 0.6, 0);

            if (access.antique$getEntity() instanceof LivingEntity entity) {
                if (entity.getUseItem().is(AntiqueItems.MYRIAD_TOOL) && entity.getItemHeldByArm(arm).getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, MyriadToolComponent.DEFAULT_NO_CLOTH).toolBit().is(AntiqueItems.MYRIAD_SHOVEL_HEAD)) {
                    poseStack.translate(0, -1.2, 0.2);
                }

                if (entity.getItemHeldByArm(arm).is(AntiqueItems.MYRIAD_TOOL) && entity.getItemHeldByArm(arm).getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, MyriadToolComponent.DEFAULT_NO_CLOTH).toolBit().is(AntiqueItems.MYRIAD_AXE_HEAD)) {
                    poseStack.translate(0, -0.3, 0);
                    if (entity.isUsingItem()) {
                        poseStack.translate(arm == HumanoidArm.RIGHT ? -0.45 : 0.45, -0.5, 0);
                    }
                }

                ItemStack stack = entity.getItemHeldByArm(arm);
                MyriadToolComponent component = stack.getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, MyriadToolComponent.DEFAULT_NO_CLOTH);

                component.cloth().ifPresent(_ -> {
                    Optional<Holder.Reference<ClothSkinData>> data = ClothUtil.getClothData(component.cloth().get(), entity.registryAccess());

                    if (data.isPresent()) {
                        Object name = stack.getOrDefault(DataComponents.CUSTOM_NAME, "");

                        if (stack.is(AntiqueItems.MYRIAD_TOOL) && !(name.equals(Component.literal("Perfected Staff")) || name.equals(Component.literal("Orb Staff")) || name.equals(Component.literal("Lapis Staff")))) {
                            Identifier clothId = access.antique$getClothId() == null ? (arm == HumanoidArm.RIGHT ? Antiquities.id("right_arm") : Antiquities.id("left_arm")) : access.antique$getClothId();
                            ClothManager manager = ClothManager.getOrCreate(new ClothOwner.OfEntity(entity), clothId, data.get().value());

                            if (manager != null) {
                                manager.renderCloth(
                                        data.get(),
                                        poseStack,
                                        submitNodeCollector,
                                        lightCoords,
                                        ClothUtil.getDynamicClothColor(component.cloth().get(), entity.registryAccess()).orElse(0xFFFFFFFF),
                                        ClothUtil.getClothPatterns(component.cloth().get()),
                                        entity.registryAccess(),
                                        Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false)
                                );
                            }
                        }
                    }
                });
            }

            poseStack.popPose();
        }
    }
}
