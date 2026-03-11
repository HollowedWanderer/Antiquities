package net.hollowed.antique.networking;

import net.hollowed.antique.Antiquities;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public record ClothOverlayPacketPayload(Identifier identifier) implements CustomPacketPayload {
    public static final Type<@NotNull ClothOverlayPacketPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(Antiquities.MOD_ID, "cloth_overlay_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClothOverlayPacketPayload> CODEC = StreamCodec.ofMember(ClothOverlayPacketPayload::write, ClothOverlayPacketPayload::new);

    public ClothOverlayPacketPayload(RegistryFriendlyByteBuf buf) {
        this(buf.readIdentifier());
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeIdentifier(identifier);
    }

    @SuppressWarnings("all")
    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
