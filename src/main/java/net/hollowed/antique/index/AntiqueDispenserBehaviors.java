package net.hollowed.antique.index;

import net.minecraft.world.level.block.DispenserBlock;

public interface AntiqueDispenserBehaviors {

    // TODO: fix smoke bomb not using firework data
    //  add behavior for tridents and myriad spades
    static void initialize() {
        DispenserBlock.registerProjectileBehavior(AntiqueItems.SMOKE_BOMB);
    }
}
