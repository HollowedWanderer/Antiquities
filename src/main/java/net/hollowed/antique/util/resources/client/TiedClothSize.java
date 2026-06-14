package net.hollowed.antique.util.resources.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum TiedClothSize {
    NORMAL,
    LARGE;

    public static final Codec<TiedClothSize> CODEC = Codec.STRING.flatXmap(
            string -> {
                try {
                    return DataResult.success(valueOf(string.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    return DataResult.error(e::getMessage);
                }
            },
            size -> DataResult.success(size.name())
    );
}
