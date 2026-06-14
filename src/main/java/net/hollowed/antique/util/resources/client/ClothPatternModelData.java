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
        List<ClothSprite> worldSprites,
        List<ClothSprite> itemSprites
) {
    public static final Codec<ClothPatternModelData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ClothSprite.CODEC.listOf().optionalFieldOf("world", List.of()).forGetter(ClothPatternModelData::worldSprites),
            ClothSprite.CODEC.listOf().optionalFieldOf("item", List.of()).forGetter(ClothPatternModelData::itemSprites)
    ).apply(instance, ClothPatternModelData::new));
    public static final FileToIdConverter FILE_LISTER = FileToIdConverter.json("models/cloth_pattern");

    public ClothPatternModelData fillDefaultSprites(Identifier id) {
        List<ClothSprite> worldSprites = this.worldSprites;
        List<ClothSprite> itemSprites = this.itemSprites;

        if (worldSprites.isEmpty()) {
            worldSprites = List.of(new ClothSprite(id.withPrefix("cloth/pattern/"), Optional.empty(), false));
        }

        if (itemSprites.isEmpty()) {
            itemSprites = List.of(new ClothSprite(id.withPrefix("item/"), Optional.empty(), false));
        }

        return new ClothPatternModelData(worldSprites, itemSprites);
    }
}
