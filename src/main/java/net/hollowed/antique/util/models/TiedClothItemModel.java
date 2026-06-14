package net.hollowed.antique.util.models;

import com.google.common.base.Suppliers;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hollowed.antique.Antiquities;
import net.hollowed.antique.index.AntiqueDataComponentTypes;
import net.hollowed.antique.index.AntiqueItemTags;
import net.hollowed.antique.items.components.MyriadToolComponent;
import net.hollowed.antique.util.ClothUtil;
import net.hollowed.antique.util.resources.ClothSkinData;
import net.hollowed.antique.util.resources.client.ClothModelData;
import net.hollowed.antique.util.resources.client.ClothSprite;
import net.hollowed.antique.util.resources.client.TiedClothDomain;
import net.hollowed.antique.util.resources.client.TiedClothSize;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

@Environment(EnvType.CLIENT)
public class TiedClothItemModel implements ItemModel {

	public static final FileToIdConverter ITEM_MODEL_LISTER = FileToIdConverter.json("models/tied_cloth");
	public static final Codec<Map<String, Identifier>> MODEL_CODEC = Codec.unboundedMap(
			Codec.STRING,
			Identifier.CODEC
	);

	public record ClothKey(
			Identifier cloth,
			TiedClothSize size,
			TiedClothDomain domain
	) {
	}

	private final List<BakedQuad> baseQuads;
	private final Map<ClothKey, List<ClothSprite>> sprites;
	private final Map<ClothKey, List<BakedQuad>> quads;
	private final Supplier<Vector3fc[]> extents;
	private final ModelRenderProperties settings;
	private final boolean animated;
	private final List<ItemTintSource> tints;

	public TiedClothItemModel(
			List<BakedQuad> baseQuads,
			Map<ClothKey, List<ClothSprite>> sprites,
			Map<ClothKey, List<BakedQuad>> quads,
			ModelRenderProperties settings,
			List<ItemTintSource> tints
	) {
		this.baseQuads = baseQuads;
		this.sprites = sprites;
		this.quads = quads;
		this.extents = Suppliers.memoize(() -> {
			Set<Vector3fc> set = new HashSet<>();

			for (BakedQuad quad : baseQuads) {
				for (int i = 0; i < 4; ++i) {
					set.add(quad.position(i));
				}
			}

			return set.toArray(Vector3fc[]::new);
		});
		this.settings = settings;
		this.animated = Stream.concat(baseQuads.stream(), quads.values().stream().flatMap(List::stream)).anyMatch(quad -> quad.sprite().contents().isAnimated());
		this.tints = tints;
	}

	@Override
	public void update(
			ItemStackRenderState state,
			ItemStack stack,
			@NotNull ItemModelResolver resolver,
			@NotNull ItemDisplayContext context,
			@Nullable ClientLevel level,
			@Nullable ItemOwner heldItemContext,
			int seed
	) {
		state.appendModelIdentityElement(this);
		state.appendModelIdentityElement(context);

		MyriadToolComponent component = stack.getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, MyriadToolComponent.DEFAULT_NO_CLOTH);

		Identifier clothId = component.cloth()
				.flatMap(ClothUtil::getCloth)
				.map(ResourceKey::identifier)
				.orElseGet(() -> Antiquities.id("empty"));

		state.appendModelIdentityElement(clothId);

		boolean large = component.toolBit().is(AntiqueItemTags.LARGE_CLOTH);
		state.appendModelIdentityElement(large);

		ItemStackRenderState.FoilType glint = null;
		if (stack.hasFoil()) {
			glint = ItemStackRenderState.FoilType.STANDARD;
			state.setAnimated();
			state.appendModelIdentityElement(glint);
		}

		ClothKey key = new ClothKey(
				clothId,
				large ? TiedClothSize.LARGE : TiedClothSize.NORMAL,
				(context == ItemDisplayContext.GROUND || context == ItemDisplayContext.FIXED || context == ItemDisplayContext.GUI) ? TiedClothDomain.INVENTORY : TiedClothDomain.HAND
		);
		List<BakedQuad> selected = quads.get(key);

		if (selected == null || selected.isEmpty()) {
			key = new ClothKey(Antiquities.id("cloth"), key.size, key.domain);
			selected = quads.get(key);
		}

		ItemStackRenderState.LayerRenderState baseLayer = state.newLayer();
		if (glint != null) baseLayer.setFoilType(glint);
		baseLayer.setExtents(this.extents);
		baseLayer.setRenderType(Sheets.translucentItemSheet());
		this.settings.applyToLayer(baseLayer, context);
		baseLayer.prepareQuadList().addAll(this.baseQuads);

		ItemStackRenderState.LayerRenderState tintLayer = state.newLayer();
		if (glint != null) tintLayer.setFoilType(glint);
		tintLayer.setExtents(this.extents);
		tintLayer.setRenderType(Sheets.translucentItemSheet());
		this.settings.applyToLayer(tintLayer, context);

		if (selected != null) {
			tintLayer.prepareQuadList().addAll(selected);
		}

		int[] tints = tintLayer.prepareTintLayers(this.tints.size());

		if (level != null && ClothSkinData.get(Optional.of(clothId), level).dyeable()) {
			for (int i = 0; i < this.tints.size(); i++) {
				int c = this.tints.get(i).calculate(stack, level, heldItemContext == null ? null : heldItemContext.asLivingEntity());
				tints[i] = c;
				state.appendModelIdentityElement(c);
			}
		} else {
			List<ClothSprite> sprites = this.sprites.get(key);

			if (sprites != null) {
				for (int i = 0; i < this.tints.size(); i++) {
					if (i < sprites.size()) {
						if (!sprites.get(i).tint()) {
							tints[i] = 0xFFFFFFFF;
							state.appendModelIdentityElement(0xFFFFFFFF);
							continue;
						}
					}

					int c = this.tints.get(i).calculate(stack, level, heldItemContext == null ? null : heldItemContext.asLivingEntity());
					tints[i] = c;
					state.appendModelIdentityElement(c);
				}
			}
		}

		if (this.animated) {
			state.setAnimated();
		}
	}

	@Environment(EnvType.CLIENT)
	public record Unbaked(Identifier base, List<ItemTintSource> tints) implements ItemModel.Unbaked {
		public static final MapCodec<TiedClothItemModel.Unbaked> CODEC = RecordCodecBuilder.mapCodec(
				instance -> instance.group(
						Identifier.CODEC.fieldOf("base").forGetter(TiedClothItemModel.Unbaked::base),
						ItemTintSources.CODEC.listOf().optionalFieldOf("tints", List.of()).forGetter(TiedClothItemModel.Unbaked::tints)
				).apply(instance, TiedClothItemModel.Unbaked::new)
		);

		@Override
		public void resolveDependencies(ResolvableModel.Resolver resolver) {
			resolver.markDependency(this.base);
		}

		@Override
		@SuppressWarnings("deprecation")
		public @NotNull ItemModel bake(ItemModel.BakingContext context) {
			ModelBaker baker = context.blockModelBaker();

			ResolvedModel baseBaked = baker.getModel(this.base);
			TextureSlots baseTex = baseBaked.getTopTextureSlots();
			ModelRenderProperties settings = ModelRenderProperties.fromResolvedModel(baker, baseBaked, baseTex);

			List<BakedQuad> baseQuads = baseBaked.bakeTopGeometry(baseTex, baker, BlockModelRotation.IDENTITY).getAll();
			Map<ClothKey, List<ClothSprite>> variantSprites = new HashMap<>();
			Map<ClothKey, List<BakedQuad>> variantQuads = new HashMap<>();

			ItemModelGenerator generator = new ItemModelGenerator();

			ClothModelData.FILE_LISTER.listMatchingResources(Minecraft.getInstance().getResourceManager()).forEach((file, resource) -> {
				Identifier id = ClothModelData.FILE_LISTER.fileToId(file);

				try (BufferedReader reader = resource.openAsReader()) {
					JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
					ClothModelData.CODEC.decode(JsonOps.INSTANCE, json)
							.ifSuccess(result -> {
								ClothModelData model = result.getFirst().fillDefaultSprites(id);

								for (TiedClothSize size : TiedClothSize.values()) {
									var domainMap = model.tiedSprites().getOrDefault(size, Map.of());

									for (TiedClothDomain domain : TiedClothDomain.values()) {
										List<ClothSprite> sprites = domainMap.get(domain);

										if (sprites == null || sprites.isEmpty()) {
											sprites = List.of(new ClothSprite(domain.defaultSprite(size), Optional.empty(), true));
										}

										TextureSlots.Data.Builder builder = new TextureSlots.Data.Builder();
										int layer = 0;

										for (ClothSprite sprite : sprites) {
											builder.addTexture("layer" + (layer++), new Material(TextureAtlas.LOCATION_ITEMS, sprite.texture()));
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
										ClothKey key = new ClothKey(id, size, domain);
										variantSprites.put(key, sprites);
										variantQuads.put(key, ClothSprite.applyToQuads(quads, sprites));
									}
								}
							})
							.ifError(error -> Antiquities.LOGGER.error("Error loading cloth model {}: {}", file, error.message()));
				} catch (IOException e) {
					Antiquities.LOGGER.error("Error loading tied cloth model {}", file, e);
				}
			});

			return new TiedClothItemModel(baseQuads, variantSprites, variantQuads, settings, tints);
		}

		@Override
		public @NotNull MapCodec<TiedClothItemModel.Unbaked> type() {
			return CODEC;
		}
	}
}