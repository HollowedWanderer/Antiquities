package net.hollowed.antique.mixin.entities.features;

import net.hollowed.antique.client.renderer.cloth.ClothManager;
import net.hollowed.antique.util.interfaces.duck.ClothAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;
import java.util.Map;

@Mixin(ClientLevel.class)
public abstract class ClothAdderMixin implements ClothAccess {

    @Unique
    private final Map<Entity, Map<Identifier, ClothManager>> antique$cloths = new HashMap<>();

    @Override
    public Map<Entity, Map<Identifier, ClothManager>> antique$getManagers() {
        return antique$cloths;
    }

    @Override
    public void antique$tickManagers() {
        if (!Minecraft.getInstance().isPaused()) {
            for (Map<Identifier, ClothManager> managers : antique$cloths.values()) {
                for (ClothManager manager : managers.values()) {
                    manager.tickSound();

                    if (manager.render) {
                        manager.tick();
                        manager.render = false;
                    }
                }
            }
        }
    }

    @Override
    public void antique$tickParticles() {
        for (Map<Identifier, ClothManager> managers : antique$cloths.values()) {
            for (ClothManager manager : managers.values()) {
                if (manager.particles) {
                    manager.tickParticles((ClientLevel) (Object) this);
                    manager.particles = false;
                }
            }
        }
    }
}
