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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class ClickableSkillItem extends AbstractSkillItem {

    public ClickableSkillItem(AureliumSkills plugin) {
        super(plugin);
    }

    @Override
    public void onClick(@NotNull Player player, @NotNull InventoryClickEvent event, @NotNull ItemStack item, @NotNull SlotPos pos, @NotNull ActiveMenu activeMenu, @NotNull Skill skill) {
        @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData == null) {
            return;
        }

        if (player.hasPermission("aureliumskills." + skill.toString().toLowerCase(Locale.ENGLISH))) {
            new LevelProgressionOpener(plugin).open(player, playerData, skill);
        }
    }

    @Override
    public @NotNull Set<@NotNull Skill> getDefinedContexts(@NotNull Player player, @NotNull ActiveMenu activeMenu) {
        return new HashSet<>(plugin.getSkillRegistry().getSkills());
    }

    @Override
    public @Nullable ItemStack onItemModify(@NotNull ItemStack baseItem, @NotNull Player player, @NotNull ActiveMenu activeMenu, @NotNull Skill skill) {
        if (OptionL.isEnabled(skill)) {
            return baseItem;
        }
        return null; // Hide item if skill is disabled
    }
}
