package net.hollowed.antique.index;

import com.mojang.serialization.Codec;
import net.hollowed.antique.Antiquities;
import net.hollowed.antique.items.components.MyriadToolComponent;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import java.util.List;

public interface AntiqueDataComponentTypes {
    DataComponentType<List<ItemStack>> SATCHEL_STACK = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Antiquities.id("satchel_stacks"),
            DataComponentType.<List<ItemStack>>builder()
                    .persistent(ItemStack.CODEC.listOf().fieldOf("satchel_stacks").codec())
                    .build()
    );
    DataComponentType<MyriadToolComponent> MYRIAD_TOOL = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Antiquities.id("myriad_tool"),
            DataComponentType.<MyriadToolComponent>builder()
                    .persistent(MyriadToolComponent.CODEC)
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

    static void initialize() {}
}
