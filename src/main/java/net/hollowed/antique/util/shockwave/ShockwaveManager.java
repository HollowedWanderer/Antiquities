package net.hollowed.antique.util.shockwave;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ShockwaveManager {

    public static final Map<Shockwave, ServerLevel> shockwaves = new HashMap<>();
    public static final Map<Block, Float> RESISTANCES = new HashMap<>();

    static {
        RESISTANCES.put(Blocks.AIR, 0.85f);
        RESISTANCES.put(Blocks.WATER, 0.98f);
        RESISTANCES.put(Blocks.LAVA, 0.9f);
    }

    public static final int MAX_PATH_LENGTH = 4;

    public static void createShockwave(@NotNull Shockwave shockwave, @NotNull ServerLevel level) {
        shockwaves.put(shockwave, level);
    }
}