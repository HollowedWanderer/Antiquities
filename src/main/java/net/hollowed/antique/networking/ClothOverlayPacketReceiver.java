package net.hollowed.antique.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hollowed.antique.util.resources.ClientClothData;

public class ClothOverlayPacketReceiver {
    public static void registerClientPacket() {
        ClientPlayNetworking.registerGlobalReceiver(ClothOverlayPacketPayload.ID, (payload, context) -> context.client().execute(() ->
                ClientClothData.addOverlay(payload.identifier())
        ));
    }
}
