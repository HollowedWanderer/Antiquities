package net.hollowed.antique.entities;

import net.hollowed.antique.index.AntiqueItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class AlloyChargeEntity extends ThrowableItemProjectile {
    public AlloyChargeEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    protected @NotNull Item getDefaultItem() {
        return AntiqueItems.ALLOY_CHARGE;
    }
}
