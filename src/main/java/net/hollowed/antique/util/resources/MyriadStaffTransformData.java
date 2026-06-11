package net.hollowed.antique.util.resources;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public record MyriadStaffTransformData(
        TagOrSingle<Item> model,
        List<Float> scale,
        List<Float> rotation,
        List<Float> translation
) {
    public static final Identifier DEFAULT_MODEL = Identifier.withDefaultNamespace("default");
    public static final Codec<MyriadStaffTransformData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TagOrSingle.codec(Registries.ITEM).fieldOf("item").orElseGet(() -> new TagOrSingle.Single<>(DEFAULT_MODEL)).forGetter(MyriadStaffTransformData::model),
            Codec.FLOAT.listOf().fieldOf("scale").orElseGet(() -> List.of(1.0f, 1.0f, 1.0f)).forGetter(MyriadStaffTransformData::scale),
            Codec.FLOAT.listOf().fieldOf("rotation").orElseGet(() -> List.of(0.0f, 0.0f, 0.0f)).forGetter(MyriadStaffTransformData::rotation),
            Codec.FLOAT.listOf().fieldOf("translation").orElseGet(() -> List.of(0.0f, 0.0f, 0.0f)).forGetter(MyriadStaffTransformData::translation)
    ).apply(instance, MyriadStaffTransformData::new));
}
