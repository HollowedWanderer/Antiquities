package net.hollowed.antique.mixin.world;

import com.llamalad7.mixinextras.sugar.Local;
import net.hollowed.antique.index.AntiqueBlockTags;
import net.hollowed.antique.index.AntiqueBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoiseBasedChunkGenerator.class)
public class NoiseChunkGeneratorMixin {

        @Inject(method = "doFill(Lnet/minecraft/world/level/levelgen/blending/Blender;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/chunk/ChunkAccess;II)Lnet/minecraft/world/level/chunk/ChunkAccess;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunkSection;setBlockState(IIILnet/minecraft/world/level/block/state/BlockState;Z)Lnet/minecraft/world/level/block/state/BlockState;"))
    private void placeClusters(Blender blender, StructureManager structureAccessor, RandomState noiseConfig, ChunkAccess chunk, int minimumCellY, int cellHeight, CallbackInfoReturnable<ChunkAccess> cir, @Local LevelChunkSection chunkSection, @Local BlockState state, @Local(ordinal = 17) int x1, @Local(ordinal = 13) int y1, @Local(ordinal = 20) int z1) {
        if (state.getBlock().equals(AntiqueBlocks.MYRIAD_ORE) || state.getBlock().equals(AntiqueBlocks.DEEPSLATE_MYRIAD_ORE)) {
            for (Direction direction : Direction.values()) {
                BlockPos blockPos = new BlockPos(x1, y1, z1).offset(direction.getUnitVec3i());

                if (chunkSection != null) {

                    int originX = SectionPos.sectionRelative(x1);
                    int originY = SectionPos.sectionRelative(y1);
                    int originZ = SectionPos.sectionRelative(z1);

                    int x = SectionPos.sectionRelative(blockPos.getX());
                    int y = SectionPos.sectionRelative(blockPos.getY());
                    int z = SectionPos.sectionRelative(blockPos.getZ());

                    BlockState cluster = state.getBlock().equals(AntiqueBlocks.MYRIAD_ORE) ? AntiqueBlocks.MYRIAD_CLUSTER.defaultBlockState() : AntiqueBlocks.DEEPSLATE_MYRIAD_CLUSTER.defaultBlockState();

                    cluster = cluster.setValue(AmethystClusterBlock.FACING, direction)
                            .setValue(AmethystClusterBlock.WATERLOGGED, chunkSection.getFluidState(x, y, z).getType() == Fluids.WATER);

                    boolean notOnBorder = Math.abs(originX - x) <= 1
                            && Math.abs(originY - y) <= 1
                            && Math.abs(originZ - z) <= 1;

                    if (chunkSection.getBlockState(x, y, z).is(AntiqueBlockTags.WATER_OR_AIR) && Math.random() < 0.3 && notOnBorder) {
                        chunkSection.setBlockState(x, y, z, cluster, false);
                    }
                }
            }
        }
    }
}
