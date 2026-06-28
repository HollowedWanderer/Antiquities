package net.hollowed.antique.index;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

@Environment(EnvType.CLIENT)
public class AntiqueModelGenerator extends FabricModelProvider {

    public AntiqueModelGenerator(FabricPackOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(@NotNull BlockModelGenerators generator) {

    }

    @Override
    public void generateItemModels(@NonNull ItemModelGenerators generator) {

    }
}
