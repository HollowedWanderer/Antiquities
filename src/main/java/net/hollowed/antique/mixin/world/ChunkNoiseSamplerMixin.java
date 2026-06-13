package net.hollowed.antique.mixin.world;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.hollowed.antique.worldgen.features.AntiqueOreVeinSampler;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(NoiseChunk.class)
public abstract class ChunkNoiseSamplerMixin {

    @Shadow protected abstract DensityFunction wrap(DensityFunction function);

    @Unique
    private ArrayList<NoiseChunk.BlockStateFiller> list;

    @WrapOperation(method = "<init>", at = @At(value = "NEW", target = "()Ljava/util/ArrayList;"))
    private ArrayList<NoiseChunk.BlockStateFiller> getList(Operation<ArrayList<NoiseChunk.BlockStateFiller>> original) {
        this.list = original.call();
        return list;
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 1))
    private void addToList(int horizontalCellCount, RandomState noiseConfig, int startBlockX, int startBlockZ, NoiseSettings generationShapeConfig, DensityFunctions.BeardifierOrMarker beardifying, NoiseGeneratorSettings chunkGeneratorSettings, Aquifer.FluidPicker fluidLevelSampler, Blender blender, CallbackInfo ci) {
        NoiseRouter noiseRouter = noiseConfig.router();
        NoiseRouter noiseRouter2 = noiseRouter.mapAll(this::wrap);

        if (chunkGeneratorSettings.oreVeinsEnabled()) {
            list.add(AntiqueOreVeinSampler.create(noiseRouter2.veinToggle(), noiseRouter2.veinRidged(), noiseRouter2.veinGap(), noiseConfig.oreRandom()));
        }
    }
}
