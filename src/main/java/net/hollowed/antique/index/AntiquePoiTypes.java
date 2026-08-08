package net.hollowed.antique.index;

import net.fabricmc.fabric.api.object.builder.v1.world.poi.PoiHelper;
import net.hollowed.antique.Antiquities;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;

@SuppressWarnings("unused")
public class AntiquePoiTypes {

    public static final ResourceKey<PoiType> EMBLAZONED_BELL_KEY = ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE,
            Antiquities.id("emblazoned_bell"));
    public static final PoiType EMBLAZONED_BELL = PoiHelper.register(Antiquities.id("emblazoned_bell"),
            1, 1, AntiqueBlocks.EMBLAZONED_BELL);

    public static void initialize() {

    }
}
