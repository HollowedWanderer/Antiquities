package net.hollowed.antique.util.models;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

@Environment(EnvType.CLIENT)
public class GlowBasicItemModel implements ItemModel {

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

	private final List<ItemTintSource> tints;
	private final List<Integer> emissions;
	private final List<BakedQuad> quads;
	private final Supplier<Vector3fc[]> vector;
	private final ModelRenderProperties settings;
	private final boolean animated;
	private final Function<ItemStack, RenderType> renderType;

	public GlowBasicItemModel(List<ItemTintSource> tints, List<Integer> emissions, List<BakedQuad> quads, ModelRenderProperties settings, Function<ItemStack, RenderType> function) {
		this.tints = tints;
		this.emissions = emissions;
		this.quads = quads;
		this.settings = settings;
		this.vector = Suppliers.memoize(() -> computeExtents(this.quads));
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

	public static Vector3fc[] computeExtents(List<BakedQuad> list) {
		Set<Vector3fc> set = new HashSet<>();

		for(BakedQuad bakedQuad : list) {
			for(int i = 0; i < 4; ++i) {
				set.add(bakedQuad.position(i));
			}
		}

		return set.toArray(Vector3fc[]::new);
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

		int i = this.tints.size();
		int[] is = layerRenderState.prepareTintLayers(i);

		for (int j = 0; j < i; j++) {
			int k = this.tints.get(j).calculate(stack, world, heldItemContext == null ? null : heldItemContext.asLivingEntity());
			is[j] = k;
			state.appendModelIdentityElement(k);
		}

		List<BakedQuad> newQuads = getNewQuads();

		layerRenderState.setExtents(this.vector);
		layerRenderState.setRenderType(this.renderType.apply(stack));
		this.settings.applyToLayer(layerRenderState, displayContext);
		layerRenderState.prepareQuadList().addAll(newQuads);
		if (this.animated) {
			state.setAnimated();
		}
	}

	private @NotNull List<BakedQuad> getNewQuads() {
		List<BakedQuad> newQuads = new java.util.ArrayList<>(List.of());
		String spriteId = this.quads.getFirst().sprite().contents().name().getPath();
		int glowIndex = 0;
		for (BakedQuad quad : this.quads) {
			if (!(quad.sprite().contents().name().getPath().equals(spriteId))) {
				glowIndex++;
				spriteId = quad.sprite().contents().name().getPath();
			}
			newQuads.add(new BakedQuad(quad.position0(), quad.position1(), quad.position2(), quad.position3(), quad.packedUV0(), quad.packedUV1(), quad.packedUV2(), quad.packedUV3(), quad.tintIndex(), quad.direction(), quad.sprite(), quad.shade(), glowIndex >= this.emissions.size() ? quad.lightEmission() : this.emissions.get(glowIndex)));
		}
		return newQuads;
	}

	@SuppressWarnings("all")
	static Function<ItemStack, RenderType> detectRenderType(List<BakedQuad> list) {
		Iterator<BakedQuad> iterator = list.iterator();
		if (!iterator.hasNext()) {
			return ITEM_RENDER_TYPE_GETTER;
		} else {
			Identifier identifier = ((BakedQuad)iterator.next()).sprite().atlasLocation();

			while(iterator.hasNext()) {
				BakedQuad bakedQuad = (BakedQuad)iterator.next();
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
				throw new IllegalArgumentException("Atlas " + String.valueOf(identifier) + " can't be usef for item models");
			}
		}
	}

	private static boolean shouldUseSpecialGlint(ItemStack stack) {
		return stack.is(ItemTags.COMPASSES) || stack.is(Items.CLOCK);
	}

	@Environment(EnvType.CLIENT)
	public record Unbaked(Identifier model, List<Integer> emissions, List<ItemTintSource> tints) implements ItemModel.Unbaked {
		public static final MapCodec<GlowBasicItemModel.Unbaked> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
					Identifier.CODEC.fieldOf("model").forGetter(GlowBasicItemModel.Unbaked::model),
					Codec.INT.listOf().optionalFieldOf("emissions", List.of()).forGetter(GlowBasicItemModel.Unbaked::emissions),
					ItemTintSources.CODEC.listOf().optionalFieldOf("tints", List.of()).forGetter(GlowBasicItemModel.Unbaked::tints)
				)
				.apply(instance, GlowBasicItemModel.Unbaked::new)
		);

		@Override
		public void resolveDependencies(ResolvableModel.Resolver resolver) {
			resolver.markDependency(this.model);
		}

		@Override
		public @NotNull ItemModel bake(ItemModel.BakingContext context) {
			ModelBaker baker = context.blockModelBaker();
			ResolvedModel bakedSimpleModel = baker.getModel(this.model);
			TextureSlots modelTextures = bakedSimpleModel.getTopTextureSlots();
			List<BakedQuad> list = bakedSimpleModel.bakeTopGeometry(modelTextures, baker, BlockModelRotation.IDENTITY).getAll();
			ModelRenderProperties modelSettings = ModelRenderProperties.fromResolvedModel(baker, bakedSimpleModel, modelTextures);
			Function<ItemStack, RenderType> function = detectRenderType(list);
			return new GlowBasicItemModel(this.tints, this.emissions, list, modelSettings, function);
		}

		@Override
		public @NotNull MapCodec<GlowBasicItemModel.Unbaked> type() {
			return CODEC;
		}
	}
}
