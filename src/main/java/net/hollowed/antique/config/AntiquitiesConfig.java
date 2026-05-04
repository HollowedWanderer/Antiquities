package net.hollowed.antique.config;

import eu.midnightdust.lib.config.MidnightConfig;

@SuppressWarnings("unused")
public class AntiquitiesConfig extends MidnightConfig {
    @Entry(name = "X", min = -100) public static double matricesX;
    @Entry(name = "Y", min = -100) public static double matricesY;
    @Entry(name = "Z", min = -100) public static double matricesZ;
}
