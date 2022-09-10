package com.archyx.aureliumskills.skills.sorcery;

import com.archyx.aureliumskills.skills.Skill;
import com.archyx.aureliumskills.skills.Skills;
import com.archyx.aureliumskills.source.BlockSource;
import com.archyx.aureliumskills.source.Source;
import com.archyx.aureliumskills.util.item.ItemUtils;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum SorcerySource implements Source, BlockSource {

    MANA_ABILITY_USE("LIGHT_BLUE_DYE"),
    SCULK,
    SCULK_CATALYST,
    SCULK_SHRIEKER,
    SCULK_VEIN,
    SCULK_SENSOR;

    private final @NotNull String material;

    SorcerySource() {
        this.material = this.name();
    }

    SorcerySource(@NotNull String material) {
        this.material = material;
    }

    @Override
    public @NotNull Skill getSkill() {
        return Skills.SORCERY;
    }

    @Override
    public @Nullable String getUnitName() {
        if (this == MANA_ABILITY_USE) {
            return "mana";
        } else {
            return null;
        }
    }

    @Override
    public @Nullable ItemStack getMenuItem() {
        return ItemUtils.parseItem(material);
    }

    public static @Nullable SorcerySource getSource(@NotNull Block block) {
        for (SorcerySource source : values()) {
            if (source.isMatch(block)) {
                return source;
            }
        }
        return null;
    }

    @Override
    public @Nullable String getLegacyMaterial() {
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
