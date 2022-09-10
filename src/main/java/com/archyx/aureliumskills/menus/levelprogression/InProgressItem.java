package com.archyx.aureliumskills.menus.levelprogression;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.lang.MenuMessage;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.util.math.NumberUtil;
import com.archyx.aureliumskills.util.math.RomanNumber;
import com.archyx.aureliumskills.util.text.TextUtil;
import com.archyx.slate.item.provider.PlaceholderType;
import com.archyx.slate.menu.ActiveMenu;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class InProgressItem extends SkillLevelItem {

    public InProgressItem(AureliumSkills plugin) {
        super(plugin);
    }

    @Override
    public String onPlaceholderReplace(String placeholder, Player player, ActiveMenu activeMenu, PlaceholderType placeholderType, Integer position) {
        Locale locale = plugin.getLang().getLocale(player);
        Skill skill = getSkill(activeMenu);
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData == null)
            return placeholder;
        int level = getLevel(activeMenu, position);
        switch (placeholder) {
            case "level_in_progress":
                return TextUtil.replace(Lang.getMessage(MenuMessage.LEVEL_IN_PROGRESS, locale),"{level}", RomanNumber.toRoman(level));
            case "level_number":
                return TextUtil.replace(Lang.getMessage(MenuMessage.LEVEL_NUMBER, locale), "{level}", String.valueOf(level));
            case "rewards":
                return getRewardsLore(skill, level, player, locale);
            case "ability":
                return getAbilityLore(skill, level, locale);
            case "mana_ability":
                return getManaAbilityLore(skill, level, locale);
            case "progress":
                double currentXp = playerData.getSkillXp(skill);
                double xpToNext = plugin.getLeveler().getXpRequirements().getXpRequired(skill, level);
                return TextUtil.replace(Lang.getMessage(MenuMessage.PROGRESS, locale)
                        ,"{percent}", NumberUtil.format2(currentXp / xpToNext * 100)
                        ,"{current_xp}", NumberUtil.format2(currentXp)
                        ,"{level_xp}", String.valueOf((int) xpToNext));
            case "in_progress":
                return Lang.getMessage(MenuMessage.IN_PROGRESS, locale);
        }
        return placeholder;
    }

    @Override
    public Set<Integer> getDefinedContexts(Player player, ActiveMenu activeMenu) {
        Set<Integer> levels = new HashSet<>();
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData == null)
            return levels;
        Skill skill = getSkill(activeMenu);
        int itemsPerPage = getItemsPerPage(activeMenu);
        int currentPage = activeMenu.getCurrentPage();
        int level = playerData.getSkillLevel(skill);
        if (level >= 1 + currentPage * itemsPerPage && level < (currentPage + 1) * itemsPerPage + 2) {
            int position = (level + 1) % itemsPerPage; // Calculate the first-page equivalent next level
            if (position == 0) { // Account for next skill level 24
                position = 24;
            } else if (position == 1) { // Account for next skill level 25
                position = 25;
            }
            levels.add(position);
        }
        return levels;
    }

    private Skill getSkill(ActiveMenu activeMenu) {
        Object property = activeMenu.getProperty("skill");
        if (!(property instanceof Skill)) {
            throw new IllegalArgumentException("Could not get menu skill property");
        }
        return (Skill) property;
    }

}
