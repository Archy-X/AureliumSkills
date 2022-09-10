package com.archyx.aureliumskills.util.entity;

import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public class EntityDataMappings {

    public static @NotNull Class<? extends EntityData> getDataClass(EntityType type) {
        return EntityData.class;
    }

}
