package com.archyx.aureliumskills.menus.skills;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.lang.MenuMessage;
import com.archyx.aureliumskills.menus.common.AbstractItem;
import com.archyx.slate.item.provider.PlaceholderType;
import com.archyx.slate.item.provider.SingleItemProvider;
import com.archyx.slate.menu.ActiveMenu;
import fr.minuskube.inv.content.SlotPos;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class StatsItem extends AbstractItem implements SingleItemProvider {

    public StatsItem(AureliumSkills plugin) {
        super(plugin);
    }

    @Override
    public @NotNull String onPlaceholderReplace(@NotNull String placeholder, @NotNull Player player, @NotNull ActiveMenu activeMenu, @NotNull PlaceholderType placeholderType) {
        @Nullable Locale locale = plugin.getLang().getLocale(player);
        switch (placeholder) {
            case "stats":
                return Lang.getMessage(MenuMessage.STATS, locale);
            case "stats_desc":
                return Lang.getMessage(MenuMessage.STATS_DESC, locale);
            case "stats_click":
                return Lang.getMessage(MenuMessage.STATS_CLICK, locale);
        }
        return placeholder;
    }

    @Override
    public void onClick(@NotNull Player player, @NotNull InventoryClickEvent event, @NotNull ItemStack item, @NotNull SlotPos pos, @NotNull ActiveMenu activeMenu) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("previous_menu", "skills");
        plugin.getMenuManager().openMenu(player, "stats", properties);
    }

    @Override
    public @NotNull ItemStack onItemModify(@NotNull ItemStack baseItem, @NotNull Player player, @NotNull ActiveMenu activeMenu) {
        if (baseItem.getItemMeta() instanceof SkullMeta) {
            @Nullable SkullMeta meta = (SkullMeta) baseItem.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()));
                baseItem.setItemMeta(meta);
            }
        }
        return baseItem;
    }
}
