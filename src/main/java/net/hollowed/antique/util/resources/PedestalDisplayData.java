package net.hollowed.antique.util.resources;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.item.ItemDisplayContext;

public record PedestalDisplayData(String item, List<Float> translations, List<Float> rotations, List<Float> scale, ItemDisplayContext displayContext) {
    public static final Codec<PedestalDisplayData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("item").forGetter(PedestalDisplayData::item),
            Codec.FLOAT.listOf().fieldOf("translation").orElseGet(() -> List.of(0.0F, 0.0F, 0.0F)).forGetter(PedestalDisplayData::translations),
            Codec.FLOAT.listOf().fieldOf("rotation").orElseGet(() -> List.of(0.0F, 0.0F, 0.0F)).forGetter(PedestalDisplayData::rotations),
            Codec.FLOAT.listOf().fieldOf("scale").orElseGet(() -> List.of(1.0F, 1.0F, 1.0F)).forGetter(PedestalDisplayData::scale),
            ItemDisplayContext.CODEC.fieldOf("mode").orElseGet(() -> ItemDisplayContext.FIXED).forGetter(PedestalDisplayData::displayContext)
    ).apply(instance, PedestalDisplayData::new));
}
