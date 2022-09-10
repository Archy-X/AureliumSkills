package com.archyx.aureliumskills.menus.levelprogression;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.lang.MenuMessage;
import com.archyx.aureliumskills.menus.AbstractMenu;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.util.text.TextUtil;
import com.archyx.slate.menu.ActiveMenu;
import com.archyx.slate.menu.ConfigurableMenu;
import com.archyx.slate.menu.MenuProvider;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class LevelProgressionMenu extends AbstractMenu implements MenuProvider {

    public LevelProgressionMenu(AureliumSkills plugin) {
        super(plugin);
    }

    @Override
    public @NotNull String onPlaceholderReplace(@NotNull String placeholder, @NotNull Player player, @NotNull ActiveMenu activeMenu) {
        @Nullable Locale locale = plugin.getLang().getLocale(player);
        Skill skill = getSkill(activeMenu);
        if (placeholder.equals("level_progression_menu_title")) {
            return TextUtil.replace(Lang.getMessage(MenuMessage.LEVEL_PROGRESSION_MENU_TITLE, locale),
                    "{skill}", skill.getDisplayName(locale),
                    "{page}", String.valueOf(activeMenu.getCurrentPage() + 1));
        }
        return placeholder;
    }

    @Override
    public int getPages(@NotNull Player player, @NotNull ActiveMenu activeMenu) {
        @Nullable Skill skill = getSkill(activeMenu);
        int itemsPerPage = 24;
        ConfigurableMenu levelProgressionMenu = plugin.getSlate().getMenuManager().getMenu("level_progression");
        if (levelProgressionMenu != null) {
            Object itemsPerPageObj = levelProgressionMenu.getOptions().get("items_per_page");
            if (itemsPerPageObj != null) {
                itemsPerPage = (int) itemsPerPageObj;
            }
        }
        return (OptionL.getMaxLevel(skill) - 2) / itemsPerPage + 1;
    }

    private @NotNull Skill getSkill(@NotNull ActiveMenu activeMenu) {
        @Nullable Object property = activeMenu.getProperty("skill");
        if (!(property instanceof Skill)) {
            throw new IllegalArgumentException("Could not get menu skill property");
        }
        return (Skill) property;
    }

}
