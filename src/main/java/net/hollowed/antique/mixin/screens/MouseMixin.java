package net.hollowed.antique.mixin.screens;

import com.llamalad7.mixinextras.sugar.Local;
import net.hollowed.antique.index.AntiqueKeyBindings;
import net.hollowed.antique.index.AntiqueDataComponentTypes;
import net.hollowed.antique.items.SatchelItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.ScrollWheelHandler;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(MouseHandler.class)
public class MouseMixin {

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getInventory()Lnet/minecraft/world/entity/player/Inventory;"), cancellable = true)
    public void antique$makeSatchelScrollable(long window, double horizontal, double vertical, CallbackInfo ci, @Local int i) {
        if (AntiqueKeyBindings.showSatchel.isDown()) {
            if (minecraft.player == null) return;
            ItemStack satchel = minecraft.player.getItemBySlot(EquipmentSlot.LEGS);
            if (satchel.getItem() instanceof SatchelItem satchelItem && satchel.get(AntiqueDataComponentTypes.SATCHEL_STACK) != null && !Objects.requireNonNull(satchel.get(AntiqueDataComponentTypes.SATCHEL_STACK)).isEmpty() && !minecraft.player.isShiftKeyDown()) {
                satchelItem.setIndex(ScrollWheelHandler.getNextScrollWheelSelection(i, satchelItem.getIndex(), 8));
                ci.cancel();
            }
        }
    }
}