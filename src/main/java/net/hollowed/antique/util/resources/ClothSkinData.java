package net.hollowed.antique.util.resources;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;

import net.hollowed.antique.items.components.ClothParticleData;
import net.hollowed.antique.items.components.ColorProvider;
import net.hollowed.antique.items.components.ColorProviders;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public record ClothSkinData(
        List<ClothSubData> list
) {
    public static final ClothSubData DEFAULT = new ClothSubData(Optional.empty(), new ColorProvider.Constant("d13a68"), 1.4F, 0.1F, Optional.empty(), 1.0F, -0.5F, 8, 0, false, false, false, false);
    public static final Codec<ClothSkinData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ClothSubData.CODEC.listOf().fieldOf("skins").orElseGet(() -> List.of(DEFAULT)).forGetter(ClothSkinData::list)
    ).apply(instance, ClothSkinData::new));

    public record ClothSubData(Optional<Identifier> model, ColorProvider color, float length, float width, Optional<ClothParticleData> particleData, float gravity, float waterGravity, int bodyAmount, int light, boolean emissiveItem, boolean emissiveLayer, boolean overlay, boolean dyeable) {
        public static final Codec<ClothSubData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.optionalFieldOf("model").forGetter(ClothSubData::model),
                ColorProviders.CODEC.optionalFieldOf("color", new ColorProvider.Constant("d13a68")).forGetter(ClothSubData::color),
                Codec.FLOAT.optionalFieldOf("length", 1.4F).forGetter(ClothSubData::length),
                Codec.FLOAT.optionalFieldOf("width", 0.1F).forGetter(ClothSubData::width),
                ClothParticleData.CODEC.optionalFieldOf("particleData").forGetter(ClothSubData::particleData),
                Codec.FLOAT.optionalFieldOf("gravity", 1.0F).forGetter(ClothSubData::gravity),
                Codec.FLOAT.optionalFieldOf("waterGravity", -0.5F).forGetter(ClothSubData::waterGravity),
                Codec.INT.optionalFieldOf("bodies", 8).forGetter(ClothSubData::bodyAmount),
                Codec.INT.optionalFieldOf("light", 0).forGetter(ClothSubData::light),
                Codec.BOOL.optionalFieldOf("emissiveItem", false).forGetter(ClothSubData::emissiveItem),
                Codec.BOOL.optionalFieldOf("emissiveLayer", false).forGetter(ClothSubData::emissiveLayer),
                Codec.BOOL.optionalFieldOf("overlay", false).forGetter(ClothSubData::overlay),
                Codec.BOOL.optionalFieldOf("dyeable", false).forGetter(ClothSubData::dyeable)
        ).apply(instance, ClothSubData::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ClothSubData> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public @NonNull ClothSubData decode(@NonNull RegistryFriendlyByteBuf buf) {
                return new ClothSubData(
                        ByteBufCodecs.optional(Identifier.STREAM_CODEC).decode(buf),
                        ColorProviders.STREAM_CODEC.decode(buf),
                        buf.readFloat(),
                        buf.readFloat(),
                        ByteBufCodecs.optional(ClothParticleData.STREAM_CODEC).decode(buf),
                        buf.readFloat(),
                        buf.readFloat(),
                        buf.readInt(),
                        buf.readInt(),
                        buf.readBoolean(),
                        buf.readBoolean(),
                        buf.readBoolean(),
                        buf.readBoolean()
                );
            }

            @Override
            public void encode(@NonNull RegistryFriendlyByteBuf buf, @NonNull ClothSubData data) {
                ByteBufCodecs.optional(Identifier.STREAM_CODEC).encode(buf, data.model);
                ColorProviders.STREAM_CODEC.encode(buf, data.color);
                buf.writeFloat(data.length);
                buf.writeFloat(data.width);
                ByteBufCodecs.optional(ClothParticleData.STREAM_CODEC).encode(buf, data.particleData);
                buf.writeFloat(data.gravity);
                buf.writeFloat(data.waterGravity);
                buf.writeInt(data.bodyAmount);
                buf.writeInt(data.light);
                buf.writeBoolean(data.emissiveItem);
                buf.writeBoolean(data.emissiveLayer);
                buf.writeBoolean(data.overlay);
                buf.writeBoolean(data.dyeable);
            }
        };
    }
}
