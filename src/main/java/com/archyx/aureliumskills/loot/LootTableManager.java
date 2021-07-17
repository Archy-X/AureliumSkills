package com.archyx.aureliumskills.loot;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.loot.parser.CommandLootParser;
import com.archyx.aureliumskills.loot.parser.ItemLootParser;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.util.misc.DataUtil;
import com.archyx.aureliumskills.util.misc.Parser;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LootTableManager extends Parser {

	private final Map<Skill, LootTable> lootTables;
	private final AureliumSkills plugin;
	
	public LootTableManager(AureliumSkills plugin) {
		this.plugin = plugin;
		lootTables = new HashMap<>();
		loadLootTables();
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
			LootTable lootTable = loadLootTable(lootTableFile, config);
			if (lootTable != null) {
				lootTables.put(skill, lootTable);
			}
		}
	}

	private LootTable loadLootTable(File file, FileConfiguration config) {
		ConfigurationSection poolsSection = config.getConfigurationSection("pools");
		if (poolsSection == null) return null;
		List<LootPool> pools = new ArrayList<>();
		for (String poolName : poolsSection.getKeys(false)) {
			ConfigurationSection currentPool = poolsSection.getConfigurationSection(poolName);
			if (currentPool == null) continue;

			double baseChance = currentPool.getDouble("base_chance", 0.01);
			double chancePerLuck = currentPool.getDouble("chance_per_luck", 0.0);
			int selectionPriority = currentPool.getInt("selection_priority", 1);
			boolean overrideVanillaLoot = currentPool.getBoolean("override_vanilla_loot", false);

			// Parse each loot entry
			List<Map<?,?>> lootMapList = currentPool.getMapList("loot");
			List<Loot> lootList = new ArrayList<>();
			int index = 0;
			for (Map<?, ?> lootEntryMap : lootMapList) {
				String type = DataUtil.getString(lootEntryMap, "type");
				Loot loot = null;
				try {
					// Item loot
					if (type.equalsIgnoreCase("item")) {
						if (getBooleanOrDefault(lootEntryMap, "ignore_legacy", false) && XMaterial.getVersion() <= 12) {
							index++;
							continue;
						}
						loot = new ItemLootParser(plugin).parse(lootEntryMap);
					}
					// Command loot
					else if (type.equalsIgnoreCase("command")) {
						loot = new CommandLootParser(plugin).parse(lootEntryMap);
					}
				} catch (Exception e) {
					plugin.getLogger().warning("Error parsing loot in file loot/" + file.getName() + " at path pools." + poolName + ".loot." + index + ", see below for error:");
					e.printStackTrace();
				}
				if (loot != null) {
					lootList.add(loot);
				}
				index++;
			}
			// Create pool
			LootPool pool = new LootPool(poolName, lootList, baseChance, chancePerLuck, selectionPriority, overrideVanillaLoot);
			pools.add(pool);
		}
		// Sort pools by selection priority
		pools.sort((pool1, pool2) -> pool2.getSelectionPriority() - pool1.getSelectionPriority());
		// Create table
		return new LootTable(pools);
	}

	@Nullable
	public LootTable getLootTable(Skill skill) {
		return lootTables.get(skill);
	}
	
	public void matchConfig(FileConfiguration config, File file) {
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
