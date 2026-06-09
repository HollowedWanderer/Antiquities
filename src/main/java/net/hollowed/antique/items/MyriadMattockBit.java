package net.hollowed.antique.items;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import net.hollowed.antique.Antiquities;
import net.hollowed.antique.index.AntiqueItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeetrootBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class MyriadMattockBit extends MyriadToolBitItem{

    protected static final Map<Block, Pair<Predicate<UseOnContext>, Consumer<UseOnContext>>> TILLING_ACTIONS = Maps.newHashMap(
            ImmutableMap.of(
                    Blocks.GRASS_BLOCK,
                    com.mojang.datafixers.util.Pair.of(HoeItem::onlyIfAirAbove, createTillAction(Blocks.FARMLAND.defaultBlockState())),
                    Blocks.DIRT_PATH,
                    com.mojang.datafixers.util.Pair.of(HoeItem::onlyIfAirAbove, createTillAction(Blocks.FARMLAND.defaultBlockState())),
                    Blocks.DIRT,
                    com.mojang.datafixers.util.Pair.of(HoeItem::onlyIfAirAbove, createTillAction(Blocks.FARMLAND.defaultBlockState())),
                    Blocks.COARSE_DIRT,
                    com.mojang.datafixers.util.Pair.of(HoeItem::onlyIfAirAbove, createTillAction(Blocks.DIRT.defaultBlockState())),
                    Blocks.ROOTED_DIRT,
                    Pair.of(itemUsageContext -> true, createTillAndDropAction(Blocks.DIRT.defaultBlockState(), Items.HANGING_ROOTS))
            )
    );

    public MyriadMattockBit(Properties settings) {
        super(settings);
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, @NotNull Player user, @NotNull LivingEntity entity, @NotNull InteractionHand hand) {
        if (entity instanceof Sheep) {
            user.swing(hand);
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
        user.swing(hand, true);
        user.getCooldowns().addCooldown(user.getItemInHand(hand), 10);
        Vec3 forward = user.position().add(user.getLookAngle().scale(2));
        AABB box = new AABB(
                forward.x - 1.5, forward.y - 1.5, forward.z - 1.5,
                forward.x + 1.5, forward.y + 1.5, forward.z + 1.5
        );
        for (Entity entity : world.getEntities(user, box)) {
            entity.push(user.getLookAngle().scale(-1));
            entity.hurtMarked = true;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult toolUseOnBlock(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        Player playerEntity = context.getPlayer();
        if (playerEntity == null) return InteractionResult.FAIL;

        if (playerEntity.isShiftKeyDown()) {
            Pair<Predicate<UseOnContext>, Consumer<UseOnContext>> pair = TILLING_ACTIONS.get(
                    world.getBlockState(blockPos).getBlock()
            );
            if (pair == null) {
                return InteractionResult.PASS;
            } else {
                Predicate<UseOnContext> predicate = pair.getFirst();
                Consumer<UseOnContext> consumer = pair.getSecond();
                if (predicate.test(context)) {
                    world.playSound(playerEntity, blockPos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                    if (!world.isClientSide()) {
                        consumer.accept(context);
                        context.getItemInHand().hurtAndBreak(1, playerEntity, context.getHand());
                    }

                    return InteractionResult.SUCCESS;
                } else {
                    return InteractionResult.PASS;
                }
            }
        } else {
            this.toolUse(world, playerEntity, context.getHand());
        }
        return super.toolUseOnBlock(context);
    }

    public static Consumer<UseOnContext> createTillAction(BlockState result) {
        return context -> {
            context.getLevel().setBlock(context.getClickedPos(), result, Block.UPDATE_ALL_IMMEDIATE);
            context.getLevel().gameEvent(GameEvent.BLOCK_CHANGE, context.getClickedPos(), GameEvent.Context.of(context.getPlayer(), result));
        };
    }

    public static Consumer<UseOnContext> createTillAndDropAction(BlockState result, ItemLike droppedItem) {
        return context -> {
            context.getLevel().setBlock(context.getClickedPos(), result, Block.UPDATE_ALL_IMMEDIATE);
            context.getLevel().gameEvent(GameEvent.BLOCK_CHANGE, context.getClickedPos(), GameEvent.Context.of(context.getPlayer(), result));
            Block.popResourceFromFace(context.getLevel(), context.getClickedPos(), context.getClickedFace(), new ItemStack(droppedItem));
        };
    }

    @Override
    public void setToolAttributes(ItemStack tool) {
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
    }
}
