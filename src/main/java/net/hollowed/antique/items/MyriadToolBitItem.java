package net.hollowed.antique.items;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public abstract class MyriadToolBitItem extends ShearsItem {

    public MyriadToolBitItem(Properties settings) {
        super(settings);
    }

    /**
     * A method that allows the tool bits to set the attributes of the Myriad Tool they are placed in
     *
     * @param stack - the ItemStack of the Myriad Tool
     * @param level - the level of the Player calling the action
     *
     */
    public abstract void setToolAttributes(ItemStack stack, Level level);

    /**
     * Gets called when the Myriad Tool hosting the tool bit has stopped being used
     *
     * @param stack - the ItemStack from Item's onStoppedUsing, in this case it is the Myriad Tool
     * @param world - the World from Item's onStoppedUsing
     * @param user - the LivingEntity from Item's onStoppedUsing
     * @param remainingUseTicks - the remainingUseTicks from Item's onStoppedUsing
     * @return - the same return style as Item's onStoppedUsing
     */
    public boolean toolOnStoppedUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        return false;
    }

    /**
     * Determines the use action of the Myriad Tool hosting the tool bit
     *
     * @param stack - the ItemStack from Item's getUseAction
     * @return - the same return style as Item's getUseAction
     */
    public ItemUseAnimation toolGetUseAction(ItemStack stack) {
        return ItemUseAnimation.NONE;
    }

    /**
     * Gets called the Myriad Tool hosting the tool bit is used
     *
     * @param world - the World from Item's use method
     * @param user - the PlayerEntity from Item's use method
     * @param hand - the Hand from Item's use method
     * @return - the same return style as Item's use method
     */
    public InteractionResult toolUse(Level world, Player user, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    /**
     *
     * @param context - the ItemUsageContext from Item's useOnBlock
     * @return - the same return style as Item's useOnBlock
     */
    public InteractionResult toolUseOnBlock(UseOnContext context) {
        return InteractionResult.PASS;
    }
}