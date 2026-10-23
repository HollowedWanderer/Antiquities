package net.hollowed.antique.mixin.entities.living.player;

import net.hollowed.antique.index.AntiqueDataComponentTypes;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class ResetAttackCooldownComponentHandler extends Avatar implements ContainerUser {

    protected ResetAttackCooldownComponentHandler(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick(CallbackInfo ci) {
        ItemStack stack = this.getMainHandItem();
        if (stack.has(AntiqueDataComponentTypes.RESET_ATTACK_COOLDOWN)) {
            stack.remove(AntiqueDataComponentTypes.RESET_ATTACK_COOLDOWN);
            this.attackStrengthTicker = 200;
            this.itemSwapTicker = 200;
        }
    }
}
