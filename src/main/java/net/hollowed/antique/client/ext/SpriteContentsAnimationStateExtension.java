package net.hollowed.antique.client.ext;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public interface SpriteContentsAnimationStateExtension {
    TextureAtlasSprite antique$getParentSprite();

    void antique$setParentSprite(TextureAtlasSprite sprite);
}
