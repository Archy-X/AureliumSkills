package com.archyx.aureliumskills.menus.abilities;

import com.archyx.aureliumskills.AureliumSkills;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class LockedManaAbilityItem extends AbstractManaAbilityItem implements TemplateItemProvider<@NotNull MAbility> {

    private final @NotNull ManaAbilityManager manager;

    public LockedManaAbilityItem(@NotNull AureliumSkills plugin) {
        super(plugin);
        this.manager = plugin.getManaAbilityManager();
    }

    @Override
    public @NotNull String onPlaceholderReplace(@NotNull String placeholder, @NotNull Player player, @NotNull ActiveMenu activeMenu, @NotNull PlaceholderType type, @NotNull MAbility mAbility) {
        @Nullable Locale locale = plugin.getLang().getLocale(player);
        Skill skill = getSkill(activeMenu);
        switch (placeholder) {
            case "name":
                return mAbility.getDisplayName(locale);
            case "locked_desc":
                return TextUtil.replace(Lang.getMessage(MenuMessage.LOCKED_DESC, locale),
                        "{desc}", TextUtil.replace(mAbility.getDescription(locale),
                            "{value}", NumberUtil.format1(manager.getDisplayValue(mAbility, 1)),
                            "{haste_level}", String.valueOf(manager.getOptionAsInt(mAbility, "haste_level", 10)),
                            "{duration}", NumberUtil.format1(getDuration(mAbility))));
            case "unlocked_at":
                return TextUtil.replace(Lang.getMessage(MenuMessage.UNLOCKED_AT, locale),
                        "{skill}", skill.getDisplayName(locale),
                        "{level}", RomanNumber.toRoman(plugin.getManaAbilityManager().getUnlock(mAbility)));
            case "locked":
                return Lang.getMessage(MenuMessage.LOCKED, locale);
        }
        return placeholder;
    }

    @Override
    public @NotNull Set<@NotNull MAbility> getDefinedContexts(@NotNull Player player, @NotNull ActiveMenu activeMenu) {
        Set<@NotNull MAbility> lockedManaAbilities = new HashSet<>();
        Skill skill = getSkill(activeMenu);
        MAbility mAbility = skill.getManaAbility();
        @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (mAbility != null && playerData != null) {
            if (playerData.getManaAbilityLevel(mAbility) <= 0) {
                lockedManaAbilities.add(mAbility);
            }
        }
        return lockedManaAbilities;
    }

    private double getDuration(@NotNull MAbility mAbility) {
        if (mAbility == MAbility.LIGHTNING_BLADE) {
            return manager.getOptionAsDouble(MAbility.LIGHTNING_BLADE, "base_duration");
        } else {
            return manager.getValue(mAbility, 1);
        }
    }

    private @NotNull Skill getSkill(@NotNull ActiveMenu activeMenu) {
        @Nullable Object property = activeMenu.getProperty("skill");
        if (!(property instanceof Skill)) {
            throw new IllegalArgumentException("Could not get menu skill property");
        }
        return (Skill) property;
    }

}
