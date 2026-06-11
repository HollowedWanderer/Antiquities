package net.hollowed.antique.entities.renderer;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class MyriadShovelRenderState extends EntityRenderState {
    public Entity entity;
    public ItemStack stack;
    public int color;
    public int overlayColor;
    public boolean isEnchanted;
    public boolean glow;
    public String cloth;
    public Optional<Identifier> pattern;
}