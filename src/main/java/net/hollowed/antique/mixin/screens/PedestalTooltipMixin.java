package net.hollowed.antique.mixin.screens;

import net.hollowed.antique.client.renderer.pedestal.PedestalTooltipRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class PedestalTooltipMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private void renderOverlays(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client.level != null && client.player != null && client.player.isShiftKeyDown()) {
            int screenWidth = client.getWindow().getGuiScaledWidth();
            int screenHeight = client.getWindow().getGuiScaledHeight();
            PedestalTooltipRenderer.renderTooltip(context, screenWidth, screenHeight);
        }
    }
}
