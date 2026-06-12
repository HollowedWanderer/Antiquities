package net.hollowed.antique.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;

import net.hollowed.antique.Antiquities;
import net.hollowed.antique.index.AntiqueDataComponentTypes;
import net.hollowed.antique.index.AntiqueItems;
import net.hollowed.antique.index.AntiqueRecipeSerializer;
import net.hollowed.antique.index.AntiqueRegistries;
import net.hollowed.antique.items.components.MyriadToolComponent;
import net.hollowed.antique.util.resources.ClothPatternData;
import net.hollowed.combatamenities.util.items.CAComponents;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClothPatternOnToolRecipe implements CraftingRecipe {
	final String group;
	final CraftingBookCategory category;
	final List<Ingredient> ingredients;
	@Nullable
	private PlacementInfo ingredientPlacement;

	public ClothPatternOnToolRecipe(String group, CraftingBookCategory category, List<Ingredient> ingredients) {
		this.group = group;
		this.category = category;
		this.ingredients = ingredients;
	}

	@Override
	public @NotNull RecipeSerializer<@NotNull ClothPatternOnToolRecipe> getSerializer() {
		return AntiqueRecipeSerializer.CLOTH_PATTERN;
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

	@Override
	public @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull CraftingInput input) {
		return defaultCraftingReminder(input);
	}

	static NonNullList<ItemStack> defaultCraftingReminder(CraftingInput input) {
		NonNullList<ItemStack> defaultedList = NonNullList.withSize(input.size(), ItemStack.EMPTY);

		for (int i = 0; i < defaultedList.size(); i++) {
			ItemStack item = input.getItem(i);
			if (item.is(AntiqueItems.CLOTH_PATTERN)) {
				defaultedList.set(i, item.copy());
			}
		}

		return defaultedList;
	}

	@SuppressWarnings("all")
	public boolean matches(CraftingInput craftingRecipeInput, @NotNull Level world) {
		if (craftingRecipeInput.ingredientCount() != this.ingredients.size()) {
			return false;
		} else {
			if (world instanceof ServerLevel) {
				for (ItemStack stack : craftingRecipeInput.items()) {
					boolean patternable = stack.getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, MyriadToolComponent.DEFAULT_NO_CLOTH)
							.cloth()
							.map(cloth ->
									world.registryAccess()
											.lookupOrThrow(AntiqueRegistries.CLOTHS)
											.get(cloth.cloth())
											.map(skin -> skin.value().patternable())
											.orElseGet(() -> {
												Antiquities.LOGGER.error("Nonexistent cloth type {}", cloth.cloth().identifier());
												return false;
											})
							)
							.orElse(false);

					if (stack.is(AntiqueItems.MYRIAD_TOOL) && !patternable) {
						return false;
					}
				}
			}
			return craftingRecipeInput.size() == 1 && this.ingredients.size() == 1
					? this.ingredients.getFirst().test(craftingRecipeInput.getItem(0))
					: craftingRecipeInput.stackedContents().canCraft(this, null);
		}
	}

	public @NotNull ItemStack assemble(CraftingInput craftingRecipeInput, HolderLookup.@NotNull Provider wrapperLookup) {
		ItemStack myriadTool = null;
		ItemStack clothPattern = null;

		for (ItemStack stack : craftingRecipeInput.items()) {
			if (stack.is(AntiqueItems.MYRIAD_TOOL)) {
				myriadTool = stack;
			} else if (stack.is(AntiqueItems.CLOTH_PATTERN)) {
				clothPattern = stack;
			}
		}

		if (myriadTool != null && clothPattern != null) {
			ItemStack result = myriadTool.copy();

			MyriadToolComponent component = result.getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, MyriadToolComponent.DEFAULT_NO_CLOTH);
			Optional<ResourceKey<ClothPatternData>> pattern = Optional.ofNullable(clothPattern.get(AntiqueDataComponentTypes.CLOTH_PATTERN_TYPE));

			result.set(AntiqueDataComponentTypes.MYRIAD_TOOL, component.withCloth(cloth -> cloth.withPattern(pattern)));
			result.set(CAComponents.BOOLEAN_PROPERTY, clothPattern.getOrDefault(CAComponents.BOOLEAN_PROPERTY, false));

			return result;
		}

		return ItemStack.EMPTY;
	}

	public static class Serializer implements RecipeSerializer<@NotNull ClothPatternOnToolRecipe> {
		private static final MapCodec<ClothPatternOnToolRecipe> CODEC = RecordCodecBuilder.mapCodec(
				instance -> instance.group(
								Codec.STRING.optionalFieldOf("group", "").forGetter(recipe -> recipe.group),
								CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(recipe -> recipe.category),
								Ingredient.CODEC.listOf(1, 9).fieldOf("ingredients").forGetter(recipe -> recipe.ingredients)
						)
						.apply(instance, ClothPatternOnToolRecipe::new)
		);
		public static final StreamCodec<RegistryFriendlyByteBuf, ClothPatternOnToolRecipe> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.STRING_UTF8,
				recipe -> recipe.group,
				CraftingBookCategory.STREAM_CODEC,
				recipe -> recipe.category,
				Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()),
				recipe -> recipe.ingredients,
				ClothPatternOnToolRecipe::new
		);

		@Override
		public @NotNull MapCodec<ClothPatternOnToolRecipe> codec() {
			return CODEC;
		}

		@Override
		public @NotNull StreamCodec<RegistryFriendlyByteBuf, ClothPatternOnToolRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}