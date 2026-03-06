package net.hollowed.antique;

import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.loader.api.FabricLoader;
import net.hollowed.antique.config.AntiquitiesConfig;
import net.hollowed.antique.index.*;
import net.hollowed.antique.items.MyriadToolItem;
import net.hollowed.antique.items.components.MyriadToolComponent;
import net.hollowed.antique.networking.*;
import net.hollowed.antique.util.resources.*;
import net.hollowed.antique.util.delay.TickDelayScheduler;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.enchantment.Enchantable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Predicate;

public class Antiquities implements ModInitializer {

	public static final String MOD_ID = "antique";

	public static Identifier id(String string) {
		return Identifier.fromNamespaceAndPath(MOD_ID, string);
	}

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@SuppressWarnings("all")
	@Override
	public void onInitialize() {
		
		/*
			Initializers
		 */

		AntiqueBlocks.initialize();
		AntiqueScreenHandlerType.initialize();
		AntiqueStats.initialize();
		AntiqueEnchantments.initialize();
		AntiqueBlockEntities.initialize();
		AntiqueLootTableModifiers.initialize();
		AntiqueEntities.initialize();
		AntiqueDataComponentTypes.initialize();
		AntiqueParticles.initialize();
		AntiqueItems.initialize();
		AntiqueSounds.initialize();
		AntiqueEffects.initialize();
		AntiqueDispenserBehaviors.initialize();
		AntiqueRecipeSerializer.initialize();
		AntiquePlacedFeatures.initialize();
		AntiqueFeatures.initialize();
		AntiqueTrackedData.initialize();

		MidnightConfig.init(MOD_ID, AntiquitiesConfig.class);

		ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloader(id("staff_transforms"), new MyriadStaffTransformResourceReloadListener());
		ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloader(id("pedestal_transforms"), new PedestalDisplayListener());
		ResourceLoader.get(PackType.SERVER_DATA).registerReloader(id("cloth_skins"), new ClothSkinListener());
		ResourceLoader.get(PackType.SERVER_DATA).registerReloader(id("cloth_overlays"), new ClothOverlayListener());

		/*
			Packets
		 */

		PayloadTypeRegistry.playS2C().register(PedestalPacketPayload.ID, PedestalPacketPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(PaleWardenTickPacketPayload.ID, PaleWardenTickPacketPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SatchelPacketPayload.ID, SatchelPacketPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(WallJumpPacketPayload.ID, WallJumpPacketPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(WallJumpParticlePacketPayload.ID, WallJumpParticlePacketPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(CrawlPacketPayload.ID, CrawlPacketPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(DyePacketPayload.ID, DyePacketPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(IllusionerParticlePacketPayload.ID, IllusionerParticlePacketPayload.CODEC);

		SatchelPacketReceiver.registerServerPacket();
		PaleWardenTickPacketReceiver.registerServerPacket();
		WallJumpPacketReceiver.registerServerPacket();
		CrawlPacketReceiver.registerServerPacket();
		DyePacketReceiver.registerServerPacket();

		/*
			Tick Events
		 */

		ServerTickEvents.END_SERVER_TICK.register(server -> TickDelayScheduler.tick());

		/*
			Component Modification
		 */

		DefaultItemComponentEvents.MODIFY.register(ctx -> ctx.modify(
				Predicate.isEqual(AntiqueItems.MIRAGE_SILK),
				(builder, item) -> builder.set(DataComponents.ITEM_NAME, Component.translatable(item.getDescriptionId()).withColor(0xc57dbe))
		));
		DefaultItemComponentEvents.MODIFY.register(ctx -> ctx.modify(
				Predicate.isEqual(AntiqueItems.BAG_OF_TRICKS),
				(builder, item) -> builder.set(DataComponents.ITEM_NAME, Component.translatable(item.getDescriptionId()).withColor(0xc57dbe))
		));

		DefaultItemComponentEvents.MODIFY.register(ctx -> ctx.modify(
				Predicate.isEqual(Items.DIAMOND),
				(builder, item) -> builder.set(DataComponents.ITEM_NAME, Component.translatable(item.getDescriptionId()).withColor(0xc57dbe))
		));

		DefaultItemComponentEvents.MODIFY.register(ctx -> ctx.modify(
				List.of(
						Items.BUNDLE, Items.WHITE_BUNDLE, Items.LIGHT_GRAY_BUNDLE, Items.GRAY_BUNDLE, Items.BLACK_BUNDLE, Items.BROWN_BUNDLE, Items.RED_BUNDLE,
						Items.ORANGE_BUNDLE, Items.YELLOW_BUNDLE, Items.LIME_BUNDLE, Items.GREEN_BUNDLE, Items.CYAN_BUNDLE, Items.LIGHT_BLUE_BUNDLE, Items.BLUE_BUNDLE,
						Items.PURPLE_BUNDLE, Items.MAGENTA_BUNDLE, Items.PINK_BUNDLE
						),
				(builder, item) -> builder.set(DataComponents.ENCHANTABLE, new Enchantable(10))
		));

		/*
			Resource Pack
		 */

		FabricLoader.getInstance().getModContainer(MOD_ID).ifPresent((container) ->
				ResourceManagerHelper.registerBuiltinResourcePack(Identifier.fromNamespaceAndPath(MOD_ID, "antique"), container, Component.translatable("resourcePack.hmi.name"), ResourcePackActivationType.NORMAL));

		/*
			Item Group
		 */

		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ANTIQUITIES_ITEMS_GROUP_KEY, ANTIQUITIES_ITEMS_GROUP);
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ANTIQUITIES_BLOCKS_GROUP_KEY, ANTIQUITIES_BLOCKS_GROUP);
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ANTIQUITIES_CLOTHS_GROUP_KEY, ANTIQUITIES_CLOTHS_GROUP);
		addItems();
		addClothItems();

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(itemGroup -> {
			itemGroup.addAfter(Items.WITCH_SPAWN_EGG, AntiqueItems.ILLUSIONER_SPAWN_EGG);
		});
	}

	public static final ResourceKey<CreativeModeTab> ANTIQUITIES_ITEMS_GROUP_KEY = ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), Identifier.fromNamespaceAndPath(MOD_ID, "antiquities_items_group"));
	public static final CreativeModeTab ANTIQUITIES_ITEMS_GROUP = FabricItemGroup.builder()
			.icon(() -> new ItemStack(AntiqueItems.FUR_BOOTS))
			.title(Component.translatable("itemGroup.antique.antiquities_items").withColor(0xFFAA2F54))
			.build();

	public static final ResourceKey<CreativeModeTab> ANTIQUITIES_BLOCKS_GROUP_KEY = ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), Identifier.fromNamespaceAndPath(MOD_ID, "antiquities_blocks_group"));
	public static final CreativeModeTab ANTIQUITIES_BLOCKS_GROUP = FabricItemGroup.builder()
			.icon(() -> new ItemStack(AntiqueBlocks.HOLLOW_CORE))
			.title(Component.translatable("itemGroup.antique.antiquities_blocks").withColor(0xFFAA2F54))
			.build();

	public static final ResourceKey<CreativeModeTab> ANTIQUITIES_CLOTHS_GROUP_KEY = ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), Identifier.fromNamespaceAndPath(MOD_ID, "antiquities_cloths_group"));
	public static final CreativeModeTab ANTIQUITIES_CLOTHS_GROUP = FabricItemGroup.builder()
			.icon(() -> new ItemStack(AntiqueItems.CLOTH))
			.title(Component.translatable("itemGroup.antique.antiquities_cloths").withColor(0xFFAA2F54))
			.build();

	private void addItems() {
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.OP_BLOCKS).register(itemGroup -> itemGroup.accept(AntiqueItems.CRYOSCYTHE));

		ItemGroupEvents.modifyEntriesEvent(ANTIQUITIES_BLOCKS_GROUP_KEY).register(itemGroup -> {
			itemGroup.accept(AntiqueBlocks.MYRIAD_ORE);
			itemGroup.accept(AntiqueBlocks.DEEPSLATE_MYRIAD_ORE);
			itemGroup.accept(AntiqueBlocks.MYRIAD_CLUSTER);
			itemGroup.accept(AntiqueBlocks.DEEPSLATE_MYRIAD_CLUSTER);
			itemGroup.accept(AntiqueBlocks.RAW_MYRIAD_BLOCK);
			itemGroup.accept(AntiqueBlocks.MYRIAD_BLOCK);
			itemGroup.accept(AntiqueBlocks.EXPOSED_MYRIAD_BLOCK);
			itemGroup.accept(AntiqueBlocks.WEATHERED_MYRIAD_BLOCK);
			itemGroup.accept(AntiqueBlocks.TARNISHED_MYRIAD_BLOCK);
			itemGroup.accept(AntiqueBlocks.COATED_MYRIAD_BLOCK);
			itemGroup.accept(AntiqueBlocks.COATED_EXPOSED_MYRIAD_BLOCK);
			itemGroup.accept(AntiqueBlocks.COATED_WEATHERED_MYRIAD_BLOCK);
			itemGroup.accept(AntiqueBlocks.COATED_TARNISHED_MYRIAD_BLOCK);
			itemGroup.accept(AntiqueBlocks.HOLLOW_CORE);
			itemGroup.accept(AntiqueBlocks.PEDESTAL);
			itemGroup.accept(AntiqueBlocks.DYE_TABLE);
			itemGroup.accept(AntiqueBlocks.JAR);
			itemGroup.accept(AntiqueBlocks.IVY);
		});

		ItemGroupEvents.modifyEntriesEvent(ANTIQUITIES_ITEMS_GROUP_KEY).register(itemGroup -> {
			ItemStack myriadTool = AntiqueItems.MYRIAD_TOOL.getDefaultInstance();
			myriadTool.set(AntiqueDataComponentTypes.MYRIAD_TOOL, new MyriadToolComponent(
					ItemStack.EMPTY,
					"antique:cloth",
					"",
					0xD43B69,
					0xFFFFFF
			));
			itemGroup.accept(myriadTool);

			ItemStack myriadMattock = AntiqueItems.MYRIAD_TOOL.getDefaultInstance();
			myriadMattock.set(AntiqueDataComponentTypes.MYRIAD_TOOL, new MyriadToolComponent(
					ItemStack.EMPTY,
					"antique:cloth",
					"",
					0xD43B69,
					0xFFFFFF
			));
			MyriadToolItem.setStoredStack(myriadMattock, AntiqueItems.MYRIAD_PICK_HEAD.getDefaultInstance());
			itemGroup.accept(myriadMattock);

			ItemStack myriadAxe = AntiqueItems.MYRIAD_TOOL.getDefaultInstance();
			myriadAxe.set(AntiqueDataComponentTypes.MYRIAD_TOOL, new MyriadToolComponent(
					ItemStack.EMPTY,
					"antique:cloth",
					"",
					0xD43B69,
					0xFFFFFF
			));
			MyriadToolItem.setStoredStack(myriadAxe, AntiqueItems.MYRIAD_AXE_HEAD.getDefaultInstance());
			itemGroup.accept(myriadAxe);

			itemGroup.accept(getMyriadShovelStack());

			ItemStack myriadCleaver = AntiqueItems.MYRIAD_TOOL.getDefaultInstance();
			myriadCleaver.set(AntiqueDataComponentTypes.MYRIAD_TOOL, new MyriadToolComponent(
					ItemStack.EMPTY,
					"antique:cloth",
					"",
					0xD43B69,
					0xFFFFFF
			));
			MyriadToolItem.setStoredStack(myriadCleaver, AntiqueItems.MYRIAD_CLEAVER_BLADE.getDefaultInstance());
			itemGroup.accept(myriadCleaver);

			itemGroup.accept(AntiqueItems.MYRIAD_PICK_HEAD);
			itemGroup.accept(AntiqueItems.MYRIAD_AXE_HEAD);
			itemGroup.accept(AntiqueItems.MYRIAD_SHOVEL_HEAD);
			itemGroup.accept(AntiqueItems.MYRIAD_CLEAVER_BLADE);
			itemGroup.accept(AntiqueItems.RAW_MYRIAD);
			itemGroup.accept(AntiqueItems.MYRIAD_INGOT);
			itemGroup.accept(AntiqueItems.MIRAGE_SILK);
			itemGroup.accept(AntiqueItems.BAG_OF_TRICKS);
			itemGroup.accept(AntiqueItems.SMOKE_BOMB);
			itemGroup.accept(AntiqueItems.MYRIAD_PAULDRONS);
			itemGroup.accept(AntiqueItems.SATCHEL);
			itemGroup.accept(AntiqueItems.FUR_BOOTS);
			itemGroup.accept(AntiqueItems.SCEPTER);
//			itemGroup.accept(AntiqueItems.COPPER_GLACE);
//			itemGroup.accept(AntiqueItems.QUARRY_GLACE);
//			itemGroup.accept(AntiqueItems.PROSPECTOR);
			itemGroup.accept(AntiqueItems.WARHORN);
		});
	}

	public static void addClothItems() {
		ItemGroupEvents.modifyEntriesEvent(ANTIQUITIES_CLOTHS_GROUP_KEY).register(itemGroup -> {
			for (ClothSkinData.ClothSubData data : ClothSkinListener.getTransforms()) {
				ItemStack stack = AntiqueItems.CLOTH.getDefaultInstance();
				stack.set(DataComponents.ITEM_NAME, Component.translatable("item." + data.model().toString().replace(":", ".")));
				if (!data.dyeable()) stack.remove(DataComponents.DYED_COLOR);
				if (!itemGroup.getDisplayStacks().contains(stack)) {
					itemGroup.accept(stack);
				}
			}

			for (Identifier data : ClothOverlayListener.getTransforms()) {
				ItemStack stack = AntiqueItems.CLOTH_PATTERN.getDefaultInstance();
				stack.set(DataComponents.ITEM_NAME, Component.translatable("item." + data.toString().replace(":", ".") + "_cloth_pattern"));
				stack.set(DataComponents.DYED_COLOR, new DyedItemColor(0xFFFFFF));
				if (!itemGroup.getDisplayStacks().contains(stack)) {
					itemGroup.accept(stack);
				}
			}
		});
	}

	public static ItemStack getMyriadShovelStack() {
		ItemStack myriadShovel = AntiqueItems.MYRIAD_TOOL.getDefaultInstance();
		myriadShovel.set(AntiqueDataComponentTypes.MYRIAD_TOOL, new MyriadToolComponent(
				ItemStack.EMPTY,
				"antique:cloth",
				"",
				0xD43B69,
				0xFFFFFF
		));
		MyriadToolItem.setStoredStack(myriadShovel, AntiqueItems.MYRIAD_SHOVEL_HEAD.getDefaultInstance());
		return myriadShovel;
	}

	public static MyriadToolComponent getDefaultMyriadTool() {
		return new MyriadToolComponent(ItemStack.EMPTY, "", "", 0xffffff, 0xffffff);
	}
}