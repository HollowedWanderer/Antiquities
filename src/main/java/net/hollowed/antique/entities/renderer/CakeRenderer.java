package net.hollowed.antique.entities.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hollowed.antique.entities.CakeEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.ProjectileEntityRenderState;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class CakeRenderer<T extends Entity> extends EntityRenderer<T, ProjectileEntityRenderState> {
    private final boolean lit;

    public CakeRenderer(EntityRendererFactory.Context ctx, boolean lit) {
        super(ctx);
        this.lit = lit;
    }

    public CakeRenderer(EntityRendererFactory.Context context) {
        this(context, false);
    }

    @Override
    protected int getBlockLight(T entity, BlockPos pos) {
        return this.lit ? 15 : super.getBlockLight(entity, pos);
    }

    public void render(ProjectileEntityRenderState flyingItemEntityRenderState, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        matrixStack.push();
        MinecraftClient client = MinecraftClient.getInstance();
        ItemRenderer itemRenderer = client.getItemRenderer();

        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(flyingItemEntityRenderState.yaw - 90.0F));
        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(flyingItemEntityRenderState.pitch));
        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-90));

        itemRenderer.renderItem(Items.CAKE.getDefaultStack(), ItemDisplayContext.NONE, i, OverlayTexture.DEFAULT_UV, matrixStack, vertexConsumerProvider, client.world, 1);
        matrixStack.pop();
    }

    public ProjectileEntityRenderState createRenderState() {
        return new ProjectileEntityRenderState();
    }

    @Override
    public void updateRenderState(T entity, ProjectileEntityRenderState state, float tickProgress) {
        super.updateRenderState(entity, state, tickProgress);
        if (entity instanceof CakeEntity cake) {
            state.yaw = cake.getStoredYaw();
            state.pitch = cake.getStoredPitch();
        }
    }
}
