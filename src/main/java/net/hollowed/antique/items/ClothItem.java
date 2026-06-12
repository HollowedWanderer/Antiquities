package net.hollowed.antique.items;

import net.hollowed.antique.index.AntiqueDataComponentTypes;
import net.hollowed.antique.util.resources.ClothSkinData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public class ClothItem extends Item {
    public ClothItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull Component getName(@NonNull ItemStack stack) {
        ResourceKey<ClothSkinData> cloth = stack.get(AntiqueDataComponentTypes.CLOTH_TYPE);

        if (cloth == null) {
            return super.getName(stack);
        }

        return Component.translatable(cloth.identifier().toLanguageKey("item"));
    }
}
