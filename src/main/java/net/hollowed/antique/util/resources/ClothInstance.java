package net.hollowed.antique.util.resources;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.hollowed.antique.Antiquities;
import net.hollowed.antique.index.AntiqueDataComponentTypes;
import net.hollowed.antique.index.AntiqueRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

import java.util.Optional;

public record ClothInstance(
        ResourceKey<ClothSkinData> cloth,
        Optional<Integer> clothColor,
        Optional<ResourceKey<ClothPatternData>> pattern,
        Optional<Integer> patternColor
) {
    public static final ClothInstance DEFAULT = new ClothInstance(
            ResourceKey.create(AntiqueRegistries.CLOTHS, Antiquities.id("cloth")),
            Optional.empty(),
            Optional.empty(),
            Optional.empty()
    );

    public static final Codec<ClothInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(AntiqueRegistries.CLOTHS).fieldOf("cloth").forGetter(ClothInstance::cloth),
            Codec.INT.optionalFieldOf("cloth_color").forGetter(ClothInstance::clothColor),
            ResourceKey.codec(AntiqueRegistries.CLOTH_PATTERNS).optionalFieldOf("patternable").forGetter(ClothInstance::pattern),
            Codec.INT.optionalFieldOf("pattern_color").forGetter(ClothInstance::patternColor)
    ).apply(instance, ClothInstance::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClothInstance> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(AntiqueRegistries.CLOTHS), ClothInstance::cloth,
            ByteBufCodecs.optional(ByteBufCodecs.INT), ClothInstance::clothColor,
            ByteBufCodecs.optional(ResourceKey.streamCodec(AntiqueRegistries.CLOTH_PATTERNS)), ClothInstance::pattern,
            ByteBufCodecs.optional(ByteBufCodecs.INT), ClothInstance::patternColor,
            ClothInstance::new
    );

    public ClothInstance(ItemStack stack) {
        this(
                stack.getOrDefault(AntiqueDataComponentTypes.CLOTH_TYPE, ClothSkinData.DEFAULT_KEY),
                Optional.ofNullable(stack.get(DataComponents.DYED_COLOR)).map(DyedItemColor::rgb),
                Optional.empty(),
                Optional.empty()
        );
    }

    public ClothInstance withCloth(ResourceKey<ClothSkinData> cloth) {
        return new ClothInstance(cloth, clothColor, pattern, patternColor);
    }

    public ClothInstance withClothColor(Optional<Integer> clothColor) {
        return new ClothInstance(cloth, clothColor, pattern, patternColor);
    }

    public ClothInstance withPattern(Optional<ResourceKey<ClothPatternData>> pattern) {
        return new ClothInstance(cloth, clothColor, pattern, patternColor);
    }

    public ClothInstance withPatternColor(Optional<Integer> patternColor) {
        return new ClothInstance(cloth, clothColor, pattern, patternColor);
    }

    public ClothInstance withCloth(ItemStack stack) {
        ResourceKey<ClothSkinData> cloth = stack.getOrDefault(AntiqueDataComponentTypes.CLOTH_TYPE, ClothSkinData.DEFAULT_KEY);
        Optional<Integer> clothColor = Optional.ofNullable(stack.get(DataComponents.DYED_COLOR)).map(DyedItemColor::rgb);
        return new ClothInstance(cloth, clothColor, pattern, patternColor);
    }

    public ClothInstance withPattern(ItemStack stack) {
        Optional<ResourceKey<ClothPatternData>> pattern = Optional.ofNullable(stack.get(AntiqueDataComponentTypes.CLOTH_PATTERN_TYPE));
        Optional<Integer> patternColor = Optional.ofNullable(stack.get(DataComponents.DYED_COLOR)).map(DyedItemColor::rgb);
        return new ClothInstance(cloth, clothColor, pattern, patternColor);
    }
}
