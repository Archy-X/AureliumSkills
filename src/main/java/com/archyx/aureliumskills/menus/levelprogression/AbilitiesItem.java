package com.archyx.aureliumskills.menus.levelprogression;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.ability.Ability;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.lang.MenuMessage;
import com.archyx.aureliumskills.menus.common.AbstractItem;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.skills.Skills;
import com.archyx.aureliumskills.util.text.TextUtil;
import com.archyx.slate.item.provider.PlaceholderType;
import com.archyx.slate.item.provider.SingleItemProvider;
import com.archyx.slate.menu.ActiveMenu;
import fr.minuskube.inv.content.SlotPos;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

public class AbilitiesItem extends AbstractItem implements SingleItemProvider {

    public AbilitiesItem(AureliumSkills plugin) {
        super(plugin);
    }

    @Override
    public String onPlaceholderReplace(String placeholder, Player player, ActiveMenu activeMenu, PlaceholderType type) {
        Locale locale = plugin.getLang().getLocale(player);
        switch (placeholder) {
            case "abilities":
                return Lang.getMessage(MenuMessage.ABILITIES, locale);
            case "abilities_desc":
                return Lang.getMessage(MenuMessage.ABILITIES_DESC, locale);
            case "abilities_click":
                Skill skill = getSkill(activeMenu);
                return TextUtil.replace(Lang.getMessage(MenuMessage.ABILITIES_CLICK, locale), "{skill}", skill.getDisplayName(locale));
        }
        return placeholder;
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event, ItemStack item, SlotPos pos, ActiveMenu activeMenu) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("skill", getSkill(activeMenu));
        properties.put("previous_menu", "level_progression");
        plugin.getMenuManager().openMenu(player, "abilities", properties);
    }

    @Override
    public ItemStack onItemModify(ItemStack baseItem, Player player, ActiveMenu activeMenu) {
        Skill skill = getSkill(activeMenu);
        if (skill == Skills.SORCERY) { // Disable for sorcery abilities REMOVE ONCE SORCERY ABILITIES ARE ADDED
            return null;
        }
        // Check if the skill has an enabled ability
        boolean hasEnabledAbility = false;
        for (Supplier<Ability> abilitySupplier : skill.getAbilities()) {
           if (plugin.getAbilityManager().isEnabled(abilitySupplier.get())) {
                hasEnabledAbility = true;
                break;
            }
        }
        if (hasEnabledAbility) {
            return baseItem;
        }
        return null; // Don't show item if no abilities are enabled
    }

    private Skill getSkill(ActiveMenu activeMenu) {
        Object property = activeMenu.getProperty("skill");
        if (!(property instanceof Skill)) {
            throw new IllegalArgumentException("Could not get menu skill property");
        }
        return (Skill) property;
    }

}
