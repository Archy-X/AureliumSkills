package com.archyx.aureliumskills.util.entity;

import org.bukkit.entity.EntityType;

public class EntityDataMappings {

    public static Class<? extends EntityData> getDataClass(EntityType type) {
        return EntityData.class;
    }

}
