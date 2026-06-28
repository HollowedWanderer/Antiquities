package net.hollowed.antique.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;

public class ItemHoldingUtil {

    @SuppressWarnings("unused")
    public static boolean isHoldingItem(LivingEntity entity, Item item) {
        return entity.getItemInHand(InteractionHand.MAIN_HAND).is(item) || entity.getItemInHand(InteractionHand.OFF_HAND).is(item);
    }

    @SuppressWarnings("unused")
    public static boolean isHoldingItem(LivingEntity entity, Identifier tagKey) {
        return entity.getItemInHand(InteractionHand.MAIN_HAND).tags().toList().contains(TagKey.create(Registries.ITEM, tagKey)) || entity.getItemInHand(InteractionHand.OFF_HAND).tags().toList().contains(TagKey.create(Registries.ITEM, tagKey));
    }
}
