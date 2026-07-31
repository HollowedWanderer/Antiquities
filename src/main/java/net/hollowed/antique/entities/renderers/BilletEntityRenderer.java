package net.hollowed.antique.entities.renderers;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hollowed.antique.Antiquities;
import net.hollowed.antique.entities.BilletEntity;
import net.hollowed.antique.entities.models.BilletModel;
import net.hollowed.antique.index.AntiqueEntityLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

@Environment(EnvType.CLIENT)
public class BilletEntityRenderer extends MobRenderer<BilletEntity, LivingEntityRenderState, BilletModel> {
    private static final Identifier BILLET_LOCATION = Antiquities.id("textures/entity/billet.png");

    public BilletEntityRenderer(final EntityRendererProvider.Context context) {
        super(context, new BilletModel(context.bakeLayer(AntiqueEntityLayers.BILLET)), 0.5F);
    }

    @Override
    public @NonNull Identifier getTextureLocation(final @NonNull LivingEntityRenderState state) {
        return BILLET_LOCATION;
    }

    public @NonNull LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }
}
