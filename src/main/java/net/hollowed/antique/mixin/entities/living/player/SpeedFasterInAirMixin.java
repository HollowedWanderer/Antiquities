package net.hollowed.antique.mixin.entities.living.player;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

@Mixin(Player.class)
public abstract class SpeedFasterInAirMixin extends LivingEntity {

    @Shadow @Final private Abilities abilities;

    protected SpeedFasterInAirMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "getFlyingSpeed", at = @At("HEAD"), cancellable = true)
    public void getAirSpeed(CallbackInfoReturnable<Float> cir) {
        float speed = 0.025999999F;
        if (this.isSprinting() && !(this.abilities.flying && !this.isPassenger())) {
            if (this.hasEffect(MobEffects.SPEED)) {
                speed += Objects.requireNonNull(this.getEffect(MobEffects.SPEED)).getAmplifier() * 0.015F;
            }
            if (this.hasEffect(MobEffects.JUMP_BOOST)) {
                speed += Objects.requireNonNull(this.getEffect(MobEffects.JUMP_BOOST)).getAmplifier() * 0.005F;
            }
            cir.setReturnValue(speed);
        }
    }
}
