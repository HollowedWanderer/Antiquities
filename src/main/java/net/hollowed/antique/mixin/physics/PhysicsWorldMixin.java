package net.hollowed.antique.mixin.physics;

import net.hollowed.antique.citrus_physics.PhysicsWorld;
import net.hollowed.antique.util.interfaces.duck.PhysicsWorldDuck;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Level.class)
public class PhysicsWorldMixin implements PhysicsWorldDuck {

	@Override public PhysicsWorld lemonLib$getPhysics() { return physicsWorld; }
	@Unique PhysicsWorld physicsWorld;

	@Inject(at = @At("TAIL"), method = "<init>")
	private void init(CallbackInfo info) {
		physicsWorld = new PhysicsWorld((Level)(Object)this);
	}

//	@Inject(at = @At("HEAD"), method = "tick")
//	private void tick(CallbackInfo info) {
//		physicsWorld.world = (World)(Object)this;
//	}

	@Inject(at = @At("TAIL"), method = "close")
	private void destroy(CallbackInfo info) {
		physicsWorld.destroy();
	}


}