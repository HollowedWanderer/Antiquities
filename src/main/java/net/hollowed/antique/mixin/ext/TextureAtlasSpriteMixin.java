package net.hollowed.antique.mixin.ext;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.hollowed.antique.util.interfaces.duck.SpriteContentsAnimationStateExtension;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TextureAtlasSprite.class)
public class TextureAtlasSpriteMixin {
    @ModifyReturnValue(
            method = "createAnimationState",
            at = @At("RETURN")
    )
    private SpriteContents.AnimationState createAnimationState(SpriteContents.AnimationState original) {
        ((SpriteContentsAnimationStateExtension) original).antique$setParentSprite((TextureAtlasSprite) (Object) this);
        return original;
    }
}
