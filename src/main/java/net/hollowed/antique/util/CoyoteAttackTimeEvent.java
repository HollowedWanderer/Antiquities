package net.hollowed.antique.util;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.hollowed.antique.config.AntiquitiesConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.component.AttackRange;
import org.jspecify.annotations.NonNull;

public class CoyoteAttackTimeEvent implements ClientTickEvents.EndLevelTick{

    public static int ticks;
    public static Entity target;

    private static final Minecraft client = Minecraft.getInstance();

    // 500 variables
    private static boolean attackCondition() {
        if (client.player == null || client.hitResult == null) return false;
        if (client.player.getMainHandItem().has(DataComponents.PIERCING_WEAPON)) return false;

        AttackRange range = client.player.getMainHandItem().get(DataComponents.ATTACK_RANGE);
        return range == null || range.isInRange(client.player, client.hitResult.getLocation());
    }

    // You better not be looking at this so you can cheat by extending the ticks...
    @Override
    public void onEndTick(@NonNull ClientLevel level) {
        if (AntiquitiesConfig.COYOTE_TIME_TICKS > 0 && client.crosshairPickEntity != null && attackCondition()) {
            ticks = AntiquitiesConfig.COYOTE_TIME_TICKS;
            target = client.crosshairPickEntity;
        }

        if (ticks > 0) ticks--;

        if (target == null || !target.isAlive() || target.isRemoved() || ticks == 0) {
            ticks = 0;
            target = null;
        }
    }

    public static void init() {
        ClientTickEvents.END_LEVEL_TICK.register(new CoyoteAttackTimeEvent());
    }
}
