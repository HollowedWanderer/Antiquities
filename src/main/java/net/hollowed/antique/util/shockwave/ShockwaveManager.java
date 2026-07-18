package net.hollowed.antique.util.shockwave;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ShockwaveManager {

    private static final Map<Shockwave, ServerLevel> shockwaves = new HashMap<>();
    public static final Map<Block, Float> RESISTANCES = new HashMap<>();

    static {
        RESISTANCES.put(Blocks.AIR, 0.85f);
        RESISTANCES.put(Blocks.WATER, 0.98f);
        RESISTANCES.put(Blocks.LAVA, 0.9f);
    }

    public static final int MAX_PATH_LENGTH = 4;

    public static Set<Shockwave> getShockwaveSet() {
        return shockwaves.keySet();
    }

    public static ServerLevel getShockwaveLevel(Shockwave shockwave) {
        return shockwaves.get(shockwave);
    }

    public static void removeShockwave(@NotNull Shockwave shockwave, @NotNull ServerLevel level) {
        shockwaves.remove(shockwave, level);
    }

    public static void createShockwave(@NotNull Shockwave shockwave, @NotNull ServerLevel level) {
        shockwaves.put(shockwave, level);
    }
}