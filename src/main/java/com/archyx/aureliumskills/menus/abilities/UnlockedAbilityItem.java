package com.archyx.aureliumskills.menus.abilities;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.ability.Ability;
import com.archyx.aureliumskills.configuration.OptionL;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.Supplier;

public class UnlockedAbilityItem extends AbstractAbilityItem {

    public UnlockedAbilityItem(AureliumSkills plugin) {
        super(plugin, "unlocked_ability");
    }

    @Override
    public @NotNull String onPlaceholderReplace(@NotNull String placeholder, @NotNull Player player, @NotNull ActiveMenu activeMenu, @NotNull PlaceholderType type, @NotNull Ability ability) {
        @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData == null)
            return placeholder;
        @Nullable Locale locale = plugin.getLang().getLocale(player);
        switch (placeholder) {
            case "name":
                return ability.getDisplayName(locale);
            case "your_ability_level":
                if (isNotMaxed(playerData, ability)) {
                    return TextUtil.replace(Lang.getMessage(MenuMessage.YOUR_ABILITY_LEVEL, locale),
                            "{level}", String.valueOf(playerData.getAbilityLevel(ability)));
                } else {
                    return TextUtil.replace(Lang.getMessage(MenuMessage.YOUR_ABILITY_LEVEL_MAXED, locale),
                            "{level}", String.valueOf(playerData.getAbilityLevel(ability)));
                }
            case "unlocked_desc":
                if (isNotMaxed(playerData, ability)) {
                    return TextUtil.replace(Lang.getMessage(MenuMessage.UNLOCKED_DESC, locale),
                            "{skill}", ability.getSkill().getDisplayName(locale),
                            "{level}", RomanNumber.toRoman(getNextUpgradeLevel(ability, playerData)),
                            "{desc}", TextUtil.replace(ability.getDescription(locale),
                                    "{value}", getUpgradeValue(ability, playerData),
                                    "{value_2}", getUpgradeValue2(ability, playerData)));
                } else {
                    return TextUtil.replace(Lang.getMessage(MenuMessage.UNLOCKED_DESC_MAXED, locale),
                            "{desc}", TextUtil.replace(ability.getDescription(locale),
                                    "{value}", getCurrentValue(ability, playerData),
                                    "{value_2}", getCurrentValue2(ability, playerData)));
                }
            case "unlocked":
                return Lang.getMessage(MenuMessage.UNLOCKED, locale);
        }
        return placeholder;
    }

    private int getNextUpgradeLevel(@NotNull Ability ability, @NotNull PlayerData playerData) {
        int unlock = plugin.getAbilityManager().getUnlock(ability);
        int levelUp = plugin.getAbilityManager().getLevelUp(ability);
        return unlock + levelUp * playerData.getAbilityLevel(ability);
    }

    private @NotNull String getCurrentValue(@NotNull Ability ability, @NotNull PlayerData playerData) {
        return NumberUtil.format1(plugin.getAbilityManager().getValue(ability, playerData.getAbilityLevel(ability)));
    }

    private @NotNull String getCurrentValue2(@NotNull Ability ability, @NotNull PlayerData playerData) {
        return NumberUtil.format1(plugin.getAbilityManager().getValue2(ability, playerData.getAbilityLevel(ability)));
    }

    private @NotNull String getUpgradeValue(@NotNull Ability ability, @NotNull PlayerData playerData) {
        String currentValue = getCurrentValue(ability, playerData);
        String nextValue = NumberUtil.format1(plugin.getAbilityManager().getValue(ability, playerData.getAbilityLevel(ability) + 1));
        return "&7" + currentValue + "&8→" + nextValue + "&7";
    }

    private @NotNull String getUpgradeValue2(@NotNull Ability ability, @NotNull PlayerData playerData) {
        String currentValue = getCurrentValue2(ability, playerData);
        String nextValue = NumberUtil.format1(plugin.getAbilityManager().getValue2(ability, playerData.getAbilityLevel(ability) + 1));
        return "&7" + currentValue + "&8→" + nextValue + "&7";
    }

    private boolean isNotMaxed(@NotNull PlayerData playerData, @NotNull Ability ability) {
        int maxLevel = plugin.getAbilityManager().getMaxLevel(ability);
        int unlock = plugin.getAbilityManager().getUnlock(ability);
        int levelUp = plugin.getAbilityManager().getLevelUp(ability);
        int maxAllowedBySkill = (OptionL.getMaxLevel(ability.getSkill()) - unlock) / levelUp + 1;
        if (maxLevel == 0 || maxLevel > maxAllowedBySkill) {
            maxLevel = maxAllowedBySkill;
        }
        return playerData.getAbilityLevel(ability) < maxLevel;
    }

    @Override
    public @NotNull Set<@NotNull Ability> getDefinedContexts(@NotNull Player player, @NotNull ActiveMenu activeMenu) {
        @NotNull Set<@NotNull Ability> unlockedAbilities = new HashSet<>();
        @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData == null)
            return unlockedAbilities;
        Skill skill = getSkill(activeMenu);
        // Add abilities that player has not unlocked yet
        for (@NotNull Supplier<@NotNull Ability> abilitySupplier : skill.getAbilities()) {
            Ability ability = abilitySupplier.get();
            if (playerData.getAbilityLevel(ability) >= 1) {
                unlockedAbilities.add(ability);
            }
        }
        return unlockedAbilities;
    }

    private @NotNull Skill getSkill(@NotNull ActiveMenu activeMenu) {
        @Nullable Object property = activeMenu.getProperty("skill");
        if (!(property instanceof Skill)) {
            throw new IllegalArgumentException("Could not get menu skill property");
        }
        return (Skill) property;
    }

}
