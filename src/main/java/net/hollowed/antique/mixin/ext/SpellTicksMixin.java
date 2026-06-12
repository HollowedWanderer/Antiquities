package net.hollowed.antique.mixin.ext;

import net.hollowed.antique.util.interfaces.duck.SpellTicksExtension;
import net.minecraft.world.entity.monster.illager.SpellcasterIllager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SpellcasterIllager.class)
public class SpellTicksMixin implements SpellTicksExtension {

    @Shadow protected int spellCastingTickCount;

    @Override
    public void antiquities$setSpellTicks(int ticks) {
        this.spellCastingTickCount = ticks;
    }
}
