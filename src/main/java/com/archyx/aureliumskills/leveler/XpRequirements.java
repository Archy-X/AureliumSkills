package com.archyx.aureliumskills.leveler;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.skills.Skill;
import com.udojava.evalex.Expression;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public class XpRequirements {

    private final AureliumSkills plugin;
    private final @NotNull List<Integer> defaultXpRequirements;
    private final @NotNull Map<Skill, List<Integer>> skillXpRequirements;

    public XpRequirements(AureliumSkills plugin) {
        this.plugin = plugin;
        this.defaultXpRequirements = new ArrayList<>();
        this.skillXpRequirements = new HashMap<>();
    }

    public int getXpRequired(Skill skill, int level) {
        // Use skill specific xp requirements if exists
        List<Integer> skillList = skillXpRequirements.get(skill);
        if (skillList != null) {
            if (skillList.size() > level - 2) {
                return skillList.get(level - 2);
            } else {
                return 0;
            }
        }
        // Else use default
        if (defaultXpRequirements.size() > level - 2) {
            return defaultXpRequirements.get(level - 2);
        } else {
            return 0;
        }
    }

    public int getListSize(Skill skill) {
        List<Integer> skillList = skillXpRequirements.get(skill);
        if (skillList != null) {
            return skillList.size();
        }
        return defaultXpRequirements.size();
    }

    public void loadXpRequirements() {
        File file = new File(plugin.getDataFolder(), "xp_requirements.yml");
        double oldMultiplier = 0.0;
        if (!file.exists()) {
            plugin.saveResource("xp_requirements.yml", false);
            // Get old multiplier if xp requirements is new
            oldMultiplier = plugin.getConfig().getDouble("skill-level-requirements-multiplier", 0.0);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        loadDefaultSection(file, config, oldMultiplier);
        // Load optional section for each skill
        for (Skill skill : plugin.getSkillRegistry().getSkills()) {
            skillXpRequirements.remove(skill); // Remove to account for deleted section
            loadSkillSection(file, config, skill);
        }
    }

    private void loadDefaultSection(@NotNull File file, @NotNull FileConfiguration config, double oldMultiplier) {
        ConfigurationSection section = config.getConfigurationSection("default");
        if (section != null) {
            if (oldMultiplier != 0.0) { // Migrate old multiplier
                section.set("multiplier", oldMultiplier);
                plugin.getConfig().set("skill-level-requirements-multiplier", null);
                try {
                    config.save(file);
                    plugin.saveConfig();
                    plugin.getLogger().warning("Successfully migrated skill-level-requirements-multiplier from config.yml to new default.multiplier in xp_requirements.yml");
                } catch (IOException e) {
                    plugin.getLogger().warning("Failed to migrate skill-level-requirements-multiplier from config.yml to new default.multiplier in xp_requirements.yml");
                    e.printStackTrace();
                }
            }
            Expression expression = getXpExpression(section);
            // Add xp requirement for each level
            defaultXpRequirements.clear();
            int highestMaxLevel = plugin.getOptionLoader().getHighestMaxLevel();
            for (int i = 0; i < highestMaxLevel; i++) {
                expression.setVariable("level", BigDecimal.valueOf(i + 2));
                defaultXpRequirements.add((int) Math.round(expression.eval().doubleValue()));
            }
        } else {
            addDefaultXpRequirements();
        }
    }

    private void loadSkillSection(File file, @NotNull FileConfiguration config, @NotNull Skill skill) {
        ConfigurationSection section = config.getConfigurationSection("skills." + skill.toString().toLowerCase(Locale.ROOT));
        if (section == null) return;

        Expression expression = getXpExpression(section);
        int maxLevel = OptionL.getMaxLevel(skill);
        // Add evaluated expression for each level to list
        List<Integer> xpRequirements = new ArrayList<>();
        for (int i = 0; i < maxLevel; i++) {
            expression.setVariable("level", BigDecimal.valueOf(i + 2));
            xpRequirements.add((int) Math.round(expression.eval().doubleValue()));
        }
        skillXpRequirements.put(skill, xpRequirements);
    }

    private @NotNull Expression getXpExpression(@NotNull ConfigurationSection section) {
        String expressionString = section.getString("expression");
        Expression expression = new Expression(expressionString);
        // Set variables
        for (String variable : section.getKeys(false)) {
            if (variable.equals("expression")) continue;
            double variableValue = section.getDouble(variable);
            expression.setVariable(variable, BigDecimal.valueOf(variableValue));
        }
        return expression;
    }

    private void addDefaultXpRequirements() {
        defaultXpRequirements.clear();
        int highestMaxLevel = plugin.getOptionLoader().getHighestMaxLevel();
        for (int i = 0; i < highestMaxLevel - 1; i++) {
            defaultXpRequirements.add(100 * i * i + 100);
        }
    }

}
