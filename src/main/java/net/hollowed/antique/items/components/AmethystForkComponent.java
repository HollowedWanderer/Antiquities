package net.hollowed.antique.items.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import org.jspecify.annotations.NonNull;

import java.awt.*;
import java.util.function.Consumer;

public record AmethystForkComponent(
        boolean charged,
        int note
) implements TooltipProvider {
    public static final AmethystForkComponent DEFAULT = new AmethystForkComponent(false, 0);

    public static final Codec<AmethystForkComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("charged", false).forGetter(AmethystForkComponent::charged),
            Codec.INT.optionalFieldOf("note", 0).forGetter(AmethystForkComponent::note)
    ).apply(instance, AmethystForkComponent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AmethystForkComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, AmethystForkComponent::charged,
            ByteBufCodecs.INT, AmethystForkComponent::note,
            AmethystForkComponent::new
    );

    public AmethystForkComponent withCharged(boolean charged) {
        return new AmethystForkComponent(charged, note);
    }

    public AmethystForkComponent withNote(int note) {
        return new AmethystForkComponent(charged, note);
    }

    @Override
    public void addToTooltip(Item.@NonNull TooltipContext context, @NonNull Consumer<Component> consumer, @NonNull TooltipFlag flag, @NonNull DataComponentGetter dataGetter) {
        float s = (float) note / 24;
        float r = Math.max(0, Mth.sin(s * (float) Math.PI * 2) * 0.65f + 0.35f);
        float g = Math.max(0, Mth.sin((s + 1f / 3) * (float) Math.PI * 2) * 0.65f + 0.35f);
        float b = Math.max(0, Mth.sin((s + 2f / 3) * (float) Math.PI * 2) * 0.65f + 0.35f);

        consumer.accept(Component.translatable("antique.note", note + 1, Component.translatable("antique.note" + note).withColor(new Color(ARGB.colorFromFloat(1f, r, g, b)).brighter().getRGB())).withStyle(ChatFormatting.GRAY));
    }
}