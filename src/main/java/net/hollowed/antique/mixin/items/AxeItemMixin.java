package net.hollowed.antique.mixin.items;

import net.hollowed.antique.util.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.hollowed.antique.index.AntiqueParticles;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(AxeItem.class)
public class AxeItemMixin {

    @Inject(method = "evaluateNewBlockState", at = @At("HEAD"), cancellable = true)
    private void tryStrip(Level level, BlockPos pos, @Nullable Player player, BlockState oldState, CallbackInfoReturnable<Optional<BlockState>> cir) {
        Optional<BlockState> optional = getPreviousTarnishLevel(oldState);
        if (optional.isPresent()) {
            level.playSound(player, pos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0F, 1.0F);
            ParticleUtils.spawnParticlesOnBlockFaces(level, pos, AntiqueParticles.SCRAPE, UniformInt.of(3, 5));
            cir.setReturnValue(optional);
        } else {
            Optional<BlockState> optional2 = getUncoat(oldState);
            if (optional2.isPresent()) {
                level.playSound(player, pos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.levelEvent(player, LevelEvent.PARTICLES_WAX_OFF, pos, 0);
                cir.setReturnValue(optional2);
            }
        }
    }

    @Unique
    private Optional<BlockState> getPreviousTarnishLevel(BlockState state) {
        return Optional.ofNullable(BlockUtil.TARNISHING_BLOCKS_REVERSE.get(state.getBlock()))
                .map(Block::defaultBlockState);
    }

    @Unique
    private Optional<BlockState> getUncoat(BlockState state) {
        return Optional.ofNullable(BlockUtil.COATED_MYRIAD_BLOCKS_REVERSE.get(state.getBlock()))
                .map(Block::defaultBlockState);
    }
}
