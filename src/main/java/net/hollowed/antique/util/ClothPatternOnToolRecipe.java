package net.hollowed.antique.util;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.hollowed.antique.config.AntiquitiesConfig;
import net.hollowed.antique.index.AntiqueDataComponentTypes;
import net.hollowed.antique.index.AntiqueItems;
import net.hollowed.antique.index.AntiqueRecipeSerializer;
import net.hollowed.antique.items.components.MyriadToolComponent;
import net.hollowed.antique.util.resources.SewnClothPattern;
import net.hollowed.combatamenities.util.items.CAComponents;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class ClothPatternOnToolRecipe extends CustomRecipe {

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

	public static final RecipeSerializer<ClothPatternOnToolRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

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
							.flatMap(cloth ->
									ClothUtil.getClothData(stack, world.registryAccess())
											.map(skin -> skin.value().patternable() && ClothUtil.getClothPatterns(cloth).size() < AntiquitiesConfig.MAX_CLOTH_PATTERNS)
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

	@Override
	public @NonNull ItemStack assemble(CraftingInput input) {
		ItemStack myriadTool = null;
		ItemStack clothPattern = null;

		for (ItemStack stack : input.items()) {
			if (stack.is(AntiqueItems.MYRIAD_TOOL)) {
				myriadTool = stack;
			} else if (stack.is(AntiqueItems.CLOTH_PATTERN)) {
				clothPattern = stack;
			}
		}

		if (myriadTool != null && clothPattern != null) {
			ItemStack result = myriadTool.copy();

			MyriadToolComponent component = result.getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, MyriadToolComponent.DEFAULT_NO_CLOTH);
			ItemStack finalClothPattern = clothPattern;

			result.set(AntiqueDataComponentTypes.MYRIAD_TOOL, component.withCloth(cloth -> ClothUtil.sewClothPattern(cloth.copy(), new SewnClothPattern(
					ClothUtil.getClothPattern(finalClothPattern).orElseThrow(),
					ClothUtil.getClothPatternColor(finalClothPattern),
					ClothUtil.getClothPatternGlowing(finalClothPattern)
			))));
			result.set(CAComponents.BOOLEAN_PROPERTY, clothPattern.getOrDefault(CAComponents.BOOLEAN_PROPERTY, false));

			return result;
		}

		return ItemStack.EMPTY;
	}
}