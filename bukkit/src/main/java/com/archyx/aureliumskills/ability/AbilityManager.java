package com.archyx.aureliumskills.ability;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.configuration.OptionValue;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.mana.MAbility;
import com.archyx.aureliumskills.mana.ManaAbilityOption;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.util.text.TextUtil;
import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;

public class AbilityManager {

    private final Map<Ability, AbilityOption> abilityOptions;
    private final Map<MAbility, ManaAbilityOption> manaAbilityOptions;
    private final AureliumSkills plugin;

    public AbilityManager(AureliumSkills plugin) {
        this.plugin = plugin;
        abilityOptions = new HashMap<>();
        manaAbilityOptions = new HashMap<>();
    }

    public void loadOptions() {
        File file = new File(plugin.getDataFolder(), "abilities_config.yml");
        if (!file.exists()) {
            plugin.saveResource("abilities_config.yml", false);
        }
        FileConfiguration config = updateFile(file, YamlConfiguration.loadConfiguration(file));
        migrateOptions(file, config);
        //Loads ability options
        int amountLoaded = 0;
        int amountDisabled = 0;
        long startTime = System.currentTimeMillis();
        ConfigurationSection abilities = config.getConfigurationSection("abilities");
        if (abilities != null) {
            for (Skill skill : plugin.getSkillRegistry().getSkills()) {
                String skillName = skill.name().toLowerCase(Locale.ENGLISH);
                ConfigurationSection skillAbilities = abilities.getConfigurationSection(skillName);
                if (skillAbilities != null) {
                    for (String abilityName : skillAbilities.getKeys(false)) {
                        // Check if ability is valid
                        boolean hasKey = false;
                        for (Ability ability : Ability.values()) {
                            if (abilityName.toUpperCase().equals(ability.name())) {
                                hasKey = true;
                                break;
                            }
                        }
                        if (hasKey) {
                            String path = "abilities." + skillName + "." + abilityName + ".";
                            Ability ability = Ability.valueOf(abilityName.toUpperCase());
                            boolean enabled = config.getBoolean(path + "enabled", true);
                            if (!enabled) {
                                amountDisabled++;
                            }
                            double baseValue = config.getDouble(path + "base", ability.getDefaultBaseValue());
                            double valuePerLevel = config.getDouble(path + "per_level", ability.getDefaultValuePerLevel());
                            // Get default unlock value
                            int defUnlock = 2;
                            for (int i = 0; i < skill.getAbilities().size(); i++) {
                                if (skill.getAbilities().get(i).get() == ability) {
                                    defUnlock += i;
                                    break;
                                }
                            }
                            int unlock = config.getInt(path + "unlock", defUnlock);
                            int levelUp = config.getInt(path + "level_up", 5);
                            int maxLevel = config.getInt(path + "max_level", 0);
                            // Load options
                            Set<String> optionKeys = getOptionKeys(ability);
                            Map<String, OptionValue> options = null;
                            if (optionKeys != null) {
                                options = new HashMap<>();
                                for (String key : optionKeys) {
                                    Object value = config.get(path + key);
                                    if (value != null) {
                                        options.put(key, new OptionValue(value));
                                    }
                                }
                            }
                            AbilityOption option;
                            //Checks if ability has 2 values
                            if (ability.hasTwoValues()) {
                                double baseValue2 = config.getDouble(path + "base_2", ability.getDefaultBaseValue2());
                                double valuePerLevel2 = config.getDouble(path + "per_level_2", ability.getDefaultValuePerLevel2());
                                if (options != null) {
                                    option = new AbilityOption(enabled, baseValue, valuePerLevel, baseValue2, valuePerLevel2, unlock, levelUp, maxLevel, options);
                                } else {
                                    option = new AbilityOption(enabled, baseValue, valuePerLevel, baseValue2, valuePerLevel2, unlock, levelUp, maxLevel);
                                }
                            }
                            else {
                                if (options != null) {
                                    option = new AbilityOption(enabled, baseValue, valuePerLevel, unlock, levelUp, maxLevel, options);
                                } else {
                                    option = new AbilityOption(enabled, baseValue, valuePerLevel, unlock, levelUp, maxLevel);
                                }
                            }
                            abilityOptions.put(ability, option);
                            amountLoaded++;
                        }
                    }
                }
            }
        }
        ConfigurationSection manaAbilities = config.getConfigurationSection("mana_abilities");
        if (manaAbilities != null) {
            for (String manaAbilityName : manaAbilities.getKeys(false)){
                boolean hasKey = false;
                for (MAbility manaAbility : MAbility.values()) {
                    if (manaAbilityName.toUpperCase().equals(manaAbility.name())) {
                        hasKey = true;
                        break;
                    }
                }
                if (hasKey) {
                    MAbility mAbility = MAbility.valueOf(manaAbilityName.toUpperCase());
                    String path = "mana_abilities." + manaAbilityName + ".";
                    boolean enabled = config.getBoolean(path + "enabled", true);
                    if (!enabled) {
                        amountDisabled++;
                    }
                    double baseValue = config.getDouble(path + "base_value", mAbility.getDefaultBaseValue());
                    double valuePerLevel = config.getDouble(path + "value_per_level", mAbility.getDefaultValuePerLevel());
                    double cooldown = config.getDouble(path + "cooldown", mAbility.getDefaultBaseCooldown());
                    double cooldownPerLevel = config.getDouble(path + "cooldown_per_level", mAbility.getDefaultCooldownPerLevel());
                    double manaCost = config.getDouble(path + "mana_cost", mAbility.getDefaultBaseManaCost());
                    double manaCostPerLevel = config.getDouble(path + "mana_cost_per_level", mAbility.getDefaultManaCostPerLevel());
                    int unlock = config.getInt(path + "unlock", 7);
                    int levelUp = config.getInt(path + "level_up", 7);
                    int maxLevel = config.getInt(path + "max_level", 0);
                    // Load options
                    Set<String> optionKeys = plugin.getManaAbilityManager().getOptionKeys(mAbility);
                    Map<String, OptionValue> options = null;
                    if (optionKeys != null) {
                        options = new HashMap<>();
                        for (String key : optionKeys) {
                            Object value = config.get(path + key);
                            if (value != null) {
                                options.put(key, new OptionValue(value));
                            }
                        }
                    }
                    ManaAbilityOption option;
                    if (options != null) {
                        option = new ManaAbilityOption(enabled, baseValue, valuePerLevel, cooldown, cooldownPerLevel, manaCost, manaCostPerLevel, unlock, levelUp, maxLevel, options);
                    } else {
                        option = new ManaAbilityOption(enabled, baseValue, valuePerLevel, cooldown, cooldownPerLevel, manaCost, manaCostPerLevel, unlock, levelUp, maxLevel);
                    }
                    manaAbilityOptions.put(mAbility, option);
                    amountLoaded++;
                }
            }
        }
        long endTime = System.currentTimeMillis();
        long timeElapsed = endTime - startTime;
        if (amountDisabled > 0) {
            plugin.getLogger().info("Disabled " + amountDisabled + " Abilities");
        }
        plugin.getLogger().info("Loaded " + amountLoaded + " Ability Options in " + timeElapsed + "ms");
    }

    private FileConfiguration updateFile(File file, FileConfiguration config) {
        if (config.contains("file_version")) {
            InputStream stream = plugin.getResource("abilities_config.yml");
            if (stream != null) {
                int currentVersion = config.getInt("file_version");
                FileConfiguration imbConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
                int imbVersion = imbConfig.getInt("file_version");
                //If versions do not match
                if (currentVersion != imbVersion) {
                    try {
                        ConfigurationSection configSection = imbConfig.getConfigurationSection("");
                        if (configSection != null) {
                            for (String key : configSection.getKeys(true)) {
                                if (!configSection.isConfigurationSection(key)) {
                                    if (!config.contains(key)) {
                                        config.set(key, imbConfig.get(key));
                                    }
                                }
                            }
                        }
                        config.set("file_version", imbVersion);
                        config.save(file);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    private void migrateOptions(File file, FileConfiguration abilitiesConfig) {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection abilities = config.getConfigurationSection("abilities");
        if (abilities == null) return;
        try {
            for (String abilityName : abilities.getKeys(false)) {
                String newKey = TextUtil.replace(abilityName, "-", "_").toUpperCase();
                if (isAbility(newKey)) {
                    Ability ability = Ability.valueOf(newKey);
                    boolean enabled = abilities.getBoolean(abilityName + ".enabled", true);
                    double base = abilities.getDouble(abilityName + ".base", ability.getDefaultBaseValue());
                    double per_level = abilities.getDouble(abilityName + ".per-level", ability.getDefaultValuePerLevel());
                    String path = "abilities." + ability.getSkill().name().toLowerCase(Locale.ENGLISH) + "." + newKey.toLowerCase(Locale.ENGLISH) + ".";
                    abilitiesConfig.set(path + "enabled", enabled);
                    abilitiesConfig.set(path + "base", base);
                    abilitiesConfig.set(path + "per_level", per_level);
                    if (ability.hasTwoValues()) {
                        double base_2 = abilities.getDouble(abilityName + ".base-2", ability.getDefaultBaseValue2());
                        double per_level_2 = abilities.getDouble(abilityName + ".per_level-2", ability.getDefaultValuePerLevel2());
                        abilitiesConfig.set(path + "base_2", base_2);
                        abilitiesConfig.set(path + "per_level_2", per_level_2);
                    }
                }
            }
            config.set("abilities", null);
            ConfigurationSection manaAbilities = config.getConfigurationSection("mana-abilities");
            if (manaAbilities != null) {
                for (String manaAbilityName : manaAbilities.getKeys(false)) {
                    String newKey = TextUtil.replace(manaAbilityName, "-", "_").toUpperCase();
                    if (isManaAbility(newKey)) {
                        MAbility mAbility = MAbility.valueOf(newKey);
                        boolean enabled = manaAbilities.getBoolean(manaAbilityName + ".enabled", true);
                        double base = manaAbilities.getDouble(manaAbilityName + ".base-value", mAbility.getDefaultBaseValue());
                        double per_level = manaAbilities.getDouble(manaAbilityName + ".value-per-level", mAbility.getDefaultValuePerLevel());
                        double cooldown = manaAbilities.getDouble(manaAbilityName + ".cooldown", mAbility.getDefaultBaseCooldown());
                        double cooldown_per_level = manaAbilities.getDouble(manaAbilityName + ".cooldown-per-level", mAbility.getDefaultCooldownPerLevel());
                        double mana_cost = manaAbilities.getDouble(manaAbilityName + ".mana-cost", mAbility.getDefaultBaseManaCost());
                        double mana_cost_per_level = manaAbilities.getDouble(manaAbilityName + ".mana-cost-per-level", mAbility.getDefaultManaCostPerLevel());
                        String path = "mana_abilities." + newKey.toLowerCase(Locale.ENGLISH) + ".";
                        abilitiesConfig.set(path + "enabled", enabled);
                        abilitiesConfig.set(path + "base_value", base);
                        abilitiesConfig.set(path + "value_per_level", per_level);
                        abilitiesConfig.set(path + "cooldown", cooldown);
                        abilitiesConfig.set(path + "cooldown_per_level", cooldown_per_level);
                        abilitiesConfig.set(path + "mana_cost", mana_cost);
                        abilitiesConfig.set(path + "mana_cost_per_level", mana_cost_per_level);
                    }
                }
                config.set("mana-abilities", null);
            }
            plugin.saveConfig();
            abilitiesConfig.save(file);
            Bukkit.getLogger().warning("[AureliumSkills] Your existing ability options have been migrated to abilities_config.yml and the old options in config.yml have been deleted, this is normal if you are updating to Alpha 1.6.0 or above from before Alpha 1.6.0!");
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getLogger().severe("[AureliumSkills] An error occurred while migrating ability options, please report this as a bug!");
        }
    }

    public AbilityOption getAbilityOption(Ability ability) {
        return abilityOptions.get(ability);
    }

    public ManaAbilityOption getAbilityOption(MAbility mAbility) {
        return manaAbilityOptions.get(mAbility);
    }

    public boolean isEnabled(Ability ability) {
        if (abilityOptions.containsKey(ability)) {
            return abilityOptions.get(ability).isEnabled();
        }
        return true;
    }

    public boolean isPlayerEnabled(AbstractAbility ability, PlayerData playerData) {
        return !playerData.getAbilityData(ability).getBoolean("disabled");
    }

    public boolean isEnabled(MAbility mAbility) {
        if (manaAbilityOptions.containsKey(mAbility)) {
            return manaAbilityOptions.get(mAbility).isEnabled();
        }
        return true;
    }

    public double getValue(Ability ability, int level) {
        return getBaseValue(ability) + (getValuePerLevel(ability) * (level - 1));
    }

    public double getBaseValue(Ability ability) {
        AbilityOption option = getAbilityOption(ability);
        if (option != null) {
            return option.getBaseValue();
        }
        return ability.getDefaultBaseValue();
    }

    public double getValuePerLevel(Ability ability) {
        AbilityOption option = getAbilityOption(ability);
        if (option != null) {
            return option.getValuePerLevel();
        }
        return ability.getDefaultValuePerLevel();
    }

    public double getValue2(Ability ability, int level) {
        return getBaseValue2(ability) + (getValuePerLevel2(ability) * (level - 1));
    }

    public double getBaseValue2(Ability ability) {
        AbilityOption option = getAbilityOption(ability);
        if (option != null) {
            return option.getBaseValue2();
        }
        return ability.getDefaultBaseValue2();
    }

    public double getValuePerLevel2(Ability ability) {
        AbilityOption option = getAbilityOption(ability);
        if (option != null) {
            return option.getValuePerLevel2();
        }
        return ability.getDefaultValuePerLevel2();
    }

    public int getUnlock(Ability ability) {
        AbilityOption option = getAbilityOption(ability);
        if (option != null) {
            return option.getUnlock();
        }
        int defUnlock = 2;
        Skill skill = ability.getSkill();
        for (int i = 0; i < skill.getAbilities().size(); i++) {
            if (skill.getAbilities().get(i).get() == ability) {
                defUnlock += i;
                break;
            }
        }
        return defUnlock;
    }

    public int getLevelUp(Ability ability) {
        AbilityOption option = getAbilityOption(ability);
        if (option != null) {
            return option.getLevelUp();
        }
        return 5;
    }

    public int getMaxLevel(Ability ability) {
        AbilityOption option = getAbilityOption(ability);
        if (option != null) {
            return option.getMaxLevel();
        }
        return 0;
    }

    /**
     * Gets a list of abilities unlocked or leveled up at a certain level
     * @param skill The skill
     * @param level The skill level
     * @return A list of abilities
     */
    public List<Ability> getAbilities(Skill skill, int level) {
        ImmutableList<Supplier<Ability>> skillAbilities = skill.getAbilities();
        List<Ability> abilities = new ArrayList<>();
        for (Supplier<Ability> abilitySupplier : skillAbilities) {
            Ability ability = abilitySupplier.get();
            if (level >= getUnlock(ability) && (level - getUnlock(ability)) % getLevelUp(ability) == 0) {
                abilities.add(ability);
            }
        }
        return abilities;
    }

    public OptionValue getOption(Ability ability, String key) {
        AbilityOption option = getAbilityOption(ability);
        if (option != null) {
            OptionValue optionValue = option.getOption(key);
            if (optionValue != null) {
                return optionValue;
            } else {
                return ability.getDefaultOptions().get(key);
            }
        } else {
            return ability.getDefaultOptions().get(key);
        }
    }

    public boolean getOptionAsBooleanElseTrue(Ability ability, String key) {
        OptionValue value = getOption(ability, key);
        if (value != null) {
            if (value.getValue() != null) {
                return value.asBoolean();
            }
        }
        return true;
    }

    @Nullable
    public Set<String> getOptionKeys(Ability ability) {
        if (ability.getDefaultOptions() != null) {
            return ability.getDefaultOptions().keySet();
        }
        return null;
    }

    public void sendMessage(Player player, String message) {
        if (OptionL.getBoolean(Option.ACTION_BAR_ABILITY) && OptionL.getBoolean(Option.ACTION_BAR_ENABLED)) {
            plugin.getActionBar().sendAbilityActionBar(player, message);
        } else {
            PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
            if (playerData == null) return;
            if (message == null || message.equals("")) return; // Don't send empty message
            player.sendMessage(AureliumSkills.getPrefix(playerData.getLocale()) + message);
        }
    }

    private boolean isAbility(String abilityName) {
        for (Ability ability : Ability.values()) {
            if (ability.toString().equals(abilityName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isManaAbility(String manaAbilityName) {
        for (MAbility mAbility : MAbility.values()) {
            if (mAbility.toString().equals(manaAbilityName)) {
                return true;
            }
        }
        return false;
    }

}
