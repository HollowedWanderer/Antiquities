package net.hollowed.antique.mixin.items;

import net.minecraft.world.item.component.ChargedProjectiles;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Objects;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.component.DyedItemColor;

@Mixin(CrossbowItem.class)
public abstract class CrossbowItemMixin extends ProjectileWeaponItem {

    public CrossbowItemMixin(Properties settings) {
        super(settings);
    }

    @Override
    public boolean allowComponentsUpdateAnimation(@NotNull Player player, @NotNull InteractionHand hand, @NotNull ItemStack oldStack, @NotNull ItemStack newStack) {
        return false;
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull ServerLevel world, @NotNull Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, world, entity, slot);
        ChargedProjectiles projectiles = stack.get(DataComponents.CHARGED_PROJECTILES);
        if (projectiles != null && !projectiles.isEmpty()) {
            ItemStack projectile = projectiles.getItems().getFirst();
            if (!projectile.isEmpty()) {
                if (projectile.get(DataComponents.POTION_CONTENTS) != null) {
                    stack.set(DataComponents.DYED_COLOR, new DyedItemColor(Objects.requireNonNull(projectile.get(DataComponents.POTION_CONTENTS)).getColor()));
                }
            }
        }
    }
}
