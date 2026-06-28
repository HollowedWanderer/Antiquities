package net.hollowed.antique.mixin.entities.living.player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Predicate;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;

@Mixin(Player.class)
public class ProjectileFixMixin {

    @ModifyVariable(method = "getProjectile", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/item/ProjectileWeaponItem;getAllSupportedProjectiles()Ljava/util/function/Predicate;"), name = "supportedProjectiles")
    private Predicate<ItemStack> fixTheDamnCrossbowsPlease(Predicate<ItemStack> supportedProjectiles, ItemStack heldWeapon) {
        return ((ProjectileWeaponItem) heldWeapon.getItem()).getSupportedHeldProjectiles();
    }
}
