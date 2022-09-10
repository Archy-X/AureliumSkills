package com.archyx.aureliumskills.mana;

import com.archyx.aureliumskills.configuration.OptionValue;

import java.util.Map;

public class ManaAbilityOption {

    private final boolean enabled;
    private final double baseValue;
    private final double valuePerLevel;
    private final double baseCooldown;
    private final double cooldownPerLevel;
    private final double baseManaCost;
    private final double manaCostPerLevel;
    private final int unlock;
    private final int levelUp;
    private final int maxLevel;
    private Map<String, OptionValue> options;

    public ManaAbilityOption(boolean enabled, double baseValue, double valuePerLevel, double baseCooldown, double cooldownPerLevel, double baseManaCost, double manaCostPerLevel, int unlock, int levelUp, int maxLevel) {
        this.enabled = enabled;
        this.baseValue = baseValue;
        this.valuePerLevel = valuePerLevel;
        this.baseCooldown = baseCooldown;
        this.cooldownPerLevel = cooldownPerLevel;
        this.baseManaCost = baseManaCost;
        this.manaCostPerLevel = manaCostPerLevel;
        this.unlock = unlock;
        this.levelUp = levelUp;
        this.maxLevel = maxLevel;
    }

    public ManaAbilityOption(boolean enabled, double baseValue, double valuePerLevel, double baseCooldown, double cooldownPerLevel, double baseManaCost, double manaCostPerLevel, int unlock, int levelUp, int maxLevel, Map<String, OptionValue> options) {
        this.enabled = enabled;
        this.baseValue = baseValue;
        this.valuePerLevel = valuePerLevel;
        this.baseCooldown = baseCooldown;
        this.cooldownPerLevel = cooldownPerLevel;
        this.baseManaCost = baseManaCost;
        this.manaCostPerLevel = manaCostPerLevel;
        this.unlock = unlock;
        this.levelUp = levelUp;
        this.maxLevel = maxLevel;
        this.options = options;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public double getBaseValue() {
        return baseValue;
    }

    public double getValuePerLevel() {
        return valuePerLevel;
    }

    public double getBaseCooldown() {
        return baseCooldown;
    }

    public double getCooldownPerLevel() {
        return cooldownPerLevel;
    }

    public double getBaseManaCost() {
        return baseManaCost;
    }

    public double getManaCostPerLevel() {
        return manaCostPerLevel;
    }

    public int getUnlock() {
        return unlock;
    }

    public int getLevelUp() {
        return levelUp;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public OptionValue getOption(String key) {
        return options.get(key);
    }

}
