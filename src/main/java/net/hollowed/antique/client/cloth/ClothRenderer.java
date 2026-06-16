package net.hollowed.antique.client.cloth;

import com.mojang.blaze3d.vertex.PoseStack;
import net.hollowed.antique.client.renderer.cloth.ClothManager;
import net.hollowed.antique.util.resources.ClothSkinData;
import net.hollowed.antique.util.resources.SewnClothPattern;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;

import java.util.List;

public interface ClothRenderer {
    Identifier getType();

    ClothRenderer fillDefaults(Identifier id);

    void render(
            ClothManager cloth,
            Holder<ClothSkinData> skin,
            PoseStack matrices,
            SubmitNodeCollector queue,
            int light,
            int color,
            List<SewnClothPattern> patterns,
            HolderLookup.Provider registryAccess,
            Matrix4f reprojectionMatrix,
            float tickDelta
    );
}
