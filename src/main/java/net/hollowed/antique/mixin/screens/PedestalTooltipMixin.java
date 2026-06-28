package net.hollowed.antique.mixin.screens;

import net.hollowed.antique.client.renderer.pedestal.PedestalTooltipRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class PedestalTooltipMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Final
    private GuiRenderState guiRenderState;

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void renderOverlays(DeltaTracker deltaTracker, boolean shouldRenderLevel, boolean resourcesLoaded, CallbackInfo ci) {
        Minecraft client = this.minecraft;
        int xMouse = (int)client.mouseHandler.getScaledXPos(client.getWindow());
        int yMouse = (int)client.mouseHandler.getScaledYPos(client.getWindow());
        GuiGraphicsExtractor graphics = new GuiGraphicsExtractor(client, this.guiRenderState, xMouse, yMouse);
        if (client.level != null && client.player != null && client.player.isShiftKeyDown()) {
            int screenWidth = client.getWindow().getGuiScaledWidth();
            int screenHeight = client.getWindow().getGuiScaledHeight();
            PedestalTooltipRenderer.renderTooltip(graphics, screenWidth, screenHeight);
        }
    }
}
