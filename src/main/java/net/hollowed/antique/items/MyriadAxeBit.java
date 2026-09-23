package net.hollowed.antique.items;

import net.hollowed.antique.index.AntiqueItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockTransformers;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

public class MyriadAxeBit extends MyriadToolBitItem{

    public MyriadAxeBit(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult toolUse(Level world, Player user, InteractionHand hand) {
        user.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResult toolUseOnBlock(UseOnContext context) {
        return context.getPlayer() != null && context.getPlayer().isCrouching() ? null : super.toolUseOnBlock(context);
    }

    @Override
    public void setToolAttributes(ItemStack toolStack, Level level) {
        toolStack.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 9, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build());
        toolStack.set(DataComponents.TOOL, new Tool(
                List.of(
                        Tool.Rule.deniesDrops(AntiqueItems.registryEntryLookup.getOrThrow(BlockTags.INCORRECT_FOR_IRON_TOOL)),
                        Tool.Rule.minesAndDrops(AntiqueItems.registryEntryLookup.getOrThrow(BlockTags.MINEABLE_WITH_AXE), 20)
                ),
                1.0F,
                1,
                true
        ));
        toolStack.set(DataComponents.WEAPON, new Weapon(0, 2));
        toolStack.set(DataComponents.BLOCK_TRANSFORMER, level.registryAccess().getOrThrow(BlockTransformers.AXE));
    }
}
