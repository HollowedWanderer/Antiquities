package net.hollowed.antique.util.models;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hollowed.antique.Antiquities;
import net.hollowed.antique.index.AntiqueDataComponentTypes;
import net.hollowed.antique.util.resources.ClothPatternData;
import net.hollowed.antique.util.resources.ClothSkinData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class ClothPatternItemModel implements ItemModel {

	public static final FileToIdConverter ROOT_MODEL_LISTER = FileToIdConverter.json("models");
	public static final FileToIdConverter ITEM_MODEL_LISTER = FileToIdConverter.json("models/cloth_pattern");

	private final Map<Identifier, List<BakedQuad>> quads;
	private final Supplier<Vector3fc[]> extents;
	private final ModelRenderProperties settings;
	private final boolean animated;

	public ClothPatternItemModel(Map<Identifier, List<BakedQuad>> quads, ModelRenderProperties settings) {
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

		Optional<ResourceKey<ClothPatternData>> pattern = Optional.ofNullable(stack.get(AntiqueDataComponentTypes.CLOTH_PATTERN_TYPE));
		Identifier patternId = pattern
				.map(ResourceKey::identifier)
				.orElse(Antiquities.id("cloth_pattern"));
		state.appendModelIdentityElement(patternId);

		List<BakedQuad> selected = quads.computeIfAbsent(patternId, key -> {
			Antiquities.LOGGER.error("Couldn't get item model for cloth pattern {}", patternId);
			return quads.get(Antiquities.id("cloth_pattern"));
		});

		layer.setExtents(this.extents);
		layer.setRenderType(Sheets.translucentItemSheet());
		layer.setUsesBlockLight(false);
		this.settings.applyToLayer(layer, displayContext);
		layer.prepareQuadList().addAll(selected);

		if (this.animated) {
			state.setAnimated();
		}
	}

	@Environment(EnvType.CLIENT)
	public record Unbaked() implements ItemModel.Unbaked {
		public static final MapCodec<ClothPatternItemModel.Unbaked> CODEC = MapCodec.unit(new ClothPatternItemModel.Unbaked());

		@Override
		public void resolveDependencies(Resolver resolver) {
			resolver.markDependency(Antiquities.id("cloth_pattern/cloth_pattern"));

			ITEM_MODEL_LISTER.listMatchingResources(Minecraft.getInstance().getResourceManager())
					.keySet()
					.forEach(file -> resolver.markDependency(ROOT_MODEL_LISTER.fileToId(file)));
		}

		@Override
		public @NotNull ItemModel bake(BakingContext context) {
			ModelBaker baker = context.blockModelBaker();
			Map<Identifier, List<BakedQuad>> variantQuads = new HashMap<>();

			ResolvedModel baseBaked = baker.getModel(Antiquities.id("cloth_pattern/cloth_pattern"));
			TextureSlots baseTex = baseBaked.getTopTextureSlots();
			ModelRenderProperties settings = ModelRenderProperties.fromResolvedModel(baker, baseBaked, baseTex);

			ITEM_MODEL_LISTER.listMatchingResources(Minecraft.getInstance().getResourceManager()).keySet().forEach(file -> {
				ResolvedModel model = baker.getModel(ROOT_MODEL_LISTER.fileToId(file));
				TextureSlots textures = model.getTopTextureSlots();
				variantQuads.computeIfAbsent(ITEM_MODEL_LISTER.fileToId(file), key -> new ArrayList<>()).addAll(model.bakeTopGeometry(textures, baker, BlockModelRotation.IDENTITY).getAll());
			});

			return new ClothPatternItemModel(variantQuads, settings);
		}

		@Override
		public @NotNull MapCodec<ClothPatternItemModel.Unbaked> type() {
			return CODEC;
		}
	}
}
