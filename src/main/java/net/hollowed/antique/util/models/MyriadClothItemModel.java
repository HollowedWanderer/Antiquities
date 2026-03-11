package net.hollowed.antique.util.models;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hollowed.antique.Antiquities;
import net.hollowed.antique.index.AntiqueDataComponentTypes;
import net.hollowed.antique.index.AntiqueItemTags;
import net.hollowed.antique.items.components.MyriadToolComponent;
import net.hollowed.antique.util.resources.ClientClothData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.color.item.ItemTintSources;
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
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

@Environment(EnvType.CLIENT)
public class MyriadClothItemModel implements ItemModel {

	private static final Function<ItemStack, RenderType> ITEM_RENDER_TYPE_GETTER = (itemStack) -> Sheets.translucentItemSheet();
	private static final Function<ItemStack, RenderType> BLOCK_RENDER_TYPE_GETTER = (itemStack) -> {
		Item item = itemStack.getItem();
		if (item instanceof BlockItem blockItem) {
			ChunkSectionLayer chunkSectionLayer = ItemBlockRenderTypes.getChunkRenderType(blockItem.getBlock().defaultBlockState());
			if (chunkSectionLayer != ChunkSectionLayer.TRANSLUCENT) {
				return Sheets.cutoutBlockSheet();
			}
		}

		return Sheets.translucentBlockItemSheet();
	};

	private enum DisplayBucket { GUI_FIXED_GROUND, HAND }
	private record QuadKey(Identifier variant, boolean large, DisplayBucket bucket) {}

	private static final ArrayList<String> models = new ArrayList<>();
	private final List<ItemTintSource> tints;
	private final List<BakedQuad> baseQuads;
	private final Map<QuadKey, BakedQuad[]> quadIndex;
	private final Supplier<Vector3fc[]> vertices;
	private final ModelRenderProperties settings;
	private final boolean animated;
	private final Function<ItemStack, RenderType> renderType;

	public MyriadClothItemModel(
			List<ItemTintSource> tints,
			List<BakedQuad> baseQuads,
			List<BakedQuad> variantQuads,
			ModelRenderProperties settings,
			Function<ItemStack, RenderType> function
	) {
		this.tints = tints;
		this.baseQuads = baseQuads;
		this.settings = settings;
		this.quadIndex = buildQuadIndex(variantQuads);
		this.renderType = function;
		List<BakedQuad> all = new ArrayList<>(baseQuads.size() + variantQuads.size());
		all.addAll(baseQuads);
		all.addAll(variantQuads);
		this.vertices = Suppliers.memoize(() -> computeExtents(all));
		boolean anyAnimated = false;
		for (BakedQuad q : all) {
			if (q.sprite().contents().isAnimated()) {
				anyAnimated = true;
				break;
			}
		}
		this.animated = anyAnimated;
	}

	private static Map<QuadKey, BakedQuad[]> buildQuadIndex(List<BakedQuad> quads) {
		Map<QuadKey, List<BakedQuad>> temp = new HashMap<>(64);

		for (BakedQuad quad : quads) {
			Identifier id = quad.sprite().contents().name();
			String path = id.getPath();

			boolean large = path.contains("large");
			DisplayBucket bucket = path.contains("_hand") ? DisplayBucket.HAND : DisplayBucket.GUI_FIXED_GROUND;

			String variantPath = extractVariantName(path);
			Identifier variantId = Identifier.fromNamespaceAndPath(id.getNamespace(), variantPath);

			QuadKey key = new QuadKey(variantId, large, bucket);
			temp.computeIfAbsent(key, k -> new ArrayList<>(8)).add(quad);
		}

		Map<QuadKey, BakedQuad[]> out = new HashMap<>(temp.size());
		for (Map.Entry<QuadKey, List<BakedQuad>> e : temp.entrySet()) {
			out.put(e.getKey(), e.getValue().toArray(BakedQuad[]::new));
		}
		return out;
	}

	private static String extractVariantName(String path) {
		int lastSlash = path.lastIndexOf('/');
		String name = lastSlash >= 0 ? path.substring(lastSlash + 1) : path;

		name = name.replace("_hand", "")
				.replace("_item", "")
				.replace("_large", "");
		return name.isEmpty() ? "cloth" : name;
	}

	public static Vector3fc[] computeExtents(List<BakedQuad> list) {
		Set<Vector3fc> set = new HashSet<>();

		for(BakedQuad bakedQuad : list) {
			for(int i = 0; i < 4; ++i) {
				set.add(bakedQuad.position(i));
			}
		}

		return set.toArray(Vector3fc[]::new);
	}

	@SuppressWarnings("all")
	static Function<ItemStack, RenderType> detectRenderType(List<BakedQuad> list) {
		Iterator<BakedQuad> iterator = list.iterator();
		if (!iterator.hasNext()) {
			return ITEM_RENDER_TYPE_GETTER;
		} else {
			Identifier identifier = iterator.next().sprite().atlasLocation();

			while(iterator.hasNext()) {
				BakedQuad bakedQuad = iterator.next();
				Identifier identifier2 = bakedQuad.sprite().atlasLocation();
				if (!identifier2.equals(identifier)) {
					String var10002 = String.valueOf(identifier);
					throw new IllegalStateException("Multiple atlases used in model, expected " + var10002 + ", but also got " + String.valueOf(identifier2));
				}
			}

			if (identifier.equals(TextureAtlas.LOCATION_ITEMS)) {
				return ITEM_RENDER_TYPE_GETTER;
			} else if (identifier.equals(TextureAtlas.LOCATION_BLOCKS)) {
				return BLOCK_RENDER_TYPE_GETTER;
			} else {
				throw new IllegalArgumentException("Atlas " + identifier + " can't be usef for item models");
			}
		}
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
			@Nullable ClientLevel world,
			@Nullable ItemOwner heldItemContext,
			int seed
	) {
		state.appendModelIdentityElement(this);
		state.appendModelIdentityElement(displayContext);

		MyriadToolComponent component = stack.getOrDefault(AntiqueDataComponentTypes.MYRIAD_TOOL, Antiquities.getDefaultMyriadTool());

		Identifier modelVariantId = Identifier.tryParse(component.clothType());
		if (modelVariantId == null) return;
		if (modelVariantId.toString().equals("minecraft:")) modelVariantId = Antiquities.id("empty");
		state.appendModelIdentityElement(modelVariantId);

		boolean large = component.toolBit().is(AntiqueItemTags.LARGE_CLOTH);
		state.appendModelIdentityElement(large);

		ItemStackRenderState.FoilType glint = null;
		if (stack.hasFoil()) {
			glint = shouldUseSpecialGlint(stack) ? ItemStackRenderState.FoilType.SPECIAL : ItemStackRenderState.FoilType.STANDARD;
			state.setAnimated();
			state.appendModelIdentityElement(glint);
		}

		DisplayBucket bucket = bucketFrom(displayContext);
		BakedQuad[] selected = quadIndex.get(new QuadKey(modelVariantId, large, bucket));

		boolean isFallback = false;
		if (selected == null || selected.length == 0) {
			selected = quadIndex.get(new QuadKey(Identifier.parse("antique:cloth"), large, bucket));
			isFallback = true;
		}

		RenderType layerType = this.renderType.apply(stack);

		ItemStackRenderState.LayerRenderState baseLayer = state.newLayer();
		if (glint != null) baseLayer.setFoilType(glint);
		baseLayer.setExtents(this.vertices);
		baseLayer.setRenderType(layerType);
		this.settings.applyToLayer(baseLayer, displayContext);
		baseLayer.prepareQuadList().addAll(this.baseQuads);

		ItemStackRenderState.LayerRenderState tintLayer = state.newLayer();
		if (glint != null) tintLayer.setFoilType(glint);
		tintLayer.setExtents(this.vertices);
		tintLayer.setRenderType(layerType);
		this.settings.applyToLayer(tintLayer, displayContext);

		if (selected != null && selected.length > 0) {
			Collections.addAll(tintLayer.prepareQuadList(), selected);
		}

		if (ClientClothData.getTransform(modelVariantId.toString()).dyeable()
				|| tintLayer.prepareQuadList().isEmpty()
				|| isFallback) {
			int n = this.tints.size();
			int[] t = tintLayer.prepareTintLayers(n);
			for (int i = 0; i < n; i++) {
				int c = this.tints.get(i).calculate(stack, world, heldItemContext == null ? null : heldItemContext.asLivingEntity());
				t[i] = c;
				state.appendModelIdentityElement(c);
			}
		}

		if (this.animated) {
			state.setAnimated();
		}
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

			ResourceManager manager = Minecraft.getInstance().getResourceManager();
			manager.listResources("models/item/cloth", path -> path.getPath().endsWith(".json")).keySet().forEach(id -> {
				if (manager.getResource(id).isPresent()) {
					String string = id.toString();
					string = string.substring(0, string.indexOf("."));
					string = string.substring(0, 8) + string.substring(15);
					if (!models.contains(string)) {
						models.add(string);
					}
				}
			});

			for (String model : models) {
				resolver.markDependency(Identifier.parse(model));
			}
		}

		@Override
		public @NotNull ItemModel bake(ItemModel.BakingContext context) {

			ModelBaker baker = context.blockModelBaker();
			List<BakedQuad> variantQuads = new ArrayList<>(64);

			ResolvedModel baseBaked = baker.getModel(this.base);
			TextureSlots baseTex = baseBaked.getTopTextureSlots();
			ModelRenderProperties settings = ModelRenderProperties.fromResolvedModel(baker, baseBaked, baseTex);

			ResourceManager manager = Minecraft.getInstance().getResourceManager();
			manager.listResources("models/item/cloth", path -> path.getPath().endsWith(".json")).keySet().forEach(id -> {
				if (manager.getResource(id).isPresent()) {
					String string = id.toString();
					string = string.substring(0, string.indexOf("."));
					string = string.substring(0, 8) + string.substring(15);
					if (!models.contains(string)) {
						models.add(string);
					}
				}
			});

			for (String model : models) {
				ResolvedModel m = baker.getModel(Identifier.parse(model));
				TextureSlots tex = m.getTopTextureSlots();
				variantQuads.addAll(m.bakeTopGeometry(tex, baker, BlockModelRotation.IDENTITY).getAll());
			}

			List<BakedQuad> baseQuads = baseBaked.bakeTopGeometry(baseTex, baker, BlockModelRotation.IDENTITY).getAll();
			Function<ItemStack, RenderType> function = detectRenderType(baseQuads);

			return new MyriadClothItemModel(this.tints, baseQuads, variantQuads, settings, function);
		}

		@Override
		public @NotNull MapCodec<MyriadClothItemModel.Unbaked> type() {
			return CODEC;
		}
	}
}