package net.hollowed.antique;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.hollowed.antique.blocks.ModBlocks;
import net.hollowed.antique.blocks.entities.ModBlockEntities;
import net.hollowed.antique.component.ModComponents;
import net.hollowed.antique.effect.BounceEffect;
import net.hollowed.antique.enchantments.ModEnchantments;
import net.hollowed.antique.entities.ModEntities;
import net.hollowed.antique.items.ModItems;
import net.hollowed.antique.networking.*;
import net.hollowed.antique.particles.ModParticles;
import net.hollowed.antique.util.FreezeFrameManager;
import net.hollowed.antique.util.ModLootTableModifiers;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.ToolComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Predicate;

import static net.minecraft.item.Item.BASE_ATTACK_DAMAGE_MODIFIER_ID;
import static net.minecraft.item.Item.BASE_ATTACK_SPEED_MODIFIER_ID;

public class Antiquities implements ModInitializer {
	public static final String MOD_ID = "antique";

	public static Identifier id(String string) {
		return Identifier.of(MOD_ID, string);
	}

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModEnchantments.initialize();
		ModBlocks.initialize();
		ModBlockEntities.initialize();
		ModLootTableModifiers.modifyLootTables();
		ModEntities.initialize();
		ModComponents.initialize();

		ModParticles.initialize();

		PayloadTypeRegistry.playS2C().register(PedestalPacketPayload.ID, PedestalPacketPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(PaleWardenTickPacketPayload.ID, PaleWardenTickPacketPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SatchelPacketPayload.ID, SatchelPacketPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(MyriadShovelSpawnPacketPayload.ID, MyriadShovelSpawnPacketPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(WallJumpPacketPayload.ID, WallJumpPacketPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(WallJumpParticlePacketPayload.ID, WallJumpParticlePacketPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(CrawlPacketPayload.ID, CrawlPacketPayload.CODEC);

		SatchelPacketReceiver.registerServerPacket();
		PaleWardenTickPacketReceiver.registerServerPacket();
		MyriadShovelSpawnPacketReceiver.registerServerPacket();
		WallJumpPacketReceiver.registerServerPacket();
		CrawlPacketReceiver.registerServerPacket();

		ModItems.initialize();

		ModKeyBindings.initialize();

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			FreezeFrameManager.tick();
		});

		// Register the group.
		Registry.register(Registries.ITEM_GROUP, ANTIQUITIES_GROUP_KEY, ANTIQUITIES_GROUP);

		addItems();

		ServerTickEvents.END_WORLD_TICK.register(world -> {
			for (ServerPlayerEntity player : world.getPlayers()) {
				if (player.isSneaking()) {
					player.setSwimming(true);
				}
			}
		});

		DefaultItemComponentEvents.MODIFY.register(ctx -> ctx.modify(
				Predicate.isEqual(ModItems.REVERENCE),
				(builder, item) -> builder.add(DataComponentTypes.ITEM_NAME, Text.translatable(item.getTranslationKey()).withColor(0xff5a00))
		));
	}

	public static final RegistryEntry<StatusEffect> VOLATILE_BOUNCE_EFFECT;
	public static final RegistryEntry<StatusEffect> BOUNCE_EFFECT;

	static {
		BOUNCE_EFFECT = registerEffect("bouncy", new BounceEffect().addAttributeModifier(EntityAttributes.STEP_HEIGHT, Identifier.ofVanilla("effect.step_height"), 1, EntityAttributeModifier.Operation.ADD_VALUE));
		VOLATILE_BOUNCE_EFFECT = registerEffect("volatile_bouncy", new BounceEffect().addAttributeModifier(EntityAttributes.STEP_HEIGHT, Identifier.ofVanilla("effect.step_height"), 1, EntityAttributeModifier.Operation.ADD_VALUE));
	}

	private static RegistryEntry<StatusEffect> registerEffect(String id, StatusEffect statusEffect) {
		return Registry.registerReference(Registries.STATUS_EFFECT, Identifier.of(MOD_ID, id), statusEffect);
	}

	public static final RegistryKey<ItemGroup> ANTIQUITIES_GROUP_KEY = RegistryKey.of(Registries.ITEM_GROUP.getKey(), Identifier.of(MOD_ID, "antiquities_group"));
	public static final ItemGroup ANTIQUITIES_GROUP = FabricItemGroup.builder()
			.icon(() -> new ItemStack(ModItems.FUR_BOOTS))
			.displayName(Text.translatable("itemGroup.antique.antiquities").withColor(0xBE7E4A))
			.build();

	private void addItems() {
		// Register items to the custom item group.
		ItemGroupEvents.modifyEntriesEvent(ANTIQUITIES_GROUP_KEY).register(itemGroup -> {
			itemGroup.add(ModItems.NETHERITE_PAULDRONS);
			itemGroup.add(ModItems.SATCHEL);
			itemGroup.add(ModItems.FUR_BOOTS);
			itemGroup.add(ModItems.COPPER_HANDLE);
			itemGroup.add(ModBlocks.HOLLOW_CORE);
			itemGroup.add(ModItems.WEIGHTLESS_SCEPTER);
			itemGroup.add(ModBlocks.PEDESTAL);
			itemGroup.add(ModItems.PALE_WARDENS_GREATSWORD);
			itemGroup.add(ModItems.PALE_WARDEN_STATUE);
			itemGroup.add(ModItems.REVERENCE);
			itemGroup.add(ModItems.DORMANT_REVERENCE);
			itemGroup.add(ModItems.MYRIAD_TOOL);

			ItemStack myriadMattock = ModItems.MYRIAD_TOOL.getDefaultStack();
			myriadMattock.set(ModComponents.MYRIAD_STACK, ModItems.MYRIAD_PICK_HEAD.getDefaultStack());
			myriadMattock.set(ModComponents.INTEGER_PROPERTY, 1);
			myriadMattock.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.builder()
					.add(EntityAttributes.ATTACK_DAMAGE, new EntityAttributeModifier(BASE_ATTACK_DAMAGE_MODIFIER_ID, 5.0, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
					.add(EntityAttributes.ATTACK_SPEED, new EntityAttributeModifier(BASE_ATTACK_SPEED_MODIFIER_ID, -2.4, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
					.add(EntityAttributes.ENTITY_INTERACTION_RANGE, new EntityAttributeModifier(Identifier.ofVanilla("base_attack_range"), 0.75, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
					.build());
			myriadMattock.set(DataComponentTypes.TOOL, new ToolComponent(
					List.of(
							ToolComponent.Rule.ofNeverDropping(ModItems.registryEntryLookup.getOrThrow(BlockTags.INCORRECT_FOR_DIAMOND_TOOL)),
							ToolComponent.Rule.ofAlwaysDropping(ModItems.registryEntryLookup.getOrThrow(TagKey.of(RegistryKeys.BLOCK, Identifier.of(Antiquities.MOD_ID, "mineable/mattock"))), 12)
					),
					1.0F,
					1
			));
			itemGroup.add(myriadMattock);

			ItemStack myriadAxe = ModItems.MYRIAD_TOOL.getDefaultStack();
			myriadAxe.set(ModComponents.MYRIAD_STACK, ModItems.MYRIAD_AXE_HEAD.getDefaultStack());
			myriadAxe.set(ModComponents.INTEGER_PROPERTY, 2);
			myriadAxe.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.builder()
					.add(EntityAttributes.ATTACK_DAMAGE, new EntityAttributeModifier(BASE_ATTACK_DAMAGE_MODIFIER_ID, 9.0, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
					.add(EntityAttributes.ATTACK_SPEED, new EntityAttributeModifier(BASE_ATTACK_SPEED_MODIFIER_ID, -3, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
					.add(EntityAttributes.ENTITY_INTERACTION_RANGE, new EntityAttributeModifier(Identifier.ofVanilla("base_attack_range"), 0.75, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
					.build());
			myriadAxe.set(DataComponentTypes.TOOL, new ToolComponent(
					List.of(
							ToolComponent.Rule.ofNeverDropping(ModItems.registryEntryLookup.getOrThrow(BlockTags.INCORRECT_FOR_DIAMOND_TOOL)),
							ToolComponent.Rule.ofAlwaysDropping(ModItems.registryEntryLookup.getOrThrow(BlockTags.AXE_MINEABLE), 12)
					),
					1.0F,
					1
			));
			itemGroup.add(myriadAxe);

			ItemStack myriadShovel = ModItems.MYRIAD_TOOL.getDefaultStack();
			myriadShovel.set(ModComponents.MYRIAD_STACK, ModItems.MYRIAD_SHOVEL_HEAD.getDefaultStack());
			myriadShovel.set(ModComponents.INTEGER_PROPERTY, 3);
			myriadShovel.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.builder()
					.add(EntityAttributes.ATTACK_DAMAGE, new EntityAttributeModifier(BASE_ATTACK_DAMAGE_MODIFIER_ID, 5.5, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
					.add(EntityAttributes.ATTACK_SPEED, new EntityAttributeModifier(BASE_ATTACK_SPEED_MODIFIER_ID, -2.8, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
					.add(EntityAttributes.ENTITY_INTERACTION_RANGE, new EntityAttributeModifier(Identifier.ofVanilla("base_attack_range"), 0.75, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
					.build());
			myriadShovel.set(DataComponentTypes.TOOL, new ToolComponent(
					List.of(
							ToolComponent.Rule.ofNeverDropping(ModItems.registryEntryLookup.getOrThrow(BlockTags.INCORRECT_FOR_DIAMOND_TOOL)),
							ToolComponent.Rule.ofAlwaysDropping(ModItems.registryEntryLookup.getOrThrow(BlockTags.SHOVEL_MINEABLE), 12)
					),
					1.0F,
					1
			));
			itemGroup.add(myriadShovel);

			itemGroup.add(ModItems.MYRIAD_PICK_HEAD);
			itemGroup.add(ModItems.MYRIAD_AXE_HEAD);
			itemGroup.add(ModItems.MYRIAD_SHOVEL_HEAD);

			itemGroup.add(ModItems.EXPLOSIVE_SPEAR);

			itemGroup.add(ModItems.IRREVERENT);

			itemGroup.add(ModBlocks.BLACK_SAND);
			itemGroup.add(ModBlocks.GILDED_BLACK_SAND);
			itemGroup.add(ModBlocks.OMINOUS_PEDESTAL);
		});
	}

	public static ItemStack getMyriadShovelStack() {
		ItemStack myriadShovel = ModItems.MYRIAD_TOOL.getDefaultStack();
		myriadShovel.set(ModComponents.MYRIAD_STACK, ModItems.MYRIAD_SHOVEL_HEAD.getDefaultStack());
		myriadShovel.set(ModComponents.INTEGER_PROPERTY, 3);
		myriadShovel.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.builder()
				.add(EntityAttributes.ATTACK_DAMAGE, new EntityAttributeModifier(BASE_ATTACK_DAMAGE_MODIFIER_ID, 5.5, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
				.add(EntityAttributes.ATTACK_SPEED, new EntityAttributeModifier(BASE_ATTACK_SPEED_MODIFIER_ID, -2.8, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
				.add(EntityAttributes.ENTITY_INTERACTION_RANGE, new EntityAttributeModifier(Identifier.ofVanilla("base_attack_range"), 0.75, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
				.build());
		myriadShovel.set(DataComponentTypes.TOOL, new ToolComponent(
				List.of(
						ToolComponent.Rule.ofNeverDropping(ModItems.registryEntryLookup.getOrThrow(BlockTags.INCORRECT_FOR_DIAMOND_TOOL)),
						ToolComponent.Rule.ofAlwaysDropping(ModItems.registryEntryLookup.getOrThrow(BlockTags.SHOVEL_MINEABLE), 12)
				),
				1.0F,
				1
		));
		return myriadShovel;
	}
}