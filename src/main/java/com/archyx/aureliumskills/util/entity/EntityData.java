package com.archyx.aureliumskills.util.entity;

import com.archyx.aureliumskills.util.misc.Parser;
import com.archyx.aureliumskills.util.version.VersionUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EntityData extends Parser {

    private final EntityType type;
    private final boolean glowing;
    private final int fireTicks;
    private final boolean visualFire;
    private final boolean persistent;
    private final List<EntityData> passengers;
    private final boolean invulnerable;
    private final boolean silent;
    private final boolean gravity;
    private final boolean customNameVisible;
    private final String customName;
    private final List<String> scoreboardTags;

    public EntityData(Map<?, ?> data) {
        type = EntityType.valueOf(getString(data, "entity_type"));
        glowing = getBooleanOrDefault(data, "glowing", false);
        fireTicks = getIntOrDefault(data, "fire_ticks", 0);
        visualFire = getBooleanOrDefault(data, "visual_fire", false);
        persistent = getBooleanOrDefault(data, "persistent", true);
        passengers = new ArrayList<>();
        for (Map<?, ?> passengerData : getMapListOrDefault(data, "passengers", new ArrayList<>())) {
            passengers.add(new EntityData(passengerData));
        }
        invulnerable = getBooleanOrDefault(data, "invulnerable", false);
        silent = getBooleanOrDefault(data, "silent", false);
        gravity = getBooleanOrDefault(data, "gravity", true);
        customNameVisible = getBooleanOrDefault(data, "custom_name_visible", true);
        customName = getStringOrDefault(data, "custom_name", null);
        scoreboardTags = getStringListOrDefault(data, "scoreboard_tags", new ArrayList<>());
    }

    /**
     * Spawns an entity with this entity data at a given location
     * @param location The location to spawn
     * @return The Entity spawned, null if the location does not have a World
     */
    @Nullable
    public Entity spawn(Location location) {
        // Spawn entity
        World world = location.getWorld();
        if (world == null) return null;
        Entity entity = world.spawnEntity(location, type);
        applyData(entity, location);
        return entity;
    }

    protected void applyData(Entity entity, Location location) {
        entity.setGlowing(glowing);
        entity.setFireTicks(fireTicks);
        if (VersionUtils.isAtLeastVersion(17)) {
            entity.setVisualFire(visualFire);
        }
        entity.setPersistent(persistent);
        for (EntityData passengerData : passengers) {
            Entity passenger = passengerData.spawn(location);
            if (passenger != null) {
                entity.addPassenger(passenger);
            }
        }
        entity.setInvulnerable(invulnerable);
        entity.setSilent(silent);
        entity.setGravity(gravity);
        entity.setCustomNameVisible(customNameVisible);
        entity.setCustomName(customName);
        for (String scoreboardTag : scoreboardTags) {
            entity.addScoreboardTag(scoreboardTag);
        }
    }

}
