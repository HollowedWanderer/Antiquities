package net.hollowed.antique.util.resources;

import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import net.hollowed.antique.Antiquities;
import net.hollowed.combatamenities.util.delay.ClientTickDelayScheduler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class PedestalDisplayListener implements ResourceManagerReloadListener {
    private static final Map<Identifier, PedestalDisplayData> transforms = new HashMap<>();
    private static PedestalDisplayData defaultTransforms;

    public void onResourceManagerReload(@NotNull ResourceManager manager) {
        Minecraft.getInstance().execute(() -> this.actuallyLoad(manager));
    }

    public void actuallyLoad(ResourceManager manager) {
        ClientTickDelayScheduler.schedule(-1, () -> {
            transforms.clear();
            manager.listResources("pedestal_transforms", (path) -> path.getPath().endsWith(".json")).keySet().forEach((id) -> {
                if (manager.getResource(id).isPresent()) {
                    try (InputStream stream = manager.getResource(id).get().open()) {
                        JsonObject json = GsonHelper.parse(new InputStreamReader(stream, StandardCharsets.UTF_8));
                        DataResult<PedestalDisplayData> result = PedestalDisplayData.CODEC.parse(JsonOps.INSTANCE, json);
                        Logger var10001 = Antiquities.LOGGER;
                        Objects.requireNonNull(var10001);
                        result.resultOrPartial(var10001::error).ifPresent((data) -> {
                            if (data.item().equals("default")) {
                                defaultTransforms = data;
                            } else if (data.item().startsWith("#")) {
                                String tagString = data.item().substring(1);
                                Identifier tagId = Identifier.parse(tagString);
                                TagKey<Item> tag = TagKey.create(BuiltInRegistries.ITEM.key(), tagId);
                                BuiltInRegistries.ITEM.forEach((item) -> {
                                    Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
                                    if (item.getDefaultInstance().typeHolder().is(tag)) {
                                        transforms.putIfAbsent(itemId, data);
                                    }

                                });
                            } else {
                                transforms.put(Identifier.parse(data.item()), data);
                            }

                        });
                    } catch (Exception e) {
                        Antiquities.LOGGER.error("Failed to load transform for {}: {}", id, e.getMessage());
                    }
                }

            });
        });
    }

    public static PedestalDisplayData getTransform(Identifier itemId) {
        return transforms.getOrDefault(itemId, defaultTransforms);
    }
}
