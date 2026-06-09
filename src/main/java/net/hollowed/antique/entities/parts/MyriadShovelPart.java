package net.hollowed.antique.entities.parts;

import net.hollowed.antique.Antiquities;
import net.hollowed.antique.entities.MyriadShovelEntity;
import net.hollowed.antique.util.delay.TickDelayScheduler;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MyriadShovelPart extends Entity implements TraceableEntity {

	private Entity owner;
	private UUID ownerUuid;
	private boolean leftOwner = false;

	private int id;

	private List<Entity> collisionEntities = List.of();
	private long prevTime;

	public MyriadShovelPart(EntityType<@NotNull MyriadShovelPart> myriadShovelEntityEntityType, Level world) {
		super(myriadShovelEntityEntityType, world);
	}

	@Nullable
	protected Entity getEntity(UUID uuid) {
		return this.level() instanceof ServerLevel serverWorld ? serverWorld.getEntity(uuid) : null;
	}

	public ItemStack getPickResult() {
		if (this.getOwner() == null) return Antiquities.getMyriadShovelStack();
		return this.getOwner() instanceof MyriadShovelEntity shovel ? shovel.getPickupItemStackOrigin() : Antiquities.getMyriadShovelStack();
	}

	public void setOrderId(int id) {
		this.id = id;
	}

	public int getOrderId() {
		return this.id;
	}

	@Override
	public void tick() {
		if (this.getOwner() == null && this.level() instanceof ServerLevel) {
			this.discard();
			return;
		}

		if (this.tickCount >= 20) {
			this.tickCount -= 9;
		}

		if (!this.leftOwner) {
			this.leftOwner = this.shouldLeaveOwner();
		}

		super.tick();
		if (this.level() instanceof ServerLevel world) {
			world.getChunkSource().removeEntity(this);
			world.getChunkSource().addEntity(this);
		}

		if (this.level().getGameTime() > (prevTime + 5)) {
			prevTime = this.level().getGameTime();
			collisionEntities = this.level().getEntities(this, this.getBoundingBox().contract(0.1, 0.1, 0.1), entity -> entity instanceof LivingEntity);
		}

		for (Entity entity : collisionEntities) {
			entity.makeStuckInBlock(Blocks.AIR.defaultBlockState(), new Vec3(0.05, 0.01, 0.05));
		}

		if (this.getOwner() instanceof MyriadShovelEntity shovel && shovel.canPickup) {
			this.discard();
			return;
		}

		if (this.getOwner() != null) {
			float multiplier = 0.25F;
			this.setPos(Objects.requireNonNull(this.getOwner()).position().x, this.getOwner().position().y - 0.25, this.getOwner().position().z);
			multiplier += (this.getOrderId() / 3.5F) - 0.2F;
			Vec3 look = this.getOwner().getViewVector(0);
			this.setPos(this.position().add(look.multiply(multiplier, multiplier, -multiplier)));
		}
	}



	@Override
	public boolean canBeCollidedWith(@Nullable Entity entity) {
		if (entity == null) return false;
		List<Entity> list = this.level().getEntities(this, this.getBoundingBox(), entity1 -> !(entity1 instanceof MyriadShovelPart));
		return entity.getY() >= this.getBoundingBox().maxY - 0.5 || !entity.getPose().equals(Pose.SWIMMING) && !list.contains(entity);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
	}

	@Override
	protected void readAdditionalSaveData(ValueInput view) {
		this.setOwner(view.read("Owner", UUIDUtil.CODEC).orElse(null));
		this.leftOwner = view.getBooleanOr("LeftOwner", false);
		this.id = view.getIntOr("Id", 0);
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput view) {
		view.storeNullable("Owner", UUIDUtil.CODEC, this.ownerUuid);
		if (this.leftOwner) {
			view.putBoolean("LeftOwner", true);
		}
		view.putInt("Id", this.id);
	}

	@Override
	public boolean isPickable() {
		return true;
	}

	@Override
	public final boolean hurtServer(@NotNull ServerLevel world, @NotNull DamageSource source, float amount) {
		TickDelayScheduler.schedule(1, () -> {
			if (this.owner != null && this.owner instanceof MyriadShovelEntity entity) {
				entity.canPickup = true;
				entity.getEntityData().set(MyriadShovelEntity.LOYALTY, (byte) 3);
				entity.returnTimer = 1;
			}
		});
		return false;
	}

    @Override
	public boolean shouldBeSaved() {
		return true;
	}

	@Override
	public boolean shouldRender(double cameraX, double cameraY, double cameraZ) {
		return true;
	}

	public void setOwner(@Nullable Entity entity) {
		if (entity != null) {
			this.ownerUuid = entity.getUUID();
			this.owner = entity;
		}
	}

	@Nullable
	@Override
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

	protected void setOwner(@Nullable UUID ownerUuid) {
		if (!Objects.equals(this.ownerUuid, ownerUuid)) {
			this.ownerUuid = ownerUuid;
			this.owner = ownerUuid != null ? this.getEntity(ownerUuid) : null;
		}
	}

	private boolean shouldLeaveOwner() {
		Entity entity = this.getOwner();
		if (entity != null) {
			AABB box = this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0);
			return entity.getRootVehicle().getSelfAndPassengers().filter(EntitySelector.CAN_BE_PICKED).noneMatch(entityx -> box.intersects(entityx.getBoundingBox()));
		} else {
			return true;
		}
	}
}