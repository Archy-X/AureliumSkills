package com.archyx.aureliumskills.support;

import com.archyx.aureliumskills.AureliumSkills;
import io.github.thebusybiscuit.slimefun4.api.events.BlockPlacerPlaceEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class SlimefunSupport implements Listener {

    private final AureliumSkills plugin;

    public SlimefunSupport(AureliumSkills plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlacerPlaceEvent(@NotNull BlockPlacerPlaceEvent event) {
        if (!event.isCancelled()) {
            this.plugin.getRegionManager().addPlacedBlock(event.getBlock());
        }
    }
}
