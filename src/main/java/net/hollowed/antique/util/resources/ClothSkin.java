package net.hollowed.antique.util.resources;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.hollowed.antique.items.components.ClothParticleData;
import net.hollowed.antique.items.components.ColorProvider;
import net.hollowed.antique.items.components.ColorProviders;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public record ClothSkin(Optional<Identifier> model, ColorProvider color, float length, float width,
                        Optional<ClothParticleData> particleData, float gravity, float waterGravity, int bodyAmount,
                        int light, boolean emissiveItem, boolean emissiveLayer, boolean overlay, boolean dyeable) {
    public static final ClothSkin DEFAULT = new ClothSkin(Optional.empty(), new ColorProvider.Constant("d13a68"), 1.4F, 0.1F, Optional.empty(), 1.0F, -0.5F, 8, 0, false, false, false, false);

    public static final Codec<ClothSkin> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.optionalFieldOf("model").forGetter(ClothSkin::model),
            ColorProviders.CODEC.optionalFieldOf("color", new ColorProvider.Constant("d13a68")).forGetter(ClothSkin::color),
            Codec.FLOAT.optionalFieldOf("length", 1.4F).forGetter(ClothSkin::length),
            Codec.FLOAT.optionalFieldOf("width", 0.1F).forGetter(ClothSkin::width),
            ClothParticleData.CODEC.optionalFieldOf("particleData").forGetter(ClothSkin::particleData),
            Codec.FLOAT.optionalFieldOf("gravity", 1.0F).forGetter(ClothSkin::gravity),
            Codec.FLOAT.optionalFieldOf("waterGravity", -0.5F).forGetter(ClothSkin::waterGravity),
            Codec.INT.optionalFieldOf("bodies", 8).forGetter(ClothSkin::bodyAmount),
            Codec.INT.optionalFieldOf("light", 0).forGetter(ClothSkin::light),
            Codec.BOOL.optionalFieldOf("emissiveItem", false).forGetter(ClothSkin::emissiveItem),
            Codec.BOOL.optionalFieldOf("emissiveLayer", false).forGetter(ClothSkin::emissiveLayer),
            Codec.BOOL.optionalFieldOf("overlay", false).forGetter(ClothSkin::overlay),
            Codec.BOOL.optionalFieldOf("dyeable", false).forGetter(ClothSkin::dyeable)
    ).apply(instance, ClothSkin::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClothSkin> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NonNull ClothSkin decode(@NonNull RegistryFriendlyByteBuf buf) {
            return new ClothSkin(
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
        public void encode(@NonNull RegistryFriendlyByteBuf buf, @NonNull ClothSkin data) {
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
