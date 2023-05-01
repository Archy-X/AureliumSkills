package dev.aurelium.skills.common.mana;

import dev.aurelium.skills.api.mana.ManaAbility;
import dev.aurelium.skills.api.skill.Skill;
import dev.aurelium.skills.common.AureliumSkillsPlugin;
import dev.aurelium.skills.common.config.OptionValue;

import java.util.HashMap;
import java.util.Map;

public class ManaAbilityManager {

    private final AureliumSkillsPlugin plugin;
    private final Map<ManaAbility, ManaAbilityConfig> configMap;

    public ManaAbilityManager(AureliumSkillsPlugin plugin) {
        this.plugin = plugin;
        configMap = new HashMap<>();
    }

    public ManaAbilityConfig getConfig(ManaAbility ability) {
        ManaAbilityConfig config = configMap.get(ability);
        if (config == null) {
            throw new IllegalArgumentException("Mana Ability " + ability + " does not have a config!");
        }
        return config;
    }

    public void addConfig(ManaAbility ability, ManaAbilityConfig config) {
        configMap.put(ability, config);
    }

    public Skill getSkill(ManaAbility ability) {
        return getConfig(ability).skill();
    }

    public boolean isEnabled(ManaAbility ability) {
        return getConfig(ability).enabled();
    }

    public double getBaseValue(ManaAbility ability) {
        return getConfig(ability).baseValue();
    }

    public double getValuePerLevel(ManaAbility ability) {
        return getConfig(ability).valuePerLevel();
    }

    public double getBaseCooldown(ManaAbility ability) {
        return getConfig(ability).baseCooldown();
    }

    public double getCooldownPerLevel(ManaAbility ability) {
        return getConfig(ability).cooldownPerLevel();
    }

    public double getBaseManaCost(ManaAbility ability) {
        return getConfig(ability).baseManaCost();
    }

    public double getManaCostPerLevel(ManaAbility ability) {
        return getConfig(ability).manaCostPerLevel();
    }

    public int getUnlock(ManaAbility ability) {
        return getConfig(ability).unlock();
    }

    public int getLevelUp(ManaAbility ability) {
        return getConfig(ability).levelUp();
    }

    public int getMaxLevel(ManaAbility ability) {
        return getConfig(ability).maxLevel();
    }

    public Map<String, OptionValue> getOptions(ManaAbility ability) {
        return getConfig(ability).options();
    }

}
