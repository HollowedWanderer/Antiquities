package net.hollowed.antique.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hollowed.antique.entities.ai.IllusionerCloneParticles;

public class IllusionerParticlePacketReceiver {
    public static void registerClientPacket() {
        ClientPlayNetworking.registerGlobalReceiver(IllusionerParticlePacketPayload.ID, (payload, context) -> context.client().execute(() -> IllusionerCloneParticles.particles(context.player().level(), payload.x(), payload.y(), payload.z())));
    }
}
