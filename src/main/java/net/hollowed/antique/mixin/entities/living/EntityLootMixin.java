package net.hollowed.antique.mixin.entities.living;

import net.hollowed.antique.Antiquities;
import net.hollowed.antique.index.AntiqueItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class EntityLootMixin extends Entity {

    @Unique
    private static final TagKey<EntityType<?>> FORCE_LOOT = TagKey.create(BuiltInRegistries.ENTITY_TYPE.key(), Antiquities.id("force_loot"));

    @Shadow protected int lastHurtByPlayerMemoryTime;

    public EntityLootMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Shadow protected abstract void dropFromLootTable(ServerLevel world, DamageSource damageSource, boolean causedByPlayer);

    @Shadow protected abstract void dropCustomDeathLoot(ServerLevel world, DamageSource source, boolean causedByPlayer);

    @Shadow protected abstract void dropExperience(ServerLevel world, @Nullable Entity attacker);

    @Shadow @Nullable public abstract ItemEntity drop(ItemStack stack, boolean dropAtSelf, boolean retainOwnership);

    @Shadow protected abstract boolean shouldDropLoot(ServerLevel world);

    @Inject(method = "dropAllDeathLoot", at = @At("HEAD"))
    public void drop(ServerLevel world, DamageSource damageSource, CallbackInfo ci) {
        if (damageSource.getEntity() != null && damageSource.getEntity().getWeaponItem() != null
                && damageSource.getEntity().getWeaponItem().is(AntiqueItems.MYRIAD_CLEAVER_BLADE)
        ) {
            if (!this.getType().is(FORCE_LOOT)) {
                boolean bl = this.lastHurtByPlayerMemoryTime > 0;
                if (this.shouldDropLoot(world) && world.getGameRules().get(GameRules.MOB_DROPS)) {
                    this.dropFromLootTable(world, damageSource, bl);
                    this.dropCustomDeathLoot(world, damageSource, bl);
                }

                this.dropExperience(world, damageSource.getEntity());
            }
            EntityType<?> type = this.getType();
            if (type.equals(EntityType.SKELETON) && Math.random() > 0.8) this.spawnAtLocation(world, Items.SKELETON_SKULL);
            if (type.equals(EntityType.CREEPER) && Math.random() > 0.8) this.spawnAtLocation(world, Items.CREEPER_HEAD);
            if (type.equals(EntityType.ZOMBIE) && Math.random() > 0.8) this.spawnAtLocation(world, Items.ZOMBIE_HEAD);
            if (type.equals(EntityType.WITHER_SKELETON) && Math.random() > 0.8) this.spawnAtLocation(world, Items.WITHER_SKELETON_SKULL);
            if (type.equals(EntityType.PIGLIN) && Math.random() > 0.8) this.spawnAtLocation(world, Items.PIGLIN_HEAD);
            if ((LivingEntity) (Object) this instanceof Player player) {
                ItemStack stack = Items.PLAYER_HEAD.getDefaultInstance();
                stack.set(DataComponents.PROFILE, ResolvableProfile.createUnresolved(player.getUUID()));
                this.drop(stack, true, false);
            }
        }
    }
}
