package com.archyx.aureliumskills.skills.endurance;

import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.skills.Skills;
import com.archyx.aureliumskills.source.Source;
import com.archyx.aureliumskills.util.item.ItemUtils;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum EnduranceSource implements Source {

    WALK_PER_METER("IRON_BOOTS"),
    SPRINT_PER_METER("LEATHER_BOOTS"),
    SWIM_PER_METER("WATER_BUCKET");

    private final @NotNull String material;

    EnduranceSource(@NotNull String material) {
        this.material = material;
    }

    @Override
    public @NotNull Skill getSkill() {
        return Skills.ENDURANCE;
    }

    @Override
    public @NotNull String getUnitName() {
        return "meter";
    }

    @Override
    public @Nullable ItemStack getMenuItem() {
        @Nullable ItemStack item = ItemUtils.parseItem(material);
        if (item != null) {
            @Nullable ItemMeta meta = item.getItemMeta();
            if (meta != null && ItemUtils.isArmor(item.getType())) {
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                item.setItemMeta(meta);
            }
        }
        return item;
    }
}
