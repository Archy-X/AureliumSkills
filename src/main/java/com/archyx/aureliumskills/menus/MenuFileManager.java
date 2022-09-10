package com.archyx.aureliumskills.menus;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.util.file.FileUtil;
import com.archyx.aureliumskills.util.misc.DataUtil;
import com.archyx.aureliumskills.util.text.TextUtil;
import com.archyx.slate.menu.MenuManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MenuFileManager {

    private final @NotNull AureliumSkills plugin;
    private final MenuManager manager;

    public MenuFileManager(@NotNull AureliumSkills plugin) {
        this.plugin = plugin;
        this.manager = plugin.getMenuManager();
    }

    public void generateDefaultFiles() {
        for (String menuName : manager.getMenuProviderNames()) {
            File file = new File(plugin.getDataFolder() + "/menus", menuName + ".yml");
            if (!file.exists()) {
                plugin.saveResource("menus/" + menuName + ".yml", false);
            }
        }
    }

    public void loadMenus() {
        try {
            migrateLegacyFiles();
        } catch (Exception e) {
            plugin.getLogger().warning("Error migrating legacy menus.yml file to new menu files. " +
                    "Any previous changes made in menus.yml may have to be transferred manually to the new files in the menus folder.");
            e.printStackTrace();
        }
        int menusLoaded = 0;
        for (String menuName : manager.getMenuProviderNames()) {
            File file = new File(plugin.getDataFolder() + "/menus", menuName + ".yml");
            if (file.exists()) {
                try {
                    manager.loadMenu(file);
                    menusLoaded++;
                } catch (Exception e) {
                    plugin.getLogger().warning("Error loading menu " + menuName);
                    e.printStackTrace();
                }
            }
        }
        plugin.getLogger().info("Loaded " + menusLoaded + " menus");
    }

    public void migrateLegacyFiles() throws IOException {
        File legacyFile = new File(plugin.getDataFolder(), "menus.yml");
        if (!legacyFile.exists()) return;

        FileConfiguration config = YamlConfiguration.loadConfiguration(legacyFile);
        @NotNull String[] legacyNames = {"skills_menu", "stats_menu", "level_progression_menu"};
        for (String legacyName : legacyNames) {
            ConfigurationSection oldSection = config.getConfigurationSection(legacyName);
            if (oldSection == null) continue;

            String menuName = TextUtil.removeEnd(legacyName, "_menu");
            // Load new file configuration
            File newFile = new File(plugin.getDataFolder(), "menus/" + menuName + ".yml");
            if (!newFile.exists()) continue;
            FileConfiguration newConfig = YamlConfiguration.loadConfiguration(newFile);

            newConfig.set("title", oldSection.getString("title"));
            newConfig.set("size", oldSection.getInt("rows"));
            // Migrate fill settings
            ConfigurationSection fillSection = newConfig.getConfigurationSection("fill");
            if (fillSection != null) {
                fillSection.set("enabled", oldSection.getBoolean("fill.enabled"));
                String material = oldSection.getString("fill.material");
                migrateBaseItem(fillSection, material != null ? material : "black_stained_glass_pane");
            }
            // Migrate items
            ConfigurationSection itemsSection = oldSection.getConfigurationSection("items");
            ConfigurationSection newItemsSection = newConfig.getConfigurationSection("items");
            ConfigurationSection newTemplatesSection = newConfig.getConfigurationSection("templates");
            if (itemsSection != null && newItemsSection != null && newTemplatesSection != null) {
                migrateItems(itemsSection, newItemsSection, newTemplatesSection);
            }
            // Migrate templates
            ConfigurationSection templatesSection = oldSection.getConfigurationSection("templates");
            if (templatesSection != null && newTemplatesSection != null) {
                migrateTemplates(templatesSection, newTemplatesSection);
            }

            // Save file
            newConfig.save(newFile);
        }

        String renamedName = FileUtil.renameNoDuplicates(legacyFile, "menus-OLD.yml", plugin.getDataFolder()); // Rename old file
        if (renamedName != null) {
            plugin.getLogger().info("Successfully migrated menus.yml to new files in the menus folder. " +
                    "menus.yml has been renamed to " + renamedName + " and should not be used anymore.");
        } else {
            plugin.getLogger().warning("Error renaming menus.yml to menus-OLD.yml");
        }
    }

    private void migrateItems(@NotNull ConfigurationSection oldSection, @NotNull ConfigurationSection newSection, @NotNull ConfigurationSection templatesSection) {
        for (String itemName : oldSection.getKeys(false)) {
            // Get the configuration sections of new and old items
            ConfigurationSection oldItem = oldSection.getConfigurationSection(itemName);
            if (oldItem == null) continue;
            ConfigurationSection newItem;
            if (itemName.equals("skill")) { // Skill moved to templates
                newItem = templatesSection.getConfigurationSection(itemName);
            } else {
                newItem = newSection.getConfigurationSection(itemName);
            }
            if (newItem == null) continue;
            // Migrate row and column to position
            int row = oldItem.getInt("row");
            int column = oldItem.getInt("column");
            String pos = row + "," + column;
            newItem.set("pos", pos);

            Object oldMaterialObj = oldItem.get("material");
            if (oldMaterialObj instanceof String) {
                migrateBaseItem(newItem, (String) oldMaterialObj);
            } else if (oldMaterialObj instanceof List<?>) { // Skill item with context dependent material
                // Convert object to string list
                List<?> objMaterialList = (List<?>) oldMaterialObj;
                List<String> materialList = new ArrayList<>();
                for (Object o : objMaterialList) {
                    if (o instanceof String) {
                        materialList.add((String) o);
                    }
                }
                // Migrate material item contexts
                for (String material : materialList) {
                    @NotNull String @NotNull [] splitMaterial = material.split(" ", 2);
                    if (splitMaterial.length < 2) continue;
                    String contextString = splitMaterial[0].toLowerCase(Locale.ROOT);

                    ConfigurationSection contextSection = newItem.getConfigurationSection(contextString);
                    if (contextSection == null) continue;

                    String oldMaterial = splitMaterial[1];
                    migrateBaseItem(contextSection, oldMaterial);
                }
            }

            String displayName = oldItem.getString("display_name");
            newItem.set("display_name", displayName);

            if (!itemName.equals("rank")) { // Migrate lore except the rank item
                List<@NotNull String> lore = oldItem.getStringList("lore");
                if (lore.size() > 0) {
                    newItem.set("lore", lore);
                }
            }
        }
    }

    private void migrateTemplates(@NotNull ConfigurationSection oldSection, @NotNull ConfigurationSection newSection) {
        for (String templateName : oldSection.getKeys(false)) {
            ConfigurationSection oldTemplate = oldSection.getConfigurationSection(templateName);
            if (oldTemplate == null) continue;
            ConfigurationSection newTemplate = newSection.getConfigurationSection(templateName);
            if (newTemplate == null) continue;
            // Migrate materials
            Object materialObj = oldTemplate.get("material");
            if (materialObj instanceof String) { // Single material
                String oldMaterial = ((String) materialObj).toLowerCase(Locale.ROOT);
                migrateBaseItem(newTemplate, oldMaterial);
            } else if (materialObj instanceof List) { // Context dependent material
                List<@NotNull String> oldMaterials = DataUtil.castStringList(materialObj);
                for (String oldMaterialEntry : oldMaterials) { // For each entry on the old list
                    @NotNull String[] splitMaterial = oldMaterialEntry.split(" ", 2); // Split into context and rest of material
                    if (splitMaterial.length < 2) continue;
                    String contextString = splitMaterial[0].toLowerCase(Locale.ROOT);
                    // Get context section in new template
                    ConfigurationSection contextSection = newTemplate.getConfigurationSection(contextString);
                    if (contextSection == null) continue;

                    String oldMaterial = splitMaterial[1];
                    migrateBaseItem(contextSection, oldMaterial);
                }
            }

            String displayName = oldTemplate.getString("display_name");
            newTemplate.set("display_name", displayName);
            List<@NotNull String> lore = oldTemplate.getStringList("lore");
            if (lore.size() > 0) {
                newTemplate.set("lore", lore);
            }

            // Migrate template position
            if (oldTemplate.contains("pos")) {
                List<@NotNull String> posList = oldTemplate.getStringList("pos");
                for (String posEntry : posList) {
                    String[] splitEntry = posEntry.split(" ", 3);
                    if (splitEntry.length < 3) continue;

                    String contextString = splitEntry[0].toLowerCase(Locale.ROOT);
                    int row = Integer.parseInt(splitEntry[1]);
                    int column = Integer.parseInt(splitEntry[2]);
                    // Get context section in new template
                    ConfigurationSection contextSection = newTemplate.getConfigurationSection(contextString);
                    if (contextSection == null) continue;

                    contextSection.set("pos", row + "," + column);
                }
            }
        }
    }

    private void migrateBaseItem(@NotNull ConfigurationSection newItem, @NotNull String oldMaterial) {
        String[] tokens = oldMaterial.split(" ", 2);
        if (tokens.length == 0) return;
        // Migrate material
        String material = tokens[0].toLowerCase(Locale.ROOT);
        newItem.set("material", material);
        // Migrate legacy meta
        if (tokens.length < 2) return;
        String[] args = tokens[1].split(" ");
        if (args.length == 0) return;
        for (String argument : args) {
            String[] splitArg = argument.split(":");
            if (splitArg.length < 2) continue;
            String key = splitArg[0];
            String value = splitArg[1];
            switch (key.toLowerCase(Locale.ROOT)) {
                case "potion_type":
                    newItem.set("potion_data.type", value.toLowerCase(Locale.ROOT));
                    break;
                case "glow":
                    newItem.set("glow", value);
                    break;
                case "custom_model_data":
                    newItem.set("nbt.CustomModelData", Integer.parseInt(value));
                    break;
            }
        }
    }
}
