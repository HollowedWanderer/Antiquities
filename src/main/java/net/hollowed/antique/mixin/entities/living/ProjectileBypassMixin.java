package net.hollowed.antique.mixin.entities.living;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class ProjectileBypassMixin {

    @Shadow protected float lastHurt;

    @Inject(method = "hurtServer", at = @At("HEAD"))
    private void bypassImmunityFrames(ServerLevel level, DamageSource source, float damage, CallbackInfoReturnable<Boolean> cir) {
        if (source.getDirectEntity() instanceof Projectile) {
            ProjectileBypassMixin entity = this;
            entity.lastHurt = 0;
        }
    }
}