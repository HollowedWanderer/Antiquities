package net.hollowed.antique.util.resources;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hollowed.antique.Antiquities;
import net.hollowed.antique.client.ext.SpriteContentsAnimationStateExtension;
import net.hollowed.antique.mixin.accessors.SpriteContentsAnimationStateAccessor;
import net.hollowed.antique.mixin.accessors.TextureAtlasAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface ColorProvider {
    Identifier getType();

    int getColor(int tick, float tickDelta);

    default Optional<Integer> getConstantColor() {
        return Optional.empty();
    }

    @Environment(EnvType.CLIENT)
    default int getColorClient() {
        ClientLevel level = Minecraft.getInstance().level;
        return getColor(level == null ? 0 : (int) level.getGameTime(), Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true));
    }

    record Constant(
            int color
    ) implements ColorProvider {
        public static final Identifier ID = Antiquities.id("constant");
        public static final MapCodec<Constant> CODEC = Codec.mapEither(
                Codec.INT.fieldOf("color").xmap(Constant::new, Constant::color),
                Codec.STRING.fieldOf("color").xmap(Constant::new, color -> Integer.toHexString(color.color))
        ).xmap(
                either -> either.map(l -> l, r -> r),
                Either::right
        );
        public static final StreamCodec<ByteBuf, Constant> STREAM_CODEC = ByteBufCodecs.INT.map(Constant::new, Constant::color);

        public Constant(String hex) {
            this(Integer.parseInt(hex, 16));
        }

        @Override
        public Identifier getType() {
            return ID;
        }

        @Override
        public int getColor(int tick, float tickDelta) {
            return color;
        }

        @Override
        public Optional<Integer> getConstantColor() {
            return Optional.of(color);
        }
    }

    record Animated(
            List<Integer> frames,
            int frameTime,
            boolean interpolate
    ) implements ColorProvider {
        public static final Identifier ID = Antiquities.id("animated");
        public static final MapCodec<Animated> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Codec.either(
                                Codec.INT,
                                Codec.STRING.xmap(text -> Integer.parseInt(text, 16), Integer::toHexString)
                        ).xmap(
                                either -> either.map(l -> l, r -> r),
                                Either::right
                        ).listOf().fieldOf("frames").forGetter(Animated::frames),
                        Codec.INT.optionalFieldOf("frameTime", 1).forGetter(Animated::frameTime),
                        Codec.BOOL.optionalFieldOf("interpolate", false).forGetter(Animated::interpolate)
                ).apply(instance, Animated::new)
        );
        public static final StreamCodec<ByteBuf, Animated> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.<ByteBuf, Integer>list().apply(ByteBufCodecs.INT), Animated::frames,
                ByteBufCodecs.INT, Animated::frameTime,
                ByteBufCodecs.BOOL, Animated::interpolate,
                Animated::new
        );

        @Override
        public Identifier getType() {
            return ID;
        }

        @Override
        public int getColor(int tick, float tickDelta) {
            int frameIndex = (tick / frameTime) % frames.size();
            int frame = frames.get(frameIndex);

            if (interpolate) {
                int frame2 = frames.get((frameIndex + 1) % frames.size());
                float delta = (tickDelta + (tick % frameTime)) / frameTime;

                float a = (frame & 0xFF) * (1 - delta) + (frame2 & 0xFF) * delta;
                float b = ((frame >>> 8) & 0xFF) * (1 - delta) + ((frame2 >>> 8) & 0xFF) * delta;
                float c = ((frame >>> 16) & 0xFF) * (1 - delta) + ((frame2 >>> 16) & 0xFF) * delta;
                float d = ((frame >>> 24) & 0xFF) * (1 - delta) + ((frame2 >>> 24) & 0xFF) * delta;

                return (int) a | ((int) b) << 8 | ((int) c) << 16 | ((int) d) << 24;
            } else {
                return frame;
            }
        }
    }

    record SpriteAnimated(
            List<Integer> frames,
            int frameTime,
            boolean interpolate,
            int frameOffset,
            Identifier atlas,
            Identifier sprite
    ) implements ColorProvider {
        public static final Identifier ID = Antiquities.id("sprite_animated");
        public static final MapCodec<SpriteAnimated> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Codec.either(
                                Codec.INT,
                                Codec.STRING.xmap(text -> Integer.parseInt(text, 16), Integer::toHexString)
                        ).xmap(
                                either -> either.map(l -> l, r -> r),
                                Either::right
                        ).listOf().fieldOf("frames").forGetter(SpriteAnimated::frames),
                        Codec.INT.optionalFieldOf("frameTime", 1).forGetter(SpriteAnimated::frameTime),
                        Codec.BOOL.optionalFieldOf("interpolate", false).forGetter(SpriteAnimated::interpolate),
                        Codec.INT.optionalFieldOf("frameOffset", 0).forGetter(SpriteAnimated::frameOffset),
                        Identifier.CODEC.fieldOf("atlas").forGetter(SpriteAnimated::atlas),
                        Identifier.CODEC.fieldOf("sprite").forGetter(SpriteAnimated::sprite)
                ).apply(instance, SpriteAnimated::new)
        );
        public static final StreamCodec<ByteBuf, SpriteAnimated> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.<ByteBuf, Integer>list().apply(ByteBufCodecs.INT), SpriteAnimated::frames,
                ByteBufCodecs.INT, SpriteAnimated::frameTime,
                ByteBufCodecs.BOOL, SpriteAnimated::interpolate,
                ByteBufCodecs.INT, SpriteAnimated::frameOffset,
                Identifier.STREAM_CODEC, SpriteAnimated::atlas,
                Identifier.STREAM_CODEC, SpriteAnimated::sprite,
                SpriteAnimated::new
        );

        @Override
        public Identifier getType() {
            return ID;
        }

        public static SpriteContentsAnimationStateAccessor findAnimationState(Identifier atlasId, Identifier spriteId) {
            TextureAtlas atlas = Minecraft.getInstance()
                    .getAtlasManager()
                    .getAtlasOrThrow(atlasId);

            for (SpriteContents.AnimationState state : ((TextureAtlasAccessor) atlas).antique$getAnimatedTexturesStates()) {
                TextureAtlasSprite sprite = ((SpriteContentsAnimationStateExtension) state).antique$getParentSprite();

                if (sprite.contents().name().equals(spriteId)) {
                    return (SpriteContentsAnimationStateAccessor) state;
                }
            }

            throw new NullPointerException("Unable to find sprite " + spriteId + " in atlas " + atlasId);
        }

        @Override
        public int getColor(int tick, float tickDelta) {
            try {
                SpriteContentsAnimationStateAccessor accessor = findAnimationState(atlas, sprite);

                int frameIndex = (frames.size() - 1 - accessor.antique$getFrame() + frameOffset) % frames.size();
                int frame = frames.get(frameIndex);

                if (interpolate) {
                    int frame2 = frames.get((frameIndex == 0 ? frames.size() : frameIndex) - 1);
                    float delta = (float) accessor.antique$getSubFrame() / frameTime;

                    float a = (frame & 0xFF) * (1 - delta) + (frame2 & 0xFF) * delta;
                    float b = ((frame >>> 8) & 0xFF) * (1 - delta) + ((frame2 >>> 8) & 0xFF) * delta;
                    float c = ((frame >>> 16) & 0xFF) * (1 - delta) + ((frame2 >>> 16) & 0xFF) * delta;
                    float d = ((frame >>> 24) & 0xFF) * (1 - delta) + ((frame2 >>> 24) & 0xFF) * delta;

                    return (int) a | ((int) b) << 8 | ((int) c) << 16 | ((int) d) << 24;
                } else {
                    return frame;
                }
            } catch (NullPointerException e) {
                Antiquities.LOGGER.error("Error getting color of sprite animated color provider {}", this, e);
                return 0;
            }
        }
    }
}
