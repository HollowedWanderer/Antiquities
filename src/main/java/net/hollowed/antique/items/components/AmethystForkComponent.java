package net.hollowed.antique.items.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
        int extraPrevNote = (note + 23) % 25;
        int prevNote = (note + 24) % 25;
        int nextNote = (note + 1) % 25;
        int extraNextNote = (note + 2) % 25;

        consumer.accept(Component.translatable("antique.note_extra_fade", extraNextNote + 1, Component.translatable("antique.note" + extraNextNote).withColor(new Color(ARGB.colorFromFloat(1f, getRed(extraNextNote), getGreen(extraNextNote), getBlue(extraNextNote))).darker().getRGB())).withColor(5592405));
        consumer.accept(Component.translatable("antique.note_fade", nextNote + 1, Component.translatable("antique.note" + nextNote).withColor(new Color(ARGB.colorFromFloat(1f, getRed(nextNote), getGreen(nextNote), getBlue(nextNote))).getRGB())).withColor(7763574));
        consumer.accept(Component.translatable("antique.note", note + 1, Component.translatable("antique.note" + note).withColor(new Color(ARGB.colorFromFloat(1f, getRed(note), getGreen(note), getBlue(note))).brighter().getRGB())).withColor(11184810));
        consumer.accept(Component.translatable("antique.note_fade", prevNote + 1, Component.translatable("antique.note" + prevNote).withColor(new Color(ARGB.colorFromFloat(1f, getRed(prevNote), getGreen(prevNote), getBlue(prevNote))).getRGB())).withColor(7763574));
        consumer.accept(Component.translatable("antique.note_extra_fade", extraPrevNote + 1, Component.translatable("antique.note" + extraPrevNote).withColor(new Color(ARGB.colorFromFloat(1f, getRed(extraPrevNote), getGreen(extraPrevNote), getBlue(extraPrevNote))).darker().getRGB())).withColor(5592405));
    }

    private float getRed(float input) {
        return Math.max(0, Mth.sin((input / 24) * (float) Math.PI * 2) * 0.65f + 0.35f);
    }

    private float getGreen(float input) {
        return Math.max(0, Mth.sin(((input / 24) + 1f / 3) * (float) Math.PI * 2) * 0.65f + 0.35f);
    }

    private float getBlue(float input) {
        return Math.max(0, Mth.sin(((input / 24) + 2f / 3) * (float) Math.PI * 2) * 0.65f + 0.35f);
    }
}