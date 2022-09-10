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

import java.util.*;

public class PlayerData {

    private final Player player;
    private final AureliumSkills plugin;

    private final Map<Skill, Integer> skillLevels;
    private final Map<Skill, Double> skillXp;

    private final Map<Stat, Double> statLevels;
    private final Map<String, StatModifier> statModifiers;

    private double mana;
    private Locale locale;

    private final Map<AbstractAbility, AbilityData> abilityData;
    private final Map<String, Object> metadata;
    private List<KeyIntPair> unclaimedItems;

    private boolean saving;
    private boolean shouldSave;

    // Not persistent data
    private final Map<String, Multiplier> multipliers;

    public PlayerData(Player player, AureliumSkills plugin) {
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

    public Player getPlayer() {
        return player;
    }

    public AureliumSkills getPlugin() {
        return plugin;
    }

    public int getSkillLevel(Skill skill) {
        Integer level = skillLevels.get(skill);
        return level != null ? level : 1;
    }

    public void setSkillLevel(Skill skill, int level) {
        skillLevels.put(skill, level);
    }

    public double getSkillXp(Skill skill) {
        Double xp = skillXp.get(skill);
        return xp != null ? xp : 0.0;
    }

    public void setSkillXp(Skill skill, double xp) {
        skillXp.put(skill, xp);
    }

    public void addSkillXp(Skill skill, double amount) {
        skillXp.merge(skill, amount, Double::sum);
    }

    public double getStatLevel(Stat stat) {
        Double level = statLevels.get(stat);
        return level != null ? level : 0.0;
    }

    public void setStatLevel(Stat stat, double level) {
        statLevels.put(stat, level);
    }

    public void addStatLevel(Stat stat, double level) {
        statLevels.merge(stat, level, Double::sum);
    }

    public void addStatLevel(Stat stat, int level) {
        Double currentLevel = statLevels.get(stat);
        if (currentLevel != null) {
            statLevels.put(stat, currentLevel + level);
        } else {
            statLevels.put(stat, (double) level);
        }
    }

    public StatModifier getStatModifier(String name) {
        return statModifiers.get(name);
    }

    public Map<String, StatModifier> getStatModifiers() {
        return statModifiers;
    }

    public void addStatModifier(StatModifier modifier) {
        addStatModifier(modifier, true);
    }

    public void addStatModifier(StatModifier modifier, boolean reload) {
        // Removes if already existing
        if (statModifiers.containsKey(modifier.getName())) {
            StatModifier oldModifier = statModifiers.get(modifier.getName());
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

    public boolean removeStatModifier(String name) {
        return removeStatModifier(name, true);
    }

    public boolean removeStatModifier(String name, boolean reload) {
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

    public Locale getLocale() {
        return locale != null ? locale : Lang.getDefaultLanguage();
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public AbilityData getAbilityData(AbstractAbility ability) {
        AbilityData data = abilityData.get(ability);
        if (data == null) {
            data = new AbilityData(ability);
            abilityData.put(ability, data);
        }
        return data;
    }

    public boolean containsAbilityData(AbstractAbility ability) {
        return abilityData.containsKey(ability);
    }

    public Map<AbstractAbility, AbilityData> getAbilityDataMap() {
        return abilityData;
    }

    public int getAbilityLevel(Ability ability) {
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

    public int getManaAbilityLevel(MAbility mAbility) {
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
        for (Map.Entry<Skill, Integer> entry : skillLevels.entrySet()) {
            if (OptionL.isEnabled(entry.getKey())) {
                power += entry.getValue();
            }
        }
        return power;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public List<KeyIntPair> getUnclaimedItems() {
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

    public void setUnclaimedItems(List<KeyIntPair> unclaimedItems) {
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

    public double getTotalMultiplier(Skill skill) {
        double totalMultiplier = 0.0;
        for (Multiplier multiplier : getMultipliers().values()) {
            Skill multiplierSkill = multiplier.getSkill();
            if (multiplier.isGlobal()) {
                totalMultiplier += multiplier.getValue();
            } else if (multiplierSkill != null && multiplierSkill.equals(skill)) {
                totalMultiplier += multiplier.getValue();
            }
        }
        return totalMultiplier;
    }

    public Map<String, Multiplier> getMultipliers() {
        return multipliers;
    }

    public void addMultiplier(Multiplier multiplier) {
        multipliers.put(multiplier.getName(), multiplier);
    }

    public void removeMultiplier(String name) {
        multipliers.remove(name);
    }
}
