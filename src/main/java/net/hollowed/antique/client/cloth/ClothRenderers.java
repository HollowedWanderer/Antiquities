package net.hollowed.antique.client.cloth;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.hollowed.antique.util.CodecUtil;
import net.hollowed.antique.util.resources.client.ClothSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ClothRenderers {
    private ClothRenderers() {
    }

    public static final BasicClothRenderer DEFAULT = new BasicClothRenderer(List.of());
    public static final Map<Identifier, MapCodec<? extends ClothRenderer>> REGISTRY = Util.make(() -> {
        Map<Identifier, MapCodec<? extends ClothRenderer>> map = new HashMap<>();
        map.put(BasicClothRenderer.ID, BasicClothRenderer.CODEC);
        return map;
    });
    @SuppressWarnings("unchecked")
    public static final Codec<ClothRenderer> CODEC = Codec.either(
            CodecUtil.compactListOf(ClothSprite.CODEC).xmap(
                    BasicClothRenderer::new,
                    renderer -> renderer.sprites
            ),
            Identifier.CODEC.dispatch(ClothRenderer::getType, id -> (MapCodec<ClothRenderer>) Objects.requireNonNull(REGISTRY.get(id), () -> "Nonexistent cloth renderer " + id))
    ).xmap(
            either -> either.map(l -> l, r -> r),
            renderer -> renderer instanceof BasicClothRenderer basic ? Either.left(basic) : Either.right(renderer)
    );
}
