package net.hollowed.antique.items.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;

public record ClothParticleData(
        ParticleOptions particle,
        float chance,
        float distance,
        float velocity,
        Optional<ParticleOptions> waterParticle,
        Optional<Float> waterChance,
        Optional<Float> waterDistance,
        Optional<Float> waterVelocity
) {
    public static final Codec<ClothParticleData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ParticleTypes.CODEC.fieldOf("particle").forGetter(ClothParticleData::particle),
            Codec.FLOAT.fieldOf("chance").forGetter(ClothParticleData::chance),
            Codec.FLOAT.fieldOf("distance").forGetter(ClothParticleData::distance),
            Codec.FLOAT.fieldOf("velocity").forGetter(ClothParticleData::velocity),
            ParticleTypes.CODEC.optionalFieldOf("waterParticle").forGetter(ClothParticleData::waterParticle),
            Codec.FLOAT.optionalFieldOf("waterChance").forGetter(ClothParticleData::waterChance),
            Codec.FLOAT.optionalFieldOf("waterDistance").forGetter(ClothParticleData::waterDistance),
            Codec.FLOAT.optionalFieldOf("waterVelocity").forGetter(ClothParticleData::waterVelocity)
    ).apply(instance, ClothParticleData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClothParticleData> STREAM_CODEC = StreamCodec.composite(
            ParticleTypes.STREAM_CODEC, ClothParticleData::particle,
            ByteBufCodecs.FLOAT, ClothParticleData::chance,
            ByteBufCodecs.FLOAT, ClothParticleData::distance,
            ByteBufCodecs.FLOAT, ClothParticleData::velocity,
            ByteBufCodecs.optional(ParticleTypes.STREAM_CODEC), ClothParticleData::waterParticle,
            ByteBufCodecs.optional(ByteBufCodecs.FLOAT), ClothParticleData::waterChance,
            ByteBufCodecs.optional(ByteBufCodecs.FLOAT), ClothParticleData::waterDistance,
            ByteBufCodecs.optional(ByteBufCodecs.FLOAT), ClothParticleData::waterVelocity,
            ClothParticleData::new
    );
}
