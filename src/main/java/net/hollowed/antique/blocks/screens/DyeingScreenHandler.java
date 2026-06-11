package net.hollowed.antique.blocks.screens;

import net.hollowed.antique.index.AntiqueDataComponentTypes;
import net.hollowed.antique.index.AntiqueItems;
import net.hollowed.antique.index.AntiqueScreenHandlerType;
import net.hollowed.antique.items.MyriadToolItem;
import net.hollowed.antique.items.components.ColorProvider;
import net.hollowed.antique.items.components.MyriadToolComponent;
import net.hollowed.antique.util.resources.ClothSkinData;
import net.hollowed.antique.util.resources.ClothSkinListener;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
					ClothSkinData.ClothSubData data = ClothSkinListener.getTransform(stack.getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, MyriadToolComponent.DEFAULT_NO_CLOTH).clothType());
					return data.dyeable();
				}
				return stack.is(ItemTags.DYEABLE);
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
	public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
		ItemStack newStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if (slot.hasItem()) {
			ItemStack originalStack = slot.getItem();
			newStack = originalStack.copy();

			int inputSlot = 0;
			int outputSlot = 1;
            int playerInvStart = 2;
			int playerInvEnd = playerInvStart + 27;
            int hotbarEnd = playerInvEnd + 9;

			if (index == outputSlot || index == inputSlot) {
				if (!moveItemStackTo(originalStack, playerInvStart, hotbarEnd, false)) {
					return ItemStack.EMPTY;
				}
			} else {
				if (originalStack.is(ItemTags.DYEABLE)) {
					if (!moveItemStackTo(originalStack, inputSlot, inputSlot + 1, false)) {
						return ItemStack.EMPTY;
					}
				} else if (index >= playerInvStart && index < playerInvEnd) {
					if (!moveItemStackTo(originalStack, playerInvEnd, hotbarEnd, false)) {
						return ItemStack.EMPTY;
					}
				} else if (index >= playerInvEnd && index < hotbarEnd) {
					if (!moveItemStackTo(originalStack, playerInvStart, playerInvEnd, false)) {
						return ItemStack.EMPTY;
					}
				}
			}

			if (originalStack.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}

			if (originalStack.getCount() == newStack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(player, originalStack);
			this.broadcastChanges();
		}

		return newStack;
	}

	@Override
	public void removed(@NotNull Player player) {
		super.removed(player);
		this.context.execute((world, pos) -> this.clearContainer(player, this.inventory));
	}

	@Override
	public boolean stillValid(@NotNull Player player) {
		return true;
	}

	public void updateResult() {
		ItemStack originalStack = this.inventory.getItem(0);
		ItemStack resultStack = originalStack.copy();
		if (this.hexCode != null && this.hexCode.length() == 6) {
			int intValue = 0;
			try {
				intValue = Integer.parseInt(this.hexCode, 16);
			} catch (NumberFormatException e) {
				System.err.println("Invalid hexadecimal string format: " + e.getMessage());
			}
			if (resultStack.getItem() instanceof MyriadToolItem) {
				MyriadToolComponent component = resultStack.get(AntiqueDataComponentTypes.MYRIAD_TOOL);
				if (component != null) {
					resultStack.set(AntiqueDataComponentTypes.MYRIAD_TOOL, new MyriadToolComponent(
							component.toolBit(),
							component.clothType(),
							component.clothPattern(),
							new ColorProvider.Constant(intValue),
							component.patternColor()
					));
				}
			} else {
				resultStack.set(DataComponents.DYED_COLOR, new DyedItemColor(intValue));
			}
		}
		this.resultInventory.setItem(0, resultStack);
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
