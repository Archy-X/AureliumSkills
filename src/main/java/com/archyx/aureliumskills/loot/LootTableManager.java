package com.archyx.aureliumskills.loot;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.util.misc.Parser;
import com.archyx.lootmanager.LootManager;
import com.archyx.lootmanager.loot.LootPool;
import com.archyx.lootmanager.loot.LootTable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class LootTableManager extends Parser {

	private final AureliumSkills plugin;
	private final @NotNull LootManager lootManager;
	private final @NotNull Map<Skill, LootTable> lootTables;
	
	public LootTableManager(AureliumSkills plugin) {
		this.plugin = plugin;
		this.lootManager = new LootManager(plugin);
		lootTables = new HashMap<>();
		initLootManager();
		loadLootTables();
	}

	public void initLootManager() {
		lootManager.registerContextManager(new SourceContextManager(plugin));
		lootManager.registerCustomItemParser(new ItemKeyParser(plugin));
		lootManager.addLootOptionKeys("xp");
		lootManager.addPoolOptionKeys("chance_per_luck", "require_open_water");
	}

	public void generateDefaultLootTables() {
		File fishingFile = new File(plugin.getDataFolder() + "/loot", "fishing.yml");
		if (!fishingFile.exists()) {
			plugin.saveResource("loot/fishing.yml", false);
		}
		File excavationFile = new File(plugin.getDataFolder() + "/loot", "excavation.yml");
		if (!excavationFile.exists()) {
			plugin.saveResource("loot/excavation.yml", false);
		}
	}

	public void loadLootTables() {
		// Check that new loot files do not exist yet for conversion
		boolean convertFishing = !new File(plugin.getDataFolder() + "/loot", "fishing.yml").exists();
		boolean convertExcavation = !new File(plugin.getDataFolder() + "/loot", "excavation.yml").exists();
		// Generate default loot files
		File lootDirectory = new File(plugin.getDataFolder() + "/loot");
		if (!lootDirectory.exists() || convertFishing || convertExcavation) {
			generateDefaultLootTables();
		}
		if (!lootDirectory.isDirectory()) return;

		// Convert legacy file
		try {
			new LegacyLootConverter(plugin).convertLegacyFile(convertFishing, convertExcavation);
		} catch (IOException e) {
			plugin.getLogger().warning("Failed to convert legacy loot file, see below for error");
			e.printStackTrace();
		}

		lootTables.clear();
		File[] files = lootDirectory.listFiles();
		if (files == null) return;
		for (File lootTableFile : files) {
			if (!lootTableFile.isFile() || !lootTableFile.getName().endsWith(".yml")) {
				continue;
			}
			// Parse skill from file name
			String skillName = lootTableFile.getName().replace(".yml", "");
			Skill skill = plugin.getSkillRegistry().getSkill(skillName);
			if (skill == null) return;

			FileConfiguration config = YamlConfiguration.loadConfiguration(lootTableFile);
			matchConfig(config, lootTableFile); // Try to update file
			// Load corresponding loot table type
			LootTable lootTable = lootManager.getLootLoader().loadLootTable(lootTableFile, config);
			if (lootTable != null) {
				lootTables.put(skill, lootTable);
			}
		}
		// Send info message
		int tablesLoaded = 0;
		int poolsLoaded = 0;
		int lootLoaded = 0;
		for (LootTable table : lootTables.values()) {
			for (LootPool pool : table.getPools()) {
				poolsLoaded++;
				lootLoaded += pool.getLoot().size();
			}
			tablesLoaded++;
		}
		plugin.getLogger().info("Loaded " + lootLoaded + " loot entries in " + poolsLoaded + " pools and " + tablesLoaded + " tables");
	}

	public @Nullable LootTable getLootTable(Skill skill) {
		return lootTables.get(skill);
	}
	
	public void matchConfig(@NotNull FileConfiguration config, @NotNull File file) {
		config.options().copyDefaults(true);
		try {
			boolean changesMade = false;
			InputStream is = plugin.getResource("loot/" + file.getName());
			if (is != null) {
				YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(is));
				ConfigurationSection configurationSection = defConfig.getConfigurationSection("");
				if (configurationSection != null) {
					for (String key : configurationSection.getKeys(true)) {
						if (!config.contains(key)) {
							config.set(key, defConfig.get(key));
							if (!changesMade) {
								changesMade = true;
							}
						}
					}
					if (changesMade) {
						config.save(file);
					}
				}
			}
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

}
