package com.archyx.aureliumskills.mana;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.api.event.ManaRegenerateEvent;
import com.archyx.aureliumskills.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ManaManager implements Listener {

    private final @NotNull AureliumSkills plugin;

    public ManaManager(@NotNull AureliumSkills plugin) {
        this.plugin = plugin;
    }

    /**
     * Start regenerating Mana
     */
    public void startRegen() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
                    if (playerData != null) {
                        double originalMana = playerData.getMana();
                        double maxMana = playerData.getMaxMana();
                        if (originalMana < maxMana) {
                            if (!playerData.getAbilityData(MAbility.ABSORPTION).getBoolean("activated")) {
                                double regen = playerData.getManaRegen();
                                double finalRegen = Math.min(originalMana + regen, maxMana) - originalMana;
                                ManaRegenerateEvent event = new ManaRegenerateEvent(player, finalRegen);
                                Bukkit.getPluginManager().callEvent(event);
                                if (!event.isCancelled()) {
                                    playerData.setMana(originalMana + event.getAmount());
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

}
