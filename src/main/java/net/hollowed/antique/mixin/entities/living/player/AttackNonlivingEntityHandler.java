package net.hollowed.antique.mixin.entities.living.player;

import net.hollowed.antique.enchantments.EnchantmentListener;
import net.hollowed.antique.index.AntiqueEffects;
import net.hollowed.antique.index.AntiqueItems;
import net.hollowed.antique.items.ScepterItem;
import net.hollowed.antique.util.delay.TickDelayScheduler;
import net.hollowed.combatamenities.index.CAParticles;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Objects;

@Mixin(Player.class)
public abstract class AttackNonlivingEntityHandler extends LivingEntity {

    @Shadow public abstract void setRemainingFireTicks(int remainingTicks);

    @Shadow public abstract void attack(Entity entity);

    @Unique
    private boolean ranScepterAttack = false;

    @Unique
    private int parryCooldown = 0;

    protected AttackNonlivingEntityHandler(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(at = @At("HEAD"), method = "attack", cancellable = true)
    private void attackWithScepter(Entity entity, CallbackInfo ci) {
        if (!ranScepterAttack) {
            Player player = (Player) (Object) this;

            float attackPower = player.getAttackStrengthScale(0.0f);
            ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);

            if (stack.getItem() instanceof ScepterItem) {
                player.level().playSound(player, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.HEAVY_CORE_PLACE, SoundSource.PLAYERS, 1.0F, 1.3F);

                if (this.level() instanceof ServerLevel serverWorld) {
                    serverWorld.sendParticles(CAParticles.RING, entity.getX(), entity.getY(0.6), entity.getZ(), 1, 0.1, 0.2, 0.1, 0);
                }

                Vec3 effectiveVelocity = ScepterItem.playerVelocity;

                if (player.getDeltaMovement().length() > 0.1) {
                    int delay = player.getDeltaMovement().length() > 0.6 ? (int) (player.getDeltaMovement().length() * 5F) : 0;
                    if (entity instanceof net.hollowed.combatamenities.util.interfaces.EntityFreezer access) {
                        access.antiquities$setFrozen(true, delay - 1);
                    }
                    if (player instanceof net.hollowed.combatamenities.util.interfaces.EntityFreezer access) {
                        access.antiquities$setFrozen(true, delay - 1);
                    }
                    ranScepterAttack = true;
                    TickDelayScheduler.schedule(delay, () -> {
                        if (attackPower > 0.9f) {
                            float pitch = 1.1f + (player.getRandom().nextFloat() * .2f);
                            if (this.level() instanceof ServerLevel serverWorld) {
                                serverWorld.sendParticles(ParticleTypes.GUST_EMITTER_SMALL, entity.getX(), entity.getY(0.5), entity.getZ(), 1, 0.1, 0.0, 0.1, 0);
                            }

                            if (!entity.onGround()) {
                                player.level().playSound(player, player.getX(), player.getY(), player.getZ(),
                                        SoundEvents.MACE_SMASH_AIR, SoundSource.PLAYERS, 1.0F, 1.3F);
                            } else {
                                player.level().playSound(player, player.getX(), player.getY(), player.getZ(),
                                        SoundEvents.MACE_SMASH_GROUND, SoundSource.PLAYERS, 1.0F, pitch);
                            }
                        }

                        // Scepter attack code
                        if (effectiveVelocity.length() > 0.1) {

                            float damage = Math.min((float) (effectiveVelocity.length() * 7.5 + 6), 30);
                            if (player.level() instanceof ServerLevel serverWorld) {
                                entity.hurtServer(serverWorld, player.level().damageSources().flyIntoWall(), damage);
                            }

                            Vec3 targetVelocity = effectiveVelocity.multiply(4, 2.5, 4);

                            if (entity instanceof Player || entity instanceof ServerPlayer) {
                                targetVelocity = targetVelocity.multiply(2, 0.2, 2);
                            }

                            entity.setDeltaMovement(targetVelocity.scale(0.6 / targetVelocity.length() * targetVelocity.length()));
                            entity.hurtMarked = true;

                            double radius = 5.0;
                            List<Entity> nearbyEntities = player.level().getEntities(
                                    entity, entity.getBoundingBox().inflate(radius));

                            for (Entity nearby : nearbyEntities) {
                                if (nearby instanceof LivingEntity && nearby != entity && nearby != player) {
                                    double distance = entity.position().distanceTo(nearby.position());
                                    if (distance <= radius) {
                                        double scalingFactor = 2.5 - (distance / radius);
                                        Vec3 reducedVelocity = effectiveVelocity.scale(scalingFactor);
                                        nearby.setDeltaMovement(reducedVelocity);
                                        nearby.hurtMarked = true;
                                    }
                                }
                            }

                            // Handle enchantments
                            if (EnchantmentListener.hasEnchantment(stack, "antique:kinematic")) {
                                Vec3 playerVelocity = effectiveVelocity.multiply(-1, -1, -1); // Reverse the velocity
                                player.setDeltaMovement(playerVelocity);
                                player.hurtMarked = true;

                                player.addEffect(new MobEffectInstance(AntiqueEffects.BOUNCE_EFFECT, 30, 0, true, true));
                            } else if (EnchantmentListener.hasEnchantment(stack, "antique:impetus")) {
                                Vec3 playerVelocity = effectiveVelocity.multiply(1.5, 1.25, 1.5); // Enhance the velocity
                                player.setDeltaMovement(playerVelocity.scale(0.6 / targetVelocity.length() * targetVelocity.length()));
                                player.hurtMarked = true;

                                player.addEffect(new MobEffectInstance(AntiqueEffects.VOLATILE_BOUNCE_EFFECT, 30, 0, true, true));
                            }

                            if (entity instanceof Player playerTarget && !EnchantmentListener.hasEnchantment(stack, "antique:kinematic")) {
                                playerTarget.addEffect(new MobEffectInstance(AntiqueEffects.VOLATILE_BOUNCE_EFFECT, 30, 0, true, true));
                            } else if (entity instanceof LivingEntity livingTarget) {
                                livingTarget.addEffect(new MobEffectInstance(AntiqueEffects.BOUNCE_EFFECT, 30, 0, true, true));
                            }
                        }

                        this.attack(entity);
                    });

                    ci.cancel();
                }
            }
        } else {
            ranScepterAttack = false;
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        if (this.parryCooldown > 0) {
            this.parryCooldown--;
        }
        if (this.getUseItem().is(AntiqueItems.SCEPTER)) {
            float time = this.getTicksUsingItem() * 0.04F;
            if (time == 2.0F) {
                this.level().playPlayerSound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.NEUTRAL, 1, 1);
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "attack")
    private void attackWithPauldrons(Entity target, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        ItemStack stack = player.getItemBySlot(EquipmentSlot.CHEST);
        if (stack.is(AntiqueItems.MYRIAD_PAULDRONS) && target instanceof Projectile entity && this.parryCooldown <= 0) {
            this.parryCooldown = 5;
            if (this.level() instanceof ServerLevel serverWorld) {
                serverWorld.sendParticles(ParticleTypes.GUST, target.getX(), target.getY(0.5), target.getZ(), 1, 0.1, 0.0, 0.1, 0);
            }

            player.level().playSound(player, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.HEAVY_CORE_PLACE, SoundSource.PLAYERS, 1.0F, 1.3F);
            player.level().playSound(player, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WIND_CHARGE_BURST, SoundSource.PLAYERS, 1.0F, 0.7F);
            Vec3 velocity = player.getViewVector(0).normalize();
            if (Objects.equals(entity.getOwner(), player)) {
                target.setDeltaMovement(velocity.x * 1.5F * target.getDeltaMovement().length(), velocity.y * 1.5F * target.getDeltaMovement().length(), velocity.z * 1.5F * target.getDeltaMovement().length());
            } else {
                target.setDeltaMovement(velocity.x * 2.0F, velocity.y * 2.0F, velocity.z * 2.0F);
            }
            target.hurtMarked = true;
            target.needsSync = true;
        }
    }
}