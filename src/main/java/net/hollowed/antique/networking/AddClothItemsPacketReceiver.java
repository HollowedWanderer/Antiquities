package net.hollowed.antique.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hollowed.antique.Antiquities;

public class AddClothItemsPacketReceiver {
    public static void registerClientPacket() {
        ClientPlayNetworking.registerGlobalReceiver(AddClothItemsPayload.ID, (payload, context) -> context.client().execute(Antiquities::addClothItems));
    }
}
