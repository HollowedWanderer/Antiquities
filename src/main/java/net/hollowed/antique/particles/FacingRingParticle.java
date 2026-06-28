package net.hollowed.antique.particles;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hollowed.antique.util.MathUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.jspecify.annotations.NonNull;

@Environment(EnvType.CLIENT)
public class FacingRingParticle extends SimpleAnimatedParticle {
	private final double dirX;
	private final double dirY;
	private final double dirZ;
	private final ClientLevel level;

	FacingRingParticle(ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteSet spriteProvider) {
		super(world, x, y, z, spriteProvider, 0);

		this.setPos(x, y, z);
		this.level = world;

		this.dirX = velocityX;
		this.dirY = velocityY;
		this.dirZ = velocityZ;

		this.xd = 0;
		this.yd = 0;
		this.zd = 0;

		this.lifetime = 7;
		this.setSpriteFromAge(spriteProvider);
		this.gravity = 0;
		this.quadSize = 1;
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
			return new FacingRingParticle(world, x, y, z, velocityX, velocityY, velocityZ, this.spriteProvider);
		}
	}

	@Override
	public void extract(@NotNull QuadParticleRenderState submittable, @NotNull Camera camera, float tickProgress) {
		Vec3 direction = new Vec3(this.dirX, this.dirY, this.dirZ).normalize();
		BlockPos blockPos = BlockPos.containing(new Vec3(this.x, this.y, this.z).subtract(direction.x, 0.2, direction.z));
		int color = level.getBlockState(blockPos).getMapColor(level, blockPos).col;
		color = ARGB.scaleRGB(ARGB.opaque(color), 1.5F, 1.5F, 1.5F);
		if (level.getBlockState(blockPos).getMapColor(level, blockPos) != MapColor.NONE) {
			this.setColor(color);
		}

		Quaternionf quaternionf = MathUtils.vec3ToQuaternion(direction).rotateX((float) Math.toRadians(-90.0F));

		if (this.roll != 0.0F) {
			quaternionf.rotateZ(Mth.lerp(tickProgress, this.oRoll, this.roll));
		}

		this.extractRotatedQuad(submittable, camera, quaternionf, tickProgress);
		Quaternionf quaternionf1 = MathUtils.vec3ToQuaternion(new Vec3(-direction.x, -direction.y, -direction.z)).rotateX((float) Math.toRadians(-90.0F));
		this.extractRotatedQuad(submittable, camera, quaternionf1, tickProgress);
	}
}
