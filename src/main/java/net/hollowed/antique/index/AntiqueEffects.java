package net.hollowed.antique.index;

import net.hollowed.antique.Antiquities;
import net.hollowed.antique.effect.AnimeEffect;
import net.hollowed.antique.effect.BounceEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public interface AntiqueEffects {

    Holder<MobEffect> VOLATILE_BOUNCE_EFFECT = registerEffect("volatile_bouncy", new BounceEffect().addAttributeModifier(Attributes.STEP_HEIGHT, Identifier.withDefaultNamespace("effect.step_height"), 1, AttributeModifier.Operation.ADD_VALUE));
    Holder<MobEffect> BOUNCE_EFFECT = registerEffect("bouncy", new BounceEffect().addAttributeModifier(Attributes.STEP_HEIGHT, Identifier.withDefaultNamespace("effect.step_height"), 1, AttributeModifier.Operation.ADD_VALUE));
    Holder<MobEffect> ANIME_EFFECT = registerEffect("anime_effect", new AnimeEffect());

    static Holder<MobEffect> registerEffect(String id, MobEffect statusEffect) {
        return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Antiquities.id(id), statusEffect);
    }

    static void initialize() {}
}
