package net.hollowed.antique.index;

import net.hollowed.antique.Antiquities;
import net.hollowed.antique.util.resources.ClothOverlayData;
import net.hollowed.antique.util.resources.ClothSkinData;
import net.minecraft.tags.TagKey;

@SuppressWarnings("unused")
public class AntiqueClothOverlayTags {

    public static final TagKey<ClothOverlayData> CREATIVE_TAB_ORDER = of("creative_tab_order");

    private AntiqueClothOverlayTags() {
    }

    private static TagKey<ClothOverlayData> of(String id) {
        return TagKey.create(AntiqueRegistries.CLOTH_OVERLAYS, Antiquities.id(id));
    }
}
