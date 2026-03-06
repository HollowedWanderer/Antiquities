package net.hollowed.antique.index;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.hollowed.antique.Antiquities;
import net.hollowed.antique.blocks.entities.PedestalBlockEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.NotNull;

public interface AntiqueBlockEntities {
    BlockEntityType<@NotNull PedestalBlockEntity> PEDESTAL_BLOCK_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath(Antiquities.MOD_ID, "pedestal"),
                    FabricBlockEntityTypeBuilder.create(PedestalBlockEntity::new,
                            AntiqueBlocks.PEDESTAL).build());

    static void initialize() {}
}
