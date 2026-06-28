package net.hollowed.antique.mixin.items;

import com.mojang.serialization.DataResult;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.component.BundleContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.apache.commons.lang3.math.Fraction;

@Mixin(BundleContents.class)
public abstract class BundleItemWeightChanger {

    @Inject(method = "getWeight", at = @At("HEAD"), cancellable = true)
    private static void adjustOccupancyForNonStackableItems(ItemInstance item, CallbackInfoReturnable<DataResult<Fraction>> cir) {
        Fraction occupancy = Fraction.getFraction(1, item.getMaxStackSize());

        if (!(item instanceof BundleItem)) {
            if (item.getMaxStackSize() == 1) {
                occupancy = occupancy.multiplyBy(Fraction.getFraction(1, 4));
            }
        }

        cir.setReturnValue(DataResult.success(occupancy));
    }
}
