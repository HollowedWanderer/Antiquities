package net.hollowed.antique.util.resources;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.hollowed.antique.index.AntiqueRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;

import java.util.Optional;

public record SewnClothPattern(
        ResourceKey<ClothPatternData> key,
        Optional<Integer> color,
        boolean glowing
) {
    public static final Codec<SewnClothPattern> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(AntiqueRegistries.CLOTH_PATTERNS).fieldOf("key").forGetter(SewnClothPattern::key),
            Codec.INT.optionalFieldOf("color").forGetter(SewnClothPattern::color),
            Codec.BOOL.optionalFieldOf("glowing", false).forGetter(SewnClothPattern::glowing)
    ).apply(instance, SewnClothPattern::new));

    public Optional<Holder.Reference<ClothPatternData>> lookup(HolderLookup.Provider registryAccess) {
        return registryAccess.lookupOrThrow(AntiqueRegistries.CLOTH_PATTERNS).get(key);
    }
}
