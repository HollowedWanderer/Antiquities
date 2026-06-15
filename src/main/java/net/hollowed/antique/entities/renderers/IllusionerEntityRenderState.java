package net.hollowed.antique.entities.renderers;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class IllusionerEntityRenderState extends IllagerRenderState {
    public Vec3[] mirrorCopyOffsets = new Vec3[0];
    public boolean spellcasting;

    public IllusionerEntityRenderState() {
    }
}
