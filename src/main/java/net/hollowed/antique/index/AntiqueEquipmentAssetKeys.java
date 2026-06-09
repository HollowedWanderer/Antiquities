package net.hollowed.antique.index;

import net.hollowed.antique.Antiquities;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.EquipmentAsset;

public interface AntiqueEquipmentAssetKeys {
    ResourceKey<? extends Registry<EquipmentAsset>> REGISTRY_KEY = ResourceKey.createRegistryKey(Identifier.withDefaultNamespace("equipment_asset"));
    ResourceKey<EquipmentAsset> EMPTY = register("empty");

    static ResourceKey<EquipmentAsset> register(String name) {
        return ResourceKey.create(REGISTRY_KEY, Antiquities.id(name));
    }
}