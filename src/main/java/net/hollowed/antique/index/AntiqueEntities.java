package net.hollowed.antique.index;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.hollowed.antique.Antiquities;
import net.hollowed.antique.entities.*;
import net.hollowed.antique.entities.parts.MyriadShovelPart;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.jetbrains.annotations.NotNull;

public interface AntiqueEntities {

    EntityType<@NotNull IllusionerEntity> ILLUSIONER = register(
            "illusioner",
            EntityType.Builder.of(IllusionerEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(8)
    );

    EntityType<@NotNull IllusionerCloneEntity> ILLUSIONER_CLONE = register(
            "illusioner_clone",
            EntityType.Builder.of(IllusionerCloneEntity::new, MobCategory.MONSTER)
                    .noLootTable()
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(8)
    );

    EntityType<@NotNull SmokeBombEntity> SMOKE_BOMB = register(
            "smoke_bomb",
            EntityType.Builder.of(SmokeBombEntity::new, MobCategory.MISC)
                    .noLootTable()
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(8)
    );

    EntityType<@NotNull CakeEntity> CAKE_ENTITY = register(
            "cake",
            EntityType.Builder.of(CakeEntity::new, MobCategory.MISC)
                    .noLootTable()
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(8)
    );

    EntityType<@NotNull MyriadShovelEntity> MYRIAD_SHOVEL = register(
            "myriad_shovel",
            EntityType.Builder.<MyriadShovelEntity>of(MyriadShovelEntity::new, MobCategory.MISC)
                    .noLootTable()
                    .sized(1.2F, 0.75F)
    );

    EntityType<@NotNull MyriadShovelPart> MYRIAD_SHOVEL_PART = register(
            "myriad_shovel_part",
            EntityType.Builder.of(MyriadShovelPart::new, MobCategory.MISC)
                    .noLootTable()
                    .sized(0.4F, 0.4F)
    );

    private static <T extends Entity> EntityType<T> register(ResourceKey<EntityType<?>> key, EntityType.Builder<T> type) {
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, type.build(key));
    }

    private static <T extends Entity> EntityType<T> register(String id, EntityType.Builder<T> type) {
        return register(keyOf(id), type);
    }

    private static ResourceKey<EntityType<?>> keyOf(String id) {
        return ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Antiquities.MOD_ID, id));
    }

    @SuppressWarnings("all")
    static void initialize() {
        FabricDefaultAttributeRegistry.register(AntiqueEntities.ILLUSIONER_CLONE, IllusionerCloneEntity.createIllusionerAttributes());
        FabricDefaultAttributeRegistry.register(AntiqueEntities.ILLUSIONER, IllusionerEntity.createIllusionerAttributes());
    }
}