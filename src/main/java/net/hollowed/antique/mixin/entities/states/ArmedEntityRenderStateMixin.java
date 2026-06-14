package net.hollowed.antique.mixin.entities.states;

import net.hollowed.antique.util.interfaces.duck.ArmedRenderStateAccess;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmedEntityRenderState.class)
public class ArmedEntityRenderStateMixin implements ArmedRenderStateAccess {

    @Unique
    private Entity antique$entity;
    @Unique
    private Identifier antique$clothId;

    @Inject(method = "extractArmedEntityRenderState", at = @At("HEAD"))
    private static void updateRenderState(LivingEntity livingEntity, ArmedEntityRenderState armedEntityRenderState, ItemModelResolver itemModelResolver, float f, CallbackInfo ci) {
        if (armedEntityRenderState instanceof ArmedRenderStateAccess access) {
            access.antique$setEntity(livingEntity);
        }
    }

    @Override
    public void antique$setEntity(Entity entity) {
        this.antique$entity = entity;
    }

    @Override
    public Entity antique$getEntity() {
        return this.antique$entity;
    }

    @Override
    public Identifier antique$getClothId() {
        return antique$clothId;
    }

    @Override
    public void antique$setClothId(Identifier id) {
        antique$clothId = id;
    }
}
