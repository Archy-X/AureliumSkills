package com.archyx.aureliumskills.skills.farming;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.skills.Skills;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.jetbrains.annotations.NotNull;

public class FarmingHarvestLeveler extends FarmingLeveler implements Listener {

    public FarmingHarvestLeveler(@NotNull AureliumSkills plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRightClick(@NotNull PlayerHarvestBlockEvent event) {
        if (!OptionL.isEnabled(Skills.FARMING)) return;
        if (OptionL.getBoolean(Option.FARMING_CHECK_CANCELLED) && event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getHarvestedBlock();

        handleRightClick(player, block);
    }

}
