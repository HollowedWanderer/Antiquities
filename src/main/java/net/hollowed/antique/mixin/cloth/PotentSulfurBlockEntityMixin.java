package net.hollowed.antique.mixin.cloth;

import com.llamalad7.mixinextras.sugar.Local;
import net.hollowed.antique.client.renderer.cloth.ClothBody;
import net.hollowed.antique.util.interfaces.duck.ClothAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.PotentSulfurBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PotentSulfurBlockEntity.class)
public class PotentSulfurBlockEntityMixin {
    @Inject(
            method = "lambda$static$5",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;"
            )
    )
    private static void launch(
            Level level,
            BlockPos pos,
            BlockState state,
            PotentSulfurBlockEntity entity,
            CallbackInfo ci,
            @Local AABB aabb
    ) {
        if (level instanceof ClothAccess access) {
            access.antique$getManagers().forEach((owner, cloths) -> {
                if (owner.getLevel() == level) {
                    cloths.forEach((id, cloth) -> {
                        for (ClothBody body : cloth.bodies) {
                            if (aabb.contains(body.pos.x, body.pos.y, body.pos.z)) {
                                body.velocity.add(0, 0.5f, 0);
                            }
                        }
                    });
                }
            });
        }
    }
}
