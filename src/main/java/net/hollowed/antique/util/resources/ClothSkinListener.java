package net.hollowed.antique.util.resources;

import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.hollowed.antique.Antiquities;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.GsonHelper;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ClothSkinListener implements ResourceManagerReloadListener {
    private static final Map<String, ClothSkinData.ClothSubData> transforms = new LinkedHashMap<>();

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        manager.listResources("cloth_skins", path -> path.getPath().endsWith(".json")).keySet().forEach(id -> {
            if (manager.getResource(id).isPresent()) {
                try (InputStream stream = manager.getResource(id).get().open()) {
                    JsonObject json = GsonHelper.parse(new InputStreamReader(stream, StandardCharsets.UTF_8));
                    DataResult<ClothSkinData> result = ClothSkinData.CODEC.parse(JsonOps.INSTANCE, json);

                    result.resultOrPartial(Antiquities.LOGGER::error).ifPresent(data -> {
                        for (ClothSkinData.ClothSubData entry : data.list()) {
                            transforms.putIfAbsent(entry.model().toString(), entry);
                        }
                    });
                } catch (Exception e) {
                    Antiquities.LOGGER.error("Failed to load transform for {}: {}", id, e.getMessage());
                }
            }
        });
    }

    public static Map<String, ClothSkinData.ClothSubData> getTransformMap() {
        return transforms;
    }

    public static ClothSkinData.ClothSubData getTransform(String id) {
        return transforms.getOrDefault(id, new ClothSkinData.ClothSubData(Identifier.parse(""), "d13a68", 1.4F, 0.1F, 1.0F, -0.5F, 8, 0, false, false));
    }
}
