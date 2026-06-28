package net.hollowed.antique.particles;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.jspecify.annotations.NonNull;

@Environment(EnvType.CLIENT)
public class CakeSmearParticle extends SingleQuadParticle {
	private final double dirX;
	private final double dirY;
	private final double dirZ;

	CakeSmearParticle(ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteSet spriteProvider) {
		super(world, x, y, z, spriteProvider.first());

		this.setPos(x, y, z);

		this.dirX = velocityX;
		this.dirY = velocityY;
		this.dirZ = velocityZ;

		this.xd = 0;
		this.yd = 0;
		this.zd = 0;

		this.lifetime = 100;
		this.setSpriteFromAge(spriteProvider);
		this.gravity = 0;
		this.quadSize = 1;
	}

	@Override
	public void tick() {
		if (this.age >= 40 && this.alpha > 0.01F) {
			this.alpha -= 0.02F;
		}
		if (this.age++ >= this.lifetime) {
			this.remove();
		}
	}

	@Override
	public void move(double dx, double dy, double dz) {
		this.setBoundingBox(this.getBoundingBox().move(dx, dy, dz));
		this.setLocationFromBoundingbox();
	}

	@Environment(EnvType.CLIENT)
	public static class Factory implements ParticleProvider<@NotNull SimpleParticleType> {
		private final SpriteSet spriteProvider;

		public Factory(SpriteSet spriteProvider) {
			this.spriteProvider = spriteProvider;
		}

		@Override
		public @Nullable Particle createParticle(@NonNull SimpleParticleType parameters, @NotNull ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, @NotNull RandomSource random) {
			CakeSmearParticle particle = new CakeSmearParticle(world, x, y, z, velocityX, velocityY, velocityZ, this.spriteProvider);
			particle.setSprite(this.spriteProvider.first());
			return particle;
		}
	}

	@Override
	public void extract(@NotNull QuadParticleRenderState submittable, @NotNull Camera camera, float tickProgress) {
		Vec3 direction = new Vec3(this.dirX, this.dirY, this.dirZ);
		Quaternionf quaternionf = new Quaternionf();
		if (direction.x == 1) quaternionf.rotateY((float) Math.toRadians(90));
		this.extractRotatedQuad(submittable, camera, quaternionf, tickProgress);

		Quaternionf quaternionf1 = new Quaternionf();
		if (direction.x == 1) quaternionf1.rotateY((float) Math.toRadians(-90));
		if (direction.z == 1) quaternionf1.rotateY((float) Math.toRadians(180));
		this.extractRotatedQuad(submittable, camera, quaternionf1, tickProgress);
	}

	@Override
	protected @NotNull Layer getLayer() {
		return Layer.TRANSLUCENT;
	}
}
