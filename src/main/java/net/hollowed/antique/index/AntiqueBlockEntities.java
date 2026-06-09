package net.hollowed.antique.index;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.hollowed.antique.Antiquities;
import net.hollowed.antique.blocks.entities.PedestalBlockEntity;
import net.hollowed.antique.blocks.entities.ResonatorBlockEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.NotNull;

public interface AntiqueBlockEntities {
    BlockEntityType<@NotNull PedestalBlockEntity> PEDESTAL_BLOCK_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Antiquities.id("pedestal"),
                    FabricBlockEntityTypeBuilder.create(PedestalBlockEntity::new,
                            AntiqueBlocks.PEDESTAL).build());
    BlockEntityType<@NotNull ResonatorBlockEntity> RESONATOR_BLOCK_ENTITY =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Antiquities.id("resonator"),
                    FabricBlockEntityTypeBuilder.create(ResonatorBlockEntity::new,
                            AntiqueBlocks.RESONATOR).build());

    static void initialize() {}
}
