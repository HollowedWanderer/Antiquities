package net.hollowed.antique.client.renderer.cloth;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import net.hollowed.antique.Antiquities;
import net.hollowed.antique.AntiquitiesClient;
import net.hollowed.antique.entities.parts.MyriadShovelPart;
import net.hollowed.antique.index.AntiqueParticles;
import net.hollowed.antique.items.components.ColorProvider;
import net.hollowed.antique.mixin.accessors.SpriteContentsAnimationStateAccessor;
import net.hollowed.antique.particles.TyphoSparkParticle;
import net.hollowed.antique.util.resources.ClothSkin;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.mojang.blaze3d.vertex.PoseStack;

import net.hollowed.antique.util.interfaces.duck.ClothAccess;
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
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class ClothManager {

    private static final double CAMERA_FOV_DECAY = .1;

    public static final RenderType CLOTH_RENDER_LAYER = RenderTypes.itemEntityTranslucentCull(AntiquitiesClient.CLOTHS_ATLAS_TEXTURE);

    public Vector3d pos = new Vector3d();
    public List<ClothBody> bodies = new ArrayList<>();
    private int bodyCountCooldown = 0;
    public Entity entity;
    public ClothSkin data;
    public boolean render = false;

    private List<Entity> collisionEntities = List.of();
    private long prevTime;

    public ClothManager(Vector3d pos, int BodyCount, ClothSkin data) {
        reset(pos, BodyCount, data);
    }

    public void reset(Vector3d pos, int BodyCount, ClothSkin data) {
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
        float delta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks();
        ClientLevel world = Minecraft.getInstance().level;

        ClothBody root = bodies.getFirst();
        root.pos = new Vector3d(root.prevPos).lerp(pos, delta * 2);

        for (ClothBody body : bodies) {
            body.prevPos = new Vector3d(body.pos);
        }

        if (delta == 0) {
            for (ClothBody body : bodies) {
                body.posCache.set(body.pos);
            }
        }

        if (world != null) {

            double previousDrag = 0.0;

            // Update pass
            for (ClothBody body : bodies) {
                Vec3 startPos = new Vec3(body.pos.x, body.pos.y, body.pos.z);
                boolean isWater = world.getFluidState(BlockPos.containing(startPos)).is(FluidTags.WATER);
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
                double newDrag = Math.random() * (isWater ? 0.25 : 1.25);
                double smoothDrag = Mth.lerp(delta * 0.1, previousDrag, newDrag);

                // Apply gravity
                double gravity = 0.05 * gravityMultiplier;
                if(isWater) {
                    gravity *= waterGravityMultiplier;
                }
                gravity /= 1;

                body.accel.add(0, -gravity, 0);

                previousDrag = smoothDrag;
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

            if (world.getGameTime() > (prevTime + 10)) {
                prevTime = world.getGameTime();
                Vec3 pos = new Vec3(new Vector3f(bodies.getFirst().pos));
                collisionEntities = world.getEntities(entity, new AABB(pos.subtract(5), pos.add(5)), entity -> !(entity instanceof MyriadShovelPart));
            }

            for (ClothBody body : bodies) {
                body.slideOutOfBlocks(world);
                accels.add(body.entityCollisionPerchance(collisionEntities, entity));
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

        double maxDistance = 1.0;
        if (root.pos.distance(root.posCache) > maxDistance) {
            resetCloth();
        }
    }

    public void tickParticles(Level level) {
        data.particleData().ifPresent(data -> {
            for (int i = 0; i < bodies.size(); i++) {
                ClothBody body = bodies.get(i);
                boolean water = level.getFluidState(BlockPos.containing(body.pos.x, body.pos.y, body.pos.z)).is(FluidTags.WATER);
                ParticleOptions particle = data.particle();
                float chance = data.chance();
                float distance = data.distance();
                float velocity = data.velocity();

                if (water) {
                    particle = data.waterParticle().orElse(particle);
                    chance = data.waterChance().orElse(chance);
                    distance = data.waterDistance().orElse(distance);
                    velocity = data.waterVelocity().orElse(velocity);
                }

                if (level.random.nextFloat() < chance) {
                    Vector3d pos = new Vector3d(
                            level.random.nextDouble() * 2 - 1,
                            level.random.nextDouble() * 2 - 1,
                            level.random.nextDouble() * 2 - 1
                    ).normalize();

                    if (particle.getType() == AntiqueParticles.TYPHO_SPARK) {
                        SpriteContentsAnimationStateAccessor accessor = ColorProvider.SpriteAnimated.findAnimationState(
                                AntiquitiesClient.CLOTHS_ATLAS,
                                Antiquities.id("cloth/typho_cloth_emissive")
                        );
                        particle = new TyphoSparkParticle.Options(
                                Optional.of((int) (((float) i / bodies.size() + (23 - accessor.antique$getFrame()) / 23f) * 8) % 8)
                        );
                    }

                    level.addParticle(
                            particle,
                            body.pos.x + pos.x * distance,
                            body.pos.y + pos.y * distance,
                            body.pos.z + pos.z * distance,
                            pos.x * velocity,
                            pos.y * velocity,
                            pos.z * velocity
                    );
                }
            }
        });
    }

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

    public static ClothManager getOrCreate(Entity entity, Identifier id, ClothSkin data) {
        if (Minecraft.getInstance().level instanceof ClothAccess clothAccess) {
            return clothAccess.antique$getManagers().computeIfAbsent(entity, k -> new HashMap<>()).computeIfAbsent(id, k -> {
                ClothManager manager = new ClothManager(new Vector3d(entity.getX(), entity.getY(), entity.getZ()), 8, data);
                manager.entity = entity;
                return manager;
            });
        }

        return null;
    }

    public void renderCloth(ClothSkin data, PoseStack matrices, SubmitNodeCollector queue, int light, boolean glow, Color color, Color overlayColor, Optional<Identifier> overlay) {
        this.renderCloth(data, matrices, queue, light, glow, color, overlayColor, overlay, new Matrix4f());
    }

    public void renderCloth(ClothSkin data, PoseStack matrices, SubmitNodeCollector queue, int light, boolean glow, Color color, Color overlayColor, Optional<Identifier> overlay, Matrix4f reprojectionMatrix) {
        this.render = true;
        this.data = data;
        Optional<Identifier> cloth = data.model();
        int bodyCount = data.bodyAmount();
        float width = data.width();
        if (data.light() != 0) light = data.light();
        if (!data.dyeable()) color = Color.WHITE;

        if (cloth.isEmpty()) return;

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

            Vector3f pos = new Vector3f(new Vector3f(body.getPos()).sub(new Vector3f(cameraPos)));
            Vector3f nextPos = new Vector3f(new Vector3f(nextBody.getPos()).sub(new Vector3f(cameraPos)));

            if (i == 0) pos = new Vector3f(this.pos).sub(new Vector3f(cameraPos));

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

            String clothType = cloth.map(id -> id.getPath().equals("cloth") ? "default" : id.getPath().substring(0, id.getPath().indexOf('_'))).orElse("default");
            int finalLight = light;
            Color finalColor = color;

            cloth.ifPresent(id -> {
                var sprite = Minecraft.getInstance()
                        .getAtlasManager()
                        .getAtlasOrThrow(AntiquitiesClient.CLOTHS_ATLAS)
                        .getSprite(id.withPrefix("cloth/"));
                drawQuad(
                        matrices,
                        new Matrix4f(),
                        CLOTH_RENDER_LAYER,
                        queue,
                        1,
                        a,
                        b,
                        posEnd,
                        negEnd,
                        new Vec2(sprite.getU0(), sprite.getV(uvTop)),
                        new Vec2(sprite.getU1(), sprite.getV(uvTop)),
                        new Vec2(sprite.getU1(), sprite.getV(uvBot)),
                        new Vec2(sprite.getU0(), sprite.getV(uvBot)),
                        finalLight,
                        finalColor
                );

                if (data.emissiveLayer()) {
                    sprite = Minecraft.getInstance()
                            .getAtlasManager()
                            .getAtlasOrThrow(AntiquitiesClient.CLOTHS_ATLAS)
                            .getSprite(id.withPrefix("cloth/").withSuffix("_emissive"));
                    drawQuad(
                            matrices,
                            new Matrix4f(),
                            CLOTH_RENDER_LAYER,
                            queue,
                            2,
                            a,
                            b,
                            posEnd,
                            negEnd,
                            new Vec2(sprite.getU0(), sprite.getV(uvTop)),
                            new Vec2(sprite.getU1(), sprite.getV(uvTop)),
                            new Vec2(sprite.getU1(), sprite.getV(uvBot)),
                            new Vec2(sprite.getU0(), sprite.getV(uvBot)),
                            255,
                            finalColor
                    );
                }
            });

            overlay.ifPresent(id -> {
                var sprite = Minecraft.getInstance()
                        .getAtlasManager()
                        .getAtlasOrThrow(AntiquitiesClient.CLOTHS_ATLAS)
                        .getSprite(id.withPrefix("cloth/overlay/").withSuffix("_" + clothType));
                drawQuad(
                        matrices,
                        new Matrix4f(),
                        CLOTH_RENDER_LAYER,
                        queue,
                        3,
                        a,
                        b,
                        posEnd,
                        negEnd,
                        new Vec2(sprite.getU0(), sprite.getV(uvTop)),
                        new Vec2(sprite.getU1(), sprite.getV(uvTop)),
                        new Vec2(sprite.getU1(), sprite.getV(uvBot)),
                        new Vec2(sprite.getU0(), sprite.getV(uvBot)),
                        glow ? 255 : finalLight,
                        overlayColor
                );
            });
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

    public void drawQuad(PoseStack matrices, Matrix4f matrix, RenderType layer, SubmitNodeCollector queue, int order, Vector3f posA, Vector3f posB, Vector3f posC, Vector3f posD, Vec2 uvA, Vec2 uvB, Vec2 uvC, Vec2 uvD, int light, Color color) {
        queue.order(order).submitCustomGeometry(matrices, layer, (matricesEntry, vertexConsumer) -> {
            vertexConsumer.addVertex(matrix, posD.x, posD.y, posD.z).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 1, 0).setLight(light).setUv(uvC.x, uvC.y).setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
            vertexConsumer.addVertex(matrix, posC.x, posC.y, posC.z).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 1, 0).setLight(light).setUv(uvD.x, uvD.y).setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
            vertexConsumer.addVertex(matrix, posB.x, posB.y, posB.z).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 1, 0).setLight(light).setUv(uvA.x, uvA.y).setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
            vertexConsumer.addVertex(matrix, posA.x, posA.y, posA.z).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 1, 0).setLight(light).setUv(uvB.x, uvB.y).setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());

            vertexConsumer.addVertex(matrix, posA.x, posA.y, posA.z).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 1, 0).setLight(light).setUv(uvB.x, uvB.y).setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
            vertexConsumer.addVertex(matrix, posB.x, posB.y, posB.z).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 1, 0).setLight(light).setUv(uvA.x, uvA.y).setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
            vertexConsumer.addVertex(matrix, posC.x, posC.y, posC.z).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 1, 0).setLight(light).setUv(uvD.x, uvD.y).setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
            vertexConsumer.addVertex(matrix, posD.x, posD.y, posD.z).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 1, 0).setLight(light).setUv(uvC.x, uvC.y).setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        });
    }

    @Override
    public String toString() {
        return "ClothManager{pos=" + pos + ", entity=" + entity + ", skin=" + data.model() + "}";
    }
}
