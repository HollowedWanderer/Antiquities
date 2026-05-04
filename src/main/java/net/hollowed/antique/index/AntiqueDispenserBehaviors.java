package net.hollowed.antique.index;

import net.hollowed.antique.items.dispenser.SmokeBombDispenseBehavior;
import net.minecraft.world.level.block.DispenserBlock;

public interface AntiqueDispenserBehaviors {

    static void initialize() {
        DispenserBlock.registerBehavior(AntiqueItems.SMOKE_BOMB, new SmokeBombDispenseBehavior(AntiqueItems.SMOKE_BOMB));
    }
}
