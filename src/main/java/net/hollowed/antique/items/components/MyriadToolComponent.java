package net.hollowed.antique.items.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;
import java.util.function.Consumer;

import net.hollowed.antique.Antiquities;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import org.jetbrains.annotations.NotNull;

public record MyriadToolComponent(ItemStack toolBit, Optional<Identifier> clothType, Optional<Identifier> clothPattern, int clothColor, int patternColor) implements TooltipProvider {

    public static final MyriadToolComponent DEFAULT_WITH_CLOTH = new MyriadToolComponent(
            ItemStack.EMPTY,
            Optional.of(Antiquities.id("cloth")),
            Optional.empty(),
            0xD43B69,
            0xFFFFFF
    );
    public static final MyriadToolComponent DEFAULT_NO_CLOTH = new MyriadToolComponent(
            ItemStack.EMPTY,
            Optional.empty(),
            Optional.empty(),
            0xFFFFFF,
            0xFFFFFF
    );

    public static final Codec<MyriadToolComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.xmap(
                    stack -> stack.getItem() == Items.BARRIER ? ItemStack.EMPTY : stack,
                    stack -> stack.isEmpty() ? new ItemStack(Items.BARRIER) : stack
            ).fieldOf("tool_bit").orElse(ItemStack.EMPTY).forGetter(MyriadToolComponent::toolBit),
            Identifier.CODEC.optionalFieldOf("cloth_type").forGetter(MyriadToolComponent::clothType),
            Identifier.CODEC.optionalFieldOf("cloth_pattern").forGetter(MyriadToolComponent::clothPattern),
            Codec.INT.fieldOf("cloth_color").forGetter(MyriadToolComponent::clothColor),
            Codec.INT.fieldOf("pattern_color").forGetter(MyriadToolComponent::patternColor)
    ).apply(instance, MyriadToolComponent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MyriadToolComponent> PACKET_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, MyriadToolComponent::toolBit,
            ByteBufCodecs.optional(Identifier.STREAM_CODEC), MyriadToolComponent::clothType,
            ByteBufCodecs.optional(Identifier.STREAM_CODEC), MyriadToolComponent::clothPattern,
            ByteBufCodecs.INT, MyriadToolComponent::clothColor,
            ByteBufCodecs.INT, MyriadToolComponent::patternColor,
            MyriadToolComponent::new
    );

    @Override
    public void addToTooltip(Item.@NotNull TooltipContext context, @NotNull Consumer<Component> textConsumer, @NotNull TooltipFlag type, @NotNull DataComponentGetter components) {
        // TODO: replace current tooltip handling with this
    }
}