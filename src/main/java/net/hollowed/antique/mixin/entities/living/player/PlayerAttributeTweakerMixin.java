package net.hollowed.antique.mixin.entities.living.player;

import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerAttributeTweakerMixin {
    @Inject(method = "createAttributes", at = @At("RETURN"), cancellable = true)
    private static void addSafeFallDistanceAttribute(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        cir.setReturnValue(cir.getReturnValue()
                .add(Attributes.SAFE_FALL_DISTANCE, 5)
                .add(Attributes.MOVEMENT_SPEED, 0.1075)
        );
    }
}
