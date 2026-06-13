package net.hollowed.antique.util.resources.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hollowed.antique.Antiquities;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public enum TiedClothDomain {
    INVENTORY,
    WORLD;

    public static final Codec<TiedClothDomain> CODEC = Codec.STRING.flatXmap(
            string -> {
                try {
                    return DataResult.success(valueOf(string.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    return DataResult.error(e::getMessage);
                }
            },
            size -> DataResult.success(size.name())
    );

    public Identifier defaultSprite(TiedClothSize size) {
        return switch (this) {
            case INVENTORY -> switch (size) {
                case NORMAL -> Antiquities.id("item/cloth_item");
                case LARGE -> Antiquities.id("item/cloth_item_large");
            };
            case WORLD -> switch (size) {
                case NORMAL -> Antiquities.id("item/cloth_hand");
                case LARGE -> Antiquities.id("item/cloth_hand_large");
            };
        };
    }
}
