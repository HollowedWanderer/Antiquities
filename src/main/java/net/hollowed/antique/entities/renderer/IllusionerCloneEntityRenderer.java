//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.hollowed.antique.entities.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hollowed.antique.Antiquities;
import net.hollowed.antique.entities.IllusionerCloneEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.illager.IllagerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class IllusionerCloneEntityRenderer extends IllagerRenderer<@NotNull IllusionerCloneEntity, @NotNull IllusionerEntityRenderState> {
    private static final Identifier TEXTURE = Antiquities.id("textures/entity/illager/illusioner_clone.png");

    public IllusionerCloneEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new IllagerModel<>(context.bakeLayer(ModelLayers.ILLUSIONER)), 0.5F);
        this.addLayer(new ItemInHandLayer<>(this) {
            public void submit(@NotNull PoseStack poseStack, @NotNull SubmitNodeCollector submitNodeCollector, int i, IllusionerEntityRenderState illusionerEntityRenderState, float f, float g) {
                if (illusionerEntityRenderState.spellcasting || illusionerEntityRenderState.isAggressive) {
                    super.submit(poseStack, submitNodeCollector, i, illusionerEntityRenderState, f, g);
                }

            }
        });
        this.model.getHat().visible = true;
    }

    public @NotNull Identifier getTextureLocation(IllusionerEntityRenderState illusionerEntityRenderState) {
        return TEXTURE;
    }

    public IllusionerEntityRenderState createRenderState() {
        return new IllusionerEntityRenderState();
    }

    @Override
    public void extractRenderState(IllusionerCloneEntity illusionerEntity, IllusionerEntityRenderState illusionerEntityRenderState, float f) {
        super.extractRenderState(illusionerEntity, illusionerEntityRenderState, f);
    }

    @Override
    public void submit(IllusionerEntityRenderState livingEntityRenderState, @NotNull PoseStack matrixStack, @NotNull SubmitNodeCollector orderedRenderCommandQueue, @NotNull CameraRenderState cameraRenderState) {
        super.submit(livingEntityRenderState, matrixStack, orderedRenderCommandQueue, cameraRenderState);
    }

    @Override
    protected boolean isBodyVisible(@NotNull IllusionerEntityRenderState livingEntityRenderState) {
        return true;
    }

    protected AABB getBoundingBox(IllusionerCloneEntity illusionerEntity) {
        return super.getBoundingBoxForCulling(illusionerEntity).inflate(3.0, 0.0, 3.0);
    }
}
