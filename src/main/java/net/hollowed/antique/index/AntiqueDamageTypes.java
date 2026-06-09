package net.hollowed.antique.index;

import net.hollowed.antique.Antiquities;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class AntiqueDamageTypes {
    public static final ResourceKey<DamageType> WALL_SLAM = ResourceKey.create(Registries.DAMAGE_TYPE, Antiquities.id("wall_slam"));
    public static final ResourceKey<DamageType> IMPALE = ResourceKey.create(Registries.DAMAGE_TYPE, Antiquities.id("impale"));

    public AntiqueDamageTypes() {
    }

    public static DamageSource of(Level world, ResourceKey<DamageType> key) {
        return new DamageSource(world.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(key));
    }

    public static DamageSource of(Level world, ResourceKey<DamageType> key, Entity entity) {
        return new DamageSource(world.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(key), entity);
    }
}