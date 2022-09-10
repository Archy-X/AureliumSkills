package com.archyx.aureliumskills.menus.common;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.lang.MenuMessage;
import com.archyx.aureliumskills.util.text.TextUtil;
import com.archyx.slate.item.provider.PlaceholderType;
import com.archyx.slate.item.provider.SingleItemProvider;
import com.archyx.slate.menu.ActiveMenu;
import fr.minuskube.inv.content.SlotPos;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public class BackItem extends AbstractItem implements SingleItemProvider {

    public BackItem(AureliumSkills plugin) {
        super(plugin);
    }

    @Override
    public String onPlaceholderReplace(String placeholder, Player player, ActiveMenu activeMenu, PlaceholderType type) {
        Locale locale = plugin.getLang().getLocale(player);
        switch (placeholder) {
            case "back":
                return Lang.getMessage(MenuMessage.BACK, locale);
            case "back_click":
                String previousMenu = getPreviousMenu(activeMenu);
                String formattedPreviousMenu = TextUtil.capitalizeWord(TextUtil.replace(previousMenu, "_", " "));
                return TextUtil.replace(Lang.getMessage(MenuMessage.BACK_CLICK, locale),
                        "{menu_name}", formattedPreviousMenu);
        }
        return placeholder;
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event, ItemStack item, SlotPos pos, ActiveMenu activeMenu) {
        String previousMenu = getPreviousMenu(activeMenu);
        if (!previousMenu.isEmpty())
            plugin.getMenuManager().openMenu(player, previousMenu);
    }

    @Override
    public ItemStack onItemModify(ItemStack baseItem, Player player, ActiveMenu activeMenu) {
        if (getPreviousMenu(activeMenu).isEmpty()) {
            return null;
        }
        return baseItem;
    }

    private String getPreviousMenu(ActiveMenu activeMenu) {
        Object property = activeMenu.getProperty("previous_menu");
        if (!(property instanceof String)) {
            property = "";
        }
        return (String) property;
    }

}
