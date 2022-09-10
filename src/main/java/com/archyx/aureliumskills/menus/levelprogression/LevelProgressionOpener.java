package com.archyx.aureliumskills.menus.levelprogression;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.slate.menu.ConfigurableMenu;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class LevelProgressionOpener {

    private final AureliumSkills plugin;

    public LevelProgressionOpener(AureliumSkills plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, @NotNull PlayerData playerData, @NotNull Skill skill) {
        int page = getPage(skill, playerData);
        Map<String, Object> properties = new HashMap<>();
        properties.put("skill", skill);
        // Get items per page from options, default to 24
        int itemsPerPage = 24;
        ConfigurableMenu levelProgressionMenu = plugin.getSlate().getMenuManager().getMenu("level_progression");
        if (levelProgressionMenu != null) {
            Object itemsPerPageObj = levelProgressionMenu.getOptions().get("items_per_page");
            if (itemsPerPageObj != null) {
                itemsPerPage = (int) itemsPerPageObj;
            }
        }
        properties.put("items_per_page", itemsPerPage);
        properties.put("previous_menu", "skills");
        plugin.getMenuManager().openMenu(player, "level_progression", properties, page);
    }

    protected int getPage(@NotNull Skill skill, @NotNull PlayerData playerData) {
        int page = (playerData.getSkillLevel(skill) - 2) / 24;
        int maxLevelPage = (OptionL.getMaxLevel(skill) - 2) / 24;
        if (page > maxLevelPage) {
            page = maxLevelPage;
        }
        return page;
    }

}
