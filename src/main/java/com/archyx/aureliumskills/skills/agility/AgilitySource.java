package com.archyx.aureliumskills.skills.agility;

import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.skills.Skills;
import com.archyx.aureliumskills.source.Source;
import com.archyx.aureliumskills.util.item.ItemUtils;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public enum AgilitySource implements Source {

    JUMP_PER_100("FEATHER", "100_jumps"),
    FALL_DAMAGE("DIAMOND_BOOTS", "damage");

    private final @NotNull String material;
    private final @NotNull String unitName;

    AgilitySource(@NotNull String material, @NotNull String unitName) {
        this.material = material.toUpperCase(Locale.ROOT);
        this.unitName = unitName;
    }

    @Override
    public @NotNull Skill getSkill() {
        return Skills.AGILITY;
    }

    @Override
    public @NotNull String getUnitName() {
        return unitName;
    }

    @Override
    public @Nullable ItemStack getMenuItem() {
        @Nullable ItemStack item = ItemUtils.parseItem(material);
        if (item != null) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && ItemUtils.isArmor(item.getType())) {
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                item.setItemMeta(meta);
            }
        }
        return item;
    }
}
