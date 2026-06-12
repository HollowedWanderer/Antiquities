package net.hollowed.antique.util.resources;

import net.minecraft.resources.Identifier;
import java.util.*;

public class ClientClothData {
    public static final Map<Identifier, ClothSkin> TRANSFORMS = new LinkedHashMap<>();
    public static final ArrayList<Identifier> OVERLAY_TRANSFORMS = new ArrayList<>();

    public static ClothSkin getTransform(Optional<Identifier> id) {
        return id.map(i -> TRANSFORMS.getOrDefault(i, ClothSkin.DEFAULT)).orElse(ClothSkin.DEFAULT);
    }
}
