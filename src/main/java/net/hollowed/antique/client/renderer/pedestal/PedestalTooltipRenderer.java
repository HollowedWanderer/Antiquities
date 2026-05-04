package net.hollowed.antique.client.renderer.pedestal;

import net.hollowed.antique.blocks.PedestalBlock;
import net.hollowed.antique.blocks.entities.PedestalBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import java.util.List;

public class PedestalTooltipRenderer {

    private static void renderItemTooltip(GuiGraphics context, ItemStack itemStack, int screenWidth, int screenHeight) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        // Get the tooltip text
        List<Component> textList = itemStack.getTooltipLines(Item.TooltipContext.EMPTY, client.player, client.options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL);
        List<ClientTooltipComponent> components = new java.util.ArrayList<>();

        for (Component text : textList) {
            components.add(ClientTooltipComponent.create(text.getVisualOrderText()));
        }

        // Tooltip positioning (slightly to the side of the crosshair)
        int posX = screenWidth / 2 + 8;
        int posY = screenHeight / 2 - textList.size() * 10 / 2;

        // Render the tooltip
        context.renderTooltip(client.font, components, posX, posY, DefaultTooltipPositioner.INSTANCE, itemStack.get(DataComponents.TOOLTIP_STYLE));
    }

    public static void renderTooltip(GuiGraphics context, int screenWidth, int screenHeight) {

        Minecraft client = Minecraft.getInstance();

        // Check if player is looking at block
        HitResult hitResult = client.hitResult;
        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) return;

        // Check if the block is a pedestal
        BlockHitResult blockHitResult = (BlockHitResult) hitResult;
        Level world = client.level;
        if (world == null) return;

        if (!(world.getBlockState(blockHitResult.getBlockPos()).getBlock() instanceof PedestalBlock)) return;

        // Get the item on the pedestal
        if (world.getBlockEntity(blockHitResult.getBlockPos()) instanceof PedestalBlockEntity pedestalEntity) {
            ItemStack itemStack = pedestalEntity.getItem(0);
            if (itemStack.isEmpty()) return;

            // Render the tooltip
            renderItemTooltip(context, itemStack, screenWidth, screenHeight);
        }
    }
}
