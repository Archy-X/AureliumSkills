package com.archyx.aureliumskills.menus.abilities;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.ability.Ability;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.menus.common.AbstractItem;
import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.util.math.NumberUtil;
import com.archyx.aureliumskills.util.misc.DataUtil;
import com.archyx.slate.item.provider.TemplateItemProvider;
import com.archyx.slate.menu.ActiveMenu;
import fr.minuskube.inv.content.SlotPos;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class AbstractAbilityItem extends AbstractItem implements TemplateItemProvider<Ability> {

    private final String itemName;

    public AbstractAbilityItem(AureliumSkills plugin, String itemName) {
        super(plugin);
        this.itemName = itemName;
    }

    @Override
    public Class<Ability> getContext() {
        return Ability.class;
    }

    @Override
    public SlotPos getSlotPos(Player player, ActiveMenu activeMenu, Ability ability) {
        Skill skill = getSkill(activeMenu);
        Object obj =  activeMenu.getItemOption(itemName, "slots");
        if (obj instanceof List<?>) {
            List<String> slots = DataUtil.castStringList(obj);
            // Get the index of the ability
            int index = 0;
            for (Supplier<Ability> abilitySupplier : skill.getAbilities()) {
                Ability skillAbility = abilitySupplier.get();
                if (plugin.getAbilityManager().isEnabled(skillAbility) && OptionL.isEnabled(skill)) {
                    if (skillAbility == ability) {
                        break;
                    }
                    index++;
                }
            }
            if (slots.size() > index) {
                String slot = slots.get(index);
                return parseSlot(slot);
            }
        }
        // Default slots
        List<Ability> abilityList = new ArrayList<>();
        for (Supplier<Ability> abilitySupplier : skill.getAbilities()) {
            Ability skillAbility = abilitySupplier.get();
            if (plugin.getAbilityManager().isEnabled(skillAbility) && OptionL.isEnabled(skill)) {
                abilityList.add(abilitySupplier.get());
            }
        }
        int index = abilityList.indexOf(ability);
        return SlotPos.of(1, 2 + index);
    }

    private SlotPos parseSlot(String slotString) {
        String[] split = slotString.split(",", 2);
        if (split.length == 2) {
            return SlotPos.of(NumberUtil.toInt(split[0]), NumberUtil.toInt(split[1]));
        } else {
            int num = NumberUtil.toInt(split[0]);
            int row = num / 9;
            int column = num % 9;
            return SlotPos.of(row, column);
        }
    }

    @Override
    public ItemStack onItemModify(ItemStack baseItem, Player player, ActiveMenu activeMenu, Ability ability) {
        // Hide abilities that are disabled
        if (!plugin.getAbilityManager().isEnabled(ability)) {
            return null;
        }
        return baseItem;
    }

    private Skill getSkill(ActiveMenu activeMenu) {
        Object property = activeMenu.getProperty("skill");
        if (!(property instanceof Skill)) {
            throw new IllegalArgumentException("Could not get menu skill property");
        }
        return (Skill) property;
    }

}
