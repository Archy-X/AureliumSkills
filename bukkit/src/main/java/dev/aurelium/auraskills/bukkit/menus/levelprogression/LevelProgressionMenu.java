package dev.aurelium.auraskills.bukkit.menus.levelprogression;

import com.archyx.slate.menu.ActiveMenu;
import com.archyx.slate.menu.ConfigurableMenu;
import com.archyx.slate.menu.MenuProvider;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.bukkit.AuraSkills;
import dev.aurelium.auraskills.bukkit.menus.AbstractMenu;
import dev.aurelium.auraskills.common.util.text.Replacer;
import org.bukkit.entity.Player;

import java.util.Locale;

public class LevelProgressionMenu extends AbstractMenu implements MenuProvider {

    public LevelProgressionMenu(AuraSkills plugin) {
        super(plugin);
    }

    @Override
    public String onPlaceholderReplace(String placeholder, Player player, ActiveMenu activeMenu) {
        Locale locale = plugin.getUser(player).getLocale();
        Skill skill = getSkill(activeMenu);
        return replaceMenuMessage(placeholder, player, activeMenu, new Replacer()
                .map("{skill}", () -> skill.getDisplayName(locale))
                .map("{page}", () -> String.valueOf(activeMenu.getCurrentPage() + 1)));
    }

    @Override
    public int getPages(Player player, ActiveMenu activeMenu) {
        Skill skill = (Skill) activeMenu.getProperty("skill");
        int itemsPerPage = 24;
        ConfigurableMenu levelProgressionMenu = plugin.getSlate().getMenuManager().getMenu("level_progression");
        if (levelProgressionMenu != null) {
            Object itemsPerPageObj = levelProgressionMenu.getOptions().get("items_per_page");
            if (itemsPerPageObj != null) {
                itemsPerPage = (int) itemsPerPageObj;
            }
        }
        return (skill.getMaxLevel() - 2) / itemsPerPage + 1;
    }

    private Skill getSkill(ActiveMenu activeMenu) {
        Object property = activeMenu.getProperty("skill");
        if (property instanceof Skill) {
            return (Skill) property;
        } else {
            throw new IllegalArgumentException("Could not get skill property");
        }
    }
}
