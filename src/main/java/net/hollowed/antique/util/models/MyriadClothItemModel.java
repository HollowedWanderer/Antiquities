package net.hollowed.antique.util.models;

import com.google.common.base.Suppliers;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hollowed.antique.Antiquities;
import net.hollowed.antique.index.AntiqueDataComponentTypes;
import net.hollowed.antique.index.AntiqueItemTags;
import net.hollowed.antique.items.components.MyriadToolComponent;
import net.hollowed.antique.util.resources.ClothSkinData;
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
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

@Environment(EnvType.CLIENT)
public class MyriadClothItemModel implements ItemModel {

	public static final FileToIdConverter ITEM_MODEL_LISTER = FileToIdConverter.json("models/tied_cloth");
	public static final Codec<Map<String, Identifier>> MODEL_CODEC = Codec.unboundedMap(
			Codec.STRING,
			Identifier.CODEC
	);

	private enum DisplayBucket {
		GUI_FIXED_GROUND,
		HAND
	}

	private record QuadKey(
			Identifier cloth,
			String variant
	) {
		public QuadKey(
				Identifier cloth,
				boolean large,
				DisplayBucket bucket
		) {
			this(cloth, (bucket == DisplayBucket.HAND ? "hand" : "item") + (large ? "_large" : ""));
		}
	}

	private final List<ItemTintSource> tints;

	private final List<BakedQuad> baseQuads;
	private final boolean baseAnimated;

	private final Supplier<Vector3fc[]> extents;
	private final ModelRenderProperties settings;

	private final Map<QuadKey, List<BakedQuad>> quads = new HashMap<>();
	private final Map<QuadKey, Boolean> animated = new HashMap<>();

	@SuppressWarnings("deprecation")
	public MyriadClothItemModel(
			List<ItemTintSource> tints,
			List<BakedQuad> baseQuads,
			ModelRenderProperties settings,
			Map<Identifier, Map<String, Identifier>> textures,
			ModelBaker baker
	) {
		this.tints = tints;

		this.baseQuads = baseQuads;
		this.baseAnimated = baseQuads.stream().anyMatch(quad -> quad.sprite().contents().isAnimated());

		this.settings = settings;
		this.extents = Suppliers.memoize(() -> {
			Set<Vector3fc> set = new HashSet<>();

			for (BakedQuad quad : baseQuads) {
				for (int i = 0; i < 4; ++i) {
					set.add(quad.position(i));
				}
			}

			return set.toArray(Vector3fc[]::new);
		});

		ItemModelGenerator generator = new ItemModelGenerator();

		textures.forEach((cloth, mapping) -> {
			mapping.forEach((variant, texture) -> {
				TextureSlots.Data slotData = new TextureSlots.Data.Builder()
						.addTexture("layer0", new Material(TextureAtlas.LOCATION_ITEMS, texture))
						.build();
				TextureSlots.Resolver resolver = new TextureSlots.Resolver();
				resolver.addFirst(slotData);
				TextureSlots slots = resolver.resolve(cloth::toString);
				QuadCollection quads = generator.geometry().bake(
						slots,
						baker,
						BlockModelRotation.IDENTITY,
						cloth::toString
				);
				this.quads.put(new QuadKey(cloth, variant), quads.getAll());
				this.animated.put(new QuadKey(cloth, variant), quads.getAll().stream().anyMatch(quad -> quad.sprite().contents().isAnimated()));
			});
		});
	}

	private static DisplayBucket bucketFrom(ItemDisplayContext ctx) {
		return (ctx == ItemDisplayContext.GROUND || ctx == ItemDisplayContext.FIXED || ctx == ItemDisplayContext.GUI)
				? DisplayBucket.GUI_FIXED_GROUND
				: DisplayBucket.HAND;
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
		state.appendModelIdentityElement(displayContext);

		MyriadToolComponent component = stack.getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, MyriadToolComponent.DEFAULT_NO_CLOTH);

		Identifier clothId = component.cloth().map(cloth -> cloth.cloth().identifier()).orElseGet(() -> Antiquities.id("empty"));

		state.appendModelIdentityElement(clothId);

		boolean large = component.toolBit().is(AntiqueItemTags.LARGE_CLOTH);
		state.appendModelIdentityElement(large);

		ItemStackRenderState.FoilType glint = null;
		if (stack.hasFoil()) {
			glint = shouldUseSpecialGlint(stack) ? ItemStackRenderState.FoilType.SPECIAL : ItemStackRenderState.FoilType.STANDARD;
			state.setAnimated();
			state.appendModelIdentityElement(glint);
		}

		DisplayBucket bucket = bucketFrom(displayContext);
		QuadKey key = new QuadKey(clothId, large, bucket);
		List<BakedQuad> selected = quads.get(key);

		boolean isFallback = false;

		if (selected == null || selected.isEmpty()) {
			key = new QuadKey(Antiquities.id("cloth"), large, bucket);
			selected = quads.get(key);
			isFallback = true;
			Antiquities.LOGGER.error("Couldn't get tied texture for cloth {}, large {}, bucket {}", clothId, large, bucket);
		}

		ItemStackRenderState.LayerRenderState baseLayer = state.newLayer();
		if (glint != null) baseLayer.setFoilType(glint);
		baseLayer.setExtents(this.extents);
		baseLayer.setRenderType(Sheets.translucentItemSheet());
		this.settings.applyToLayer(baseLayer, displayContext);
		baseLayer.prepareQuadList().addAll(this.baseQuads);

		ItemStackRenderState.LayerRenderState tintLayer = state.newLayer();
		if (glint != null) tintLayer.setFoilType(glint);
		tintLayer.setExtents(this.extents);
		tintLayer.setRenderType(Sheets.translucentItemSheet());
		this.settings.applyToLayer(tintLayer, displayContext);

		if (selected != null && !selected.isEmpty()) {
			List<BakedQuad> newQuads = getNewQuads(selected, component.cloth().flatMap(cloth -> Optional.ofNullable(level).map(level1 -> ClothSkinData.get(cloth.cloth(), level1).emissiveItem())).orElse(false) ? List.of(15) : List.of());
			tintLayer.prepareQuadList().addAll(newQuads);
		}

		if (level != null && ClothSkinData.get(Optional.of(clothId), level).dyeable()
				|| tintLayer.prepareQuadList().isEmpty()
				|| isFallback) {
			int n = this.tints.size();
			int[] t = tintLayer.prepareTintLayers(n);

			for (int i = 0; i < n; i++) {
				int c = this.tints.get(i).calculate(stack, level, heldItemContext == null ? null : heldItemContext.asLivingEntity());
				t[i] = c;
				state.appendModelIdentityElement(c);
			}
		}

		if (this.baseAnimated || this.animated.getOrDefault(key, false)) {
			state.setAnimated();
		}
	}

	private @NotNull List<BakedQuad> getNewQuads(List<BakedQuad> selected, List<Integer> emissions) {
		List<BakedQuad> newQuads = new ArrayList<>();
		Identifier spriteId = selected.getFirst().sprite().contents().name();
		int glowIndex = 0;

		for (BakedQuad quad : selected) {
			if (!(quad.sprite().contents().name().equals(spriteId))) {
				glowIndex++;
				spriteId = quad.sprite().contents().name();
			}

			newQuads.add(new BakedQuad(quad.position0(), quad.position1(), quad.position2(), quad.position3(), quad.packedUV0(), quad.packedUV1(), quad.packedUV2(), quad.packedUV3(), quad.tintIndex(), quad.direction(), quad.sprite(), quad.shade(), glowIndex >= emissions.size() ? quad.lightEmission() : emissions.get(glowIndex)));
		}

		return newQuads;
	}

	private static boolean shouldUseSpecialGlint(ItemStack stack) {
		return stack.is(ItemTags.COMPASSES) || stack.is(Items.CLOCK);
	}

	@Environment(EnvType.CLIENT)
	public record Unbaked(Identifier base, List<ItemTintSource> tints) implements ItemModel.Unbaked {
		public static final MapCodec<MyriadClothItemModel.Unbaked> CODEC = RecordCodecBuilder.mapCodec(
				instance -> instance.group(
						Identifier.CODEC.fieldOf("base").forGetter(MyriadClothItemModel.Unbaked::base),
						ItemTintSources.CODEC.listOf().optionalFieldOf("tints", List.of()).forGetter(MyriadClothItemModel.Unbaked::tints)
				).apply(instance, MyriadClothItemModel.Unbaked::new)
		);

		@Override
		public void resolveDependencies(ResolvableModel.Resolver resolver) {
			resolver.markDependency(this.base);
		}

		@Override
		public @NotNull ItemModel bake(ItemModel.BakingContext context) {
			ModelBaker baker = context.blockModelBaker();

			ResolvedModel baseBaked = baker.getModel(this.base);
			TextureSlots baseTex = baseBaked.getTopTextureSlots();
			ModelRenderProperties settings = ModelRenderProperties.fromResolvedModel(baker, baseBaked, baseTex);

			List<BakedQuad> baseQuads = baseBaked.bakeTopGeometry(baseTex, baker, BlockModelRotation.IDENTITY).getAll();
			Map<Identifier, Map<String, Identifier>> textures = new HashMap<>();

			ITEM_MODEL_LISTER.listMatchingResources(Minecraft.getInstance().getResourceManager()).forEach((file, resource) -> {
				Identifier id = ITEM_MODEL_LISTER.fileToId(file);

				try (BufferedReader reader = resource.openAsReader()) {
					Map<String, Identifier> map = new HashMap<>(MODEL_CODEC.decode(JsonOps.INSTANCE, JsonParser.parseReader(reader)).getOrThrow().getFirst());

					map.putIfAbsent("item", id.withPrefix("item/cloth/").withSuffix("_item"));
					map.putIfAbsent("item_large", map.get("item").withSuffix("_large"));
					map.putIfAbsent("hand", id.withPrefix("item/cloth/").withSuffix("_hand"));
					map.putIfAbsent("hand_large", map.get("hand").withSuffix("_large"));

					textures.put(id, map);
				} catch (IOException e) {
					Antiquities.LOGGER.error("Error loading tied cloth model {}", file, e);
				}
			});

			return new MyriadClothItemModel(this.tints, baseQuads, settings, textures, baker);
		}

		@Override
		public @NotNull MapCodec<MyriadClothItemModel.Unbaked> type() {
			return CODEC;
		}
	}
}