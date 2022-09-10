package com.archyx.aureliumskills.menus.common;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.ability.Ability;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.lang.ManaAbilityMessage;
import com.archyx.aureliumskills.lang.MenuMessage;
import com.archyx.aureliumskills.lang.MessageKey;
import com.archyx.aureliumskills.mana.MAbility;
import com.archyx.aureliumskills.mana.ManaAbilityManager;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.stats.Stat;
import com.archyx.aureliumskills.util.math.NumberUtil;
import com.archyx.aureliumskills.util.math.RomanNumber;
import com.archyx.aureliumskills.util.text.TextUtil;
import com.archyx.slate.item.provider.PlaceholderType;
import com.archyx.slate.item.provider.TemplateItemProvider;
import com.archyx.slate.menu.ActiveMenu;
import com.google.common.collect.ImmutableList;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

public abstract class AbstractSkillItem extends AbstractItem implements TemplateItemProvider<Skill> {

    public AbstractSkillItem(AureliumSkills plugin) {
        super(plugin);
    }

    @Override
    public Class<Skill> getContext() {
        return Skill.class;
    }

    @Override
    public String onPlaceholderReplace(String placeholder, Player player, ActiveMenu activeMenu, PlaceholderType type, Skill skill) {
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData == null)
            return placeholder;
        Locale locale = playerData.getLocale();
        int skillLevel = playerData.getSkillLevel(skill);

        switch (placeholder) {
            case "skill":
                return skill.getDisplayName(locale);
            case "skill_desc":
                return skill.getDescription(locale);
            case "stats_leveled":
                return getStatsLeveled(skill, locale);
            case "ability_levels":
                return getAbilityLevels(skill, playerData);
            case "mana_ability":
                return getManaAbility(skill, playerData);
            case "level":
                if (type == PlaceholderType.DISPLAY_NAME) {
                    return RomanNumber.toRoman(skillLevel);
                } else {
                    return TextUtil.replace(Lang.getMessage(MenuMessage.LEVEL, locale), "{level}", RomanNumber.toRoman(skillLevel));
                }
            case "progress_to_level":
                return getProgressToLevel(skill, playerData);
            case "max_level":
                if (skillLevel >= OptionL.getMaxLevel(skill)) {
                    return Lang.getMessage(MenuMessage.MAX_LEVEL, locale);
                } else {
                    return "";
                }
            case "skill_click":
                return Lang.getMessage(MenuMessage.SKILL_CLICK, locale);
        }
        return placeholder;
    }

    protected int getPage(Skill skill, PlayerData playerData) {
        int page = (playerData.getSkillLevel(skill) - 2) / 24;
        int maxLevelPage = (OptionL.getMaxLevel(skill) - 2) / 24;
        if (page > maxLevelPage) {
            page = maxLevelPage;
        }
        return page;
    }

    private String getStatsLeveled(Skill skill, Locale locale) {
        ImmutableList<Stat> statsLeveled = plugin.getRewardManager().getRewardTable(skill).getStatsLeveled();
        StringBuilder statList = new StringBuilder();
        for (Stat stat : statsLeveled) {
            statList.append(stat.getColor(locale)).append(stat.getDisplayName(locale)).append(ChatColor.GRAY).append(", ");
        }
        if (statList.length() > 1) {
            statList.delete(statList.length() - 2, statList.length());
        }
        if (statsLeveled.size() > 0) {
            return TextUtil.replace(Lang.getMessage(MenuMessage.STATS_LEVELED, locale),
                    "{stats}", statList.toString());
        } else {
            return "";
        }
    }

    private String getAbilityLevels(Skill skill, PlayerData playerData) {
        Locale locale = playerData.getLocale();
        StringBuilder abilityLevelsLore = new StringBuilder();
        if (skill.getAbilities().size() == 5) {
            String levelsMessage = Lang.getMessage(MenuMessage.ABILITY_LEVELS, locale);
            int num = 1;
            List<Ability> abilities = new ArrayList<>();
            for (Supplier<Ability> abilitySupplier : skill.getAbilities()) {
                abilities.add(abilitySupplier.get());
            }
            abilities.sort(Comparator.comparingInt(a -> plugin.getAbilityManager().getUnlock(a)));
            for (Ability ability : abilities) {
                if (plugin.getAbilityManager().isEnabled(ability)) {
                    if (playerData.getAbilityLevel(ability) > 0) {
                        int abilityLevel = playerData.getAbilityLevel(ability);
                        levelsMessage = TextUtil.replace(levelsMessage, "{ability_" + num + "}", TextUtil.replace(Lang.getMessage(MenuMessage.ABILITY_LEVEL_ENTRY, locale)
                                , "{ability}", ability.getDisplayName(locale)
                                , "{level}", RomanNumber.toRoman(playerData.getAbilityLevel(ability))
                                , "{info}", TextUtil.replace(ability.getInfo(locale)
                                , "{value}", NumberUtil.format1(plugin.getAbilityManager().getValue(ability, abilityLevel))
                                , "{value_2}", NumberUtil.format1(plugin.getAbilityManager().getValue2(ability, abilityLevel)))));
                    } else {
                        levelsMessage = TextUtil.replace(levelsMessage, "{ability_" + num + "}", TextUtil.replace(Lang.getMessage(MenuMessage.ABILITY_LEVEL_ENTRY_LOCKED, locale)
                                , "{ability}", ability.getDisplayName(locale)));
                    }
                } else {
                    levelsMessage = TextUtil.replace(levelsMessage, "\\n  {ability_" + num + "}", ""
                            , "{ability_" + num + "}", "");
                }
                num++;
            }
            abilityLevelsLore.append(levelsMessage);
        }
        return abilityLevelsLore.toString();
    }

    private String getManaAbility(Skill skill, PlayerData playerData) {
        Locale locale = playerData.getLocale();
        StringBuilder manaAbilityLore = new StringBuilder();
        MAbility mAbility = skill.getManaAbility();
        if (mAbility != null) {
            int level = playerData.getManaAbilityLevel(mAbility);
            if (level > 0 && plugin.getAbilityManager().isEnabled(mAbility)) {
                ManaAbilityManager manager = plugin.getManaAbilityManager();
                manaAbilityLore.append(TextUtil.replace(Lang.getMessage(getManaAbilityMessage(mAbility), locale)
                        , "{mana_ability}", mAbility.getDisplayName(locale)
                        , "{level}", RomanNumber.toRoman(level)
                        , "{duration}", NumberUtil.format1(getDuration(mAbility, level))
                        , "{value}", NumberUtil.format1(manager.getValue(mAbility, level))
                        , "{mana_cost}", NumberUtil.format1(manager.getManaCost(mAbility, level))
                        , "{cooldown}", NumberUtil.format1(manager.getCooldown(mAbility, level))));

            }
        }
        return manaAbilityLore.toString();
    }

    private MessageKey getManaAbilityMessage(MAbility mAbility) {
        switch (mAbility) {
            case SHARP_HOOK:
                return ManaAbilityMessage.SHARP_HOOK_MENU;
            case CHARGED_SHOT:
                return ManaAbilityMessage.CHARGED_SHOT_MENU;
            case LIGHTNING_BLADE:
                return ManaAbilityMessage.LIGHTNING_BLADE_MENU;
            default:
                return MenuMessage.MANA_ABILITY;
        }
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

    private String getProgressToLevel(Skill skill, PlayerData playerData) {
        int skillLevel = playerData.getSkillLevel(skill);
        Locale locale = playerData.getLocale();
        if (skillLevel < OptionL.getMaxLevel(skill)) {
            double currentXp = playerData.getSkillXp(skill);
            double xpToNext = plugin.getLeveler().getXpRequirements().getXpRequired(skill, skillLevel + 1);
            return TextUtil.replace(Lang.getMessage(MenuMessage.PROGRESS_TO_LEVEL, locale)
                    ,"{level}", RomanNumber.toRoman(skillLevel + 1)
                    ,"{percent}", NumberUtil.format2(currentXp / xpToNext * 100)
                    ,"{current_xp}", NumberUtil.format2(currentXp)
                    ,"{level_xp}", String.valueOf((int) xpToNext));
        } else {
            return "";
        }
    }

}
