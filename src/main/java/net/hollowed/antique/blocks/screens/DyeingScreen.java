package net.hollowed.antique.blocks.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hollowed.antique.Antiquities;
import net.hollowed.antique.index.AntiqueDataComponentTypes;
import net.hollowed.antique.items.MyriadToolItem;
import net.hollowed.antique.items.components.MyriadToolComponent;
import net.hollowed.antique.networking.DyePacketPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class DyeingScreen extends AbstractContainerScreen<@NotNull DyeingScreenHandler> implements ContainerListener {
	private static final Identifier TEXTURE = Antiquities.id("textures/gui/container/dye_table.png");
	private EditBox colorField;

	public DyeingScreen(DyeingScreenHandler handler, Inventory inventory, Component title) {
		super(handler, inventory, title);
	}

	@Override
	protected void init() {
		super.init();
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
        this.colorField = new EditBox(Minecraft.getInstance().font, i + 63, j + 65, 58, 9, Component.translatable("container.antique.hex"));
		this.colorField.setCanLoseFocus(false);
		this.colorField.setTextColor(0xFFFFF4BC);
		this.colorField.setTextColorUneditable(0xFFFFF4BC);
		this.colorField.setBordered(false);
		this.colorField.setMaxLength(6);
		this.colorField.setResponder(this::onColorChanged);
		this.colorField.setValue("");
		this.colorField.setFilter(s -> s.matches("(?i)[0-9a-f]{0,6}"));
		this.addRenderableWidget(this.colorField);
		this.colorField.setEditable(this.menu.getSlot(0).hasItem());
		this.menu.addSlotListener(this);
	}

	private void onColorChanged(String color) {
		Slot slot = this.menu.getSlot(0);
		if (slot.hasItem()) {
            if (this.menu.setHexCode(color)) {
                if (this.minecraft.player == null) return;
				ClientPlayNetworking.send(new DyePacketPayload(color));
			}
		}
	}

	@Override
	public void render(@NotNull GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);
		this.renderTooltip(context, mouseX, mouseY);
	}

	@Override
	protected void renderLabels(GuiGraphics context, int mouseX, int mouseY) {
		context.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xff6b5e3c, false);
	}

	@Override
	public void removed() {
		super.removed();
		this.menu.removeSlotListener(this);
	}

	@Override
	public void resize(int i, int j) {
		String string = this.colorField.getValue();
		this.init(width, height);
		this.colorField.setValue(string);
	}

	@Override
	protected void setInitialFocus() {
		super.setInitialFocus(this.colorField);
	}

	@Override
	public boolean keyPressed(KeyEvent input) {
		if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
			if (this.minecraft.player == null) return false;
			this.minecraft.player.closeContainer();
		}

		return this.colorField.keyPressed(input) || this.colorField.canConsumeInput() || super.keyPressed(input);
	}

	@Override
	protected void renderBg(GuiGraphics context, float deltaTicks, int mouseX, int mouseY) {
		int i = this.leftPos;
		int j = this.topPos;
		context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i, j, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

		ItemStack result = this.menu.getResult();
		if (!result.isEmpty()) {
            int intValue = 0xFF000000 | (result.getItem() instanceof MyriadToolItem
					? result.getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, MyriadToolComponent.DEFAULT_NO_CLOTH).clothColor()
					: result.getOrDefault(DataComponents.DYED_COLOR, new DyedItemColor(0xFFFFFF)).rgb());
			context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i + 1, j + 3, 0.0F, 176.0F, 174, 80, 256, 256, intValue);
		}
	}

	@Override
	public void slotChanged(@NotNull AbstractContainerMenu handler, int slotId, @NotNull ItemStack stack) {
		if (slotId == 0) {
			this.colorField.setValue("");
			this.colorField.setEditable(!stack.isEmpty());
			this.setFocused(this.colorField);
		}
	}

	@Override
	public void dataChanged(@NotNull AbstractContainerMenu handler, int property, int value) {}
}
