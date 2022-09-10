package com.archyx.aureliumskills.data.backup;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.data.storage.MySqlStorageProvider;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Locale;
import java.util.UUID;

public class MysqlBackup extends BackupProvider {

    private final MySqlStorageProvider storageProvider;

    public MysqlBackup(@NotNull AureliumSkills plugin, MySqlStorageProvider storageProvider) {
        super(plugin);
        this.storageProvider = storageProvider;
    }

    @Override
    public void saveBackup(@NotNull CommandSender sender, boolean savePlayerData) {
        try {
            // Save online players
            if (savePlayerData) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    storageProvider.save(player, false);
                }
            }
            Connection connection = storageProvider.getConnection();
            try (Statement statement = connection.createStatement()) {
                String query = "SELECT * FROM SkillData;";
                try (ResultSet result = statement.executeQuery(query)) {
                    createBackupFolder();
                    LocalTime time = LocalTime.now();
                    File file = new File(plugin.getDataFolder() + "/backups/backup-" + LocalDate.now()
                            + "_" + time.getHour() + "-" + time.getMinute() + "-" + time.getSecond() + ".yml");
                    FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                    config.set("backup_version", 1);
                    while (result.next()) {
                        UUID id = UUID.fromString(result.getString("ID"));
                        for (Skill skill : Skills.values()) {
                            int level = result.getInt(skill.toString().toUpperCase(Locale.ROOT) + "_LEVEL");
                            double xp = result.getDouble(skill.toString().toUpperCase(Locale.ROOT) + "_XP");

                            String path = "player_data." + id + "." + skill.toString().toLowerCase(Locale.ROOT) + ".";
                            config.set(path + "level", level);
                            config.set(path + "xp", xp);
                        }
                    }
                    config.save(file);
                    @Nullable Locale locale = plugin.getLang().getLocale(sender);
                    String message = TextUtil.replace(Lang.getMessage(CommandMessage.BACKUP_SAVE_SAVED, locale)
                            , "{type}", "MySQL", "{file}", file.getName());
                    if (sender instanceof ConsoleCommandSender) {
                        plugin.getLogger().info(ChatColor.stripColor(message));
                    } else {
                        sender.sendMessage(AureliumSkills.getPrefix(locale) + message);
                    }
                }
            }
        } catch (Exception e) {
            @Nullable Locale locale = plugin.getLang().getLocale(sender);
            String message = TextUtil.replace(Lang.getMessage(CommandMessage.BACKUP_SAVE_ERROR, locale), "{type}", "MySQL");
            if (sender instanceof ConsoleCommandSender) {
                plugin.getLogger().warning(ChatColor.stripColor(message));
            } else {
                sender.sendMessage(AureliumSkills.getPrefix(locale) + message);
            }
            e.printStackTrace();
        }
    }

}
