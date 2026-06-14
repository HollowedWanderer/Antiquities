package net.hollowed.antique.util.resources;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.hollowed.antique.Antiquities;
import net.hollowed.antique.index.AntiqueRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record ClothSkinData(
        Optional<Identifier> model,
        Optional<String> shape,
        ColorProvider color,
        float length,
        float width,
        Optional<ClothParticleData> particleData,
        Optional<ClothSoundData> ambientSound,
        float gravity,
        float waterGravity,
        int bodyAmount,
        int light,
        boolean patternable,
        boolean dyeable
) {
    public static final String DEFAULT_SHAPE = "default";
    public static final String TATTERED_SHAPE = "tattered";
    public static final String LONG_SHAPE = "long";
    public static final String FORKED_SHAPE = "forked";

    public static final ResourceKey<ClothSkinData> DEFAULT_KEY = ResourceKey.create(AntiqueRegistries.CLOTHS, Antiquities.id("cloth"));
    public static final int DEFAULT_COLOR = 0xFFD13A68;
    public static final ClothSkinData DEFAULT = new ClothSkinData(
            Optional.empty(),
            Optional.empty(),
            new ColorProvider.Constant(DEFAULT_COLOR),
            1.4F,
            0.1F,
            Optional.empty(),
            Optional.empty(),
            1.0F,
            -0.5F,
            8,
            0,
            false,
            false
    );

    public ClothSkinData {
        if (patternable && shape.isEmpty()) {
            throw new IllegalStateException("Must specify a cloth shape to be dyeable");
        }
    }

    public static final Codec<ClothSkinData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.optionalFieldOf("model").forGetter(ClothSkinData::model),
            Codec.STRING.optionalFieldOf("shape").forGetter(ClothSkinData::shape),
            ColorProviders.CODEC.optionalFieldOf("color", new ColorProvider.Constant(DEFAULT_COLOR)).forGetter(ClothSkinData::color),
            Codec.FLOAT.optionalFieldOf("length", 1.4F).forGetter(ClothSkinData::length),
            Codec.FLOAT.optionalFieldOf("width", 0.1F).forGetter(ClothSkinData::width),
            ClothParticleData.CODEC.optionalFieldOf("particleData").forGetter(ClothSkinData::particleData),
            ClothSoundData.CODEC.optionalFieldOf("ambientSound").forGetter(ClothSkinData::ambientSound),
            Codec.FLOAT.optionalFieldOf("gravity", 1.0F).forGetter(ClothSkinData::gravity),
            Codec.FLOAT.optionalFieldOf("waterGravity", -0.5F).forGetter(ClothSkinData::waterGravity),
            Codec.INT.optionalFieldOf("bodies", 8).forGetter(ClothSkinData::bodyAmount),
            Codec.INT.optionalFieldOf("light", 0).forGetter(ClothSkinData::light),
            Codec.BOOL.optionalFieldOf("patternable", false).forGetter(ClothSkinData::patternable),
            Codec.BOOL.optionalFieldOf("dyeable", false).forGetter(ClothSkinData::dyeable)
    ).apply(instance, ClothSkinData::new));

    public static String getTranslationKey(ResourceKey<ClothSkinData> key) {
        return key.identifier().toLanguageKey("item");
    }

    public static ClothSkinData get(Optional<Identifier> id, @NotNull HolderGetter<ClothSkinData> lookup) {
        return id.map(key ->
                lookup.get(ResourceKey.create(AntiqueRegistries.CLOTHS, key))
                        .map(Holder.Reference::value)
                        .orElse(ClothSkinData.DEFAULT)
        ).orElse(ClothSkinData.DEFAULT);
    }

    public static ClothSkinData get(Optional<Identifier> id, @NotNull HolderGetter.Provider lookup) {
        return get(id, lookup.lookupOrThrow(AntiqueRegistries.CLOTHS));
    }

    public static ClothSkinData get(Optional<Identifier> id, @NotNull Level level) {
        return get(id, level.registryAccess());
    }

    public static ClothSkinData get(ResourceKey<ClothSkinData> key, @NotNull HolderGetter<ClothSkinData> lookup) {
        return lookup.get(key)
                .map(Holder.Reference::value)
                .orElseThrow();
    }

    public static ClothSkinData get(ResourceKey<ClothSkinData> key, @NotNull HolderGetter.Provider lookup) {
        return get(key, lookup.lookupOrThrow(AntiqueRegistries.CLOTHS));
    }

    public static ClothSkinData get(ResourceKey<ClothSkinData> key, @NotNull Level level) {
        return get(key, level.registryAccess());
    }

    public static Optional<Holder.Reference<ClothSkinData>> getHolder(Optional<Identifier> id, @NotNull HolderGetter<ClothSkinData> lookup) {
        return id.flatMap(key ->
                lookup.get(ResourceKey.create(AntiqueRegistries.CLOTHS, key))
        );
    }

    public static Optional<Holder.Reference<ClothSkinData>> getHolder(Optional<Identifier> id, @NotNull HolderGetter.Provider lookup) {
        return getHolder(id, lookup.lookupOrThrow(AntiqueRegistries.CLOTHS));
    }

    public static Optional<Holder.Reference<ClothSkinData>> getHolder(Optional<Identifier> id, @NotNull Level level) {
        return getHolder(id, level.registryAccess());
    }

    public static Holder.Reference<ClothSkinData> getHolder(ResourceKey<ClothSkinData> key, @NotNull HolderGetter<ClothSkinData> lookup) {
        return lookup.get(key).orElseThrow();
    }

    public static Holder.Reference<ClothSkinData> getHolder(ResourceKey<ClothSkinData> key, @NotNull HolderGetter.Provider lookup) {
        return getHolder(key, lookup.lookupOrThrow(AntiqueRegistries.CLOTHS));
    }

    public static Holder.Reference<ClothSkinData> getHolder(ResourceKey<ClothSkinData> key, @NotNull Level level) {
        return getHolder(key, level.registryAccess());
    }

    public static Optional<Holder.Reference<ClothSkinData>> getHolderFromKey(Optional<ResourceKey<ClothSkinData>> key, @NotNull HolderGetter<ClothSkinData> lookup) {
        return key.flatMap(lookup::get);
    }

    public static Optional<Holder.Reference<ClothSkinData>> getHolderFromKey(Optional<ResourceKey<ClothSkinData>> key, @NotNull HolderGetter.Provider lookup) {
        return getHolderFromKey(key, lookup.lookupOrThrow(AntiqueRegistries.CLOTHS));
    }

    public static Optional<Holder.Reference<ClothSkinData>> getHolderFromKey(Optional<ResourceKey<ClothSkinData>> key, @NotNull Level level) {
        return getHolderFromKey(key, level.registryAccess());
    }
}
