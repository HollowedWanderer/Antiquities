package net.hollowed.antique.items;

import net.hollowed.antique.enchantments.EnchantmentListener;
import net.hollowed.antique.index.AntiqueEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ScepterItem extends Item {

    private static Vec3 startTickPosition;
    public static Vec3 playerVelocity = new Vec3(0, 0, 0);
    public static Vec3 lastHitVelocity = new Vec3(0, 0, 0);

    public ScepterItem(Properties settings) {
        super(settings);
    }

    @Override
    public boolean canDestroyBlock(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, LivingEntity user) {
        return !user.hasInfiniteMaterials();
    }

    public static ItemAttributeModifiers createAttributeModifiers() {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 5.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -2.2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(Identifier.withDefaultNamespace("base_attack_range"), 0.75, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build();
    }

    @Override
    public boolean releaseUsing(@NotNull ItemStack stack, @NotNull Level world, @NotNull LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof Player player)) {
            return false;
        }

        Vec3 multiplier;

        if (EnchantmentListener.hasEnchantment(stack, "antique:kinematic") || EnchantmentListener.hasEnchantment(stack, "antique:impetus")) {
            if (EnchantmentListener.hasEnchantment(stack, "antique:kinematic")) {
                multiplier = new Vec3(-3, -1.5, -3);
            } else {
                multiplier = new Vec3(3, -1, 3);
            }

            int chargeTime = user.getTicksUsingItem();

            // Check if the player is targeting a block
            HitResult hitResult = player.pick(5.0D, 0.0F, false);
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                // Interact with a block
                float velocity = Math.clamp(chargeTime * 0.04F + 0.25F, EnchantmentListener.hasEnchantment(stack, "antique:impetus") ? 0.6F : 1.0F, 2.0F);

                player.push(player.getViewVector(0).multiply(multiplier).scale(velocity));
                player.syncVelocity = true;
                if (user.level() instanceof ServerLevel serverWorld) {
                    serverWorld.sendParticles(ParticleTypes.GUST, user.getX(), user.getY() + 0.25F, user.getZ(), 1, 0.1, 0.0, 0.1, 0);
                }

                world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.MACE_SMASH_AIR, SoundSource.NEUTRAL, 1F, 0.9f + (player.getRandom().nextFloat() * .2f));
                player.awardStat(Stats.ITEM_USED.get(this));
                user.swing(user.getUsedItemHand(), SwingAnimation.DEFAULT, true);
                if (!user.level().isClientSide()) {
                    player.getCooldowns().addCooldown(stack, 130);
                    if (EnchantmentListener.hasEnchantment(stack, "antique:kinematic")) {
                        user.addEffect(new MobEffectInstance(AntiqueEffects.VOLATILE_BOUNCE_EFFECT, (int) Math.clamp((velocity - 0.625) * 40, 10, 10000), 0, true, true));
                    } else {
                        user.addEffect(new MobEffectInstance(AntiqueEffects.BOUNCE_EFFECT, (int) Math.clamp((velocity - 0.625) * 50, 10, 10000), 0, true, true));
                    }
                }
            } else {
                // Spawn a hitbox and interact with entities inside it
                Vec3 forwardVec = user.getViewVector(1.0F); // Get the direction the player is facing
                Vec3 hitboxCenter = user.position().add(forwardVec.scale(2.0)); // Position hitbox 2 blocks ahead
                AABB hitbox = new AABB(hitboxCenter.subtract(1, 1, 1), hitboxCenter.add(1, 1, 1)); // 2x2x2 box

                // Get all entities in the hitbox, excluding the player
                List<Entity> entities = world.getEntities(user, hitbox);

                for (Entity entity : entities) {
                    if (entity instanceof LivingEntity target) {
                        // Apply damage
                        float damage = Math.clamp((float) chargeTime * 0.1F, 0.0F, 10.0F);
                        if (target.level() instanceof ServerLevel serverWorld) {
                            target.hurtServer(serverWorld,
                                    target.level().damageSources().playerAttack(player), damage);
                        }

                        // Knockback
                        Vec3 knockback = forwardVec.scale(1.5); // Apply knockback in player's direction
                        target.push(knockback.x, knockback.y, knockback.z);
                        target.syncVelocity = true;

                        // Apply a custom effect
                        if (EnchantmentListener.hasEnchantment(stack, "antique:kinematic")) {
                            target.addEffect(new MobEffectInstance(AntiqueEffects.VOLATILE_BOUNCE_EFFECT, 60, 0, true, true));
                        } else {
                            target.addEffect(new MobEffectInstance(AntiqueEffects.BOUNCE_EFFECT, 30, 0, true, true));
                        }
                    }
                }

                if (!entities.isEmpty()) world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.MACE_SMASH_AIR, SoundSource.NEUTRAL, 1F, 1.0F);
                player.getCooldowns().addCooldown(stack, 60);
                user.swing(user.getUsedItemHand(), SwingAnimation.DEFAULT, true);
            }
            return false;
        }
        return true;
    }

    @Override
    public int getUseDuration(ItemStack stack, @NotNull LivingEntity user) {
        return stack.isEnchanted() ? 72000 : 0;
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level world, Player user, @NotNull InteractionHand hand) {
        if (EnchantmentListener.hasEnchantment(user.getItemInHand(hand), "antique:kinematic")) {
            user.startUsingItem(hand);
            return InteractionResult.PASS;
        } else if (EnchantmentListener.hasEnchantment(user.getItemInHand(hand), "antique:impetus")) {
            user.startUsingItem(hand);
            return InteractionResult.PASS;
        }

        user.swing(hand, SwingAnimation.DEFAULT, true);
        HitResult hitResult = user.pick(5.0D, 0.0F, false);
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            user.getCooldowns().addCooldown(user.getItemInHand(hand), 40);
            user.push(user.getViewVector(0).multiply(new Vec3(-1.5, -0.5, -1.5)));
            user.syncVelocity = true;
            if (user.level() instanceof ServerLevel serverWorld) {
                serverWorld.sendParticles(ParticleTypes.GUST, user.getX(), user.getY() + 0.25F, user.getZ(), 1, 0.1, 0.0, 0.1, 0);
                user.addEffect(new MobEffectInstance(AntiqueEffects.VOLATILE_BOUNCE_EFFECT, 30, 0, true, true));
            }

            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.MACE_SMASH_AIR, SoundSource.NEUTRAL, 1F, 0.9f + (user.getRandom().nextFloat() * .2f));
            user.awardStat(Stats.ITEM_USED.get(this));
        } else {
            user.getCooldowns().addCooldown(user.getItemInHand(hand), 20);
        }

        return InteractionResult.PASS;
    }

    @Override
    public @NotNull ItemUseAnimation getUseAnimation(@NotNull ItemStack stack) {
        if (EnchantmentListener.hasEnchantment(stack, "antique:impetus")) {
            return ItemUseAnimation.SPEAR;
        }
        return ItemUseAnimation.NONE;
    }

    @Override
    public void hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        ScepterItem.lastHitVelocity = playerVelocity;
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, ServerLevel world, @NotNull Entity entity, @Nullable EquipmentSlot slot) {
        if (!world.isClientSide() && entity instanceof Player player) {

            // Track the start position of the tick
            if (startTickPosition == null) {
                startTickPosition = player.position();
            }

            // Calculate velocity at the end of the tick (this will be after position has changed)
            Vec3 endTickPosition = player.position();

            // Check if there has been a position change
            if (!startTickPosition.equals(endTickPosition)) {
                // Calculate velocity as the difference between start and end position
                playerVelocity = endTickPosition.subtract(startTickPosition);
            }

            // Optionally, store the velocity for later use or apply any logic
            // For example, updating player velocity based on the calculated velocity
            player.setDeltaMovement(playerVelocity);

            // Update start position for the next tick
            startTickPosition = endTickPosition;
        }
    }
}
