package net.hollowed.antique.items.custom;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import net.hollowed.antique.Antiquities;
import net.hollowed.antique.component.ModComponents;
import net.hollowed.antique.entities.custom.MyriadShovelEntity;
import net.hollowed.antique.items.ModItems;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.ToolComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.*;
import net.minecraft.item.consume.UseAction;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class MyriadToolItem extends Item {
    protected static final Map<Block, Block> STRIPPED_BLOCKS = new ImmutableMap.Builder<Block, Block>()
            .put(Blocks.OAK_WOOD, Blocks.STRIPPED_OAK_WOOD)
            .put(Blocks.OAK_LOG, Blocks.STRIPPED_OAK_LOG)
            .put(Blocks.DARK_OAK_WOOD, Blocks.STRIPPED_DARK_OAK_WOOD)
            .put(Blocks.DARK_OAK_LOG, Blocks.STRIPPED_DARK_OAK_LOG)
            .put(Blocks.PALE_OAK_WOOD, Blocks.STRIPPED_PALE_OAK_WOOD)
            .put(Blocks.PALE_OAK_LOG, Blocks.STRIPPED_PALE_OAK_LOG)
            .put(Blocks.ACACIA_WOOD, Blocks.STRIPPED_ACACIA_WOOD)
            .put(Blocks.ACACIA_LOG, Blocks.STRIPPED_ACACIA_LOG)
            .put(Blocks.CHERRY_WOOD, Blocks.STRIPPED_CHERRY_WOOD)
            .put(Blocks.CHERRY_LOG, Blocks.STRIPPED_CHERRY_LOG)
            .put(Blocks.BIRCH_WOOD, Blocks.STRIPPED_BIRCH_WOOD)
            .put(Blocks.BIRCH_LOG, Blocks.STRIPPED_BIRCH_LOG)
            .put(Blocks.JUNGLE_WOOD, Blocks.STRIPPED_JUNGLE_WOOD)
            .put(Blocks.JUNGLE_LOG, Blocks.STRIPPED_JUNGLE_LOG)
            .put(Blocks.SPRUCE_WOOD, Blocks.STRIPPED_SPRUCE_WOOD)
            .put(Blocks.SPRUCE_LOG, Blocks.STRIPPED_SPRUCE_LOG)
            .put(Blocks.WARPED_STEM, Blocks.STRIPPED_WARPED_STEM)
            .put(Blocks.WARPED_HYPHAE, Blocks.STRIPPED_WARPED_HYPHAE)
            .put(Blocks.CRIMSON_STEM, Blocks.STRIPPED_CRIMSON_STEM)
            .put(Blocks.CRIMSON_HYPHAE, Blocks.STRIPPED_CRIMSON_HYPHAE)
            .put(Blocks.MANGROVE_WOOD, Blocks.STRIPPED_MANGROVE_WOOD)
            .put(Blocks.MANGROVE_LOG, Blocks.STRIPPED_MANGROVE_LOG)
            .put(Blocks.BAMBOO_BLOCK, Blocks.STRIPPED_BAMBOO_BLOCK)
            .build();

    protected static final Map<Block, com.mojang.datafixers.util.Pair<Predicate<ItemUsageContext>, Consumer<ItemUsageContext>>> TILLING_ACTIONS = Maps.<Block, com.mojang.datafixers.util.Pair<Predicate<ItemUsageContext>, Consumer<ItemUsageContext>>>newHashMap(
            ImmutableMap.of(
                    Blocks.GRASS_BLOCK,
                    com.mojang.datafixers.util.Pair.of(HoeItem::canTillFarmland, createTillAction(Blocks.FARMLAND.getDefaultState())),
                    Blocks.DIRT_PATH,
                    com.mojang.datafixers.util.Pair.of(HoeItem::canTillFarmland, createTillAction(Blocks.FARMLAND.getDefaultState())),
                    Blocks.DIRT,
                    com.mojang.datafixers.util.Pair.of(HoeItem::canTillFarmland, createTillAction(Blocks.FARMLAND.getDefaultState())),
                    Blocks.COARSE_DIRT,
                    com.mojang.datafixers.util.Pair.of(HoeItem::canTillFarmland, createTillAction(Blocks.DIRT.getDefaultState())),
                    Blocks.ROOTED_DIRT,
                    Pair.of(itemUsageContext -> true, createTillAndDropAction(Blocks.DIRT.getDefaultState(), Items.HANGING_ROOTS))
            )
    );

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected static final Map<Block, BlockState> PATH_STATES = Maps.<Block, BlockState>newHashMap(
            new ImmutableMap.Builder()
                    .put(Blocks.GRASS_BLOCK, Blocks.DIRT_PATH.getDefaultState())
                    .put(Blocks.DIRT, Blocks.DIRT_PATH.getDefaultState())
                    .put(Blocks.PODZOL, Blocks.DIRT_PATH.getDefaultState())
                    .put(Blocks.COARSE_DIRT, Blocks.DIRT_PATH.getDefaultState())
                    .put(Blocks.MYCELIUM, Blocks.DIRT_PATH.getDefaultState())
                    .put(Blocks.ROOTED_DIRT, Blocks.DIRT_PATH.getDefaultState())
                    .build()
    );

    public MyriadToolItem(Settings settings) {
        super(settings);
    }

    public static AttributeModifiersComponent createAttributeModifiers() {
        return AttributeModifiersComponent.builder()
                .add(EntityAttributes.ATTACK_DAMAGE, new EntityAttributeModifier(BASE_ATTACK_DAMAGE_MODIFIER_ID, 2.0, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
                .add(EntityAttributes.ATTACK_SPEED, new EntityAttributeModifier(BASE_ATTACK_SPEED_MODIFIER_ID, -2.2, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
                .add(EntityAttributes.ENTITY_INTERACTION_RANGE, new EntityAttributeModifier(Identifier.ofVanilla("base_attack_range"), 0.25, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
                .build();
    }

    @Override
    public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
        ItemStack storedStack = getStoredStack(stack);
        ItemStack otherStack = slot.getStack();
        if (clickType == ClickType.RIGHT) {
            if (otherStack.isEmpty()) {

                // Remove the internal selected stack :3
                if (!storedStack.isEmpty()) {
                    slot.setStack(storedStack.copy());
                    storedStack = ItemStack.EMPTY;
                    player.playSound(SoundEvents.ITEM_BUNDLE_REMOVE_ONE, 0.8F, 1.0F);
                    setStoredStack(stack, storedStack);
                    return true;
                }
            }
        } else {
            if (otherStack.isEmpty()) {
                return false;
            }

            // Check if the item being added is invalid
            if (isInvalidItem(otherStack)) {
                return false;
            }

            if (storedStack.isEmpty()) {
                storedStack = otherStack.split(otherStack.getCount());
                player.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 0.8F, 1.0F);
                setStoredStack(stack, storedStack); // Re-set without empty stacks

                // Clear the cursor stack after adding an item to the tool
                slot.setStack(ItemStack.EMPTY);
                return true;
            }
        }
        return super.onStackClicked(stack, slot, clickType, player);
    }

    @Override
    public boolean onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (user instanceof PlayerEntity playerEntity && Objects.requireNonNull(stack.get(ModComponents.INTEGER_PROPERTY)) == 3) {
            int i = this.getMaxUseTime(stack, user) - remainingUseTicks;
            if (i < 10) {
                return false;
            } else {
                float f = EnchantmentHelper.getTridentSpinAttackStrength(stack, playerEntity);
                if (f > 0.0F && !playerEntity.isTouchingWaterOrRain()) {
                    return false;
                } else if (stack.willBreakNextUse()) {
                    return false;
                } else {
                    RegistryEntry<SoundEvent> registryEntry = EnchantmentHelper.getEffect(stack, EnchantmentEffectComponentTypes.TRIDENT_SOUND)
                            .orElse(SoundEvents.ITEM_TRIDENT_THROW);
                    playerEntity.incrementStat(Stats.USED.getOrCreateStat(this));
                    if (world instanceof ServerWorld serverWorld) {
                        stack.damage(1, playerEntity);
                        if (f == 0.0F) {
                            float multiplier = 1.0F;
                            Vec3d look = playerEntity.getRotationVec(0).multiply(multiplier, multiplier, multiplier);

                            // Calculate a position slightly in front of the player
                            Vec3d spawnPos = playerEntity.getPos().add(0, 1.6, 0).add(look);

                            // Create and spawn the MyriadShovelEntity at the calculated position
                            MyriadShovelEntity tridentEntity = ProjectileEntity.spawnWithVelocity(MyriadShovelEntity::new, serverWorld, stack, playerEntity, 0.0F, 2.5F, 1.0F);
                            tridentEntity.setPosition(spawnPos);

                            if (playerEntity.isInCreativeMode()) {
                                tridentEntity.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
                            } else {
                                playerEntity.getInventory().removeOne(stack);
                            }

                            world.playSoundFromEntity(null, tridentEntity, registryEntry.value(), SoundCategory.PLAYERS, 1.0F, 1.0F);
                            return true;
                        }
                    }

                    if (f > 0.0F) {
                        float g = playerEntity.getYaw();
                        float h = playerEntity.getPitch();
                        float j = -MathHelper.sin(g * (float) (Math.PI / 180.0)) * MathHelper.cos(h * (float) (Math.PI / 180.0));
                        float k = -MathHelper.sin(h * (float) (Math.PI / 180.0));
                        float l = MathHelper.cos(g * (float) (Math.PI / 180.0)) * MathHelper.cos(h * (float) (Math.PI / 180.0));
                        float m = MathHelper.sqrt(j * j + k * k + l * l);
                        j *= f / m;
                        k *= f / m;
                        l *= f / m;
                        playerEntity.addVelocity((double)j, (double)k, (double)l);
                        playerEntity.useRiptide(20, 8.0F, stack);
                        if (playerEntity.isOnGround()) {
                            playerEntity.move(MovementType.SELF, new Vec3d(0.0, 1.1999999F, 0.0));
                        }

                        world.playSoundFromEntity(null, playerEntity, registryEntry.value(), SoundCategory.PLAYERS, 1.0F, 1.0F);
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        } else {
            return false;
        }
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        if (Objects.requireNonNull(stack.get(ModComponents.INTEGER_PROPERTY)) == 3) {
            return UseAction.SPEAR;
        }
        return super.getUseAction(stack);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (Objects.requireNonNull(user.getStackInHand(hand).get(ModComponents.INTEGER_PROPERTY)) == 2
                || Objects.requireNonNull(user.getStackInHand(hand).get(ModComponents.INTEGER_PROPERTY)) == 3) {
            user.setCurrentHand(hand);
            return ActionResult.PASS;
        }
        return ActionResult.FAIL;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        ItemStack storedStack = getStoredStack(stack);  // Create a mutable copy of the list
        if (clickType == ClickType.RIGHT) {
            if (otherStack.isEmpty()) {

                // Remove the internal selected stack :3
                if (!storedStack.isEmpty()) {
                    cursorStackReference.set(storedStack.copy());
                    storedStack = ItemStack.EMPTY;
                    player.playSound(SoundEvents.ITEM_BUNDLE_REMOVE_ONE, 1.0F, 1.0F);
                    setStoredStack(stack, storedStack); // Re-set without empty stacks
                    return true;
                }
            }
        } else {
            if (cursorStackReference.get().isEmpty()) {
                return false;
            }

            // Check if the item being added is invalid
            if (isInvalidItem(otherStack)) {
                return false;
            }

            ItemStack temp = getStoredStack(stack);
            storedStack = otherStack.split(otherStack.getCount());
            player.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 1.0F, 1.0F);
            setStoredStack(stack, storedStack); // Re-set without empty stacks

            // Clear the cursor stack after adding an item to the tool
            cursorStackReference.set(temp);
            return true;
        }
        return super.onClicked(stack, otherStack, slot, clickType, player, cursorStackReference);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (stack.get(ModComponents.INTEGER_PROPERTY) != null) {
            int toolInt = Objects.requireNonNull(stack.get(ModComponents.INTEGER_PROPERTY));

            assert Formatting.GRAY.getColorValue() != null;
            Text line = Text.translatable("item.antique.myriad_tool.no_tool").withColor(Formatting.GRAY.getColorValue());

            switch (toolInt) {
                case 1 -> line = Text.translatable("item.antique.myriad_tool.mattock").withColor(Formatting.GRAY.getColorValue());
                case 2 -> line = Text.translatable("item.antique.myriad_tool.axe").withColor(Formatting.GRAY.getColorValue());
                case 3 -> line = Text.translatable("item.antique.myriad_tool.shovel").withColor(Formatting.GRAY.getColorValue());
            }

            // Add to tooltip
            tooltip.add(line);
        }
        super.appendTooltip(stack, context, tooltip, type);
    }

    public static boolean isInvalidItem(ItemStack stack) {
        Item item = stack.getItem();
        return !(item instanceof MyriadToolBitItem);
    }

    public static ItemStack getStoredStack(ItemStack tool) {
        return tool.get(ModComponents.MYRIAD_STACK);
    }

    public static void setStoredStack(ItemStack tool, ItemStack newStack) {
        if (newStack.getItem() instanceof MyriadToolBitItem item) {
            tool.set(ModComponents.INTEGER_PROPERTY, item.getId());
            switch (item.getId()) {
                case 1 -> {
                    tool.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.builder()
                            .add(EntityAttributes.ATTACK_DAMAGE, new EntityAttributeModifier(BASE_ATTACK_DAMAGE_MODIFIER_ID, 5.0, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
                            .add(EntityAttributes.ATTACK_SPEED, new EntityAttributeModifier(BASE_ATTACK_SPEED_MODIFIER_ID, -2.4, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
                            .add(EntityAttributes.ENTITY_INTERACTION_RANGE, new EntityAttributeModifier(Identifier.ofVanilla("base_attack_range"), 0.75, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
                            .build());
                    tool.set(DataComponentTypes.TOOL, new ToolComponent(
                            List.of(
                                    ToolComponent.Rule.ofNeverDropping(ModItems.registryEntryLookup.getOrThrow(BlockTags.INCORRECT_FOR_IRON_TOOL)),
                                    ToolComponent.Rule.ofAlwaysDropping(ModItems.registryEntryLookup.getOrThrow(TagKey.of(RegistryKeys.BLOCK, Identifier.of(Antiquities.MOD_ID, "mineable/mattock"))), 20)
                            ),
                            1.0F,
                            1
                    ));
                }
                case 2 -> {
                    tool.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.builder()
                            .add(EntityAttributes.ATTACK_DAMAGE, new EntityAttributeModifier(BASE_ATTACK_DAMAGE_MODIFIER_ID, 9.0, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
                            .add(EntityAttributes.ATTACK_SPEED, new EntityAttributeModifier(BASE_ATTACK_SPEED_MODIFIER_ID, -3, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
                            .add(EntityAttributes.ENTITY_INTERACTION_RANGE, new EntityAttributeModifier(Identifier.ofVanilla("base_attack_range"), 0.75, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
                            .build());
                    tool.set(DataComponentTypes.TOOL, new ToolComponent(
                            List.of(
                                    ToolComponent.Rule.ofNeverDropping(ModItems.registryEntryLookup.getOrThrow(BlockTags.INCORRECT_FOR_IRON_TOOL)),
                                    ToolComponent.Rule.ofAlwaysDropping(ModItems.registryEntryLookup.getOrThrow(BlockTags.AXE_MINEABLE), 20)
                            ),
                            1.0F,
                            1
                    ));
                }
                case 3 -> {
                    tool.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.builder()
                            .add(EntityAttributes.ATTACK_DAMAGE, new EntityAttributeModifier(BASE_ATTACK_DAMAGE_MODIFIER_ID, 8, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
                            .add(EntityAttributes.ATTACK_SPEED, new EntityAttributeModifier(BASE_ATTACK_SPEED_MODIFIER_ID, -2.9, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
                            .add(EntityAttributes.ENTITY_INTERACTION_RANGE, new EntityAttributeModifier(Identifier.ofVanilla("base_attack_range"), 0.75, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
                            .build());
                    tool.set(DataComponentTypes.TOOL, new ToolComponent(
                            List.of(
                                    ToolComponent.Rule.ofNeverDropping(ModItems.registryEntryLookup.getOrThrow(BlockTags.INCORRECT_FOR_IRON_TOOL)),
                                    ToolComponent.Rule.ofAlwaysDropping(ModItems.registryEntryLookup.getOrThrow(BlockTags.SHOVEL_MINEABLE), 20)
                            ),
                            1.0F,
                            1
                    ));
                }
            }
        } else {
            tool.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.builder()
                    .add(EntityAttributes.ATTACK_DAMAGE, new EntityAttributeModifier(BASE_ATTACK_DAMAGE_MODIFIER_ID, 2.0, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
                    .add(EntityAttributes.ATTACK_SPEED, new EntityAttributeModifier(BASE_ATTACK_SPEED_MODIFIER_ID, -2.2, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
                    .add(EntityAttributes.ENTITY_INTERACTION_RANGE, new EntityAttributeModifier(Identifier.ofVanilla("base_attack_range"), 0.25, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
                    .build());
            tool.set(ModComponents.INTEGER_PROPERTY, 0);
            tool.remove(DataComponentTypes.TOOL);
        }
        tool.set(ModComponents.MYRIAD_STACK, newStack);
    }

    /*

        Tool Functionality Code

     */

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        PlayerEntity playerEntity = context.getPlayer();

        if (context.getStack().get(ModComponents.INTEGER_PROPERTY) != null && Objects.requireNonNull(context.getStack().get(ModComponents.INTEGER_PROPERTY)) == 2) {
            if (shouldCancelStripAttempt(context)) {
                return ActionResult.PASS;
            } else {
                Optional<BlockState> optional = this.tryStrip(world, blockPos, playerEntity, world.getBlockState(blockPos));
                if (optional.isEmpty()) {
                    return ActionResult.PASS;
                } else {
                    ItemStack itemStack = context.getStack();
                    if (playerEntity instanceof ServerPlayerEntity) {
                        Criteria.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity) playerEntity, blockPos, itemStack);
                    }

                    world.setBlockState(blockPos, optional.get(), Block.NOTIFY_ALL_AND_REDRAW);
                    world.emitGameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Emitter.of(playerEntity, (BlockState) optional.get()));
                    if (playerEntity != null) {
                        itemStack.damage(1, playerEntity, LivingEntity.getSlotForHand(context.getHand()));
                    }

                    return ActionResult.SUCCESS;
                }
            }
        } else if (context.getStack().get(ModComponents.INTEGER_PROPERTY) != null && Objects.requireNonNull(context.getStack().get(ModComponents.INTEGER_PROPERTY)) == 1) {
            Pair<Predicate<ItemUsageContext>, Consumer<ItemUsageContext>> pair = TILLING_ACTIONS.get(
                    world.getBlockState(blockPos).getBlock()
            );
            if (pair == null) {
                return ActionResult.PASS;
            } else {
                Predicate<ItemUsageContext> predicate = pair.getFirst();
                Consumer<ItemUsageContext> consumer = pair.getSecond();
                if (predicate.test(context)) {
                    world.playSound(playerEntity, blockPos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    if (!world.isClient) {
                        consumer.accept(context);
                        if (playerEntity != null) {
                            context.getStack().damage(1, playerEntity, LivingEntity.getSlotForHand(context.getHand()));
                        }
                    }

                    return ActionResult.SUCCESS;
                } else {
                    return ActionResult.PASS;
                }
            }
        } else {
            if (context.getStack().get(ModComponents.INTEGER_PROPERTY) != null && Objects.requireNonNull(context.getStack().get(ModComponents.INTEGER_PROPERTY)) == 3) {
                assert playerEntity != null;
                if (playerEntity.isSneaking()) {
                    BlockState blockState = world.getBlockState(blockPos);
                    if (context.getSide() == Direction.DOWN) {
                        return ActionResult.PASS;
                    } else {
                        BlockState blockState2 = PATH_STATES.get(blockState.getBlock());
                        BlockState blockState3 = null;
                        if (blockState2 != null && world.getBlockState(blockPos.up()).isAir()) {
                            world.playSound(playerEntity, blockPos, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS, 1.0F, 1.0F);
                            blockState3 = blockState2;
                        } else if (blockState.getBlock() instanceof CampfireBlock && blockState.get(CampfireBlock.LIT)) {
                            if (!world.isClient()) {
                                world.syncWorldEvent(null, WorldEvents.FIRE_EXTINGUISHED, blockPos, 0);
                            }

                            CampfireBlock.extinguish(context.getPlayer(), world, blockPos, blockState);
                            blockState3 = blockState.with(CampfireBlock.LIT, Boolean.FALSE);
                        }

                        if (blockState3 != null) {
                            if (!world.isClient) {
                                world.setBlockState(blockPos, blockState3, Block.NOTIFY_ALL_AND_REDRAW);
                                world.emitGameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Emitter.of(playerEntity, blockState3));
                                context.getStack().damage(1, playerEntity, LivingEntity.getSlotForHand(context.getHand()));
                            }

                            return ActionResult.SUCCESS;
                        } else {
                            return ActionResult.PASS;
                        }
                    }
                }
                return ActionResult.PASS;
            }
        }
        return ActionResult.FAIL;
    }

    private static boolean shouldCancelStripAttempt(ItemUsageContext context) {
        PlayerEntity playerEntity = context.getPlayer();
        if (!context.getHand().equals(Hand.MAIN_HAND)) return false;
        assert playerEntity != null;
        return playerEntity.getOffHandStack().isOf(Items.SHIELD) && !playerEntity.shouldCancelInteraction();
    }

    private Optional<BlockState> tryStrip(World world, BlockPos pos, @Nullable PlayerEntity player, BlockState state) {
        Optional<BlockState> optional = this.getStrippedState(state);
        if (optional.isPresent()) {
            world.playSound(player, pos, SoundEvents.ITEM_AXE_STRIP, SoundCategory.BLOCKS, 1.0F, 1.0F);
            return optional;
        } else {
            Optional<BlockState> optional2 = Oxidizable.getDecreasedOxidationState(state);
            if (optional2.isPresent()) {
                world.playSound(player, pos, SoundEvents.ITEM_AXE_SCRAPE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                world.syncWorldEvent(player, WorldEvents.BLOCK_SCRAPED, pos, 0);
                return optional2;
            } else {
                Optional<BlockState> optional3 = Optional.ofNullable((Block)((BiMap<?, ?>)HoneycombItem.WAXED_TO_UNWAXED_BLOCKS.get()).get(state.getBlock()))
                        .map(block -> block.getStateWithProperties(state));
                if (optional3.isPresent()) {
                    world.playSound(player, pos, SoundEvents.ITEM_AXE_WAX_OFF, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    world.syncWorldEvent(player, WorldEvents.WAX_REMOVED, pos, 0);
                    return optional3;
                } else {
                    return Optional.empty();
                }
            }
        }
    }

    private Optional<BlockState> getStrippedState(BlockState state) {
        return Optional.ofNullable(STRIPPED_BLOCKS.get(state.getBlock()))
                .map(block -> block.getDefaultState().with(PillarBlock.AXIS, state.get(PillarBlock.AXIS)));
    }

    public static Consumer<ItemUsageContext> createTillAction(BlockState result) {
        return context -> {
            context.getWorld().setBlockState(context.getBlockPos(), result, Block.NOTIFY_ALL_AND_REDRAW);
            context.getWorld().emitGameEvent(GameEvent.BLOCK_CHANGE, context.getBlockPos(), GameEvent.Emitter.of(context.getPlayer(), result));
        };
    }

    public static Consumer<ItemUsageContext> createTillAndDropAction(BlockState result, ItemConvertible droppedItem) {
        return context -> {
            context.getWorld().setBlockState(context.getBlockPos(), result, Block.NOTIFY_ALL_AND_REDRAW);
            context.getWorld().emitGameEvent(GameEvent.BLOCK_CHANGE, context.getBlockPos(), GameEvent.Emitter.of(context.getPlayer(), result));
            Block.dropStack(context.getWorld(), context.getBlockPos(), context.getSide(), new ItemStack(droppedItem));
        };
    }
}