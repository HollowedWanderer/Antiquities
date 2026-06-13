package net.hollowed.antique.util.resources.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public record ClothModelData(
        List<ClothSprite> worldSprites,
        List<ClothSprite> itemSprites,
        Map<TiedClothSize, Map<TiedClothDomain, List<ClothSprite>>> tiedSprites
) {
    public static final ClothModelData EMPTY = new ClothModelData(List.of(), List.of(), Map.of());
    public static final Codec<ClothModelData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ClothSprite.CODEC.listOf().optionalFieldOf("world", List.of()).forGetter(ClothModelData::worldSprites),
            ClothSprite.CODEC.listOf().optionalFieldOf("item", List.of()).forGetter(ClothModelData::itemSprites),
            Codec.unboundedMap(TiedClothSize.CODEC, Codec.unboundedMap(TiedClothDomain.CODEC, ClothSprite.CODEC.listOf())).optionalFieldOf("tied", Map.of()).forGetter(ClothModelData::tiedSprites)
    ).apply(instance, ClothModelData::new));
    public static final FileToIdConverter FILE_LISTER = FileToIdConverter.json("models/cloth");

    public ClothModelData fillDefaultSprites(Identifier id) {
        List<ClothSprite> worldSprites = this.worldSprites;
        List<ClothSprite> itemSprites = this.itemSprites;

        if (worldSprites.isEmpty()) {
            worldSprites = List.of(new ClothSprite(id.withPrefix("cloth/"), Optional.empty(), false));
        }

        if (itemSprites.isEmpty()) {
            itemSprites = List.of(new ClothSprite(id.withPrefix("item/"), Optional.empty(), false));
        }

        return new ClothModelData(worldSprites, itemSprites, tiedSprites);
    }
}
