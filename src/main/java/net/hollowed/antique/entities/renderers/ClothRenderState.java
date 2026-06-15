package net.hollowed.antique.entities.renderers;

import net.hollowed.antique.entities.ClothEntity;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.item.ItemStack;

public class ClothRenderState extends EntityRenderState {
    public ItemStack cloth;
    public ClothEntity entity;
    public float tickDelta;
}