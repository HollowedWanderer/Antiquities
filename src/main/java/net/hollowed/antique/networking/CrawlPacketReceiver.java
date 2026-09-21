package net.hollowed.antique.networking;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.hollowed.antique.config.AntiquitiesConfig;
import net.hollowed.antique.util.interfaces.duck.Crawl;

public class CrawlPacketReceiver {
    public static void registerServerPacket() {
        ServerPlayNetworking.registerGlobalReceiver(CrawlPacketPayload.ID, (payload, context) -> context.server().execute(() -> {
            if (context.player() instanceof Crawl access) {
                access.antique$setCrawl(payload.crawling());
                if (payload.slide() && context.player().onGround() && context.player().isSprinting()) access.antique$setCrawlStart(AntiquitiesConfig.SLIDE_DURATION);
            }
        }));
    }
}
