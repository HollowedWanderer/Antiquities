package net.hollowed.antique.mixin.items.renderers;

import net.hollowed.antique.Antiquities;
import net.hollowed.antique.client.cloth.ClothOwner;
import net.hollowed.antique.client.renderer.cloth.ClothManager;
import net.hollowed.antique.index.AntiqueDataComponentTypes;
import net.hollowed.antique.index.AntiqueItems;
import net.hollowed.antique.items.components.MyriadToolComponent;
import net.hollowed.antique.util.ClothUtil;
import net.hollowed.antique.util.resources.ClothSkinData;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.vertex.PoseStack;

import java.util.Optional;

@Mixin(ItemInHandRenderer.class)
public abstract class FirstPersonHeldItemRendererMixin {

    @Inject(method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"))
    public void renderItem(LivingEntity mob, ItemStack itemStack, ItemDisplayContext type, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, CallbackInfo ci) {
        poseStack.pushPose();
        boolean leftHanded = mob.getMainArm() == HumanoidArm.LEFT;
        poseStack.translate((float)(leftHanded ? -1 : 1) / 16.0F, 0.125F, -0.625F);
        switch (type) {
            case ItemDisplayContext.FIRST_PERSON_RIGHT_HAND -> poseStack.translate(leftHanded ? 0.1 : 0, 0, 0);
            case ItemDisplayContext.FIRST_PERSON_LEFT_HAND -> poseStack.translate(!leftHanded ? -0.1 : 0, 0, 0);
        }

        poseStack.translate(0, 0.4, 0.7);
        if (type == ItemDisplayContext.NONE) {
            poseStack.translate(0, -0.5, -0.1);
        }

        ClothManager manager;

        if (mob instanceof Player player) {
            if (itemStack.is(AntiqueItems.MYRIAD_TOOL)) {
                boolean reproject = true;
                MyriadToolComponent component = itemStack.getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, MyriadToolComponent.DEFAULT_NO_CLOTH);

                if (type != ItemDisplayContext.NONE) {
                    poseStack.translate(0, -0.1, 0.1);
                }

                if (component.toolBit().is(AntiqueItems.MYRIAD_AXE_HEAD) && mob.isUsingItem()) {
                    poseStack.translate(type == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND ? -0.5 : 0.5, -0.1, 0);
                }

                if (component.toolBit().is(AntiqueItems.MYRIAD_SHOVEL_HEAD) && mob.isUsingItem()) {
                    poseStack.translate(type == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND ? 0.1 : -0.1, 0, -0.2);
                }

                if (type == ItemDisplayContext.NONE && component.toolBit().is(AntiqueItems.MYRIAD_CLEAVER_BLADE)) {
                    poseStack.translate(-0.15, -0.15, 0);
                }

                if (component.cloth().isPresent()) {
                    Optional<Holder.Reference<ClothSkinData>> data = ClothUtil.getClothData(component.cloth().get(), player.registryAccess());

                    if (data.isPresent()) {
                        manager = type == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND ? ClothManager.getOrCreate(new ClothOwner.OfEntity(mob), Antiquities.id("right_arm"), data.get().value()) : ClothManager.getOrCreate(new ClothOwner.OfEntity(mob), Antiquities.id("left_arm"), data.get().value());

                        switch (type) {
                            case ItemDisplayContext.NONE -> {
                                manager = ClothManager.getOrCreate(new ClothOwner.OfEntity(mob), Antiquities.id("back"), data.get().value());
                                reproject = false;
                            }
                            case ItemDisplayContext.GUI -> manager = null;
                        }

                        if (player.getInventory().getItem(42).equals(itemStack)) {
                            manager = ClothManager.getOrCreate(new ClothOwner.OfEntity(mob), Antiquities.id("belt"), data.get().value());
                            reproject = false;
                        }

                        if (manager != null) {
                            manager.renderCloth(
                                    data.get(),
                                    poseStack,
                                    submitNodeCollector,
                                    lightCoords,
                                    ClothUtil.getDynamicClothColor(component.cloth().get(), player.registryAccess()).orElse(0xFFFFFFFF),
                                    ClothUtil.getClothPatterns(component.cloth().get()),
                                    player.registryAccess(),
                                    reproject ? getReprojectMatrix() : new Matrix4f(),
                                    Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false)
                            );
                        }
                    }
                }
            }
        }

        poseStack.popPose();
    }

    @Unique
    private Matrix4f getReprojectMatrix() {
        GameRenderer renderer = Minecraft.getInstance().gameRenderer;
        Camera mainCamera = renderer.mainCamera();
        float cameraFov = mainCamera.getFov();
        Matrix4f projectionA = this.getProjection(renderer, cameraFov);
        Matrix4f projectionO = this.getProjection(renderer, 70);
        return projectionO.invert().mul(projectionA);
    }

    @Unique
    private Matrix4f getProjection(GameRenderer renderer, float fov) {
        Camera mainCamera = renderer.mainCamera();
        CameraRenderState state = renderer.gameRenderState().levelRenderState.cameraRenderState;
        Matrix4f projMat = new Matrix4f();
        Projection proj = new Projection();
        proj.setupPerspective(0.05f, state.depthFar, fov, Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight());
        proj.getMatrix(projMat);
        Quaternionf quaternionf = mainCamera.rotation();
        Matrix4f rotation = (new Matrix4f()).rotation(quaternionf).invert();
        return projMat.mul(rotation);
    }
}
