package net.hollowed.antique.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class AntiqueWorldgenProvider extends FabricDynamicRegistryProvider {

    public AntiqueWorldgenProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(HolderLookup.@NonNull Provider registries, @NonNull Entries entries) {
        entries.addAll(registries.lookupOrThrow(Registries.FEATURE));
    }

    @Override
    public @NonNull String getName() {
        return "World Generation";
    }
}
