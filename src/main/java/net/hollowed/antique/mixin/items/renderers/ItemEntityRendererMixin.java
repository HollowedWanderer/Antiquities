package net.hollowed.antique.mixin.items.renderers;

import net.hollowed.antique.index.AntiqueComponents;
import net.hollowed.antique.index.AntiqueItems;
import net.hollowed.antique.util.resources.MyriadStaffTransformData;
import net.hollowed.antique.util.resources.MyriadStaffTransformResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin {

    @Unique
    private ItemStack stack = ItemStack.EMPTY;

    @Inject(method = "updateRenderState(Lnet/minecraft/entity/ItemEntity;Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;F)V", at = @At("TAIL"))
    public void getStack(ItemEntity itemEntity, ItemEntityRenderState itemEntityRenderState, float f, CallbackInfo ci) {
        stack = itemEntity.getStack();
    }

    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("TAIL"))
    public void render(ItemEntityRenderState itemEntityRenderState, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (!itemEntityRenderState.itemRenderState.isEmpty()) {
            matrixStack.push();
            Box box = itemEntityRenderState.itemRenderState.getModelBoundingBox();
            if (box == null) return;
            float f = -((float)box.minY) + 0.0625F;
            float g = MathHelper.sin(itemEntityRenderState.age / 10.0F + itemEntityRenderState.uniqueOffset) * 0.1F + 0.1F;
            matrixStack.translate(0.0F, g + f, 0.0F);
            float h = ItemEntity.getRotation(itemEntityRenderState.age, itemEntityRenderState.uniqueOffset);
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation(h));
            matrixStack.translate(0.075, 0.2, 0);
            matrixStack.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(45));
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
            matrixStack.scale(0.2F, 0.2F, 0.2F);

            if (stack.isOf(AntiqueItems.MYRIAD_STAFF)) {
                MinecraftClient client = MinecraftClient.getInstance();
                ItemRenderer itemRenderer = client.getItemRenderer();

                ItemStack stackToRender = stack.getOrDefault(AntiqueComponents.MYRIAD_STACK, ItemStack.EMPTY);

                matrixStack.scale(0.875F, 0.875F, 0.875F);
                matrixStack.translate(0.0, -0.035, 0.05);

                MyriadStaffTransformData data = MyriadStaffTransformResourceReloadListener.getTransform(Registries.ITEM.getId(stackToRender.getItem()));
                matrixStack.scale(data.scale().get(0), data.scale().get(1), data.scale().get(2));
                matrixStack.translate(data.translation().get(0), data.translation().get(1), data.translation().get(2));
                matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(data.rotation().getFirst()));
                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(data.rotation().get(1)));
                matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(data.rotation().get(2)));

                Identifier customModel = stackToRender.getOrDefault(DataComponentTypes.ITEM_MODEL, Registries.ITEM.getId(stackToRender.getItem()));
                if (!data.model().equals(Identifier.of("default"))) {
                    stackToRender.set(DataComponentTypes.ITEM_MODEL, data.model());
                }

                itemRenderer.renderItem(
                        stackToRender,
                        ItemDisplayContext.NONE,
                        i,
                        OverlayTexture.DEFAULT_UV,
                        matrixStack,
                        vertexConsumerProvider,
                        client.world,
                        1
                );

                stackToRender.set(DataComponentTypes.ITEM_MODEL, customModel);
            }

            matrixStack.pop();
        }
    }
}
