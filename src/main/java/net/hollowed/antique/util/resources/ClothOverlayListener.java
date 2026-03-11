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
import java.util.ArrayList;

public class ClothOverlayListener implements ResourceManagerReloadListener {
    private static final ArrayList<Identifier> transforms = new ArrayList<>();

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        manager.listResources("cloth_overlays", path -> path.getPath().endsWith(".json")).keySet().forEach(id -> {
            if (manager.getResource(id).isPresent()) {
                try (InputStream stream = manager.getResource(id).get().open()) {
                    JsonObject json = GsonHelper.parse(new InputStreamReader(stream, StandardCharsets.UTF_8));
                    DataResult<ClothOverlayData> result = ClothOverlayData.CODEC.parse(JsonOps.INSTANCE, json);

                    result.resultOrPartial(Antiquities.LOGGER::error).ifPresent(data -> transforms.addAll(data.list()));
                } catch (Exception e) {
                    Antiquities.LOGGER.error("Failed to load transform for {}: {}", id, e.getMessage());
                }
            }
        });
    }

    public static ArrayList<Identifier> getTransforms() {
        return transforms;
    }
}
