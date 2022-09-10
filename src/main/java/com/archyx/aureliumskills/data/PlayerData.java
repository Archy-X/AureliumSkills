package com.archyx.aureliumskills.data;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.ability.Ability;
import com.archyx.aureliumskills.ability.AbstractAbility;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.mana.MAbility;
import com.archyx.aureliumskills.modifier.Multiplier;
import com.archyx.aureliumskills.modifier.StatModifier;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.stats.Luck;
import com.archyx.aureliumskills.stats.Stat;
import com.archyx.aureliumskills.stats.Stats;
import com.archyx.aureliumskills.util.misc.KeyIntPair;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PlayerData {

    private final @NotNull Player player;
    private final @NotNull AureliumSkills plugin;

    private final @NotNull Map<@NotNull Skill, Integer> skillLevels;
    private final @NotNull Map<@NotNull Skill, Double> skillXp;

    private final @NotNull Map<@NotNull Stat, Double> statLevels;
    private final @NotNull Map<@NotNull String, StatModifier> statModifiers;

    private double mana;
    private @Nullable Locale locale;

    private final @NotNull Map<AbstractAbility, AbilityData> abilityData;
    private final @NotNull Map<@NotNull String, @NotNull Object> metadata;
    private @NotNull List<@NotNull KeyIntPair> unclaimedItems;

    private boolean saving;
    private boolean shouldSave;

    // Not persistent data
    private final @NotNull Map<@NotNull String, @NotNull Multiplier> multipliers;

    public PlayerData(@NotNull Player player, @NotNull AureliumSkills plugin) {
        this.player = player;
        this.plugin = plugin;
        this.skillLevels = new HashMap<>();
        this.skillXp = new HashMap<>();
        this.statLevels = new HashMap<>();
        this.statModifiers = new HashMap<>();
        this.abilityData = new HashMap<>();
        this.metadata = new HashMap<>();
        this.unclaimedItems = new LinkedList<>();
        this.saving = false;
        this.shouldSave = true;
        this.mana = OptionL.getDouble(Option.BASE_MANA);
        this.multipliers = new HashMap<>();
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    public @NotNull AureliumSkills getPlugin() {
        return plugin;
    }

    public int getSkillLevel(@Nullable Skill skill) {
        @Nullable Integer level = skillLevels.get(skill);
        return level != null ? level : 1;
    }

    public void setSkillLevel(@NotNull Skill skill, int level) {
        skillLevels.put(skill, level);
    }

    public double getSkillXp(@Nullable Skill skill) {
        @Nullable Double xp = skillXp.get(skill);
        return xp != null ? xp : 0.0;
    }

    public void setSkillXp(@NotNull Skill skill, double xp) {
        skillXp.put(skill, xp);
    }

    public void addSkillXp(@NotNull Skill skill, double amount) {
        skillXp.merge(skill, amount, Double::sum);
    }

    public double getStatLevel(@Nullable Stat stat) {
        @Nullable Double level = statLevels.get(stat);
        return level != null ? level : 0.0;
    }

    public void setStatLevel(@NotNull Stat stat, double level) {
        statLevels.put(stat, level);
    }

    public void addStatLevel(@NotNull Stat stat, double level) {
        statLevels.merge(stat, level, Double::sum);
    }

    public void addStatLevel(@NotNull Stat stat, int level) {
        @Nullable Double currentLevel = statLevels.get(stat);
        if (currentLevel != null) {
            statLevels.put(stat, currentLevel + level);
        } else {
            statLevels.put(stat, (double) level);
        }
    }

    public StatModifier getStatModifier(@NotNull String name) {
        return statModifiers.get(name);
    }

    public @NotNull Map<@NotNull String, StatModifier> getStatModifiers() {
        return statModifiers;
    }

    public void addStatModifier(@NotNull StatModifier modifier) {
        addStatModifier(modifier, true);
    }

    public void addStatModifier(@NotNull StatModifier modifier, boolean reload) {
        // Removes if already existing
        if (statModifiers.containsKey(modifier.getName())) {
            @Nullable StatModifier oldModifier = statModifiers.get(modifier.getName());
            if (oldModifier == null)
                throw new IllegalStateException("Invalid modifier stat index key: " + modifier.getName());
            if (oldModifier.getStat() == modifier.getStat() && oldModifier.getValue() == modifier.getValue()) {
                return;
            }
            removeStatModifier(modifier.getName());
        }
        statModifiers.put(modifier.getName(), modifier);
        setStatLevel(modifier.getStat(), getStatLevel(modifier.getStat()) + modifier.getValue());
        // Reloads stats
        if (reload) {
            if (modifier.getStat() == Stats.HEALTH) {
                plugin.getHealth().reload(player);
            } else if (modifier.getStat() == Stats.LUCK) {
                new Luck(plugin).reload(player);
            }
        }
    }

    public boolean removeStatModifier(@NotNull String name) {
        return removeStatModifier(name, true);
    }

    public boolean removeStatModifier(@NotNull String name, boolean reload) {
        StatModifier modifier = statModifiers.get(name);
        if (modifier == null) return false;
        Double level = statLevels.get(modifier.getStat());
        if (level == null)
            throw new IllegalStateException("Invalid modifier stat index key: " + modifier.getStat());
        setStatLevel(modifier.getStat(), level - modifier.getValue());
        statModifiers.remove(name);
        // Reloads stats
        if (reload) {
            if (modifier.getStat() == Stats.HEALTH) {
                plugin.getHealth().reload(player);
            } else if (modifier.getStat() == Stats.LUCK) {
                new Luck(plugin).reload(player);
            }
        }
        return true;
    }

    public double getMana() {
        return mana;
    }

    public double getMaxMana() {
        return OptionL.getDouble(Option.BASE_MANA) + (OptionL.getDouble(Option.WISDOM_MAX_MANA_PER_WISDOM) * getStatLevel(Stats.WISDOM));
    }

    public double getManaRegen() {
        return OptionL.getDouble(Option.REGENERATION_BASE_MANA_REGEN) + getStatLevel(Stats.REGENERATION) * OptionL.getDouble(Option.REGENERATION_MANA_MODIFIER);
    }

    public void setMana(double mana) {
        this.mana = mana;
    }

    public @Nullable Locale getLocale() {
        return locale != null ? locale : Lang.getDefaultLanguage();
    }

    public void setLocale(@Nullable Locale locale) {
        this.locale = locale;
    }

    public @NotNull AbilityData getAbilityData(@NotNull AbstractAbility ability) {
        AbilityData data = abilityData.get(ability);
        if (data == null) {
            data = new AbilityData(ability);
            abilityData.put(ability, data);
        }
        return data;
    }

    public boolean containsAbilityData(@NotNull AbstractAbility ability) {
        return abilityData.containsKey(ability);
    }

    public @NotNull Map<AbstractAbility, AbilityData> getAbilityDataMap() {
        return abilityData;
    }

    public int getAbilityLevel(@NotNull Ability ability) {
        Skill skill = ability.getSkill();
        if (getSkillLevel(skill) < plugin.getAbilityManager().getUnlock(ability)) {
            return 0;
        }
        int level = (getSkillLevel(skill) - plugin.getAbilityManager().getUnlock(ability)) / plugin.getAbilityManager().getLevelUp(ability) + 1;
        // Check max level
        if (level <= plugin.getAbilityManager().getMaxLevel(ability) || plugin.getAbilityManager().getMaxLevel(ability) == 0) {
            return level;
        } else {
            return plugin.getAbilityManager().getMaxLevel(ability);
        }
    }

    public int getManaAbilityLevel(@NotNull MAbility mAbility) {
        // Check if unlocked
        if (getSkillLevel(mAbility.getSkill()) < plugin.getManaAbilityManager().getUnlock(mAbility)) {
            return 0;
        }
        int level = (getSkillLevel(mAbility.getSkill()) - plugin.getManaAbilityManager().getUnlock(mAbility)) / plugin.getManaAbilityManager().getLevelUp(mAbility) + 1;
        // Check max level
        if (level <= plugin.getManaAbilityManager().getMaxLevel(mAbility) || plugin.getManaAbilityManager().getMaxLevel(mAbility) == 0) {
            return level;
        } else {
            return plugin.getManaAbilityManager().getMaxLevel(mAbility);
        }
    }

    public int getPowerLevel() {
        int power = 0;
        for (Map.@NotNull Entry<@NotNull Skill, Integer> entry : skillLevels.entrySet()) {
            if (OptionL.isEnabled(entry.getKey())) {
                power += entry.getValue();
            }
        }
        return power;
    }

    public @NotNull Map<@NotNull String, @NotNull Object> getMetadata() {
        return metadata;
    }

    public @NotNull List<@NotNull KeyIntPair> getUnclaimedItems() {
        return unclaimedItems;
    }

    public void clearInvalidItems() {
        // Find items that are not registered
        List<KeyIntPair> toRemove = new ArrayList<>();
        for (KeyIntPair unclaimedItem : unclaimedItems) {
            if (plugin.getItemRegistry().getItem(unclaimedItem.getKey()) == null) {
                toRemove.add(unclaimedItem);
            }
        }
        // Remove from unclaimed items list
        for (KeyIntPair unclaimedItemToRemove : toRemove) {
            unclaimedItems.remove(unclaimedItemToRemove);
        }
    }

    public void setUnclaimedItems(@NotNull List<@NotNull KeyIntPair> unclaimedItems) {
        this.unclaimedItems = unclaimedItems;
    }

    public boolean isSaving() {
        return saving;
    }

    public void setSaving(boolean saving) {
        this.saving = saving;
    }

    public boolean shouldNotSave() {
        return !shouldSave;
    }

    public void setShouldSave(boolean shouldSave) {
        this.shouldSave = shouldSave;
    }

    public double getTotalMultiplier(@Nullable Skill skill) {
        double totalMultiplier = 0.0;
        for (Multiplier multiplier : getMultipliers().values()) {
            @Nullable Skill multiplierSkill = multiplier.getSkill();
            if (multiplier.isGlobal()) {
                totalMultiplier += multiplier.getValue();
            } else if (multiplierSkill != null && multiplierSkill.equals(skill)) {
                totalMultiplier += multiplier.getValue();
            }
        }
        return totalMultiplier;
    }

    public @NotNull Map<@NotNull String, @NotNull Multiplier> getMultipliers() {
        return multipliers;
    }

    public void addMultiplier(@NotNull Multiplier multiplier) {
        multipliers.put(multiplier.getName(), multiplier);
    }

    public void removeMultiplier(@NotNull String name) {
        multipliers.remove(name);
    }
}
