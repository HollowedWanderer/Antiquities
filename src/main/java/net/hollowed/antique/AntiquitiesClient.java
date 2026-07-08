package net.hollowed.antique;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.hollowed.antique.client.armor.renderers.AdventureArmorFeatureRenderer;
import net.hollowed.antique.entities.models.ClothKnotModel;
import net.hollowed.antique.index.*;
import net.hollowed.antique.blocks.screens.DyeingScreen;
import net.hollowed.antique.blocks.entities.renderer.PedestalRenderer;
import net.hollowed.antique.client.armor.models.AdventureArmor;
import net.hollowed.antique.entities.renderers.*;
import net.hollowed.antique.items.components.MyriadToolComponent;
import net.hollowed.antique.networking.*;
import net.hollowed.antique.util.ClothUtil;
import net.hollowed.antique.util.interfaces.duck.ClothAccess;
import net.hollowed.antique.util.models.*;
import net.hollowed.antique.util.properties.*;
import net.hollowed.antique.util.resources.SewnClothPattern;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.awt.*;

public class AntiquitiesClient implements ClientModInitializer {

    //private static final Map<UUID, Deque<Vec3d>> TRAILS = new HashMap<>();

    private static long lastUseTime = 0;  // Time of last use in milliseconds
    private static final long COOLDOWN_TIME = 250;  // Cooldown time in milliseconds (500 ms = 0.5 seconds)
    private static boolean wasCrawling = false; // Store previous key state

    public static final Identifier CLOTHS_ATLAS = Antiquities.id("cloths");
    public static final Identifier CLOTHS_ATLAS_TEXTURE = CLOTHS_ATLAS.withPath(path -> "textures/atlas/" + path + ".png");

    @Override
    public void onInitializeClient() {

        ClientTickEvents.START_LEVEL_TICK.register(level -> ((ClothAccess) level).antique$tick());

        AntiqueKeyBindings.initialize();

        ArmorRenderer.register(new AdventureArmorFeatureRenderer.Factory(), AntiqueItems.MYRIAD_PAULDRONS, AntiqueItems.SATCHEL, AntiqueItems.FUR_BOOTS);

        ItemModels.ID_MAPPER.put(Antiquities.id("satchel/selected_item"), SatchelSelectedItemModel.Unbaked.CODEC);
        ItemModels.ID_MAPPER.put(Antiquities.id("bag/selected_item"), BagOfTricksSelectedItemModel.Unbaked.CODEC);
        ItemModels.ID_MAPPER.put(Antiquities.id("bag/first_stack"), BagOfTricksFirstStackItemModel.Unbaked.CODEC);
        ItemModels.ID_MAPPER.put(Antiquities.id("myriad_cloth"), TiedClothItemModel.Unbaked.CODEC);
        ItemModels.ID_MAPPER.put(Antiquities.id("cloth"), ClothItemModel.Unbaked.CODEC);
        ItemModels.ID_MAPPER.put(Antiquities.id("cloth_pattern"), ClothPatternItemModel.Unbaked.CODEC);
        ItemModels.ID_MAPPER.put(Antiquities.id("model_glow"), GlowBasicItemModel.Unbaked.CODEC);

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

        BlockEntityRenderers.register(AntiqueBlockEntities.PEDESTAL_BLOCK_ENTITY, _ -> new PedestalRenderer());

        /*
            Packets
         */

        PedestalPacketReceiver.registerClientPacket();
        WallJumpParticlePacketReceiver.registerClientPacket();
        IllusionerParticlePacketReceiver.registerClientPacket();
        AddClothItemsPacketReceiver.registerClientPacket();
        ShockwaveParticlesReceiver.registerClientPacket();

        /*
            Entity Renderers
         */

        ModelLayerRegistry.registerModelLayer(AntiqueEntityLayers.ADVENTURE_ARMOR, AdventureArmor::getTexturedModelData);
        ModelLayerRegistry.registerModelLayer(AntiqueEntityLayers.CLOTH_KNOT, ClothKnotModel::createBodyLayer);

        EntityRenderers.register(AntiqueEntities.CLOTH, ClothEntityRenderer::new);
        EntityRenderers.register(AntiqueEntities.MYRIAD_SHOVEL, MyriadShovelEntityRenderer::new);
        EntityRenderers.register(AntiqueEntities.MYRIAD_SHOVEL_PART, MyriadShovelPartRenderer::new);

        EntityRenderers.register(AntiqueEntities.ILLUSIONER, IllusionerEntityRenderer::new);
        EntityRenderers.register(AntiqueEntities.ILLUSIONER_CLONE, IllusionerCloneEntityRenderer::new);
        EntityRenderers.register(AntiqueEntities.SMOKE_BOMB, ThrownItemRenderer::new);
        EntityRenderers.register(AntiqueEntities.CAKE_ENTITY, CakeRenderer::new);

        // Crawl handling
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                return;
            }

            if (AntiqueKeyBindings.crawl.consumeClick() && !client.player.isUnderWater()) {
                wasCrawling = !wasCrawling;
                ClientPlayNetworking.send(new CrawlPacketPayload(wasCrawling));
            }
            if (!client.player.onGround()) {
                wasCrawling = false;
                ClientPlayNetworking.send(new CrawlPacketPayload(false));
            }
        });

        // Snowball particle adder
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                Vec3 pos = client.player.position();
                AABB box = new AABB(pos.x - 1, pos.y - 1, pos.z - 1, pos.x + 1, pos.y + 1, pos.z + 1);
                box = box.inflate(60);
                if (client.level != null) {
                    for (Snowball entity : client.level.getEntitiesOfClass(Snowball.class, box, _ -> true)) {
                        if (Math.random() > 0.65) {
                            entity.level().addParticle(ParticleTypes.ITEM_SNOWBALL, entity.getX(), entity.getY(), entity.getZ(), 0, 0, 0);
                        }
                    }
                }
            }
        });

        // Right click listener
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            long currentTime = System.currentTimeMillis();
            if (AntiqueKeyBindings.showSatchel.isDown()) {
                if (client.options.keyUse.isDown() && currentTime - lastUseTime >= COOLDOWN_TIME) {
                    ClientPlayNetworking.send(new SatchelPacketPayload(true));
                    lastUseTime = currentTime;
                }
            }
        });

        ItemTooltipCallback.EVENT.register((itemStack, context, _, list) -> {
            list.replaceAll(text -> {
                if (text.getContents() instanceof TranslatableContents translatable && translatable.getKey().contains("item.color")) {
                    return text.copy().withColor(new Color(itemStack.getOrDefault(DataComponents.DYED_COLOR, new DyedItemColor(0xFFFFFF)).rgb()).brighter().getRGB());
                } else {
                    return text;
                }
            });

            if (itemStack.is(AntiqueItems.MYRIAD_TOOL)) {
                list.removeIf(component -> component.getContents() instanceof TranslatableContents translatable && (translatable.getKey().contains("dyed") || translatable.getKey().contains("item.color")));

                MyriadToolComponent component = itemStack.getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, MyriadToolComponent.DEFAULT_NO_CLOTH);

                Component line = Component.translatable("item.antique.myriad_tool.no_tool").withColor(0xAAAAAA);

                ItemStack toolBit = component.toolBit();

                if (!toolBit.isEmpty()) {
                    String string = toolBit.getItem().getDescriptionId();
                    string = string.substring(20);
                    string = "item.antique.myriad_tool." + string.substring(0, string.indexOf("_"));
                    line = Component.translatable(string).withColor(0xAAAAAA);
                }

                list.add(1, line);

                component.cloth().ifPresent(cloth -> ClothUtil.getCloth(cloth).ifPresent(clothKey -> {
                    int index = 2;

                    String clothName = clothKey.identifier().toLanguageKey();
                    list.add(index++, Component.literal(" - ").append(Component.translatable("item." + clothName)).withColor(new Color(ClothUtil.getDynamicClothColor(cloth, context.registries()).orElse(0xFFFFFFFF)).brighter().getRGB()));

                    for (SewnClothPattern pattern : ClothUtil.getClothPatterns(cloth)) {
                        String patternName = pattern.key().identifier().toLanguageKey();
                        Component text = Component.literal(" - ").append(Component.translatable("item." + patternName)).withColor(new Color(pattern.color().orElse(0xFFFFFFFF)).brighter().getRGB());

                        if (pattern.glowing()) {
                            text = text.copy().append(Component.literal(" - ").withColor(0xFF4ADBB8)).append(Component.translatable("item.antique.glowing").withColor(0xFF4ADBB8));
                        }

                        list.add(index++, text);
                    }
                }));
            }

            if (itemStack.is(AntiqueItems.CLOTH)) {
                int index = 2;

                for (SewnClothPattern pattern : ClothUtil.getClothPatterns(itemStack)) {
                    String patternName = pattern.key().identifier().toLanguageKey();
                    Component text = Component.literal(" - ").append(Component.translatable("item." + patternName)).withColor(new Color(pattern.color().orElse(0xFFFFFFFF)).brighter().getRGB());

                    if (pattern.glowing()) {
                        text = text.copy().append(Component.literal(" - ").withColor(0xFF4ADBB8)).append(Component.translatable("item.antique.glowing").withColor(0xFF4ADBB8));
                    }

                    list.add(index++, text);
                }
            }

            if (itemStack.is(AntiqueItems.CLOTH_PATTERN)) {
                if (itemStack.getOrDefault(AntiqueDataComponentTypes.CLOTH_PATTERN_GLOWING, false)) {
                    list.add(2, Component.translatable("item.antique.glowing").withColor(0xFF4ADBB8));
                }
            }

            if (itemStack.is(Items.BOW) || itemStack.is(Items.CROSSBOW)) {
                list.removeIf(component -> component.getContents() instanceof TranslatableContents translatable && translatable.getKey().contains("item.color"));
            }
        });
    }
}
