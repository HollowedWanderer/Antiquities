package net.hollowed.antique.blocks.entities;

import net.hollowed.antique.index.AntiqueBlockEntities;
import net.hollowed.antique.util.shockwave.ShockwaveManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class ResonatorBlockEntity extends BlockEntity {
    @Nullable
    public ShockwaveManager shockwave;

    public ResonatorBlockEntity(BlockPos pos, BlockState state) {
        super(AntiqueBlockEntities.RESONATOR_BLOCK_ENTITY, pos, state);
    }
}
