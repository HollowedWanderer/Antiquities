package net.hollowed.antique.util.properties;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ChargedProjectiles;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public record ProjectileTypeProperty() implements SelectItemModelProperty<Identifier> {
	public static final SelectItemModelProperty.Type<@NotNull ProjectileTypeProperty, Identifier> TYPE = SelectItemModelProperty.Type.create(
			MapCodec.unit(new ProjectileTypeProperty()), Identifier.CODEC
	);

	public Identifier get(
			ItemStack itemStack, @Nullable ClientLevel clientWorld, @Nullable LivingEntity livingEntity, int i, @NotNull ItemDisplayContext itemDisplayContext
	) {
		ChargedProjectiles chargedProjectilesComponent = itemStack.get(DataComponents.CHARGED_PROJECTILES);
		if (chargedProjectilesComponent == null || chargedProjectilesComponent.isEmpty()) {
			return Identifier.parse("none");
		} else {
			return BuiltInRegistries.ITEM.getKey(chargedProjectilesComponent.items().getFirst().item().value());
		}
	}

	@Override
	public @NotNull Type<@NotNull ProjectileTypeProperty, Identifier> type() {
		return TYPE;
	}

	@Override
	public @NotNull Codec<Identifier> valueCodec() {
		return Identifier.CODEC;
	}
}
