package net.hollowed.antique.client.sound.cloth;

import net.hollowed.antique.client.cloth.ClothOwner;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

public class AmbientClothSoundInstance extends AbstractTickableSoundInstance {
    public final ClothOwner owner;

    public AmbientClothSoundInstance(SoundEvent event, SoundSource source, ClothOwner owner) {
        super(event, source, RandomSource.create());
        this.owner = owner;

        looping = true;
        x = owner.getPosition().x;
        y = owner.getPosition().y;
        z = owner.getPosition().z;
    }

    public void publicStop() {
        stop();
    }

    @Override
    public boolean canPlaySound() {
        return !owner.isSilent();
    }

    @Override
    public void tick() {
        if (owner.isRemoved()) {
            stop();
        } else {
            x = owner.getPosition().x;
            y = owner.getPosition().y;
            z = owner.getPosition().z;
        }
    }
}
