package net.hollowed.antique.client.sound.cloth;

import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

public class AmbientClothSoundInstance extends EntityBoundSoundInstance {
    public AmbientClothSoundInstance(SoundEvent event, SoundSource source, Entity owner) {
        super(event, source, 1, 1, owner, owner.getRandom().nextLong());
        looping = true;
    }

    public void publicStop() {
        stop();
    }
}
