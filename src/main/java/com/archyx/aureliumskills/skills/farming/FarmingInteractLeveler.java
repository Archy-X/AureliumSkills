package com.archyx.aureliumskills.skills.farming;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.skills.Skills;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

public class FarmingInteractLeveler extends FarmingLeveler implements Listener {

    public FarmingInteractLeveler(@NotNull AureliumSkills plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRightClick(@NotNull PlayerInteractEvent event) {
        if (!OptionL.isEnabled(Skills.FARMING)) return;
        if (OptionL.getBoolean(Option.FARMING_CHECK_CANCELLED) && event.useInteractedBlock() == Event.Result.DENY) {
            return;
        }

        Player player = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;

        handleRightClick(player, block);
    }
}
