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

public record ClothOverlayData(
        Optional<String> translationKey,
        Optional<Identifier> texture
) {
    public static final ClothOverlayData DEFAULT = new ClothOverlayData(
            Optional.empty(),
            Optional.empty()
    );

    public static final Codec<ClothOverlayData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("translationKey").forGetter(ClothOverlayData::translationKey),
            Identifier.CODEC.optionalFieldOf("texture").forGetter(ClothOverlayData::texture)
    ).apply(instance, ClothOverlayData::new));

    public static ClothOverlayData get(Optional<Identifier> id, @NotNull HolderGetter<ClothOverlayData> lookup) {
        return id.map(key ->
                lookup.get(ResourceKey.create(AntiqueRegistries.CLOTH_OVERLAYS, key))
                        .map(Holder.Reference::value)
                        .orElse(ClothOverlayData.DEFAULT)
        ).orElse(ClothOverlayData.DEFAULT);
    }

    public static ClothOverlayData get(Optional<Identifier> id, @NotNull HolderGetter.Provider lookup) {
        return get(id, lookup.lookupOrThrow(AntiqueRegistries.CLOTH_OVERLAYS));
    }

    public static ClothOverlayData get(Optional<Identifier> id, @NotNull Level level) {
        return get(id, level.registryAccess());
    }

    public static ClothOverlayData get(ResourceKey<ClothOverlayData> key, @NotNull HolderGetter<ClothOverlayData> lookup) {
        return lookup.get(key)
                .map(Holder.Reference::value)
                .orElseThrow();
    }

    public static ClothOverlayData get(ResourceKey<ClothOverlayData> key, @NotNull HolderGetter.Provider lookup) {
        return get(key, lookup.lookupOrThrow(AntiqueRegistries.CLOTH_OVERLAYS));
    }

    public static ClothOverlayData get(ResourceKey<ClothOverlayData> key, @NotNull Level level) {
        return get(key, level.registryAccess());
    }

    public static Optional<Holder.Reference<ClothOverlayData>> getHolder(Optional<Identifier> id, @NotNull HolderGetter<ClothOverlayData> lookup) {
        return id.flatMap(key ->
                lookup.get(ResourceKey.create(AntiqueRegistries.CLOTH_OVERLAYS, key))
        );
    }

    public static Optional<Holder.Reference<ClothOverlayData>> getHolder(Optional<Identifier> id, @NotNull HolderGetter.Provider lookup) {
        return getHolder(id, lookup.lookupOrThrow(AntiqueRegistries.CLOTH_OVERLAYS));
    }

    public static Optional<Holder.Reference<ClothOverlayData>> getHolder(Optional<Identifier> id, @NotNull Level level) {
        return getHolder(id, level.registryAccess());
    }

    public static Holder.Reference<ClothOverlayData> getHolder(ResourceKey<ClothOverlayData> key, @NotNull HolderGetter<ClothOverlayData> lookup) {
        return lookup.get(key).orElseThrow();
    }

    public static Holder.Reference<ClothOverlayData> getHolder(ResourceKey<ClothOverlayData> key, @NotNull HolderGetter.Provider lookup) {
        return getHolder(key, lookup.lookupOrThrow(AntiqueRegistries.CLOTH_OVERLAYS));
    }

    public static Holder.Reference<ClothOverlayData> getHolder(ResourceKey<ClothOverlayData> key, @NotNull Level level) {
        return getHolder(key, level.registryAccess());
    }

    public static Optional<Holder.Reference<ClothOverlayData>> getHolderFromKey(Optional<ResourceKey<ClothOverlayData>> key, @NotNull HolderGetter<ClothOverlayData> lookup) {
        return key.flatMap(lookup::get);
    }

    public static Optional<Holder.Reference<ClothOverlayData>> getHolderFromKey(Optional<ResourceKey<ClothOverlayData>> key, @NotNull HolderGetter.Provider lookup) {
        return getHolderFromKey(key, lookup.lookupOrThrow(AntiqueRegistries.CLOTH_OVERLAYS));
    }

    public static Optional<Holder.Reference<ClothOverlayData>> getHolderFromKey(Optional<ResourceKey<ClothOverlayData>> key, @NotNull Level level) {
        return getHolderFromKey(key, level.registryAccess());
    }
}
