package com.archyx.aureliumskills.data.converter;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.util.file.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Locale;
import java.util.UUID;

public class LegacyFileToYamlConverter extends DataConverter {

    public LegacyFileToYamlConverter(@NotNull AureliumSkills plugin) {
        super(plugin);
    }

    @Override
    public void convert() {
        File file = new File(plugin.getDataFolder(), "data.yml");
        if (file.exists()) { // Only convert if data.yml exists
            Bukkit.getLogger().info("[AureliumSkills] Converting legacy file data to new yaml file format...");
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection skillData = config.getConfigurationSection("skillData");
            int playersConverted = 0;
            if (skillData != null) {
                for (String stringUUID : skillData.getKeys(false)) {
                    try {
                        UUID uuid = UUID.fromString(stringUUID);
                        File playerDataFile = new File(plugin.getDataFolder() + "/playerdata/" + uuid + ".yml");
                        if (!playerDataFile.exists()) { // Only convert if playerdata file does not exist
                            // Create config
                            FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerDataFile);
                            playerConfig.set("uuid", uuid.toString());
                            // Convert skill data
                            ConfigurationSection playerSection = skillData.getConfigurationSection(stringUUID);
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
                                                String path = "skills." + skillName.toLowerCase(Locale.ROOT) + ".";
                                                playerConfig.set(path + "level", level);
                                                playerConfig.set(path + "xp", xp);
                                            }
                                        }
                                    }
                                }
                                playerConfig.save(playerDataFile);
                                playersConverted++;
                            }
                        }
                    } catch (Exception e) {
                        Bukkit.getLogger().warning("[AureliumSkills] There was an error converting skill data for player with uuid " + stringUUID + ", see below for details:");
                        e.printStackTrace();
                    }
                }
            }
            String renamedName = FileUtil.renameNoDuplicates(file, "data-OLD.yml", plugin.getDataFolder());
            if (renamedName != null) {
                Bukkit.getLogger().info("[AureliumSkills] Successfully renamed data.yml to " + renamedName);
            } else {
                Bukkit.getLogger().warning("[AureliumSkills] Failed to rename old data.yml file");
            }
            Bukkit.getLogger().info("[AureliumSkills] Successfully converted " + playersConverted + " player skill data to the new yaml file format!");
        }
    }
}
