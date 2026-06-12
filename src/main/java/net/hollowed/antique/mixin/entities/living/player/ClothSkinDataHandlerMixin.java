package net.hollowed.antique.mixin.entities.living.player;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.hollowed.antique.networking.AddClothItemsPayload;
import net.hollowed.antique.networking.ClothOverlayPacketPayload;
import net.hollowed.antique.util.resources.ClothOverlayListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.server.level.ServerLevel$EntityCallbacks")
public class ClothSkinDataHandlerMixin {

    @Inject(method = "onTrackingStart(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"))
    private void onTrackingStart(Entity entity, CallbackInfo ci) {
        if (entity instanceof ServerPlayer serverPlayer) {
            for (Identifier identifier : ClothOverlayListener.getTransforms()) {
                ServerPlayNetworking.send(serverPlayer, new ClothOverlayPacketPayload(identifier));
            }
            ServerPlayNetworking.send(serverPlayer, new AddClothItemsPayload());
        }
    }
}
