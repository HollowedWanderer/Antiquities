package net.hollowed.antique.mixin.entities.features;

import net.hollowed.antique.util.interfaces.duck.IsWitherGetter;
import net.minecraft.client.model.monster.skeleton.SkeletonModel;
import net.minecraft.client.renderer.entity.AbstractSkeletonRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSkeletonRenderer.class)
public abstract class SkeletonFeatureAdder<T extends AbstractSkeleton, S extends SkeletonRenderState>
        extends HumanoidMobRenderer<T, S, @NotNull SkeletonModel<S>> {

    public SkeletonFeatureAdder(EntityRendererProvider.Context context, SkeletonModel<S> model, float shadowRadius) {
        super(context, model, shadowRadius);
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/monster/skeleton/AbstractSkeleton;Lnet/minecraft/client/renderer/entity/state/SkeletonRenderState;F)V", at = @At("HEAD"))
    public void updateRenderState(T entity, S state, float partialTicks, CallbackInfo ci) {
        if (entity instanceof WitherSkeleton) {
            if (state instanceof IsWitherGetter access) {
                access.antiquities$setWither(true);
            }
        }
    }
}
