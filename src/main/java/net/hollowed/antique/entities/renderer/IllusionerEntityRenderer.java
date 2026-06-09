//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.hollowed.antique.entities.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hollowed.antique.Antiquities;
import net.hollowed.antique.entities.IllusionerEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.illager.IllagerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class IllusionerEntityRenderer extends IllagerRenderer<@org.jetbrains.annotations.NotNull IllusionerEntity, @org.jetbrains.annotations.NotNull IllusionerEntityRenderState> {
    private static final Identifier TEXTURE = Antiquities.id("textures/entity/illager/illusioner.png");
    private static final Identifier CLONE_TEXTURE = Antiquities.id("textures/entity/illager/illusioner_clone.png");

    public IllusionerEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new IllagerModel<>(context.bakeLayer(ModelLayers.ILLUSIONER)), 0.5F);
        this.addLayer(new ItemInHandLayer<@NotNull IllusionerEntityRenderState, @NotNull IllagerModel<@NotNull IllusionerEntityRenderState>>(this) {
            public void submit(@NotNull PoseStack matrixStack, @NotNull SubmitNodeCollector orderedRenderCommandQueue, int i, IllusionerEntityRenderState illusionerEntityRenderState, float f, float g) {
                if (illusionerEntityRenderState.spellcasting || illusionerEntityRenderState.isAggressive) {
                    super.submit(matrixStack, orderedRenderCommandQueue, i, illusionerEntityRenderState, f, g);
                }
            }
        });
        this.model.getHat().visible = true;
    }

    public @NotNull Identifier getTextureLocation(IllusionerEntityRenderState illusionerEntityRenderState) {
        if (illusionerEntityRenderState.isInvisible) {
            return CLONE_TEXTURE;
        }
        return TEXTURE;
    }

    public IllusionerEntityRenderState createRenderState() {
        return new IllusionerEntityRenderState();
    }

    public void extractRenderState(IllusionerEntity illusionerEntity, IllusionerEntityRenderState illusionerEntityRenderState, float f) {
        super.extractRenderState(illusionerEntity, illusionerEntityRenderState, f);
        Vec3[] vec3ds = illusionerEntity.getMirrorCopyOffsets(f);
        illusionerEntityRenderState.mirrorCopyOffsets = Arrays.copyOf(vec3ds, vec3ds.length);
        illusionerEntityRenderState.spellcasting = illusionerEntity.isCastingSpell();
    }

    @Override
    public void submit(IllusionerEntityRenderState illusionerEntityRenderState, @NotNull PoseStack matrixStack, @NotNull SubmitNodeCollector queue, @NotNull CameraRenderState cameraRenderState) {
        if (illusionerEntityRenderState.isInvisible) {
            matrixStack.pushPose();
            matrixStack.translate(0, 1.4, 0);
            matrixStack.mulPose(Axis.YP.rotationDegrees(-(illusionerEntityRenderState.yRot + illusionerEntityRenderState.bodyRot)));
            matrixStack.mulPose(Axis.XP.rotationDegrees(illusionerEntityRenderState.xRot));
            ItemStack stack = Items.APPLE.getDefaultInstance();
            stack.set(DataComponents.ITEM_MODEL, Antiquities.id("illusioner_idol"));
            ItemStackRenderState stackRenderState = new ItemStackRenderState();
            Minecraft.getInstance().getItemModelResolver().appendItemLayers(stackRenderState, stack, ItemDisplayContext.NONE, Minecraft.getInstance().level, null, 1);
            stackRenderState.submit(matrixStack, queue, illusionerEntityRenderState.lightCoords, OverlayTexture.NO_OVERLAY, 0);

            matrixStack.popPose();
            Vec3[] vec3ds = illusionerEntityRenderState.mirrorCopyOffsets;

            for(int j = 0; j < vec3ds.length; ++j) {
                matrixStack.pushPose();
                matrixStack.translate(vec3ds[j].x + (double)Mth.cos((float)j + illusionerEntityRenderState.ageInTicks * 0.5F) * 0.025, vec3ds[j].y + (double)Mth.cos((float)j + illusionerEntityRenderState.ageInTicks * 0.75F) * 0.0125, vec3ds[j].z + (double)Mth.cos((float)j + illusionerEntityRenderState.ageInTicks * 0.7F) * 0.025);
                super.submit(illusionerEntityRenderState, matrixStack, queue, cameraRenderState);
                matrixStack.popPose();
            }
        } else {
            super.submit(illusionerEntityRenderState, matrixStack, queue, cameraRenderState);
        }
    }

    protected boolean isBodyVisible(IllusionerEntityRenderState illusionerEntityRenderState) {
        return true;
    }

    protected AABB getBoundingBox(IllusionerEntity illusionerEntity) {
        return super.getBoundingBoxForCulling(illusionerEntity).inflate(3.0, 0.0, 3.0);
    }
}
