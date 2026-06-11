package net.hollowed.antique.util.models;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hollowed.antique.Antiquities;
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
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
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

	private final List<BakedQuad> quads;
	private final Supplier<Vector3fc[]> vector;
	private final ModelRenderProperties settings;
	private final boolean animated;
	private static final ArrayList<String> models = new ArrayList<>();
	private final Function<ItemStack, RenderType> renderType;

	private record QuadKey(String variant) {}
	private final Map<QuadKey, BakedQuad[]> quadIndex;

	public ClothPatternItemModel(List<BakedQuad> quads, ModelRenderProperties settings, Function<ItemStack, RenderType> function) {
		this.quads = quads;
		this.settings = settings;
		this.vector = Suppliers.memoize(() -> computeExtents(this.quads));
		this.quadIndex = buildQuadIndex(quads);
		this.renderType = function;
		boolean bl = false;

		for (BakedQuad bakedQuad : quads) {
			if (bakedQuad.sprite().contents().isAnimated()) {
				bl = true;
				break;
			}
		}

		this.animated = bl;
	}

	private static Map<QuadKey, BakedQuad[]> buildQuadIndex(List<BakedQuad> quads) {
		Map<QuadKey, List<BakedQuad>> temp = new HashMap<>(64);

		for (BakedQuad quad : quads) {
			Identifier id = quad.sprite().contents().name();
			String path = id.getPath();

			String variant = extractVariantName(path);
			variant = variant.intern();

			QuadKey key = new QuadKey(variant);
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
		return name.isEmpty() ? "cloth_pattern" : name;
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
		ItemStackRenderState.LayerRenderState layerRenderState = state.newLayer();
		if (stack.hasFoil()) {
			ItemStackRenderState.FoilType glint = shouldUseSpecialGlint(stack) ? ItemStackRenderState.FoilType.SPECIAL : ItemStackRenderState.FoilType.STANDARD;
			layerRenderState.setFoilType(glint);
			state.setAnimated();
			state.appendModelIdentityElement(glint);
		}

		String modelVariant = "item.antique.cloth_pattern";
		Component text = stack.getOrDefault(DataComponents.ITEM_NAME, Component.translatable("item.antique.cloth_pattern"));
		if (text.getContents() instanceof TranslatableContents translatable) {
			modelVariant = translatable.getKey();
		}
		modelVariant = modelVariant.substring(modelVariant.lastIndexOf(".") + 1);
		state.appendModelIdentityElement(modelVariant);

		BakedQuad[] selected = quadIndex.get(new QuadKey(modelVariant));
		if (selected == null || selected.length == 0) selected = quadIndex.get(new QuadKey("cloth_pattern"));

		layerRenderState.setExtents(this.vector);
		layerRenderState.setRenderType(this.renderType.apply(stack));
		this.settings.applyToLayer(layerRenderState, displayContext);
		if (selected != null && selected.length > 0) {
			Collections.addAll(layerRenderState.prepareQuadList(), selected);
		}

		if (this.animated) {
			state.setAnimated();
		}
	}

	private static boolean shouldUseSpecialGlint(ItemStack stack) {
		return stack.is(ItemTags.COMPASSES) || stack.is(Items.CLOCK);
	}

	@Environment(EnvType.CLIENT)
	public record Unbaked() implements ItemModel.Unbaked {
		public static final MapCodec<net.hollowed.antique.util.models.ClothPatternItemModel.Unbaked> CODEC = MapCodec.unit(new net.hollowed.antique.util.models.ClothPatternItemModel.Unbaked());

		@Override
		public void resolveDependencies(Resolver resolver) {
			resolver.markDependency(Antiquities.id("item/cloth_pattern"));

			ResourceManager manager = Minecraft.getInstance().getResourceManager();
			manager.listResources("models/item", path -> path.getPath().endsWith(".json")).keySet().forEach(id -> {
				if (manager.getResource(id).isPresent() && id.getPath().contains("_cloth_pattern")) {
					String string = id.toString();
					string = string.substring(0, string.indexOf("."));
					string = string.substring(0, string.indexOf(":") + 1) + string.substring(string.indexOf("/") + 1);
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
		public @NotNull ItemModel bake(BakingContext context) {
			ModelBaker baker = context.blockModelBaker();
			List<BakedQuad> variantQuads = new ArrayList<>(64);

			if (!models.contains("antique:item/cloth_pattern")) {
				models.add("antique:item/cloth_pattern");
			}

			ResolvedModel baseBaked = baker.getModel(Antiquities.id("item/cloth_pattern"));
			TextureSlots baseTex = baseBaked.getTopTextureSlots();
			ModelRenderProperties settings = ModelRenderProperties.fromResolvedModel(baker, baseBaked, baseTex);

			ResourceManager manager = Minecraft.getInstance().getResourceManager();
			manager.listResources("models/item", path -> path.getPath().endsWith(".json")).keySet().forEach(id -> {
				if (manager.getResource(id).isPresent() && id.getPath().contains("_cloth_pattern")) {
					String string = id.toString();
					string = string.substring(0, string.indexOf("."));
					string = string.substring(0, string.indexOf(":") + 1) + string.substring(string.indexOf("/") + 1);
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

			Function<ItemStack, RenderType> function = detectRenderType(variantQuads);

			return new ClothPatternItemModel(variantQuads, settings, function);
		}

		@Override
		public @NotNull MapCodec<net.hollowed.antique.util.models.ClothPatternItemModel.Unbaked> type() {
			return CODEC;
		}
	}
}
