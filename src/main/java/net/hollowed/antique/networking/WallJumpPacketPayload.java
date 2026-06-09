package net.hollowed.antique.networking;

import net.hollowed.antique.Antiquities;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public record WallJumpPacketPayload(int entityId) implements CustomPacketPayload {
    public static final Type<@NotNull WallJumpPacketPayload> ID = new Type<>(Antiquities.id("wall_jump_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, WallJumpPacketPayload> CODEC = StreamCodec.ofMember(WallJumpPacketPayload::write, WallJumpPacketPayload::new);

    public WallJumpPacketPayload(RegistryFriendlyByteBuf buf) {
        this(buf.readInt());
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeInt(entityId);
    }

    @SuppressWarnings("all")
    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
