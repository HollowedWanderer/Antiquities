package net.hollowed.antique.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class AntiquitiesConfig extends MidnightConfig {

    @Entry(name = "Max Cloth Patterns", min = 0, max = 16)
    public static int MAX_CLOTH_PATTERNS = 4;

    @Entry(name = "Coyote Attack Time Ticks", min = 0, max = 40)
    public static int COYOTE_TIME_TICKS = 4;

    @Entry(name = "Slide Power", min = 0.0, max = 100.0)
    public static float SLIDE_POWER = 0.4F;

    @Entry(name = "Slide Duration", min = 0, max = 100)
    public static int SLIDE_DURATION = 12;

    @Entry(name = "Slide Start Delay", min = 0, max = 100)
    public static int SLIDE_START_DELAY = 4;

    @Entry(name = "Slide Pounce Window", min = 0, max = 100)
    public static int SLIDE_POUNCE_WINDOW = 5;

    @Entry(name = "Slide Pounce Vertical Power", min = 0.0, max = 100.0)
    public static float SLIDE_POUNCE_VERTICAL_POWER = 0.25F;

    @Entry(name = "Slide Pounce Horizontal Power", min = 0.0, max = 100.0)
    public static float SLIDE_POUNCE_HORIZONTAL_POWER = 0.2F;
}
