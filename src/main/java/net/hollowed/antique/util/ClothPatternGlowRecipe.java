package net.hollowed.antique.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.hollowed.antique.index.AntiqueItems;
import net.hollowed.combatamenities.util.items.CAComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class ClothPatternGlowRecipe extends CustomRecipe {

	private static final MapCodec<ClothPatternGlowRecipe> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
							Codec.STRING.optionalFieldOf("group", "").forGetter(recipe -> recipe.group),
							CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(recipe -> recipe.category),
							Ingredient.CODEC.listOf(1, 9).fieldOf("ingredients").forGetter(recipe -> recipe.ingredients)
					)
					.apply(instance, ClothPatternGlowRecipe::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, ClothPatternGlowRecipe> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8,
			recipe -> recipe.group,
			CraftingBookCategory.STREAM_CODEC,
			recipe -> recipe.category,
			Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()),
			recipe -> recipe.ingredients,
			ClothPatternGlowRecipe::new
	);

	public static final RecipeSerializer<ClothPatternGlowRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

	final String group;
	final CraftingBookCategory category;
	final List<Ingredient> ingredients;
	@Nullable
	private PlacementInfo ingredientPlacement;

	public ClothPatternGlowRecipe(String group, CraftingBookCategory category, List<Ingredient> ingredients) {
		this.group = group;
		this.category = category;
		this.ingredients = ingredients;
	}

	@Override
	public @NotNull RecipeSerializer<@NotNull ClothPatternGlowRecipe> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public @NotNull String group() {
		return this.group;
	}

	@Override
	public @NotNull CraftingBookCategory category() {
		return this.category;
	}

	@Override
	public @NotNull PlacementInfo placementInfo() {
		if (this.ingredientPlacement == null) {
			this.ingredientPlacement = PlacementInfo.create(this.ingredients);
		}

		return this.ingredientPlacement;
	}

	@SuppressWarnings("all")
	public boolean matches(CraftingInput craftingRecipeInput, @NotNull Level world) {
		if (craftingRecipeInput.ingredientCount() != this.ingredients.size()) {
			return false;
		} else {
			return craftingRecipeInput.size() == 1 && this.ingredients.size() == 1
					? this.ingredients.getFirst().test(craftingRecipeInput.getItem(0))
					: craftingRecipeInput.stackedContents().canCraft(this, null);
		}
	}

	@Override
	public @NonNull ItemStack assemble(CraftingInput input) {
		ItemStack clothPattern = null;
		boolean glow = false;

		for (ItemStack stack : input.items()) {
			if (stack.is(AntiqueItems.CLOTH_PATTERN)) {
				clothPattern = stack;
			} else if (stack.is(Items.GLOW_INK_SAC)) {
				glow = true;
			}
		}

		if (clothPattern != null) {
			ItemStack result = clothPattern.copy();
			result.set(CAComponents.BOOLEAN_PROPERTY, glow);
			return result;
		}
		return ItemStack.EMPTY;
	}
}