package net.hollowed.antique.mixin.entities.living;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityFreezer implements net.hollowed.combatamenities.util.interfaces.EntityFreezer {
    @Shadow public abstract void setDeltaMovement(Vec3 deltaMovement);

    @Shadow public boolean hurtMarked;
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

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        if (this.frozen && this.time > 0) {
            this.setDeltaMovement(Vec3.ZERO);
            this.hurtMarked = true;
            this.time--;
        }
    }
}