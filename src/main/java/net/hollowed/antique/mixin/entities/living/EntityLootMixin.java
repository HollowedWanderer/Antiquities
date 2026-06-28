package net.hollowed.antique.mixin.entities.living;

import net.hollowed.antique.Antiquities;
import net.hollowed.antique.index.AntiqueItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
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

    @Shadow protected abstract void dropFromLootTable(ServerLevel level, DamageSource source, boolean playerKilled);

    @Shadow protected abstract void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer);

    @Shadow protected abstract void dropExperience(ServerLevel level, @Nullable Entity killer);

    @Shadow @Nullable public abstract ItemEntity drop(ItemStack itemStack, boolean randomly, boolean thrownFromHand);

    @Shadow protected abstract boolean shouldDropLoot(ServerLevel level);

    @Inject(method = "dropAllDeathLoot", at = @At("HEAD"))
    public void drop(ServerLevel level, DamageSource source, CallbackInfo ci) {
        if (source.getEntity() != null && source.getEntity().getWeaponItem() != null
                && source.getEntity().getWeaponItem().is(AntiqueItems.MYRIAD_CLEAVER_BLADE)
        ) {
            if (!this.is(FORCE_LOOT)) {
                boolean bl = this.lastHurtByPlayerMemoryTime > 0;
                if (this.shouldDropLoot(level) && level.getGameRules().get(GameRules.MOB_DROPS)) {
                    this.dropFromLootTable(level, source, bl);
                    this.dropCustomDeathLoot(level, source, bl);
                }

                this.dropExperience(level, source.getEntity());
            }
            EntityType<?> type = this.getType();
            if (type.equals(EntityTypes.SKELETON) && Math.random() > 0.8) this.spawnAtLocation(level, Items.SKELETON_SKULL);
            if (type.equals(EntityTypes.CREEPER) && Math.random() > 0.8) this.spawnAtLocation(level, Items.CREEPER_HEAD);
            if (type.equals(EntityTypes.ZOMBIE) && Math.random() > 0.8) this.spawnAtLocation(level, Items.ZOMBIE_HEAD);
            if (type.equals(EntityTypes.WITHER_SKELETON) && Math.random() > 0.8) this.spawnAtLocation(level, Items.WITHER_SKELETON_SKULL);
            if (type.equals(EntityTypes.PIGLIN) && Math.random() > 0.8) this.spawnAtLocation(level, Items.PIGLIN_HEAD);
            if ((LivingEntity) (Object) this instanceof Player player) {
                ItemStack stack = Items.PLAYER_HEAD.getDefaultInstance();
                stack.set(DataComponents.PROFILE, ResolvableProfile.createUnresolved(player.getUUID()));
                this.drop(stack, true, false);
            }
        }
    }
}
