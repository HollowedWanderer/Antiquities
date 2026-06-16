package net.hollowed.antique.networking;

import net.hollowed.antique.Antiquities;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record TuneAmethystForkPayload(int slot, int note) implements CustomPacketPayload {
    public static final Type<@NotNull TuneAmethystForkPayload> ID = new Type<>(Antiquities.id("tune_amethyst_work"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TuneAmethystForkPayload> CODEC = StreamCodec.ofMember(TuneAmethystForkPayload::write, TuneAmethystForkPayload::new);

    public TuneAmethystForkPayload(RegistryFriendlyByteBuf buf) {
        this(buf.readInt(), buf.readInt());
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeInt(slot);
        buf.writeInt(note);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
