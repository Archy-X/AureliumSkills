package com.archyx.aureliumskills.loot;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.util.file.FileUtil;
import com.archyx.aureliumskills.util.math.NumberUtil;
import com.archyx.aureliumskills.util.text.TextUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class LegacyLootConverter {

    private final AureliumSkills plugin;

    public LegacyLootConverter(AureliumSkills plugin) {
        this.plugin = plugin;
    }

    public void convertLegacyFile(boolean convertFishing, boolean convertExcavation) throws IOException {
        File legacyFile = new File(plugin.getDataFolder(), "loot.yml");
        if (!legacyFile.exists()) { // Don't convert if doesn't exist
            return;
        }
        // Convert fishing loot tables
        FileConfiguration legacyConfig = YamlConfiguration.loadConfiguration(legacyFile);
        if (convertFishing) {
            File fishingFile = new File(plugin.getDataFolder() + "/loot/fishing.yml");
            FileConfiguration fishingConfig = YamlConfiguration.loadConfiguration(fishingFile);
            // Fishing Rare
            List<String> fishingRareLoot = legacyConfig.getStringList("lootTables.fishing-rare");
            convertSection(fishingConfig, fishingRareLoot, "pools.rare");
            // Fishing Epic
            List<String> fishingEpicLoot = legacyConfig.getStringList("lootTables.fishing-epic");
            convertSection(fishingConfig, fishingEpicLoot, "pools.epic");
            fishingConfig.save(fishingFile);
        }
        // Convert excavation loot tables
        if (convertExcavation) {
            File excavationFile = new File(plugin.getDataFolder() + "/loot/excavation.yml");
            FileConfiguration excavationConfig = YamlConfiguration.loadConfiguration(excavationFile);
            // Excavation rare
            List<String> excavationRareLoot = legacyConfig.getStringList("lootTables.excavation-rare");
            convertSection(excavationConfig, excavationRareLoot, "pools.rare");
            // Excavation epic
            List<String> excavationEpicLoot = legacyConfig.getStringList("lootTables.excavation-epic");
            convertSection(excavationConfig, excavationEpicLoot, "pools.epic");
            excavationConfig.save(excavationFile);
        }

        if (convertFishing || convertExcavation) {
            renameLegacyFile(legacyFile);
            plugin.getLogger().info("Converted old loot.yml file to new files in the loot folder");
        }
    }

    private void convertSection(@NotNull FileConfiguration config, @NotNull List<String> legacyLoot, @NotNull String sectionPath) {
        // Convert loot
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (String entry : legacyLoot) {
            Map<String, Object> map = new LinkedHashMap<>();
            if (entry.startsWith("cmd:")) { // Legacy command loot
                map.put("type", "command");
                String commandString = TextUtil.replace(entry, "cmd:", "");
                map.put("command", commandString);
                map.put("executor", "console");
            } else { // Item loot
                map.put("type", "item");
                String[] split = entry.split(" ");
                if (split.length < 3) {
                    plugin.getLogger().warning("Failed to convert the following legacy loot entry because min, max, and material were not specified: " + entry);
                    continue;
                }
                int minAmount = NumberUtil.toInt(split[0]);
                int maxAmount = NumberUtil.toInt(split[1]);
                String materialName = split[2];
                map.put("material", materialName.toLowerCase(Locale.ROOT));
                if (minAmount == maxAmount) {
                    map.put("amount", minAmount);
                } else {
                    map.put("amount", minAmount + "-" + maxAmount);
                }
                // Convert meta arguments
                for (int i = 3 ; i < split.length; i++) {
                    String[] splitPair = split[i].split(":", 2);
                    if (splitPair.length == 2) {
                        String key = splitPair[0];
                        String value = splitPair[1].replace("_", " ");
                        String originalValue = splitPair[1];
                        switch (key) {
                            case "name":
                                map.put("display_name", value);
                                break;
                            case "lore":
                                List<@NotNull String> lore = new LinkedList<>(Arrays.asList(value.split("\\|")));
                                map.put("lore", lore);
                                break;
                            case "glow":
                                if (Boolean.parseBoolean(value)) {
                                    map.put("glow", true);
                                }
                                break;
                            case "enchantments":
                                List<@NotNull String> enchantments = new ArrayList<>(Arrays.asList(originalValue.split("\\|")));
                                List<String> enchantmentList = new ArrayList<>();
                                for (String enchantString : enchantments) {
                                    String[] splitEnchantString = enchantString.split(":", 2);
                                    String enchantName = splitEnchantString[0];
                                    int enchantLevel = 1;
                                    if (splitEnchantString.length == 2) {
                                        enchantLevel = Integer.parseInt(splitEnchantString[1]);
                                    }
                                    enchantmentList.add(enchantName.toLowerCase(Locale.ROOT) + " " + enchantLevel);
                                }
                                map.put("enchantments", enchantmentList);
                                break;
                            case "potion_type":
                                Map<String, String> potionData = new HashMap<>();
                                potionData.put("type", originalValue.toUpperCase(Locale.ROOT));
                                map.put("potion_data", potionData);
                                break;
                            case "custom_effect":
                                @NotNull String[] values = originalValue.split(",");
                                List<Map<String, Object>> customEffects = new ArrayList<>();
                                if (values.length == 3) {
                                    @Nullable PotionEffectType type = PotionEffectType.getByName(values[0]);
                                    if (type != null) {
                                        Map<String, Object> effect = new HashMap<>();
                                        effect.put("type", type.toString());
                                        effect.put("duration", Integer.parseInt(values[1]));
                                        effect.put("amplifier", Integer.parseInt(values[2]));
                                        customEffects.add(effect);
                                    }
                                }
                                map.put("custom_effects", customEffects);
                                break;
                        }
                    }
                }
            }
            map.put("weight", 10); // Use default weight
            mapList.add(map);
        }
        config.set(sectionPath + ".loot", mapList);
    }

    private void renameLegacyFile(@NotNull File file) {
        String renamedName = FileUtil.renameNoDuplicates(file, "loot-OLD.yml", plugin.getDataFolder());
        if (renamedName != null) {
            plugin.getLogger().info("Successfully renamed loot.yml to " + renamedName);
        } else {
            plugin.getLogger().warning("Failed to renamed old loot.yml file");
        }
    }

}
