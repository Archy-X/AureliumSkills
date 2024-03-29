package dev.aurelium.auraskills.bukkit.source;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.skill.Skills;
import dev.aurelium.auraskills.api.source.type.EntityXpSource;
import dev.aurelium.auraskills.api.source.type.EntityXpSource.EntityDamagers;
import dev.aurelium.auraskills.api.source.type.EntityXpSource.EntityTriggers;
import dev.aurelium.auraskills.bukkit.AuraSkills;
import dev.aurelium.auraskills.bukkit.skills.fighting.FightingAbilities;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.source.SourceTypes;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.util.data.Pair;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class EntityLeveler extends SourceLeveler {

    private final NamespacedKey SPAWNER_MOB_KEY;

    public EntityLeveler(AuraSkills plugin) {
        super(plugin, SourceTypes.ENTITY);
        this.SPAWNER_MOB_KEY = new NamespacedKey(plugin, "is_spawner_mob");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event) {
        if (disabled()) return;
        LivingEntity entity = event.getEntity();
        // Ensure that the entity has a killer
        @Nullable
        Player player = entity.getKiller();
        // Ensure that the killer is an entity
        Entity damagerEntity;
        if (entity.getLastDamageCause() instanceof EntityDamageByEntityEvent damageEvent) {
            damagerEntity = damageEvent.getDamager();
        } else {
            player = getBleedDamager(entity);
            damagerEntity = player;
        }
        if (player == null) return;

        User user = plugin.getUser(player);

        // Resolve damager from EntityDamageByEntityEvent
        EntityXpSource.EntityDamagers damager;
        if (damagerEntity instanceof Player) {
            damager = EntityXpSource.EntityDamagers.PLAYER;
        } else if (damagerEntity instanceof Projectile) {
            damager = EntityXpSource.EntityDamagers.PROJECTILE;
        } else {
            return;
        }

        Pair<EntityXpSource, Skill> sourcePair = getSource(entity, damager, EntityXpSource.EntityTriggers.DEATH);
        if (sourcePair == null) return;

        EntityXpSource source = sourcePair.first();
        Skill skill = sourcePair.second();

        if (failsChecks(player, entity.getLocation(), skill)) return;

        plugin.getLevelManager().addEntityXp(user, skill, source, getSpawnerMultiplier(entity, skill) * source.getXp(),
                entity, damagerEntity, event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (disabled()) return;
        if (!(event.getEntity() instanceof LivingEntity entity) || event.getEntity() instanceof ArmorStand) {
            return;
        }
        // Get the player who damaged the entity and the damager type
        Pair<Player, EntityXpSource.EntityDamagers> damagerPair = resolveDamager(event.getDamager(), event.getCause());
        if (damagerPair == null) return;

        Player player = damagerPair.first();
        if (player.hasMetadata("NPC")) return;
        User user = plugin.getUser(player);
        EntityXpSource.EntityDamagers damager = damagerPair.second();

        // Get matching source with damage trigger
        Pair<EntityXpSource, Skill> sourcePair = getSource(entity, damager, EntityXpSource.EntityTriggers.DAMAGE);
        if (sourcePair == null) return;

        EntityXpSource source = sourcePair.first();
        Skill skill = sourcePair.second();

        if (failsChecks(event, player, entity.getLocation(), skill)) return;

        double damageMultiplier = getDamageMultiplier(entity, source, event);
        plugin.getLevelManager().addEntityXp(user, skill, source, damageMultiplier * getSpawnerMultiplier(entity, skill) * source.getXp(),
                entity, event.getDamager(), event);
    }

    @EventHandler
    public void onBleedDamage(EntityDamageEvent event) {
        if (disabled()) return;
        if (!(event.getEntity() instanceof LivingEntity entity) || event.getEntity() instanceof ArmorStand) {
            return;
        }

        Player player = getBleedDamager(entity);
        if (player == null) return; // Was not damaged by Bleed

        if (player.hasMetadata("NPC")) return;

        User user = plugin.getUser(player);
        Pair<EntityXpSource, Skill> sourcePair = getSource(entity, EntityDamagers.PLAYER, EntityTriggers.DAMAGE);
        if (sourcePair == null) return;

        EntityXpSource source = sourcePair.first();
        Skill skill = sourcePair.second();

        if (failsChecks(player, entity.getLocation(), skill)) return;

        double damageMultiplier = getDamageMultiplier(entity, source, event);
        plugin.getLevelManager().addEntityXp(user, skill, source, damageMultiplier * getSpawnerMultiplier(entity, skill) * source.getXp(),
                entity, player, null);
    }

    @Nullable
    public Pair<Player, EntityXpSource.EntityDamagers> resolveDamager(Entity damager, EntityDamageEvent.DamageCause cause) {
        if (damager instanceof Player player) { // Player damager
            if (cause != EntityDamageEvent.DamageCause.ENTITY_ATTACK && cause != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
                return null;
            }
            return new Pair<>(player, EntityXpSource.EntityDamagers.PLAYER);
        } else if (damager instanceof Projectile projectile) { // Projectile damager
            if (!(projectile.getShooter() instanceof Player player)) { // Make sure shooter is a player
                return null;
            }
            if (damager instanceof ThrownPotion) {
                // Mark as thrown potion if
                if (Skills.ALCHEMY.isEnabled() && plugin.configBoolean(Option.SOURCE_ENTITY_GIVE_ALCHEMY_ON_POTION_COMBAT)) {
                    return new Pair<>(player, EntityXpSource.EntityDamagers.THROWN_POTION);
                }
            }
            return new Pair<>(player, EntityXpSource.EntityDamagers.PROJECTILE);
        }
        return null;
    }

    @Nullable
    public Pair<EntityXpSource, Skill> getSource(LivingEntity entity, EntityXpSource.EntityDamagers eventDamager, EntityXpSource.EntityTriggers trigger) {
        Map<EntityXpSource, Skill> sources = plugin.getSkillManager().getSourcesOfType(EntityXpSource.class);
        sources = filterByTrigger(sources, trigger);

        for (Map.Entry<EntityXpSource, Skill> entry : sources.entrySet()) {
            EntityXpSource source = entry.getKey();
            Skill skill = entry.getValue();

            // Discard if entity type does not match
            String entityName = plugin.getPlatformUtil().convertEntityName(source.getEntity().toLowerCase(Locale.ROOT));
            if (!entityName.toUpperCase(Locale.ROOT).equals(entity.getType().toString())) {
                continue;
            }

            // Give Alchemy XP if potion is thrown with option give_xp_on_potion_combat
            if (eventDamager == EntityXpSource.EntityDamagers.THROWN_POTION) {
                return new Pair<>(source, Skills.ALCHEMY);
            }

            // Return if damager matches
            for (EntityXpSource.EntityDamagers sourceDamager : source.getDamagers()) {
                if (sourceDamager == eventDamager) {
                    return new Pair<>(source, skill);
                }
            }
        }
        return null;
    }

    private Map<EntityXpSource, Skill> filterByTrigger(Map<EntityXpSource, Skill> sources, EntityXpSource.EntityTriggers trigger) {
        Map<EntityXpSource, Skill> filtered = new HashMap<>();
        for (Map.Entry<EntityXpSource, Skill> entry : sources.entrySet()) {
            EntityXpSource source = entry.getKey();
            Skill skill = entry.getValue();
            // Check if trigger matches any of the source triggers
            for (EntityXpSource.EntityTriggers sourceTrigger : source.getTriggers()) {
                if (sourceTrigger == trigger) {
                    filtered.put(source, skill);
                    break;
                }
            }
        }
        return filtered;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMobSpawn(CreatureSpawnEvent event) {
        if (event.isCancelled()) return;
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER) {
            return;
        }
        if (!Skills.FIGHTING.isEnabled() && !Skills.ARCHERY.isEnabled()) {
            return;
        }
        // Don't mark if multiplier is default
        if (Skills.FIGHTING.optionDouble("spawner_multiplier", 1) == 1.0 && Skills.ARCHERY.optionDouble("spawner_multiplier", 1) == 1.0) {
            return;
        }
        LivingEntity entity = event.getEntity();
        PersistentDataContainer data = entity.getPersistentDataContainer();
        data.set(SPAWNER_MOB_KEY, PersistentDataType.INTEGER, 1);
    }

    private double getSpawnerMultiplier(Entity entity, Skill skill) {
        if (entity.getPersistentDataContainer().has(SPAWNER_MOB_KEY, PersistentDataType.INTEGER)) { // Is spawner mob
            return skill.optionDouble("spawner_multiplier", 1.0);
        } else {
            return 1.0;
        }
    }

    @Nullable
    private Player getBleedDamager(Entity entity) {
        PersistentDataContainer container = entity.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, FightingAbilities.BLEED_DAMAGER_KEY);
        if (container.has(key, PersistentDataType.STRING)) { // Handle damager from Bleed
            String uuidStr = container.get(key, PersistentDataType.STRING);
            if (uuidStr == null) return null;

            UUID uuid = UUID.fromString(uuidStr);
            return Bukkit.getPlayer(uuid);
        } else {
            return null;
        }
    }

    private double getDamageMultiplier(LivingEntity entity, EntityXpSource source, EntityDamageEvent event) {
        double damageDealt = Math.min(entity.getHealth(), event.getFinalDamage());
        if (source.scaleXpWithHealth()) {
            AttributeInstance healthAttribute = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (healthAttribute != null) {
                double maxHealth = healthAttribute.getValue();
                return damageDealt / maxHealth; // XP gain is damage/maxHealth * sourceXp
            }
        }
        return damageDealt;
    }

}
