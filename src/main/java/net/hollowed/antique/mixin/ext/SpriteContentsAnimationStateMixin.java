package net.hollowed.antique.mixin.ext;

import net.hollowed.antique.client.ext.SpriteContentsAnimationStateExtension;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SpriteContents.AnimationState.class)
public class SpriteContentsAnimationStateMixin implements SpriteContentsAnimationStateExtension {
    @Unique
    private TextureAtlasSprite antique$parentSprite = null;
    
    @Override
    public TextureAtlasSprite antique$getParentSprite() {
        return antique$parentSprite;
    }

    @Override
    public void antique$setParentSprite(TextureAtlasSprite sprite) {
        antique$parentSprite = sprite;
    }
}
