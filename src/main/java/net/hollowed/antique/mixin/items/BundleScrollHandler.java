package net.hollowed.antique.mixin.items;

import net.minecraft.client.gui.BundleMouseActions;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Debug(export = true)
@Mixin(BundleMouseActions.class)
public class BundleScrollHandler {

    @ModifyVariable(method = "onMouseScrolled", at = @At(value = "STORE", ordinal = 0), name = "wheel")
    private int modifyScrollAmount(int wheel) {
        return wheel * -1;
    }
}