package net.hollowed.antique;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.hollowed.antique.datagen.AntiqueModelGenerator;
import net.hollowed.antique.datagen.AntiqueWorldgenProvider;
import net.hollowed.antique.index.AntiqueFeatures;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import org.jspecify.annotations.NonNull;

public class AntiquitiesDataGenerator implements DataGeneratorEntrypoint {

	@Override
	public void onInitializeDataGenerator(FabricDataGenerator generator) {
		FabricDataGenerator.Pack pack = generator.createPack();

		pack.addProvider(AntiqueModelGenerator::new);
		pack.addProvider(AntiqueWorldgenProvider::new);
	}

	@Override
	public void buildRegistry(@NonNull RegistrySetBuilder registryBuilder) {
		DataGeneratorEntrypoint.super.buildRegistry(registryBuilder);

		registryBuilder.add(Registries.FEATURE, AntiqueFeatures::bootstrap);
	}
}
