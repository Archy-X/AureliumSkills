package com.archyx.aureliumskills.listeners;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.stats.Stats;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class Critical {

    private final Random r = new Random();
    private final AureliumSkills plugin;

    public Critical(AureliumSkills plugin) {
        this.plugin = plugin;
    }

    public void applyCrit(EntityDamageByEntityEvent event, Player player, PlayerData playerData) {
        if (isCrit(playerData)) {
            event.setDamage(event.getDamage() * getCritMultiplier(playerData));
            player.setMetadata("skillsCritical", new FixedMetadataValue(plugin, true));
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.removeMetadata("skillsCritical", plugin);
                }
            }.runTaskLater(plugin, 1L);
        }
    }

    private boolean isCrit(PlayerData playerData) {
        double chance = playerData.getStatLevel(Stats.CRIT_CHANCE) / 100;
        return r.nextDouble() < chance;
    }

    private double getCritMultiplier(PlayerData playerData) {
        return 1 + playerData.getStatLevel(Stats.CRIT_DAMAGE) / 100;
    }

}
