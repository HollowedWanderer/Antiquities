package net.hollowed.antique.mixin.items;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStackTemplate;
import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientBundleTooltip;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.component.BundleContents;

@Mixin(ClientBundleTooltip.class)
public abstract class BundleTooltipShowsAll {
    @Unique
    private static final int slotSize = 13;
    @Unique
    private static final int columns = 8;

    @Mutable
    @Final
    @Shadow
    private final BundleContents contents;

    protected BundleTooltipShowsAll(BundleContents bundleContents) {
        this.contents = bundleContents;
    }

    @Shadow
    private List<ItemStackTemplate> getShownItems(int amountOfItemsToShow) {
        return null;
    }

    @Shadow
    private int gridSizeY() {
        return 0;
    }

    @Shadow
    private int itemGridHeight() {
        return 0;
    }

    @Shadow @Final private static Identifier SLOT_HIGHLIGHT_BACK_SPRITE;

    @Shadow @Final private static Identifier SLOT_BACKGROUND_SPRITE;

    @Shadow @Final private static Identifier SLOT_HIGHLIGHT_FRONT_SPRITE;

    @Shadow
    protected abstract void extractSlot(int slotNumber, int drawX, int drawY, List<ItemStackTemplate> shownItems, int slotIndex, Font font, GuiGraphicsExtractor graphics);

    @Shadow
    protected abstract void extractSelectedItemTooltip(Font font, GuiGraphicsExtractor graphics, int x, int y, int w);

    @Shadow
    private static void extractProgressbar(int x, int y, Font font, GuiGraphicsExtractor graphics, Fraction weight) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Shadow
    private static int getContentXOffset(int tooltipWidth) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Inject(method = "slotCount", at = @At("HEAD"), cancellable = true)
    private void modifySlotCount(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(this.contents.size());
    }

    @ModifyConstant(method = {"itemGridHeight", "extractSlot"}, constant = @Constant(intValue = 24))
    private int slotHeight(int original) {
        return BundleTooltipShowsAll.slotSize + 4;
    }

    @ModifyConstant(method = "extractSlot", constant = @Constant(intValue = 4))
    private int modifyPadding(int original) {
        return 0;
    }

    @ModifyConstant(method = "gridSizeY", constant = @Constant(intValue = 4))
    private int numColumns(int original) {
        return BundleTooltipShowsAll.columns;
    }

    @ModifyConstant(method = {"getWidth", "getContentXOffset", "extractProgressbar"}, constant = @Constant(intValue = 96))
    private static int tooltipWidth(int original) {
        return (BundleTooltipShowsAll.slotSize + 4) * BundleTooltipShowsAll.columns;
    }

    @ModifyConstant(method = "getProgressBarFill", constant = @Constant(intValue = 94))
    private static int barProgress(int original) {
        int totalWidth = (BundleTooltipShowsAll.columns * (BundleTooltipShowsAll.slotSize + 4));
        return totalWidth - 2;
    }


    @ModifyConstant(method = "extractProgressbar", constant = @Constant(intValue = 48))
    private static int fillText(int original) {
        int totalWidth = (BundleTooltipShowsAll.columns * (BundleTooltipShowsAll.slotSize + 4));
        return totalWidth / 2;
    }

    @Inject(method = "extractBundleWithItemsTooltip", at = @At("HEAD"), cancellable = true)
    private void drawNonEmptyTooltip(Font font, int x, int y, int w, int h, GuiGraphicsExtractor graphics, Fraction weight, CallbackInfo ci) {
        List<ItemStackTemplate> list = this.getShownItems(this.contents.getNumberOfItemsToShow());
        int o = 1;

        for (int p = 0; p < this.gridSizeY(); ++p) {
            for (int q = 0; q < BundleTooltipShowsAll.columns; ++q) {
                if (list == null) return;
                if (o > list.size()) {
                    break;
                }
                int r = x + q * (BundleTooltipShowsAll.slotSize + 4);
                int s = y + p * (BundleTooltipShowsAll.slotSize + 4);
                this.extractSlot(o, r, s, list, o, font, graphics);
                ++o;
            }
        }

        this.extractSelectedItemTooltip(font, graphics, x, y, w);
        extractProgressbar(x + getContentXOffset(w), y + this.itemGridHeight() + 4, font, graphics, weight);

        ci.cancel();
    }

    @Inject(method = "extractSlot", at = @At("HEAD"), cancellable = true)
    private void adjustBackgroundTexture(int slotNumber, int drawX, int drawY, List<ItemStackTemplate> shownItems, int slotIndex, Font font, GuiGraphicsExtractor graphics, CallbackInfo ci) {
        int adjustedX = drawX - 3;
        int adjustedY = drawY - 3;

        int i = shownItems.size() - slotIndex;
        boolean bl = i == this.contents.getSelectedItemIndex();

        ItemStackTemplate itemStack = shownItems.get(i);
        if (bl) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_BACK_SPRITE, adjustedX, adjustedY, 20, 20);
        } else {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_BACKGROUND_SPRITE, adjustedX, adjustedY, 20, 20);
        }

        graphics.item(itemStack.create(), drawX - 1, drawY - 1, slotIndex);
        graphics.itemDecorations(font, itemStack.create(), drawX -1, drawY -1);
        if (bl) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_FRONT_SPRITE, adjustedX, adjustedY, 20, 20);
        }

        ci.cancel();
    }
}
