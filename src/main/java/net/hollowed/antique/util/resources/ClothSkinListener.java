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
    public static final Map<Identifier, ClothSkinData.ClothSubData> TRANSFORMS = new LinkedHashMap<>();

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        manager.listResources("cloth_skins", path -> path.getPath().endsWith(".json")).keySet().forEach(id -> {
            if (manager.getResource(id).isPresent()) {
                try (InputStream stream = manager.getResource(id).get().open()) {
                    JsonObject json = GsonHelper.parse(new InputStreamReader(stream, StandardCharsets.UTF_8));
                    DataResult<ClothSkinData> result = ClothSkinData.CODEC.parse(JsonOps.INSTANCE, json);

                    result.resultOrPartial(Antiquities.LOGGER::error).ifPresent(data -> {
                        for (ClothSkinData.ClothSubData entry : data.list()) {
                            TRANSFORMS.putIfAbsent(entry.model().orElseThrow(), entry);
                        }
                    });
                } catch (Exception e) {
                    Antiquities.LOGGER.error("Failed to load transform for {}: {}", id, e.getMessage());
                }
            }
        });
    }

    @SuppressWarnings("all")
    public static ClothSkinData.ClothSubData getTransform(Optional<Identifier> id) {
        return id.map(i -> TRANSFORMS.getOrDefault(i, ClothSkinData.DEFAULT)).orElse(ClothSkinData.DEFAULT);
    }
}
