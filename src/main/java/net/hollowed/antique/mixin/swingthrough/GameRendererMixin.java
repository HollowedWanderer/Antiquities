// **********************************************
// This file is licensed under:
// GNU Lesser General Public License v3.0 only
// From the mod "SwingThrough" located here: https://modrinth.com/mod/swingthrough
// **********************************************

package net.hollowed.antique.mixin.swingthrough;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LocalPlayer.class)
public class GameRendererMixin {

    @Unique
    private static boolean validBlock = false;
    @Unique
    private static boolean validEntity = false;

    @ModifyVariable(method = "pick", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/phys/HitResult;getLocation()Lnet/minecraft/world/phys/Vec3;"), name = "blockHitResult")
    private static HitResult getReach(HitResult blockHitResult, Entity cameraEntity, double blockInteractionRange, double entityInteractionRange, float partialTicks) {
        validBlock = blockHitResult.getType() == HitResult.Type.BLOCK && blockHitResult instanceof BlockHitResult && (Minecraft.getInstance().level != null && Minecraft.getInstance().level.getBlockState(((BlockHitResult) blockHitResult).getBlockPos()).getCollisionShape(Minecraft.getInstance().level, ((BlockHitResult) blockHitResult).getBlockPos()).isEmpty());
        return blockHitResult;
    }

    @ModifyReceiver(method = "pick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/HitResult;getType()Lnet/minecraft/world/phys/HitResult$Type;"))
    private static HitResult treatTransparentAsMissed(HitResult instance) {
        return validBlock ? BlockHitResult.miss(instance.getLocation(), Direction.EAST, BlockPos.containing(instance.getLocation())) : instance;
    }

    @SuppressWarnings("all")
    @ModifyVariable(method = "pick", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;"), name = "entityHitResult")
    private static EntityHitResult checkEntityValid(EntityHitResult entityHitResult, Entity cameraEntity, double blockInteractionRange, double entityInteractionRange, float partialTicks) {
        Entity hitEntity = entityHitResult == null ? null : entityHitResult.getEntity();
        validEntity = hitEntity != null && Minecraft.getInstance().player != null && Minecraft.getInstance().getCameraEntity() != null && Minecraft.getInstance().getCameraEntity().position().distanceToSqr(hitEntity.position()) < Mth.square(entityInteractionRange) && hitEntity instanceof LivingEntity && !hitEntity.isSpectator() && hitEntity.isAttackable() && !hitEntity.equals(Minecraft.getInstance().player.getVehicle() != null ? Minecraft.getInstance().player.getVehicle() : entityHitResult);
        return entityHitResult;
    }

    @ModifyVariable(method = "pick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;distanceToSqr(Lnet/minecraft/world/phys/Vec3;)D", ordinal = 1), name = "blockDistanceSq")
    private static double ignoreBlockHit(double blockDistanceSq) {
        return validBlock && validEntity ? Double.MAX_VALUE : blockDistanceSq;
    }
}
