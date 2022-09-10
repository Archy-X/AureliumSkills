package com.archyx.aureliumskills.skills.excavation;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.ability.Ability;
import com.archyx.aureliumskills.api.event.LootDropCause;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.loot.handler.BlockLootHandler;
import com.archyx.aureliumskills.skills.Skills;
import com.archyx.aureliumskills.source.Source;
import com.archyx.lootmanager.loot.LootPool;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExcavationLootHandler extends BlockLootHandler {

    public ExcavationLootHandler(@NotNull AureliumSkills plugin) {
        super(plugin, Skills.EXCAVATION, Ability.EXCAVATOR);
    }

    @Override
    public @Nullable Source getSource(@NotNull Block block) {
        return ExcavationSource.getSource(block);
    }

    @Override
    public double getChance(@NotNull LootPool pool, @NotNull PlayerData playerData) {
        double chance = getCommonChance(pool, playerData);
        if (pool.getName().equals("rare") && plugin.getAbilityManager().isEnabled(Ability.METAL_DETECTOR)) {
            chance += (getValue(Ability.METAL_DETECTOR, playerData) / 100);
        } else if (pool.getName().equals("epic") && plugin.getAbilityManager().isEnabled(Ability.LUCKY_SPADES)) {
            chance += (getValue(Ability.LUCKY_SPADES, playerData) / 100);
        }
        return chance;
    }

    @Override
    public @NotNull LootDropCause getCause(@NotNull LootPool pool) {
        LootDropCause cause;
        if (pool.getName().equals("rare") && plugin.getAbilityManager().isEnabled(Ability.METAL_DETECTOR)) {
            cause = LootDropCause.METAL_DETECTOR;
        } else if (pool.getName().equals("epic") && plugin.getAbilityManager().isEnabled(Ability.LUCKY_SPADES)) {
            cause = LootDropCause.LUCKY_SPADES;
        } else {
            cause = LootDropCause.EXCAVATION_OTHER_LOOT;
        }
        return cause;
    }

}
