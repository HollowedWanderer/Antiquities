package net.hollowed.antique.index;

import com.google.common.collect.Sets;
import net.hollowed.antique.Antiquities;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.Identifier;
import java.util.Set;

public class AntiqueEntityLayers extends ModelLayers {
    private static final Set<ModelLayerLocation> LAYERS = Sets.newHashSet();

    public static final ModelLayerLocation ADVENTURE_ARMOR = register("adventure_armor");

    @SuppressWarnings("SameParameterValue")
    private static ModelLayerLocation register(String id) {
        return register(id, "main");
    }

    @SuppressWarnings("SameParameterValue")
    private static ModelLayerLocation register(String id, String variant) {
        ModelLayerLocation entityModelLayer = create(id, variant);
        if (!LAYERS.add(entityModelLayer)) {
            throw new IllegalStateException("Duplicate registration for " + entityModelLayer);
        } else {
            return entityModelLayer;
        }
    }

    private static ModelLayerLocation create(String id, String variant) {
        return new ModelLayerLocation(Identifier.fromNamespaceAndPath(Antiquities.MOD_ID, id), variant);
    }
}
