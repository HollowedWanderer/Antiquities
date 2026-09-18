package net.hollowed.antique.items;

import net.hollowed.antique.entities.MyriadShovelEntity;
import net.hollowed.antique.index.AntiqueItems;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.BlockTransformers;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class MyriadShovelBit extends MyriadToolBitItem{

//    protected static final Map<Block, Block> CHISEL_BLOCKS = new ImmutableMap.Builder<Block, Block>()
//            .put(Blocks.STONE, Blocks.STONE_BRICKS)
//            .put(Blocks.INFESTED_STONE, Blocks.INFESTED_STONE_BRICKS)
//            .put(Blocks.STONE_BRICKS, Blocks.CHISELED_STONE_BRICKS)
//            .put(Blocks.INFESTED_STONE_BRICKS, Blocks.INFESTED_CHISELED_STONE_BRICKS)
//            .put(Blocks.BAMBOO_PLANKS, Blocks.BAMBOO_MOSAIC)
//            .put(Blocks.ANDESITE, Blocks.POLISHED_ANDESITE)
//            .put(Blocks.DIORITE, Blocks.POLISHED_DIORITE)
//            .put(Blocks.GRANITE, Blocks.POLISHED_GRANITE)
//            .put(Blocks.TUFF, Blocks.POLISHED_TUFF)
//            .put(Blocks.POLISHED_TUFF, Blocks.TUFF_BRICKS)
//            .put(Blocks.TUFF_BRICKS, Blocks.CHISELED_TUFF_BRICKS)
//            .put(Blocks.DEEPSLATE, Blocks.POLISHED_DEEPSLATE)
//            .put(Blocks.POLISHED_DEEPSLATE, Blocks.CHISELED_DEEPSLATE)
//            .put(Blocks.RESIN_BRICKS, Blocks.CHISELED_RESIN_BRICKS)
//            .put(Blocks.SANDSTONE, Blocks.CHISELED_SANDSTONE)
//            .put(Blocks.RED_SANDSTONE, Blocks.CHISELED_RED_SANDSTONE)
//            .put(Blocks.NETHER_BRICKS, Blocks.CHISELED_NETHER_BRICKS)
//            .put(Blocks.BLACKSTONE, Blocks.POLISHED_BLACKSTONE)
//            .put(Blocks.POLISHED_BLACKSTONE, Blocks.CHISELED_POLISHED_BLACKSTONE)
//            .put(Blocks.QUARTZ_PILLAR, Blocks.CHISELED_QUARTZ_BLOCK)
//            .put(Blocks.COPPER_BLOCK.weathering().unaffected(), Blocks.CUT_COPPER.weathering().unaffected())
//            .put(Blocks.CUT_COPPER.weathering().unaffected(), Blocks.CHISELED_COPPER.weathering().unaffected())
//            .put(Blocks.COPPER_BLOCK.weathering().exposed(), Blocks.CUT_COPPER.weathering().exposed())
//            .put(Blocks.CUT_COPPER.weathering().exposed(), Blocks.CHISELED_COPPER.weathering().exposed())
//            .put(Blocks.COPPER_BLOCK.weathering().weathered(), Blocks.CUT_COPPER.weathering().weathered())
//            .put(Blocks.CUT_COPPER.weathering().weathered(), Blocks.CHISELED_COPPER.weathering().weathered())
//            .put(Blocks.COPPER_BLOCK.weathering().oxidized(), Blocks.CUT_COPPER.weathering().oxidized())
//            .put(Blocks.CUT_COPPER.weathering().oxidized(), Blocks.CHISELED_COPPER.weathering().oxidized())
//            .put(Blocks.COPPER_BLOCK.waxed().unaffected(), Blocks.CUT_COPPER.waxed().unaffected())
//            .put(Blocks.CUT_COPPER.waxed().unaffected(), Blocks.CHISELED_COPPER.waxed().unaffected())
//            .put(Blocks.COPPER_BLOCK.waxed().exposed(), Blocks.CUT_COPPER.waxed().exposed())
//            .put(Blocks.CUT_COPPER.waxed().exposed(), Blocks.CHISELED_COPPER.waxed().exposed())
//            .put(Blocks.COPPER_BLOCK.waxed().weathered(), Blocks.CUT_COPPER.waxed().weathered())
//            .put(Blocks.CUT_COPPER.waxed().weathered(), Blocks.CHISELED_COPPER.waxed().weathered())
//            .put(Blocks.COPPER_BLOCK.waxed().oxidized(), Blocks.CUT_COPPER.waxed().oxidized())
//            .put(Blocks.CUT_COPPER.waxed().oxidized(), Blocks.CHISELED_COPPER.waxed().oxidized())
//            .put(Blocks.PACKED_MUD, Blocks.MUD_BRICKS)
//            .put(Blocks.PRISMARINE, Blocks.PRISMARINE_BRICKS)
//            .put(Blocks.PURPUR_BLOCK, Blocks.PURPUR_PILLAR)
//            .put(Blocks.QUARTZ_BLOCK, Blocks.QUARTZ_BRICKS)
//            .put(Blocks.QUARTZ_BRICKS, Blocks.QUARTZ_PILLAR)
//            .put(Blocks.END_STONE, Blocks.END_STONE_BRICKS)
//            .build();
//
//    protected static final Map<Block, Block> CRACK_BLOCKS = new ImmutableMap.Builder<Block, Block>()
//            .put(Blocks.STONE, Blocks.COBBLESTONE)
//            .put(Blocks.STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS)
//            .put(Blocks.DEEPSLATE_BRICKS, Blocks.CRACKED_DEEPSLATE_BRICKS)
//            .put(Blocks.DEEPSLATE_TILES, Blocks.CRACKED_DEEPSLATE_TILES)
//            .put(Blocks.NETHER_BRICKS, Blocks.CRACKED_NETHER_BRICKS)
//            .put(Blocks.POLISHED_BLACKSTONE_BRICKS, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS)
//            .put(Blocks.INFESTED_STONE_BRICKS, Blocks.INFESTED_CRACKED_STONE_BRICKS)
//            .put(Blocks.INFESTED_STONE, Blocks.INFESTED_COBBLESTONE)
//            .put(Blocks.DEEPSLATE, Blocks.COBBLED_DEEPSLATE)
//            .put(Blocks.TUFF, Blocks.CHISELED_TUFF)
//            .put(Blocks.BLACKSTONE, Blocks.POLISHED_BLACKSTONE_BRICKS)
//            .build();

    public MyriadShovelBit(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult toolUse(Level world, Player user, InteractionHand hand) {
        user.startUsingItem(hand);
        return InteractionResult.PASS;
    }

    @Override
    public ItemUseAnimation toolGetUseAction(ItemStack stack) {
        return ItemUseAnimation.TRIDENT;
    }

    @Override
    public boolean toolOnStoppedUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        if (user instanceof Player playerEntity) {
            int i = stack.getUseDuration(user) - remainingUseTicks;
            if (i < 10) {
                return false;
            } else {
                float f = EnchantmentHelper.getTridentSpinAttackStrength(stack, playerEntity);
                if (f > 0.0F && !playerEntity.isInWaterOrRain()) {
                    return false;
                } else if (stack.nextDamageWillBreak()) {
                    return false;
                } else {
                    Holder<SoundEvent> registryEntry = EnchantmentHelper.pickHighestLevel(stack, EnchantmentEffectComponents.TRIDENT_SOUND)
                            .orElse(SoundEvents.TRIDENT_THROW);
                    playerEntity.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                    if (world instanceof ServerLevel serverWorld) {
                        stack.hurtWithoutBreaking(1, playerEntity);
                        if (f == 0.0F) {
                            // Create and spawn the MyriadShovelEntity at the calculated position
                            MyriadShovelEntity tridentEntity = Projectile.spawnProjectileFromRotation(MyriadShovelEntity::new, serverWorld, stack, playerEntity, 0.0F, 2.5F, 1.0F);

                            if (playerEntity.hasInfiniteMaterials()) {
                                tridentEntity.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                            } else {
                                playerEntity.getInventory().removeItem(stack);
                            }

                            world.playSound(null, tridentEntity, registryEntry.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                            return true;
                        }
                    }

                    if (f > 0.0F) {
                        float g = playerEntity.getYRot();
                        float h = playerEntity.getXRot();
                        float j = -Mth.sin(g * (float) (Math.PI / 180.0)) * Mth.cos(h * (float) (Math.PI / 180.0));
                        float k = -Mth.sin(h * (float) (Math.PI / 180.0));
                        float l = Mth.cos(g * (float) (Math.PI / 180.0)) * Mth.cos(h * (float) (Math.PI / 180.0));
                        float m = Mth.sqrt(j * j + k * k + l * l);
                        j *= f / m;
                        k *= f / m;
                        l *= f / m;
                        playerEntity.push(j, k, l);
                        playerEntity.startAutoSpinAttack(20, 8.0F, stack);
                        if (playerEntity.onGround()) {
                            playerEntity.move(MoverType.SELF, new Vec3(0.0, 1.1999999F, 0.0));
                        }

                        world.playSound(null, playerEntity, registryEntry.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void setToolAttributes(ItemStack tool, Level level) {
        tool.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 8, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -2.9, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build());
        tool.set(DataComponents.TOOL, new Tool(
                List.of(
                        Tool.Rule.deniesDrops(AntiqueItems.registryEntryLookup.getOrThrow(BlockTags.INCORRECT_FOR_IRON_TOOL)),
                        Tool.Rule.minesAndDrops(AntiqueItems.registryEntryLookup.getOrThrow(BlockTags.MINEABLE_WITH_SHOVEL), 20)
                ),
                1.0F,
                1,
                true
        ));
        tool.set(DataComponents.BLOCK_TRANSFORMER, level.registryAccess().getOrThrow(BlockTransformers.HOE));
    }
}
