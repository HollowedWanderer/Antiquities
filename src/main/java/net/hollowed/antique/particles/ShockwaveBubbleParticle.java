package net.hollowed.antique.particles;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import org.jspecify.annotations.NonNull;

import java.util.Random;

@Environment(EnvType.CLIENT)
public class ShockwaveBubbleParticle extends SingleQuadParticle {
	private final int spawnAge;

	ShockwaveBubbleParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, TextureAtlasSprite textureAtlasSprite) {
		super(clientLevel, d, e, f, textureAtlasSprite);
		this.setSize(0.02F, 0.02F);
		this.quadSize = this.quadSize * (this.random.nextFloat() * 0.6F + 0.2F);
		this.xd = g * 0.2F + (this.random.nextFloat() * 2.0F - 1.0F) * 0.02F;
		this.yd = h * 0.2F + (this.random.nextFloat() * 2.0F - 1.0F) * 0.02F;
		this.zd = i * 0.2F + (this.random.nextFloat() * 2.0F - 1.0F) * 0.02F;
		this.lifetime = (int)(8.0 / (this.random.nextFloat() * 0.8 + 0.2));
		this.alpha = 0;
		this.spawnAge = new Random().nextInt(0, 7);
	}

	@Override
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		if (this.lifetime-- <= 0) {
			this.remove();
		} else {
			if (this.age == this.spawnAge) this.alpha = 1;
			this.yd += 0.002;
			this.move(this.xd, this.yd, this.zd);
			this.xd *= 0.85F;
			this.yd *= 0.85F;
			this.zd *= 0.85F;

			this.age++;

			if (!this.level.getFluidState(BlockPos.containing(this.x, this.y, this.z)).is(FluidTags.WATER)) {
				this.remove();
			}
		}
	}

	@Override
	public SingleQuadParticle.@NonNull Layer getLayer() {
		return Layer.OPAQUE;
	}

	@Environment(EnvType.CLIENT)
	public static class Factory implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprite;

		public Factory(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		public Particle createParticle(
                @NonNull SimpleParticleType simpleParticleType, @NonNull ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, @NonNull RandomSource randomSource
		) {
			return new ShockwaveBubbleParticle(clientLevel, d, e, f, g, h, i, this.sprite.get(randomSource));
		}
	}
}