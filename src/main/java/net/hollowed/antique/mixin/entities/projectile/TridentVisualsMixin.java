package net.hollowed.antique.mixin.entities.projectile;

import net.hollowed.antique.index.AntiqueDataComponentTypes;
import net.hollowed.combatamenities.index.CAParticles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownTrident.class)
public abstract class TridentVisualsMixin extends AbstractArrow {

    @Unique
    private boolean canPickup = false;

    protected TridentVisualsMixin(EntityType<? extends AbstractArrow> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    protected void onHit(@NotNull HitResult hitResult) {
        super.onHit(hitResult);
        if (this.level() instanceof ServerLevel serverWorld) {
            Vec3 pos = this.position().add(this.getLookAngle().multiply(1, 1, -1));
            serverWorld.sendParticles(CAParticles.RING, pos.x(), pos.y(), pos.z(), 1, 0.0, 0.0, 0.0, 0);
        }
    }

    @Inject(method = "onHitEntity", at = @At("TAIL"))
    private void onHitEntity(EntityHitResult hitResult, CallbackInfo ci) {
        canPickup = true;
    }

    @ModifyArg(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/arrow/ThrownTrident;deflect(Lnet/minecraft/world/entity/projectile/ProjectileDeflection;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/EntityReference;ZLnet/minecraft/world/phys/Vec3;)Z"), index = 4)
    private Vec3 onHitEntity(Vec3 par5) {
        return new Vec3(0.3, 0.6, 0.3);
    }

    @Override
    protected boolean tryPickup(final @NonNull Player player) {
        return tryPickupEdit(player) || this.isNoPhysics() && this.ownedBy(player) && player.getInventory().add(this.getPickupItem());
    }

    @Unique
    protected boolean tryPickupEdit(final Player player) {
        return switch (this.pickup) {
            case DISALLOWED -> false;
            case ALLOWED -> {
                ItemStack stack = this.getPickupItem();
                if (!this.isInGround()) stack.set(AntiqueDataComponentTypes.RESET_ATTACK_COOLDOWN, Unit.INSTANCE);
                yield player.getInventory().add(stack);
            }
            case CREATIVE_ONLY -> player.hasInfiniteMaterials();
        };
    }

    @Override
    public void playerTouch(final @NonNull Player player) {
        if (!this.level().isClientSide() && canPickup && this.shakeTime <= 0) {
            if (this.tryPickup(player)) {
                player.take(this, 1);
                this.discard();
                return;
            }
        }

        super.playerTouch(player);
    }
}
