package net.hollowed.antique.mixin.entities.living;

import net.hollowed.antique.index.AntiqueEntities;
import net.hollowed.antique.entities.IllusionerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Raid.class)
public abstract class RaidModifierMixin {

    @Shadow private int groupsSpawned;

    @Shadow public abstract int getNumGroups(Difficulty difficulty);

    @Shadow public abstract void joinRaid(ServerLevel level, int groupNumber, Raider raider, @Nullable BlockPos pos, boolean exists);

    @Inject(method = "spawnGroup", at = @At("HEAD"))
    public void spawnNextWave(ServerLevel level, BlockPos pos, CallbackInfo ci) {
        int i = this.groupsSpawned;
        IllusionerEntity raiderEntity = new IllusionerEntity(AntiqueEntities.ILLUSIONER, level);
        raiderEntity.setItemInHand(InteractionHand.MAIN_HAND, Items.BOW.getDefaultInstance());
        switch (level.getDifficulty()) {
            case Difficulty.EASY -> {
                if (i == this.getNumGroups(Difficulty.EASY)) {
                    this.joinRaid(level, i, raiderEntity, pos, false);
                }
            }
            case Difficulty.NORMAL -> {
                if (i == this.getNumGroups(Difficulty.NORMAL)) {
                    this.joinRaid(level, i, raiderEntity, pos, false);
                }
            }
            case Difficulty.HARD -> {
                if (i == this.getNumGroups(Difficulty.HARD)) {
                    this.joinRaid(level, i, raiderEntity, pos, false);
                }
            }
        }
    }
}
