package net.hollowed.antique.entities.renderers;

import net.hollowed.antique.util.resources.ClothPatternData;
import net.hollowed.antique.util.resources.ClothSkinData;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

@SuppressWarnings("all")
public class MyriadShovelRenderState extends EntityRenderState {
    public Entity entity;
    public ItemStack stack;
    public Integer color;
    public Optional<Integer> patternColor;
    public boolean patternGlow;
    public boolean isEnchanted;
    public Optional<? extends Holder<ClothSkinData>> cloth;
    public Optional<? extends Holder<ClothPatternData>> pattern;
}