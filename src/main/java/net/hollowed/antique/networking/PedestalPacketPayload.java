package net.hollowed.antique.networking;

import net.hollowed.antique.Antiquities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record PedestalPacketPayload(BlockPos blockPos, ItemStack stack) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<@NotNull PedestalPacketPayload> ID = new CustomPacketPayload.Type<>(Antiquities.id("pedestal_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PedestalPacketPayload> CODEC = StreamCodec.ofMember(PedestalPacketPayload::write, PedestalPacketPayload::new);

    public PedestalPacketPayload(RegistryFriendlyByteBuf buf) {
        this(buf.readBlockPos(), decodeStack(buf));
    }

    private static ItemStack decodeStack(RegistryFriendlyByteBuf buf) {
        return buf.readBoolean() ? ItemStack.STREAM_CODEC.decode(buf) : ItemStack.EMPTY;
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        boolean hasItem = !stack.isEmpty();
        buf.writeBoolean(hasItem);
        if (hasItem) {
            ItemStack.STREAM_CODEC.encode(buf, stack);
        }
    }

    @SuppressWarnings("all")
    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
