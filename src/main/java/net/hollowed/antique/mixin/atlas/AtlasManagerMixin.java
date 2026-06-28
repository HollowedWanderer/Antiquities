package net.hollowed.antique.mixin.atlas;

import net.hollowed.antique.AntiquitiesClient;
import net.minecraft.client.resources.model.sprite.AtlasManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;

@Mixin(AtlasManager.class)
public class AtlasManagerMixin {
    @Shadow
    @Final
    @Mutable
    private static List<AtlasManager.AtlasConfig> KNOWN_ATLASES;

    static {
        List<AtlasManager.AtlasConfig> list = new ArrayList<>(KNOWN_ATLASES);
        list.add(new AtlasManager.AtlasConfig(AntiquitiesClient.CLOTHS_ATLAS_TEXTURE, AntiquitiesClient.CLOTHS_ATLAS, false));
        KNOWN_ATLASES = list;
    }
}
