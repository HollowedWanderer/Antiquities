package net.hollowed.antique.index;

import net.hollowed.antique.blocks.screens.DyeingScreenHandler;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.NotNull;

public class AntiqueScreenHandlerType implements FeatureElement {
	public static final MenuType<@NotNull DyeingScreenHandler> DYE_TABLE = register("dye_table", DyeingScreenHandler::new);

	@SuppressWarnings("all")
	private static <T extends AbstractContainerMenu> MenuType<T> register(String id, MenuType.MenuSupplier<T> factory) {
		return Registry.register(BuiltInRegistries.MENU, id, new MenuType<>(factory, FeatureFlags.VANILLA_SET));
	}

	@SuppressWarnings("all")
	@Override
	public FeatureFlagSet requiredFeatures() {
		return null;
	}

	public static void initialize() {}
}
