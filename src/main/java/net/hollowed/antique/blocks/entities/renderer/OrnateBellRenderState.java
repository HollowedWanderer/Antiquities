package net.hollowed.antique.blocks.entities.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class OrnateBellRenderState extends BlockEntityRenderState {
	@Nullable
	public Direction shakeDirection;
	@Nullable
	public Direction facingDirection;
	public float ticks;
}