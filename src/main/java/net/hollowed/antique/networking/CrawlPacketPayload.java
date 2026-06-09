package net.hollowed.antique.networking;

import net.hollowed.antique.Antiquities;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public record CrawlPacketPayload(boolean crawling) implements CustomPacketPayload {
    public static final Type<@NotNull CrawlPacketPayload> ID = new Type<>(Antiquities.id("crawl_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CrawlPacketPayload> CODEC = StreamCodec.ofMember(CrawlPacketPayload::write, CrawlPacketPayload::new);

    public CrawlPacketPayload(RegistryFriendlyByteBuf buf) {
        this(buf.readBoolean());
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(crawling);
    }

    @SuppressWarnings("all")
    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
