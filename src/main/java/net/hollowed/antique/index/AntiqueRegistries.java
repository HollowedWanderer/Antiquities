package net.hollowed.antique.index;

import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.hollowed.antique.Antiquities;
import net.hollowed.antique.util.resources.ClothOverlayData;
import net.hollowed.antique.util.resources.ClothSkinData;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public interface AntiqueRegistries {
    ResourceKey<Registry<ClothSkinData>> CLOTHS = ResourceKey.createRegistryKey(Antiquities.id("cloths"));
    ResourceKey<Registry<ClothOverlayData>> CLOTH_OVERLAYS = ResourceKey.createRegistryKey(Antiquities.id("cloth_overlays"));

    static void initialize() {
        DynamicRegistries.registerSynced(CLOTHS, ClothSkinData.CODEC);
        DynamicRegistries.registerSynced(CLOTH_OVERLAYS, ClothOverlayData.CODEC);
    }
}
