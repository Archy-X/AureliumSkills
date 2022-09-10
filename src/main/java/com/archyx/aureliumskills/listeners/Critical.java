package com.archyx.aureliumskills.listeners;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.ability.Ability;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class Critical {

    private final Random r = new Random();
    private final @NotNull AureliumSkills plugin;

    public Critical(@NotNull AureliumSkills plugin) {
        this.plugin = plugin;
    }

    public void applyCrit(@NotNull EntityDamageByEntityEvent event, @NotNull Player player, @NotNull PlayerData playerData) {
        if (plugin.getAbilityManager().isEnabled(Ability.CRIT_CHANCE)) {
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
    }

    private boolean isCrit(@NotNull PlayerData playerData) {
        return r.nextDouble() < (plugin.getAbilityManager().getValue(Ability.CRIT_CHANCE, playerData.getAbilityLevel(Ability.CRIT_CHANCE)) / 100);
    }

    private double getCritMultiplier(@NotNull PlayerData playerData) {
        if (plugin.getAbilityManager().isEnabled(Ability.CRIT_DAMAGE)) {
            double multiplier = plugin.getAbilityManager().getValue(Ability.CRIT_DAMAGE, playerData.getAbilityLevel(Ability.CRIT_DAMAGE)) / 100;
            return OptionL.getDouble(Option.CRITICAL_BASE_MULTIPLIER) * (1 + multiplier);
        }
        return OptionL.getDouble(Option.CRITICAL_BASE_MULTIPLIER);
    }

}
