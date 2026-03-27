package net.hollowed.antique.client.renderer.cloth;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.mojang.blaze3d.vertex.PoseStack;

import net.hollowed.antique.util.interfaces.duck.ClothAccess;
import net.hollowed.antique.util.resources.ClothSkinData;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
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

public class ClothManager {

    private static final double CAMERA_FOV_DECAY = .1;

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

    public void renderCloth(ClothSkinData.ClothSubData data, PoseStack matrices, SubmitNodeCollector queue, int light, boolean glow, Color color, Color overlayColor, Identifier overlay){
        this.renderCloth(data, matrices, queue, light, glow, color, overlayColor, overlay, new Matrix4f());
    }

    public void renderCloth(ClothSkinData.ClothSubData data, PoseStack matrices, SubmitNodeCollector queue, int light, boolean glow, Color color, Color overlayColor, Identifier overlay, Matrix4f reprojectionMatrix) {
        this.render = true;
        this.data = data;
        Identifier cloth = data.model();
        int bodyCount = data.bodyAmount();
        float width = data.width();
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

        Vector3f lastA = null;
        Vector3f lastB = null;

        Vector3d danglePos = new Vector3d(position.x, position.y, position.z);
        pos = new Vector3d(danglePos);

        matrices.pushPose();

        int count = bodies.size() - 1;

        // Get camera position, only once, no more is needed.
        final Vec3 cameraPosVec3d = Minecraft.getInstance().gameRenderer.getMainCamera().position();
        final Vector3d cameraPos = new Vector3d(cameraPosVec3d.x, cameraPosVec3d.y, cameraPosVec3d.z);
        
        Vector3f toCam = new Vector3f();
        Vector3f thicknessVec = new Vector3f();

        for (int i = 0; i < count; i++) {
            float worldPositionWeight = 1f - ((float)Math.exp(-i * CAMERA_FOV_DECAY));
            ClothBody body = bodies.get(i);
            ClothBody nextBody = bodies.get(i + 1);

            Vector3f pos = new Vector3f(body.getPos().sub(cameraPos));
            Vector3f nextPos = new Vector3f(nextBody.getPos().sub(cameraPos));

            applyReprojection(reprojectionMatrix, pos, worldPositionWeight);
            applyReprojection(reprojectionMatrix, nextPos, worldPositionWeight);

            float uvTop = (1f / count) * i;
            float uvBot = uvTop + (1f / count);

            // Compute thickness vector from segment midpoint
            pos.add(nextPos, toCam).normalize();
            pos.sub(nextPos, thicknessVec).cross(toCam).normalize().mul(width);

            Vector3f a = lastA != null ? lastA : pos.sub(thicknessVec, new Vector3f());
            Vector3f b = lastB != null ? lastB : pos.add(thicknessVec, new Vector3f());

            // Compute end vertices for this segment
            Vector3f posEnd = nextPos.add(thicknessVec, new Vector3f());
            Vector3f negEnd = nextPos.sub(thicknessVec, new Vector3f());

            // Cache for next loop
            lastA = negEnd;
            lastB = posEnd;

            RenderType clothLayer = getClothRenderLayer(cloth);
            String clothType = !Objects.equals(cloth.getPath(), "cloth") ? !cloth.getPath().isEmpty() ? cloth.getPath().substring(0, cloth.getPath().indexOf("_")) : "default" : "default";
            RenderType overlayLayer = getOverlayRenderLayer("_" + clothType, overlay);

            drawQuad(
                    matrices,
                    new Matrix4f(),
                    clothLayer,
                    !overlay.equals(Identifier.parse("")) ? overlayLayer : null,
                    queue,
                    a, 
                    b, 
                    posEnd, 
                    negEnd,
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

    private static void applyReprojection(Matrix4f res, Vector3f toReproj, float weight) {
        Vector4f transformed = res.transform(new Vector4f(toReproj.x, toReproj.y, toReproj.z, 1f));
        transformed.div(transformed.w);
        transformed.mul(weight);
        toReproj.mul(1f - weight);
        toReproj.x += transformed.x;
        toReproj.y += transformed.y;
        toReproj.z += transformed.z;
    }

    public void drawQuad(PoseStack matrices, Matrix4f matrix, RenderType layer, @Nullable RenderType overlay, SubmitNodeCollector queue, Vector3f posA, Vector3f posB, Vector3f posC, Vector3f posD, Vec2 uvA, Vec2 uvB, Vec2 uvC, Vec2 uvD, int light, boolean glow, Color color, Color overlayColor) {
        for (int i = 0; i < 2; i++) {
            if (i == 1) {
                if (overlay == null) break;
                color = overlayColor;
                light = glow ? 255 : light;
            }
            Color finalColor = color;
            int finalLight = light;

            queue.order(i + 1).submitCustomGeometry(matrices, i == 1 ? overlay : layer, ((matricesEntry, vertexConsumer) -> {
                vertexConsumer.addVertex(matrix, posD.x, posD.y, posD.z).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 1, 0).setLight(finalLight).setUv(uvD.x, uvD.y).setColor(finalColor.getRed(), finalColor.getGreen(), finalColor.getBlue(), finalColor.getAlpha());
                vertexConsumer.addVertex(matrix, posC.x, posC.y, posC.z).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 1, 0).setLight(finalLight).setUv(uvC.x, uvC.y).setColor(finalColor.getRed(), finalColor.getGreen(), finalColor.getBlue(), finalColor.getAlpha());
                vertexConsumer.addVertex(matrix, posB.x, posB.y, posB.z).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 1, 0).setLight(finalLight).setUv(uvB.x, uvB.y).setColor(finalColor.getRed(), finalColor.getGreen(), finalColor.getBlue(), finalColor.getAlpha());
                vertexConsumer.addVertex(matrix, posA.x, posA.y, posA.z).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 1, 0).setLight(finalLight).setUv(uvA.x, uvA.y).setColor(finalColor.getRed(), finalColor.getGreen(), finalColor.getBlue(), finalColor.getAlpha());
                
                vertexConsumer.addVertex(matrix, posA.x, posA.y, posA.z).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 1, 0).setLight(finalLight).setUv(uvA.x, uvA.y).setColor(finalColor.getRed(), finalColor.getGreen(), finalColor.getBlue(), finalColor.getAlpha());
                vertexConsumer.addVertex(matrix, posB.x, posB.y, posB.z).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 1, 0).setLight(finalLight).setUv(uvB.x, uvB.y).setColor(finalColor.getRed(), finalColor.getGreen(), finalColor.getBlue(), finalColor.getAlpha());
                vertexConsumer.addVertex(matrix, posC.x, posC.y, posC.z).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 1, 0).setLight(finalLight).setUv(uvC.x, uvC.y).setColor(finalColor.getRed(), finalColor.getGreen(), finalColor.getBlue(), finalColor.getAlpha());
                vertexConsumer.addVertex(matrix, posD.x, posD.y, posD.z).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 1, 0).setLight(finalLight).setUv(uvD.x, uvD.y).setColor(finalColor.getRed(), finalColor.getGreen(), finalColor.getBlue(), finalColor.getAlpha());
            }));
        }
    }
}
