package com.archyx.aureliumskills.menus.stats;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.lang.MenuMessage;
import com.archyx.aureliumskills.menus.common.AbstractItem;
import com.archyx.aureliumskills.stats.Stat;
import com.archyx.aureliumskills.util.math.NumberUtil;
import com.archyx.aureliumskills.util.text.TextUtil;
import com.archyx.slate.item.provider.PlaceholderType;
import com.archyx.slate.item.provider.SingleItemProvider;
import com.archyx.slate.menu.ActiveMenu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class SkullItem extends AbstractItem implements SingleItemProvider {

    public SkullItem(AureliumSkills plugin) {
        super(plugin);
    }

    @Override
    public @NotNull String onPlaceholderReplace(@NotNull String placeholder, @NotNull Player player, @NotNull ActiveMenu activeMenu, @NotNull PlaceholderType type) {
        @Nullable Locale locale = plugin.getLang().getLocale(player);
        if (placeholder.equals("player")) {
            return player.getName();
        }
        @Nullable PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData != null) {
            Stat stat = plugin.getStatRegistry().getStat(placeholder);
            if (stat != null) {
                return TextUtil.replace(Lang.getMessage(MenuMessage.PLAYER_STAT_ENTRY, locale),
                        "{color}", stat.getColor(locale),
                        "{symbol}", stat.getSymbol(locale),
                        "{stat}", stat.getDisplayName(locale),
                        "{level}", NumberUtil.format1(playerData.getStatLevel(stat)));
            }
        }
        return placeholder;
    }

    @Override
    public @NotNull ItemStack onItemModify(@NotNull ItemStack baseItem, @NotNull Player player, @NotNull ActiveMenu activeMenu) {
        if (baseItem.getItemMeta() instanceof SkullMeta) {
            SkullMeta meta = (SkullMeta) baseItem.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()));
                baseItem.setItemMeta(meta);
            }
        }
        return baseItem;
    }
}
