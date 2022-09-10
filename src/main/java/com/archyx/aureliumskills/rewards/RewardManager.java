package com.archyx.aureliumskills.rewards;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.rewards.parser.RewardParser;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.stats.Stat;
import com.archyx.aureliumskills.util.misc.DataUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class RewardManager {

    private final AureliumSkills plugin;
    private final Map<Skill, RewardTable> rewardTables;

    public RewardManager(AureliumSkills plugin) {
        this.plugin = plugin;
        this.rewardTables = new HashMap<>();
    }

    public RewardTable getRewardTable(Skill skill) {
        return rewardTables.get(skill);
    }

    public void loadRewards() {
        this.rewardTables.clear();
        File rewardsDirectory = new File(plugin.getDataFolder() + "/rewards");
        // Load each file
        int patternsLoaded = 0;
        int levelsLoaded = 0;
        for (Skill skill : plugin.getSkillRegistry().getSkills()) {
            File rewardsFile = new File(rewardsDirectory + "/" + skill.toString().toLowerCase(Locale.ROOT) + ".yml");
            if (!rewardsFile.exists()) {
                plugin.saveResource("rewards/" + skill.toString().toLowerCase(Locale.ROOT) + ".yml", false);
            }
            FileConfiguration rewardsConfig = YamlConfiguration.loadConfiguration(rewardsFile);
            RewardTable rewardTable = new RewardTable(plugin);
            // Load patterns
            patternsLoaded += loadPatterns(rewardTable, rewardsConfig, rewardsFile, OptionL.getMaxLevel(skill));
            // Load levels section
            levelsLoaded += loadLevels(rewardTable, rewardsConfig, rewardsFile);
            // Register reward table
            this.rewardTables.put(skill, rewardTable);
        }
        // Load global rewards
        File globalFile = new File(plugin.getDataFolder() + "/rewards/global.yml");
        if (!globalFile.exists()) {
            plugin.saveResource("rewards/global.yml", false);
        }
        FileConfiguration globalConfig = YamlConfiguration.loadConfiguration(globalFile);
        RewardTable globalTable = new RewardTable(plugin);
        patternsLoaded += loadPatterns(globalTable, globalConfig, globalFile, plugin.getOptionLoader().getHighestMaxLevel());
        levelsLoaded += loadLevels(globalTable, globalConfig, globalFile);
        // Apply global rewards table to each skill reward table
        for (Map.Entry<Integer, List<Reward>> entry : globalTable.getRewardsMap().entrySet()) {
            Integer level = entry.getKey();
            List<Reward> rewards = entry.getValue();
            for (Skill skill : plugin.getSkillRegistry().getSkills()) {
                RewardTable rewardTable = this.rewardTables.get(skill);
                if (rewardTable != null) {
                    for (Reward reward : rewards) {
                        rewardTable.addReward(reward, level);
                    }
                }
            }
        }
        plugin.getLogger().info("Loaded " + patternsLoaded + " pattern rewards and " + levelsLoaded + " level rewards");
    }

    private int loadPatterns(RewardTable rewardTable, FileConfiguration rewardsConfig, File rewardsFile, int maxLevel) {
        // Load patterns section
        int patternsLoaded = 0;
        List<Map<?, ?>> patterns = rewardsConfig.getMapList("patterns");
        for (int index = 0; index < patterns.size(); index++) {
            Map<?, ?> rewardMap = patterns.get(index);
            try {
                Reward reward = parseReward(rewardMap);
                // Load pattern info
                Object patternObj = DataUtil.getElement(rewardMap, "pattern");
                if (!(patternObj instanceof Map<?, ?>)) {
                    throw new IllegalArgumentException("Pattern must be a section");
                }
                Map<?, ?> patternMap = (Map<?, ?>) patternObj;
                int start = DataUtil.getInt(patternMap, "start");
                int interval = DataUtil.getInt(patternMap, "interval");
                // Get stop interval and check it is not above max skill level
                int stop = maxLevel;
                if (patternMap.containsKey("stop")) {
                    stop = DataUtil.getInt(patternMap, "stop");
                }
                if (stop > maxLevel) {
                    stop = maxLevel;
                }
                // Add to reward table
                for (int level = start; level <= stop; level += interval) {
                    rewardTable.addReward(reward, level);
                }
                patternsLoaded++;
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Error while loading rewards file " + rewardsFile.getName() + " at path patterns.[" + index + "]: " + e.getMessage());
            }
        }
        return patternsLoaded;
    }

    private int loadLevels(RewardTable rewardTable, FileConfiguration rewardsConfig, File rewardsFile) {
        int levelsLoaded = 0;
        ConfigurationSection levelsSection = rewardsConfig.getConfigurationSection("levels");
        if (levelsSection != null) {
            // For each level defined
            for (String levelString : levelsSection.getKeys(false)) {
                try {
                    int level = Integer.parseInt(levelString);
                    // For each reward in that level
                    List<Map<?, ?>> rewards = levelsSection.getMapList(levelString);
                    for (int index = 0; index < rewards.size(); index++) {
                        Map<?, ?> rewardMap = rewards.get(index);
                        try {
                            Reward reward = parseReward(rewardMap);
                            rewardTable.addReward(reward, level);
                            levelsLoaded++;
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Error while loading rewards file " + rewardsFile.getName() + " at path levels." + levelString + ".[" + index + "]: " + e.getMessage());
                        }
                    }
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Error while loading rewards file " + rewardsFile.getName() + " at path levels." + levelString + ": Key " + levelString + " must be of type int");
                }
            }
        }
        return levelsLoaded;
    }

    private Reward parseReward(Map<?, ?> map) {
        // Get type of reward
        String type = DataUtil.getString(map, "type");
        // Parse the type
        for (RewardType rewardType : RewardType.values()) {
            if (rewardType.getKey().equalsIgnoreCase(type)) {
                try {
                    Constructor<? extends RewardParser> constructor = rewardType.getParser().getConstructor(AureliumSkills.class);
                    return constructor.newInstance(plugin).parse(map);
                } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        throw new IllegalArgumentException("Unrecognized reward type: " + type);
    }

    // Gets all the skills a stat is leveled by
    public List<Skill> getSkillsLeveledBy(Stat stat) {
        List<Skill> skillsLeveledBy = new ArrayList<>();
        for (Skill skill : plugin.getSkillRegistry().getSkills()) {
            if (!OptionL.isEnabled(skill)) continue; // Skip disabled skills
            RewardTable table = rewardTables.get(skill);
            if (table != null) {
                for (Stat statLeveled : table.getStatsLeveled()) {
                    if (statLeveled.equals(stat)) {
                        skillsLeveledBy.add(skill);
                        break;
                    }
                }
            }
        }
        return skillsLeveledBy;
    }

}
