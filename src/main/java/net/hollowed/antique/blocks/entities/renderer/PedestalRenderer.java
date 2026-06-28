package net.hollowed.antique.blocks.entities.renderer;

import net.hollowed.antique.blocks.entities.PedestalBlockEntity;
import net.hollowed.antique.util.interfaces.duck.BipedEntityRenderStateAccess;
import net.hollowed.antique.util.resources.PedestalDisplayData;
import net.hollowed.antique.util.resources.PedestalDisplayListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.CatRenderState;
import net.minecraft.client.renderer.entity.state.ChickenRenderState;
import net.minecraft.client.renderer.entity.state.CowRenderState;
import net.minecraft.client.renderer.entity.state.EndCrystalRenderState;
import net.minecraft.client.renderer.entity.state.LlamaRenderState;
import net.minecraft.client.renderer.entity.state.PhantomRenderState;
import net.minecraft.client.renderer.entity.state.PigRenderState;
import net.minecraft.client.renderer.entity.state.PiglinRenderState;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.client.renderer.entity.state.ZombieVillagerRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.equine.TraderLlama;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.EndCrystalItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class PedestalRenderer implements BlockEntityRenderer<@NotNull PedestalBlockEntity, @NotNull PedestalRenderState> {

    private static final Vec3 ITEM_POS = new Vec3(0.5, 1.5, 0.5);

    public PedestalRenderer() {}

    @Override
    public void submit(@NotNull PedestalRenderState state, @NonNull PoseStack poseStack, @NonNull SubmitNodeCollector submitNodeCollector, @NonNull CameraRenderState camera) {
        ItemStack heldItem = state.storedStack;
        if (!heldItem.isEmpty()) {
            float tickDelta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
            double preciseTime = (state.worldTime) % 360;
            float rotation = (float) ((preciseTime + tickDelta) * 3);

            float bob = (float) Math.sin((Math.toRadians(rotation))) * 0.075f;

            if (heldItem.getItem() instanceof EndCrystalItem) {
                renderEndCrystalEntity(camera, submitNodeCollector, poseStack, state.world, tickDelta, state.lightCoords);
            } else if (heldItem.getItem() instanceof SpawnEggItem) {
                renderEntityFromSpawnEgg(heldItem, ITEM_POS.add(0, bob, 0), camera, submitNodeCollector, poseStack, state.world, rotation, tickDelta, state.lightCoords);
            } else {
                renderItem(heldItem, ITEM_POS.add(0, bob, 0), rotation, poseStack, submitNodeCollector, state.lightCoords);
            }
        }
    }

    private void renderItem(ItemStack itemStack, Vec3 offset, float yRot, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light) {
        PedestalDisplayData data = PedestalDisplayListener.getTransform(BuiltInRegistries.ITEM.getKey(itemStack.getItem()));
        List<Float> translations = data.translations();
        List<Float> rotations = data.rotations();
        List<Float> scales = data.scale();

        poseStack.pushPose();
        poseStack.translate(offset.x, offset.y, offset.z);
        poseStack.translate(translations.getFirst(), translations.get(1), translations.get(2));
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(rotations.getFirst()));
        poseStack.mulPose(Axis.YP.rotationDegrees(rotations.get(1)));
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotations.get(2)));
        poseStack.scale(0.6f, 0.6f, 0.6f);
        poseStack.scale(scales.getFirst(), scales.get(1), scales.get(2));

        ItemStackRenderState stackRenderState = new ItemStackRenderState();
        Minecraft.getInstance().getItemModelResolver().appendItemLayers(stackRenderState, itemStack, ItemDisplayContext.FIXED, Minecraft.getInstance().level, null, 1);
        stackRenderState.submit(poseStack, submitNodeCollector, light, OverlayTexture.NO_OVERLAY, 0);

        poseStack.popPose();
    }

    private void renderEndCrystalEntity(CameraRenderState cameraState, SubmitNodeCollector queue, PoseStack matrices, Level world, float tickDelta, int light) {
        matrices.pushPose();
        if (world.isClientSide()) {
            EndCrystalRenderState entityState = new EndCrystalRenderState();
            entityState.showsBottom = false;
            entityState.passengerOffset = PedestalRenderer.ITEM_POS;
            float scale = 0.75F;
            matrices.scale(scale, scale, scale);
            matrices.translate(0.5 * (1 - scale), 0.5 * (1 - scale), 0.5 * (1 - scale));
            entityState.ageInTicks = world.getGameTime() % 24000 + tickDelta;
            entityState.entityType = EntityTypes.END_CRYSTAL;
            entityState.lightCoords = light;
            EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            dispatcher.submit(entityState, cameraState, 0, 0, 0, matrices, queue);
        }
        matrices.popPose();
    }

    private void renderEntityFromSpawnEgg(ItemStack stack, Vec3 offset, CameraRenderState cameraState, SubmitNodeCollector queue, PoseStack matrices, Level world, float yRot, float tickDelta, int light) {
        matrices.pushPose();
        EntityType<?> entityType = SpawnEggItem.getType(stack);
        if (entityType == null) return;
        Entity entity = entityType.create(world, EntitySpawnReason.MOB_SUMMONED);
        if (entity instanceof LivingEntity livingEntity) {
            EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            var entityState = dispatcher.getRenderer(livingEntity).createRenderState();

            matrices.translate(offset.x, offset.y, offset.z);
            matrices.translate(0, entity.getBbHeight() / 2.0, 0);
            matrices.mulPose(Axis.YP.rotationDegrees(yRot));
            if (stack.get(DataComponents.CUSTOM_NAME) != null) {
                entityState.nameTag = stack.get(DataComponents.CUSTOM_NAME);
            }
            matrices.translate(0, -entity.getBbHeight() / 2.0, 0);
            entityState.passengerOffset = Vec3.ZERO;
            entityState.entityType = entityType;
            entityState.ageInTicks = world.getGameTime() % 24000 + tickDelta;
            entityState.lightCoords = light;

            if (entityState instanceof BipedEntityRenderStateAccess access) {
                access.antique$setEntity(livingEntity);
            }
            if (entityState instanceof PhantomRenderState phantomEntityRenderState) {
                phantomEntityRenderState.flapTime = entityState.ageInTicks;
            }
            if (entityState instanceof ChickenRenderState chickenEntityRenderState && livingEntity instanceof Chicken chickenEntity) {
                chickenEntityRenderState.variant = chickenEntity.getVariant().value();
            }
            if (entityState instanceof CatRenderState catEntityRenderState && livingEntity instanceof Cat catEntity) {
                catEntityRenderState.texture = catEntity.getVariant().value().assetInfo(false).texturePath();
            }
            if (entityState instanceof PigRenderState pigEntityRenderState && livingEntity instanceof Pig pigEntity) {
                pigEntityRenderState.variant = pigEntity.getVariant().value();
            }
            if (entityState instanceof CowRenderState cowEntityRenderState && livingEntity instanceof Cow cowEntity) {
                cowEntityRenderState.variant = cowEntity.getVariant().value();
            }
            if (entityState instanceof PiglinRenderState piglinEntityRenderState && livingEntity instanceof PiglinBrute) {
                piglinEntityRenderState.isBrute = true;
            }
            if (entityState instanceof LlamaRenderState llamaEntityRenderState && entity instanceof TraderLlama) {
                llamaEntityRenderState.isTraderLlama = true;
            }
            if (entityState instanceof VillagerRenderState villagerEntityRenderState && entity instanceof Villager villagerEntity) {
                villagerEntityRenderState.villagerData = villagerEntity.getVillagerData();
            }
            if (entityState instanceof ZombieVillagerRenderState villagerEntityRenderState && entity instanceof ZombieVillager villagerEntity) {
                villagerEntityRenderState.villagerData = villagerEntity.getVillagerData().withProfession(BuiltInRegistries.VILLAGER_PROFESSION.getOrThrow(VillagerProfession.NONE));
            }

            dispatcher.submit(entityState, cameraState, 0, 0, 0, matrices, queue);
        }
        matrices.popPose();
    }

    @Override
    public @NonNull PedestalRenderState createRenderState() {
        return new PedestalRenderState();
    }

    @Override
    public void extractRenderState(@NonNull PedestalBlockEntity blockEntity, @NonNull PedestalRenderState state, float f, @NotNull Vec3 vec3, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, f, vec3, crumblingOverlay);
        state.storedStack = blockEntity.getItem(0);
        state.world = blockEntity.getLevel();
        state.worldTime = blockEntity.getLevel() != null ? blockEntity.getLevel().getGameTime() : 1L;
    }
}
