package net.hollowed.antique.mixin.items.renderers;

import net.hollowed.antique.Antiquities;
import net.hollowed.antique.client.renderer.cloth.ClothManager;
import net.hollowed.antique.index.AntiqueDataComponentTypes;
import net.hollowed.antique.index.AntiqueItems;
import net.hollowed.antique.items.components.MyriadToolComponent;
import net.hollowed.antique.util.interfaces.duck.ArmedRenderStateAccess;
import net.hollowed.antique.util.resources.ClientClothData;
import net.hollowed.antique.util.resources.ClothSkinData;
import net.hollowed.combatamenities.util.items.CAComponents;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
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
import java.awt.*;

@Mixin(ItemInHandLayer.class)
public abstract class HeldItemRendererMixin<S extends ArmedEntityRenderState, M extends EntityModel<S> & ArmedModel<S>> extends RenderLayer<S, @NotNull M> {

    public HeldItemRendererMixin(RenderLayerParent<S, @NotNull M> context) {
        super(context);
    }

    @Inject(method = "submitArmWithItem", at = @At("HEAD"))
    public void renderItem(S entityState, ItemStackRenderState itemStackRenderState, ItemStack itemStack, HumanoidArm arm, PoseStack matrices, SubmitNodeCollector submitNodeCollector, int i, CallbackInfo ci) {
        if (entityState instanceof ArmedRenderStateAccess access) {
            matrices.pushPose();
            this.getParentModel().translateToHand(entityState, arm, matrices);
            matrices.mulPose(Axis.XP.rotationDegrees(-90.0F));
            matrices.mulPose(Axis.YP.rotationDegrees(180.0F));
            boolean bl = arm == HumanoidArm.LEFT;
            matrices.translate((float)(bl ? -1 : 1) / 16.0F, 0.125F, -0.625F);
            matrices.translate(0, 0.6, 0);

            Entity entity = access.antique$getEntity();

            if (entity instanceof LivingEntity living && living.getUseItem().is(AntiqueItems.MYRIAD_TOOL)
                    && living.getItemHeldByArm(arm).getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, Antiquities.getDefaultMyriadTool()).toolBit().is(AntiqueItems.MYRIAD_SHOVEL_HEAD)) {
                matrices.translate(0, -1.2, 0.2);
            }
            if (entity instanceof LivingEntity living && living.getItemHeldByArm(arm).is(AntiqueItems.MYRIAD_TOOL)
                    && living.getItemHeldByArm(arm).getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, Antiquities.getDefaultMyriadTool()).toolBit().is(AntiqueItems.MYRIAD_AXE_HEAD)) {
                matrices.translate(0, -0.3, 0);
                if (living.isUsingItem()) {
                    matrices.translate(arm == HumanoidArm.RIGHT ? -0.45 : 0.45, -0.5, 0);
                }
            }

            ClothManager manager;

            if (entity instanceof LivingEntity living) {
                ItemStack stack = living.getItemHeldByArm(arm);

                ClothSkinData.ClothSubData data = ClientClothData.getTransform(String.valueOf(stack.getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, Antiquities.getDefaultMyriadTool()).clothType()));
                Object name = stack.getOrDefault(DataComponents.CUSTOM_NAME, "");
                if (stack.is(AntiqueItems.MYRIAD_TOOL) && !(name.equals(Component.literal("Perfected Staff")) || name.equals(Component.literal("Orb Staff")) || name.equals(Component.literal("Lapis Staff")))) {
                    manager = arm == HumanoidArm.RIGHT ? ClothManager.getOrCreate(entity, Antiquities.id(entity.getId() + "_right_arm"), data) : ClothManager.getOrCreate(entity, Antiquities.id(entity.getId() + "_left_arm"), data);
                    if (manager != null) {
                        MyriadToolComponent component = stack.getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, Antiquities.getDefaultMyriadTool());

                        manager.renderCloth(
                                data,
                                matrices,
                                submitNodeCollector,
                                i,
                                stack.getOrDefault(CAComponents.BOOLEAN_PROPERTY, false),
                                new Color(component.clothColor()),
                                new Color(component.patternColor()),
                                Identifier.parse(component.clothPattern())
                        );
                    }
                }
            }

            matrices.popPose();
        }
    }
}
