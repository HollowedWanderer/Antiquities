package net.hollowed.antique.mixin.entities.living.player;

import net.hollowed.antique.util.CoyoteAttackTimeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class CoyoteBiteMixin {

    @Shadow
    @Nullable
    public MultiPlayerGameMode gameMode;

    @Shadow
    @Nullable
    public LocalPlayer player;

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void coyoteBite(CallbackInfoReturnable<Boolean> cir) {
        if (CoyoteAttackTimeEvent.target != null && gameMode != null && player != null) {
            gameMode.attack(player, CoyoteAttackTimeEvent.target);
            player.swing(InteractionHand.MAIN_HAND, true);
            cir.setReturnValue(true);
        }
    }
}
