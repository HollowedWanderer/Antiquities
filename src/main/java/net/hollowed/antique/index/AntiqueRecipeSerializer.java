package net.hollowed.antique.index;

import net.hollowed.antique.util.ClothPatternGlowRecipe;
import net.hollowed.antique.util.ClothPatternOnToolRecipe;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public interface AntiqueRecipeSerializer {
    RecipeSerializer<@NotNull ClothPatternOnToolRecipe> CLOTH_PATTERN = register(
            "crafting_special_cloth_pattern", new ClothPatternOnToolRecipe.Serializer()
    );
    RecipeSerializer<@NotNull ClothPatternGlowRecipe> PATTERN_GLOW = register(
            "crafting_special_pattern_glow", new ClothPatternGlowRecipe.Serializer()
    );

    static void initialize() {}

    static <S extends RecipeSerializer<@NotNull T>, T extends Recipe<?>> S register(String id, S serializer) {
        return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, "antique:" + id, serializer);
    }
}
