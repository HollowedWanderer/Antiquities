package net.hollowed.antique.util.interfaces.duck;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public interface SpriteContentsAnimationStateExtension {
    TextureAtlasSprite antique$getParentSprite();

    void antique$setParentSprite(TextureAtlasSprite sprite);
}
