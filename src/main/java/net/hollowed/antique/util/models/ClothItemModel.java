package net.hollowed.antique.util.models;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hollowed.antique.Antiquities;
import net.hollowed.antique.index.AntiqueDataComponentTypes;
import net.hollowed.antique.util.resources.ClothSkinData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

import java.util.*;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class ClothItemModel implements ItemModel {

	public static final FileToIdConverter ROOT_MODEL_LISTER = FileToIdConverter.json("models");
	public static final FileToIdConverter ITEM_MODEL_LISTER = FileToIdConverter.json("models/cloth");

	private final Map<Identifier, List<BakedQuad>> quads;
	private final Supplier<Vector3fc[]> extents;
	private final ModelRenderProperties settings;
	private final boolean animated;
	private final List<ItemTintSource> tints;

	public ClothItemModel(Map<Identifier, List<BakedQuad>> quads, ModelRenderProperties settings, List<ItemTintSource> tints) {
		this.quads = quads;
		this.settings = settings;
		this.extents = Suppliers.memoize(() -> {
			Set<Vector3fc> extents = new HashSet<>();

			for (List<BakedQuad> list : this.quads.values()) {
				for (BakedQuad quad : list) {
					for (int i = 0; i < 4; ++i) {
						extents.add(quad.position(i));
					}
				}
			}

			return extents.toArray(Vector3fc[]::new);
		});
		this.animated = quads.values().stream().anyMatch(list -> list.stream().anyMatch(quad -> quad.sprite().contents().isAnimated()));
		this.tints = tints;
	}

	@Override
	public void update(
			ItemStackRenderState state,
			ItemStack stack,
			@NotNull ItemModelResolver resolver,
			@NotNull ItemDisplayContext displayContext,
			@Nullable ClientLevel level,
			@Nullable ItemOwner heldItemContext,
			int seed
	) {
		state.appendModelIdentityElement(this);
		ItemStackRenderState.LayerRenderState layer = state.newLayer();

		if (stack.hasFoil()) {
			ItemStackRenderState.FoilType glint = ItemStackRenderState.FoilType.STANDARD;
			layer.setFoilType(glint);
			state.setAnimated();
			state.appendModelIdentityElement(glint);
		}

		Optional<ResourceKey<ClothSkinData>> cloth = Optional.ofNullable(stack.get(AntiqueDataComponentTypes.CLOTH_TYPE));
		Identifier clothId = cloth
				.map(ResourceKey::identifier)
				.orElse(Antiquities.id("cloth"));
		state.appendModelIdentityElement(clothId);

		List<BakedQuad> selected = quads.computeIfAbsent(clothId, key -> {
			Antiquities.LOGGER.error("Couldn't get item model for cloth {}", clothId);
			return quads.get(Antiquities.id("cloth"));
		});

		layer.setExtents(this.extents);
		layer.setRenderType(Sheets.translucentItemSheet());
		layer.setUsesBlockLight(false);
		this.settings.applyToLayer(layer, displayContext);
		layer.prepareQuadList().addAll(selected);

		if (level != null && ClothSkinData.getHolderFromKey(cloth, level).map(skin -> skin.value().dyeable()).orElse(false)) {
			int[] tintLayers = layer.prepareTintLayers(this.tints.size());

			for (int i = 0; i < this.tints.size(); i++) {
				int tint = this.tints.get(i).calculate(stack, level, heldItemContext == null ? null : heldItemContext.asLivingEntity());
				tintLayers[i] = tint;
				state.appendModelIdentityElement(tint);
			}
		}

		if (this.animated) {
			state.setAnimated();
		}
	}

	@Environment(EnvType.CLIENT)
	public record Unbaked(List<ItemTintSource> tints) implements ItemModel.Unbaked {
		public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(
				instance -> instance.group(
						ItemTintSources.CODEC.listOf().optionalFieldOf("tints", List.of()).forGetter(Unbaked::tints)
				).apply(instance, Unbaked::new)
		);

		@Override
		public void resolveDependencies(Resolver resolver) {
			resolver.markDependency(Antiquities.id("cloth/cloth"));

			ITEM_MODEL_LISTER.listMatchingResources(Minecraft.getInstance().getResourceManager())
					.keySet()
					.forEach(id -> resolver.markDependency(ROOT_MODEL_LISTER.fileToId(id)));
		}

		@Override
		public @NotNull ItemModel bake(BakingContext context) {
			ModelBaker baker = context.blockModelBaker();
			Map<Identifier, List<BakedQuad>> variantQuads = new HashMap<>();

			ResolvedModel baseBaked = baker.getModel(Antiquities.id("cloth/cloth"));
			TextureSlots baseTex = baseBaked.getTopTextureSlots();
			ModelRenderProperties settings = ModelRenderProperties.fromResolvedModel(baker, baseBaked, baseTex);

			ITEM_MODEL_LISTER.listMatchingResources(Minecraft.getInstance().getResourceManager()).keySet().forEach(file -> {
				ResolvedModel model = baker.getModel(ROOT_MODEL_LISTER.fileToId(file));
				TextureSlots textures = model.getTopTextureSlots();
				variantQuads.computeIfAbsent(ITEM_MODEL_LISTER.fileToId(file), key -> new ArrayList<>()).addAll(model.bakeTopGeometry(textures, baker, BlockModelRotation.IDENTITY).getAll());
			});

			return new ClothItemModel(variantQuads, settings, tints);
		}

		@Override
		public @NotNull MapCodec<Unbaked> type() {
			return CODEC;
		}
	}
}
