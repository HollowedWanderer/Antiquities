package net.hollowed.antique.items;

import net.hollowed.antique.Antiquities;
import net.hollowed.antique.index.AntiqueItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockTransformers;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeetrootBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MyriadMattockBit extends MyriadToolBitItem{

    public MyriadMattockBit(Properties settings) {
        super(settings);
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, @NotNull Player user, @NotNull LivingEntity entity, @NotNull InteractionHand hand) {
        if (entity instanceof Sheep) {
            user.swing(hand, SwingAnimation.DEFAULT, true);
        }
        return super.interactLivingEntity(stack, user, entity, hand);
    }

    @Override
    public boolean canDestroyBlock(@NotNull ItemStack stack, BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull LivingEntity miner) {
        if (state.getBlock() instanceof CropBlock cropBlock && !(state.getBlock() instanceof BeetrootBlock)) {
            if (state.getValue(CropBlock.AGE) != cropBlock.getMaxAge() && !miner.isShiftKeyDown()) {
                return false;
            }
        } else if (state.getBlock() instanceof BeetrootBlock cropBlock) {
            if (state.getValue(BeetrootBlock.AGE) != cropBlock.getMaxAge() && !miner.isShiftKeyDown()) {
                return false;
            }
        } else if (state.getBlock() instanceof NetherWartBlock) {
            if (state.getValue(NetherWartBlock.AGE) != NetherWartBlock.MAX_AGE && !miner.isShiftKeyDown()) {
                return false;
            }
        } else if (state.getBlock() instanceof CocoaBlock) {
            if (state.getValue(CocoaBlock.AGE) != CocoaBlock.MAX_AGE && !miner.isShiftKeyDown()) {
                return false;
            }
        }
        return super.canDestroyBlock(stack, state, world, pos, miner);
    }

    @Override
    public boolean mineBlock(@NotNull ItemStack stack, @NotNull Level world, BlockState state, @NotNull BlockPos pos, @NotNull LivingEntity miner) {
        if (state.getBlock() instanceof CropBlock cropBlock && !(state.getBlock() instanceof BeetrootBlock)) {
            if (state.getValue(CropBlock.AGE) == cropBlock.getMaxAge() && !miner.isShiftKeyDown()) {
                BlockState newState = cropBlock.defaultBlockState();
                world.setBlockAndUpdate(pos, newState);
            }
        } else if (state.getBlock() instanceof BeetrootBlock cropBlock) {
            if (state.getValue(BeetrootBlock.AGE) == cropBlock.getMaxAge() && !miner.isShiftKeyDown()) {
                BlockState newState = cropBlock.defaultBlockState();
                world.setBlockAndUpdate(pos, newState);
            }
        } else if (state.getBlock() instanceof NetherWartBlock cropBlock) {
            if (state.getValue(NetherWartBlock.AGE) == NetherWartBlock.MAX_AGE && !miner.isShiftKeyDown()) {
                BlockState newState = cropBlock.defaultBlockState();
                world.setBlockAndUpdate(pos, newState);
            }
        } else if (state.getBlock() instanceof CocoaBlock) {
            if (state.getValue(CocoaBlock.AGE) == CocoaBlock.MAX_AGE && !miner.isShiftKeyDown()) {
                BlockState newState = state.setValue(CocoaBlock.AGE, 0);
                world.setBlockAndUpdate(pos, newState);
            }
        }
        return super.mineBlock(stack, world, state, pos, miner);
    }

    @Override
    public InteractionResult toolUse(Level world, Player user, InteractionHand hand) {
        double d = -Mth.sin(user.getYRot() * (float) (Math.PI / 180.0));
        double e = Mth.cos(user.getYRot() * (float) (Math.PI / 180.0));
        if (user.level() instanceof ServerLevel serverWorld) {
            serverWorld.sendParticles(ParticleTypes.SWEEP_ATTACK, user.getX() + d, user.getY(0.5), user.getZ() + e, 0, d, 0.0, e, 0.0);
        }
        user.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1, 1);
        user.swing(hand, SwingAnimation.DEFAULT, true);
        user.getCooldowns().addCooldown(user.getItemInHand(hand), 10);
        Vec3 forward = user.position().add(user.getLookAngle().scale(2));
        AABB box = new AABB(
                forward.x - 1.5, forward.y - 1.5, forward.z - 1.5,
                forward.x + 1.5, forward.y + 1.5, forward.z + 1.5
        );
        for (Entity entity : world.getEntities(user, box)) {
            entity.push(user.getLookAngle().scale(-1));
            entity.syncVelocity = true;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void setToolAttributes(ItemStack tool, Level level) {
        tool.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 5.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -2.4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build());
        tool.set(DataComponents.TOOL, new Tool(
                List.of(
                        Tool.Rule.deniesDrops(AntiqueItems.registryEntryLookup.getOrThrow(BlockTags.INCORRECT_FOR_IRON_TOOL)),
                        Tool.Rule.minesAndDrops(AntiqueItems.registryEntryLookup.getOrThrow(TagKey.create(Registries.BLOCK, Antiquities.id("mineable/mattock"))), 20)
                ),
                1.0F,
                1,
                true
        ));
        tool.set(DataComponents.BLOCK_TRANSFORMER, level.registryAccess().getOrThrow(BlockTransformers.HOE));
    }
}
