package net.hollowed.antique.util;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class LeftClickHandler {
    public static final MinecraftClient client = MinecraftClient.getInstance();
    private static boolean keyProcessed = false;
    private static long lastItemUseTime = 0;
    private static final long COOLDOWN_PERIOD = 200;

    public static void checkRightClickOnTopFace() {
        if (client.world == null || client.player == null) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (client.options.attackKey.isPressed() && client.player.getMainHandStack() != ItemStack.EMPTY) {
            lastItemUseTime = System.currentTimeMillis();
        }

        if (client.options.attackKey.isPressed() && !client.player.isUsingItem() && (currentTime - lastItemUseTime) > COOLDOWN_PERIOD) {
            HitResult hitResult = client.crosshairTarget;

            assert hitResult != null;
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                Direction face = blockHitResult.getSide();

                if (face == Direction.UP && isInteractingWithUsableBlock(blockHitResult)) {

                    // Call stuff here

                } else if (face != Direction.UP && isInteractingWithUsableBlock(blockHitResult)) {

                    // Call stuff here

                }
            }
        }
    }

    public static void checkRightClickInAir() {
        if (client.world == null || client.player == null) {
            return;
        }

        if (client.options.attackKey.isPressed() && client.player.getMainHandStack() != ItemStack.EMPTY) {
            lastItemUseTime = System.currentTimeMillis();
            if (client.player.isUsingItem()) {
                client.player.stopUsingItem();
                client.player.getItemCooldownManager().set(client.player.getMainHandStack(), 5);
                client.player.swingHand(Hand.MAIN_HAND);
            }
        }
    }

    public static boolean isInteractingWithUsableBlock(BlockHitResult blockHitResult) {
        BlockPos blockPos = blockHitResult.getBlockPos();
        assert client.world != null;
        BlockState blockState = client.world.getBlockState(blockPos);
        ClientPlayerEntity player = client.player;

        if (blockState.hasBlockEntity()) {
            return false;
        }

        ActionResult result = blockState.onUse(client.world, player, blockHitResult);

        return result == ActionResult.PASS;
    }
}
