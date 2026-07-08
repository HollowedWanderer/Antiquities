package net.hollowed.antique.util;

import net.hollowed.antique.index.AntiqueDataComponentTypes;
import net.hollowed.antique.items.ClothPatternItem;
import net.hollowed.antique.util.resources.ClothPatternData;
import net.hollowed.antique.util.resources.ClothSkinData;
import net.hollowed.antique.util.resources.ColorProvider;
import net.hollowed.antique.util.resources.SewnClothPattern;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClothUtil {
    private ClothUtil() {
    }

    public static Optional<ResourceKey<ClothSkinData>> getCloth(ItemStack stack) {
        return Optional.ofNullable(stack.get(AntiqueDataComponentTypes.CLOTH_TYPE));
    }

    public static ResourceKey<ClothSkinData> getClothOrDefault(ItemStack stack) {
        return stack.getOrDefault(AntiqueDataComponentTypes.CLOTH_TYPE, ClothSkinData.DEFAULT_KEY);
    }

    public static Optional<Holder.Reference<ClothSkinData>> getClothData(ItemStack stack, @Nullable HolderLookup.Provider registries) {
        return getCloth(stack).flatMap(key -> Optional.ofNullable(registries).map(r -> ClothSkinData.getHolder(key, r)));
    }

    public static ClothSkinData getClothDataOrDefault(ItemStack stack, @NotNull HolderLookup.Provider registries) {
        return ClothSkinData.get(getClothOrDefault(stack), registries);
    }

    public static Optional<Integer> getClothDyedColor(ItemStack stack) {
        return Optional.ofNullable(stack.get(DataComponents.DYED_COLOR))
                .map(DyedItemColor::rgb);
    }

    public static Optional<Integer> getStaticClothColor(ItemStack stack, @Nullable HolderLookup.Provider registries) {
        return Optional.ofNullable(stack.get(DataComponents.DYED_COLOR))
                .map(DyedItemColor::rgb)
                .or(() ->
                        getCloth(stack).flatMap(cloth ->
                                Optional.ofNullable(registries).flatMap(r ->
                                        ClothSkinData.get(cloth, r)
                                                .color()
                                                .flatMap(ColorProvider::getConstantColor)
                                )
                        )
                );
    }

    public static Optional<Integer> getDynamicClothColor(ItemStack stack, @Nullable HolderLookup.Provider registries) {
        return Optional.ofNullable(stack.get(DataComponents.DYED_COLOR))
                .map(DyedItemColor::rgb)
                .or(() ->
                        getCloth(stack).flatMap(cloth ->
                                Optional.ofNullable(registries).flatMap(r ->
                                        ClothSkinData.get(cloth, r)
                                                .color()
                                                .map(ColorProvider::getColorClient)
                                )
                        )
                );
    }

    public static ItemStack setClothColor(ItemStack stack, Optional<Integer> color) {
        color.ifPresentOrElse(
                key -> stack.set(DataComponents.DYED_COLOR, new DyedItemColor(key)),
                () -> stack.remove(DataComponents.DYED_COLOR)
        );
        return stack;
    }

    public static Optional<ResourceKey<ClothPatternData>> getClothPattern(ItemStack stack) {
        return Optional.ofNullable(stack.get(AntiqueDataComponentTypes.CLOTH_PATTERN_TYPE));
    }

    public static List<SewnClothPattern> getClothPatterns(ItemStack stack) {
        return stack.getOrDefault(AntiqueDataComponentTypes.SEWN_CLOTHS, List.of());
    }

    public static boolean getClothPatternGlowing(ItemStack stack) {
        return stack.getOrDefault(AntiqueDataComponentTypes.CLOTH_PATTERN_GLOWING, false);
    }

    public static Optional<Holder.Reference<ClothPatternData>> getClothPatternData(ItemStack stack, @Nullable HolderLookup.Provider registries) {
        return getClothPattern(stack).flatMap(key -> Optional.ofNullable(registries).map(r -> ClothPatternData.getHolder(key, r)));
    }

    public static Optional<Integer> getClothPatternColor(ItemStack stack) {
        if (stack.getItem() instanceof ClothPatternItem) {
            return Optional.ofNullable(stack.get(DataComponents.DYED_COLOR))
                    .map(DyedItemColor::rgb);
        } else {
            return Optional.ofNullable(stack.get(AntiqueDataComponentTypes.CLOTH_PATTERN_COLOR))
                    .map(DyedItemColor::rgb);
        }
    }

    public static ItemStack setClothPattern(ItemStack stack, Optional<ResourceKey<ClothPatternData>> pattern) {
        pattern.ifPresentOrElse(
                key -> stack.set(AntiqueDataComponentTypes.CLOTH_PATTERN_TYPE, key),
                () -> stack.remove(AntiqueDataComponentTypes.CLOTH_PATTERN_TYPE)
        );
        return stack;
    }

    public static ItemStack addClothPattern(ItemStack stack, SewnClothPattern pattern) {
        List<SewnClothPattern> patterns = new ArrayList<>(getClothPatterns(stack));
        patterns.add(pattern);
        setClothPatterns(stack, patterns);
        return stack;
    }

    public static ItemStack sewClothPattern(ItemStack stack, SewnClothPattern pattern) {
        List<SewnClothPattern> patterns = new ArrayList<>(getClothPatterns(stack));
        patterns.removeIf(p -> p.key() == pattern.key());
        patterns.add(pattern);
        setClothPatterns(stack, patterns);
        return stack;
    }

    public static ItemStack setClothPatterns(ItemStack stack, List<SewnClothPattern> patterns) {
        stack.set(AntiqueDataComponentTypes.SEWN_CLOTHS, patterns);
        return stack;
    }

    public static ItemStack setClothPatternGlowing(ItemStack stack, boolean glowing) {
        stack.set(AntiqueDataComponentTypes.CLOTH_PATTERN_GLOWING, glowing);
        return stack;
    }

    public static ItemStack setClothPatternColor(ItemStack stack, Optional<Integer> color) {
        if (stack.getItem() instanceof ClothPatternItem) {
            color.ifPresentOrElse(
                    key -> stack.set(DataComponents.DYED_COLOR, new DyedItemColor(key)),
                    () -> stack.remove(DataComponents.DYED_COLOR)
            );
        } else {
            color.ifPresentOrElse(
                    key -> stack.set(AntiqueDataComponentTypes.CLOTH_PATTERN_COLOR, new DyedItemColor(key)),
                    () -> stack.remove(AntiqueDataComponentTypes.CLOTH_PATTERN_COLOR)
            );
        }

        return stack;
    }

    public static Optional<SewnClothPattern> extractClothPattern(ItemStack patternStack) {
        return getClothPattern(patternStack).map(pattern -> new SewnClothPattern(
                pattern,
                getClothPatternColor(patternStack),
                getClothPatternGlowing(patternStack)
        ));
    }
}
