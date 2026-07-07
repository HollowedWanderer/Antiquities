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

import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.jspecify.annotations.NonNull;

@Environment(EnvType.CLIENT)
public class GlowBasicItemModel implements ItemModel {

	private static final Function<TextureAtlasSprite, RenderType> ITEM_RENDER_TYPE_GETTER = (_) -> Sheets.translucentItemSheet();
	private static final Function<TextureAtlasSprite, RenderType> BLOCK_RENDER_TYPE_GETTER = (sprite) -> {
		ChunkSectionLayer chunkSectionLayer = ChunkSectionLayer.byTransparency(sprite.transparency());
		if (chunkSectionLayer != ChunkSectionLayer.TRANSLUCENT) {
			return Sheets.cutoutBlockItemSheet();
		}

		return Sheets.translucentBlockItemSheet();
	};

	private final List<ItemTintSource> tints;
	private final List<Integer> emissions;
	private final List<BakedQuad> quads;
	private final Supplier<Vector3fc[]> vector;
	private final ModelRenderProperties settings;
	private final boolean animated;

	public GlowBasicItemModel(List<ItemTintSource> tints, List<Integer> emissions, List<BakedQuad> quads, ModelRenderProperties settings) {
		this.tints = tints;
		this.emissions = emissions;
		this.quads = quads;
		this.settings = settings;
		this.vector = Suppliers.memoize(() -> computeExtents(this.quads));
		boolean bl = false;

		for (BakedQuad bakedQuad : quads) {
			if (bakedQuad.materialInfo().sprite().contents().isAnimated()) {
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
			@Nullable ClientLevel level,
			@Nullable ItemOwner owner,
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

		IntList tintLayers = layerRenderState.tintLayers();

		for (ItemTintSource tintSource : this.tints) {
			int tint = tintSource.calculate(stack, level, owner == null ? null : owner.asLivingEntity());
			tintLayers.add(tint);
			state.appendModelIdentityElement(tint);
		}

		List<BakedQuad> newQuads = getNewQuads();

		layerRenderState.setExtents(this.vector);
		this.settings.applyToLayer(layerRenderState, displayContext);
		layerRenderState.prepareQuadList().addAll(newQuads);
		if (this.animated) {
			state.setAnimated();
		}
	}

	private @NotNull List<BakedQuad> getNewQuads() {
		List<BakedQuad> newQuads = new java.util.ArrayList<>(List.of());
		String spriteId = this.quads.getFirst().materialInfo().sprite().contents().name().getPath();
		int glowIndex = 0;
		for (BakedQuad quad : this.quads) {
			if (!(quad.materialInfo().sprite().contents().name().getPath().equals(spriteId))) {
				glowIndex++;
				spriteId = quad.materialInfo().sprite().contents().name().getPath();
			}
			newQuads.add(
					new BakedQuad(
							quad.position0(),
							quad.position1(),
							quad.position2(),
							quad.position3(),
							quad.packedUV0(),
							quad.packedUV1(),
							quad.packedUV2(),
							quad.packedUV3(),
							quad.direction(),
							new BakedQuad.MaterialInfo(
									quad.materialInfo().sprite(),
									quad.materialInfo().layer(),
									quad.materialInfo().itemRenderType(),
									quad.materialInfo().tintIndex(),
									quad.materialInfo().shade(),
									glowIndex >= this.emissions.size() ? quad.materialInfo().lightEmission() : this.emissions.get(glowIndex)
							)
					));
		}
		return newQuads;
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
		public @NonNull ItemModel bake(BakingContext context, @NonNull Matrix4fc transformation) {
			ModelBaker baker = context.blockModelBaker();
			ResolvedModel bakedSimpleModel = baker.getModel(this.model);
			TextureSlots modelTextures = bakedSimpleModel.getTopTextureSlots();
			List<BakedQuad> list = bakedSimpleModel.bakeTopGeometry(modelTextures, baker, BlockModelRotation.IDENTITY).getAll();
			ModelRenderProperties modelSettings = ModelRenderProperties.fromResolvedModel(baker, bakedSimpleModel, modelTextures);
			return new GlowBasicItemModel(this.tints, this.emissions, list, modelSettings);
		}

		@Override
		public @NotNull MapCodec<GlowBasicItemModel.Unbaked> type() {
			return CODEC;
		}
	}
}
