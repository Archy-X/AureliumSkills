package com.archyx.aureliumskills.skills.enchanting;

import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.skills.Skills;
import com.archyx.aureliumskills.source.Source;
import com.archyx.aureliumskills.util.item.ItemUtils;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum EnchantingSource implements Source {

    WEAPON_PER_LEVEL("DIAMOND_SWORD"),
    ARMOR_PER_LEVEL("DIAMOND_CHESTPLATE"),
    TOOL_PER_LEVEL("DIAMOND_PICKAXE"),
    BOOK_PER_LEVEL("ENCHANTED_BOOK");

    private final @NotNull String material;

    EnchantingSource(@NotNull String material) {
        this.material = material;
    }

    @Override
    public @NotNull Skill getSkill() {
        return Skills.ENCHANTING;
    }

    @Override
    public @NotNull String getUnitName() {
        return "enchant_level";
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
