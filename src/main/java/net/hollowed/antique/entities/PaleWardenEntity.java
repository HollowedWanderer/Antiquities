package net.hollowed.antique.entities;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hollowed.antique.index.AntiqueItems;
import net.hollowed.antique.networking.PaleWardenTickPacketPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("all")
public class PaleWardenEntity extends PathfinderMob {

    private boolean awakened = false;
    private int mode = 0;

    public final AnimationState awakenAnimationState = new AnimationState();
    public int awakenAnimationTimeout = 0;

    public final AnimationState idleAnimationState = new AnimationState();
    public int idleAnimationTimeout = 0;

    public PaleWardenEntity(EntityType<? extends PathfinderMob> entityType, Level world) {
        super(entityType, world);
        if (Minecraft.getInstance().player != null) {
            ClientPlayNetworking.send(new PaleWardenTickPacketPayload(this.getId(), AntiqueItems.MYRIAD_PICK_HEAD.getDefaultInstance(), ItemStack.EMPTY));
        }
    }

    @Override
    public boolean isLeftHanded() {
        return false;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 24.0F));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    public void swapStacks(ItemStack mainhand, ItemStack offhand) {
        if (this.level() instanceof ServerLevel) {
            // Set the stacks on the server
            this.setItemInHand(InteractionHand.MAIN_HAND, mainhand);
            this.setItemInHand(InteractionHand.OFF_HAND, offhand);

            // Swap the stacks
            ItemStack tempStack = this.getItemInHand(InteractionHand.MAIN_HAND).copy();
            this.setItemInHand(InteractionHand.MAIN_HAND, this.getItemInHand(InteractionHand.OFF_HAND));
            this.setItemInHand(InteractionHand.OFF_HAND, tempStack);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (Minecraft.getInstance().player != null && this.awakenAnimationTimeout == 40) {
            ItemStack mainhand = this.getItemInHand(InteractionHand.MAIN_HAND);
            ItemStack offhand = this.getItemInHand(InteractionHand.OFF_HAND);

            ClientPlayNetworking.send(new PaleWardenTickPacketPayload(this.getId(), mainhand, offhand));
        }

        if (this.level().isClientSide()) {
            if (this.awakenAnimationTimeout == 80) {
                this.idleAnimationTimeout = 0;
                this.idleAnimationState.stop();
                this.awakenAnimationState.start(this.tickCount);
            }
            if (this.awakenAnimationTimeout >= 0) {
                --this.awakenAnimationTimeout;
            } else {
                this.awakenAnimationState.stop();
            }
            if (this.idleAnimationTimeout == 60) {
                this.idleAnimationState.start(this.tickCount);
            }
            if (this.idleAnimationTimeout >= 0) {
                --this.idleAnimationTimeout;
            } else if (!(this.awakenAnimationTimeout >= 0)) {
                this.idleAnimationTimeout = 60;
            }
        }
    }

    @Override
    public boolean hurtClient(DamageSource source) {
        if (this.awakenAnimationTimeout <= 0) {
            this.awakenAnimationTimeout = 80;
        }
        return super.hurtClient(source);
    }

    @Override
    public boolean canBeCollidedWith(@Nullable Entity entity) {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected float getWaterSlowDown() {
        return 0;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    @Override
    public boolean canFreeze() {
        return false;
    }

    public boolean isInvulnerableTo(ServerLevel world, DamageSource source) {
        if (source.is(DamageTypeTags.IS_PROJECTILE)
                || source.is(DamageTypeTags.IS_DROWNING)
                || source.is(DamageTypeTags.IS_EXPLOSION)
                || source.is(DamageTypeTags.IS_FALL)
                || source.is(DamageTypeTags.IS_FIRE)
                || source.is(DamageTypeTags.IS_FREEZING)
                || source.is(DamageTypeTags.IS_LIGHTNING)
                || source.is(DamageTypes.MAGIC)
                || source.is(DamageTypes.INDIRECT_MAGIC)) {
            return true;
        }
        return this.isInvulnerableToBase(source) || EnchantmentHelper.isImmuneToDamage(world, this, source);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.STEP_HEIGHT, 1.0)
                .add(Attributes.ARMOR, 20.0)
                .add(Attributes.ARMOR_TOUGHNESS, 8.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.ATTACK_KNOCKBACK, 2.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.MAX_HEALTH, 180.0)
                .add(Attributes.FALL_DAMAGE_MULTIPLIER, 0)
                .add(Attributes.MOVEMENT_SPEED, 1);
    }
}
