package net.hollowed.antique.util.interfaces.duck;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

public interface ArmedRenderStateAccess {
    Entity antique$getEntity();

    void antique$setEntity(Entity entity);

    Identifier antique$getClothId();

    void antique$setClothId(Identifier id);
}
