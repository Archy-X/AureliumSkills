package com.archyx.aureliumskills.menus.skills;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.menus.common.AbstractSkillItem;
import com.archyx.aureliumskills.menus.levelprogression.LevelProgressionOpener;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.slate.menu.ActiveMenu;
import fr.minuskube.inv.content.SlotPos;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class ClickableSkillItem extends AbstractSkillItem {

    public ClickableSkillItem(AureliumSkills plugin) {
        super(plugin);
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event, ItemStack item, SlotPos pos, ActiveMenu activeMenu, Skill skill) {
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData == null) {
            return;
        }

        if (player.hasPermission("aureliumskills." + skill.toString().toLowerCase(Locale.ENGLISH))) {
            new LevelProgressionOpener(plugin).open(player, playerData, skill);
        }
    }

    @Override
    public Set<Skill> getDefinedContexts(Player player, ActiveMenu activeMenu) {
        return new HashSet<>(plugin.getSkillRegistry().getSkills());
    }

    @Override
    public ItemStack onItemModify(ItemStack baseItem, Player player, ActiveMenu activeMenu, Skill skill) {
        if (OptionL.isEnabled(skill)) {
            return baseItem;
        }
        return null; // Hide item if skill is disabled
    }
}
