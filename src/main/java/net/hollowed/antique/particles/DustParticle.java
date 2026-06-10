package net.hollowed.antique.particles;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

@Environment(EnvType.CLIENT)
public class DustParticle extends SingleQuadParticle {

	DustParticle(ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, boolean signal, SpriteSet provider) {
		super(world, x, y, z, provider.first());
		this.scale(2.0F);
		this.setSize(0.15F, 0.15F);
		if (signal) {
			this.lifetime = this.random.nextInt(50) + 280;
		} else {
			this.lifetime = this.random.nextInt(12) + 20;
		}

		this.gravity = 3.0E-6F;
		this.xd = velocityX;
		this.yd = velocityY + (double)(this.random.nextFloat() / 500.0F);
		this.zd = velocityZ;
	}

	@Override
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		if (this.age++ < this.lifetime && !(this.alpha <= 0.0F)) {
			this.xd = this.xd + (double)(this.random.nextFloat() / 5000.0F * (float)(this.random.nextBoolean() ? 1 : -1));
			this.zd = this.zd + (double)(this.random.nextFloat() / 5000.0F * (float)(this.random.nextBoolean() ? 1 : -1));
			this.yd = this.yd - (double)this.gravity;
			this.move(this.xd, this.yd, this.zd);
			if (this.age >= this.lifetime - 60 && this.alpha > 0.01F) {
				this.alpha -= 0.015F;
			}
		} else {
			this.remove();
		}
	}

	@Override
	public @NotNull Layer getLayer() {
		return Layer.TRANSLUCENT;
	}

	@Environment(EnvType.CLIENT)
	public static class CosySmokeFactory implements ParticleProvider<@NotNull SimpleParticleType> {
		private final SpriteSet spriteProvider;

		public CosySmokeFactory(SpriteSet spriteProvider) {
			this.spriteProvider = spriteProvider;
		}

		@Override
		public @Nullable Particle createParticle(@NonNull SimpleParticleType parameters, @NotNull ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, @NotNull RandomSource random) {
			DustParticle campfireSmokeParticle = new DustParticle(world, x, y, z, velocityX, velocityY, velocityZ, false, this.spriteProvider);
			campfireSmokeParticle.setAlpha(0.9F);
			campfireSmokeParticle.setSprite(this.spriteProvider.first());
			return campfireSmokeParticle;
		}
	}
}
