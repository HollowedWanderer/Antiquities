package net.hollowed.antique.mixin.entities.living;

import net.hollowed.combatamenities.util.interfaces.EntityFreezer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityFreezer implements EntityFreezer {
    @Unique
    boolean frozen;
    @Unique
    int time;

    @Override
    public void antiquities$setFrozen(boolean frozen, int time) {
        this.frozen = frozen;
        this.time = time;
    }

    @Override
    public boolean antiquities$getFrozen() {
        return this.frozen;
    }

    @Inject(method = "handleRelativeFrictionAndCalculateMovement", at = @At("HEAD"), cancellable = true)
    public void setMovementInput(Vec3 input, float friction, CallbackInfoReturnable<Vec3> cir) {
        if (this.frozen && this.time > 0) {
            this.time--;
            cir.setReturnValue(Vec3.ZERO);
        }
    }
}