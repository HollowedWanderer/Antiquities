package net.hollowed.antique.util.interfaces.duck;

import net.hollowed.antique.client.cloth.ClothOwner;
import net.hollowed.antique.client.renderer.cloth.ClothManager;
import net.minecraft.resources.Identifier;

import java.util.Map;

public interface ClothAccess {
    Map<ClothOwner, Map<Identifier, ClothManager>> antique$getManagers();

    void antique$tick();
}
