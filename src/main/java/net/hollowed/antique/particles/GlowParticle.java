package net.hollowed.antique.particles;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

@Environment(EnvType.CLIENT)
public class GlowParticle extends SingleQuadParticle {
	private final SpriteSet spriteProvider;

	GlowParticle(ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteSet spriteProvider) {
		super(world, x, y, z, velocityX, velocityY, velocityZ, spriteProvider.first());
		this.friction = 0.96F;
		this.speedUpWhenYMotionIsBlocked = true;
		this.spriteProvider = spriteProvider;
		this.quadSize *= 0.75F;
		this.hasPhysics = false;
		this.setSpriteFromAge(spriteProvider);
	}

	@Override
	public @NotNull Layer getLayer() {
		return Layer.TRANSLUCENT;
	}

	@Override
	public int getLightCoords(float tint) {
		float f = (this.age + tint) / this.lifetime;
		f = Mth.clamp(f, 0.0F, 1.0F);
		int i = super.getLightCoords(tint);
		int j = i & 0xFF;
		int k = i >> 16 & 0xFF;
		j += (int)(f * 15.0F * 16.0F);
		if (j > 240) {
			j = 240;
		}

		return j | k << 16;
	}

	@Override
	public void tick() {
		super.tick();
		this.setSpriteFromAge(this.spriteProvider);
	}

	@Environment(EnvType.CLIENT)
	public static class ScrapeFactory implements ParticleProvider<@NotNull SimpleParticleType> {
		private final SpriteSet spriteProvider;

		public ScrapeFactory(SpriteSet spriteProvider) {
			this.spriteProvider = spriteProvider;
		}

		@Override
		public @Nullable Particle createParticle(@NonNull SimpleParticleType parameters, @NotNull ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, @NotNull RandomSource random) {
			GlowParticle glowParticle = new GlowParticle(world, x, y, z, 0.0, 0.0, 0.0, this.spriteProvider);
			if (world.getRandom().nextBoolean()) {
				glowParticle.setColor(229 / 255.0F, 158 /255.0F, 88 / 255.0F);
			} else {
				glowParticle.setColor(210 / 255.0F, 126 / 255.0F, 86 / 255.0F);
			}

			glowParticle.setParticleSpeed(velocityX * 0.01, velocityY * 0.01, velocityZ * 0.01);
			glowParticle.setLifetime(world.getRandom().nextInt(30) + 10);
			return glowParticle;
		}
	}
}
