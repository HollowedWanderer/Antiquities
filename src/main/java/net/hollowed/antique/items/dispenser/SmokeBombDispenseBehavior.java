package net.hollowed.antique.items.dispenser;

import net.hollowed.antique.entities.SmokeBombEntity;
import net.hollowed.antique.index.AntiqueEntities;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public class SmokeBombDispenseBehavior extends DefaultDispenseItemBehavior {
    private final ProjectileItem.DispenseConfig dispenseConfig;

	public SmokeBombDispenseBehavior(Item item) {
		if (item instanceof ProjectileItem projectileItem) {
            this.dispenseConfig = projectileItem.createDispenseConfig();
		} else {
			throw new IllegalArgumentException(item + " not instance of " + ProjectileItem.class.getSimpleName());
		}
	}

	@Override
	public @NonNull ItemStack execute(BlockSource blockSource, @NonNull ItemStack itemStack) {
		ServerLevel serverLevel = blockSource.level();
		Direction direction = blockSource.state().getValue(DispenserBlock.FACING);
		Position position = this.dispenseConfig.positionFunction().getDispensePosition(blockSource, direction);

		SmokeBombEntity smokeBomb = new SmokeBombEntity(AntiqueEntities.SMOKE_BOMB, serverLevel);
		smokeBomb.setPos(new Vec3(position.x(), position.y(), position.z()).add((Math.random() - 0.5) * 2, 3, (Math.random() - 0.5) * 2));
		smokeBomb.setDeltaMovement(direction.getStepX(), direction.getStepY(), direction.getStepZ());
		smokeBomb.setItem(itemStack);
		serverLevel.addFreshEntity(smokeBomb);
		itemStack.shrink(1);
		return itemStack;
	}

	@Override
	protected void playSound(BlockSource blockSource) {
		blockSource.level().levelEvent(this.dispenseConfig.overrideDispenseEvent().orElse(1002), blockSource.pos(), 0);
	}
}
