package net.hollowed.antique.index;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.item.v1.ComponentTooltipAppenderRegistry;
import net.hollowed.antique.Antiquities;
import net.hollowed.antique.items.components.MyriadToolComponent;
import net.hollowed.antique.items.components.AmethystForkComponent;
import net.hollowed.antique.util.resources.ClothPatternData;
import net.hollowed.antique.util.resources.ClothSkinData;
import net.hollowed.antique.util.resources.SewnClothPattern;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

import java.util.List;

public interface AntiqueDataComponentTypes {
    DataComponentType<List<ItemStack>> SATCHEL_STACK = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Antiquities.id("satchel_stacks"),
            DataComponentType.<List<ItemStack>>builder()
                    .persistent(ItemStack.CODEC.listOf().fieldOf("satchel_stacks").codec())
                    .build()
    );
    DataComponentType<AmethystForkComponent> AMETHYST_FORK = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Antiquities.id("amethyst_fork"),
            DataComponentType.<AmethystForkComponent>builder()
                    .persistent(AmethystForkComponent.CODEC)
                    .networkSynchronized(AmethystForkComponent.STREAM_CODEC)
                    .build()
    );
    DataComponentType<MyriadToolComponent> MYRIAD_TOOL = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Antiquities.id("myriad_tool"),
            DataComponentType.<MyriadToolComponent>builder()
                    .persistent(MyriadToolComponent.CODEC)
                    .networkSynchronized(MyriadToolComponent.STREAM_CODEC)
                    .build()
    );
    DataComponentType<ResourceKey<ClothSkinData>> CLOTH_TYPE = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Antiquities.id("cloth_type"),
            DataComponentType.<ResourceKey<ClothSkinData>>builder()
                    .persistent(ResourceKey.codec(AntiqueRegistries.CLOTHS))
                    .networkSynchronized(ResourceKey.streamCodec(AntiqueRegistries.CLOTHS))
                    .build()
    );
    DataComponentType<ResourceKey<ClothPatternData>> CLOTH_PATTERN_TYPE = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Antiquities.id("cloth_pattern_type"),
            DataComponentType.<ResourceKey<ClothPatternData>>builder()
                    .persistent(ResourceKey.codec(AntiqueRegistries.CLOTH_PATTERNS))
                    .networkSynchronized(ResourceKey.streamCodec(AntiqueRegistries.CLOTH_PATTERNS))
                    .build()
    );
    DataComponentType<List<SewnClothPattern>> SEWN_CLOTHS = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Antiquities.id("sewn_cloths"),
            DataComponentType.<List<SewnClothPattern>>builder()
                    .persistent(SewnClothPattern.CODEC.listOf())
                    .build()
    );
    DataComponentType<DyedItemColor> CLOTH_PATTERN_COLOR = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Antiquities.id("cloth_pattern_color"),
            DataComponentType.<DyedItemColor>builder()
                    .persistent(DyedItemColor.CODEC)
                    .networkSynchronized(DyedItemColor.STREAM_CODEC)
                    .build()
    );
    DataComponentType<Boolean> CLOTH_PATTERN_GLOWING = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Antiquities.id("cloth_pattern_glowing"),
            DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
                    .build()
    );
    DataComponentType<Integer> COUNTER = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Antiquities.id("counter"),
            DataComponentType.<Integer>builder()
                    .persistent(Codec.INT.fieldOf("counter").codec())
                    .build()
    );
    DataComponentType<Boolean> STICKY_TOOLTIP = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Antiquities.id("sticky_tooltip"),
            DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL.fieldOf("sticky_tooltip").codec())
                    .build()
    );

    static void initialize() {
        ComponentTooltipAppenderRegistry.addFirst(AMETHYST_FORK);
    }
}
