package net.hollowed.antique.mixin.entities.features;

import net.hollowed.antique.client.cloth.ClothOwner;
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
    private final Map<ClothOwner, Map<Identifier, ClothManager>> antique$cloths = new HashMap<>();

    @Override
    public Map<ClothOwner, Map<Identifier, ClothManager>> antique$getManagers() {
        return antique$cloths;
    }

    @Override
    public void antique$tick() {
        for (Map<Identifier, ClothManager> managers : antique$cloths.values()) {
            managers.values().removeIf(manager -> {
                manager.tick();
                return manager.owner.isRemoved();
            });
        }
    }
}
