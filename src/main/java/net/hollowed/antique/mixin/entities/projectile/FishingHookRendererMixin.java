package net.hollowed.antique.mixin.entities.projectile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.client.renderer.entity.state.FishingHookRenderState;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingHookRenderer.class)
public abstract class FishingHookRendererMixin extends EntityRenderer<@NotNull FishingHook, @NotNull FishingHookRenderState> {

    protected FishingHookRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/projectile/FishingHook;Lnet/minecraft/client/renderer/entity/state/FishingHookRenderState;F)V", at = @At("TAIL"))
    public void changeLineOffset(FishingHook fishingHook, FishingHookRenderState fishingHookRenderState, float f, CallbackInfo ci) {
        Player player = fishingHook.getPlayerOwner();

        boolean hasHollowedPack = false;

        for (Pack pack : Minecraft.getInstance().getResourcePackRepository().getSelectedPacks().stream().toList()) {
            if (pack.getId().equals("file/Hollowed Pack")) hasHollowedPack = true;
        }

        if (player != null && hasHollowedPack) {
            double yaw = Math.toRadians(player.getViewYRot(0.0F));
            double bodyYaw = Math.toRadians(player.yBodyRot % 360);

            if (this.entityRenderDispatcher.options.getCameraType().isFirstPerson()) {
                fishingHookRenderState.lineOriginOffset = new Vec3(fishingHookRenderState.lineOriginOffset.toVector3f()).add(
                        -0.12 * player.getViewVector(1.0F).x,
                        -0.12 * player.getViewVector(1.0F).y,
                        -0.12 * player.getViewVector(1.0F).z
                );

                Vec3 playerLook = player.getViewVector(1.0F).normalize();
                Vec3 right = new Vec3(playerLook.toVector3f()).cross(new Vec3(0, 1, 0)).normalize();
                Vec3 up = new Vec3(player.getViewVector(1.0F).toVector3f()).cross(right).normalize();

                if (playerLook.y == 1.0) {
                    up = new Vec3(
                            -Math.sin(yaw),
                            0,
                            Math.cos(yaw)
                    ).normalize();
                }

                if (playerLook.y == -1.0) {
                    up = new Vec3(
                            Math.sin(yaw),
                            0,
                            -Math.cos(yaw)
                    ).normalize();
                }

                fishingHookRenderState.lineOriginOffset = new Vec3(fishingHookRenderState.lineOriginOffset.toVector3f()).add(
                        -0.4 * up.x,
                        -0.4 * up.y,
                        -0.4 * up.z
                );
            } else {
                fishingHookRenderState.lineOriginOffset = new Vec3(fishingHookRenderState.lineOriginOffset.toVector3f()).add(
                        0.4 * -Math.sin(bodyYaw),
                        0.3,
                        0.4 * Math.cos(bodyYaw)
                );
            }
        }
    }
}
