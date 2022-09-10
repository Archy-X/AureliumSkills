package com.archyx.aureliumskills.skills.alchemy;

import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.skills.Skills;
import com.archyx.aureliumskills.source.Source;
import com.archyx.aureliumskills.util.item.ItemUtils;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.Locale;

public enum AlchemySource implements Source {

    AWKWARD("POTION", PotionType.AWKWARD),
    REGULAR("POTION", PotionType.SPEED),
    EXTENDED("POTION", PotionType.NIGHT_VISION),
    UPGRADED("POTION", PotionType.STRENGTH),
    SPLASH("SPLASH_POTION", PotionType.JUMP),
    LINGERING("LINGERING_POTION", PotionType.INSTANT_DAMAGE);

    private final String material;
    private final PotionType potionType;

    AlchemySource(String material, PotionType potionType) {
        this.material = material.toUpperCase(Locale.ROOT);
        this.potionType = potionType;
    }

    @Override
    public Skill getSkill() {
        return Skills.ALCHEMY;
    }

    @Override
    public ItemStack getMenuItem() {
        ItemStack baseItem = ItemUtils.parseItem(material);
        if (baseItem != null && baseItem.getItemMeta() instanceof PotionMeta) {
            PotionMeta meta = (PotionMeta) baseItem.getItemMeta();
            if (meta != null) {
                meta.setBasePotionData(new PotionData(potionType));
                meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
                baseItem.setItemMeta(meta);
            }
        }
        return baseItem;
    }
}
