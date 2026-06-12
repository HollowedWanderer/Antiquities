package net.hollowed.antique.mixin.entities.features;

import net.hollowed.antique.client.renderer.cloth.ClothManager;
import net.hollowed.antique.util.interfaces.duck.ClothAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

@Mixin(ClientLevel.class)
public abstract class ClothAdderMixin implements ClothAccess {

    @Unique
    private Map<Identifier, ClothManager> map = new HashMap<>();

    @Override
    public Map<Identifier, ClothManager> antique$getManagers() {
        return this.map;
    }

    @Override
    public void antique$tickManagers() {
        if (!Minecraft.getInstance().isPaused()) {
            for (ClothManager manager : map.values()) {
                if (manager.render) {
                    manager.tick();
                    manager.render = false;
                }
            }
        }
    }

    @Override
    public void antique$tickParticles() {
        for (ClothManager manager : map.values()) {
            if (manager.particles) {
                manager.tickParticles((ClientLevel) (Object) this);
                manager.particles = false;
            }
        }
    }
}
