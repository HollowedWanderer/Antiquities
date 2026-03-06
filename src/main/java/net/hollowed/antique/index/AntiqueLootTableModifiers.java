package net.hollowed.antique.index;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public class AntiqueLootTableModifiers {
    private static final Identifier TRIAL_VAULT =
            Identifier.withDefaultNamespace("chests/trial_chambers/reward_unique");

    // List of village loot tables where bundles may appear
    private static final Identifier[] VILLAGE_CHESTS = new Identifier[] {
            Identifier.withDefaultNamespace("chests/village/village_desert_house"),
            Identifier.withDefaultNamespace("chests/village/village_plains_house"),
            Identifier.withDefaultNamespace("chests/village/village_savanna_house"),
            Identifier.withDefaultNamespace("chests/village/village_snowy_house"),
            Identifier.withDefaultNamespace("chests/village/village_taiga_house"),
            Identifier.withDefaultNamespace("chests/village/village_tannery")
    };

    public static void initialize() {
        LootTableEvents.MODIFY.register((id, tableBuilder, source, registries) -> {

            // Add Hollow Core to trial vault
            if (id.identifier().equals(TRIAL_VAULT)) {
                LootPool.Builder poolBuilder = LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.2f))
                        .add(LootItem.lootTableItem(AntiqueBlocks.HOLLOW_CORE))
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 1.0f)).build());
                tableBuilder.pool(poolBuilder.build());
            }

            // Modify bundle items in village chests
            for (Identifier chestId : VILLAGE_CHESTS) {
                if (id.identifier().equals(chestId)) {
                    LootPool.Builder enchantedBundlePool = LootPool.lootPool()
                            .when(LootItemRandomChanceCondition.randomChance(0.5f)) // 50% chance to appear
                            .setRolls(ConstantValue.exactly(1))
                            .add(LootItem.lootTableItem(Items.BUNDLE)
                                    .apply(EnchantRandomlyFunction.randomApplicableEnchantment(registries))); // Random enchant

                    tableBuilder.pool(enchantedBundlePool.build());
                }
            }
        });
    }
}
