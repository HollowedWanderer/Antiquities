package net.hollowed.antique.mixin.entities.living;

import net.hollowed.antique.index.AntiqueItems;
import net.hollowed.antique.index.AntiqueSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class ItemEntityMixin {

    @Shadow
    private Level level;

    @Shadow
    private Vec3 position;

    @Shadow
    public double fallDistance;

    @Inject(method = "checkFallDamage", at = @At("HEAD"))
    private void metalPipe(double ya, boolean onGround, BlockState onState, BlockPos pos, CallbackInfo ci) {
        if (((Entity) (Object) this instanceof ItemEntity item && item.getItem().is(AntiqueItems.BILLET_ROD)) && onGround && this.fallDistance > 0.0) {
            this.level.playSound(null, this.position.x, this.position.y, this.position.z, AntiqueSounds.METAL_PIPE, SoundSource.PLAYERS);
        }
    }
}
