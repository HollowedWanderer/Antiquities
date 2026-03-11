package net.hollowed.antique.networking;

import net.hollowed.antique.Antiquities;
import net.hollowed.antique.util.resources.ClothSkinData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public record ClothSkinPacketPayload(String string, ClothSkinData.ClothSubData data) implements CustomPacketPayload {
    public static final Type<@NotNull ClothSkinPacketPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(Antiquities.MOD_ID, "cloth_skin_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClothSkinPacketPayload> CODEC = StreamCodec.ofMember(ClothSkinPacketPayload::write, ClothSkinPacketPayload::new);

    public ClothSkinPacketPayload(RegistryFriendlyByteBuf buf) {
        this(decodeString(buf), decodeData(buf));
    }

    public void write(RegistryFriendlyByteBuf buf) {
        boolean hasString = string != null;
        buf.writeBoolean(hasString);
        ByteBufCodecs.STRING_UTF8.encode(buf, hasString ? string : "");
        boolean hasData = data != null;
        buf.writeBoolean(hasData);
        ClothSkinData.ClothSubData.STREAM_CODEC.encode(buf, hasData ? data : new ClothSkinData.ClothSubData(Identifier.parse(""), "d13a68", 1.4F, 0.1F, 1.0F, -0.5F, 8, 0, false, false));
    }

    private static String decodeString(RegistryFriendlyByteBuf buf) {
        return buf.readBoolean() ? ByteBufCodecs.STRING_UTF8.decode(buf) : null;
    }

    private static ClothSkinData.ClothSubData decodeData(RegistryFriendlyByteBuf buf) {
        return buf.readBoolean() ? ClothSkinData.ClothSubData.STREAM_CODEC.decode(buf) : null;
    }

    @SuppressWarnings("all")
    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
