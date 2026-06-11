package net.hollowed.antique.items.components;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.hollowed.antique.Antiquities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public interface ColorProvider {
    Identifier getType();

    int getColor(int tick, float tickDelta);

    int getConstantColor(Function<ColorProvider, String> error);

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
        public int getConstantColor(Function<ColorProvider, String> error) {
            return color;
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

        @Override
        public int getConstantColor(Function<ColorProvider, String> error) {
            throw new UnsupportedOperationException(error.apply(this));
        }
    }
}
