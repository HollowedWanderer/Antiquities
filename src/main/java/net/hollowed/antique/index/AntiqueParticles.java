package net.hollowed.antique.index;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.hollowed.antique.Antiquities;
import net.hollowed.antique.particles.*;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;

public interface AntiqueParticles {
    SimpleParticleType SPARKLE_PARTICLE = FabricParticleTypes.simple();
    SimpleParticleType DUST_PARTICLE = FabricParticleTypes.simple();
    SimpleParticleType CAKE_SMEAR = FabricParticleTypes.simple();
    SimpleParticleType HIT_MARKER = FabricParticleTypes.simple();
    SimpleParticleType SCRAPE = FabricParticleTypes.simple();
    SimpleParticleType SHOCKWAVE_BUBBLE = FabricParticleTypes.simple();
    ParticleType<TyphoSparkParticle.Options> TYPHO_SPARK = FabricParticleTypes.complex(
            Codec.INT.optionalFieldOf("sprite").xmap(
                    TyphoSparkParticle.Options::new,
                    TyphoSparkParticle.Options::sprite
            ),
            ByteBufCodecs.optional(ByteBufCodecs.INT).map(
                    TyphoSparkParticle.Options::new,
                    TyphoSparkParticle.Options::sprite
            )
    );

    static void initialize() {
	    Registry.register(BuiltInRegistries.PARTICLE_TYPE, Antiquities.id("sparkle_particle"), SPARKLE_PARTICLE);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, Antiquities.id("dust"), DUST_PARTICLE);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, Antiquities.id("cake_smear"), CAKE_SMEAR);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, Antiquities.id("hit_marker"), HIT_MARKER);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, Antiquities.id("scrape"), SCRAPE);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, Antiquities.id("shockwave_bubble"), SHOCKWAVE_BUBBLE);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, Antiquities.id("typho_spark"), TYPHO_SPARK);
    }

    static void initializeClient() {
        ParticleProviderRegistry.getInstance().register(SPARKLE_PARTICLE, FacingRingParticle.Factory::new);
        ParticleProviderRegistry.getInstance().register(CAKE_SMEAR, CakeSmearParticle.Factory::new);
        ParticleProviderRegistry.getInstance().register(DUST_PARTICLE, DustParticle.CosySmokeFactory::new);
        ParticleProviderRegistry.getInstance().register(HIT_MARKER, HitMarkerParticle.Factory::new);
        ParticleProviderRegistry.getInstance().register(SCRAPE, GlowParticle.ScrapeFactory::new);
        ParticleProviderRegistry.getInstance().register(SHOCKWAVE_BUBBLE, ShockwaveBubbleParticle.Factory::new);
        ParticleProviderRegistry.getInstance().register(TYPHO_SPARK, TyphoSparkParticle.Factory::new);
    }
}
