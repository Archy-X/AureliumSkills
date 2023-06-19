package com.archyx.aureliumskills.menus.stats;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.lang.MenuMessage;
import com.archyx.aureliumskills.menus.common.AbstractItem;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.stats.Stat;
import com.archyx.aureliumskills.stats.Stats;
import com.archyx.aureliumskills.util.math.NumberUtil;
import com.archyx.aureliumskills.util.text.TextUtil;
import com.archyx.slate.item.provider.PlaceholderData;
import com.archyx.slate.item.provider.TemplateItemProvider;
import com.archyx.slate.menu.ActiveMenu;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.*;

public class StatItem extends AbstractItem implements TemplateItemProvider<Stat> {

    public StatItem(AureliumSkills plugin) {
        super(plugin);
    }

    @Override
    public Class<Stat> getContext() {
        return Stat.class;
    }

    @Override
    public Set<Stat> getDefinedContexts(Player player, ActiveMenu activeMenu) {
        return new HashSet<>(plugin.getStatRegistry().getStats());
    }

    @Override
    public String onPlaceholderReplace(String placeholder, Player player, ActiveMenu activeMenu, PlaceholderData data, Stat stat) {
        Locale locale = plugin.getLang().getLocale(player);
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData == null) return placeholder;
        switch (placeholder) {
            case "color":
                return stat.getColor(locale);
            case "stat":
                return stat.getDisplayName(locale);
            case "stat_desc":
                return stat.getDescription(locale);
            case "skills":
                return getSkillsLeveledBy(stat, locale);
            case "your_level":
                return TextUtil.replace(Lang.getMessage(MenuMessage.YOUR_LEVEL, locale),
                        "{color}", stat.getColor(locale),
                        "{level}", adjustAndFormatStatLevel(stat, playerData, player));
            case "descriptors":
                switch (stat.name().toLowerCase(Locale.ROOT)) {
                    case "strength":
                        return getStrengthDescriptors(playerData, locale);
                    case "health":
                        return getHealthDescriptors(playerData, locale, player);
                    case "regeneration":
                        return getRegenerationDescriptors(playerData, locale);
                    case "luck":
                        return getLuckDescriptors(playerData, locale);
                    case "wisdom":
                        return getWisdomDescriptors(playerData, locale);
                    case "toughness":
                        return getToughnessDescriptors(playerData, locale);
                    case "crit_chance":
                        return getCritChanceDescriptors(playerData, locale);
                    case "crit_damage":
                        return getCritDamageDescriptors(playerData, locale);
                    case "speed":
                        return getSpeedDescriptors(playerData, locale);
                    default:
                        return "";
                }
        }
        return placeholder;
    }

    private String getSkillsLeveledBy(Stat stat, Locale locale) {
        List<Skill> skillsLeveledBy = plugin.getRewardManager().getSkillsLeveledBy(stat);
        StringBuilder skillList = new StringBuilder();
        for (Skill skill : skillsLeveledBy) {
            skillList.append(skill.getDisplayName(locale)).append(", ");
        }
        if (skillList.length() > 1) {
            skillList.delete(skillList.length() - 2, skillList.length());
        }
        if (skillsLeveledBy.size() > 0) {
            return TextUtil.replace(Lang.getMessage(MenuMessage.SKILLS, locale),
                    "{skills}", skillList.toString());
        } else {
            return "";
        }
    }

    private String getStrengthDescriptors(PlayerData playerData, Locale locale) {
        double strengthLevel = playerData.getStatLevel(Stats.STRENGTH);
        double attackDamage = strengthLevel * OptionL.getDouble(Option.STRENGTH_MODIFIER);
        if (OptionL.getBoolean(Option.STRENGTH_DISPLAY_DAMAGE_WITH_HEALTH_SCALING) && !OptionL.getBoolean(Option.STRENGTH_USE_PERCENT)) {
            attackDamage *= OptionL.getDouble(Option.HEALTH_HP_INDICATOR_SCALING);
        }
        return TextUtil.replace(Lang.getMessage(MenuMessage.ATTACK_DAMAGE, locale),"{value}", NumberUtil.format2(attackDamage));
    }

    private String getHealthDescriptors(PlayerData playerData, Locale locale, Player player) {
        double modifier;
        if (OptionL.getBoolean(Option.HEALTH_SHOW_TOTAL_HEALTH)) {
            modifier = Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue();
        } else {
            modifier = playerData.getStatLevel(Stats.HEALTH) * OptionL.getDouble(Option.HEALTH_MODIFIER);
        }
        double scaledHealth = modifier * OptionL.getDouble(Option.HEALTH_HP_INDICATOR_SCALING);
        return TextUtil.replace(Lang.getMessage(MenuMessage.HP, locale),"{value}", NumberUtil.format2(scaledHealth));
    }

    private String getRegenerationDescriptors(PlayerData playerData, Locale locale) {
        double regenLevel = playerData.getStatLevel(Stats.REGENERATION);
        double saturatedRegen = regenLevel * OptionL.getDouble(Option.REGENERATION_SATURATED_MODIFIER) * OptionL.getDouble(Option.HEALTH_HP_INDICATOR_SCALING);
        double hungerFullRegen = regenLevel *  OptionL.getDouble(Option.REGENERATION_HUNGER_FULL_MODIFIER) * OptionL.getDouble(Option.HEALTH_HP_INDICATOR_SCALING);
        double almostFullRegen = regenLevel *  OptionL.getDouble(Option.REGENERATION_HUNGER_ALMOST_FULL_MODIFIER) * OptionL.getDouble(Option.HEALTH_HP_INDICATOR_SCALING);
        double manaRegen = playerData.getManaRegen();
        return TextUtil.replace(Lang.getMessage(MenuMessage.SATURATED_REGEN, locale),"{value}", NumberUtil.format2(saturatedRegen))
                + "\n" + TextUtil.replace(Lang.getMessage(MenuMessage.FULL_HUNGER_REGEN, locale),"{value}", NumberUtil.format2(hungerFullRegen))
                + "\n" + TextUtil.replace(Lang.getMessage(MenuMessage.ALMOST_FULL_HUNGER_REGEN, locale),"{value}", NumberUtil.format2(almostFullRegen))
                + "\n" + TextUtil.replace(Lang.getMessage(MenuMessage.MANA_REGEN, locale),"{value}", String.valueOf(Math.round(manaRegen)));
    }

    private String getLuckDescriptors(PlayerData playerData, Locale locale) {
        double luckLevel = playerData.getStatLevel(Stats.LUCK);
        double luck = luckLevel * OptionL.getDouble(Option.LUCK_MODIFIER);
        double doubleDropChance = luckLevel * OptionL.getDouble(Option.LUCK_DOUBLE_DROP_MODIFIER) * 100;
        if (doubleDropChance > OptionL.getDouble(Option.LUCK_DOUBLE_DROP_PERCENT_MAX)) {
            doubleDropChance = OptionL.getDouble(Option.LUCK_DOUBLE_DROP_PERCENT_MAX);
        }
        return TextUtil.replace(Lang.getMessage(MenuMessage.LUCK, locale),"{value}", NumberUtil.format2(luck))
                + "\n" + TextUtil.replace(Lang.getMessage(MenuMessage.DOUBLE_DROP_CHANCE, locale),"{value}", NumberUtil.format2(doubleDropChance));
    }

    private String getWisdomDescriptors(PlayerData playerData, Locale locale) {
        double wisdomLevel = playerData.getStatLevel(Stats.WISDOM);
        double xpModifier = wisdomLevel * OptionL.getDouble(Option.WISDOM_EXPERIENCE_MODIFIER) * 100;
        double anvilCostReduction = (-1.0 * Math.pow(1.025, -1.0 * wisdomLevel * OptionL.getDouble(Option.WISDOM_ANVIL_COST_MODIFIER)) + 1) * 100;
        double maxMana = playerData.getMaxMana();
        return TextUtil.replace(Lang.getMessage(MenuMessage.XP_GAIN, locale),"{value}", NumberUtil.format2(xpModifier))
                + "\n" + TextUtil.replace(Lang.getMessage(MenuMessage.ANVIL_COST_REDUCTION, locale),"{value}", NumberUtil.format1(anvilCostReduction)) + " "
                + "\n" + TextUtil.replace(Lang.getMessage(MenuMessage.MAX_MANA, locale), "{value}", NumberUtil.format1(maxMana));
    }

    private String getToughnessDescriptors(PlayerData playerData, Locale locale) {
        double toughness = playerData.getStatLevel(Stats.TOUGHNESS) * OptionL.getDouble(Option.TOUGHNESS_NEW_MODIFIER);
        double damageReduction = (-1.0 * Math.pow(1.01, -1.0 * toughness) + 1) * 100;
        return TextUtil.replace(Lang.getMessage(MenuMessage.INCOMING_DAMAGE, locale),"{value}", NumberUtil.format2(damageReduction));
    }

    private String getCritChanceDescriptors(PlayerData playerData, Locale locale) {
        double critChance = playerData.getStatLevel(Stats.CRIT_CHANCE);
        if (critChance > 100) {
            critChance = 100;
        }
        return TextUtil.replace(Lang.getMessage(MenuMessage.CRIT_CHANCE, locale), "{value}", NumberUtil.format2(critChance));
    }

    private String getCritDamageDescriptors(PlayerData playerData, Locale locale) {
        double critDamage = playerData.getStatLevel(Stats.CRIT_DAMAGE);
        return TextUtil.replace(Lang.getMessage(MenuMessage.CRIT_DAMAGE, locale), "{value}", NumberUtil.format2(critDamage));
    }

    private String getSpeedDescriptors(PlayerData playerData, Locale locale) {
        double movementSpeed = playerData.getStatLevel(Stats.SPEED);
        return TextUtil.replace(Lang.getMessage(MenuMessage.MOVEMENT_SPEED, locale), "{value}", NumberUtil.format2(movementSpeed));
    }

    public static String adjustAndFormatStatLevel(Stat stat, PlayerData playerData, Player player) {
        if (stat == Stats.HEALTH && OptionL.getBoolean(Option.HEALTH_SHOW_TOTAL_HEALTH)) {
            double attributeValue = Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue();
            double scaledHealth = attributeValue * OptionL.getDouble(Option.HEALTH_HP_INDICATOR_SCALING);
            return Lang.formatStatLevel(stat, scaledHealth);
        } else {
            return Lang.formatStatLevel(stat, playerData.getStatLevel(stat));
        }
    }

}
