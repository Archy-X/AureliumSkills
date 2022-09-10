package com.archyx.aureliumskills.region;

import com.archyx.aureliumskills.AureliumSkills;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class RegionListener implements Listener {

    private final @NotNull AureliumSkills plugin;
    private final RegionManager regionManager;

    public RegionListener(@NotNull AureliumSkills plugin) {
        this.plugin = plugin;
        regionManager = plugin.getRegionManager();
        startSaveTimer();
    }

    @EventHandler
    public void onChunkLoad(@NotNull ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();
        int regionX = (int) Math.floor((double) chunk.getX() / 32.0);
        int regionZ = (int) Math.floor((double) chunk.getZ() / 32.0);
        RegionCoordinate regionCoordinate = new RegionCoordinate(event.getWorld(), regionX, regionZ);
        Region region = regionManager.getRegion(regionCoordinate);
        if (region == null || region.shouldReload()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Region obtainedRegion = regionManager.getRegion(regionCoordinate);
                    if (obtainedRegion == null) {
                        obtainedRegion = new Region(event.getWorld(), regionX, regionZ);
                        regionManager.setRegion(regionCoordinate, obtainedRegion);
                        regionManager.loadRegion(obtainedRegion);
                    } else if (obtainedRegion.shouldReload()) {
                        regionManager.loadRegion(obtainedRegion);
                    }
                }
            }.runTaskAsynchronously(plugin);
        }
    }

    public void startSaveTimer() {
        new BukkitRunnable() {
            @Override
            public void run() {
                regionManager.saveAllRegions(true, false);
            }
        }.runTaskTimerAsynchronously(plugin, 6000L, 6000L);
    }

}
