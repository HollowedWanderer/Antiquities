package net.hollowed.antique.util.resources;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.hollowed.antique.index.AntiqueRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record ClothPatternData(
        Optional<String> translationKey,
        Optional<Identifier> texture
) {
    public static final ClothPatternData DEFAULT = new ClothPatternData(
            Optional.empty(),
            Optional.empty()
    );

    public static final Codec<ClothPatternData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("translationKey").forGetter(ClothPatternData::translationKey),
            Identifier.CODEC.optionalFieldOf("texture").forGetter(ClothPatternData::texture)
    ).apply(instance, ClothPatternData::new));

    public static ClothPatternData get(Optional<Identifier> id, @NotNull HolderGetter<ClothPatternData> lookup) {
        return id.map(key ->
                lookup.get(ResourceKey.create(AntiqueRegistries.CLOTH_PATTERNS, key))
                        .map(Holder.Reference::value)
                        .orElse(ClothPatternData.DEFAULT)
        ).orElse(ClothPatternData.DEFAULT);
    }

    public static ClothPatternData get(Optional<Identifier> id, @NotNull HolderGetter.Provider lookup) {
        return get(id, lookup.lookupOrThrow(AntiqueRegistries.CLOTH_PATTERNS));
    }

    public static ClothPatternData get(Optional<Identifier> id, @NotNull Level level) {
        return get(id, level.registryAccess());
    }

    public static ClothPatternData get(ResourceKey<ClothPatternData> key, @NotNull HolderGetter<ClothPatternData> lookup) {
        return lookup.get(key)
                .map(Holder.Reference::value)
                .orElseThrow();
    }

    public static ClothPatternData get(ResourceKey<ClothPatternData> key, @NotNull HolderGetter.Provider lookup) {
        return get(key, lookup.lookupOrThrow(AntiqueRegistries.CLOTH_PATTERNS));
    }

    public static ClothPatternData get(ResourceKey<ClothPatternData> key, @NotNull Level level) {
        return get(key, level.registryAccess());
    }

    public static Optional<Holder.Reference<ClothPatternData>> getHolder(Optional<Identifier> id, @NotNull HolderGetter<ClothPatternData> lookup) {
        return id.flatMap(key ->
                lookup.get(ResourceKey.create(AntiqueRegistries.CLOTH_PATTERNS, key))
        );
    }

    public static Optional<Holder.Reference<ClothPatternData>> getHolder(Optional<Identifier> id, @NotNull HolderGetter.Provider lookup) {
        return getHolder(id, lookup.lookupOrThrow(AntiqueRegistries.CLOTH_PATTERNS));
    }

    public static Optional<Holder.Reference<ClothPatternData>> getHolder(Optional<Identifier> id, @NotNull Level level) {
        return getHolder(id, level.registryAccess());
    }

    public static Holder.Reference<ClothPatternData> getHolder(ResourceKey<ClothPatternData> key, @NotNull HolderGetter<ClothPatternData> lookup) {
        return lookup.get(key).orElseThrow();
    }

    public static Holder.Reference<ClothPatternData> getHolder(ResourceKey<ClothPatternData> key, @NotNull HolderGetter.Provider lookup) {
        return getHolder(key, lookup.lookupOrThrow(AntiqueRegistries.CLOTH_PATTERNS));
    }

    public static Holder.Reference<ClothPatternData> getHolder(ResourceKey<ClothPatternData> key, @NotNull Level level) {
        return getHolder(key, level.registryAccess());
    }

    public static Optional<Holder.Reference<ClothPatternData>> getHolderFromKey(Optional<ResourceKey<ClothPatternData>> key, @NotNull HolderGetter<ClothPatternData> lookup) {
        return key.flatMap(lookup::get);
    }

    public static Optional<Holder.Reference<ClothPatternData>> getHolderFromKey(Optional<ResourceKey<ClothPatternData>> key, @NotNull HolderGetter.Provider lookup) {
        return getHolderFromKey(key, lookup.lookupOrThrow(AntiqueRegistries.CLOTH_PATTERNS));
    }

    public static Optional<Holder.Reference<ClothPatternData>> getHolderFromKey(Optional<ResourceKey<ClothPatternData>> key, @NotNull Level level) {
        return getHolderFromKey(key, level.registryAccess());
    }
}
