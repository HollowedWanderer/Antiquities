package net.hollowed.antique.entities;

import net.hollowed.antique.index.AntiqueTrackedData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ClothEntity extends BlockAttachedEntity {
	public static final EntityDataAccessor<ItemStack> CLOTH = SynchedEntityData.defineId(ClothEntity.class, AntiqueTrackedData.CLOTH_ATTRIBUTES);

	public ClothEntity(EntityType<? extends BlockAttachedEntity> entityType, Level level, ItemStack cloth) {
		super(entityType, level);
		entityData.set(CLOTH, cloth);
	}

	public ClothEntity(EntityType<? extends BlockAttachedEntity> entityType, Level level, BlockPos blockPos, ItemStack cloth) {
		super(entityType, level, blockPos);
		entityData.set(CLOTH, cloth);
	}

	public ClothEntity(EntityType<? extends BlockAttachedEntity> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	public @Nullable ItemStack getPickResult() {
		return getCloth();
	}

	@Override
	protected void recalculateBoundingBox() {
		setPosRaw(pos.getX() + 0.5, pos.getY() + 0.375, pos.getZ() + 0.5);
		double d = getType().getWidth() / 2;
		double e = getType().getHeight();
		setBoundingBox(new AABB(getX() - d, getY(), getZ() - d, getX() + d, getY() + e, getZ() + d));
	}

	@Override
	public boolean survives() {
		return !getCloth().isEmpty() && level().getBlockState(pos).is(BlockTags.FENCES);
	}

	@Override
	public void dropItem(@NonNull ServerLevel level, @Nullable Entity entity) {
		if (!(entity instanceof Player player) || !player.hasInfiniteMaterials()) {
			playSound(SoundEvents.LEAD_UNTIED, 1, 1);
			spawnAtLocation(level, getCloth());
		}
	}

	public ItemStack getCloth() {
		return entityData.get(CLOTH);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
		builder.define(CLOTH, ItemStack.EMPTY);
	}

	@Override
	protected void readAdditionalSaveData(@NotNull ValueInput view) {
		super.readAdditionalSaveData(view);
		entityData.set(CLOTH, view.read("Attributes", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY));
	}

	@Override
	protected void addAdditionalSaveData(@NotNull ValueOutput view) {
		super.addAdditionalSaveData(view);
		view.store("Attributes", ItemStack.OPTIONAL_CODEC, getCloth());
	}
}
