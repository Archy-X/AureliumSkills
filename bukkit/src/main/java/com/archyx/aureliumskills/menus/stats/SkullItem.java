package com.archyx.aureliumskills.menus.stats;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.lang.MenuMessage;
import com.archyx.aureliumskills.menus.common.AbstractItem;
import com.archyx.aureliumskills.stats.Stat;
import com.archyx.aureliumskills.stats.StatProvider;
import com.archyx.aureliumskills.util.text.TextUtil;
import com.archyx.slate.item.provider.PlaceholderData;
import com.archyx.slate.item.provider.SingleItemProvider;
import com.archyx.slate.menu.ActiveMenu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Locale;

public class SkullItem extends AbstractItem implements SingleItemProvider {

    public SkullItem(AureliumSkills plugin) {
        super(plugin);
    }

    @Override
    public String onPlaceholderReplace(String placeholder, Player player, ActiveMenu activeMenu, PlaceholderData data) {
        Locale locale = plugin.getLang().getLocale(player);
        if (placeholder.equals("player")) {
            return player.getName();
        }
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData != null) {
            // Handle each stat entry
            Stat stat = plugin.getStatRegistry().getStat(placeholder);
            if (stat != null) {
                return TextUtil.replace(Lang.getMessage(MenuMessage.PLAYER_STAT_ENTRY, locale),
                        "{color}", stat.getColor(locale),
                        "{symbol}", stat.getSymbol(locale),
                        "{stat}", stat.getDisplayName(locale),
                        "{level}", getFormattedStatValue(player, stat, playerData));
            }
        }
        return placeholder;
    }

    @Override
    public ItemStack onItemModify(ItemStack baseItem, Player player, ActiveMenu activeMenu) {
        if (baseItem.getItemMeta() instanceof SkullMeta) {
            SkullMeta meta = (SkullMeta) baseItem.getItemMeta();
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()));
            baseItem.setItemMeta(meta);
        }
        return baseItem;
    }

    private String getFormattedStatValue(Player player, Stat stat, PlayerData playerData) {
        double statLevel = playerData.getStatLevel(stat);
        StatProvider provider = plugin.getStatRegistry().getProvider(stat);
        double value = provider.getEffectiveValue(plugin, player, statLevel); // Get modified stat effective value if options are enabled
        return provider.formatValue(plugin, value); // Round and add prefixes and suffixes
    }

}
