package net.hollowed.antique.networking;

import net.hollowed.antique.Antiquities;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record DyePacketPayload(String color) implements CustomPacketPayload {
    public static final Type<@NotNull DyePacketPayload> ID = new Type<>(Antiquities.id("dye_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DyePacketPayload> CODEC = StreamCodec.ofMember(DyePacketPayload::write, DyePacketPayload::new);

    public DyePacketPayload(RegistryFriendlyByteBuf buf) {
        this(buf.readUtf());
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(color);
    }

    @SuppressWarnings("all")
    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
