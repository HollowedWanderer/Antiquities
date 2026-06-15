package net.hollowed.antique.client.cloth;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public interface ClothOwner {
    @NonNull Vec3 getPosition();

    @NonNull Level getLevel();

    @Nullable SoundSource getSoundSource();

    boolean isRemoved();

    boolean isSilent();

    default @Nullable Entity asEntity() {
        return null;
    }

    default @Nullable BlockEntity asBlockEntity() {
        return null;
    }

    record OfEntity(
            @NonNull Entity entity
    ) implements ClothOwner {
        @Override
        public @NonNull Vec3 getPosition() {
            return entity.position();
        }

        @Override
        public @NonNull Level getLevel() {
            return entity.level();
        }

        @Override
        public @NonNull SoundSource getSoundSource() {
            return entity.getSoundSource();
        }

        @Override
        public boolean isRemoved() {
            return entity.isRemoved();
        }

        @Override
        public boolean isSilent() {
            return entity.isSilent();
        }

        @Override
        public @NonNull Entity asEntity() {
            return entity;
        }
    }

    record OfBlockEntity(
            @NonNull BlockEntity entity,
            @NonNull Level level
    ) implements ClothOwner {
        @Override
        public @NonNull Vec3 getPosition() {
            return Vec3.atCenterOf(entity.getBlockPos());
        }

        @Override
        public @NonNull Level getLevel() {
            return level;
        }

        @Override
        public @NonNull SoundSource getSoundSource() {
            return SoundSource.BLOCKS;
        }

        @Override
        public boolean isRemoved() {
            return entity.isRemoved();
        }

        @Override
        public boolean isSilent() {
            return false;
        }

        @Override
        public @NonNull BlockEntity asBlockEntity() {
            return entity;
        }
    }
}
