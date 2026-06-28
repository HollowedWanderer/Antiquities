package net.hollowed.antique.mixin.items;

import net.hollowed.antique.index.AntiqueDataComponentTypes;
import net.hollowed.antique.index.AntiqueItems;
import net.hollowed.antique.items.MyriadToolItem;
import net.hollowed.antique.items.components.MyriadToolComponent;
import net.hollowed.combatamenities.index.CAParticles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Attackable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MyriadAxeBlockMixin extends Entity implements Attackable {

    @Shadow public abstract boolean isUsingItem();

    @Shadow protected ItemStack useItem;

    @Shadow protected int useItemRemaining;

    @Unique
    private boolean ran = false;

    public MyriadAxeBlockMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    public void blockWithMyriadAxe(ServerLevel level, DamageSource source, float damage, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        Vec3 attackDirection = source.getSourcePosition() != null ? source.getSourcePosition().subtract(self.position()).normalize() : Vec3.ZERO;
        if (attackDirection.equals(Vec3.ZERO)) {
            attackDirection = new Vec3(0, -1, 0);
        }

        Vec3 lookDirection = self.getViewVector(1.0F).normalize();
        double angle = attackDirection.dot(lookDirection);

        if (this.getAxeBlockingItem() != null && (angle > 0.0F) && !ran
            && !(source.is(DamageTypeTags.IS_PROJECTILE)) && (!source.is(DamageTypeTags.BYPASSES_SHIELD) || source.is(DamageTypes.FALL)) && !source.is(DamageTypeTags.IS_FIRE)) {

            float reducedDamage = damage * 0.5F;
            if (self instanceof Player player) {
                player.level().playSound(null, self.blockPosition(), SoundEvents.HEAVY_CORE_PLACE, SoundSource.PLAYERS, 1.0F, 1.2F);
                player.level().playSound(null, self.blockPosition(), SoundEvents.SHIELD_BLOCK.value(), SoundSource.PLAYERS, 0.25F, 1.2F);
                if (source.getDirectEntity() instanceof LivingEntity attacker) {
                    Vec3 knockbackDirection = attacker.position().subtract(player.position()).normalize();
                    attacker.knockback(0.25, -knockbackDirection.x, -knockbackDirection.z, source, 0);
                    attacker.hurtMarked = true;
                    attacker.needsSync = true;
                }
            }
            this.ran = true;
            self.hurtServer(level, source, reducedDamage);

            if (damage > 20.0F) {
                if (self instanceof ServerPlayer player) {
                    player.getCooldowns().addCooldown(player.getUseItem(), 20);
                    self.releaseUsingItem();
                }
            }

            double d = -Mth.sin(this.getYRot() * (float) (Math.PI / 180.0)) * 1.5;
            double e = Mth.cos(this.getYRot() * (float) (Math.PI / 180.0)) * 1.5;
            level.sendParticles(CAParticles.RING, this.getX() + d, this.getY(0.5) + 0.25, this.getZ() + e, 0, d, 0.0, e, 0.0);

            cir.setReturnValue(false);
        } else {
            if (source.is(DamageTypeTags.IS_PROJECTILE) && this.getAxeBlockingItem() != null && (angle > 0.0F)) {
                self.level().playSound(null, self.blockPosition(), SoundEvents.HEAVY_CORE_PLACE, SoundSource.PLAYERS, 1.0F, 1.2F);
                self.level().playSound(null, self.blockPosition(), SoundEvents.SHIELD_BLOCK.value(), SoundSource.PLAYERS, 0.25F, 1.2F);
                if (source.getDirectEntity() != null) {
                    level.sendParticles(CAParticles.RING, source.getDirectEntity().getX(), source.getDirectEntity().getY(), source.getDirectEntity().getZ(), 1, 0.0, 0.0, 0.0, 0);
                }
                cir.setReturnValue(false);
            }
            this.ran = false;
        }
    }

    @Unique
    @Nullable
    public ItemStack getAxeBlockingItem() {
        if (this.isUsingItem() && !this.useItem.isEmpty()) {
            Item item = this.useItem.getItem();
            if (!(item instanceof MyriadToolItem && this.useItem.getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, MyriadToolComponent.DEFAULT_NO_CLOTH).toolBit().is(AntiqueItems.MYRIAD_AXE_HEAD))) {
                return null;
            } else {
                return item.getUseDuration(this.useItem, (LivingEntity) (Object) this) - this.useItemRemaining < 5 ? null : this.useItem;
            }
        } else {
            return null;
        }
    }
}
