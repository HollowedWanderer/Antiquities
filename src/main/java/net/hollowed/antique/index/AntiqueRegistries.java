package net.hollowed.antique.index;

import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.hollowed.antique.Antiquities;
import net.hollowed.antique.util.resources.ClothSkin;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public interface AntiqueRegistries {
    ResourceKey<Registry<ClothSkin>> CLOTHS = ResourceKey.createRegistryKey(Antiquities.id("cloths"));

    static void initialize() {
        DynamicRegistries.registerSynced(CLOTHS, ClothSkin.CODEC);
    }
}
