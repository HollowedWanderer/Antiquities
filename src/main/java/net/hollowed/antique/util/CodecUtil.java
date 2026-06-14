package net.hollowed.antique.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

import java.util.List;

public class CodecUtil {
    private CodecUtil() {
    }

    public static <T> Codec<List<T>> compactListOf(Codec<T> elementCodec) {
        return Codec.either(
                elementCodec.listOf(),
                elementCodec
        ).xmap(
                either -> either.map(l -> l, List::of),
                list -> list.isEmpty() ? Either.right(list.getFirst()) : Either.left(list)
        );
    }
}
