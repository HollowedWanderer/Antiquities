package net.hollowed.antique.util.models;

import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hollowed.antique.index.AntiqueDataComponentTypes;
import net.hollowed.antique.items.SatchelItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fc;
import org.jspecify.annotations.NonNull;

import java.util.List;

@Environment(EnvType.CLIENT)
public class SatchelSelectedItemModel implements ItemModel {
    static final ItemModel INSTANCE = new SatchelSelectedItemModel();

    public SatchelSelectedItemModel() {}

    public void update(ItemStackRenderState state, ItemStack stack, @NotNull ItemModelResolver resolver, @NotNull ItemDisplayContext displayContext, @Nullable ClientLevel world, @Nullable ItemOwner heldItemContext, int seed) {
        state.appendModelIdentityElement(this);
        List<ItemStack> list = stack.getOrDefault(AntiqueDataComponentTypes.SATCHEL_STACK, List.of(ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY));
        if (SatchelItem.getInternalIndex(stack) >= 0 && !list.isEmpty() && SatchelItem.getInternalIndex(stack) < list.size()) {
            ItemStack itemStack = list.get(SatchelItem.getInternalIndex(stack));
            if (!itemStack.isEmpty()) {
                resolver.appendItemLayers(state, itemStack, displayContext, world, heldItemContext, seed);
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public record Unbaked() implements ItemModel.Unbaked {
        public static final MapCodec<net.hollowed.antique.util.models.SatchelSelectedItemModel.Unbaked> CODEC = MapCodec.unit(new net.hollowed.antique.util.models.SatchelSelectedItemModel.Unbaked());

        public @NotNull MapCodec<net.hollowed.antique.util.models.SatchelSelectedItemModel.Unbaked> type() {
            return CODEC;
        }

        @Override
        public @NonNull ItemModel bake(@NonNull BakingContext context, @NonNull Matrix4fc transformation) {
            return SatchelSelectedItemModel.INSTANCE;
        }

        public void resolveDependencies(ResolvableModel.@NotNull Resolver resolver) {
        }
    }
}