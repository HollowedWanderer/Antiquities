package net.hollowed.antique.mixin.items;

import net.hollowed.antique.items.components.BagOfTricksTooltipComponent;
import net.hollowed.antique.items.components.SatchelTooltipComponent;
import net.hollowed.antique.items.tooltips.BagOfTricksTooltipData;
import net.hollowed.antique.items.tooltips.SatchelTooltipData;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ClientTooltipComponent.class)
public interface TooltipComponentMixin {

    @Inject(method = "create(Lnet/minecraft/world/inventory/tooltip/TooltipComponent;)Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipComponent;", at = @At("HEAD"), cancellable = true)
    private static void of(TooltipComponent component, CallbackInfoReturnable<ClientTooltipComponent> cir) {
        if (component instanceof SatchelTooltipData(List<ItemStack> contents, ItemStack stack)) {
            cir.setReturnValue(new SatchelTooltipComponent(contents, stack));
        }
        if (component instanceof BagOfTricksTooltipData(List<ItemStack> contents, ItemStack stack)) {
            cir.setReturnValue(new BagOfTricksTooltipComponent(contents, stack));
        }
    }
}
