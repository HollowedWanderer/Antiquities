package net.hollowed.antique.entities.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hollowed.antique.Antiquities;
import net.hollowed.antique.client.renderer.cloth.ClothManager;
import net.hollowed.antique.entities.MyriadShovelEntity;
import net.hollowed.antique.index.AntiqueDataComponentTypes;
import net.hollowed.antique.index.AntiqueItems;
import net.hollowed.antique.items.components.MyriadToolComponent;
import net.hollowed.antique.util.resources.ClientClothData;
import net.hollowed.antique.util.resources.ClothSkin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.awt.*;

@Environment(EnvType.CLIENT)
public class MyriadShovelEntityRenderer extends EntityRenderer<@NotNull MyriadShovelEntity, @NotNull MyriadShovelRenderState> {

	public MyriadShovelEntityRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public void submit(MyriadShovelRenderState state, PoseStack matrixStack, @NotNull SubmitNodeCollector queue, @NotNull CameraRenderState cameraState) {
		matrixStack.pushPose();

		float multiplier = 1.25F;

		matrixStack.translate(state.entity.getViewVector(0).multiply(multiplier, multiplier, -multiplier));

		matrixStack.mulPose(Axis.YP.rotationDegrees(state.entity.getYRot() - 180.0F));
		matrixStack.mulPose(Axis.XP.rotationDegrees(state.entity.getXRot() - 105.0F));

		matrixStack.scale(1.5F, 1.5F, 1.5F);
		matrixStack.translate(0, 0, 0.125);

		matrixStack.mulPose(Axis.YP.rotationDegrees(90.0F));
		matrixStack.mulPose(Axis.ZP.rotationDegrees(15.0F));
		matrixStack.mulPose(Axis.XP.rotationDegrees(-20.0F));

		if (state.entity instanceof MyriadShovelEntity entity) {
			ItemStack shovel = entity.getPickupItemStackOrigin();
			shovel.set(AntiqueDataComponentTypes.MYRIAD_TOOL, new MyriadToolComponent(
					AntiqueItems.MYRIAD_SHOVEL_HEAD.getDefaultInstance(),
					state.cloth.map(holder -> holder.unwrapKey().orElseThrow().identifier()),
					state.pattern,
					state.color,
					state.overlayColor,
					state.emissiveItem
			));

			ItemStackRenderState stackRenderState = new ItemStackRenderState();
			Minecraft.getInstance().getItemModelResolver().appendItemLayers(stackRenderState, shovel, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, Minecraft.getInstance().level, null, 1);
			stackRenderState.submit(matrixStack, queue, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);

			ClothSkin data = state.cloth.map(Holder::value).orElse(ClothSkin.DEFAULT);

			ClothManager manager = ClothManager.getOrCreate(entity, Antiquities.id("spade"), data);
			if(manager != null) {
				matrixStack.translate(0.05, 0.3, 0.1);
				manager.renderCloth(
						data,
						matrixStack,
						queue,
						state.lightCoords,
						state.glow,
						new Color(state.color.getColorClient()),
						new Color(state.overlayColor),
						state.pattern
				);
			}
		}
		matrixStack.popPose();
	}

	@Override
	protected boolean affectedByCulling(@NonNull MyriadShovelEntity entity) {
		return false;
	}

	public @NonNull MyriadShovelRenderState createRenderState() {
		return new MyriadShovelRenderState();
	}

	public void extractRenderState(@NonNull MyriadShovelEntity entity, @NonNull MyriadShovelRenderState state, float f) {
		super.extractRenderState(entity, state, f);
		state.entity = entity;
		state.stack = entity.getPickupItemStackOrigin();
		state.color = entity.getDyeColor();
		state.overlayColor = entity.getOverlayColor();
		state.isEnchanted = entity.isEnchanted();
		state.glow = entity.getGlow();
		state.cloth = ClothSkin.getHolder(entity.getCloth(), entity.level());
		state.pattern = entity.getPattern();
		state.emissiveItem = entity.getEmissiveItem();
	}
}