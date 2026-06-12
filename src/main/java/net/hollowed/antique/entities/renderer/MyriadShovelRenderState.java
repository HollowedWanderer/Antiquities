package net.hollowed.antique.entities.renderer;

import net.hollowed.antique.items.components.ColorProvider;
import net.hollowed.antique.util.resources.ClothSkin;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class MyriadShovelRenderState extends EntityRenderState {
    public Entity entity;
    public ItemStack stack;
    public ColorProvider color;
    public int overlayColor;
    public boolean isEnchanted;
    public boolean glow;
    @SuppressWarnings("all")
    public Optional<? extends Holder<ClothSkin>> cloth;
    @SuppressWarnings("all")
    public Optional<Identifier> pattern;
    public boolean emissiveItem;
}