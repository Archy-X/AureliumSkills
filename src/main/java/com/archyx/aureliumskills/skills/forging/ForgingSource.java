package com.archyx.aureliumskills.skills.forging;

import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.skills.Skills;
import com.archyx.aureliumskills.source.Source;
import com.archyx.aureliumskills.util.item.ItemUtils;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum ForgingSource implements Source {
    
    COMBINE_BOOKS_PER_LEVEL("ENCHANTED_BOOK", "combine_level"),
    COMBINE_WEAPON_PER_LEVEL("IRON_SWORD", "combine_level"),
    COMBINE_ARMOR_PER_LEVEL("IRON_LEGGINGS", "combine_level"),
    COMBINE_TOOL_PER_LEVEL("IRON_SHOVEL", "combine_level"),
    GRINDSTONE_PER_LEVEL("GRINDSTONE", "grindstone_level");

    private final @NotNull String material;
    private final @NotNull String unitName;

    ForgingSource(@NotNull String material, @NotNull String unitName) {
        this.material = material;
        this.unitName = unitName;
    }

    @Override
    public @NotNull Skill getSkill() {
        return Skills.FORGING;
    }

    @Override
    public @NotNull String getUnitName() {
        return unitName;
    }

    @Override
    public @Nullable ItemStack getMenuItem() {
        @Nullable ItemStack item = ItemUtils.parseItem(material);
        if (item != null) {
            @Nullable ItemMeta meta = item.getItemMeta();
            if (meta != null && (ItemUtils.isArmor(item.getType()) || ItemUtils.isWeapon(item.getType()) || ItemUtils.isTool(item.getType()))) {
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                item.setItemMeta(meta);
            }
        }
        return item;
    }
}
