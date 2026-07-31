package net.hollowed.antique.entities.models;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

import java.util.Arrays;

public class BilletModel extends EntityModel<LivingEntityRenderState> {
	private final ModelPart head;
	private final ModelPart rodBottom;
	private final ModelPart[] rods;

	public BilletModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
		this.rodBottom = root.getChild("rodBottom");
		this.rods = new ModelPart[3];
		Arrays.setAll(rods, i -> root.getChild(getPartName(i)));
	}

	private static String getPartName(final int i) {
		return "rod" + i;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 12).addBox(-4.0F, -23.0F, -3.0F, 8.0F, 6.0F, 2.0F, new CubeDeformation(0.3F))
		.texOffs(0, 0).addBox(-3.0F, -23.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		root.addOrReplaceChild("rodBottom", CubeListBuilder.create().texOffs(24, 0).addBox(-1.0F, -13.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		root.addOrReplaceChild("rod0", CubeListBuilder.create().texOffs(8, 20).addBox(-1.0F, -13.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));
		root.addOrReplaceChild("rod1", CubeListBuilder.create().texOffs(20, 12).addBox(-1.0F, -13.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));
		root.addOrReplaceChild("rod2", CubeListBuilder.create().texOffs(0, 20).addBox(-1.0F, -13.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		return LayerDefinition.create(mesh, 32, 32);
	}
}