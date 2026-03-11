package net.hollowed.antique.networking;

import net.hollowed.antique.Antiquities;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public record AddClothItemsPayload() implements CustomPacketPayload {
    public static final Type<@NotNull AddClothItemsPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(Antiquities.MOD_ID, "cloth_items_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AddClothItemsPayload> CODEC = StreamCodec.ofMember(AddClothItemsPayload::write, AddClothItemsPayload::new);

    public AddClothItemsPayload(RegistryFriendlyByteBuf buf) {
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
