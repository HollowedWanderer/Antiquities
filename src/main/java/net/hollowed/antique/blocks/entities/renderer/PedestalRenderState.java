package net.hollowed.antique.blocks.entities.renderer;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PedestalRenderState extends BlockEntityRenderState {

    public ItemStack storedStack;
    public long worldTime;
    public Level world;
}
