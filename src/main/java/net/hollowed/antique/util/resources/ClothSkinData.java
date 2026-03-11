package net.hollowed.antique.util.resources;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record ClothSkinData(
        List<ClothSubData> list
) {
    public static final Codec<ClothSkinData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ClothSubData.CODEC.listOf().fieldOf("skins").orElseGet(() -> List.of(new ClothSubData(Identifier.parse(""), "", 0, 0, 1, -0.5F, 0, 0, false, false))).forGetter(ClothSkinData::list)
    ).apply(instance, ClothSkinData::new));

    public record ClothSubData(Identifier model, String hex, float length, float width, float gravity, float waterGravity, int bodyAmount, int light, boolean overlay, boolean dyeable) {
        public static final Codec<ClothSubData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("model").forGetter(ClothSubData::model),
                Codec.STRING.fieldOf("color").orElse("d13a68").forGetter(ClothSubData::hex),
                Codec.FLOAT.fieldOf("length").orElse(1.4F).forGetter(ClothSubData::length),
                Codec.FLOAT.fieldOf("width").orElse(0.1F).forGetter(ClothSubData::width),
                Codec.FLOAT.fieldOf("gravity").orElse(1.0F).forGetter(ClothSubData::gravity),
                Codec.FLOAT.fieldOf("water_gravity").orElse(-0.5F).forGetter(ClothSubData::waterGravity),
                Codec.INT.fieldOf("bodies").orElse(8).forGetter(ClothSubData::bodyAmount),
                Codec.INT.fieldOf("light").orElse(0).forGetter(ClothSubData::light),
                Codec.BOOL.fieldOf("overlay").orElse(false).forGetter(ClothSubData::overlay),
                Codec.BOOL.fieldOf("dyeable").orElse(false).forGetter(ClothSubData::dyeable)
        ).apply(instance, ClothSubData::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ClothSubData> STREAM_CODEC = StreamCodec.composite(
                Identifier.STREAM_CODEC, ClothSubData::model,
                ByteBufCodecs.STRING_UTF8, ClothSubData::hex,
                ByteBufCodecs.FLOAT, ClothSubData::length,
                ByteBufCodecs.FLOAT, ClothSubData::width,
                ByteBufCodecs.FLOAT, ClothSubData::gravity,
                ByteBufCodecs.FLOAT, ClothSubData::waterGravity,
                ByteBufCodecs.INT, ClothSubData::bodyAmount,
                ByteBufCodecs.INT, ClothSubData::light,
                ByteBufCodecs.BOOL, ClothSubData::overlay,
                ByteBufCodecs.BOOL, ClothSubData::dyeable,
                ClothSubData::new
        );
    }
}
