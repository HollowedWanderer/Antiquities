package net.hollowed.antique.networking;

import net.hollowed.antique.Antiquities;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public record IllusionerParticlePacketPayload(double x, double y, double z) implements CustomPacketPayload {
    public static final Type<@NotNull IllusionerParticlePacketPayload> ID = new Type<>(Antiquities.id("illusioner_particle_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, IllusionerParticlePacketPayload> CODEC = StreamCodec.ofMember(IllusionerParticlePacketPayload::write, IllusionerParticlePacketPayload::new);

    public IllusionerParticlePacketPayload(RegistryFriendlyByteBuf buf) {
        this(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
    }

    @SuppressWarnings("all")
    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
