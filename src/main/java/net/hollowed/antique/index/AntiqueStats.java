package net.hollowed.antique.index;

import net.hollowed.antique.Antiquities;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;

public class AntiqueStats {
    public static final Identifier INTERACT_WITH_DYE_TABLE = register("interact_with_dye_table", StatFormatter.DEFAULT);

    @SuppressWarnings("all")
    private static Identifier register(String id, StatFormatter formatter) {
        Identifier identifier = Antiquities.id(id);
        Registry.register(BuiltInRegistries.CUSTOM_STAT, id, identifier);
        Stats.CUSTOM.get(identifier, formatter);
        return identifier;
    }

    public static void initialize() {}
}
