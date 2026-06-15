package net.hollowed.antique.entities.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hollowed.antique.entities.CakeEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class CakeRenderer<T extends Entity> extends EntityRenderer<T, @org.jetbrains.annotations.NotNull ArrowRenderState> {
    private final boolean lit;

    public CakeRenderer(EntityRendererProvider.Context ctx, boolean lit) {
        super(ctx);
        this.lit = lit;
    }

    public CakeRenderer(EntityRendererProvider.Context context) {
        this(context, false);
    }

    @Override
    protected int getBlockLightLevel(T entity, @NotNull BlockPos pos) {
        return this.lit ? 15 : super.getBlockLightLevel(entity, pos);
    }

    @Override
    public void submit(ArrowRenderState renderState, PoseStack matrixStack, @NotNull SubmitNodeCollector queue, @NotNull CameraRenderState cameraState) {
        matrixStack.pushPose();

        matrixStack.mulPose(Axis.YP.rotationDegrees(renderState.yRot - 90.0F));
        matrixStack.mulPose(Axis.ZP.rotationDegrees(renderState.xRot));
        matrixStack.mulPose(Axis.ZP.rotationDegrees(-90));

        ItemStackRenderState stackRenderState = new ItemStackRenderState();
        Minecraft.getInstance().getItemModelResolver().appendItemLayers(stackRenderState, Items.CAKE.getDefaultInstance(), ItemDisplayContext.NONE, Minecraft.getInstance().level, null, 1);
        stackRenderState.submit(matrixStack, queue, renderState.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        matrixStack.popPose();
    }

    public ArrowRenderState createRenderState() {
        return new ArrowRenderState();
    }

    @Override
    public void extractRenderState(T entity, ArrowRenderState state, float tickProgress) {
        super.extractRenderState(entity, state, tickProgress);
        if (entity instanceof CakeEntity cake) {
            state.yRot = cake.getStoredYaw();
            state.xRot = cake.getStoredPitch();
        }
    }
}
