package net.hollowed.antique.util.resources;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

public record ClothSkins(
        List<ClothSkin> list
) {
    public static final Codec<ClothSkins> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ClothSkin.CODEC.listOf().fieldOf("skins").orElseGet(() -> List.of(ClothSkin.DEFAULT)).forGetter(ClothSkins::list)
    ).apply(instance, ClothSkins::new));
}
