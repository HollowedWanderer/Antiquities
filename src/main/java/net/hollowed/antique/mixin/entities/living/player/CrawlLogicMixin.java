package net.hollowed.antique.mixin.entities.living.player;

import net.hollowed.antique.config.AntiquitiesConfig;
import net.hollowed.antique.enchantments.EnchantmentListener;
import net.hollowed.antique.util.delay.TickDelayScheduler;
import net.hollowed.antique.util.interfaces.duck.Crawl;
import net.hollowed.combatamenities.util.delay.ClientTickDelayScheduler;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
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
    private static final EntityDataAccessor<Boolean> SLIDING =
            SynchedEntityData.defineId(CrawlLogicMixin.class, EntityDataSerializers.BOOLEAN);

    @Unique
    private static final EntityDataAccessor<Boolean> CAN_POUNCE =
            SynchedEntityData.defineId(CrawlLogicMixin.class, EntityDataSerializers.BOOLEAN);

    @Unique
    private int slideCooldown;

    @Unique
    private int slideTicks;

    @Inject(method = "defineSynchedData", at = @At("HEAD"))
    protected void initDataTracker(SynchedEntityData.Builder entityData, CallbackInfo ci) {
        entityData.define(CRAWLING, false);
        entityData.define(SLIDING, false);
        entityData.define(CAN_POUNCE, false);
    }

    @Override
    public void antique$setCrawl(boolean crawling) {
        this.entityData.set(CRAWLING, crawling);
    }

    @Override
    public boolean antique$canSlidePounce() {
        return this.entityData.get(CAN_POUNCE);
    }

    @Override
    public void antique$setCrawlStartClient(int time) {
        if (this.slideCooldown <= 0) {
            ClientTickDelayScheduler.schedule(AntiquitiesConfig.SLIDE_START_DELAY, () -> this.entityData.set(SLIDING, true));
            ClientTickDelayScheduler.schedule(time - AntiquitiesConfig.SLIDE_POUNCE_WINDOW, () -> this.entityData.set(CAN_POUNCE, true));
            this.slideTicks = time;
            this.slideCooldown = time + 20;
        }
    }

    @Override
    public void antique$setCrawlStart(int time) {
        if (this.slideCooldown <= 0) {
            TickDelayScheduler.schedule(AntiquitiesConfig.SLIDE_START_DELAY, () -> this.entityData.set(SLIDING, true));
            TickDelayScheduler.schedule(time - AntiquitiesConfig.SLIDE_POUNCE_WINDOW, () -> this.entityData.set(CAN_POUNCE, true));
            this.slideTicks = time;
            this.slideCooldown = time + 20;
        }
    }

    @Inject(method = "isSwimming", at = @At("RETURN"), cancellable = true)
    public void swimming(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(cir.getReturnValue() || this.entityData.get(CRAWLING));
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void stopCrawling(CallbackInfo ci) {
        if (!this.onGround()) {
            this.slideTicks = 0;
            if (this.entityData.get(CAN_POUNCE)) this.entityData.set(CAN_POUNCE, false);
            if (this.entityData.get(CRAWLING)) this.entityData.set(CRAWLING, false);
            if (this.entityData.get(SLIDING)) this.entityData.set(SLIDING, false);
        }
    }

    @Inject(method = "aiStep", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        if (this.slideTicks <= 0) {
            this.entityData.set(SLIDING, false);
        }
        if (this.slideTicks <= 1) {
            this.entityData.set(CAN_POUNCE, false);
        }

        if (this.entityData.get(SLIDING) && this.isSwimming()) {
            this.push(this.getViewVector(0).horizontal().normalize().scale(this.onGround() ? EnchantmentListener.hasEnchantment(this.getItemBySlot(EquipmentSlot.LEGS), "minecraft:swift_sneak") ? 0.55 : AntiquitiesConfig.SLIDE_POWER : 0));

            this.syncVelocity = true;
            this.needsSync = true;
        }

        if (this.slideTicks > 0) {
            this.slideTicks--;
        }

        if (this.slideCooldown > 0) {
            this.slideCooldown--;
        }
    }

    @Override
    public void jumpFromGround() {
        if (this.entityData.get(SLIDING) || this.slideTicks > 0) {
            this.setSprinting(true);
            float jumpPower = this.getJumpPower() + AntiquitiesConfig.SLIDE_POUNCE_VERTICAL_POWER;
            if (this.antique$canSlidePounce()) {
                float angle = this.getYRot() * (float) (Math.PI / 180.0);
                this.addDeltaMovement(new Vec3(-Mth.sin(angle) * AntiquitiesConfig.SLIDE_POUNCE_HORIZONTAL_POWER, jumpPower, Mth.cos(angle) * AntiquitiesConfig.SLIDE_POUNCE_HORIZONTAL_POWER));
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GOAT_LONG_JUMP, SoundSource.PLAYERS, 1.0F, 1.0F);
                this.needsSync = true;
                this.syncVelocity = true;
                this.slideTicks = 0;
                this.entityData.set(SLIDING, false);
                this.entityData.set(CAN_POUNCE, false);
                this.entityData.set(CRAWLING, false);
                return;
            }
        }

        super.jumpFromGround();
    }
}
