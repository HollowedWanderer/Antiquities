package net.hollowed.antique.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityFreezer implements net.hollowed.antique.util.EntityFreezer {
    @Shadow public abstract void setVelocity(Vec3d velocity);

    @Shadow public boolean velocityModified;
    @Unique
    boolean frozen;

    @Override
    public void antiquities$setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    @Override
    public boolean antiquities$getFrozen() {
        return this.frozen;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        if (this.frozen) {
            this.setVelocity(new Vec3d(0, 0, 0));
            this.velocityModified = true;
        }
    }
}
