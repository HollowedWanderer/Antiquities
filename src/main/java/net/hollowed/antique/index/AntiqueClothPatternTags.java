package net.hollowed.antique.index;

import net.hollowed.antique.Antiquities;
import net.hollowed.antique.util.resources.ClothPatternData;
import net.minecraft.tags.TagKey;

@SuppressWarnings("unused")
public class AntiqueClothPatternTags {

    public static final TagKey<ClothPatternData> CREATIVE_TAB_ORDER = of("creative_tab_order");

    private AntiqueClothPatternTags() {
    }

    private static TagKey<ClothPatternData> of(String id) {
        return TagKey.create(AntiqueRegistries.CLOTH_PATTERNS, Antiquities.id(id));
    }
}
