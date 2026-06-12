package net.hollowed.antique.util.interfaces.duck;

import net.hollowed.antique.client.renderer.cloth.ClothManager;
import net.minecraft.resources.Identifier;
import java.util.Map;

public interface ClothAccess {
    Map<Identifier, ClothManager> antique$getManagers();

    void antique$tickManagers();

    void antique$tickParticles();
}
