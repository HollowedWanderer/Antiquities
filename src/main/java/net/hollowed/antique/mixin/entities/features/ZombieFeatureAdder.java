package net.hollowed.antique.mixin.entities.features;

import net.hollowed.antique.util.interfaces.duck.IsHuskGetter;
import net.minecraft.client.model.monster.zombie.ZombieModel;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.world.entity.monster.zombie.Husk;
import net.minecraft.world.entity.monster.zombie.Zombie;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractZombieRenderer.class)
public abstract class ZombieFeatureAdder<T extends Zombie, S extends ZombieRenderState, M extends ZombieModel<S>>
        extends HumanoidMobRenderer<T, S, @NotNull M> {

    public ZombieFeatureAdder(EntityRendererProvider.Context context, M model, float shadowRadius) {
        super(context, model, shadowRadius);
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/monster/zombie/Zombie;Lnet/minecraft/client/renderer/entity/state/ZombieRenderState;F)V", at = @At("HEAD"))
    public void updateRenderState(T entity, S state, float partialTicks, CallbackInfo ci) {
        if (entity instanceof Husk) {
            if (state instanceof IsHuskGetter access) {
                access.antiquities$setHusk(true);
            }
        }
    }
}
