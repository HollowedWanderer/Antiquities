package net.hollowed.antique.mixin.screens;

import net.hollowed.antique.index.AntiqueDataComponentTypes;
import net.hollowed.antique.index.AntiqueItems;
import net.hollowed.antique.items.BagOfTricksItem;
import net.hollowed.antique.items.SatchelItem;
import net.hollowed.antique.mixin.accessors.GetSlotAtAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ScrollWheelHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(AbstractContainerScreen.class)
public abstract class HandledScreenMixin {
    @Shadow @Nullable protected Slot hoveredSlot;
    @Unique
    private final ScrollWheelHandler scroller = new ScrollWheelHandler();
    @Unique
    private ItemStack lastSatchel = null;
    @Unique
    private ItemStack lastBag = null;

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    public void onMouseScrolled(double x, double y, double scrollX, double scrollY, CallbackInfoReturnable<Boolean> cir) {
        Minecraft client = Minecraft.getInstance();
        if (client.gui.screen() != null) {
            boolean bl = client.options.discreteMouseScroll().get();
            double d = client.options.mouseWheelSensitivity().get();
            double e = (bl ? Math.signum(scrollX) : scrollX) * d;
            double f = (bl ? Math.signum(scrollY) : scrollY) * d;
            Vector2i vector2i = this.scroller.onMouseScroll(e, f);
            if (vector2i.x == 0 && vector2i.y == 0) {
                return;
            }

            int i = vector2i.y == 0 ? -vector2i.x : vector2i.y;
            Slot hoveredSlot = ((GetSlotAtAccessor) client.gui.screen()).invokeGetSlotAt(x, y);

            if (hoveredSlot != null && !hoveredSlot.getItem().isEmpty() && hoveredSlot.getItem().getItem() instanceof SatchelItem) {
                performScrollAction(hoveredSlot.getItem(), i);
                cir.setReturnValue(true);
            }
            if (hoveredSlot != null && !hoveredSlot.getItem().isEmpty() && hoveredSlot.getItem().getItem() instanceof BagOfTricksItem) {
                performBagScrollAction(hoveredSlot.getItem(), i);
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "extractTooltip", at = @At("HEAD"))
    private void drawMouseoverTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (this.hoveredSlot == null && lastSatchel != null || lastSatchel != null && !this.hoveredSlot.hasItem() || lastSatchel != null && this.hoveredSlot.getItem() != lastSatchel) SatchelItem.setInternalIndex(lastSatchel, -1);
        if (this.hoveredSlot != null && this.hoveredSlot.hasItem() && this.hoveredSlot.getItem().is(AntiqueItems.SATCHEL)) lastSatchel = this.hoveredSlot.getItem();

        if (this.hoveredSlot == null && lastBag != null || lastBag != null && !this.hoveredSlot.hasItem() || lastBag != null && this.hoveredSlot.getItem() != lastBag) BagOfTricksItem.setInternalIndex(lastBag, -1);
        if (this.hoveredSlot != null && this.hoveredSlot.hasItem() && this.hoveredSlot.getItem().is(AntiqueItems.BAG_OF_TRICKS)) lastBag = this.hoveredSlot.getItem();
    }

    @Inject(method = "showTooltipWithItemInHand", at = @At("HEAD"), cancellable = true)
    private void stickyTooltips(ItemStack item, CallbackInfoReturnable<Boolean> cir) {
        if (item.getOrDefault(AntiqueDataComponentTypes.STICKY_TOOLTIP, false)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "onClose", at = @At("HEAD"))
    private void close(CallbackInfo ci) {
        if (lastSatchel != null) SatchelItem.setInternalIndex(lastSatchel, -1);
        if (lastBag != null) BagOfTricksItem.setInternalIndex(lastBag, -1);
    }

    @Unique
    private void performScrollAction(ItemStack stack, int i) {
        if (stack.getItem() instanceof SatchelItem && !Objects.requireNonNull(stack.get(AntiqueDataComponentTypes.SATCHEL_STACK)).isEmpty()) {
            SatchelItem.setInternalIndex(stack, ScrollWheelHandler.getNextScrollWheelSelection(i, SatchelItem.getInternalIndex(stack), Objects.requireNonNull(stack.get(AntiqueDataComponentTypes.SATCHEL_STACK)).size()));
        }
    }
    @Unique
    private void performBagScrollAction(ItemStack stack, int i) {
        if (stack.getItem() instanceof BagOfTricksItem && !Objects.requireNonNull(stack.get(AntiqueDataComponentTypes.SATCHEL_STACK)).isEmpty()) {
            BagOfTricksItem.setInternalIndex(stack, ScrollWheelHandler.getNextScrollWheelSelection(i, BagOfTricksItem.getInternalIndex(stack), Objects.requireNonNull(stack.get(AntiqueDataComponentTypes.SATCHEL_STACK)).size()));
        }
    }
}
