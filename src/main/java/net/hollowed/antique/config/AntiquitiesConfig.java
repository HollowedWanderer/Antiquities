package net.hollowed.antique.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class AntiquitiesConfig extends MidnightConfig {
    @Entry(name = "Max Cloth Patterns", min = 0, max = 16)
    public static int MAX_CLOTH_PATTERNS;
}
