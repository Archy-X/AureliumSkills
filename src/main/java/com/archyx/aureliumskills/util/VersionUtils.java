package com.archyx.aureliumskills.util;

import org.bukkit.entity.EntityType;

public class VersionUtils {

    public static boolean isPigman(EntityType type) {
        if (XMaterial.getVersion() == 16) {
            return type.equals(EntityType.ZOMBIFIED_PIGLIN);
        }
        else {
            return type.name().equals("PIG_ZOMBIE");
        }
    }
}
