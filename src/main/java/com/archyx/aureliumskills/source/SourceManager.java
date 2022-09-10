package com.archyx.aureliumskills.source;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.skills.Skills;
import com.archyx.aureliumskills.util.item.ItemUtils;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class SourceManager {

    private final AureliumSkills plugin;
    private final Map<Source, Double> sources;
    private final Map<SourceTag, List<Source>> tags;
    private Map<Skill, Map<XMaterial, Double>> customBlocks;
    private Map<Skill, Map<String, Double>> customMobs;
    private Set<XMaterial> customBlockSet;
    private Set<String> customMobSet;

    public SourceManager(AureliumSkills plugin) {
        this.plugin = plugin;
        this.sources = new HashMap<>();
        this.tags = new HashMap<>();
    }

    public void loadSources() {
        long start = System.currentTimeMillis();
        // Create file
        File file = new File(plugin.getDataFolder(), "sources_config.yml");
        if (!file.exists()) {
            plugin.saveResource("sources_config.yml", false);
        }
        // Load FileConfigurations
        FileConfiguration config = updateFile(file, YamlConfiguration.loadConfiguration(file));
        // Load sources
        int sourcesLoaded = 0;
        for (Source source : plugin.getSourceRegistry().values()) {
            String path = source.getPath();
            // Add if exists
            if (config.contains("sources." + path)) {
                sources.put(source, config.getDouble("sources." + path));
                sourcesLoaded++;
            }
            // Otherwise add default value and write to config
            else {
                Bukkit.getLogger().warning("[AureliumSkills] sources_config.yml is missing source of path sources." + path + ", value has been set to 0");
                sources.put(source, 0.0);
            }
        }
        // Load tags
        int tagsLoaded = 0;
        for (SourceTag tag : SourceTag.values()) {
            String path = tag.getPath();
            if (config.contains("tags." + path)) {
                List<String> sourceStringList = config.getStringList("tags." + path);
                List<Source> sourcesList = new ArrayList<>();
                for (String sourceString : sourceStringList) {
                    if (sourceString.equals("*")) { // Add all sources in that skill if use * syntax
                        sourcesList.addAll(Arrays.asList(plugin.getSourceRegistry().values(tag.getSkill())));
                    } else if (sourceString.startsWith("!")) { // Remove source if starts with !
                        Source source = plugin.getSourceRegistry().valueOf(sourceString.substring(1));
                        if (source != null) {
                            sourcesList.remove(source);
                        }
                    } else { // Add source
                        Source source = plugin.getSourceRegistry().valueOf(sourceString);
                        if (source != null) {
                            sourcesList.add(source);
                        }
                    }
                }
                tags.put(tag, sourcesList);
                tagsLoaded++;
            } else {
                plugin.getLogger().warning("sources_config.yml is missing tag of path tags." + path + ", tag will be empty");
                tags.put(tag, new ArrayList<>());
            }
        }
        // Load custom blocks
        customBlocks = new HashMap<>();
        customBlockSet = new HashSet<>();
        Skill[] customBlockSkills = new Skill[] {Skills.FARMING, Skills.FORAGING, Skills.MINING, Skills.EXCAVATION};
        for (Skill skill : customBlockSkills) {
            ConfigurationSection section = config.getConfigurationSection("sources." + skill.toString().toLowerCase(Locale.ENGLISH) + ".custom");
            if (section != null) {
                Map<XMaterial, Double> blockMap = new HashMap<>();
                for (String key : section.getKeys(false)) {
                    double value = section.getDouble(key);
                    Optional<XMaterial> optionalMaterial = XMaterial.matchXMaterial(key.toUpperCase());
                    if (optionalMaterial.isPresent()) {
                        XMaterial material = optionalMaterial.get();
                        blockMap.put(material, value);
                        customBlockSet.add(material);
                        sourcesLoaded++;
                    }
                    else {
                        Bukkit.getLogger().warning("[AureliumSkills] Custom block " + key + " is not a valid block!");
                    }
                }
                customBlocks.put(skill, blockMap);
            }
        }
        // Load custom mobs
        customMobs = new HashMap<>();
        customMobSet = new HashSet<>();
        Skill[] customMobSkills = new Skill[] {Skills.FIGHTING, Skills.ARCHERY};
        for (Skill skill : customMobSkills) {
            ConfigurationSection section = config.getConfigurationSection("sources." + skill.toString().toLowerCase(Locale.ENGLISH) + ".custom");
            if (section != null) {
                Map<String, Double> mobMap = new HashMap<>();
                for (String key : section.getKeys(false)) {
                    double value = section.getDouble(key);
                    mobMap.put(key, value);
                    customMobSet.add(key);
                    sourcesLoaded++;
                }
                customMobs.put(skill, mobMap);
            }
        }
        Bukkit.getLogger().info("[AureliumSkills] Loaded " + sourcesLoaded + " sources and " + tagsLoaded + " tags in " + (System.currentTimeMillis() - start) + "ms");
    }

    private FileConfiguration updateFile(File file, FileConfiguration config) {
        if (config.contains("file_version")) {
            InputStream stream = plugin.getResource("sources_config.yml");
            if (stream != null) {
                int currentVersion = config.getInt("file_version");
                FileConfiguration imbConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(stream));
                int imbVersion = imbConfig.getInt("file_version");
                //If versions do not match
                if (currentVersion != imbVersion) {
                    try {
                        ConfigurationSection configSection = imbConfig.getConfigurationSection("");
                        int keysAdded = 0;
                        if (configSection != null) {
                            for (String key : configSection.getKeys(true)) {
                                if (!config.contains(key)) {
                                    config.set(key, imbConfig.get(key));
                                    keysAdded++;
                                }
                            }
                        }
                        config.set("file_version", imbVersion);
                        config.save(file);
                        Bukkit.getLogger().info("[AureliumSkills] sources_config.yml was updated to a new file version, " + keysAdded + " new keys were added.");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public double getXp(Source source) {
        Double xp = sources.get(source);
        if (xp == null)
            throw new IllegalStateException("Invalid xp souce index key: " + source.getPath());
        return xp;
    }

    public List<Source> getTag(SourceTag tag) {
        List<Source> list = tags.get(tag);
        return list != null ? list : new ArrayList<>();
    }

    public Map<XMaterial, Double> getCustomBlocks(Skill skill) {
        return customBlocks.get(skill);
    }

    public Set<XMaterial> getCustomBlockSet() {
        return customBlockSet;
    }

    public Map<String, Double> getCustomMobs(Skill skill) {
        return customMobs.get(skill);
    }

    public Set<String> getCustomMobSet() {
        return customMobSet;
    }

    public static ItemStack getMenuItem(Source source) {
        String material = source + "_SPAWN_EGG";
        ItemStack item = null;
        switch (source.name()) {
            case "SNOWMAN":
                material = "JACK_O_LANTERN";
                break;
            case "IRON_GOLEM":
                material = "IRON_BLOCK";
                break;
            case "WITHER":
                material = "NETHER_STAR";
                break;
            case "ENDER_DRAGON":
                material = "DRAGON_EGG";
                break;
            case "GIANT":
                material = "ZOMBIE_HEAD";
                break;
            case "ILLUSIONER":
                item = new ItemStack(Material.POTION);
                PotionMeta meta = (PotionMeta) item.getItemMeta();
                if (meta != null) {
                    meta.setBasePotionData(new PotionData(PotionType.INVISIBILITY));
                    meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
                }
                item.setItemMeta(meta);
                break;
            case "PLAYER":
                material = "PLAYER_HEAD";
        }
        if (item != null) {
            return item;
        } else {
            return ItemUtils.parseItem(material);
        }
    }

}
