package net.hollowed.antique.mixin.entities.living;

import net.hollowed.antique.items.MyriadMattockBit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Sheep.class)
public abstract class SheepEntityMixin extends Animal implements Shearable {

    @Shadow public abstract boolean readyForShearing();

    @Shadow public abstract void shear(@NotNull ServerLevel level, @NotNull SoundSource soundSource, @NotNull ItemStack tool);

    protected SheepEntityMixin(EntityType<? extends Animal> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "mobInteract", at = @At("HEAD"))
    public void interact(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (itemStack.getItem() instanceof MyriadMattockBit) {
            if (this.level() instanceof ServerLevel serverWorld && this.readyForShearing()) {
                this.shear(serverWorld, SoundSource.PLAYERS, itemStack);
                this.gameEvent(GameEvent.SHEAR, player);
            }
        }
    }
}
