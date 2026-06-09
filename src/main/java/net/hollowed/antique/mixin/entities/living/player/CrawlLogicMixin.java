package net.hollowed.antique.mixin.entities.living.player;

import net.hollowed.antique.enchantments.EnchantmentListener;
import net.hollowed.antique.util.interfaces.duck.Crawl;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class CrawlLogicMixin extends LivingEntity implements Crawl {

    @Shadow public abstract boolean isSwimming();

    protected CrawlLogicMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Unique
    private static final EntityDataAccessor<Boolean> CRAWLING =
            SynchedEntityData.defineId(CrawlLogicMixin.class, EntityDataSerializers.BOOLEAN);
    @Unique
    private static final EntityDataAccessor<Integer> SLIDE_TIMER =
            SynchedEntityData.defineId(CrawlLogicMixin.class, EntityDataSerializers.INT);
    @Unique
    private static final EntityDataAccessor<Integer> SLIDE_COOLDOWN =
            SynchedEntityData.defineId(CrawlLogicMixin.class, EntityDataSerializers.INT);

    @Inject(method = "defineSynchedData", at = @At("HEAD"))
    protected void initDataTracker(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(CRAWLING, false);
        builder.define(SLIDE_TIMER, 0);
        builder.define(SLIDE_COOLDOWN, 0);
    }

    @Override
    public void antique$setCrawl(boolean crawling) {
        this.entityData.set(CRAWLING, crawling);
    }

    @Override
    public void antique$setCrawlStart(int time) {
        if (this.entityData.get(SLIDE_COOLDOWN) <= 0) {
            this.entityData.set(SLIDE_TIMER, time);
            this.entityData.set(SLIDE_COOLDOWN, time + 20);
        }
    }

    @Inject(method = "isSwimming", at = @At("RETURN"), cancellable = true)
    public void swimming(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(cir.getReturnValue() || this.entityData.get(CRAWLING));
    }

    @Inject(method = "aiStep", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        if (this.entityData.get(SLIDE_TIMER) > 0 && this.isSwimming()) {
            if (this.entityData.get(SLIDE_TIMER) < 9) {
                this.push(this.getViewVector(0).horizontal().normalize().scale(this.onGround() ? EnchantmentListener.hasEnchantment(this.getItemBySlot(EquipmentSlot.LEGS), "minecraft:swift_sneak") ? 0.55 : 0.3 : 0));
                if (this.getDeltaMovement().length() < 0.2) {
                    this.entityData.set(SLIDE_TIMER, 0);
                }
            } else {
                this.push(this.getViewVector(0).horizontal().normalize().scale(this.onGround() ? 0.05 : 0));
            }

            this.hurtMarked = true;
            this.needsSync = true;
        }

        if (this.entityData.get(SLIDE_TIMER) > 0) {
            int newTime = this.entityData.get(SLIDE_TIMER) - 1;
            this.entityData.set(SLIDE_TIMER, newTime);
        }

        if (this.entityData.get(SLIDE_COOLDOWN) > 0) {
            int newTime = this.entityData.get(SLIDE_COOLDOWN) - 1;
            this.entityData.set(SLIDE_COOLDOWN, newTime);
        }
    }

    @Override
    public void jumpFromGround() {
        super.jumpFromGround();
        if (this.entityData.get(SLIDE_TIMER) > 0 && this.entityData.get(SLIDE_TIMER) < 4 && this.entityData.get(CRAWLING)) {
            this.entityData.set(CRAWLING, false);
            this.push(this.getViewVector(0).horizontal().normalize().scale(0.25).add(0, 0.2, 0));
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GOAT_LONG_JUMP, SoundSource.PLAYERS, 1.0F, 1.0F);
            this.hurtMarked = true;
            this.needsSync = true;
            this.entityData.set(SLIDE_TIMER, 0);
        }
    }
}
