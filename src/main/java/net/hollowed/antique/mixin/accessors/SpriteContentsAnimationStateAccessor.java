package net.hollowed.antique.mixin.accessors;

import net.minecraft.client.renderer.texture.SpriteContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SpriteContents.AnimationState.class)
public interface SpriteContentsAnimationStateAccessor {
    @Accessor("frame")
    int antique$getFrame();

    @Accessor("subFrame")
    int antique$getSubFrame();
}
