package net.hollowed.antique.client.renderer.cloth;

import net.hollowed.antique.mixin.accessors.RendererAccessor;
import net.hollowed.antique.util.interfaces.duck.ClothAccess;
import net.hollowed.antique.util.resources.ClothSkinData;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.*;
import java.lang.Math;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClothManager {

    public static RenderType getClothRenderLayer(Identifier cloth) {
        return RenderTypes.itemEntityTranslucentCull(Identifier.parse(cloth.getNamespace() + ":textures/cloth/" + cloth.getPath() + ".png"));
    }

    public static RenderType getOverlayRenderLayer(String cloth, Identifier overlay) {
        return RenderTypes.itemEntityTranslucentCull(Identifier.parse(overlay.getNamespace() + ":textures/overlay/" + overlay.getPath() + cloth + ".png"));
    }

    public Vector3d pos = new Vector3d();
    public ArrayList<ClothBody> bodies = new ArrayList<>();
    private int bodyCountCooldown = 0;
    public Entity entity;
    public ClothSkinData.ClothSubData data;
    public boolean render = false;

    public ClothManager(Vector3d pos, int BodyCount, ClothSkinData.ClothSubData data) {
        reset(pos, BodyCount, data);
    }

    public void reset(Vector3d pos, int BodyCount, ClothSkinData.ClothSubData data) {
        bodies.clear();
        this.data = data;
        for (int i = 0; i < Math.abs(BodyCount+1); i++) {
            ClothBody body = new ClothBody(pos);
            bodies.add(body);
        }
    }

    public void setBodyCount(int count) {
        if (count != bodies.size()) {
            reset(this.pos, count, this.data);
        }
    }

    public void tick() {
        float gravityMultiplier = data.gravity();
        float waterGravityMultiplier = data.waterGravity();
        double length = data.length();
        double delta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks();
        ClientLevel world = Minecraft.getInstance().level;

        if (delta == 0) {
            for (ClothBody body : bodies) {
                body.posCache.set(body.pos);
            }
        }

        if (world != null) {

            double previousDrag = 0.0;

            bodies.getFirst().isPinned = true;

            // Update pass
            for (ClothBody body : bodies) {
                Vec3 startPos = new Vec3(body.pos.x, body.pos.y, body.pos.z);
                BlockPos blockPos = BlockPos.containing(startPos);
                BlockState state = world.getBlockState(blockPos);
                Vector3d vel = new Vector3d(body.pos).sub(body.posCache);
                double maxVel = 0.05;
                if (vel.length() > maxVel) {
                    vel.normalize().mul(maxVel);
                }
                double velLength = vel.length();
                double dynamicDrag = Mth.clamp(1.0 - velLength * 0.05, 0.85, 0.98);
                vel.mul(dynamicDrag);
                body.posCache.set(new Vector3d(body.pos).sub(vel));

                // Compute new drag value smoothly
                double newDrag = Math.random() * (state.getBlock() == Blocks.WATER ? 0.25 : 1.25);
                double smoothDrag = Mth.lerp(delta * 0.1, previousDrag, newDrag);

                // Apply gravity
                double gravity = 0.05 * gravityMultiplier;
                if(state.getBlock() == Blocks.WATER) {
                    gravity *= waterGravityMultiplier;
                }
                gravity /= 1;

                body.accel.add(0, -gravity, 0);

                previousDrag = smoothDrag; // Store for next iteration
                body.update(delta);
            }
        }

        for (int k = 0; k < 32; k++) {
            if (k % 2 == 0) {
                for (int i = 0; i < bodies.size() - 1; i++) {
                    bodies.get(i).containDistance(bodies.get(i + 1), length / bodies.size());
                }
            } else {
                for (int i = bodies.size() - 2; i >= 0; i--) {
                    bodies.get(i).containDistance(bodies.get(i + 1), length / bodies.size());
                }
            }
        }

        // Collision pass
        if (world != null) {
            List<Vector3d> accels = new ArrayList<>();

            for (ClothBody body : bodies) {
                body.slideOutOfBlocks(world);
                accels.add(body.entityCollisionPerchance(world, entity));
                body.pos.x = Mth.lerp(0.125, body.pos.x, body.posCache.x);
                body.pos.y = Mth.lerp(0.125, body.pos.y, body.posCache.y);
                body.pos.z = Mth.lerp(0.125, body.pos.z, body.posCache.z);
            }

            Vector3d average = new Vector3d();
            for (Vector3d accel : accels) {
                average.add(accel);
            }

            average.div(accels.size());
            for (ClothBody body : bodies) {
                body.accel.add(average);
            }
        }

        // Update parent position
        ClothBody root = bodies.getFirst();
        root.pos = new Vector3d(pos);

        // Check if cloth is too far from the root position
        double maxDistance = 5.0;
        if (root.pos.distance(root.posCache) > maxDistance) {
            resetCloth(); // Call reset method
        }
    }

    // Reset all body segments to be near the root position
    private void resetCloth() {
        Vector3d offset = new Vector3d(0, -0.2, 0);
        for (int i = 0; i < bodies.size(); i++) {
            bodies.get(i).pos.set(pos.add(offset.mul(i)));
            bodies.get(i).posCache.set(bodies.get(i).pos);
        }
    }

    public static Vec3 matrixToVec(PoseStack matrixStack) {
        Matrix4f matrix = matrixStack.last().pose();
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vector4f localPos = new Vector4f(0, 0, 0, 1);
        matrix.transform(localPos);
        Vec3 cameraPos = camera.position();
        return new Vec3(cameraPos.x + localPos.x(), cameraPos.y + localPos.y(), cameraPos.z + localPos.z());
    }

    @SuppressWarnings("unused")
    public int rgbToDecimal(int red, int green, int blue) {
        return (red << 16) | (green << 8) | blue;
    }

    public static ClothManager getOrCreate(Entity entity, Identifier id, ClothSkinData.ClothSubData data) {
        if (Minecraft.getInstance().level instanceof ClothAccess clothAccess) {
            clothAccess.antique$getManagers().computeIfAbsent(id, k -> {
                ClothManager manager = new ClothManager(new Vector3d(entity.getX(), entity.getY(), entity.getZ()), 8, data);
                manager.entity = entity;
                return manager;
            });
            return clothAccess.antique$getManagers().get(id);
        }
        return null;
    }

    public void renderCloth(ClothSkinData.ClothSubData data, PoseStack matrices, SubmitNodeCollector queue, int light, boolean glow, Color color, Color overlayColor, Identifier overlay) {
        this.render = true;
        this.data = data;
        Identifier cloth = data.model();
        int bodyCount = data.bodyAmount();
        double width = data.width();
        if (data.light() != 0) light = data.light();
        if (!data.dyeable()) color = Color.WHITE;

        if (cloth == null || cloth.equals(Identifier.parse("minecraft:"))) return;

        Vec3 position = matrixToVec(matrices);

        if (bodyCount != 0 && this.bodyCountCooldown <= 0 && bodyCount != (bodies.size() - 1)) {
            setBodyCount(bodyCount);
            this.bodyCountCooldown = 3;
        }

        if (this.bodyCountCooldown > 0) {
            this.bodyCountCooldown--;
        }

        Vector3d lastA = null;
        Vector3d lastB = null;
        Vector3d lastThicknessVec = null;

        Vector3d danglePos = new Vector3d(position.x, position.y, position.z);
        pos = new Vector3d(danglePos);
        //this.tick();

        matrices.pushPose();
        GameRenderer renderer = Minecraft.getInstance().gameRenderer;
        RendererAccessor mixin = (RendererAccessor) renderer;
        Camera mainCamera = renderer.getMainCamera();
        Matrix4f projectionA = getProjection(renderer, mixin._getFov(mainCamera, 0.0f, true));
        Matrix4f projectionO = getProjection(renderer, 70);
        Matrix4f res = projectionO.invert().mul(projectionA);

        int count = bodies.size() - 1;

        for (int i = 0; i < count; i++) {

            ClothBody body = bodies.get(i);
            ClothBody nextBody = bodies.get(i + 1);

            Vector3d pos = body.getPos();
            Vector3d nextPos = nextBody.getPos();

            float uvTop = (1f / count) * i;
            float uvBot = uvTop + (1f / count);

            // Get camera position
            Vec3 cameraPosVec3d = Minecraft.getInstance().gameRenderer.getMainCamera().position();
            Vector3d cameraPos = new Vector3d(cameraPosVec3d.x, cameraPosVec3d.y, cameraPosVec3d.z);

            // Compute thickness vector from segment midpoint
            Vector3d mid = new Vector3d((pos.x + nextPos.x) / 2.0, (pos.y + nextPos.y) / 2.0, (pos.z + nextPos.z) / 2.0);
            Vector3d toCam = cameraPos.sub(mid, new Vector3d()).normalize();
            Vector3d up = new Vector3d(0, 1, 0);
            Vector3d thicknessVec = up.cross(toCam, new Vector3d()).normalize().mul(width);

            if (lastThicknessVec != null) {
                thicknessVec = thicknessVec.lerp(lastThicknessVec, 1).normalize().mul(width);
            }

            Vector3d a = lastA != null ? lastA : pos.sub(thicknessVec, new Vector3d());
            Vector3d b = lastB != null ? lastB : pos.add(thicknessVec, new Vector3d());

            // Compute end vertices for this segment
            Vector3d c = nextPos.add(thicknessVec, new Vector3d());
            Vector3d d = nextPos.sub(thicknessVec, new Vector3d());

            // Cache for next loop
            lastA = d;
            lastB = c;
            lastThicknessVec = thicknessVec;

            RenderType clothLayer = getClothRenderLayer(cloth);
            String clothType = !Objects.equals(cloth.getPath(), "cloth") ? !cloth.getPath().isEmpty() ? cloth.getPath().substring(0, cloth.getPath().indexOf("_")) : "default" : "default";
            RenderType overlayLayer = getOverlayRenderLayer("_" + clothType, overlay);

            drawQuad(
                    new PoseStack(),
                    new Matrix4f(),
                    clothLayer,
                    !overlay.equals(Identifier.parse("")) ? overlayLayer : null,
                    queue,
                    transform(res,a), transform(res,b), transform(res,c), transform(res,d),
                    new Vec2(0f, uvTop),
                    new Vec2(1f, uvTop),
                    new Vec2(1f, uvBot),
                    new Vec2(0f, uvBot),
                    light,
                    glow,
                    color,
                    overlayColor
            );
        }

        matrices.popPose();
    }

    private static Vector3d transform(Matrix4f res, Vector3d a) {
        Vector4f transformed = res.transform(new Vector4f((float) a.x, (float) a.y, (float) a.z, 1f));
        Vector3d retVal = new Vector3d();
        transformed.xyz(retVal);
        retVal.div(transformed.w);
        return retVal;
    }

    private static Matrix4f getProjection(GameRenderer renderer, float fov) {
        Camera mainCamera = renderer.getMainCamera();
        Matrix4f projection = renderer.getProjectionMatrix(fov);
        Quaternionf quaternionf = mainCamera.rotation();
        Matrix4f rotation = (new Matrix4f()).rotation(quaternionf).invert();
        Vec3 vec32 = mainCamera.position();
        Matrix4f translation = (new Matrix4f()).setTranslation(vec32.multiply(-1f, -1f, -1f).toVector3f());

        return projection.mul(rotation).mul(translation);
    }

    public void drawQuad(PoseStack matrices, Matrix4f matrix, RenderType layer, @Nullable RenderType overlay, SubmitNodeCollector queue, Vector3d posA, Vector3d posB, Vector3d posC, Vector3d posD, Vec2 uvA, Vec2 uvB, Vec2 uvC, Vec2 uvD, int light, boolean glow, Color color, Color overlayColor) {
        Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().position().multiply(-1, -1, -1);

        for (int i = 0; i < 2; i++) {
            if (i == 1) {
                if (overlay == null) break;
                color = overlayColor;
                light = glow ? 255 : light;
            }
            Color finalColor = color;
            int finalLight = light;

            queue.order(i + 1).submitCustomGeometry(matrices, i == 1 ? overlay : layer, ((matricesEntry, vertexConsumer) -> {
                vertexConsumer.addVertex(matrix, (float) ((float) posD.x + cam.x), (float) ((float) posD.y + cam.y), (float) ((float) posD.z + cam.z)).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 1, 0).setLight(finalLight).setUv(uvD.x, uvD.y).setColor(finalColor.getRed(), finalColor.getGreen(), finalColor.getBlue(), finalColor.getAlpha());
                vertexConsumer.addVertex(matrix, (float) ((float) posC.x + cam.x), (float) ((float) posC.y + cam.y), (float) ((float) posC.z + cam.z)).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 1, 0).setLight(finalLight).setUv(uvC.x, uvC.y).setColor(finalColor.getRed(), finalColor.getGreen(), finalColor.getBlue(), finalColor.getAlpha());
                vertexConsumer.addVertex(matrix, (float) ((float) posB.x + cam.x), (float) ((float) posB.y + cam.y), (float) ((float) posB.z + cam.z)).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 1, 0).setLight(finalLight).setUv(uvB.x, uvB.y).setColor(finalColor.getRed(), finalColor.getGreen(), finalColor.getBlue(), finalColor.getAlpha());
                vertexConsumer.addVertex(matrix, (float) ((float) posA.x + cam.x), (float) ((float) posA.y + cam.y), (float) ((float) posA.z + cam.z)).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 1, 0).setLight(finalLight).setUv(uvA.x, uvA.y).setColor(finalColor.getRed(), finalColor.getGreen(), finalColor.getBlue(), finalColor.getAlpha());

                vertexConsumer.addVertex(matrix, (float) ((float) posA.x + cam.x), (float) ((float) posA.y + cam.y), (float) ((float) posA.z + cam.z)).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 1, 0).setLight(finalLight).setUv(uvA.x, uvA.y).setColor(finalColor.getRed(), finalColor.getGreen(), finalColor.getBlue(), finalColor.getAlpha());
                vertexConsumer.addVertex(matrix, (float) ((float) posB.x + cam.x), (float) ((float) posB.y + cam.y), (float) ((float) posB.z + cam.z)).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 1, 0).setLight(finalLight).setUv(uvB.x, uvB.y).setColor(finalColor.getRed(), finalColor.getGreen(), finalColor.getBlue(), finalColor.getAlpha());
                vertexConsumer.addVertex(matrix, (float) ((float) posC.x + cam.x), (float) ((float) posC.y + cam.y), (float) ((float) posC.z + cam.z)).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 1, 0).setLight(finalLight).setUv(uvC.x, uvC.y).setColor(finalColor.getRed(), finalColor.getGreen(), finalColor.getBlue(), finalColor.getAlpha());
                vertexConsumer.addVertex(matrix, (float) ((float) posD.x + cam.x), (float) ((float) posD.y + cam.y), (float) ((float) posD.z + cam.z)).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 1, 0).setLight(finalLight).setUv(uvD.x, uvD.y).setColor(finalColor.getRed(), finalColor.getGreen(), finalColor.getBlue(), finalColor.getAlpha());
            }));
        }
    }
}
