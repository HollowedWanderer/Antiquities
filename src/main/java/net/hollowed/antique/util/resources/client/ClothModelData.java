package net.hollowed.antique.util.resources.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hollowed.antique.client.cloth.ClothRenderer;
import net.hollowed.antique.client.cloth.ClothRenderers;
import net.hollowed.antique.util.CodecUtil;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public record ClothModelData(
        ClothRenderer worldRenderer,
        List<ClothSprite> itemSprites,
        Map<TiedClothSize, Map<TiedClothDomain, List<ClothSprite>>> tiedSprites
) {
    public static final ClothModelData EMPTY = new ClothModelData(ClothRenderers.DEFAULT, List.of(), Map.of());
    public static final Codec<ClothModelData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ClothRenderers.CODEC.optionalFieldOf("world", ClothRenderers.DEFAULT).forGetter(ClothModelData::worldRenderer),
            CodecUtil.compactListOf(ClothSprite.CODEC).optionalFieldOf("item", List.of()).forGetter(ClothModelData::itemSprites),
            Codec.unboundedMap(TiedClothSize.CODEC, Codec.unboundedMap(TiedClothDomain.CODEC, CodecUtil.compactListOf(ClothSprite.CODEC))).optionalFieldOf("tied", Map.of()).forGetter(ClothModelData::tiedSprites)
    ).apply(instance, ClothModelData::new));
    public static final FileToIdConverter FILE_LISTER = FileToIdConverter.json("models/cloth");

    public ClothModelData fillDefaultSprites(Identifier id) {
        List<ClothSprite> itemSprites = this.itemSprites;

        if (itemSprites.isEmpty()) {
            itemSprites = List.of(new ClothSprite(id.withPrefix("item/"), Optional.empty(), false));
        }

        return new ClothModelData(worldRenderer.fillDefaults(id), itemSprites, tiedSprites);
    }
}
