package net.hollowed.antique.mixin.entities.living;

import net.hollowed.antique.index.AntiqueItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class ArmorSoundSuppressorMixin {

    @Shadow public abstract boolean hasInfiniteMaterials();

    @Inject(method = "onEquipItem", at = @At("HEAD"), cancellable = true)
    public void onEquip(EquipmentSlot slot, ItemStack oldStack, ItemStack stack, CallbackInfo ci) {
        if (this.hasInfiniteMaterials() && oldStack.is(AntiqueItems.SATCHEL) && stack.is(AntiqueItems.SATCHEL)) ci.cancel();
    }
}
