package com.archyx.aureliumskills.skills.fishing;

import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.skills.Skills;
import com.archyx.aureliumskills.source.Source;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public enum FishingSource implements Source {

    COD,
    SALMON,
    TROPICAL_FISH,
    PUFFERFISH,
    TREASURE,
    JUNK,
    RARE,
    EPIC;

    @Override
    public Skill getSkill() {
        return Skills.FISHING;
    }

    @Override
    public String getPath() {
        return "fishing." + toString().toLowerCase(Locale.ROOT);
    }

    @SuppressWarnings("deprecation")
    @Nullable
    public static FishingSource valueOf(ItemStack item) {
        Material mat = item.getType();
        if (XMaterial.isNewVersion()) {
            if (mat.equals(XMaterial.COD.parseMaterial())) {
                return FishingSource.COD;
            } else if (mat.equals(XMaterial.SALMON.parseMaterial())) {
                return FishingSource.SALMON;
            } else if (mat.equals(XMaterial.TROPICAL_FISH.parseMaterial())) {
                return FishingSource.TROPICAL_FISH;
            } else if (mat.equals(XMaterial.PUFFERFISH.parseMaterial())) {
                return FishingSource.PUFFERFISH;
            }
        } else if (mat.equals(XMaterial.COD.parseMaterial())) {
            switch (item.getDurability()) {
                case 0:
                    return FishingSource.COD;
                case 1:
                    return FishingSource.SALMON;
                case 2:
                    return FishingSource.TROPICAL_FISH;
                case 3:
                    return FishingSource.PUFFERFISH;
            }
        }
        if (mat.equals(Material.BOW) || mat.equals(Material.ENCHANTED_BOOK) || mat.equals(Material.NAME_TAG) || mat.equals(Material.SADDLE) || mat.equals(XMaterial.NAUTILUS_SHELL.parseMaterial())) {
            return FishingSource.TREASURE;
        } else if (mat.equals(Material.BOWL) || mat.equals(Material.LEATHER) || mat.equals(Material.LEATHER_BOOTS) || mat.equals(Material.ROTTEN_FLESH)
                || mat.equals(Material.POTION) || mat.equals(Material.BONE) || mat.equals(Material.TRIPWIRE_HOOK) || mat.equals(Material.STICK)
                || mat.equals(Material.STRING) || mat.equals(XMaterial.INK_SAC.parseMaterial()) || mat.equals(XMaterial.LILY_PAD.parseMaterial())
                || mat.equals(XMaterial.BAMBOO.parseMaterial())) {
            return FishingSource.JUNK;
        } else if (mat.equals(Material.FISHING_ROD)) {
            if (item.getEnchantments().size() != 0) {
                return FishingSource.TREASURE;
            } else {
                return FishingSource.JUNK;
            }
        }
        return null;
    }

}
