package net.hollowed.antique.util.properties;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.hollowed.antique.index.AntiqueDataComponentTypes;
import net.hollowed.antique.items.components.MyriadToolComponent;
import net.hollowed.antique.util.resources.ClothInstance;
import net.hollowed.antique.util.resources.ClothSkinData;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record ClothTintSource(int defaultColor) implements ItemTintSource {
	public static final MapCodec<ClothTintSource> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(ExtraCodecs.RGB_COLOR_CODEC.fieldOf("default").forGetter(ClothTintSource::defaultColor)).apply(instance, ClothTintSource::new)
	);

	@SuppressWarnings("unused")
	public ClothTintSource() {
		this(-13083194);
	}

	@Override
	public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity user) {
		MyriadToolComponent component = stack.get(AntiqueDataComponentTypes.MYRIAD_TOOL);
		return component != null
				? ARGB.opaque(component.cloth()
				.flatMap(cloth ->
						cloth.clothColor().or(() -> {
							if (level == null) {
								return Optional.empty();
							} else {
								return Optional.of(ClothSkinData.get(cloth.cloth(), level).color().getColorClient());
							}
						})
				)
				.orElse(0xFFFFFFFF))
				: ARGB.opaque(this.defaultColor);
	}

	@Override
	public @NotNull MapCodec<ClothTintSource> type() {
		return CODEC;
	}
}