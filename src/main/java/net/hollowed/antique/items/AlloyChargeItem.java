package net.hollowed.antique.items;

import net.hollowed.antique.entities.AlloyChargeEntity;
import net.hollowed.antique.index.AntiqueEntities;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class AlloyChargeItem extends Item implements ProjectileItem {

	public AlloyChargeItem(Properties settings) {
		super(settings);
	}

	@Override
	public @NotNull InteractionResult use(Level world, Player user, @NotNull InteractionHand hand) {
		ItemStack itemStack = user.getItemInHand(hand);
		world.playSound(
			null,
			user.getX(),
			user.getY(),
			user.getZ(),
			SoundEvents.WITCH_THROW,
			SoundSource.NEUTRAL,
			0.5F,
			1.0F
		);
		if (world instanceof ServerLevel serverWorld) {
			AlloyChargeEntity alloyCharge = new AlloyChargeEntity(AntiqueEntities.SMOKE_BOMB, serverWorld);
			alloyCharge.setItem(itemStack);
			alloyCharge.setPos(user.getEyePosition());
			alloyCharge.shootFromRotation(user, user.getXRot(), user.getYRot(), 0, 0.5F, 1.0F);
			serverWorld.addFreshEntity(alloyCharge);
		}

		user.awardStat(Stats.ITEM_USED.get(this));
		itemStack.consume(1, user);
		user.getCooldowns().addCooldown(this.getDefaultInstance(), 20);
		return InteractionResult.SUCCESS;
	}

	@Override
	public @NotNull Projectile asProjectile(@NotNull Level world, Position pos, @NotNull ItemStack stack, @NotNull Direction direction) {
		AlloyChargeEntity alloyCharge = new AlloyChargeEntity(AntiqueEntities.ALLOY_CHARGE, world);
		alloyCharge.setPos(pos.x(), pos.y(), pos.z());
		return alloyCharge;
	}
}
