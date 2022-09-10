package com.archyx.aureliumskills.menus.abilities;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.lang.MenuMessage;
import com.archyx.aureliumskills.mana.MAbility;
import com.archyx.aureliumskills.mana.ManaAbilityManager;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.util.math.NumberUtil;
import com.archyx.aureliumskills.util.math.RomanNumber;
import com.archyx.aureliumskills.util.text.TextUtil;
import com.archyx.slate.item.provider.PlaceholderType;
import com.archyx.slate.item.provider.TemplateItemProvider;
import com.archyx.slate.menu.ActiveMenu;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class UnlockedManaAbilityItem extends AbstractManaAbilityItem implements TemplateItemProvider<MAbility> {

    private final ManaAbilityManager manager;
    
    public UnlockedManaAbilityItem(AureliumSkills plugin) {
        super(plugin);
        manager = plugin.getManaAbilityManager();
    }

    @Override
    public String onPlaceholderReplace(String placeholder, Player player, ActiveMenu activeMenu, PlaceholderType type, MAbility mAbility) {
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData == null)
            return placeholder;
        Locale locale = plugin.getLang().getLocale(player);
        switch (placeholder) {
            case "name":
                return mAbility.getDisplayName(locale);
            case "your_ability_level":
                if (isNotMaxed(playerData, mAbility)) {
                    return TextUtil.replace(Lang.getMessage(MenuMessage.YOUR_ABILITY_LEVEL, locale),
                            "{level}", String.valueOf(playerData.getManaAbilityLevel(mAbility)));
                } else {
                    return TextUtil.replace(Lang.getMessage(MenuMessage.YOUR_ABILITY_LEVEL_MAXED, locale),
                            "{level}", String.valueOf(playerData.getManaAbilityLevel(mAbility)));
                }
            case "unlocked_desc":
                if (isNotMaxed(playerData, mAbility)) {
                    return TextUtil.replace(Lang.getMessage(MenuMessage.UNLOCKED_DESC, locale),
                            "{skill}", mAbility.getSkill().getDisplayName(locale),
                            "{level}", RomanNumber.toRoman(getNextUpgradeLevel(mAbility, playerData)),
                            "{desc}", TextUtil.replace(mAbility.getDescription(locale),
                                    "{value}", getUpgradeValue(mAbility, playerData),
                                    "{haste_level}", String.valueOf(manager.getOptionAsInt(mAbility, "haste_level", 10)),
                                    "{duration}", getUpgradeDuration(mAbility, playerData)));
                } else {
                    return TextUtil.replace(Lang.getMessage(MenuMessage.UNLOCKED_DESC_MAXED, locale),
                            "{desc}", TextUtil.replace(mAbility.getDescription(locale),
                                    "{value}", getUpgradeValue(mAbility, playerData),
                                    "{haste_level}", String.valueOf(manager.getOptionAsInt(mAbility, "haste_level", 10)),
                                    "{duration}", getUpgradeDuration(mAbility, playerData)));
                }
            case "unlocked":
                return Lang.getMessage(MenuMessage.UNLOCKED, locale);
        }
        return placeholder;
    }

    private int getNextUpgradeLevel(MAbility mAbility, PlayerData playerData) {
        int unlock = manager.getUnlock(mAbility);
        int levelUp = manager.getLevelUp(mAbility);
        return unlock + levelUp * playerData.getManaAbilityLevel(mAbility);
    }

    private String getUpgradeValue(MAbility mAbility, PlayerData playerData) {
        String currentValue = NumberUtil.format1(manager.getDisplayValue(mAbility, playerData.getManaAbilityLevel(mAbility)));
        String nextValue = NumberUtil.format1(manager.getDisplayValue(mAbility, playerData.getManaAbilityLevel(mAbility) + 1));
        return "&7" + currentValue + "&8→" + nextValue + "&7";
    }

    private String getUpgradeDuration(MAbility mAbility, PlayerData playerData) {
        String currentDuration = NumberUtil.format1(getDuration(mAbility, playerData.getManaAbilityLevel(mAbility)));
        String nextDuration = NumberUtil.format1(getDuration(mAbility, playerData.getManaAbilityLevel(mAbility) + 1));
        return "&7" + currentDuration + "&8→" + nextDuration + "&7";
    }

    private boolean isNotMaxed(PlayerData playerData, MAbility mAbility) {
        int maxLevel = manager.getMaxLevel(mAbility);
        int unlock = manager.getUnlock(mAbility);
        int levelUp = manager.getLevelUp(mAbility);
        int maxAllowedBySkill = (OptionL.getMaxLevel(mAbility.getSkill()) - unlock) / levelUp + 1;
        if (maxLevel == 0 || maxLevel > maxAllowedBySkill) {
            maxLevel = maxAllowedBySkill;
        }
        return playerData.getManaAbilityLevel(mAbility) < maxLevel;
    }

    @Override
    public Set<MAbility> getDefinedContexts(Player player, ActiveMenu activeMenu) {
        Skill skill = getSkill(activeMenu);
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        Set<MAbility> unlockedManaAbilities = new HashSet<>();
        if (playerData != null) {
            // Add abilities that player has not unlocked yet
            MAbility mAbility = skill.getManaAbility();
            if (mAbility != null && playerData.getManaAbilityLevel(mAbility) >= 1) {
                unlockedManaAbilities.add(mAbility);
            }
        }
        return unlockedManaAbilities;
    }

    private double getDuration(MAbility mAbility, int level) {
        if (mAbility == MAbility.LIGHTNING_BLADE) {
            double baseDuration = manager.getOptionAsDouble(MAbility.LIGHTNING_BLADE, "base_duration");
            double durationPerLevel = manager.getOptionAsDouble(MAbility.LIGHTNING_BLADE, "duration_per_level");
            return baseDuration + (durationPerLevel * (level - 1));
        } else {
            return manager.getValue(mAbility, level);
        }
    }

    private Skill getSkill(ActiveMenu activeMenu) {
        Object property = activeMenu.getProperty("skill");
        if (!(property instanceof Skill)) {
            throw new IllegalArgumentException("Could not get menu skill property");
        }
        return (Skill) property;
    }

}
