package net.hollowed.antique.mixin.entities.living.player;

import net.hollowed.antique.index.AntiqueKeyBindings;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PreventActionsMixin {

    @Shadow @Final private PlayerAbilities abilities;

    @Inject(method = "tick", at = @At("HEAD"))
    public void preventInteract(CallbackInfo ci) {
        this.abilities.allowModifyWorld = !AntiqueKeyBindings.showSatchel.isPressed();
    }

    @Inject(method = "canInteractWithBlockAt", at = @At("HEAD"), cancellable = true)
    public void preventInteract(BlockPos pos, double additionalRange, CallbackInfoReturnable<Boolean> cir) {
        if (AntiqueKeyBindings.showSatchel.isPressed()) {
            cir.setReturnValue(false);
        }
    }
}
