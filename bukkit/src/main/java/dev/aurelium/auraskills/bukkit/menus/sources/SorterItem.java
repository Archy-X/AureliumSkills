package dev.aurelium.auraskills.bukkit.menus.sources;

import com.archyx.slate.item.provider.ListBuilder;
import com.archyx.slate.item.provider.PlaceholderData;
import com.archyx.slate.item.provider.SingleItemProvider;
import com.archyx.slate.menu.ActiveMenu;
import dev.aurelium.auraskills.bukkit.AuraSkills;
import dev.aurelium.auraskills.bukkit.menus.common.AbstractItem;
import dev.aurelium.auraskills.common.message.type.MenuMessage;
import dev.aurelium.auraskills.common.util.text.TextUtil;
import fr.minuskube.inv.content.SlotPos;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public class SorterItem extends AbstractItem implements SingleItemProvider {

    public SorterItem(AuraSkills plugin) {
        super(plugin);
    }

    @Override
    public String onPlaceholderReplace(String placeholder, Player player, ActiveMenu activeMenu, PlaceholderData data) {
        Locale locale = plugin.getUser(player).getLocale();
        if (placeholder.equals("sort_types")) {
            return getSortedTypesLore(locale, activeMenu, data);
        }
        return replaceMenuMessage(placeholder, player, activeMenu);
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event, ItemStack item, SlotPos pos, ActiveMenu activeMenu) {
        SortType[] sortTypes = SortType.values();
        SortType currentType = (SortType) activeMenu.getProperty("sort_type");
        // Get the index of the current sort type in the array
        int currentTypeIndex = 0;
        for (int i = 0; i < sortTypes.length; i++) {
            SortType type = sortTypes[i];
            if (type == currentType) {
                currentTypeIndex = i;
            }
        }
        // Find the next type in the array
        SortType nextType;
        if (currentTypeIndex < sortTypes.length - 1) {
            nextType = sortTypes[currentTypeIndex + 1];
        } else {
            nextType = sortTypes[0];
        }
        // Set new sort type and reload menu
        activeMenu.setProperty("sort_type", nextType);
        activeMenu.reload();
        activeMenu.setCooldown("sorter", 5);
    }

    private String getSortedTypesLore(Locale locale, ActiveMenu activeMenu, PlaceholderData data) {
        ListBuilder builder = new ListBuilder(data.getListData());

        SortType selected = (SortType) activeMenu.getProperty("sort_type");
        for (SortType sortType : SortType.values()) {
            String typeString = TextUtil.replace(activeMenu.getFormat("sort_type_entry"), "{type_name}",
                    plugin.getMsg(MenuMessage.valueOf(sortType.toString()), locale));
            if (selected == sortType) {
                typeString = TextUtil.replace(typeString, "{selected}", activeMenu.getFormat("selected"));
            } else {
                typeString = TextUtil.replace(typeString, "{selected}", "");
            }
            builder.append(typeString);
        }

        return builder.build();
    }

    public enum SortType {

        ASCENDING,
        DESCENDING,
        ALPHABETICAL,
        REVERSE_ALPHABETICAL;

        public SourceComparator getComparator(AuraSkills plugin, Locale locale) {
            switch (this) {
                case DESCENDING:
                    return new SourceComparator.Descending(plugin);
                case ALPHABETICAL:
                    return new SourceComparator.Alphabetical(plugin, locale);
                case REVERSE_ALPHABETICAL:
                    return new SourceComparator.ReverseAlphabetical(plugin, locale);
                default:
                    return new SourceComparator.Ascending(plugin);
            }
        }

    }

}
