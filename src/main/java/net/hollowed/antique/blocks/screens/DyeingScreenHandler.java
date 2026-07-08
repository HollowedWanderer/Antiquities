package net.hollowed.antique.blocks.screens;

import net.hollowed.antique.Antiquities;
import net.hollowed.antique.index.AntiqueDataComponentTypes;
import net.hollowed.antique.index.AntiqueItems;
import net.hollowed.antique.index.AntiqueScreenHandlerType;
import net.hollowed.antique.items.components.MyriadToolComponent;
import net.hollowed.antique.util.ClothUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class DyeingScreenHandler extends AbstractContainerMenu {
	private final ContainerLevelAccess context;
	@Nullable
	private String hexCode;
	public final Container inventory = new SimpleContainer(1) {
		@Override
		public void setChanged() {
			super.setChanged();
			DyeingScreenHandler.this.slotsChanged(this);
		}
	};
	private final ResultContainer resultInventory = new ResultContainer() {
		@Override
		public void setChanged() {
			DyeingScreenHandler.this.slotsChanged(this);
		}
	};

	@Override
	public void slotsChanged(@NotNull Container inventory) {
		super.slotsChanged(inventory);
		if (inventory == this.inventory) {
			this.updateResult();
		}
	}

	public ItemStack getResult() {
		return this.resultInventory.getItem(0);
	}

	public DyeingScreenHandler(int syncId, Inventory playerInventory) {
		this(syncId, playerInventory, ContainerLevelAccess.NULL);
	}

	public DyeingScreenHandler(int syncId, Inventory playerInventory, ContainerLevelAccess context) {
		super(AntiqueScreenHandlerType.DYE_TABLE, syncId);
		this.context = context;
		this.addSlot(new Slot(this.inventory, 0, 62, 37) {
			@Override
			public boolean mayPlace(@NotNull ItemStack stack) {
				if (stack.is(AntiqueItems.MYRIAD_TOOL)) {
					return stack.getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, MyriadToolComponent.DEFAULT_NO_CLOTH)
							.cloth()
							.map(key ->
									context.evaluate((level, _) -> ClothUtil.getClothData(key, level.registryAccess()))
											.flatMap(it -> it)
											.map(skin -> skin.value().dyeable())
											.orElse(false)
							)
							.orElse(false);
				}
				return stack.get(DataComponents.DYED_COLOR) != null;
			}

			@Override
			public void onTake(@NotNull Player player, @NotNull ItemStack stack) {
				DyeingScreenHandler.this.resetHex();
			}
		});
		this.addSlot(new Slot(this.resultInventory, 0, 98, 37) {
			@Override
			public boolean mayPlace(@NotNull ItemStack stack) {
				return false;
			}

			@Override
			public void onTake(@NotNull Player player, @NotNull ItemStack stack) {
				DyeingScreenHandler.this.removeItem();
			}
		});
		this.addStandardInventorySlots(playerInventory, 8, 84);
	}

	private void removeItem() {
		this.inventory.setItem(0, ItemStack.EMPTY);
		this.resetHex();
	}

	private void resetHex() {
		this.hexCode = "";
	}

	@Override
	public @NotNull ItemStack quickMoveStack(@NotNull Player player, int slotIndex) {
		ItemStack clicked = ItemStack.EMPTY;
		Slot slot = this.slots.get(slotIndex);

		if (slot.hasItem()) {
			ItemStack stack = slot.getItem();
			clicked = stack.copy();

			if (slotIndex == 0) {
				if (!this.moveItemStackTo(stack, 2, 38, true)) {
					return ItemStack.EMPTY;
				}
			} else if (slotIndex == 1) {
				if (!this.moveItemStackTo(stack, 2, 38, true)) {
					return ItemStack.EMPTY;
				}
			} else if (stack.get(DataComponents.DYED_COLOR) != null) {
				if (!this.moveItemStackTo(stack, 1, 2, true)) {
					return ItemStack.EMPTY;
				}
			} else {
				if (this.slots.getFirst().hasItem() || !this.slots.getFirst().mayPlace(stack)) {
					return ItemStack.EMPTY;
				}

				ItemStack singleItem = stack.copyWithCount(1);
				stack.shrink(1);
				this.slots.getFirst().setByPlayer(singleItem);
			}

			if (stack.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}

			if (stack.getCount() == clicked.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(player, stack);
		}

		return clicked;
	}

	@Override
	public void removed(@NotNull Player player) {
		super.removed(player);
		this.context.execute((_, _) -> this.clearContainer(player, this.inventory));
	}

	@Override
	public boolean stillValid(@NotNull Player player) {
		return true;
	}

	public void updateResult() {
		ItemStack original = this.inventory.getItem(0);
		ItemStack result = original.copy();

		if (this.hexCode != null && this.hexCode.length() == 6) {
			try {
				DyedItemColor dyeColor = new DyedItemColor(Integer.parseInt(this.hexCode, 16));
				MyriadToolComponent component = result.get(AntiqueDataComponentTypes.MYRIAD_TOOL);

				if (component != null) {
					component = component.withCloth(component.cloth().orElse(ItemStack.EMPTY).copy());
					result.set(AntiqueDataComponentTypes.MYRIAD_TOOL, component.withCloth(cloth -> ClothUtil.setClothColor(cloth, Optional.of(dyeColor.rgb()))));
				} else {
					result.set(DataComponents.DYED_COLOR, dyeColor);
				}
			} catch (NumberFormatException e) {
                Antiquities.LOGGER.error("Invalid hexadecimal string format: {}", e.getMessage());
			}
		}

		this.resultInventory.setItem(0, result);
	}

	public boolean setHexCode(String string) {
		if (string != null && !string.equals(this.hexCode) && string.length() == 6) {
			this.hexCode = string;
			this.updateResult();
			return true;
		} else {
			return false;
		}
	}
}
