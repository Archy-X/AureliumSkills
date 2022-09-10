package com.archyx.aureliumskills.data.backup;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.lang.CommandMessage;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.skills.Skills;
import com.archyx.aureliumskills.util.text.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Locale;

public class YamlBackup extends BackupProvider {

    public YamlBackup(AureliumSkills plugin) {
        super(plugin);
    }

    @Override
    public void saveBackup(CommandSender sender, boolean savePlayerData) {
        try {
            if (savePlayerData) {
                // Save online players
                for (Player player : Bukkit.getOnlinePlayers()) {
                    plugin.getStorageProvider().save(player, false);
                }
            }
            createBackupFolder();
            LocalTime time = LocalTime.now();
            File backupFile = new File(plugin.getDataFolder() + "/backups/backup-" + LocalDate.now()
                    + "_" + time.getHour() + "-" + time.getMinute() + "-" + time.getSecond() + ".yml");
            FileConfiguration backup = YamlConfiguration.loadConfiguration(backupFile);
            backup.set("backup_version", 1);

            File playerDataFolder = new File(plugin.getDataFolder() + "/playerdata");
            if (playerDataFolder.exists() && playerDataFolder.isDirectory()) {
                File[] files = playerDataFolder.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.getName().endsWith(".yml")) {
                            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                            String stringId = config.getString("uuid");
                            if (stringId != null) {
                                for (Skill skill : Skills.values()) {
                                    int level = config.getInt("skills." + skill.toString().toLowerCase(Locale.ROOT) + ".level");
                                    double xp = config.getInt("skills." + skill.toString().toLowerCase(Locale.ROOT) + ".xp");
                                    String path = "player_data." + stringId + "." + skill.toString().toLowerCase(Locale.ROOT) + ".";
                                    backup.set(path + "level", level);
                                    backup.set(path + "xp", xp);
                                }
                            }
                        }
                    }
                }
            }
            backup.save(backupFile);
            Locale locale = plugin.getLang().getLocale(sender);
            String message = AureliumSkills.getPrefix(locale) + TextUtil.replace(Lang.getMessage(CommandMessage.BACKUP_SAVE_SAVED, locale)
                    , "{type}", "Yaml", "{file}", backupFile.getName());
            if (sender instanceof ConsoleCommandSender) {
                String m = ChatColor.stripColor(message);
                assert (null != m);
                message = m;
            }
            sender.sendMessage(message);
        } catch (Exception e) {
            Locale locale = plugin.getLang().getLocale(sender);
            String message = AureliumSkills.getPrefix(locale) + TextUtil.replace(Lang.getMessage(CommandMessage.BACKUP_SAVE_ERROR, locale), "{type}", "Yaml");
            if (sender instanceof ConsoleCommandSender) {
                Bukkit.getLogger().warning(ChatColor.stripColor(message));
            } else {
                sender.sendMessage(message);
            }
            e.printStackTrace();
        }
    }
}
