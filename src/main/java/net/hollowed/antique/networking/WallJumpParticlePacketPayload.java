package net.hollowed.antique.networking;

import net.hollowed.antique.Antiquities;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public record WallJumpParticlePacketPayload(float x, float y, float z, float particleX, float particleZ, Vec3 pushVector) implements CustomPacketPayload {
    public static final Type<@NotNull WallJumpParticlePacketPayload> ID = new Type<>(Antiquities.id("wall_jump_particle_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, WallJumpParticlePacketPayload> CODEC = StreamCodec.ofMember(WallJumpParticlePacketPayload::write, WallJumpParticlePacketPayload::new);

    public WallJumpParticlePacketPayload(RegistryFriendlyByteBuf buf) {
        this(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readVec3());
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeFloat(x);
        buf.writeFloat(y);
        buf.writeFloat(z);
        buf.writeFloat(particleX);
        buf.writeFloat(particleZ);
        buf.writeVec3(pushVector);
    }

    @SuppressWarnings("all")
    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
