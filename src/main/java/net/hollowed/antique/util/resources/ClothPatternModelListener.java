package net.hollowed.antique.util.resources;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.hollowed.antique.Antiquities;
import net.hollowed.antique.util.resources.client.ClothPatternModelData;
import net.hollowed.combatamenities.util.delay.ClientTickDelayScheduler;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ClothPatternModelListener implements ResourceManagerReloadListener {
    public static final Map<Identifier, ClothPatternModelData> MODELS = new HashMap<>();

    public void onResourceManagerReload(@NotNull ResourceManager manager) {
        Minecraft.getInstance().execute(() -> this.actuallyLoad(manager));
    }

    public void actuallyLoad(ResourceManager manager) {
        ClientTickDelayScheduler.schedule(-1, () -> {
            MODELS.clear();

            ClothPatternModelData.FILE_LISTER.listMatchingResources(manager).forEach((file, resource) -> {
                Identifier id = ClothPatternModelData.FILE_LISTER.fileToId(file);

                try (BufferedReader reader = resource.openAsReader()) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                    ClothPatternModelData.CODEC.decode(JsonOps.INSTANCE, json)
                            .ifSuccess(result -> {
                                MODELS.put(id, result.getFirst().fillDefaultSprites(id));
                            })
                            .ifError(error -> Antiquities.LOGGER.error("Error loading cloth pattern model {}: {}", file, error.message()));
                } catch (IOException e) {
                    Antiquities.LOGGER.error("Error loading cloth pattern model {}", file, e);
                }
            });
        });
    }
}
