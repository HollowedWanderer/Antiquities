package net.hollowed.antique.index;

import net.hollowed.antique.Antiquities;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class AntiqueBlockTags {

    public static final TagKey<Block> WATER_OR_AIR = of("water_or_air");
    public static final TagKey<Block> SHOCKWAVE_PASSABLE = of("shockwave_passable");

    private AntiqueBlockTags() {

    }

    @SuppressWarnings("all")
    private static TagKey<Block> of(String id) {
        return TagKey.create(Registries.BLOCK, Antiquities.id(id));
    }
}
