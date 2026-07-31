package net.hollowed.antique.client.armor.models;

import net.hollowed.antique.index.AntiqueItems;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartNames;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.world.entity.player.PlayerModelType;

public class AdventureArmor<S extends HumanoidRenderState> extends HumanoidModel<S> {
	public final ModelPart satchel;
	public final ModelPart rightArmArmorThick;
	public final ModelPart leftArmArmorThick;
	public final ModelPart rightArmArmor;
	public final ModelPart leftArmArmor;
	public final ModelPart realBody;
	public final ModelPart rightBoot;
	public final ModelPart leftBoot;

	public AdventureArmor(ModelPart root) {
		super(root);
		this.realBody = root.getChild(PartNames.BODY).getChild("realBody");
		this.satchel = root.getChild(PartNames.BODY).getChild("satchel");
		this.rightArmArmor = root.getChild(PartNames.RIGHT_ARM).getChild("rightArmArmor");
		this.rightArmArmorThick = root.getChild(PartNames.RIGHT_ARM).getChild("rightArmArmorThick");
		this.leftArmArmor = root.getChild(PartNames.LEFT_ARM).getChild("leftArmArmor");
		this.leftArmArmorThick = root.getChild(PartNames.LEFT_ARM).getChild("leftArmArmorThick");
		this.rightBoot = root.getChild(PartNames.RIGHT_LEG).getChild("rightBoot");
		this.leftBoot = root.getChild(PartNames.LEFT_LEG).getChild("leftBoot");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition modelData = new MeshDefinition();
		PartDefinition modelPartData = modelData.getRoot();

		PartDefinition head = modelPartData.addOrReplaceChild(PartNames.HEAD, CubeListBuilder.create(), PartPose.ZERO);
		head.addOrReplaceChild(PartNames.HAT, CubeListBuilder.create(), PartPose.ZERO);

		PartDefinition body = modelPartData.addOrReplaceChild(PartNames.BODY, CubeListBuilder.create(), PartPose.ZERO);
		PartDefinition rightArm = modelPartData.addOrReplaceChild(PartNames.RIGHT_ARM, CubeListBuilder.create(), PartPose.offset(-5, 2, 0));
		PartDefinition leftArm = modelPartData.addOrReplaceChild(PartNames.LEFT_ARM, CubeListBuilder.create(), PartPose.offset(5, 2, 0));
		PartDefinition rightLeg = modelPartData.addOrReplaceChild(PartNames.RIGHT_LEG, CubeListBuilder.create(), PartPose.offset(-1.9F, 12, 0));
		PartDefinition leftLeg = modelPartData.addOrReplaceChild(PartNames.LEFT_LEG, CubeListBuilder.create(), PartPose.offset(1.9F, 12, 0));

		body.addOrReplaceChild("realBody", CubeListBuilder.create().texOffs(0, 16).addBox(-4.0F, -24.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.27F))
				.texOffs(24, 0).addBox(2.0F, -26.0F, -3.0F, 3.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(24, 11).addBox(-5.0F, -26.0F, -3.0F, 3.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(46, 16).addBox(-2.0F, -26.0F, 2.0F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0, 24, 0));

		body.addOrReplaceChild("satchel", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -24.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.32F))
				.texOffs(54, 22).addBox(-6.0F, -13.0F, -2.0F, 2.0F, 5.0F, 4.0F, new CubeDeformation(0.15F)), PartPose.offset(0, 24, 0));

		rightArm.addOrReplaceChild("rightArmArmor", CubeListBuilder.create().texOffs(14, 54).addBox(-6.0F, -24.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.8F))
				.texOffs(0, 84).addBox(-6.0F, -24.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.99F))
				.texOffs(48, 38).addBox(-6.0F, -24.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.47F)), PartPose.offset(4, 22, 0));

		leftArm.addOrReplaceChild("leftArmArmor", CubeListBuilder.create().texOffs(46, 0).addBox(3.0F, -24.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.27F))
				.texOffs(0, 48).addBox(3.0F, -24.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.47F)), PartPose.offset(-4, 22, 0));

		rightArm.addOrReplaceChild("rightArmArmorThick", CubeListBuilder.create().texOffs(14, 54).addBox(-7F, -24.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.8F))
				.texOffs(26, 96).addBox(-7F, -24.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.99F))
				.texOffs(48, 38).addBox(-7F, -24.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.47F)), PartPose.offset(4, 22, 0));

		leftArm.addOrReplaceChild("leftArmArmorThick", CubeListBuilder.create().texOffs(46, 0).addBox(3.0F, -24.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.27F))
				.texOffs(0, 83).addBox(3.0F, -24.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.47F)), PartPose.offset(-4, 22, 0));

		leftLeg.addOrReplaceChild("leftBoot", CubeListBuilder.create().texOffs(32, 38).mirror().addBox(-4.0F, -12.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.27F)).mirror(false)
				.texOffs(41, 66).mirror().addBox(-4.0F, -12.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.47F)).mirror(false)
				.texOffs(14, 79).mirror().addBox(-4.0F, -2.0F, -4.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.27F)).mirror(false), PartPose.offset(2.0F, 12.0F, 0.0F));

		rightLeg.addOrReplaceChild("rightBoot", CubeListBuilder.create().texOffs(32, 38).addBox(0.0F, -12.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.27F))
				.texOffs(41, 66).addBox(0.0F, -12.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.47F))
				.texOffs(14, 79).addBox(0.0F, -2.0F, -4.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.27F)), PartPose.offset(-2.0F, 12.0F, 0.0F));
		return LayerDefinition.create(modelData, 128, 128);
	}

	@Override
	public void setupAnim(S state) {
		boolean slim = state instanceof AvatarRenderState playerState && playerState.skin.model() == PlayerModelType.SLIM || state instanceof SkeletonRenderState;
		rightArmArmorThick.visible = leftArmArmorThick.visible = !slim;
		rightArmArmor.visible = leftArmArmor.visible = slim;

		realBody.visible = rightArm.visible = leftArm.visible = state.chestEquipment.is(AntiqueItems.MYRIAD_PAULDRONS);
		satchel.visible = state.legsEquipment.is(AntiqueItems.SATCHEL);
		rightBoot.visible = leftBoot.visible = state.feetEquipment.is(AntiqueItems.FUR_BOOTS);
	}
}