package com.archyx.aureliumskills.menus.common;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.lang.MenuMessage;
import com.archyx.slate.item.provider.PlaceholderType;
import com.archyx.slate.item.provider.SingleItemProvider;
import com.archyx.slate.menu.ActiveMenu;
import fr.minuskube.inv.content.SlotPos;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class PreviousPageItem extends AbstractItem implements SingleItemProvider {

    public PreviousPageItem(AureliumSkills plugin) {
        super(plugin);
    }

    @Override
    public @NotNull String onPlaceholderReplace(@NotNull String placeholder, @NotNull Player player, @NotNull ActiveMenu activeMenu, @NotNull PlaceholderType type) {
        @Nullable Locale locale = plugin.getLang().getLocale(player);
        switch (placeholder) {
            case "previous_page":
                return Lang.getMessage(MenuMessage.PREVIOUS_PAGE, locale);
            case "previous_page_click":
                return Lang.getMessage(MenuMessage.PREVIOUS_PAGE_CLICK, locale);
        }
        return placeholder;
    }

    @Override
    public void onClick(@NotNull Player player, @NotNull InventoryClickEvent event, @NotNull ItemStack item, @NotNull SlotPos pos, @NotNull ActiveMenu activeMenu) {
        plugin.getMenuManager().openMenu(player, activeMenu.getName(), activeMenu.getProperties(), activeMenu.getCurrentPage() - 1);
    }

    @Override
    public @Nullable ItemStack onItemModify(@NotNull ItemStack baseItem, @NotNull Player player, @NotNull ActiveMenu activeMenu) {
        if (activeMenu.getCurrentPage() == 0) {
            return null;
        }
        return baseItem;
    }
}
