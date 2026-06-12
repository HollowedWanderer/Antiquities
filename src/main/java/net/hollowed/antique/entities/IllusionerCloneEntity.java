//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.hollowed.antique.entities;

import net.hollowed.antique.util.interfaces.duck.SpellTicksExtension;
import net.hollowed.antique.util.delay.TickDelayScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.creaking.Creaking;
import net.minecraft.world.entity.monster.illager.AbstractIllager;
import net.minecraft.world.entity.monster.illager.SpellcasterIllager;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Objects;
import java.util.UUID;

public class IllusionerCloneEntity extends SpellcasterIllager implements RangedAttackMob, TraceableEntity {

    @Nullable
    private UUID ownerUuid;
    @Nullable
    private LivingEntity owner;

    @SuppressWarnings("all")
    public IllusionerCloneEntity(EntityType<? extends IllusionerCloneEntity> entityType, Level world) {
        super(entityType, world);
        this.xpReward = 5;
    }

    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SpellcasterIllager.SpellcasterCastingSpellGoal());
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Creaking.class, 8.0F, 1.0, 1.2));
        this.goalSelector.addGoal(3, new TeleportGoal(this));
        this.goalSelector.addGoal(6, new RangedBowAttackGoal<>(this, 0.5, 60, 15.0F));
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this, Raider.class)).setAlertOthers());
        this.targetSelector.addGoal(2, (new NearestAttackableTargetGoal<>(this, Player.class, true)).setUnseenMemoryTicks(300));
        this.targetSelector.addGoal(3, (new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false)).setUnseenMemoryTicks(300));
        this.targetSelector.addGoal(3, (new NearestAttackableTargetGoal<>(this, IronGolem.class, false)).setUnseenMemoryTicks(300));
    }

    @Override
    protected void addAdditionalSaveData(@NotNull ValueOutput view) {
        super.addAdditionalSaveData(view);
        view.storeNullable("Owner", UUIDUtil.CODEC, this.ownerUuid);
    }

    @Override
    protected void readAdditionalSaveData(@NotNull ValueInput view) {
        super.readAdditionalSaveData(view);
        this.setOwner(view.read("Owner", UUIDUtil.CODEC).orElse(null));
    }

    public static AttributeSupplier.Builder createIllusionerAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED, 0.5).add(Attributes.FOLLOW_RANGE, 18.0).add(Attributes.MAX_HEALTH, 1.0);
    }

    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor world, @NotNull DifficultyInstance difficulty, @NotNull EntitySpawnReason spawnReason, @Nullable SpawnGroupData entityData) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        return super.finalizeSpawn(world, difficulty, spawnReason, entityData);
    }

    public @NotNull SoundEvent getCelebrateSound() {
        return SoundEvents.ILLUSIONER_AMBIENT;
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.ILLUSIONER_AMBIENT;
    }

    protected @NotNull SoundEvent getDeathSound() {
        return SoundEvents.ILLUSIONER_DEATH;
    }

    protected @NotNull SoundEvent getHurtSound(@NotNull DamageSource source) {
        return SoundEvents.ILLUSIONER_HURT;
    }

    protected @NotNull SoundEvent getCastingSoundEvent() {
        return SoundEvents.ILLUSIONER_CAST_SPELL;
    }

    public void applyRaidBuffs(@NotNull ServerLevel world, int wave, boolean unused) {
    }

    @Override
    public boolean isInvulnerableTo(@NotNull ServerLevel world, DamageSource source) {
        if (source.is(DamageTypes.FIREWORKS)) return true;
        return super.isInvulnerableTo(world, source);
    }

    @Override
    public void die(@NotNull DamageSource damageSource) {
        for (int j = 0; j < 24; ++j) {
            this.level().addParticle(ParticleTypes.CLOUD, this.getX() + Math.random() - 0.5, this.getRandomY(), this.getZ() + Math.random() - 0.5, 0.0, 0.1, 0.0);
        }
        this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ILLUSIONER_MIRROR_MOVE, this.getSoundSource(), 1.0F, 1.0F, false);
        TickDelayScheduler.schedule(1, this::discard);
        super.die(damageSource);
    }

    public void setOwner(@Nullable LivingEntity entity) {
        if (entity != null) {
            this.ownerUuid = entity.getUUID();
            this.owner = entity;
        }
    }

    protected void setOwner(@Nullable UUID ownerUuid) {
        if (!Objects.equals(this.ownerUuid, ownerUuid)) {
            this.ownerUuid = ownerUuid;
            this.owner = ownerUuid != null ? this.getEntity(ownerUuid) : null;
        }
    }

    @Nullable
    public Entity getOwner() {
        if (this.owner != null && !this.owner.isRemoved()) {
            return this.owner;
        } else if (this.ownerUuid != null) {
            this.owner = this.getEntity(this.ownerUuid);
            return this.owner;
        } else {
            return null;
        }
    }

    @Nullable
    protected LivingEntity getEntity(UUID uuid) {
        Level var3 = this.level();
        if (var3 instanceof ServerLevel serverWorld && serverWorld.getEntity(uuid) instanceof LivingEntity) {
            return (LivingEntity) serverWorld.getEntity(uuid);
        } else {
            return null;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getOwner() == null || !this.getOwner().isAlive()) {
            if (this.level() instanceof ServerLevel world) {
                this.kill(world);
            }
        }
    }

    public void performRangedAttack(LivingEntity target, float pullProgress) {
        ItemStack itemStack = this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, Items.BOW));
        ItemStack itemStack2 = this.getProjectile(itemStack);
        AbstractArrow persistentProjectileEntity = ProjectileUtil.getMobArrow(this, itemStack2, pullProgress, itemStack);
        double d = target.getX() - this.getX();
        double e = target.getY(0.3333333333333333) - persistentProjectileEntity.getY();
        double f = target.getZ() - this.getZ();
        double g = Math.sqrt(d * d + f * f);
        Level var15 = this.level();
        if (var15 instanceof ServerLevel serverWorld) {
            Projectile.spawnProjectileUsingShoot(persistentProjectileEntity, serverWorld, itemStack2, d, e + g * 0.20000000298023224, f, 1.6F, (float)(14 - serverWorld.getDifficulty().getId() * 4));
        }

        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        if (this.level() instanceof ServerLevel serverWorld) {
            this.kill(serverWorld);
        }
    }

    public AbstractIllager.@NotNull IllagerArmPose getArmPose() {
        if (this.isCastingSpell()) {
            return IllagerArmPose.SPELLCASTING;
        } else {
            return this.isAggressive() ? IllagerArmPose.BOW_AND_ARROW : IllagerArmPose.CROSSED;
        }
    }

    public static boolean teleportRandomly(LivingEntity entity) {
        if (entity.isAlive()) {
            for (int i = 0; i < 10; i++) {
                double d = entity.getX() + (entity.getRandom().nextDouble() - 0.5) * 16.0;
                double e = entity.getY() + (double) (entity.getRandom().nextInt(16) - 8);
                double f = entity.getZ() + (entity.getRandom().nextDouble() - 0.5) * 16.0;
                if (teleportTo(d, e, f, entity)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    private static boolean teleportTo(double x, double y, double z, LivingEntity entity) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(x, y, z);

        while(mutable.getY() > entity.level().getMinY() && !entity.level().getBlockState(mutable).blocksMotion()) {
            mutable.move(Direction.DOWN);
        }

        BlockState blockState = entity.level().getBlockState(mutable);
        boolean bl = blockState.blocksMotion();
        boolean bl2 = blockState.getFluidState().is(FluidTags.WATER);
        if (bl && !bl2) {
            Vec3 vec3d = entity.position();
            boolean bl3 = entity.randomTeleport(x, y, z, true);
            if (bl3) {
                entity.level().gameEvent(GameEvent.TELEPORT, vec3d, GameEvent.Context.of(entity));
                if (!entity.isSilent()) {
                    entity.level().playSound(null, entity.xo, entity.yo, entity.zo, SoundEvents.ENDERMAN_TELEPORT, entity.getSoundSource(), 1.0F, 1.0F);
                    entity.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
                }
            }

            return bl3;
        } else {
            return false;
        }
    }

    public static class TeleportGoal extends Goal {
        protected int spellCooldown;
        protected int startTime;
        protected SpellcasterIllager entity;

        public TeleportGoal(SpellcasterIllager entity) {
            this.entity = entity;
        }

        public boolean canUse() {
            LivingEntity livingEntity = entity.getTarget();
            if (livingEntity != null && livingEntity.isAlive()) {
                if (entity.isCastingSpell()) {
                    return false;
                } else {
                    return entity.tickCount >= this.startTime;
                }
            } else {
                return false;
            }
        }

        public boolean canContinueToUse() {
            LivingEntity livingEntity = entity.getTarget();
            return livingEntity != null && livingEntity.isAlive() && this.spellCooldown > 0;
        }

        public void start() {
            this.spellCooldown = this.adjustedTickDelay(this.getInitialCooldown());
            if (entity instanceof SpellTicksExtension access) {
                access.antiquities$setSpellTicks(this.getSpellTicks());
            }
            this.startTime = entity.tickCount + this.startTimeDelay();
            SoundEvent soundEvent = this.getSoundPrepare();
            if (soundEvent != null) {
                entity.playSound(soundEvent, 1.0F, 1.0F);
            }

            entity.setIsCastingSpell(this.getSpell());
        }

        public void tick() {
            if (entity instanceof IllusionerEntity illusionerEntity) {
                illusionerEntity.getEntityData().set(IllusionerEntity.SPELL_COLOR, new Vector3f(0.3F, 0.3F, 0.8F));
            }
            --this.spellCooldown;
            if (this.spellCooldown == 0) {
                this.castSpell();
                entity.playSound(SoundEvents.ILLUSIONER_MIRROR_MOVE, 1.0F, 1.0F);
            }

        }

        protected void castSpell() {
            teleportRandomly(entity);
        }

        protected int getInitialCooldown() {
            return 20;
        }

        protected int getSpellTicks() {
            return 20;
        }

        protected int startTimeDelay() {
            return 200;
        }

        @Nullable
        protected SoundEvent getSoundPrepare() {
            return null;
        }

        protected IllagerSpell getSpell() {
            return IllagerSpell.DISAPPEAR;
        }
    }
}
