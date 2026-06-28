package net.hollowed.antique.mixin.screens;

import net.hollowed.antique.index.AntiqueKeyBindings;
import net.hollowed.antique.index.AntiqueItems;
import net.hollowed.antique.items.SatchelItem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(Gui.class)
public class SatchelOverlayMixin {

    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    @Final
    private GuiRenderState guiRenderState;
    @Unique
    private static final Identifier HOTBAR_SLOT = Identifier.withDefaultNamespace("textures/gui/sprites/hud/hotbar_offhand_left.png");
    @Unique
    private static final Identifier HOTBAR_SELECTORS = Identifier.withDefaultNamespace("textures/gui/sprites/hud/hotbar_selection.png");

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    public void render(DeltaTracker deltaTracker, boolean shouldRenderLevel, boolean resourcesLoaded, CallbackInfo ci) {
        Minecraft client = this.minecraft;
        Font textRenderer = client.font;
        Player player = client.player;
        if (player == null) return;

        int xMouse = (int)client.mouseHandler.getScaledXPos(client.getWindow());
        int yMouse = (int)client.mouseHandler.getScaledYPos(client.getWindow());
        GuiGraphicsExtractor graphics = new GuiGraphicsExtractor(client, this.guiRenderState, xMouse, yMouse);

        ItemStack satchel = player.getItemBySlot(EquipmentSlot.LEGS);
        if (satchel.getItem() != AntiqueItems.SATCHEL || !AntiqueKeyBindings.showSatchel.isDown()) return;

        int x = (graphics.guiWidth() / 2);
        int y = (graphics.guiHeight() / 2) + 7;

        if (satchel.getItem() instanceof SatchelItem satchelItem) {
            List<ItemStack> storedStacks = SatchelItem.getStoredStacks(satchel);
            List<ItemStack> allStacks = new ArrayList<>(storedStacks);

            if (storedStacks.isEmpty()) {
                allStacks.add(ItemStack.EMPTY);
            }

            while (allStacks.size() < 8) {
                allStacks.add(ItemStack.EMPTY);
            }

            int maxRows = 2;
            int maxCols = 4;

            for (int row = 0; row < maxRows; row++) {
                for (int col = 0; col < maxCols; col++) {
                    int index = row * 4 + col;

                    ItemStack stack = allStacks.get(index);
                    int slotX = (x - 44) + (22 * col);
                    int slotY = y - 1 + (22 * row);

                    graphics.blit(
                            RenderPipelines.GUI_TEXTURED,
                            HOTBAR_SLOT,
                            slotX, slotY,
                            0, 1,
                            22, 22,
                            29, 24
                    );
                     if (row == 0 && col < 3) {
                         for (int inRow = 0; inRow < 4; inRow++) {
                             for (int inCol = 0; inCol < 4; inCol++) {
                                 graphics.blit(
                                         RenderPipelines.GUI_TEXTURED,
                                         HOTBAR_SLOT,
                                         slotX + 20 + inRow, slotY + 20 + inCol,
                                         2, 1,
                                         1, 1,
                                         29, 24
                                 );
                             }
                         }
                     }

                    if (!stack.isEmpty()) {
                        graphics.item(stack, slotX + 3, slotY + 3);
                        graphics.itemDecorations(textRenderer, stack, slotX + 2, slotY + 2);
                    }
                }
            }

            ItemStack selectedStack = allStacks.get(satchelItem.getIndex());
            if (selectedStack != null && !selectedStack.isEmpty()) {
                Component text = selectedStack.getStyledHoverName();
                int i = textRenderer.width(text.getVisualOrderText());
                ClientTooltipComponent tooltipComponent = ClientTooltipComponent.create(text.getVisualOrderText());
                graphics.tooltip(textRenderer, List.of(tooltipComponent), x - 12 - i /2, y - 15, DefaultTooltipPositioner.INSTANCE, selectedStack.get(DataComponents.TOOLTIP_STYLE));

            }

            int selectorX = (x - 45) + (22 * (satchelItem.getIndex() <= 3 ? satchelItem.getIndex() : (satchelItem.getIndex() - 4)));
            int selectorY = (y - 2) + (22 * (satchelItem.getIndex() > 3 ? 1 : 0));

            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    HOTBAR_SELECTORS,
                    selectorX, selectorY,
                    0, 0,
                    24, 23,
                    24, 23
            );
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    HOTBAR_SELECTORS,
                    selectorX, selectorY + 23,
                    0, 0,
                    24, 1,
                    24, 23
            );
        }
    }
}
