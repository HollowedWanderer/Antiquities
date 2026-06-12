package net.hollowed.antique.util.resources;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;

import net.hollowed.antique.items.components.ColorProvider;
import net.hollowed.antique.items.components.ColorProviders;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record ClothSkinData(
        List<ClothSubData> list
) {
    public static final ClothSubData DEFAULT = new ClothSubData(Optional.empty(), new ColorProvider.Constant("d13a68"), 1.4F, 0.1F, 1.0F, -0.5F, 8, 0, false, false, false, false);
    public static final Codec<ClothSkinData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ClothSubData.CODEC.listOf().fieldOf("skins").orElseGet(() -> List.of(DEFAULT)).forGetter(ClothSkinData::list)
    ).apply(instance, ClothSkinData::new));

    public record ClothSubData(Optional<Identifier> model, ColorProvider color, float length, float width, float gravity, float waterGravity, int bodyAmount, int light, boolean emissiveItem, boolean emissiveLayer, boolean overlay, boolean dyeable) {
        public static final Codec<ClothSubData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.optionalFieldOf("model").forGetter(ClothSubData::model),
                ColorProviders.CODEC.optionalFieldOf("color", new ColorProvider.Constant("d13a68")).forGetter(ClothSubData::color),
                Codec.FLOAT.optionalFieldOf("length", 1.4F).forGetter(ClothSubData::length),
                Codec.FLOAT.optionalFieldOf("width", 0.1F).forGetter(ClothSubData::width),
                Codec.FLOAT.optionalFieldOf("gravity", 1.0F).forGetter(ClothSubData::gravity),
                Codec.FLOAT.optionalFieldOf("waterGravity", -0.5F).forGetter(ClothSubData::waterGravity),
                Codec.INT.optionalFieldOf("bodies", 8).forGetter(ClothSubData::bodyAmount),
                Codec.INT.optionalFieldOf("light", 0).forGetter(ClothSubData::light),
                Codec.BOOL.optionalFieldOf("emissiveItem", false).forGetter(ClothSubData::emissiveItem),
                Codec.BOOL.optionalFieldOf("emissiveLayer", false).forGetter(ClothSubData::emissiveLayer),
                Codec.BOOL.optionalFieldOf("overlay", false).forGetter(ClothSubData::overlay),
                Codec.BOOL.optionalFieldOf("dyeable", false).forGetter(ClothSubData::dyeable)
        ).apply(instance, ClothSubData::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ClothSubData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.optional(Identifier.STREAM_CODEC), ClothSubData::model,
                ColorProviders.STREAM_CODEC, ClothSubData::color,
                ByteBufCodecs.FLOAT, ClothSubData::length,
                ByteBufCodecs.FLOAT, ClothSubData::width,
                ByteBufCodecs.FLOAT, ClothSubData::gravity,
                ByteBufCodecs.FLOAT, ClothSubData::waterGravity,
                ByteBufCodecs.INT, ClothSubData::bodyAmount,
                ByteBufCodecs.INT, ClothSubData::light,
                ByteBufCodecs.BOOL, ClothSubData::emissiveItem,
                ByteBufCodecs.BOOL, ClothSubData::emissiveLayer,
                ByteBufCodecs.BOOL, ClothSubData::overlay,
                ByteBufCodecs.BOOL, ClothSubData::dyeable,
                ClothSubData::new
        );
    }
}
