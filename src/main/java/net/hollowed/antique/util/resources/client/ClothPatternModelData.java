package net.hollowed.antique.util.resources.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public record ClothPatternModelData(
        List<ClothSprite> sprites
) {
    public static final Codec<ClothPatternModelData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ClothSprite.CODEC.listOf().optionalFieldOf("sprites", List.of()).forGetter(ClothPatternModelData::sprites)
    ).apply(instance, ClothPatternModelData::new));
    public static final FileToIdConverter FILE_LISTER = FileToIdConverter.json("models/cloth_pattern");

    public ClothPatternModelData fillDefaultSprites(Identifier id) {
        List<ClothSprite> sprites = this.sprites;

        if (sprites.isEmpty()) {
            sprites = List.of(new ClothSprite(id.withPrefix("item/"), Optional.empty(), false));
        }

        return new ClothPatternModelData(sprites);
    }
}
