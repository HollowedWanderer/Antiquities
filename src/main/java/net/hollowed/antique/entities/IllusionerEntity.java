//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.hollowed.antique.entities;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.hollowed.antique.index.AntiqueEntities;
import net.hollowed.antique.index.AntiqueItems;
import net.hollowed.antique.networking.IllusionerParticlePacketPayload;
import net.hollowed.antique.util.FireworkUtil;
import net.hollowed.antique.util.delay.TickDelayScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
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
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class IllusionerEntity extends SpellcasterIllager implements RangedAttackMob {
    public static final EntityDataAccessor<Vector3fc> SPELL_COLOR = SynchedEntityData.defineId(IllusionerEntity.class, EntityDataSerializers.VECTOR3);
    private int mirrorSpellTimer;
    private final Vec3[][] mirrorCopyOffsets;

    @SuppressWarnings("all")
    public IllusionerEntity(EntityType<? extends IllusionerEntity> entityType, Level world) {
        super(entityType, world);
        this.xpReward = 5;
        this.mirrorCopyOffsets = new Vec3[2][4];

        for(int i = 0; i < 4; ++i) {
            this.mirrorCopyOffsets[0][i] = Vec3.ZERO;
            this.mirrorCopyOffsets[1][i] = Vec3.ZERO;
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SPELL_COLOR, new Vector3f(0, 0, 0));
    }

    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SpellcasterIllager.SpellcasterCastingSpellGoal());
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Creaking.class, 8.0F, 1.0, 1.2));
        this.goalSelector.addGoal(3, new IllusionerCloneEntity.TeleportGoal(this));
        this.goalSelector.addGoal(4, new SmokeBombGoal());
        this.goalSelector.addGoal(5, new GiveInvisibilityGoal());
        this.goalSelector.addGoal(6, new RangedBowAttackGoal<>(this, 0.5, 50, 15.0F));
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this, Raider.class)).setAlertOthers());
        this.targetSelector.addGoal(2, (new NearestAttackableTargetGoal<>(this, Player.class, true)).setUnseenMemoryTicks(300));
        this.targetSelector.addGoal(3, (new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false)).setUnseenMemoryTicks(300));
        this.targetSelector.addGoal(3, (new NearestAttackableTargetGoal<>(this, IronGolem.class, false)).setUnseenMemoryTicks(300));
    }

    public static AttributeSupplier.Builder createIllusionerAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.5)
                .add(Attributes.FOLLOW_RANGE, 36.0)
                .add(Attributes.GRAVITY, 0.05)
                .add(Attributes.SAFE_FALL_DISTANCE, 8)
                .add(Attributes.MAX_HEALTH, 32.0);
    }

    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor world, @NotNull DifficultyInstance difficulty, @NotNull EntitySpawnReason spawnReason, @Nullable SpawnGroupData entityData) {
        ItemStack bow = Items.BOW.getDefaultInstance();
        bow.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(ItemStackTemplate.fromStack(Items.FIREWORK_ROCKET.getDefaultInstance())));
        this.setItemSlot(EquipmentSlot.MAINHAND, bow);
        return super.finalizeSpawn(world, difficulty, spawnReason, entityData);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide() && this.isCastingSpell()) {
            float i = this.yBodyRot * 0.017453292F + Mth.cos((float)this.tickCount * 0.6662F) * 0.25F;
            float j = Mth.cos(i);
            float k = Mth.sin(i);
            double d = 0.6 * (double)this.getScale();
            double e = 1.8 * (double)this.getScale();
            this.level().addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, this.entityData.get(SPELL_COLOR).x(), this.entityData.get(SPELL_COLOR).y(), this.entityData.get(SPELL_COLOR).z()), this.getX() + (double)j * d, this.getY() + e, this.getZ() + (double)k * d, 0.0, 0.0, 0.0);
            this.level().addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, this.entityData.get(SPELL_COLOR).x(), this.entityData.get(SPELL_COLOR).y(), this.entityData.get(SPELL_COLOR).z()), this.getX() - (double)j * d, this.getY() + e, this.getZ() - (double)k * d, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public boolean canBeCollidedWith(@org.jspecify.annotations.Nullable Entity entity) {
        return super.canBeCollidedWith(entity);
    }

    @Override
    protected void actuallyHurt(@NotNull ServerLevel world, @NotNull DamageSource source, float amount) {
        IllusionerCloneEntity clone = new IllusionerCloneEntity(AntiqueEntities.ILLUSIONER_CLONE, this.level());
        clone.setItemSlot(EquipmentSlot.MAINHAND, Items.BOW.getDefaultInstance());
        clone.setPos(this.getX(), this.getY(), this.getZ());
        clone.setOwner(this);
        clone.setTarget(this.getTarget());
        clone.setYBodyRot(this.getVisualRotationYInDegrees());
        clone.setYHeadRot(this.getYHeadRot());
        if (IllusionerCloneEntity.teleportRandomly(this)) {
            this.level().addFreshEntity(clone);
        }
        super.actuallyHurt(world, source, amount);
    }

    public void aiStep() {
        super.aiStep();

        if (this.getDeltaMovement().y < -0.2) {
            this.level().addParticle(ParticleTypes.POOF, this.getRandomX(0.5), this.getY(), this.getZ(0.5), 0.0, 0.0, 0.0);
        }
        if (this.isInvisible()) {
            this.level().addParticle(ParticleTypes.LARGE_SMOKE, this.getRandomX(0.1), this.getY() + 1.5, this.getZ(0.1), 0.0, 0.0, 0.0);
        }

        if (this.level().isClientSide() && this.isInvisible()) {
            --this.mirrorSpellTimer;
            if (this.mirrorSpellTimer < 0) {
                this.mirrorSpellTimer = 0;
            }

            if (this.hurtTime != 1 && this.tickCount % 1200 != 0) {
                if (this.hurtTime == this.hurtDuration - 1) {
                    this.mirrorSpellTimer = 3;

                    for(int k = 0; k < 4; ++k) {
                        this.mirrorCopyOffsets[0][k] = this.mirrorCopyOffsets[1][k];
                        this.mirrorCopyOffsets[1][k] = new Vec3(0.0, 0.0, 0.0);
                    }
                }
            } else {
                this.mirrorSpellTimer = 3;

                int j;
                for(j = 0; j < 4; ++j) {
                    this.mirrorCopyOffsets[0][j] = this.mirrorCopyOffsets[1][j];
                    this.mirrorCopyOffsets[1][j] = new Vec3((double)(-6.0F + (float)this.random.nextInt(13)) * 0.5, Math.max(0, this.random.nextInt(6) - 4), (double)(-6.0F + (float)this.random.nextInt(13)) * 0.5);
                }

                for(j = 0; j < 16; ++j) {
                    this.level().addParticle(ParticleTypes.CLOUD, this.getRandomX(0.5), this.getRandomY(), this.getZ(0.5), 0.0, 0.0, 0.0);
                }

                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ILLUSIONER_MIRROR_MOVE, this.getSoundSource(), 1.0F, 1.0F, false);
            }
        }

    }

    public @NotNull SoundEvent getCelebrateSound() {
        return SoundEvents.ILLUSIONER_AMBIENT;
    }

    public Vec3[] getMirrorCopyOffsets(float tickProgress) {
        if (this.mirrorSpellTimer <= 0) {
            return this.mirrorCopyOffsets[1];
        } else {
            double d = ((float)this.mirrorSpellTimer - tickProgress) / 3.0F;
            d = Math.pow(d, 0.25);
            Vec3[] vec3ds = new Vec3[4];

            for(int i = 0; i < 4; ++i) {
                vec3ds[i] = this.mirrorCopyOffsets[1][i].scale(1.0 - d).add(this.mirrorCopyOffsets[0][i].scale(d));
            }

            return vec3ds;
        }
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

    public void performRangedAttack(@NotNull LivingEntity target, float pullProgress) {
        Level world = this.level();
        ItemStack stack = Items.FIREWORK_ROCKET.getDefaultInstance();
        stack.set(DataComponents.FIREWORKS, FireworkUtil.randomFirework());
        if (world instanceof ServerLevel serverWorld) {
            FireworkRocketEntity projectile = new FireworkRocketEntity(world, stack, this.getX(), this.getY() + 1, this.getZ(), true);
            Vec3 direction = this.position().subtract(target.position());
            projectile.setDeltaMovement(direction.normalize().scale(-1.75));
            projectile.setOwner(this);
            projectile.hurtMarked = true;
            serverWorld.addFreshEntity(projectile);
        }
        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.playSound(SoundEvents.FIREWORK_ROCKET_LAUNCH, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
    }

    public AbstractIllager.@NotNull IllagerArmPose getArmPose() {
        if (this.isCastingSpell()) {
            return IllagerArmPose.SPELLCASTING;
        } else {
            return this.isAggressive() ? IllagerArmPose.BOW_AND_ARROW : IllagerArmPose.CROSSED;
        }
    }

    private class GiveInvisibilityGoal extends SpellcasterIllager.SpellcasterUseSpellGoal {
        GiveInvisibilityGoal() {
            super();
        }

        public boolean canUse() {
            if (!super.canUse()) {
                return false;
            } else {
                return !IllusionerEntity.this.hasEffect(MobEffects.INVISIBILITY);
            }
        }

        protected int getCastingTime() {
            return 80;
        }

        protected int getCastingInterval() {
            return 340;
        }

        protected void performSpellCasting() {
            IllusionerEntity.this.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 300), IllusionerEntity.this);

            int count = 0;
            switch (IllusionerEntity.this.level().getDifficulty()) {
                case Difficulty.EASY -> count = 2;
                case Difficulty.NORMAL -> count = 4;
                case Difficulty.HARD -> count = 6;
            }

            for (int i = 0; i < count; i++) {
                for (int tries = 0; tries < 10; tries++) {
                    double offsetX = (IllusionerEntity.this.getRandom().nextDouble() - 0.5) * 12.0;
                    double offsetZ = (IllusionerEntity.this.getRandom().nextDouble() - 0.5) * 12.0;
                    double x = IllusionerEntity.this.getX() + offsetX;
                    double z = IllusionerEntity.this.getZ() + offsetZ;

                    double originY = IllusionerEntity.this.getY();
                    int bestY = -1;
                    double bestDistance = Double.MAX_VALUE;

                    for (int yOffset = -15; yOffset <= 15; yOffset++) {
                        int testY = (int) (originY + yOffset);
                        if (!IllusionerEntity.this.level().isOutsideBuildHeight(testY)) {
                            if (IllusionerEntity.this.level().getBlockState(new BlockPos((int)x, testY, (int)z)).isAir()) {
                                double distance = Math.abs(originY - testY);
                                if (distance < bestDistance) {
                                    bestY = testY;
                                    bestDistance = distance;
                                }
                            }
                        }
                    }

                    if (bestY != -1) {
                        IllusionerCloneEntity clone = new IllusionerCloneEntity(AntiqueEntities.ILLUSIONER_CLONE, IllusionerEntity.this.level());
                        clone.setItemSlot(EquipmentSlot.MAINHAND, Items.BOW.getDefaultInstance());
                        clone.setPos(x, bestY, z);
                        clone.setOwner(IllusionerEntity.this);
                        clone.setTarget(IllusionerEntity.this.getTarget());
                        IllusionerEntity.this.level().addFreshEntity(clone);

                        ServerLevel serverWorld = (ServerLevel) clone.level();
                        for (ServerPlayer player : serverWorld.players()) {
                            ServerPlayNetworking.send(player, new IllusionerParticlePacketPayload(
                                    x, bestY + 1 + ((Math.random() - 0.5) * 2), z
                            ));
                        }

                        break;
                    }
                }
            }
        }

        @Override
        public void tick() {
            super.tick();
            IllusionerEntity.this.getEntityData().set(SPELL_COLOR, new Vector3f(103 / 255.0F, 68 / 255.0F, 99 / 255.0F));
        }

        @Nullable
        protected SoundEvent getSpellPrepareSound() {
            return SoundEvents.ILLUSIONER_PREPARE_MIRROR;
        }

        protected SpellcasterIllager.@NotNull IllagerSpell getSpell() {
            return IllagerSpell.BLINDNESS;
        }
    }

    private class SmokeBombGoal extends SpellcasterIllager.SpellcasterUseSpellGoal {
        private int targetId;

        SmokeBombGoal() {
            super();
        }

        public boolean canUse() {
            if (!super.canUse()) {
                return false;
            } else if (IllusionerEntity.this.getTarget() == null) {
                return false;
            } else if (IllusionerEntity.this.getTarget().getId() == this.targetId) {
                return false;
            } else {
                return IllusionerEntity.this.level().getDifficulty().getId() >= 2;
            }
        }

        public void start() {
            super.start();
            LivingEntity livingEntity = IllusionerEntity.this.getTarget();
            if (livingEntity != null) {
                this.targetId = livingEntity.getId();
            }

        }

        protected int getCastingTime() {
            return 60;
        }

        protected int getCastingInterval() {
            return 300;
        }

        protected void performSpellCasting() {
            for (int i = 0; i < 3; i++) {
                TickDelayScheduler.schedule(random.nextIntBetweenInclusive(0, 10), () -> {
                    ItemStack stack = AntiqueItems.SMOKE_BOMB.getDefaultInstance();
                    stack.set(DataComponents.FIREWORKS, FireworkUtil.randomFireworkBall());

                    SmokeBombEntity smokeBomb = new SmokeBombEntity(AntiqueEntities.SMOKE_BOMB, IllusionerEntity.this.level());
                    smokeBomb.setPos(IllusionerEntity.this.position().add((Math.random() - 0.5) * 2, 3, (Math.random() - 0.5) * 2));
                    smokeBomb.setDeltaMovement((Math.random() - 0.5) * 0.8, 0.25, (Math.random() - 0.5) * 0.8);
                    smokeBomb.setItem(stack);
                    IllusionerEntity.this.level().addFreshEntity(smokeBomb);
                    IllusionerEntity.this.level().playSound(null, IllusionerEntity.this.getX(), IllusionerEntity.this.getY(), IllusionerEntity.this.getZ(), SoundEvents.WITCH_THROW, SoundSource.BLOCKS, 1F, 1F);
                });
            }
        }

        @Override
        public void tick() {
            super.tick();
            IllusionerEntity.this.getEntityData().set(SPELL_COLOR, new Vector3f(1, 55 / 255.0F, 130 / 255.0F));
        }

        protected SoundEvent getSpellPrepareSound() {
            return SoundEvents.ILLUSIONER_PREPARE_BLINDNESS;
        }

        protected SpellcasterIllager.@NotNull IllagerSpell getSpell() {
            return IllagerSpell.BLINDNESS;
        }
    }
}
