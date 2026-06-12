package net.hollowed.antique.index;

import net.hollowed.antique.Antiquities;
import net.hollowed.antique.util.resources.ClothSkin;
import net.minecraft.tags.TagKey;

@SuppressWarnings("unused")
public class AntiqueClothTags {

    public static final TagKey<ClothSkin> BASIC_CLOTHS = of("basic_cloths");
    public static final TagKey<ClothSkin> FRIEND_CLOTHS = of("friend_cloths");
    public static final TagKey<ClothSkin> GENERIC_CLOTHS = of("generic_cloths");
    public static final TagKey<ClothSkin> LGBTQ_CLOTHS = of("lgbtq_cloths");
    public static final TagKey<ClothSkin> REFERENCE_CLOTHS = of("reference_cloths");

    public static final TagKey<ClothSkin> CREATIVE_TAB_ORDER = of("creative_tab_order");

    private AntiqueClothTags() {
    }

    private static TagKey<ClothSkin> of(String id) {
        return TagKey.create(AntiqueRegistries.CLOTHS, Antiquities.id(id));
    }
}
