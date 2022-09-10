package com.archyx.aureliumskills.skills.fighting;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.ability.Ability;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.leveler.SkillLeveler;
import com.archyx.aureliumskills.skills.Skills;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FightingLeveler extends SkillLeveler implements Listener {

	public FightingLeveler(@NotNull AureliumSkills plugin) {
		super(plugin, Ability.FIGHTER);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDeath(@NotNull EntityDeathEvent event) {
		if (OptionL.isEnabled(Skills.FIGHTING)) {
			if (OptionL.getBoolean(Option.FIGHTING_DAMAGE_BASED)) return;
			LivingEntity e = event.getEntity();
			if (e.getKiller() != null) {
				if (e.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
					@Nullable EntityDamageByEntityEvent ee = (EntityDamageByEntityEvent) e.getLastDamageCause();
					if (ee != null && ee.getDamager() instanceof Player) {
						EntityType type = e.getType();
						Player p = (Player) ee.getDamager();
						if (blockXpGainLocation(e.getLocation(), p)) return;
						if (blockXpGainPlayer(p)) return;
						if (e.equals(p)) return;
						double spawnerMultiplier = OptionL.getDouble(Option.FIGHTING_SPAWNER_MULTIPLIER);
						try {
							if (e.hasMetadata("aureliumskills_spawner_mob")) {
								plugin.getLeveler().addXp(p, Skills.FIGHTING, spawnerMultiplier * getXp(p, FightingSource.valueOf(type.toString())));
							} else {
								plugin.getLeveler().addXp(p, Skills.FIGHTING, getXp(p, FightingSource.valueOf(type.toString())));
							}
						} catch (IllegalArgumentException exception) {
							if (type.toString().equals("PIG_ZOMBIE")) {
								if (e.hasMetadata("aureliumskills_spawner_mob")) {
									plugin.getLeveler().addXp(p, Skills.FIGHTING, spawnerMultiplier * getXp(p, FightingSource.ZOMBIFIED_PIGLIN));
								} else {
									plugin.getLeveler().addXp(p, Skills.FIGHTING, getXp(p, FightingSource.ZOMBIFIED_PIGLIN));
								}
							}
						}
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityDamage(@NotNull EntityDamageByEntityEvent event) {
		// Damage based listener
		if (OptionL.isEnabled(Skills.FIGHTING)) {
			if (event.isCancelled()) return;
			if (!OptionL.getBoolean(Option.FIGHTING_DAMAGE_BASED)) return;
			if (event.getDamager() instanceof Player) {
				Player player = (Player) event.getDamager();
				if (event.getCause() != DamageCause.ENTITY_ATTACK && event.getCause() != DamageCause.ENTITY_SWEEP_ATTACK) return;
				if (event.getEntity() instanceof LivingEntity) {
					LivingEntity entity = (LivingEntity) event.getEntity();
					if (blockXpGainLocation(entity.getLocation(), player)) return;
					EntityType type = entity.getType();
					if (blockXpGainPlayer(player)) return;
					if (entity.equals(player)) return;
					double health = entity.getHealth();
					double damage = Math.min(health, event.getFinalDamage());
					// Apply spawner multiplier
					if (entity.hasMetadata("aureliumskills_spawner_mob")) {
						double spawnerMultiplier = OptionL.getDouble(Option.FIGHTING_SPAWNER_MULTIPLIER);
						damage *= spawnerMultiplier;
					}
					try {
						plugin.getLeveler().addXp(player, Skills.FIGHTING, damage * getXp(player, FightingSource.valueOf(type.toString())));
					} catch (IllegalArgumentException e) {
						if (type.toString().equals("PIG_ZOMBIE")) {
							plugin.getLeveler().addXp(player, Skills.FIGHTING, damage * getXp(player, FightingSource.ZOMBIFIED_PIGLIN));
						}
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onMobSpawn(@NotNull CreatureSpawnEvent event) {
		if (event.isCancelled()) return;
		if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
			if (OptionL.isEnabled(Skills.FIGHTING) || OptionL.isEnabled(Skills.ARCHERY)) {
				if (OptionL.getDouble(Option.ARCHERY_SPAWNER_MULTIPLIER) < 1.0 || OptionL.getDouble(Option.FIGHTING_SPAWNER_MULTIPLIER) < 1.0) {
					LivingEntity entity = event.getEntity();
					entity.setMetadata("aureliumskills_spawner_mob", new FixedMetadataValue(plugin, true));
				}
			}
		}
	}

}
