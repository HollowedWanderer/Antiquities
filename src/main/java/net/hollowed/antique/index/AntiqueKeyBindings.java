package net.hollowed.antique.index;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.hollowed.antique.Antiquities;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class AntiqueKeyBindings {

    public static KeyMapping.Category ANTIQUE = KeyMapping.Category.register(Antiquities.id("category.antique.keybinds"));

    // Keybindings
    public static KeyMapping showSatchel;
    public static KeyMapping crawl;

    public static void registerKeyBindings() {
        showSatchel = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.antique.show_satchel",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                ANTIQUE
        ));
        crawl = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.antique.crawl",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_ALT,
                ANTIQUE
        ));
    }

    public static void initialize() {
        registerKeyBindings();
    }
}
