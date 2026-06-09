package net.hollowed.antique.mixin.screens;

import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.entries.ModListEntry;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.hollowed.antique.Antiquities;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModsScreen.class)
public abstract class ModMenuEntryMixin extends Screen {

    @Shadow private ModListEntry selected;

    @Shadow private int rightPaneX;

    protected ModMenuEntryMixin(Component title) {
        super(title);
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void render(GuiGraphics DrawContext, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ModListEntry selectedEntry = this.selected;
        if (selectedEntry != null) {
            Mod mod = selectedEntry.getMod();
            int imageOffset = 36;
            Component name = Component.literal(mod.getTranslatedName());
            FormattedText trimmedName = name;
            int maxNameWidth = this.width - (this.rightPaneX + imageOffset);
            if (this.font.width(name) > maxNameWidth) {
                FormattedText ellipsis = FormattedText.of("...");
                trimmedName = FormattedText.composite(this.font.substrByWidth(name, maxNameWidth - this.font.width(ellipsis)), ellipsis);
            }

            // Custom color logic
            int nameColor = 0xFFAA2F54;

            if ("antique".equals(mod.getId())) {
                DrawContext.drawString(this.font, Language.getInstance().getVisualOrder(trimmedName), this.rightPaneX + imageOffset, 49, nameColor, true);
                DrawContext.blit(RenderPipelines.GUI_TEXTURED, Antiquities.id("antiquities_small_icon.png"), this.rightPaneX + imageOffset + 50, 45, 0, 0, 16, 16, 16, 16);
            }
        }
    }
}
