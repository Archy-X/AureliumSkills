package com.archyx.aureliumskills.source;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BlockSource {

    @Nullable String getLegacyMaterial();

    byte getLegacyData();

    boolean allowBothIfLegacy();

    @SuppressWarnings("deprecation")
    default boolean isMatch(@NotNull Block block) {
        boolean matched = false;
        String materialName = block.getType().toString();
        @Nullable String legacyMaterial =  getLegacyMaterial();
        if (XMaterial.isNewVersion() || legacyMaterial == null) { // Standard block handling
            if (toString().equalsIgnoreCase(materialName)) {
                matched = true;
            }
        } else { // Legacy block handling
            if (getLegacyData() == (byte) -1) { // No data value
                if (allowBothIfLegacy()) { // Allow both new and legacy material names
                    if (legacyMaterial.equalsIgnoreCase(materialName) || toString().equalsIgnoreCase(materialName)) {
                        matched = true;
                    }
                } else if (legacyMaterial.equalsIgnoreCase(materialName)) {
                    matched = true;
                }
            } else { // With data value
                if (allowBothIfLegacy()) { // Allow both new and legacy material names
                    if ((legacyMaterial.equalsIgnoreCase(materialName) && getLegacyData() == block.getData()
                            || (toString().equalsIgnoreCase(materialName) && getLegacyData() == block.getData()))) {
                        matched = true;
                    }
                } else if (legacyMaterial.equalsIgnoreCase(materialName) && getLegacyData() == block.getData()) {
                    matched = true;
                }
            }
        }
        return matched;
    }

}
