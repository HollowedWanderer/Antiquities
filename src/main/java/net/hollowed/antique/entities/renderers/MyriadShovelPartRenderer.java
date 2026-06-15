package net.hollowed.antique.entities.renderers;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hollowed.antique.entities.parts.MyriadShovelPart;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

@Environment(EnvType.CLIENT)
public class MyriadShovelPartRenderer extends EntityRenderer<@NotNull MyriadShovelPart, @NotNull MyriadShovelRenderState> {

	public MyriadShovelPartRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	public @NonNull MyriadShovelRenderState createRenderState() {
		return new MyriadShovelRenderState();
	}
}