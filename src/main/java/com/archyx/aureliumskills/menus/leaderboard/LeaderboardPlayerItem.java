package com.archyx.aureliumskills.menus.leaderboard;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.lang.MenuMessage;
import com.archyx.aureliumskills.leaderboard.SkillValue;
import com.archyx.aureliumskills.menus.common.AbstractItem;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.util.text.TextUtil;
import com.archyx.aureliumskills.util.version.VersionUtils;
import com.archyx.slate.item.provider.PlaceholderType;
import com.archyx.slate.item.provider.TemplateItemProvider;
import com.archyx.slate.menu.ActiveMenu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class LeaderboardPlayerItem extends AbstractItem implements TemplateItemProvider<@NotNull Integer> {

    public LeaderboardPlayerItem(AureliumSkills plugin) {
        super(plugin);
    }

    @Override
    public @NotNull Class<@NotNull Integer> getContext() {
        return Integer.class;
    }

    @Override
    public @NotNull String onPlaceholderReplace(@NotNull String placeholder, @NotNull Player player, @NotNull ActiveMenu activeMenu, @NotNull PlaceholderType placeholderType, @NotNull Integer place) {
        @Nullable Locale locale = plugin.getLang().getLocale(player);
        @Nullable Skill skill = getSkill(activeMenu);
        SkillValue value = plugin.getLeaderboardManager().getLeaderboard(skill, place, 1).get(0);
        switch (placeholder) {
            case "player_entry":
                UUID id = value.getId();
                String name = Bukkit.getOfflinePlayer(id).getName();
                return TextUtil.replace(Lang.getMessage(MenuMessage.PLAYER_ENTRY, locale),
                        "{place}", String.valueOf(place),
                        "{player}", name != null ? name : "?");
            case "skill_level":
                return TextUtil.replace(Lang.getMessage(MenuMessage.SKILL_LEVEL, locale),
                        "{level}", String.valueOf(value.getLevel()));
        }
        return placeholder;
    }

    @Override
    public @NotNull Set<@NotNull Integer> getDefinedContexts(@NotNull Player player, @NotNull ActiveMenu activeMenu) {
        Set<@NotNull Integer> places = new HashSet<>();
        for (int i = 1; i <= 10; i++) {
            places.add(i);
        }
        return places;
    }

    @Override
    public @Nullable ItemStack onItemModify(@NotNull ItemStack baseItem, @NotNull Player player, @NotNull ActiveMenu activeMenu, @NotNull Integer place) {
        Skill skill = getSkill(activeMenu);
        List<SkillValue> values = plugin.getLeaderboardManager().getLeaderboard(skill, place, 1);
        if (values.size() == 0) {
            return null;
        }
        SkillValue skillValue = values.get(0);
        UUID id = skillValue.getId();
        if (baseItem.getItemMeta() instanceof SkullMeta) {
            // Replace player heads with other material in legacy versions to prevent lag
            if (!VersionUtils.isAtLeastVersion(17)) {
                Boolean autoReplaceHeadsOnLegacy = activeMenu.getOption(Boolean.class, "auto_replace_heads_on_legacy");
                if (autoReplaceHeadsOnLegacy != null && autoReplaceHeadsOnLegacy) {
                    return new ItemStack(Material.GOLD_BLOCK); // Default material replacement
                }
            }
            // Set the player skin on the head
            @Nullable SkullMeta meta = (SkullMeta) baseItem.getItemMeta();
            
            if (meta != null)
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(id));
            
            baseItem.setItemMeta(meta);
        }
        return baseItem;
    }

    private @NotNull Skill getSkill(@NotNull ActiveMenu activeMenu) {
        @Nullable Object property = activeMenu.getProperty("skill");
        if (!(property instanceof Skill)) {
            throw new IllegalArgumentException("Could not get menu skill property");
        }
        return (Skill) property;
    }

}
