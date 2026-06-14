package net.hollowed.antique.items.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;
import java.util.function.UnaryOperator;

import net.hollowed.antique.index.AntiqueItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record MyriadToolComponent(
        ItemStack toolBit,
        Optional<ItemStack> cloth
) {

    public static MyriadToolComponent getDefaultWithCloth() {
        return new MyriadToolComponent(
                ItemStack.EMPTY,
                Optional.of(AntiqueItems.CLOTH.getDefaultInstance())
        );
    }

    public static final MyriadToolComponent DEFAULT_NO_CLOTH = new MyriadToolComponent(
            ItemStack.EMPTY,
            Optional.empty()
    );

    public static final Codec<MyriadToolComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.OPTIONAL_CODEC.fieldOf("tool_bit").orElse(ItemStack.EMPTY).forGetter(MyriadToolComponent::toolBit),
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("cloth").forGetter(MyriadToolComponent::cloth)
    ).apply(instance, MyriadToolComponent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MyriadToolComponent> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC, MyriadToolComponent::toolBit,
            ByteBufCodecs.optional(ItemStack.OPTIONAL_STREAM_CODEC), MyriadToolComponent::cloth,
            MyriadToolComponent::new
    );

    public MyriadToolComponent withToolBit(ItemStack toolBit) {
        return new MyriadToolComponent(toolBit, cloth);
    }

    public MyriadToolComponent withCloth(ItemStack cloth) {
        return withCloth(Optional.of(cloth));
    }

    public MyriadToolComponent withCloth(Optional<ItemStack> cloth) {
        return new MyriadToolComponent(toolBit, cloth);
    }

    public MyriadToolComponent withCloth(UnaryOperator<ItemStack> cloth) {
        return withCloth(this.cloth.map(cloth));
    }
}