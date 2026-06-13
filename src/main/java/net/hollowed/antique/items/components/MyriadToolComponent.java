package net.hollowed.antique.items.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import net.hollowed.antique.index.AntiqueDataComponentTypes;
import net.hollowed.antique.index.AntiqueItems;
import net.hollowed.antique.util.resources.ClothInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.TooltipProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record MyriadToolComponent(
        ItemStack toolBit,
        Optional<ClothInstance> cloth
) implements TooltipProvider {

    public static final MyriadToolComponent DEFAULT_WITH_CLOTH = new MyriadToolComponent(
            ItemStack.EMPTY,
            Optional.of(ClothInstance.DEFAULT)
    );
    public static final MyriadToolComponent DEFAULT_NO_CLOTH = new MyriadToolComponent(
            ItemStack.EMPTY,
            Optional.empty()
    );

    public static final Codec<MyriadToolComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.OPTIONAL_CODEC.fieldOf("tool_bit").orElse(ItemStack.EMPTY).forGetter(MyriadToolComponent::toolBit),
            ClothInstance.CODEC.optionalFieldOf("cloth").forGetter(MyriadToolComponent::cloth)
    ).apply(instance, MyriadToolComponent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MyriadToolComponent> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC, MyriadToolComponent::toolBit,
            ByteBufCodecs.optional(ClothInstance.STREAM_CODEC), MyriadToolComponent::cloth,
            MyriadToolComponent::new
    );

    public MyriadToolComponent withToolBit(ItemStack toolBit) {
        return new MyriadToolComponent(toolBit, cloth);
    }

    public MyriadToolComponent withCloth(Optional<ClothInstance> cloth) {
        return new MyriadToolComponent(toolBit, cloth);
    }

    public MyriadToolComponent withCloth(UnaryOperator<ClothInstance> cloth) {
        return withCloth(this.cloth.map(cloth));
    }

    @Override
    public void addToTooltip(Item.@NotNull TooltipContext context, @NotNull Consumer<Component> textConsumer, @NotNull TooltipFlag type, @NotNull DataComponentGetter components) {
        // TODO: replace current tooltip handling with this
    }

    public ItemStack removePattern(@Nullable ItemStack toolStack) {
        return cloth.flatMap(cloth -> cloth.pattern().map(pattern -> {
            ItemStack stack = new ItemStack(AntiqueItems.CLOTH_PATTERN);

            stack.set(AntiqueDataComponentTypes.CLOTH_PATTERN_TYPE, pattern);
            cloth.patternColor().ifPresentOrElse(
                    rgb -> stack.set(DataComponents.DYED_COLOR, new DyedItemColor(rgb)),
                    () -> stack.remove(DataComponents.DYED_COLOR)
            );

            if (toolStack != null) {
                toolStack.set(AntiqueDataComponentTypes.MYRIAD_TOOL, withCloth(Optional.of(new ClothInstance(cloth.cloth(), cloth.clothColor(), Optional.empty(), Optional.empty()))));
            }

            return stack;
        })).orElse(ItemStack.EMPTY);
    }

    public ItemStack removeCloth(@Nullable ItemStack toolStack) {
        return cloth.map(cloth -> {
            ItemStack stack = new ItemStack(AntiqueItems.CLOTH);

            stack.set(AntiqueDataComponentTypes.CLOTH_TYPE, cloth.cloth());
            cloth.clothColor().ifPresentOrElse(
                    rgb -> stack.set(DataComponents.DYED_COLOR, new DyedItemColor(rgb)),
                    () -> stack.remove(DataComponents.DYED_COLOR)
            );

            if (toolStack != null) {
                toolStack.set(AntiqueDataComponentTypes.MYRIAD_TOOL, withCloth(Optional.empty()));
            }

            return stack;
        }).orElse(ItemStack.EMPTY);
    }

    public ItemStack removeClothOrPattern(@Nullable ItemStack toolStack) {
        return cloth.map(cloth -> cloth.pattern().map(pattern -> {
            ItemStack stack = new ItemStack(AntiqueItems.CLOTH_PATTERN);

            stack.set(AntiqueDataComponentTypes.CLOTH_PATTERN_TYPE, pattern);
            cloth.patternColor().ifPresentOrElse(
                    rgb -> stack.set(DataComponents.DYED_COLOR, new DyedItemColor(rgb)),
                    () -> stack.remove(DataComponents.DYED_COLOR)
            );

            if (toolStack != null) {
                toolStack.set(AntiqueDataComponentTypes.MYRIAD_TOOL, withCloth(Optional.of(new ClothInstance(cloth.cloth(), cloth.clothColor(), Optional.empty(), Optional.empty()))));
            }

            return stack;
        }).orElseGet(() -> {
            ItemStack stack = new ItemStack(AntiqueItems.CLOTH);

            stack.set(AntiqueDataComponentTypes.CLOTH_TYPE, cloth.cloth());
            cloth.clothColor().ifPresentOrElse(
                    rgb -> stack.set(DataComponents.DYED_COLOR, new DyedItemColor(rgb)),
                    () -> stack.remove(DataComponents.DYED_COLOR)
            );

            if (toolStack != null) {
                toolStack.set(AntiqueDataComponentTypes.MYRIAD_TOOL, withCloth(Optional.empty()));
            }

            return stack;
        })).orElse(ItemStack.EMPTY);
    }
}