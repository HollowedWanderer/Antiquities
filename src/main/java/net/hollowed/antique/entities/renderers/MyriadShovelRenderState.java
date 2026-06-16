package net.hollowed.antique.entities.renderers;

import net.hollowed.antique.util.resources.ClothSkinData;
import net.hollowed.antique.util.resources.SewnClothPattern;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("all")
public class MyriadShovelRenderState extends EntityRenderState {
    public Entity entity;
    public ItemStack stack;
    public Integer color;
    public boolean isEnchanted;
    public Optional<? extends Holder<ClothSkinData>> cloth;
    public List<SewnClothPattern> patterns;
    public float tickDelta;
}