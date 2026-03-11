package net.hollowed.antique.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hollowed.antique.util.resources.ClientClothData;

public class ClothSkinPacketReceiver {
    public static void registerClientPacket() {
        ClientPlayNetworking.registerGlobalReceiver(ClothSkinPacketPayload.ID, (payload, context) -> context.client().execute(() ->
                ClientClothData.addTransform(payload.string(), payload.data())
        ));
    }
}
