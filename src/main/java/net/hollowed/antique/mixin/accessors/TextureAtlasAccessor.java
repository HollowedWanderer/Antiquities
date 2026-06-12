package net.hollowed.antique.mixin.accessors;

import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(TextureAtlas.class)
public interface TextureAtlasAccessor {
    @Accessor("animatedTexturesStates")
    List<SpriteContents.AnimationState> antique$getAnimatedTexturesStates();
}
