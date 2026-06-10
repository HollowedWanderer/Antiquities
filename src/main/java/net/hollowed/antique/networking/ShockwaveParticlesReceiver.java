package net.hollowed.antique.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hollowed.antique.index.AntiqueParticles;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ShockwaveParticlesReceiver {
    public static void registerClientPacket() {
        ClientPlayNetworking.registerGlobalReceiver(ShockwaveParticlesPayload.ID, (payload, context) ->
                context.client().execute(() -> {
                    Level level = context.client().level;
                    float x = payload.x();
                    float y = payload.y();
                    float z = payload.z();
                    Vec3 pushVector = payload.pushVector();

                    if (level != null) {
                        for (int i = 0; i < 10; i++) {
                            level.addAlwaysVisibleParticle(
                                    AntiqueParticles.SHOCKWAVE_BUBBLE,
                                    x + Math.random() - 0.5,
                                    y + Math.random() - 0.5,
                                    z + Math.random() - 0.5,
                                    pushVector.x,
                                    pushVector.y,
                                    pushVector.z
                            );
                        }
                    }
                })
        );
    }
}
