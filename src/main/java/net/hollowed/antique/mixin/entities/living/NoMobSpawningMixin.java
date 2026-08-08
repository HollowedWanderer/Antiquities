package net.hollowed.antique.mixin.entities.living;

import net.hollowed.antique.blocks.entities.OrnateBellBlockEntity;
import net.hollowed.antique.index.AntiquePoiTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.stream.Stream;

@Mixin(Mob.class)
public class NoMobSpawningMixin {

    @Inject(method = "checkMobSpawnRules", at = @At("HEAD"), cancellable = true)
    private static void preventBellSpawnsMob(
            EntityType<? extends Animal> type,
            LevelAccessor level,
            EntitySpawnReason spawnReason,
            BlockPos pos,
            RandomSource random,
            CallbackInfoReturnable<Boolean> cir
    ) {
        int spawnPreventionRadius = 64;

        if (level instanceof ServerLevel serverLevel) {
            Stream<BlockPos> pairs = serverLevel.getPoiManager().findAll(
                    p -> p.is(AntiquePoiTypes.EMBLAZONED_BELL_KEY), _ -> true, pos, spawnPreventionRadius, PoiManager.Occupancy.ANY
            ).map(BlockPos::immutable);

            for (BlockPos bellPos : pairs.toList()) {
                if (level.getBlockEntity(bellPos) instanceof OrnateBellBlockEntity ornateBell && ornateBell.blazing) {
                    cir.setReturnValue(false);
                }
            }
        }
    }
}