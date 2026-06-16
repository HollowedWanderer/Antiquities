package net.hollowed.antique.mixin.items;

import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hollowed.antique.index.AntiqueDataComponentTypes;
import net.hollowed.antique.items.components.AmethystForkComponent;
import net.hollowed.antique.mixin.accessors.AbstractContainerScreenAccessor;
import net.hollowed.antique.networking.TuneAmethystForkPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Inject(
            method = "onScroll",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/MouseHandler;getScaledXPos(Lcom/mojang/blaze3d/platform/Window;)D"
            ),
            cancellable = true
    )
    private void onScroll(long l, double d, double e, CallbackInfo ci, @Local(ordinal = 4) double ySign) {
        if (Minecraft.getInstance().screen instanceof AbstractContainerScreenAccessor accessor) {
            Slot slot = accessor.antique$getHoveredSlot();
            LocalPlayer player = Minecraft.getInstance().player;

            if (slot != null && player != null && slot.container == player.getInventory() && slot.hasItem()) {
                ItemStack stack = slot.getItem();
                AmethystForkComponent component = stack.get(AntiqueDataComponentTypes.AMETHYST_FORK);

                if (component != null) {
                    if (ySign == 1) {
                        component = component.withNote(component.note() == 24 ? 0 : (component.note() + 1));
                    } else if (ySign == -1) {
                        component = component.withNote(component.note() == 0 ? 24 : (component.note() - 1));
                    }

                    player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.1F, 2.0F);
                    stack.set(AntiqueDataComponentTypes.AMETHYST_FORK, component);
                    ClientPlayNetworking.send(new TuneAmethystForkPayload(slot.getContainerSlot(), component.note()));
                    ci.cancel();
                }
            }
        }
    }
}
