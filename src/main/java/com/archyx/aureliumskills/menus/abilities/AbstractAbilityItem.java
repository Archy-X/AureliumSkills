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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class AbstractAbilityItem extends AbstractItem implements TemplateItemProvider<@NotNull Ability> {

    private final @NotNull String itemName;

    public AbstractAbilityItem(AureliumSkills plugin, @NotNull String itemName) {
        super(plugin);
        this.itemName = itemName;
    }

    @Override
    public @NotNull Class<@NotNull Ability> getContext() {
        return Ability.class;
    }

    @Override
    public @NotNull SlotPos getSlotPos(@NotNull Player player, @NotNull ActiveMenu activeMenu, @NotNull Ability ability) {
        Skill skill = getSkill(activeMenu);
        @Nullable Object obj =  activeMenu.getItemOption(itemName, "slots");
        if (obj instanceof List<?>) {
            List<@NotNull String> slots = DataUtil.castStringList(obj);
            // Get the index of the ability
            int index = 0;
            for (Supplier<@NotNull Ability> abilitySupplier : skill.getAbilities()) {
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
        for (Supplier<@NotNull Ability> abilitySupplier : skill.getAbilities()) {
            Ability skillAbility = abilitySupplier.get();
            if (plugin.getAbilityManager().isEnabled(skillAbility) && OptionL.isEnabled(skill)) {
                abilityList.add(abilitySupplier.get());
            }
        }
        int index = abilityList.indexOf(ability);
        return SlotPos.of(1, 2 + index);
    }

    private @NotNull SlotPos parseSlot(@NotNull String slotString) {
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
    public @Nullable ItemStack onItemModify(@NotNull ItemStack baseItem, @NotNull Player player, @NotNull ActiveMenu activeMenu, @NotNull Ability ability) {
        // Hide abilities that are disabled
        if (!plugin.getAbilityManager().isEnabled(ability)) {
            return null;
        }
        return baseItem;
    }

    private @NotNull Skill getSkill(@NotNull ActiveMenu activeMenu) {
        @Nullable Object property = activeMenu.getProperty("skill");
        if (!(property instanceof Skill)) {
            throw new IllegalArgumentException("Could not get menu skill property");
        }
        return (Skill) property;
    }

}
