package net.hollowed.antique.items.components;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ColorProviders {
    private ColorProviders() {
    }

    public static final Map<Identifier, MapCodec<? extends ColorProvider>> REGISTRY = Util.make(() -> {
        Map<Identifier, MapCodec<? extends ColorProvider>> map = new HashMap<>();
        map.put(ColorProvider.Constant.ID, ColorProvider.Constant.CODEC);
        map.put(ColorProvider.Animated.ID, ColorProvider.Animated.CODEC);
        return map;
    });
    public static final Map<Identifier, StreamCodec<ByteBuf, ? extends ColorProvider>> STREAM_REGISTRY = Util.make(() -> {
        Map<Identifier, StreamCodec<ByteBuf, ? extends ColorProvider>> map = new HashMap<>();
        map.put(ColorProvider.Constant.ID, ColorProvider.Constant.STREAM_CODEC);
        map.put(ColorProvider.Animated.ID, ColorProvider.Animated.STREAM_CODEC);
        return map;
    });
    @SuppressWarnings("unchecked")
    public static final Codec<ColorProvider> CODEC = Codec.either(
            Codec.STRING.flatXmap(hex -> DataResult.success(new ColorProvider.Constant(hex)), color -> color instanceof ColorProvider.Constant(int color1) ? DataResult.success(Integer.toHexString(color1)) : DataResult.error(() -> "Must be Constant")),
            Identifier.CODEC.dispatch(ColorProvider::getType, id -> (MapCodec<ColorProvider>) Objects.requireNonNull(REGISTRY.get(id), () -> "Nonexistent color provider " + id))
    ).xmap(
            either -> either.map(l -> l, r -> r),
            color -> color instanceof ColorProvider.Constant constant ? Either.left(constant) : Either.right(color)
    );
    public static final StreamCodec<ByteBuf, ColorProvider> STREAM_CODEC = Identifier.STREAM_CODEC.dispatch(ColorProvider::getType, id -> Objects.requireNonNull(STREAM_REGISTRY.get(id), () -> "Nonexistent color provider " + id));
}
