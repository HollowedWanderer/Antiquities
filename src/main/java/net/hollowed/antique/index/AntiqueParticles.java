package net.hollowed.antique.index;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.hollowed.antique.Antiquities;
import net.hollowed.antique.particles.*;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public interface AntiqueParticles {
    SimpleParticleType SPARKLE_PARTICLE = FabricParticleTypes.simple();
    SimpleParticleType DUST_PARTICLE = FabricParticleTypes.simple();
    SimpleParticleType CAKE_SMEAR = FabricParticleTypes.simple();
    SimpleParticleType HIT_MARKER = FabricParticleTypes.simple();
    SimpleParticleType SCRAPE = FabricParticleTypes.simple();

    static void initialize() {
	    Registry.register(BuiltInRegistries.PARTICLE_TYPE, Antiquities.id("sparkle_particle"), SPARKLE_PARTICLE);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, Antiquities.id("dust"), DUST_PARTICLE);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, Antiquities.id("cake_smear"), CAKE_SMEAR);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, Antiquities.id("hit_marker"), HIT_MARKER);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, Antiquities.id("scrape"), SCRAPE);
    }

    static void initializeClient() {
        ParticleFactoryRegistry.getInstance().register(SPARKLE_PARTICLE, FacingRingParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(CAKE_SMEAR, CakeSmearParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(DUST_PARTICLE, DustParticle.CosySmokeFactory::new);
        ParticleFactoryRegistry.getInstance().register(HIT_MARKER, HitMarkerParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(SCRAPE, GlowParticle.ScrapeFactory::new);
    }
}
