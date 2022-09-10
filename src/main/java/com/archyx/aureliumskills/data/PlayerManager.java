package com.archyx.aureliumskills.data;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerManager {

    private final AureliumSkills plugin;
    private final ConcurrentHashMap<UUID, PlayerData> playerData;

    public PlayerManager(AureliumSkills plugin) {
        this.plugin = plugin;
        this.playerData = new ConcurrentHashMap<>();
        if (OptionL.getBoolean(Option.AUTO_SAVE_ENABLED)) {
            startAutoSave();
        }
    }

    public PlayerData getPlayerData(Player player) {
        return playerData.get(player.getUniqueId());
    }

    public PlayerData getPlayerData(UUID id) {
        return this.playerData.get(id);
    }

    public void addPlayerData(PlayerData playerData) {
        this.playerData.put(playerData.getPlayer().getUniqueId(), playerData);
    }

    public void removePlayerData(UUID id) {
        this.playerData.remove(id);
    }

    public boolean hasPlayerData(Player player) {
        return playerData.containsKey(player.getUniqueId());
    }

    public ConcurrentHashMap<UUID, PlayerData> getPlayerDataMap() {
        return playerData;
    }

    public void startAutoSave() {
        long interval = OptionL.getInt(Option.AUTO_SAVE_INTERVAL_TICKS);
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
                    if (playerData != null && !playerData.isSaving()) {
                        plugin.getStorageProvider().save(player, false);
                    }
                }
            }
        }.runTaskTimerAsynchronously(plugin, interval, interval);
    }

}
