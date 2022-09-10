package com.archyx.aureliumskills.data.backup;

import com.archyx.aureliumskills.AureliumSkills;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Locale;

public class LegacyFileBackup extends BackupProvider {

    public LegacyFileBackup(@NotNull AureliumSkills plugin) {
        super(plugin);
    }

    @Override
    public void saveBackup(@NotNull CommandSender sender, boolean savePlayerData) {
        try {
            File file = new File(plugin.getDataFolder(), "data.yml");
            if (!file.exists()) return;
            createBackupFolder();
            LocalTime time = LocalTime.now();
            File backupFile = new File(plugin.getDataFolder() + "/backups/backup-" + LocalDate.now()
                    + "_" + time.getHour() + "-" + time.getMinute() + "-" + time.getSecond() + ".yml");
            FileConfiguration data = YamlConfiguration.loadConfiguration(file);
            FileConfiguration config = YamlConfiguration.loadConfiguration(backupFile);
            config.set("backup_version", "1");

            ConfigurationSection skillData = data.getConfigurationSection("skillData");
            if (skillData != null) {
                for (String stringId : skillData.getKeys(false)) {
                    ConfigurationSection playerSection = skillData.getConfigurationSection(stringId);
                    if (playerSection != null) {
                        ConfigurationSection skills = playerSection.getConfigurationSection("skills");
                        if (skills != null) {
                            for (String skillName : skills.getKeys(false)) {
                                String legacySkillData = skills.getString(skillName);
                                if (legacySkillData != null) {
                                    String[] splitLegacySkillData = legacySkillData.split(":");
                                    if (splitLegacySkillData.length == 2) {
                                        int level = Integer.parseInt(splitLegacySkillData[0]);
                                        double xp = Double.parseDouble(splitLegacySkillData[1]);
                                        String path = "player_data." + stringId + "." + skillName.toLowerCase(Locale.ROOT) + ".";
                                        config.set(path + "level", level);
                                        config.set(path + "xp", xp);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            config.save(backupFile);
            sender.sendMessage("[AureliumSkills] Backed up legacy data.yml file as " + backupFile.getName());
        } catch (Exception e) {
            sender.sendMessage("[AureliumSkills] Error backing up legacy data file! See error below for details:");
            e.printStackTrace();
        }
    }

}
