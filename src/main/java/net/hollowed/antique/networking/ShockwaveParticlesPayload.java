package net.hollowed.antique.networking;

import net.hollowed.antique.Antiquities;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public record ShockwaveParticlesPayload(float x, float y, float z, Vec3 pushVector) implements CustomPacketPayload {
    public static final Type<@NotNull ShockwaveParticlesPayload> ID = new Type<>(Antiquities.id("shockwave_particles"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShockwaveParticlesPayload> CODEC = StreamCodec.ofMember(ShockwaveParticlesPayload::write, ShockwaveParticlesPayload::new);

    public ShockwaveParticlesPayload(RegistryFriendlyByteBuf buf) {
        this(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readVec3());
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeFloat(x);
        buf.writeFloat(y);
        buf.writeFloat(z);
        buf.writeVec3(pushVector);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
