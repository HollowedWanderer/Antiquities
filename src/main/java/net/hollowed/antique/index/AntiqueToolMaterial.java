package net.hollowed.antique.index;

import java.util.List;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

@SuppressWarnings("unused")
public record AntiqueToolMaterial(
	TagKey<Block> incorrectBlocksForDrops, int durability, float speed, float attackDamageBonus, int enchantmentValue, TagKey<Item> repairItems
) {

	@SuppressWarnings("unused")
	public static final AntiqueToolMaterial WOOD = new AntiqueToolMaterial(BlockTags.INCORRECT_FOR_WOODEN_TOOL, 59, 2.0F, 0.0F, 15, ItemTags.WOODEN_TOOL_MATERIALS);
	@SuppressWarnings("unused")
	public static final AntiqueToolMaterial STONE = new AntiqueToolMaterial(BlockTags.INCORRECT_FOR_STONE_TOOL, 131, 4.0F, 1.0F, 5, ItemTags.STONE_TOOL_MATERIALS);
	public static final AntiqueToolMaterial IRON = new AntiqueToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 250, 6.0F, 2.0F, 14, ItemTags.IRON_TOOL_MATERIALS);
	public static final AntiqueToolMaterial DIAMOND = new AntiqueToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1561, 8.0F, 3.0F, 10, ItemTags.DIAMOND_TOOL_MATERIALS);
	public static final AntiqueToolMaterial GOLD = new AntiqueToolMaterial(BlockTags.INCORRECT_FOR_GOLD_TOOL, 32, 12.0F, 0.0F, 22, ItemTags.GOLD_TOOL_MATERIALS);
	public static final AntiqueToolMaterial NETHERITE = new AntiqueToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2031, 9.0F, 4.0F, 15, ItemTags.NETHERITE_TOOL_MATERIALS);


	private Item.Properties applyBaseSettings(Item.Properties settings) {
		return settings.durability(this.durability).repairable(this.repairItems).enchantable(this.enchantmentValue);
	}

	public Item.Properties applyGreatswordSettings(Item.Properties settings, float attackDamage, float attackSpeed, float reach) {
		HolderGetter<Block> registryEntryLookup = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);
		return this.applyBaseSettings(settings)
				.component(
						DataComponents.TOOL,
						new Tool(
								List.of(
										Tool.Rule.minesAndDrops(HolderSet.direct(Blocks.COBWEB.defaultBlockState().typeHolder()), 15.0F),
										Tool.Rule.overrideSpeed(registryEntryLookup.getOrThrow(BlockTags.SWORD_EFFICIENT), 1.5F)
								),
								1.0F,
								2,
								false
						)
				)
				.attributes(this.createGreatswordAttributeModifiers(attackDamage, attackSpeed, reach));
	}

	private ItemAttributeModifiers createGreatswordAttributeModifiers(float attackDamage, float attackSpeed, float reach) {
		return ItemAttributeModifiers.builder()
				.add(
						Attributes.ATTACK_DAMAGE,
						new AttributeModifier(
								Item.BASE_ATTACK_DAMAGE_ID, attackDamage + this.attackDamageBonus, AttributeModifier.Operation.ADD_VALUE
						),
						EquipmentSlotGroup.MAINHAND
				)
				.add(
						Attributes.ATTACK_SPEED,
						new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, attackSpeed, AttributeModifier.Operation.ADD_VALUE),
						EquipmentSlotGroup.MAINHAND
				)
				.add(
						Attributes.ENTITY_INTERACTION_RANGE,
						new AttributeModifier(Identifier.withDefaultNamespace("base_entity_reach"), reach, AttributeModifier.Operation.ADD_VALUE),
						EquipmentSlotGroup.MAINHAND
				)
				.build();
	}
}
