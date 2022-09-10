package com.archyx.aureliumskills.data.storage;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.ability.AbstractAbility;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.AbilityData;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.data.PlayerDataLoadEvent;
import com.archyx.aureliumskills.lang.CommandMessage;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.leaderboard.LeaderboardManager;
import com.archyx.aureliumskills.leaderboard.SkillValue;
import com.archyx.aureliumskills.modifier.StatModifier;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.skills.Skills;
import com.archyx.aureliumskills.stats.Stat;
import com.archyx.aureliumskills.util.math.NumberUtil;
import com.archyx.aureliumskills.util.misc.KeyIntPair;
import com.archyx.aureliumskills.util.text.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class YamlStorageProvider extends StorageProvider {

    public YamlStorageProvider(@NotNull AureliumSkills plugin) {
        super(plugin);
    }

    @Override
    public void load(@NotNull Player player) {
        File file = new File(plugin.getDataFolder() + "/playerdata/" + player.getUniqueId() + ".yml");
        if (file.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            @Nullable PlayerData playerData = new PlayerData(player, plugin);
            try {
                // Make sure file name and uuid match
                UUID id = UUID.fromString(config.getString("uuid", player.getUniqueId().toString()));
                if (!player.getUniqueId().equals(id)) {
                    throw new IllegalArgumentException("File name and uuid field do not match!");
                }
                // Load skill data
                for (Skill skill : Skills.values()) {
                    String path = "skills." + skill.name().toLowerCase(Locale.ROOT) + ".";
                    int level = config.getInt(path + "level", 1);
                    double xp = config.getDouble(path + "xp", 0.0);
                    playerData.setSkillLevel(skill, level);
                    playerData.setSkillXp(skill, xp);
                    // Add stat levels
                    plugin.getRewardManager().getRewardTable(skill).applyStats(playerData, level);
                }
                // Load stat modifiers
                ConfigurationSection modifiersSection = config.getConfigurationSection("stat_modifiers");
                if (modifiersSection != null) {
                    for (String entry : modifiersSection.getKeys(false)) {
                        ConfigurationSection modifierEntry = modifiersSection.getConfigurationSection(entry);
                        if (modifierEntry != null) {
                            String name = modifierEntry.getString("name");
                            String statName = modifierEntry.getString("stat");
                            double value = modifierEntry.getDouble("value");
                            if (name != null && statName != null) {
                                @Nullable Stat stat = plugin.getStatRegistry().getStat(statName);
                                if (stat != null) {
                                    StatModifier modifier = new StatModifier(name, stat, value);
                                    playerData.addStatModifier(modifier);
                                }
                            }
                        }
                    }
                }
                playerData.setMana(config.getDouble("mana")); // Load mana
                // Load locale
                String locale = config.getString("locale");
                if (locale != null) {
                    playerData.setLocale(new Locale(locale));
                }
                // Load ability data
                ConfigurationSection abilitySection = config.getConfigurationSection("ability_data");
                if (abilitySection != null) {
                    for (String abilityName : abilitySection.getKeys(false)) {
                        ConfigurationSection abilityEntry = abilitySection.getConfigurationSection(abilityName);
                        if (abilityEntry != null) {
                            AbstractAbility ability = AbstractAbility.valueOf(abilityName.toUpperCase(Locale.ROOT));
                            if (ability != null) {
                                AbilityData abilityData = playerData.getAbilityData(ability);
                                for (String key : abilityEntry.getKeys(false)) {
                                    Object value = abilityEntry.get(key);
                                    if (value == null)
                                        throw new IllegalStateException("Invalid configuration index key: " + key);
                                    abilityData.setData(key, value);
                                }
                            }
                        }
                    }
                }
                // Unclaimed item rewards
                List<String> unclaimedItemsList = config.getStringList("unclaimed_items");
                if (unclaimedItemsList.size() > 0) {
                    List<@NotNull KeyIntPair> unclaimedItems = new ArrayList<>();
                    for (String entry : unclaimedItemsList) {
                        @NotNull String[] splitEntry = entry.split(" ");
                        String itemKey = splitEntry[0];
                        int amount = 1;
                        if (splitEntry.length >= 2) {
                            amount = NumberUtil.toInt(splitEntry[1], 1);
                        }
                        unclaimedItems.add(new KeyIntPair(itemKey, amount));
                    }
                    playerData.setUnclaimedItems(unclaimedItems);
                    playerData.clearInvalidItems();
                }
                playerManager.addPlayerData(playerData);
                plugin.getLeveler().updatePermissions(player);
                PlayerDataLoadEvent event = new PlayerDataLoadEvent(playerData);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Bukkit.getPluginManager().callEvent(event);
                    }
                }.runTask(plugin);
            } catch (Exception e) {
                Bukkit.getLogger().warning("There was an error loading player data for player " + player.getName() + " with UUID " + player.getUniqueId() + ", see below for details.");
                e.printStackTrace();
                @Nullable PlayerData newPlayerData = createNewPlayer(player);
                newPlayerData.setShouldSave(false);
                sendErrorMessageToPlayer(player, e);
            }
        } else {
            createNewPlayer(player);
        }
    }

    @Override
    public void save(@NotNull Player player, boolean removeFromMemory) {
        @Nullable PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData == null) return;
        if (playerData.shouldNotSave()) return;
        // Save lock
        if (playerData.isSaving()) return;
        playerData.setSaving(true);
        // Load file
        File file = new File(plugin.getDataFolder() + "/playerdata/" + player.getUniqueId() + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        try {
            config.set("uuid", player.getUniqueId().toString());
            // Save skill data
            for (Skill skill : Skills.values()) {
                String path = "skills." + skill.toString().toLowerCase(Locale.ROOT) + ".";
                config.set(path + "level", playerData.getSkillLevel(skill));
                config.set(path + "xp", playerData.getSkillXp(skill));
            }
            config.set("stat_modifiers", null); // Clear existing modifiers
            // Save stat modifiers
            int count = 0;
            for (StatModifier modifier : playerData.getStatModifiers().values()) {
                String path = "stat_modifiers." + count + ".";
                config.set(path + "name", modifier.getName());
                config.set(path + "stat", modifier.getStat().toString().toLowerCase(Locale.ROOT));
                config.set(path + "value", modifier.getValue());
                count++;
            }
            config.set("mana", playerData.getMana()); // Save mana
            // Save locale
            Locale locale = playerData.getLocale();
            if (locale != null) {
                config.set("locale", locale.toString());
            }
            // Save ability data
            for (AbilityData abilityData : playerData.getAbilityDataMap().values()) {
                String path = "ability_data." + abilityData.getAbility().toString().toLowerCase(Locale.ROOT) + ".";
                for (Map.Entry<@NotNull String, Object> entry : abilityData.getDataMap().entrySet()) {
                    config.set(path + entry.getKey(), entry.getValue());
                }
            }
            // Save unclaimed items
            List<@NotNull KeyIntPair> unclaimedItems = playerData.getUnclaimedItems();
            config.set("unclaimed_items", null);
            if (unclaimedItems.size() > 0) {
                List<@NotNull String> stringList = new ArrayList<>();
                for (KeyIntPair unclaimedItem : unclaimedItems) {
                    stringList.add(unclaimedItem.getKey() + " " + unclaimedItem.getValue());
                }
                config.set("unclaimed_items", stringList);
            }
            config.save(file);
            if (removeFromMemory) {
                playerManager.removePlayerData(player.getUniqueId()); // Remove from memory
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("There was an error saving player data for player " + player.getName() + " with UUID " + player.getUniqueId() + ", see below for details.");
            e.printStackTrace();
        }
        playerData.setSaving(false); // Unlock
    }

    @Override
    public void loadBackup(@NotNull FileConfiguration config, @NotNull CommandSender sender) {
        ConfigurationSection playerDataSection = config.getConfigurationSection("player_data");
        Locale locale = plugin.getLang().getLocale(sender);
        if (playerDataSection != null) {
            try {
                for (String stringId : playerDataSection.getKeys(false)) {
                    UUID id = UUID.fromString(stringId);
                    // Load levels and xp from backup
                    Map<Skill, Integer> levels = getLevelsFromBackup(playerDataSection, stringId);
                    Map<Skill, Double> xpLevels = getXpLevelsFromBackup(playerDataSection, stringId);
                    @Nullable PlayerData playerData = playerManager.getPlayerData(id);
                    if (playerData != null) {
                        applyData(playerData, levels, xpLevels);
                    } else {
                        // Load file for offline players
                        File file = new File(plugin.getDataFolder() + "/playerdata/" + id + ".yml");
                        FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(file);
                        playerConfig.set("uuid", id.toString());
                        // Save skill data
                        for (Skill skill : Skills.values()) {
                            String path = "skills." + skill.toString().toLowerCase(Locale.ROOT) + ".";
                            playerConfig.set(path + "level", levels.get(skill));
                            playerConfig.set(path + "xp", xpLevels.get(skill));
                        }
                        // Save file
                        playerConfig.save(file);
                    }
                }
                sender.sendMessage(AureliumSkills.getPrefix(locale) + Lang.getMessage(CommandMessage.BACKUP_LOAD_LOADED, locale));
            } catch (Exception e) {
                String message = Lang.getMessage(CommandMessage.BACKUP_LOAD_ERROR, locale);
                @Nullable String m = e.getMessage();
                if (m != null)
                    message = TextUtil.replace(m, "{error}", m);
                sender.sendMessage(AureliumSkills.getPrefix(locale) + message);
            }
        }
    }

    @Override
    public void updateLeaderboards() {
        LeaderboardManager manager = plugin.getLeaderboardManager();
        manager.setSorting(true);
        // Initialize lists
        Map<Skill, List<SkillValue>> leaderboards = new HashMap<>();
        for (Skill skill : Skills.values()) {
            leaderboards.put(skill, new ArrayList<>());
        }
        List<SkillValue> powerLeaderboard = new ArrayList<>();
        List<SkillValue> averageLeaderboard = new ArrayList<>();
        // Add players already in memory
        Set<UUID> loadedFromMemory = addLoadedPlayersToLeaderboards(leaderboards, powerLeaderboard, averageLeaderboard);

        File playerDataFolder = new File(plugin.getDataFolder() + "/playerdata");
        // Load data from files
        if (playerDataFolder.exists() && playerDataFolder.isDirectory()) {
            File[] files = playerDataFolder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().endsWith(".yml")) {
                        UUID id = UUID.fromString(file.getName().substring(0, file.getName().lastIndexOf('.')));
                        if (!loadedFromMemory.contains(id)) {
                            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                            try {
                                int powerLevel = 0;
                                double powerXp = 0;
                                int numEnabled = 0;
                                for (Skill skill : Skills.values()) {
                                    // Load from config
                                    String path = "skills." + skill.toString().toLowerCase(Locale.ROOT) + ".";
                                    int level = config.getInt(path + "level", 1);
                                    double xp = config.getDouble(path + "xp");
                                    // Add to lists
                                    SkillValue skillLevel = new SkillValue(id, level, xp);
                                    List<SkillValue> skillList = leaderboards.get(skill);
                                    if (skillList == null)
                                        throw new IllegalStateException("Invalid skill leaderboard skill index key: " + skill);
                                    skillList.add(skillLevel);

                                    if (OptionL.isEnabled(skill)) {
                                        powerLevel += level;
                                        powerXp += xp;
                                        numEnabled++;
                                    }
                                }
                                // Add power and average
                                SkillValue powerValue = new SkillValue(id, powerLevel, powerXp);
                                powerLeaderboard.add(powerValue);
                                double averageLevel = (double) powerLevel / numEnabled;
                                SkillValue averageValue = new SkillValue(id, 0, averageLevel);
                                averageLeaderboard.add(averageValue);
                            } catch (Exception e) {
                                Bukkit.getLogger().warning("[AureliumSkills] Error reading playerdata file " + file.getName() + ", see error below for details:");
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        sortLeaderboards(leaderboards, powerLeaderboard, averageLeaderboard);
    }

    @Override
    public void delete(@NotNull UUID uuid) throws IOException {
        File file = new File(plugin.getDataFolder() + "/playerdata/" + uuid.toString() + ".yml");
        if (file.exists()) {
            boolean success = file.delete();
            if (!success) {
                throw new IOException("Unable to delete file");
            }
        } else {
            throw new IOException("File not found in playerdata folder");
        }
    }

}
