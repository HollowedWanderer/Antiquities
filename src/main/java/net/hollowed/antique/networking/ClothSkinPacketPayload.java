package net.hollowed.antique.networking;

import net.hollowed.antique.Antiquities;
import net.hollowed.antique.util.resources.ClothSkinData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public record ClothSkinPacketPayload(Identifier id, ClothSkinData.ClothSubData data) implements CustomPacketPayload {
    public static final Type<@NotNull ClothSkinPacketPayload> ID = new Type<>(Antiquities.id("cloth_skin_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClothSkinPacketPayload> CODEC = StreamCodec.ofMember(ClothSkinPacketPayload::write, ClothSkinPacketPayload::new);

    public ClothSkinPacketPayload(RegistryFriendlyByteBuf buf) {
        this(buf.readIdentifier(), ClothSkinData.ClothSubData.STREAM_CODEC.decode(buf));
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeIdentifier(id);
        ClothSkinData.ClothSubData.STREAM_CODEC.encode(buf, data);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
