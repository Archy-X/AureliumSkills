package com.archyx.aureliumskills.menus.sources;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.ability.Ability;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.lang.CustomMessageKey;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.lang.MenuMessage;
import com.archyx.aureliumskills.menus.common.AbstractItem;
import com.archyx.aureliumskills.menus.sources.SorterItem.SortType;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.source.Source;
import com.archyx.aureliumskills.util.math.NumberUtil;
import com.archyx.aureliumskills.util.text.TextUtil;
import com.archyx.slate.item.provider.PlaceholderType;
import com.archyx.slate.item.provider.TemplateItemProvider;
import com.archyx.slate.menu.ActiveMenu;
import com.cryptomorin.xseries.XMaterial;
import fr.minuskube.inv.content.SlotPos;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class SourceItem extends AbstractItem implements TemplateItemProvider<Source> {

    public SourceItem(AureliumSkills plugin) {
        super(plugin);
    }

    @Override
    public Class<Source> getContext() {
        return Source.class;
    }

    @Override
    public String onPlaceholderReplace(String placeholder, Player player, ActiveMenu activeMenu, PlaceholderType placeholderType, Source source) {
        Locale locale = plugin.getLang().getLocale(player);
        switch (placeholder) {
            case "source_name":
                return TextUtil.replace(Lang.getMessage(MenuMessage.SOURCE_NAME, locale),
                        "{name}", source.getDisplayName(locale));
            case "source_xp":
                String unitName = source.getUnitName();
                if (unitName == null) {
                    return TextUtil.replace(Lang.getMessage(MenuMessage.SOURCE_XP, locale),
                            "{xp}", NumberUtil.format2(plugin.getSourceManager().getXp(source)));
                } else {
                    return TextUtil.replace(Lang.getMessage(MenuMessage.SOURCE_XP_RATE, locale),
                            "{xp}", NumberUtil.format2(plugin.getSourceManager().getXp(source)),
                            "{unit}", getCustomMessage("sources.units." + unitName, locale));
                }
            case "multiplied_xp":
                Skill skill = getSkill(activeMenu);
                double multiplier = getMultiplier(player, skill);
                if (multiplier > 1.0) {
                    String unit = source.getUnitName();
                    if (unit == null) {
                        return TextUtil.replace(Lang.getMessage(MenuMessage.MULTIPLIED_XP, locale),
                                "{xp}", NumberUtil.format1(plugin.getSourceManager().getXp(source) * multiplier));
                    } else {
                        return TextUtil.replace(Lang.getMessage(MenuMessage.MULTIPLIED_XP_RATE, locale),
                                "{xp}", NumberUtil.format1(plugin.getSourceManager().getXp(source) * multiplier),
                                "{unit}", getCustomMessage("sources.units." + unit, locale));
                    }
                } else {
                    return "";
                }
            case "multiplied_desc":
                Skill skill1 = getSkill(activeMenu);
                if (getMultiplier(player, skill1) > 1.0) {
                    return Lang.getMessage(MenuMessage.MULTIPLIED_DESC, locale);
                } else {
                    return "";
                }
        }
        return placeholder;
    }

    @Override
    public Set<Source> getDefinedContexts(Player player, ActiveMenu activeMenu) {
        // Gets the needed properties from the menu
        SortType sortType = getSortType(activeMenu);
        Skill skill = getSkill(activeMenu);
        int itemsPerPage = getItemsPerPage(activeMenu);
        int page = activeMenu.getCurrentPage();
        Locale locale = plugin.getLang().getLocale(player);
        // Sort the sources in the skill by the selected sort type
        Source[] allSources = plugin.getSourceRegistry().values(skill);
        // Filter valid sources
        List<Source> filteredSources = new ArrayList<>();
        for (Source source : allSources) {
            if (plugin.getSourceManager().getXp(source) == 0.0) {
                continue;
            }
            if (source.getMenuItem() == null) {
                continue;
            }
            filteredSources.add(source);
        }
        filteredSources.sort(sortType.getComparator(plugin, locale));
        // Gets a sublist of the sources displayed based on the current page
        int toIndex = (page + 1) * itemsPerPage;
        if (toIndex > filteredSources.size()) {
            toIndex = filteredSources.size();
        }
        List<Source> shownSources = filteredSources.subList(page * itemsPerPage, toIndex);
        activeMenu.setProperty("sources", shownSources); // Set sorted sources property for easy access in other methods
        return new HashSet<>(shownSources);
    }

    @Override
    public SlotPos getSlotPos(Player player, ActiveMenu activeMenu, Source source) {
        List<Source> sources = getSortedSources(activeMenu);
        int index = sources.indexOf(source);
        if (index != -1) {
            // Convert index of source into position on menu
            int row = 1 + index / 7;
            int column = 1 + index % 7;
            return SlotPos.of(row, column);
        } else {
            return null;
        }
    }

    @Override
    public ItemStack onItemModify(ItemStack baseItem, Player player, ActiveMenu activeMenu, Source source) {
        if (baseItem.getType() != XMaterial.GRAY_DYE.parseMaterial()) {
            return baseItem;
        }
        ItemStack item = source.getMenuItem();
        if (item == null) {
            plugin.getLogger().warning("Item of source " + source.getPath() + " not found");
        }
        return item;
    }

    // Safely get list of sources from property
    private List<Source> getSortedSources(ActiveMenu activeMenu) {
        List<Source> sources = new ArrayList<>();
        List<?> list = getSources(activeMenu);
        for (Object element : list) {
            if (element instanceof Source) {
                sources.add((Source) element);
            }
        }
        return sources;
    }

    private double getMultiplier(Player player, Skill skill) {
        Ability ability = skill.getXpMultiplierAbility();
        PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        if (playerData == null) {
            return 1.0;
        }
        double multiplier = 1.0;
        if (playerData.getAbilityLevel(ability) > 0) {
            double abilityValue = plugin.getAbilityManager().getValue(ability, playerData.getAbilityLevel(ability));
            double addedMultiplier = abilityValue / 100;
            multiplier += addedMultiplier;
        }
        multiplier *= plugin.getLeveler().getMultiplier(player, skill);
        return multiplier;
    }

    private String getCustomMessage(String path, Locale locale) {
        String message;
        try {
            message = Lang.getMessage(new CustomMessageKey(path), locale);
        }
        catch (IllegalStateException ex) {
            throw new IllegalStateException("Unknown custom message with path: " + path);
        }
        return message;
    }

    private List<?> getSources(ActiveMenu activeMenu) {
        Object property = activeMenu.getProperty("sources");
        if (!(property instanceof List)) {
            // TODO: Unclear if this should throw an exception
            //throw new IllegalArgumentException("Could not get menu sources property");
            return Collections.EMPTY_LIST;
        }
        return (List<?>) property;
    }

    protected int getItemsPerPage(ActiveMenu activeMenu) {
        Object property = activeMenu.getProperty("items_per_page");
        int itemsPerPage;
        if (property instanceof Integer) {
            itemsPerPage = (Integer) property;
        } else {
            itemsPerPage = 24;
        }
        return itemsPerPage;
    }

    private SortType getSortType(ActiveMenu activeMenu) {
        Object property = activeMenu.getProperty("sort_type");
        if (!(property instanceof SortType)) {
            throw new IllegalArgumentException("Could not get menu sort_type property");
        }
        return (SortType) property;
    }

    private Skill getSkill(ActiveMenu activeMenu) {
        Object property = activeMenu.getProperty("skill");
        if (!(property instanceof Skill)) {
            throw new IllegalArgumentException("Could not get menu skill property");
        }
        return (Skill) property;
    }

}
