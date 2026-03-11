package net.hollowed.antique.entities;

import com.google.common.base.Suppliers;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.hollowed.antique.Antiquities;
import net.hollowed.antique.index.AntiqueDataComponentTypes;
import net.hollowed.antique.index.AntiqueDamageTypes;
import net.hollowed.antique.index.AntiqueEntities;
import net.hollowed.antique.entities.parts.MyriadShovelPart;
import net.hollowed.antique.index.AntiqueTrackedData;
import net.hollowed.antique.items.components.MyriadToolComponent;
import net.hollowed.antique.util.interfaces.duck.ClothAccess;
import net.hollowed.combatamenities.index.CAParticles;
import net.hollowed.combatamenities.util.items.CAComponents;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MyriadShovelEntity extends AbstractArrow {
	public static final EntityDataAccessor<Byte> LOYALTY = SynchedEntityData.defineId(MyriadShovelEntity.class, EntityDataSerializers.BYTE);
	public static final EntityDataAccessor<MyriadToolComponent> ATTRIBUTES = SynchedEntityData.defineId(MyriadShovelEntity.class, AntiqueTrackedData.MYRIAD_ATTRIBUTES);
	public static final EntityDataAccessor<Boolean> ENCHANTED = SynchedEntityData.defineId(MyriadShovelEntity.class, EntityDataSerializers.BOOLEAN);
	public static final EntityDataAccessor<Byte> PIERCE_LEVEL = SynchedEntityData.defineId(MyriadShovelEntity.class, EntityDataSerializers.BYTE);
	public static final EntityDataAccessor<Boolean> GLOW = SynchedEntityData.defineId(MyriadShovelEntity.class, EntityDataSerializers.BOOLEAN);
    private boolean dealtDamage;
	public int returnTimer;
	@Nullable
	private IntOpenHashSet piercedEntities;

	public boolean canPickup;

	public MyriadShovelEntity(EntityType<@NotNull MyriadShovelEntity> entityType, Level world) {
		super(entityType, world);
		this.setBaseDamage(8);
		this.setPickupItemStack(Antiquities.getMyriadShovelStack());
		this.entityData.set(ATTRIBUTES, this.getPickupItemStackOrigin().getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, Antiquities.getDefaultMyriadTool()));
		this.entityData.set(GLOW, this.getPickupItemStackOrigin().getOrDefault(CAComponents.BOOLEAN_PROPERTY, false));
		this.setPierceLevel((byte) 5);
	}

	public MyriadShovelEntity(Level world, LivingEntity owner, ItemStack stack) {
		super(AntiqueEntities.MYRIAD_SHOVEL, owner, world, stack, null);
		this.setBaseDamage(8);
		this.entityData.set(LOYALTY, this.getLoyalty(stack));
		this.entityData.set(ENCHANTED, stack.hasFoil());
		this.entityData.set(GLOW, stack.getOrDefault(CAComponents.BOOLEAN_PROPERTY, false));
		this.setPickupItemStack(stack);
		this.entityData.set(ATTRIBUTES, this.getPickupItemStackOrigin().getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, Antiquities.getDefaultMyriadTool()));
		this.setPierceLevel((byte) 5);
	}

	public int getDyeColor() {
		return this.entityData.get(ATTRIBUTES).clothColor();
	}

	public int getOverlayColor() {
		return this.entityData.get(ATTRIBUTES).patternColor();
	}

	public boolean getGlow() {
		return this.entityData.get(GLOW);
	}

	public void summonPart() {
		for (int i = 1; i < 8; i++) {
			MyriadShovelPart entity = new MyriadShovelPart(AntiqueEntities.MYRIAD_SHOVEL_PART, this.level());
			entity.setPos(this.position());
			entity.setOwner(this);
			entity.setOrderId(i);
			this.level().addFreshEntity(entity);
		}
	}

	public boolean isEnchanted() {
		return this.entityData.get(ENCHANTED);
	}

	public String getCloth() {
		return this.entityData.get(ATTRIBUTES).clothType();
	}

	public String getPattern() {
		return this.entityData.get(ATTRIBUTES).clothPattern();
	}

	public MyriadToolComponent getAttributes() {
		return this.entityData.get(ATTRIBUTES);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
		super.defineSynchedData(builder);
		builder.define(LOYALTY, (byte)0);
		builder.define(ENCHANTED, false);
		builder.define(GLOW, false);
		builder.define(ATTRIBUTES, Antiquities.getDefaultMyriadTool());
		builder.define(PIERCE_LEVEL, (byte) 0);
	}

	@Override
	public void tick() {
		if (this instanceof ClothAccess clothAccess) {
			if (this.level().isClientSide()) {
				clothAccess.antique$tickManagers();
			}
		}
		if (this.inGroundTime > 4) {
			this.dealtDamage = true;
		}

		Entity entity = this.getOwner();
		int i = this.entityData.get(LOYALTY);
		if (i > 0 && (this.dealtDamage || this.isNoPhysics()) && entity != null) {
			if (!this.isOwnerAlive()) {
				if (this.level() instanceof ServerLevel serverWorld && this.pickup == AbstractArrow.Pickup.ALLOWED) {
					this.spawnAtLocation(serverWorld, this.getPickupItem(), 0.1F);
				}

				this.discard();
			} else {
				if (!(entity instanceof Player) && this.position().distanceTo(entity.getEyePosition()) < (double)entity.getBbWidth() + 1.0) {
					this.discard();
					return;
				}

				this.setNoPhysics(true);
				Vec3 vec3d = entity.getEyePosition().subtract(this.position());
				this.setPosRaw(this.getX(), this.getY() + vec3d.y * 0.015 * (double)i, this.getZ());
				double d = 0.05 * (double)i;
				this.setDeltaMovement(this.getDeltaMovement().scale(0.95).add(vec3d.normalize().scale(d)));
				if (this.returnTimer == 0) {
					this.playSound(SoundEvents.TRIDENT_RETURN, 10.0F, 1.0F);
				}

				this.returnTimer++;
			}
		}

		super.tick();
	}

	private boolean isOwnerAlive() {
		Entity entity = this.getOwner();
		return entity != null && entity.isAlive() && (!(entity instanceof ServerPlayer) || !entity.isSpectator());
	}

	@Override
	protected void onHit(@NotNull HitResult hitResult) {
		super.onHit(hitResult);
		if (this.level() instanceof ServerLevel serverWorld) {
			Vec3 pos = this.position().add(this.getLookAngle().multiply(1, 1, -1));
			serverWorld.sendParticles(CAParticles.RING, pos.x(), pos.y(), pos.z(), 1, 0.0, 0.0, 0.0, 0);
		}
	}

	@Override
	protected void onHitEntity(@NotNull EntityHitResult entityHitResult) {
		if (this.tickCount >= 2000) {
			this.tickCount -= 2000;
		}

		Entity entity = entityHitResult.getEntity();
		float f = 8.0F;
		Entity entity2 = this.getOwner();
		DamageSource damageSource = AntiqueDamageTypes.of(entity.level(), AntiqueDamageTypes.IMPALE, entity2 == null ? this : entity2);
		if (this.level() instanceof ServerLevel serverWorld) {

			if (this.getPierceLevel() > 0) {
				if (this.piercedEntities == null) {
					this.piercedEntities = new IntOpenHashSet(5);
				}

				this.piercedEntities.add(entity.getId());
			}

			f = EnchantmentHelper.modifyDamage(serverWorld, this.getWeaponItem() != null ? this.getWeaponItem() : ItemStack.EMPTY, entity, damageSource, f);
			if (entity.hurtServer(serverWorld, damageSource, f)) {
				if (entity.getType() == EntityType.ENDERMAN) {
					return;
				}

				EnchantmentHelper.doPostAttackEffectsWithItemSourceOnBreak(serverWorld, entity, damageSource, this.getWeaponItem(), item -> this.kill(serverWorld));
				if (entity instanceof LivingEntity) {
					entity.setDeltaMovement(this.getDeltaMovement().multiply(0.6, 0.45, 0.6));
					entity.hurtMarked = true;
				}
			}
		}

		if (this.getPierceLevel() <= 0) {
			this.deflect(ProjectileDeflection.REVERSE, entity, null, false);
			this.setDeltaMovement(this.getDeltaMovement().multiply(0.2, 0.2, 0.02));
			this.playSound(SoundEvents.TRIDENT_HIT, 1.0F, 1.0F);
		}
	}

	@Override
	protected void hitBlockEnchantmentEffects(@NotNull ServerLevel world, BlockHitResult blockHitResult, @NotNull ItemStack weaponStack) {
		Vec3 vec3d = blockHitResult.getBlockPos().clampLocationWithin(blockHitResult.getLocation());
		EnchantmentHelper.onHitBlock(
				world,
				weaponStack,
				this.getOwner() instanceof LivingEntity livingEntity ? livingEntity : null,
				this,
				null,
				vec3d,
				world.getBlockState(blockHitResult.getBlockPos()),
				item -> this.kill(world)
		);
	}

	@Override
	protected void onHitBlock(@NotNull BlockHitResult blockHitResult) {
		super.onHitBlock(blockHitResult);
		this.setPierceLevel((byte)0);
		this.resetPiercedEntities();
		summonPart();
	}

	@Override
	public ItemStack getWeaponItem() {
		return this.getPickupItemStackOrigin();
	}

	@Override
	protected boolean tryPickup(@NotNull Player player) {
		return this.canPickup && !player.isCreative() && player.getInventory().add(this.getPickupItem())
				|| this.isNoPhysics() && !player.isCreative() && this.ownedBy(player) && player.getInventory().add(this.getPickupItem())
				|| this.canPickup && player.isCreative();
	}

	@Override
	protected @NotNull ItemStack getDefaultPickupItem() {
		return new ItemStack(Items.TRIDENT);
	}

	@Override
	protected @NotNull SoundEvent getDefaultHitGroundSoundEvent() {
		return SoundEvents.TRIDENT_HIT_GROUND;
	}

	@Override
	public void playerTouch(@NotNull Player player) {
		if (this.ownedBy(player) || this.getOwner() == null) {
			super.playerTouch(player);
		}
	}

	@Override
	protected void readAdditionalSaveData(@NotNull ValueInput view) {
		super.readAdditionalSaveData(view);
		this.dealtDamage = view.getBooleanOr("DealtDamage", false);
		this.entityData.set(LOYALTY, this.getLoyalty(this.getPickupItemStackOrigin()));
		this.entityData.set(ENCHANTED, view.getBooleanOr("Glint", false));
		this.entityData.set(GLOW, view.getBooleanOr("Glow", false));
		this.entityData.set(ATTRIBUTES, view.read("Attributes", MyriadToolComponent.CODEC).orElseGet(Suppliers.ofInstance(Antiquities.getDefaultMyriadTool())));
		this.setPierceLevel(view.getByteOr("PierceLevel", (byte) 0));
	}

	@Override
	protected void addAdditionalSaveData(@NotNull ValueOutput view) {
		super.addAdditionalSaveData(view);
		view.putBoolean("DealtDamage", this.dealtDamage);
		view.putBoolean("Glint", this.isEnchanted());
		view.putBoolean("Glow", this.getGlow());
		view.putByte("PierceLevel", this.getPierceLevel());
		view.store("Attributes", MyriadToolComponent.CODEC, this.getAttributes());
	}

	private byte getLoyalty(ItemStack stack) {
		return this.level() instanceof ServerLevel serverWorld
				? (byte)Mth.clamp(EnchantmentHelper.getTridentReturnToOwnerAcceleration(serverWorld, stack, this), 0, 127)
				: 0;
	}

	@Override
	public void tickDespawn() {

	}

	@Override
	protected boolean canHitEntity(@NotNull Entity entity) {
		if (entity instanceof Player) {
			Entity var3 = this.getOwner();
			if (var3 instanceof Player playerEntity) {
                if (!playerEntity.canHarmPlayer((Player)entity)) {
					return false;
				}
			}
		}

		return super.canHitEntity(entity) && (this.piercedEntities == null || !this.piercedEntities.contains(entity.getId()));
	}

	@Override
	protected float getWaterInertia() {
		return 0.7F;
	}

	private void resetPiercedEntities() {
		if (this.piercedEntities != null) {
			this.piercedEntities.clear();
		}

	}

	private void setPierceLevel(byte level) {
		this.entityData.set(PIERCE_LEVEL, level);
	}

	public byte getPierceLevel() {
		return this.entityData.get(PIERCE_LEVEL);
	}
}
