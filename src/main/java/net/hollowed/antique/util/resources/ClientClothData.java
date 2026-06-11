package net.hollowed.antique.util.resources;

import net.minecraft.resources.Identifier;
import java.util.*;

public class ClientClothData {
    public static final Map<Identifier, ClothSkinData.ClothSubData> TRANSFORMS = new LinkedHashMap<>();
    public static final ArrayList<Identifier> OVERLAY_TRANSFORMS = new ArrayList<>();

    public static ClothSkinData.ClothSubData getTransform(Optional<Identifier> id) {
        return id.map(i -> TRANSFORMS.getOrDefault(i, ClothSkinData.DEFAULT)).orElse(ClothSkinData.DEFAULT);
    }
}
