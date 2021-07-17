package com.archyx.aureliumskills.skills.foraging;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.ability.Ability;
import com.archyx.aureliumskills.api.event.LootDropCause;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.loot.LootPool;
import com.archyx.aureliumskills.loot.handler.BlockLootHandler;
import com.archyx.aureliumskills.skills.Skills;
import com.archyx.aureliumskills.source.Source;
import org.bukkit.block.Block;

public class ForagingLootHandler extends BlockLootHandler {

    public ForagingLootHandler(AureliumSkills plugin) {
        super(plugin, Skills.MINING, Ability.MINER);
    }

    @Override
    public Source getSource(Block block) {
        return ForagingSource.getSource(block);
    }

    @Override
    public double getChance(LootPool pool, PlayerData playerData) {
        return getCommonChance(pool, playerData);
    }

    @Override
    public LootDropCause getCause(LootPool pool) {
        return LootDropCause.FORAGING_OTHER_LOOT;
    }
}
