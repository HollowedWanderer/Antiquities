package net.hollowed.antique.networking;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.hollowed.antique.items.MyriadToolBitItem;
import net.hollowed.antique.items.MyriadToolItem;
import net.hollowed.antique.items.SatchelItem;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

public class SatchelPacketReceiver {

    public static void registerServerPacket() {

        ServerPlayNetworking.registerGlobalReceiver(SatchelPacketPayload.ID, (payload, context) -> {

            Player player = context.player();

            ItemStack satchelInventory = player.getItemBySlot(EquipmentSlot.LEGS);

            if (satchelInventory.getItem() instanceof SatchelItem satchelItem) {
                Inventory playerInventory = player.getInventory();

                int currentHotbarSlot = playerInventory.getSelectedSlot();

                ItemStack currentHotbarStack = playerInventory.getItem(currentHotbarSlot);
                ItemStack currentSatchelStack = satchelItem.getSelectedStack(satchelInventory);

                ItemStack myriadItem = ItemStack.EMPTY;

                if (!satchelItem.isInvalidItem(currentHotbarStack)) {
                    if (currentSatchelStack.isEmpty() && currentHotbarStack.isEmpty()) {
                        player.level().playSound(null, player.blockPosition(), SoundEvents.BUNDLE_INSERT_FAIL, SoundSource.PLAYERS, 1.0F, 1.0F);
                    }

                    // Move the selected satchel stack to the hotbar
                    if (!currentSatchelStack.isEmpty()) {
                        if (currentHotbarStack.getItem() instanceof MyriadToolItem && !MyriadToolItem.isInvalidItem(currentSatchelStack)) {
                            myriadItem = MyriadToolItem.getStoredStack(currentHotbarStack);
                            MyriadToolItem.setStoredStack(currentHotbarStack, currentSatchelStack);
                            player.level().playSound(null, player.blockPosition(), SoundEvents.BUNDLE_REMOVE_ONE, SoundSource.PLAYERS, 1.0F, 1.0F);
                        } else {
                            List<ItemStack> storedStacks = new ArrayList<>(SatchelItem.getStoredStacks(satchelInventory));
                            if (storedStacks.stream().anyMatch(itemStack -> ItemStack.isSameItemSameComponents(itemStack, currentHotbarStack) && itemStack.getCount() < itemStack.getMaxStackSize())) {
                                ItemStack remainder = SatchelItem.addToStoredStacks(storedStacks, currentHotbarStack);
                                SatchelItem.setStoredStacks(satchelInventory, storedStacks);
                                playerInventory.setItem(currentHotbarSlot, remainder);
                                player.level().playSound(null, player.blockPosition(), SoundEvents.BUNDLE_INSERT, SoundSource.PLAYERS, 1.0F, 1.0F);
                                return;
                            } else {
                                playerInventory.setItem(currentHotbarSlot, currentSatchelStack);
                            }
                            player.level().playSound(null, player.blockPosition(), SoundEvents.BUNDLE_REMOVE_ONE, SoundSource.PLAYERS, 1.0F, 1.0F);
                        }
                    } else if (currentHotbarStack.getItem() instanceof MyriadToolItem) {
                        myriadItem = MyriadToolItem.getStoredStack(currentHotbarStack);
                        if (myriadItem.isEmpty()) {
                            satchelItem.setSlot(satchelInventory, myriadItem);
                            if (currentSatchelStack.isEmpty()) {
                                playerInventory.removeItemNoUpdate(currentHotbarSlot);
                            } else {
                                MyriadToolItem.setStoredStack(currentHotbarStack, ItemStack.EMPTY);
                            }
                        } else {
                            MyriadToolItem.setStoredStack(currentHotbarStack, ItemStack.EMPTY);
                        }
                    } else {
                        playerInventory.removeItemNoUpdate(currentHotbarSlot);
                    }

                    // Update the satchel's slot with the hotbar item
                    if (!currentHotbarStack.isEmpty()) {
                        if ((currentHotbarStack.getItem() instanceof MyriadToolItem)) {
                            if (myriadItem == ItemStack.EMPTY) {
                                if (currentSatchelStack.getItem() instanceof MyriadToolBitItem) {
                                    satchelItem.setSlot(satchelInventory, ItemStack.EMPTY);
                                } else {
                                    satchelItem.setSlot(satchelInventory, currentHotbarStack);
                                }
                            } else {
                                satchelItem.setSlot(satchelInventory, myriadItem);
                            }
                        } else {
                            if (currentSatchelStack.isEmpty()) {
                                List<ItemStack> storedStacks = new ArrayList<>(SatchelItem.getStoredStacks(satchelInventory));
                                SatchelItem.addToStoredStacks(storedStacks, currentHotbarStack);
                                SatchelItem.setStoredStacks(satchelInventory, storedStacks);
                            } else {
                                satchelItem.setSlot(satchelInventory, currentHotbarStack);
                            }
                        }
                        player.level().playSound(null, player.blockPosition(), SoundEvents.BUNDLE_INSERT, SoundSource.PLAYERS, 1.0F, 1.0F);
                    } else  {
                        satchelItem.setSlot(satchelInventory, ItemStack.EMPTY);
                    }
                } else {
                    player.level().playSound(null, player.blockPosition(), SoundEvents.BUNDLE_INSERT_FAIL, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
            }
        });
    }
}