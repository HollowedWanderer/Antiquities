package net.hollowed.antique.util.resources;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public record ClothSoundData(
        Optional<Identifier> sound,
        Optional<Identifier> waterSound
) {
    public static final Codec<ClothSoundData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.optionalFieldOf("sound").forGetter(ClothSoundData::sound),
            Identifier.CODEC.optionalFieldOf("waterSound").forGetter(ClothSoundData::waterSound)
    ).apply(instance, ClothSoundData::new));
}
