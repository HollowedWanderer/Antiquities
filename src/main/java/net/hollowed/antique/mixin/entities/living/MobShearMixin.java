package net.hollowed.antique.mixin.entities.living;

import net.hollowed.antique.index.AntiqueItems;
import net.hollowed.antique.mixin.accessors.CanRemoveSaddleAccessor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobShearMixin {

    @Shadow
    protected abstract boolean attemptToShearEquipment(Player player, InteractionHand hand, ItemStack heldItem);

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void shear(Player player, InteractionHand hand, Vec3 location, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack itemStack = player.getItemInHand(hand);
        if ((Entity) (Object) this instanceof CanRemoveSaddleAccessor accessor) {
            if (itemStack.is(AntiqueItems.MYRIAD_PICK_HEAD) && accessor.canRemoveSaddle(player) && !player.isSecondaryUseActive() && this.attemptToShearEquipment(player, hand, itemStack)) {
                cir.setReturnValue(InteractionResult.SUCCESS);
            }
        }
    }
}
