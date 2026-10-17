package net.hollowed.antique.networking;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class PouncePacketReceiver {
    public static void registerServerPacket() {
        ServerPlayNetworking.registerGlobalReceiver(PouncePacketPayload.ID, (_, context) -> context.server().execute(() ->
                context.player().level().playSound(null, context.player().getX(), context.player().getY(), context.player().getZ(), SoundEvents.GOAT_LONG_JUMP, SoundSource.PLAYERS, 1.0F, 1.0F)));
    }
}
