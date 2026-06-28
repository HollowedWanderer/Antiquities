package net.hollowed.antique.util.resources.client;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public record ClothSprite(
        Identifier texture,
        Optional<Integer> light,
        boolean tint
) {
    public static final Codec<ClothSprite> CODEC = Codec.either(
            RecordCodecBuilder.<ClothSprite>create(instance -> instance.group(
                    Identifier.CODEC.fieldOf("texture").forGetter(ClothSprite::texture),
                    Codec.INT.optionalFieldOf("light").forGetter(ClothSprite::light),
                    Codec.BOOL.optionalFieldOf("tint", false).forGetter(ClothSprite::tint)
            ).apply(instance, ClothSprite::new)),
            Identifier.CODEC.xmap(
                    texture -> new ClothSprite(texture, Optional.empty(), false),
                    ClothSprite::texture
            )
    ).xmap(
            either -> either.map(l -> l, r -> r),
            sprite -> sprite.light.isEmpty() || !sprite.tint ? Either.right(sprite) : Either.left(sprite)
    );

    public static @NotNull List<BakedQuad> applyToQuads(List<BakedQuad> quads, List<ClothSprite> sprites) {
        List<BakedQuad> newQuads = new ArrayList<>();

        quadLoop:
        for (BakedQuad quad : quads) {
            Identifier spriteId = quad.materialInfo().sprite().contents().name();

            for (ClothSprite sprite : sprites) {
                if (sprite.texture.equals(spriteId)) {
                    newQuads.add(sprite.light.map(_ -> new BakedQuad(
                            quad.position0(),
                            quad.position1(),
                            quad.position2(),
                            quad.position3(),
                            quad.packedUV0(),
                            quad.packedUV1(),
                            quad.packedUV2(),
                            quad.packedUV3(),
                            quad.direction(),
                            quad.materialInfo()
                    )).orElse(quad));
                    continue quadLoop;
                }
            }

            newQuads.add(quad);
        }

        return newQuads;
    }
}
