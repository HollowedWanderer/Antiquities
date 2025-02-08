package net.hollowed.antique.mixin;

import net.hollowed.antique.ModKeyBindings;
import net.hollowed.antique.items.ModItems;
import net.hollowed.antique.items.custom.SatchelItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

import static net.hollowed.antique.client.gui.SatchelOverlay.SATCHEL_SELECTORS;

@Mixin(InGameHud.class)
public class SatchelOverlayMixin {
    @Inject(method = "render", at = @At("HEAD"))
    public void render(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;
        PlayerEntity player = client.player;
        assert player != null;

        ItemStack satchel = player.getEquippedStack(EquipmentSlot.LEGS);
        if (satchel.getItem() != ModItems.SATCHEL || !ModKeyBindings.showSatchel.isPressed()) return;

        int x = (context.getScaledWindowWidth() / 2);
        int y = (context.getScaledWindowHeight() / 2) + 7;

        if (satchel.getItem() instanceof SatchelItem satchelItem) {
            // Get the list of stored stacks and ensure it has a length of 8
            List<ItemStack> storedStacks = SatchelItem.getStoredStacks(satchel);
            List<ItemStack> allStacks = new ArrayList<>(storedStacks);

            if (storedStacks.isEmpty()) {
                allStacks.add(ItemStack.EMPTY);
            }

            // If the stored stacks are fewer than 8, fill the remaining slots with empty ItemStacks
            while (allStacks.size() < 8) {
                allStacks.add(ItemStack.EMPTY);
            }

            int maxRows = 2; // Always use 2 rows (4 columns each)
            int maxCols = 4;

            // Render slots and items
            for (int row = 0; row < maxRows; row++) {
                for (int col = 0; col < maxCols; col++) {
                    int index = row * 4 + col; // Calculate the correct index for the list

                    ItemStack stack = allStacks.get(index); // Get the stack (could be empty)
                    int slotX = (x - 43) + (22 * col);
                    int slotY = y + (22 * row);

                    // Render the slot background
                    context.drawTexture(
                            RenderLayer::getGuiTexturedOverlay,
                            SATCHEL_SELECTORS,
                            slotX, slotY,
                            0, 0,
                            20, 20,
                            64, 64
                    );

                    // Render the item stack and overlay
                    if (!stack.isEmpty()) {
                        context.drawItem(stack, slotX + 2, slotY + 2);
                        context.drawStackOverlay(textRenderer, stack, slotX + 2, slotY + 2);
                    }
                }
            }

            // Display the tooltip for the selected stack
            ItemStack selectedStack = allStacks.get(satchelItem.getIndex());
            if (selectedStack != null && !selectedStack.isEmpty()) {
                List<Text> textTooltip = selectedStack.getTooltip(Item.TooltipContext.create(client.world), player, TooltipType.BASIC);
                List<OrderedText> orderedTooltip = new ArrayList<>();
                for (Text text : textTooltip) {
                    orderedTooltip.add(text.asOrderedText());
                }
                context.drawTooltip(
                        textRenderer,
                        orderedTooltip,
                        (screenWidth, screenHeight, tipX, tipY, width, height) -> new Vector2i(x - (width / 2), y - (height) - 20),
                        x, y
                );
            }

            // Draw the selected stack's selector at the end to render it on top
            int selectorX = (x - 45) + (22 * (satchelItem.getIndex() <= 3 ? satchelItem.getIndex() : (satchelItem.getIndex() - 4)));
            int selectorY = (y - 2) + (22 * (satchelItem.getIndex() > 3 ? 1 : 0));

            context.drawTexture(
                    RenderLayer::getGuiTexturedOverlay,
                    SATCHEL_SELECTORS,
                    selectorX, selectorY,
                    20, 0,
                    24, 24,
                    64, 64
            );
        }
    }
}
