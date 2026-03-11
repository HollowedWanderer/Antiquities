package net.hollowed.antique.mixin.items;

import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Objects;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.component.DyedItemColor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Item.class)
public abstract class BowItemMixin implements FabricItem {

    @Override
    public boolean allowComponentsUpdateAnimation(@NotNull Player player, @NotNull InteractionHand hand, @NotNull ItemStack oldStack, @NotNull ItemStack newStack) {
        return !((Item) (Object) this instanceof BowItem);
    }

    @Inject(method = "inventoryTick", at = @At("HEAD"))
    public void inventoryTick(ItemStack stack, ServerLevel serverLevel, Entity entity, EquipmentSlot equipmentSlot, CallbackInfo ci) {
        if ((Item) (Object) this instanceof BowItem) {
            if (entity instanceof Player user) {
                ItemStack projectile = user.getProjectile(stack);
                if (user.hasInfiniteMaterials() || !projectile.isEmpty()) {
                    stack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(projectile));
                    if (projectile.get(DataComponents.POTION_CONTENTS) != null) {
                        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(Objects.requireNonNull(projectile.get(DataComponents.POTION_CONTENTS)).getColor()));
                    }
                }
            }
        }
    }
}
