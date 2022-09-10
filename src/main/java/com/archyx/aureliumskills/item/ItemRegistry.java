package com.archyx.aureliumskills.item;

import com.archyx.aureliumskills.AureliumSkills;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ItemRegistry {

    private final AureliumSkills plugin;
    private final ConcurrentMap<String, ItemStack> items;

    public ItemRegistry(AureliumSkills plugin) {
        this.plugin = plugin;
        items = new ConcurrentHashMap<>();
    }

    public void register(String key, ItemStack item) {
        items.put(key, item.clone());
    }

    public void unregister(String key) {
        items.remove(key);
    }

    public ItemStack getItem(String key) {
        ItemStack item = items.get(key);
        if (item != null) {
            return item.clone();
        } else {
            return null;
        }
    }

    public Set<String> getKeys() {
        return items.keySet();
    }

    public void loadFromFile() {
        File file = new File(plugin.getDataFolder(), "items.yml");
        // Don't load if file doesn't exist
        if (!file.exists()) {
            return;
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection itemsSection = config.getConfigurationSection("items");
        if (itemsSection == null) return;
        int itemsRegistered = 0;
        for (String key : itemsSection.getKeys(false)) {
            ItemStack item = itemsSection.getItemStack(key);
            if (item == null) {
                plugin.getLogger().warning("Failed to load item at path items." + key + " in items.yml, did you edit this file? " +
                        "This file should not be edited directly and items should only be added in game through the command /skills item register");
                continue;
            }
            items.put(key, item);
            itemsRegistered++;
        }
        if (itemsRegistered > 1) {
            plugin.getLogger().info("Registered " + itemsRegistered + " items");
        } else if (itemsRegistered == 1) {
            plugin.getLogger().info("Registered " + itemsRegistered + " item");
        }
    }

    public void saveToFile() {
        File file = new File(plugin.getDataFolder(), "items.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("items", null); // Delete to account for unregistered items
        // Save each item
        for (Map.Entry<String, ItemStack> entry : items.entrySet()) {
            config.set("items." + entry.getKey(), entry.getValue());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save items.yml, see error below:");
            e.printStackTrace();
        }
    }

}
