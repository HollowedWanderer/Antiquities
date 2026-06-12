package net.hollowed.antique.mixin.entities.features;

import net.hollowed.antique.client.renderer.cloth.ClothManager;
import net.hollowed.antique.util.interfaces.duck.ClothAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;
import java.util.Map;

@Mixin(ClientLevel.class)
public abstract class ClothAdderMixin implements ClothAccess {

    @Unique
    private final Map<Identifier, ClothManager> antique$cloths = new HashMap<>();

    @Override
    public Map<Identifier, ClothManager> antique$getManagers() {
        return antique$cloths;
    }

    @Override
    public void antique$tickManagers() {
        if (!Minecraft.getInstance().isPaused()) {
            antique$cloths.values().removeIf(manager -> {
                if (manager.render) {
                    manager.tick();
                    manager.render = false;
                    return false;
                } else {
                    return true;
                }
            });
        }
    }

    @Override
    public void antique$tickParticles() {
        for (ClothManager manager : antique$cloths.values()) {
            manager.tickParticles((ClientLevel) (Object) this);
        }
    }
}
