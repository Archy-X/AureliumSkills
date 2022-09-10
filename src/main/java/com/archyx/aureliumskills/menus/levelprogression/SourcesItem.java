package com.archyx.aureliumskills.menus.levelprogression;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.lang.MenuMessage;
import com.archyx.aureliumskills.menus.common.AbstractItem;
import com.archyx.aureliumskills.menus.sources.SorterItem;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.util.text.TextUtil;
import com.archyx.slate.item.provider.PlaceholderType;
import com.archyx.slate.item.provider.SingleItemProvider;
import com.archyx.slate.menu.ActiveMenu;
import fr.minuskube.inv.content.SlotPos;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SourcesItem extends AbstractItem implements SingleItemProvider {

    public SourcesItem(AureliumSkills plugin) {
        super(plugin);
    }

    @Override
    public @NotNull String onPlaceholderReplace(@NotNull String placeholder, @NotNull Player player, @NotNull ActiveMenu activeMenu, @NotNull PlaceholderType placeholderType) {
        @Nullable Locale locale = plugin.getLang().getLocale(player);
        switch (placeholder) {
            case "sources":
                return Lang.getMessage(MenuMessage.SOURCES, locale);
            case "sources_desc":
                return Lang.getMessage(MenuMessage.SOURCES_DESC, locale);
            case "sources_click":
                Skill skill = getSkill(activeMenu);
                return TextUtil.replace(Lang.getMessage(MenuMessage.SOURCES_CLICK, locale),
                        "{skill}", skill.getDisplayName(locale));
        }
        return placeholder;
    }

    @Override
    public void onClick(@NotNull Player player, @NotNull InventoryClickEvent event, @NotNull ItemStack item, @NotNull SlotPos pos, @NotNull ActiveMenu activeMenu) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("skill", getSkill(activeMenu));
        properties.put("items_per_page", 28);
        properties.put("sort_type", SorterItem.SortType.ASCENDING);
        properties.put("previous_menu", "level_progression");
        plugin.getMenuManager().openMenu(player, "sources", properties, 0);
    }

    private @NotNull Skill getSkill(@NotNull ActiveMenu activeMenu) {
        @Nullable Object property = activeMenu.getProperty("skill");
        if (!(property instanceof Skill)) {
            throw new IllegalArgumentException("Could not get menu skill property");
        }
        return (Skill) property;
    }

}
