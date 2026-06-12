package net.hollowed.antique.util.resources;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.hollowed.antique.Antiquities;
import net.hollowed.antique.index.AntiqueDataComponentTypes;
import net.hollowed.antique.index.AntiqueItems;
import net.hollowed.antique.index.AntiqueRegistries;
import net.hollowed.antique.items.components.MyriadToolComponent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

import java.util.Optional;

public record ClothInstance(
        ResourceKey<ClothSkinData> cloth,
        Optional<Integer> clothColor,
        Optional<ResourceKey<ClothOverlayData>> overlay,
        Optional<Integer> overlayColor
) {
    public static final ClothInstance DEFAULT = new ClothInstance(
            ResourceKey.create(AntiqueRegistries.CLOTHS, Antiquities.id("cloth")),
            Optional.empty(),
            Optional.empty(),
            Optional.empty()
    );

    public static final Codec<ClothInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(AntiqueRegistries.CLOTHS).fieldOf("cloth").forGetter(ClothInstance::cloth),
            Codec.INT.optionalFieldOf("cloth_color").forGetter(ClothInstance::clothColor),
            ResourceKey.codec(AntiqueRegistries.CLOTH_OVERLAYS).optionalFieldOf("overlay").forGetter(ClothInstance::overlay),
            Codec.INT.optionalFieldOf("dye_color").forGetter(ClothInstance::overlayColor)
    ).apply(instance, ClothInstance::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClothInstance> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(AntiqueRegistries.CLOTHS), ClothInstance::cloth,
            ByteBufCodecs.optional(ByteBufCodecs.INT), ClothInstance::clothColor,
            ByteBufCodecs.optional(ResourceKey.streamCodec(AntiqueRegistries.CLOTH_OVERLAYS)), ClothInstance::overlay,
            ByteBufCodecs.optional(ByteBufCodecs.INT), ClothInstance::overlayColor,
            ClothInstance::new
    );

    public ClothInstance(ItemStack stack) {
        this(
                stack.getOrDefault(AntiqueDataComponentTypes.CLOTH_TYPE, ClothSkinData.DEFAULT_KEY),
                Optional.ofNullable(stack.get(DataComponents.DYED_COLOR)).map(DyedItemColor::rgb),
                Optional.empty(),
                Optional.empty()
        );
    }

    public ClothInstance withCloth(ResourceKey<ClothSkinData> cloth) {
        return new ClothInstance(cloth, clothColor, overlay, overlayColor);
    }

    public ClothInstance withClothColor(Optional<Integer> clothColor) {
        return new ClothInstance(cloth, clothColor, overlay, overlayColor);
    }

    public ClothInstance withOverlay(Optional<ResourceKey<ClothOverlayData>> overlay) {
        return new ClothInstance(cloth, clothColor, overlay, overlayColor);
    }

    public ClothInstance withOverlayColor(Optional<Integer> overlayColor) {
        return new ClothInstance(cloth, clothColor, overlay, overlayColor);
    }

    public ClothInstance withCloth(ItemStack stack) {
        ResourceKey<ClothSkinData> cloth = stack.getOrDefault(AntiqueDataComponentTypes.CLOTH_TYPE, ClothSkinData.DEFAULT_KEY);
        Optional<Integer> clothColor = Optional.ofNullable(stack.get(DataComponents.DYED_COLOR)).map(DyedItemColor::rgb);
        return new ClothInstance(cloth, clothColor, overlay, overlayColor);
    }

    public ClothInstance withOverlay(ItemStack stack) {
        Optional<ResourceKey<ClothOverlayData>> overlay = Optional.ofNullable(stack.get(AntiqueDataComponentTypes.CLOTH_OVERLAY_TYPE));
        Optional<Integer> overlayColor = Optional.ofNullable(stack.get(DataComponents.DYED_COLOR)).map(DyedItemColor::rgb);
        return new ClothInstance(cloth, clothColor, overlay, overlayColor);
    }

    public Optional<ClothInstance> exportCloth(ItemStack stack) {
        return overlay.map(overlay -> {
            stack.set(AntiqueDataComponentTypes.CLOTH_OVERLAY_TYPE, overlay);
            overlayColor.ifPresentOrElse(
                    rgb -> stack.set(DataComponents.DYED_COLOR, new DyedItemColor(rgb)),
                    () -> stack.remove(DataComponents.DYED_COLOR)
            );

            return new ClothInstance(cloth, clothColor, Optional.empty(), Optional.empty());
        }).or(() -> {
            stack.set(AntiqueDataComponentTypes.CLOTH_TYPE, cloth);
            clothColor.ifPresentOrElse(
                    rgb -> stack.set(DataComponents.DYED_COLOR, new DyedItemColor(rgb)),
                    () -> stack.remove(DataComponents.DYED_COLOR)
            );

            return Optional.empty();
        });
    }

    public ItemStack exportClothToTool(ItemStack toolStack, MyriadToolComponent tool) {
        return overlay.map(overlay -> {
            ItemStack stack = new ItemStack(AntiqueItems.CLOTH_PATTERN);

            stack.set(AntiqueDataComponentTypes.CLOTH_OVERLAY_TYPE, overlay);
            overlayColor.ifPresentOrElse(
                    rgb -> stack.set(DataComponents.DYED_COLOR, new DyedItemColor(rgb)),
                    () -> stack.remove(DataComponents.DYED_COLOR)
            );

            toolStack.set(AntiqueDataComponentTypes.MYRIAD_TOOL, tool.withCloth(Optional.of(new ClothInstance(cloth, clothColor, Optional.empty(), Optional.empty()))));

            return stack;
        }).orElseGet(() -> {
            ItemStack stack = new ItemStack(AntiqueItems.CLOTH);

            stack.set(AntiqueDataComponentTypes.CLOTH_TYPE, cloth);
            clothColor.ifPresentOrElse(
                    rgb -> stack.set(DataComponents.DYED_COLOR, new DyedItemColor(rgb)),
                    () -> stack.remove(DataComponents.DYED_COLOR)
            );

            toolStack.set(AntiqueDataComponentTypes.MYRIAD_TOOL, tool.withCloth(Optional.empty()));

            return stack;
        });
    }
}
