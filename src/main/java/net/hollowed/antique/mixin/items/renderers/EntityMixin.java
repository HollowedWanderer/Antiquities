package net.hollowed.antique.mixin.items.renderers;

import net.hollowed.antique.util.interfaces.duck.ClothAccess;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
    @Shadow
    private Level level;

    @Inject(
            method = "onRemoval",
            at = @At("HEAD")
    )
    @SuppressWarnings("all")
    private void onRemoval(Entity.RemovalReason reason, CallbackInfo ci) {
        if (level instanceof ClothAccess cloths) {
            cloths.antique$getManagers().remove(this);
        }
    }
}
