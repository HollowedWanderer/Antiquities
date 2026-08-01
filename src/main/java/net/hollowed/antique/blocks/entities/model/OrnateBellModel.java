package net.hollowed.antique.blocks.entities.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class OrnateBellModel extends Model<OrnateBellModel.State> {
	private final ModelPart bell;

	public OrnateBellModel(ModelPart root) {
		super(root, RenderTypes::entityCutout);
		this.bell = root.getChild("bell");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		root.addOrReplaceChild("bell", CubeListBuilder.create().texOffs(32, 17).addBox(-2.0F, -6.0F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 13).addBox(-4.0F, -3.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-5.0F, 5.0F, -5.0F, 10.0F, 3.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(32, 13).addBox(-6.0F, -5.0F, -1.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 29).addBox(-7.0F, -5.0F, 0.0F, 14.0F, 7.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(28, 29).addBox(-6.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 14.0F, 0.0F));
		return LayerDefinition.create(mesh, 64, 64);
	}

	public void setupAnim(final OrnateBellModel.@NonNull State state) {
		super.setupAnim(state);
		float xRot = 0.0F;
		float zRot = 0.0F;
		if (state.shakeDirection != null) {
			float baseRot = Mth.sin(state.ticks / (float) Math.PI) / (4.0F + state.ticks / 3.0F);
			switch (state.shakeDirection) {
				case NORTH:
					xRot = -baseRot;
					break;
				case SOUTH:
					xRot = baseRot;
					break;
				case EAST:
					zRot = -baseRot;
					break;
				case WEST:
					zRot = baseRot;
			}
		}

		this.bell.xRot = xRot;
		this.bell.zRot = zRot;
	}

	@Environment(EnvType.CLIENT)
	public record State(float ticks, @Nullable Direction shakeDirection) {
	}
}