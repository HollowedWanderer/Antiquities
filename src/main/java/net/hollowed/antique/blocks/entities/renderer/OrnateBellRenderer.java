package net.hollowed.antique.blocks.entities.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hollowed.antique.Antiquities;
import net.hollowed.antique.blocks.OrnateBellBlock;
import net.hollowed.antique.blocks.entities.OrnateBellBlockEntity;
import net.hollowed.antique.blocks.entities.model.OrnateBellModel;
import net.hollowed.antique.index.AntiqueBlocks;
import net.hollowed.antique.index.AntiqueEntityLayers;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

@Environment(EnvType.CLIENT)
public class OrnateBellRenderer implements BlockEntityRenderer<OrnateBellBlockEntity, OrnateBellRenderState> {
	public static final SpriteId ORNATE_BELL_TEXTURE = Sheets.BLOCK_ENTITIES_MAPPER.apply(Antiquities.id("bell/ornate_bell"));
	public static final SpriteId EMBLAZONED_BELL_TEXTURE = Sheets.BLOCK_ENTITIES_MAPPER.apply(Antiquities.id("bell/emblazoned_bell"));
	private final SpriteGetter sprites;
	private final OrnateBellModel model;

	public OrnateBellRenderer(final BlockEntityRendererProvider.Context context) {
		this.sprites = context.sprites();
		this.model = new OrnateBellModel(context.bakeLayer(AntiqueEntityLayers.ORNATE_BELL));
	}

	public @NonNull OrnateBellRenderState createRenderState() {
		return new OrnateBellRenderState();
	}

	public void extractRenderState(
		final @NonNull OrnateBellBlockEntity blockEntity,
		final @NonNull OrnateBellRenderState state,
		final float partialTicks,
		final @NonNull Vec3 cameraPosition,
		final ModelFeatureRenderer.CrumblingOverlay breakProgress
	) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

		Level level = blockEntity.getLevel();
		BlockState blockState = null;
		if (level != null) {
			blockState = level.getBlockState(blockEntity.getBlockPos());
			if (!(blockState.getBlock() instanceof OrnateBellBlock)) blockState = null;
		}

		BellAttachType type = null;
		if (blockState != null) type = blockState.getValue(OrnateBellBlock.ATTACHMENT);

		state.ticks = blockEntity.ticks + partialTicks;

		state.shakeDirection = blockEntity.shaking ? blockEntity.clickDirection : null;
		if (state.shakeDirection != null && state.shakeDirection.getAxis().equals(Direction.Axis.X)) state.shakeDirection = state.shakeDirection.getCounterClockWise();

		state.facingDirection = blockState != null ? blockState.getValue(OrnateBellBlock.FACING) : null;
		if (state.facingDirection != null && (type.equals(BellAttachType.SINGLE_WALL) || type.equals(BellAttachType.DOUBLE_WALL))) state.facingDirection = state.facingDirection.getCounterClockWise();

		if (state.shakeDirection != null && state.facingDirection != null && (state.facingDirection.equals(Direction.SOUTH) || state.facingDirection.equals(Direction.WEST))) {
			state.shakeDirection = state.shakeDirection.getOpposite();
		}

		state.emblazoned = blockState != null && blockState.is(AntiqueBlocks.EMBLAZONED_BELL);
	}

	@SuppressWarnings("ConstantConditions")
	public void submit(final OrnateBellRenderState state, final @NonNull PoseStack poseStack, final @NonNull SubmitNodeCollector submitNodeCollector, final @NonNull CameraRenderState camera) {
		OrnateBellModel.State modelState = new OrnateBellModel.State(state.ticks, state.shakeDirection);
		poseStack.pushPose();
		this.model.setupAnim(modelState);
		poseStack.translate(0.5, 1.5, 0.5);
		poseStack.mulPose(Axis.XP.rotationDegrees(180));
		if (state.facingDirection != null && state.facingDirection.getAxis().equals(Direction.Axis.X)) {
			poseStack.mulPose(Axis.YP.rotationDegrees(90));
		}
		if (state.facingDirection != null && (state.facingDirection.equals(Direction.SOUTH) || state.facingDirection.equals(Direction.WEST))) {
			poseStack.mulPose(Axis.YP.rotationDegrees(180));
		}
		submitNodeCollector.submitModel(
				this.model, modelState, poseStack, state.lightCoords, OverlayTexture.NO_OVERLAY, -1, state.emblazoned ? EMBLAZONED_BELL_TEXTURE : ORNATE_BELL_TEXTURE, this.sprites, 0, state.breakProgress
		);
		poseStack.popPose();
	}
}
