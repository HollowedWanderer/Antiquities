package net.hollowed.antique.networking;

import net.hollowed.antique.Antiquities;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record PouncePacketPayload() implements CustomPacketPayload {
    public static final Type<@NotNull PouncePacketPayload> ID = new Type<>(Antiquities.id("pounce_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PouncePacketPayload> CODEC = StreamCodec.ofMember(PouncePacketPayload::write, PouncePacketPayload::new);

    public PouncePacketPayload(RegistryFriendlyByteBuf buf) {
        this();
    }

    public void write(RegistryFriendlyByteBuf buf) {

    }

    @SuppressWarnings("all")
    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
