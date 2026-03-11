package net.hollowed.antique.mixin.entities.features;

import net.hollowed.antique.client.renderer.cloth.ClothManager;
import net.hollowed.antique.util.interfaces.duck.ClothAccess;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;
import java.util.Map;

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
        for (ClothManager manager : map.values()) {
            manager.tick();
        }
    }
}
