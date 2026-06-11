package net.hollowed.antique.index;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.hollowed.antique.Antiquities;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public class AntiqueModelGenerator extends FabricModelProvider {

    public AntiqueModelGenerator(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(@NotNull BlockModelGenerators generator) {

    }

    @Override
    public void generateItemModels(ItemModelGenerators generator) {
        ModelTemplate test = model(Identifier.withDefaultNamespace("item/generated"), TextureSlot.LAYER0);

        test.create(Antiquities.id("test"), TextureMapping.layer0(Antiquities.id("test1")), generator.modelOutput);
        test.create(Antiquities.id("test_other"), TextureMapping.layer0(Antiquities.id("test2")), generator.modelOutput);
    }

    private static ModelTemplate model(Identifier parent, TextureSlot... keys) {
        return new ModelTemplate(Optional.of(parent), Optional.empty(), keys);
    }
}
