package com.archyx.aureliumskills.menu.templates;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.ability.Ability;
import com.archyx.aureliumskills.ability.AbilityManager;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.lang.MenuMessage;
import com.archyx.aureliumskills.mana.MAbility;
import com.archyx.aureliumskills.mana.ManaAbilityManager;
import com.archyx.aureliumskills.rewards.MoneyReward;
import com.archyx.aureliumskills.rewards.Reward;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.util.math.NumberUtil;
import com.archyx.aureliumskills.util.math.RomanNumber;
import com.archyx.aureliumskills.util.text.TextUtil;
import com.google.common.collect.ImmutableList;

import java.util.Locale;

public class ProgressLevelItem {

    private final AureliumSkills plugin;

    public ProgressLevelItem(AureliumSkills plugin) {
        this.plugin = plugin;
    }

    public String getRewardsLore(Skill skill, int level, Locale locale) {
        ImmutableList<Reward> rewards = plugin.getRewardManager().getRewardTable(skill).getRewards(level);
        StringBuilder message = new StringBuilder();
        double totalMoney = 0;
        for (Reward reward : rewards) {
            message.append(reward.getMenuMessage(locale));
            if (reward instanceof MoneyReward) {
                totalMoney += ((MoneyReward) reward).getAmount();
            }
        }
        // Legacy money rewards
        if (plugin.isVaultEnabled()) {
            if (OptionL.getBoolean(Option.SKILL_MONEY_REWARDS_ENABLED)) {
                double base = OptionL.getDouble(Option.SKILL_MONEY_REWARDS_BASE);
                double multiplier = OptionL.getDouble(Option.SKILL_MONEY_REWARDS_MULTIPLIER);
                totalMoney += base + (multiplier * level * level);
            }
        }
        if (totalMoney > 0) {
            message.append(TextUtil.replace(Lang.getMessage(MenuMessage.MONEY_REWARD, locale), "{amount}", NumberUtil.format2(totalMoney)));
        }
        return TextUtil.replace(Lang.getMessage(MenuMessage.REWARDS, locale),"{rewards}", message.toString());
    }

    public String getAbilityLore(Skill skill, int level, Locale locale) {
        StringBuilder abilityLore = new StringBuilder();
        if (skill.getAbilities().size() == 5) {
            AbilityManager manager = plugin.getAbilityManager();
            for (Ability ability : manager.getAbilities(skill, level)) {
                if (manager.isEnabled(ability)) {
                    if (level == manager.getUnlock(ability)) {
                        abilityLore.append(TextUtil.replace(Lang.getMessage(MenuMessage.ABILITY_UNLOCK, locale)
                                , "{ability}", ability.getDisplayName(locale)
                                , "{desc}", TextUtil.replace(ability.getDescription(locale)
                                        , "{value_2}", NumberUtil.format1(manager.getValue2(ability, 1))
                                        , "{value}", NumberUtil.format1(manager.getValue(ability, 1)))));
                    } else {
                        int abilityLevel = ((level - manager.getUnlock(ability)) / manager.getLevelUp(ability)) + 1;
                        if (abilityLevel <= manager.getMaxLevel(ability) || manager.getMaxLevel(ability) == 0) { // Check max level
                            abilityLore.append(TextUtil.replace(Lang.getMessage(MenuMessage.ABILITY_LEVEL, locale)
                                    , "{ability}", ability.getDisplayName(locale)
                                    , "{level}", RomanNumber.toRoman(abilityLevel)
                                    , "{desc}", TextUtil.replace(ability.getDescription(locale)
                                            , "{value_2}", NumberUtil.format1(manager.getValue2(ability, abilityLevel))
                                            , "{value}", NumberUtil.format1(manager.getValue(ability, abilityLevel)))));
                        }
                    }
                }
            }
        }
        return abilityLore.toString();
    }

    public String getManaAbilityLore(Skill skill, int level, Locale locale) {
        ManaAbilityManager manager = plugin.getManaAbilityManager();
        MAbility mAbility = manager.getManaAbility(skill, level);
        StringBuilder manaAbilityLore = new StringBuilder();
        if (mAbility != null) {
            if (plugin.getAbilityManager().isEnabled(mAbility)) {
                if (level == manager.getUnlock(mAbility)) {
                    manaAbilityLore.append(TextUtil.replace(Lang.getMessage(MenuMessage.MANA_ABILITY_UNLOCK, locale)
                            , "{mana_ability}", mAbility.getDisplayName(locale)
                            , "{desc}", TextUtil.replace(mAbility.getDescription(locale)
                                    , "{value}", NumberUtil.format1(manager.getDisplayValue(mAbility, 1))
                                    , "{duration}", NumberUtil.format1(getDuration(mAbility, 1))
                                    , "{haste_level}", String.valueOf(manager.getOptionAsInt(MAbility.SPEED_MINE, "haste_level", 10)))));
                }
                else {
                    int manaAbilityLevel = ((level - manager.getUnlock(mAbility)) / manager.getLevelUp(mAbility)) + 1;
                    if (manaAbilityLevel <= manager.getMaxLevel(mAbility) || manager.getMaxLevel(mAbility) == 0) {
                        manaAbilityLore.append(TextUtil.replace(Lang.getMessage(MenuMessage.MANA_ABILITY_LEVEL, locale)
                                , "{mana_ability}", mAbility.getDisplayName(locale)
                                , "{level}", RomanNumber.toRoman(manaAbilityLevel)
                                , "{desc}", TextUtil.replace(mAbility.getDescription(locale)
                                        , "{value}", NumberUtil.format1(manager.getDisplayValue(mAbility, manaAbilityLevel))
                                        , "{duration}", NumberUtil.format1(getDuration(mAbility, manaAbilityLevel))
                                        , "{haste_level}", String.valueOf(manager.getOptionAsInt(MAbility.SPEED_MINE, "haste_level", 10)))));
                    }
                }
            }
        }
        return manaAbilityLore.toString();
    }

    private double getDuration(MAbility mAbility, int level) {
        if (mAbility == MAbility.LIGHTNING_BLADE) {
            double baseDuration = plugin.getManaAbilityManager().getOptionAsDouble(MAbility.LIGHTNING_BLADE, "base_duration");
            double durationPerLevel = plugin.getManaAbilityManager().getOptionAsDouble(MAbility.LIGHTNING_BLADE, "duration_per_level");
            return baseDuration + (durationPerLevel * (level - 1));
        } else {
            return plugin.getManaAbilityManager().getValue(mAbility, level);
        }
    }
}
