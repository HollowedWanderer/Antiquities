package net.hollowed.antique.mixin.entities.living.player;

import net.hollowed.antique.index.AntiqueKeyBindings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PreventActionsMixin {

    @Shadow @Final private Abilities abilities;

    @Inject(method = "tick", at = @At("HEAD"))
    public void preventInteract(CallbackInfo ci) {
        this.abilities.mayBuild = !AntiqueKeyBindings.showSatchel.isDown();
    }

    @Inject(method = "isWithinBlockInteractionRange", at = @At("HEAD"), cancellable = true)
    public void preventInteract(BlockPos pos, double buffer, CallbackInfoReturnable<Boolean> cir) {
        if (AntiqueKeyBindings.showSatchel.isDown()) {
            cir.setReturnValue(false);
        }
    }
}
