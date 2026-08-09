package net.hollowed.antique.entities.models;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.List;

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

		root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 12).addBox(-4.0F, -2.0F, -3.0F, 8.0F, 6.0F, 2.0F, new CubeDeformation(0.3F))
		.texOffs(0, 0).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		root.addOrReplaceChild("rodBottom", CubeListBuilder.create().texOffs(24, 0).addBox(-1.0F, -13.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        List<CubeListBuilder> list = List.of(
				CubeListBuilder.create().texOffs(8, 20).addBox(-1.0F, -13.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)),
				CubeListBuilder.create().texOffs(20, 12).addBox(-1.0F, -13.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)),
				CubeListBuilder.create().texOffs(0, 20).addBox(-1.0F, -13.0F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F))
		);

		float angle = 0;

		for (int i = 0; i < 3; i++) {
			float x = Mth.cos(angle) * 11.0F;
			float y = -2.0F + Mth.cos(i * 2 * 0.15F);
			float z = Mth.sin(angle) * 11.0F;
			root.addOrReplaceChild(getPartName(i), list.get(i), PartPose.offset(x, y, z));
			angle += (float) ((2 * Math.PI) / 3);
		}

		return LayerDefinition.create(mesh, 32, 32);
	}

	public void setupAnim(final @NonNull LivingEntityRenderState state) {
		super.setupAnim(state);
		float angle = state.ageInTicks * (float) Math.PI * -0.05F;

		for (int i = 0; i < 3; i++) {
			this.rods[i].y = 12.0F + Mth.cos((i * 4 + state.ageInTicks) * 0.15F);
			this.rods[i].x = Mth.cos(angle) * 11.0F;
			this.rods[i].z = Mth.sin(angle) * 11.0F;
			angle += (float) ((2 * Math.PI) / 3);
		}

		this.rodBottom.y = 23.0F + Mth.cos((state.ageInTicks) * 0.15F);

		this.head.y = 2.0F;
		this.head.yRot = state.yRot * (float) (Math.PI / 180.0);
		this.head.xRot = state.xRot * (float) (Math.PI / 180.0);
	}
}