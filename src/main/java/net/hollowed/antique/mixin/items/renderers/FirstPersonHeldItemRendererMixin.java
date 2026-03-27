package net.hollowed.antique.mixin.items.renderers;

import net.hollowed.antique.Antiquities;
import net.hollowed.antique.client.renderer.cloth.ClothManager;
import net.hollowed.antique.index.AntiqueDataComponentTypes;
import net.hollowed.antique.index.AntiqueItems;
import net.hollowed.antique.items.components.MyriadToolComponent;
import net.hollowed.antique.mixin.accessors.RendererAccessor;
import net.hollowed.antique.util.resources.ClientClothData;
import net.hollowed.antique.util.resources.ClothSkinData;
import net.hollowed.combatamenities.util.items.CAComponents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.*;

@Mixin(ItemInHandRenderer.class)
public abstract class FirstPersonHeldItemRendererMixin {

    @Inject(method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"))
    public void renderItem(LivingEntity entity, ItemStack stack, ItemDisplayContext renderMode, PoseStack matrices, SubmitNodeCollector orderedRenderCommandQueue, int light, CallbackInfo ci) {
        matrices.pushPose();
        boolean leftHanded = entity.getMainArm() == HumanoidArm.LEFT;
        matrices.translate((float)(leftHanded ? -1 : 1) / 16.0F, 0.125F, -0.625F);
        switch (renderMode) {
            case ItemDisplayContext.FIRST_PERSON_RIGHT_HAND -> matrices.translate(leftHanded ? 0.1 : 0, 0, 0);
            case ItemDisplayContext.FIRST_PERSON_LEFT_HAND -> matrices.translate(!leftHanded ? -0.1 : 0, 0, 0);
        }

        matrices.translate(0, 0.4, 0.7);
        if (renderMode == ItemDisplayContext.NONE) {
            matrices.translate(0, -0.5, -0.1);
        }

        ClothManager manager;

        if (entity instanceof Player player) {
            if (stack.is(AntiqueItems.MYRIAD_TOOL)) {
                ClothSkinData.ClothSubData data = ClientClothData.getTransform(stack.getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, Antiquities.getDefaultMyriadTool()).clothType());

                MyriadToolComponent component = stack.getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, Antiquities.getDefaultMyriadTool());

                if (renderMode != ItemDisplayContext.NONE) {
                    matrices.translate(0, -0.1, 0.1);
                }
                if (stack.getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, Antiquities.getDefaultMyriadTool()).toolBit().is(AntiqueItems.MYRIAD_AXE_HEAD) && entity.isUsingItem()) {
                    matrices.translate(renderMode == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND ? -0.5 : 0.5, -0.1, 0);
                }
                if (stack.getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, Antiquities.getDefaultMyriadTool()).toolBit().is(AntiqueItems.MYRIAD_SHOVEL_HEAD) && entity.isUsingItem()) {
                    matrices.translate(renderMode == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND ? 0.1 : -0.1, 0, -0.2);
                }
                if (renderMode == ItemDisplayContext.NONE && (
                        stack.getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, Antiquities.getDefaultMyriadTool()).toolBit().is(AntiqueItems.MYRIAD_CLEAVER_BLADE)
                )) {
                    matrices.translate(-0.15, -0.15, 0);
                }
                manager = renderMode == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND ? ClothManager.getOrCreate(entity, Antiquities.id(entity.getId() + "_first_person_right_arm"), data) : ClothManager.getOrCreate(entity, Antiquities.id(entity.getId() + "_first_person_left_arm"), data);
                switch (renderMode) {
                    case ItemDisplayContext.NONE ->
                            manager = ClothManager.getOrCreate(entity, Antiquities.id(entity.getId() + "_back"), data);
                    case ItemDisplayContext.GUI -> manager = null;
                }
                if (player.getInventory().getItem(42).equals(stack)) {
                    manager = ClothManager.getOrCreate(entity, Antiquities.id(entity.getId() + "_belt"), data);
                }
                if (manager != null) {
                    Matrix4f reprojectMatrix = getReprojectMatrix();
                    manager.renderCloth(
                            data,
                            matrices,
                            orderedRenderCommandQueue,
                            light,
                            stack.getOrDefault(CAComponents.BOOLEAN_PROPERTY, false),
                            new Color(component.clothColor()),
                            new Color(component.patternColor()),
                            Identifier.parse(component.clothPattern()),
                            reprojectMatrix
                    );
                }
            }
        }

        matrices.popPose();
    }

    private Matrix4f getReprojectMatrix() {
        GameRenderer renderer = Minecraft.getInstance().gameRenderer;
        RendererAccessor mixin = (RendererAccessor) renderer;
        Camera mainCamera = renderer.getMainCamera();
        Matrix4f projectionA = this.getProjection(renderer, mixin._getFov(mainCamera, 0.0f, true));
        Matrix4f projectionO = this.getProjection(renderer, 70);
        Matrix4f reprojectMatrix = projectionO.invert().mul(projectionA);
        return reprojectMatrix;
    }
    
    private Matrix4f getProjection(GameRenderer renderer, float fov) {
        Camera mainCamera = renderer.getMainCamera();
        Matrix4f projection = renderer.getProjectionMatrix(fov);
        Quaternionf quaternionf = mainCamera.rotation();
        Matrix4f rotation = (new Matrix4f()).rotation(quaternionf).invert();
        return projection.mul(rotation);
    }
}
