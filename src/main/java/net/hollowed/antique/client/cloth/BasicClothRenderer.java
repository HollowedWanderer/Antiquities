package net.hollowed.antique.client.cloth;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.hollowed.antique.Antiquities;
import net.hollowed.antique.AntiquitiesClient;
import net.hollowed.antique.client.renderer.cloth.ClothBody;
import net.hollowed.antique.client.renderer.cloth.ClothManager;
import net.hollowed.antique.util.CodecUtil;
import net.hollowed.antique.util.resources.ClothPatternData;
import net.hollowed.antique.util.resources.ClothPatternModelListener;
import net.hollowed.antique.util.resources.ClothSkinData;
import net.hollowed.antique.util.resources.client.ClothPatternModelData;
import net.hollowed.antique.util.resources.client.ClothSprite;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.awt.*;
import java.util.List;
import java.util.Optional;

public class BasicClothRenderer implements ClothRenderer {
    public static final Identifier ID = Antiquities.id("basic");
    public static final MapCodec<BasicClothRenderer> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            CodecUtil.compactListOf(ClothSprite.CODEC).optionalFieldOf("sprites", List.of()).forGetter(renderer -> renderer.sprites)
    ).apply(instance, BasicClothRenderer::new));

    private static final double CAMERA_FOV_DECAY = .1;

    public static final RenderType CLOTH_RENDER_LAYER = RenderTypes.itemEntityTranslucentCull(AntiquitiesClient.CLOTHS_ATLAS_TEXTURE);

    public final List<ClothSprite> sprites;

    public BasicClothRenderer(List<ClothSprite> sprites) {
        this.sprites = sprites;
    }

    @Override
    public Identifier getType() {
        return ID;
    }

    @Override
    public BasicClothRenderer fillDefaults(Identifier id) {
        List<ClothSprite> sprites = this.sprites;

        if (sprites.isEmpty()) {
            sprites = List.of(new ClothSprite(id.withPrefix("cloth/"), Optional.empty(), false));
        }

        return new BasicClothRenderer(sprites);
    }

    @Override
    public void render(
            ClothManager cloth,
            Holder<ClothSkinData> skin,
            PoseStack matrices,
            SubmitNodeCollector queue,
            int light,
            boolean patternGlow,
            Color color,
            Color patternColor,
            Optional<? extends Holder<ClothPatternData>> pattern,
            Matrix4f reprojectionMatrix
    ) {
        float width = skin.value().width();
        if (skin.value().light() != 0) light = skin.value().light();
        if (!skin.value().dyeable()) color = Color.WHITE;

        Vector3f lastA = null;
        Vector3f lastB = null;

        matrices.pushPose();

        int count = cloth.bodies.size() - 1;

        // Get camera position, only once, no more is needed.
        final Vec3 cameraPosVec3d = Minecraft.getInstance().gameRenderer.getMainCamera().position();
        final Vector3d cameraPos = new Vector3d(cameraPosVec3d.x, cameraPosVec3d.y, cameraPosVec3d.z);

        Vector3f toCam = new Vector3f();
        Vector3f thicknessVec = new Vector3f();

        for (int i = 0; i < count; i++) {
            float worldPositionWeight = 1f - ((float)Math.exp(-i * CAMERA_FOV_DECAY));
            ClothBody body = cloth.bodies.get(i);
            ClothBody nextBody = cloth.bodies.get(i + 1);

            Vector3f pos = new Vector3f(body.getPos()).sub(new Vector3f(cameraPos));
            Vector3f nextPos = new Vector3f(nextBody.getPos()).sub(new Vector3f(cameraPos));

            if (i == 0) pos = new Vector3f(cloth.pos).sub(new Vector3f(cameraPos));

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

            int finalLight = light;
            int[] layer = { 1 };

            for (ClothSprite spriteData : sprites) {
                TextureAtlasSprite sprite = Minecraft.getInstance()
                        .getAtlasManager()
                        .getAtlasOrThrow(AntiquitiesClient.CLOTHS_ATLAS)
                        .getSprite(spriteData.texture());
                drawQuad(
                        matrices,
                        new Matrix4f(),
                        CLOTH_RENDER_LAYER,
                        queue,
                        layer[0]++,
                        a,
                        b,
                        posEnd,
                        negEnd,
                        new Vec2(sprite.getU0(), sprite.getV(uvTop)),
                        new Vec2(sprite.getU1(), sprite.getV(uvTop)),
                        new Vec2(sprite.getU1(), sprite.getV(uvBot)),
                        new Vec2(sprite.getU0(), sprite.getV(uvBot)),
                        spriteData.light().map(l -> LightTexture.pack(l, l)).orElse(finalLight),
                        color
                );
            }

            pattern.ifPresent(holder -> {
                skin.value().shape().ifPresent(shape -> {
                    ClothPatternModelData patternModel = ClothPatternModelListener.MODELS.get(holder.value().model().orElseGet(() -> holder.unwrapKey().orElseThrow().identifier()));

                    if (patternModel != null) {
                        for (ClothSprite spriteData : patternModel.worldSprites()) {
                            TextureAtlasSprite sprite = Minecraft.getInstance()
                                    .getAtlasManager()
                                    .getAtlasOrThrow(AntiquitiesClient.CLOTHS_ATLAS)
                                    .getSprite(spriteData.texture().withSuffix("_" + shape));
                            drawQuad(
                                    matrices,
                                    new Matrix4f(),
                                    CLOTH_RENDER_LAYER,
                                    queue,
                                    layer[0]++,
                                    a,
                                    b,
                                    posEnd,
                                    negEnd,
                                    new Vec2(sprite.getU0(), sprite.getV(uvTop)),
                                    new Vec2(sprite.getU1(), sprite.getV(uvTop)),
                                    new Vec2(sprite.getU1(), sprite.getV(uvBot)),
                                    new Vec2(sprite.getU0(), sprite.getV(uvBot)),
                                    patternGlow ? 255 : finalLight,
                                    patternColor
                            );
                        }
                    }
                });
            });
        }

        matrices.popPose();
    }

    public static void applyReprojection(Matrix4f res, Vector3f toReproj, float weight) {
        Vector4f transformed = res.transform(new Vector4f(toReproj.x, toReproj.y, toReproj.z, 1f));
        transformed.div(transformed.w);
        transformed.mul(weight);
        toReproj.mul(1f - weight);
        toReproj.x += transformed.x;
        toReproj.y += transformed.y;
        toReproj.z += transformed.z;
    }

    public static void drawQuad(PoseStack matrices, Matrix4f matrix, RenderType layer, SubmitNodeCollector queue, int order, Vector3f posA, Vector3f posB, Vector3f posC, Vector3f posD, Vec2 uvA, Vec2 uvB, Vec2 uvC, Vec2 uvD, int light, Color color) {
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
}
