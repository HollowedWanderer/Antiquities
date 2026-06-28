package net.hollowed.antique.mixin.items;

import net.hollowed.antique.enchantments.EnchantmentListener;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockItem.class)
public class BundleJumblingPreventStackDecrement {

    @Redirect(
            method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;consume(ILnet/minecraft/world/entity/LivingEntity;)V"
            )
    )
    private void conditionalDecrement(ItemStack instance, int amount, LivingEntity owner) {
        if (!EnchantmentListener.hasEnchantment(instance, "antique:jumbling")) {
            instance.consume(1, owner);
        }
    }
}
