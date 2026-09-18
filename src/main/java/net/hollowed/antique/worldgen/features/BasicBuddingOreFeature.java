package net.hollowed.antique.worldgen.features;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.feature.BlockReplacement;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.NonNull;

public class BasicBuddingOreFeature extends AbstractBuddingOreFeature {
    public static final MapCodec<BasicBuddingOreFeature> CODEC = makeCodec(BasicBuddingOreFeature::new);

    public BasicBuddingOreFeature(final List<Pair<BlockReplacement, BlockReplacement>> targetStates, final int size, final float discardChanceOnAirExposure) {
        super(targetStates, size, discardChanceOnAirExposure);
    }

    public @NonNull MapCodec<BasicBuddingOreFeature> codec() {
        return CODEC;
    }

    public boolean place(final @NonNull WorldGenLevel level, final @NonNull ChunkGenerator chunkGenerator, final RandomSource random, final BlockPos origin) {
        float dir = random.nextFloat() * (float)Math.PI;
        float spreadXY = (float)this.size / 8.0F;
        int maxRadius = Mth.ceil(((float)this.size / 16.0F * 2.0F + 1.0F) / 2.0F);
        double x0 = (double)origin.getX() + Math.sin(dir) * (double)spreadXY;
        double x1 = (double)origin.getX() - Math.sin(dir) * (double)spreadXY;
        double z0 = (double)origin.getZ() + Math.cos(dir) * (double)spreadXY;
        double z1 = (double)origin.getZ() - Math.cos(dir) * (double)spreadXY;
        double y0 = origin.getY() + random.nextInt(3) - 2;
        double y1 = origin.getY() + random.nextInt(3) - 2;
        int xStart = origin.getX() - Mth.ceil(spreadXY) - maxRadius;
        int yStart = origin.getY() - 2 - maxRadius;
        int zStart = origin.getZ() - Mth.ceil(spreadXY) - maxRadius;
        int sizeXZ = 2 * (Mth.ceil(spreadXY) + maxRadius);
        int sizeY = 2 * (2 + maxRadius);

        for(int xprobe = xStart; xprobe <= xStart + sizeXZ; ++xprobe) {
            for(int zprobe = zStart; zprobe <= zStart + sizeXZ; ++zprobe) {
                if (yStart <= level.getHeight(Types.OCEAN_FLOOR_WG, xprobe, zprobe)) {
                    return this.doPlace(level, random, x0, x1, z0, z1, y0, y1, xStart, yStart, zStart, sizeXZ, sizeY);
                }
            }
        }

        return false;
    }

    protected boolean doPlace(final WorldGenLevel level, final RandomSource random, final double x0, final double x1, final double z0, final double z1, final double y0, final double y1, final int xStart, final int yStart, final int zStart, final int sizeXZ, final int sizeY) {
        int placed = 0;
        BitSet tested = new BitSet(sizeXZ * sizeY * sizeXZ);
        BlockPos.MutableBlockPos orePos = new BlockPos.MutableBlockPos();
        double[] data = new double[this.size * 4];

        for(int i = 0; i < this.size; ++i) {
            float step = (float)i / (float)this.size;
            double xx = Mth.lerp(step, x0, x1);
            double yy = Mth.lerp(step, y0, y1);
            double zz = Mth.lerp(step, z0, z1);
            double ss = random.nextDouble() * (double)this.size / (double)16.0F;
            double r = ((double)(Mth.sin((float)Math.PI * step) + 1.0F) * ss + (double)1.0F) / (double)2.0F;
            data[i * 4] = xx;
            data[i * 4 + 1] = yy;
            data[i * 4 + 2] = zz;
            data[i * 4 + 3] = r;
        }

        for(int i1 = 0; i1 < this.size - 1; ++i1) {
            if (!(data[i1 * 4 + 3] <= (double)0.0F)) {
                for(int i2 = i1 + 1; i2 < this.size; ++i2) {
                    if (!(data[i2 * 4 + 3] <= (double)0.0F)) {
                        double dx = data[i1 * 4] - data[i2 * 4];
                        double dy = data[i1 * 4 + 1] - data[i2 * 4 + 1];
                        double dz = data[i1 * 4 + 2] - data[i2 * 4 + 2];
                        double dr = data[i1 * 4 + 3] - data[i2 * 4 + 3];
                        if (dr * dr > dx * dx + dy * dy + dz * dz) {
                            if (dr > (double)0.0F) {
                                data[i2 * 4 + 3] = -1.0F;
                            } else {
                                data[i1 * 4 + 3] = -1.0F;
                            }
                        }
                    }
                }
            }
        }

        try (BulkSectionAccess sectionGetter = new BulkSectionAccess(level)) {
            for(int i = 0; i < this.size; ++i) {
                double r = data[i * 4 + 3];
                if (!(r < (double)0.0F)) {
                    double xx = data[i * 4];
                    double yy = data[i * 4 + 1];
                    double zz = data[i * 4 + 2];
                    int xMin = Math.max(Mth.floor(xx - r), xStart);
                    int yMin = Math.max(Mth.floor(yy - r), yStart);
                    int zMin = Math.max(Mth.floor(zz - r), zStart);
                    int xMax = Math.max(Mth.floor(xx + r), xMin);
                    int yMax = Math.max(Mth.floor(yy + r), yMin);
                    int zMax = Math.max(Mth.floor(zz + r), zMin);

                    for(int x = xMin; x <= xMax; ++x) {
                        double xd = ((double)x + (double)0.5F - xx) / r;
                        if (xd * xd < (double)1.0F) {
                            for(int y = yMin; y <= yMax; ++y) {
                                double yd = ((double)y + (double)0.5F - yy) / r;
                                if (xd * xd + yd * yd < (double)1.0F) {
                                    for(int z = zMin; z <= zMax; ++z) {
                                        double zd = ((double)z + (double)0.5F - zz) / r;
                                        if (xd * xd + yd * yd + zd * zd < (double)1.0F && !level.isOutsideBuildHeight(y)) {
                                            int bitSetIndex = x - xStart + (y - yStart) * sizeXZ + (z - zStart) * sizeXZ * sizeY;
                                            if (!tested.get(bitSetIndex)) {
                                                tested.set(bitSetIndex);
                                                orePos.set(x, y, z);
                                                if (level.ensureCanWrite(orePos)) {
                                                    LevelChunkSection section = sectionGetter.getSection(orePos);
                                                    if (section != null) {
                                                        int sectionRelativeX = SectionPos.sectionRelative(x);
                                                        int sectionRelativeY = SectionPos.sectionRelative(y);
                                                        int sectionRelativeZ = SectionPos.sectionRelative(z);
                                                        BlockState blockState = section.getBlockState(sectionRelativeX, sectionRelativeY, sectionRelativeZ);

                                                        for(Pair<BlockReplacement, BlockReplacement> targetStates : this.targetStates) {
                                                            Objects.requireNonNull(sectionGetter);
                                                            if (this.canPlaceOre(blockState, sectionGetter::getBlockState, random, targetStates.getFirst(), orePos)) {
                                                                section.setBlockState(sectionRelativeX, sectionRelativeY, sectionRelativeZ, targetStates.getFirst().state(), false);
                                                                ++placed;

                                                                placeClusters(sectionGetter, blockState, random, targetStates.getSecond().state(), targetStates.getSecond(), x, y, z);
                                                                break;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return placed > 0;
    }

    public void placeClusters(BulkSectionAccess sectionGetter, BlockState blockState, RandomSource random, BlockState cluster, BlockReplacement clusterReplacement, int x1, int y1, int z1) {
        for (Direction direction : Direction.values()) {
            BlockPos blockPos = new BlockPos(x1, y1, z1).offset(direction.getUnitVec3i());

            LevelChunkSection chunkSection = sectionGetter.getSection(blockPos);
            if (chunkSection != null) {

                int x = SectionPos.sectionRelative(blockPos.getX());
                int y = SectionPos.sectionRelative(blockPos.getY());
                int z = SectionPos.sectionRelative(blockPos.getZ());

                cluster = cluster.setValue(AmethystClusterBlock.FACING, direction)
                        .setValue(AmethystClusterBlock.WATERLOGGED, chunkSection.getFluidState(x, y, z).getType() == Fluids.WATER);

                if (this.canPlaceOre(blockState, sectionGetter::getBlockState, random, clusterReplacement, blockPos.mutable()) && Math.random() < 0.3) {
                    chunkSection.setBlockState(x, y, z, cluster, false);
                }
            }
        }
    }
}