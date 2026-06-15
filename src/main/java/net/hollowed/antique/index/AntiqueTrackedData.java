package net.hollowed.antique.index;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricTrackedDataRegistry;
import net.hollowed.antique.Antiquities;
import net.hollowed.antique.items.components.MyriadToolComponent;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.item.ItemStack;

public class AntiqueTrackedData {

    public static final EntityDataSerializer<MyriadToolComponent> MYRIAD_ATTRIBUTES = EntityDataSerializer.forValueType(MyriadToolComponent.STREAM_CODEC);
    public static final EntityDataSerializer<ItemStack> CLOTH_ATTRIBUTES = EntityDataSerializer.forValueType(ItemStack.OPTIONAL_STREAM_CODEC);

    public static void initialize() {
        FabricTrackedDataRegistry.register(Antiquities.id("myriad_tool_attributes"), MYRIAD_ATTRIBUTES);
        FabricTrackedDataRegistry.register(Antiquities.id("cloth_attributes"), CLOTH_ATTRIBUTES);
    }
}
