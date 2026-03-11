package net.hollowed.antique;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.hollowed.antique.client.armor.models.VanillaArmorModel;
import net.hollowed.antique.client.armor.renderers.AdventureArmorFeatureRenderer;
import net.hollowed.antique.client.armor.renderers.VanillaArmorFeatureRenderer;
import net.hollowed.antique.index.*;
import net.hollowed.antique.blocks.screens.DyeingScreen;
import net.hollowed.antique.blocks.entities.renderer.PedestalRenderer;
import net.hollowed.antique.client.armor.models.AdventureArmor;
import net.hollowed.antique.entities.models.PaleWardenModel;
import net.hollowed.antique.entities.renderer.*;
import net.hollowed.antique.items.components.MyriadToolComponent;
import net.hollowed.antique.networking.*;
import net.hollowed.antique.util.models.*;
import net.hollowed.antique.util.properties.*;
import net.hollowed.combatamenities.util.items.CAComponents;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AntiquitiesClient implements ClientModInitializer {

    //private static final Map<UUID, Deque<Vec3d>> TRAILS = new HashMap<>();

    private static long lastUseTime = 0;  // Time of last use in milliseconds
    private static final long COOLDOWN_TIME = 250;  // Cooldown time in milliseconds (500 ms = 0.5 seconds)
    private static boolean wasCrawling = false; // Store previous key state

    public static final ModelLayerLocation PALE_WARDEN_LAYER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Antiquities.MOD_ID, "pale_warden"), "main");

    public static List<Identifier> BETTER_ARMOR_LIST = new ArrayList<>();

    @SuppressWarnings("unused")
    public static void registerVanillaArmor(ItemLike... items) {
        for (ItemLike item : items) {
            BETTER_ARMOR_LIST.add(BuiltInRegistries.ITEM.getKey(item.asItem()));
        }

        ArmorRenderer.register(new VanillaArmorFeatureRenderer.Factory(), items);
    }

    @Override
    public void onInitializeClient() {

        AntiqueKeyBindings.initialize();

        ArmorRenderer.register(new AdventureArmorFeatureRenderer.Factory(), AntiqueItems.MYRIAD_PAULDRONS, AntiqueItems.SATCHEL, AntiqueItems.FUR_BOOTS);
        //registerVanillaArmor(Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS);

        ItemModels.ID_MAPPER.put(Identifier.fromNamespaceAndPath(Antiquities.MOD_ID, "satchel/selected_item"), SatchelSelectedItemModel.Unbaked.CODEC);
        ItemModels.ID_MAPPER.put(Identifier.fromNamespaceAndPath(Antiquities.MOD_ID, "bag/selected_item"), BagOfTricksSelectedItemModel.Unbaked.CODEC);
        ItemModels.ID_MAPPER.put(Identifier.fromNamespaceAndPath(Antiquities.MOD_ID, "bag/first_stack"), BagOfTricksFirstStackItemModel.Unbaked.CODEC);
        ItemModels.ID_MAPPER.put(Identifier.fromNamespaceAndPath(Antiquities.MOD_ID, "myriad_cloth"), MyriadClothItemModel.Unbaked.CODEC);
        ItemModels.ID_MAPPER.put(Identifier.fromNamespaceAndPath(Antiquities.MOD_ID, "cloth"), ClothItemModel.Unbaked.CODEC);
        ItemModels.ID_MAPPER.put(Identifier.fromNamespaceAndPath(Antiquities.MOD_ID, "cloth_pattern"), ClothPatternItemModel.Unbaked.CODEC);
        ItemModels.ID_MAPPER.put(Identifier.fromNamespaceAndPath(Antiquities.MOD_ID, "model_glow"), GlowBasicItemModel.Unbaked.CODEC);

        ItemTintSources.ID_MAPPER.put(Antiquities.id("myriad"), ClothTintSource.CODEC);

        /*
			Model Properties
		 */

        ConditionalItemModelProperties.ID_MAPPER.put(Antiquities.id("satchel/has_selected_item"), SatchelHasSelectedItemProperty.CODEC);
        ConditionalItemModelProperties.ID_MAPPER.put(Antiquities.id("bag/has_selected_item"), BagOfTricksHasSelectedItemProperty.CODEC);
        ConditionalItemModelProperties.ID_MAPPER.put(Antiquities.id("satchel/has_first_stack"), SatchelHasFirstStackItemProperty.CODEC);
        ConditionalItemModelProperties.ID_MAPPER.put(Antiquities.id("screen_open"), ScreenOpenItemProperty.CODEC);

        SelectItemModelProperties.ID_MAPPER.put(Antiquities.id("projectile_type"), ProjectileTypeProperty.TYPE);


        AntiqueParticles.initializeClient();
        MenuScreens.register(AntiqueScreenHandlerType.DYE_TABLE, DyeingScreen::new);

        /*
            Block Renderers
         */

        BlockEntityRenderers.register(AntiqueBlockEntities.PEDESTAL_BLOCK_ENTITY, context -> new PedestalRenderer());
        BlockRenderLayerMap.putBlocks(ChunkSectionLayer.CUTOUT, AntiqueBlocks.PEDESTAL, AntiqueBlocks.HOLLOW_CORE, AntiqueBlocks.JAR, AntiqueBlocks.MYRIAD_CLUSTER, AntiqueBlocks.DEEPSLATE_MYRIAD_CLUSTER);
        BlockRenderLayerMap.putBlocks(ChunkSectionLayer.CUTOUT, AntiqueBlocks.IVY);
        BlockRenderLayerMap.putBlocks(ChunkSectionLayer.TRANSLUCENT, Blocks.GLASS, Blocks.GLASS_PANE);

        /*
            Packets
         */

        PedestalPacketReceiver.registerClientPacket();
        WallJumpParticlePacketReceiver.registerClientPacket();
        IllusionerParticlePacketReceiver.registerClientPacket();
        ClothSkinPacketReceiver.registerClientPacket();
        ClothOverlayPacketReceiver.registerClientPacket();
        AddClothItemsPacketReceiver.registerClientPacket();

        /*
            Entity Renderers
         */

        EntityModelLayerRegistry.registerModelLayer(AntiqueEntityLayers.VANILLA_ARMOR, VanillaArmorModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(AntiqueEntityLayers.ADVENTURE_ARMOR, AdventureArmor::getTexturedModelData);

        EntityRenderers.register(AntiqueEntities.PALE_WARDEN, PaleWardenRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(PALE_WARDEN_LAYER, PaleWardenModel::getTexturedModelData);

        EntityRenderers.register(AntiqueEntities.MYRIAD_SHOVEL, MyriadShovelEntityRenderer::new);
        EntityRenderers.register(AntiqueEntities.MYRIAD_SHOVEL_PART, MyriadShovelPartRenderer::new);

        EntityRenderers.register(AntiqueEntities.ILLUSIONER, IllusionerEntityRenderer::new);
        EntityRenderers.register(AntiqueEntities.ILLUSIONER_CLONE, IllusionerCloneEntityRenderer::new);
        EntityRenderers.register(AntiqueEntities.SMOKE_BOMB, ThrownItemRenderer::new);
        EntityRenderers.register(AntiqueEntities.CAKE_ENTITY, CakeRenderer::new);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                return;
            }

            if (AntiqueKeyBindings.crawl.consumeClick() && !client.player.isUnderWater()) { // Detect key press event
                wasCrawling = !wasCrawling; // Toggle crawling state
                ClientPlayNetworking.send(new CrawlPacketPayload(wasCrawling));
            }
            if (!client.player.onGround()) {
                wasCrawling = false;
                ClientPlayNetworking.send(new CrawlPacketPayload(false));
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                Vec3 pos = client.player.position();
                AABB box = new AABB(pos.x - 1, pos.y - 1, pos.z - 1, pos.x + 1, pos.y + 1, pos.z + 1);
                box = box.inflate(60);
                if (client.level != null) {
                    for (Snowball entity : client.level.getEntitiesOfClass(Snowball.class, box, arrowEntity -> true)) {
                        if (Math.random() > 0.65) {
                            entity.level().addParticle(ParticleTypes.ITEM_SNOWBALL, entity.getX(), entity.getY(), entity.getZ(), 0, 0, 0);
                        }
                    }
                }
            }
        });

        // Right Click Listener
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Ensure the client player is not null
            if (client.player == null) {
                return;
            }

            long currentTime = System.currentTimeMillis();  // Get current time in milliseconds

            // Check if the show satchel key is pressed and if the cooldown period has passed
            if (AntiqueKeyBindings.showSatchel.isDown()) {
                // Check if right-click is pressed and if the cooldown has passed
                if (client.options.keyUse.isDown() && currentTime - lastUseTime >= COOLDOWN_TIME) {
                    // Send the packet if right-click is detected and the other keys are pressed
                    ClientPlayNetworking.send(new SatchelPacketPayload(true));

                    // Update the last use time to the current time
                    lastUseTime = currentTime;
                }
            }
        });

        ItemTooltipCallback.EVENT.register((itemStack, tooltipContext, tooltipType, list) -> {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).toString().contains("item.color")) {
                    Color color = new Color(itemStack.getOrDefault(DataComponents.DYED_COLOR, new DyedItemColor(0xFFFFFF)).rgb());
                    list.set(i, list.get(i).copy().withColor(color.brighter().getRGB()));
                }
            }

            if (itemStack.is(AntiqueItems.MYRIAD_TOOL)) {
                int toRemove = -1;
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).toString().contains("dyed")) {
                        toRemove = i;
                    }
                }
                if (toRemove != -1) list.remove(toRemove);
            }
            if (itemStack.is(AntiqueItems.MYRIAD_TOOL)) {
                MyriadToolComponent component = itemStack.getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, Antiquities.getDefaultMyriadTool());

                int toRemove = -1;
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).toString().contains("item.color")) {
                        toRemove = i;
                    }
                }
                if (toRemove != -1) list.remove(toRemove);
                Component line = Component.translatable("item.antique.myriad_tool.no_tool").withColor(11184810);

                ItemStack storedStack = itemStack.getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, Antiquities.getDefaultMyriadTool()).toolBit();

                if (!storedStack.isEmpty()) {
                    String string = storedStack.getItem().getDescriptionId();
                    string = string.substring(20);
                    string = "item.antique.myriad_tool." + string.substring(0, string.indexOf("_"));
                    line = Component.translatable(string).withColor(11184810);
                }

                list.add(1, line);

                if (!component.clothType().isEmpty()) {
                    String clothName = component.clothType().replace(":", ".");
                    Component cloth = Component.literal(" - ").append(Component.translatable("item." + clothName)).withColor(new Color(component.clothColor()).brighter().getRGB());
                    list.add(2, cloth);
                }

                String patternName = String.valueOf(component.clothPattern()).replace(":", ".");
                Component pattern = Component.literal(" - ").append(Component.translatable("item." + patternName + "_cloth_pattern")).withColor(new Color(component.patternColor()).brighter().getRGB());
                if (itemStack.getOrDefault(CAComponents.BOOLEAN_PROPERTY, false)) {
                    pattern = pattern.copy().append(Component.literal(" - ").withColor(0xff4adbb8)).append(Component.translatable("item.antique.glowing").withColor(0xff4adbb8));
                }
                if (!patternName.isEmpty()) {
                    list.add(3, pattern);
                }
            }
            if (itemStack.is(AntiqueItems.CLOTH_PATTERN)) {
                if (itemStack.getOrDefault(CAComponents.BOOLEAN_PROPERTY, false)) {
                    list.add(2, Component.translatable("item.antique.glowing").withColor(0xff4adbb8));
                }
            }
            if (itemStack.is(Items.BOW) || itemStack.is(Items.CROSSBOW)) {
                int toRemove = -1;
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).toString().contains("item.color")) {
                        toRemove = i;
                    }
                }
                if (toRemove != -1) list.remove(toRemove);
            }
        });


        /*
            This entire section is ARR
         */

//        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
//            MinecraftClient client = MinecraftClient.getInstance();
//            if (client.player == null) return;
//            Vec3d pos = client.player.getPos();
//            Box box = new Box(pos.x - 1, pos.y - 1, pos.z - 1, pos.x + 1, pos.y + 1, pos.z + 1);
//            box = box.expand(60);
//
//            if (client.world != null) {
//                for (ArrowEntity entity : client.world.getEntitiesByClass(ArrowEntity.class, box, arrowEntity -> true)) {
//                    Deque<Vec3d> trail = TRAILS.computeIfAbsent(entity.getUuid(), id -> new ArrayDeque<>());
//                    Vector3f color = ColorHelper.toVector(entity.getColor());
//
//                    if (entity.isRegionUnloaded()) {
//                        TRAILS.remove(entity.getUuid());
//                    }
//
//                    RenderUtils.renderEntityTrail(
//                            context.matrixStack(),
//                            Objects.requireNonNull(context.consumers()).getBuffer(RenderLayer.getEntityTranslucentEmissive(Identifier.of(Antiquities.MOD_ID, "textures/render/color.png"))),
//                            context.camera(),
//                            entity,
//                            context.tickCounter().getTickProgress(false),
//                            trail,
//                            200,
//                            0.1f,
//                            0.001f,
//                            200,
//                            75,
//                            color.x,
//                            color.y,
//                            color.z,
//                            new Vec3d(0.0, 0, 0.0)
//                    );
//                }
//                for (SplashPotionEntity entity : client.world.getEntitiesByClass(SplashPotionEntity.class, box, arrowEntity -> true)) {
//                    Deque<Vec3d> trail = TRAILS.computeIfAbsent(entity.getUuid(), id -> new ArrayDeque<>());
//                    Vector3f color = ColorHelper.toVector(entity.getStack().getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT).getColor());
//
//                    if (entity.isRegionUnloaded()) {
//                        TRAILS.remove(entity.getUuid());
//                    }
//
//                    RenderUtils.renderEntityTrail(
//                            context.matrixStack(),
//                            Objects.requireNonNull(context.consumers()).getBuffer(RenderLayer.getEntityTranslucent(Identifier.of(Antiquities.MOD_ID, "textures/render/color.png"))),
//                            context.camera(),
//                            entity,
//                            context.tickCounter().getTickProgress(false),
//                            trail,
//                            40,
//                            0.1f,
//                            0.001f,
//                            200,
//                            0,
//                            color.x,
//                            color.y,
//                            color.z,
//                            new Vec3d(0.0, 0, 0.0)
//                    );
//                }
//                for (LingeringPotionEntity entity : client.world.getEntitiesByClass(LingeringPotionEntity.class, box, arrowEntity -> true)) {
//                    Deque<Vec3d> trail = TRAILS.computeIfAbsent(entity.getUuid(), id -> new ArrayDeque<>());
//                    Vector3f color = ColorHelper.toVector(entity.getStack().getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT).getColor());
//
//                    if (entity.isRegionUnloaded()) {
//                        TRAILS.remove(entity.getUuid());
//                    }
//
//                    RenderUtils.renderEntityTrail(
//                            context.matrixStack(),
//                            Objects.requireNonNull(context.consumers()).getBuffer(RenderLayer.getEntityTranslucent(Identifier.of(Antiquities.MOD_ID, "textures/render/color.png"))),
//                            context.camera(),
//                            entity,
//                            context.tickCounter().getTickProgress(false),
//                            trail,
//                            40,
//                            0.1f,
//                            0.001f,
//                            200,
//                            0,
//                            color.x,
//                            color.y,
//                            color.z,
//                            new Vec3d(0.0, 0, 0.0)
//                    );
//                }
//                for (TridentEntity entity : client.world.getEntitiesByClass(TridentEntity.class, box, arrowEntity -> true)) {
//                    Deque<Vec3d> trail = TRAILS.computeIfAbsent(entity.getUuid(), id -> new ArrayDeque<>());
//                    Vector3f color = new Vector3f((float) 173 / 255.0F, (float) 255 / 255.0F, (float) 237 / 255.0F);
//
//                    if (entity.isRegionUnloaded()) {
//                        TRAILS.remove(entity.getUuid());
//                    }
//
//                    RenderUtils.renderEntityTrail(
//                            context.matrixStack(),
//                            Objects.requireNonNull(context.consumers()).getBuffer(RenderLayer.getEntityTranslucentEmissive(Identifier.of(Antiquities.MOD_ID, "textures/render/color.png"))),
//                            context.camera(),
//                            entity,
//                            context.tickCounter().getTickProgress(false),
//                            trail,
//                            200,
//                            0.1f,
//                            0.001f,
//                            200,
//                            75,
//                            color.x,
//                            color.y,
//                            color.z,
//                            new Vec3d(0.0, 0, 0.0)
//                    );
//                }
//                for (WindChargeEntity entity : client.world.getEntitiesByClass(WindChargeEntity.class, box, arrowEntity -> true)) {
//                    Deque<Vec3d> trail = TRAILS.computeIfAbsent(entity.getUuid(), id -> new ArrayDeque<>());
//                    Vector3f color = new Vector3f((float) 1, (float) 1, (float) 1);
//
//                    if (entity.isRegionUnloaded()) {
//                        TRAILS.remove(entity.getUuid());
//                    }
//
//                    RenderUtils.renderEntityTrail(
//                            context.matrixStack(),
//                            Objects.requireNonNull(context.consumers()).getBuffer(RenderLayer.getEntityTranslucentEmissive(Identifier.of(Nitrogen.MOD_ID, "textures/render/color.png"))),
//                            context.camera(),
//                            entity,
//                            context.tickCounter().getTickProgress(false),
//                            trail,
//                            300,
//                            0.1f,
//                            0.001f,
//                            255,
//                            75,
//                            color.x,
//                            color.y,
//                            color.z,
//                            new Vec3d(0.0, 0, 0.0)
//                    );
//                }
//                for (MyriadShovelEntity entity : client.world.getEntitiesByClass(MyriadShovelEntity.class, box, arrowEntity -> true)) {
//                    Deque<Vec3d> trail = TRAILS.computeIfAbsent(entity.getUuid(), id -> new ArrayDeque<>());
//                    Vector3f color = ColorHelper.toVector(entity.getDyeColor());
//
//                    if (entity.isRegionUnloaded()) {
//                        TRAILS.remove(entity.getUuid());
//                    }
//
//                    RenderUtils.renderEntityTrail(
//                            context.matrixStack(),
//                            Objects.requireNonNull(context.consumers()).getBuffer(RenderLayer.getEntityTranslucentEmissive(Identifier.of(Antiquities.MOD_ID, "textures/render/color.png"))),
//                            context.camera(),
//                            entity,
//                            context.tickCounter().getTickProgress(false),
//                            trail,
//                            200,
//                            0.1f,
//                            0.001f,
//                            200,
//                            75,
//                            color.x,
//                            color.y,
//                            color.z,
//                            new Vec3d(0.0, 0, 0.0)
//                    );
//                }
//                for (FireworkRocketEntity entity : client.world.getEntitiesByClass(FireworkRocketEntity.class, box, arrowEntity -> true)) {
//                    Deque<Vec3d> trail = TRAILS.computeIfAbsent(entity.getUuid(), id -> new ArrayDeque<>());
//                    Vector3f color = new Vector3f((float) 1, (float) 1, (float) 1);
//
//                    if (entity.isRegionUnloaded()) {
//                        TRAILS.remove(entity.getUuid());
//                    }
//
//                    RenderUtils.renderEntityTrail(
//                            context.matrixStack(),
//                            Objects.requireNonNull(context.consumers()).getBuffer(RenderLayer.getEntityTranslucentEmissive(Identifier.of(Antiquities.MOD_ID, "textures/render/color.png"))),
//                            context.camera(),
//                            entity,
//                            context.tickCounter().getTickProgress(false),
//                            trail,
//                            200,
//                            0.1f,
//                            0.001f,
//                            200,
//                            0,
//                            color.x,
//                            color.y,
//                            color.z,
//                            new Vec3d(0.0, 0, 0.0)
//                    );
//                }
//                for (SpectralArrowEntity entity : client.world.getEntitiesByClass(SpectralArrowEntity.class, box, arrowEntity -> true)) {
//                    Deque<Vec3d> trail = TRAILS.computeIfAbsent(entity.getUuid(), id -> new ArrayDeque<>());
//                    Vector3f color = new Vector3f((float) 255 / 255, (float) 248 / 255, (float) 93 / 255);
//
//                    if (entity.isRegionUnloaded()) {
//                        TRAILS.remove(entity.getUuid());
//                    }
//
//                    RenderUtils.renderEntityTrail(
//                            context.matrixStack(),
//                            Objects.requireNonNull(context.consumers()).getBuffer(RenderLayer.getEntityTranslucentEmissive(Identifier.of(Antiquities.MOD_ID, "textures/render/color.png"))),
//                            context.camera(),
//                            entity,
//                            context.tickCounter().getTickProgress(false),
//                            trail,
//                            200,
//                            0.1f,
//                            0.001f,
//                            200,
//                            75,
//                            color.x,
//                            color.y,
//                            color.z,
//                            new Vec3d(0.0, 0, 0.0)
//                    );
//                }
//                for (SmokeBombEntity entity : client.world.getEntitiesByClass(SmokeBombEntity.class, box, arrowEntity -> true)) {
//                    Deque<Vec3d> trail = TRAILS.computeIfAbsent(entity.getUuid(), id -> new ArrayDeque<>());
//                    Vector3f color = new Vector3f(1.0f, 0.216f, 0.51f);
//
//                    if (entity.isRegionUnloaded()) {
//                        TRAILS.remove(entity.getUuid());
//                    }
//
//                    RenderUtils.renderEntityTrail(
//                            context.matrixStack(),
//                            Objects.requireNonNull(context.consumers()).getBuffer(RenderLayer.getEntityTranslucentEmissive(Identifier.of(Antiquities.MOD_ID, "textures/render/color.png"))),
//                            context.camera(),
//                            entity,
//                            context.tickCounter().getTickProgress(false),
//                            trail,
//                            75,
//                            0.1f,
//                            0.001f,
//                            200,
//                            75,
//                            color.x,
//                            color.y,
//                            color.z,
//                            new Vec3d(0.0, 0, 0.0)
//                    );
//                }
//                for (AllayEntity entity : client.world.getEntitiesByClass(AllayEntity.class, box, arrowEntity -> true)) {
//                    Deque<Vec3d> trail = TRAILS.computeIfAbsent(entity.getUuid(), id -> new ArrayDeque<>());
//                    Vector3f color = new Vector3f((float) 95 / 255, (float) 205 / 255, (float) 228 / 255);
//
//                    if (entity.isRegionUnloaded()) {
//                        TRAILS.remove(entity.getUuid());
//                    }
//
//                    RenderUtils.renderEntityTrail(
//                            context.matrixStack(),
//                            Objects.requireNonNull(context.consumers()).getBuffer(RenderLayer.getEntitySmoothCutout(Identifier.of(Nitrogen.MOD_ID, "textures/render/color.png"))),
//                            context.camera(),
//                            entity,
//                            context.tickCounter().getTickProgress(false),
//                            trail,
//                            200,
//                            0.1f,
//                            0.001f,
//                            255,
//                            0,
//                            color.x,
//                            color.y,
//                            color.z,
//                            new Vec3d(0.0, 0.25, 0.0)
//                    );
//                }
//            }
//        });

        /*
            End of ARR section
         */
    }
}
