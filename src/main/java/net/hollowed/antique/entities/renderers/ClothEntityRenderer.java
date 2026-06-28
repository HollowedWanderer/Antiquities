package net.hollowed.antique.entities.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hollowed.antique.Antiquities;
import net.hollowed.antique.client.cloth.ClothOwner;
import net.hollowed.antique.client.renderer.cloth.ClothManager;
import net.hollowed.antique.entities.ClothEntity;
import net.hollowed.antique.entities.models.ClothKnotModel;
import net.hollowed.antique.index.AntiqueEntityLayers;
import net.hollowed.antique.util.ClothUtil;
import net.hollowed.antique.util.resources.ClothModelListener;
import net.hollowed.antique.util.resources.client.ClothModelData;
import net.hollowed.antique.util.resources.client.ClothSprite;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.awt.*;

@Environment(EnvType.CLIENT)
public class ClothEntityRenderer extends EntityRenderer<@NotNull ClothEntity, @NotNull ClothRenderState> {
	private final ClothKnotModel model;

	public ClothEntityRenderer(EntityRendererProvider.Context context) {
		super(context);
		model = new ClothKnotModel(context.bakeLayer(AntiqueEntityLayers.CLOTH_KNOT));
	}

	@SuppressWarnings("all")
	@Override
	public void submit(@NonNull ClothRenderState state, @NonNull PoseStack poseStack, @NotNull SubmitNodeCollector queue, @NotNull CameraRenderState cameraState) {
		ClothUtil.getClothData(state.cloth, state.entity.registryAccess()).ifPresent(cloth -> {
			poseStack.pushPose();

			Identifier modelId = cloth.value().model().orElseGet(() -> cloth.unwrapKey().orElseThrow().identifier());
			ClothModelData model = ClothModelListener.MODELS.get(modelId);

			if (model != null) {
				poseStack.pushPose();
				poseStack.scale(-1, -1, 1);

				for (ClothSprite sprite : model.fenceTiedSprites()) {
					queue.submitModel(
							this.model,
							state,
							poseStack,
							this.model.renderType(sprite.texture().withPrefix("textures/").withSuffix(".png")),
							sprite.light().map(l -> LightCoordsUtil.pack(l, l)).orElse(state.lightCoords),
							OverlayTexture.NO_OVERLAY,
							sprite.tint() ? ClothUtil.getDynamicClothColor(state.cloth, state.entity.registryAccess()).orElse(0xFFFFFFFF) : 0xFFFFFFFF,
							null,
							state.outlineColor,
							null
					);
				}

				poseStack.popPose();
			}

			ClothManager manager = ClothManager.getOrCreate(new ClothOwner.OfEntity(state.entity), Antiquities.id("spade"), cloth.value());

			if (manager != null) {
				poseStack.pushPose();
				poseStack.translate(0, 0.425, 0);
				manager.renderCloth(
						cloth,
						poseStack,
						queue,
						state.lightCoords,
						ClothUtil.getDynamicClothColor(state.cloth, state.entity.registryAccess()).orElse(0xFFFFFFFF),
						ClothUtil.getClothPatterns(state.cloth),
						state.entity.registryAccess(),
						state.tickDelta
				);
				poseStack.popPose();
			}

			poseStack.popPose();
		});
	}

	@Override
	protected boolean affectedByCulling(@NonNull ClothEntity entity) {
		return false;
	}

	public @NonNull ClothRenderState createRenderState() {
		return new ClothRenderState();
	}

	public void extractRenderState(@NonNull ClothEntity entity, @NonNull ClothRenderState state, float tickDelta) {
		super.extractRenderState(entity, state, tickDelta);
		state.cloth = entity.getCloth();
		state.entity = entity;
		state.tickDelta = tickDelta;
	}
}