package net.hollowed.antique.mixin.items;

import net.hollowed.antique.index.AntiqueEntities;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.hollowed.antique.entities.CakeEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class CakeThrowMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void use(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (player.getItemInHand(hand).is(Items.CAKE)) {
            ItemStack stack = player.getItemInHand(hand);
            player.swing(hand, true);
            if (!level.isClientSide()) {
                CakeEntity cake = new CakeEntity(AntiqueEntities.CAKE_ENTITY, level);
                cake.setPosRaw(player.getX(), player.getY() + 1.5, player.getZ());
                cake.setDeltaMovement(player.getLookAngle().scale(0.75));
                cake.absSnapRotationTo(-player.getYHeadRot(), -player.getXRot());
                level.addFreshEntity(cake);

                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WIND_CHARGE_THROW, SoundSource.NEUTRAL, 0.5F, 0.1F);
            }
            stack.consume(1, player);
            player.awardStat(Stats.ITEM_USED.get((Item) (Object) this));
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }
}
