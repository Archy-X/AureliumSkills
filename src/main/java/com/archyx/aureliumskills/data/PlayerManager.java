package com.archyx.aureliumskills.data;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerManager {

    private final @NotNull AureliumSkills plugin;
    private final @NotNull ConcurrentHashMap<UUID, PlayerData> playerData;

    public PlayerManager(@NotNull AureliumSkills plugin) {
        this.plugin = plugin;
        this.playerData = new ConcurrentHashMap<>();
        if (OptionL.getBoolean(Option.AUTO_SAVE_ENABLED)) {
            startAutoSave();
        }
    }

    public @Nullable PlayerData getPlayerData(@NotNull Player player) {
        return playerData.get(player.getUniqueId());
    }

    public @Nullable PlayerData getPlayerData(UUID id) {
        return this.playerData.get(id);
    }

    public void addPlayerData(@NotNull PlayerData playerData) {
        this.playerData.put(playerData.getPlayer().getUniqueId(), playerData);
    }

    public void removePlayerData(@NotNull UUID id) {
        this.playerData.remove(id);
    }

    public boolean hasPlayerData(@NotNull Player player) {
        return playerData.containsKey(player.getUniqueId());
    }

    public @NotNull ConcurrentHashMap<UUID, PlayerData> getPlayerDataMap() {
        return playerData;
    }

    public void startAutoSave() {
        long interval = OptionL.getInt(Option.AUTO_SAVE_INTERVAL_TICKS);
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
                    if (playerData != null && !playerData.isSaving()) {
                        plugin.getStorageProvider().save(player, false);
                    }
                }
            }
        }.runTaskTimerAsynchronously(plugin, interval, interval);
    }

}
