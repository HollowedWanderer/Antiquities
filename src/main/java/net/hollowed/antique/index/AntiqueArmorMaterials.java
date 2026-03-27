package net.hollowed.antique.index;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Util;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import java.util.EnumMap;

@SuppressWarnings("all")
public interface AntiqueArmorMaterials extends ArmorMaterials {
    AdventureArmorMaterial ADVENTURE = new AdventureArmorMaterial(2000, Util.make(new EnumMap(ArmorType.class), (map) -> {
        map.put(ArmorType.BOOTS, 3);
        map.put(ArmorType.LEGGINGS, 2);
        map.put(ArmorType.CHESTPLATE, 6);
        map.put(ArmorType.HELMET, 0);
        map.put(ArmorType.BODY, 0);
    }), 15, SoundEvents.ARMOR_EQUIP_LEATHER, 1.0F, 0.025F, ItemTags.REPAIRS_LEATHER_ARMOR, AntiqueEquipmentAssetKeys.EMPTY, 0.0F, 0.5F, 0.2F);
}
