package net.hollowed.antique.items.components;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hollowed.antique.index.AntiqueDataComponentTypes;
import net.hollowed.antique.items.SatchelItem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

@Environment(EnvType.CLIENT)
public class SatchelTooltipComponent implements ClientTooltipComponent {
	private static final Identifier BUNDLE_PROGRESS_BAR_BORDER_TEXTURE = Identifier.withDefaultNamespace("container/bundle/bundle_progressbar_border");
	private static final Identifier BUNDLE_PROGRESS_BAR_FILL_TEXTURE = Identifier.withDefaultNamespace("container/bundle/bundle_progressbar_fill");
	private static final Identifier BUNDLE_PROGRESS_BAR_FULL_TEXTURE = Identifier.withDefaultNamespace("container/bundle/bundle_progressbar_full");
	private static final Identifier BUNDLE_SLOT_HIGHLIGHT_BACK_TEXTURE = Identifier.withDefaultNamespace("container/bundle/slot_highlight_back");
	private static final Identifier BUNDLE_SLOT_HIGHLIGHT_FRONT_TEXTURE = Identifier.withDefaultNamespace("container/bundle/slot_highlight_front");
	private static final Identifier BUNDLE_SLOT_BACKGROUND_TEXTURE = Identifier.withDefaultNamespace("container/bundle/slot_background");
	private static final Component BUNDLE_FULL = Component.translatable("item.minecraft.bundle.full");
	private static final Component BUNDLE_EMPTY = Component.translatable("item.minecraft.bundle.empty");
	private static final Component BUNDLE_EMPTY_DESCRIPTION = Component.translatable("item.antique.satchel.empty.description");
	private final List<ItemStack> satchelContents;
	private final ItemStack stack;

	public SatchelTooltipComponent(List<ItemStack> bundleContents, ItemStack stack) {
		this.satchelContents = bundleContents;
		this.stack = stack;
	}

	@Override
	public int getHeight(@NotNull Font textRenderer) {
		return this.satchelContents.isEmpty() ? getHeightOfEmpty(textRenderer) : this.getHeightOfNonEmpty();
	}

	@Override
	public int getWidth(@NotNull Font textRenderer) {
		return 128;
	}

	@Override
	public boolean showTooltipWithItemInHand() {
		return true;
	}

	private static int getHeightOfEmpty(Font textRenderer) {
		return getDescriptionHeight(textRenderer) + 13 + 8;
	}

	private int getHeightOfNonEmpty() {
		return this.getRowsHeight() + 13 + 8;
	}

	private int getRowsHeight() {
		return this.getRows() * 24;
	}

	private int getXMargin(int width) {
		return (width - 96) / 2;
	}

	private int getRows() {
		return 2;
	}

	@Override
	public void extractImage(@NonNull Font font, int x, int y, int w, int h, @NonNull GuiGraphicsExtractor graphics) {
		x -= 22;
		if (this.satchelContents != SatchelItem.lastContents) {
			SatchelItem.lastContents = this.satchelContents;
			SatchelItem.setInternalIndex(this.stack, -1);
		}
		this.stack.set(AntiqueDataComponentTypes.COUNTER, 0);
		if (this.satchelContents.isEmpty()) {
			this.drawEmptyTooltip(font, x, y, w, graphics);
		} else {
			this.drawNonEmptyTooltip(font, x, y, w, graphics);
		}
	}

	private void drawEmptyTooltip(Font textRenderer, int x, int y, int width, GuiGraphicsExtractor context) {
		drawEmptyDescription(x + this.getXMargin(width), y, textRenderer, context);
		this.drawProgressBar(x + this.getXMargin(width), y + getDescriptionHeight(textRenderer) + 4, textRenderer, context);
	}

	private void drawNonEmptyTooltip(Font textRenderer, int x, int y, int width, GuiGraphicsExtractor context) {
		List<ItemStack> list = this.firstStacksInContents();
		int i = x + this.getXMargin(width);
		int k = 0;

		for (int l = 0; l < this.getRows(); l++) {
			for (int m = 0; m < 4; m++) {
				int n = i + m * 24;
				int o = y + l * 24;
				if (shouldDrawItem(list, k)) {
					this.drawItem(k, n, o, list, k, textRenderer, context);
					k++;
				}
			}
		}

		this.drawSelectedItemTooltip(textRenderer, context, x + 22, y, width);
		this.drawProgressBar(x + this.getXMargin(width), y + this.getRowsHeight() + 4, textRenderer, context);
	}

	private List<ItemStack> firstStacksInContents() {
		int i = Math.min(this.satchelContents.size(), 8);
		return this.satchelContents.stream().toList().subList(0, i);
	}

	private static boolean shouldDrawItem(List<ItemStack> items, int itemIndex) {
		return items.size() > itemIndex;
	}

	private void drawItem(int index, int x, int y, List<ItemStack> stacks, int seed, Font textRenderer, GuiGraphicsExtractor drawContext) {
		boolean bl = index == this.selectedIndex();
		ItemStack itemStack = stacks.get(index);
		if (bl) {
			drawContext.blitSprite(RenderPipelines.GUI_TEXTURED, BUNDLE_SLOT_HIGHLIGHT_BACK_TEXTURE, x, y, 24, 24);
		} else {
			drawContext.blitSprite(RenderPipelines.GUI_TEXTURED, BUNDLE_SLOT_BACKGROUND_TEXTURE, x, y, 24, 24);
		}

		drawContext.item(itemStack, x + 4, y + 4, seed);
		drawContext.itemDecorations(textRenderer, itemStack, x + 4, y + 4);
		if (bl) {
			drawContext.blitSprite(RenderPipelines.GUI_TEXTURED, BUNDLE_SLOT_HIGHLIGHT_FRONT_TEXTURE, x, y, 24, 24);
		}
	}

	private void drawSelectedItemTooltip(Font textRenderer, GuiGraphicsExtractor drawContext, int x, int y, int width) {
		if (!this.satchelContents.isEmpty() && this.selectedIndex() < this.satchelContents.size() && this.selectedIndex() != -1) {
			ItemStack itemStack = this.satchelContents.get(this.selectedIndex());
			Component text = itemStack.getStyledHoverName();
			int i = textRenderer.width(text.getVisualOrderText());
			int j = x + width / 2 - 12;
			ClientTooltipComponent tooltipComponent = ClientTooltipComponent.create(text.getVisualOrderText());
			drawContext.tooltip(
				textRenderer, List.of(tooltipComponent), j - i / 2, y - 15, DefaultTooltipPositioner.INSTANCE, itemStack.get(DataComponents.TOOLTIP_STYLE)
			);
		}
	}

	private void drawProgressBar(int x, int y, Font textRenderer, GuiGraphicsExtractor drawContext) {
		drawContext.blitSprite(RenderPipelines.GUI_TEXTURED, this.getProgressBarFillTexture(), x + 1, y, this.getProgressBarFill(), 13);
		drawContext.blitSprite(RenderPipelines.GUI_TEXTURED, BUNDLE_PROGRESS_BAR_BORDER_TEXTURE, x, y, 128, 13);
		Component text = this.getProgressBarLabel();
		if (text != null) {
			drawContext.centeredText(textRenderer, text, x + 64, y + 3, CommonColors.WHITE);
		}
	}

	private static void drawEmptyDescription(int x, int y, Font textRenderer, GuiGraphicsExtractor drawContext) {
		drawContext.textWithWordWrap(textRenderer, BUNDLE_EMPTY_DESCRIPTION, x, y, 128, -5592406);
	}

	private static int getDescriptionHeight(Font textRenderer) {
		return textRenderer.split(BUNDLE_EMPTY_DESCRIPTION, 128).size() * 9;
	}

	private int getProgressBarFill() {
		return Mth.clamp(this.satchelContents.size() * 16, 0, 126);
	}

	private Identifier getProgressBarFillTexture() {
		return this.satchelContents.size() == 8 ? BUNDLE_PROGRESS_BAR_FULL_TEXTURE : BUNDLE_PROGRESS_BAR_FILL_TEXTURE;
	}

	private int selectedIndex() {
		return SatchelItem.getInternalIndex(this.stack);
	}

	@Nullable
	private Component getProgressBarLabel() {
		if (this.satchelContents.isEmpty()) {
			return BUNDLE_EMPTY;
		} else {
			return this.satchelContents.size() == 8 ? BUNDLE_FULL : null;
		}
	}
}
