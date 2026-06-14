package net.hollowed.antique.mixin.screens;

import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.widget.entries.ModListEntry;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.hollowed.antique.Antiquities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModListEntry.class)
public abstract class ModMenuMixin extends ObjectSelectionList.Entry<@NotNull ModListEntry> {

    @Shadow @Final public Mod mod;

    @Shadow @Final protected Minecraft client;

    @Shadow public abstract int getXOffset();

    @Shadow public abstract int getYOffset();

    @Inject(
            method = "renderContent",
            at = @At("TAIL")
    )
    private void modifyModNameColor(
            GuiGraphics drawContext, int mouseX, int mouseY, boolean hovered, float delta, CallbackInfo ci
    ) {
        String modId = this.mod.getId();
        int iconSize = ModMenuConfig.COMPACT_LIST.getValue() ? 19 : 32;

        int x = this.getX() + this.getXOffset();
        int y = this.getContentY() + this.getYOffset();
        int rowWidth = this.getContentWidth();

        // Custom color logic
        int nameColor = 0xFFAA2F54;

        Component name = Component.literal(this.mod.getTranslatedName());
        FormattedText trimmedName = name;
        int maxNameWidth = rowWidth - iconSize - 3;
        Font font = this.client.font;
        if (font.width(name) > maxNameWidth) {
            FormattedText ellipsis = FormattedText.of("...");
            trimmedName = FormattedText.composite(font.substrByWidth(name, maxNameWidth - font.width(ellipsis)), ellipsis);
        }

        if ("antique".equals(modId)) {
            // Modify the text rendering with a new color
            drawContext.drawString(
                    this.client.font,
                    Language.getInstance().getVisualOrder(trimmedName),
                    x + iconSize + 3,
                    y + 1,
                    nameColor
            );

            // Draws the small icon
            drawContext.blit(RenderPipelines.GUI_TEXTURED, Antiquities.id("antiquities_small_icon.png"), x + iconSize + 53, y - 3, 0, 0, 16, 16, 16, 16);

            // Draws the colored line below the one line of text
            drawContext.blit(RenderPipelines.GUI_TEXTURED, Antiquities.id("antiquities_line.png"), x + iconSize + 3, y + 22, 0, 0, 76, 1, 76, 1);

            // Draws the H signature
            drawContext.blit(RenderPipelines.GUI_TEXTURED, Antiquities.id("h.png"), rowWidth - 2, y, 0, 0, 16, 16, 16, 16);
        }
    }
}
