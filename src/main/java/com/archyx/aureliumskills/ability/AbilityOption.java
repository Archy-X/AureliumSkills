package com.archyx.aureliumskills.ability;

import com.archyx.aureliumskills.configuration.OptionValue;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class AbilityOption {

    private final boolean enabled;
    private final double baseValue;
    private final double valuePerLevel;
    private double baseValue2;
    private double valuePerLevel2;
    private final int unlock;
    private final int levelUp;
    private final int maxLevel;
    private Map<String, OptionValue> options;

    public AbilityOption(boolean enabled, double baseValue, double valuePerLevel, int unlock, int levelUp, int maxLevel) {
        this.enabled = enabled;
        this.baseValue = baseValue;
        this.valuePerLevel = valuePerLevel;
        this.unlock = unlock;
        this.levelUp = levelUp;
        this.maxLevel = maxLevel;
    }

    public AbilityOption(boolean enabled, double baseValue, double valuePerLevel, int unlock, int levelUp, int maxLevel, Map<String, OptionValue> options) {
        this.enabled = enabled;
        this.baseValue = baseValue;
        this.valuePerLevel = valuePerLevel;
        this.unlock = unlock;
        this.levelUp = levelUp;
        this.maxLevel = maxLevel;
        this.options = options;
    }

    public AbilityOption(boolean enabled, double baseValue1, double valuePerLevel1, double baseValue2, double valuePerLevel2, int unlock, int levelUp, int maxLevel) {
        this.enabled = enabled;
        this.baseValue = baseValue1;
        this.valuePerLevel = valuePerLevel1;
        this.baseValue2 = baseValue2;
        this.valuePerLevel2 = valuePerLevel2;
        this.unlock = unlock;
        this.levelUp = levelUp;
        this.maxLevel = maxLevel;
    }

    public AbilityOption(boolean enabled, double baseValue1, double valuePerLevel1, double baseValue2, double valuePerLevel2, int unlock, int levelUp, int maxLevel, Map<String, OptionValue> options) {
        this.enabled = enabled;
        this.baseValue = baseValue1;
        this.valuePerLevel = valuePerLevel1;
        this.baseValue2 = baseValue2;
        this.valuePerLevel2 = valuePerLevel2;
        this.unlock = unlock;
        this.levelUp = levelUp;
        this.maxLevel = maxLevel;
        this.options = options;
    }

    public double getBaseValue() {
        return baseValue;
    }

    public double getValuePerLevel() {
        return valuePerLevel;
    }

    public double getBaseValue2() {
        return baseValue2;
    }

    public double getValuePerLevel2() {
        return valuePerLevel2;
    }

    public boolean isEnabled() {
        return enabled;
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

    public @Nullable OptionValue getOption(String key) {
        return options.get(key);
    }

}
