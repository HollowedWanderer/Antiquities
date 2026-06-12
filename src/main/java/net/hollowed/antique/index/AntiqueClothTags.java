package net.hollowed.antique.index;

import net.hollowed.antique.Antiquities;
import net.hollowed.antique.util.resources.ClothSkinData;
import net.minecraft.tags.TagKey;

@SuppressWarnings("unused")
public class AntiqueClothTags {

    public static final TagKey<ClothSkinData> BASIC_CLOTHS = of("basic_cloths");
    public static final TagKey<ClothSkinData> FRIEND_CLOTHS = of("friend_cloths");
    public static final TagKey<ClothSkinData> GENERIC_CLOTHS = of("generic_cloths");
    public static final TagKey<ClothSkinData> LGBTQ_CLOTHS = of("lgbtq_cloths");
    public static final TagKey<ClothSkinData> REFERENCE_CLOTHS = of("reference_cloths");

    public static final TagKey<ClothSkinData> CREATIVE_TAB_ORDER = of("creative_tab_order");

    private AntiqueClothTags() {
    }

    private static TagKey<ClothSkinData> of(String id) {
        return TagKey.create(AntiqueRegistries.CLOTHS, Antiquities.id(id));
    }
}
