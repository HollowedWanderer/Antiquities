package net.hollowed.antique.util.resources;

import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class ClientClothData {
    private static final Map<String, ClothSkinData.ClothSubData> transforms = new LinkedHashMap<>();
    private static final ArrayList<Identifier> overlayTransforms = new ArrayList<>();

    public static void addTransform(String string, ClothSkinData.ClothSubData data) {
        if (string != null && data != null) {
            transforms.putIfAbsent(string, data);
        }
    }

    public static void addOverlay(Identifier identifier) {
        if (identifier != null) {
            overlayTransforms.add(identifier);
        }
    }

    public static Collection<ClothSkinData.ClothSubData> getTransforms() {
        return transforms.values();
    }

    public static ArrayList<Identifier> getOverlayTransforms() {
        return overlayTransforms;
    }

    public static ClothSkinData.ClothSubData getTransform(String id) {
        return transforms.getOrDefault(id, new ClothSkinData.ClothSubData(Identifier.parse(""), "d13a68", 1.4F, 0.1F, 1.0F, -0.5F, 8, 0, false, false));
    }
}
