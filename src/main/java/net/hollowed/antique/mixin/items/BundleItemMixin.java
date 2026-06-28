package net.hollowed.antique.mixin.items;

import net.hollowed.antique.enchantments.EnchantmentListener;
import net.hollowed.antique.index.AntiqueDataComponentTypes;
import net.hollowed.antique.index.AntiqueEnchantments;
import net.hollowed.antique.index.AntiqueItems;
import net.hollowed.antique.items.MyriadToolBitItem;
import net.hollowed.antique.items.MyriadToolItem;
import net.hollowed.antique.items.SatchelItem;
import net.hollowed.antique.items.components.MyriadToolComponent;
import net.hollowed.antique.mixin.accessors.MinecartItemAccessor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.FireChargeItem;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ThrowablePotionItem;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(BundleItem.class)
public abstract class BundleItemMixin extends Item {

    public BundleItemMixin(Properties settings) {
        super(settings);
    }

    @Inject(method = "overrideStackedOnOther", at = @At("HEAD"), cancellable = true)
    private void preventSatchelsOnStackClicked(ItemStack self, Slot slot, ClickAction clickAction, Player player, CallbackInfoReturnable<Boolean> cir) {
        ItemStack itemStack = slot.getItem();
        if (itemStack.getItem() instanceof SatchelItem) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "overrideOtherStackedOnMe", at = @At("HEAD"), cancellable = true)
    private void preventSatchelsOnClicked(ItemStack self, ItemStack other, Slot slot, ClickAction clickAction, Player player, SlotAccess carriedItem, CallbackInfoReturnable<Boolean> cir) {
        if (other.getItem() instanceof SatchelItem) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "overrideOtherStackedOnMe", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 1), cancellable = true)
    private void myriadToolCompatibleSelectable(ItemStack self, ItemStack other, Slot slot, ClickAction clickAction, Player player, SlotAccess carriedItem, CallbackInfoReturnable<Boolean> cir) {
        if (other.is(AntiqueItems.MYRIAD_TOOL)) {
            BundleContents bundleContents = self.get(DataComponents.BUNDLE_CONTENTS);
            if (bundleContents != null) {
                BundleContents.Mutable mutable = new BundleContents.Mutable(bundleContents);
                MyriadToolComponent toolComponent = other.get(AntiqueDataComponentTypes.MYRIAD_TOOL);
                if (toolComponent != null) {
                    ItemStack replacementStack = toolComponent.toolBit();
                    if (bundleContents.getSelectedItemIndex() != -1 && bundleContents.itemCopyStream().toList().get(bundleContents.getSelectedItemIndex()).getItem() instanceof MyriadToolBitItem) {
                        ItemStack toolBitStack = mutable.removeOne();

                        if (toolBitStack != null) {
                            MyriadToolItem.setToolBit(other, toolBitStack);
                            playRemoveOneSound(player);
                            if (!replacementStack.isEmpty() && !(slot.allowModification(player) && mutable.tryInsert(replacementStack) > 0)) {
                                playInsertFailSound(player);
                            }
                        }

                        self.set(DataComponents.BUNDLE_CONTENTS, mutable.toImmutable());
                        this.broadcastChangesOnContainerMenu(player);
                        cir.setReturnValue(true);
                    } else if (!replacementStack.isEmpty()) {
                        if (slot.allowModification(player) && mutable.tryInsert(replacementStack) > 0) {
                            MyriadToolItem.setToolBit(other, ItemStack.EMPTY);
                            playInsertSound(player);
                        } else {
                            playInsertFailSound(player);
                        }

                        self.set(DataComponents.BUNDLE_CONTENTS, mutable.toImmutable());
                        this.broadcastChangesOnContainerMenu(player);
                        cir.setReturnValue(true);
                    }
                }
            }
        }
    }

    @Shadow
    private static Optional<ItemStack> removeOneItemFromBundle(ItemStack self, Player player, BundleContents initialContents) {
        return Optional.empty();
    }

    @Shadow protected abstract void broadcastChangesOnContainerMenu(Player player);

    @Shadow
    private static void playInsertSound(Entity entity) {}

    @Shadow
    private static void playInsertFailSound(Entity entity) {}

    @Shadow
    private static void playRemoveOneSound(Entity entity) {}

    @Inject(method = "overrideStackedOnOther", at = @At("RETURN"))
    public void onStackClicked(ItemStack self, Slot slot, ClickAction clickAction, Player player, CallbackInfoReturnable<Boolean> cir) {
        if (EnchantmentListener.hasEnchantment(self, AntiqueEnchantments.CURSE_OF_VOIDING.identifier().toString())) {
            BundleContents bundleContentsComponent = self.get(DataComponents.BUNDLE_CONTENTS);
            if (bundleContentsComponent == null) return;

            BundleContents.Mutable builder = new BundleContents.Mutable(bundleContentsComponent);
            ItemStack itemStack = builder.removeOne();

            if (itemStack != null) {
                float xpToGrant = calculateXpFromStackSize(itemStack.getCount(), itemStack) / 2;
                if (xpToGrant > 0) {
                    spawnXpOrb(player, (int) xpToGrant + 1);
                }
            }

            builder.removeOne();
            self.set(DataComponents.BUNDLE_CONTENTS, builder.toImmutable());
        }
    }

    @Inject(method = "overrideOtherStackedOnMe", at = @At("RETURN"))
    public void onClicked(ItemStack self, ItemStack other, Slot slot, ClickAction clickAction, Player player, SlotAccess carriedItem, CallbackInfoReturnable<Boolean> cir) {
        if (EnchantmentListener.hasEnchantment(self, AntiqueEnchantments.CURSE_OF_VOIDING.identifier().toString())) {
            BundleContents bundleContentsComponent = self.get(DataComponents.BUNDLE_CONTENTS);
            if (bundleContentsComponent == null) return;

            BundleContents.Mutable builder = new BundleContents.Mutable(bundleContentsComponent);
            ItemStack itemStack = builder.removeOne();

            if (itemStack != null) {
                float xpToGrant = calculateXpFromStackSize(itemStack.getCount(), itemStack) / 2;
                if (xpToGrant > 0) {
                    spawnXpOrb(player, (int) xpToGrant + 1);
                }
            }

            builder.removeOne();
            self.set(DataComponents.BUNDLE_CONTENTS, builder.toImmutable());
        }
    }

    @Unique
    private float calculateXpFromStackSize(int stackSize, ItemStack stack) {
        float xp = Math.max(1f, stackSize);

        switch (stack.getRarity()) {
            case Rarity.EPIC -> xp *= 512;
            case Rarity.RARE -> xp *= 256;
            case Rarity.UNCOMMON -> xp *= 128;
            default -> {}
        }

        return xp;
    }

    @Unique
    private void spawnXpOrb(Player player, int xpAmount) {
        if (xpAmount > 0 && player.level() instanceof ServerLevel) {
            ExperienceOrb xpOrb = new ExperienceOrb(player.level(), player.getX(), player.getY(), player.getZ(), xpAmount);
            player.level().addFreshEntity(xpOrb);
        }
    }

    @Inject(method = "removeOneItemFromBundle", at = @At("HEAD"), cancellable = true)
    private static void popFirstBundledStackInject(ItemStack self, Player player, BundleContents initialContents, CallbackInfoReturnable<Optional<ItemStack>> cir) {

        boolean hasProjectingEnchantment = EnchantmentListener.hasEnchantment(self, AntiqueEnchantments.PROJECTING.identifier().toString());

        BundleContents.Mutable builder = new BundleContents.Mutable(initialContents);
        ItemStack itemStack = builder.removeOne();

        if (hasProjectingEnchantment && itemStack != null) {
            cir.setReturnValue(Optional.of(itemStack));

            boolean hasFlintAndSteel = player.getMainHandItem().getItem() instanceof FlintAndSteelItem ||
                    player.getOffhandItem().getItem() instanceof FlintAndSteelItem ||
                    player.getMainHandItem().getItem() instanceof FireChargeItem ||
                    player.getOffhandItem().getItem() instanceof FireChargeItem;

            boolean isMainhand = player.getMainHandItem().getItem() instanceof FlintAndSteelItem ||
                    player.getMainHandItem().getItem() instanceof FireChargeItem;

            switch (itemStack.getItem()) {
                case BlockItem blockItem when blockItem.getBlock() instanceof TntBlock -> {
                    if (hasFlintAndSteel || player.isCreative()) {
                        itemStack.shrink(1);
                        builder.tryInsert(itemStack);
                        ItemStack item = isMainhand ? player.getMainHandItem() : player.getOffhandItem();

                        if (item.getItem() instanceof FlintAndSteelItem) {
                            item.hurtWithoutBreaking(1, player);
                        } else {
                            item.shrink(1);
                        }

                        PrimedTnt tntEntity = new PrimedTnt(player.level(), player.getX(), player.getY() + 0.75, player.getZ(), player);
                        tntEntity.setFuse(40);

                        Vec3 forward = player.getViewVector(1.5F);
                        tntEntity.setDeltaMovement(forward);

                        player.level().addFreshEntity(tntEntity);
                        player.level().playSound(null, player.blockPosition(), SoundEvents.WITCH_THROW, SoundSource.PLAYERS, 0.8F, 1.0F);
                        player.level().playSound(null, player.blockPosition(), SoundEvents.BUNDLE_DROP_CONTENTS, SoundSource.PLAYERS, 0.8F, 1.0F);
                        player.level().playSound(null, player.blockPosition(), SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS, 0.8F, 1.0F);

                        player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
                        if (!player.isCreative()) {
                            self.set(DataComponents.BUNDLE_CONTENTS, builder.toImmutable());
                        }
                    } else {
                        self.set(DataComponents.BUNDLE_CONTENTS, builder.toImmutable());
                    }
                }
                case MinecartItem ignored -> {

                    boolean takeItem = true;

                    if (player.level() instanceof ServerLevel) {

                        EntityType<? extends AbstractMinecart> minecartType = ((MinecartItemAccessor) itemStack.getItem()).getType();

                        Vec3 forward = player.getLookAngle().normalize();
                        double offsetDistance = 1.5;
                        Vec3 offsetPos = player.position().add(forward.scale(offsetDistance)).add(0, 1, 0);

                        if (player.level().noCollision(minecartType.create(player.level(), EntitySpawnReason.DISPENSER), new AABB(offsetPos.x - 0.25, offsetPos.y, offsetPos.z - 0.25, offsetPos.x + 0.25, offsetPos.y + 0.5, offsetPos.z + 0.25))) {

                            AbstractMinecart abstractMinecartEntity = AbstractMinecart.createMinecart(
                                    player.level(),
                                    offsetPos.x, offsetPos.y, offsetPos.z,
                                    minecartType, EntitySpawnReason.DISPENSER, itemStack, player
                            );

                            if (abstractMinecartEntity == null) return;
                            abstractMinecartEntity.setDeltaMovement(forward.add(player.getDeltaMovement()));
                            player.level().addFreshEntity(abstractMinecartEntity);
                            player.level().playSound(null, player.blockPosition(), SoundEvents.WITCH_THROW, SoundSource.PLAYERS, 0.8F, 1.0F);
                            player.level().playSound(null, player.blockPosition(), SoundEvents.BUNDLE_DROP_CONTENTS, SoundSource.PLAYERS, 0.8F, 1.0F);

                            itemStack.shrink(1);
                            builder.tryInsert(itemStack);
                        } else {
                            takeItem = false;
                            player.sendOverlayMessage(Component.translatable("item.antique.bundle_minecart"));
                            cir.setReturnValue(Optional.empty());
                        }
                        player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
                        if (!player.isCreative() && takeItem) {
                            self.set(DataComponents.BUNDLE_CONTENTS, builder.toImmutable());
                        }
                    }
                }
                case ThrowablePotionItem ignored -> {
                    if (!player.isCreative()) {
                        self.set(DataComponents.BUNDLE_CONTENTS, builder.toImmutable());
                    }
                }
                default -> self.set(DataComponents.BUNDLE_CONTENTS, builder.toImmutable());
            }
        }
    }

    @Inject(method = "dropContent(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;)Z", at = @At("HEAD"), cancellable = true)
    private void projectingDropStack(ItemStack bundle, Player player, CallbackInfoReturnable<Boolean> cir) {
        if (EnchantmentListener.hasEnchantment(bundle, AntiqueEnchantments.PROJECTING.identifier().toString())) {
            BundleContents bundleContentsComponent = bundle.get(DataComponents.BUNDLE_CONTENTS);
            if (bundleContentsComponent != null && !bundleContentsComponent.isEmpty()) {
                Optional<ItemStack> optional = removeOneItemFromBundle(bundle, player, bundleContentsComponent);
                if (optional.isPresent()) {
                    cir.setReturnValue(true);
                    if (optional.get().getItem() instanceof ThrowablePotionItem) {
                        handlePotionThrow(player, optional.get());
                    } else {
                        player.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
                        ItemEntity entity = player.drop(optional.get(), true);
                        if (entity != null) {
                            entity.setDeltaMovement(entity.getDeltaMovement().scale(2));
                            entity.hurtMarked = true;
                        }
                    }
                } else {
                    cir.setReturnValue(false);
                }
            }
            cir.setReturnValue(false);
        }
    }

    @Unique
    private void handlePotionThrow(Player player, ItemStack itemStack) {
        if (player.level() instanceof ServerLevel) {
            ThrownSplashPotion potionEntity = new ThrownSplashPotion(player.level(), player, itemStack);
            potionEntity.setPos(player.position().add(0, 1.5, 0));
            potionEntity.setDeltaMovement(player.getLookAngle().add(0, 0.25, 0));
            player.level().addFreshEntity(potionEntity);
            player.level().playSound(null, player.blockPosition(), SoundEvents.WITCH_THROW, SoundSource.PLAYERS, 0.8F, 1.0F);
            player.level().playSound(null, player.blockPosition(), SoundEvents.BUNDLE_DROP_CONTENTS, SoundSource.PLAYERS, 0.8F, 1.0F);
        }
        player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
    }
}