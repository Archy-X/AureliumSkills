package com.archyx.aureliumskills.skills.foraging;

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

public class ForagingLootHandler extends BlockLootHandler {

    public ForagingLootHandler(@NotNull AureliumSkills plugin) {
        super(plugin, Skills.FORAGING, Ability.FORAGER);
    }

    @Override
    public @Nullable Source getSource(@NotNull Block block) {
        return ForagingSource.getSource(block);
    }

    @Override
    public double getChance(@NotNull LootPool pool, @NotNull PlayerData playerData) {
        return getCommonChance(pool, playerData);
    }

    @Override
    public @NotNull LootDropCause getCause(@NotNull LootPool pool) {
        return LootDropCause.FORAGING_OTHER_LOOT;
    }
}
