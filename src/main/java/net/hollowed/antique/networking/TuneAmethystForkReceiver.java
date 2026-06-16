package net.hollowed.antique.networking;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.hollowed.antique.index.AntiqueDataComponentTypes;
import net.hollowed.antique.items.components.AmethystForkComponent;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;

public class TuneAmethystForkReceiver {
    public static void registerServerPacket() {
        ServerPlayNetworking.registerGlobalReceiver(TuneAmethystForkPayload.ID, (payload, context) -> context.server().execute(() -> {
            SlotAccess slot = context.player().getInventory().getSlot(payload.slot());

            if (slot != null) {
                ItemStack stack = slot.get();
                AmethystForkComponent component = stack.get(AntiqueDataComponentTypes.AMETHYST_FORK);

                if (component != null) {
                    stack.set(AntiqueDataComponentTypes.AMETHYST_FORK, component.withNote(payload.note()));
                    slot.set(stack);
                }
            }
        }));
    }
}
