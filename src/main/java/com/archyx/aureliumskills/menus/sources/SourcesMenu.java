package com.archyx.aureliumskills.menus.sources;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.lang.MenuMessage;
import com.archyx.aureliumskills.menus.AbstractMenu;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.source.Source;
import com.archyx.aureliumskills.util.text.TextUtil;
import com.archyx.slate.menu.ActiveMenu;
import com.archyx.slate.menu.MenuProvider;
import org.bukkit.entity.Player;

import java.util.Locale;

public class SourcesMenu extends AbstractMenu implements MenuProvider {

    public SourcesMenu(AureliumSkills plugin) {
        super(plugin);
    }

    @Override
    public String onPlaceholderReplace(String placeholder, Player player, ActiveMenu activeMenu) {
        Locale locale = plugin.getLang().getLocale(player);
        if (placeholder.equals("sources_title")) {
            Skill skill = getSkill(activeMenu);
            return TextUtil.replace(Lang.getMessage(MenuMessage.SOURCES_TITLE, locale),
                    "{skill}", skill.getDisplayName(locale),
                    "{current_page}", String.valueOf(activeMenu.getCurrentPage() + 1),
                    "{total_pages}", String.valueOf(activeMenu.getTotalPages()));
        }
        return placeholder;
    }

    @Override
    public int getPages(Player player, ActiveMenu activeMenu) {
        Skill skill = getSkill(activeMenu);
        int itemsPerPage = getItemsPerPage(activeMenu);
        Source[] sources = plugin.getSourceRegistry().values(skill);
        return (sources.length - 1) / itemsPerPage + 1;
    }

    protected int getItemsPerPage(ActiveMenu activeMenu) {
        Object property = activeMenu.getProperty("items_per_page");
        int itemsPerPage;
        if (property instanceof Integer) {
            itemsPerPage = (Integer) property;
        } else {
            itemsPerPage = 24;
        }
        return itemsPerPage;
    }

    private Skill getSkill(ActiveMenu activeMenu) {
        Object property = activeMenu.getProperty("skill");
        if (!(property instanceof Skill)) {
            throw new IllegalArgumentException("Could not get menu skill property");
        }
        return (Skill) property;
    }
}
