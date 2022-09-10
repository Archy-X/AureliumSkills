package com.archyx.aureliumskills.data.backup;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.data.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public abstract class BackupProvider {

    public final @NotNull AureliumSkills plugin;
    public final @NotNull PlayerManager playerManager;

    public BackupProvider(@NotNull AureliumSkills plugin) {
        this.plugin = plugin;
        this.playerManager = plugin.getPlayerManager();
    }

    public abstract void saveBackup(@NotNull CommandSender sender, boolean savePlayerData);

    public void createBackupFolder() {
        File backupFolder = new File(plugin.getDataFolder() + "/backups");
        if (!backupFolder.exists()) {
            if (!backupFolder.mkdir()) {
                Bukkit.getLogger().warning("[AureliumSkills] Error creating backups folder!");
            }
        }
    }

}
