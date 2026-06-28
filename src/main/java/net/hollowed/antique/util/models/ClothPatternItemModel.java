package net.hollowed.antique.util.models;

import com.google.common.base.Suppliers;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hollowed.antique.Antiquities;
import net.hollowed.antique.index.AntiqueDataComponentTypes;
import net.hollowed.antique.util.resources.ClothPatternData;
import net.hollowed.antique.util.resources.client.ClothPatternModelData;
import net.hollowed.antique.util.resources.client.ClothSprite;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.cuboid.ItemModelGenerator;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.jspecify.annotations.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class ClothPatternItemModel implements ItemModel {
	private final Map<Identifier, List<BakedQuad>> quads;
	private final Supplier<Vector3fc[]> extents;
	private final ModelRenderProperties settings;
	private final boolean animated;

	public ClothPatternItemModel(Map<Identifier, List<BakedQuad>> quads, ModelRenderProperties settings) {
		this.quads = quads;
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
		this.settings = settings;
		this.animated = quads.values().stream().anyMatch(list -> list.stream().anyMatch(quad -> quad.materialInfo().sprite().contents().isAnimated()));
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

		Optional<ResourceKey<ClothPatternData>> cloth = Optional.ofNullable(stack.get(AntiqueDataComponentTypes.CLOTH_PATTERN_TYPE));
		Identifier clothId = cloth
				.map(ResourceKey::identifier)
				.orElse(Antiquities.id("cloth"));
		state.appendModelIdentityElement(clothId);

		List<BakedQuad> selected = quads.computeIfAbsent(clothId, _ -> {
			Antiquities.LOGGER.error("Couldn't get item model for cloth pattern {}", clothId);
			return quads.get(Antiquities.id("cloth"));
		});

		layer.setExtents(this.extents);
		layer.setUsesBlockLight(false);
		this.settings.applyToLayer(layer, displayContext);

		if (selected != null) {
			layer.prepareQuadList().addAll(selected);
		}

		if (this.animated) {
			state.setAnimated();
		}
	}

	@Environment(EnvType.CLIENT)
	public record Unbaked() implements ItemModel.Unbaked {
		public static final MapCodec<ClothPatternItemModel.Unbaked> CODEC = MapCodec.unit(new ClothPatternItemModel.Unbaked());

		@Override
		public void resolveDependencies(Resolver resolver) {
			resolver.markDependency(Antiquities.id("item/cloth_pattern"));
		}

		@Override
		public @NonNull ItemModel bake(BakingContext context, @NonNull Matrix4fc transformation) {
			ModelBaker baker = context.blockModelBaker();
			Map<Identifier, List<BakedQuad>> variantQuads = new HashMap<>();

			ResolvedModel baseBaked = baker.getModel(Antiquities.id("item/cloth_pattern"));
			TextureSlots baseTex = baseBaked.getTopTextureSlots();
			ModelRenderProperties settings = ModelRenderProperties.fromResolvedModel(baker, baseBaked, baseTex);

			ItemModelGenerator generator = new ItemModelGenerator();

			ClothPatternModelData.FILE_LISTER.listMatchingResources(Minecraft.getInstance().getResourceManager()).forEach((file, resource) -> {
				Identifier id = ClothPatternModelData.FILE_LISTER.fileToId(file);

				try (BufferedReader reader = resource.openAsReader()) {
					JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
					ClothPatternModelData.CODEC.decode(JsonOps.INSTANCE, json)
							.ifSuccess(result -> {
								ClothPatternModelData model = result.getFirst().fillDefaultSprites(id);

								TextureSlots.Data.Builder builder = new TextureSlots.Data.Builder();
								int layer = 0;

								for (ClothSprite sprite : model.itemSprites()) {
									builder.addTexture("layer" + (layer++), new Material(sprite.texture()));
								}

								TextureSlots.Resolver resolver = new TextureSlots.Resolver();
								resolver.addFirst(builder.build());
								TextureSlots slots = resolver.resolve(file::toString);
								List<BakedQuad> quads = generator.geometry().bake(
										slots,
										baker,
										BlockModelRotation.IDENTITY,
										file::toString
								).getAll();
								variantQuads.put(id, ClothSprite.applyToQuads(quads, model.itemSprites()));
							})
							.ifError(error -> Antiquities.LOGGER.error("Error loading cloth model {}: {}", file, error.message()));
				} catch (IOException e) {
					Antiquities.LOGGER.error("Error loading cloth item model {}", file, e);
				}
			});

			return new ClothPatternItemModel(variantQuads, settings);
		}

		@Override
		public @NotNull MapCodec<Unbaked> type() {
			return CODEC;
		}
	}
}
