package net.hollowed.antique.index;

import net.hollowed.antique.Antiquities;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

@SuppressWarnings("unused")
public interface AntiqueEnchantments {
    ResourceKey<Enchantment> PROJECTING = of("projecting");

    ResourceKey<Enchantment> JUMBLING = of("jumbling");

    ResourceKey<Enchantment> CURSE_OF_VOIDING = of("curse_of_voiding");

    ResourceKey<Enchantment> KINEMATIC = of("kinematic");

    ResourceKey<Enchantment> IMPETUS = of("impetus");

    private static ResourceKey<Enchantment> of(String name) {
      return ResourceKey.create(Registries.ENCHANTMENT, Antiquities.id(name));
    }

    static void initialize() {}
}