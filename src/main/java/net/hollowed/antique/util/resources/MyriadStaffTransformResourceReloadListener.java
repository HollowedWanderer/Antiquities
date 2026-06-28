package net.hollowed.antique.util.resources;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.hollowed.combatamenities.CombatAmenities;
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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyriadStaffTransformResourceReloadListener implements ResourceManagerReloadListener {
    private static final Map<Identifier, MyriadStaffTransformData> transforms = new HashMap<>();
    private static MyriadStaffTransformData defaultTransforms;

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager manager) {
        Minecraft.getInstance().execute(() -> this.actuallyLoad(manager));
    }

    public void actuallyLoad(ResourceManager manager) {
        ClientTickDelayScheduler.schedule(-1, () -> {
            transforms.clear();

            manager.listResources("staff_transforms", path -> path.getPath().endsWith(".json")).keySet().forEach(id -> {
                if (manager.getResource(id).isPresent()) {
                    try (InputStream stream = manager.getResource(id).get().open()) {
                        var json = GsonHelper.parse(new InputStreamReader(stream, StandardCharsets.UTF_8));
                        DataResult<MyriadStaffTransformData> result = MyriadStaffTransformData.CODEC.parse(JsonOps.INSTANCE, json);

                        result.resultOrPartial(CombatAmenities.LOGGER::error).ifPresent(data -> {
                            if (data.model().isDefault()) {
                                data = new MyriadStaffTransformData(
                                        data.model(),
                                        data.scale(),
                                        data.rotation(),
                                        data.translation()
                                );
                                defaultTransforms = data;
                            } else if (data.model() instanceof TagOrSingle.Tag<Item>(TagKey<Item> key)) {
                                MyriadStaffTransformData finalData = data;
                                BuiltInRegistries.ITEM.forEach(item -> {
                                    Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
                                    if (item.getDefaultInstance().typeHolder().is(key)) {
                                        transforms.put(itemId, finalData);
                                    }
                                });
                            } else if (data.model() instanceof TagOrSingle.Single<Item>(Identifier id1)) {
                                transforms.put(id1, data);
                            }
                        });
                    } catch (Exception e) {
                        CombatAmenities.LOGGER.error("Failed to load transform for {}: {}", id, e.getMessage());
                    }
                }
            });
        });
    }

    public static MyriadStaffTransformData getTransform(Identifier itemId) {
        MyriadStaffTransformData baseTransform = transforms.getOrDefault(itemId, defaultTransforms);

        if (baseTransform != null) {
            return baseTransform;
        }

        // Fallback to a fully default transform if no data is available
        return new MyriadStaffTransformData(
                new TagOrSingle.Single<>(itemId),
                List.of(1.0f, 1.0f, 1.0f), // Default scale
                List.of(0.0f, 0.0f, 0.0f), // Default rotation
                List.of(0.0f, 0.0f, 0.0f) // Default translation
        );
    }
}
