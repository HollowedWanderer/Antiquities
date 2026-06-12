package net.hollowed.antique.util.resources;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.hollowed.antique.index.AntiqueRegistries;
import net.hollowed.antique.items.components.ClothParticleData;
import net.hollowed.antique.items.components.ClothSoundData;
import net.hollowed.antique.items.components.ColorProvider;
import net.hollowed.antique.items.components.ColorProviders;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public record ClothSkin(
        Optional<Identifier> model,
        ColorProvider color,
        float length,
        float width,
        Optional<ClothParticleData> particleData,
        Optional<ClothSoundData> ambientSound,
        float gravity,
        float waterGravity,
        int bodyAmount,
        int light,
        boolean emissiveItem,
        boolean emissiveLayer,
        boolean overlay,
        boolean dyeable
) {
    public static final ClothSkin DEFAULT = new ClothSkin(
            Optional.empty(),
            new ColorProvider.Constant("d13a68"),
            1.4F,
            0.1F,
            Optional.empty(),
            Optional.empty(),
            1.0F,
            -0.5F,
            8,
            0,
            false,
            false,
            false,
            false
    );

    public static final Codec<ClothSkin> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.optionalFieldOf("model").forGetter(ClothSkin::model),
            ColorProviders.CODEC.optionalFieldOf("color", new ColorProvider.Constant("d13a68")).forGetter(ClothSkin::color),
            Codec.FLOAT.optionalFieldOf("length", 1.4F).forGetter(ClothSkin::length),
            Codec.FLOAT.optionalFieldOf("width", 0.1F).forGetter(ClothSkin::width),
            ClothParticleData.CODEC.optionalFieldOf("particleData").forGetter(ClothSkin::particleData),
            ClothSoundData.CODEC.optionalFieldOf("ambientSound").forGetter(ClothSkin::ambientSound),
            Codec.FLOAT.optionalFieldOf("gravity", 1.0F).forGetter(ClothSkin::gravity),
            Codec.FLOAT.optionalFieldOf("waterGravity", -0.5F).forGetter(ClothSkin::waterGravity),
            Codec.INT.optionalFieldOf("bodies", 8).forGetter(ClothSkin::bodyAmount),
            Codec.INT.optionalFieldOf("light", 0).forGetter(ClothSkin::light),
            Codec.BOOL.optionalFieldOf("emissiveItem", false).forGetter(ClothSkin::emissiveItem),
            Codec.BOOL.optionalFieldOf("emissiveLayer", false).forGetter(ClothSkin::emissiveLayer),
            Codec.BOOL.optionalFieldOf("overlay", false).forGetter(ClothSkin::overlay),
            Codec.BOOL.optionalFieldOf("dyeable", false).forGetter(ClothSkin::dyeable)
    ).apply(instance, ClothSkin::new));

    public static ClothSkin get(Optional<Identifier> id, @NotNull HolderGetter<ClothSkin> lookup) {
        return id.map(key ->
                lookup.get(ResourceKey.create(AntiqueRegistries.CLOTHS, key))
                        .map(Holder.Reference::value)
                        .orElse(ClothSkin.DEFAULT)
        ).orElse(ClothSkin.DEFAULT);
    }

    public static ClothSkin get(Optional<Identifier> id, @NotNull HolderGetter.Provider lookup) {
        return get(id, lookup.lookupOrThrow(AntiqueRegistries.CLOTHS));
    }

    public static ClothSkin get(Optional<Identifier> id, @NotNull Level level) {
        return get(id, level.registryAccess());
    }

    public static Optional<Holder.Reference<ClothSkin>> getHolder(Optional<Identifier> id, @NotNull HolderGetter<ClothSkin> lookup) {
        return id.flatMap(key ->
                lookup.get(ResourceKey.create(AntiqueRegistries.CLOTHS, key))
        );
    }

    public static Optional<Holder.Reference<ClothSkin>> getHolder(Optional<Identifier> id, @NotNull HolderGetter.Provider lookup) {
        return getHolder(id, lookup.lookupOrThrow(AntiqueRegistries.CLOTHS));
    }

    public static Optional<Holder.Reference<ClothSkin>> getHolder(Optional<Identifier> id, @NotNull Level level) {
        return getHolder(id, level.registryAccess());
    }
}
