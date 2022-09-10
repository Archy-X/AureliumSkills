package com.archyx.aureliumskills.skills.sorcery;

import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.skills.Skills;
import com.archyx.aureliumskills.source.BlockSource;
import com.archyx.aureliumskills.source.Source;
import com.archyx.aureliumskills.util.item.ItemUtils;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public enum SorcerySource implements Source, BlockSource {

    MANA_ABILITY_USE("LIGHT_BLUE_DYE"),
    SCULK,
    SCULK_CATALYST,
    SCULK_SHRIEKER,
    SCULK_VEIN,
    SCULK_SENSOR;

    private final String material;

    SorcerySource() {
        this.material = this.name();
    }

    SorcerySource(String material) {
        this.material = material;
    }

    @Override
    public Skill getSkill() {
        return Skills.SORCERY;
    }

    @Override
    public String getUnitName() {
        if (this == MANA_ABILITY_USE) {
            return "mana";
        } else {
            return null;
        }
    }

    @Override
    public ItemStack getMenuItem() {
        return ItemUtils.parseItem(material);
    }

    public static SorcerySource getSource(Block block) {
        for (SorcerySource source : values()) {
            if (source.isMatch(block)) {
                return source;
            }
        }
        return null;
    }

    @Override
    public String getLegacyMaterial() {
        return null;
    }

    @Override
    public byte getLegacyData() {
        return 0;
    }

    @Override
    public boolean allowBothIfLegacy() {
        return false;
    }
}
