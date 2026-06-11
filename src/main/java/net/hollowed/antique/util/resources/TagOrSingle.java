package net.hollowed.antique.util.resources;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public sealed interface TagOrSingle<T> {
    boolean isDefault();

    static <T> Codec<TagOrSingle<T>> codec(ResourceKey<? extends Registry<T>> registry) {
        return Codec.either(
                Identifier.CODEC.<Single<T>>xmap(
                        Single::new,
                        Single::id
                ),
                Codec.STRING.flatXmap(
                        text -> text.startsWith("#") ? DataResult.success(new Tag<>(TagKey.create(registry, Identifier.parse(text.substring(1))))) : DataResult.error(() -> "TagOrSingle.Tag must start with #"),
                        tag -> DataResult.success("#" + tag.key.location())
                )
        ).xmap(
                either -> either.map(l -> l, r -> r),
                value -> {
                    if (value instanceof Single<?>) {
                        return Either.left((Single<T>) value);
                    } else {
                        return Either.right((Tag<T>) value);
                    }
                }
        );
    }

    record Single<T>(
            Identifier id
    ) implements TagOrSingle<T> {
        @Override
        public boolean isDefault() {
            return id.equals(MyriadStaffTransformData.DEFAULT_MODEL);
        }
    }

    record Tag<T>(
            TagKey<T> key
    ) implements TagOrSingle<T> {
        @Override
        public boolean isDefault() {
            return false;
        }
    }
}
