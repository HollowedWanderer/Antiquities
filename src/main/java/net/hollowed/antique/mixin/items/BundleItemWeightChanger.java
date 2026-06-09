package net.hollowed.antique.mixin.items;

import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.apache.commons.lang3.math.Fraction;

@Mixin(BundleContents.class)
public abstract class BundleItemWeightChanger {

    @Inject(method = "getWeight(Lnet/minecraft/world/item/ItemStack;)Lorg/apache/commons/lang3/math/Fraction;", at = @At("HEAD"), cancellable = true)
    private static void adjustOccupancyForNonStackableItems(ItemStack stack, CallbackInfoReturnable<Fraction> cir) {
        Fraction occupancy = Fraction.getFraction(1, stack.getMaxStackSize());

        if (!(stack.getItem() instanceof BundleItem)) {
            if (!stack.isStackable()) {
                occupancy = occupancy.multiplyBy(Fraction.getFraction(1, 4));
            }
        }

        cir.setReturnValue(occupancy);
    }
}
