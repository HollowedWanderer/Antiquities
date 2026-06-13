package net.hollowed.antique.mixin.ext;

import net.hollowed.antique.util.shockwave.Shockwave;
import net.hollowed.antique.util.shockwave.ShockwaveManager;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ServerLevel.class)
public class ShockwaveTickMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(BooleanSupplier booleanSupplier, CallbackInfo ci) {
        for (Shockwave shockwave : ShockwaveManager.shockwaves.keySet()) {
            ServerLevel level = ShockwaveManager.shockwaves.get(shockwave);
            if (shockwave.tick(level)) {
                ShockwaveManager.shockwaves.remove(shockwave, level);
            }
        }
    }
}
